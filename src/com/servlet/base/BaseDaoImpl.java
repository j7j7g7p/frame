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
	 * 新增数据 - 返回新增数据的主键
	 * 
	 * 主键回填： jdbc自带 存储过程
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
			// 设置参数
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}

			int result = ps.executeUpdate();
			if (result > 0) {
				// 添加成功
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
	 * 修改和删除的方法
	 * 
	 * @return
	 */
	public int update(String sql, Object... params) throws SQLException {
		Connection conn = ConnectionUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			// 设置参数
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			return ps.executeUpdate();
		} finally {
			ConnectionUtil.close(conn, ps, null);
		}
	}

	/**
	 * 查询全部
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
				// 获得查询结果 -> 实体类
				Field[] fields = tcls.getDeclaredFields();
				// 创建实体类
				T t = null;
				try {
					t = tcls.newInstance();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				for (Field field : fields) {
					try {
						field.setAccessible(true);// 授权
						// 从结果集中获取对象的数据值
						Object value = rs.getObject(field.getName());
						// 将该数据值放入实体类的对应字段中
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
	 * 查询单条记录
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
	 * 更通用的查询封装
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

			// 离线ResultSet
			// 封装离线结果集
			RowSetFactory factory = RowSetProvider.newFactory();
			CachedRowSet cacherowset = factory.createCachedRowSet();
			// 将结果集放入离线集合
			cacherowset.populate(rs);
			return cacherowset;
		} finally {
			ConnectionUtil.close(conn, ps, rs);
		}
	}

	/**
	 * 分页查询
	 * 
	 * select count(*) from xxxx
	 * 
	 * @return
	 * @throws SQLException
	 */
	public <T> Page<T> queryByPage(Page<T> page, Class<T> tcls, String sql, Object... params) throws SQLException {

		// 将sql语句转换成小写
		sql = sql.toLowerCase();

		// 查询总条数
		String sqlcount = "select count(*) " + sql.substring(sql.indexOf("from"));

		// 查询总条数
		ResultSet rs = this.query(sqlcount, params);
		int sum = 0;
		if (rs.next()) {
			sum = rs.getInt(1);
		}

		// 计算总页码
		page.setPageSum(sum);
		page.setPageCount(page.getPageSum() % page.getPageSize() == 0 ? page.getPageSum() / page.getPageSize()
				: page.getPageSum() / page.getPageSize() + 1);

		// 开始分页查询
		sql = sql + " limit ?,?";

		// 参数的处理
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
