package com.home.neil.knowledgebase.pool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.appmanager.ApplicationPrecompilerSettings;
import com.home.neil.knowledgebase.IKnowledgeBaseObject;
import com.home.neil.knowledgebase.KnowledgeBaseException;
import com.home.neil.knowledgebase.index.ILastAccessIndex;
import com.home.neil.knowledgebase.index.LastAccessIndex;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThreadFactory;
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

	// Active Pool Variables
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

		private Class<?> mPoolItemInitializationThreadClassFactory = null;
		private IPoolItemInitializationThreadFactory mInitializationThreadFactory = null;

		private Class<?> mPoolItemRetirementThreadClassFactory = null;
		private LinkedList<IPoolItemRetiringThread> mPoolItemRetirementThreads = null;
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

		private Class<?> mPoolItemInitializationThreadClassFactory = null;
		private IPoolItemInitializationThreadFactory mInitializationThreadFactory = null;

		private Class<?> mPoolItemRetirementThreadClassFactory = null;
		private LinkedList<IPoolItemRetiringThread> mPoolItemRetirementThreads = null;
		private int mPoolItemRetirementThreadCount = 2;
	}

	private ActivePoolVars mActivePoolVars = null;
	private int mSubPoolLevels = 0;
	private SubPoolVars[] mSubPoolVars = null;

	private final Object mPoolLock = new Object();

	protected Pool(IPoolConfig pPoolConfig) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		mSubPoolLevels = pPoolConfig.getSubPoolLevels();

		mActivePoolVars = new ActivePoolVars();
		mActivePoolVars.mHighWaterMark = pPoolConfig.getActiveHighWaterMark();
		mActivePoolVars.mLowWaterMark = pPoolConfig.getActiveLowWaterMark();

		try {
			mActivePoolVars.mPoolItemInitializationThreadClassFactory = Class.forName(pPoolConfig.getActiveInitializationThreadClassFactory());
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		try {
			mActivePoolVars.mInitializationThreadFactory = (IPoolItemInitializationThreadFactory) mActivePoolVars.mPoolItemInitializationThreadClassFactory
					.getConstructor(new Class[] {}).newInstance(new Object[] {});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		try {
			mActivePoolVars.mPoolItemRetirementThreadClassFactory = Class.forName(pPoolConfig.getActiveRetirementThreadClassFactory());
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}
		mActivePoolVars.mPoolItemRetirementThreadCount = pPoolConfig.getActiveRetirementThreadCount();

		if (mSubPoolLevels > 0) {
			mSubPoolVars = new SubPoolVars[mSubPoolLevels];
			for (int i = 0; i < mSubPoolLevels; i++) {
				mSubPoolVars[i].mHighWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);
				mSubPoolVars[i].mLowWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);

				try {
					mSubPoolVars[i].mPoolItemInitializationThreadClassFactory = Class.forName(pPoolConfig.getSubPoolResurrectionThreadClassFactory(i));
				} catch (ClassNotFoundException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException(e);
				}

				try {
					mSubPoolVars[i].mInitializationThreadFactory = (IPoolItemInitializationThreadFactory) mSubPoolVars[i].mPoolItemInitializationThreadClassFactory
							.getConstructor(new Class[] {}).newInstance(new Object[] {});
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException(e);
				}

				try {
					mSubPoolVars[i].mPoolItemRetirementThreadClassFactory = Class.forName(pPoolConfig.getSubPoolRetirementThreadClassFactory(i));
				} catch (ClassNotFoundException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException(e);
				}
				mSubPoolVars[i].mPoolItemRetirementThreadCount = pPoolConfig.getSubPoolRetirementThreadCount(i);

			}
		}

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

		mActivePoolVars.mUnReservedPoolItems = new HashMap<>();
		mActivePoolVars.mReservedPoolItems = new HashMap<>();
		mActivePoolVars.mRetiringPoolItems = new HashMap<>();
		mActivePoolVars.mPoolItemLastAccessIndexes = new LastAccessIndex();
		mActivePoolVars.mReservations = new PoolReservations();
		mActivePoolVars.mCount = 0;

		mActivePoolVars.mPoolItemRetirementThreads = new LinkedList<>();

		for (int i = 0; i < mSubPoolLevels; i++) {
			mSubPoolVars[i].mPoolItems = new HashMap<>();
			mSubPoolVars[i].mPoolItemLastAccessIndexes = new LastAccessIndex();
			mSubPoolVars[i].mResurrectionReservations = new PoolReservations();
			mSubPoolVars[i].mCount = 0;
			mSubPoolVars[i].mPoolItemRetirementThreads = new LinkedList<>();
		}

		// Start the ActiveCleanupThreads
		IPoolItemRetiringThreadFactory lPoolItemRetiringThreadFactory = null;
		try {
			lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) mActivePoolVars.mPoolItemRetirementThreadClassFactory
					.getDeclaredConstructor(new Class[] {}).newInstance(new Object[] {});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		for (int i = 0; i < mActivePoolVars.mPoolItemRetirementThreadCount; i++) {
			IPoolItemRetiringThread lPoolItemRetiringThread;
			try {
				lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.getRetiringThread(this, 0);
			} catch (KnowledgeBaseException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException(e);
			}

			lPoolItemRetiringThread.start();

			mActivePoolVars.mPoolItemRetirementThreads.add(lPoolItemRetiringThread);

		}

		// Start the SubPoolCleanupThreads
		for (int j = 0; j < mSubPoolLevels; j++) {
			lPoolItemRetiringThreadFactory = null;
			try {
				lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) mSubPoolVars[j].mPoolItemRetirementThreadClassFactory
						.getDeclaredConstructor(new Class[] {}).newInstance(new Object[] {});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException(e);
			}

			for (int i = 0; i < mSubPoolVars[i].mPoolItemRetirementThreadCount; i++) {
				IPoolItemRetiringThread lPoolItemRetiringThread;
				try {
					lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.getRetiringThread(this, j + 1);
				} catch (KnowledgeBaseException e) {
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException(e);
				}

				lPoolItemRetiringThread.start();

				mSubPoolVars[j].mPoolItemRetirementThreads.add(lPoolItemRetiringThread);

			}
		}

		mPoolState = POOLSTATE.READY;
		sLogger.debug("Pool is entering READY State");

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

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
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem); // Reset Pool Item
																									// Index in Active
																									// Pool
				mActivePoolVars.mReservedPoolItems.put(lPoolItem.getPoolItemId(), lPoolItem); // Place into Active Pool
																								// Reserved List

				pTask.setReservedPoolItem(lPoolItem);

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-1: PoolItem is already instantiated and now reserved in Active Pool: {} Thread: {}", pPoolItemId,
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
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem); // Reset Pool Item
																									// Index in Active
																									// Pool
				mActivePoolVars.mReservations.addReservation(new PoolReservation(lPoolItem, pTask)); // Add to the
																										// Reservation
																										// List

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-2: PoolItem is already instantiated and now queued for reservation in Active Pool: {} Thread: {}",
							pPoolItemId, pTask.getTaskName(), pTask.getTaskThread().getName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return null;
			}

			// 3 check if the Pool Item is in the Active Pool and in the middle of being
			// retired
			lPoolItem = mActivePoolVars.mRetiringPoolItems.get(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
				// you don't need to reset the Last Access Index for a file in the middle of
				// being retired

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Reserve-3: PoolItem is currently being retired in Active Pool and Queued for Resurrection: {} Thread: {}",
							pPoolItemId, pTask.getTaskName(), pTask.getTaskThread().getName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return null;
			}
		}

		// Okay so we know the PoolItem is not in the active pool.
		// 4A Check if you have reached the active pool high water mark. if so pause
		// because otherwise you will overflow the ActivePool
		boolean lWait = true;
		while (lWait) {
			synchronized (mPoolLock) {
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
					sLogger.debug(
							"{} Transition State HighWaterMark-Reserve-4A: HighWaterMark reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}",
							pPoolItemId, mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
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
					sLogger.debug(
							"{} Transition State HighWaterMark-Reserve-4B: HighWaterMark not reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}",
							pPoolItemId, mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
				}
			}
		}

		synchronized (mPoolLock) {
			// 5 check SubPools for PoolItem
			for (int i = 0; i < mSubPoolLevels; i++) {
				// 5A check SubPool for PoolItem
				lPoolItem = mSubPoolVars[i].mPoolItems.remove(pPoolItemId);
				if (lPoolItem != null) {
					sLogger.debug(
							"{} State SubPool{}-Reserve-5A: PoolItem is currently in SubPool {} and Now Queued in Active Pool for Resurrection: {} Thread: {}",
							lPoolItem.getPoolItemId(), i, i, pTask.getTaskName(), pTask.getTaskThread().getName());

					reinitPoolItemThread(lPoolItem, i);

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return null;
				}

				// 5B check SubPool for Retirement
				lPoolItem = mSubPoolVars[i].mRetiringPoolItems.get(pPoolItemId);
				if (lPoolItem != null) {
					if (sLogger.isDebugEnabled()) {
						sLogger.debug(
								"{} State SubPool{}-Reserve-5B: PoolItem is currently being retired in Sub Pool {} and Queued for Resurrection: {} Thread: {}",
								pPoolItemId, i, i, pTask.getTaskName(), pTask.getTaskThread().getName());
					}

					mSubPoolVars[i].mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return null;
				}
			}

			initPoolItemThread(pPoolItemId, pTask);

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}

			return null;

		}

	}

	private void initPoolItemThread(String pPoolItemId, IPoolItemOperationsTask pTask) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lInitThread = mActivePoolVars.mInitializationThreadFactory.getInitializationThread(this, pPoolItemId, pTask);

		IPoolItem lPoolItem = lInitThread.getPoolItem();

		mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));

		lInitThread.start();

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("{} State Init-Reserve-6: PoolItem is currently being initialized: {} Thread: {}", pPoolItemId, pTask.getTaskName(),
					pTask.getTaskThread().getName());
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void reinitPoolItemThread(IPoolItem pPoolItem, int pSubPoolLevel) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lInitThread = mSubPoolVars[pSubPoolLevel].mInitializationThreadFactory.getInitializationThread(this, pPoolItem);

		lInitThread.start();

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("{} State Reinit-Reserve-7: PoolItem is currently being reinitialized", pPoolItem.getPoolItemId());
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	public void initPoolItemThreadCallback(IPoolItem pPoolItem) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mPoolLock) {
			mActivePoolVars.mReservedPoolItems.put(pPoolItem.getPoolItemId(), pPoolItem);

			IPoolReservation lResurrectedPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(pPoolItem);

			while (lResurrectedPoolReservation != null) {
				mActivePoolVars.mReservations.addReservation(lResurrectedPoolReservation);
				lResurrectedPoolReservation = (IPoolReservation) mActivePoolVars.mResurrectionReservations.popReservation(pPoolItem);
			}

			IPoolReservation lNextPoolReservation = mActivePoolVars.mReservations.popReservation(pPoolItem);

			if (lNextPoolReservation != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);

				IPoolItemOperationsTask lPoolItemTask = lNextPoolReservation.getTask();

				lPoolItemTask.setReservedPoolItem(pPoolItem);

				lPoolItemTask.notifyAll();

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State InitCallback-Reserve-8: PoolItem has finished re/initialization on CallBack: {} Thread: {}", pPoolItem.getPoolItemId(),
							lPoolItemTask.getTaskName(), lPoolItemTask.getTaskThread().getName());
				}
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

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
			IPoolReservation lPoolReservation = mActivePoolVars.mReservations.popReservation(pPoolItem);
			if (lPoolReservation != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);
				IPoolItemOperationsTask lPoolItemTask = lPoolReservation.getTask();

				lPoolItemTask.setReservedPoolItem(pPoolItem);
				lPoolItemTask.notifyAll();

				if (sLogger.isDebugEnabled()) {
					sLogger.debug("{} State Active-Release-1: PoolItem is released to next task: {} Thread: {}", pPoolItem.getPoolItemId(),
							lPoolItemTask.getTaskName(), lPoolItemTask.getTaskThreadName());
				}

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return;
			}

			if (sLogger.isDebugEnabled()) {
				sLogger.debug("{} State Active-Release-1: PoolItem is released.", pPoolItem.getPoolItemId());
			}
			mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);
			mActivePoolVars.mReservedPoolItems.remove(lPoolItemId);
			mActivePoolVars.mUnReservedPoolItems.put(lPoolItemId, pPoolItem);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public List<IPoolItem> getRetiringPoolItems(int pTargetSubPoolLevel) throws PoolException {
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
			if (pTargetSubPoolLevel == 0) {
				//Checking Active Pool to retire
				int lPoolItemsOverLowWaterMark = mActivePoolVars.mCount - mActivePoolVars.mLowWaterMark;
				
				int lPoolItemsUnderSubPoolWaterMark = mSubPoolVars[0].mHighWaterMarks - mSubPoolVars[0].mCount;
				
				int lPoolItemsToTransfer = 
						(lPoolItemsOverLowWaterMark > lPoolItemsUnderSubPoolWaterMark) ? lPoolItemsUnderSubPoolWaterMark : lPoolItemsOverLowWaterMark;
						
				for (int i = 0; i < lPoolItemsToTransfer; i++) {
					
					IPoolItem lPoolItemToRetire = (IPoolItem) mActivePoolVars.mPoolItemLastAccessIndexes.popLastAccessedIndexEntry();
					
					IPoolItem lRetiringPoolItem = mActivePoolVars.mUnReservedPoolItems.remove(lPoolItemToRetire.getPoolItemId());

					if (lRetiringPoolItem != null) {
						mActivePoolVars.mRetiringPoolItems.put(lPoolItemToRetire.getPoolItemId(), lPoolItemToRetire);
						
						lRetiringPoolItems.add(lPoolItemToRetire);

						if (sLogger.isDebugEnabled()) {
							sLogger.debug("{} State Active-Retire-1: PoolItem to be retired", lRetiringPoolItem.getPoolItemId());
						}
					} else {
						sLogger.warn("{} State Active-Retire-1A: THRASH WARNING (Possibly more threads than Active Pool Items: Last Accessed Pool Item is reserved.", lRetiringPoolItem.getPoolItemId());
						mActivePoolVars.mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);;
					}
				}
			} else if (pTargetSubPoolLevel > 0 && pTargetSubPoolLevel <= mSubPoolLevels) {
				//Checking SubPool to retire
				
				int pSourceSubPoolLevel = pTargetSubPoolLevel - 1;
				
				int lPoolItemsOverLowWaterMark = mSubPoolVars[pSourceSubPoolLevel].mCount - mSubPoolVars[pSourceSubPoolLevel].mLowWaterMarks;
				
				int lPoolItemsUnderSubPoolWaterMark = pTargetSubPoolLevel >= mSubPoolLevels ? Integer.MAX_VALUE : 
						mSubPoolVars[pTargetSubPoolLevel].mHighWaterMarks - mSubPoolVars[pTargetSubPoolLevel].mCount; // The Drain is infinite
				
				int lPoolItemsToTransfer = 
						(lPoolItemsOverLowWaterMark > lPoolItemsUnderSubPoolWaterMark) ? lPoolItemsUnderSubPoolWaterMark : lPoolItemsOverLowWaterMark;
				
				for (int i = 0; i < lPoolItemsToTransfer; i++) {
					
					IPoolItem lPoolItemToRetire = (IPoolItem) mSubPoolVars[pSourceSubPoolLevel].mPoolItemLastAccessIndexes.popLastAccessedIndexEntry();
					
					mSubPoolVars[pSourceSubPoolLevel].mPoolItems.remove(lPoolItemToRetire.getPoolItemId());

					mSubPoolVars[pSourceSubPoolLevel].mRetiringPoolItems.put(lPoolItemToRetire.getPoolItemId(), lPoolItemToRetire);
						
					lRetiringPoolItems.add(lPoolItemToRetire);

					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State SubPool{}-Retire-2: PoolItem to be retired", lPoolItemToRetire.getPoolItemId(), pSourceSubPoolLevel);
					}
				}
			} else {
				sLogger.error("{} State Error-Retire-2A: Retiring thread is working below the Drain.");
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lRetiringPoolItems;
	}

	public void retirePoolItemFromPoolCallback (int pTargetSubPoolLevel, String pPoolItemId) throws PoolException {
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
			if (pTargetSubPoolLevel == 0) {
				// Finish retirement from ActivePool
				
				// Remove from Retiring List
				IPoolItem lPoolItemToRetire = mActivePoolVars.mRetiringPoolItems.remove(pPoolItemId);
				
				if (lPoolItemToRetire == null) {
					sLogger.error("{} Pool Item to Retire is not found in the Active Retiring List", pPoolItemId);

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException();
				}
				
				// Check for reservations
				IPoolReservation lPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(lPoolItemToRetire);

				if (lPoolReservation == null) {
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-3: PoolItem retired to SubPool[0]", lPoolItemToRetire.getPoolItemId());
					}
					if (mSubPoolLevels > 0) {
						mSubPoolVars[0].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
						mSubPoolVars[0].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
						mSubPoolVars[0].mCount++;
					} // Otherwise it goes to the drain					

					// Remove from the Active Pool Count
					mActivePoolVars.mCount--;
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				}

				
				// Check if we are in a pool retirement state
				if (mPoolState != POOLSTATE.RETIRED) {
					// Not in a Pool Retirement state?  Guess what... we gotta reinstantiate and requeue the reservations
					reinitPoolItemThread(lPoolItemToRetire, pTargetSubPoolLevel);
					
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-4: PoolItem set for Resurrection to Main Pool", lPoolItemToRetire.getPoolItemId());
					}
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				} else {

					// Remove from the Active Pool Count
					mActivePoolVars.mCount--;

					if (mSubPoolLevels > 0) {
						mSubPoolVars[0].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
						mSubPoolVars[0].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
						mSubPoolVars[0].mCount++;
					} // Otherwise it goes to the drain					
					
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-5: PoolItem is retired", lPoolItemToRetire.getPoolItemId());
					}

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				}
			} else if (pTargetSubPoolLevel > 0  && mSubPoolLevels > 0) {
				// Finish retirement from SubPool
				
				int lSourceSubPoolLevel = pTargetSubPoolLevel - 1;
				
				// Remove from Retiring List
				IPoolItem lPoolItemToRetire = mSubPoolVars[lSourceSubPoolLevel].mRetiringPoolItems.remove(pPoolItemId);
				
				if (lPoolItemToRetire == null) {
					sLogger.error("{} Pool Item to Retire is not found in the SubPool{} Retiring List", pPoolItemId, lSourceSubPoolLevel);

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					throw new PoolException();
				}
				
				// Check for reservations
				IPoolReservation lPoolReservation = mSubPoolVars[lSourceSubPoolLevel].mResurrectionReservations.popReservation(lPoolItemToRetire);

				if (lPoolReservation == null) {
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-6: PoolItem retired to SubPool[0]", lPoolItemToRetire.getPoolItemId());
					}

					
					if (pTargetSubPoolLevel < mSubPoolLevels) {
						mSubPoolVars[pTargetSubPoolLevel].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
						mSubPoolVars[pTargetSubPoolLevel].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
						mSubPoolVars[pTargetSubPoolLevel].mCount++;
					}					
					// Remove from the Active Pool Count
					mSubPoolVars[lSourceSubPoolLevel].mCount--;
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				}

				
				// Check if we are in a pool retirement state
				if (mPoolState != POOLSTATE.RETIRED) {
					// Not in a Pool Retirement state?  Guess what... we gotta reinstantiate and requeue the reservations
					reinitPoolItemThread(lPoolItemToRetire, pTargetSubPoolLevel);
					
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-4: PoolItem set for Resurrection to Main Pool", lPoolItemToRetire.getPoolItemId());
					}
					
					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				} else {
					if (pTargetSubPoolLevel < mSubPoolLevels) {
						mSubPoolVars[pTargetSubPoolLevel].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
						mSubPoolVars[pTargetSubPoolLevel].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
						mSubPoolVars[pTargetSubPoolLevel].mCount++;
					}					
					// Remove from the Active Pool Count
					mSubPoolVars[lSourceSubPoolLevel].mCount--;
					
					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} State Active-Retire-5: PoolItem is retired", lPoolItemToRetire.getPoolItemId());
					}

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				}
				
			}
			
			

		}
	}
	
	
	private int getFullPoolCount () {
		int lActiveCount = mActivePoolVars.mCount;
		int lCounts = lActiveCount;
		sLogger.info("Pool Retirement: Active Pool Count", lActiveCount);
		for (int i = 0; i < mSubPoolLevels; i++) {
			int lSubPoolCount = mSubPoolVars[i].mCount; 
			sLogger.info("Pool Retirement: SubPool{i} Count", lSubPoolCount);
			lCounts += lSubPoolCount;
		}
		sLogger.info("Pool Retirement: Full Count", lCounts);
		return lCounts;
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
		// first place the Pool into Retirement to halt all requests
		sLogger.debug("Pool is entering RETIRING State");
		mPoolState = POOLSTATE.RETIRING;

		// set the low water marks to 0
		
		for (int i = mSubPoolLevels - 1; i >= 0; i--) {
			mSubPoolVars[i].mLowWaterMarks = 0;
		}
		mActivePoolVars.mLowWaterMark = 0;

		// wait for the counts to collapse

		int lCounts = getFullPoolCount();
		
		while (lCounts >= 0) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				sLogger.error("Thread interrupted", e);
			}
			lCounts = getFullPoolCount();
		}
		
		sLogger.info("Pool Retirement: Full Count is zero.  Terminating Retiring Threads");
		
		for (IPoolItemRetiringThread lRetiringThread : mActivePoolVars.mPoolItemRetirementThreads) {
			lRetiringThread.setTerminate();
		}

		for (int i = 0; i < mSubPoolLevels; i++ ) {
			for (IPoolItemRetiringThread lRetiringThread : mSubPoolVars[i].mPoolItemRetirementThreads) {
				lRetiringThread.setTerminate();
			}
		}
		
		sLogger.info("Pool Retirement: Retiring Threads terminated.");
		
		sLogger.debug("Pool is entering RETIRED State");
		mPoolState = POOLSTATE.RETIRED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

}
