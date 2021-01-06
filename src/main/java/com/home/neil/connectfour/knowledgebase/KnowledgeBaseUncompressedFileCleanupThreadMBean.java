package com.home.neil.connectfour.knowledgebase;

import java.io.IOException;

import org.apache.commons.configuration2.ex.ConfigurationException;

public interface KnowledgeBaseUncompressedFileCleanupThreadMBean {
		public void setTerminate ();
		public boolean getTerminate();
		public void setThrottle (long pThrottleValue);
		public long getThrottle ();
		public void togglePause();
		public boolean getPause();
}
