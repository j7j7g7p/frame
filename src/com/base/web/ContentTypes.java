package com.base.web;

import java.util.HashMap;

public class ContentTypes {
	static HashMap<String, String> typeMap = new HashMap<String, String>();
	static {
		typeMap.put("bmp", "image/bmp");
		typeMap.put("gif", "image/gif");
		typeMap.put("jpg", "image/jpeg");
		typeMap.put("png", "image/png");
		typeMap.put("swf", "application/x-shockwave-flash");
		typeMap.put("ppt", "application/mspowerpoint");
		typeMap.put("pdf", "application/pdf");
		typeMap.put("doc", "application/msword");
		typeMap.put("docx", "application/msword");
		typeMap.put("xls", "application/vnd.ms-excel");
		typeMap.put("xlsx", "application/vnd.ms-excel");
		typeMap.put("zip", "application/zip");
		typeMap.put("txt", "text/plain");
		typeMap.put("lic", "application/octet-stream");
		// typeMap.put("js", "application/x-javascript");
		typeMap.put("js", "text/javascript");
		typeMap.put("css", "text/css");
	}

	public static String getContentType(String extName) {
		return typeMap.get(extName.toLowerCase());
	}

}
