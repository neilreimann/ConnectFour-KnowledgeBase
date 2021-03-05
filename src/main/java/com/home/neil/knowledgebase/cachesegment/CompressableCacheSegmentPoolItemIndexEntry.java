package com.home.neil.knowledgebase.cachesegment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.index.IndexEntry;
import com.home.neil.knowledgebase.pool.IPoolItem;

public class CompressableCacheSegmentPoolItemIndexEntry extends IndexEntry implements IPoolItem {
	public static final String CLASS_NAME = CompressableCacheSegmentPoolItemIndexEntry.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private CompressableCacheSegment mCompressableCacheSegment = null;
	private String mPoolItemId = null;

	public CompressableCacheSegmentPoolItemIndexEntry(String pPoolItemId, CompressableCacheSegment pCompressableCacheSegment) {
		mCompressableCacheSegment = pCompressableCacheSegment;
		mPoolItemId = pPoolItemId;
		setIndexEntry();
	}

	public long setIndexEntry() {
		return setIndexEntry(CompressableCacheSegmentPoolItemIndexEntry.class);
	}

	public CompressableCacheSegment getCompressableCacheSegment() {
		return mCompressableCacheSegment;
	}

	public void setCompressableCacheSegment(CompressableCacheSegment pCompressableCacheSegment) {
		mCompressableCacheSegment = pCompressableCacheSegment;
	}

	@Override
	public String getPoolItemId() {
		return mPoolItemId;
	}

	@Override
	public void init() throws KnowledgeBaseException {
		// No initialization necessary

	}

	@Override
	public void retire() throws KnowledgeBaseException {
		// No initialization necessary

	}

}
