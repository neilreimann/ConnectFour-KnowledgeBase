package com.home.neil.knowledgebase.pool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.index.ILastAccessIndex;
import com.home.neil.knowledgebase.index.LastAccessIndex;
import com.home.neil.knowledgebase.pool.task.IPoolItemTask;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsThread;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThread;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThreadFactory;

public abstract class Pool implements IPool, IKnowledgeBaseObject {
	public static final String CLASS_NAME = Pool.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public enum POOLSTATE {
		INSTANTIATED, READY, RETIRING, RETIRED
	}
	
	private POOLSTATE mPoolState = POOLSTATE.INSTANTIATED;

	//Active Pool Variables
	private class ActivePoolVars {
		private HashMap<String, IPoolItem> mReservedPoolItems = null;
		private HashMap<String, IPoolItem> mUnReservedPoolItems = null;
		private HashMap<String, IPoolItem> mRetiringPoolItems = null;
		private IPoolReservations mReservations = null;
		private IPoolReservations mResurrectionReservations = null;
		private ILastAccessIndex mPoolItemLastAccessIndexes = null;
		private int mHighWaterMark = 100;
		private int mLowWaterMark = 90;
		private int mCount = 0;
		
		private Class <?> mPoolItemInitializationThreadClassFactory = null;
		private LinkedList <IPoolItemInitializationThread> mPoolItemInitializationThreads = null; 
		
		private LinkedList <IPoolItemOperationsThread> mPoolItemOperationsThreads = null; 

		private Class <?> mPoolItemRetirementThreadClassFactory = null;
		private LinkedList <IPoolItemRetiringThread> mPoolItemRetirementThreads = null; 
		private int mPoolItemRetirementThreadCount = 2;
	}


	private class SubPoolVars {
		private HashMap<String, IPoolItem> mPoolItems = null;
		private HashMap<String, IPoolItem> mRetiringPoolItems = null;
		private IPoolReservations mResurrectionReservations = null;
		private ILastAccessIndex mPoolItemLastAccessIndexes = null;
		
		private int mHighWaterMarks = 100;
		private int mLowWaterMarks = 90;
		private int mCount = 0;

		private Class <?> mPoolItemResurrectionThreadClassFactory = null;
		private LinkedList <IPoolItemInitializationThread> mPoolItemResurrectionThreads = null; 

		private Class <?> mPoolItemRetirementThreadClassFactory = null;
		private LinkedList <IPoolItemRetiringThread> mPoolItemRetirementThreads = null; 
		private int mPoolItemRetirementThreadCount = 2;
	}
	
	
	//Drain Variables
//	private class DrainVars {
//		private Class <?> mPoolItemResurrectionThreadClassFactory = null;
//		private LinkedList <IPoolItemInitializationThread> mPoolItemResurrectionThreads = null; 
//		
//		private Class <?> mPoolItemRetirementThreadClassFactory = null;
//		private LinkedList <IPoolItemRetiringThread> mPoolItemRetirementThreads = null; 
//		private int mPoolItemRetirementThreadCount = 2;
//	}
	
	private ActivePoolVars mActivePoolVars = null;
	private int mSubPoolLevels = 0; 
	private SubPoolVars [] mSubPoolVars = null;
	//private DrainVars mDrainVars = null;
	
	private final Object mPoolLock = new Object();
	
	protected Pool(PoolConfig pPoolConfig) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		mSubPoolLevels = pPoolConfig.getSubPoolLevels();
		
		mActivePoolVars = new ActivePoolVars ();
		mActivePoolVars.mHighWaterMark = pPoolConfig.getActiveHighWaterMark();
		mActivePoolVars.mLowWaterMark = pPoolConfig.getActiveLowWaterMark();

		try {
			mActivePoolVars.mPoolItemInitializationThreadClassFactory = Class.forName(pPoolConfig.getActiveInitializationThreadClassFactory());
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException (e);
		}
		
		try {
			mActivePoolVars.mPoolItemRetirementThreadClassFactory = Class.forName(pPoolConfig.getActiveRetirementThreadClassFactory());
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException (e);
		}
		mActivePoolVars.mPoolItemRetirementThreadCount = pPoolConfig.getActiveRetirementThreadCount();
		
		
		if (mSubPoolLevels > 0) {
			mSubPoolVars = new SubPoolVars[mSubPoolLevels];
			for (int i = 0; i < mSubPoolLevels; i++) {
				mSubPoolVars[i].mHighWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);
				mSubPoolVars[i].mLowWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);

				try {
					mSubPoolVars[i].mPoolItemResurrectionThreadClassFactory = Class.forName(pPoolConfig.getSubPoolResurrectionThreadClassFactory(i));
				} catch (ClassNotFoundException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException (e);
				}
				
				try {
					mSubPoolVars[i].mPoolItemRetirementThreadClassFactory = Class.forName(pPoolConfig.getSubPoolRetirementThreadClassFactory(i));
				} catch (ClassNotFoundException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException (e);
				}
				mSubPoolVars[i].mPoolItemRetirementThreadCount = pPoolConfig.getSubPoolRetirementThreadCount(i);
				
			}
		}

//		try {
//			mDrainVars.mPoolItemResurrectionThreadClassFactory = Class.forName(pPoolConfig.getDrainResurrectionThreadClassFactory());
//		} catch (ClassNotFoundException e) {
//			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
//				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
//			}
//			throw new PoolException (e);
//		}
//
//		try {
//			mDrainVars.mPoolItemRetirementThreadClassFactory = Class.forName(pPoolConfig.getDrainRetirementThreadClassFactory());
//		} catch (ClassNotFoundException e) {
//			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
//				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
//			}
//			throw new PoolException (e);
//		}
//		mDrainVars.mPoolItemRetirementThreadCount = pPoolConfig.getDrainRetirementThreadCount();
		
		sLogger.debug("Pool is entering INSTANTIATED State");
		mPoolState = POOLSTATE.INSTANTIATED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void init() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.INSTANTIATED) {
			sLogger.error("Pool is not in a instantiated state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		mActivePoolVars.mUnReservedPoolItems = new HashMap<> ();
		mActivePoolVars.mReservedPoolItems = new HashMap<> ();
		mActivePoolVars.mRetiringPoolItems = new HashMap<> ();
		mActivePoolVars.mPoolItemLastAccessIndexes = new LastAccessIndex();
		mActivePoolVars.mReservations = new PoolReservations();
		mActivePoolVars.mCount = 0;

		
		mActivePoolVars.mPoolItemInitializationThreads = new LinkedList <> ();
		mActivePoolVars.mPoolItemOperationsThreads = new LinkedList <> ();
		mActivePoolVars.mPoolItemRetirementThreads = new LinkedList <> ();
		
		
		
		for (int i = 0; i < mSubPoolLevels; i++) {
			mSubPoolVars[i].mPoolItems = new HashMap <> ();
			mSubPoolVars[i].mPoolItemLastAccessIndexes = new LastAccessIndex();
			mSubPoolVars[i].mResurrectionReservations = new PoolReservations();
			mSubPoolVars[i].mCount = 0;
			mSubPoolVars[i].mPoolItemResurrectionThreads = new LinkedList <> ();
			mSubPoolVars[i].mPoolItemRetirementThreads = new LinkedList <> ();
		}
		
//		mDrainVars.mPoolItemResurrectionThreads = new LinkedList <> ();
//		mDrainVars.mPoolItemRetirementThreads = new LinkedList <> ();

		
		//Start the ActiveCleanupThreads
		IPoolItemRetiringThreadFactory lPoolItemRetiringThreadFactory = null;
		try {
			lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) 
					mActivePoolVars.mPoolItemRetirementThreadClassFactory.getDeclaredConstructor(new Class [] {}).newInstance(new Object [] {});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException (e);
		}

		for (int i = 0; i < mActivePoolVars.mPoolItemRetirementThreadCount; i++) {
			IPoolItemRetiringThread lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.createPoolItemRetiringThread(this);
			
			lPoolItemRetiringThread.start();
			
			mActivePoolVars.mPoolItemRetirementThreads.add (lPoolItemRetiringThread);
			
		}

		//Start the SubPoolCleanupThreads
		for (int j = 0 ; j < mSubPoolLevels; j++) {
			lPoolItemRetiringThreadFactory = null;
			try {
				lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) 
						mSubPoolVars[j].mPoolItemRetirementThreadClassFactory.getDeclaredConstructor(new Class [] {}).newInstance(new Object [] {});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException (e);
			}
	
			for (int i = 0; i < mSubPoolVars[i].mPoolItemRetirementThreadCount; i++) {
				IPoolItemRetiringThread lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.createPoolItemRetiringThread(this);
				
				lPoolItemRetiringThread.start();
				
				mSubPoolVars[j].mPoolItemRetirementThreads.add (lPoolItemRetiringThread);
				
			}
		}

//		//Start the DrainCleanupThreads
//		lPoolItemRetiringThreadFactory = null;
//		try {
//			lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) 
//					mDrainVars.mPoolItemRetirementThreadClassFactory.getDeclaredConstructor(new Class [] {}).newInstance(new Object [] {});
//		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
//				| NoSuchMethodException | SecurityException e) {
//			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
//				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
//			}
//			throw new PoolException (e);
//		}
//
//		for (int i = 0; i < mDrainVars.mPoolItemRetirementThreadCount; i++) {
//			IPoolItemRetiringThread lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.createPoolItemRetiringThread(this);
//			
//			lPoolItemRetiringThread.start();
//			
//			mDrainVars.mPoolItemRetirementThreads.add (lPoolItemRetiringThread);
//			
//		}
		
		mPoolState = POOLSTATE.READY;
		sLogger.debug("Pool is entering READY State");
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	
	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	public IPoolItem reservePoolItem(String pPoolItemId, IPoolItemOperationsTask pTask) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY) {
			sLogger.error("Pool is not in a ready state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		
		IPoolItem lPoolItem = null;

		synchronized (mPoolLock) {
			// 1 check if the Pool Item is in the Active Pool and Unreserved
			lPoolItem = mActivePoolVars.mUnReservedPoolItems.remove(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem); // Reset Pool Item Index in Active Pool
				mActivePoolVars.mReservedPoolItems.put(lPoolItem.getPoolItemId(), lPoolItem); // Place into Active Pool Reserved List

				pTask.setReservedPoolItem(lPoolItem);

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-1: PoolItem is already instantiated and now reserved in Active Pool: {} Thread: {}", lPoolItem.getPoolItemId(),
							pTask.getTaskName(), pTask.getTaskThread().getName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return lPoolItem;
			}

			// 2 check if the Pool Item is in the Active Pool and Reserved
			lPoolItem = mActivePoolVars.mReservedPoolItems.get(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem); // Reset Pool Item Index in Active Pool
				mActivePoolVars.mReservations.addReservation(new PoolReservation(lPoolItem, pTask)); // Add to the Reservation List

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-2: PoolItem is already instantiated and now queued for reservation in Active Pool: {} Thread: {}", lPoolItem.getPoolItemId(),
							pTask.getTaskName(), pTask.getTaskThread().getName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return null;
			}

			// 3 check if the Pool Item is in the Active Pool and in the middle of being retired
			lPoolItem = mActivePoolVars.mRetiringPoolItems.get(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
				// you don't need to reset the Last Access Index for a file in the middle of being retired

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-3: PoolItem is currently being retired in Active Pool and Queued for Resurrection: {} Thread: {}", lPoolItem.getPoolItemId(),
							pTask.getTaskName(), pTask.getTaskThread().getName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return null;
			}
		}

		// Okay so we know the PoolItem is not in the active pool.
		// 4A Check if you have reached the active pool high water mark.  if so pause because otherwise you will overflow the ActivePool
		boolean lWait = true;
		while (lWait) {
			synchronized (mPoolLock)  {
				if (mActivePoolVars.mCount >= mActivePoolVars.mHighWaterMark) {
					lWait = true;
				} else {
					// Add 1 to active count
					mActivePoolVars.mCount++;
					lWait = false;
				}
			}
			if (lWait) {
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} Transition State Active-Reserve-4A: HighWaterMark reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}", lPoolItem.getPoolItemId(),
						mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
				}
				try {
					wait(10000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					sLogger.error("Thread interrupted", e);
				}
			} else {
				// 4B Okay so the Active Pool is now below the HighWaterMark so continue
				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} Transition State Active-Reserve-4B: HighWaterMark not reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}", lPoolItem.getPoolItemId(),
						mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
				}
			}
		}
		
		synchronized (mPoolLock) {
			//5 check SubPools for PoolItem
			for (int i = 0; i < mSubPoolLevels; i++) {
				// 5A check SubPool for PoolItem
				lPoolItem = mSubPoolVars[i].mPoolItems.remove(pPoolItemId);
				if (lPoolItem != null) {
					sLogger.debug("{} State Active-Reserve-5A: PoolItem is currently in SubPool {} and Now Queued in Active Pool for Resurrection: {} Thread: {}", lPoolItem.getPoolItemId(),
							i, pTask.getTaskName(), pTask.getTaskThread().getName());
					
					// TODO Create initialization thread & task
					
					// Add Initial Task to Active Resurrection
					mActivePoolVars.mRetiringPoolItems.put(pPoolItemId, lPoolItem);
					mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
										
					// TODO Start initialization thread & task
					
					// TODO initialization thread & task Initiates call back function to place in reserved and start reservation task and reduce Sub Pool count (not here) and reindex in Active Thread
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return null;
				}
			
				// 5B check SubPool for Retirement
				lPoolItem = mSubPoolVars[i].mRetiringPoolItems.get(pPoolItemId);
				if (lPoolItem != null) {
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Reserve-5B: PoolItem is currently being retired in Sub Pool {} and Queued for Resurrection: {} Thread: {}", lPoolItem.getPoolItemId(),
								i, pTask.getTaskName(), pTask.getTaskThread().getName());
					}

					mSubPoolVars[i].mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return null;
				}
			}		
		
			
			//6 either been drained or never instantiated
			//TODO create initialization thread
			
			
		}
		
		
		
		
		
	}

	private IPoolItem addNewPoolItemToPool(String pPoolItemId, IPoolItemTask pTask) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		// TODO Create initialization thread & task
		
		//TODO Create PoolItem but do not initialize
		IPoolItem lPoolItem = null; //this should not be null
		
		// Add Initial Task to Active Resurrection
		mActivePoolVars.mRetiringPoolItems.put(pPoolItemId, lPoolItem); //Add to the retiring pool item list temporarily.  The call back function will put the real poolItem in.
		mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
							
		// TODO Start initialization thread & task
		
		// TODO initialization thread & task Initiates call back function to place in reserved and start reservation task and reduce Sub Pool count (not here) and reindex in Active Thread

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("{} StateA4: PoolItem is instantiated and reserved for Task: {} Thread: {}",
					lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());


		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lPoolItem;

	}

	
	
	protected abstract IPoolItem initializePoolItem(String pPoolItemId);
	
	
	

	public void releasePoolItem(IPoolItem pPoolItem) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY) {
			sLogger.error("Pool is not in a ready state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		
		synchronized (mPoolLock) {
			String lPoolItemId = pPoolItem.getPoolItemId();

			// first check if there are any waiting task threads
			IPoolReservation lPoolReservation = mActiveReservations.popReservation(pPoolItem);
			if (lPoolReservation != null) {
				mActiveLastAccessIndex.resetLastAccessedIndexEntry(pPoolItem);
				IPoolItemTask lPoolItemTask = lPoolReservation.getTask();
				// no need to take off reserved list

				lPoolItemTask.notifyAll();

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} StateB1A: PoolItem is released to next task: {} Thread: {}",
							pPoolItem.getPoolItemId(), lPoolItemTask.getTaskName(), lPoolItemTask.getTaskThreadName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return;
			}

			sLogger.debug("{} StateB2A: PoolItem is released", pPoolItem.getPoolItemId());
			// there are no reservations
			mActiveLastAccessIndex.resetLastAccessedIndexEntry(pPoolItem);
			mCurrentActiveReservedPoolItems.remove(lPoolItemId);
			mCurrentActiveUnReservedPoolItems.put(lPoolItemId, pPoolItem);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public List<IPoolItem> getRetiringPoolItems() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY || mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("Pool is not in a READY or RETIRING state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		
		LinkedList<IPoolItem> lRetiringPoolItems = new LinkedList<>();

		synchronized (mPoolLock) {
			int lFilesOverLowWaterMark = mActivePoolItems - mActiveLowWaterMark;

			for (int i = 0; i < lFilesOverLowWaterMark; i++) {
				IPoolItem lPoolItemToRetire = (IPoolItem) mActiveLastAccessIndex.popLastAccessedIndexEntry();
				mCurrentActiveUnReservedPoolItems.remove(lPoolItemToRetire.getPoolItemId());
				mCurrentRetiringPoolItems.put(lPoolItemToRetire.getPoolItemId(), lPoolItemToRetire);

				lRetiringPoolItems.add(lPoolItemToRetire);

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} StateA5: PoolItem to retire.", lPoolItemToRetire.getPoolItemId());
				}
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lRetiringPoolItems;
	}

	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	public void retirePoolItem(String pPoolItemId) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY || mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("Pool is not in a ready state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		
		synchronized (mPoolLock) {
			IPoolItem lPoolItemToRetire = mCurrentRetiringPoolItems.remove(pPoolItemId);
			if (lPoolItemToRetire == null) {
				sLogger.error("{} Pool Item to Retire is not found in the Retiring List", pPoolItemId);

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException();
			}

			IPoolReservation lPoolReservation = mRetiringReservations.popReservation(lPoolItemToRetire);

			if (lPoolReservation == null) {
				sLogger.debug("{} StateC1A: PoolItem is retired", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mActivePoolItems--;
				notifyAll();
				return;
			}

			
			if (mPoolState != POOLSTATE.RETIRED) {
				// Guess what... we gotta reinstantiate and requeue the reservations

				IPoolItemTask lResurrectedPoolItemTask = lPoolReservation.getTask();
				IPoolItem lResurrectedPoolItem = addNewPoolItemToPool(pPoolItemId, lResurrectedPoolItemTask);

				lResurrectedPoolItemTask.setReservedPoolItem(lResurrectedPoolItem);
				lResurrectedPoolItemTask.notifyAll();

				lPoolReservation = mRetiringReservations.popReservation(lPoolItemToRetire);
				while (lPoolReservation != null) {
					lResurrectedPoolItemTask = lPoolReservation.getTask();
					IPoolReservation lResurrectedPoolReservation = new PoolReservation(lResurrectedPoolItem,
							lResurrectedPoolItemTask);
					mActiveReservations.addReservation(lResurrectedPoolReservation);
					lPoolReservation = mRetiringReservations.popReservation(lPoolItemToRetire);
				}

				sLogger.debug("{} StateC1B: PoolItem is resurrected.", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				return;
			} else {
				sLogger.debug("{} StateC1A: PoolItem is retired", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mActivePoolItems--;
				return;
			}

		}
	}
	
	
	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	public void retireActivePoolItem(String pPoolItemId) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY || mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("Pool is not in a ready state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		
		synchronized (mPoolLock) {
			IPoolItem lPoolItemToRetire = mActivePoolVars.mRetiringPoolItems.remove(pPoolItemId);
			if (lPoolItemToRetire == null) {
				sLogger.error("{} Pool Item to Retire is not found in the Retiring List", pPoolItemId);

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException();
			}

			IPoolReservation lPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(lPoolItemToRetire);

			if (lPoolReservation == null) {
				sLogger.debug("{} State Active-Retire-1: PoolItem is retired without Resurrection", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mActivePoolVars.mCount--;
				notifyAll();
				return;
			}
			
			if (mPoolState != POOLSTATE.RETIRED) {
				// Guess what... we gotta reinstantiate and requeue the reservations

				IPoolItemOperationsTask lResurrectedPoolItemTask = lPoolReservation.getTask();
				
				IPoolItem lResurrectedPoolItem = addNewPoolItemToPool(pPoolItemId, lResurrectedPoolItemTask);

				//TODO Take the Resurrected Pool item and invoke the initialization factory....				
				lResurrectedPoolItemTask.setReservedPoolItem(lResurrectedPoolItem);
				lResurrectedPoolItemTask.notifyAll();

				// Put all Resurrection Reservations back to Active Reservations
				lPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(lPoolItemToRetire);
				while (lPoolReservation != null) {
					lResurrectedPoolItemTask = lPoolReservation.getTask();
					IPoolReservation lResurrectedPoolReservation = new PoolReservation(lResurrectedPoolItem,
							lResurrectedPoolItemTask);
					mActivePoolVars.mReservations.addReservation(lResurrectedPoolReservation);
					lPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(lPoolItemToRetire);
				}

				sLogger.debug("{} State Active-Retire-2: PoolItem is retired but is resurrected.", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
			} else {
				sLogger.debug("{} State Active-Retire-3: PoolItem is retired. Pool is retiring", pPoolItemId);
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				mActivePoolVars.mCount--;
			}

		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public synchronized void retire() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		
		if (mPoolState != POOLSTATE.READY) {
			sLogger.error("Pool is not in a READY state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}
		//first place the Pool into Retirement to halt all requests
		mPoolState = POOLSTATE.READY;
		sLogger.debug("Pool is entering RETIRING State");
		mPoolState = POOLSTATE.RETIRING;
		
		synchronized (mPoolLock) {
			//move all reserved and unreserved pool items to Retiring
			mCurrentRetiringPoolItems.putAll(mCurrentActiveReservedPoolItems);
			mCurrentRetiringPoolItems.putAll(mCurrentActiveUnReservedPoolItems);
		}
		while (!mCurrentRetiringPoolItems.isEmpty()) {
			try {
				Thread.sleep(1000); //wait for the cleanup threads to retire PoolItems
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		sLogger.debug("Pool is entering RETIRED State");
		mPoolState = POOLSTATE.RETIRED;
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

}
