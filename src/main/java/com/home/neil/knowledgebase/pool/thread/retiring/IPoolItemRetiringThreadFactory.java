package com.home.neil.knowledgebase.pool.thread.retiring;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;

public interface IPoolItemRetiringThreadFactory {
	public IPoolItemInitializationThread getCompressableCacheSegmentRetiringThread(
			IPool pPool, int pSubPoolLevel, String pLogContext);
}
