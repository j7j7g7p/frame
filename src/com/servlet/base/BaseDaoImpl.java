package com.servlet.base;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import com.servlet.entity.Page;
import com.servlet.util.ConnectionUtil;

public class BaseDaoImpl {

	/**
	 * �������� - �����������ݵ�����
	 * 
	 * ������� jdbc�Դ� �洢����
	 * 
	 * @return
	 * @throws SQLException
	 */
	public int insert(String sql, Object... params) throws SQLException {
		Connection conn = ConnectionUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			// ���ò���
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}

			int result = ps.executeUpdate();
			if (result > 0) {
				// ��ӳɹ�
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					return rs.getInt(1);
				}
			}

			return result;
		} finally {
			ConnectionUtil.close(conn, ps, rs);
		}
	}

	/**
	 * �޸ĺ�ɾ���ķ���
	 * 
	 * @return
	 */
	public int update(String sql, Object... params) throws SQLException {
		Connection conn = ConnectionUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			// ���ò���
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			return ps.executeUpdate();
		} finally {
			ConnectionUtil.close(conn, ps, null);
		}
	}

	/**
	 * ��ѯȫ��
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T> List<T> queryAll(String sql, Class<T> tcls, Object... params) throws SQLException {
		Connection conn = ConnectionUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		List<T> rList = new ArrayList<>();
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				// ��ò�ѯ��� -> ʵ����
				Field[] fields = tcls.getDeclaredFields();
				// ����ʵ����
				T t = null;
				try {
					t = tcls.newInstance();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				for (Field field : fields) {
					try {
						field.setAccessible(true);// ��Ȩ
						// �ӽ�����л�ȡ���������ֵ
						Object value = rs.getObject(field.getName());
						// ��������ֵ����ʵ����Ķ�Ӧ�ֶ���
						field.set(t, value);
					} catch (Exception e) {
					}
				}

				rList.add(t);
			}
		} finally {
			ConnectionUtil.close(conn, ps, rs);
		}

		return rList;
	}

	/**
	 * ��ѯ������¼
	 * 
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T> T queryOne(String sql, Class<T> tcls, Object... params)
			throws InstantiationException, IllegalAccessException, SQLException {
		List<T> ts = this.queryAll(sql, tcls, params);
		return ts != null ? ts.get(0) : null;
	}

	/**
	 * ��ͨ�õĲ�ѯ��װ
	 * 
	 * @throws SQLException
	 */
	public ResultSet query(String sql, Object... params) throws SQLException {
		Connection conn = ConnectionUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}

			rs = ps.executeQuery();

			// ����ResultSet
			// ��װ���߽����
			RowSetFactory factory = RowSetProvider.newFactory();
			CachedRowSet cacherowset = factory.createCachedRowSet();
			// ��������������߼���
			cacherowset.populate(rs);
			return cacherowset;
		} finally {
			ConnectionUtil.close(conn, ps, rs);
		}
	}

	/**
	 * ��ҳ��ѯ
	 * 
	 * select count(*) from xxxx
	 * 
	 * @return
	 * @throws SQLException
	 */
	public <T> Page<T> queryByPage(Page<T> page, Class<T> tcls, String sql, Object... params) throws SQLException {

		// ��sql���ת����Сд
		sql = sql.toLowerCase();

		// ��ѯ������
		String sqlcount = "select count(*) " + sql.substring(sql.indexOf("from"));

		// ��ѯ������
		ResultSet rs = this.query(sqlcount, params);
		int sum = 0;
		if (rs.next()) {
			sum = rs.getInt(1);
		}

		// ������ҳ��
		page.setPageSum(sum);
		page.setPageCount(page.getPageSum() % page.getPageSize() == 0 ? page.getPageSum() / page.getPageSize()
				: page.getPageSum() / page.getPageSize() + 1);

		// ��ʼ��ҳ��ѯ
		sql = sql + " limit ?,?";

		// �����Ĵ���
		List<Object> paramsList = new ArrayList<>();
		for (Object param : params) {
			paramsList.add(param);
		}
		paramsList.add((page.getPage() - 1) * page.getPageSize());
		paramsList.add(page.getPageSize());

		List<T> datas = this.queryAll(sql, tcls, paramsList.toArray());
		page.setDatas(datas);
		return page;
	}
}
