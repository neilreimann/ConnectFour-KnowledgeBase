package com.home.neil.knowledgebase.cachesegment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.cachesegment.file.FileCacheSegment;

public class CompressableCacheSegmentFile extends FileCacheSegment implements ICompressableCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = CompressableCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public enum COMPRESSABLECACHESEGMENTFILESTATE {
		INSTANTIATED, MEMORYUNCOMPRESSED, DISKUNCOMPRESSED, MEMORYCOMPRESSED, DISKCOMPRESSED, ERROR
	}

	private COMPRESSABLECACHESEGMENTFILESTATE mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED;

	private final Object mLock = new Object();
	private boolean mCompressedCacheSegmentFileDirty = false;

	private byte[] mDebugUncompressedCacheSegment = null;

	
	private CompressableCacheSegment mCompressableCacheSegment = null;
	

	public CompressableCacheSegmentFile(String pBasePath, String[] pStatePaths, String pFileName, int pCacheSegmentSize)
			throws ConfigurationException {
		super (pBasePath, pStatePaths, pFileName, pCacheSegmentSize);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void init() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED) {
				sLogger.error("KnowledgeBaseFile is not in a instantiated state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}

			byte [] lCompressableCacheSegmentBytes = null;
			
			if (mFullPathFile.exists()) {
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Opening KnowledgeBase: {} ", mFullPathFileName);
				}

				lCompressableCacheSegmentBytes = readCacheSegmentFile();

				mCompressedCacheSegmentFileDirty = false;
			} else {
				mCompressedCacheSegmentFileDirty = true;
			}

			mCompressableCacheSegment = new CompressableCacheSegment(false, lCompressableCacheSegmentBytes, mCacheSegmentSize);
			
			mCompressableCacheSegment.init();

			mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.READY;
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private byte [] readCacheSegmentFile() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		FileInputStream lFileIn = null;
		BufferedInputStream lBIS = null;
		
		byte [] lCompressableCacheSegmentBytes = null;
		try {
			lFileIn = new FileInputStream(mFullPathFileName);
			lBIS = new BufferedInputStream(lFileIn, (int) mFullPathFile.length());

			lCompressableCacheSegmentBytes = new byte[(int) mFullPathFile.length()];

			lBIS.read(lCompressableCacheSegmentBytes);

		} catch (IOException e) {
			throw new CacheSegmentStateException(e);
		} finally {
			
			try {
				if (lBIS != null ) {
					lBIS.close();
				}
				if (lFileIn != null) {
					lFileIn.close();
				}
			} catch (IOException e) {

			}

		}
		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Read Successful: {}", mFullPathFileName);
			sLogger.error("Bytes Read: {}", mFullPathFile.length());
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		
		return lCompressableCacheSegmentBytes;
	}


	public void retire() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Writing KnowledgeBase: " + mFullPathFileName);
		}

		byte[] lCompressedCacheSegment = null;

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}

			mCompressableCacheSegment.retire();

			lCompressedCacheSegment = mCompressableCacheSegment.getCompressedCacheSegment();
			if (mCompressedCacheSegmentFileDirty || mCompressableCacheSegment.isCacheSegmentDirty()) {
				writeCacheSegmentFile(lCompressedCacheSegment, mFullPathFile);
			}

			if (sLogger.isDebugEnabled()) {
				mDebugUncompressedCacheSegment = mCompressableCacheSegment.getRetiredUnCompressedCacheSegment();
				writeCacheSegmentFile(mDebugUncompressedCacheSegment, mFullPathDebugFile);
			}

			mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.RETIRED;

		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	
	private void writeCacheSegmentFile(byte [] pBytesToWrite, File pFullPathFile) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		mFullPathDirectory.mkdirs();

		FileOutputStream lFileOut = null;
		BufferedOutputStream lBOS = null;
		try {
			lFileOut = new FileOutputStream(pFullPathFile);
			lBOS = new BufferedOutputStream(lFileOut);

			lBOS.write(pBytesToWrite, 0, pBytesToWrite.length);

		} catch (IOException e) {
			sLogger.error("Write KnowledgeBaseFile failed!");
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.ERROR;
			throw new CacheSegmentStateException(e);
		} finally {
			if (lBOS != null) {
				try {
					lBOS.flush();
					lBOS.close();
				} catch (Exception e) {
					
				}
			}
			if (lFileOut != null) {
				try {
					lFileOut.flush();
					lFileOut.close();
				} catch (Exception e) {
					
				}
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Write Successful: {}", mFullPathFile);
			sLogger.debug("Bytes Written: {}", mFullPathFile.length());
		}

		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public byte[] getRetiredUnCompressedCacheSegment() throws CacheSegmentStateException {
		return mDebugUncompressedCacheSegment;
	}

	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte [] lScore = mCompressableCacheSegment.readScore(pFileIndex, pSize);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

		return lScore;
	}

	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		mCompressableCacheSegment.writeScore(pFileIndex, pScoreToWrite, pSize);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public boolean isCacheSegmentDirty() {
		return mCompressedCacheSegmentFileDirty || mCompressableCacheSegment.isCacheSegmentDirty();
	}

	public void uncompress() throws IOException, DataFormatException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}
			
			mCompressableCacheSegment.uncompress();
			
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void compress() throws IOException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}
			
			mCompressableCacheSegment.compress();
			
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

}
