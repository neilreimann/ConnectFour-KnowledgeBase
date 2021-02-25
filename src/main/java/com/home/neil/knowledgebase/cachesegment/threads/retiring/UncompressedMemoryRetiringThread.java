package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.retiring.PoolItemRetiringThread;
import com.home.neil.task.BasicAppTask;
import com.home.neil.task.TaskException;

public class UncompressedMemoryRetiringThread extends PoolItemRetiringThread {

	public static final String CLASS_NAME = UncompressedMemoryRetiringThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public UncompressedMemoryRetiringThread(IPool pPool, UncompressedMemoryRetiringThreadConfig pConfig) {
		super(pPool, pConfig.getLogContext(), pConfig.getMaxThrottleCount(), pConfig.getThrottleValue());

	}

	
	
	@Override
	protected BasicAppTask createAppTask() throws TaskException {
		return new UncompressedMemoryRetiringTask (mPool, this,	mLogContext, true);
	}
}
