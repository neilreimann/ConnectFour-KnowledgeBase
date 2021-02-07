package com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.util.zip.DataFormatException;

public interface ICompressableCacheSegment extends IReadWriteCacheSegment{
	public void uncompress () throws IOException, DataFormatException, CacheSegmentStateException;

	public void compress () throws IOException, CacheSegmentStateException;
	
	public byte[] getRetiredUnCompressedCacheSegment() throws CacheSegmentStateException;

}
