package com.home.neil.knowledgebase.pool.thread;

import com.home.neil.knowledgebase.pool.IPool;

public interface IPoolThread {
	public IPool getPool ();
	
	public void start();
}
