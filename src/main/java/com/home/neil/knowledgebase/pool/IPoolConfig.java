package com.home.neil.knowledgebase.pool;

import com.home.neil.appconfig.DefaultConfigValue;

public interface IPoolConfig {

	public int getActiveHighWaterMark ();
	public int getActiveLowWaterMark();

	public String getActiveInitializationThreadClassFactory ();

	public String getActiveRetirementThreadClassFactory ();

	@DefaultConfigValue (value = "2")
	public int getActiveRetirementThreadCount();

	@DefaultConfigValue (value = "0")
	public int getSubPoolLevels();
	
	@DefaultConfigValue (value = "10")
	public int getSubPoolHighWaterMark (int pSubPoolLevel);
	
	@DefaultConfigValue (value = "8")
	public int getSubPoolLowWaterMark (int pSubPoolLevel);

	public String getSubPoolResurrectionThreadClassFactory (int pSubPoolLevel);
	public String getSubPoolRetirementThreadClassFactory (int pSubPoolLevel);

	@DefaultConfigValue (value = "2")
	public int getSubPoolRetirementThreadCount(int pSubPoolLevel);

	public String getDrainResurrectionThreadClassFactory ();
	public String getDrainRetirementThreadClassFactory ();

	@DefaultConfigValue (value = "2")
	public int getDrainRetirementThreadCount();
	
	@DefaultConfigValue (value = "false")
	public boolean getDebug();
	

}
