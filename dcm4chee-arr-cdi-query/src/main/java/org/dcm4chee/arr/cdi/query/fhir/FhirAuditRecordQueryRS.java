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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.dcm4chee.arr.cdi.query.AbstractAuditRecordQueryRS;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.AuditRecordQueryResult;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils.FhirConversionException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryParam.FhirQueryParamParseException;
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
@Path("/")
@Produces({ 
	MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
	Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
public class FhirAuditRecordQueryRS extends AbstractAuditRecordQueryRS
{	
	private static final Logger LOG = LoggerFactory.getLogger(FhirAuditRecordQueryRS.class);

	@GET
	@Transactional
	@Path("/AuditEvent")
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
			AuditRecordQueryResult result = doQuery( queryDecorator );
			
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
			
			LOG.info( "Not acceptable content type: " + acceptE.getMessage() );
			LOG.debug(null, acceptE );
			
			// => returns HTTP 406 - Not Acceptable
			return Response.notAcceptable(
					Variant.mediaTypes(
						MediaType.APPLICATION_JSON_TYPE,
						MediaType.APPLICATION_XML_TYPE,
						MediaType.valueOf( Constants.CT_FHIR_JSON ),
						MediaType.valueOf( Constants.CT_FHIR_XML )
						).build())
					.build();
		}
		catch ( FhirQueryParamParseException parseE )
		{
			LOG.info( "Bad request: " + parseE.getMessage() ); //$NON-NLS-1$
			LOG.debug( null, parseE );
			
			// => returns HTTP 400 - Bad Request
			return Response.status(Status.BAD_REQUEST)
					.entity(parseE.getMessage())
					.build();
		}
		catch( FhirConversionException conversionE )
		{
			LOG.error( null, conversionE );
			
			// => returns HTTP 500 - Internal Server Error
			return Response.status( Status.INTERNAL_SERVER_ERROR )
					.entity(conversionE.getMessage())
					.build();		
		}
		catch( Exception e )
		{
			LOG.error( null, e );
			
			// => returns HTTP 500 - Internal Server Error
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		finally
		{
			fireAuditRecordRepositoryUsedEvent( request );
		}
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
	
}
