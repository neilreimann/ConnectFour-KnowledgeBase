package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.pool.thread.IPoolThread;
import com.home.neil.thread.ITerminateAppThreadMBean;

public interface IPoolItemRetiringThread extends IPoolThread, ITerminateAppThreadMBean {
	IPoolItemRetiringTask getPoolItemRetiringTask();
}
