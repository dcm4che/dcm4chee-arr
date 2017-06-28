package org.dcm4chee.arr.cdi.query.paging;

import org.apache.commons.lang3.RandomStringUtils;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.InvalidPageNumberException;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.InvalidPageOffsetException;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.InvalidPageSizeException;
import org.dcm4chee.arr.cdi.query.paging.PageableExceptions.PageableException;

import com.mysema.query.SearchResults;

/**
 * 
 * @author bernhard.ableitinger@agfa.com
 *
 */
public class PageableResults<T> 
{
	private static final int ID_LENGTH = 12;
	
	private final String id;
	private final SearchResults<T> searchResults;
	private final int defaultPageSize;
	private final Class<T> parameterizedType;
	
	private PageableResults( String id, SearchResults<T> searchResults, Class<T> parameterizedType, int defaultPageSize )
	{
		this.id = id;
		this.searchResults = searchResults;
		this.defaultPageSize = defaultPageSize;
		this.parameterizedType = parameterizedType;
	}
	
	public static <T> PageableResults<T> create( SearchResults<T> searchResults, Class<T> parameterizedType, int defaultPageSize )
	{
		return new PageableResults<T>( RandomStringUtils.randomAlphanumeric( ID_LENGTH ), 
				searchResults, parameterizedType, defaultPageSize );
	}
	
	/**
	 * Returns the unique identifier.
	 * 
	 * @return
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Returns the max. number of matching items that an initial search query can return.
	 * In other words, the limit is the safety parameter that limits database queries to a maximum.
	 * 
	 * @return
	 */
	public long getLimit()
	{
		return searchResults.getLimit();
	}
	
	/**
	 * Returns the number of matching items that the initial database query actually returned.
	 * Be aware that count <= limit and count <= total.
	 * 
	 * @return
	 */
	public long getCount()
	{
		return searchResults.getResults().size();
	}
	
	/**
	 * Returns the total number of items that match the initial search criteria in the database.
	 * 
	 * @return
	 */
	public long getTotal()
	{
		return searchResults.getTotal();
	}
	
	/**
	 * Returns the default page size.
	 * 
	 * @return
	 */
	public int getDefaultPageSize()
	{
		return defaultPageSize;
	}
	
	/**
	 * Returns the number of pages with respect to the default page size.
	 * 
	 * @return
	 */
	public int getPageCount()
	{
		if ( defaultPageSize == 0 )
		{
			return 0;
		}
		return (int) Math.ceil( (double) getCount() / (double) defaultPageSize );
	}
	
	/**
	 * Returns the parameterized type of this cache instance.
	 * 
	 * @return
	 */
	public Class<T> getParameterizedType()
	{
		return parameterizedType;
	}
	
	/**
	 * Returns the subset of the results that corresponds to the given page number 
	 * with respect to the default page size.
	 * 
	 * @param pageNumber
	 * @return
	 */
	public SearchResults<T> getPage( int pageNumber ) throws PageableException
	{
		int pageCount = getPageCount();
		if ( pageNumber <= 0 || pageNumber > pageCount )
		{
			throw new InvalidPageNumberException( pageNumber, pageCount, String.format( "Unable to calculate paged subset: Illegal page number (%d) - Make sure that page number is >=1 and <%d", pageNumber, pageCount ) ); //$NON-NLS-1$
		}
				
		return getPage( (pageNumber-1) * defaultPageSize, (int) defaultPageSize );
	}
	
	/**
	 * Returns a subset of the results. The subset starts at the given offset (inclusive) and extends
	 * to the given page size.
	 * 
	 * @param offset
	 * @param pageSize
	 * @return
	 */
	public SearchResults<T> getPage( int offset, int pageSize ) throws PageableException
	{
		if ( offset < 0 || offset >= getCount() )
		{
			throw new InvalidPageOffsetException( offset, String.format( "Unable to calculate paged subset: Illegal offset (%d) - Make sure that offset is greater/equal 0 and less than results count", offset ) ); //$NON-NLS-1$
		}
		
		if ( pageSize <= 0 )
		{
			throw new InvalidPageSizeException( pageSize, String.format( "Unable to calculate paged subset: Illegal page size (%d) - Make sure that page size is greater than 0", pageSize) );
		}
		
		try
		{
			return new SearchResults<T>( searchResults.getResults().subList( 
					offset, (int) Math.min( getCount(), offset + pageSize ) ), 
					searchResults.getLimit(), searchResults.getOffset(), searchResults.getTotal() );
		}
		catch ( Exception e )
		{
			throw new PageableException( "Unable to calculate paged subset", e ); //$NON-NLS-1$
		}
	}
}
