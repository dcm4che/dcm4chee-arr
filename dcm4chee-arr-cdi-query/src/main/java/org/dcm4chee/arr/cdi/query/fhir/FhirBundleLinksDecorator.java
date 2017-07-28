package org.dcm4chee.arr.cdi.query.fhir;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.dcm4chee.arr.cdi.query.paging.PageableResults;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleLinkComponent;


/**
 * 
 * @author bernhard.ableitinger@agfa.com
 *
 */
public class FhirBundleLinksDecorator 
{
	private static final String SELF_RELATION = "self"; //$NON-NLS-1$
	private static final String FIRST_RELATION = "first"; //$NON-NLS-1$
	private static final String LAST_RELATION = "last"; //$NON-NLS-1$
	private static final String PREVIOUS_RELATION = "previous"; //$NON-NLS-1$
	private static final String NEXT_RELATION = "next"; //$NON-NLS-1$
	private static final String SEARCH_RELATION = "search"; //$NON-NLS-1$
	
	private final PageableResults<?> pageableResults;
	private final String basePagingUrl;
	
	private FhirBundleLinksDecorator( String basePagingUrl, PageableResults<?> pageableResults )
	{
		this.pageableResults = pageableResults;
		this.basePagingUrl = basePagingUrl;
	}
	
	public static FhirBundleLinksDecorator create( String baseUrl, PageableResults<?> pageableResults, LinkParam...params )
	{
		return new FhirBundleLinksDecorator( 
				getBasePagingUrl( baseUrl, pageableResults, params ), 
				pageableResults );
	}
	
	public FhirBundleLinksDecorator addBasePagingLinkIfNeeded( Bundle bundle )
	{
		int pageCount = pageableResults.getPageCount();
		
		if ( pageCount > 0 )
		{
			// add 'search' link
			bundle.addLink( createLink( SEARCH_RELATION, basePagingUrl ) );
		}
		
		return this;
	}
	
	public FhirBundleLinksDecorator addPagingLinksIfNeeded( Bundle bundle, Integer pageNumber )
	{
		int pageCount = pageableResults.getPageCount();
		
		if ( pageCount > 0 && pageNumber != null )
		{
			// add 'self' link
			bundle.addLink( createPagingLink( SELF_RELATION, pageNumber ) );
		
			// add 'first' link
			bundle.addLink( createPagingLink( FIRST_RELATION, 1 ) );
			
			// add 'last' link
			bundle.addLink( createPagingLink( LAST_RELATION, pageCount ) );
		
			// add 'previous' link
			if ( pageNumber > 1 )
			{
				bundle.addLink( createPagingLink( PREVIOUS_RELATION, pageNumber-1 ) );
			}
			
			// add 'next' link
			if ( pageNumber < pageCount )
			{
				bundle.addLink( createPagingLink( NEXT_RELATION, pageNumber+1 ) );
			}
		}
		
		return this;
	}
		
	private BundleLinkComponent createPagingLink( String relation, int pageNumber )
	{
		BundleLinkComponent link = new BundleLinkComponent();
		
		UriBuilder builder = UriBuilder.fromUri( basePagingUrl )
				.queryParam( "page", pageNumber );
		
		link.setRelation( relation );
		link.setUrl( builder.build().toString() );
		
		return link;
	}
	
	private BundleLinkComponent createLink( String relation, String url )
	{
		BundleLinkComponent link = new BundleLinkComponent();
				
		link.setRelation( relation );
		link.setUrl( url );
		
		return link;
	}
	
	private static String getBasePagingUrl( String baseUrl, PageableResults<?> pageableResults, LinkParam...params )
	{
		UriBuilder builder = UriBuilder.fromUri( baseUrl )
				.path( pageableResults.getId() );
		
		if ( params != null )
		{
			for ( LinkParam param : params )
			{
				if ( param != null && param.hasValue() )
				{
					builder.queryParam( param.getKey(), param.getValues() );
				}
			}
		}
		
		return builder.build().toString();
	}
	
	public static class LinkParam
	{
		private final String key;
		private final Object[] values;
		
		public LinkParam( String key, Object...values )
		{
			this.key = key;
			this.values = values;
		}
		
		public LinkParam( String key, List<?> values )
		{
			this.key = key;
			this.values = values != null ? 
					values.toArray() : null;
		}
		
		public String getKey()
		{
			return key;
		}
		
		public Object[] getValues()
		{
			return values;
		}
		
		public boolean hasValue()
		{
			return values != null;
		}
	}
}
