package com.home.neil.connectfour.knowledgebase;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.knowledgebase.exception.KnowledgeBaseException;
import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;

public abstract class KnowledgeBaseFileAccessTask implements KnowledgeBaseFileAccessTaskInterface {
	public static final String CLASS_NAME = KnowledgeBaseFileAccessTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));

	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	protected KnowledgeBaseFilePool mKnowledgeBaseFilePool = null;
	protected volatile KnowledgeBaseFile mKnowledgeBaseFileInUse = null;

	protected String mFileDirectory = null;
	protected String mFileLocation = null;
	protected int mFileIndex = 0;
	protected byte mBoardScore = 0;

	protected boolean mTransactionSuccessful = false;
	protected boolean mScoreFound = false;
	protected boolean mTransactionFinished = false;

	protected long mTaskStartTime = 0;
	protected long mTaskEndTime = 0;

	protected String mLogContext = null;
	
	protected String mTaskName = null;
	
	protected Thread mCurrentThread = null;
	
	public String getTaskName () {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mTaskName;
	}
	
	protected abstract void renameTask (String pLogContext);
	
	public KnowledgeBaseFileAccessTask(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pStateString, String pActionString, String pLogContext) throws ConfigurationException, KnowledgeBaseException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (ApplicationPrecompilerSettings.RENAMETASKSINLOGGING) {
			renameTask (pLogContext);
		}
		
		mKnowledgeBaseFilePool = pKnowledgeBaseFilePool;
		
		if (pActionString.startsWith("0")) {
			pActionString = pActionString.substring(1);
		}

		try {
			determineFilesAndIndexes(pStateString, pActionString);
		} catch (ConfigurationException eCE) {
			sLogger.error("Configuration Exception Occurred!");
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace("Exiting");
			}
			throw eCE;
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}


	public KnowledgeBaseFileAccessTask(KnowledgeBaseFilePool pKnowledgeBaseFilePool, KnowledgeBaseFile pKnowledgeBaseFile, String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}

		renameTask(pLogContext);

		mKnowledgeBaseFilePool = pKnowledgeBaseFilePool;
		mKnowledgeBaseFileInUse = pKnowledgeBaseFile;

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public boolean isTransactionFinished() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mTransactionFinished;
	}

	public KnowledgeBaseFile getKnowledgeBaseFileInUse() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mKnowledgeBaseFileInUse;
	}

	public void setKnowledgeBaseFileInUse(KnowledgeBaseFile pKnowledgeBaseFileInUse) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		mKnowledgeBaseFileInUse = pKnowledgeBaseFileInUse;
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	private void determineFilesAndIndexes(String pStateString, String pActionString) throws ConfigurationException, KnowledgeBaseException {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}

			String mNewFileDirectory = null;
			String mNewFileLocation = null;
			int mNewFileIndex = 0;
			int lFileMoveStringCount = pActionString.length() / mKnowledgeBaseFilePool.getActionsPerFile();
			int lFileMoveStringModulus = pActionString.length() % mKnowledgeBaseFilePool.getActionsPerFile();
			//String lNewBoardStateString = null;
			
			String mNewFileIndexString = pActionString.substring(lFileMoveStringCount * mKnowledgeBaseFilePool.getActionsPerFile());
			if (pActionString.length() == 0 ) {
				mNewFileIndex = 0;
				mNewFileDirectory = "";
				mNewFileLocation = "XMove";
			} else if (pActionString.length() < mKnowledgeBaseFilePool.getActionsPerFile()) {
				mNewFileIndex = Integer.parseInt (mNewFileIndexString, 8);
				mNewFileDirectory = "";
				mNewFileLocation = "XMove";
			} else {
/*
				BoardState lBoardStateforMoveFile = pBoardStateToRecord; 
				for (int i = 0; i < lFileMoveStringModulus; i++) {
					lBoardStateforMoveFile = lBoardStateforMoveFile.getParentBoardState();
				}
				
				lNewBoardStateString = lBoardStateforMoveFile.getBoardStateString();
*/				
				if (lFileMoveStringModulus == 0) {
					mNewFileIndex = 0;
				} else {
					mNewFileIndex = Integer.parseInt(mNewFileIndexString, 8);
				}
				
					
				mNewFileDirectory = "L" + lFileMoveStringCount;
				mNewFileLocation = "XMove" + pStateString;
			}
			
		mFileDirectory = mNewFileDirectory;
		mFileLocation = mNewFileLocation;
		mFileIndex = mNewFileIndex;
		

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("MoveString: " + pActionString + " File Directory: " + mFileDirectory + " File Location: " + mFileLocation + " FileIndex = " + mFileIndex);
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}

	}

	public abstract void executeTask ();

	public boolean isTransactionSuccessful() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mTransactionSuccessful;
	}

	public boolean isScoreFound() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mScoreFound;
	}

	public byte getBoardScore() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mBoardScore;
	}
}
