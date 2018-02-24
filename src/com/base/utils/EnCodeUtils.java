package com.base.utils;

import java.net.URLEncoder;

public class EnCodeUtils {

	public static String urlEncode(String s) throws Exception {
		return URLEncoder.encode(s, "UTF-8");
	}

	public static String encode(String s) throws Exception {
		String s1 = urlEncode(s);
		s1 = s1.replaceAll("\\+", "%20");
		s1 = s1.replaceAll(urlEncode("("), "(");
		s1 = s1.replaceAll(urlEncode(")"), ")");
		s1 = s1.replaceAll(urlEncode("!"), "!");
		s1 = s1.replaceAll(urlEncode("="), "=");
		return s1;
	}

	public static void main(String[] args) throws Exception {
		String ss = "(=!  )";
		System.out.println(urlEncode(ss));
		System.out.println(encode(ss));
	}

}
