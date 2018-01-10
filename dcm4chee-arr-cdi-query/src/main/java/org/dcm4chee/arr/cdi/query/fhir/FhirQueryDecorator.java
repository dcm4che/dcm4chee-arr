/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.arr.cdi.query.fhir;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.AbstractAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.SearchParamParseException;
import org.dcm4chee.arr.cdi.query.utils.QueryUtils;
import org.dcm4chee.arr.cdi.query.utils.QueryUtils.TemporalPrecision;
import org.dcm4chee.arr.entities.QCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;

/**
 * 
 *
 */
public class FhirQueryDecorator extends AbstractAuditRecordQueryDecorator
{
	
	private static final Logger LOG = LoggerFactory.getLogger(FhirQueryDecorator.class);
	
	/**
	 * The two date parameters are recommended in every search by the Audit Consumer and shall be 
	 * supported by the Audit Record Repository in order to avoid overload of matching AuditEvent 
	 * resources in the Response message. One or two date parameters shall be present.
	 */
	private DateRangeParam dateRange;

	/**
	 * Identifier of the network access point (NetworkAccessPointID) of the 
	 * user device that created the audit record (i.e this could be a device id,
	 * IP address, or some other identifier associated with a device)
	 */
	private StringOrListParam addresses;
	
	/**
	 * This parameter specifies the identifier of the patient involved in the event
	 * as a participant. The value of this parameter can contain the namespace URI
	 * (that represents the assigning authority for the identifier) and the identifier.
	 */
	private TokenOrListParam patientIdentifiers;
	
	/**
	 * This parameter specifies unique identifier for the object. 
	 * The parameter value should be identified in accordance to the object type.
	 * 
	 */
	private TokenOrListParam entityIdentities;
	
	/**
	 * This parameter specifies the type of the object (e.g., Person, System Object, etc.)
	 * The parameter value shall contain the namespace URI 
	 * http://hl7.org/fhir/DSTU2/valueset-object-type.html defined by FHIR and a
	 * coded value. See http://hl7.org/fhir/DSTU2/valueset-object-type.html
	 * for available codes. 
	 */
	private TokenOrListParam entityTypes;
	
	/**
	 * This parameter specifies the role played by the object (e.g., Report, 
	 * Location, Query, etc.). The parameter value shall contain the namespace
	 * URI http://hl7.org/fhir/DSTU2/object-role defined by FHIR and a coded value.
	 * See http://hl7.org/fhir/DSTU2/object-role for available codes. 
	 */
	private TokenOrListParam entityRoles;

	/**
	 * This parameter identifies the source of the audit event (DICOM AuditSourceID).
	 */
	private TokenOrListParam sources;
	
	/**
	 * This parameter represents the identifier of the specific type of event audited.
	 * The parameter value shall contain namespace URI
	 * http://nema.org/dicom/dicm and a coded value. Codes available are defined
	 * by DICOM and IHE (seeITI TF-1: Table 3.20.6-1: Audit Record trigger events)
	 */
	private TokenOrListParam types;
	
	/**
	 * This parameter identifies the user that participated in the event that
	 * originated the audit record.
	 */
	private TokenOrListParam users;
	
	/**
	 * This parameter identifies the specific IHE transaction that originated
	 * the audit record. The parameter value shall contain the namespace URI
	 * urn:ihe:event-type-code. Each IHE transaction specifies an associated
	 * audit record that defines a specific code identifying the transaction 
	 * itself, and assigns this code to the EventTypeCode element within
	 * [ITI-20] audit record.
	 */
	private TokenOrListParam subtypes;
	
	/**
	 * This parameter represents whether the event succeeded or failed. The
	 * parameter value shall contain the namespace URI
	 * http://hl7.org/fhir/DSTU2/audit-event-outcome and a code taken from
	 * the related value set. Codes available can be found at
	 * http://hl7.org/fhir/DSTU2/audit-event-outcome.
	 */
	private TokenOrListParam outcomes;
	
	public FhirQueryDecorator()
	{
	}
	
	public FhirQueryDecorator setDates( DateRangeParam dateRange )
	{
		this.dateRange = dateRange;
		return this;
	}
	
	public FhirQueryDecorator setAddresses( StringOrListParam addresses )
	{
		this.addresses = addresses;
		return this;
	}
	
	public FhirQueryDecorator setObjectIdentities( TokenOrListParam objectIdentities )
	{
		this.entityIdentities = objectIdentities;
		return this;
	}
	
	public FhirQueryDecorator setObjectTypes( TokenOrListParam objectTypes )
	{
		this.entityTypes = objectTypes;
		return this;
	}
	
	public FhirQueryDecorator setOutcomes( TokenOrListParam outcomes )
	{
		this.outcomes = outcomes;
		return this;
	}
	
	public FhirQueryDecorator setPatientIdentifiers( TokenOrListParam identifiers )
	{
		this.patientIdentifiers = identifiers;
		return this;
	}
	
	public FhirQueryDecorator setRoles( TokenOrListParam roles )
	{
		this.entityRoles = roles;
		return this;
	}
	
	public FhirQueryDecorator setSources( TokenOrListParam sources )
	{
		this.sources = sources;
		return this;
	}
	
	public FhirQueryDecorator setSubTypes( TokenOrListParam subtypes )
	{
		this.subtypes = subtypes;
		return this;
	}
	
	public FhirQueryDecorator setTypes( TokenOrListParam types )
	{
		this.types = types;
		return this;
	}
	
	public FhirQueryDecorator setUsers( TokenOrListParam users )
	{
		this.users = users;
		return this;
	}	
	
	public FhirQueryDecorator setMaxResults( Long maxResults )
	{
		super.setMaxResults( maxResults );
		return this;
	}
	
	@Override
	public List<Predicate> getAuditRecordPredicates()
	{
		List<Predicate> predicates = new ArrayList<>(8);
				
		// date range
		predicates = addIgnoreNull( predicates, toExpression( ar.eventDateTime, dateRange ) );

		// subtypes
		predicates = addIgnoreNull( predicates, toExpression( ar.eventType, subtypes ) );
		
		// types
		predicates = addIgnoreNull( predicates, toExpression( ar.eventID, types ) );
		
		// outcomes
		predicates = addIgnoreNull( predicates, toExpression( ar.eventOutcome, outcomes ) );
				
		// sources
		predicates = addIgnoreNull( predicates, toExpression( ar.sourceID, sources ) );
						
		return predicates;
	}
	
	@Override
	public List<Predicate> getActiveParticipantPredicates()
	{
		List<Predicate> predicates = null;
				
		// addresses
		predicates = addIgnoreNull( predicates, toExpression( ap.networkAccessPointID, addresses ) );
		
		// patient identifiers (as specified in IHE ITI Add Restful Query to ATNA, Rev. 2.2 Trial Implementation, line 592)
		//predicates = addIgnoreNull( predicates, toExpression( ap.userID, patientIdentifiers ) );

		// users
		predicates = addIgnoreNull( predicates, toExpression( ap.userID, users ) );
				
		return predicates;
	}
	
	@Override
	public List<Predicate> getParticipantObjectPredicates()
	{
		List<Predicate> predicates = null;
					
		BooleanExpression patE = toExpression( po.objectID, patientIdentifiers );
		if ( patE != null )
		{
			predicates = addIgnoreNull( predicates,
					patE.andAnyOf(
						po.objectIDType.value.eq("1"),
						po.objectRole.eq(1)
					)
				);
		}

		// entity id
		predicates = addIgnoreNull( predicates, toExpression( po.objectID, entityIdentities ) );
		
		// entity types
		predicates = addIgnoreNull( predicates, toExpression( po.objectIDType, entityTypes ) );
		
		// entity roles
		predicates = addIgnoreNull( predicates, toExpression( po.objectRole, entityRoles ) );
		
		return predicates;
	}
	
	@Override
	public List<Predicate> getAllPredicates()
	{
		List<Predicate> predicates = new ArrayList<>();
		
		// add audit-record precidates if present
		List<Predicate> arPredicates = getAuditRecordPredicates();
		if ( arPredicates != null )
		{
			predicates.addAll( arPredicates );
		}

		// subquery for matching active participants
		List<Predicate> apPredicates = getActiveParticipantPredicates();
		if ( !emptyOrNull( apPredicates ) )
		{
			apPredicates.add(0, ap.auditRecord.pk.eq(ar.pk) );
			predicates = addIgnoreNull( predicates, new JPASubQuery()
				.from( ap )
				.join( ap.auditRecord )
				.leftJoin( ap.roleID )
				.where( apPredicates.toArray(new Predicate[apPredicates.size()] ) )
				.exists() );
		}
		
		// subquery for matching participant objects
		List<Predicate> poPredicates = getParticipantObjectPredicates();
		if ( !emptyOrNull( poPredicates ) )
		{
			poPredicates.add(0, po.auditRecord.pk.eq(ar.pk) );
			predicates = addIgnoreNull( predicates, new JPASubQuery()
				.from( po )
				.join( po.auditRecord )
				.leftJoin( po.objectIDType )
				.where( poPredicates.toArray(new Predicate[poPredicates.size()] ) )
				.exists() );
		}
		
		return predicates;
	}

	private static BooleanExpression toExpression( DateTimePath<Date> path, DateParam date )
	{
		Boolean missing = date.getMissing();
		if ( missing != null )
		{
			return missing ? path.isNull() : path.isNotNull();
		}
		
		ParamPrefixEnum prefix = date.getPrefix();
		if ( prefix == null )
		{
			prefix = ParamPrefixEnum.EQUAL;
		}
		
		TemporalPrecision precision = toTemporalPrecision( date.getPrecision() );
		Date upperBound = QueryUtils.toUpperBoundDate( date.getValue(), precision );
		Date lowerBound = QueryUtils.toLowerBoundDate( date.getValue(), precision );
		
		switch( prefix )
		{
			case APPROXIMATE: /* fall through */
			case EQUAL: return path.eq( date.getValue() ); 
			case NOT_EQUAL: return path.ne( date.getValue() );
			
			case STARTS_AFTER: /* fall through */
			case GREATERTHAN: return path.gt( upperBound );
			case GREATERTHAN_OR_EQUALS: return path.goe( lowerBound );
	
			case ENDS_BEFORE: /* fall through */
			case LESSTHAN: return path.lt( lowerBound );
			case LESSTHAN_OR_EQUALS: return path.loe( upperBound );
		}
		
		return null;
	}
	
	private BooleanExpression toExpression( DateTimePath<Date> path, DateRangeParam param )
	{
		if ( param != null )
		{
			DateParam start = param.getLowerBound();
			DateParam end = param.getUpperBound();

			if ( start != null )
			{
				ParamPrefixEnum prefix = start.getPrefix();
				if (prefix == null || ( end != null && prefix == ParamPrefixEnum.EQUAL ) )
				{
					start.setPrefix( ParamPrefixEnum.GREATERTHAN_OR_EQUALS );
				}
			}
			
			if ( end != null )
			{
				ParamPrefixEnum prefix = end.getPrefix();
				if ( prefix == null || ( start != null && prefix == ParamPrefixEnum.EQUAL) )
				{
					end.setPrefix( ParamPrefixEnum.LESSTHAN_OR_EQUALS );
				}
			}
			
			if ( start!=null && end!=null && !Objects.equals( start.getValue(), end.getValue() ) )
			{
				return toExpression( path, start ).and( toExpression( path, end ) );
			}
			else if ( start !=  null )
			{
				return toExpression( path, start );
			}
			else if ( end != null )
			{
				return toExpression( path, end );
			}
		}

		return null;
	}
	
	private BooleanExpression toExpression( StringPath path, StringOrListParam param )
	{
		if ( param != null )
		{
			List<StringParam> params = param.getValuesAsQueryTokens();
			if ( params != null )
			{
				BooleanExpression e = null;
				for ( StringParam p : params )
				{
					e = ( e == null ) ? e = toExpression( path, p ) :
						e.or( toExpression( path, p ) );
				}
				return e;
			}
		}
		return null;
	}
	
	private BooleanExpression toExpression( StringPath path, TokenOrListParam param )
	{
		if ( param != null )
		{
			List<TokenParam> params = param.getValuesAsQueryTokens();
			if ( params != null )
			{
				BooleanExpression e = null;
				for ( TokenParam p : params )
				{
					if ( e == null )
					{
						 e = toExpression( path, p );
					}
					else
					{
						e = p.getModifier() == TokenParamModifier.NOT ?
								e.and( toExpression( path, p ) ) :
									e.or( toExpression(path, p) );
					}
				}
				return e;
			}
		}
		return null;
	}
	
	private BooleanExpression toExpression( NumberPath<Integer> path, TokenOrListParam param )
	{
		if ( param != null )
		{
			
			List<TokenParam> params = param.getValuesAsQueryTokens();
			if ( params != null )
			{
				BooleanExpression e = null;
				for ( TokenParam p : params )
				{
					if ( e == null )
					{
						 e = toExpression( path, p );
					}
					else
					{
						e = p.getModifier() == TokenParamModifier.NOT ?
								e.and( toExpression( path, p ) ) :
									e.or( toExpression(path, p) );
					}
				}
				return e;
			}
		}
		return null;
	}
	
	private BooleanExpression toExpression( QCode path, TokenOrListParam param )
	{
		if ( param != null )
		{
			List<TokenParam> params = param.getValuesAsQueryTokens();
			if ( params != null )
			{
				BooleanExpression e = null;
				for ( TokenParam p : params )
				{
					if ( e == null )
					{
						 e = toExpression( path, p );
					}
					else
					{
						e = p.getModifier() == TokenParamModifier.NOT ?
								e.and( toExpression( path, p ) ) :
									e.or( toExpression(path, p) );
					}
				}
				return e;
			}
		}
		return null;
	}
	
	
	private BooleanExpression toExpression( StringPath path, StringParam param )
	{
		Boolean missing = param.getMissing();
		if ( missing != null )
		{
			return missing ? path.isNull().or(path.isEmpty()) :
				path.isNotNull().and( path.isNotEmpty() );
		}
		else if (param.isContains() )
		{
			return path.containsIgnoreCase( param.getValue() );
		}
		else if ( param.isExact() )
		{
			return path.like( param.getValue() );
		}
		else
		{
			return path.endsWithIgnoreCase( param.getValue() );
		}
	}
	
	private BooleanExpression toExpression( StringPath path, TokenParam param )
	{
		Boolean missing = param.getMissing();
		if ( missing != null )
		{
			return missing ? path.isNull().or(path.isEmpty()) :
				path.isNotNull().and( path.isNotEmpty() );
		}
		else
		{
			BooleanExpression e = null;
			
			TokenParamModifier modifier = param.getModifier();
			if ( modifier == null || modifier == TokenParamModifier.NOT )
			{
				e = path.equalsIgnoreCase(
						toSearchString( param, true, true ) );
			}
			
			if ( modifier != null )
			{
				switch( modifier )
				{
				case NOT: e = e.not(); break;
				case TEXT: e = path.equalsIgnoreCase( param.getValue() ); break;
				// not supported
				case IN:
				case NOT_IN:
				case ABOVE:
				case BELOW:
				default:
					return null;
				}
			}
			
			return e;
		}
	}
	
	private BooleanExpression toExpression( QCode path, TokenParam param )
	{
		Boolean missing = param.getMissing();
		if ( missing != null )
		{
			return missing ? path.isNull() : path.isNotNull();
		}
		else
		{
			BooleanExpression e = null;
			
			TokenParamModifier modifier = param.getModifier();
			if ( modifier == null || modifier == TokenParamModifier.NOT )
			{
				e = path.value.equalsIgnoreCase( param.getValue() );
				if ( param.getSystem() != null )
				{
					e = e.and( path.designator.equalsIgnoreCase( param.getSystem() ) );
				}
			}
			
			if ( modifier != null )
			{
				switch( modifier )
				{
				case NOT: e = e.not(); break;
				case TEXT: e = path.value.equalsIgnoreCase( param.getValue() ); break;
				case IN: e = path.designator.equalsIgnoreCase( param.getValue() ); break;
				case NOT_IN: e = path.designator.notEqualsIgnoreCase( param.getValue() ); break;
				case ABOVE: // TODO: where's the difference?
				case BELOW: 
					e = path.designator.equalsIgnoreCase( param.getSystem() );
					if ( param.getValue() != null )
					{
						e = e.and( path.value.equalsIgnoreCase(param.getValue() ) );
					}
					return e;
				}
			}
			
			return e;
		}
	}
	
	private static BooleanExpression toExpression( NumberPath<Integer> path, TokenParam param )
	{
		Boolean missing = param.getMissing();
		if ( missing != null )
		{
			return missing ? path.isNull() : path.isNotNull();
		}
		else
		{
			BooleanExpression e = null;
			
			TokenParamModifier modifier = param.getModifier();
			if ( modifier == null || modifier == TokenParamModifier.NOT )
			{
				try
				{
					e = path.eq( Integer.valueOf( param.getValue() ) );
				}
				catch ( NumberFormatException ex )
				{
					LOG.warn( "Unable to process FHIR query parameter: Bad value (integer value expected)");
					
					throw new SearchParamParseException( "Unable to process FHIR query parameter: Bad value (integer value expected)" );
				}
			}
			
			if ( modifier != null )
			{
				switch( modifier )
				{
				case NOT: e = e.not(); break;
				// not supported
				case TEXT:
				case IN:
				case NOT_IN:
				case ABOVE:
				case BELOW:
				default:
					return null;
				}
			}
			
			return e;
		}
	}
	
	private static TemporalPrecision toTemporalPrecision( TemporalPrecisionEnum e )
	{
		if ( e != null )
		{
			switch( e )
			{
			case YEAR: return TemporalPrecision.YEAR;
			case MONTH: return TemporalPrecision.MONTH;
			case DAY: return TemporalPrecision.DAY;
			case MINUTE: return TemporalPrecision.MINUTE;
			case SECOND: return TemporalPrecision.SECOND;
			case MILLI: return TemporalPrecision.MILLI;
			}
		}
		
		return null;
	}
	
	private static String toSearchString( TokenParam token, boolean includeSystem, boolean includeValue )
	{
		String system = token.getSystem();
		String value = token.getValue();
		
		StringBuilder s = new StringBuilder();
		if ( system!=null && includeSystem )
		{
			s.append( system );
		}
		if ( value != null && includeValue )
		{
			if ( s.length() > 0 )
			{
				s.append("|");
			}
			s.append( value );
		}
		return s.toString();
	}
		
}
