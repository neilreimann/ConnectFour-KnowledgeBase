package com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;

public class CacheSegment implements ICacheSegment {
	public static final String CLASS_NAME = CacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	public static final String TRACE_ENTERING = "Entering";
	public static final String TRACE_EXITING = "Exiting";

	public enum CACHESTATE {
		INSTANTIATED, READY, RETIRED
	}

	private CACHESTATE mCacheSegmentState = CACHESTATE.INSTANTIATED;
	private byte[] mCacheSegment = null;
	private boolean mCacheSegmentDirty = false;
	private final Object mCacheSegmentStateLock = new Object();

	private boolean mThreadSafe = false;

	public CacheSegment(boolean pThreadSafe, byte[] pInitialCacheSegment) {
		mThreadSafe = pThreadSafe;
		mCacheSegment = pInitialCacheSegment;		
		mCacheSegmentState = CACHESTATE.INSTANTIATED;
	}

	public void init() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				initCritical();
			}
		} else {
			initCritical();
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}


	private void initCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		if (mCacheSegmentState != CACHESTATE.INSTANTIATED) {
			sLogger.error("Cache Segment is not in a instantiated state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		mCacheSegmentState = CACHESTATE.READY;

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public byte[] retire() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		byte[] lCacheSegment = null;
		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				lCacheSegment = retireCritical();
			}
		} else {
			lCacheSegment = retireCritical();
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		return lCacheSegment;
	}

	private byte[] retireCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		byte[] lCacheSegment = null;
		if (mCacheSegmentState != CACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		mCacheSegmentState = CACHESTATE.RETIRED;
		lCacheSegment = mCacheSegment;
		mCacheSegment = null;

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		return lCacheSegment;
	}

	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		byte[] lScoreRead = new byte[pSize];

		if (mCacheSegmentState != CACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		for (int i = 0; i < pSize; i++) {
			lScoreRead[i] = mCacheSegment[pFileIndex + i];
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Cache Segment Write Bytes: {} at: {}", lScoreRead[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}

		return lScoreRead;
	}


	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		if (mCacheSegmentState != CACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {}.", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		for (int i = 0; i < pSize; i++) {
			if (mCacheSegment[pFileIndex + i] != pScoreToWrite[i]) {
				mCacheSegment[pFileIndex + i] = pScoreToWrite[i];
				mCacheSegmentDirty = true;
			}
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Cache Segment Write Bytes: {} at: {}", pScoreToWrite[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public boolean isCacheSegmentDirty() {
		return mCacheSegmentDirty;
	}

}
