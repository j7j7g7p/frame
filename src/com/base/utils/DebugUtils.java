package com.base.utils;

import java.util.Date;

import com.base.web.AppConfig;

public class DebugUtils {

	private static long begin = 0;
	private static ThreadLocal<Long> tl = new ThreadLocal<Long>();

	public static void init() {
		begin = System.currentTimeMillis();
	}

	public static void elapse() {
		long interval = System.currentTimeMillis() - begin;
		System.out.println(interval + "ms");
		begin = System.currentTimeMillis();
	}

	public static void thread_init() {
		tl.set(System.currentTimeMillis());
	}

	public static void thread_elapse(String tag) {
		long end = System.currentTimeMillis();
		if (!AppConfig.release())
			System.out.println("thread[" + Thread.currentThread().getId()
					+ "]-" + tag + ":" + (end - tl.get()) + "ms");
		thread_init();
	}

	public static void thread_elapse2(String tag) {
		long end = System.currentTimeMillis();
		if (!AppConfig.release())
			System.out.println("thread[" + Thread.currentThread().getId()
					+ "]-" + tag + ":" + (end - tl.get()) + "ms");
		thread_init();
	}

	public static void thread_info(String tag) {
		if (!AppConfig.release())
			System.out.println("thread[" + Thread.currentThread().getId()
					+ "]:" + tag);
	}

	public static void main(String[] args) {
		Date d1 = DateUtils.getDate("2015-06-01 00:00:00");
		System.out.println(d1.getTime());
	}

}
