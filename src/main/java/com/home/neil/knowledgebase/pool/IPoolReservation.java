package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.task.ITask;

public interface IPoolReservation {
	public IPoolItem getPoolItem();
	public ITask getTask();
}
