package com.home.neil.junit.sandbox;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


public abstract class SandboxTest {
	public static final String CLASS_NAME = SandboxTest.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	public void deleteFolderContents(File pFolder) {
		sLogger.info("Folder to clear: {}", pFolder.getAbsolutePath());
		assertTrue(pFolder.getAbsolutePath().startsWith("C:\\Personal\\TestingZone"));

		File[] pFiles = pFolder.listFiles();
		if (pFiles != null) { // some JVMs return null for empty dirs
			for (File lFile : pFiles) {
				if (!lFile.isDirectory() && lFile.getName().endsWith("dat")) {
					sLogger.info("Clearing file: {}", lFile.getName());
					lFile.delete();
				} else if (!lFile.isDirectory() && lFile.getName().endsWith("cache")) {
					sLogger.info("Clearing file: {}", lFile.getName());
					lFile.delete();
				} else if (!lFile.isDirectory() && lFile.getName().endsWith("debug")) {
					sLogger.info("Clearing file: {}", lFile.getName());
					lFile.delete();
	            } else if (lFile.isDirectory()) {
	            	deleteFolderContents (lFile);
				}
			}
		}

	}

	protected void setTestPropertiesFileLocation(String pClassName, String pMethod) {
		String lPropertiesFile = pClassName.replaceAll("\\.", "/") + "/" + pMethod + ".properties";

		sLogger.info(lPropertiesFile);

		ClassLoader lClassLoader = this.getClass().getClassLoader();

		URL lURL = lClassLoader.getResource(lPropertiesFile);

		System.setProperty("conf.properties.location", lURL.toString());
	}
	
	@BeforeAll
	protected static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	protected static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	protected void setUp() throws Exception {
		deleteFolderContents (new File("C:\\Personal\\TestingZone"));
	}

	@AfterEach
	protected void tearDown() throws Exception {
	
	}
}
