package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.pool.thread.IPoolThread;

public interface IPoolItemRetiringThread extends IPoolThread {
	IPoolItemRetiringTask getPoolItemRetiringTask();
}
