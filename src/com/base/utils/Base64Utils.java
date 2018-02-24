package com.base.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class Base64Utils {
	public static byte[] encode(byte[] buf) {
		return Base64.encodeBase64(buf);
	}

	public static byte[] decode(byte[] buf) {
		return Base64.decodeBase64(buf);
	}

	public static String encode(String in) {
		return Base64.encodeBase64String(in.getBytes());
	}

	public static String decode(String in) {
		return StringUtils.newStringUtf8(Base64.decodeBase64(in));
	}

}
