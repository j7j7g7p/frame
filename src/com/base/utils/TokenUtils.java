package com.base.utils;

public class TokenUtils {

	public static String getToken(String u, String t1) {
		long te = Long.parseLong(t1.substring(t1.length() - 18));
		int ue = getCode(u);
		long currTime = System.currentTimeMillis() % (long) Math.pow(10, 6);
		int ran = (int) (Math.random() * 80000000 + currTime + 1);
		long tail = (te % ran) ^ ue;
		return format(currTime, 6) + format(ran, 8) + format(tail, 8);
	}

	private static String format(Number i, int len) {
		String str = "" + i;
		while (str.length() < len) {
			str = '0' + str;
		}
		return str;
	}

	public static boolean checkToken(String u, String t1, String t2) {
		try {
			int t2Code = Integer.parseInt(t2.substring(14));
			String tEnd = t1.substring(4);
			long te = Long.parseLong(tEnd);
			int ue = getCode(u);

			String ranStr = t2.substring(6, 14);
			int ran = Integer.parseInt(ranStr);

			int tail = (int) (te % ran) ^ ue;
			return (t2Code ^ tail) == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	private static int getCode(String u) {
		while (u.length() < 16) {
			u = u.concat(u);
		}
		char[] value = u.toCharArray();
		int h = value[0] | value[1];
		int s2 = value.length - 1;
		int s1 = s2 - 16;
		for (int i = s2; i > s1; i--) {
			h = (h << 1) + value[i];
		}
		return h;
	}

	public static void main(String[] args) {
		String t1 = TokenUtils.getToken("00", "12345678901234567890");
		System.out.println(t1);
		String u = "201506121711177424937asfdadsfa4";
		String su = "c69a3ad7f5f011e4b907000c29a0f9fff";

		long ss = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			String t = getToken(su, t1);
			// System.out.println((System.nanoTime() - ss) / Math.pow(10, 6) +
			// "ms");

			ss = System.nanoTime();
			boolean b = checkToken(su, t1, t);
			// System.out.println((System.nanoTime() - ss) / Math.pow(10, 6) +
			// "ms");

			System.out.println(t + " " + b);
		}
	}
}
