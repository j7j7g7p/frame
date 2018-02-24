package com.base.web;

import java.io.File;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;

public class LogUtils {
	protected static Logger log = Logger.getLogger(LogUtils.class);

	public static void getLogFile() {
		DailyRollingFileAppender appender = (DailyRollingFileAppender) log
				.getRootLogger().getAppender("file");
		File file = new File(appender.getFile());
		SimpleDateFormat df = new SimpleDateFormat(appender.getDatePattern());
		String d = df.format(DateUtils.now());
		log.debug("日志文件:" + file.getAbsolutePath() + " " + d);
	}

	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("http_client_ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		// 如果是多级代理，那么取第一个ip为客户ip
		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
		}
		return ip;
	}

	public static String getServerIp(HttpServletRequest request) {
		return request.getLocalAddr();
	}

	public static int getServerPort(HttpServletRequest request) {
		return request.getServerPort();
	}

	public static void log(HttpServletRequest httpReq, ParaMap inMap) {
		try {
			String lo = inMap.getString("lo");
			String ll = inMap.getString("ll");
			String u = inMap.getString("u");
			String clientIP = getClientIp(httpReq);
			String serverIp = getServerIp(httpReq);
			int serverPort = getServerPort(httpReq);
			// if (StringUtils.isNotEmpty(lo)) {
			// OperateLogDao.writeInfo(ll, lo, null, null, null, serverIp,
			// serverPort, clientIP, inMap.toString(), u);
			// }

		} catch (Exception ex) {

		}
	}
}
