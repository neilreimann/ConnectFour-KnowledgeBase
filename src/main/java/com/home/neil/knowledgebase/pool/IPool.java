package com.home.neil.knowledgebase.pool;

import java.util.List;

import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;

public interface IPool {
	
	public IPoolItem reservePoolItem (String pPoolItemId, IPoolItemOperationsTask pTask) throws PoolException;

	public void releasePoolItem (IPoolItem pPoolItem) throws PoolException;
	
	public void retirePoolItemFromPoolCallback (int pTargetSubPoolLevel, String pPoolItemId) throws PoolException;
	
	public void initPoolItemThreadCallback (IPoolItem pPoolItem);
	
	public List<IPoolItem> getRetiringPoolItems(int pTargetSubPoolLevel) throws PoolException;
}
