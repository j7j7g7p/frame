package com.base.web.filter;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.log.ReqUtils;
import com.base.security.ApiUtils;
import com.base.service.BaseService;
import com.base.utils.ParaMap;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.base.utils.CharsetUtils;
import com.base.utils.StreamUtils;

public abstract class BaseFilter {
	public static List<String> mainHtmlList = new ArrayList<String>();
	public static List<String> passHtmlList = new ArrayList<String>();
	public static Hashtable<String, BaseService> serviceMap = new Hashtable<String, BaseService>();
	public static Hashtable<String, Method> methodMap = new Hashtable<String, Method>();
	public static String jsonStr = "jsonStr";

	public static synchronized BaseService getServiceInstance(ParaMap inMap)
			throws Exception {
		String clazz = "com." + inMap.getString("module") + ".service."
				+ inMap.getString("service") + "Service";
		boolean hasClazz = serviceMap.containsKey(clazz);
		if (!hasClazz) {
			BaseService service = (BaseService) Class.forName(clazz)
					.newInstance();
			serviceMap.put(clazz, service);
		}
		return serviceMap.get(clazz);
	}

	public static synchronized Method getMethod(ParaMap inMap) throws Exception {
		String module = inMap.getString("module");
		String service = inMap.getString("service");
		String method = inMap.getString("method");
		String mergeStr = module + "_" + service + "_" + method;
		boolean hasMethod = methodMap.containsKey(mergeStr);
		if (!hasMethod) {
			Class clazz = getServiceInstance(inMap).getClass();
			Method m = clazz
					.getMethod(inMap.getString("method"), ParaMap.class);
			methodMap.put(mergeStr, m);
		}
		return methodMap.get(mergeStr);
	}
	
	
	public static synchronized String getMethodStr(ParaMap inMap){
		String module = inMap.getString("module");
		String service = inMap.getString("service");
		String method = inMap.getString("method");
		String mergeStr = module + "_" + service + "_" + method;
		return mergeStr;
	}

	public static ParaMap parseRequestPure(HttpServletRequest httpReq)
			throws Exception {
		ParaMap outMap = new ParaMap();
		// 取参数
		Enumeration it = httpReq.getParameterNames();
		boolean outer = "true".equals(httpReq.getParameter("outer"));
		while (it.hasMoreElements()) {
			String key = String.valueOf(it.nextElement());
			String value = httpReq.getParameter(key);
			if (CharsetUtils.getEncoding(value).equals(CharsetUtils.iso)) {
				if (outer)
					value = CharsetUtils.getString(value);
				else
					value = new String(value.getBytes(CharsetUtils.iso),
							CharsetUtils.utf);

			}
			value = value.replaceAll("%", "%25");
			value = value.replaceAll("\\+", "%2B");
			String value1 = java.net.URLDecoder.decode(value, "UTF-8");
			// sbBuffer.append(":key:" + key + ":value:" + value1);
			outMap.put(key, value1);
		}
		ApiUtils.initSecretKey(outMap);
		ReqUtils.initRId(outMap);
		return outMap;
	}

	public static ParaMap parseRequestPart(HttpServletRequest httpReq)
			throws Exception {
		ParaMap outMap = new ParaMap();
		// 取二进制数据
		boolean isMultipart = ServletFileUpload.isMultipartContent(httpReq);
		if (isMultipart) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List items = upload.parseRequest(httpReq);
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);
				if (!item.isFormField()) {
					outMap.put(item.getFieldName(), item.get());
				} else {
					outMap.put(item.getFieldName(), item.getString());
				}
			}
		} else {
			byte[] buf = StreamUtils
					.InputStreamToByte(httpReq.getInputStream());
			outMap.put("content", buf);
		}
		// 取参数
		Enumeration it = httpReq.getParameterNames();
		boolean outer = "true".equals(httpReq.getParameter("outer"));
		while (it.hasMoreElements()) {
			String key = String.valueOf(it.nextElement());
			String value = httpReq.getParameter(key);
			if (CharsetUtils.getEncoding(value).equals(CharsetUtils.iso)) {
				if (outer)
					value = CharsetUtils.getString(value);
				else
					value = new String(value.getBytes(CharsetUtils.iso),
							CharsetUtils.utf);

			}
			value = value.replaceAll("%", "%25");
			String value1 = java.net.URLDecoder.decode(value, "UTF-8");
			// sbBuffer.append(":key:" + key + ":value:" + value1);
			outMap.put(key, value1);
		}
		
		ApiUtils.initSecretKey(outMap);
		ReqUtils.initRId(outMap);
		return outMap;
	}

	public static void write(HttpServletRequest httpReq,
			HttpServletResponse httpRes, byte[] buf) throws Exception {
		String acceptEncoding = httpReq.getHeader("accept-encoding");
		ServletOutputStream out = httpRes.getOutputStream();
		if (acceptEncoding != null && acceptEncoding.indexOf("gzip") >= 0) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			httpRes.setHeader("Content-Encoding", "gzip");
			GZIPOutputStream gzip = new GZIPOutputStream(bout);
			gzip.write(buf);
			gzip.close();
			byte[] gzipBuf = bout.toByteArray();
			httpRes.setHeader("Content-Length", gzipBuf.length + "");
			out.write(gzipBuf);
			// System.out.println("yes gzip:" + buff.length + "");
		} else {
			httpRes.setHeader("Content-Length", buf.length + "");
			// System.out.println("no gzip:" + buf.length + "");
			out.write(buf);
		}
		out.flush();
		out.close();
	}

	public static void main(String[] args) throws Exception {
		String s1 = "http%3A%2F%2Fitunes.apple.com%2Fcn%2Fapp%2Fjellies%21%2Fid853087982%3Fmt%3D8";
		String s2 = java.net.URLDecoder.decode(s1);
		System.out.println(s2);

	}

}
