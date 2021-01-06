package com.home.neil.connectfour.knowledgebase.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBaseException extends Exception {
	private static final long serialVersionUID = -5917963768122024428L;
	public static final String CLASS_NAME = KnowledgeBaseException.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0,
			CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public KnowledgeBaseException () {
		super ("Knowledge Base Exception occurred.");
		
		StringWriter lSW = new StringWriter();
		PrintWriter lPW = new PrintWriter(lSW);
		this.printStackTrace(lPW);
		lSW.toString(); // stack trace as a string

		sLogger.error("StackTrace: " + lSW);
		
		sLogger.trace("Entering");
		sLogger.info("KnowledgeBaseException");
		sLogger.trace("Exiting");
	}
}
