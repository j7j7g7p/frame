package com.servlet.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * ����Ĺ�����
 * 
 * @author ken
 *
 */
public class EncodingFilter implements Filter {

	public void destroy() {

	}

	/*
	 * ���ģʽ - װ�������ģʽ
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		response.setCharacterEncoding("utf-8");

		String method = req.getMethod();
		if (method.equals("POST")) {
			// ���post����
			request.setCharacterEncoding("utf-8");
			// ����
			chain.doFilter(request, response);
		} else {
			// ���get����
			// �����Զ����request����
			MyRequest myRequest = new MyRequest((HttpServletRequest) request);
			// ����
			chain.doFilter(myRequest, response);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

	private static class MyRequest extends HttpServletRequestWrapper {

		public MyRequest(HttpServletRequest request) {
			super(request);
		}

		/**
		 * ��дgetParament����
		 */
		@Override
		public String getParameter(String name) {
			String value = super.getParameter(name);
			if (value != null) {
				try {
					return new String(value.getBytes("iso-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return value;
		}

	}

}
