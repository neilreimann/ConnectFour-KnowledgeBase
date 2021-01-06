package com.home.neil.connectfour.knowledgebase.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordNotFoundException extends Exception {
	private static final long serialVersionUID = 5138763265858636257L;
	public static final String CLASS_NAME = RecordNotFoundException.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0,
			CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	public RecordNotFoundException () {
		super ("Record Not Found in Knowledge Base");
		sLogger.trace("Entering");
		sLogger.info("RecordNotFoundException");
		sLogger.trace("Exiting");
	}
}
