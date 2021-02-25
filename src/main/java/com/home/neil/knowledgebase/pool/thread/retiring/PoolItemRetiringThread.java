package com.home.neil.knowledgebase.pool.thread.retiring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.thread.SteppedThrottledAppThread;

public abstract class PoolItemRetiringThread extends SteppedThrottledAppThread {
	public static final String CLASS_NAME = PoolItemRetiringThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	protected IPool mPool = null;
	
	public PoolItemRetiringThread(IPool pPool, String pLogContext, int pMaxThrottleCount, int pThrottleValue) {
		super(false, pThrottleValue, pMaxThrottleCount, pLogContext, true);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		mPool = pPool;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

}
