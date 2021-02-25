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
	
	public IPoolItem mReservedPoolItem = null;
	public String mPoolItemId = null;
	public IPool mPool = null;
	
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
		notifyAll();
	}

	@Override
	protected void executeTask() throws TaskException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		try {
			mReservedPoolItem = mPool.reservePoolItem(mPoolItemId, this);
		} catch (PoolException e1) {
			sLogger.error("Could not reserve the PoolItem!");
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return;
		}
		
		if (mReservedPoolItem == null) {
			try {
				wait(120000);
			} catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                sLogger.error("Thread interrupted", e); 
			}
		}
		
		if (mReservedPoolItem == null) {
			sLogger.error("Could not reserve the PoolItem!");
			mTaskSuccessful = false;
			mTaskFinished = true;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return;
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
			sLogger.error("Could not reserve the PoolItem!");
			mTaskSuccessful = false;
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			return;
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
