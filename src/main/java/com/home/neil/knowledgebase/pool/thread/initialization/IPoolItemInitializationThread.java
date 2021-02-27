package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.IPoolThread;

public interface IPoolItemInitializationThread extends IPoolThread {
	public IPoolItemInitializationTask getPoolItemInitializationTask();
	
	public IPoolItem getPoolItem ();
}
