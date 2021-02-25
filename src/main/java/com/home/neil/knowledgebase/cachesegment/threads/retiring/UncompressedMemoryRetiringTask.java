package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.retiring.PoolItemRetiringTask;
import com.home.neil.thread.SteppedThrottledAppThread;

public class UncompressedMemoryRetiringTask extends PoolItemRetiringTask {
	public static final String CLASS_NAME = UncompressedMemoryRetiringTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected UncompressedMemoryRetiringTask(IPool pPool, SteppedThrottledAppThread pSteppedThrottledAppThread,
			String pLogContext, boolean pRecordTaskStatistics) {
		super(pPool, pSteppedThrottledAppThread, pLogContext, pRecordTaskStatistics);
		
	}

	@Override
	public IPoolItem retirePoolItem(IPoolItem pPoolItem) throws KnowledgeBaseException {
		MemoryCacheSegmentPoolItemIndexEntry lPoolItem = (MemoryCacheSegmentPoolItemIndexEntry) pPoolItem;
		
		lPoolItem.retire();
		
		//TODO I know it seems bare but when we have multiple levels, we'll need to rebuild the Pool Item to it's retired version for now it's null
		return null;
	}

}
