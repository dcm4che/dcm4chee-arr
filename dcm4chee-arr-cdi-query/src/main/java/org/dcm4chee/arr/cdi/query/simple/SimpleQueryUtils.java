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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dcm4chee.arr.cdi.query.utils.QueryUtils;
import org.dcm4chee.arr.cdi.query.utils.QueryUtils.DateRange;
import org.dcm4chee.arr.cdi.query.utils.QueryUtils.TemporalPrecision;

/**
 * 
 *
 */
public class SimpleQueryUtils 
{
	
	public static <T> List<T> parseParams( String name, List<String> values, Class<T> valueClass )
			throws SearchParamParseException
	{
		return parseParams( name, values, valueClass, -1, -1 , null );
	}
	
	public static <T> List<T> parseParams( String name, List<String> values, Class<T> valueClass, int minValues, int maxValues )
			throws SearchParamParseException
	{
		return parseParams( name, values, valueClass, minValues, maxValues, null );
	}
	
	public static <T> List<T> parseParams( String name, List<String> values, Class<T> valueClass, int minValues, int maxValues, ISearchParamValidator<T> validator )
		throws SearchParamParseException
	{
		if ( minValues > 0 && (values == null || values.isEmpty() ) )
		{
			throw new SearchParamParseException( name, "Too few search values for parameter " + name ); //$NON-NLS-1$
		}
		if ( maxValues >= 0 && values != null && values.size() > maxValues )
		{
			throw new SearchParamParseException( name, "Too many search values for parameter " + name ); //$NON-NLS-1$
		}
		
		if ( values != null )
		{
			List<T> parsedValues = new ArrayList<>( values.size() );
			for( String value : values )
			{
				T parsedValue = parseParam( name, value, valueClass, validator );
				if ( parsedValue != null )
				{
					parsedValues.add( parsedValue );
				}
			}
			return parsedValues;
		}
		
		return null;
	}
	
	public static DateRange parseDateRange( String name, List<String> values, boolean required ) throws SearchParamParseException
	{
		List<DateWithPrecision> dates = parseParams( name, values, DateWithPrecision.class, required ? 1 : -1, 2, null );
		if ( dates != null && !dates.isEmpty() )
		{
			if ( dates.size() == 1 )
			{
				DateWithPrecision date = dates.get(0);
				DateQualifier qualifier = date.getQualifier();
				DateRange range = null;
				
				if ( qualifier != null )
				{
					switch ( qualifier )
					{
					case GE: range = new DateRange( date.toLowerBound(), null ); break;
					case GT: range = new DateRange( date.toUpperBound(), null ); break;
					case LE: range = new DateRange( null, date.toUpperBound() ); break;
					case LT: range = new DateRange( null, date.toLowerBound() ); break;
					}
				}
				
				if ( range == null )
				{
					range = new DateRange( 
							dates.get(0).toLowerBound(),
							dates.get(0).toUpperBound());	
				}
				
				return range;
			}
			else
			{
				DateWithPrecision start = dates.get(0);
				DateWithPrecision end = dates.get(1);
				if ( start.getDate().after(end.getDate() ) )
				{
					DateWithPrecision tmp = start;
					start = end;
					end = tmp;
				}
				
				return new DateRange(
					start.toLowerBound(),
					end.toUpperBound() );
			}
		}
		return null;
	}

	public static <T> T parseParam( String name, String value, Class<T> valueClass )
		throws SearchParamParseException
	{
		return parseParam( name, value, valueClass, null );
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T parseParam( String name, String value, Class<T> valueClass, ISearchParamValidator<T> validator )
		throws SearchParamParseException
	{
		T parsedValue = null;
		
		if ( valueClass == DateWithPrecision.class )
		{
			parsedValue = (T) parseDateParam( name, value, (ISearchParamValidator<Date>) validator );
		}
		else if ( valueClass == Date.class )
		{
			parsedValue = (T) parseDateParam( name, value, (ISearchParamValidator<Date>) validator ).getDate();
		}
		else if ( valueClass == String.class )
		{
			parsedValue = (T) parseStringParam( name, value, (ISearchParamValidator<String>) validator );
		}
		else if ( valueClass == Integer.class )
		{
			parsedValue = (T) parseIntegerParam( name, value, (ISearchParamValidator<Integer>) validator);
		}
		else if ( valueClass == ClassifiedString.class )
		{
			parsedValue = (T) parseClassifiedStringParam( name, value, (ISearchParamValidator<ClassifiedString>) validator );
		}
		
		return parsedValue;
	}
	
	public static String parseStringParam( String name, String value ) throws SearchParamParseException
	{
		return parseStringParam( name, value, null );
	}
	
	public static String parseStringParam( String name, String value, ISearchParamValidator<String> validator )
		throws SearchParamParseException
	{
		if ( validator != null )
		{
			if ( !validator.validate( value ) )
			{
				throw new SearchParamParseException( name );
			}
		}
		return value;
	}
	
	public static ClassifiedString parseClassifiedStringParam( String name, String value, ISearchParamValidator<ClassifiedString> validator )
			throws SearchParamParseException
	{
		ClassifiedString cs = ClassifiedString.parse( value );
		if ( validator != null )
		{
			if ( !validator.validate( cs ) )
			{
				throw new SearchParamParseException( name );
			}
		}
		return cs;
	}
	
	public static Integer parseIntegerParam( String name, String value ) throws SearchParamParseException
	{
		return parseIntegerParam( name, value, null );
	}
	
	public static Integer parseIntegerParam( String name, String value, ISearchParamValidator<Integer> validator )
		throws SearchParamParseException
	{
		if ( value != null )
		{
			try
			{
				Integer i = Integer.valueOf(value);
				
				if ( validator != null )
				{
					if ( !validator.validate( i ) )
					{
						throw new SearchParamParseException( name );
					}
				}
				return i;
			}
			catch ( NumberFormatException nfE )
			{
				throw new SearchParamParseException( name, nfE );
			}
		}
		return null;
	}	
	
	public static DateWithPrecision parseDateParam( String name, String value ) throws SearchParamParseException
	{
		return parseDateParam( name, value, null );
	}
	
	public static DateWithPrecision parseDateParam( String name, String value, ISearchParamValidator<Date> validator )
			throws SearchParamParseException
	{
		if ( value == null )
		{
			return null;
		}
		
		int length = value.length();
		if ( length < 4 )
		{
			throw new SearchParamParseException( name );
		}
		
		Date date = null;
		TemporalPrecision precision = null;
		DateQualifier qualifier = null;
		
		try
		{
			// parse qualifier
			qualifier = DateQualifier.fromSearchValue( value );
			if ( qualifier != null )
			{
				int qualifierLength = qualifier.getQualifier().length();
				value = value.substring( qualifierLength );
				length -= qualifierLength;
			}
			
			Calendar cal = Calendar.getInstance();
			
			// year
			cal.set(Calendar.YEAR, Integer.parseInt(value.substring(0, 4)));
			precision = TemporalPrecision.YEAR;
			if (length > 4) 
			{
				validateCharAtIndexIs(value, 4, '-'); //$NON-NLS-1$
				validateLengthIsAtLeast(value, 7);
				cal.set(Calendar.MONTH, parseInt(value, value.substring(5, 7), 1, 12) - 1);
				precision = TemporalPrecision.MONTH;
				if ( length > 7 ) 
				{
					validateCharAtIndexIs(value, 7, '-'); //$NON-NLS-1$
					validateLengthIsAtLeast(value, 10);
					cal.set(Calendar.DATE, 1); // for some reason getActualMaximum works incorrectly if date isn't set
					int actualMaximum = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
					cal.set(Calendar.DAY_OF_MONTH, parseInt(value, value.substring(8, 10), 1, actualMaximum));
					precision = TemporalPrecision.DAY;
					if (length > 10) 
					{
						validateLengthIsAtLeast(value, 17);
						validateCharAtIndexIs(value, 10, 'T'); // yyyy-mm-ddThh:mm:ss
						
						int offsetIdx = getOffsetIndex(value);
						if (offsetIdx != -1) 
						{
							cal.setTimeZone( parseTimeZone( value, value.substring(offsetIdx) ) );
						}

						validateCharAtIndexIs(value, 13, ':'); //$NON-NLS-1$
						cal.set(Calendar.HOUR_OF_DAY, parseInt(value, value.substring(11, 13), 0, 23));
						cal.set(Calendar.MINUTE, parseInt(value, value.substring(14, 16), 0, 59));
						precision = TemporalPrecision.MINUTE;
					}
				}
			}

			date = cal.getTime();
		}
		catch ( Exception e )
		{
			throw new SearchParamParseException( name, e );
		}

		if ( validator != null )
		{
			if ( !validator.validate(date) )
			{
				throw new SearchParamParseException( name );
			}
		}
		
		return new DateWithPrecision( date, precision, qualifier );
	}
	
	private static void validateCharAtIndexIs(String value, int index, char c)
		throws Exception
	{
		if ( value.charAt(index) != c ) 
		{
			throw new Exception( "Expected character '" + c + "' at index " + index + " but found " + value.charAt(index) );
		}
	}

	private static void validateLengthIsAtLeast(String value, int length) 
		throws Exception
	{
		if ( value.length() < length ) 
		{
			throw new Exception( "Value is not correctly formatted" ); //$NON-NLS-1$
		}
	}
	
	private static int parseInt(String value, String s, int lowerBound, int upperBound) 
		throws Exception
	{
		int i = Integer.parseInt( s );

		if ( i < lowerBound || i > upperBound ) {
			throw new Exception( i + " must be in the range [" + lowerBound + "," + upperBound + "]" );
		}

		return i;
	}
	
	private static int getOffsetIndex(String s) throws Exception
	{
		int plusIndex = s.indexOf('+', 16);
		int minusIndex = s.indexOf('-', 16);
		int zIndex = s.indexOf('Z', 16);
		int retVal = Math.max(Math.max(plusIndex, minusIndex), zIndex);
		if (retVal == -1) {
			return -1;
		}
		if ((retVal - 2) != (plusIndex + minusIndex + zIndex)) {
			throw new Exception( "Time offset is invalid" );
		}
		return retVal;
	}
	
	private static TimeZone parseTimeZone(String wholeValue, String value) throws Exception
	{
		if ( isBlank( value ) ) 
		{
			throw new Exception( "Parsing timezone failed: Bad timezone" ); //$NON-NLS-1$
		} 
		else if (value.charAt(0) == 'Z')  //$NON-NLS-1$
		{
			return TimeZone.getTimeZone("Z"); //$NON-NLS-1$
		} 
		else if (value.length() != 6) 
		{
			throw new Exception("Parsing timezone failed: Timezone offset must be in the form \"Z\", \"-HH:mm\", or \"+HH:mm\"");
		} 
		else if (value.charAt(3) != ':' || !(value.charAt(0) == '+' || value.charAt(0) == '-')) 
		{
			throw new Exception("Parsing timezone failed: Timezone offset must be in the form \"Z\", \"-HH:mm\", or \"+HH:mm\"");
		} 
		else 
		{
			parseInt(wholeValue, value.substring(1, 3), 0, 23);
			parseInt(wholeValue, value.substring(4, 6), 0, 59);
			return TimeZone.getTimeZone("GMT" + value);
		}
	}
	
	public static interface ISearchParamValidator<T>
	{
		boolean validate( T value );
	}
	
	public static class ClassifiedString
	{
		private static final String DELIM = "|"; //$NON-NLS-1$
		
		private final String designator;
		private final String value;
		
		private ClassifiedString( String designator, String value )
		{
			this.designator = designator;
			this.value = value;
		}
		
		public boolean isClassified()
		{
			return designator != null;
		}
		
		public String getDesignator()
		{
			return designator;
		}
		
		public String getValue()
		{
			return value;
		}
		
		public static ClassifiedString parse( String s )
		{
			if ( s != null )
			{
				int i = s.lastIndexOf(DELIM);
				if ( i >= 0 )
				{
					if ( i==0 )
					{
						return new ClassifiedString( "", s.length()>1 ?
								s.substring(1) : "" );
					}
					
					return new ClassifiedString(
							s.substring(0, i),
							s.substring(i+1, s.length() ) );
				}
				else
				{
					return new ClassifiedString( null, s );
				}
			}
			return null;
		}
	}

	@SuppressWarnings("serial")
	public static class SearchParamParseException extends RuntimeException
	{
		private final String param;
		
		public SearchParamParseException( String param )
		{
			this( param, (Throwable) null );
		}
		
		public SearchParamParseException( String param, String msg )
		{
			super( msg );
			this.param = param;
		}
		
		public SearchParamParseException( String param, Throwable cause )
		{
			super( toMsg(param), cause );
			this.param = param;
		}
		
		public String getSearchParam()
		{
			return param;
		}
		
		private static String toMsg( String param )
		{
			return new StringBuilder()
					.append( "Failed to parse search parameter " )
					.append( param )
					.toString();
		}
	}
		
	private static enum DateQualifier
	{
		GE("ge"), LE("le"), GT("gt"), LT("lt");
		
		private String qualifier;
		
		private DateQualifier( String qualifier )
		{
			this.qualifier = qualifier;
		}
		
		public String getQualifier()
		{
			return qualifier;
		}
		
		public static DateQualifier fromSearchValue( String value )
		{
			for ( DateQualifier q : values() )
			{
				if ( value.startsWith(q.getQualifier()))
				{
					return q;
				}
			}
			return null;
		}
	}
	
	private static class DateWithPrecision
	{
		private final Date date;
		private final TemporalPrecision precision;
		private final DateQualifier qualifier;
		
		public DateWithPrecision( Date date, TemporalPrecision precision, DateQualifier qualifier )
		{
			this.date = date;
			this.precision = precision;
			this.qualifier = qualifier;
		}
		
		public Date getDate()
		{
			return date;
		}
		
		public DateQualifier getQualifier()
		{
			return qualifier;
		}
		
		public Date toUpperBound()
		{
			return QueryUtils.toUpperBoundDate(date, precision);
		}
		
		public Date toLowerBound()
		{
			return QueryUtils.toLowerBoundDate(date, precision);
		}
	}
}
