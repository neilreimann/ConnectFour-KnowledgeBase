package com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;

public interface IReadWriteCacheSegment {
	
	public byte [] readScore(int pFileIndex, int pSize) throws IOException, CacheSegmentStateException, ConfigurationException, DataFormatException;
	
	public void writeScore(int pFileIndex, byte [] pScoreToWrite, int pSize) throws IOException, DataFormatException, CacheSegmentStateException, ConfigurationException;
	

}
