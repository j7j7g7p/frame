package com.servlet.service;

import java.util.List;

import com.servlet.entity.Page;
import com.servlet.entity.User;

/**
 * 用户管理的业务层接口 - 面向接口编程
 * 
 * @author ken
 *
 */
public interface IUserService {

	int addUser(User user);

	List<User> queryAll();

	Page<User> queryPage(Page<User> pageObj);
}
