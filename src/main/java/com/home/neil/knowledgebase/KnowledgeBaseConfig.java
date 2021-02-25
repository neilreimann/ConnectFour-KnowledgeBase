package com.home.neil.knowledgebase;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appconfig.Connect4PropertiesConfiguration;

public class KnowledgeBaseConfig {
	public static final String CLASS_NAME = KnowledgeBaseConfig.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private static final String CONFIGKEY_ACTIONCOUNTPERCACHE = CLASS_NAME + ".ActionCountPerCache";
	private int mActionCountPerCache;
	
	private static final String CONFIGKEY_PATHCOUNTPERCACTION = CLASS_NAME + ".PathCountPerAction";
	private int mPathCountPerAction;
	
	private static final String CONFIGKEY_METADATASIZEINBYTESPERACTION = CLASS_NAME + ".MetaDataSizeInBytesPerAction";
	private int mMetaDataSizeInBytesPerAction;
	
	private static final String CONFIGKEY_SCORESIZEINBYTES = CLASS_NAME + ".ScoreSizeInBytes";
	private int mScoreSizeInBytes;

	private static KnowledgeBaseConfig sInstance = null;
	
	private KnowledgeBaseConfig () throws NoSuchElementException, ConfigurationException, IOException {

		Connect4PropertiesConfiguration lConfig = Connect4PropertiesConfiguration.getInstance();

		mActionCountPerCache = lConfig.getInt(CONFIGKEY_ACTIONCOUNTPERCACHE, 8);
		mPathCountPerAction = lConfig.getInt(CONFIGKEY_PATHCOUNTPERCACTION, 7);
		mMetaDataSizeInBytesPerAction = lConfig.getInt(CONFIGKEY_METADATASIZEINBYTESPERACTION, 1);
		mScoreSizeInBytes = lConfig.getInt(CONFIGKEY_SCORESIZEINBYTES, 1);
	}

	public static synchronized KnowledgeBaseConfig getInstance () throws NoSuchElementException, ConfigurationException, IOException {
		if (sInstance == null) {
			sInstance = new KnowledgeBaseConfig ();
		}
		return sInstance;
	}
	
	
	public int getActionCountPerCache() {
		return mActionCountPerCache;
	}

	public int getPathCountPerAction() {
		return mPathCountPerAction;
	}

	public int getMetaDataSizeInBytesPerAction() {
		return mMetaDataSizeInBytesPerAction;
	}

	public int getScoreSizeInBytes() {
		return mScoreSizeInBytes;
	}
	
	public int getFileSize () {
		return ((mScoreSizeInBytes * mPathCountPerAction) + mMetaDataSizeInBytesPerAction) * mActionCountPerCache; 
	}
	
}
