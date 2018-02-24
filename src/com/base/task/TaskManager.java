package com.base.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.lang.StringUtils;

public class TaskManager {
	private static Timer timer = new Timer();
	private static Map<String, MyTimerTask> timeTaskMap = new HashMap<String, MyTimerTask>();

	private static void addOneTimeTask(MyTimerTask task) throws Exception {
		String id = task.getId();
		if (StringUtils.isEmpty(id)) {
			throw new Exception("定时任务Id不能为空！");
		}
		if (timeTaskMap.containsKey(id)) {
			// remove(id);
			throw new Exception("定时任务Id重复..." + id);
		}
		timeTaskMap.put(id, task);
	}

	public static void add(MyTimerTask task, long delay) throws Exception {
		addOneTimeTask(task);
		timer.schedule(task, delay);
	}

	public static void add(MyTimerTask task, Date time) throws Exception {
		addOneTimeTask(task);
		timer.schedule(task, time);
	}

	public static void add(MyTimerTask task, long delay, long period)
			throws Exception {
		addOneTimeTask(task);
		timer.schedule(task, delay, period);
	}

	public static void add(MyTimerTask task, Date firstTime, long period)
			throws Exception {
		addOneTimeTask(task);
		timer.schedule(task, firstTime, period);
	}

	public static void addAtFixedRate(MyTimerTask task, long delay, long period)
			throws Exception {
		addOneTimeTask(task);
		timer.scheduleAtFixedRate(task, delay, period);
	}

	public static void addAtFixedRate(MyTimerTask task, Date firstTime,
			long period) throws Exception {
		addOneTimeTask(task);
		timer.scheduleAtFixedRate(task, firstTime, period);
	}

	public static void remove(String id) {
		MyTimerTask task = timeTaskMap.get(id);
		if (task != null) {
			task.cancel();
			timeTaskMap.remove(id);
			timer.purge();
		}
	}

	public static void remove(MyTimerTask task) {
		remove(task.getId());
	}

	// 终止此定时器，丢弃所有当前已安排的任务
	public static void stop() throws Exception {
		timer.cancel();
	}

	// 重新启动定时器
	public static void start() throws Exception {
		timer = new Timer();
		init();
	}

	public static void init() throws Exception {

	}

}
