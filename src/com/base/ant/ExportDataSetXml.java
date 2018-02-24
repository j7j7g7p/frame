package com.base.ant;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ExportDataSetXml {
	public static void main(String[] args) throws Exception {
		String url = "jdbc:oracle:thin:@192.168.5.249:1521:SLREEC";
		String userName = "transaction_guangzhou";
		String passWord = "transaction_guangzhou";
		String outputFile = "release/web/WEB-INF/classes/ds.xml";
		if (args.length == 4) {
			url = args[0];
			userName = args[1];
			passWord = args[2];
			outputFile = args[3];
		}
		//
		System.out.println("开始导出数据集...");
		System.out.println("数据库配置:" + url);
		System.out.println("用户名:" + userName);
		System.out.println("密码:" + passWord);
		System.out.println("输出文件:" + outputFile);
		// 测试配置
		Class.forName("oracle.jdbc.driver.OracleDriver");// 加入oracle的驱动，
		Connection con = DriverManager.getConnection(url, userName, passWord);
		HashMap map = new HashMap();
		map.put("syncModuleData", "1");
		map.put("syncDataSetData", "1");
		map.put("syncResourceData", "0");
		ExportDataSetXml e = new ExportDataSetXml();
		String xml = e.getSyncDataXml(con, map);
		FileUtils.writeStringToFile(new File(outputFile), xml);
		System.out.println("成功导出数据集...");
	}

	public String getSyncDataXml(Connection con, HashMap params)
			throws Exception {
		String syncModuleId = (String) params.get("syncModuleId");
		String syncDataSetId = (String) params.get("syncDataSetId");
		String syncResourceId = (String) params.get("syncResourceId");
		boolean syncModuleData = params.get("syncModuleData").equals("1");
		boolean syncDataSetData = params.get("syncDataSetData").equals("1");
		boolean syncResourceData = params.get("syncResourceData").equals("1");
		if (!(syncModuleData || syncDataSetData || syncResourceData)) {// 未选择任何同步的数据
			return null;
		}
		String sqlModule = null;
		String sqlDataSet = null;
		String sqlResource = null;
		HashMap sqlModuleParams = null;
		HashMap sqlDataSetParams = null;
		HashMap sqlResourceParams = null;
		if (isNotNull(syncModuleId) || isNotNull(syncDataSetId)
				|| isNotNull(syncResourceId)) {
			// 按选择节点
			if (isNotNull(syncModuleId)) {
				// 模块所有下级节点以及上级链路上的父节点
				sqlModule = "select * from ("
						+ "select sys_module.*, '0_' || (1000 - level) as module_level from sys_module CONNECT BY id = PRIOR parent_id start with id = '"
						+ syncModuleId
						+ "'"
						+ " union select sys_module.*, '1_' || level as module_level from sys_module CONNECT BY parent_id = PRIOR id start with parent_id = '"
						+ syncModuleId + "'" + ") order by module_level, turn";
				sqlDataSet = "select * from sys_dataset where module_id = '"
						+ syncModuleId
						+ "' or module_id in (select id from sys_module CONNECT BY parent_id = PRIOR id start with parent_id = '"
						+ syncModuleId + "')";
				sqlResource = "select * from sys_resource where module_id = '"
						+ syncModuleId
						+ "' or module_id in (select id from sys_module CONNECT BY parent_id = PRIOR id start with parent_id = '"
						+ syncModuleId + "')";
				sqlModuleParams = new HashMap();
			} else if (isNotNull(syncDataSetId)) {
				sqlModule = "select * from ("
						+ "select sys_module.*, level as module_level from sys_module CONNECT BY id = PRIOR parent_id start with id in (select module_id from sys_dataset where id = '"
						+ syncDataSetId + "')"
						+ ") order by module_level desc, turn";
				sqlDataSet = "select * from sys_dataset where id = '"
						+ syncDataSetId + "'";
			} else if (isNotNull(syncResourceId)) {
				sqlModule = "select * from ("
						+ "select sys_module.*, level as module_level from sys_module CONNECT BY id = PRIOR parent_id start with id in (select module_id from sys_resource where id = '"
						+ syncResourceId + "')"
						+ ") order by module_level desc, turn";
				sqlResource = "select * from sys_resource where id = '"
						+ syncResourceId + "'";
			}
		} else {
			sqlModule = "select sys_module.*, level as module_level from sys_module CONNECT BY parent_id = PRIOR id start with parent_id is null"
					+ " order by module_level, turn";
			sqlDataSet = "select * from sys_dataset";
			sqlResource = "select * from sys_resource";
		}
		HashMap moduleData = queryData(sqlModule, con);
		HashMap dataSetData = syncDataSetData && isNotNull(sqlDataSet) ? queryData(
				sqlDataSet, con) : null;
		HashMap resourceData = syncResourceData && isNotNull(sqlResource) ? queryData(
				sqlResource, con) : null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("utf-8");
		Element rootElement = document.addElement("datas");
		String nowDateSql = "select sysdate nowdate from dual";
		String nowDate = (String) getRecordValue(queryData(nowDateSql, con), 0,
				"nowdate");
		rootElement.addAttribute("sync_date", nowDate);
		// 导出模块
		Element modulesElement = rootElement.addElement("modules");
		modulesElement.addAttribute("sync_module", syncModuleData ? "1" : "0");
		modulesElement
				.addAttribute("sync_dataset", syncDataSetData ? "1" : "0");
		modulesElement.addAttribute("sync_resource", syncResourceData ? "1"
				: "0");
		for (int i = 0; i < ((List) moduleData.get("rs")).size(); i++) {
			String moduleId = String
					.valueOf(getRecordValue(moduleData, i, "id"));
			Element moduleElement = modulesElement.addElement("module");
			moduleElement.addAttribute("id", moduleId);
			moduleElement.addAttribute("no",
					String.valueOf(getRecordValue(moduleData, i, "no")));
			moduleElement.addAttribute("name",
					String.valueOf(getRecordValue(moduleData, i, "name")));
			// moduleElement.addAttribute("menu_id",
			// String.valueOf(moduleData.getRecordValue(i, "menu_id")));//菜单无法导入
			moduleElement
					.addAttribute("class_name", String.valueOf(getRecordValue(
							moduleData, i, "class_name")));
			moduleElement.addAttribute("parent_id",
					String.valueOf(getRecordValue(moduleData, i, "parent_id")));
			moduleElement.addAttribute("is_valid",
					String.valueOf(getRecordValue(moduleData, i, "is_valid")));
			moduleElement.addAttribute("remark",
					String.valueOf(getRecordValue(moduleData, i, "remark")));
			if (dataSetData != null) {
				Element dataSetsElement = moduleElement.addElement("dataSets");
				for (int j = 0; j < ((List) dataSetData.get("rs")).size(); j++) {
					if (String.valueOf(
							getRecordValue(dataSetData, j, "module_id"))
							.equalsIgnoreCase(moduleId)) {
						// Element dataSetElement =
						// dataSetsElement.addElement("dataSet");
						Element dataSetElement = addTextElement(
								dataSetsElement, "dataSet",
								String.valueOf(getRecordValue(dataSetData, j,
										"sql")), true);
						dataSetElement.addAttribute("id", String
								.valueOf(getRecordValue(dataSetData, j, "id")));
						dataSetElement.addAttribute("no", String
								.valueOf(getRecordValue(dataSetData, j, "no")));
						dataSetElement.addAttribute("name",
								String.valueOf(getRecordValue(dataSetData, j,
										"name")));
						dataSetElement.addAttribute("module_id", String
								.valueOf(getRecordValue(dataSetData, j,
										"module_id")));
						// dataSetElement.addAttribute("sql",
						// String.valueOf(dataSetData.getRecordValue(j,
						// "sql")));
						dataSetElement.addAttribute("is_valid", String
								.valueOf(getRecordValue(dataSetData, j,
										"is_valid")));
						dataSetElement.addAttribute("remark", String
								.valueOf(getRecordValue(dataSetData, j,
										"remark")));
					}
				}
			}
			if (resourceData != null) {
				Element resourcesElement = moduleElement
						.addElement("resources");
				for (int j = 0; j < ((List) resourceData.get("rs")).size(); j++) {
					if (String.valueOf(
							getRecordValue(resourceData, j, "module_id"))
							.equalsIgnoreCase(moduleId)) {
						// Element resourceElement =
						// resourcesElement.addElement("resource");
						Element resourceElement = addTextElement(
								resourcesElement, "resource",
								String.valueOf(getRecordValue(resourceData, j,
										"content")), true);
						resourceElement
								.addAttribute("id", String
										.valueOf(getRecordValue(resourceData,
												j, "id")));
						resourceElement
								.addAttribute("no", String
										.valueOf(getRecordValue(resourceData,
												j, "no")));
						resourceElement.addAttribute("name",
								String.valueOf(getRecordValue(resourceData, j,
										"name")));
						resourceElement.addAttribute("module_id", String
								.valueOf(getRecordValue(resourceData, j,
										"module_id")));
						// resourceElement.addAttribute("content",
						// String.valueOf(resourceData.getRecordValue(j,
						// "content")));
						resourceElement.addAttribute("is_valid", String
								.valueOf(getRecordValue(resourceData, j,
										"is_valid")));
						resourceElement.addAttribute("remark", String
								.valueOf(getRecordValue(resourceData, j,
										"remark")));
					}
				}
			}
		}
		System.out.println(nowDate + "导出数据集XML");
		return document.asXML();
	}

	private Element addTextElement(Element parent, String name, String text,
			boolean writeCData) {
		Element result = parent.addElement(name);
		if (text != null && !text.equals("") && !text.equalsIgnoreCase("null")) {
			if (writeCData) {
				// DefaultCDATA cdata = new DefaultCDATA(text);
				// result.add(cdata);
				result.addCDATA(text);
			} else
				result.setText(text);
		}
		return result;
	}

	private Element addTextElement(Element parent, String name, String text) {
		return addTextElement(parent, name, text, false);
	}

	private HashMap queryData(String sql, Connection con) {
		Statement stmt = null;
		ResultSet rs = null;
		HashMap result = new HashMap();
		List fields = new ArrayList();
		List records = new ArrayList();
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();//
			int count = rsmd.getColumnCount();
			for (int i = 1; i <= count; i++) {
				fields.add(rsmd.getColumnName(i).toLowerCase());//
			}
			while (rs.next()) {
				List record = new ArrayList();
				for (int i = 1; i <= count; i++) {
					record.add(getValue(rs, i));
				}
				records.add(record);
			}
			result.put("fs", fields);
			result.put("rs", records);
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public Object getRecordValue(HashMap hashmap, int recordNo, String fieldName) {
		List fs = (List) hashmap.get("fs");
		int index = fs.indexOf(fieldName);
		if (index >= 0) {
			List record = (List) ((List) hashmap.get("rs")).get(recordNo);
			return record.get(index);
		} else
			return null;
	}

	public Object getValue(ResultSet rs, int col) throws SQLException {
		Object obj = rs.getObject(col);
		if (obj != null) {
			int type = rs.getMetaData().getColumnType(col);
			switch (type) {
			case Types.BIT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
			case Types.NUMERIC:
			case Types.DECIMAL:
				double d = rs.getDouble(col);
				try {
					int i = (new Double(d)).intValue();
					BigDecimal b1 = new BigDecimal(d);
					BigDecimal b2 = new BigDecimal(i);
					int compare = b1.compareTo(b2);
					if (compare == 0)
						obj = i;
					else
						obj = d;
				} catch (Exception ex) {
					obj = d;
				}
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				obj = rs.getString(col);
				break;
			case Types.DATE:
			case Types.TIME:
				Timestamp s1 = rs.getTimestamp(col);
				Date d1 = new Date(s1.getTime());
				obj = getStr(d1);
				break;
			case Types.TIMESTAMP:
				Timestamp s2 = rs.getTimestamp(col);
				obj = s2.toString();
				break;
			}
		}
		return obj;
	}

	public static String getStr(Date date) {
		if (date == null)
			return null;
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(date);
		}
	}

	public static boolean isNull(String s) {
		return s == null || s.equals("") || s.equalsIgnoreCase("null")
				|| s.equalsIgnoreCase("undefined");
	}

	public static boolean isNotNull(String s) {
		return !isNull(s);
	}
}
