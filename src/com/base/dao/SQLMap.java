package com.base.dao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.base.utils.ParaMap;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SQLMap extends ParaMap {
	private String version = "";
	private HashMap<String, String> placeHolderMap = new HashMap<String, String>();

	public SQLMap() {
		put(BaseDataSetDao.InMap_Param, new ArrayList());
		put(BaseDataSetDao.InMap_BatchParam, new ArrayList());
	}

	public void setSQL(String module, String sqlName) throws Exception {
		String sql = getSQL(module, sqlName);
		put(BaseDataSetDao.InMap_SQL, sql);

	}

	public void setPlaceHolder(String placeHolder, String replaceStr) {
		String key = placeHolder;
		if (placeHolder.startsWith("$"))
			key = "\\" + placeHolder;
		placeHolderMap.put(key, replaceStr);
	}

	public void setSQL(String sql) throws Exception {
		put(BaseDataSetDao.InMap_SQL, sql);
	}

	public String getFinalSql() {
		String sql = getString(BaseDataSetDao.InMap_SQL);
		if (placeHolderMap.size() > 0) {
			Iterator<String> it = placeHolderMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = placeHolderMap.get(key);
				sql = sql.replaceAll(key, value);
			}
		}
		return sql;
	}

	public String getSQL(String module, String sqlName) throws Exception {
		InputStream in = SQLMap.class
				.getResourceAsStream("/" + module + ".xml");
		SAXReader reader = new SAXReader();
		Document document = reader.read(in);
		Element rootElement = document.getRootElement();
		StringBuffer xpath = new StringBuffer();
		xpath.append("//sql[@name='" + sqlName + "']");
		List<Element> list = rootElement.selectNodes(xpath.toString());
		if (list.size() == 0)
			throw new Exception(module + ".xml没有" + sqlName);
		if (list.size() > 1)
			throw new Exception(module + ".xml有多余的" + sqlName);
		Element ele = list.get(0);
		String sql = ele.getTextTrim();
		//
		return sql;
	}

	public SQLMap addParam(Object obj) {
		List list = getList(BaseDataSetDao.InMap_Param);
		list.add(obj);
		return this;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public SQLMap addBatchParam(List obj) {
		List list = getList(BaseDataSetDao.InMap_BatchParam);
		list.add(obj);
		return this;
	}
}
