package org.dcm4chee.arr.cdi.query.paging;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PageableResultsStoreProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( PageableResultsStoreProvider.class );
	
	private static final String RESULTS_SESSION_KEY = "PAGEABLE_RESULTS_CACHE"; //$NON-NLS-1$

	private static final String RESULTS_STORE_SIZE_VM_ARG = "arr.query.paging.defaultStoreSize"; //$NON-NLS-1$
	
	private static final String USE_SESSION_STORE_VM_ARG = "arr.query.paging.useSessionStore"; //$NON-NLS-1$
	
	private static final boolean USE_SESSION_STORE = useSessionStore();

	private static final int DEFAULT_RESULTS_STORE_SIZE = 10;
	
	private PageableResultsStore defaultStore;
	
	public IPageableResultsStore getStore( HttpServletRequest request )
	{
		if ( USE_SESSION_STORE && request != null )
		{
			IPageableResultsStore store = getFromSession( request.getSession(true) );
			if ( store != null )
			{
				return store;
			}
		}
		
		return getDefault();
	}
	
	private static PageableResultsStore getFromSession( HttpSession session )
	{
		if ( session != null )
		{
			PageableResultsStore store = (PageableResultsStore) session.getAttribute(RESULTS_SESSION_KEY);
			if ( store == null )
			{
				store = new PageableResultsStore( 1 );
				session.setAttribute( RESULTS_SESSION_KEY, store );
			}
			return store;
		}
		return null;
	}
	
	private synchronized PageableResultsStore getDefault()
	{
		if ( defaultStore == null )
		{
			defaultStore = new PageableResultsStore( defaultStoreSize() );
		}
		return defaultStore;
	}
	
	private static boolean hasCookie( HttpServletRequest request, String...cookieNames )
	{
		if ( request != null && cookieNames != null )
		{
			Cookie[] cookies = request.getCookies();
			if ( cookies != null )
			{
				for ( Cookie cookie : cookies )
				{
					for ( String name : cookieNames )
					{
						if ( cookie.getName().equalsIgnoreCase( name ) )
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private static boolean useSessionStore()
	{
		String value = System.getProperty(USE_SESSION_STORE_VM_ARG);
		return value != null ?
				BooleanUtils.toBoolean(value) :
					true;
	}
	
	private static int defaultStoreSize()
	{
		String value = System.getProperty(RESULTS_STORE_SIZE_VM_ARG);

		try
		{
			if ( value != null )
			{
				return Integer.valueOf(value);
			}
		}
		catch (NumberFormatException e )
		{
			LOG.error( String.format( "Failed to parse argument '%s=%s' (int value expected)...Using the default %s",
					RESULTS_STORE_SIZE_VM_ARG, value, DEFAULT_RESULTS_STORE_SIZE) );
		}
		
		return DEFAULT_RESULTS_STORE_SIZE;
	}
}
