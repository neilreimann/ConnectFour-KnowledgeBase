package com.home.neil.knowledgebase.pool;

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
import com.home.neil.knowledgebase.pool.thread.operations.IPoolItemOperationsThread;
import com.home.neil.knowledgebase.pool.thread.retiring.IPoolItemRetiringThread;

public abstract class Pool implements IPool, IKnowledgeBaseObject {
	public static final String CLASS_NAME = Pool.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	public enum POOLSTATE {
		INSTANTIATED, READY, RETIRING, RETIRED
	}
	
	private POOLSTATE mPoolState = POOLSTATE.INSTANTIATED;

	//Active Pool
	private HashMap<String, IPoolItem> mActivePool_ReservedPoolItems = null;
	private HashMap<String, IPoolItem> mActivePool_UnReservedPoolItems = null;
	private IPoolReservations mActivePool_Reservations = null;
	private ILastAccessIndex [] mActivePool_PoolItemLastAccessIndexes = null;
	private int mActivePool_HighWaterMark = 1000;
	private int mActivePool_LowWaterMark = 900;
	private int mActivePool_Count = 0;
	private LinkedList <IPoolItemInitializationThread> mActivePool_PoolItemInitializationThreads = null; 
	private LinkedList <IPoolItemOperationsThread> mActivePool_PoolItemOperationsThreads = null; 
	private LinkedList <IPoolItemRetiringThread> mActivePool_PoolItemRetirementThreads = null; 
	
	private int mSubPool_Levels = 0; 
	//Sub Pool 
	private HashMap<String, IPoolItem> [] mSubPool_PoolItems = null;
	private IPoolReservations [] mSubPool_Reservations = null;
	private ILastAccessIndex [] mSubPool_PoolItemLastAccessIndexes = null;
	private LinkedList <IPoolItemInitializationThread> [] mSubPool_PoolItemInitializationThreads = null; 
	private LinkedList <IPoolItemRetiringThread> [] mSubPool_PoolItemRetirementThreads = null; 
	private int [] mSubPool_HighWaterMarks = null;
	private int [] mSubPool_LowWaterMarks = null;
	private int [] mSubPool_Counts = null;
	
	//The Drain
	private HashMap <String, IPoolItem> mDrainPoolItems = null;
	private IPoolReservations mDrainResurrectionReservations = null;
	
	protected Pool(int pPoolLevels, int [] pHighWaterMarks, int [] pLowWaterMarks) {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}
		mPoolLevels = pPoolLevels;
		
		mHighWaterMarks = pHighWaterMarks;
		mLowWaterMarks = pLowWaterMarks;
		
		mPoolItemCounts = new int [mPoolLevels];
		for (int i  = 0; i < mPoolLevels; i++) {
			mPoolItemCounts [i] = 0;
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

		
		
		mCurrentActiveUnReservedPoolItems = new HashMap<>();
		mCurrentActiveReservedPoolItems = new HashMap<>();
		mActiveLastAccessIndex = new LastAccessIndex();
		mActiveReservations = new PoolReservations();
		mActivePoolItems = 0;

		mCurrentRetiringPoolItems = new HashMap<>();
		mRetiringLastAccessIndex = new LastAccessIndex();
		mRetiringReservations = new PoolReservations();
		mRetiringPoolItems = 0;

		mCurrentTerminalPoolItems = new HashMap<> ();
		mTerminalReservations = new PoolReservations();
		
		if (mPoolItemRetiringThreads == null || mPoolItemRetiringThreads.length < 1) {
			sLogger.error("PoolItem Cleanup Threads are not defined!");

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		//Start the CleanupThreads
		for (int i = 0; i < mPoolItemRetiringThreads.length; i++) {
			mPoolItemRetiringThreads[i].start();
		}

		mPoolState = POOLSTATE.READY;
		sLogger.debug("Pool is entering READY State");
		
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
		}
	}

	
	@SuppressWarnings("squid:S3776") //ignoring complexity rule only
	public IPoolItem reservePoolItem(String pPoolItemId, IPoolItemTask pTask) throws PoolException {
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

		while (true) {
			synchronized (mPoolLock) {
				// first check if the Pool Item is unreserved
				lPoolItem = mCurrentActiveUnReservedPoolItems.remove(pPoolItemId);
				if (lPoolItem != null) {
					mActiveLastAccessIndex.resetLastAccessedIndexEntry(lPoolItem); // Reset Last Access Index
					mCurrentActiveReservedPoolItems.put(lPoolItem.getPoolItemId(), lPoolItem); // Place into Reserved List

					pTask.setReservedPoolItem(lPoolItem);

					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} StateA1: Lock Obtained for Task: {} Thread: {}", lPoolItem.getPoolItemId(),
								pTask.getTaskName(), pTask.getTaskThreadName());
					}

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return lPoolItem;
				}

				// second check if the Pool Item is reserved
				lPoolItem = mCurrentActiveReservedPoolItems.get(pPoolItemId);
				if (lPoolItem != null) {
					mActiveLastAccessIndex.resetLastAccessedIndexEntry(lPoolItem); // Reset Last Access Index
					mActiveReservations.addReservation(new PoolReservation(lPoolItem, pTask)); // Add to the Reservation
																								// List

					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} StateA2: Reservation Obtained for Task: {} Thread: {}",
								lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
					}

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return null;
				}

				// third check if the Pool Item is in the middle of being retired
				lPoolItem = mCurrentRetiringPoolItems.get(pPoolItemId);
				if (lPoolItem != null) {
					mRetiringReservations.addReservation(new PoolReservation(lPoolItem, pTask));
					// you don't need to reset the Last Access Index for a file in the middle of
					// being retired

					if (sLogger.isDebugEnabled()) {
						sLogger.debug("{} StateA3: PoolItem is currently being retired for Task: {} Thread: {}",
								lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
					}

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}

					return null;
				}

				if (mActivePoolItems < mActiveHighWaterMark) {
					lPoolItem = addNewPoolItemToPool(pPoolItemId, pTask);

					if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
						sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
					}
					return lPoolItem;
				}

				try {
					sLogger.debug("HighWaterMark reached: HighWaterMark {} PoolItems Count {}", mActiveHighWaterMark,
							mActivePoolItems);
					wait(10000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					sLogger.error("Thread interrupted", e);
				}
			}
		}
	}

	private IPoolItem addNewPoolItemToPool(String pPoolItemId, IPoolItemTask pTask) throws PoolException {
		if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
			sLogger.trace(ApplicationPrecompilerSettings.TRACE_ENTERING);
		}

		// Okay the Pool Item does not exist, so we need to create it
		IPoolItem lPoolItem = initializePoolItem(pPoolItemId);

		if (lPoolItem == null) {
			sLogger.error("Pool Item failed to initialize");

			if (ApplicationPrecompilerSettings.TRACE_LOGACTIVE) {
				sLogger.trace(ApplicationPrecompilerSettings.TRACE_EXITING);
			}
			throw new PoolException();
		}

		mCurrentActiveReservedPoolItems.put(pPoolItemId, lPoolItem);
		mActiveLastAccessIndex.addLastAccessedIndexEntry(lPoolItem); // Add to the Last Access Index
		mActivePoolItems++;

		if (sLogger.isDebugEnabled()) {
			sLogger.debug("{} StateA4: PoolItem is instantiated and reserved for Task: {} Thread: {}",
					lPoolItem.getPoolItemId(), pTask.getTaskName(), pTask.getTaskThreadName());
		}

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
