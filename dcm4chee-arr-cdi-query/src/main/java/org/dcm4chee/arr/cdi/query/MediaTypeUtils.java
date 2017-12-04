package org.dcm4chee.arr.cdi.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.util.MediaTypeHelper;

import ca.uhn.fhir.rest.server.Constants;

public class MediaTypeUtils 
{
	public static final MediaType FHIR_JSON_TYPE = MediaType.valueOf( Constants.CT_FHIR_JSON );
	
	public static final MediaType FHIR_JSON_TYPE_NEW = MediaType.valueOf( Constants.CT_FHIR_JSON_NEW );
	
	public static final MediaType FHIR_XML_TYPE = MediaType.valueOf( Constants.CT_FHIR_XML );
	
	public static final MediaType FHIR_XML_TYPE_NEW = MediaType.valueOf( Constants.CT_FHIR_XML_NEW );
		
	private static final List<MediaType> SUPPORTED_TYPES = Arrays.asList( new MediaType[] {
			MediaType.APPLICATION_JSON_TYPE,
			MediaType.APPLICATION_XML_TYPE,
			FHIR_JSON_TYPE,
			FHIR_JSON_TYPE_NEW,
			FHIR_XML_TYPE,
			FHIR_XML_TYPE_NEW
	});
	
	public static List<MediaType> getSupportedTypes()
	{
		return Collections.unmodifiableList( SUPPORTED_TYPES );
	}
	
	public static List<MediaType> toTypeList( List<String> types )
	{
		if ( types != null )
		{
			return types.stream()
					.map(type -> MediaType.valueOf(type) )
					.collect(Collectors.toList() );
		}
		return Collections.emptyList();
	}
	
	public static MediaType negotiateType( List<MediaType> types )
	{
		if ( types != null && !types.isEmpty() )
		{
			// use new list instance for safe sorting of list
			List<MediaType> list = new ArrayList<>( types );
			
			MediaTypeHelper.sortByWeight( list );
			
			for ( MediaType type : list )
			{
				if ( isMediaTypeSupported( type ) )
				{
					return type;
				}
			};
		}

		return null;
	}
	
	public static boolean isJsonType( MediaType type )
	{
		return type != null && (
				FHIR_JSON_TYPE.isCompatible( type ) ||
				FHIR_JSON_TYPE_NEW.isCompatible( type ) ||
				MediaType.APPLICATION_JSON_TYPE.isCompatible( type ) );
	}	
	
	private static boolean isMediaTypeSupported( MediaType type )
	{
		if ( type != null )
		{
			for ( MediaType supportedType : SUPPORTED_TYPES )
			{
				if ( supportedType.isCompatible(type) )
				{
					return true;
				}
			}
		}
		
		return false;
	}
}

