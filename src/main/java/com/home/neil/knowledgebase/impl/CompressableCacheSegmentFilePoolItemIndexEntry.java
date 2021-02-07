package com.home.neil.knowledgebase.impl;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentFile;
import com.home.neil.knowledgebase.cachesegment.ICompressableCacheSegment;
import com.home.neil.knowledgebase.index.IndexEntry;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.PoolException;

public class CompressableCacheSegmentFilePoolItemIndexEntry extends IndexEntry implements IPoolItem, ICompressableCacheSegment, IKnowledgeBaseObject {
	public static final String CLASS_NAME = CompressableCacheSegmentFilePoolItemIndexEntry.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private CompressableCacheSegmentFile mCompressableCacheSegmentFile = null;
	
	public CompressableCacheSegmentFilePoolItemIndexEntry (CompressableCacheSegmentFile pCompressableCacheSegmentFile) {
		mCompressableCacheSegmentFile = pCompressableCacheSegmentFile;
		setIndexEntry ();
	}
	
	public long setIndexEntry() {
		return setIndexEntry (CompressableCacheSegmentFilePoolItemIndexEntry.class);
	}
	
	public CompressableCacheSegmentFile getCompressableCacheSegmentFile() {
		return mCompressableCacheSegmentFile;
	}

	public void init() throws KnowledgeBaseException {
		try {
			mCompressableCacheSegmentFile.init();
		} catch (CacheSegmentStateException e) {
			sLogger.error("CacheSegmentStateException: Could not Initialize CompressableCacheSegmentFile Object!");
			throw e;
		} catch (Exception e) {
			sLogger.error("IOException: Could not Initialize CompressableCacheSegmentFile Object!");
			throw new PoolException (e);
		}
	}

	public void retire() throws KnowledgeBaseException {
		try {
			mCompressableCacheSegmentFile.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.error("CacheSegmentStateException: Could not Retire CompressableCacheSegmentFile Object!");
			throw new PoolException ();
		} 
	}

	public String getPoolItemId() {
		return mCompressableCacheSegmentFile.getFileName();
	}

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
	public boolean isCacheSegmentDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void uncompress() throws IOException, DataFormatException, CacheSegmentStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void compress() throws IOException, CacheSegmentStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getRetiredUnCompressedCacheSegment() throws CacheSegmentStateException {
		// TODO Auto-generated method stub
		return null;
	}


}
