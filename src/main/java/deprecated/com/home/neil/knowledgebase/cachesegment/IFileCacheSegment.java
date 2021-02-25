package deprecated.com.home.neil.knowledgebase.cachesegment;

public interface IFileCacheSegment {
	
	public String getBasePath();
	
	public String [] getStatePaths ();
	
	public String getFileName();

	public int getCacheSegmentSize();

}
