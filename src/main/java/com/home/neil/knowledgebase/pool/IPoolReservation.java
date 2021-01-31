package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public interface IPoolReservation {
	public IPoolItem getPoolItem();
	public IPoolItemTask getTask();
}
