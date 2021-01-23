package com.home.neil.knowledgebase.file;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;

public class CacheSegmentFile implements ICacheSegmentFile {
	public static final String CLASS_NAME = CacheSegmentFile.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected String mBasePath = null;
	protected String[] mStatePaths = null;
	protected String mFileName = null;
	
	protected String mFullPathDirectoryName = null;
	protected String mFullPathFileName = null;
	protected String mFullPathDebugFileName = null;

	protected static final String KNOWLEDGEBASEFILE_EXT = ".zip";
	protected static final String KNOWLEDGEBASEFILE_DAT = ".dat";

	protected File mFullPathFile = null;
	protected File mFullPathDirectory = null;
	protected File mFullPathDebugFile = null;
	
	protected int mCacheSegmentSize = 0;
	
	public CacheSegmentFile(String pBasePath, String[] pStatePaths, String pFileName, int pCacheSegmentSize) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		mBasePath = pBasePath;
		mStatePaths = pStatePaths;
		mFileName = pFileName;
		mFullPathFileName = mBasePath;

		for (String lStatePath : mStatePaths) {
			mFullPathFileName += "/" + lStatePath;
		}
		mFullPathDirectoryName = mFullPathFileName;
		mFullPathFileName += "/" + mFileName + KNOWLEDGEBASEFILE_EXT;
		mFullPathDebugFileName += "/" + mFileName + KNOWLEDGEBASEFILE_DAT;
		
		mFullPathDirectory = new File(mFullPathDirectoryName);
		mFullPathFile = new File(mFullPathFileName);
		mFullPathDebugFile = new File (mFullPathDebugFileName);

		mCacheSegmentSize = pCacheSegmentSize;
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public String getBasePath() {
		return mBasePath;
	}

	public String[] getStatePaths() {
		return mStatePaths;
	}

	public String getFileName() {
		return mFileName;
	}

	public int getCacheSegmentSize() {
		return mCacheSegmentSize;
	}
	
	
	
	
}
