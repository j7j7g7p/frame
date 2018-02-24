package com.base.task;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.base.ds.DataSourceManager;

public abstract class MyTimerTask extends TimerTask {
	private static final Logger log = Logger.getLogger(MyTimerTask.class);
	private String id;
	private String info = "";

	/**
	 * id=业务Id
	 * 
	 * @param id
	 */
	public MyTimerTask(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public abstract void execute() throws Exception;

	public void run() {
		Thread thread = new Thread() {
			public void run() {
				try {
					log.debug(info + "定时器[" + id + "]开始执行...");
					execute();
					DataSourceManager.commit();
				} catch (Exception ex) {
					ex.printStackTrace();
					DataSourceManager.rollback();
				} finally {
					// DataSourceManager.printState();
				}
			}
		};
		thread.start();
	}
}
