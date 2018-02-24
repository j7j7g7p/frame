package com.base.web.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.service.BaseService;
import com.base.utils.CharsetUtils;
import com.base.utils.ParaMap;

public class WeixinFilter extends BaseFilter {

	public static List<String> msmList = new ArrayList<String>();

	public static void action(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		httpRes.setContentType("text/html;charset=" + CharsetUtils.utf);
		httpRes.setCharacterEncoding(CharsetUtils.utf);
		httpReq.setCharacterEncoding(CharsetUtils.utf);
		String uri = httpReq.getServletPath();
		//
		ParaMap inMap = parseUri(uri);
		//
		ParaMap inMap2 = parseRequestPure(httpReq);
		inMap.putAll(inMap2);
		//
		String clazz = "com." + inMap.getString("module") + ".service."
				+ inMap.getString("service") + "Service";
		BaseService service = (BaseService) Class.forName(clazz).newInstance();
		service.setRequest(httpReq);
		service.setSession(httpReq.getSession());
		service.setResponse(httpRes);
		Method m = service.getClass().getMethod(inMap.getString("method"),
				ParaMap.class);
		m.invoke(service, inMap);
	}

	public static void check(ParaMap inMap) throws Exception {
		String s = inMap.getString("module") + "_" + inMap.getString("service")
				+ "_" + inMap.getString("method");
		boolean b = msmList.contains(s);
		if (!b)
			throw new Exception(s + " not grant right to access!");
	}

	public static ParaMap parseUri(String uri) {
		String paraStr = uri.substring(uri.lastIndexOf("/weixin")
				+ "/weixin".length() + 1);
		String[] paraPair = paraStr.split("_");
		ParaMap inMap = new ParaMap();
		for (String paraStr1 : paraPair) {
			String[] para = paraStr1.split("=");
			String key = para[0];
			String value = para[1];
			inMap.put(key, value);
		}
		return inMap;
	}

	public static void main(String[] args) throws Exception {
		WeixinFilter filter = new WeixinFilter();
		String uri = "weixin-module=comp_service=CooperateLogin_method=getCompidByComname?name=aaa";
		String value1 = java.net.URLEncoder.encode("#");
		System.out.println(value1);
		//
		ParaMap inMap = filter.parseUri(uri);
		System.out.println(inMap);

	}

}
