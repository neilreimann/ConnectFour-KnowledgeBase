package com.home.neil.knowledgebase.pool.thread;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.PoolException;
import com.home.neil.thread.PersistentAppThread;

public abstract class PoolItemRetiringThread extends PersistentAppThread {
	public static final String CLASS_NAME = PoolItemRetiringThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private IPool mPool = null;
	
	private int mMaxThrottleCount = 5;
	private long mThrottleWaitMs = 1000;

	private int mThrottleCount = 0;
	
	public PoolItemRetiringThread(IPool pPool, String pLogContext, int pMaxThrottleCount, int pThrottleWaitMs) {
		super(pLogContext, false, true);
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		mPool = pPool;
		mMaxThrottleCount = pMaxThrottleCount;
		mThrottleWaitMs = pThrottleWaitMs;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}


	@SuppressWarnings("squid:S3776")	//ignoring complexity rule only
	protected void executeThreadTask() {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Executing Lookup of PoolItems to Retire");
		}
		
		List<IPoolItem> lPoolItems = null;
		try {
			lPoolItems = mPool.getRetiringPoolItems();
		} catch (PoolException e1) {
			sLogger.error("Error obtaining PoolItems to retire!");
		}

		if (lPoolItems == null || lPoolItems.isEmpty()) {
			if (mThrottleCount < mMaxThrottleCount) {
				mThrottleCount++;
			}
			try {
				Thread.sleep(mThrottleWaitMs * mThrottleCount);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} else {
			for (Iterator<IPoolItem> lIterator = lPoolItems.iterator() ;  lIterator.hasNext();) {
				IPoolItem lPoolItem = lIterator.next();

				try {
					lPoolItem.retire();
				} catch (KnowledgeBaseException e) {
					sLogger.error("Error retiring PoolItem Object!");
				}
				
				try {
					mPool.retirePoolItem(lPoolItem.getPoolItemId());
				} catch (PoolException e) {
					sLogger.error("Error retiring PoolItem Object from Pool!");
				}
			}
			if (mThrottleCount > 0) {
				try {
					Thread.sleep(mThrottleWaitMs * mThrottleCount);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				mThrottleCount--;
			}
		}
		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("Compression is complete");
		}
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
}
