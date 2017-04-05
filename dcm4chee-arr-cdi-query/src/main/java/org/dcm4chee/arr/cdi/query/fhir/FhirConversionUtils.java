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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dcm4chee.arr.entities.ActiveParticipant;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.Code;
import org.dcm4chee.arr.entities.ParticipantObject;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.Event;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.ObjectElement;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.Participant;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.ParticipantNetwork;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent.Source;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Bundle.EntrySearch;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventActionEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventObjectLifecycleEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventObjectRoleEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventObjectTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventOutcomeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventParticipantNetworkTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.AuditEventSourceTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.IdentifierTypeCodesEnum;
import ca.uhn.fhir.model.dstu2.valueset.SearchEntryModeEnum;
import ca.uhn.fhir.model.primitive.InstantDt;

/**
 * 
 *
 */
public class FhirConversionUtils 
{

	public static Bundle toBundle( BundleTypeEnum type, List<AuditRecord> records,
			String contextURL, final boolean lenient )
		throws FhirConversionException
	{
		Bundle bundle = new Bundle();
		bundle.setType( type );
		
		// set number of search results
		if ( type == BundleTypeEnum.SEARCH_RESULTS )
		{
			bundle.setTotal( records.size() );
		}
		
		// convert audit record and add to bundle
		if ( records != null )
		{
			for( AuditRecord record : records )
			{
				bundle.addEntry( toBundleEntry( record, contextURL, lenient ) );
			}
		}
	
		return bundle;
	}
	
	private static Entry toBundleEntry( AuditRecord record, String contextURL, boolean lenient )
		throws FhirConversionException
	{
		Entry entry = new Entry();

		EntrySearch search = new EntrySearch();
		search.setMode( SearchEntryModeEnum.MATCH );
		
		entry.setSearch( search );
		entry.setResource( toAuditEvent( record, lenient ) );
		
		long pk = record.getPk();
		if ( pk>=0 && contextURL != null )
		{
			String fullUrl = new StringBuilder( StringUtils.appendIfMissing( contextURL, "/", "/" ) )
				.append( "view/xmlview" ) //$NON-NLS-1$
				.append( "?pk=" ) //$NON-NLS-1$
				.append( pk )
				.toString();
			
			entry.setFullUrl( fullUrl );
		}
		
		return entry;
	}

	public static AuditEvent toAuditEvent( AuditRecord record, boolean lenient )
		throws FhirConversionException
	{
		AuditEvent auditEvent = new AuditEvent();
		
		AuditEventOutcomeEnum outcome = toEnum( record.getEventOutcome(), AuditEventOutcomeEnum.class, lenient );
		
		// event
		Event event = new Event();
		event.setType( toCoding( record.getEventID() ) );
		event.setSubtype( toNullableList( toCoding( record.getEventType() ) ) );
		event.setAction( toEnum( record.getEventAction(), AuditEventActionEnum.class, lenient ) );
		event.setDateTime( toInstant( record.getEventDateTime() ) );
		event.setOutcome( outcome );
		event.setOutcomeDesc( toAuditEventOutcomeDescription( outcome ) );
		auditEvent.setEvent( event );
		
		// source
		Source source = new Source();
		source.setSite( record.getEnterpriseSiteID() );
		source.setIdentifier( toIdentifier( record.getSourceID(), lenient ) );
		source.setType( toNullableList( 
				toEnumCoding( record.getSourceType(), 
						AuditEventSourceTypeEnum.class, lenient ) ) );
		auditEvent.setSource( source );
		
		// participants
		Collection<ActiveParticipant> activeParticipants = record.getActiveParticipants();
		if ( activeParticipants != null )
		{
			for( ActiveParticipant ap : activeParticipants )
			{
				auditEvent.addParticipant( toParticipant( ap, lenient ) );
			}
		}
		
		// objects
		Collection<ParticipantObject> objects = record.getParticipantObjects();
		if ( activeParticipants != null )
		{
			for( ParticipantObject o : objects )
			{
				auditEvent.addObject( toObject( o, lenient ) );
			}
		}
		
		return auditEvent;
	}
	
	private static Participant toParticipant( ActiveParticipant ap, boolean lenient )
		throws FhirConversionException
	{
		Participant p = new Participant();
		p.setRole( toNullableList( toCodeableConcept( ap.getRoleID() ) ) );
		p.setUserId( toIdentifier( ap.getUserID(), lenient ) );
		p.setAltId( ap.getAlternativeUserID() );
		p.setName( ap.getUserName() );
		p.setRequestor( ap.getUserIsRequestor() );
		
		ParticipantNetwork network = new ParticipantNetwork();
		network.setAddress( ap.getNetworkAccessPointID() );
		network.setType( toEnum( ap.getNetworkAccessPointType(), AuditEventParticipantNetworkTypeEnum.class, lenient ) );
		p.setNetwork( network );
		
		return p;
	}
	
	private static ObjectElement toObject( ParticipantObject po, boolean lenient )
		throws FhirConversionException
	{
		Code idType = po.getObjectIDType();
		
		ObjectElement o = new ObjectElement();
		o.setIdentifier( idType != null ?
				toIdentifier( po.getObjectID(), idType.getValue(), idType.getDesignator(), lenient ) :
					toIdentifier( po.getObjectID(), lenient ) );
		o.setType( toEnumCoding( po.getObjectType(), AuditEventObjectTypeEnum.class, lenient ) );
		o.setRole( toEnumCoding( po.getObjectRole(), AuditEventObjectRoleEnum.class, lenient ) );
		o.setLifecycle( toEnumCoding( po.getDataLifeCycle(), AuditEventObjectLifecycleEnum.class, lenient ) );
		o.setSecurityLabel( toNullableList( toCoding( null, po.getObjectSensitivity(), null ) ) );
		o.setName( po.getObjectName() );
		
		return o;
	}
	
	private static InstantDt toInstant( Date date )
	{
		return new InstantDt( date );
	}
	
	private static IdentifierDt toIdentifier( String id, boolean lenient )
			throws FhirConversionException
	{
		return toIdentifier( id, null, null, lenient );
	}
	
	private static IdentifierDt toIdentifier( String id, String typeId, String typeSystem, boolean lenient )
		throws FhirConversionException
	{
		if ( id != null )
		{
			// TODO: parse system here???
			IdentifierDt identifier = new IdentifierDt( null, id );
			
			if ( typeId != null )
			{
				identifier.setType( toEnum( typeId, typeSystem, IdentifierTypeCodesEnum.class, lenient ) );
			}
			return identifier;
		}
		return null;
	}
	
	private static CodingDt toCoding( Code code )
	{
		if ( code != null )
		{
			return toCoding( code.getDesignator(), code.getValue(), code.getMeaning() );
		}
		return null;
	}
	
	private static CodingDt toCoding( String system, String value, String display )
	{
		if ( value != null )
		{
			CodingDt coding = new CodingDt( system, value );
			if ( display != null )
			{
				coding.setDisplay( display );
			}
			return coding;
		}
		return null;
	}
	
	private static CodeableConceptDt toCodeableConcept( Code code )
	{
		if ( code != null )
		{
			CodeableConceptDt c = new CodeableConceptDt( code.getDesignator(), code.getValue() );
			c.setText( code.getMeaning() );
			return c;
		}
		return null;
	}
	
	private static String toAuditEventOutcomeDescription( AuditEventOutcomeEnum outcome )
	{
		if ( outcome != null )
		{
			switch( outcome )
			{
			case SUCCESS: return "Success"; //$NON-NLS-1$
			case MINOR_FAILURE: return "Minor failure"; //$NON-NLS-1$
			case SERIOUS_FAILURE: return "Serious failure"; //$NON-NLS-1$
			case MAJOR_FAILURE: return "Major failure"; //$NON-NLS-1$
			}
		}
		return null;
	}
	
	private static Class<?>[] getClasses( Object...objects )
	{
		if ( objects != null && objects.length > 0 )
		{
			Class<?>[] classes = new Class[objects.length];
			for (int i=0; i<objects.length; i++)
			{
				classes[i] = objects[i].getClass();
			}
			return classes;
		}
		return null;
	}
	
	private static Object invokeMethod( Class<?> clazz, String methodName, Object instance, Object...args )
	{
		try
		{
			Method method = null;
			if ( args != null )
			{
				method = clazz.getMethod(methodName, getClasses( args ) );
			}
			else
			{
				method = clazz.getMethod(methodName);
			}
			
			if ( method != null )
			{
				return method.invoke(instance, args);
			}
		}
		catch ( Exception e )
		{
			// ignore
		}
		return null;
	}
	
	private static <E extends Enum<?>> CodingDt toEnumCoding( int value, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		return toEnumCoding( value, null, clazz, lenient );
	}
	
	private static <E extends Enum<?>> CodingDt toEnumCoding( int value, String system, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		E instance = toEnum( value, system, clazz, lenient );
		if ( instance != null )
		{
			try
			{
				return new CodingDt(
					(String) invokeMethod( clazz, "getSystem", instance ),
					(String) invokeMethod( clazz, "getCode", instance )
				);
			}
			catch ( Exception e )
			{
				throw new FhirConversionException( "Unable to convert FHIR enum to coding", e ); //$NON-NLS-1$
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static <E extends Enum> E toEnum( int value, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		return toEnum( Integer.toString(value), null, clazz, lenient );
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static <E extends Enum> E toEnum( int value, String system, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		return toEnum( Integer.toString(value), system, clazz, lenient );
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static <E extends Enum> E toEnum( String value, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		return toEnum( value, null, clazz, lenient );
	}
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E extends Enum> E toEnum( String value, String system, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		try
		{
			if ( value != null )
			{
				E instance = (E) invokeMethod( clazz, "forCode", null, value );
				if ( instance == null )
				{
					instance = (E) Enum.valueOf(clazz, value );
				}
				
				// check if the explicitly given system matches that
				// of the enum
				if ( instance != null && system != null )
				{
					String s = (String) invokeMethod( clazz, "getCode", instance );
					if ( s != null && !s.equals(system) )
					{
						return null;
					}
				}
				
				return instance;
			}
		}
		catch ( Exception e )
		{
			if ( !lenient )
			{
				throw new FhirConversionException( "Unable to convert to convert to FHIR AuditEvent enum " + clazz.getName(), e ); //$NON-NLS-1$
			}
		}
		return null;
	}
 	
	
	private static <T> List<T> toNullableList( T item )
	{
		if ( item != null )
		{
			return Collections.singletonList( item );
		}
		return null;
	}
	
	@SuppressWarnings("serial")
	public static class FhirConversionException extends Exception
	{
		public FhirConversionException( String msg )
		{
			this( msg, null );
		}
		
		public FhirConversionException( String msg, Throwable cause )
		{
			super( msg, cause );
		}
	}
}
