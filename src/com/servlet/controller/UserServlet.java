package com.servlet.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.servlet.base.BaseServlet;
import com.servlet.entity.Page;
import com.servlet.entity.User;
import com.servlet.service.IUserService;
import com.servlet.service.impl.UserServiceImpl;

/**
 * ��ܣ� 
 * 1�����Ʋ�ļ�  : �������ַ�������, ���������������� ,��ת�ļ�, �ϴ����صļ�.... 
 * 2���־ò�ļ� - ORM��� :��������,����ķ�װ ��ϵӳ��...
 * 3������֮��Ľ��� - spring
 * 
 * 
 * 
 * Ч���� 1������¼��ַ������� 2��������ȡ������ 3�������б�ļ� 4����ת�ļ� - Ĭ�Ͼ���ת�����ض������ǰ׺redirect:
 * 
 */
public class UserServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	
	//������ʹ�ö��ģ��Ժ�ʵ���ֻ࣬��Ķ�ʵ���࣬�������ݲ��ø�
	private IUserService userService = new UserServiceImpl();

	/**
	 * ע���û�
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public String register(User user) throws IOException {
		System.out.println("--->" + user);
		// ����service�� - �����û������
		int id = userService.addUser(user);
		System.out.println("���ɵ�������" + id);

		if (id > 0) {
			return "redirect:index.jsp";
		} else {
			return "register.jsp";
		}
	}

	/**
	 * ��ѯ�����û�
	 * 
	 * @return
	 */
	public String queryall(Page page, HttpServletRequest request) {
		// ��ҳ��ѯ
		page = userService.queryPage(page);
		request.setAttribute("page", page);
		return "userlist.jsp";
	}
}
