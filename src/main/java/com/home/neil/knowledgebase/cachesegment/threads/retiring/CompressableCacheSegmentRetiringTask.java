package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegment;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentPoolItemIndexEntry;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.retiring.PoolItemRetiringTask;
import com.home.neil.thread.SteppedThrottledAppThread;

public class CompressableCacheSegmentRetiringTask extends PoolItemRetiringTask {
	public static final String CLASS_NAME = CompressableCacheSegmentRetiringTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	protected CompressableCacheSegmentRetiringTask(IPool pPool, int pSubPoolLevel, SteppedThrottledAppThread pSteppedThrottledAppThread, String pLogContext,
			boolean pRecordTaskStatistics) {
		super(pPool, pSubPoolLevel, pSteppedThrottledAppThread, pLogContext, pRecordTaskStatistics);

	}

	@Override
	public IPoolItem retirePoolItem(IPoolItem pPoolItem) throws KnowledgeBaseException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegmentPoolItemIndexEntry lPoolItem = (CompressableCacheSegmentPoolItemIndexEntry) pPoolItem;

		CompressableCacheSegment lCompressableCacheSegment = lPoolItem.getCompressableCacheSegment();
		switch (mSubPoolLevel) {
		case 0:
			lCompressableCacheSegment.saveUncompressedMemoryToUncompressedFile();
			break;
		case 1:
			lCompressableCacheSegment.saveUncompressedFileToCompressedMemory();
			break;
		case 2:
			lCompressableCacheSegment.saveCompressedMemoryToCompressedFile();
			break;
		default:
			lCompressableCacheSegment.retire();
			break;
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return null;
	}

}
