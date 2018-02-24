package com.base.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;

public class HolderDataSetDao extends BaseDataSetDao {
	public String parseHolder(String s1, ParaMap map) throws Exception {
		StringBuffer s2 = new StringBuffer();
		boolean keyword = false;
		StringBuffer s3 = new StringBuffer();
		for (int i = 0; i < s1.length(); i++) {
			char c = s1.charAt(i);
			if (c == ':') {
				keyword = true;
				s3 = new StringBuffer();
				continue;
			}
			if (keyword) {
				if (c == ',' || c == ' ' || c == ')') {
					String holderKey = s3.toString();
					if (!map.containsKey(holderKey))
						throw new Exception(holderKey + " has not value!");
					String holderValue = map.getString(holderKey);
					s2.append(holderValue);
					keyword = false;
				} else
					s3.append(c);
			}
			if (!keyword)
				s2.append(c);

		}
		return s2.toString();
	}

	public HashMap<Integer, String> parseHolder(String s1) throws Exception {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		boolean keyword = false;
		StringBuffer s3 = new StringBuffer();
		Integer keyIndex = 0;
		for (int i = 0; i < s1.length(); i++) {
			char c = s1.charAt(i);
			if (c == ':') {
				keyword = true;
				s3 = new StringBuffer();
				continue;
			}
			if (keyword) {
				if (c == ',' || c == ' ' || c == ')') {
					String holderKey = s3.toString();
					map.put(keyIndex++, holderKey);
					keyword = false;
				} else
					s3.append(c);
			}

		}
		return map;
	}

	public PreparedStatement setObject(String newSql, SQLMap inMap)
			throws Exception {
		Connection conn = getCon();
		PreparedStatement pst = conn.prepareStatement(newSql);
		HashMap<Integer, String> holderMap = parseHolder(newSql);
		Iterator<Integer> vit = holderMap.keySet().iterator();
		while (vit.hasNext()) {
			Integer index = vit.next();
			String name = holderMap.get(index);
			Object value = inMap.get(name);
			pst.setObject(index, value);
		}
		return pst;

	}

	public ParaMap query(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		String sql = inMap.getFinalSql();
		PreparedStatement pst = setObject(sql, inMap);
		ResultSet rs = pst.executeQuery();
		outMap = convert(rs, true);
		pst.close();
		return outMap;
	}

	public ParaMap insert(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		String sql = inMap.getFinalSql();

		int bracket1Begin = sql.indexOf("(");
		int bracket1End = sql.indexOf(")", bracket1Begin);

		int bracket2Begin = sql.indexOf("(", bracket1End);
		int bracket2End = sql.lastIndexOf(")");

		String insertKey = sql.substring(bracket1Begin, bracket1End);
		String insertKey2 = insertKey + ",createtime,updatetime";

		String insertValue = sql.substring(bracket2Begin, bracket2End);
		long now = DateUtils.nowTime();
		String insertValue2 = insertValue + "," + now + "," + now;

		String newSql = sql.substring(0, bracket1Begin) + insertKey2;
		newSql += sql.substring(bracket1End, bracket2Begin);
		newSql += insertValue2;
		newSql += sql.substring(bracket2End);

		PreparedStatement pst = setObject(newSql, inMap);
		int num = pst.executeUpdate();
		pst.close();
		outMap.put("num", num);

		return outMap;

	}

	public ParaMap updateAtom(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		String version = inMap.getVersion();
		if (version == null)
			throw new Exception("updateAtom 没有版本号");
		String sql = inMap.getFinalSql();
		int whereIndex = sql.indexOf("where");
		long now = DateUtils.nowTime();
		String newSql = sql.substring(0, whereIndex) + ",updatetime=" + now
				+ " " + sql.substring(whereIndex);
		if (StrUtils.isNull(version))
			newSql += " and isnull(updatetime) =1";
		else
			newSql += " and updatetime=" + version;

		PreparedStatement pst = setObject(newSql, inMap);
		int num = pst.executeUpdate();
		pst.close();
		if (num != 1)
			throw new Exception("updateAtom 更新不成功:" + newSql);
		outMap.put("num", num);
		return outMap;

	}

}
