package com.home.neil.knowledgebase.cachesegment.threads.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;
import com.home.neil.knowledgebase.pool.thread.operations.PoolItemOperationsTask;
import com.home.neil.task.TaskException;

public abstract class CompressableCacheSegmentOperationsTask extends PoolItemOperationsTask
		implements ICompressableCacheSegmentOperationsTask, IPoolItemOperationsTask {
	public static final String CLASS_NAME = CompressableCacheSegmentOperationsTask.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private String[] mStatePaths = null;
	private String mFileName = null;

	protected CompressableCacheSegmentOperationsTask(IPool pPool, String pPoolItemId, String[] pStatePaths, String pFileName, String pLogContext) {
		super(pPool, pPoolItemId, pLogContext, true);
		mStatePaths = pStatePaths;
		mFileName = pFileName;
	}

	@Override
	public String[] getStatePaths() {
		return mStatePaths;
	}

	@Override
	public String getFileName() {
		return mFileName;
	}

	@Override
	protected abstract boolean executeOperation() throws TaskException;

}
