package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;

public interface IPoolItemRetiringThreadFactory {
	public IPoolItemRetiringThread getRetiringThread(
			IPool pPool, int pSubPoolLevel) throws KnowledgeBaseException;
}
