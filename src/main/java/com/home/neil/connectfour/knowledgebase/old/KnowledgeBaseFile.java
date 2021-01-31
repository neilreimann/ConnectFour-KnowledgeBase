package com.home.neil.connectfour.knowledgebase.old;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.knowledgebase.old.exception.KnowledgeBaseException;
import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;

public class KnowledgeBaseFile {
	public static final String CLASS_NAME = KnowledgeBaseFile.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private static final String COMPRESSED_EXT = ".zip";
	private static final String COMPRESSED_DAT = ".dat";

	private byte[] mKnowledgeBaseCache = null;
	private boolean mKnowledgeBaseCacheDirty = false;

	private byte[] mKnowledgeBaseCacheMemoryCompression = null;
	private boolean mKnowledgeBaseCacheIsMemoryCompressed = false;
	private boolean mCompressedExists = false;
	
	private File mCompressedKnowledgeBaseBufferedFileDetails = null;
	private File mCompressedKnowledgeBaseBufferedFileDirectory = null;

	private String mStateFileDirectory = null;
	private String mStateFileName = null;

	private String mCompressedFileLocation = null;
	private String mCompressedFileDirectory = null;

	private boolean mCleaned = false;

	private boolean mBeingCleaned = false;
	

	private boolean mInitialized = false;

	private final Object mMemoryCompressionLock = new Object();
	
	private KnowledgeBaseFilePool mKnowledgeBaseFilePool = null;
	
	private long mKnowledgeBaseFileCompressedIndex = 0;
	private static long mKnowledgeBaseFileNextCompressedIndex = 0;

	private static synchronized long getNextKnowledgeBaseFileCompressedIndex () {
		return mKnowledgeBaseFileNextCompressedIndex++;
	}
	
	public long setKnowledgeBaseFileCompressedIndex() {
		mKnowledgeBaseFileCompressedIndex = KnowledgeBaseFile.getNextKnowledgeBaseFileCompressedIndex();
		return mKnowledgeBaseFileCompressedIndex;
	}
	
	public long getKnowledgeBaseFileCompressedIndex() {
		return mKnowledgeBaseFileCompressedIndex;
	}

	private long mKnowledgeBaseFileUncompressedIndex = 0;
	private static long mKnowledgeBaseFileNextUncompressedIndex = 0;

	private static synchronized long getNextKnowledgeBaseFileUncompressedIndex () {
		return mKnowledgeBaseFileNextUncompressedIndex++;
	}
	
	public long setKnowledgeBaseFileUncompressedIndex() {
		mKnowledgeBaseFileUncompressedIndex = KnowledgeBaseFile.getNextKnowledgeBaseFileUncompressedIndex();
		return mKnowledgeBaseFileUncompressedIndex;
	}
	
	public long getKnowledgeBaseFileUncompressedIndex() {
		return mKnowledgeBaseFileUncompressedIndex;
	}
	
	
	
	public KnowledgeBaseFile(KnowledgeBaseFilePool pKnowledgeBaseFilePool, String pStateFileDirectory, String pStateFileLocation) throws ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		
		setKnowledgeBaseFileCompressedIndex();

		mStateFileDirectory = pStateFileDirectory;
		mStateFileName = pStateFileLocation;
		mKnowledgeBaseFilePool = pKnowledgeBaseFilePool;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}
	

	public String getCompressFileLocation () {
		return mCompressedFileLocation;
	}
	

	public void initialize() throws IOException,  DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		mCompressedFileDirectory = mKnowledgeBaseFilePool.getKnowledgeBaseDirectory() + mStateFileDirectory;

		mCompressedFileLocation = mCompressedFileDirectory + "/" + mStateFileName + COMPRESSED_EXT;

		mCompressedKnowledgeBaseBufferedFileDetails = new File(mCompressedFileLocation);
		mCompressedKnowledgeBaseBufferedFileDirectory = new File(mCompressedFileDirectory);

		mCompressedExists = mCompressedKnowledgeBaseBufferedFileDetails.exists();
		if (mCompressedExists) {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Opening KnowledgeBase: " + mStateFileName);
			}

			readKnowledgeBaseFile();

			mKnowledgeBaseCacheDirty = false;
		}

		addAndCheckMemoryCompressions(this);

		mInitialized = true;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}
	
	
	private void readKnowledgeBaseFile() throws IOException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Reading KnowledgeBase File: " + mCompressedFileLocation);
		}
		
		FileInputStream lFileIn = new FileInputStream(mCompressedKnowledgeBaseBufferedFileDetails);
		BufferedInputStream lBIS = new BufferedInputStream(lFileIn, mKnowledgeBaseFilePool.getBytesPerFile());

		mKnowledgeBaseCacheMemoryCompression = new byte[(int) mCompressedKnowledgeBaseBufferedFileDetails.length()];
		
		lBIS.read(mKnowledgeBaseCacheMemoryCompression);
		
		lBIS.close();
		lFileIn.close();


		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Read Successful: " + mCompressedFileLocation);
			sLogger.error("Bytes Read: " + mKnowledgeBaseCacheMemoryCompression.length);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	private void retireKnowledgeBaseFile() throws IOException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Writing KnowledgeBase: " + mCompressedFileLocation);
		}

		mCompressedKnowledgeBaseBufferedFileDirectory.mkdirs();

		FileOutputStream lFileOut = new FileOutputStream(mCompressedKnowledgeBaseBufferedFileDetails);
		BufferedOutputStream lBOS = new BufferedOutputStream(lFileOut);

		lBOS.write(mKnowledgeBaseCacheMemoryCompression, 0, mKnowledgeBaseCacheMemoryCompression.length);
		
		lBOS.flush();
		lBOS.close();

		
		if (sLogger.isDebugEnabled()) {
			uncompressMemory();
			
			lFileOut = new FileOutputStream(new File (mCompressedFileLocation = mCompressedFileDirectory + "/" + mStateFileName + COMPRESSED_DAT));
		
			lBOS = new BufferedOutputStream(lFileOut);
		
			lBOS.write(mKnowledgeBaseCache, 0, mKnowledgeBaseCache.length);
			
			lBOS.flush();
			lBOS.close();
		}		

		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compression Successful: " + mCompressedFileLocation);
			sLogger.debug("Bytes Written: " + mKnowledgeBaseCacheMemoryCompression.length);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}


	private void addAndCheckMemoryCompressions (KnowledgeBaseFile pKnowledgeBaseFile) throws IOException, DataFormatException {
		while (mKnowledgeBaseFilePool.checkKnowledgeBaseUncompressedFilesReachedHighWaterMark()) {
			
		}
		synchronized(mMemoryCompressionLock) {
			mKnowledgeBaseCacheIsMemoryCompressed = true;
			if (mCompressedExists) {
				pKnowledgeBaseFile.uncompressMemory();
			} else {
				mKnowledgeBaseCache = new byte[mKnowledgeBaseFilePool.getBytesPerFile()];
				mKnowledgeBaseCacheMemoryCompression = null;
			}
			mKnowledgeBaseCacheIsMemoryCompressed = false;

			//sLogger.error("Adding File By Creation: " + pKnowledgeBaseFile.getFileLocation());

			mKnowledgeBaseFilePool.addLastAccessedMemoryUncompressedKnowledgeBaseFiles(pKnowledgeBaseFile);

		}
	}

	
	// Mark to unsynchronize
	private void accessAndCheckMemoryCompressions (KnowledgeBaseFile pKnowledgeBaseFile) throws IOException, DataFormatException {
		boolean lUncompressing = false;
//		synchronized(mMemoryCompressionLock) {
			if (pKnowledgeBaseFile.mKnowledgeBaseCacheIsMemoryCompressed) {
				pKnowledgeBaseFile.uncompressMemory();
				lUncompressing = true;
//			}				

			mKnowledgeBaseFilePool.resetLastAccessedMemoryUncompressedKnowledgeBaseFiles(pKnowledgeBaseFile);
		}
	}
	
	// Mark to unsynchronize
	private void removeAndCheckMemoryCompressions (KnowledgeBaseFile pKnowledgeBaseFile) throws IOException, DataFormatException {
//		synchronized(mMemoryCompressionLock) {
			if (!pKnowledgeBaseFile.mKnowledgeBaseCacheIsMemoryCompressed) {
				pKnowledgeBaseFile.compressMemory();
			}				
			mKnowledgeBaseFilePool.removeLastAccessedMemoryUncompressedKnowledgeBaseFiles(pKnowledgeBaseFile);
//		}
	}
	
	
	
	

	
	public void compressMemory () throws IOException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compressing Memory");
		}
		
		synchronized(mMemoryCompressionLock) {
			if (!mKnowledgeBaseCacheIsMemoryCompressed) {
			
				Deflater lDeflater = new Deflater();
				lDeflater.setInput(mKnowledgeBaseCache);
				
				ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(mKnowledgeBaseFilePool.getBytesPerFile()); 
				
				lDeflater.finish();  
		
				byte[] lBuffer = new byte[1024];   
		
				while (!lDeflater.finished()) {  
		
				    int count = lDeflater.deflate(lBuffer); // returns the generated code... index  
		
				    lBAOS.write(lBuffer, 0, count);   
		
				}  
		
				lBAOS.close();  
		
				mKnowledgeBaseCacheMemoryCompression = lBAOS.toByteArray();  
		
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Original: " + mKnowledgeBaseCache.length + " bytes");  
					sLogger.debug("Compressed: " + mKnowledgeBaseCacheMemoryCompression.length + " bytes"); 
				}				
				mKnowledgeBaseCache = null;
				
				mKnowledgeBaseCacheIsMemoryCompressed = true;
				
			}
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Compression Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
				
	}
	
	private void uncompressMemory () throws IOException,DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Decompressing Memory");
		}
		
		synchronized(mMemoryCompressionLock) {
			if (mKnowledgeBaseCacheIsMemoryCompressed) {
				Inflater lInflater = new Inflater();
				
				lInflater.setInput(mKnowledgeBaseCacheMemoryCompression);
				
		
				ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(mKnowledgeBaseFilePool.getBytesPerFile()); 
				
				byte[] lBuffer = new byte[1024];   
		
				while (!lInflater.finished()) {  
		
				    int count = lInflater.inflate(lBuffer); // returns the generated code... index  
		
				    lBAOS.write(lBuffer, 0, count);   
		
				}  
		
				lBAOS.close();  
		
				mKnowledgeBaseCache = lBAOS.toByteArray();  
		
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("Original: " + mKnowledgeBaseCacheMemoryCompression.length + " bytes");  
					sLogger.debug("Uncompressed: " + mKnowledgeBaseCache.length + " bytes"); 
				}				
				mKnowledgeBaseCacheMemoryCompression = null;
				
				mKnowledgeBaseCacheIsMemoryCompressed = false;
			}
		}
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Memory Decompression Successful");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		
	}

	

	public void setBeingCleaned() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		mBeingCleaned = true;
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public boolean getBeingCleaned() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mBeingCleaned;
	}

	public boolean isCleaned() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mCleaned;
	}

	public void cleanup() throws IOException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		setBeingCleaned();

		if (mCleaned) {
			sLogger.warn("Object has been cleaned!  GO AWAY!");
			sLogger.trace("Exiting");
			throw new IOException();
		}
		synchronized(mMemoryCompressionLock) {
			removeAndCheckMemoryCompressions(this);
	
			if (mKnowledgeBaseCacheDirty) {
				retireKnowledgeBaseFile();
			}
		}
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("KnowledgeBaseFile is closed: " + mStateFileName);
		}
		mCleaned = true;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public String getFileLocation() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
		return mStateFileName;
	}

	public void writeScore(int pFileIndex, byte pScoreToWrite) throws IOException, DataFormatException, KnowledgeBaseException, ConfigurationException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (mCleaned || mBeingCleaned) {
			sLogger.warn("Object has been cleaned!  GO AWAY!");
			sLogger.trace("Exiting");
			throw new KnowledgeBaseException();
		}

		synchronized(mMemoryCompressionLock) {
			if (!mInitialized) {
				initialize();
			} else {
				accessAndCheckMemoryCompressions(this);
			}

		
			if (!(mKnowledgeBaseCache[pFileIndex] == pScoreToWrite)) {
				mKnowledgeBaseCache[pFileIndex] = pScoreToWrite;
				mKnowledgeBaseCacheDirty = true;
			}
		}
		
		if (sLogger.isDebugEnabled()) {
			if (pFileIndex % 8 == 0 && pFileIndex != 0 && pScoreToWrite == 6 && mStateFileName.equals("XMove")) {
				sLogger.error("KnowledgeBaseFile Write Evaluated: " + pScoreToWrite + " at: " + pFileIndex + " in " + mCompressedFileLocation);
			}		

			sLogger.debug("KnowledgeBaseFile Write Score: " + pScoreToWrite + " at: " + pFileIndex);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}
	}

	public byte readScore(int pFileIndex) throws IOException, KnowledgeBaseException, ConfigurationException, DataFormatException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Entering");
		}

		if (mCleaned || mBeingCleaned) {
			sLogger.error("Object has been cleaned!  GO AWAY!");
			sLogger.trace("Exiting");
			throw new KnowledgeBaseException();
		}

		byte lScoreRead = 0;

		synchronized(mMemoryCompressionLock) {
			if (!mInitialized) {
				initialize();
			} else {
				accessAndCheckMemoryCompressions(this);
			}

			
			lScoreRead = mKnowledgeBaseCache[pFileIndex];
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("KnowledgeBaseFile Read Score: " + lScoreRead + " at: " + pFileIndex);
		}
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace("Exiting");
		}

		return lScoreRead;
	}

}
