package com.servlet.service.impl;

import java.sql.SQLException;
import java.util.List;

import com.servlet.dao.IUserDao;
import com.servlet.dao.impl.UserDaoImpl;
import com.servlet.entity.Page;
import com.servlet.entity.User;
import com.servlet.service.IUserService;

/**
 * 用户管理业务层的实现类
 * 
 * @author ken
 *
 */
public class UserServiceImpl implements IUserService {

	/**
	 * 持久层对象
	 */
	private IUserDao userDao = new UserDaoImpl();

	/**
	 * 添加用户
	 */
	@Override
	public int addUser(User user) {
		try {
			return userDao.insert(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public List<User> queryAll() {
		try {
			return userDao.queryAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Page<User> queryPage(Page<User> pageObj) {
		try {
			return userDao.queryPage(pageObj);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
