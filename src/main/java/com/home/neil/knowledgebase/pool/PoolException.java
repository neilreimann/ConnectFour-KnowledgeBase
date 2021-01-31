package com.home.neil.knowledgebase.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.KnowledgeBaseException;

public class PoolException extends KnowledgeBaseException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2689689949491083870L;
	public static final String CLASS_NAME = PoolException.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0,
			CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public PoolException () {
		super ("CacheSegmentStateException occurred.");
	}
	
	public PoolException (Exception pE) {
		super ("CacheSegmentStateException occurred.  Underlying "+ pE.getClass().getName(), pE);
	}
	
	public PoolException (String pMessage, Exception pE) {
		super (pMessage, pE);
	}
}
