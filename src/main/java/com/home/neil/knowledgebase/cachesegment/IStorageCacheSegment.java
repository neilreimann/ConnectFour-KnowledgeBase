package com.home.neil.knowledgebase.cachesegment;

public interface IStorageCacheSegment {
	// interface for higher State Storage Cache Segment to obtain Lower State Storage Byte array on Lower State Retirement 
	public byte[] getRetiredBytes() throws CacheSegmentStateException;
}
