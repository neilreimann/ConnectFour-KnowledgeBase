package com.home.neil.knowledgebase.pool;

import java.util.List;

import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public interface IPool {
	
	public IPoolItem reservePoolItem (String pPoolItemId, IPoolItemTask pTask) throws PoolException;

	public void releasePoolItem (IPoolItem pPoolItem) throws PoolException;
	
	public void retirePoolItem (String pPoolItemId) throws PoolException;

	public void terminatePoolItem (String pPoolItemId) throws PoolException;

	public List<IPoolItem> getRetiringPoolItems() throws PoolException;

	public List<IPoolItem> getTerminatingPoolItems() throws PoolException;
}
