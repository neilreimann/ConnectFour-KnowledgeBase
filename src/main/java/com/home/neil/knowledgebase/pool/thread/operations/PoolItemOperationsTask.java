package com.home.neil.knowledgebase.pool.thread.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.PoolException;
import com.home.neil.task.BasicAppTask;
import com.home.neil.task.TaskException;

public abstract class PoolItemOperationsTask extends BasicAppTask implements IPoolItemOperationsTask {
	public static final String CLASS_NAME = PoolItemOperationsTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	protected IPoolItem mReservedPoolItem = null;
	protected String mPoolItemId = null;
	protected IPool mPool = null;
	
	private static final String LOG_COULDNOTRESERVEPOOLITEM = "Could not reserve the PoolItem!";
	
	private Object mLock = new Object();
	
	protected PoolItemOperationsTask(IPool pPool, String pPoolItemId, String pLogContext, boolean pRecordTaskStatistics) {
		super(pLogContext, pRecordTaskStatistics);
		mPoolItemId = pPoolItemId;
		mPool=pPool;
	}

	@Override
	public IPool getPool() {
		return mPool;
	}

	@Override
	public void setReservedPoolItem(IPoolItem pReservedPoolItem) {
		mReservedPoolItem = pReservedPoolItem;
		synchronized (mLock) {
			notifyAll();
		}
	}

	@Override
	protected void executeTask() throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		try {
			mReservedPoolItem = mPool.reservePoolItem(mPoolItemId, this);
		} catch (PoolException e1) {
			sLogger.error(LOG_COULDNOTRESERVEPOOLITEM);
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return;
		}
		
		synchronized (mLock) {
			while (mReservedPoolItem == null) {
				try {
					wait(120000);
				} catch (InterruptedException e) {
	                Thread.currentThread().interrupt(); 
	                sLogger.error("Thread interrupted", e); 
				}
			}
		}
		
		try {
			mTaskSuccessful = executeOperation();
		} catch (TaskException eE) {
			sLogger.error("Task Exception occurred during Operation!");
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return;
		}
		
		try {
			mPool.releasePoolItem(mReservedPoolItem);
		} catch (PoolException e1) {
			sLogger.error(LOG_COULDNOTRESERVEPOOLITEM);
			mTaskSuccessful = false;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
		}
	}
	
	protected abstract boolean executeOperation () throws TaskException;
	
	

	public IPoolItem getPoolItem() {
		return mReservedPoolItem;
	}	
	
	
	public String getPoolItemId() {
		return mPoolItemId;
	}

}
