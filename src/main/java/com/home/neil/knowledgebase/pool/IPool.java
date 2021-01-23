package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public interface IPool {
	
	public void init();
	
	public void retire ();
	
	public IPoolItem reservePoolItem (String pPoolItemId, IPoolItemTask pTask);

	public void releasePoolItem (IPoolItem pPoolItem);
	
}
