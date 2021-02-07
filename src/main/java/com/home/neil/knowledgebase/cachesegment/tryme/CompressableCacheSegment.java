package com.home.neil.knowledgebase.cachesegment.tryme;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.IReadWriteCacheSegment;

public class CompressableCacheSegment implements IKnowledgeBaseObject, IReadWriteCacheSegment {
	public static final String CLASS_NAME = CompressableCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public enum COMPRESSABLECACHESEGMENTSTATE {
		INSTANTIATED, COMPRESSEDFILE, COMPRESSEDMEMORY, UNCOMPRESSEDFILE, READY, RETIRED
	}
	
	private COMPRESSABLECACHESEGMENTSTATE mCacheSegmentState = COMPRESSABLECACHESEGMENTSTATE.INSTANTIATED;

	protected int mCacheSegmentUncompressedSize = 0;
	
	// COMPRESSEDFILE State Variables
	private String mCompressedFile_BasePath = null;
	private String[] mCompressedFile_StatePaths = null;
	private String mCompressedFile_FileName = null;
	
	private String mCompressedFile_FullPathDirectoryName = null;
	private String mCompressedFile_FullPathFileName = null;
	private String mCompressedFile_FullPathDebugFileName = null;

	private static final String COMPRESSEDFILE_EXT = ".zip";
	private static final String DEBUG_COMPRESSEDFILE_EXT = ".dat";

	private File mCompressedFile_FullPathFile = null;
	private File mCompressedFile_FullPathDirectory = null;
	private File mCompressedFile_FullPathDebugFile = null;

	//COMPRESSEDMEMORY State Variables
	private byte [] mCompressedMemory_CacheSegmentBytes = null;
	private boolean mCompressedMemory_CacheSegmentDirty = false;
	
	//UNCOMPRESSEDFILE State Variables
	private String mUncompressedFile_BasePath = null;
	private String mUncompressedFile_FileName = null;
	
	private String mUncompressedFile_FullPathDirectoryName = null;
	private String mUncompressedFile_FullPathFileName = null;

	private static final String UNCOMPRESSEDFILE_EXT = ".cache";

	private File mUncompressedFile_FullPathFile = null;
	private File mUncompressedFile_FullPathDirectory = null;
	
	private boolean mUncompressedFile_CacheSegmentDirty = false;
	
	// READY State Variables
	private byte[] mUncompressedCacheSegmentBytes = null;
	private boolean mUncompressedCacheSegmentDirty = false;
	
	// Thread Safety Variables
	private final Object mCacheSegmentStateLock = new Object();
	private boolean mThreadSafe = false;

	
	//The State Transitions
	public void loadCompressedFileToCompressedMemory ();

	public void saveCompressedMemoryToCompressedFile ();
	
	
	public void uncompressMemoryToUncompressedCacheFile();
	
	public void compressCacheFileToCompressedMemory();
	
	
	public void loadUnCompressedFileToUnCompressedMemory();
	
	public void saveUncompressedMemoryToUnCompressedFile();

	@Override
	public byte[] readScore(int pFileIndex, int pSize)
			throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeScore(int pFileIndex, byte[] pScoreToWrite, int pSize)
			throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() throws KnowledgeBaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retire() throws KnowledgeBaseException {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
