package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appconfig.AppConfig;
import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThread;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThreadFactory;

public class CompressableCacheSegmentRetiringThreadFactory implements IPoolItemRetiringThreadFactory {
	public static final String CLASS_NAME = CompressableCacheSegmentRetiringThreadFactory.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public CompressableCacheSegmentRetiringThreadFactory() {
		// Do Nothing, this is just a factory class
	}

	@Override
	public IPoolItemRetiringThread getRetiringThread(IPool pPool, int pSubPoolLevel) throws KnowledgeBaseException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		CompressableCacheSegmentRetiringThreadConfig lConfig;
		try {
			lConfig = AppConfig
					.bind(CompressableCacheSegmentRetiringThreadConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException
				| IOException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new KnowledgeBaseException ("Unable to read configuration for CompressableCacheSegmentRetiring Thread", e);
		}

		IPoolItemRetiringThread lAppRetiringTask = (IPoolItemRetiringThread) new CompressableCacheSegmentRetiringThread(
				pPool, pSubPoolLevel, lConfig, null);

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lAppRetiringTask;
	}
}
