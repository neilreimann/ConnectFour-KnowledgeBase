package com.home.neil.knowledgebase.pool.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;

import javax.management.openmbean.OpenDataException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.connectfour.performancemetrics.ThreadPerformanceMetricsMBean;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;

public abstract class PoolItemTask implements IPoolItemTask {
	public static final String CLASS_NAME = PoolItemTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private IPool mPool = null;
	private String mPoolItemId = null;
	private IPoolItem mPoolItem = null;

	public static final boolean sLogMetrics = false;
	private long mTaskStartTime = 0;
	private long mTaskEndTime = 0;

	private boolean mTaskSuccessful = false;
	private boolean mTaskFinished = false;

	protected String mTaskClassName = null; 

	protected Thread mExecutingThread = null;
	
	protected String mLogContext = null;
	protected String mTaskName = null;
	protected static int sTaskNumber = 0;
	
	
	private synchronized void incTaskNumber() {
		sTaskNumber++;
	}

	public PoolItemTask (IPool pPool, String pPoolItemId) {
		mPool = pPool;
		mPoolItemId = pPoolItemId;
		mExecutingThread = Thread.currentThread();
	}
	
	public void renameTask(String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		incTaskNumber();

		mTaskClassName = this.getClass().getSimpleName();
		
		mTaskName = mTaskClassName + "." + sTaskNumber;

		if (pLogContext == null || pLogContext.trim().isEmpty()) {
			mLogContext = mTaskClassName + "." + sTaskNumber;
			ThreadContext.put("LogContext", mLogContext);
		} else {
			mLogContext = pLogContext;
			ThreadContext.put("LogContext", mLogContext);
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	public String getTaskName() {
		return mTaskName;
	}

	public String getTaskThreadName() {
		return mExecutingThread.getName();
	}
	
	public void notifyThread() {
		notifyAll();
	}

	
	public void startTask  () {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		mExecutingThread = Thread.currentThread();
		
		logStartingTaskMetrics();

		mPoolItem = mPool.reservePoolItem(mPoolItemId, this);
		if (mPoolItem == null) {
			try {
				wait(10000);
			} catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                sLogger.error("Thread interrupted", e); 
			}
		}
		
		if (mPoolItem == null) {
			sLogger.error("Could not reserve the PoolItem!");
			mTaskSuccessful = false;
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			return;
		}
		
		try {
			//
			mTaskSuccessful = executeTask();
		} catch (Exception eE) {
			sLogger.error("Unanticipated Exception occurred during Thread Execution!");
			sLogger.error("Exception Message: {}", eE.getMessage());

			StringWriter lSW = new StringWriter();
			PrintWriter lPW = new PrintWriter(lSW);
			eE.printStackTrace(lPW);
			lSW.toString(); // stack trace as a string
			sLogger.error("StackTrace: {}", lSW);

			mTaskSuccessful = false;
		}
		
		
		mPool.releasePoolItem(mPoolItem);
		
		logEndingTaskMetrics();
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}
	
	
	protected abstract boolean executeTask() throws Exception;
	
	
	private void logStartingTaskMetrics () {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		if (sLogMetrics) {
			mTaskStartTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Thread: {} is starting at {}", getTaskName(), mTaskStartTime);
		}
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}

	private void logEndingTaskMetrics () {
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}

		mTaskFinished = true;

		if (sLogMetrics) {

			mTaskEndTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Task: {} is ending at {}", getTaskName(), mTaskEndTime);
			long lDuration = mTaskEndTime - mTaskStartTime;

			try {
				ThreadPerformanceMetricsMBean lThreadPerformanceMetricsMBean = ThreadPerformanceMetricsMBean.getInstance();
				lThreadPerformanceMetricsMBean.updateThreadStatistics(mTaskClassName, "executeTask()", mTaskStartTime, lDuration, mTaskSuccessful);
			} catch (OpenDataException eODE) {
				sLogger.error("Open Data Exception occurred during Thread Execution!");
				sLogger.error("Exception Message: {}", eODE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eODE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: {}", lSW);
			} catch (Exception eE) {
				sLogger.error("Exception Message: {}", eE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.error("StackTrace: {}", lSW);
			}
		}

		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
	}
	
	public boolean isTaskSuccessful() {
		return mTaskSuccessful;
	}
	
	public boolean isTaskFinished() {
		return mTaskFinished;
	}
	
	public IPoolItem getPoolItem() {
		return mPoolItem;
	}
	
	public void setReservedPoolItem (IPoolItem pPoolItem) {
		mPoolItem = pPoolItem;
	}

	
}
