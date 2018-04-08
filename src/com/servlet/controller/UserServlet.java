package com.servlet.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.servlet.base.BaseServlet;
import com.servlet.entity.Page;
import com.servlet.entity.User;
import com.servlet.service.IUserService;
import com.servlet.service.impl.UserServiceImpl;

/**
 * 框架： 
 * 1、控制层的简化  : 解决请求分发的问题, 解决参数处理的问题 ,跳转的简化, 上传下载的简化.... 
 * 2、持久层的简化 - ORM框架 :级联操作,对象的封装 关系映射...
 * 3、各层之间的解耦 - spring
 * 
 * 
 * 
 * 效果： 1、解决事件分发的问题 2、参数获取的问题 3、参数列表的简化 4、跳转的简化 - 默认就是转发，重定向加上前缀redirect:
 * 
 */
public class UserServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	
	//？？？使用多肽，以后换实现类，只需改动实现类，其他内容不用改
	private IUserService userService = new UserServiceImpl();

	/**
	 * 注册用户
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public String register(User user) throws IOException {
		System.out.println("--->" + user);
		// 调用service层 - 进行用户的添加
		int id = userService.addUser(user);
		System.out.println("生成的主键：" + id);

		if (id > 0) {
			return "redirect:index.jsp";
		} else {
			return "register.jsp";
		}
	}

	/**
	 * 查询所有用户
	 * 
	 * @return
	 */
	public String queryall(Page page, HttpServletRequest request) {
		// 分页查询
		page = userService.queryPage(page);
		request.setAttribute("page", page);
		return "userlist.jsp";
	}
}
