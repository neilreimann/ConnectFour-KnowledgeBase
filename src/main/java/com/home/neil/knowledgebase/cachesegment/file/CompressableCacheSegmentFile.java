package com.home.neil.knowledgebase.cachesegment.file;

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

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegment;
import com.home.neil.knowledgebase.cachesegment.ICompressableCacheSegment;
import com.home.neil.knowledgebase.file.CacheSegmentFile;

public class CompressableCacheSegmentFile extends CacheSegmentFile implements ICompressableCacheSegment {
	public static final String CLASS_NAME = CompressableCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public enum COMPRESSABLECACHESEGMENTFILESTATE {
		INSTANTIATED, READY, RETIRED, ERROR
	}

	private COMPRESSABLECACHESEGMENTFILESTATE mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED;

	private final Object mLock = new Object();
	private boolean mCompressedCacheSegmentFileDirty = false;

	private byte[] mDebugUncompressedCacheSegment = null;

	
	private CompressableCacheSegment mCompressableCacheSegment = null;
	

	public CompressableCacheSegmentFile(String pBasePath, String[] pStatePaths, String pFileName, int pCacheSegmentSize)
			throws ConfigurationException {
		super (pBasePath, pStatePaths, pFileName, pCacheSegmentSize);
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED;

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public void init() throws CacheSegmentStateException, IOException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.INSTANTIATED) {
				sLogger.error("KnowledgeBaseFile is not in a instantiated state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
					sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
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

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	private byte [] readCacheSegmentFile() throws IOException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
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
			throw e;
		} finally {
			lBIS.close();
			lFileIn.close();
		}
		

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Read Successful: {}", mFullPathFileName);
			sLogger.error("Bytes Read: {}", mFullPathFile.length());
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		
		return lCompressableCacheSegmentBytes;
	}


	public byte[] retire() throws CacheSegmentStateException, IOException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Writing KnowledgeBase: " + mFullPathFileName);
		}

		byte[] lCompressedCacheSegment = null;

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
					sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}

			lCompressedCacheSegment = mCompressableCacheSegment.retire();

			if (mCompressedCacheSegmentFileDirty || mCompressableCacheSegment.isCacheSegmentDirty()) {
				writeCacheSegmentFile(lCompressedCacheSegment, mFullPathFile);
			}

			if (sLogger.isDebugEnabled()) {
				mDebugUncompressedCacheSegment = mCompressableCacheSegment.getRetiredDebugCacheSegment();
				writeCacheSegmentFile(mDebugUncompressedCacheSegment, mFullPathDebugFile);
			}

			mCompressableCacheSegmentFileState = COMPRESSABLECACHESEGMENTFILESTATE.RETIRED;

		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		return lCompressedCacheSegment;
	}

	
	private void writeCacheSegmentFile(byte [] pBytesToWrite, File pFullPathFile) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
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
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
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

		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public byte[] getRetiredDebugCacheSegment() throws CacheSegmentStateException {
		return mDebugUncompressedCacheSegment;
	}

	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		byte [] lScore = mCompressableCacheSegment.readScore(pFileIndex, pSize);
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}

		return lScore;
	}

	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		mCompressableCacheSegment.writeScore(pFileIndex, pScoreToWrite, pSize);

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public boolean isCacheSegmentDirty() {
		return mCompressedCacheSegmentFileDirty || mCompressableCacheSegment.isCacheSegmentDirty();
	}

	public void uncompress() throws IOException, DataFormatException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
					sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}
			
			mCompressableCacheSegment.uncompress();
			
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public void compress() throws IOException, CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		synchronized (mLock) {
			if (mCompressableCacheSegmentFileState != COMPRESSABLECACHESEGMENTFILESTATE.READY) {
				sLogger.error("KnowledgeBaseFile is not in a ready state!  GO AWAY! State: {} ",
						mCompressableCacheSegmentFileState);
				if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
					sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
				}
				throw new CacheSegmentStateException();
			}
			
			mCompressableCacheSegment.compress();
			
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}

	}

}
