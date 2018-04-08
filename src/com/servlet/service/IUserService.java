package com.servlet.service;

import java.util.List;

import com.servlet.entity.Page;
import com.servlet.entity.User;

/**
 * �û������ҵ���ӿ� - ����ӿڱ��
 * 
 * @author ken
 *
 */
public interface IUserService {

	int addUser(User user);

	List<User> queryAll();

	Page<User> queryPage(Page<User> pageObj);
}
