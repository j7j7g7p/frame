package com.base.bigdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.base.ds.DataSourceManager;

public class Record {
	public Partition p;
	public int xIndex;
	public int yIndex;

	public Record(Partition p, int xIndex, int yIndex) {
		this.p = p;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
	}

	public Long getV() {
		Object o = p.data[xIndex][yIndex];
		String v = String.valueOf(o);
		return Long.parseLong(v);
	}

	public List data() {
		List list = new ArrayList();
		String[] sqls = p.parser.loadDataSql();
		for (int i = 0; i < sqls.length; i++) {
			List sub = loadData(sqls[i]);
			list.addAll(sub);
		}
		return list;
	}

	public List loadData(String sql) {
		List list = new ArrayList();
		try {
			Connection conn = DataSourceManager.getConnection();
			int idIndex = p.parser.idIndex(sql);
			Object id = p.data[xIndex][idIndex];
			sql = sql.replaceFirst("\\?", "\'" + String.valueOf(id) + "\'");
			System.out.println(sql);
			PreparedStatement pst = conn.prepareStatement(sql);
			//			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			int colCount = rs.getMetaData().getColumnCount();
			rs.next();
			for (int i = 1; i <= colCount; i++) {
				list.add(rs.getObject(i));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DataSourceManager.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return list;
		}
	}

}
