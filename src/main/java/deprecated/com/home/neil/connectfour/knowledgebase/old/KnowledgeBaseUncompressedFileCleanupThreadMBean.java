package deprecated.com.home.neil.connectfour.knowledgebase.old;

public interface KnowledgeBaseUncompressedFileCleanupThreadMBean {
		public void setTerminate ();
		public boolean getTerminate();
		public void setThrottle (long pThrottleValue);
		public long getThrottle ();
		public void togglePause();
		public boolean getPause();
}
