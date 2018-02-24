package com.base.utils;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class UnsafeUtils {

	public static Unsafe getUnsafe() {
		Unsafe unsafe = null;
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe"); // Internal
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(Unsafe.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return unsafe;
	}

	public static String systemInfo() {
		String s = "";
		Runtime rt = Runtime.getRuntime();
		long totalMemory = rt.totalMemory();
		long freeMemory = rt.freeMemory();
		long maxMemory = rt.maxMemory();
		long processors = rt.availableProcessors();
		s += "processors:" + processors + " ";
		s += "totalMemory[已用内存]:" + totalMemory + " ";
		s += "maxMemory[最大内存]:" + maxMemory + " ";
		s += "freeMemory[可用内存]:" + freeMemory + " ";
		return s;
	}

	public static String pretty(byte[] ba) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ba.length; i++) {
			String b = Integer.toHexString(ba[i] & 0xFF);
			if (b.length() == 1)
				b = "0" + b;
			sb.append(b + " ");
		}
		return sb.toString().trim().toUpperCase();
	}

	public static String p(byte b) {
		String b1 = Integer.toHexString(b & 0xFF);
		if (b1.length() == 1)
			b1 = "0" + b1;
		return b1;
	}
}
