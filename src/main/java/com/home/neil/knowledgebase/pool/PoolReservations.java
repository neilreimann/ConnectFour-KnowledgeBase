package com.home.neil.knowledgebase.pool;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoolReservations implements IPoolReservations {
	public static final String CLASS_NAME = PoolReservations.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static final Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private HashMap<String, LinkedList<IPoolReservation>> mCurrentReservations = new HashMap<>();
	
	private final Object mPoolReservationsLock = new Object();
	
	public void addReservation(IPoolReservation pPoolReservation) {
		synchronized (mPoolReservationsLock) {
			LinkedList<IPoolReservation> lPoolReservations = mCurrentReservations.get(pPoolReservation.getPoolItem().getPoolItemId());
			if (lPoolReservations == null) {
				lPoolReservations = new LinkedList<> ();
				mCurrentReservations.put(pPoolReservation.getPoolItem().getPoolItemId(), lPoolReservations);
			} 
			lPoolReservations.add(pPoolReservation);
		}
	}

	@Override
	public IPoolReservation popReservation(IPoolItem pPoolItem) {
		synchronized (mPoolReservationsLock) {
			LinkedList<IPoolReservation> lPoolReservations = mCurrentReservations.get(pPoolItem.getPoolItemId());
			if (lPoolReservations == null) {
				return null;
			}
			IPoolReservation lPoolReservation = lPoolReservations.pop();
			if (lPoolReservations.isEmpty()) {
				mCurrentReservations.remove(pPoolItem.getPoolItemId());
			}
			return lPoolReservation;
			
		}
	}

}
