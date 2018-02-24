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

public class BaseDataSetDao extends BaseDao {
	public static final String InMap_SQL = "SQL";
	public static final String InMap_Param = "Param";
	public static final String InMap_BatchParam = "BatchParam";
	public static final String InMap_UpdatetimeParam = "updatetime";

	public ParaMap query(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		Connection conn = getCon();
		String sql = inMap.getFinalSql();
		PreparedStatement pst = conn.prepareStatement(sql);
		List list = inMap.getList(InMap_Param);
		for (int i = 0; i < list.size(); i++) {
			pst.setObject(i + 1, list.get(i));
		}
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

		long now = DateUtils.nowTime();
		String insertValue = sql.substring(bracket2Begin, bracket2End);
		String insertValue2 = insertValue + "," + now + "," + now;

		String newSql = sql.substring(0, bracket1Begin) + insertKey2;
		newSql += sql.substring(bracket1End, bracket2Begin);
		newSql += insertValue2;
		newSql += sql.substring(bracket2End);

		Connection conn = getCon();
		log.info(newSql);
		PreparedStatement pst = conn.prepareStatement(newSql);
		List list = inMap.getList(InMap_Param);
		for (int i = 0; i < list.size(); i++) {
			pst.setObject(i + 1, list.get(i));
		}
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
		log.info(newSql);
		Connection conn = getCon();
		PreparedStatement pst = conn.prepareStatement(newSql);
		List list = inMap.getList(InMap_Param);
		for (int i = 0; i < list.size(); i++) {
			pst.setObject(i + 1, list.get(i));
		}
		int num = pst.executeUpdate();
		pst.close();
		if (num != 1)
			throw new Exception("updateAtom 更新不成功:" + newSql);
		outMap.put("num", num);
		return outMap;

	}

	public ParaMap queryForPage(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		int pageSize = inMap.getInt("pageSize");
		int pageNo = inMap.getInt("pageIndex");
		String sql = inMap.getFinalSql();
		String sql1 = sql + " LIMIT " + (pageNo - 1) * pageSize + " , "
				+ pageSize;
		inMap.put(InMap_SQL, sql1);
		outMap = query(inMap);
		String sql2 = "SELECT COUNT(*) AS count1 FROM ( " + sql + " ) AS t1";
		inMap.put(InMap_SQL, sql2);
		ParaMap outMap2 = query(inMap);
		int count = outMap2.getRecordInt(0, 0);
		outMap.put("totalCount", count);
		outMap.put("pageSize", pageSize);
		outMap.put("pageIndex", pageNo);
		return outMap;
	}

	public ParaMap update(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		Connection conn = getCon();
		String sql = inMap.getFinalSql();
		PreparedStatement pst = conn.prepareStatement(sql);
		List list = inMap.getList(InMap_Param);
		for (int i = 0; i < list.size(); i++) {
			pst.setObject(i + 1, list.get(i));
		}
		int num = pst.executeUpdate();
		pst.close();
		outMap.put("num", num);
		return outMap;
	}

	public ParaMap batch(SQLMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		Connection conn = getCon();
		String sql = inMap.getFinalSql();
		PreparedStatement pst = conn.prepareStatement(sql);
		List<List> pairList = inMap.getList(InMap_BatchParam);
		for (List pair : pairList) {
			for (int i = 0; i < pair.size(); i++) {
				pst.setObject(i + 1, pair.get(i));
			}
			pst.addBatch();
		}
		int[] res = pst.executeBatch();
		outMap.put("res", res);
		pst.close();
		return outMap;
	}

	public static void main(String[] args) throws Exception {
		BaseDataSetDao dao = new BaseDataSetDao();
		String sql = "update blc_balance set balancestatus = 2, receiveuserid = ?, receivedate = ?	where isvalid = 1 and id = ?";
		sql = "update blc_balance set balancestatus = 2, receiveuserid = ?, receivedate = ?	where isvalid = 1 and id = ?";
		sql = "SELECT * FROM $table0 where id!=?";
		SQLMap sqlMap = new SQLMap();
		sqlMap.setSQL(sql);
		sqlMap.setPlaceHolder("$table0", "t1");
		// List list1 = new ArrayList();
		// list1.add("name1");
		// list1.add("city1");
		// list1.add("phone1");
		// List list2 = new ArrayList();
		// list2.add("name1");
		// list2.add("city1");
		// list2.add("phone1");
		// sqlMap.addBatchParam(list1).addBatchParam(list2);
		// ParaMap outMap = dao.batch(sqlMap);
		// System.out.println(outMap);
		// dao.insert(sqlMap);
		sqlMap.setVersion("sfasdfas");

		dao.updateAtom(sqlMap);

		// sql = sql.replaceAll("\\$table0", "t1");
		// System.out.println(sql);

	}
}
