package com.base.bigdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.base.ds.DataSourceManager;
import com.base.utils.DebugUtils;

public class Partition implements Callable<List<Record>> {
	public String sql;
	public List column = new ArrayList();
	public Object[][] data;
	public SQLParser parser;

	public Partition(String partitionSql, String sortStr, String fieldStr,
			int pageSize) {
		parser = new SQLParser(partitionSql, sortStr, fieldStr, pageSize);

	}

	public List<Record> toList(int sortIndex) {
		List<Record> list = new ArrayList<Record>();
		for (int row = 0; data != null && row < data.length; row++) {
			list.add(new Record(this, row, sortIndex));
		}
		return list;
	}

	@Override
	public List<Record> call() throws Exception {
		DebugUtils.thread_init();
		loadIdsData();
		List<Record> list = toList(parser.getSortIndex());
		return list;
	}

	public void loadIdsData() {
		try {
			DebugUtils.thread_init();
			Connection conn = DataSourceManager.getConnection();
			String sql = parser.sql();
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			rs = pst.executeQuery();
			int colCount = rs.getMetaData().getColumnCount();
			rs.last();
			int rowCount = rs.getRow();
			if (rowCount > 0) {
				data = new Object[rowCount][colCount];
				for (int i = 1; i <= colCount; i++) {
					ResultSetMetaData rmd = rs.getMetaData();
					String name = rmd.getColumnLabel(i).toLowerCase();
					column.add(name);
				}
				rs.first();
				int row = 0;
				do {
					int col = 1;
					for (; col <= colCount; col++) {
						data[row][col - 1] = rs.getObject(col);
					}
					row++;
				} while (rs.next());
			}
			DebugUtils.thread_elapse(rowCount + "\n" + sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DataSourceManager.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
