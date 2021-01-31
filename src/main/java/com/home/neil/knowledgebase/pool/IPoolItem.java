package com.home.neil.knowledgebase.pool;

import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.index.IIndexEntry;

public interface IPoolItem extends IIndexEntry, IKnowledgeBaseObject {
	
	public String getPoolItemId();
}
