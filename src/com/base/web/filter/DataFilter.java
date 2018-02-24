package com.base.web.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.base.dt.DTClient;
import com.base.log.Logging;
import com.base.service.BaseService;
import com.base.utils.CacheUtils;
import com.base.utils.CharsetUtils;
import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.web.AccessCheck;
import com.base.web.LogUtils;

public class DataFilter extends BaseFilter {
	static Logging loger = Logging.getLogging("framework");

	public static void doFilter(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		String json = null;
		String clientType = null;
		String callback = null;
		String u = null;
		String ut = null;
		ParaMap inMap = null;
		try {
			httpRes.setContentType("text/html;charset=" + CharsetUtils.utf);
			httpRes.setCharacterEncoding(CharsetUtils.utf);
			httpReq.setCharacterEncoding(CharsetUtils.utf);
			inMap = parseRequestPure(httpReq);
			clientType = inMap.getString("clientType");
			callback = inMap.getString("callback");
			u = inMap.getString("u");
			inMap.remove("callback");
			inMap.remove("_");
			String module = inMap.getString("module");
			if (!"base".equals(module))
				AccessCheck.checkUTV(inMap);
			LogUtils.log(httpReq, inMap);
			BaseService service = getServiceInstance(inMap);
			service.setRequest(httpReq);
			service.setSession(httpReq.getSession());
			service.setResponse(httpRes);
			Method m = getMethod(inMap);
			String methodStr = getMethodStr(inMap);
			loger.setName("framework/" + methodStr);
			loger.info(inMap.toString());
			DTClient.validDt(inMap);
			ParaMap outMap = (ParaMap) m.invoke(service, inMap);
			outMap.put("ts", DateUtils.nowTime());
			if (!"base".equals(module))
				ut = AccessCheck.addToken(inMap, outMap);
			// if (outMap != null)
			// LobUtils.query(outMap);
			//
			json = outMap.toMd5String();
		} catch (Exception ex) {
			String errMsg = ex.getMessage();
			loger.info(errMsg);
			ParaMap outMap = new ParaMap();
			outMap.put("state", 0);
			outMap.put("ts", DateUtils.nowTime());
			if (AccessCheck.msg6.equals(errMsg)) {
				ut = CacheUtils.get(AccessCheck.getT(u));
				outMap.put("t", ut);
				json = outMap.toMd5String();
			} else {
				ut = null;
				String errorMessage = null;
				if (ex instanceof InvocationTargetException) {
					Throwable targetEx = ((InvocationTargetException) ex)
							.getTargetException();
					errorMessage = targetEx.getMessage();
				} else {
					errorMessage = ex.getMessage();
				}
				// 对errorMessage进行JSON解析
				try {
					JSONObject jsonObj = JSON.parseObject(errorMessage);
					outMap.putAll(jsonObj);
				} catch (Exception ex2) {
					outMap.put("message", errorMessage);
				}
				//
				json = outMap.toMd5String();
				throw ex;
			}
		} finally {
			if ("html".equals(clientType) && StrUtils.isNotNull(callback))
				json = callback + "(" + json + ")";
			try {
				json = json.replaceAll("script>", "s c r i p t>");
				String methodStr = getMethodStr(inMap);
				loger.setName("framework/" + methodStr);
				loger.info(json);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			write(httpReq, httpRes, json.getBytes());
			if (StrUtils.isNotNull(u) && StrUtils.isNotNull(ut))
				CacheUtils.set(AccessCheck.getT(u), ut);
		}

	}
}
