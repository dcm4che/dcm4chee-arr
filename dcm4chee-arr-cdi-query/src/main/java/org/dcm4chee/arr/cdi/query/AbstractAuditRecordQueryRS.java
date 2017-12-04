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

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceUsed;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.dcm4chee.arr.cdi.query.fhir.FhirConversionUtils.FhirConversionException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryParam.FhirQueryParamParseException;
import org.dcm4chee.arr.cdi.query.fhir.FhirQueryUtils;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.PageableException;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.PrematureCacheRemovalException;
import org.dcm4chee.arr.cdi.query.simple.SimpleQueryUtils.SearchParamParseException;
import org.dcm4chee.arr.entities.AuditRecord;
import org.hl7.fhir.dstu3.model.Bundle;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.SearchResults;

import ca.uhn.fhir.rest.server.Constants;

/**
 * 
 */
public class AbstractAuditRecordQueryRS
{	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAuditRecordQueryRS.class);

	public static final MediaType FHIR_JSON_TYPE = MediaType.valueOf( Constants.CT_FHIR_JSON );
	public static final MediaType FHIR_JSON_TYPE_NEW = MediaType.valueOf( Constants.CT_FHIR_JSON_NEW );
	public static final MediaType FHIR_XML_TYPE = MediaType.valueOf( Constants.CT_FHIR_XML );
	public static final MediaType FHIR_XML_TYPE_NEW = MediaType.valueOf( Constants.CT_FHIR_XML_NEW );
	
	@Inject @ArrDevice
	private Device device;
	
	@Inject
	private IAuditRecordQueryBean queryBean;
	
	@Inject
	@AuditRecordRepositoryServiceUsed
	private Event<UsedEvent> auditRecordRepositoryServiceUsedEvent;

	protected SearchResults<AuditRecord> doRecordQuery( IAuditRecordQueryDecorator decorator ) throws Exception
	{
		return queryBean.findRecords( decorator );
	}
	
	protected SearchResults<byte[]> doRawQuery( IAuditRecordQueryDecorator decorator ) throws Exception
	{
		return queryBean.findRaw( decorator );
	}
	
	protected void fireAuditRecordRepositoryUsedEvent( HttpServletRequest request )
	{
		auditRecordRepositoryServiceUsedEvent.fire(
				new UsedEvent( true, device, 
						new RemoteSource( 
								request.getRemoteHost(),
								request.getRemoteUser(),
								request.getRequestURI())));
	}
	
	protected static MediaType negotiateType( List<MediaType> acceptedTypes, List<MediaType> acceptedFormats ) throws NotAcceptableException
	{
		// give '_format' parameter precedence over 'accept' header param
		MediaType type = MediaTypeUtils.negotiateType( acceptedFormats );
		if ( type == null )
		{
			type = MediaTypeUtils.negotiateType( acceptedTypes );
		}
		
		if ( type == null )
		{
			throw new NotAcceptableException( String.format( "Unable to fulfill request: Accepted types are %s",
						StringUtils.join( MediaTypeUtils.getSupportedTypes().toArray( new MediaType[0] ), ", ") ) ); 
		}
		
		return type;
	}
		
	protected static String getContextURL( HttpServletRequest request )
	{
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return url.substring(0, url.length() - uri.length() + ctx.length());
	}
	
	protected static Response toResponse( Bundle bundle, MediaType type, long total, Long maxLimit )
	{
		// depending on the requested type/format
		// => encode to appropriate response
		String content = MediaTypeUtils.isJsonType(type ) ? 
				FhirQueryUtils.encodeToJson( bundle ) : FhirQueryUtils.encodeToXML( bundle );

		if( maxLimit!=null && maxLimit < total )
		{
			return Response
					.status( 206 ) // Partial Content
					.entity( content )
					.type(type)
					.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
					.build();
		}
		else
		{
			return Response
					.ok( content, type )
					.cacheControl( CacheControl.valueOf("no-cache") ) //$NON-NLS-1$
					.build();
		}
	}
	
	protected static Response toErrorResponse( Exception e )
	{
		if ( e instanceof NotAcceptableException )
		{
			LOG.warn( "Bad request: Request was made with an unsupported accept/format type " + e.getMessage() );
			LOG.trace(null, e );

			return Response.notAcceptable(
					Variant.mediaTypes(
						MediaType.APPLICATION_JSON_TYPE,
						MediaType.APPLICATION_XML_TYPE,
						MediaType.valueOf( Constants.CT_FHIR_JSON ),
						MediaType.valueOf( Constants.CT_FHIR_XML )
						).build())
					.build();
		}
		else if ( e instanceof PrematureCacheRemovalException )
		{
			LOG.warn( e.getMessage() );
			LOG.trace( null, e );
			
			return Response.status(Status.GONE)
					.entity(e.getMessage())
					.build();
		}
		else if ( e instanceof PageableException ||
				e instanceof FhirQueryParamParseException ||
				e instanceof SearchParamParseException )
		{
			LOG.warn( e.getMessage() );
			LOG.trace( null, e );
			
			return Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage())
					.build();
		}
		else if ( e instanceof FhirConversionException )
		{
			LOG.error( null, e );

			return Response.status( Status.INTERNAL_SERVER_ERROR )
					.entity(e.getMessage())
					.build();		
		}
		else
		{
			LOG.error( null, e );

			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
