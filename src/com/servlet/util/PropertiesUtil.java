package com.servlet.util;

import java.io.IOException;
import java.util.Properties;

/**
 * ��ȡproperties�����ļ��Ĺ��߷���
 * 
 * @author ken
 *
 */
public class PropertiesUtil {

	private static Properties properties = new Properties();

	static {
		try {
			properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("jdbc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperties(String key) {
		return properties.getProperty(key, null);
	}

}
