package org.dcm4chee.arr.cdi.query.paging;

/**
 * 
 * @author bernhard.ableitinger
 *
 */
public class PageableExceptions 
{

	@SuppressWarnings("serial")
	public static class PageableException extends Exception 
	{
		public PageableException( String msg )
		{
			this( msg, null );
		}
		
		public PageableException( String msg, Throwable cause )
		{
			super( msg, cause );
		}
	}
	
	@SuppressWarnings("serial")
	public static class InvalidPageNumberException extends PageableException
	{
		private final int pageNumber;
		private final int pageCount;
		
		public InvalidPageNumberException( int pageNumber, int pageCount, String msg )
		{
			this( pageNumber, pageCount, msg, null );
		}
		
		public InvalidPageNumberException( int pageNumber, int pageCount, String msg, Throwable cause )
		{
			super( msg, cause );
			
			this.pageNumber = pageNumber;
			this.pageCount = pageCount;
		}
		
		public int getPageNumber()
		{
			return pageNumber;
		}
		
		public int getPageCount()
		{
			return pageCount;
		}
	}
	
	@SuppressWarnings("serial")
	public static class InvalidPageOffsetException extends PageableException
	{
		private final int pageOffset;
		
		public InvalidPageOffsetException( int offset, String msg )
		{
			this( offset, msg, null );
		}
		
		public InvalidPageOffsetException( int offset, String msg, Throwable cause )
		{
			super( msg, cause );
			
			this.pageOffset = offset;
		}
		
		public int getPageOffset()
		{
			return pageOffset;
		}
	}
	
	@SuppressWarnings("serial")
	public static class InvalidPageSizeException extends PageableException
	{
		private final int pageSize;
		
		public InvalidPageSizeException( int pageSize, String msg )
		{
			this( pageSize, msg, null );
		}
		
		public InvalidPageSizeException( int pageSize, String msg, Throwable cause )
		{
			super( msg, cause );
			
			this.pageSize = pageSize;
		}
		
		public int getPageSize()
		{
			return pageSize;
		}
	}

	@SuppressWarnings("serial")
	public static class PrematureCacheRemovalException extends PageableException
	{
		public PrematureCacheRemovalException( String msg )
		{
			this( msg, null );
		}
		
		public PrematureCacheRemovalException( String msg, Throwable cause )
		{
			super( msg, cause );
		}
	}
}
