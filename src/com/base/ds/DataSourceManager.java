package com.base.ds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.base.dt.DTClient;
import com.base.dt.DTDriver;
import com.base.utils.CallerUtils;
import com.base.utils.ParaMap;
import com.base.web.AppConfig;
import org.apache.log4j.Logger;

public class DataSourceManager {
	private static final Logger log = Logger.getLogger(DataSourceManager.class);
	private static Hashtable<Long, WrapConnection> activeMap = new Hashtable<Long, WrapConnection>();
	private static ConcurrentHashMap<Long, String> callPathMap = new ConcurrentHashMap<Long, String>();
	private static Stack<WrapConnection> pool = new Stack<WrapConnection>();

	public static Connection getConnection() {
		long curThreadId = Thread.currentThread().getId();
		WrapConnection wrcon = activeMap.get(curThreadId);
		callPathMap.put(curThreadId, CallerUtils.getCallStack());
		try {
			if (wrcon == null) {
				while (!pool.empty()) {
					wrcon = pool.pop();
					if (wrcon.validate()) {
						log.debug("从数据库连接池获取连接>>thread id:" + curThreadId
								+ " hashcode:" + wrcon.hashCode());
						activeMap.put(curThreadId, wrcon);
						wrcon.realSetAutoCommit(false);
						return wrcon;
					} else {
						wrcon.realClose();
						pool.remove(wrcon);
						wrcon = null;
					}
				}
				Connection con = DriverManager.getConnection(
						AppConfig.getStringPro("url"), AppConfig.getStringPro("user"),
						AppConfig.getStringPro("password"));
				wrcon = new WrapConnection(con);
				wrcon.realSetAutoCommit(false);
				wrcon.setLastTime(System.currentTimeMillis());
				activeMap.put(curThreadId, wrcon);
				log.debug("getConnection建立数据库连接>>thread id:" + curThreadId
						+ " hashcode:" + wrcon.hashCode() + " autocommit:"
						+ con.getAutoCommit());
			}
			log.debug("连接>>hashcode:" + wrcon.hashCode() + " autocommit:"
					+ wrcon.getAutoCommit());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return wrcon;
	}

	public static void commit() throws Exception {
		long curThreadId = Thread.currentThread().getId();
		WrapConnection wrcon = activeMap.get(curThreadId);
		activeMap.remove(curThreadId);
		callPathMap.remove(curThreadId);
		if (wrcon == null)
			return;
		try {
			wrcon.commit();
			log.debug("commit存入数据库连接池>>thread id:" + curThreadId + " hashcode:"
					+ wrcon.hashCode() + " autocommit:" + wrcon.getAutoCommit());
		} catch (SQLException ex) {
			try {
				wrcon.rollback();
				throw ex;
			} catch (SQLException ex2) {
				log.error(ex2);
			}
			log.error(ex);
		} finally {
			wrcon.clearStatement();
			wrcon.setLastTime(System.currentTimeMillis());
			pool.push(wrcon);
		}
	}

	public static void rollback() {
		long curThreadId = Thread.currentThread().getId();
		WrapConnection wrcon = activeMap.get(curThreadId);
		activeMap.remove(curThreadId);
		callPathMap.remove(curThreadId);
		if (wrcon == null)
			return;
		try {
			wrcon.rollback();
			log.debug("rollback存入数据库连接池>>thread id:" + curThreadId
					+ " hashcode:" + wrcon.hashCode() + " autocommit:"
					+ wrcon.getAutoCommit());
			DTDriver.rollback();
		} catch (Exception ex) {
			log.error(ex);
		} finally {
			wrcon.clearStatement();
			wrcon.setLastTime(System.currentTimeMillis());
			pool.push(wrcon);
		}
	}

	public static void closeAllConnection() {
		Enumeration<Long> e = activeMap.keys();
		while (e.hasMoreElements()) {
			WrapConnection wrcon = activeMap.get(e.nextElement());
			wrcon.clearStatement();
			wrcon.realClose();
		}
		activeMap.clear();
		while (!pool.isEmpty()) {
			WrapConnection wrcon = pool.pop();
			wrcon.clearStatement();
			wrcon.realClose();
		}
		pool.clear();
		callPathMap.clear();
	}

	public static int numberActive() {
		return activeMap.size();
	}

	public static int numberIdle() {
		return pool.size();
	}

	public static void printState() {
		int active = DataSourceManager.numberActive();
		int idle = DataSourceManager.numberIdle();
		if (active > 0 || idle > 0)
			log.debug("connection pool -- active:" + active + "  idle:" + idle);
	}

	public static ParaMap getCallers() {
		ParaMap out = new ParaMap();
		out.put("active", DataSourceManager.numberActive());
		out.put("idle", DataSourceManager.numberIdle());
		out.put("callPathMap size", callPathMap.size());
		Enumeration<Long> en = activeMap.keys();
		while (en.hasMoreElements()) {
			Long key = en.nextElement();
			String path = callPathMap.get(key);
			out.put(key, path);
		}
		return out;
	}

}