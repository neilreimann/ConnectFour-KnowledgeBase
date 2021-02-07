package com.home.neil.connectfour.knowledgebase.old;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.zip.DataFormatException;

import javax.management.openmbean.OpenDataException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.connectfour.knowledgebase.old.exception.KnowledgeBaseException;
import com.home.neil.thread.performancemetrics.ThreadPerformanceMetricsMBean;

public class KnowledgeBaseFileAccessWriteTask extends KnowledgeBaseFileAccessTask {
	public static final String SIMPLE_CLASS_NAME = KnowledgeBaseFileAccessWriteTask.class.getSimpleName();
	public static final String CLASS_NAME = KnowledgeBaseFileAccessWriteTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public static final boolean sLogMetrics = false;

	protected static int sTaskNumber = 0;

	private synchronized void incTaskNumber() {
		sTaskNumber++;
	}

	protected void renameTask(String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		incTaskNumber();

		mTaskName = SIMPLE_CLASS_NAME + "." + sTaskNumber;

		if (pLogContext == null || pLogContext.trim().isEmpty()) {
			mLogContext = SIMPLE_CLASS_NAME + "." + sTaskNumber;
			ThreadContext.put("LogContext", mLogContext);
		} else {
			mLogContext = pLogContext;
			ThreadContext.put("LogContext", mLogContext);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
	}

	public KnowledgeBaseFileAccessWriteTask(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pStateString, String pActionString, byte pBoardScore, String pLogContext) throws ConfigurationException, KnowledgeBaseException {
		super(pKnowledgeBaseFilePool, pStateString, pActionString, pLogContext);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}

		if (pBoardScore == 0) {
			mBoardScore = 120;
		} else {
			mBoardScore = pBoardScore;
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
	}

	public void executeTask() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (sLogMetrics) {
			mTaskStartTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Task: " + getTaskName() + " is starting at " + mTaskStartTime);
		}

		try {
			try {
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Update file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex));
				}
				updateScore();
			} catch (IOException eIO) {
				sLogger.error("Update file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " IO Exception occurred!");
				sLogger.error("IOException Occurred when reading score: Message: " + eIO.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIO.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);

				mTransactionSuccessful = false;
			} catch (KnowledgeBaseException eKBE) {
				sLogger.error("Update file:  " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " Knowledge Base Exception occurred!");
				sLogger.error("Knowledge Base Occurred when reading score: Message: " + eKBE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eKBE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);

				mTransactionSuccessful = false;
			} catch (ConfigurationException eCE) {
				sLogger.error("Update file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " Knowledge Base Exception occurred!");
				sLogger.error("Knowledge Base Occurred when reading score: Message: " + eCE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eCE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);

				mTransactionSuccessful = false;
			}

		} catch (Exception eE) {
			sLogger.error("Unanticipated Exception occurred during Thread Execution!");
			sLogger.error("Exception Message: " + eE.getMessage());

			StringWriter lSW = new StringWriter();
			PrintWriter lPW = new PrintWriter(lSW);
			eE.printStackTrace(lPW);
			lSW.toString(); // stack trace as a string
			sLogger.error("StackTrace: " + lSW);

			mTransactionSuccessful = false;
		}

		mTransactionFinished = true;
		if (sLogMetrics) {

			mTaskEndTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Thread: " + getTaskName() + " is ending at " + mTaskEndTime);
			long lDuration = mTaskEndTime - mTaskStartTime;

			try {
				ThreadPerformanceMetricsMBean lThreadPerformanceMetricsMBean = ThreadPerformanceMetricsMBean.getInstance();
				lThreadPerformanceMetricsMBean.updateThreadStatistics(SIMPLE_CLASS_NAME, "executeTask()", mTaskStartTime, lDuration, mTransactionSuccessful);
			} catch (OpenDataException eODE) {
				sLogger.error("Open Data Exception occurred during Thread Execution!");
				sLogger.error("Exception Message: " + eODE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eODE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);
			} catch (Exception eE) {
				sLogger.error("Exception Message: " + eE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);
			}
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
	}

	public void updateScore() throws IOException, DataFormatException, KnowledgeBaseException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		
		mCurrentThread = Thread.currentThread();
		KnowledgeBaseFilePool lKnowledgeBaseFilePool = KnowledgeBaseFilePool.getMasterInstance();
		boolean lGotKnowledgeBaseFile = lKnowledgeBaseFilePool.reserveKnowledgeBaseFile(mFileDirectory, mFileLocation, this);
		if (!lGotKnowledgeBaseFile) {
			while (mKnowledgeBaseFileInUse == null) {
				//sLogger.error("spinlock");
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException eIE) {
//					// sLogger.debug("Unanticipated Interrupt exception occurred!");
//					//
//					// StringWriter lSW = new StringWriter();
//					// PrintWriter lPW = new PrintWriter(lSW);
//					// eIE.printStackTrace(lPW);
//					// lSW.toString(); // stack trace as a string
//					// sLogger.debug("StackTrace: " + lSW);
//
//					// Thread.currentThread().interrupt();
//				}
			}
		}

		if (mKnowledgeBaseFileInUse == null) {
			sLogger.error("Update file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " COULD NOT GET THE FILES RESERVED!");
			mTransactionSuccessful = false;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.debug("Exiting");
			}
			return;
		}

		mKnowledgeBaseFileInUse.writeScore(mFileIndex, mBoardScore);
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Update file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " Score written: " + mBoardScore);
		}
		lKnowledgeBaseFilePool.releaseKnowledgeBaseFile(mKnowledgeBaseFileInUse);

		mTransactionSuccessful = true;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
		return;
	}

	public boolean isTransactionSuccessful() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
		return mTransactionSuccessful;
	}

	public boolean isScoreFound() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Exiting");
		}
		return mScoreFound;
	}

	public byte getBoardScore() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mBoardScore;
	}

	public void interrupt() {
		sLogger.trace("Entering");
		mCurrentThread.interrupt();
		sLogger.trace("Exiting");
	}

	@Override
	public String getWaitingThreadName() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mCurrentThread.getName();
	}

}
