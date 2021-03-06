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
import com.home.neil.knowledgebase.pool.task.IPoolItemTask;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThread;
import com.home.neil.knowledgebase.pool.thread.initialization.IPoolItemInitializationThreadFactory;
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsTask;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThread;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThreadFactory;

public class Pool implements IPool, IKnowledgeBaseObject {
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

	private static final String LOG_NOT_READY_STATE = "Pool is not in a ready state!  GO AWAY! State: {} ";

//	CONSTRUCTOR LOGIC

	public Pool(IPoolConfig pPoolConfig) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		mSubPoolLevels = pPoolConfig.getSubPoolLevels();

		mActivePoolVars = new ActivePoolVars();
		mActivePoolVars.mHighWaterMark = pPoolConfig.getActiveHighWaterMark();
		mActivePoolVars.mLowWaterMark = pPoolConfig.getActiveLowWaterMark();
		mActivePoolVars.mPoolItemRetirementThreadCount = pPoolConfig.getActiveRetirementThreadCount();

		constructActiveFactories(pPoolConfig);

		if (mSubPoolLevels > 0) {
			mSubPoolVars = new SubPoolVars[mSubPoolLevels];
			for (int i = 0; i < mSubPoolLevels; i++) {
				mSubPoolVars[i].mHighWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);
				mSubPoolVars[i].mLowWaterMarks = pPoolConfig.getSubPoolHighWaterMark(i);
				mSubPoolVars[i].mPoolItemRetirementThreadCount = pPoolConfig.getSubPoolRetirementThreadCount(i);

				constructSubPoolFactories(pPoolConfig, i);

			}
		}

		sLogger.debug("Pool is entering INSTANTIATED State");
		mPoolState = POOLSTATE.INSTANTIATED;

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void constructActiveFactories(IPoolConfig pPoolConfig) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

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
					.getConstructor().newInstance();
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

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void constructSubPoolFactories(IPoolConfig pPoolConfig, int pSubPoolLevel) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		try {
			mSubPoolVars[pSubPoolLevel].mPoolItemInitializationThreadClassFactory = Class
					.forName(pPoolConfig.getSubPoolResurrectionThreadClassFactory(pSubPoolLevel));
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		try {
			mSubPoolVars[pSubPoolLevel].mInitializationThreadFactory = (IPoolItemInitializationThreadFactory) mSubPoolVars[pSubPoolLevel].mPoolItemInitializationThreadClassFactory
					.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		try {
			mSubPoolVars[pSubPoolLevel].mPoolItemRetirementThreadClassFactory = Class
					.forName(pPoolConfig.getSubPoolRetirementThreadClassFactory(pSubPoolLevel));
		} catch (ClassNotFoundException e) {
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException(e);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

//	INITIALIZATION LOGIC

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

		initStartActiveRetiringThreads();
		for (int i = 0; i < mSubPoolLevels; i++) {
			initStartSubPoolRetiringThreads(i);
		}

		mPoolState = POOLSTATE.READY;
		sLogger.debug("Pool is entering READY State");

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void initStartActiveRetiringThreads() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		// Start the ActiveCleanupThreads
		IPoolItemRetiringThreadFactory lPoolItemRetiringThreadFactory = null;
		try {
			lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) mActivePoolVars.mPoolItemRetirementThreadClassFactory.getDeclaredConstructor()
					.newInstance();
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

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void initStartSubPoolRetiringThreads(int pSubPoolLevel) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		// Start the SubPoolCleanupThreads

		IPoolItemRetiringThreadFactory lPoolItemRetiringThreadFactory = null;
		try {
			lPoolItemRetiringThreadFactory = (IPoolItemRetiringThreadFactory) mSubPoolVars[pSubPoolLevel].mPoolItemRetirementThreadClassFactory
					.getDeclaredConstructor().newInstance();
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
				lPoolItemRetiringThread = lPoolItemRetiringThreadFactory.getRetiringThread(this, pSubPoolLevel + 1);
			} catch (KnowledgeBaseException e) {
				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				throw new PoolException(e);
			}

			lPoolItemRetiringThread.start();

			mSubPoolVars[pSubPoolLevel].mPoolItemRetirementThreads.add(lPoolItemRetiringThread);

		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

//	RESERVE POOL ITEM LOGIC

	public IPoolItem reservePoolItem(String pPoolItemId, IPoolItemOperationsTask pTask) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		ReserveResult lReserveResult = reserveSearchActivePool(pPoolItemId, pTask);

		if (lReserveResult.mFound) {
			return lReserveResult.mReservedPoolItem;
		}

		// Okay so we know the PoolItem is not in the active pool.
		// 4A Check if you have reached the active pool high water mark. if so pause
		// because otherwise you will overflow the ActivePool
		reserveWaitOnActivePoolHighWaterMark(pPoolItemId, pTask);

		reserveSearchSubPoolsOrCreate(pPoolItemId, pTask);

		return null;

	}

	private class ReserveResult {
		boolean mFound = false;
		IPoolItem mReservedPoolItem = null;

		private ReserveResult(boolean pFound, IPoolItem pReservedPoolItem) {
			mFound = pFound;
			mReservedPoolItem = pReservedPoolItem;
		}
	}

	private ReserveResult reserveSearchActivePool(String pPoolItemId, IPoolItemOperationsTask pTask) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		synchronized (mPoolLock) {
			IPoolItem lPoolItem = null;

			// 1 check if the Pool Item is in the Active Pool and Unreserved
			lPoolItem = mActivePoolVars.mUnReservedPoolItems.remove(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem); // Reset Pool Item
																									// Index in Active
																									// Pool
				mActivePoolVars.mReservedPoolItems.put(lPoolItem.getPoolItemId(), lPoolItem); // Place into Active Pool
																								// Reserved List

				pTask.setReservedPoolItem(lPoolItem);

				sLogger.debug("{} State Active-Reserve-1: PoolItem is already instantiated and now reserved in Active Pool: {} Thread: {}", pPoolItemId,
						pTask.getTaskName(), pTask.getTaskThread().getName());

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return new ReserveResult(true, lPoolItem);
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

				sLogger.debug("{} State Active-Reserve-2: PoolItem is already instantiated and now queued for reservation in Active Pool: {} Thread: {}",
						pPoolItemId, pTask.getTaskName(), pTask.getTaskThread().getName());

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return new ReserveResult(true, null);
			}

			// 3 check if the Pool Item is in the Active Pool and in the middle of being
			// retired
			lPoolItem = mActivePoolVars.mRetiringPoolItems.get(pPoolItemId);
			if (lPoolItem != null) {
				mActivePoolVars.mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));
				// you don't need to reset the Last Access Index for a file in the middle of
				// being retired

				sLogger.debug("{} State Active-Reserve-3: PoolItem is currently being retired in Active Pool and Queued for Resurrection: {} Thread: {}",
						pPoolItemId, pTask.getTaskName(), pTask.getTaskThread().getName());

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return new ReserveResult(true, null);
			}

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}

			return new ReserveResult(false, null);
		}
	}

	private void reserveWaitOnActivePoolHighWaterMark(String pPoolItemId, IPoolItemOperationsTask pTask) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		boolean lWait = true;
		synchronized (mPoolLock) {
			while (lWait) {
				if (mActivePoolVars.mCount >= mActivePoolVars.mHighWaterMark) {
					lWait = true;
				} else {
					// Add 1 to active count
					mActivePoolVars.mCount++;
					lWait = false;
				}

				if (lWait) {
					sLogger.debug(
							"{} Transition State HighWaterMark-Reserve-4A: HighWaterMark reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}",
							pPoolItemId, mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
					try {
						wait(10000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						sLogger.error("Thread interrupted", e);
					}
				} else {
					// 4B Okay so the Active Pool is now below the HighWaterMark so continue
					sLogger.debug(
							"{} Transition State HighWaterMark-Reserve-4B: HighWaterMark not reached: Active Pool Count {} Active Pool High Water Mark {} {} Thread: {}",
							pPoolItemId, mActivePoolVars.mCount, mActivePoolVars.mHighWaterMark, pTask.getTaskName(), pTask.getTaskThread().getName());
				}
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void reserveSearchSubPoolsOrCreate(String pPoolItemId, IPoolItemOperationsTask pTask) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItem lPoolItem = null;

		synchronized (mPoolLock) {
			// 5 check SubPools for PoolItem
			for (int i = 0; i < mSubPoolLevels; i++) {
				// 5A check SubPool for PoolItem
				lPoolItem = mSubPoolVars[i].mPoolItems.remove(pPoolItemId);
				if (lPoolItem != null) {
					sLogger.debug(
							"{} State SubPool{}-Reserve-5A: PoolItem is currently in SubPool {} and Now Queued in Active Pool for Resurrection: {} Thread: {}",
							lPoolItem.getPoolItemId(), i, i, pTask.getTaskName(), pTask.getTaskThread().getName());

					reinitPoolItemThread(lPoolItem, pTask, i);

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return;
				}

				// 5B check SubPool for Retirement
				lPoolItem = mSubPoolVars[i].mRetiringPoolItems.get(pPoolItemId);
				if (lPoolItem != null) {
					sLogger.debug(
							"{} State SubPool{}-Reserve-5B: PoolItem is currently being retired in Sub Pool {} and Queued for Resurrection: {} Thread: {}",
							pPoolItemId, i, i, pTask.getTaskName(), pTask.getTaskThread().getName());

					mSubPoolVars[i].mResurrectionReservations.addReservation(new PoolReservation(lPoolItem, pTask));

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return;
				}
			}

			initPoolItemThread(pPoolItemId, pTask);

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
		}
	}

	private void initPoolItemThread(String pPoolItemId, IPoolItemOperationsTask pTask) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lInitThread = mActivePoolVars.mInitializationThreadFactory.getInitializationThread(this, pPoolItemId, pTask);

		IPoolItem lPoolItem = lInitThread.getPoolItem();

		mActivePoolVars.mReservedPoolItems.put(pPoolItemId, lPoolItem);
		mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(lPoolItem);
		mActivePoolVars.mReservations.addReservation(new PoolReservation(lPoolItem, pTask));

		lInitThread.start();

		sLogger.debug("{} State Init-Reserve-6: PoolItem is currently being initialized: {} Thread: {}", pPoolItemId, pTask.getTaskName(),
				pTask.getTaskThread().getName());

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}

	}

	private void reinitPoolItemThread(IPoolItem pPoolItem, IPoolItemOperationsTask pTask, int pSubPoolLevel) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lInitThread = mSubPoolVars[pSubPoolLevel].mInitializationThreadFactory.getInitializationThread(this, pPoolItem);

		mActivePoolVars.mReservedPoolItems.put(pPoolItem.getPoolItemId(), pPoolItem);
		mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);
		mActivePoolVars.mReservations.addReservation(new PoolReservation(pPoolItem, pTask));

		lInitThread.start();

		sLogger.debug("{} State Reinit-Reserve-7: PoolItem is currently being reinitialized", pPoolItem.getPoolItemId());

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	private void resurrectPoolItemThread(IPoolItem pPoolItem, int pSubPoolLevel) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		IPoolItemInitializationThread lInitThread = mSubPoolVars[pSubPoolLevel].mInitializationThreadFactory.getInitializationThread(this, pPoolItem);

		mActivePoolVars.mReservedPoolItems.put(pPoolItem.getPoolItemId(), pPoolItem);
		mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);

		lInitThread.start();

		sLogger.debug("{} State Reinit-Reserve-7: PoolItem is currently being reinitialized", pPoolItem.getPoolItemId());

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
				lResurrectedPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(pPoolItem);
			}

			IPoolReservation lNextPoolReservation = mActivePoolVars.mReservations.popReservation(pPoolItem);

			if (lNextPoolReservation != null) {
				mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);

				IPoolItemOperationsTask lPoolItemTask = lNextPoolReservation.getTask();

				lPoolItemTask.setReservedPoolItem(pPoolItem);

				lPoolItemTask.notifyAll();

				sLogger.debug("{} State InitCallback-Reserve-8: PoolItem has finished re/initialization on CallBack: {} Thread: {}", pPoolItem.getPoolItemId(),
						lPoolItemTask.getTaskName(), lPoolItemTask.getTaskThread().getName());
			}
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

// RELEASE POOL ITEM LOGIC

	public void releasePoolItem(IPoolItem pPoolItem) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
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

				sLogger.debug("{} State Active-Release-1: PoolItem is released to next task: {} Thread: {}", pPoolItem.getPoolItemId(),
						lPoolItemTask.getTaskName(), lPoolItemTask.getTaskThreadName());

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}

				return;
			}

			sLogger.debug("{} State Active-Release-1: PoolItem is released.", pPoolItem.getPoolItemId());

			mActivePoolVars.mPoolItemLastAccessIndexes.resetLastAccessedIndexEntry(pPoolItem);
			mActivePoolVars.mReservedPoolItems.remove(lPoolItemId);
			mActivePoolVars.mUnReservedPoolItems.put(lPoolItemId, pPoolItem);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

// RETIRING POOL ITEM LOGIC

	public List<IPoolItem> getRetiringPoolItems(int pTargetSubPoolLevel) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("State Error-Retire-0: Pool is not in a READY or RETIRING state!  GO AWAY! State: {} ", mPoolState);

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		List<IPoolItem> lRetiringPoolItems = null;

		if (pTargetSubPoolLevel == 0) {
			lRetiringPoolItems = getRetiringActivePoolItems();
		} else if (pTargetSubPoolLevel > 0 && pTargetSubPoolLevel <= mSubPoolLevels) {
			lRetiringPoolItems = getRetiringSubPoolItems(pTargetSubPoolLevel);
		} else {
			sLogger.error("{} State Error-Retire-2A: Retiring thread is working below the Drain.", "Not Found");
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lRetiringPoolItems;
	}

	public List<IPoolItem> getRetiringActivePoolItems() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("State Active-Retire-0: Pool is not in a READY or RETIRING state!  GO AWAY! State: {} ", mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		LinkedList<IPoolItem> lRetiringPoolItems = new LinkedList<>();

		synchronized (mPoolLock) {
			// Checking Active Pool to retire
			int lPoolItemsToTransfer = 0;

			int lPoolItemsOverLowWaterMark = mActivePoolVars.mCount - mActivePoolVars.mLowWaterMark;
			
			if (mSubPoolLevels > 0) {

				int lPoolItemsUnderSubPoolWaterMark = mSubPoolVars[0].mHighWaterMarks - mSubPoolVars[0].mCount;

				lPoolItemsToTransfer = (lPoolItemsOverLowWaterMark > lPoolItemsUnderSubPoolWaterMark) ? lPoolItemsUnderSubPoolWaterMark
						: lPoolItemsOverLowWaterMark;
			} else {
				lPoolItemsToTransfer = lPoolItemsOverLowWaterMark;
			}

			for (int i = 0; i < lPoolItemsToTransfer; i++) {

				IPoolItem lPoolItemToRetire = (IPoolItem) mActivePoolVars.mPoolItemLastAccessIndexes.popLastAccessedIndexEntry();

				IPoolItem lRetiringPoolItem = mActivePoolVars.mUnReservedPoolItems.remove(lPoolItemToRetire.getPoolItemId());

				if (lRetiringPoolItem != null) {
					mActivePoolVars.mRetiringPoolItems.put(lPoolItemToRetire.getPoolItemId(), lPoolItemToRetire);

					lRetiringPoolItems.add(lPoolItemToRetire);

					sLogger.debug("{} State Active-Retire-1: PoolItem to be retired", lRetiringPoolItem.getPoolItemId());
				} else {
					sLogger.warn(
							"{} State Active-Retire-1A: THRASH WARNING (Possibly more threads than Active Pool Items: Last Accessed Pool Item is reserved.",
							lPoolItemToRetire.getPoolItemId());
					mActivePoolVars.mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
				}
			}

		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lRetiringPoolItems;
	}

	public List<IPoolItem> getRetiringSubPoolItems(int pTargetSubPoolLevel) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error("State SubPool{}-Retire-0: Pool is not in a READY or RETIRING state!  GO AWAY! State: {} ", mPoolState, pTargetSubPoolLevel);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		LinkedList<IPoolItem> lRetiringPoolItems = new LinkedList<>();

		if (pTargetSubPoolLevel > 0 && pTargetSubPoolLevel <= mSubPoolLevels) {
			synchronized (mPoolLock) {
				// Checking SubPool to retire

				int pSourceSubPoolLevel = pTargetSubPoolLevel - 1;
				
				int lPoolItemsToTransfer = 0;

				int lPoolItemsOverLowWaterMark = mSubPoolVars[pSourceSubPoolLevel].mCount - mSubPoolVars[pSourceSubPoolLevel].mLowWaterMarks;
				
				if (pTargetSubPoolLevel < mSubPoolLevels) {
					int lPoolItemsUnderSubPoolWaterMark = mSubPoolVars[pTargetSubPoolLevel].mHighWaterMarks - mSubPoolVars[pTargetSubPoolLevel].mCount;

					lPoolItemsToTransfer = (lPoolItemsOverLowWaterMark > lPoolItemsUnderSubPoolWaterMark) ? lPoolItemsUnderSubPoolWaterMark
							: lPoolItemsOverLowWaterMark;
				} else {
					lPoolItemsToTransfer = lPoolItemsOverLowWaterMark;
				}

				for (int i = 0; i < lPoolItemsToTransfer; i++) {

					IPoolItem lPoolItemToRetire = (IPoolItem) mSubPoolVars[pSourceSubPoolLevel].mPoolItemLastAccessIndexes.popLastAccessedIndexEntry();

					mSubPoolVars[pSourceSubPoolLevel].mPoolItems.remove(lPoolItemToRetire.getPoolItemId());

					mSubPoolVars[pSourceSubPoolLevel].mRetiringPoolItems.put(lPoolItemToRetire.getPoolItemId(), lPoolItemToRetire);

					lRetiringPoolItems.add(lPoolItemToRetire);

					sLogger.debug("{} State SubPool{}-Retire-2: PoolItem to be retired", lPoolItemToRetire.getPoolItemId(), pSourceSubPoolLevel);
				}
			}
		} else {
			sLogger.error("{} State Error-Retire-2A: Retiring thread is working below the Drain.", "Not Found");
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
		return lRetiringPoolItems;
	}

	public void retirePoolItemFromPoolCallback(int pTargetSubPoolLevel, String pPoolItemId) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		if (pTargetSubPoolLevel == 0) {
			retirePoolItemFromActivePoolCallback(pPoolItemId);
		} else if (pTargetSubPoolLevel > 0 && mSubPoolLevels > 0) {
			retirePoolItemFromSubPoolCallback(pTargetSubPoolLevel, pPoolItemId);
		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void retirePoolItemFromActivePoolCallback(String pPoolItemId) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		synchronized (mPoolLock) {
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

			if (lPoolReservation == null || mPoolState == POOLSTATE.RETIRED || mPoolState == POOLSTATE.RETIRING) {
				if (mSubPoolLevels > 0) {
					sLogger.debug("{} State Active-Retire-3: PoolItem retired to SubPool[0]", lPoolItemToRetire.getPoolItemId());
					mSubPoolVars[0].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
					mSubPoolVars[0].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
					mSubPoolVars[0].mCount++;
				} else {
					// Otherwise it goes to the drain
					sLogger.debug("{} State Active-Retire-3A: PoolItem retired to the Drain", lPoolItemToRetire.getPoolItemId());
				}

				// Remove from the Active Pool Count
				mActivePoolVars.mCount--;

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				return;
			}

			// Check if we are in a pool retirement state
			// Not in a Pool Retirement state? Guess what... we gotta reinstantiate and
			// requeue the reservations

			sLogger.debug("{} State Active-Retire-4: PoolItem set for Resurrection to Main Pool", lPoolItemToRetire.getPoolItemId());

			while (lPoolReservation != null) {
				mActivePoolVars.mReservations.addReservation(lPoolReservation);
				lPoolReservation = mActivePoolVars.mResurrectionReservations.popReservation(lPoolItemToRetire);
			}

			resurrectPoolItemThread(lPoolItemToRetire, 0);

		}
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	public void retirePoolItemFromSubPoolCallback(int pTargetSubPoolLevel, String pPoolItemId) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY && mPoolState != POOLSTATE.RETIRING) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		synchronized (mPoolLock) {
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

			if (lPoolReservation == null || mPoolState == POOLSTATE.RETIRED || mPoolState == POOLSTATE.RETIRING) {

				if (pTargetSubPoolLevel < mSubPoolLevels) {
					sLogger.debug("{} State SubPool{}-Retire-1: PoolItem retired to SubPool{}", lPoolItemToRetire.getPoolItemId(), lSourceSubPoolLevel,
							pTargetSubPoolLevel);
					mSubPoolVars[pTargetSubPoolLevel].mPoolItems.put(pPoolItemId, lPoolItemToRetire);
					mSubPoolVars[pTargetSubPoolLevel].mPoolItemLastAccessIndexes.addLastAccessedIndexEntry(lPoolItemToRetire);
					mSubPoolVars[pTargetSubPoolLevel].mCount++;
				} else {
					sLogger.debug("{} State SubPool{}-Retire-1A: PoolItem retired to the Drain", lPoolItemToRetire.getPoolItemId(), lSourceSubPoolLevel);
				}
				// Remove from the Sub Pool Count
				mSubPoolVars[lSourceSubPoolLevel].mCount--;

				if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
					sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
				}
				return;
			}

			// Not in a Pool Retirement state? Guess what... we gotta reinstantiate and
			// requeue the reservations

			sLogger.debug("{} State SubPool{}-Retire-4: PoolItem set for Resurrection to Active Pool", lPoolItemToRetire.getPoolItemId(), lSourceSubPoolLevel);

			while (lPoolReservation != null) {
				mActivePoolVars.mReservations.addReservation(lPoolReservation);
				lPoolReservation = mSubPoolVars[lSourceSubPoolLevel].mResurrectionReservations.popReservation(lPoolItemToRetire);
			}

			resurrectPoolItemThread(lPoolItemToRetire, pTargetSubPoolLevel);
		}

		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

// POOL RETIREMENT FUNCTIONS

	public synchronized void retire() throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		if (mPoolState != POOLSTATE.READY) {
			sLogger.error(LOG_NOT_READY_STATE, mPoolState);
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

		while (lCounts > 0) {
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

		
		
		for (int i = 0; i < mSubPoolLevels; i++) {
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

	private int getFullPoolCount() {
		int lActiveCount = mActivePoolVars.mCount;
		int lCounts = lActiveCount;
		sLogger.info("Pool Retirement: Active Pool Count {}", lActiveCount);
		for (int i = 0; i < mSubPoolLevels; i++) {
			int lSubPoolCount = mSubPoolVars[i].mCount;
			sLogger.info("Pool Retirement: SubPool{i} Count {}", lSubPoolCount);
			lCounts += lSubPoolCount;
		}
		sLogger.info("Pool Retirement: Full Count {}", lCounts);
		return lCounts;
	}


}
