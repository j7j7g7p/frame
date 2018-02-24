package com.base.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;

import com.base.utils.StreamUtils;

public class ResourceUtils {
	public static HashMap<String, byte[]> resByteMap = new HashMap<String, byte[]>();
	public static HashMap<String, String> resStringMap = new HashMap<String, String>();

	public static String getString(ServletContext sc, String key)
			throws Exception {
		String versionMode = AppConfig.getStringPro("versionMode");
		String buf = resStringMap.get(key);
		if (buf == null) {
			InputStream in = sc.getResourceAsStream(key);
			buf = StreamUtils.InputStreamToString(in);
			if (!"debug".equals(versionMode))
				resStringMap.put(key, buf);
			in.close();
		}
		return buf;
	}

	public static byte[] getBytes(ServletContext sc, String key)
			throws Exception {
		String versionMode = AppConfig.getStringPro("versionMode");
		byte[] buf = resByteMap.get(key);
		if (buf == null) {
			InputStream in = sc.getResourceAsStream(key);
			buf = StreamUtils.InputStreamToByte(in);
			if (!"debug".equals(versionMode))
				resByteMap.put(key, buf);
			in.close();
		}
		return buf;
	}

	public static byte[] getBytes(String fileName) throws Exception {
		String versionMode = AppConfig.getStringPro("versionMode");
		byte[] buf = resByteMap.get(fileName);
		if (buf == null) {
			FileInputStream fin = new FileInputStream(fileName);
			buf = StreamUtils.InputStreamToByte(fin);
			if (!"debug".equals(versionMode))
				resByteMap.put(fileName, buf);
			fin.close();
		}
		return buf;
	}

	public static long getModified(ServletContext sc, String key) {
		long l = 0;
		try {
			String path = sc.getRealPath(key);
			l = (new File(path)).lastModified();
			l = l / 1000 * 1000;
		} catch (Exception ex) {
		}
		return l;
	}

	public static void main(String[] args) throws IOException {
		File f = new File("D:/fileroot/test.jpg");
		System.out.println(f.lastModified());

	}
}
