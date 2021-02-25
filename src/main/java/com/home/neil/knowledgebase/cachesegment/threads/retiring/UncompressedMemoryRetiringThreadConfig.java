package com.home.neil.knowledgebase.cachesegment.threads.retiring;

import com.home.neil.appconfig.DefaultConfigValue;

public interface UncompressedMemoryRetiringThreadConfig {
	
	@DefaultConfigValue (Value = "5")
	public int getMaxThrottleCount ();
	
	@DefaultConfigValue (Value = "1000")
	public int getThrottleValue ();
	
	@DefaultConfigValue (Value = "CacheSegment")
	public String getLogContext ();
	
}
