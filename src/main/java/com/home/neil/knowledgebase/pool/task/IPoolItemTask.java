package com.home.neil.knowledgebase.pool.task;

import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.task.ITask;

public interface IPoolItemTask extends ITask {
	public IPoolItem getPoolItem();
	
	public void setReservedPoolItem (IPoolItem pPoolItem);

}
