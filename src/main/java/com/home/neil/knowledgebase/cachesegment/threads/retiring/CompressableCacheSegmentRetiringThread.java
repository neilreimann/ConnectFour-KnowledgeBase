package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringTask;
import com.home.neil.knowledgebase.pool.thread.retiring.PoolItemRetiringThread;
import com.home.neil.task.BasicAppTask;

public class CompressableCacheSegmentRetiringThread extends PoolItemRetiringThread  {

	public static final String CLASS_NAME = CompressableCacheSegmentRetiringThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public CompressableCacheSegmentRetiringThread(IPool pPool, int pSubPoolLevel, CompressableCacheSegmentRetiringThreadConfig pConfig, String pLogContext) {
		super(pPool, pSubPoolLevel, pLogContext, pConfig.getMaxThrottleCount(pSubPoolLevel), pConfig.getThrottleValue(pSubPoolLevel));

	}

	@Override
	public BasicAppTask setNewAppTask() {
		mAppTask = new CompressableCacheSegmentRetiringTask(getPool(), getSubPoolLevel(), this,  mLogContext, true);
		return mAppTask;
	}

	@Override
	public IPoolItemRetiringTask getPoolItemRetiringTask() {
		return (IPoolItemRetiringTask) mAppTask;
	}

}
