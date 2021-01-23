package com.home.neil.knowledgebase.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.task.ITask;

public class PoolReservation implements IPoolReservation {
	public static final String CLASS_NAME = PoolReservation.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public IPoolItem mPoolItem = null;
	public ITask mTask = null;
	
	public PoolReservation (IPoolItem pPoolItem, ITask pTask) {
		mPoolItem = pPoolItem;
		mTask = pTask;
	}
	
	public IPoolItem getPoolItem() {
		return mPoolItem;
	}

	public ITask getTask() {
		return mTask;
	}

	
	
}
