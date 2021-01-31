package com.home.neil.knowledgebase;

public interface IKnowledgeBaseObject {
	public void init () throws KnowledgeBaseException;
	
	public void retire () throws KnowledgeBaseException;
}
