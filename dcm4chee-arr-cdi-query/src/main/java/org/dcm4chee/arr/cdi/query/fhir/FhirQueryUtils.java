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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterAnd;
import ca.uhn.fhir.model.api.IQueryParameterOr;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.method.QualifiedParamList;

/**
 * 
 *
 */
public class FhirQueryUtils 
{
	// the used fhir context
	private static final FhirContext FHIR_CONTEXT = FhirContext.forDstu3();
	
	private static final String SEARCH_MODIFIER_DELIMITER = ":"; //$NON-NLS-1$
		
	
	public static String encodeToJson( IBaseResource resource )
	{
		return FHIR_CONTEXT.newJsonParser().encodeResourceToString( resource );
	}
	
	public static void encodeToJson( IBaseResource resource, Writer writer ) throws IOException
	{
		FHIR_CONTEXT.newJsonParser().encodeResourceToWriter( resource, writer );
	}
	
	public static String encodeToXML( IBaseResource resource )
	{
		return FHIR_CONTEXT.newXmlParser().encodeResourceToString( resource );
	}
	
	public static void encodeToXML( IBaseResource resource, Writer writer ) throws IOException
	{
		FHIR_CONTEXT.newXmlParser().encodeResourceToWriter( resource, writer );
	}
	
	
	public static <P extends IQueryParameterType, T extends IQueryParameterOr<P>> T parseSearchParamsOR( Class<T> paramsType, String paramName, String paramModifier, List<String> paramValues )
		throws Exception
	{
		QualifiedParamList paramList = new QualifiedParamList(); 
		paramList.setQualifier( paramModifier );
		paramValues.forEach( value -> {
			String[] values = StringUtils.split(value, ',');
			if ( values != null )
			{
				for( String v : values )
				{
					paramList.add(v);
				}
			}
		} );
		
		T params = paramsType.newInstance();
		params.setValuesAsQueryTokens(FHIR_CONTEXT, paramName, paramList );
		return params;
	}
	
	public static <P extends IQueryParameterOr<?>, T extends IQueryParameterAnd<P>> T parseSearchParamsAND( Class<T> paramsType, String paramName, String paramModifier, List<String> paramValues )
			throws Exception
	{
		List<QualifiedParamList> paramList = new ArrayList<>(4);
		paramValues.forEach( value -> paramList.add( 
				QualifiedParamList.singleton( paramModifier, value) ) );

		T params = paramsType.newInstance();
		params.setValuesAsQueryTokens(FHIR_CONTEXT, paramName, paramList );
		return params;
	}
	
	public static <T extends IQueryParameterType> T parseSearchParam( Class<T> paramType, String paramName, String paramModifier, String paramValue )
		throws Exception
	{
		T param = paramType.newInstance();
		param.setValueAsQueryToken(FHIR_CONTEXT, 
				paramName, paramModifier, paramValue );
		return param;
	}
	
	public static String parseSearchParamModifier( String param )
	{
		if ( param.contains( SEARCH_MODIFIER_DELIMITER ) )
		{
			return param.substring( 
					param.lastIndexOf( SEARCH_MODIFIER_DELIMITER) ); // ':' inclusive!
		}
		return null;
	}
	
	public static String parseSearchParamName( String param )
	{
		if ( param.contains( SEARCH_MODIFIER_DELIMITER ) )
		{
			return param.substring( 0,
					param.lastIndexOf( SEARCH_MODIFIER_DELIMITER) );
		}
		return param;
	}

}
