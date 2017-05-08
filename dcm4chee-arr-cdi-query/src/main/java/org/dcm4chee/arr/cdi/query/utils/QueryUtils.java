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

package org.dcm4chee.arr.cdi.query.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 *
 */
public class QueryUtils 
{

	public static enum TemporalPrecision
	{
		YEAR(Calendar.YEAR), 
		MONTH(Calendar.MONTH), 
		DAY(Calendar.DAY_OF_MONTH), 
		MINUTE(Calendar.MINUTE),
		SECOND(Calendar.SECOND),
		MILLI(Calendar.MILLISECOND);		
		
		private int calendarConstant;
		
		private TemporalPrecision( int calendarConstant )
		{
			this.calendarConstant = calendarConstant;
		}
		
		public int getCalendarConstant()
		{
			return calendarConstant;
		}
	}
	
	public static Date toUpperBoundDate( Date date, TemporalPrecision precision )
	{
		if ( date != null )
		{
			Calendar cal = Calendar.getInstance() ;
			cal.setTime( date );
			
			switch( precision )
			{
				case YEAR: 
					cal.set(Calendar.MONTH, 11);
				case MONTH: 
					cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE) );
				case DAY: 
					cal.set(Calendar.HOUR_OF_DAY, 23);
					cal.set(Calendar.MINUTE, 59);
				case MINUTE: 
					cal.set(Calendar.SECOND, 59);
				case SECOND:
					cal.set(Calendar.MILLISECOND, 999);
				case MILLI:
			}
			
			return cal.getTime();
		}
		return null;
	}
	
	public static Date toLowerBoundDate( Date date, TemporalPrecision precision )
	{
		if ( date != null )
		{
			Calendar cal = Calendar.getInstance() ;
			cal.setTime( date );
			
			switch( precision )
			{
				case YEAR: 
					cal.set(Calendar.MONTH, 0);
				case MONTH: 
					cal.set(Calendar.DAY_OF_MONTH, 1);
				case DAY: 
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
				case MINUTE: 
					cal.set(Calendar.SECOND, 0);
				case SECOND:
					cal.set(Calendar.MILLISECOND, 0);
				case MILLI:
			}
			
			return cal.getTime();
		}
		return null;
	}
	
	public static class DateRange
	{
	    private final Date start;
	    private final Date end;

	    public DateRange(Date start, Date end) 
	    {
	    	if ( start != null && end != null && start.after( end ) )
	    	{
	    		this.end = start;
	    		this.start = end;
	    	}
	    	else
	    	{
		        this.start = start;
		        this.end = end;
	    	}
	    }
	    
	    public final boolean isRange()
	    {
	    	return start!=null && end!=null;
	    }

	    public final Date getStartDate() {
	        return start;
	    }

	    public final Date getEndDate() {
	        return end;
	    }

	    public boolean contains(Date when) 
	    {
	        return !(start != null && start.after(when)
	              || end != null && end.before(when));
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (obj == this)
	            return true;

	        if (!(obj instanceof DateRange))
	            return false;

	        DateRange other = (DateRange) obj;
	        return (start == null 
	                ? other.start == null
	                : start.equals(other.start)) 
	            && (end == null
	                ? other.end == null
	                : end.equals(other.end));
	    }

	    @Override
	    public int hashCode() {
	        int code = 0;
	        if (start != null)
	            code = start.hashCode();
	        if (end != null)
	            code ^= start.hashCode();
	        return code;
	    }

	    @Override
	    public String toString() {
	        return "[" + start + ", " + end + "]";
	    }
	}
}
