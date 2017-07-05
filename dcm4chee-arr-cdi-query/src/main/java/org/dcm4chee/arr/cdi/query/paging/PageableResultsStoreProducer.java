package org.dcm4chee.arr.cdi.query.paging;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

@ApplicationScoped
public class PageableResultsStoreProducer
{
	private static final boolean USE_SESSION_STORE = false;

	private static final String RESULTS_SESSION_KEY = "PAGEABLE_RESULTS_CACHE"; //$NON-NLS-1$

	private PageableResultsStore defaultStore;
	
	@SuppressWarnings("unused")
	@Produces
	@PreferredStore
	@SessionScoped
	public IPageableResultsStore get( @Context HttpServletRequest request )
	{
		if ( USE_SESSION_STORE && request != null )
		{
			return getFromSession( request.getSession() );
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
				store = new PageableResultsStore( 3 );
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
			defaultStore = new PageableResultsStore( 10 );
		}
		return defaultStore;
	}
}
