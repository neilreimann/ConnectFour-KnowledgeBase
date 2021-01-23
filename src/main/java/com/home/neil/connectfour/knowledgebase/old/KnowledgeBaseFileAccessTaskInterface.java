package com.home.neil.connectfour.knowledgebase.old;


public interface KnowledgeBaseFileAccessTaskInterface {
	
	public boolean isTransactionSuccessful();

	public KnowledgeBaseFile getKnowledgeBaseFileInUse();

	public void setKnowledgeBaseFileInUse(KnowledgeBaseFile pKnowledgeBaseFileInUse);

	public void interrupt();
	
	public String getWaitingThreadName ();
	
	public String getTaskName (); 
}

