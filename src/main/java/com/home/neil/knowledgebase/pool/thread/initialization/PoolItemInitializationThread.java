package com.home.neil.knowledgebase.pool.thread.initialization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.thread.BasicAppThread;

public abstract class PoolItemInitializationThread extends BasicAppThread implements IPoolItemInitializationThread {
	public static final String CLASS_NAME = PoolItemInitializationThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected IPool mPool = null;
	
	protected PoolItemInitializationThread(IPool pPool, String pLogContext, boolean pRecordThreadStatistics) {
		super(pLogContext, pRecordThreadStatistics);
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		mPool = pPool;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public IPool getPool() {
		return mPool;
	}

	@Override
	public IPoolItemInitializationTask getPoolItemInitializationTask() {
		return (IPoolItemInitializationTask) mAppTask;
	}

}
