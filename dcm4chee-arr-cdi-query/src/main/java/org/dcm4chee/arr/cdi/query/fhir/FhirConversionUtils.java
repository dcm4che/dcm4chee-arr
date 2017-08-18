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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dcm4chee.arr.entities.ActiveParticipant;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.Code;
import org.dcm4chee.arr.entities.ParticipantObject;
import org.hl7.fhir.dstu3.model.AuditEvent;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventAgentNetworkComponent;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventAgentNetworkType;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventEntityComponent;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventOutcome;
import org.hl7.fhir.dstu3.model.AuditEvent.AuditEventSourceComponent;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntrySearchComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.SearchEntryMode;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.codesystems.AuditEntityType;
import org.hl7.fhir.dstu3.model.codesystems.AuditSourceType;
import org.hl7.fhir.dstu3.model.codesystems.DicomAuditLifecycle;
import org.hl7.fhir.dstu3.model.codesystems.ObjectRole;

import com.mysema.query.SearchResults;

/**
 * 
 *
 */
public class FhirConversionUtils 
{
	
	public static Bundle toBundle( BundleType type, 
			SearchResults<AuditRecord> results,
			String contextURL, final boolean lenient )
		throws FhirConversionException
	{
		Bundle bundle = new Bundle();
		bundle.setType(type);

		// set number of search results
		if ( type == BundleType.SEARCHSET )
		{
			bundle.setTotal( (int) results.getTotal() );
		}

		// convert audit record and add to bundle
		List<AuditRecord> records = results.getResults();
		if ( records != null )
		{
			for( AuditRecord record : records )
			{
				bundle.addEntry( toBundleEntry( record, contextURL, lenient ) );
			}
		}
	
		return bundle;
	}

	private static BundleEntryComponent toBundleEntry( AuditRecord record, String contextURL, boolean lenient )
		throws FhirConversionException
	{
		BundleEntryComponent entry = new BundleEntryComponent();

		BundleEntrySearchComponent search = new BundleEntrySearchComponent();
		search.setMode( SearchEntryMode.MATCH );
		
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
		AuditEvent event = new AuditEvent();
		
		AuditEventOutcome outcome = toEnum( record.getEventOutcome(), AuditEventOutcome.class, lenient );
		
		// event
		event.setType( toCoding( record.getEventID() ) );
		event.setSubtype( toNullableList( toCoding( record.getEventType() ) ) );
		event.setAction( toEnum( record.getEventAction(), AuditEventAction.class, lenient ) );
		event.setRecorded( record.getEventDateTime() );
		event.setOutcome( outcome );
		event.setOutcomeDesc( toAuditEventOutcomeDescription( outcome ) );

		// source
		AuditEventSourceComponent source = new AuditEventSourceComponent();
		source.setSite( record.getEnterpriseSiteID() );
		source.setIdentifier( toIdentifier( record.getSourceID(), lenient ) );
		source.setType( toNullableList( 
				toEnumCoding( record.getSourceType(), 
						AuditSourceType.class, lenient ) ) );
		event.setSource( source );
		
		// participants
		Collection<ActiveParticipant> activeParticipants = record.getActiveParticipants();
		if ( activeParticipants != null )
		{
			for( ActiveParticipant ap : activeParticipants )
			{
				event.addAgent( toAgent( ap, lenient ) );
			}
		}
		
		// objects
		Collection<ParticipantObject> objects = record.getParticipantObjects();
		if ( activeParticipants != null )
		{
			for( ParticipantObject o : objects )
			{
				event.addEntity( toObject( o, lenient ) );
			}
		}
		
		return event;
	}
	
	private static AuditEventAgentComponent toAgent( ActiveParticipant ap, boolean lenient )
		throws FhirConversionException
	{
		AuditEventAgentComponent agent = new AuditEventAgentComponent();
		agent.setRole( toNullableList( toCodeableConcept( ap.getRoleID() ) ) );
		agent.setUserId( toIdentifier( ap.getUserID(), lenient ) );
		agent.setAltId( ap.getAlternativeUserID() );
		agent.setName( ap.getUserName() );
		agent.setRequestor( ap.getUserIsRequestor() );
		
		AuditEventAgentNetworkComponent network = new AuditEventAgentNetworkComponent();
		network.setAddress( ap.getNetworkAccessPointID() );
		network.setType( toEnum( ap.getNetworkAccessPointType(), AuditEventAgentNetworkType.class, lenient ) );
		agent.setNetwork( network );
		
		return agent;
	}
	
	private static AuditEventEntityComponent toObject( ParticipantObject po, boolean lenient )
		throws FhirConversionException
	{
		Code idType = po.getObjectIDType();
		
		AuditEventEntityComponent entity = new AuditEventEntityComponent();
		entity.setIdentifier( idType != null ?
				toIdentifier( po.getObjectID(), idType.getValue(), idType.getDesignator(), lenient ) :
					toIdentifier( po.getObjectID(), lenient ) );
		entity.setType( toEnumCoding( po.getObjectType(), AuditEntityType.class, lenient ) );
		entity.setRole( toEnumCoding( po.getObjectRole(), ObjectRole.class, lenient ) );
		entity.setLifecycle( toEnumCoding( po.getDataLifeCycle(), DicomAuditLifecycle.class, lenient ) );
		entity.setSecurityLabel( toNullableList( toCoding( null, po.getObjectSensitivity(), null ) ) );
		entity.setName( po.getObjectName() );
		
		return entity;
	}
	
	private static Identifier toIdentifier( String id, boolean lenient )
			throws FhirConversionException
	{
		return toIdentifier( id, null, null, lenient );
	}
	
	private static Identifier toIdentifier( String id, String typeId, String typeSystem, boolean lenient )
		throws FhirConversionException
	{
		if ( id != null )
		{
			// TODO: parse system here???
			Identifier identifier = new Identifier();
			identifier.setValue( id );
			
			if ( typeId != null )
			{
				identifier.setType( toCodeableConcept( typeSystem, typeId, null) );
			}
			return identifier;
		}
		return null;
	}
	
	private static Coding toCoding( Code code )
	{
		if ( code != null )
		{
			return toCoding( code.getDesignator(), code.getValue(), code.getMeaning() );
		}
		return null;
	}
	
	private static Coding toCoding( String system, String value, String display )
	{
		if ( value != null )
		{
			return new Coding( system, value, display );
		}
		return null;
	}
	
	private static CodeableConcept toCodeableConcept( Code code )
	{
		if ( code != null )
		{
			return toCodeableConcept( 
					code.getDesignator(), code.getValue(), code.getMeaning() );
		}
		return null;
	}
	
	private static CodeableConcept toCodeableConcept( String system, String value, String meaning )
	{
		return new CodeableConcept().addCoding( 
					new Coding( system, value, meaning ) );
	}
	
	private static String toAuditEventOutcomeDescription( AuditEventOutcome outcome )
	{
		if ( outcome != null )
		{
			switch( outcome )
			{
			case _0: return "Success"; //$NON-NLS-1$
			case _4: return "Minor failure"; //$NON-NLS-1$
			case _8: return "Serious failure"; //$NON-NLS-1$
			case _12: return "Major failure"; //$NON-NLS-1$
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
	
	private static <E extends Enum<?>> Coding toEnumCoding( int value, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		return toEnumCoding( value, null, clazz, lenient );
	}
	
	private static <E extends Enum<?>> Coding toEnumCoding( int value, String system, Class<E> clazz, boolean lenient )
		throws FhirConversionException
	{
		E instance = toEnum( value, system, clazz, lenient );
		if ( instance != null )
		{
			try
			{
				return new Coding(
					(String) invokeMethod( clazz, "getSystem", instance ),
					(String) invokeMethod( clazz, "toCode", instance ),
					null
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
				E instance = (E) invokeMethod( clazz, "fromCode", null, value );
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
