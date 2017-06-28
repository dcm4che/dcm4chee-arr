package org.dcm4chee.arr.cdi.query.paging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author bernhard.ableitinger@agfa.com
 *
 */
public class PageableResultsCache 
{
	/**
	 * The maximum number of simultaneously cached <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code>s.
	 */
	private static final int MAX_CACHE_SIZE = 3;
	
	/**
	 * The maximum number of <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code> identifiers that
	 * had been cached once.
	 */
	private static final int MAX_HISTORY_SIZE = 50;

	/**
	 * FIFO-based map of <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code>s.
	 */
	private final Map<String, PageableResults<?>> cache;
	
	/**
	 * FIFO-based list of identifiers that had been cached once.
	 */
	private final List<String> history;
	
	@SuppressWarnings("serial")
	public PageableResultsCache()
	{
		this.cache = new LinkedHashMap<String, PageableResults<?>>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, PageableResults<?>> eldest) 
			{
				if ( size() >= MAX_CACHE_SIZE )
				{
					willBeRemoved( eldest.getValue() );
					
					return true;
				}
				return false;
			}
		};
		
		this.history = new ArrayList<>( 4 );
	}
	
	public PageableResults<?> getResults( String id )
	{
		return cache.get( id );
	}
	
	public String putResults( PageableResults<?> results )
	{
		cache.put( results.getId(), results );
		return results.getId();
	}
		
	public void clear()
	{
		cache.clear();
		history.clear();
	}
	
	public boolean wasCachedOnce( String id )
	{
		return history.contains( id );
	}
	
	private void willBeRemoved( PageableResults<?> pageableResult )
	{
		if ( history.size() >= MAX_HISTORY_SIZE )
		{
			history.remove(0);
		}
		history.add( pageableResult.getId() );
	}
}
