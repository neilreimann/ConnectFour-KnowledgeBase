package com.home.neil.knowledgebase.cachesegment.file.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.cachesegment.file.CompressableCacheSegmentFile;
import com.home.neil.knowledgebase.index.IndexEntry;

public class CompressableCacheSegmentFileIndexEntry extends IndexEntry {
	public static final String CLASS_NAME = CompressableCacheSegmentFileIndexEntry.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public CompressableCacheSegmentFile mCompressableCacheSegmentFile = null;
	
	public CompressableCacheSegmentFileIndexEntry (CompressableCacheSegmentFile pCompressableCacheSegmentFile) {
		mCompressableCacheSegmentFile = pCompressableCacheSegmentFile;
		setIndexEntry ();
	}
	
	public long setIndexEntry() {
		return setIndexEntry (CompressableCacheSegmentFileIndexEntry.class);
	}
	
	public CompressableCacheSegmentFile getCompressableCacheSegmentFile() {
		return mCompressableCacheSegmentFile;
	}
}
