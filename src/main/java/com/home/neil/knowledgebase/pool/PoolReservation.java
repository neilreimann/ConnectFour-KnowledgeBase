package com.home.neil.knowledgebase.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public class PoolReservation implements IPoolReservation {
	public static final String CLASS_NAME = PoolReservation.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private IPoolItem mPoolItem = null;
	private IPoolItemOperationsTask mTask = null;
	
	public PoolReservation (IPoolItem pPoolItem, IPoolItemOperationsTask pTask) {
		mPoolItem = pPoolItem;
		mTask = pTask;
	}
	
	public IPoolItem getPoolItem() {
		return mPoolItem;
	}

	public IPoolItemOperationsTask getTask() {
		return mTask;
	}

	
	
}
