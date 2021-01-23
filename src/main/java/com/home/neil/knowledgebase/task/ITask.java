package com.home.neil.knowledgebase.task;

public interface ITask {

	public String getTaskThreadName ();
	
	public void renameTask(String pLogContext);
	
	public String getTaskName (); 
	
	public void notifyThread();
	
	public void startTask();
	
	public boolean isTaskSuccessful();
	
	public boolean isTaskFinished();
}
