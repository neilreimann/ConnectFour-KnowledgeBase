package com.home.neil.knowledgebase.cachesegment.threads.initialization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentPoolItemIndexEntry;
import com.home.neil.knowledgebase.cachesegment.threads.operations.CompressableCacheSegmentOperationsTask;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThreadFactory;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public class CompressableCacheSegmentInitializationThreadFactory implements IPoolItemInitializationThreadFactory {
	public static final String CLASS_NAME = CompressableCacheSegmentInitializationThreadFactory.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public CompressableCacheSegmentInitializationThreadFactory() {
		// Do Nothing, this is just a factory class
	}

	public IPoolItemInitializationThread getInitializationThread(IPool pPool, String pPoolItemId, IPoolItemOperationsTask pPoolItemOperationsTask) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lAppInitializationTask = new CompressableCacheSegmentInitializationThread(pPool, pPoolItemId,
				(CompressableCacheSegmentOperationsTask) pPoolItemOperationsTask, null);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lAppInitializationTask;

	}

	public IPoolItemInitializationThread getInitializationThread(IPool pPool, IPoolItem pPoolItem) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lAppInitializationTask = new CompressableCacheSegmentInitializationThread(pPool,
				(CompressableCacheSegmentPoolItemIndexEntry) pPoolItem, null);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lAppInitializationTask;

	}

}
