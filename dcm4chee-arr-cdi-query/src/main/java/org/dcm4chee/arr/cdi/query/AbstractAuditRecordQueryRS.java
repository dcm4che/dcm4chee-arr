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
import javax.ws.rs.core.MediaType;

import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceUsed;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.AuditRecordQueryResult;
import org.dcm4chee.arr.cdi.query.IAuditRecordQueryBean.IAuditRecordQueryDecorator;
import org.jboss.resteasy.spi.NotAcceptableException;

import ca.uhn.fhir.rest.server.Constants;

/**
 * 
 */
public class AbstractAuditRecordQueryRS
{	

	@Inject @ArrDevice
	private Device device;
	
	@Inject
	private IAuditRecordQueryBean queryBean;
	
	@Inject
	@AuditRecordRepositoryServiceUsed
	private Event<UsedEvent> auditRecordRepositoryServiceUsedEvent;

	protected AuditRecordQueryResult doQuery( IAuditRecordQueryDecorator decorator ) throws Exception
	{
		return queryBean.find( decorator );
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
	
	protected static MediaType negotiateType( List<MediaType> acceptedTypes, List<String> acceptedFormats ) throws NotAcceptableException
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
	
	protected static MediaType negotiateTypeAsString( List<String> types )
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
	
	protected static boolean isJsonType( MediaType type )
	{
		return type != null && (
				type.equals( MediaType.valueOf( Constants.CT_FHIR_JSON ) ) ||
				type.equals( MediaType.valueOf( Constants.CT_FHIR_JSON_NEW ) ) ||
				type.equals( MediaType.APPLICATION_JSON_TYPE ) );
	}

	protected static String getContextURL( HttpServletRequest request )
	{
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return url.substring(0, url.length() - uri.length() + ctx.length());
	}
	
}
