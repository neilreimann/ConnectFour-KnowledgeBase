package com.home.neil.knowledgebase.pool.thread.initialization;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public interface IPoolItemInitializationTask extends IPoolItemTask {
	
	public IPoolItem initPoolItem (IPoolItem pPoolItem) throws KnowledgeBaseException;

	public IPoolItem initPoolItem (String pPoolItemId) throws KnowledgeBaseException;
	
	public String getPoolItemId ();
}
