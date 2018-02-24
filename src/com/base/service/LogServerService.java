package com.base.service;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.web.AppConfig;

public class LogServerService extends BaseService {

	public ParaMap write(ParaMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		String content = new String(inMap.getBytes("content"));
		appendToFile(content);
		return outMap;
	}

	public ParaMap log(ParaMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		String catalog = inMap.getString("catalog");
		String content = inMap.getString("content");
		String filePath = getFilePath(catalog);
		File file = new File(filePath);
		if (!file.exists())
			FileUtils.writeStringToFile(file, "");
		FileWriter fw = new FileWriter(filePath, true);
		fw.write(content + "\n");
		fw.close();
		return outMap;
	}

	public void appendToFile(String content) throws Exception {
		JSONObject json = JSONObject.parseObject(content);
		String catalog = json.getString("catalog");
		String frameworkLogEnable = AppConfig.getStringPro(
				"frameworkLogEnable", "false");
		if ((catalog.startsWith("framework"))
				&& (!"true".equals(frameworkLogEnable.toLowerCase())))
			return;
		String filePath = getFilePath(catalog);
		File file = new File(filePath);
		if (!file.exists())
			FileUtils.writeStringToFile(file, "");
		FileWriter fw = new FileWriter(filePath, true);
		fw.write(formatContent(json) + "\n");
		fw.close();

	}

	public String formatContent(JSONObject json) {
		StringBuffer logs = new StringBuffer();
		// 2016-05-03 11:52:02,255
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss,SSS");
		logs.append(df.format(new Date(json.getLong("ts"))));
		logs.append(" " + json.getString("rid"));
		logs.append(" [" + json.getString("rpath") + ":"
				+ json.getString("rline") + "]");
		logs.append(" " + json.getString("level"));
		logs.append(" " + json.getString("content"));
		// System.out.println(logs);
		return logs.toString();
	}

	public String getFilePath(String catalog) {
		String fileRoot = AppConfig.getStringPro("fileRoot");
		if (StrUtils.isNull(fileRoot))
			fileRoot = "/app/fileRoot/logs";
		DateFormat df = new SimpleDateFormat("YYYYMMdd");
		String ymd = df.format(new Date());
		String file = fileRoot + "/logs/" + ymd + "/" + catalog + ".txt";
		return file;
	}

	public static void main(String[] args) throws Exception {
		LogServerService log = new LogServerService();
		//
		ParaMap map = new ParaMap();
		map.put("rid", System.currentTimeMillis());
		map.put("level", "INFO");
		map.put("rpath", "com.safdaf.dbdbd.method");
		map.put("rline", 100);
		map.put("content", "...AAA...");
		map.put("catalog", "trade");
		map.put("ts", System.currentTimeMillis());
		//
		log.appendToFile(map.toString());

	}
}
