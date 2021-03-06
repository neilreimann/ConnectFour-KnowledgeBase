package com.home.neil.knowledgebase.cachesegment.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.home.neil.knowledgebase.cachesegment.CacheSegmentStateException;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegment;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentConfig;
import com.home.neil.knowledgebase.cachesegment.CompressableCacheSegmentFactory;

class CompressableCacheSegmentTest extends SandboxTest {
	public static final String CLASS_NAME = CompressableCacheSegmentTest.class.getName();
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

	public CompressableCacheSegmentConfig component_readConfig (String lTestMethod) {
		
		sLogger.info("#################################### Start Read Config" + lTestMethod + " ####################################");
		
		setTestPropertiesFileLocation(CLASS_NAME, lTestMethod);
		
		CompressableCacheSegmentConfig lCompressableCacheSegmentConfig = null;
		
		try {
			lCompressableCacheSegmentConfig = AppConfig.bind(CompressableCacheSegmentConfig.class);
		} catch (NumberFormatException | NoSuchElementException | URISyntaxException | ConfigurationException | IOException e) {
			sLogger.info("Unable to Instantiate CompressableCacheSegmentConfig");
			assertTrue(false);
		}

		assertNotNull(lCompressableCacheSegmentConfig);
		
		sLogger.info("#################################### End Read Config" + lTestMethod + " ####################################");
		
		return lCompressableCacheSegmentConfig;
	}
	
	
	@Test
	void test() {
		String lCurrentMethodName = new Object(){}.getClass().getEnclosingMethod().getName();
		
		CompressableCacheSegmentConfig lCompressableCacheSegmentConfig = component_readConfig(lCurrentMethodName);
		
		
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
				assertEquals (0, lScore[i]);
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
