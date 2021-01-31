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

import com.home.neil.connectfour.knowledgebase.old.exception.KnowledgeBaseException;
import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.connectfour.performancemetrics.ThreadPerformanceMetricsMBean;

public class KnowledgeBaseFileAccessReadTask extends KnowledgeBaseFileAccessTask {
	public static final String SIMPLE_CLASS_NAME = KnowledgeBaseFileAccessReadTask.class.getSimpleName();
	public static final String CLASS_NAME = KnowledgeBaseFileAccessReadTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public static final boolean sLogMetrics = false;

	protected static int sTaskNumber = 0;

	private synchronized void incTaskNumber() {
		sTaskNumber++;
	}

	protected void renameTask(String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
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
			sLogger.trace("Exiting");
		}
	}

	public KnowledgeBaseFileAccessReadTask(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pStateString, String pMoveString, byte pBoardScore, String pLogContext) throws ConfigurationException, KnowledgeBaseException  {
		super(pKnowledgeBaseFilePool, pStateString, pMoveString, pLogContext);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (pBoardScore == 0) {
			mBoardScore = 120;
		} else {
			mBoardScore = pBoardScore;
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public KnowledgeBaseFileAccessReadTask(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pStateString, String pMoveString, String pLogContext) throws ConfigurationException, KnowledgeBaseException  {
		super(pKnowledgeBaseFilePool, pStateString, pMoveString, pLogContext);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public void executeTask() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogMetrics) {
			mTaskStartTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Thread: " + getTaskName() + " is starting at " + mTaskStartTime);
		}

		try {
			try {
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Read file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex));
				}
				readScore();
			} catch (IOException eIO) {
				sLogger.error("Read file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " IO Exception occurred!");
				sLogger.error("IOException Occurred when reading score: Message: " + eIO.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIO.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);

				mTransactionSuccessful = false;
			} catch (KnowledgeBaseException eKBE) {
				sLogger.error("Read file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " Knowledge Base Exception occurred!");
				sLogger.error("Knowledge Base Occurred when reading score: Message: " + eKBE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eKBE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: " + lSW);

				mTransactionSuccessful = false;
			} catch (ConfigurationException eCE) {
				sLogger.error("Read file: " + mFileLocation + " at Location: " + Long.toHexString(mFileIndex) + " Knowledge Base Exception occurred!");
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
			sLogger.debug("Task: " + getTaskName() + " is ending at " + mTaskEndTime);
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
			sLogger.trace("Exiting");
		}
	}

	public void readScore() throws IOException, KnowledgeBaseException, ConfigurationException, DataFormatException  {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		mCurrentThread = Thread.currentThread();

		KnowledgeBaseFilePool lKnowledgeBaseFilePool = KnowledgeBaseFilePool.getMasterInstance();
		boolean lGotKnowledgeBaseFile = lKnowledgeBaseFilePool.reserveKnowledgeBaseFile(mFileDirectory, mFileLocation, this);
		if (!lGotKnowledgeBaseFile) {
			while (mKnowledgeBaseFileInUse == null) {
				//sLogger.error("spinlock");

				// try {
				// Thread.sleep(10000);
				// } catch (InterruptedException eIE) {
				// //
				// sLogger.debug("Unanticipated Interrupt exception occurred!");
				// //
				// // StringWriter lSW = new StringWriter();
				// // PrintWriter lPW = new PrintWriter(lSW);
				// // eIE.printStackTrace(lPW);
				// // lSW.toString(); // stack trace as a string
				// // sLogger.debug("StackTrace: " + lSW);
				// }
			}
		}

		if (mKnowledgeBaseFileInUse == null) {
			sLogger.error("Read file: " + mFileLocation + " at Location: " + Integer.toHexString(mFileIndex) + " COULD NOT GET THE FILES RESERVED!");
			mTransactionSuccessful = false;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return;
		}
		byte lBoardScoreToRead = mKnowledgeBaseFileInUse.readScore(mFileIndex);

		if (lBoardScoreToRead == 0) {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Read file: " + mFileLocation + " at Location: " + Integer.toHexString(mFileIndex) + " Score not found.");
			}
			mScoreFound = false;
		} else if (lBoardScoreToRead == 120) {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Read file: " + mFileLocation + " at Location: " + Integer.toHexString(mFileIndex) + " Score read: " + lBoardScoreToRead);
			}
			mBoardScore = 0;
			mScoreFound = true;
		} else {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Read file: " + mFileLocation + " at Location: " + Integer.toHexString(mFileIndex) + " Score found: " + lBoardScoreToRead);
			}
			mBoardScore = lBoardScoreToRead;
			mScoreFound = true;
		}

		lKnowledgeBaseFilePool.releaseKnowledgeBaseFile(mKnowledgeBaseFileInUse);

		mTransactionSuccessful = true;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return;
	}

	public void interrupt() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		mCurrentThread.interrupt();
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	@Override
	public String getWaitingThreadName() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mCurrentThread.getName();
	}

}
