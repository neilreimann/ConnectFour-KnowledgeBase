package deprecated.com.home.neil.connectfour.knowledgebase.old;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appconfig.Connect4PropertiesConfiguration;
import com.home.neil.appmanager.ApplicationPrecompilerSettings;

public class KnowledgeBaseFilePool implements KnowledgeBaseFilePoolMBean {
	public static final String CLASS_NAME = KnowledgeBaseFilePool.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final String MBEAN_NAME = PACKAGE_NAME + ":type=" + KnowledgeBaseFilePool.class.getSimpleName();
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private final static String MASTER_KNOWLEDGEBASE_DIRECTORY_CONFIG_KEY = CLASS_NAME + ".MasterKnowledgeBaseDirectory";
	private final static String TRANSIENT_KNOWLEDGEBASE_DIRECTORY_CONFIG_KEY = CLASS_NAME + ".TransientKnowledgeBaseDirectory";

	private final static String MASTER_OPEN_FILES_HIGH_WATER_CONFIG_KEY = CLASS_NAME + ".MasterOpenFilesHighWater";
	private final static String MASTER_OPEN_FILES_LOW_WATER_CONFIG_KEY = CLASS_NAME + ".MasterOpenFilesLowWater";
	
	private final static String TRANSIENT_OPEN_FILES_HIGH_WATER_CONFIG_KEY = CLASS_NAME + ".TransientOpenFilesHighWater";
	private final static String TRANSIENT_OPEN_FILES_LOW_WATER_CONFIG_KEY = CLASS_NAME + ".TransientOpenFilesLowWater";
	
	private final static String MASTER_UNCOMPRESSED_OPEN_FILES_HIGH_WATER_CONFIG_KEY = CLASS_NAME + ".MasterUncompressedOpenFilesHighWater";
	private final static String MASTER_UNCOMPRESSED_OPEN_FILES_LOW_WATER_CONFIG_KEY = CLASS_NAME + ".MasterUncompressedOpenFilesLowWater";
	
	private final static String TRANSIENT_UNCOMPRESSED_OPEN_FILES_HIGH_WATER_CONFIG_KEY = CLASS_NAME + ".TransientUncompressedOpenFilesHighWater";
	private final static String TRANSIENT_UNCOMPRESSED_OPEN_FILES_LOW_WATER_CONFIG_KEY = CLASS_NAME + ".TransientUncompressedOpenFilesLowWater";

	private final static String ACTIONS_PER_FILE_CONFIG_KEY = CLASS_NAME + ".ActionsPerFile";
	private final static String BYTES_PER_FILE_CONFIG_KEY = CLASS_NAME + ".BytesPerFile";

	
	private int mOpenFilesHighWater = 10;
	private int mOpenFilesLowWater = 8;
	
	private int mUncompressedOpenFilesHighWater = 10;
	private int mUncompressedOpenFilesLowWater = 8;

	private int mActionsPerFile = 9;
	private int mBytesPerFile = 16777216;
	
	private static KnowledgeBaseFilePool sMasterInstance = null;

	private String mSessionId = null;
	private String mKnowledgeBaseDirectory = null;

	private HashMap<String, KnowledgeBaseFile> mCurrentUnReservedKnowledgeBaseFiles = null;
	private HashMap<String, KnowledgeBaseFile> mCurrentReservedKnowledgeBaseFiles = null;
	private HashMap<String, LinkedList<KnowledgeBaseFileAccessTaskInterface>> mCurrentReservations = null;
	private TreeSet <KnowledgeBaseFile> mCurrentLastAccessedUnReservedKnowledgeBaseFiles = null;
	
	private final static String MASTER_CLEANUP_THREAD_COUNT_CONFIG_KEY = CLASS_NAME + ".MasterCleanupThreadCount";
	private final static String TRANSIENT_CLEANUP_THREAD_COUNT_CONFIG_KEY = CLASS_NAME + ".TransientCleanupThreadCount";
	private int mCleanupThreadCount = 1;
	private KnowledgeBaseUncompressedFileCleanupThread [] mKnowledgeBaseUncompressedFileCleanupThread = null;
	
	
	public class KnowledgeBaseFileCompressedIndexComparator implements Comparator <KnowledgeBaseFile> {
		public int compare (KnowledgeBaseFile pFile1, KnowledgeBaseFile pFile2) {
			return Long.compare(pFile1.getKnowledgeBaseFileCompressedIndex(), pFile2.getKnowledgeBaseFileCompressedIndex());
		}
	}
	
	public class KnowledgeBaseFileUncompressedIndexComparator implements Comparator <KnowledgeBaseFile> {
		public int compare (KnowledgeBaseFile pFile1, KnowledgeBaseFile pFile2) {
			return Long.compare(pFile1.getKnowledgeBaseFileUncompressedIndex(), pFile2.getKnowledgeBaseFileUncompressedIndex());
		}
	}


	private TreeSet <KnowledgeBaseFile> sLastAccessedMemoryUncompressedKnowledgeBaseFiles = new TreeSet <KnowledgeBaseFile> (new KnowledgeBaseFileUncompressedIndexComparator());
	
	private final Object mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock = new Object();
	
	public void addLastAccessedMemoryUncompressedKnowledgeBaseFiles(KnowledgeBaseFile pKnowledgeBaseFile) {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			pKnowledgeBaseFile.setKnowledgeBaseFileUncompressedIndex();
			sLastAccessedMemoryUncompressedKnowledgeBaseFiles.add(pKnowledgeBaseFile);
		}
	}

	public void removeLastAccessedMemoryUncompressedKnowledgeBaseFiles(KnowledgeBaseFile pKnowledgeBaseFile) {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			sLastAccessedMemoryUncompressedKnowledgeBaseFiles.remove(pKnowledgeBaseFile);
		}
	}

	public KnowledgeBaseFile popLastAccessedMemoryUncompressedKnowledgeBaseFiles () {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			return sLastAccessedMemoryUncompressedKnowledgeBaseFiles.pollFirst();
		}
	}

	public void resetLastAccessedMemoryUncompressedKnowledgeBaseFiles (KnowledgeBaseFile pKnowledgeBaseFile){
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			sLastAccessedMemoryUncompressedKnowledgeBaseFiles.remove(pKnowledgeBaseFile);
			pKnowledgeBaseFile.setKnowledgeBaseFileUncompressedIndex();
			sLastAccessedMemoryUncompressedKnowledgeBaseFiles.add(pKnowledgeBaseFile);
		}
	}
	
	public int getLastAccessedMemoryUncompressedKnowledgeBaseFilesSize () {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			return sLastAccessedMemoryUncompressedKnowledgeBaseFiles.size();
		}
	}

	public KnowledgeBaseFile peekLastAccessedMemoryUncompressedKnowledgeBaseFile () {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			return sLastAccessedMemoryUncompressedKnowledgeBaseFiles.first();
		}
	}
	
	
	public boolean checkKnowledgeBaseUncompressedFilesReachedHighWaterMark () {
		int lKnowledgeBaseFilesUncompressedCount = getLastAccessedMemoryUncompressedKnowledgeBaseFilesSize();
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Uncompressed Count: " + lKnowledgeBaseFilesUncompressedCount);
		}

		if (lKnowledgeBaseFilesUncompressedCount > getUncompressedOpenFilesHighWater()) {
			return true;
		}
		return false;
	}

	public LinkedList <KnowledgeBaseFile> getKnowledgeBaseUncompressedFilesToCleanup () throws IOException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Checking Memory Compressions");
		}
		LinkedList <KnowledgeBaseFile> lKnowledgeBaseFilesToCompress = new LinkedList <KnowledgeBaseFile> ();
		
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			int lKnowledgeBaseFilesUncompressedCount = getLastAccessedMemoryUncompressedKnowledgeBaseFilesSize();
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Memory Uncompressed Count: " + lKnowledgeBaseFilesUncompressedCount);
			}
			//sLogger.error("Memory Uncompressed Count: " + lKnowledgeBaseFilesUncompressedCount);

			while (lKnowledgeBaseFilesUncompressedCount > getUncompressedOpenFilesLowWater()) {

				lKnowledgeBaseFilesUncompressedCount--;
					
				KnowledgeBaseFile lKnowledgeBaseFileToCompress = popLastAccessedMemoryUncompressedKnowledgeBaseFiles();

				lKnowledgeBaseFilesToCompress.add(lKnowledgeBaseFileToCompress);
						
				//sLogger.error("Add Compression to List: " + lKnowledgeBaseFileToCompress.getFileLocation());

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Add Compression to List: " + lKnowledgeBaseFileToCompress.getFileLocation());
				}
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Compressions Check Done");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		
		return lKnowledgeBaseFilesToCompress;
	}
	
		
		
		
		
		
	
	
	public String getCurrentKnowledgeBaseUncompressedFiles () {
		synchronized(mLastAccessedMemoryUncompressedKnowledgeBaseFilesLock) {
			KnowledgeBaseFile lKnowledgeBaseFile = sLastAccessedMemoryUncompressedKnowledgeBaseFiles.last();
			String lReturnString = new String();
			for (int i = 0; i < 10; i++) {
				lReturnString += lKnowledgeBaseFile.getKnowledgeBaseFileUncompressedIndex() + "," + lKnowledgeBaseFile.getCompressFileLocation() + "\r\n";
				lKnowledgeBaseFile = sLastAccessedMemoryUncompressedKnowledgeBaseFiles.lower(lKnowledgeBaseFile);
			}
			return lReturnString;
		}
	}
	
	
	
	private KnowledgeBaseFilePool() throws ConfigurationException, IOException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		mCurrentLastAccessedUnReservedKnowledgeBaseFiles = new TreeSet<KnowledgeBaseFile>(new KnowledgeBaseFileCompressedIndexComparator());
		mCurrentUnReservedKnowledgeBaseFiles = new HashMap<String, KnowledgeBaseFile>();
		mCurrentReservedKnowledgeBaseFiles = new HashMap<String, KnowledgeBaseFile>();
		mCurrentReservations = new HashMap<String, LinkedList<KnowledgeBaseFileAccessTaskInterface>>();
		
		Connect4PropertiesConfiguration lConfig = Connect4PropertiesConfiguration.getInstance();

		mKnowledgeBaseDirectory = lConfig.getString(MASTER_KNOWLEDGEBASE_DIRECTORY_CONFIG_KEY);

		mOpenFilesHighWater = lConfig.getInt(MASTER_OPEN_FILES_HIGH_WATER_CONFIG_KEY);
		mOpenFilesLowWater = lConfig.getInt(MASTER_OPEN_FILES_LOW_WATER_CONFIG_KEY);
		
		mUncompressedOpenFilesHighWater = lConfig.getInt(MASTER_UNCOMPRESSED_OPEN_FILES_HIGH_WATER_CONFIG_KEY);
		mUncompressedOpenFilesLowWater = lConfig.getInt(MASTER_UNCOMPRESSED_OPEN_FILES_LOW_WATER_CONFIG_KEY);

		mBytesPerFile = lConfig.getInt(BYTES_PER_FILE_CONFIG_KEY);
		mActionsPerFile = lConfig.getInt(ACTIONS_PER_FILE_CONFIG_KEY);
		
		mCleanupThreadCount = lConfig.getInt(MASTER_CLEANUP_THREAD_COUNT_CONFIG_KEY);
		mKnowledgeBaseUncompressedFileCleanupThread = new KnowledgeBaseUncompressedFileCleanupThread [mCleanupThreadCount];
		
		for (int i = 0 ; i < mCleanupThreadCount; i++) {
			mKnowledgeBaseUncompressedFileCleanupThread [i] = new KnowledgeBaseUncompressedFileCleanupThread(this, null);
			mKnowledgeBaseUncompressedFileCleanupThread [i] .start();
		}
		

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public KnowledgeBaseFilePool (String pSessionId) throws ConfigurationException, IOException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		mCurrentLastAccessedUnReservedKnowledgeBaseFiles = new TreeSet<KnowledgeBaseFile>(new KnowledgeBaseFileCompressedIndexComparator());
		mCurrentUnReservedKnowledgeBaseFiles = new HashMap<String, KnowledgeBaseFile>();
		mCurrentReservedKnowledgeBaseFiles = new HashMap<String, KnowledgeBaseFile>();
		mCurrentReservations = new HashMap<String, LinkedList<KnowledgeBaseFileAccessTaskInterface>>();

		Connect4PropertiesConfiguration lConfig = Connect4PropertiesConfiguration.getInstance();

		mSessionId = pSessionId;

		mKnowledgeBaseDirectory = lConfig.getString(TRANSIENT_KNOWLEDGEBASE_DIRECTORY_CONFIG_KEY) + "/" + mSessionId;
		
		mOpenFilesHighWater = lConfig.getInt(TRANSIENT_OPEN_FILES_HIGH_WATER_CONFIG_KEY);
		mOpenFilesLowWater = lConfig.getInt(TRANSIENT_OPEN_FILES_LOW_WATER_CONFIG_KEY);

		mUncompressedOpenFilesHighWater = lConfig.getInt(TRANSIENT_UNCOMPRESSED_OPEN_FILES_HIGH_WATER_CONFIG_KEY);
		mUncompressedOpenFilesLowWater = lConfig.getInt(TRANSIENT_UNCOMPRESSED_OPEN_FILES_LOW_WATER_CONFIG_KEY);

		
		
		mCleanupThreadCount = lConfig.getInt(TRANSIENT_CLEANUP_THREAD_COUNT_CONFIG_KEY);
		mKnowledgeBaseUncompressedFileCleanupThread = new KnowledgeBaseUncompressedFileCleanupThread [mCleanupThreadCount];
		
		for (int i = 0 ; i < mCleanupThreadCount; i++) {
			mKnowledgeBaseUncompressedFileCleanupThread [i] = new KnowledgeBaseUncompressedFileCleanupThread(this, null);
			mKnowledgeBaseUncompressedFileCleanupThread [i] .start();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}
	
	
	
	//Gets the master instance
	public static synchronized KnowledgeBaseFilePool getMasterInstance() throws IOException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (sMasterInstance == null) {
			sMasterInstance = new KnowledgeBaseFilePool();

			MBeanServer lMBS = ManagementFactory.getPlatformMBeanServer();
			ObjectName lBeanName;
			try {
				lBeanName = new ObjectName(MBEAN_NAME);
				lMBS.registerMBean(sMasterInstance, lBeanName);
			} catch (MalformedObjectNameException e2) {
				sLogger.error("Could not register the MBean");
				e2.printStackTrace();
			} catch (InstanceAlreadyExistsException e1) {
				sLogger.error("Could not register the MBean");
				e1.printStackTrace();
			} catch (MBeanRegistrationException e1) {
				sLogger.error("Could not register the MBean");
				e1.printStackTrace();
			} catch (NotCompliantMBeanException e1) {
				sLogger.error("Could not register the MBean");
				e1.printStackTrace();

			}

		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return sMasterInstance;
	}

	public synchronized void cleanupAll() throws IOException,DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		
		for (int i = 0; i < mCleanupThreadCount; i++) {
			//Terminate the KnowledgeBaseUncompressedFileCleanupThread
			mKnowledgeBaseUncompressedFileCleanupThread[i].setTerminate();
			
			try {
				mKnowledgeBaseUncompressedFileCleanupThread[i].join();
			} catch (InterruptedException eIE) {
				sLogger.error("Unanticipated Interrupted Exception Message: " + eIE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.warn("StackTrace: " + lSW);
			}
			
		}
		

		while (!mCurrentReservedKnowledgeBaseFiles.isEmpty()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException eIE) {
				sLogger.error("Unanticipated Interrupted Exception Message: " + eIE.getMessage());

				StringWriter lSW = new StringWriter();
				PrintWriter lPW = new PrintWriter(lSW);
				eIE.printStackTrace(lPW);
				lSW.toString(); // stack trace as a string
				sLogger.warn("StackTrace: " + lSW);
			}
		}

		while (!mCurrentLastAccessedUnReservedKnowledgeBaseFiles.isEmpty()) {
			KnowledgeBaseFile lCurrentKnowledgeBaseFile = mCurrentLastAccessedUnReservedKnowledgeBaseFiles.pollFirst();
			mCurrentUnReservedKnowledgeBaseFiles.remove(lCurrentKnowledgeBaseFile.getFileLocation());
			
			lCurrentKnowledgeBaseFile.cleanup();
		}
		

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public synchronized boolean reserveKnowledgeBaseFile(String pFileDirectory, String pFileLocation, KnowledgeBaseFileAccessTaskInterface pTask)
			throws IOException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		
		//long lStartTime = new GregorianCalendar().getTimeInMillis();

		KnowledgeBaseFile lKnowledgeBaseFile = mCurrentUnReservedKnowledgeBaseFiles.remove(pFileLocation);
		if (lKnowledgeBaseFile != null) {
			mCurrentLastAccessedUnReservedKnowledgeBaseFiles.remove(lKnowledgeBaseFile);
			mCurrentReservedKnowledgeBaseFiles.put(pFileLocation, lKnowledgeBaseFile);
			pTask.setKnowledgeBaseFileInUse(lKnowledgeBaseFile);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State1: Lock Obtained: " + lKnowledgeBaseFile.getFileLocation() + " for Thread: " + Thread.currentThread().getName());
			}

			/*
			long lDurationTime = new GregorianCalendar().getTimeInMillis() - lStartTime;
			if (lDurationTime > 5) {
				//sLogger.error("Duration1=" + lDurationTime);
			}
			 */
			
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return true;
		}

		lKnowledgeBaseFile = mCurrentReservedKnowledgeBaseFiles.get(pFileLocation);
		if (lKnowledgeBaseFile == null) {
			lKnowledgeBaseFile = new KnowledgeBaseFile(this, pFileDirectory, pFileLocation);
			mCurrentReservedKnowledgeBaseFiles.put(pFileLocation, lKnowledgeBaseFile);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State2: File Created and Lock Obtained: " + lKnowledgeBaseFile.getFileLocation() + " for Thread: "
						+ Thread.currentThread().getName());
			}
			
			int lCurrentOpenFiles = mCurrentUnReservedKnowledgeBaseFiles.size();

			if (lCurrentOpenFiles > mOpenFilesHighWater) {
				while (lCurrentOpenFiles > mOpenFilesLowWater) {
					try {
						KnowledgeBaseFile lKnowledgeBaseFileToRemove = mCurrentLastAccessedUnReservedKnowledgeBaseFiles.pollFirst();
						mCurrentUnReservedKnowledgeBaseFiles.remove(lKnowledgeBaseFileToRemove.getFileLocation());

						lKnowledgeBaseFileToRemove.setBeingCleaned();
						mCurrentReservedKnowledgeBaseFiles.put(lKnowledgeBaseFileToRemove.getFileLocation(), lKnowledgeBaseFileToRemove);

						if (sLogger.isDebugEnabled()) {
							sLogger.debug("State2A: Locked File to be Finalized: " + lKnowledgeBaseFileToRemove.getFileLocation() + " for Thread: "
									+ Thread.currentThread().getName());
						}

						// TODO: define subthread context
						KnowledgeBaseFileCompressionCleanupThread lFinalizingKnowledgeBaseFileAccessThread = new KnowledgeBaseFileCompressionCleanupThread(
								lKnowledgeBaseFileToRemove, null);

						lFinalizingKnowledgeBaseFileAccessThread.start();

					} catch (NoSuchElementException eE) {

					}
					lCurrentOpenFiles--;
				}
			}

			pTask.setKnowledgeBaseFileInUse(lKnowledgeBaseFile);

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			/*
			long lDurationTime = new GregorianCalendar().getTimeInMillis() - lStartTime;
			if (lDurationTime > 5) {
				sLogger.error("Duration2=" + lDurationTime);
			}
			 */
			return true;
		} else if (lKnowledgeBaseFile.getBeingCleaned()) {

			while (!lKnowledgeBaseFile.isCleaned()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException eIE) {
					sLogger.error("Unanticipated Interrupted Exception Message: " + eIE.getMessage());

					StringWriter lSW = new StringWriter();
					PrintWriter lPW = new PrintWriter(lSW);
					eIE.printStackTrace(lPW);
					lSW.toString(); // stack trace as a string
					sLogger.warn("StackTrace: " + lSW);

					Thread.currentThread().interrupt();
				}
			}
			lKnowledgeBaseFile = new KnowledgeBaseFile(this, pFileDirectory, pFileLocation);
			mCurrentReservedKnowledgeBaseFiles.put(pFileLocation, lKnowledgeBaseFile);
			pTask.setKnowledgeBaseFileInUse(lKnowledgeBaseFile);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State3: File Delayed Created and Lock Obtained: " + lKnowledgeBaseFile.getFileLocation() + " for Thread: "
						+ Thread.currentThread().getName());
			}
			/*
			long lDurationTime = new GregorianCalendar().getTimeInMillis() - lStartTime;
			if (lDurationTime > 5) {
				sLogger.error("Duration3=" + lDurationTime);
			}
			*/
			
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return true;
		} else {
			LinkedList<KnowledgeBaseFileAccessTaskInterface> lWaitingThreads = mCurrentReservations.get(pFileLocation);
			if (lWaitingThreads == null) { // Nobody is waiting
				lWaitingThreads = new LinkedList<KnowledgeBaseFileAccessTaskInterface>();
				lWaitingThreads.add(pTask);
				mCurrentReservations.put(pFileLocation, lWaitingThreads);
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("State4: Lock Reserved on Open File: " + lKnowledgeBaseFile.getFileLocation() + " for Thread: "
							+ Thread.currentThread().getName());
				}
				
				/*
				long lDurationTime = new GregorianCalendar().getTimeInMillis() - lStartTime;
				if (lDurationTime > 5) {
					sLogger.error("Duration4=" + lDurationTime);
				}
				*/
				
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace("Exiting");
				}
				return false;
			} else { // A thread is already waiting
				lWaitingThreads.add(pTask);
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("State5: Lock Reserved on Open File: " + lKnowledgeBaseFile.getFileLocation() + " for Thread: "
							+ Thread.currentThread().getName());
				}
				/*
				long lDurationTime = new GregorianCalendar().getTimeInMillis() - lStartTime;
				if (lDurationTime > 5) {
					sLogger.error("Duration5=" + lDurationTime);
				}
				*/
				
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace("Exiting");
				}
				return false;
			}
		}
	}

	public synchronized void releaseKnowledgeBaseFile(KnowledgeBaseFile pKnowledgeBaseFile) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		String lFileLocation = pKnowledgeBaseFile.getFileLocation();

		if (pKnowledgeBaseFile.isCleaned()) {
			mCurrentReservedKnowledgeBaseFiles.remove(lFileLocation);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State6: Lock Released on Finalized File: " + lFileLocation + " for Thread: " + Thread.currentThread().getName());
			}
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return;
		}

		LinkedList<KnowledgeBaseFileAccessTaskInterface> lWaitingThreads = mCurrentReservations.get(lFileLocation);
		if (lWaitingThreads == null) {
			mCurrentReservedKnowledgeBaseFiles.remove(lFileLocation);
			mCurrentUnReservedKnowledgeBaseFiles.put(lFileLocation, pKnowledgeBaseFile);
			pKnowledgeBaseFile.setKnowledgeBaseFileCompressedIndex();
			mCurrentLastAccessedUnReservedKnowledgeBaseFiles.add(pKnowledgeBaseFile);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State7: Lock Released and No Thread Waiting: " + lFileLocation + " for Thread: " + Thread.currentThread().getName());
			}
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return;
		} else if (lWaitingThreads.isEmpty()) {
			mCurrentReservations.remove(lFileLocation);
			mCurrentReservedKnowledgeBaseFiles.remove(lFileLocation);
			mCurrentUnReservedKnowledgeBaseFiles.put(lFileLocation, pKnowledgeBaseFile);
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State8: Lock Released and No Thread Waiting: " + lFileLocation + " for Thread: " + Thread.currentThread().getName());
			}
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace("Exiting");
			}
			return;
		} else {
			KnowledgeBaseFileAccessTaskInterface lWaitingThread = lWaitingThreads.pop();
			lWaitingThread.setKnowledgeBaseFileInUse(pKnowledgeBaseFile);
			// lWaitingThread.interrupt();
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("State9: Lock Released and Given to Waiting Thread: (" + lWaitingThread.getWaitingThreadName() + ") " + lFileLocation
					+ " for Thread: " + Thread.currentThread().getName());
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public String getCurrentUnReservedKnowledgeBaseFilesSize() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return String.valueOf(mCurrentUnReservedKnowledgeBaseFiles.size());
	}

	public String getCurrentLastAccessedUnReservedKnowledgeBaseFilesSize() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return String.valueOf(mCurrentLastAccessedUnReservedKnowledgeBaseFiles.size());
	}

	public String getCurrentReservedKnowledgeBaseFilesSize() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return String.valueOf(mCurrentReservedKnowledgeBaseFiles.size());
	}

	public String getCurrentReservationsSize() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return String.valueOf(mCurrentReservations.size());
	}

	
	public String getKnowledgeBaseDirectory () {
		return mKnowledgeBaseDirectory;
	}

	public int getOpenFilesHighWater () {
		return mOpenFilesHighWater;
	}
	
	public int getOpenFilesLowWater () {
		return mOpenFilesLowWater;
	}

	public int getUncompressedOpenFilesHighWater () {
		return mUncompressedOpenFilesHighWater;
	}
	
	public int getUncompressedOpenFilesLowWater () {
		return mUncompressedOpenFilesLowWater;
	}
	
	public int getActionsPerFile () {
		return mActionsPerFile;
	}
	
	public int getBytesPerFile () {
		return mBytesPerFile;
	}
	
	
	
	
	
}
