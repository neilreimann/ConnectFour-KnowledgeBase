package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public interface IPoolReservation {
	public IPoolItem getPoolItem();
	public IPoolItemOperationsTask getTask();
}
