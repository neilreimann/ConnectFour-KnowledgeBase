package com.home.neil.knowledgebase.cachesegment;

import com.home.neil.appconfig.DefaultConfigValue;

public interface CompressableCacheSegmentConfig {

	public String getCompressedFileBasePath();
	
	public String getCompressedFileExtension();

	public String getUncompressedFileBasePath();
	
	public String getUncompressedFileExtension();
	
	@DefaultConfigValue (value = "debug")
	public String getUncompressedFileDebugExtension();

	public int getCacheSegmentUncompressedSize();

	@DefaultConfigValue (value = "false")
	public boolean getDebug();

	@DefaultConfigValue (value = "false")
	public boolean getThreadSafe();
}
