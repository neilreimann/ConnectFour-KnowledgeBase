package com.home.neil.knowledgebase.cachesegment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.zip.DataFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.knowledgebase.old.exception.KnowledgeBaseException;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;

public class CacheSegmentStateException extends Exception {
	private static final long serialVersionUID = 3925973193438306614L;
	public static final String CLASS_NAME = KnowledgeBaseException.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0,
			CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public CacheSegmentStateException () {
		super ("CacheSegmentStateException occurred.");
		sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		
		StringWriter lSW = new StringWriter();
		PrintWriter lPW = new PrintWriter(lSW);
		this.printStackTrace(lPW);
		lSW.toString(); // stack trace as a string
		sLogger.error("StackTrace: {} ", lSW);
		
		sLogger.error ("CacheSegmentStateException occurred.");
		sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
	}
	
	public CacheSegmentStateException (DataFormatException pE) {
		super ("CacheSegmentStateException occurred.  Underlying DataFormatException");
		sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		
		StringWriter lSW = new StringWriter();
		PrintWriter lPW = new PrintWriter(lSW);
		pE.printStackTrace(lPW);
		lSW.toString(); // stack trace as a string
		sLogger.error("StackTrace: {}", lSW);
		sLogger.info("CacheSegmentStateException occurred: Underlying DataFormatException: {}", pE.getMessage());
		sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
	}
	
	public CacheSegmentStateException (IOException pE) {
		super ("CacheSegmentStateException occurred.  Underlying IOException");
		sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		
		StringWriter lSW = new StringWriter();
		PrintWriter lPW = new PrintWriter(lSW);
		pE.printStackTrace(lPW);
		lSW.toString(); // stack trace as a string
		sLogger.error("StackTrace: {}", lSW);
		sLogger.info("CacheSegmentStateException occurred: Underlying IOException: {}", pE.getMessage());
		sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
	}
}
