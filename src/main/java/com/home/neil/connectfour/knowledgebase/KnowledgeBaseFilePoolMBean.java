package com.home.neil.connectfour.knowledgebase;

public interface KnowledgeBaseFilePoolMBean {
	public String getCurrentUnReservedKnowledgeBaseFilesSize();
	public String getCurrentLastAccessedUnReservedKnowledgeBaseFilesSize();
	public String getCurrentReservedKnowledgeBaseFilesSize();
	public String getCurrentReservationsSize();
	public String getCurrentKnowledgeBaseUncompressedFiles();
}
