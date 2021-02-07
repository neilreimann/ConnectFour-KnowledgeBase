package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.pool.thread.IPoolThread;

public interface IPoolItemInitializationThread extends IPoolThread {
	IPoolItemInitializationTask getPoolItemInitializationTask();
}
