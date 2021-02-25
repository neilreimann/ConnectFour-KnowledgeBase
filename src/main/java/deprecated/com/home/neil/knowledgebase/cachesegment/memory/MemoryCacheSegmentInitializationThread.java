package deprecated.com.home.neil.knowledgebase.cachesegment.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.home.neil.knowledgebase.pool.IPool;
import com.home.neil.knowledgebase.pool.IPoolItem;
import com.home.neil.knowledgebase.pool.thread.initialization.PoolItemInitializationThread;
import com.home.neil.task.BasicAppTask;
import com.home.neil.task.TaskException;

public class MemoryCacheSegmentInitializationThread extends PoolItemInitializationThread {
	public static final String CLASS_NAME = MemoryCacheSegmentInitializationThread.class.getName();
	public static final String PACKAGE_NAME = CLASS_NAME.substring(0, CLASS_NAME.lastIndexOf("."));
	public static Logger sLogger = LogManager.getLogger(PACKAGE_NAME);
	
	private MemoryCacheSegmentInitializationTask mMemoryCacheSegmentInitializationTask = null;
	
	protected MemoryCacheSegmentInitializationThread(IPool pPool, IPoolItem pOrigPoolItem,
			String pLogContext,	boolean pRecordThreadStatistics) {
		super(pPool, pLogContext, pRecordThreadStatistics);
		
		mMemoryCacheSegmentInitializationTask = new MemoryCacheSegmentInitializationTask(pPool, pOrigPoolItem, pLogContext, pRecordThreadStatistics);
	
	}

	protected BasicAppTask createAppTask() throws TaskException {
		return mMemoryCacheSegmentInitializationTask;
	}
	
}
