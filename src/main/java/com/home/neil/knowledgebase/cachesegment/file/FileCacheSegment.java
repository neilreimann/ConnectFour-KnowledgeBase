package com.home.neil.knowledgebase.cachesegment.file;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.cachesegment.IReadWriteCacheSegment;
import com.home.neil.knowledgebase.cachesegment.IStorageCacheSegment;

public class FileCacheSegment implements IFileCacheSegment, IStorageCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = FileCacheSegment.class.getName();
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
	
	public FileCacheSegment(String pBasePath, String pFileName, int pCacheSegmentSize) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
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
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
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
