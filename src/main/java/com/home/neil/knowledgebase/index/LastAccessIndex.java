package com.home.neil.knowledgebase.index;

import java.util.Comparator;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LastAccessIndex implements ILastAccessIndex {
	public static final String CLASS_NAME = LastAccessIndex.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public class IIndexEntryComparator implements Comparator <IIndexEntry> {
		public int compare (IIndexEntry pIndexEntry1, IIndexEntry pIndexEntry2) {
			return Long.compare(pIndexEntry1.getIndexEntry(), pIndexEntry2.getIndexEntry());
		}
	}
	
	private final Object mLastAccessIndexLock = new Object();
	
	private TreeSet <IIndexEntry> mIndexEntries = new TreeSet <IIndexEntry> (new IIndexEntryComparator());

	public void addLastAccessedIndexEntry(IIndexEntry pIndexEntry) {
		synchronized(mLastAccessIndexLock) {
			pIndexEntry.setIndexEntry();
			mIndexEntries.add(pIndexEntry);
		}
	}

	public void removeLastAccessedIndexEntry(IIndexEntry pIndexEntry) {
		synchronized(mLastAccessIndexLock) {
			mIndexEntries.remove(pIndexEntry);
		}
	}

	public IIndexEntry popLastAccessedIndexEntry() {
		synchronized(mLastAccessIndexLock) {
			return mIndexEntries.pollFirst();
		}
	}

	public void resetLastAccessedIndexEntry(IIndexEntry pIndexEntry) {
		synchronized(mLastAccessIndexLock) {
			mIndexEntries.remove(pIndexEntry);
			pIndexEntry.setIndexEntry();
			mIndexEntries.add(pIndexEntry);
		}
	}

	public int getLastAccessedIndexEntrySize() {
		synchronized(mLastAccessIndexLock) {
			return mIndexEntries.size();
		}
	}

	public IIndexEntry peekLastAccessedIndexEntry() {
		synchronized(mLastAccessIndexLock) {
			return mIndexEntries.first();
		}
	}

}
