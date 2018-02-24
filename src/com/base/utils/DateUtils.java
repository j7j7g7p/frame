package com.base.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DateUtils {
	public static String sub(Date thisTime, Date endTime) {
		long l1 = thisTime.getTime() / 1000;
		long l2 = endTime.getTime() / 1000;
		long ll = l2 - l1;
		long dd = ll / 24 / 60 / 60;
		long hh = (ll - dd * 24 * 60 * 60) / (60 * 60);
		long mm = (ll - dd * 24 * 60 * 60 - hh * 60 * 60) / 60;
		long ss = (ll - dd * 24 * 60 * 60 - hh * 60 * 60 - mm * 60);
		String ddStr = dd == 0 ? "" : dd + "天 ";
		String hhStr = hh == 0 ? "" : hh + "小时 ";
		String mmStr = mm == 0 ? "" : mm + "分钟 ";
		String ssStr = ss == 0 ? "" : ss + "秒";
		String result = ddStr + hhStr + mmStr + ssStr;
		return result;
	}

	public static int getProcess(Date beginTime, Date endTime) {
		long l1 = beginTime.getTime() / 1000;
		long l2 = endTime.getTime() / 1000;
		long now = (DateUtils.now()).getTime() / 1000;
		long d1 = l2 - now;
		long d2 = l2 - l1;
		Double d = d1 * 100.0 / d2;
		int dd = 100 - d.intValue();
		if (dd > 100) {
			dd = 100;
		}
		if (dd < 0) {
			dd = 0;
		}
		return dd;
	}

	public static String getStr(Date date) {
		if (date == null)
			return null;
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(date);
		}
	}

	public static String getStr(long delay) {
		long now = (DateUtils.now()).getTime();
		Date d = new Date(now + delay);
		return getStr(d);
	}

	public static String nowStr() {
		return getStr(DateUtils.now());
	}

	public static String nowStrYYYYMMddHHmmssSSS() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String s1 = sdf.format(DateUtils.now());
		return s1;
	}

	public static String nowStr2() {
		return getStr(new Date());
	}

	public static long nowTime() {
		Date d1 = DateUtils.now();
		long now = d1.getTime();
		return now;
	}

	public static Date now() {
		return new Date();
	}

	public static Date getDate(String date) {
		Date d1 = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			d1 = sdf.parse(date);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return d1;
	}

	public static int getInt(double d) {
		return (new Double(d)).intValue();
	}

	public static String uuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static boolean isNull(String in) {
		if (in == null || "".equals(in) || in.equalsIgnoreCase("null")) {
			return true;
		} else {
			return false;
		}
	}

	// 如果为null返回""
	public static String nullToEmpty(String in) {
		if (in == null || "".equals(in) || in.equalsIgnoreCase("null")) {
			return "";
		} else {
			return in;
		}
	}

	// 如果为null返回null
	public static String emptyToNull(String in) {
		if (in == null || "".equals(in) || in.equalsIgnoreCase("null")) {
			return null;
		} else {
			return in;
		}
	}

	/**
	 * 校正为本机时间
	 * 
	 * @param dbDate
	 *            数据库时间
	 * @return
	 */
	public static Date adjust(Date dbDate) {
		long dbTime = nowTime();
		long before = dbDate.getTime();
		//
		long localTime = (new Date()).getTime();
		long after = before + (localTime - dbTime);
		//
		Date d1 = new Date(after);
		return d1;
	}

	public static String nowLocal() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String t = sdf.format(new Date());
		return t;
	}

	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = sdf.parse("2012-03-01 10:00:00");
		Date d2 = sdf.parse("2012-03-01 10:06:00");
		System.out.println(getProcess(d1, d2));
		System.out.println(nowLocal());
	}

}
