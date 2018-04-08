package com.servlet.dao.impl;

import java.sql.SQLException;
import java.util.List;

import com.servlet.base.BaseDaoImpl;
import com.servlet.dao.IUserDao;
import com.servlet.entity.Page;
import com.servlet.entity.User;

/**
 * 用户管理的持久层实现类
 * 
 * @author ken
 *
 */
public class UserDaoImpl extends BaseDaoImpl implements IUserDao {

	@Override
	public int insert(User user) throws SQLException {
		String sql = "insert into user value(null, ?, ?, ?, ?)";
		return super.insert(sql, user.getUsername(), user.getPassword(), user.getName(), user.getAge());
	}

	/**
	 * 查询所有用户
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Override
	public List<User> queryAll() throws SQLException {
		String sql = "select * from user";
		return super.queryAll(sql, User.class);
	}

	/**
	 * 分页查询
	 */
	@Override
	public Page<User> queryPage(Page<User> pageObj) throws SQLException {
		String sql = "select * from user";
		return super.queryByPage(pageObj, User.class, sql);
	}
}
