package com.home.neil.knowledgebase.pool;

import com.home.neil.appconfig.DefaultConfigValue;

public interface IPoolConfig {

	public int getActiveHighWaterMark ();
	public int getActiveLowWaterMark();

	public String getActiveInitializationThreadClassFactory ();

	public String getActiveRetirementThreadClassFactory ();

	@DefaultConfigValue (Value = "2")
	public int getActiveRetirementThreadCount();

	@DefaultConfigValue (Value = "0")
	public int getSubPoolLevels();
	
	@DefaultConfigValue (Value = "10")
	public int getSubPoolHighWaterMark (int pSubPoolLevel);
	
	@DefaultConfigValue (Value = "8")
	public int getSubPoolLowWaterMark (int pSubPoolLevel);

	public String getSubPoolResurrectionThreadClassFactory (int pSubPoolLevel);
	public String getSubPoolRetirementThreadClassFactory (int pSubPoolLevel);

	@DefaultConfigValue (Value = "2")
	public int getSubPoolRetirementThreadCount(int pSubPoolLevel);

	public String getDrainResurrectionThreadClassFactory ();
	public String getDrainRetirementThreadClassFactory ();

	@DefaultConfigValue (Value = "2")
	public int getDrainRetirementThreadCount();
	
	@DefaultConfigValue (Value = "false")
	public boolean getDebug();
	

}
