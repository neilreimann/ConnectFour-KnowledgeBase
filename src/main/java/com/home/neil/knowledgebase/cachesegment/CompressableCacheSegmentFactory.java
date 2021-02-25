package com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appconfig.AppConfig;
import com.home.neil.appmanager.ApplicationPrecompilerSettings;

public class CompressableCacheSegmentFactory {
	public static final String CLASS_NAME = CompressableCacheSegmentFactory.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private CompressableCacheSegmentFactory () {
		
	}
	
	public static CompressableCacheSegment getCompressableCacheSegment (String[] pStatePaths,
			String pFileName) throws CacheSegmentStateException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegmentConfig lConfig = null;
		try {
			lConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException
				| IOException e) {
			throw new CacheSegmentStateException ("Error occurred creating CompressableCacheSegment", e);
		}

		CompressableCacheSegment lCompressableCacheSegment = new CompressableCacheSegment (lConfig, pStatePaths, pFileName);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lCompressableCacheSegment;
	}
}
