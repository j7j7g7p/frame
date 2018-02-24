package com.base.web;

public abstract class AppInit {
	public abstract void init();

	public abstract void saveMQException(long id, String mqName,
			String mqContent,String errorMessage) throws Exception;
}
