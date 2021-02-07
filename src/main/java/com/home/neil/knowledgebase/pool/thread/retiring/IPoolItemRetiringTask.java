package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.task.IPoolTask;

public interface IPoolItemRetiringTask extends IPoolTask {
	public IPoolItem retirePoolItem (IPoolItem pPoolItem) throws KnowledgeBaseException;
}
