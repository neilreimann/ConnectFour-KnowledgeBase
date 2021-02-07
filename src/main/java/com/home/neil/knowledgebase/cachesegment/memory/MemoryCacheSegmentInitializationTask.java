package com.home.neil.knowledgebase.cachesegment.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.initialization.PoolItemInitializationTask;
import com.home.neil.task.TaskException;

public class MemoryCacheSegmentInitializationTask extends PoolItemInitializationTask{

	public static final String CLASS_NAME = MemoryCacheSegmentInitializationTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private IPoolItem mOrigPoolItem = null;
	private String mPoolItemId = null;
	
	//This constructor is for when there is a template Pool Item
	protected MemoryCacheSegmentInitializationTask (IPool pPool, IPoolItem pOrigPoolItem, String pLogContext, boolean pRecordThreadStatistics) {
		super(pPool, pLogContext, pRecordThreadStatistics);

		mOrigPoolItem = pOrigPoolItem;
	}
	
	//This constructor is for when there is a no template Pool Item
	protected MemoryCacheSegmentInitializationTask (IPool pPool, String pPoolItemId, String pLogContext, boolean pRecordThreadStatistics) {
		super(pPool, pLogContext, pRecordThreadStatistics);

		mPoolItemId = pPoolItemId;
	}

	@Override
	protected void executeTask() throws TaskException {
		try {
			initPoolItem (mOrigPoolItem);
		} catch (KnowledgeBaseException e) {
			throw new TaskException(e.getMessage(), e);
		}
	}

	@Override
	public IPoolItem initPoolItem(IPoolItem pOrigPoolItem) throws KnowledgeBaseException {
		//TODO  initializing the pCacheSegment from Original Pool Object - FileCacheSEgmentPoolItemIndexEntry
		byte [] lCacheSegment = new byte [10000]; // Make a temporary cache segment for now until FileCacheSEgmentPoolItemIndexEntry implemented
		
		mPoolItem = new MemoryCacheSegmentPoolItemIndexEntry (pOrigPoolItem.getPoolItemId(), false, lCacheSegment);
		
		mPoolItem.init();

		return mPoolItem;
	}

	@Override
	public IPoolItem initPoolItem(String pOrigPoolItemId) throws KnowledgeBaseException {
		// MemoryCacheSegments are always created from FileCacheSegmentsPoolItemIndexEntry, thus this function should never execute 
		//throw new KnowledgeBaseException ("Attempting to create Memory Cache Segment from Scratch");

		byte [] lCacheSegment = new byte [10000]; // Make a temporary cache segment for now until FileCacheSEgmentPoolItemIndexEntry implemented

		mPoolItem = new MemoryCacheSegmentPoolItemIndexEntry (mPoolItemId, false, lCacheSegment);

		mPoolItem.init();
		
		return mPoolItem;
	}
}
