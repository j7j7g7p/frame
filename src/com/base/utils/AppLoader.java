package com.base.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.base.web.AppConfig;
import org.apache.log4j.Logger;

public class AppLoader extends ClassLoader {
	private static final Logger log = Logger.getLogger(AppLoader.class);
	public String libs;
	public static Hashtable<String, Long> timeTable = new Hashtable();
	public static Hashtable<String, Object> objTable = new Hashtable();

	public AppLoader(String libs) {
		super(Thread.currentThread().getContextClassLoader());
		this.libs = libs;

	}

	private synchronized void initJar(File file) throws Exception {
		JarFile jarFile = new JarFile(file);
		URL[] urls = new URL[1];
		urls[0] = file.toURI().toURL();
		URLClassLoader ul = new URLClassLoader(urls, Thread.currentThread()
				.getContextClassLoader());
		timeTable.put(file.getName(), file.lastModified());
		log.info("initjar:" + file.getName());
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry jarEntry = entrys.nextElement();
			if (jarEntry.getName().endsWith(".class")) {
				String clazz = jarEntry.getName().replaceAll("/", ".")
						.replaceFirst(".class", "");
				log.info(file.getName()+"-"+clazz);
				Class cla = ul.loadClass(clazz);
				if (clazz.indexOf("$") < 0) {
					Object obj = cla.newInstance();
					objTable.put(clazz, obj);
				}
			}
		}
		ul.close();
		jarFile.close();
	}

	// public Class loadClass(String name) throws ClassNotFoundException {
	// System.out.println("loadClass:" + name);
	// Class clazz = clazzTable.get(name);
	// return clazz;
	// }

	private String company(String clazz) {
		int endIndex = clazz.indexOf(".");
		return clazz.substring(0, endIndex);
	}

	public Object getObj(String clazz) throws Exception {
		if (AppConfig.getBooleanPro("companyLoaderLocal")) {
			Class c = Class.forName(clazz);
			return c.newInstance();
		} else {
			String company = company(clazz);
			File filec = new File(libs + "/" + company + ".jar");
			long modify = filec.lastModified();
			if (!timeTable.containsKey(filec.getName())
					|| modify != timeTable.get(filec.getName()))
				initJar(filec);
		}
		Object obj = objTable.get(clazz);
		return obj;

	}

}
