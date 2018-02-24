package com.base.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.base.utils.AppLoader;
import com.base.utils.FileRoot;
import com.base.utils.StrUtils;
import com.base.web.AppConfig;

public abstract class BaseService {
	public Logger log = Logger.getLogger(this.getClass());
	private HttpServletRequest request;
	private HttpSession session;
	private HttpServletResponse response;
	public AppLoader appLoader = new AppLoader(AppConfig.getFile(FileRoot.jars)
			.getAbsolutePath());

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public String getRemoteIp() {
		String ip = "";
		// 获取请求ip
		if (request != null) {
			ip = request.getHeader("X-Real-IP");
			if (StrUtils.isNull(ip)) {
				ip = request.getRemoteAddr();
			}
		}
		return ip;
	}

}
