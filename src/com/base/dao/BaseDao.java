package com.base.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import org.apache.log4j.Logger;

import com.base.ds.DataSourceManager;

/**
 * 
 */
public class BaseDao {
	protected Logger log = Logger.getLogger(this.getClass());

	public Connection getCon() {
		return DataSourceManager.getConnection();
	}

	public ParaMap convert(ResultSet rs, boolean moreMeta) throws SQLException {
		ParaMap pm = new ParaMap();
		List fields = new ArrayList();
		List fieldList = moreMeta ? new ArrayList() : null;
		List records = new ArrayList();
		int colCount = rs.getMetaData().getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			ResultSetMetaData rmd = rs.getMetaData();
			String name = rmd.getColumnLabel(i).toLowerCase();
			fields.add(name);
			if (moreMeta) {
				ParaMap field = new ParaMap();
				field.put("name", name);
				field.put("size", rmd.getColumnDisplaySize(i));
				field.put("precision", rmd.getPrecision(i));
				field.put("scale", rmd.getScale(i));
				field.put("type", rmd.getColumnType(i));
				String typeName = rmd.getColumnTypeName(i);
				if (name.startsWith("lob_")
						&& typeName.equalsIgnoreCase("varchar2"))
					field.put("typeName", "blob");
				else
					field.put("typeName", typeName);
				fieldList.add(field);
			}
		}
		while (rs.next()) {
			List record = new ArrayList();
			for (int i = 1; i <= colCount; i++) {
				record.add(getValue(rs, i));
			}
			records.add(record);
		}
		pm.put("fs", fields);
		if (fieldList != null)
			pm.put("fs", fieldList);
		pm.put("rs", records);
		return pm;
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
				obj = DateUtils.getStr(d1);
				break;
			case Types.TIMESTAMP:
				Timestamp s2 = rs.getTimestamp(col);
				obj = s2.toString();
				break;
			}
		}
		return obj;
	}

}
