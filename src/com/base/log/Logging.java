package com.base.log;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.base.service.LogServerService;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.web.AppConfig;

public class Logging {
	private Logger logger;
	private String name;
	private static Hashtable<String, Logger> loggerMap = new Hashtable<String, Logger>();
	private LogServerService logService = new LogServerService();

	private Logging(String name) {
		this.name = name;
		logger = loggerMap.get(name);
		if (logger == null) {
			logger = Logger.getLogger(name);
			loggerMap.put(name, logger);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Logging getLogging(String name) {
		return new Logging(name);
	}

	public void debug(String message) {
		internal("DEBUG", message);
	}

	public void error(String message) {
		internal("ERROR", message);
	}

	public void fatal(String message) {
		internal("FATAL", message);
	}

	public void info(String message) {
		internal("INFO", message);
	}

	private void internal(String level, String message) {
		String logType = AppConfig.getStringPro("logType");
		if (StrUtils.isNull(logType))
			logType = "log4j";
		StackTraceElement stack[] = Thread.currentThread().getStackTrace();
		ParaMap log = new ParaMap();
		log.put("rid", ReqUtils.getRId());
		log.put("level", level);
		if (stack.length >= 3) {
			log.put("rpath",
					stack[3].getClassName() + "." + stack[3].getMethodName());
			log.put("rline", stack[3].getLineNumber());
		}
		log.put("catalog", name);
		log.put("ts", System.currentTimeMillis());
		log.put("content", message);
		if ("remote".equals(logType)) {
			LogClient.addLog(log);
		} else if ("log4j".equals(logType)) {
			try {
				logService.appendToFile(log.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// logger.info(message);
		} else {
			System.out.println(message);
		}
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Logging loger = Logging.getLogging("orderInfo/泊位号");
		loger.fatal("mmmmmmmmmmmmmm");
		LogClient.start();

		Thread.sleep(Integer.MAX_VALUE);
	}

}
