package com.home.neil.knowledgebase.pool.thread.initialization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.task.BasicAppTask;

public abstract class PoolItemInitializationTask extends BasicAppTask implements IPoolItemInitializationTask {
	public static final String CLASS_NAME = PoolItemInitializationTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected IPool mPool = null;
	protected String mPoolItemId = null;
	protected IPoolItem mPoolItem = null;
	
	
	protected PoolItemInitializationTask(IPool pPool, String pLogContext, boolean pRecordThreadStatistics) {
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

	public IPoolItem getPoolItem() {
		return mPoolItem;
	}

	public String getPoolItemId() {
		return mPoolItemId;
	}


}
