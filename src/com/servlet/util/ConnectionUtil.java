package com.servlet.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author ken
 *
 */
public class ConnectionUtil {

	// private static String user = PropertiesUtil.getProperties("user");
	// private static String password =
	// PropertiesUtil.getProperties("password");
	// private static String url = PropertiesUtil.getProperties("url");
	// private static String driver = PropertiesUtil.getProperties("driver");
	//
	// static{
	// try {
	// Class.forName(driver);
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * 缓存的链接对象 - 不同的线程获得不同的缓存对象 ThreadLocal
	 */
	// private static Connection conn = null;
	// 创建一个连接池
	private static ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

	/**
	 * 获得数据库链接
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		if (threadLocal.get() == null) {
			try {
				// return DriverManager.getConnection(url, user, password);
				// 从连接池中获得新的链接
				return comboPooledDataSource.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return threadLocal.get();
	}

	/**
	 * 开启事务的方法
	 */
	public static void startTransaction() {
		// 获取一条新的链接
		Connection conn = getConnection();
		threadLocal.set(conn);
		// 对该连接开启事务
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提交事务
	 */
	public static void commit() {
		if (threadLocal.get() != null) {
			Connection conn = threadLocal.get();
			try {
				conn.commit();
				conn.close();
				threadLocal.set(null);// 清空缓存
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 回滚事务
	 */
	public static void rollback() {
		if (threadLocal.get() != null) {
			Connection conn = threadLocal.get();
			try {
				conn.rollback();
				conn.close();
				threadLocal.set(null);// 清空缓存
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param conn
	 * @param st
	 * @param rs
	 */
	public static void close(Connection conn, Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if (conn != null && conn.getAutoCommit()) {
				conn.close();// 通过连接池获取的连接对象，调用close方法时不会关闭连接，而是返回给连接池
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
