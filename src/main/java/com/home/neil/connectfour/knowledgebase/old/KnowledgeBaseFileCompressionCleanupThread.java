package com.home.neil.connectfour.knowledgebase.old;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;

import javax.management.openmbean.OpenDataException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.connectfour.performancemetrics.ThreadPerformanceMetricsMBean;

public class KnowledgeBaseFileCompressionCleanupThread extends Thread implements KnowledgeBaseFileAccessTaskInterface {
	public static final String CLASS_NAME = KnowledgeBaseFileCompressionCleanupThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private KnowledgeBaseFile mKnowledgeBaseFileInUse = null;

	private boolean mTransactionSuccessful = false;
	private boolean mTransactionFinished = false;

	private long mThreadStartTime = 0;
	private long mThreadEndTime = 0;

	private static int sThreadNumber = 0;

	private String mLogContext = null;
	
	public String getTaskName () {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return getName();
	}

	public synchronized void renameThread(String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		sThreadNumber++;

		setName(KnowledgeBaseFileCompressionCleanupThread.class.getSimpleName() + "." + sThreadNumber);

		if (pLogContext == null || pLogContext.trim().isEmpty()) {
			mLogContext = KnowledgeBaseFileCompressionCleanupThread.class.getSimpleName() + "." + sThreadNumber;
			ThreadContext.put("LogContext", mLogContext);
		} else {
			mLogContext = pLogContext;
			ThreadContext.put("LogContext", mLogContext);
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public KnowledgeBaseFileCompressionCleanupThread(KnowledgeBaseFile pKnowledgeBaseFile, String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}

		renameThread(pLogContext);

		mKnowledgeBaseFileInUse = pKnowledgeBaseFile;

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public boolean isTransactionFinished() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mTransactionFinished;
	}

	public KnowledgeBaseFile getKnowledgeBaseFileInUse() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mKnowledgeBaseFileInUse;
	}

	public void setKnowledgeBaseFileInUse(KnowledgeBaseFile pKnowledgeBaseFileInUse) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		mKnowledgeBaseFileInUse = pKnowledgeBaseFileInUse;
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public void run() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}

		mThreadStartTime = new GregorianCalendar().getTimeInMillis();
		sLogger.debug("Thread: " + this.getName() + " is starting at " + mThreadStartTime);

		try {
			try {
					sLogger.debug("Cleaning up file: " + mKnowledgeBaseFileInUse.getFileLocation());
					mKnowledgeBaseFileInUse.cleanup();
					KnowledgeBaseFilePool lKnowledgeBaseFilePool = KnowledgeBaseFilePool.getMasterInstance();
					lKnowledgeBaseFilePool.releaseKnowledgeBaseFile(mKnowledgeBaseFileInUse);
					mTransactionSuccessful = true;
				} catch (IOException eIO) {
					sLogger.error("Finalize file: " + mKnowledgeBaseFileInUse.getFileLocation() + " IO Exception occurred!");
					sLogger.error("IOException Occurred when reading score: Message: " + eIO.getMessage());

					StringWriter lSW = new StringWriter();
					PrintWriter lPW = new PrintWriter(lSW);
					eIO.printStackTrace(lPW);
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

		mThreadEndTime = new GregorianCalendar().getTimeInMillis();
		sLogger.debug("Thread: " + this.getName() + " is ending at " + mThreadEndTime);
		long lDuration = mThreadEndTime - mThreadStartTime;

		try {
			ThreadPerformanceMetricsMBean lThreadPerformanceMetricsMBean = ThreadPerformanceMetricsMBean.getInstance();
			lThreadPerformanceMetricsMBean
					.updateThreadStatistics(this.getClass().getSimpleName(), "run()", mThreadStartTime, lDuration, mTransactionSuccessful);
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

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public boolean isTransactionSuccessful() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.debug("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mTransactionSuccessful;
	}

	@Override
	public String getWaitingThreadName() {
		// TODO Auto-generated method stub
		return null;
	}

}
