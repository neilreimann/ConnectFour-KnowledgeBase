package com.home.neil.knowledgebase.cachesegment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.KnowledgeBaseException;

public class CacheSegmentStateException extends KnowledgeBaseException {
	private static final long serialVersionUID = 3925973193438306614L;
	public static final String CLASS_NAME = CacheSegmentStateException.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0,
			CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public CacheSegmentStateException () {
		super ("CacheSegmentStateException occurred.");
	}
	
	public CacheSegmentStateException (Exception pE) {
		super ("CacheSegmentStateException occurred.  Underlying "+ pE.getClass().getName(), pE);
	}
	
	public CacheSegmentStateException (String pMessage) {
		super (pMessage);
	}

	public CacheSegmentStateException (String pMessage, Exception pE) {
		super (pMessage, pE);
	}
	
	

}
