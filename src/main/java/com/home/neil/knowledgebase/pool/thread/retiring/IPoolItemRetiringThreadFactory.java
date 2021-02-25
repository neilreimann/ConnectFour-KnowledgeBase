package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.pool.IPool;

public interface IPoolItemRetiringThreadFactory {
	public IPoolItemRetiringThread createPoolItemRetiringThread (IPool pPool);
}
