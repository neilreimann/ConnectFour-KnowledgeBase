package com.home.neil.knowledgebase.pool.thread.operations;

import com.home.neil.knowledgebase.pool.thread.IPoolThread;

public interface IPoolItemOperationsThread extends IPoolThread {
	IPoolItemOperationsTask getPoolItemReservationTask();
}
