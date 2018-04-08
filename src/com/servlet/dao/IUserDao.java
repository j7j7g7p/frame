package com.servlet.dao;

import java.sql.SQLException;
import java.util.List;

import com.servlet.entity.Page;
import com.servlet.entity.User;

public interface IUserDao {

	int insert(User user) throws SQLException;

	List<User> queryAll() throws SQLException;

	Page<User> queryPage(Page<User> pageObj) throws SQLException;
}
