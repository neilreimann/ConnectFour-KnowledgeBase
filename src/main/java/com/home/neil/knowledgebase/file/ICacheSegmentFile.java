package com.home.neil.knowledgebase.file;

public interface ICacheSegmentFile {
	
	public String getBasePath();
	
	public String [] getStatePaths ();
	
	public String getFileName();

	public int getCacheSegmentSize();

}
