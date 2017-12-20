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
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import ca.uhn.fhir.model.api.IQueryParameterAnd;
import ca.uhn.fhir.model.api.IQueryParameterOr;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;

/**
 * 
 *
 */
public abstract class FhirQueryParam<T>
{
	
	public static FhirQueryParamAnd<DateParam, DateRangeParam> Date =
			new FhirQueryParamAnd<>( DateRangeParam.class, "date", true );
	
	public static FhirQueryParamOr<StringParam, StringOrListParam> Address = 
			new FhirQueryParamOr<>( StringOrListParam.class, "address" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> PatientIdentifier = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "patient.identifier" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> ObjectIdentity = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "entity-id" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> ObjectType = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "entity-type" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> Source = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "source" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> Type = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "type" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> User = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "user" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> SubType = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "subtype" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> Role = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "entity-role" );
	
	public static FhirQueryParamOr<TokenParam, TokenOrListParam> Outcome = 
			new FhirQueryParamOr<>( TokenOrListParam.class, "outcome" );
	
	
	protected final String paramName;
	protected final Class<T> fhirParamType;
	protected final boolean required;
	
	protected FhirQueryParam( Class<T> fhirParamType, String paramName, boolean required )
	{
		this.fhirParamType = fhirParamType;
		this.paramName = paramName;
		this.required = required;
	}
	
	public String getParamName()
	{
		return paramName;
	}
	
	public boolean isRequired()
	{
		return required;
	}
	
	public abstract T parseFhirParam( UriInfo uriInfo ) throws FhirQueryParamParseException;
	
	public abstract T parseFhirParam( MultivaluedMap<String,String> params ) throws FhirQueryParamParseException;

	public static class FhirQueryParamSimple<T extends IQueryParameterType> extends FhirQueryParam<T>
	{
		private FhirQueryParamSimple( Class<T> fhirParamType, String paramName )
		{
			super(fhirParamType, paramName, false );
		}
		
		public T parseFhirParam( UriInfo uriInfo ) throws FhirQueryParamParseException
		{
			return parseFhirParam( uriInfo.getQueryParameters() );
		}
		
		public T parseFhirParam( MultivaluedMap<String,String> params ) throws FhirQueryParamParseException
		{
			for( Map.Entry<String, List<String>> entry : params.entrySet() )
			{
				String param = entry.getKey();
				String name = FhirQueryUtils.parseSearchParamName( param );
				if ( name != null && paramName.equalsIgnoreCase( name ) )
				{
					try
					{
						T result = null;
						List<String> values = entry.getValue();
						if ( values != null && !values.isEmpty() )
						{
							result = FhirQueryUtils.parseSearchParam( fhirParamType,
									name, 
									FhirQueryUtils.parseSearchParamModifier(param),
									values.get(0) );
						}
						
						if ( result != null )
						{
							return result;
						}
					}
					catch ( Exception e )
					{
						throw new FhirQueryParamParseException( this, e );
					}
				}
			}
			
			if ( isRequired() )
			{
				throw new FhirQueryParamParseException( this, 
						getParamName() + " is required/mandatory" );
			}
			
			return null;
		}

	}

	public static class FhirQueryParamOr<P extends IQueryParameterType, T extends IQueryParameterOr<P>>
		extends FhirQueryParam<T>
	{
		private FhirQueryParamOr( Class<T> fhirParamType, String paramName )
		{
			super(fhirParamType, paramName, false );
		}
		
		private FhirQueryParamOr( Class<T> fhirParamType, String paramName, boolean required )
		{
			super(fhirParamType, paramName, required );
		}
		
		public T parseFhirParam( UriInfo uriInfo ) throws FhirQueryParamParseException
		{
			return parseFhirParam( uriInfo.getQueryParameters() );
		}
		
		public T parseFhirParam( MultivaluedMap<String,String> params ) throws FhirQueryParamParseException
		{
			for( Map.Entry<String, List<String>> entry : params.entrySet() )
			{
				String param = entry.getKey();
				String name = FhirQueryUtils.parseSearchParamName( param );
				if ( name != null && paramName.equalsIgnoreCase( name ) )
				{
					try
					{
						return FhirQueryUtils.parseSearchParamsOR( fhirParamType,
								name, 
								FhirQueryUtils.parseSearchParamModifier(param),
								entry.getValue() );
					}
					catch ( Exception e )
					{
						throw new FhirQueryParamParseException( this, e );
					}
				}
			}
			
			if ( isRequired() )
			{
				throw new FhirQueryParamParseException( this, 
						getParamName() + " is required/mandatory" );
			}
			
			return null;
		}
	}
	
	public static class FhirQueryParamAnd<P extends IQueryParameterOr<?>, T extends IQueryParameterAnd<P>>
		extends FhirQueryParam<T>
	{
		private FhirQueryParamAnd( Class<T> fhirParamType, String paramName )
		{
			super(fhirParamType, paramName, false );
		}
		
		private FhirQueryParamAnd( Class<T> fhirParamType, String paramName, boolean required )
		{
			super(fhirParamType, paramName, required );
		}

		public T parseFhirParam( UriInfo uriInfo ) throws FhirQueryParamParseException
		{
			return parseFhirParam( uriInfo.getQueryParameters() );
		}

		public T parseFhirParam( MultivaluedMap<String,String> params ) throws FhirQueryParamParseException
		{
			for( Map.Entry<String, List<String>> entry : params.entrySet() )
			{
				String param = entry.getKey();
				String name = FhirQueryUtils.parseSearchParamName( param );
				if ( name != null && paramName.equalsIgnoreCase( name ) )
				{
					try
					{
						return FhirQueryUtils.parseSearchParamsAND( fhirParamType,
								name, 
								FhirQueryUtils.parseSearchParamModifier(param),
								entry.getValue() );
					}
					catch ( Exception e )
					{
						throw new FhirQueryParamParseException( this, e );
					}
				}
			}
			
			if ( isRequired() )
			{
				throw new FhirQueryParamParseException( this, 
						getParamName() + " is required/mandatory" );
			}
			
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FhirQueryParamParseException extends Exception
	{
		private final FhirQueryParam<?> param;
		
		public FhirQueryParamParseException( FhirQueryParam<?> param )
		{
			this( param, (Throwable) null );
		}
		
		public FhirQueryParamParseException( FhirQueryParam<?> param, String msg )
		{
			super( msg );
			this.param = param;
		}
		
		public FhirQueryParamParseException( FhirQueryParam<?> param, Throwable cause )
		{
			super( toMsg( param ), cause );
			this.param = param;
		}
		
		public FhirQueryParam<?> getSearchParam()
		{
			return param;
		}
		
		private static String toMsg( FhirQueryParam<?> param )
		{
			return new StringBuilder()
					.append( "Failed to parse FHIR search parameter ")
					.append( param.getParamName() )
					.toString();
		}
	}
}
