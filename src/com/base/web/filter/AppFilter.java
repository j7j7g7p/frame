package com.base.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.ds.DataSourceManager;
import com.base.log.Logging;
import com.base.mq.MQReceUtils;
import com.base.utils.CharsetUtils;
import com.base.utils.StrUtils;
import com.base.web.AppConfig;
import com.base.web.AppInit;

public class AppFilter implements Filter {
	static Logging log = Logging.getLogging("framework-exception");
	public static AppInit app;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		httpRes.setHeader("Access-Control-Allow-Origin", "*");
		httpReq.setCharacterEncoding(CharsetUtils.utf);
		httpRes.setCharacterEncoding(CharsetUtils.utf);
		String uri = httpReq.getServletPath();
		try {
			if (uri.startsWith("/data")) {
				DataFilter.doFilter(httpReq, httpRes, filterChain);
			} else if (uri.indexOf("/base/") >= 0) {
				ResourceFilter.doFilter(httpReq, httpRes, filterChain);
			} else if (uri.endsWith(".js")) {
				ResourceFilter.jsFilter(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/weixin")) {
				WeixinFilter.action(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/direct")) {
				DirectFilter.redirect(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/redirect")) {
				ReDirectFilter.redirect(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/image")) {
				IOClientFilter.viewImage(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/downClient")) {
				IOClientFilter.down(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/dClient")) {
				IOClientFilter.upload(httpReq, httpRes, filterChain);
			} else if (uri.startsWith("/upClient")) {
				IOClientFilter.upload(httpReq, httpRes, filterChain);
			} else {
				filterChain.doFilter(httpReq, httpRes);
			}
			DataSourceManager.commit();
		} catch (Exception ex) {
			log.info(uri + " " + ex.getMessage());
			ex.printStackTrace();
			DataSourceManager.rollback();
		} finally {
			DataSourceManager.printState();
		}
	}

	public void destroy() {
		log.info("destroy...");
		MQReceUtils.destroy();
	}

	public void init(FilterConfig arg0) throws ServletException {
		try {
			String className = AppConfig.getStringPro("appInit");
			if (StrUtils.isNotNull(className)) {
				app = (AppInit) Class.forName(className).newInstance();
				app.init();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
