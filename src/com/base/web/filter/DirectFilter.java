package com.base.web.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.service.BaseService;
import com.base.utils.CharsetUtils;
import com.base.utils.DateUtils;
import com.base.utils.ParaMap;

public class DirectFilter extends BaseFilter {

	public static List<String> msmList = new ArrayList<String>();

	public static void redirect(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		try {
			httpRes.setContentType("text/html;charset=" + CharsetUtils.utf);
			httpRes.setCharacterEncoding(CharsetUtils.utf);
			httpReq.setCharacterEncoding(CharsetUtils.utf);
			String uri = httpReq.getServletPath();
			String paraStr = uri.substring(uri.lastIndexOf("/direct")
					+ "/direct".length() + 1);
			String[] paraPair = paraStr.split("_");
			ParaMap inMap = new ParaMap();
			for (String paraStr1 : paraPair) {
				String[] para = paraStr1.split("=");
				String key = para[0];
				if (para.length == 2) {
					String value = para[1];
					inMap.put(key, value);
				} else {
					inMap.put(key, null);
				}
			}
			check(inMap);
			String clazz = "com." + inMap.getString("module") + ".service."
					+ inMap.getString("service") + "Service";
			BaseService service = (BaseService) Class.forName(clazz)
					.newInstance();
			service.setRequest(httpReq);
			service.setSession(httpReq.getSession());
			service.setResponse(httpRes);
			Method m = service.getClass().getMethod(inMap.getString("method"),
					ParaMap.class);
			m.invoke(service, inMap);
		} catch (Exception ex) {
			try {
				ParaMap outMap = new ParaMap();
				outMap.put("state", 0x3);
				outMap.put("ts", DateUtils.nowTime());
				String errorMessage = null;
				if (ex instanceof InvocationTargetException) {
					Throwable targetEx = ((InvocationTargetException) ex)
							.getTargetException();
					errorMessage = targetEx.getMessage();
				} else {
					errorMessage = ex.getMessage();
				}
				outMap.put("msg", errorMessage);
				String json = outMap.toMd5String();
				write(httpReq, httpRes, json.getBytes());
			} finally {
				throw ex;
			}
		}
	}

	public static void check(ParaMap inMap) throws Exception {
		String s = inMap.getString("module") + "_" + inMap.getString("service")
				+ "_" + inMap.getString("method");
		boolean b = msmList.contains(s);
		if (!b)
			throw new Exception(s + " not grant right to access!");
	}

	public static void main(String[] args) {
		String uri = "/demo/redirect-module_alipay__service_Notify__method_notifyListener";
		uri = "redirect-module_comp__service_CooperateLogin__method_getCompidByComname__comname_sjhy_0__sign_B0ACDE2787EA0270823CAF03809A7D89";
		String paraStr = uri.substring(uri.lastIndexOf("/redirect")
				+ "/redirect".length() + 1);
		String[] paraPair = paraStr.split("__");
		for (String paraStr1 : paraPair) {
			String[] para = paraStr1.split("_");
			String key = para[0];
			String value = para[1];
			System.out.println(key + ":" + value);
		}

	}

}
