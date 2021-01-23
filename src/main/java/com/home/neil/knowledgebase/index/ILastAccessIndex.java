package com.home.neil.knowledgebase.index;

public interface ILastAccessIndex {
	
	public void addLastAccessedIndexEntry (IIndexEntry pIndexEntry);

	public void removeLastAccessedIndexEntry (IIndexEntry pIndexEntry);

	public IIndexEntry popLastAccessedIndexEntry ();

	public void resetLastAccessedIndexEntry (IIndexEntry pIndexEntry);
	
	public int getLastAccessedIndexEntrySize ();

	public IIndexEntry peekLastAccessedIndexEntry ();
	
}
