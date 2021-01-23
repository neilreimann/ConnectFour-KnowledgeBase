package com.home.neil.knowledgebase.pool;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.connectfour.managers.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.KnowledgeBaseConstants;
import com.home.neil.knowledgebase.index.ILastAccessIndex;
import com.home.neil.knowledgebase.index.LastAccessIndex;
import com.home.neil.knowledgebase.pool.task.IPoolItemTask;

public abstract class Pool implements IPool {
	public static final String CLASS_NAME = Pool.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);

	private HashMap<String, IPoolItem> mCurrentUnReservedPoolItems = null;
	private HashMap<String, IPoolItem> mCurrentReservedPoolItems = null;
	private HashMap<String, IPoolItem> mCurrentRetiringPoolItems = null;
	
	private IPoolReservations mPoolReservations = null;
	private IPoolReservations mRetiringReservations = null;
	
	private ILastAccessIndex mLastAccessIndex = null;
	
	private int mHighWaterMark = 10;
	private int mLowWaterMark = 8;
	private int mOpenPoolItems = 0;
	
	private int mCleanupThreadCount = 1;
	
	
	
	public Pool (int pHighWaterMark, int pLowWaterMark, int pCleanupThreadCount) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace("Entering");
		}
		mHighWaterMark = pHighWaterMark;
		mLowWaterMark = pLowWaterMark;
		mOpenPoolItems = 0;
		mCleanupThreadCount = pCleanupThreadCount;
	}
	

	public void init() {
		mPoolReservations = new PoolReservations();
		mCurrentUnReservedPoolItems = new HashMap <String, IPoolItem> ();
		mCurrentReservedPoolItems = new HashMap <String, IPoolItem> ();
		
		mCurrentRetiringPoolItems = new HashMap <String, IPoolItem> ();
		mRetiringReservations = new PoolReservations ();
		mLastAccessIndex = new LastAccessIndex ();
		
		mOpenPoolItems = 0;
	}


	public synchronized IPoolItem reservePoolItem(String pPoolItemId, IPoolItemTask pTask) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		//first check if the Pool Item is unreserved
		IPoolItem lPoolItem = mCurrentUnReservedPoolItems.remove(pPoolItemId);
		if (lPoolItem != null) {
			mLastAccessIndex.resetLastAccessedIndexEntry(lPoolItem); // Reset Last Access Index
			mCurrentReservedPoolItems.put(lPoolItem.getPoolItemId(), lPoolItem); // Place into Reserved List
			
			pTask.setReservedPoolItem(lPoolItem);
						
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("{} State1: Lock Obtained for Task: {} Thread: {}", lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
			}
			
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			
			return lPoolItem;
		}

		//second check if the Pool Item is reserved
		lPoolItem = mCurrentReservedPoolItems.get(pPoolItemId);
		if (lPoolItem != null) {
			mLastAccessIndex.resetLastAccessedIndexEntry(lPoolItem); // Reset Last Access Index
			mPoolReservations.addReservation(new PoolReservation (lPoolItem, pTask)); // Add to the Reservation List

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("{} State2: Reservation Obtained for Task: {} Thread: {}", lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
			}
			
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			
			return null;
		}
		
		//third check if the Pool Item is in the middle of being retired
		lPoolItem = mCurrentRetiringPoolItems.get(pPoolItemId);
		if (lPoolItem != null) {
			mRetiringReservations.addReservation(new PoolReservation (lPoolItem, pTask));
			// you don't need to reset the Last Access Index for a file in the middle of being retired
			
			if (sLogger.isDebugEnabled()) {
				sLogger.debug("{} State3: PoolItem is currently being retired for Task: {} Thread: {}", lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
			}
			
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			
			return null;
		}
		
		//Okay the Pool Item does not exist, so we need to create it
		lPoolItem = instantiatePoolItem (pPoolItemId);
		if (lPoolItem == null) {
			//TODO yah we gotta problem here, throw something
			if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
				sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
			}
			return null;
		}
		
		mCurrentReservedPoolItems.put(pPoolItemId, lPoolItem);
		mLastAccessIndex.addLastAccessedIndexEntry(lPoolItem); // Add to the Last Access Index
		mOpenPoolItems++;
		
		if (sLogger.isDebugEnabled()) {
			sLogger.debug("{} State4: PoolItem is instantiated and reserved for Task: {} Thread: {}", lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
		}
		
		//TODO before returning you must check the highwater/lowwater marks and retire PoolItems
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		return lPoolItem;
	}
	
	
	protected abstract IPoolItem instantiatePoolItem (String pPoolItemId);
	

	public void releasePoolItem(IPoolItem pPoolItem) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		
	}

	private void retirePoolItem (IPoolItem pPoolItem) {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		
	}

	public void retire() {
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_ENTERING);
		}
		
		
		if (ApplicationPrecompilerSettings.TRACELOGACTIVE) {
			sLogger.trace(KnowledgeBaseConstants.TRACE_EXITING);
		}
		
	}
	
}
