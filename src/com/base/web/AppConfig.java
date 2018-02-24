package com.base.web;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.base.utils.ParaMap;

public class AppConfig {
	private static ParaMap map;
	private static final Logger log = Logger.getLogger(AppConfig.class);

	public static synchronized void init() {
		if (map != null)
			return;
		try {
			map = new ParaMap();
			InputStream in = AppConfig.class
					.getResourceAsStream("/appConfig.properties");
			Properties appConfig = new Properties();
			appConfig.load(in);
			Iterator it = appConfig.keySet().iterator();
			while (it.hasNext()) {
				String key = String.valueOf(it.next()).trim();
				String value = appConfig.getProperty(key).trim();
				map.put(key, value);
			}
			if (map.containsKey("driverClassName"))
				Class.forName(map.getString("driverClassName"));
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	public static boolean hasPro(String key) {
		init();
		if (key == null)
			return false;
		else
			return map.containsKey(key);
	}

	public static boolean getBooleanPro(String key) {
		String value = getStringPro(key);
		if ("true".equals(value))
			return true;
		else
			return false;
	}

	public static String getStringPro(String key) {
		if (hasPro(key))
			return map.getString(key);
		else
			return null;
	}

	public static String getStringPro(String key, String defaultValue) {
		if (hasPro(key))
			return map.getString(key);
		else
			return defaultValue;
	}

	public static Integer getIntPro(String key) {
		if (hasPro(key))
			return map.getInteger(key);
		else
			return null;
	}

	public static ParaMap getAll() {
		init();
		return new ParaMap(map);
	}

	public static File getFile(String path) {
		init();
		String fileRoot = map.getString("fileRoot");
		File file = new File(fileRoot + "/" + path);
		return file;
	}

	public static boolean release() {
		init();
		String version = map.getString("versionMode").toLowerCase();
		if ("release".equals(version))
			return true;
		else
			return false;
	}

	public static boolean localCache() {
		init();
		String localCache = map.getString("localCache").toLowerCase();
		if ("true".equals(localCache))
			return true;
		else
			return false;
	}
}
