package com.home.neil.knowledgebase.pool.thread.operations;

import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public interface IPoolItemOperationsTask extends IPoolItemTask {
	public void setReservedPoolItem (IPoolItem pReservedPoolItem);
}
