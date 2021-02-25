package com.home.neil.knowledgebase.cachesegment.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegment;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentConfig;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentFactory;

class CompressableCacheSegmentJUnit {
	public static final String CLASS_NAME = CompressableCacheSegmentJUnit.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {

	}

	@AfterEach
	void tearDown() throws Exception {
	}

	public void deleteFolderContents(File pFolder) {
		sLogger.info("Folder to clear: {}", pFolder.getAbsolutePath());
		assertTrue(pFolder.getAbsolutePath().startsWith("C:\\Personal\\TestingZone\\"));

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
//	            } else if (lFile.isDirectory()) {
//	            	deleteFolderContents (lFile);
				}
			}
		}

	}

	private void setTestPropertiesFileLocation(String pMethod) {
		String lPropertiesFile = CLASS_NAME.replaceAll("\\.", "/") + "/" + pMethod + ".properties";

		sLogger.info(lPropertiesFile);

		ClassLoader lClassLoader = this.getClass().getClassLoader();

		URL lURL = lClassLoader.getResource(lPropertiesFile);

		System.setProperty("conf.properties.location", lURL.toString());

	}

	private void clearTestFiles() {
		CompressableCacheSegmentConfig lCompressableCacheSegmentConfig = null;

		try {
			lCompressableCacheSegmentConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException
				| IOException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegmentConfig");
			assertTrue(false);
		}

		deleteFolderContents(new File(lCompressableCacheSegmentConfig.getCompressedFileBasePath()));
		deleteFolderContents(new File(lCompressableCacheSegmentConfig.getUncompressedFileBasePath()));
	}

	@Test
	void test() {
		String lCurrentMethodName = new Object(){}.getClass().getEnclosingMethod().getName();
		
		sLogger.info("#################################### Start " + lCurrentMethodName + " ####################################");
		
		setTestPropertiesFileLocation(lCurrentMethodName);
		
		clearTestFiles();

		

		CompressableCacheSegmentConfig lCompressableCacheSegmentConfig = null;
		
		try {
			lCompressableCacheSegmentConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException | IOException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegmentConfig");
			assertTrue(false);
		}

		sLogger.info("#################################### Part 1 " + lCurrentMethodName + " ####################################");

		//First open empty file then retire
		CompressableCacheSegment lCompressableCacheSegment = null;
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testfile");
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}

		sLogger.info("#################################### Part 2 " + lCurrentMethodName + " ####################################");

		//Reopen closed file
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testfile");
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			byte [] lScore = lCompressableCacheSegment.readScore(0,  lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize());
			for (int i = 0 ; i < lScore.length; i++) {
				assertEquals (lScore[i], 0);
			}
			
			byte [] lScoreToWrite = new byte [] {Byte.MIN_VALUE};
			for (int i = 0; i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToWrite[0] == Byte.MAX_VALUE) {
					lScoreToWrite[0] = Byte.MIN_VALUE;
				} else {
					lScoreToWrite[0] += 1;
				}
				lCompressableCacheSegment.writeScore(i, lScoreToWrite, 1);
			}
			
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 3 " + lCurrentMethodName + " ####################################");
		//Reopen closed file
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testfile");
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			byte [] lScoreToCompare = new byte [] {Byte.MIN_VALUE};
			for (int i = 0 ;  i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToCompare[0] == Byte.MAX_VALUE) {
					lScoreToCompare[0] = Byte.MIN_VALUE;
				} else {
					lScoreToCompare[0] += 1;
				}
				byte [] lScore = lCompressableCacheSegment.readScore(i, 1);
				assertEquals (lScore[0], lScoreToCompare[0]);
			}
			
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		
	}

	
	@Test
	void testStates() {
		String lCurrentMethodName = new Object(){}.getClass().getEnclosingMethod().getName();
		
		sLogger.info("#################################### Start " + lCurrentMethodName + " ####################################");
		
		setTestPropertiesFileLocation(lCurrentMethodName);
		
		clearTestFiles();

		

		CompressableCacheSegmentConfig lCompressableCacheSegmentConfig = null;
		
		try {
			lCompressableCacheSegmentConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException | IOException e) {
			assertTrue(false);
		}

		sLogger.info("#################################### Part 1 " + lCurrentMethodName + " ####################################");

		//First open empty file then retire
		CompressableCacheSegment lCompressableCacheSegment = null;
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testStatesfile");
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		try {
			byte [] lScoreToWrite = new byte [] {Byte.MAX_VALUE};
			for (int i = 0; i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToWrite[0] == Byte.MIN_VALUE) {
					lScoreToWrite[0] = Byte.MAX_VALUE;
				} else {
					lScoreToWrite[0] -= 1;
				}
				lCompressableCacheSegment.writeScore(i, lScoreToWrite, 1);
			}
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());
		
		sLogger.info("#################################### Part 2 - Uncompressed Memory to Uncompressed File " + lCurrentMethodName + " ####################################");

		// In Uncompressed Memory State - Try some bad state changes
		try {
			lCompressableCacheSegment.saveCompressedMemoryToCompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.saveUncompressedFileToCompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to UncompressedFileState
		try {
			lCompressableCacheSegment.saveUncompressedMemoryToUncompressedFile();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}
		
		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());

		sLogger.info("#################################### Part 3 - Uncompressed File to Compressed Memory " + lCurrentMethodName + " ####################################");

		// In Uncompressed File State - Try some bad state changes
		try {
			lCompressableCacheSegment.saveCompressedMemoryToCompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.saveUncompressedMemoryToUncompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to CompressedMemoryState
		try {
			lCompressableCacheSegment.saveUncompressedFileToCompressedMemory();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());
		
		sLogger.info("#################################### Part 4 - Compressed Memory to Compressed File " + lCurrentMethodName + " ####################################");

		// In Compressed Memory State - Try some bad state changes
		try {
			lCompressableCacheSegment.saveUncompressedFileToCompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.saveUncompressedMemoryToUncompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to Compressed File State
		try {
			lCompressableCacheSegment.saveCompressedMemoryToCompressedFile();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());

		sLogger.info("#################################### Part 5 - Compressed File to Compressed Memory " + lCurrentMethodName + " ####################################");

		// In Compressed Memory State - Try some bad state changes
		try {
			lCompressableCacheSegment.loadCompressedMemoryToUncompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.loadUncompressedFileToUncompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to Compressed Memory State
		try {
			lCompressableCacheSegment.loadCompressedFileToCompressedMemory();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());

		sLogger.info("#################################### Part 6 - Compressed Memory to Uncompressed File " + lCurrentMethodName + " ####################################");

		// In Compressed Memory State - Try some bad state changes
		try {
			lCompressableCacheSegment.loadCompressedFileToCompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.loadUncompressedFileToUncompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to Compressed Memory State
		try {
			lCompressableCacheSegment.loadCompressedMemoryToUncompressedFile();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());
		
		sLogger.info("#################################### Part 7 - Uncompressed File to Uncompressed Memory " + lCurrentMethodName + " ####################################");

		// In Compressed Memory State - Try some bad state changes
		try {
			lCompressableCacheSegment.loadCompressedFileToCompressedMemory();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		try {
			lCompressableCacheSegment.loadCompressedMemoryToUncompressedFile();
			assertTrue(false);
		} catch (CacheSegmentStateException e) {
		}
		
		// Now try to bring to Compressed Memory State
		try {
			lCompressableCacheSegment.loadUncompressedFileToUncompressedMemory();
		} catch (CacheSegmentStateException e) {
			assertTrue(false);
		}

		assertTrue(lCompressableCacheSegment.validateStateUsageMemoryUsage());

		sLogger.info("#################################### Part 8 - Validate Contents " + lCurrentMethodName + " ####################################");
		
		try {
			byte [] lScoreToCompare = new byte [] {Byte.MAX_VALUE};
			for (int i = 0 ;  i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToCompare[0] == Byte.MIN_VALUE) {
					lScoreToCompare[0] = Byte.MAX_VALUE;
				} else {
					lScoreToCompare[0] -= 1;
				}
				byte [] lScore = lCompressableCacheSegment.readScore(i, 1);
				assertEquals (lScore[0], lScoreToCompare[0]);
			}
			
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 9 - Retiring " + lCurrentMethodName + " ####################################");
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}

		sLogger.info("#################################### Part 10 - Reopening Closed File " + lCurrentMethodName + " ####################################");

		//Reopen closed file
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testStatesfile");
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 11 - Validate Contents " + lCurrentMethodName + " ####################################");
		
		try {
			byte [] lScoreToCompare = new byte [] {Byte.MAX_VALUE};
			for (int i = 0 ;  i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToCompare[0] == Byte.MIN_VALUE) {
					lScoreToCompare[0] = Byte.MAX_VALUE;
				} else {
					lScoreToCompare[0] -= 1;
				}
				byte [] lScore = lCompressableCacheSegment.readScore(i, 1);
				assertEquals (lScore[0], lScoreToCompare[0]);
			}
			
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 12 - Retiring " + lCurrentMethodName + " ####################################");
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 13 - Reopening Closed File " + lCurrentMethodName + " ####################################");

		//Reopen closed file
		try {
			lCompressableCacheSegment = CompressableCacheSegmentFactory.getCompressableCacheSegment(new String [] {}, "testStatesfile");
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		try {
			lCompressableCacheSegment.init();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 14 - Validate Contents " + lCurrentMethodName + " ####################################");
		
		try {
			byte [] lScoreToCompare = new byte [] {Byte.MAX_VALUE};
			for (int i = 0 ;  i < lCompressableCacheSegmentConfig.getCacheSegmentUncompressedSize(); i++) {
				if (lScoreToCompare[0] == Byte.MIN_VALUE) {
					lScoreToCompare[0] = Byte.MAX_VALUE;
				} else {
					lScoreToCompare[0] -= 1;
				}
				byte [] lScore = lCompressableCacheSegment.readScore(i, 1);
				assertEquals (lScore[0], lScoreToCompare[0]);
			}
			
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegment");
			assertTrue(false);
		}
		
		sLogger.info("#################################### Part 15 - Retiring " + lCurrentMethodName + " ####################################");
		
		try {
			lCompressableCacheSegment.retire();
		} catch (CacheSegmentStateException e) {
			sLogger.info("Unable to Initialize CompressableCacheSegment");
			assertTrue(false);
		}
		
		
	}

	
	
	
}
