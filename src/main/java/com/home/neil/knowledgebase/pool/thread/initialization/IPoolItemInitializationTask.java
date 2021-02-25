package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.pool.task.IPoolItemTask;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public interface IPoolItemInitializationTask extends IPoolItemTask {
	
	public String getPoolItemId ();
	
	public IPoolItemOperationsTask getPoolItemOperationsTask ();
}
