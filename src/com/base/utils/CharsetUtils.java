package com.base.utils;

public class CharsetUtils {
	public final static String utf = "UTF-8";
	public final static String gbk = "GBK";
	public final static String iso = "ISO-8859-1";
	public final static String gb2312 = "GB2312";

	/**
	 * 判断字符串的编码
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = gb2312;
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = iso;
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = utf;
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = gbk;
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "";
	}

	public static String getString(String tempStr) {
		try {
			String encoding = getEncoding(tempStr);
			return new String(tempStr.getBytes(encoding));
		} catch (Exception ex) {
		}
		return tempStr;
	}

	public static void main(String[] args) throws Exception {
		String name = new String("李光荣中文");

		String name1 = new String(name.getBytes(iso));

		String name2 = new String(name1.getBytes(iso));

		System.out.println(name1);
		System.out.println(name2);
		System.out.println(name1.equals(name2));

		System.out.println(getEncoding(name1));//gb2312

	}
}
