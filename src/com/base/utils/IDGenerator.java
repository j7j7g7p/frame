package com.base.utils;

import java.util.Random;

public class IDGenerator {

	public final static int idlen = 32;
	static Random random;
	static {
		random = new Random(System.currentTimeMillis());
	}

	public synchronized static String newGUID() {
		String dateStr = DateUtils.nowStrYYYYMMddHHmmssSSS();
		String randomStr = Math.abs(random.nextLong()) + "";
		String s = dateStr + randomStr;
		int len = idlen - s.length();
		for (int i = 0; i < len; i++)
			s += "0";
		if (s.length() > idlen)
			s = s.substring(0, idlen);
		return s;
	}

	public synchronized static String newNo(String prefix) {
		return prefix + newGUID().substring(prefix.length());
	}

	public static void main(String[] args) {
		String no1 = newGUID();
		String no2 = newNo("MQ");

		System.out.println(no1);
		System.out.println(no1.length());
		System.out.println(no2);
		System.out.println(no2.length());
	}

}
