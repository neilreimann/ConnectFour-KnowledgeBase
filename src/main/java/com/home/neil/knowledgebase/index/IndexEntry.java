package com.home.neil.knowledgebase.index;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IndexEntry implements IIndexEntry {
	public static final String CLASS_NAME = IndexEntry.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private long mIndex = 0;
	private static HashMap<String, Long> sIndex = new HashMap<String, Long>();
	
	protected long setIndexEntry(Class pClass) {
		mIndex = getNextIndex (pClass);
		return mIndex;
	}

	public long getIndexEntry() {
		return mIndex;
	}

	private static synchronized long getNextIndex (Class pClass) {
		long lIndex = (sIndex.get(pClass.toString()).longValue());
		lIndex++;
		sIndex.put(pClass.toString(), Long.valueOf(lIndex));
		return lIndex;
	}
}
