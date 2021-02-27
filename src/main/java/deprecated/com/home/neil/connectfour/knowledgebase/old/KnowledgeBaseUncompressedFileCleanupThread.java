package deprecated.com.home.neil.connectfour.knowledgebase.old;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;

import javax.management.openmbean.OpenDataException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.thread.performancemetrics.ThreadPerformanceMetricsMBean;

public class KnowledgeBaseUncompressedFileCleanupThread extends Thread implements KnowledgeBaseUncompressedFileCleanupThreadMBean {
	public static final String CLASS_NAME = KnowledgeBaseUncompressedFileCleanupThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	protected String mBeanName = null;

	protected static int sThreadNumber = 1;

	private long mThrottleTime = 0;
	private boolean mPauseActive = false;
	private boolean mTerminate = false;

	protected boolean mTransactionSuccessful = false;
	protected boolean mTransactionFinished = false;

	protected long mThreadStartTime = 0;
	protected long mThreadEndTime = 0;

	protected String mLogContext = null;
	
	private int mThrottleCount = 0;
	
	protected KnowledgeBaseFilePool mKnowledgeBaseFilePool = null;
	
	public synchronized void renameThread(String pLogContext) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.debug("Entering");
		}
		sThreadNumber++;

		setName(KnowledgeBaseUncompressedFileCleanupThread.class.getSimpleName() + "." + sThreadNumber);

		if (pLogContext == null || pLogContext.trim().isEmpty()) {
			mLogContext = KnowledgeBaseUncompressedFileCleanupThread.class.getSimpleName() + "." + sThreadNumber;
			ThreadContext.put("LogContext", mLogContext);
		} else {
			mLogContext = pLogContext;
			ThreadContext.put("LogContext", mLogContext);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}
	

	public KnowledgeBaseUncompressedFileCleanupThread(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pLogContext) throws ConfigurationException {
		super();
		sLogger.trace("Entering");
		
		mKnowledgeBaseFilePool = pKnowledgeBaseFilePool;

		mLogContext = pLogContext;

		renameThread(mLogContext);

		sLogger.trace("Exiting");
	}

	public String getBeanName() {
		sLogger.trace("Entering");

		sLogger.trace("Exiting");
		return mBeanName;
	}


	
	
	public void run() {
		sLogger.trace("Entering");

		while (!isTimeToTerminate()) {
		
			mThreadStartTime = new GregorianCalendar().getTimeInMillis();
			sLogger.debug("Thread: " + this.getName() + " is starting at " + mThreadStartTime);
	
			try {
				runCompressionThread();
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

		}
		sLogger.trace("Exiting");
	}


	public void setThrottle(long pThrottleValue) {
		sLogger.trace("Entering");
		mThrottleTime = pThrottleValue;
		sLogger.trace("Exiting");
	}

	public long getThrottle() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mThrottleTime;
	}

	public void togglePause() {
		sLogger.trace("Entering");
		if (mPauseActive) {
			mPauseActive = false;
		} else {
			mPauseActive = true;
		}
		sLogger.trace("Exiting");

	}

	public boolean getPause() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mPauseActive;
	}

	public boolean isTransactionSuccessful() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mTransactionSuccessful;
	}

	public void setTerminate() {
		sLogger.trace("Entering");
		mTerminate = true;
		sLogger.trace("Exiting");
	}

	public boolean getTerminate() {
		sLogger.trace("Entering");
		sLogger.trace("Exiting");
		return mTerminate;
	}

	public boolean isTimeToTerminate() {
		sLogger.trace("Entering");

		if (mThrottleTime > 0) {
			try {
				Thread.sleep(mThrottleTime);
			} catch (InterruptedException eIE) {
				sLogger.warn("Unanticipated Interrupt exception occurred!");
				
				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.debug("StackTrace: " + lSW);
			}
		}

		while (mPauseActive && !mTerminate) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException eIE) {
				sLogger.warn("Unanticipated Interrupt exception occurred!");
				
				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.debug("StackTrace: " + lSW);
			}
		}

		if (mTerminate) {
			sLogger.trace("Exiting");
			return true;
		} else {
			sLogger.trace("Exiting");
			return false;
		}
	}

	public void runCompressionThread() throws IOException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Executing Lookup of Files to Clean");
		}

		
		LinkedList <KnowledgeBaseFile> lKnowledgeBaseFilesToCompress = mKnowledgeBaseFilePool.getKnowledgeBaseUncompressedFilesToCleanup();

		if (lKnowledgeBaseFilesToCompress.isEmpty()) {
			if (mThrottleCount < 5) {
				mThrottleCount++;
			}
			try {
				Thread.sleep(1000 * mThrottleCount);
			} catch (InterruptedException e) {

			}
		} else {
			for (Iterator<KnowledgeBaseFile> lIterator = lKnowledgeBaseFilesToCompress.iterator() ;  lIterator.hasNext();) {
				KnowledgeBaseFile lKnowledgeBaseFile = lIterator.next();
				
				lKnowledgeBaseFile.compressMemory();
				
			}
			if (mThrottleCount > 0) {
				try {
					Thread.sleep(1000 * mThrottleCount);
				} catch (InterruptedException e) {

				}
				mThrottleCount--;
			}
		}
		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compression is complete");
		}
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		
	}

}
