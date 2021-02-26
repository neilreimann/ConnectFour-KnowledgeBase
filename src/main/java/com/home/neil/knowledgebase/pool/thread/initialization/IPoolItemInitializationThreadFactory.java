package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.pool.IPool;

public interface IPoolItemInitializationThreadFactory {
	public IPoolItemInitializationThread getCompressableCacheSegmentInitializationThread(
			IPool pPool, String pPoolItemId, IPoolItemInitializationTask pPoolItemOperationsTask, String pLogContext);
}
