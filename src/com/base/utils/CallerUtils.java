package com.base.utils;

public class CallerUtils {

	public static String getCallStack() {
		Throwable ex = new Throwable();
		StackTraceElement[] stackElements = ex.getStackTrace();
		StringBuffer sb = new StringBuffer();
		sb.append("[" + DateUtils.nowLocal() + "]");
		for (int i = stackElements.length - 1; i >= 1; i--) {
			String info = stackElements[i].toString();
			if (!info.startsWith("com."))
				continue;
			sb.append(info);
			if (i > 1)
				sb.append(" -> ");
		}
		return sb.toString();

	}
}
