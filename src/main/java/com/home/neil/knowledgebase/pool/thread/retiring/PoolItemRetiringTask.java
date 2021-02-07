package com.home.neil.knowledgebase.pool.thread.retiring;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.PoolException;
import com.home.neil.task.SteppedThrottledAppTask;
import com.home.neil.task.TaskException;
import com.home.neil.thread.SteppedThrottledAppThread;

public abstract class PoolItemRetiringTask extends SteppedThrottledAppTask implements IPoolItemRetiringTask {
	public static final String CLASS_NAME = PoolItemRetiringTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected IPool mPool = null;
	
	protected PoolItemRetiringTask(IPool pPool, SteppedThrottledAppThread pSteppedThrottledAppThread, String pLogContext,
			boolean pRecordTaskStatistics) {
		super(pSteppedThrottledAppThread, pLogContext, pRecordTaskStatistics);

		mPool = pPool;
	}

	protected void executeTask() throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		List<IPoolItem> lPoolItemsToRetire = null;
		
		try {
			lPoolItemsToRetire = mPool.getRetiringPoolItems(0);
		} catch (PoolException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			mTaskSuccessful = false;
			mTaskFinished = true;
			throw new TaskException ("Pool Exception getting list of Pool Items To Retire", e);
		}
		
		if (lPoolItemsToRetire == null || lPoolItemsToRetire.isEmpty()) {
			mSteppedThrottledAppThread.throttleUp();

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			mTaskSuccessful = true;
			mTaskFinished = true;
			return;
			
		} else {
			mSteppedThrottledAppThread.throttleDown();
		}
		
		mTaskSuccessful = true;
		for (IPoolItem lPoolItemToRetire : lPoolItemsToRetire) {
			try {
				retirePoolItem(lPoolItemToRetire);
			} catch (KnowledgeBaseException e) {
				mTaskSuccessful = false;
				sLogger.error("KnowledgeBaseException occurred when attempting to retire Pool Item Actual.  PoolItemId {}", lPoolItemToRetire.getPoolItemId());
			}
			
			try {
				mPool.retirePoolItem(0, lPoolItemToRetire.getPoolItemId());
			} catch (PoolException e) {
				mTaskSuccessful = false;
				sLogger.error("PoolException occurred when attempting to retire Pool Item from Pool.  Level {}  PoolItemId {}", 0, lPoolItemToRetire.getPoolItemId());
			}
		}
		
		mTaskFinished = true;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}


	@Override
	public IPool getPool() {
		return mPool;
	}
}
