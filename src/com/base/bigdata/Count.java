package com.base.bigdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

import com.base.ds.DataSourceManager;

public class Count implements Callable<Long> {

	private String sql;

	public Count(String sql) {
		this.sql = sql;
	}

	public Long call() throws Exception {
		long cc = 0;
		try {
			Connection conn = DataSourceManager.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			rs.first();
			cc = rs.getLong(1);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DataSourceManager.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return cc;
	}

}
