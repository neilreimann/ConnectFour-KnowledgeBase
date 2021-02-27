package com.home.neil.knowledgebase.cachesegment.threads.initialization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentPoolItemIndexEntry;
import com.home.neil.knowledgebase.cachesegment.threads.operations.CompressableCacheSegmentOperationsTask;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.initialization.PoolItemInitializationThread;

public class CompressableCacheSegmentInitializationThread extends PoolItemInitializationThread {
	public static final String CLASS_NAME = CompressableCacheSegmentInitializationThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected CompressableCacheSegmentInitializationThread(IPool pPool, String pPoolItemId,
			CompressableCacheSegmentOperationsTask pPoolItemOperationsTask, String pLogContext) {
		super(pPool, pLogContext, true);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		mAppTask = new CompressableCacheSegmentInitializationTask (pPool, pPoolItemId, pPoolItemOperationsTask, pLogContext);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	protected CompressableCacheSegmentInitializationThread(IPool pPool, CompressableCacheSegmentPoolItemIndexEntry pPoolItem,
			String pLogContext) {
		super(pPool, pLogContext, true);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		mAppTask = new CompressableCacheSegmentInitializationTask (pPool, pPoolItem, pLogContext);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	
	
	@Override
	public IPoolItem getPoolItem() {
		return ((CompressableCacheSegmentInitializationTask) mAppTask).getPoolItem();
	}
}
