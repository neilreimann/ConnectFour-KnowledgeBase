package com.home.neil.knowledgebase.cachesegment.memory;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.IReadWriteCacheSegment;
import com.home.neil.knowledgebase.cachesegment.IStorageCacheSegment;

public class MemoryCacheSegment implements IReadWriteCacheSegment, IStorageCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = MemoryCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);


	public enum MEMORYCACHESTATE {
		INSTANTIATED, READY, RETIRED
	}

	private MEMORYCACHESTATE mCacheSegmentState = MEMORYCACHESTATE.INSTANTIATED;
	private byte[] mCacheSegmentBytes = null;
	private boolean mCacheSegmentDirty = false;
	
	private final Object mCacheSegmentStateLock = new Object();
	private boolean mThreadSafe = false;

	public MemoryCacheSegment(boolean pThreadSafe, byte [] pCacheSegment) {
		mThreadSafe = pThreadSafe;
		mCacheSegmentBytes = pCacheSegment;		
		mCacheSegmentState = MEMORYCACHESTATE.INSTANTIATED;
	}

	public void init() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				initCritical();
			}
		} else {
			initCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}


	private void initCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != MEMORYCACHESTATE.INSTANTIATED) {
			sLogger.error("Cache Segment is not in a instantiated state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		mCacheSegmentState = MEMORYCACHESTATE.READY;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void retire() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				retireCritical();
			}
		} else {
			retireCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void retireCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != MEMORYCACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		mCacheSegmentState = MEMORYCACHESTATE.RETIRED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	@Override
	public byte[] getRetiredBytes() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		if (mCacheSegmentState != MEMORYCACHESTATE.RETIRED) {
			sLogger.error("Cache Segment is not in a retired state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return mCacheSegmentBytes;
	}

	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte[] lScoreRead = new byte[pSize];

		if (mCacheSegmentState != MEMORYCACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {} ", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		for (int i = 0; i < pSize; i++) {
			lScoreRead[i] = mCacheSegmentBytes[pFileIndex + i];
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Cache Segment Write Bytes: {} at: {}", lScoreRead[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

		return lScoreRead;
	}


	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != MEMORYCACHESTATE.READY) {
			sLogger.error("Cache Segment is not in a ready state!  GO AWAY! State: {}.", mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		for (int i = 0; i < pSize; i++) {
			if (mCacheSegmentBytes[pFileIndex + i] != pScoreToWrite[i]) {
				mCacheSegmentBytes[pFileIndex + i] = pScoreToWrite[i];
				mCacheSegmentDirty = true;
			}
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Cache Segment Write Bytes: {} at: {}", pScoreToWrite[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public boolean isCacheSegmentDirty() {
		return mCacheSegmentDirty;
	}

	public byte [] getCacheSegment() {
		return mCacheSegmentBytes;
	}

	
}
