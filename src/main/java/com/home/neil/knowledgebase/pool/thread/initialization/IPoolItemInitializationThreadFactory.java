package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public interface IPoolItemInitializationThreadFactory {
	public IPoolItemInitializationThread getInitializationThread(
			IPool pPool, String pPoolItemId, IPoolItemOperationsTask pPoolItemOperationsTask);

	public IPoolItemInitializationThread getInitializationThread(
			IPool pPool, IPoolItem pPoolItem);

}
