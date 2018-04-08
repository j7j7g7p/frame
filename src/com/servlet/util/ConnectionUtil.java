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
	 * ��������Ӷ��� - ��ͬ���̻߳�ò�ͬ�Ļ������ ThreadLocal
	 */
	// private static Connection conn = null;
	// ����һ�����ӳ�
	private static ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

	/**
	 * ������ݿ�����
	 * 
	 * @return
	 */
	public static Connection getConnection() {
		if (threadLocal.get() == null) {
			try {
				// return DriverManager.getConnection(url, user, password);
				// �����ӳ��л���µ�����
				return comboPooledDataSource.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return threadLocal.get();
	}

	/**
	 * ��������ķ���
	 */
	public static void startTransaction() {
		// ��ȡһ���µ�����
		Connection conn = getConnection();
		threadLocal.set(conn);
		// �Ը����ӿ�������
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ύ����
	 */
	public static void commit() {
		if (threadLocal.get() != null) {
			Connection conn = threadLocal.get();
			try {
				conn.commit();
				conn.close();
				threadLocal.set(null);// ��ջ���
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �ع�����
	 */
	public static void rollback() {
		if (threadLocal.get() != null) {
			Connection conn = threadLocal.get();
			try {
				conn.rollback();
				conn.close();
				threadLocal.set(null);// ��ջ���
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
				conn.close();// ͨ�����ӳػ�ȡ�����Ӷ��󣬵���close����ʱ����ر����ӣ����Ƿ��ظ����ӳ�
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
