package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;

public interface IPoolItemRetiringThreadFactory {
	public IPoolItemRetiringThread getRetiringThread(
			IPool pPool, int pSubPoolLevel) throws KnowledgeBaseException;
}
