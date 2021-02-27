package com.home.neil.knowledgebase.cachesegment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;

public class CompressableCacheSegment implements IKnowledgeBaseObject, IReadWriteCacheSegment {
	public static final String CLASS_NAME = CompressableCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public enum COMPRESSABLECACHESEGMENTSTATE {
		UNINSTANTIATED, INSTANTIATED, COMPRESSEDFILE, COMPRESSEDMEMORY, UNCOMPRESSEDFILE, READY, RETIRED, ERROR
	}
	
	// COMPRESSEDFILE State Variables
	private class CompressedFileStateVars {

		private String mBasePath = null;
		private String[] mStatePaths = null;
		private String mFileName = null;
		
		private String mFullPathDirectoryName = null;
		private String mFullPathFileName = null;

		private File mFullPathFile = null;
		private File mFullPathDirectory = null;
	}
	
	// COMPRESSEDMEMORY State Variables
	private class CompressedMemoryStateVars {
		private byte[] mCacheSegmentBytes = null;
		private boolean mCacheSegmentDirty = false;
	}
	
	// UNCOMPRESSEDFILE State Variables
	private class UncompressedFileStateVars {
		private String mBasePath = null;
		private String[] mStatePaths = null;
		private String mFileName = null;
	
		private String mFullPathDirectoryName = null;
		private String mFullPathFileName = null;
		private String mFullPathDebugFileName = null;
	
		private File mFullPathFile = null;
		private File mFullPathDebugFile = null;
		private File mFullPathDirectory = null;
	
		private boolean mCacheSegmentDirty = false;
	}
	
	// READY State Variables
	private class UncompressedMemoryStateVars {
		private byte[] mCacheSegmentBytes = null;
		private boolean mCacheSegmentDirty = false;
	}

	private COMPRESSABLECACHESEGMENTSTATE mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.UNINSTANTIATED;

	protected int mCacheSegmentUncompressedSize = 0;

	private boolean mDebug = false;

	private CompressedFileStateVars mCompressedFileStateVars = null;
	private CompressedMemoryStateVars mCompressedMemoryStateVars = null;
	private UncompressedFileStateVars mUncompressedFileStateVars = null; 
	private UncompressedMemoryStateVars mUncompressedMemoryStateVars = null;
	
	// Thread Safety Variables
	private final Object mCacheSegmentStateLock = new Object();
	private boolean mThreadSafe = false;



	public CompressableCacheSegment(CompressableCacheSegmentConfig pConfig, String[] pStatePaths, String pFileName) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		mCompressedFileStateVars = new CompressedFileStateVars();
		mCompressedMemoryStateVars = new CompressedMemoryStateVars();
		mUncompressedFileStateVars = new UncompressedFileStateVars();
		mUncompressedMemoryStateVars = new UncompressedMemoryStateVars();
				
		mCompressedFileStateVars.mBasePath = pConfig.getCompressedFileBasePath();
		
		mCompressedFileStateVars.mBasePath = pConfig.getCompressedFileBasePath();
		mCompressedFileStateVars.mStatePaths = pStatePaths;
		mCompressedFileStateVars.mFileName = pFileName;
		mCompressedFileStateVars.mFullPathFileName = mCompressedFileStateVars.mBasePath;

		StringBuilder lCompressedFilePathBuilder = new StringBuilder();
		for (String lStatePath : mCompressedFileStateVars.mStatePaths) {
			  lCompressedFilePathBuilder.append("/" + lStatePath);
		}
		mCompressedFileStateVars.mFullPathFileName += lCompressedFilePathBuilder.toString();
		
		mCompressedFileStateVars.mFullPathDirectoryName = mCompressedFileStateVars.mFullPathFileName;
		mCompressedFileStateVars.mFullPathFileName += "/" + mCompressedFileStateVars.mFileName + "." + pConfig.getCompressedFileExtension();

		mCompressedFileStateVars.mFullPathDirectory = new File(mCompressedFileStateVars.mFullPathDirectoryName);
		mCompressedFileStateVars.mFullPathFile = new File(mCompressedFileStateVars.mFullPathFileName);

		mCacheSegmentUncompressedSize = pConfig.getCacheSegmentUncompressedSize();

		
		
		mUncompressedFileStateVars.mBasePath = pConfig.getUncompressedFileBasePath();
		mUncompressedFileStateVars.mStatePaths = pStatePaths;
		mUncompressedFileStateVars.mFileName = pFileName;
		mUncompressedFileStateVars.mFullPathDirectoryName = mUncompressedFileStateVars.mBasePath;
		mUncompressedFileStateVars.mFullPathFileName = mUncompressedFileStateVars.mBasePath;
		mUncompressedFileStateVars.mFullPathDebugFileName = mUncompressedFileStateVars.mBasePath;

		StringBuilder lUncompressedFilePathBuilder = new StringBuilder();
		for (String lStatePath : mUncompressedFileStateVars.mStatePaths) {
			lUncompressedFilePathBuilder.append("/" + lStatePath);
		}
		
		mUncompressedFileStateVars.mFullPathFileName += lUncompressedFilePathBuilder.toString();
		mUncompressedFileStateVars.mFullPathDebugFileName += lUncompressedFilePathBuilder.toString();
		
		mUncompressedFileStateVars.mFullPathFileName += "/" + pFileName + "." + pConfig.getUncompressedFileExtension();
		mUncompressedFileStateVars.mFullPathDebugFileName += "/" + pFileName + "." + pConfig.getUncompressedFileDebugExtension();

		mUncompressedFileStateVars.mFullPathDirectory = new File(mUncompressedFileStateVars.mFullPathDirectoryName);
		mUncompressedFileStateVars.mFullPathFile = new File(mUncompressedFileStateVars.mFullPathFileName);
		mUncompressedFileStateVars.mFullPathDebugFile = new File(mUncompressedFileStateVars.mFullPathDebugFileName);

		mDebug = pConfig.getDebug();
		mThreadSafe = pConfig.getThreadSafe();

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.INSTANTIATED;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void init() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				initCritical();
			}
		} else {
			initCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void initCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.INSTANTIATED) {
			sLogger.error("CompressableCacheSegment is not in a INSTANTIATED state!  GO AWAY! State: {} ",
					mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		if (mCompressedFileStateVars.mFullPathFile.exists()) {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Opening Compressed Cache Segment File: {} ", mCompressedFileStateVars.mFullPathFileName);
			}

			loadCompressedFileToCompressedMemory();

			loadCompressedMemoryToUncompressedFile();

			loadUncompressedFileToUncompressedMemory();

		} else {
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Creating New Compressed Cache Segment File: {} ", mCompressedFileStateVars.mFullPathFileName);
			}
			initNew();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	// already threadsafe
	private void initNew() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.INSTANTIATED) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a instantiated state!  GO AWAY! State: " + mCacheSegmentState);
		}

		mUncompressedMemoryStateVars.mCacheSegmentBytes = new byte[mCacheSegmentUncompressedSize];
		mUncompressedMemoryStateVars.mCacheSegmentDirty = true;

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.READY;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void reinit() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				reinitCritical();
			}
		} else {
			reinitCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void reinitCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDFILE ||
				mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY || 
				mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE ||
				mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.READY) {
			sLogger.error("CompressableCacheSegment is not in a COMPRESSEDFILE, COMPRESSEDMEMORY, UNCOMPRESSEDFILE OR READY state!  GO AWAY! State: {} ",
					mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		}

		
		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDFILE) {
			loadCompressedFileToCompressedMemory();
		}
	
		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY) {
			loadCompressedMemoryToUncompressedFile();
		}
		
		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE) {
			loadUncompressedFileToUncompressedMemory();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	
	// The State Transitions
	public void loadCompressedFileToCompressedMemory() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				loadCompressedFileToCompressedMemoryCritical();
			}
		} else {
			loadCompressedFileToCompressedMemoryCritical();
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void loadCompressedFileToCompressedMemoryCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDFILE ||
				mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.INSTANTIATED) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a COMPRESSEDFILE state!  GO AWAY! State: "
							+ mCacheSegmentState);
		}

		try (FileInputStream lFileIn = new FileInputStream(mCompressedFileStateVars.mFullPathFileName);
				BufferedInputStream lBIS = new BufferedInputStream(lFileIn,
						(int) mCompressedFileStateVars.mFullPathFile.length());) {

			mCompressedMemoryStateVars.mCacheSegmentBytes = new byte[(int) mCompressedFileStateVars.mFullPathFile.length()];

			int lBytesRead = lBIS.read(mCompressedMemoryStateVars.mCacheSegmentBytes);
			if (lBytesRead < 1) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException("Bytes read by Compressed File is too small.");
			}

			mCompressedMemoryStateVars.mCacheSegmentDirty = false;
		} catch (IOException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
			throw new CacheSegmentStateException(e);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Read Successful: {}", mCompressedFileStateVars.mFullPathFile);
			sLogger.error("Bytes Read: {}", mCompressedFileStateVars.mFullPathFile.length());
		}

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void loadCompressedMemoryToUncompressedFile() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				loadCompressedMemoryToUncompressedFileCritical();
			}
		} else {
			loadCompressedMemoryToUncompressedFileCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void loadCompressedMemoryToUncompressedFileCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a COMPRESSEDMEMORY state!  GO AWAY! State: "
							+ mCacheSegmentState);
		}

		mUncompressedFileStateVars.mFullPathDirectory.mkdirs();

		Inflater lInflater = new Inflater();

		lInflater.setInput(mCompressedMemoryStateVars.mCacheSegmentBytes);

		try (FileOutputStream lBAOS = new FileOutputStream(mUncompressedFileStateVars.mFullPathFile)) {
			byte[] lBuffer = new byte[1024];

			while (!lInflater.finished()) {

				int count = lInflater.inflate(lBuffer); // returns the generated code... index
				lBAOS.write(lBuffer, 0, count);
			}
		} catch (DataFormatException | IOException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(e);
		}

		mCompressedMemoryStateVars.mCacheSegmentBytes = null;
		mCompressedMemoryStateVars.mCacheSegmentDirty = false;

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE;

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Uncompressed File Write Successful: {}", mUncompressedFileStateVars.mFileName);
			sLogger.debug("Uncompressed File Bytes Written: {}", mUncompressedFileStateVars.mFullPathFile.length());
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	public void loadUncompressedFileToUncompressedMemory() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				loadUncompressedFileToUncompressedMemoryCritical();
			}
		} else {
			loadUncompressedFileToUncompressedMemoryCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void loadUncompressedFileToUncompressedMemoryCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}

			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a UNCOMPRESSEDFILE state!  GO AWAY! State: "
							+ mCacheSegmentState);
		}

		try (FileInputStream lFileIn = new FileInputStream(mUncompressedFileStateVars.mFullPathFileName);
				BufferedInputStream lBIS = new BufferedInputStream(lFileIn, mCacheSegmentUncompressedSize);) {

			mUncompressedMemoryStateVars.mCacheSegmentBytes = new byte[(int) mCacheSegmentUncompressedSize];

			int lBytesRead = lBIS.read(mUncompressedMemoryStateVars.mCacheSegmentBytes);

			if (lBytesRead != mCacheSegmentUncompressedSize) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException("Uncompressed Bytes Read does not match Cache Segment Size set");
			}

			mUncompressedMemoryStateVars.mCacheSegmentDirty = false;
		} catch (IOException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
			throw new CacheSegmentStateException(e);
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Uncompressed File Read Successful: {}", mUncompressedFileStateVars.mFullPathFile);
			sLogger.error("Uncompressed File Bytes Read: {}", mCacheSegmentUncompressedSize);
		}

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.READY;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	public void saveUncompressedMemoryToUncompressedFile() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				saveUncompressedMemoryToUncompressedFileCritical();
			}
		} else {
			saveUncompressedMemoryToUncompressedFileCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	private void saveUncompressedMemoryToUncompressedFileCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.READY) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}

			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a READY state!  GO AWAY! State: " + mCacheSegmentState);
		}

		if (mUncompressedMemoryStateVars.mCacheSegmentDirty) {
			try (FileOutputStream lFileOut = new FileOutputStream(mUncompressedFileStateVars.mFullPathFile);
					BufferedOutputStream lBOS = new BufferedOutputStream(lFileOut);) {

				lBOS.write(mUncompressedMemoryStateVars.mCacheSegmentBytes);

			} catch (IOException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException(e);
			}

			if (mDebug) {
				try (FileOutputStream lFileOut = new FileOutputStream(mUncompressedFileStateVars.mFullPathDebugFile);
						BufferedOutputStream lBOS = new BufferedOutputStream(lFileOut);) {

					lBOS.write(mUncompressedMemoryStateVars.mCacheSegmentBytes);

				} catch (IOException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
					throw new CacheSegmentStateException(e);
				}

			}

			mUncompressedFileStateVars.mCacheSegmentDirty = true;
		} else {
			mUncompressedFileStateVars.mCacheSegmentDirty = false;
		}

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Uncompressed File Write Successful: {}", mUncompressedFileStateVars.mFullPathFileName);
			sLogger.debug("Uncompressed File Bytes Written: {}", mCacheSegmentUncompressedSize);
		}

		mUncompressedMemoryStateVars.mCacheSegmentBytes = null;
		mUncompressedMemoryStateVars.mCacheSegmentDirty = false;

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void saveUncompressedFileToCompressedMemory() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				saveUncompressedFileToCompressedMemoryCritical();
			}
		} else {
			saveUncompressedFileToCompressedMemoryCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	private void saveUncompressedFileToCompressedMemoryCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}

			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a READY state!  GO AWAY! State: " + mCacheSegmentState);
		}

		if (mUncompressedFileStateVars.mCacheSegmentDirty) {

			try (FileInputStream lFileIn = new FileInputStream(mUncompressedFileStateVars.mFullPathFileName);
					BufferedInputStream lBIS = new BufferedInputStream(lFileIn, mCacheSegmentUncompressedSize);
					ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(mCacheSegmentUncompressedSize);) {

				Deflater lDeflater = new Deflater();

				lDeflater.setInput(lBIS.readAllBytes());

				lDeflater.finish();

				byte[] lBuffer = new byte[1024];

				while (!lDeflater.finished()) {
					int count = lDeflater.deflate(lBuffer); // returns the generated code... index
					lBAOS.write(lBuffer, 0, count);
				}

				mCompressedMemoryStateVars.mCacheSegmentBytes = lBAOS.toByteArray();

			} catch (IOException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException(e);
			}

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Deleting File: {}", mUncompressedFileStateVars.mFullPathFile.getAbsoluteFile());
			}

			try {
				Files.delete(Paths.get(mUncompressedFileStateVars.mFullPathFile.getAbsolutePath()));
			} catch (IOException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException(e);
			}
			
			sLogger.debug("Compressing Dirty Cache Segment");

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("CompressedMemory Bytes Written: {}", mCompressedMemoryStateVars.mCacheSegmentBytes.length);
			}

			mCompressedMemoryStateVars.mCacheSegmentDirty = true;
		} else {
			if (sLogger.isDebugEnabled()) {
				sLogger.error("CompressedMemory Not Dirty");
			}

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Deleting File: {}", mUncompressedFileStateVars.mFullPathFile.getAbsoluteFile());
			}

			try {
				Files.delete(Paths.get(mUncompressedFileStateVars.mFullPathFile.getAbsolutePath()));
			} catch (IOException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException(e);
			}
			
			mCompressedMemoryStateVars.mCacheSegmentDirty = false;
		}

		mUncompressedFileStateVars.mCacheSegmentDirty = false;
		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void saveCompressedMemoryToCompressedFile() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				saveCompressedMemoryToCompressedFileCritical();
			}
		} else {
			saveCompressedMemoryToCompressedFileCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	private void saveCompressedMemoryToCompressedFileCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"CompressableCacheSegment is not in a COMPRESSEDMEMORY state!  GO AWAY! State: "
							+ mCacheSegmentState);
		}

		if (mCompressedMemoryStateVars.mCacheSegmentDirty) {
			mCompressedFileStateVars.mFullPathDirectory.mkdirs();

			try (FileOutputStream lFileOut = new FileOutputStream(mCompressedFileStateVars.mFullPathFileName);
					BufferedOutputStream lBOS = new BufferedOutputStream(lFileOut)) {

				lBOS.write(mCompressedMemoryStateVars.mCacheSegmentBytes, 0, mCompressedMemoryStateVars.mCacheSegmentBytes.length);

			} catch (IOException e) {
				sLogger.error("Write KnowledgeBaseFile failed!");
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.ERROR;
				throw new CacheSegmentStateException(e);
			}

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("Compressed File Write Successful: {}", mCompressedFileStateVars.mFullPathFileName);
				sLogger.debug("Compressed File Bytes Written: {}", mCompressedFileStateVars.mFullPathFile.length());
			}
		} else {
			if (sLogger.isDebugEnabled()) {
				sLogger.error("CompressedMemory Not Dirty");
			}
		}

		// Clear the Memory usage
		mCompressedMemoryStateVars.mCacheSegmentBytes = null;
		mCompressedMemoryStateVars.mCacheSegmentDirty = false;

		mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDFILE;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void retire() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				retireCritical();
			}
		} else {
			retireCritical();
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void retireCritical() throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState == COMPRESSABLECACHESEGMENTSTATE.READY) {
			saveUncompressedMemoryToUncompressedFile();
		}

		if (mCacheSegmentState == COMPRESSABLECACHESEGMENTSTATE.UNCOMPRESSEDFILE) {
			saveUncompressedFileToCompressedMemory();
		}

		if (mCacheSegmentState == COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDMEMORY) {
			saveCompressedMemoryToCompressedFile();
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.COMPRESSEDFILE) {
			sLogger.error("CompressableCacheSegment is not in a COMPRESSEDFILE state!  GO AWAY! State: {} ",
					mCacheSegmentState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException();
		} else {
			mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.RETIRED;
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public byte[] readScore(int pFileIndex, int pSize) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte[] lScoreRead = null;
		
		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				lScoreRead = readScoreCritical(pFileIndex, pSize);
			}
		} else {
			lScoreRead = readScoreCritical(pFileIndex, pSize);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

		return lScoreRead;
	}

	
	private byte[] readScoreCritical (int pFileIndex, int pSize) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		byte[] lScoreRead = new byte[pSize];

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.READY) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"Cache Segment is not in a ready state!  GO AWAY! State: " + mCacheSegmentState);
		}
		for (int i = 0; i < pSize; i++) {
			lScoreRead[i] = mUncompressedMemoryStateVars.mCacheSegmentBytes[pFileIndex + i];
			if (sLogger.isTraceEnabled()) {
				sLogger.trace("Cache Segment Read Bytes: {} at: {}", lScoreRead[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

		return lScoreRead;
	}
	
	
	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mThreadSafe) {
			synchronized (mCacheSegmentStateLock) {
				writeScoreCritical(pFileIndex, pScoreToWrite, pSize);
			}
		} else {
			writeScoreCritical(pFileIndex, pScoreToWrite, pSize);
		}


		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void writeScoreCritical(int pFileIndex, byte[] pScoreToWrite, int pSize) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mCacheSegmentState != COMPRESSABLECACHESEGMENTSTATE.READY) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new CacheSegmentStateException(
					"Cache Segment is not in a ready state!  GO AWAY! State: " + mCacheSegmentState);
		}

		for (int i = 0; i < pSize; i++) {
			if (mUncompressedMemoryStateVars.mCacheSegmentBytes[pFileIndex + i] != pScoreToWrite[i]) {
				mUncompressedMemoryStateVars.mCacheSegmentBytes[pFileIndex + i] = pScoreToWrite[i];
				mUncompressedMemoryStateVars.mCacheSegmentDirty = true;
			}
			if (sLogger.isTraceEnabled()) {
				sLogger.trace("Cache Segment Write Bytes: {} at: {}", pScoreToWrite[i], pFileIndex + i);
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public boolean validateStateUsageMemoryUsage() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		
		switch (mCacheSegmentState) {
		case COMPRESSEDFILE:
		case UNCOMPRESSEDFILE:
		case RETIRED:
		case UNINSTANTIATED:
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return (mUncompressedMemoryStateVars.mCacheSegmentBytes == null && mCompressedMemoryStateVars.mCacheSegmentBytes == null);
		case COMPRESSEDMEMORY:
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return (mUncompressedMemoryStateVars.mCacheSegmentBytes == null && mCompressedMemoryStateVars.mCacheSegmentBytes != null);
		case READY:
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return (mUncompressedMemoryStateVars.mCacheSegmentBytes != null && mCompressedMemoryStateVars.mCacheSegmentBytes == null);
		default:
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return false;
		}
	}	
	
}
