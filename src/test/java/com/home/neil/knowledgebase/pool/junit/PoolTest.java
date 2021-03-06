package com.home.neil.knowledgebase.pool.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.home.neil.appconfig.AppConfig;
import com.home.neil.junit.sandbox.SandboxTest;
import com.home.neil.knowledgebase.pool.IPoolConfig;
import com.home.neil.knowledgebase.pool.Pool;
import com.home.neil.knowledgebase.pool.PoolException;

class PoolTest extends SandboxTest{
	public static final String CLASS_NAME = PoolTest.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	
	
	@BeforeAll
	protected static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	protected static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	protected void setUp() throws Exception {
		super.setUp();
	}

	@AfterEach
	protected void tearDown() throws Exception {
	}

	public IPoolConfig component_readConfig (String pCurrentMethodName) {
		
		sLogger.info("#################################### Start Read Config" + pCurrentMethodName + " ####################################");
		
		setTestPropertiesFileLocation(CLASS_NAME, pCurrentMethodName);
		
		IPoolConfig lPoolConfig = null;
		
		try {
			lPoolConfig = AppConfig.bind(IPoolConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException | IOException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegmentConfig");
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(lPoolConfig);
		
		sLogger.info("#################################### End Read Config" + pCurrentMethodName + " ####################################");
		
		return lPoolConfig;
	}
	
	public Pool component_instantiatePool (String pCurrentMethodName, IPoolConfig pConfig) {
		
		sLogger.info("#################################### Start Instantiate Pool" + pCurrentMethodName + " ####################################");
	
		Pool lPool = null;
		try {
			lPool = new Pool(pConfig);
		} catch (PoolException e) {
			sLogger.info("Unable to Instantiate Pool");
			e.printStackTrace();
			assertTrue(false);
		}
		
		sLogger.info("####################################   End Instantiate Pool" + pCurrentMethodName + " ####################################");
		return lPool;
	}

	public void component_initPool (String pCurrentMethodName, Pool pPool) {
		
		sLogger.info("#################################### Start Init Pool" + pCurrentMethodName + " ####################################");
	
		try {
			pPool.init();
		} catch (PoolException e) {
			sLogger.info("Unable to Init Pool");
			e.printStackTrace();
			assertTrue(false);
		}
		
		sLogger.info("####################################   End Init Pool" + pCurrentMethodName + " ####################################");
	}
	

	public void component_retirePool (String pCurrentMethodName, Pool pPool) {
		
		sLogger.info("#################################### Start Retire Pool" + pCurrentMethodName + " ####################################");
	
		try {
			pPool.retire();
		} catch (PoolException e) {
			sLogger.info("Unable to Retire Pool");
			e.printStackTrace();
			assertTrue(false);
		}
		
		sLogger.info("####################################   End Retire Pool" + pCurrentMethodName + " ####################################");
	}
	

	
	
	@Test
	void testPoolConfig() {
		String lCurrentMethodName = new Object(){}.getClass().getEnclosingMethod().getName();
		
		IPoolConfig lPoolConfig = component_readConfig(lCurrentMethodName);
		
		Pool lPool = component_instantiatePool(lCurrentMethodName, lPoolConfig);
		
		component_initPool(lCurrentMethodName, lPool);
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		
		
		component_retirePool(lCurrentMethodName, lPool);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		
	}

	
	
}
