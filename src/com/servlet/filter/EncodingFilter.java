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
 * 编码的过滤器
 * 
 * @author ken
 *
 */
public class EncodingFilter implements Filter {

	public void destroy() {

	}

	/*
	 * 设计模式 - 装饰者设计模式
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		response.setCharacterEncoding("utf-8");

		String method = req.getMethod();
		if (method.equals("POST")) {
			// 解决post请求
			request.setCharacterEncoding("utf-8");
			// 放行
			chain.doFilter(request, response);
		} else {
			// 解决get请求
			// 创建自定义的request对象
			MyRequest myRequest = new MyRequest((HttpServletRequest) request);
			// 放行
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
		 * 重写getParament方法
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
