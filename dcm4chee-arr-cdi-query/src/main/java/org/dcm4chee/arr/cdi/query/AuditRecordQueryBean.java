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
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4chee.arr.entities.AuditRecord;

import com.mysema.query.SearchResults;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;

/**
 * 
 *
 */
public class AuditRecordQueryBean implements IAuditRecordQueryBean
{

    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;

    @Override
	public SearchResults<byte[]> findRaw( IAuditRecordQueryDecorator decorator ) throws Exception
	{    	
    	SearchResults<AuditRecord> results = findRecords( decorator );
    	
    	List<AuditRecord> records = results.getResults();
    	List<byte[]> data = new ArrayList<>(records.size());
    	for ( AuditRecord record : records )
    	{
    		data.add( record.getXmldata() );
    	}
    	
    	return new SearchResults<>( data, 
    			results.getLimit(), results.getOffset(), results.getTotal() );
	}
    

    @Override
    public SearchResults<AuditRecord> findRecords( IAuditRecordQueryDecorator decorator ) throws Exception
    {
    	JPAQuery countQuery = new JPAQuery(em)
      			.from( qAuditRecord )
    			.join( qAuditRecord.eventID, qEventId )  			
    			.join( qAuditRecord.eventType, qEventType );
    	
    	decorateQuery( countQuery, decorator, true );
    	
    	Long maxLimit = decorator.getMaxLimit();
    	
    	long total = countQuery.count();
    	
    	if ( maxLimit != null && total > maxLimit )
    	{
    		return new SearchResults<>( Collections.emptyList(),
    				maxLimit, null, total );
    	}
    	else
    	{
	    	JPAQuery query = new JPAQuery(em)
	      			.from( qAuditRecord )
	    			.join( qAuditRecord.eventID, qEventId )		
	    			.join( qAuditRecord.eventType, qEventType );

	    	decorateQuery( query, decorator, false );

	    	return new SearchResults<>( query.list( qAuditRecord ), 
	    			decorator.getLimit(), null, total );
    	}
    }   
    
    private static void decorateQuery( JPAQuery query, IAuditRecordQueryDecorator decorator, boolean forCountQuery )
    {
    	// predicates
    	if ( forCountQuery )
    	{
    		//simplified count query without sub-selects but inner joins
    		List<Predicate> predicates = decorator.getAuditRecordPredicates();
    		List<Predicate> apPredicates = decorator.getActiveParticipantPredicates();
    		if ( apPredicates != null && !apPredicates.isEmpty() )
    		{
    			query.join( qAuditRecord.activeParticipants, qActiveParticipant );
    			predicates.addAll( apPredicates );
    		}
    		
    		List<Predicate> poPredicates = decorator.getParticipantObjectPredicates();
    		if ( poPredicates != null && !poPredicates.isEmpty() )
    		{
    			query.join( qAuditRecord.participantObjects, qParticipantObject );
    			predicates.addAll( poPredicates );
    		}
    		
    		if ( !predicates.isEmpty() )
    		{
    			query.where( predicates.toArray( new Predicate[predicates.size()] ) );
    		}
    	}
    	else
    	{
	    	List<Predicate> predicates = decorator.getAllPredicates();
	    	if ( predicates != null && !predicates.isEmpty() )
	    	{
	    		query.where( predicates.toArray( new Predicate[predicates.size()] ) );
	    	}
    	}
    	
    	if ( !forCountQuery )
    	{
	    	// ordering
	    	OrderSpecifier<?> orderSpec = decorator.getOrderSpecifier();
	    	if ( orderSpec != null )
	    	{
	    		query.orderBy( orderSpec );
	    	}
	
	    	// max results
	    	Long limit = decorator.getLimit();
	    	if ( limit != null )
	    	{
	    		query.limit( limit );
	    	}
    	}
    }
    
}
