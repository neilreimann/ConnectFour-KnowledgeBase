package com.home.neil.knowledgebase.cachesegment.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.retiring.PoolItemRetiringThread;
import com.home.neil.task.BasicAppTask;
import com.home.neil.task.TaskException;

public class MemoryCacheSegmentRetiringThread extends PoolItemRetiringThread {

	public static final String CLASS_NAME = MemoryCacheSegmentRetiringThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public MemoryCacheSegmentRetiringThread(IPool pPool, String pLogContext, int pMaxThrottleCount,
			int pThrottleValue) {
		super(pPool, pLogContext, pMaxThrottleCount, pThrottleValue);

	}

	@Override
	protected BasicAppTask createAppTask() throws TaskException {
		return new MemoryCacheSegmentRetiringTask (mPool, this,	mLogContext, true);
	}
}
