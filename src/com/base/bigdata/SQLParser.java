package com.base.bigdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.base.utils.StrUtils;

public class SQLParser {

	private Map<String, String> tableMap = new HashMap<String, String>();
	private Map<String, String> fieldMap = new HashMap<String, String>();
	private List<String> idList = new ArrayList<String>();

	private String sql;
	private String sortStr;
	private String fieldStr;
	private int pageCount;

	private int sortIndex = -1;
	private boolean desc = true;

	public SQLParser(String sql, String sortStr, String fieldStr, int pageCount) {
		this.sql = sql;
		this.sortStr = sortStr;
		this.fieldStr = fieldStr;
		this.pageCount = pageCount;
		//解析sortsql
		String sortField = StrUtils.getSubStr(sortStr, "order by", "desc")
				.trim();
		if (sortField == null) {
			sortField = StrUtils.getSubStr(sortStr, "order by", "asc").trim();
			desc = false;
		}
		//解析select
		String queryStr = StrUtils.getSubStr(sql, "select", "from");
		String[] fs = queryStr.split(",");
		for (int i = 0; i < fs.length; i++) {
			String segment = fs[i];
			String[] arr = segment.split("as");
			String f0 = arr[0].trim();
			String f1 = arr[1].trim();
			if (f0.equals(sortField) || f1.equals(sortField))
				sortIndex = i;
			fieldMap.put(f1, f0);
			idList.add(f0);
		}
		//解析from
		String fromstr = StrUtils.getSubStr(sql, "from", "where");
		String[] tablearr = null;
		if (fromstr.indexOf("left join") > 0) {
			fromstr = StrUtils.getSubStr(sql, "from", "on");
			tablearr = fromstr.split("left join");
		} else if (fromstr.indexOf(",") > 0)
			tablearr = fromstr.split(",");
		else
			tablearr = new String[] { fromstr };
		for (int i = 0; tablearr != null && i < tablearr.length; i++) {
			String[] arr = tablearr[i].split("as");
			tableMap.put(arr[1].trim(), arr[0].trim());
		}

	}

	public String sql() {
		return sql + " " + sortStr + " limit 0," + pageCount;
	}

	public String getTableName(String alias) {
		if (tableMap.containsKey(alias))
			return tableMap.get(alias);
		else
			return null;
	}

	public String getFieldName(String alias) {
		if (fieldMap.containsKey(alias))
			return fieldMap.get(alias);
		else
			return null;
	}

	public int getSortIndex() {
		return this.sortIndex;
	}

	/**
	 * 
	 * @param exp  "t1[name as ,age as ,addr as],t2[sex,salary]"
	 * @param id "fasdfasdf"
	 * @return
	 */
	public String[] loadDataSql1() {
		String[] fieldArr = fieldStr.split(";");
		String[] arr = new String[fieldArr.length];
		for (int i = 0; i < fieldArr.length; i++) {
			String exp = fieldArr[i];
			int index = exp.indexOf("[");
			String tableName = exp.substring(0, index);
			String[] fields = StrUtils.getSubStr(exp, "[", "]").split(",");
			StringBuffer sb = new StringBuffer("select ");
			for (int j = 0; j < fields.length; j++) {
				sb.append(tableName + "." + fields[j]);
				if (j < fields.length - 1)
					sb.append(",");
			}
			sb.append(" from " + tableMap.get(tableName) + " as " + tableName);
			sb.append(" where " + tableName + ".id=?");
			arr[i] = sb.toString();
		}
		return arr;
	}

	public String[] loadDataSql() {
		String[] arr = new String[] { fieldStr };
		Iterator<String> keyIt = tableMap.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			String value = tableMap.get(key);
			arr[0] = arr[0].replaceAll("\\$" + key, value);
		}
		return arr;
	}

	public int idIndex(String loaddatasql) {
		String idname = StrUtils.getSubStr(loaddatasql, "where", "=").trim();
		return idList.indexOf(idname);

	}

	public String columnDesc() {
		String[] arr = new String[] { fieldStr };
		Iterator<String> keyIt = tableMap.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			String value = tableMap.get(key);
			arr[0] = arr[0].replaceAll("\\$" + key, value);
		}
		arr[0] = arr[0].replaceAll("\\?", "'0'");
		return arr[0];
	}

	public String columnDesc1() {
		StringBuffer sb = new StringBuffer("select ");
		String[] fieldArr = fieldStr.split(";");
		for (int i = 0; i < fieldArr.length; i++) {
			String exp = fieldArr[i];
			int index = exp.indexOf("[");
			String tableName = exp.substring(0, index);
			String realName = tableMap.get(tableName);
			String[] fields = StrUtils.getSubStr(exp, "[", "]").split(",");
			for (int j = 0; j < fields.length; j++) {
				sb.append(tableName + "." + fields[j]);
				if (j < fields.length - 1)
					sb.append(",");
			}
			if (i < fieldArr.length - 1)
				sb.append(",");
		}
		String fromstr = StrUtils.getSubStr(sql, "from", "where");
		sb.append(" from " + fromstr + " where id='0'");
		return sb.toString();
	}

	public String count() {
		StringBuffer sb = new StringBuffer("select count(1) as cc from");
		sb.append(StrUtils.getSubStr(sql, "from"));
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		String sql = "select t1.id as id1,t2.id as id2 from table1 as t1 left join table2 as t2 on t1.id=t2.refid where 1!=0";
		String sortstr = "order by t1.time desc";
		String fieldStr = "t1[name as name1,age as age1];t2[sex as sex1,salary as salary1]";
		SQLParser parser = new SQLParser(sql, sortstr, fieldStr, 10);
		System.out.println(parser.columnDesc());
		System.out.println(parser.count());
		//		String[] sqlarr = parser.loadDataSql();
		//		for (int i = 0; i < sqlarr.length; i++)
		//			System.out.println(sqlarr[i]);

	}
}
