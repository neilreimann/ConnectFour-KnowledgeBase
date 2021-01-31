package com.home.neil.knowledgebase.cachesegment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;

public class CompressableCacheSegment implements ICompressableCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = CompressableCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public enum COMPRESSABLE_CACHESTATE {
		INSTANTIATED, COMPRESSED, UNCOMPRESSED, RETIRED, ERROR
	}

	private COMPRESSABLE_CACHESTATE mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.INSTANTIATED;

	private byte[] mCompressableCacheSegment = null;
	private CacheSegment mCacheSegment = null;
	private final Object mMemoryCompressionLock = new Object();
	private int mCacheSegmentSize = 0;
	private boolean mCompressableCacheSegmentDirty = false;

	private byte[] mDebugCacheSegment = null;

	private boolean mThreadSafe = false;

	public CompressableCacheSegment(boolean pThreadSafe, byte[] pInitialCacheSegment, int pCacheSegmentSize) {
		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.INSTANTIATED;

		mThreadSafe = pThreadSafe;
		
		mCompressableCacheSegment = pInitialCacheSegment;

		mCacheSegmentSize = pCacheSegmentSize;
	}

	@Override
	public void init() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mMemoryCompressionLock) {
			if (mThreadSafe) {
				synchronized (mMemoryCompressionLock) {
					initCritical();
				}
			} else {
				initCritical();
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	
	private void initCritical () throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCompressableCacheSegmentState != COMPRESSABLE_CACHESTATE.INSTANTIATED) {
			sLogger.error("Cache Segment is not in a instantiated state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.COMPRESSED;

		try {
			if (mCompressableCacheSegment == null) {
				initializeFirstTimeSegment();
			} else {
				uncompress();
			}
			
		} catch (IOException e) {
			sLogger.error("Cache Segment is not in a instantiated state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);

			mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.ERROR;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(e);
		} catch (DataFormatException e) {
			mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.ERROR;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(e);
		} catch (CacheSegmentStateException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	@Override
	public void retire() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mMemoryCompressionLock) {
			if (mThreadSafe) {
				synchronized (mMemoryCompressionLock) {
					retireCritical();
				}
			} else {
				retireCritical();
			}
		}
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	private void retireCritical () throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mMemoryCompressionLock != COMPRESSABLE_CACHESTATE.COMPRESSED
				&& mMemoryCompressionLock != COMPRESSABLE_CACHESTATE.UNCOMPRESSED) {
			sLogger.error("Cache Segment is not in a compressed or uncompressed state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		if (mMemoryCompressionLock == COMPRESSABLE_CACHESTATE.UNCOMPRESSED) {
			try {
				compress();
			} catch (IOException e) {
				mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.ERROR;
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new CacheSegmentStateException(e);
			}
		}

		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.RETIRED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	

	public byte[] getRetiredUnCompressedCacheSegment() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		if (mMemoryCompressionLock != COMPRESSABLE_CACHESTATE.RETIRED) {
			sLogger.error("Cache Segment is not in a retired state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return mDebugCacheSegment;
	}

	@Override
	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte[] lReadScore = null;

		if (mCompressableCacheSegmentState != COMPRESSABLE_CACHESTATE.UNCOMPRESSED) {
			sLogger.error("Cache Segment is not in a uncompressed state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		lReadScore = mCacheSegment.readScore(pFileIndex, pSize);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lReadScore;
	}

	@Override
	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCompressableCacheSegmentState != COMPRESSABLE_CACHESTATE.UNCOMPRESSED) {
			sLogger.error("Cache Segment is not in a uncompressed state!  GO AWAY! State: {} ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		mCacheSegment.writeScore(pFileIndex, pScoreToWrite, pSize);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void uncompress() throws IOException, DataFormatException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Decompressing Memory");
		}

		synchronized (mMemoryCompressionLock) {
			if (mThreadSafe) {
				synchronized (mMemoryCompressionLock) {
					uncompressCritical();
				}
			} else {
				uncompressCritical();
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Decompression Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void initializeFirstTimeSegment() throws IOException, DataFormatException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Initializing Uncompressed Memory");
		}

		synchronized (mMemoryCompressionLock) {
			if (mThreadSafe) {
				synchronized (mMemoryCompressionLock) {
					initializeFirstTimeSegmentCritical();
				}
			} else {
				initializeFirstTimeSegmentCritical();
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Uncompressed Memory Initialization Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void initializeFirstTimeSegmentCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte [] lUncompressedCacheSegment = new byte [mCacheSegmentSize];
		
		mCacheSegment = new CacheSegment(false, lUncompressedCacheSegment);

		mCacheSegment.init();

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Uncompressed: {} bytes", mCacheSegmentSize);
		}

		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.UNCOMPRESSED;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}
	
	
	private void uncompressCritical () throws IOException, DataFormatException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCompressableCacheSegmentState != COMPRESSABLE_CACHESTATE.COMPRESSED) {
			sLogger.error("Cache Segment is not in a compressed state!  GO AWAY! State: %s ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		Inflater lInflater = new Inflater();

		lInflater.setInput(mCompressableCacheSegment);

		ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(mCacheSegmentSize);

		byte[] lBuffer = new byte[1024];

		while (!lInflater.finished()) {

			int count = lInflater.inflate(lBuffer); // returns the generated code... index
			lBAOS.write(lBuffer, 0, count);
		}

		lBAOS.close();

		mCacheSegment = new CacheSegment(false, lBAOS.toByteArray());

		mCacheSegment.init();

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Original: {} bytes", mCompressableCacheSegment.length);
			sLogger.debug("Uncompressed: {} bytes", mCacheSegmentSize);
		}

		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.UNCOMPRESSED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	
	
	public void compress() throws IOException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compressing Memory");
		}

		synchronized (mMemoryCompressionLock) {
			if (mThreadSafe) {
				synchronized (mMemoryCompressionLock) {
					compressCritical();
				}
			} else {
				compressCritical();
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Compression Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void compressCritical() throws IOException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compressing Memory");
		}

		if (mCompressableCacheSegmentState != COMPRESSABLE_CACHESTATE.UNCOMPRESSED) {
			sLogger.error("Cache Segment is not in a uncompressed state!  GO AWAY! State: %s ",
					mCompressableCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		mCacheSegment.retire();
		
		if (sLogger.isDebugEnabled()) {
			mDebugCacheSegment = mCacheSegment.getCacheSegment();
		}

		if (mCacheSegment.isCacheSegmentDirty()) {

			sLogger.debug("Compressing Dirty Cache Segment");

			Deflater lDeflater = new Deflater();

			mCacheSegment = null;

			lDeflater.setInput(mDebugCacheSegment);

			ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(mCacheSegmentSize);

			lDeflater.finish();

			byte[] lBuffer = new byte[1024];

			while (!lDeflater.finished()) {

				int count = lDeflater.deflate(lBuffer); // returns the generated code... index

				lBAOS.write(lBuffer, 0, count);

			}

			lBAOS.close();

			mCompressableCacheSegment = lBAOS.toByteArray();

			mCompressableCacheSegmentDirty = true;
		} else {
			sLogger.debug("No Need to Compress, Cache Segment was clean");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Original: {} bytes", mCacheSegmentSize);
			sLogger.debug("Compressed: {} bytes", mCompressableCacheSegment.length + " bytes");
		}

		mCacheSegment.retire();
		
		mCacheSegment = null;

		mCompressableCacheSegmentState = COMPRESSABLE_CACHESTATE.COMPRESSED;

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Compression Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}


	
	public boolean isCacheSegmentDirty() {
		return mCompressableCacheSegmentDirty;
	}

	public byte [] getCompressedCacheSegment () {
		return mCompressableCacheSegment;
	}
	
}
