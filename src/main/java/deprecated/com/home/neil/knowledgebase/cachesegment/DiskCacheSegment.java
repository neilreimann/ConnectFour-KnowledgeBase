package deprecated.com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.IReadWriteCacheSegment;

import deprecated.com.home.neil.knowledgebase.cachesegment.memory.MemoryCacheSegment.MEMORYCACHESTATE;

public class DiskCacheSegment implements IReadWriteCacheSegment, IKnowledgeBaseObject{
	public static final String CLASS_NAME = DiskCacheSegment.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);


	public enum DISKCACHESTATE {
		INSTANTIATED, READY, RETIRED
	}

	private DISKCACHESTATE mCacheSegmentState = MEMORYCACHESTATE.INSTANTIATED;
	private byte[] mCacheSegmentBytes = null;
	private boolean mCacheSegmentDirty = false;
	
	private final Object mCacheSegmentStateLock = new Object();
	private boolean mThreadSafe = false;

	@Override
	public void init() throws KnowledgeBaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retire() throws KnowledgeBaseException {
		// TODO Auto-generated method stub
		
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
	public byte[] getRetiredBytes() throws CacheSegmentStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCacheSegmentDirty() {
		// TODO Auto-generated method stub
		return false;
	}

}
