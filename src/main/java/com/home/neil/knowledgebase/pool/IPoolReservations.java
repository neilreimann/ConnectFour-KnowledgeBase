package com.home.neil.knowledgebase.pool;

public interface IPoolReservations {
	public void addReservation (IPoolReservation pPoolReservation);

	public IPoolReservation popReservation (IPoolItem pPoolItem);
}
