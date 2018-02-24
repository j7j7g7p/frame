package com.base.bigdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.base.dao.BaseDao;
import com.base.utils.ParaMap;
import com.base.ds.DataSourceManager;

public class SQLUtils {
	public static ParaMap columnInfo(String sql) {
		ParaMap outMap = new ParaMap();
		try {
			Connection conn = DataSourceManager.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			BaseDao dao = new BaseDao();
			outMap = dao.convert(rs, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DataSourceManager.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return outMap;
	}

}
