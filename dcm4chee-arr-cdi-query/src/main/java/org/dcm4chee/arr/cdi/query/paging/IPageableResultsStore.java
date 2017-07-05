package org.dcm4chee.arr.cdi.query.paging;

import java.io.Serializable;

/**
 * 
 * @author bernhard.ableitinger@agfa.com
 *
 */
public interface IPageableResultsStore extends Serializable
{

	String putResults( PageableResults<?> results );
	
	PageableResults<?> getResults( String id );
	
	<T> PageableResults<T> getResults( String id, Class<T> parameterizedType );

	boolean wasCachedOnce( String id );
	
}
