package deprecated.com.home.neil.connectfour.knowledgebase.old;

public interface KnowledgeBaseFilePoolMBean {
	public String getCurrentUnReservedKnowledgeBaseFilesSize();
	public String getCurrentLastAccessedUnReservedKnowledgeBaseFilesSize();
	public String getCurrentReservedKnowledgeBaseFilesSize();
	public String getCurrentReservationsSize();
	public String getCurrentKnowledgeBaseUncompressedFiles();
}
