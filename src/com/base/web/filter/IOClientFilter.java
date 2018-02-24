package com.base.web.filter;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.service.BaseService;
import com.base.utils.CharsetUtils;
import com.base.utils.ParaMap;
import com.base.utils.StreamUtils;
import com.base.web.AccessCheck;
import com.base.web.ContentTypes;

public class IOClientFilter extends BaseFilter {

	/**
	 * @param httpReq
	 * @param httpRes
	 * @param filterChain
	 * @throws Exception
	 */
	public static void upload(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		ParaMap outMap = new ParaMap();
		String clientType = "";
		try {
			ParaMap inMap = parseRequestPart(httpReq);
			AccessCheck.checkUV(inMap);
			if (inMap.containsKey("clientType"))
				clientType = inMap.getString("clientType").toLowerCase();
			String clazz = "com." + inMap.getString("module") + ".service."
					+ inMap.getString("service") + "Service";
			BaseService service = (BaseService) Class.forName(clazz)
					.newInstance();
			service.setRequest(httpReq);
			service.setSession(httpReq.getSession());
			service.setResponse(httpRes);
			Method m = service.getClass().getMethod(inMap.getString("method"),
					ParaMap.class);
			outMap = (ParaMap) m.invoke(service, inMap);
			outMap.put("state", 1);
		} catch (Exception ex) {
			outMap = new ParaMap();
			outMap.put("state", 0);
			String errorMessage = null;
			if (ex instanceof InvocationTargetException) {
				Throwable targetEx = ((InvocationTargetException) ex)
						.getTargetException();
				errorMessage = targetEx.getMessage();
			} else {
				errorMessage = ex.getMessage();
			}
			outMap.put("message", errorMessage);
			throw ex;
		} finally {
			PrintWriter pw = httpRes.getWriter();
			String json = outMap.toMd5String();
			if ("html".equals(clientType)) {
				InputStream ins = IOClientFilter.class
						.getResourceAsStream("uploadTemplate.html");
				String html = StreamUtils.InputStreamToString(ins);
				json = html.replaceFirst("jsonstring", json);
			}
			pw.write(json);
			pw.flush();
		}

	}

	/**
	 * 下载文件
	 * 
	 * @param httpReq
	 * @param httpRes
	 * @param filterChain
	 * @throws Exception
	 */
	public static void down(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		httpRes.setContentType("text/html;charset=" + CharsetUtils.utf);
		httpRes.setCharacterEncoding(CharsetUtils.utf);
		httpReq.setCharacterEncoding(CharsetUtils.utf);
		ParaMap inMap = parseRequestPart(httpReq);
		String clazz = "com." + inMap.getString("module") + ".service."
				+ inMap.getString("service") + "Service";
		BaseService service = (BaseService) Class.forName(clazz).newInstance();
		service.setRequest(httpReq);
		service.setSession(httpReq.getSession());
		service.setResponse(httpRes);
		Method m = service.getClass().getMethod(inMap.getString("method"),
				ParaMap.class);
		byte[] buf = (byte[]) m.invoke(service, inMap);
		write(httpReq, httpRes, buf);
	}

	/**
	 * 查看图片
	 * 
	 * @param httpReq
	 * @param httpRes
	 * @param filterChain
	 * @throws Exception
	 */
	public static void viewImage(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		httpRes.setContentType("text/html;charset=" + CharsetUtils.utf);
		httpRes.setCharacterEncoding(CharsetUtils.utf);
		httpReq.setCharacterEncoding(CharsetUtils.utf);
		ParaMap inMap = parseRequestPart(httpReq);
		String clazz = "com." + inMap.getString("module") + ".service."
				+ inMap.getString("service") + "Service";
		BaseService service = (BaseService) Class.forName(clazz).newInstance();
		service.setRequest(httpReq);
		service.setSession(httpReq.getSession());
		service.setResponse(httpRes);
		Method m = service.getClass().getMethod(inMap.getString("method"),
				ParaMap.class);
		byte[] buf = (byte[]) m.invoke(service, inMap);
		String contentType = ContentTypes.getContentType("jpg");
		httpRes.setContentType(contentType + ";charset=" + CharsetUtils.utf);
		write(httpReq, httpRes, buf);
	}

	public static void main(String[] args) throws Exception {
		String path = IOClientFilter.class.getPackage().getName()
				.replaceAll("\\.", "\\/");
		InputStream ins = IOClientFilter.class
				.getResourceAsStream("uploadTemplate.html");
		String html = StreamUtils.InputStreamToString(ins);
		System.out.println(html);
	}
}
