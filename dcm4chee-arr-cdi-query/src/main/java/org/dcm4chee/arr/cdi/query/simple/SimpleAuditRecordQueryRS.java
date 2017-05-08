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
package org.dcm4chee.arr.cdi.query.simple;

import java.nio.charset.StandardCharsets;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.apache.commons.lang3.StringUtils;
import org.dcm4chee.arr.cdi.query.AbstractAuditRecordQueryRS;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils.FhirConversionException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryUtils;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.ClassifiedString;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.SearchParamParseException;
import org.dcm4chee.arr.cdi.query.utils.XSLTUtils;
import org.dcm4chee.arr.entities.AuditRecord;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.SearchResults;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.server.Constants;

/**
 */
@Path("/")
public class SimpleAuditRecordQueryRS extends AbstractAuditRecordQueryRS
{	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleAuditRecordQueryRS.class);
	
	@GET
	@Transactional
	@Path("/raw/AuditMessage")
	@Produces( MediaType.APPLICATION_XML )
    public Response searchRaw(
    		@Context HttpServletRequest request,
    		@Context HttpHeaders headers,
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
			@QueryParam( "user" ) String user,
			@QueryParam( "host" ) String host,
			@QueryParam( "rawFormat") String rawFormat )
    {
		try
		{
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
					.setUserId( SimpleQueryUtils.parseParam("user", user, String.class ) )
					.setHost( SimpleQueryUtils.parseParam("host", host, String.class ) )
					.setMaxResults(maxResults);
			
			// actually do the query
			SearchResults<byte[]> result = doRawQuery( queryDecorator );
			
			// build the xml document
		    String doc = toXMLDocument( result.getResults() );
		    
		    // do a format conversion if needed
		    if ( StringUtils.isNotEmpty( rawFormat ) &&
		    		!rawFormat.equalsIgnoreCase( "orig" ) )
		    {
		    	doc = convertXMLDocument( doc, rawFormat );
		    }
		    
		    // return HTTP response
		    return Response.ok( doc, MediaType.APPLICATION_XML )
		    		.cacheControl( CacheControl.valueOf( "no-cache" ) )
		    		.build();	
		}
		catch ( SearchParamParseException parseE )
		{
			LOG.info( "Bad request: " + parseE.getMessage() ); //$NON-NLS-1$
			LOG.debug( null, parseE );
			
			// => returns HTTP 400 - Bad Request
			return Response.status(Status.BAD_REQUEST)
					.entity(parseE.getMessage())
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
			fireAuditRecordRepositoryUsedEvent(request);
		}
    }
	
	@GET
	@Transactional
	@Path("/AuditMessage")
	@Produces({ 
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
		Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
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
			@QueryParam( "user" ) String user,
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
					.setUserId( SimpleQueryUtils.parseParam("user", user, String.class ) )
					.setHost( SimpleQueryUtils.parseParam("host", host, String.class ) )
					.setMaxResults(maxResults);
			
			// actually do the query
			SearchResults<AuditRecord> result = doRecordQuery( queryDecorator );
			
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
		catch ( SearchParamParseException parseE )
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
			fireAuditRecordRepositoryUsedEvent(request);
		}
    }
	
	
	private String toXMLDocument( List<byte[]> dataList )
	{
		StringBuilder docBuilder = new StringBuilder();
		
	    docBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
	    docBuilder.append("<AuditMessages>");

	    for( byte[] data : dataList )
	    {
	    	docBuilder.append(
	    		new String( data, StandardCharsets.UTF_8 )
	    			.replaceAll("\\<\\?xml(.+?)\\?\\>", "")
	    			.trim()
	    	);
	    }
	    docBuilder.append("</AuditMessages>");
	    
	    return docBuilder.toString();
	}
	
	private String convertXMLDocument( String doc, String rawFormat )
	{
		if ( rawFormat.equalsIgnoreCase( "SUP95" ) )
		{
			return XSLTUtils.render( XSLTUtils.NORMALIZE_TO_SUP95, 
					doc.getBytes( StandardCharsets.UTF_8 ) );
		}
		else if ( rawFormat.equalsIgnoreCase( "DICOM" ) )
		{
			return XSLTUtils.render( XSLTUtils.NORMALIZE_TO_DICOM, 
					doc.getBytes( StandardCharsets.UTF_8 ) );
		}

	    return doc;
	}

}
