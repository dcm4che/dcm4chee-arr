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
package org.dcm4chee.arr.cdi.query;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceUsed;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils.FhirConversionException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryParam;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryParam.FhirQueryParamParseException;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryDecorator;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.ClassifiedString;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.SearchParamParseException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryUtils;
import org.dcm4chee.arr.entities.AuditRecord;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.server.Constants;

/**
 * 
 *
 */
@Path("/query")
@Produces({ 
	MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
	Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML,
	Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW })
public class AuditRecordQueryRS
{	
	private static final Logger LOG = LoggerFactory.getLogger(AuditRecordQueryRS.class);
	
	@Inject @ArrDevice
	private Device device;
	
	@Inject
	private IAuditRecordQueryBean queryBean;
	
	@Inject
	@AuditRecordRepositoryServiceUsed
	private Event<UsedEvent> auditRecordRepositoryServiceUsedEvent;

	@GET
	@Path("/fhir")
    public Response search(
    		@Context HttpServletRequest request,
			@Context HttpHeaders headers,
			@Context UriInfo uriInfo,
			@QueryParam( "_format" ) List<String> formats,
			@QueryParam( "_count" ) Integer maxResults )
    {
		try
		{
			// negotiate content type
			MediaType type = negotiateType( headers.getAcceptableMediaTypes(), formats );	

			// convert search params into search restrictions
			IAuditRecordQueryDecorator queryDecorator = createFhirQueryDecorator( 
					uriInfo.getQueryParameters(), maxResults );
			
			// actually do the query
			List<AuditRecord> result = queryBean.find( queryDecorator );
			
			// convert into FHIR bundle
			Bundle bundle = FhirConversionUtils.toBundle( 
					BundleTypeEnum.SEARCH_RESULTS, result, 
					getContextURL( request ),
					true );
			
			// depending on the requested type/format
			// => encode to appropriate response
			if( isJsonType( type ) )
			{
				//... either JSON
				return Response
						.ok( FhirQueryUtils.encodeToJson( bundle ), type )
						.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
						.build();
			}
			else
			{
				//... or XML
				return Response
						.ok( FhirQueryUtils.encodeToXML( bundle ), type )
						.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
						.build();
			}
		}
		catch ( NotAcceptableException acceptE )
		{
			LOG.warn( "Not acceptable content type: " + acceptE.getMessage() );
			LOG.debug(null, acceptE );
			
			// => returns HTTP 406 - Not Acceptable
			throw new WebApplicationException( acceptE, Response.notAcceptable(
					Variant.mediaTypes(
						MediaType.APPLICATION_JSON_TYPE,
						MediaType.APPLICATION_XML_TYPE,
						MediaType.valueOf( Constants.CT_FHIR_JSON ),
						MediaType.valueOf( Constants.CT_FHIR_JSON_NEW ),
						MediaType.valueOf( Constants.CT_FHIR_XML ),
						MediaType.valueOf( Constants.CT_FHIR_XML_NEW )
						).build())
					.build() );
		}
		catch ( FhirQueryParamParseException parseE )
		{
			LOG.warn( "Bad request: " + parseE.getMessage() ); //$NON-NLS-1$
			LOG.debug( null, parseE );
			
			// => returns HTTP 400 - Bad Request
			throw new WebApplicationException( parseE, 
					Response.status(Status.BAD_REQUEST)
						.entity( parseE.getMessage() )
						.build() );
		}
		catch( FhirConversionException conversionE )
		{
			LOG.error( null, conversionE );
			
			// => returns HTTP 500 - Internal Server Error
			throw new WebApplicationException( conversionE, Status.INTERNAL_SERVER_ERROR );
		}
		catch( Exception e )
		{
			LOG.error( null, e );
			
			// => returns HTTP 500 - Internal Server Error
			throw new WebApplicationException( e, Status.INTERNAL_SERVER_ERROR );
		}
		finally
		{
			auditRecordRepositoryServiceUsedEvent.fire(
					new UsedEvent( true, device, 
							new RemoteSource( 
									request.getRemoteHost(),
									request.getRemoteUser(),
									request.getRequestURI())));
		}
    }
	
	@GET
	@Path("/")
    public Response search(
    		@Context HttpServletRequest request,
    		@Context HttpHeaders headers,
			@QueryParam( "_format" ) List<String> formats,
			@QueryParam( "_count" ) Integer maxResults,
			@QueryParam( "date") List<String> dates,
			@QueryParam( "type" ) List<String> types,
			@QueryParam( "subtype" ) List<String> subtypes,
			@QueryParam( "outcome" ) List<String> outcomes,
			@QueryParam( "action" ) List<String> actions,
			@QueryParam( "aet" ) String aet,
			@QueryParam( "patientId" ) String patientId,
			@QueryParam( "studyUid" ) String studyUid,
			@QueryParam( "accNr" ) String accNr,
			@QueryParam( "userId" ) String userId,
			@QueryParam( "host" ) String host )
    {
		try
		{
			// negotiate content type
			MediaType type = negotiateType( headers.getAcceptableMediaTypes(), formats );	
			
			// convert search params into search restrictions
			IAuditRecordQueryDecorator queryDecorator = new SimpleQueryDecorator()
					.setDateRange( SimpleQueryUtils.parseDateRange("date", dates, true ) )
					.setTypes( SimpleQueryUtils.parseParams( "type", types, ClassifiedString.class ) )
					.setSubTypes( SimpleQueryUtils.parseParams( "subtype", subtypes, ClassifiedString.class ) )
					.setOutcomes( SimpleQueryUtils.parseParams( "outcome", outcomes, Integer.class ) )
					.setActions( SimpleQueryUtils.parseParams( "action", actions, String.class ) )
					.setAET( SimpleQueryUtils.parseParam("aet", aet, String.class ) )
					.setPatientId( SimpleQueryUtils.parseParam("patientId", patientId, String.class ) )
					.setStudyUid( SimpleQueryUtils.parseParam("studyUid", studyUid, String.class ) )
					.setAccNr( SimpleQueryUtils.parseParam("accNr", accNr, String.class ) )
					.setUserId( SimpleQueryUtils.parseParam("userId", userId, String.class ) )
					.setHost( SimpleQueryUtils.parseParam("host", host, String.class ) )
					.setMaxResults(maxResults);
			
			// actually do the query
			List<AuditRecord> result = queryBean.find( queryDecorator );
			
			// convert into FHIR bundle
			Bundle bundle = FhirConversionUtils.toBundle( 
					BundleTypeEnum.SEARCH_RESULTS, result,
					getContextURL( request ),
					true );
			
			// depending on the requested type/format
			// => encode to appropriate response
			if( isJsonType( type ) )
			{
				//... either JSON
				return Response
						.ok(FhirQueryUtils.encodeToJson( bundle ), type )
						.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
						.build();
			}
			else
			{
				//... or XML
				return Response
						.ok( FhirQueryUtils.encodeToXML( bundle ), type )
						.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
						.build();
			}
		}
		catch ( NotAcceptableException acceptE )
		{
			LOG.warn( "Not acceptable content type: " + acceptE.getMessage() );
			LOG.debug(null, acceptE );
			
			// => returns HTTP 406 - Not Acceptable
			throw new WebApplicationException( acceptE, Response.notAcceptable(
					Variant.mediaTypes(
						MediaType.APPLICATION_JSON_TYPE,
						MediaType.APPLICATION_XML_TYPE,
						MediaType.valueOf( Constants.CT_FHIR_JSON ),
						MediaType.valueOf( Constants.CT_FHIR_JSON_NEW ),
						MediaType.valueOf( Constants.CT_FHIR_XML ),
						MediaType.valueOf( Constants.CT_FHIR_XML_NEW )
						).build())
					.build() );
		}
		catch ( SearchParamParseException parseE )
		{
			LOG.warn( "Bad request: " + parseE.getMessage() ); //$NON-NLS-1$
			LOG.debug( null, parseE );
			
			// => returns HTTP 400 - Bad Request
			throw new WebApplicationException( parseE, 
					Response.status(Status.BAD_REQUEST)
						.entity( parseE.getMessage() )
						.build() );
		}
		catch( FhirConversionException conversionE )
		{
			LOG.error( null, conversionE );
			
			// => returns HTTP 500 - Internal Server Error
			throw new WebApplicationException( conversionE, Status.INTERNAL_SERVER_ERROR );
		}
		catch( Exception e )
		{
			LOG.error( null, e );
			
			// => returns HTTP 500 - Internal Server Error
			throw new WebApplicationException( e, Status.INTERNAL_SERVER_ERROR );
		}
		finally
		{
			auditRecordRepositoryServiceUsedEvent.fire(
					new UsedEvent( true, device, 
							new RemoteSource( 
									request.getRemoteHost(),
									request.getRemoteUser(),
									request.getRequestURI())));
		}
    }
	
	private static MediaType negotiateType( List<MediaType> acceptedTypes, List<String> acceptedFormats ) throws NotAcceptableException
	{
		// give '_format' parameter precedence over 'accept' header param
		MediaType type = negotiateTypeAsString( acceptedFormats );
		if ( type == null )
		{
			type = negotiateTypeAsString( acceptedTypes == null ? null : acceptedTypes.stream()
				.map( t -> t.toString() )
				.collect(Collectors.toList()));
		}
		
		if ( type == null )
		{
			String types = new StringBuilder()
					.append( MediaType.APPLICATION_JSON )
					.append( ", " ).append( MediaType.APPLICATION_XML )
					.append( ", " ).append( Constants.CT_FHIR_JSON )
					.append( ", " ).append( Constants.CT_FHIR_JSON_NEW )
					.append( ", " ).append( Constants.CT_FHIR_XML )
					.append( ", " ).append( Constants.CT_FHIR_XML_NEW )
					.toString();
			throw new NotAcceptableException( "Unable to fulfill request: Accepted types are " + types ); //$NON-NLS-1$
		}
		
		return type;
	}
	
	private static MediaType negotiateTypeAsString( List<String> types )
	{
		if ( types != null && !types.isEmpty() )
		{
			for ( String type : types )
			{
				if ( type != null )
				{
					switch( type )
					{
					case Constants.CT_FHIR_JSON:
						return MediaType.valueOf( Constants.CT_FHIR_JSON );
					case Constants.CT_FHIR_JSON_NEW:
						return MediaType.valueOf( Constants.CT_FHIR_JSON_NEW );
					case MediaType.APPLICATION_JSON:
						return MediaType.APPLICATION_JSON_TYPE;
					case Constants.CT_FHIR_XML:
						return MediaType.valueOf( Constants.CT_FHIR_XML );
					case Constants.CT_FHIR_XML_NEW:
						return MediaType.valueOf( Constants.CT_FHIR_XML_NEW );
					case MediaType.APPLICATION_XML:
						return MediaType.APPLICATION_XML_TYPE;
					}
				}
			};
		}

		return null;
	}
	
	private static boolean isJsonType( MediaType type )
	{
		return type != null && (
				type.equals( MediaType.valueOf( Constants.CT_FHIR_JSON ) ) ||
				type.equals( MediaType.valueOf( Constants.CT_FHIR_JSON_NEW ) ) ||
				type.equals( MediaType.APPLICATION_JSON_TYPE ) );
	}
    
	private static IAuditRecordQueryDecorator createFhirQueryDecorator( 
			MultivaluedMap<String,String> params, Integer maxResults ) 
			throws FhirQueryParamParseException
	{
		return new FhirQueryDecorator()
			.setDates( FhirQueryParam.Date.parseFhirParam( params  ) )
			.setAddresses( FhirQueryParam.Address.parseFhirParam( params ) )
			.setPatientIdentifiers( FhirQueryParam.PatientIdentifier.parseFhirParam( params ) )
			.setObjectIdentities( FhirQueryParam.ObjectIdentity.parseFhirParam( params ) )
			.setObjectTypes( FhirQueryParam.ObjectType.parseFhirParam( params ) )
			.setSources( FhirQueryParam.Source.parseFhirParam( params ) )
			.setTypes( FhirQueryParam.Type.parseFhirParam( params ) )
			.setUsers( FhirQueryParam.User.parseFhirParam( params ) )
			.setSubTypes( FhirQueryParam.SubType.parseFhirParam( params ) )
			.setRoles( FhirQueryParam.Role.parseFhirParam( params ) )
			.setOutcomes( FhirQueryParam.Outcome.parseFhirParam( params ) )
			.setMaxResults( maxResults );
	}
	
	private static String getContextURL( HttpServletRequest request )
	{
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return url.substring(0, url.length() - uri.length() + ctx.length());
	}

}
