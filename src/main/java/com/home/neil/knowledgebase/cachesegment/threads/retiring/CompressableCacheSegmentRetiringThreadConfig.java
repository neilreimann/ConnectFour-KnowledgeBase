package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import com.home.neil.appconfig.DefaultConfigValue;

public interface CompressableCacheSegmentRetiringThreadConfig {
	
	@DefaultConfigValue (Value = "5")
	public int getMaxThrottleCount (int SubPoolLevel);
	
	@DefaultConfigValue (Value = "1000")
	public int getThrottleValue (int SubPoolLevel);
	
}
