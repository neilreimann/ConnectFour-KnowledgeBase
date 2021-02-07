package com.home.neil.knowledgebase.cachesegment.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.cachesegment.IReadWriteCacheSegment;
import com.home.neil.knowledgebase.index.IIndexEntry;
import com.home.neil.knowledgebase.pool.IPoolItem;

public class MemoryCacheSegmentPoolItemIndexEntry extends MemoryCacheSegment implements IIndexEntry, IPoolItem, IReadWriteCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = MemoryCacheSegmentPoolItemIndexEntry.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private String mPoolItemId = null;
	
	public MemoryCacheSegmentPoolItemIndexEntry (String pPoolItemId, boolean pThreadSafe, byte [] pCacheSegment) {
		super (pThreadSafe, pCacheSegment);
		mPoolItemId = pPoolItemId;
		setIndexEntry ();
	}
	
	private long mIndex = 0;
	private static long sIndex = 0;
	
	public long setIndexEntry() {
		mIndex = getNextIndex ();
		return mIndex;
	}

	public long getIndexEntry() {
		return mIndex;
	}

	private static synchronized long getNextIndex () {
		long lIndex = sIndex++;
		return lIndex;
	}

	@Override
	public String getPoolItemId() {
		return mPoolItemId;
	}
}
