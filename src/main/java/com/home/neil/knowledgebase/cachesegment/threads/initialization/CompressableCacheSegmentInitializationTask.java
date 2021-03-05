package com.home.neil.knowledgebase.cachesegment.threads.initialization;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appconfig.AppConfig;
import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegment;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentConfig;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentPoolItemIndexEntry;
import com.home.neil.knowledgebase.cachesegment.threads.operations.CompressableCacheSegmentOperationsTask;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.initialization.PoolItemInitializationTask;
import com.home.neil.task.TaskException;

public class CompressableCacheSegmentInitializationTask extends PoolItemInitializationTask {
	public static final String CLASS_NAME = CompressableCacheSegmentInitializationTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public CompressableCacheSegmentInitializationTask(IPool pPool, String pPoolItemId, CompressableCacheSegmentOperationsTask pPoolItemOperationsTask,
			String pLogContext) {
		super(pPool, pPoolItemId, pPoolItemOperationsTask, pLogContext, true);

		mPoolItem = new CompressableCacheSegmentPoolItemIndexEntry(mPoolItemId, null);

	}

	public CompressableCacheSegmentInitializationTask(IPool pPool, CompressableCacheSegmentPoolItemIndexEntry pPoolItem, String pLogContext) {
		super(pPool, pPoolItem, pLogContext, true);
	}

	private void executeInitialization(CompressableCacheSegmentPoolItemIndexEntry pPoolItem) throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegmentConfig lConfig = null;

		try {
			lConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException | IOException e) {

			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new TaskException(e);
		}

		CompressableCacheSegmentOperationsTask lTask = (CompressableCacheSegmentOperationsTask) mPoolItemOperationsTask;

		CompressableCacheSegment lCompressableCacheSegment = new CompressableCacheSegment(lConfig, lTask.getStatePaths(), lTask.getFileName());

		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new TaskException(e);
		}

		pPoolItem.setCompressableCacheSegment(lCompressableCacheSegment);

		mPool.initPoolItemThreadCallback((IPoolItem) pPoolItem);

		mTaskSuccessful = true;
		mTaskFinished = true;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void executeReinitialization(CompressableCacheSegmentPoolItemIndexEntry pPoolItem) throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegment lCompressableCacheSegment = pPoolItem.getCompressableCacheSegment();

		try {
			lCompressableCacheSegment.reinit();
		} catch (CacheSegmentStateException e) {
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new TaskException(e);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	protected void executeTask() throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegmentPoolItemIndexEntry lPoolItem = (CompressableCacheSegmentPoolItemIndexEntry) mPoolItem;

		if (lPoolItem.getCompressableCacheSegment() == null) {
			executeInitialization(lPoolItem);
		} else {
			executeReinitialization(lPoolItem);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

}
