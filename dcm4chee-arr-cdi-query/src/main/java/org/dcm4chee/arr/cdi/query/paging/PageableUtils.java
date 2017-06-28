package org.dcm4chee.arr.cdi.query.paging;

import javax.servlet.http.HttpSession;

/**
 * 
 * @author bernhard.ableitinger
 *
 */
public class PageableUtils 
{
	private static final String RESULTS_SESSION_KEY = "PAGEABLE_RESULTS_CACHE"; //$NON-NLS-1$
	
	public static String putResults( HttpSession session, PageableResults<?> results )
	{
		if ( session != null )
		{
			PageableResultsCache cache = (PageableResultsCache) session.getAttribute(RESULTS_SESSION_KEY);
			if ( cache == null )
			{
				cache = new PageableResultsCache();
				session.setAttribute( RESULTS_SESSION_KEY, cache );
			}
			
			return cache.putResults( results );
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> PageableResults<T> getResults( HttpSession session, String id, Class<T> parameterizedType )
	{
		if ( session != null )
		{
			PageableResultsCache cache = (PageableResultsCache) session.getAttribute(RESULTS_SESSION_KEY);
			if ( cache != null )
			{
				PageableResults<?> results = cache.getResults(id);
				if ( parameterizedType.isAssignableFrom( results.getParameterizedType() ) )
				{
					return (PageableResults<T>) results;
				}
			}
		}
		return null;
	}
	
	public static boolean wereResultsCachedOnce( HttpSession session, String id )
	{
		if ( session != null )
		{
			PageableResultsCache cache = (PageableResultsCache) session.getAttribute(RESULTS_SESSION_KEY);
			if ( cache != null )
			{
				return cache.wasCachedOnce(id);
			}
		}
		return false;
	}

}
