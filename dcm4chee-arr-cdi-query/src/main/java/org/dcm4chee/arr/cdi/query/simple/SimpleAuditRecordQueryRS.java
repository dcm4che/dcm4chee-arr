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
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.dcm4chee.arr.cdi.query.AbstractAuditRecordQueryRS;
import org.dcm4chee.arr.cdi.query.AuditRecordQueryBean;
import org.dcm4chee.arr.cdi.query.MediaTypeUtils;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirBundleLinksDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirBundleLinksDecorator.LinkParam;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils;
import org.dcm4chee.arr.cdi.query.paging.IPageableResultsStore;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.PageableException;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.PrematureCacheRemovalException;
import org.dcm4chee.arr.cdi.query.paging.PageableResults;
import org.dcm4chee.arr.cdi.query.paging.PageableResultsStoreProvider;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.ClassifiedString;
import org.dcm4chee.arr.cdi.query.utils.XSLTUtils;
import org.dcm4chee.arr.entities.AuditRecord;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.SearchResults;

import ca.uhn.fhir.rest.server.Constants;

/**
 */
@Path("/")
@RequestScoped
public class SimpleAuditRecordQueryRS extends AbstractAuditRecordQueryRS
{	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleAuditRecordQueryRS.class);

	private static final String RESOURCE_PATH = "AuditMessage"; //$NON-NLS-1$
	private static final String RAW_RESOURCE_PATH = "raw/AuditMessage"; //$NON-NLS-1$
	
	@Inject
	private PageableResultsStoreProvider resultsStoreProvider;
	
	@GET
	@Transactional
	@Path( RAW_RESOURCE_PATH )
	@Produces( MediaType.APPLICATION_XML )
    public Response searchRaw(
    		@Context HttpServletRequest request,
    		@Context HttpHeaders headers,
			@QueryParam( "_limit" ) Long limit,
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
					.setMaxResults(limit);
			
			// do the actual query
			SearchResults<byte[]> results = doRawQuery( queryDecorator );
			
			// build the xml document
		    String doc = toXMLDocument( results );
		    
		    // do a format conversion if needed
		    if ( StringUtils.isNotEmpty( rawFormat ) &&
		    		!rawFormat.equalsIgnoreCase( "orig" ) )
		    {
		    	doc = convertXMLDocument( doc, rawFormat );
		    }
		    
		    // return HTTP response
			Long limitApplied = results.getLimit();
			if ( limitApplied != null && limitApplied < results.getTotal() )
			{
				return Response.status(206) // partial content
						.entity(doc)
						.type( MediaType.APPLICATION_XML )
			    		.cacheControl( CacheControl.valueOf( "no-cache" ) )
			    		.build();	
			}
			else
			{
				return Response.ok( doc, MediaType.APPLICATION_XML )
			    		.cacheControl( CacheControl.valueOf( "no-cache" ) )
			    		.build();	
			}
		}
		catch( Exception e )
		{
			return toErrorResponse( e );
		}
		finally
		{
			fireAuditRecordRepositoryUsedEvent(request);
		}
    }
	
	@GET
	@Transactional
	@Path( RESOURCE_PATH + "/{search-id}")
    public Response search(
    		@Context HttpServletRequest request,
			@Context HttpHeaders headers,
			@PathParam( "search-id" ) String searchId,
			@QueryParam( "page" ) Integer pageNumber,
			@QueryParam( "offset" ) Integer pageOffset,
			@QueryParam( "_count" ) Integer pageSize,
			@QueryParam( "_format" ) List<String> formats )
    {
		try
		{
			// negotiate content type
			MediaType type = negotiateType( headers.getAcceptableMediaTypes(), 
					MediaTypeUtils.toTypeList( formats ) );	
			
			// get cached search result
			IPageableResultsStore resultsStore = resultsStoreProvider.getStore(request);
			PageableResults<AuditRecord> searchResults = resultsStore.getResults(
					searchId, AuditRecord.class );
			
			// cached results are present
			if ( searchResults != null )
			{
				// get results for requested page or alternatively offset/pagesize
				SearchResults<AuditRecord> pageResults = pageNumber != null ?
						searchResults.getPage(pageNumber) :
							searchResults.getPage(pageOffset, pageSize);
				
				// convert into FHIR bundle
				String contextURL = getContextURL( request );
				Bundle bundle = FhirConversionUtils.toBundle( 
						BundleType.SEARCHSET, pageResults, 
						contextURL, true );
				
				// add paging links to bundle
				String resourceURL = UriBuilder.fromUri( contextURL ).path( RESOURCE_PATH ).build().toString();
				FhirBundleLinksDecorator.create( resourceURL, searchResults, 
						new LinkParam("_format", formats) )
					.addBasePagingLinkIfNeeded(bundle)
					.addPagingLinksIfNeeded(bundle, pageNumber);
				
				// encode to appropriate response
				return toResponse( bundle, type, searchResults.getTotal(), searchResults.getLimit() );
			}
			
			// not cached results for that search-id
			else
			{
				// the results of the search with search-id were cached but
				// have been removed meanwhile from the cache again
				if ( resultsStore.wasCachedOnce( searchId ) )
				{
					throw new PrematureCacheRemovalException( String.format(
							"Unable to calculate paged subset: Results have already been removed from the cache for that search-id (%s)", searchId) );
				}
				else
				{
					throw new PageableException( String.format(
						"Unable to calculate paged subset: No cached search found for that search-id (%s)", searchId ) ); //$NON-NLS-1$
				}
			}
		}
		catch( Exception e )
		{
			return toErrorResponse( e );
		}
    }
	
	@GET
	@Transactional
	@Path( RESOURCE_PATH )
	@Produces({ 
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
		Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW})
    public Response search(
    		@Context HttpServletRequest request,
    		@Context HttpHeaders headers,
			@QueryParam( "_format" ) List<String> formats,
			@QueryParam( "_count" ) Integer count,
			@QueryParam( "_limit" ) Long limit,
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
			@QueryParam( "host" ) String host)
    {
		try
		{
			// negotiate content type
			MediaType type = negotiateType( headers.getAcceptableMediaTypes(), 
					MediaTypeUtils.toTypeList( formats ) );	
			
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
					.setMaxResults(limit);
			
			// actually do the query
			SearchResults<AuditRecord> results = doRecordQuery( queryDecorator );
			
			// convert into FHIR bundle
			String contextURL = getContextURL( request );
			Bundle bundle = null;
			
			long time = System.currentTimeMillis();
			
			// if a 'page-size' was requested and the result contains at least one entry
			if ( count != null && count > 0 && results.getResults().size()>0 )
			{
				// create pageable result
				PageableResults<AuditRecord> pageableResults = PageableResults.create(
						results, AuditRecord.class, count);

				// put/cache results
				IPageableResultsStore resultsStore = resultsStoreProvider.getStore(request);
				resultsStore.putResults( pageableResults );

				// create bundle with results from first page
				bundle = FhirConversionUtils.toBundle( 
						BundleType.SEARCHSET, pageableResults.getPage(1), 
						contextURL, true );

				// add paging links to bundle
				String resourceURL = UriBuilder.fromUri( contextURL ).path( RESOURCE_PATH ).build().toString();
				FhirBundleLinksDecorator.create( resourceURL, pageableResults, 
						new LinkParam( "_format", formats ))
					.addBasePagingLinkIfNeeded(bundle)
					.addPagingLinksIfNeeded(bundle, 1);
			}
			else if ( count != null && count == 0 )
			{
				// create bundle without entries
				bundle = FhirConversionUtils.toBundle( 
						BundleType.SEARCHSET, new SearchResults<>( Collections.emptyList(), 
								results.getLimit(), results.getOffset(), results.getTotal() ), 
						contextURL,
						true );
			}
			else
			{
				// create bundle with all results
				bundle = FhirConversionUtils.toBundle( 
						BundleType.SEARCHSET, results, 
						contextURL,
						true );
			}
			
			
			LOG.debug( String.format( "ARR query API: JSON format rendering took %s ms", System.currentTimeMillis()-time ) );

			// encode to appropriate response
			return toResponse( bundle, type, results.getTotal(), results.getLimit() );
		}
		catch( Exception e )
		{
			return toErrorResponse( e );
		}
		finally
		{
			fireAuditRecordRepositoryUsedEvent(request);
		}
    }
	
	
	private String toXMLDocument( SearchResults<byte[]> results )
	{
		StringBuilder docBuilder = new StringBuilder();
		
	    docBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
	    docBuilder.append("<AuditMessages>");

	    List<byte[]> list = results.getResults();
	    for( byte[] data : list )
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
