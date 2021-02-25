package com.home.neil.knowledgebase.pool.task;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.task.IBasicAppTaskMBean;
import com.home.neil.task.ILogContextAppTask;

public interface IPoolTask extends ILogContextAppTask, IBasicAppTaskMBean {
	public IPool getPool ();
}
