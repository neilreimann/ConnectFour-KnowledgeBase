package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.task.ITask;

public interface IPoolReservations {
	public void addReservation (IPoolReservation pPoolReservation);

	public IPoolReservation popReservation (IPoolItem pPoolItem);
}
