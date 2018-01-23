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
public class PageableResultsStore implements IPageableResultsStore 
{
	/**
	 * The maximum number of simultaneously cached <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code>s.
	 */
	private static final int DEFAULT_MAX_CACHE_SIZE = 3;
	
	/**
	 * The maximum number of <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code> identifiers that
	 * had been cached once.
	 */
	private static final int DEFAULT_MAX_HISTORY_SIZE = 50;

	/**
	 * FIFO-based map of <code>org.dcm4chee.arr.cdi.query.paging.PageableResult</code>s.
	 */
	private final Map<String, PageableResults<?>> cache;
	
	/**
	 * FIFO-based list of identifiers that had been cached once.
	 */
	private final List<String> history;
	
	private final int maxCacheSize;
	
	private final int maxHistorySize;

	
	public PageableResultsStore()
	{
		this( DEFAULT_MAX_CACHE_SIZE );
	}

	public PageableResultsStore( int maxCacheSize )
	{
		this( maxCacheSize, DEFAULT_MAX_HISTORY_SIZE );
	}
	
	@SuppressWarnings("serial")
	public PageableResultsStore(int maxCacheSize, int maxHistorySize )
	{
		this.maxCacheSize = maxCacheSize;
		this.maxHistorySize = maxHistorySize;
		this.cache = new LinkedHashMap<String, PageableResults<?>>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, PageableResults<?>> eldest) 
			{
				if ( size() >= PageableResultsStore.this.maxCacheSize )
				{
					willBeRemoved( eldest.getValue() );
					
					return true;
				}
				return false;
			}
		};
		
		this.history = new ArrayList<>( 4 );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> PageableResults<T> getResults( String id, Class<T> parameterizedType )
	{
		PageableResults<?> results = getResults(id);
		if ( results!=null && parameterizedType.isAssignableFrom( results.getParameterizedType() ) )
		{
			return (PageableResults<T>) results;
		}
		return null;
	}
	
	@Override
	public PageableResults<?> getResults( String id )
	{
		return cache.get(id);
	}
	
	@Override
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
	
	@Override
	public boolean wasCachedOnce( String id )
	{
		return history.contains( id );
	}
	
	private void willBeRemoved( PageableResults<?> pageableResult )
	{
		if ( history.size() >= maxHistorySize )
		{
			history.remove(0);
		}
		history.add( pageableResult.getId() );
	}
}
