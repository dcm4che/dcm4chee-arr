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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dcm4chee.arr.cdi.query.simple.SimpleQueryDecorator;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.QActiveParticipant;
import org.dcm4chee.arr.entities.QAuditRecord;
import org.dcm4chee.arr.entities.QCode;
import org.dcm4chee.arr.entities.QParticipantObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.SearchResults;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;

/**
 * 
 *
 */
public interface IAuditRecordQueryBean 
{
    // QueryDSL metadata type instances
    public static QAuditRecord qAuditRecord = new QAuditRecord("ar");
    public static QCode qEventId = new QCode("eid");
    public static QCode qEventType = new QCode("etype");
    public static QActiveParticipant qActiveParticipant = new QActiveParticipant("ap");
    public static QParticipantObject qParticipantObject = new QParticipantObject("po");

	SearchResults<AuditRecord> findRecords( 
			IAuditRecordQueryDecorator decorator ) throws Exception;
	
	SearchResults<byte[]> findRaw(
			IAuditRecordQueryDecorator decorator ) throws Exception;
	
    public static interface IAuditRecordQueryDecorator
    {
        public static QAuditRecord ar = qAuditRecord;
        public static QCode eventId = qEventId;
        public static QCode eventType = qEventType;
        public static QActiveParticipant ap = qActiveParticipant;
        public static QParticipantObject po = qParticipantObject;
        
    	List<Predicate> getAllPredicates();
    	
    	List<Predicate> getAuditRecordPredicates();
    	
    	List<Predicate> getActiveParticipantPredicates();
    	
    	List<Predicate> getParticipantObjectPredicates();

    	OrderSpecifier<?> getOrderSpecifier();
    	
    	Long getLimit();
    	
    	Long getMaxLimit();
    	
    }

    
    public static abstract class AbstractAuditRecordQueryDecorator
    	implements IAuditRecordQueryDecorator
    {
    	private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryDecorator.class);
    	
    	private static final String ARR_QUERY_MAX_LIMIT_PROPERTY = "arr.query.maxLimit"; //$NON-NLS-1$

    	private static final Long DEFAULT_MAX_LIMIT = getMaxLimitFromSystemProperty();
    	
    	/**
    	 * The maximum number of returned search results
    	 */
    	private Long limit;
    	
    	protected AbstractAuditRecordQueryDecorator setMaxResults( Long maxResults )
    	{
    		this.limit = maxResults;
    		return this;
    	}
    	
    	@Override
    	public Long getLimit()
    	{
    		Long maxLimit = getMaxLimit();
    		if ( limit != null )
    		{
    			return maxLimit != null ? Math.min(limit, maxLimit) : limit;
    		}
    		return maxLimit;
    	}
    	
    	@Override
    	public Long getMaxLimit()
    	{
    		return DEFAULT_MAX_LIMIT;
    	}
    	
    	@Override
    	public OrderSpecifier<?> getOrderSpecifier()
    	{
    		return ar.eventDateTime.desc();
    	}

    	private static Long getMaxLimitFromSystemProperty()
    	{
    		try
    		{
    			String s = System.getProperty(ARR_QUERY_MAX_LIMIT_PROPERTY);
    			if ( s != null )
    			{
    				return Long.valueOf(s);
    			}
    		}
    		catch ( Exception e )
    		{
    			LOG.error( "Failed to parse system property " + ARR_QUERY_MAX_LIMIT_PROPERTY, e);
    		}
    		return null;
    	}
    	
    	protected static List<Predicate> addIgnoreNull( List<Predicate> predicates, Predicate predicate )
    	{
    		if ( predicate != null )
    		{
    			if ( predicates == null )
    			{
    				predicates = new ArrayList<>();
    			}
    			predicates.add( predicate );
    		}
    		return predicates;
    	}
    	
    	protected static boolean emptyOrNull( Collection<?> c )
    	{
    		return c == null || c.isEmpty();
    	}
    }
}
