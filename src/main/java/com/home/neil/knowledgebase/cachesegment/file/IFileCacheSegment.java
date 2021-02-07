package com.home.neil.knowledgebase.cachesegment.file;

public interface IFileCacheSegment {
	
	public String getBasePath();
	
	public String [] getStatePaths ();
	
	public String getFileName();

	public int getCacheSegmentSize();

}
