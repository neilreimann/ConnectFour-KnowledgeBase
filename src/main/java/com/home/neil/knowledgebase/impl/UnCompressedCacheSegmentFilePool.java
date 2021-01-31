package com.home.neil.knowledgebase.impl;

import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.Pool;

public class UnCompressedCacheSegmentFilePool extends Pool {

	protected UnCompressedCacheSegmentFilePool(int pHighWaterMark, int pLowWaterMark) {
		super(pHighWaterMark, pLowWaterMark);

		
	
	}

	protected IPoolItem initializePoolItem(String pPoolItemId) {
		// There are two places the Pool Item can be initialized from...
		// 1. We pull it from the CompressedCacheSegmentFilePool
		// 2. We create from scratch
		
		return null;
	}

}
