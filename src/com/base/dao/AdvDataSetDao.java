package com.base.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.base.dao.BaseDataSetDao;
import com.base.dao.SQLMap;
import com.base.ds.DataSourceManager;
import com.base.utils.IDGenerator;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;

/**
 * 数据集子类<br>
 * 实现：<br>
 * 1、按参数方式执行sql，避免点位符方式。参数值不够自动null替代；<br>
 *   原方式：select * from sys_bank where no = ?<br>
 *     sqlMap.addParam("0001");<br>
 *     优点：简单，不需要解析sql执行速度快。缺点：参数必须按顺序，加减参数比较麻烦。<br>
 *   新方式：select * from sys_bank where no = :no<br>
 *     sqlMap.put("no", "0001");<br>
 *     优点：简单，参数无顺序，阅读清晰，参数值少了仍然不会失败。缺点：需要解析sql参数，绝对意义上会慢些。<br>
 * 2、可以使用非预定义sql方式，执行insert、update语句；<br>
 * 3、BaseDataSetDao原方法仍然都继续有效；<br>
 * 4、原insert、update、query、queryForPage兼容新旧方式。新增execute方法，可替代insert、update，合二为一。
 * 
 */

public class AdvDataSetDao extends BaseDataSetDao {
	private static final String SQL_EXCLUDE_PARAM_NAMES = "%H;%k;%h;%I;%l;%i;%r;%T;%S;%s"; //需排除的参数，主要是时间格式化
	
	public ParaMap query(SQLMap sqlMap) throws Exception {
		if (sqlMap == null)
			return null;
		convertSQLMap(sqlMap);
		ParaMap result = super.query(sqlMap);
		int rowCount = result.getRecords().size();
		result.put("totalCount", rowCount); //总记录数
		result.put("rowCount", rowCount); //当前返回记录行数，等于totalCount。这里主要是保持同queryForPage方法返回的结构基本一致
		result.put("num", rowCount);
		result.put("status", 1);
		return result;
	}
	
	public ParaMap queryForPage(SQLMap sqlMap) throws Exception {
		if (sqlMap == null)
			return null;
		convertSQLMap(sqlMap);
		//分布查询如果无分页信息，则使用默认信息（第1页，每页10行）
		int pageSize = sqlMap.getInt("pageSize");
		if (pageSize <= 0) {
			pageSize = 10;
			sqlMap.put("pageSize", pageSize);
		}
		int pageIndex = sqlMap.getInt("pageIndex");
		if (pageIndex <= 0) {
			pageIndex = 1;
			sqlMap.put("pageIndex", pageIndex);
		}
		ParaMap result = super.queryForPage(sqlMap);
		int totalCount = result.getInt("totalCount");
		int rowCount = result.getRecords().size();
		result.put("rowCount", rowCount); //当前返回记录行数
		result.put("num", rowCount);
		result.put("pageCount", Math.round(Math.ceil(totalCount / (pageSize + 0.0)))); //总页数
		result.put("status", 1);
		return result;
	}
	
	public ParaMap insert(SQLMap sqlMap) throws Exception {
		if (sqlMap == null)
			return null;
		convertSQLMap(sqlMap);
		ParaMap result = super.insert(sqlMap);
		int rowCount = result.getInt("num");
		result.put("rowCount", rowCount);
		result.put("status", 1);
		return result;
	}
	
	public ParaMap update(SQLMap sqlMap) throws Exception {
		if (sqlMap == null)
			return null;
		convertSQLMap(sqlMap);
		ParaMap result = super.update(sqlMap);
		int rowCount = result.getInt("num");
		result.put("rowCount", rowCount);
		result.put("status", 1);
		return result;
	}
	
	/**
	 * 执行指定SQL。不再需要按父类区分insert、update，但未考虑updatetime字段赋值。可在sql上添加updatetime = UNIX_TIMESTAMP() * 1000
	 * 
	 * @param inMap SQL中的参数值MAP，替换参数请查看convertSQLParams等方法
	 * @return 返回SQL执行情况，status参数为1表示查询成功，0为查询失败；msg参数在失败时写入错误原因
	 * @throws Exception
	 */
	public ParaMap execute(SQLMap sqlMap) throws Exception {
		ParaMap result = new ParaMap();
		convertSQLMap(sqlMap);
		int sqlRowCount = -1;
		PreparedStatement pstm = null;
		try {
			String sql = sqlMap.getFinalSql();
			pstm = getCon().prepareStatement(sql);
			List list = sqlMap.getList("Param");
			for (int i = 0; i < list.size(); ++i) {
				pstm.setObject(i + 1, list.get(i));
			}
			sqlRowCount = pstm.executeUpdate();
			result.put("status", 1);
			result.put("num", sqlRowCount);
			result.put("rowCount", sqlRowCount);
		} catch (SQLException e) {
			result = new ParaMap();
			result.put("status", 0);
			result.put("msg", e.getMessage());
			throw e;
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}
	
	/**
	 * 生成主键值，GUID方式
	 * @return
	 */
	public static String newPrimayKey() {
		return IDGenerator.newGUID();
	}
	
	/**
	 * 	生成主键值，指定前缀方式按日期
	 * @param prefix
	 * @return
	 */
	public static String newPrimayKey(String prefix) {
		return IDGenerator.newNo(prefix);
	}
	
	/**
	 * 返回数据库当前时间
	 * @return
	 */
	public static Date getDBServerDate() {
		Date result = new Date();
		SQLMap sqlMap = new SQLMap();
		try {
			sqlMap.setSQL("select now() as dbdate");
			AdvDataSetDao dataSetDao = new AdvDataSetDao();
			ParaMap data = dataSetDao.query(sqlMap);
			if (data != null && data.getRecordCount() > 0)
				result = data.getRecordDateTime(0, 0);
		} catch (Exception ex) {
		}
		return result;
	}
	
	/**
	 * 判断sql是按参数查询。指不是使用【?】作为占位符方式，而是通过【:name】作为参数的方式
	 * @param sql 待判断sql
	 * @return
	 */
	public static boolean isParamSql(String sql) {
		if (StrUtils.isNull(sql))
			return false;
		boolean result = false;
		boolean inQuoted = false;
		for(int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (c == '\'')
				inQuoted = !inQuoted;
			if (c == ':' && !inQuoted) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 转换sql参数map为占位符方式（即转换为原父类的方式）
	 * @param sqlMap
	 * @return
	 */
	public static boolean convertSQLMap(SQLMap sqlMap) {
		String sql = sqlMap.getString(InMap_SQL);
		boolean result = isParamSql(sql);
		if (result) {
			try {
				Map<Integer, String> sqlParamsPos = new HashMap<Integer, String>();
				String convertSql = AdvDataSetDao.convertSqlParams(sql, sqlParamsPos, null);
				sqlMap.setSQL(convertSql);
				List list = sqlMap.getList(InMap_Param);
				list.clear();
				if (sqlParamsPos != null && sqlParamsPos.size() > 0) {
					for(int i = 0; i < sqlParamsPos.size(); i++) {
						String paramName = sqlParamsPos.get(i);
						list.add(sqlMap.get(paramName));
					}
				}
			} catch (Exception e) {
			}
		}
		return result;
	}
	
	/***********************************************************************************
	 * 下面的方法无需特别关注
	************************************************************************************/
	
	/**
	 * 查询表元数据（实际只是查空表）
	 * @param tableName 表名
	 * @return
	 * @throws Exception
	 */
	protected ParaMap getTableMetaData(String tableName) throws Exception {
		SQLMap sqlMap = new SQLMap();
		sqlMap.setSQL("select * from " + tableName + " where 1 = 2");
		ParaMap result = query(sqlMap);
		return result;
	}
	
	/**
	 * 检查需处理的表数据data中的字段是否是指定表中的字段字段，删除非表中的字段。本方法需访问一次数据库，返回的字段名全部为小写。建议缓存以提高效率
	 * @param checkTableName 需检查的表名，不传表名则仅将所有字段名转换为小写
	 * @param data 需检查的数据MAP，调用格式如：<br/>
	 *            module: sysman <br/>
	 *            service: Module <br/>
	 *            method: updateModule <br/>
	 *            id: 1234567890 <br/>
	 *            name: abcdefg <br/>
	 * @return 清除非指定表名字段后的数据MAP，如：<br/>
	 *         id: 1234567890 <br/>
	 *         name: abcdefg <br/>
	 * @throws Exception
	 */
	protected ParaMap convertDataFieldName(Map data, String checkTableName) throws Exception {
		if (data == null || data.size() == 0)
			return null;
		ParaMap result = new ParaMap();
		if (StrUtils.isNull(checkTableName)) {// 没传入表名则将所有字段名转换为小写
			Iterator it = data.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				result.put(key.toLowerCase(), data.get(key));
			}
		} else {// 否则仅包含指定表的列，列名转换为小写
			ParaMap tableMeta = getTableMetaData(checkTableName);
			List fields = tableMeta.getFields();
			Iterator it = data.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				for(int i = 0; i < fields.size(); i++) {
					ParaMap field = (ParaMap) fields.get(i);
					if (field != null && StrUtils.equalsIgnoreCase(field.getString("name"), key)) {
						result.put(key.toLowerCase(), data.get(key));
						break;
					}
				}
//				if (fields.indexOf(key.toLowerCase()) != -1)
//					result.put(key.toLowerCase(), data.get(key));
			}
		}
		return result;
	}

	/**
	 * 转换字段
	 * 
	 * @param fields 字段列表，以半角逗号“,”或者分号“;”分隔的串
	 * @param checkTableName 表名，有值则表示删除fields中不存在于checkTableName表中的字段。空则仅转换字段为小写
	 * @param allowNotCondition true表示允许出现反条件字段。即字段名前添加“!”
	 * @return
	 * @throws Exception
	 */
	protected List<String> convertFields(String fields, String checkTableName, boolean allowNotCondition) throws Exception {
		if (StrUtils.isNull(fields))
			return null;
		List<String> result = new ArrayList<String>();
		if (fields.indexOf(',') == -1 && fields.indexOf(';') == -1)
			result.add(fields.toLowerCase());
		else {
			StringTokenizer st = null;
			if (fields.indexOf(',') == -1)
				st = new StringTokenizer(fields, ";");
			else
				st = new StringTokenizer(fields, ",");
			while (st.hasMoreTokens())
				result.add(st.nextToken().toLowerCase());
		}
		if (StrUtils.isNotNull(checkTableName)) {
			ParaMap tableMeta = getTableMetaData(checkTableName);
			List columns = tableMeta.getFields();
			List<String> checkFields = new ArrayList<String>();
			for (int i = 0; i < result.size(); i++) {
				String fieldName = result.get(i);
				boolean hasNotCondition = false;
				if (allowNotCondition && fieldName.startsWith("!")) {
					fieldName = fieldName.substring(1);
					hasNotCondition = true;
				}
				if (columns.indexOf(fieldName) != -1)
					if (hasNotCondition)
						checkFields.add("!" + fieldName);
					else
						checkFields.add(fieldName);
			}
			result = checkFields;
		}
		return result;
	}

	protected List<String> convertFields(String fields, String checkTableName) throws Exception {
		return convertFields(fields, checkTableName, false);
	}

	protected List<String> convertFields(String fields) throws Exception {
		return convertFields(fields, null);
	}
	
	/**
	 * 转换sql中所有参数（:name）为占位符（?）
	 * @param sql 待转换sql
	 * @param sqlParams
	 * @return
	 */
	protected static String convertSqlParams(String sql, Map sqlParams) {
		if (sqlParams != null && sqlParams.size() > 0) {
			List paramList = new ArrayList();
			Iterator it = sqlParams.keySet().iterator();
			while (it.hasNext()) {
				paramList.add(it.next().toString());
			}
			// 参数排序
			Collections.sort(paramList, Collator.getInstance());
			for (int i = paramList.size() - 1; i >= 0; i--) {
				String paramName = (String) paramList.get(i);
				String paramValue = String.valueOf(sqlParams.get(paramName));
				if (StrUtils.isNull(paramValue))
					paramValue = "";
				sql = sql.replaceAll("(?u):" + paramName, paramValue);
			}
		}
		return sql;
	}
	
	/**
	 * 转换sql中的查询参数为【？】占位符方式
	 * @param sql 待转换sql
	 * @param sqlParamsPos 返回sql各参数的位置，返回格式如：0:name,1:no,2:bank_type,3:no
	 * @param excludeParamNames 需排除不替换的参数，可以为null
	 * @return 转换后的sql <br>
	 * select * from sys_bank where name = :name --> select * from sys_bank where name = ?
	 */
	protected static String convertSqlParams(String sql, Map<Integer, String> sqlParamsPos, List<String> excludeParamNames) {
		if (sql.indexOf(":") == -1)
			return sql;
		sqlParamsPos.clear();
		ParaMap replaceParams = new ParaMap();
		// 提取所有参数的序
		int paramPos = 0;
		boolean inQuoted = false;
		boolean paramNameStarted = false;
		StringBuilder sbParam = new StringBuilder();
		for(int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (c == '\'')
				inQuoted = !inQuoted;
			if (c == ':' && !inQuoted) {
				paramNameStarted = true;
				String paramName = sbParam.toString();
				if (StrUtils.isNotNull(paramName)) {
					boolean blnStaticExcluded = StrUtils.paramExists(SQL_EXCLUDE_PARAM_NAMES, paramName, true);
					boolean blnExcluded = excludeParamNames != null && excludeParamNames.indexOf(paramName) >= 0;
					if (!blnStaticExcluded && !blnExcluded) {
						if (!replaceParams.containsKey(paramName))
							replaceParams.put(paramName, "?");
						sqlParamsPos.put(paramPos, paramName);
						paramPos++;
					}
				}
				sbParam = new StringBuilder();
			} else if (paramNameStarted) {
				int cAscii = (int) c;
				if ((c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c == 95)
					sbParam.append(c);
				else {
					paramNameStarted = false;
					String paramName = sbParam.toString();
					if (StrUtils.isNotNull(paramName)) {
						boolean blnStaticExcluded = StrUtils.paramExists(SQL_EXCLUDE_PARAM_NAMES, paramName, true);
						boolean blnExcluded = excludeParamNames != null && excludeParamNames.indexOf(paramName) >= 0;
						if (!blnStaticExcluded && !blnExcluded) {
							if (!replaceParams.containsKey(paramName))
								replaceParams.put(paramName, "?");
							sqlParamsPos.put(paramPos, paramName);
							paramPos++;
						}
					}
					sbParam = new StringBuilder();
				}
			}
		}
		String paramName = sbParam.toString();
		if (StrUtils.isNotNull(paramName)) {
			boolean blnStaticExcluded = StrUtils.paramExists(SQL_EXCLUDE_PARAM_NAMES, paramName, true);
			boolean blnExcluded = excludeParamNames != null && excludeParamNames.indexOf(paramName) >= 0;
			if (!blnStaticExcluded && !blnExcluded) {
				if (!replaceParams.containsKey(paramName))
					replaceParams.put(paramName, "?");
				sqlParamsPos.put(paramPos, paramName);
				paramPos++;
			}
		}
		// 替换所有参数为?
		sql = convertSqlParams(sql, replaceParams);
		return sql;
	}
	
	/**
	 * 简单查询数据，主要用于编辑数据时查询单条记录无需定义数据集
	 * @param tableName 查询数据的表名
	 * @param keyData 查询条件键值对
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @param returnFields 返回结果集的字段列表
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数 MAP中还包含：<br/>
	 *         totalCount: 总记录数<br/>
	 *         rowCount: 当前结果集返回的记录数，实际同totalCount。主要是包含queryData返回值基本一致<br/>
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, ParaMap keyData, String orderBy, String returnFields) throws Exception {
		ParaMap result = new ParaMap();
		if (StrUtils.isNull(tableName)) {
			result.put("status", 0);
			result.put("msg", "查询数据必须传入目标表名");
			return result;
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Map<Integer, Object> sqlParamsPos = new HashMap<Integer, Object>();
		int intParamPos = 0;
		try {
			StringBuffer sql = new StringBuffer("select ");
			if (StrUtils.isNotNull(returnFields))
				sql.append(returnFields);
			else
				sql.append("*");
			sql.append(" from " + tableName + " where 1 = 1");
			if (keyData != null && keyData.size() > 0) {
				Iterator it = keyData.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next().toString();
					Object value = keyData.get(key);
					String strFieldName = key;
					if (StrUtils.isNotNull(strFieldName)) {
						boolean blnExclude = false;
						boolean blnLookup = false;
						if (strFieldName.substring(0, 1).equals("!")) {
							strFieldName = strFieldName.substring(1);
							blnExclude = true;
						}
						if (strFieldName.substring(0, 1).equals("@")) {
							strFieldName = strFieldName.substring(1);
							blnLookup = true;
						}
						if (value == null) {
							if (blnExclude)
								sql.append(" and " + strFieldName + " is not null ");
							else
								sql.append(" and " + strFieldName + " is null ");
						} else if (value instanceof List) {
							// 值列表，sql转换如: f in (1, 2, 4)
							List list = (List) value;
							if (list.size() > 0) {
								if (blnExclude)
									sql.append(" and " + strFieldName + " not in (");
								else
									sql.append(" and " + strFieldName + " in (");
								for (int i = 0; i < list.size(); i++) {
									sql.append("?,");
									sqlParamsPos.put(intParamPos, list.get(i));
									intParamPos++;
								}
								sql.deleteCharAt(sql.length() - 1);
								sql.append(")");
							}
						} else {
							if (blnExclude)
								sql.append(" and " + strFieldName + " <> ? ");
							else
								sql.append(" and " + strFieldName + " = ? ");
							sqlParamsPos.put(intParamPos, value);
							intParamPos++;
						}
					}
				}
			}
			if (!StrUtils.isNull(orderBy)) {
				sql.append(" order by " + orderBy);
			}
			pstm = getCon().prepareStatement(sql.toString());
			int intParamCount = 1;
			for (int i = 0; i < sqlParamsPos.size(); i++) {
				Object paramValue = sqlParamsPos.get(i);
				String strParamValue = String.valueOf(paramValue);
				if (StrUtils.isNull(strParamValue))
					pstm.setObject(intParamCount, "");// 仅使SQL不出错，但一般不是调用处希望的结果，如果该参数不接受null值则不影响结果集
				else
					pstm.setObject(intParamCount, paramValue);
				intParamCount++;
			}
			rs = pstm.executeQuery();
			result = convert(rs, true);
			int rowCount = result.getRecords().size();
			result.put("totalCount", rowCount);
			result.put("rowCount", rowCount);
			result.put("status", 1);
		} catch (SQLException e) {
			result = new ParaMap();
			result.put("status", 0);
			result.put("msg", e.getMessage());
			throw e;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}

	/**
	 * 简单查询数据，主要用于编辑数据时查询单条记录无需定义数据集
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyData 查询条件键值对
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数 MAP中还包含：<br/>
	 *         totalCount: 总记录数<br/>
	 *         rowCount: 当前结果集返回的记录数，实际同totalCount。主要是包含queryData返回值基本一致<br/>
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, ParaMap keyData, String orderBy) throws Exception {
		return querySimple(tableName, keyData, orderBy, null);
	}

	/**
	 * 简单查询数据，主要用于编辑数据时查询单条记录无需定义数据集
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyData 查询条件键值对
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数 MAP中还包含：<br/>
	 *         totalCount: 总记录数<br/>
	 *         rowCount: 当前结果集返回的记录数，实际同totalCount。主要是包含queryData返回值基本一致<br/>
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, ParaMap keyData) throws Exception {
		return querySimple(tableName, keyData, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param data 字段键值对，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @param returnFields 返回结果集的字段列表
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, ParaMap data, String orderBy, String returnFields) throws Exception {
		ParaMap keyData = new ParaMap();
		ParaMap convertData = convertDataFieldName(data, null);
		List<String> keyFields = new ArrayList<String>();
		// 提取所有主键字段
		if (keyField.indexOf(',') == -1 && keyField.indexOf(';') == -1)
			keyData.put(keyField.toLowerCase(), convertData.get(keyField.toLowerCase()));
		else {
			keyFields = convertFields(keyField, null, true);
			for (int i = 0; i < keyFields.size(); i++) {
				String fieldName = keyFields.get(i);
				if (convertData.containsKey(fieldName))
					keyData.put(fieldName, convertData.get(fieldName));
				else if (fieldName.startsWith("!") && convertData.containsKey(fieldName.substring(1)))
					keyData.put(fieldName, convertData.get(fieldName.substring(1)));
			}
		}
		return querySimple(tableName, keyData, orderBy, returnFields);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param data 字段键值对，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含
	 * @param orderBy
	 *            排序字段，格式如：abc desc, efg, xyz asc
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, ParaMap data, String orderBy) throws Exception {
		return querySimple(tableName, keyField, data, orderBy, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param data 字段键值对，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, ParaMap data) throws Exception {
		return querySimple(tableName, keyField, data, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param keyFieldValue 查询条件字段值，多个字段请使用用半角逗号或者分号分隔。如“0001”、“0001,0002”
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @param returnFields 返回结果集的字段列表
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, String keyFieldValue, String orderBy, String returnFields) throws Exception {
		if (keyField != null) {
			List<String> fields = keyField.indexOf(";") >= 0 ? StrUtils.getSubStrs(keyField, ";") : StrUtils.getSubStrs(keyField, ",");
			List<String> fieldValues = keyFieldValue != null ? (keyFieldValue.indexOf(";") >= 0 ? StrUtils.getSubStrs(keyFieldValue, ";") : StrUtils
					.getSubStrs(keyFieldValue, ",")) : null;
			ParaMap keyData = new ParaMap();
			for (int i = 0; i < fields.size(); i++)
				if (fieldValues != null && fieldValues.size() > i)
					keyData.put(fields.get(i), fieldValues.get(i));
				else
					keyData.put(fields.get(i), null);
			return querySimple(tableName, keyData, orderBy, returnFields);
		} else
			return querySimple(tableName, (ParaMap) null, orderBy, returnFields);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param keyFieldValue 查询条件字段值，多个字段请使用用半角逗号或者分号分隔。如“0001”、“0001,0002”
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, String keyFieldValue, String orderBy) throws Exception {
		return querySimple(tableName, keyField, keyFieldValue, orderBy, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param keyFieldValue 查询条件字段值，多个字段请使用用半角逗号或者分号分隔。如“0001”、“0001,0002”
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimple(String tableName, String keyField, String keyFieldValue) throws Exception {
		return querySimple(tableName, keyField, keyFieldValue, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param idFieldValue id字段值
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @param returnFields 返回结果集的字段列表
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimpleById(String tableName, String idFieldValue, String orderBy, String returnFields) throws Exception {
		return querySimple(tableName, "id", idFieldValue, orderBy, returnFields);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param idFieldValue id字段值
	 * @param orderBy 排序字段，格式如：abc desc, efg, xyz asc
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimpleById(String tableName, String idFieldValue, String orderBy) throws Exception {
		return querySimpleById(tableName, idFieldValue, orderBy, null);
	}

	/**
	 * 简单查询数据（重载方法）
	 * 
	 * @param tableName 查询数据的表名
	 * @param idFieldValue id字段值
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap querySimpleById(String tableName, String idFieldValue) throws Exception {
		return querySimpleById(tableName, idFieldValue, null);
	}
	
	/**
	 * 简单查询记录数
	 * 
	 * @param tableName 查询记录数的表名
	 * @param keyData 查询条件键值对
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数。从返回值中取rowCount
	 * @throws Exception
	 */
	public ParaMap querySimpleRowCount(String tableName, ParaMap keyData) throws Exception {
		ParaMap result = new ParaMap();
		if (StrUtils.isNull(tableName)) {
			result.put("status", 0);
			result.put("msg", "查询记录数必须传入目标表名");
			return result;
		}
		if (keyData == null || keyData.size() == 0) {
			result.put("status", 0);
			result.put("msg", "查询记录数必须传入目标表" + tableName + "的主键字段名及值列表信息");
			return result;
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Map<Integer, Object> sqlParamsPos = new HashMap<Integer, Object>();
		int intParamPos = 0;
		try {
			StringBuffer sql = new StringBuffer("select count(*) as total_row_count from " + tableName + " where 1 = 1");
			Iterator it = keyData.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				Object value = keyData.get(key);
				String strFieldName = key;
				if (StrUtils.isNotNull(strFieldName)) {
					boolean blnExclude = false;
					boolean blnLookup = false;
					if (strFieldName.substring(0, 1).equals("!")) {
						strFieldName = strFieldName.substring(1);
						blnExclude = true;
					}
					if (strFieldName.substring(0, 1).equals("@")) {
						strFieldName = strFieldName.substring(1);
						blnLookup = true;
					}
					if (value == null) {
						if (blnExclude)
							sql.append(" and " + strFieldName + " is not null ");
						else
							sql.append(" and " + strFieldName + " is null ");
					} else if (value instanceof List) {
						List list = (List) value;
						if (list.size() > 0) {
							if (blnExclude)
								sql.append(" and " + strFieldName + " not in (");
							else
								sql.append(" and " + strFieldName + " in (");
							for (int i = 0; i < list.size(); i++) {
								sql.append("?,");
								sqlParamsPos.put(intParamPos, list.get(i));
								intParamPos++;
							}
							sql.deleteCharAt(sql.length() - 1);
							sql.append(")");
						}
					} else {
						if (blnExclude)
							sql.append(" and " + strFieldName + " <> ? ");
						else
							sql.append(" and " + strFieldName + " = ? ");
						sqlParamsPos.put(intParamPos, value);
						intParamPos++;
					}
				}
			}
			pstm = getCon().prepareStatement(sql.toString());
			int intParamCount = 1;
			for (int i = 0; i < sqlParamsPos.size(); i++) {
				Object paramValue = sqlParamsPos.get(i);
				String strParamValue = String.valueOf(paramValue);
				if (StrUtils.isNull(strParamValue))
					pstm.setObject(intParamCount, "");// 仅使SQL不出错，但一般不是调用处希望的结果，如果该参数不接受null值则不影响结果集
				else
					pstm.setObject(intParamCount, paramValue);
				intParamCount++;
			}
			rs = pstm.executeQuery();
			rs.next();
			result.put("rowCount", rs.getInt(1));
			result.put("status", 1);
		} catch (SQLException e) {
			result = new ParaMap();
			result.put("status", 0);
			result.put("msg", e.getMessage());
			throw e;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}

	/**
	 * 简单查询记录数（重载方法）
	 * 
	 * @param tableName 查询记录数的表名
	 * @param keyField 查询条件字段，多个字段请使用用半角逗号或者分号分隔。如“id”、“department_id,emp_id”
	 * @param data 字段键值对，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含
	 * @return 返回查询结果，通过status=1表示更新成功，否则请检查msg参数。从返回值中取rowCount
	 * @throws Exception
	 */
	public ParaMap querySimpleRowCount(String tableName, String keyField, ParaMap data) throws Exception {
		ParaMap keyData = new ParaMap();
		ParaMap convertData = convertDataFieldName(data, null);
		// 提取所有主键字段
		if (keyField.indexOf(',') == -1 && keyField.indexOf(';') == -1)
			keyData.put(keyField.toLowerCase(), convertData.get(keyField.toLowerCase()));
		else {
			List<String> keyFields = convertFields(keyField, null, true);
			for (int i = 0; i < keyFields.size(); i++) {
				String fieldName = keyFields.get(i);
				if (convertData.containsKey(fieldName))
					keyData.put(fieldName, convertData.get(fieldName));
				else if (fieldName.startsWith("!") && convertData.containsKey(fieldName.substring(1)))
					keyData.put(fieldName, convertData.get(fieldName.substring(1)));
			}
		}
		return querySimpleRowCount(tableName, keyData);
	}
	
	/**
	 * 简单查询记录数（重载方法）
	 * @param tableName 表名
	 * @param keyField 主键字段名称，多个字段时以半角分号或者逗号分隔
	 * @param keyFieldValue 主键字段值
	 * @return
	 * @throws Exception
	 */
	public ParaMap querySimpleRowCount(String tableName, String keyField, String keyFieldValue) throws Exception {
		ParaMap keyData = new ParaMap();
		if (keyField.indexOf(',') == -1 && keyField.indexOf(';') == -1)
			keyData.put(keyField.toLowerCase(), keyFieldValue);
		else {
			String split = keyField.indexOf(',') != -1 ? "," : ";";
			List<String> keyFields = StrUtils.getSubStrs(keyField, split);
			for (int i = 0; i < keyFields.size(); i++) {
				String fieldName = keyFields.get(i);
				if (StrUtils.isNotNull(fieldName))
					keyData.put(fieldName, StrUtils.getSubStr(keyFieldValue, split, i));
			}
		}
		return querySimpleRowCount(tableName, keyData);
	}
			
	/**
	 * 更新数据
	 * 
	 * @param tableName 更新数据的表名
	 * @param keyField 更新数据表的主键字段，多个主键字段请使用用半角逗号或者分号分隔，如“id”、“department_id,emp_id”
	 * @param data 更新的数据内容，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含，单主键字段为空值将自动新增数据，并且自动获取主键字段值。
	 * @param format data中有特殊格式的字段值则需要在此map中添加格式转换说明，如：<br/>
	 *            create_date: str_to_date('2016-08-21 16:30:22', '%Y-%m-%d %T')
	 * @param checkTableFields 如果data中有键值非tableName表中的字段，请将本参数设置为true
	 * @param ignoreNullValue true表示忽略data中的null值，不会被组织到更新SQL语句中
	 * @return 返回更新结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	protected ParaMap save(String tableName, String keyField, ParaMap data, ParaMap format, boolean checkTableFields, boolean ignoreNullValue)
			throws Exception {
		ParaMap result = new ParaMap();
		if (StrUtils.isNull(tableName)) {
			result.put("status", 0);
			result.put("msg", "更新数据必须传入目标表名");
			return result;
		}
		if (StrUtils.isNull(keyField)) {
			result.put("status", 0);
			result.put("msg", "更新数据必须传入目标表" + tableName + "的主键字段名，如果无主键字段则无法通过此方法更新");
			return result;
		}
		ParaMap convertData = checkTableFields ? convertDataFieldName(data, tableName) : convertDataFieldName(data, null);
		ParaMap convertFormat = convertDataFieldName(format, null);
		PreparedStatement pstm = null;
		Map<Integer, Object> sqlParamsPos = new HashMap<Integer, Object>();
		List<String> keyFields = new ArrayList<String>();
		try {
			// 提取所有主键字段
			if (keyField.indexOf(',') == -1 && keyField.indexOf(';') == -1)
				keyFields.add(keyField);
			else {
				keyFields = convertFields(keyField, null);
			}
			// 检查主键字段是否都在更新字段列表中
			for (int i = 0; i < keyFields.size(); i++) {
				String keyFieldName = keyFields.get(i);
				if (!convertData.containsKey(keyFieldName))
					convertData.put(keyFieldName, null);
			}
			if (keyFields.size() == convertData.size()) {
				// 如果仅包含主键字段则直接返回更新成功
				result.put("status", 1);
				result.put("msg", "只有主键字段不需要更新");
				return result;
			}
			String keyFieldValue = convertData.containsKey(keyField) ? String.valueOf(convertData.get(keyField)) : null;
			int intParamPos = 0;
			if (keyFields.size() == 1 && StrUtils.isNull(keyFieldValue)) { // 单字段主键新增数据
				StringBuffer sqlLeft = new StringBuffer("insert into " + tableName + "(");
				StringBuffer sqlRight = new StringBuffer(" values(");
				Iterator it = convertData.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next().toString();
					Object fieldValue = convertData.get(key);
					String fieldValueFormat = convertFormat != null && convertFormat.containsKey(key) ? convertFormat.getString(key) : null;
					// 仅当不忽略null值、主键字段、值不为null、有格式化等之一的字段
					if (!ignoreNullValue || fieldValue != null || fieldValueFormat != null || key.equalsIgnoreCase(keyField)) {
						String keyValue = null;
						sqlLeft.append(key + ",");
						if (fieldValueFormat != null) {
							keyValue = fieldValueFormat;
							sqlRight.append(keyValue + ",");
						} else
							sqlRight.append("?,");
						if (key.equalsIgnoreCase(keyField)) {// 新增的主键字段暂不允许从format中取值
							String newKeyFieldValue = newPrimayKey();
							result.put(keyField, newKeyFieldValue);// 仅当主键字段值是本方法产生的才返回，其它任何情况都不会返回
							sqlParamsPos.put(intParamPos, newKeyFieldValue);
							intParamPos++;
						} else if (keyValue == null || keyValue.indexOf("?") >= 0) {
							sqlParamsPos.put(intParamPos, fieldValue);
							intParamPos++;
						}
					}
				}
				sqlLeft.deleteCharAt(sqlLeft.length() - 1);
				sqlLeft.append(")");
				sqlRight.deleteCharAt(sqlRight.length() - 1);
				sqlRight.append(")");
				sqlLeft.append(sqlRight.toString());
				pstm = getCon().prepareStatement(sqlLeft.toString());
			} else { // 修改数据或者多主键字段新增数据(仅支持oracle，其它类型数据库需修改)
				StringBuffer sql = new StringBuffer("");
				ParaMap checkDataExists = querySimpleRowCount(tableName, keyField, data);
				if (checkDataExists.getInt("rowCount") > 0) {// 记录存在
					// 添加更新语句
					sql.append(" update " + tableName + " set ");
					Iterator it = convertData.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next().toString();
						Object fieldValue = convertData.get(key);
						String fieldValueFormat = convertFormat != null && convertFormat.containsKey(key) ? convertFormat.getString(key) : null;
						if (keyFields.indexOf(key) == -1 && (!ignoreNullValue || fieldValue != null || fieldValueFormat != null)) {
							if (fieldValueFormat != null) {
								String keyValue = fieldValueFormat;
								sql.append(key + " = " + keyValue + ",");
								if (keyValue.indexOf("?") >= 0) {
									sqlParamsPos.put(intParamPos, fieldValue);
									intParamPos++;
								}
							} else {
								sql.append(key + " = ?,");
								sqlParamsPos.put(intParamPos, fieldValue);
								intParamPos++;
							}
						}
					}
					sql.deleteCharAt(sql.length() - 1);
					sql.append(" where 1 = 1 ");
					for (int i = 0; i < keyFields.size(); i++) {
						String fieldName = keyFields.get(i);
						boolean blnExclude = false;
						if (fieldName.substring(0, 1).equals("!")) {
							fieldName = fieldName.substring(1);
							blnExclude = true;
						}
						Object fieldValue = convertData.get(fieldName);
						String fieldValueFormat = null;
						if (convertFormat != null && convertFormat.containsKey(fieldName))
							fieldValueFormat = convertFormat.getString(fieldName);
						if (fieldValue == null) {
							if (blnExclude)
								sql.append(" and " + fieldName + " is not null ");
							else
								sql.append(" and " + fieldName + " is null ");
						} else if (fieldValue instanceof List) {
							List list = (List) fieldValue;
							if (list.size() > 0) {
								if (blnExclude)
									sql.append(" and " + fieldName + " not in (");
								else
									sql.append(" and " + fieldName + " in (");
								for (int j = 0; j < list.size(); j++) {
									if (fieldValueFormat != null) {
										sql.append(fieldValueFormat + ",");
										if (fieldValueFormat.indexOf("?") >= 0) {
											sqlParamsPos.put(intParamPos, list.get(j));
											intParamPos++;
										}
									} else {
										sql.append("?,");
										sqlParamsPos.put(intParamPos, list.get(j));
										intParamPos++;
									}
								}
								sql.deleteCharAt(sql.length() - 1);
								sql.append(")");
							}
						} else {
							if (blnExclude)
								sql.append(" and " + fieldName + " <> ? ");
							else
								sql.append(" and " + fieldName + " = ? ");
							sqlParamsPos.put(intParamPos, fieldValue);
							intParamPos++;
						}
					}
				} else {
					// 添加新增语句
					sql.append(" insert into " + tableName + "(");
					StringBuffer sqlRight = new StringBuffer(" values(");
					Iterator it = convertData.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next().toString();
						Object fieldValue = convertData.get(key);
						String fieldValueFormat = convertFormat != null && convertFormat.containsKey(key) ? convertFormat.getString(key) : null;
						if (!ignoreNullValue || fieldValue != null || fieldValueFormat != null) {
							sql.append(key + ",");
							if (fieldValueFormat != null) {
								String keyValue = fieldValueFormat;
								sqlRight.append(keyValue + ",");
								if (keyValue.indexOf("?") >= 0) {
									sqlParamsPos.put(intParamPos, fieldValue);
									intParamPos++;
								}
							} else {
								sqlRight.append("?,");
								if (fieldValue instanceof List) {
									// 新增数据时不允许有list键值出现，如果出现则取第一个值。无法按list排列组合生成多条记录
									List list = (List) fieldValue;
									if (list.size() > 0)
										sqlParamsPos.put(intParamPos, list.get(0));
									else
										sqlParamsPos.put(intParamPos, null);
								} else
									sqlParamsPos.put(intParamPos, fieldValue);
								intParamPos++;
							}
						}
					}
					sql.deleteCharAt(sql.length() - 1);
					sql.append(")");
					sqlRight.deleteCharAt(sqlRight.length() - 1);
					sqlRight.append(")");
					sql.append(sqlRight.toString());
				}
				pstm = getCon().prepareStatement(sql.toString());
			}
			for (int i = 0; i < sqlParamsPos.size(); i++) {
				Object paramValue = sqlParamsPos.get(i);
				String strParamValue = String.valueOf(paramValue);
				if (strParamValue == null || strParamValue.equals("") || strParamValue.equalsIgnoreCase("null"))
					pstm.setObject(i + 1, null);// 仅使SQL不出错，但一般不是调用处希望的结果，如果该参数不接受null值则不影响结果集
				else
					pstm.setObject(i + 1, paramValue);
			}
			int num = pstm.executeUpdate();
			// getCon().commit();
			result.put("status", 1);
			result.put("num", num);
			result.put("rowCount", num);
		} catch (SQLException e) {
			result = new ParaMap();
			result.put("status", 0);
			result.put("msg", e.getMessage());
			throw e;
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}

	/**
	 * 更新数据
	 * 
	 * @param tableName 更新数据的表名
	 * @param keyField 主键字段名。非空时表示如果按checkField未查找到记录则调用updateData方法新增记录，为空则不会添加任何记录
	 * @param checkField 查找记录条件字段，如果存在记录则更新，否则有keyField值则新增。<br/>
	 *            多个主键字段请使用用半角逗号或者分号分隔，如“id”、“department_id,emp_id”
	 * @param data 更新的数据内容，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含，单主键字段为空值将自动新增数据，并且自动获取主键字段值。
	 * @param format data中有特殊格式的字段值则需要在此map中添加格式转换说明，如：<br/>
	 *            create_date: str_to_date(?, 'yyyy-mm-dd hh24:mi:ss')
	 * @param checkTableFields 如果data中有键值非tableName表中的字段，请将本参数设置为true
	 * @param ignoreNullValue
	 *            true表示忽略data中的null值，不会被组织到更新SQL语句中
	 * @return 返回更新结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap save(String tableName, String keyField, String checkField, ParaMap data, ParaMap format, boolean checkTableFields,
			boolean ignoreNullValue) throws Exception {
		ParaMap result = new ParaMap();
		if (StrUtils.isNull(tableName)) {
			result.put("status", 0);
			result.put("msg", "更新数据必须传入目标表名");
			return result;
		}
		if (StrUtils.isNull(checkField)) {
			if (StrUtils.isNull(keyField)) {
				result.put("status", 0);
				result.put("msg", "更新数据必须传入目标表" + tableName + "的键值字段名，如果无键值字段则无法通过此方法更新");
				return result;
			} else {
				return save(tableName, keyField, data, format, checkTableFields, ignoreNullValue);
			}
		}
		// 提取所有键值字段
		List<String> keyFields = convertFields(keyField);
		List<String> checkFields = convertFields(checkField, null, true);
		ParaMap convertData = null;
		ParaMap convertFormat = null;
		// 保存更新值，仅当checkField中的字段需要更新时。其它字段不需要(主键字段keyField不能被更新)
		ParaMap newKeyFieldData = new ParaMap();
		ParaMap notConditionFieldData = new ParaMap();
		Iterator itData = data.keySet().iterator();
		while (itData.hasNext()) {
			String key = itData.next().toString();
			if (StrUtils.isNotNull(key)) {
				String fieldName = key.toLowerCase();
				if (fieldName.startsWith("new.")) {
					fieldName = fieldName.substring("new.".length());
					if (checkFields.indexOf(fieldName) != -1 || checkFields.indexOf("!" + fieldName) != -1)
						newKeyFieldData.put(fieldName, data.get(key));
				}
			}
		}
		// 转换值列表中的字段名为小写，按checkTableFields决定是否清除非指定表的字段
		if (checkTableFields) {
			convertData = convertDataFieldName(data, tableName);// 检查data中不存在于表tableName中的字段名Key值，并且将所有字段名转换为小写。多访问一次数据库
		} else {
			convertData = convertDataFieldName(data, null);// 不检查字段名，要求data中所有的Key值都是tableName表中的字段，由调用方来处理
		}
		convertFormat = convertDataFieldName(format, null);
		PreparedStatement pstm = null;
		Map<Integer, Object> sqlParamsPos = new HashMap<Integer, Object>();
		try {
			int intParamPos = 0;
			boolean hasUpdateField = false;
			ParaMap checkDataExists = querySimpleRowCount(tableName, checkField, data);
			if (checkDataExists.getInt("rowCount") == 0) {// 无记录
				if (StrUtils.isNotNull(keyField)) {// 如果键值字段不为空则
					if (keyField.indexOf(',') == -1 && keyField.indexOf(';') == -1)
						data.put(keyField, "");
					return save(tableName, keyField, convertData, convertFormat, false, ignoreNullValue);
				} else {
					result.put("status", 1);
					result.put("msg", "未返回记录");
					return result;
				}
			} else {// 修改数据
				StringBuffer sql = new StringBuffer("update " + tableName + " set ");
				Iterator it = convertData.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next().toString().toLowerCase();
					Object fieldValue = newKeyFieldData.containsKey(key) ? newKeyFieldData.get(key) : convertData.get(key);
					String fieldValueFormat = convertFormat != null && convertFormat.containsKey(key) ? convertFormat.getString(key) : null;
					// 主键字段不能更新，并且仅当不忽略null值、主键字段、值不为null、有格式化等之一的字段
					if (((checkFields.indexOf(key) == -1 && checkFields.indexOf("!" + key) == -1) || newKeyFieldData.containsKey(key))
							&& (keyFields == null || keyFields.indexOf(key) == -1)
							&& (!ignoreNullValue || fieldValue != null || fieldValueFormat != null)) {
						if (fieldValueFormat != null) {
							String keyValue = fieldValueFormat;
							sql.append(key + " = " + keyValue + ",");
							if (keyValue.indexOf("?") >= 0) {
								sqlParamsPos.put(intParamPos, fieldValue);
								intParamPos++;
							}
						} else {
							sql.append(key + " = ?,");
							sqlParamsPos.put(intParamPos, fieldValue);
							intParamPos++;
						}
						hasUpdateField = true;
					}
				}
				sql.deleteCharAt(sql.length() - 1);
				sql.append(" where 1 = 1 ");
				for (int i = 0; i < checkFields.size(); i++) {
					String checkFieldName = checkFields.get(i);
					String strFieldName = checkFieldName;
					boolean blnExclude = false;
					if (strFieldName.startsWith("!")) {
						strFieldName = strFieldName.substring(1);
						blnExclude = true;
					}
					Object fieldValue = convertData.get(strFieldName);
					if (convertFormat != null && convertFormat.containsKey(strFieldName)) {
						// 需要格式化的值不支持多个值
						String keyValue = convertFormat.getString(strFieldName);
						if (blnExclude)
							sql.append(" and " + strFieldName + " <> " + keyValue + " ");
						else
							sql.append(" and " + strFieldName + " = " + keyValue + " ");
						if (keyValue.indexOf("?") >= 0) {
							sqlParamsPos.put(intParamPos, fieldValue);
							intParamPos++;
						}
					} else {
						if (fieldValue == null) {
							if (blnExclude)
								sql.append(" and " + strFieldName + " is not null ");
							else
								sql.append(" and " + strFieldName + " is null ");
						} else if (fieldValue instanceof List) {
							List list = (List) fieldValue;
							if (list.size() > 0) {
								if (blnExclude)
									sql.append(" and " + strFieldName + " not in (");
								else
									sql.append(" and " + strFieldName + " in (");
								for (int j = 0; j < list.size(); j++) {
									sql.append("?,");
									sqlParamsPos.put(intParamPos, list.get(j));
									intParamPos++;
								}
								sql.deleteCharAt(sql.length() - 1);
								sql.append(")");
							}
						} else {
							if (blnExclude)
								sql.append(" and " + strFieldName + " <> ? ");
							else
								sql.append(" and " + strFieldName + " = ? ");
							sqlParamsPos.put(intParamPos, fieldValue);
							intParamPos++;
						}
					}
				}
				pstm = getCon().prepareStatement(sql.toString());
			}
			for (int i = 0; i < sqlParamsPos.size(); i++) {
				Object paramValue = sqlParamsPos.get(i);
				String strParamValue = String.valueOf(paramValue);
				if (StrUtils.isNull(strParamValue))
					pstm.setObject(i + 1, null);// 仅使SQL不出错，但一般不是调用处希望的结果，如果该参数不接受null值则不影响结果集
				else
					pstm.setObject(i + 1, paramValue);
			}
			int num = 0;
			if (hasUpdateField) // 仅更新时有需要修改的字段，新增记录前面已跳出
				num = pstm.executeUpdate();
			result.put("status", 1);
			result.put("num", num);
			result.put("rowCount", num);
		} catch (SQLException e) {
			result = new ParaMap();
			result.put("status", 0);
			result.put("msg", e.getMessage());
			throw e;
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}

	/**
	 * 更新数据
	 * 
	 * @param tableName 更新数据的表名
	 * @param keyField 主键字段名。非空时表示如果按checkField未查找到记录则调用updateData方法新增记录，为空则不会添加任何记录
	 * @param checkField 查找记录条件字段，如果存在记录则更新，否则有keyField值则新增。<br/>
	 *            多个主键字段请使用用半角逗号或者分号分隔，如“id”、“department_id,emp_id”
	 * @param data 更新的数据内容，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含，单主键字段为空值将自动新增数据，并且自动获取主键字段值。
	 * @param format data中有特殊格式的字段值则需要在此map中添加格式转换说明，如：<br/>
	 *            create_date: str_to_date('2016-08-21 16:30:22', '%Y-%m-%d %T')
	 * @param checkTableFields 如果data中有键值非tableName表中的字段，请将本参数设置为true
	 * @return 返回更新结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap save(String tableName, String keyField, String checkField, ParaMap data, ParaMap format, boolean checkTableFields)
			throws Exception {
		return save(tableName, keyField, checkField, data, format, checkTableFields, true);
	}

	/**
	 * 更新数据（重载方法），自动设置checkTableFields为true
	 * 
	 * @param tableName 更新数据的表名
	 * @param keyField 更新数据表的主键字段，多个主键字段请使用用半角逗号或者分号分隔，未传入则无法更新。如“id”、“department_id,
	 *            emp_id”
	 * @param checkField 查找记录条件字段，如果存在记录则更新，否则有keyField值则新增。<br/>
	 *            多个主键字段请使用用半角逗号或者分号分隔，如“id”、“department_id,emp_id”
	 * @param data 更新的数据内容，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含，单主键字段为空值将自动新增数据，并且自动获取主键字段值。
	 * @param format data中有特殊格式的字段值则需要在此map中添加格式转换说明，如：<br/>
	 *            create_date: str_to_date('2016-08-21 16:30:22', '%Y-%m-%d %T')
	 * @return 返回更新结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap save(String tableName, String keyField, String checkField, ParaMap data, ParaMap format) throws Exception {
		return save(tableName, keyField, checkField, data, format, true);
	}

	/**
	 * 更新数据（重载方法），自动设置checkTableFields为true
	 * 
	 * @param tableName 更新数据的表名
	 * @param keyField 更新数据表的主键字段，多个主键字段请使用用半角逗号或者分号分隔，未传入则无法更新。如“id”、“department_id,
	 *            emp_id”
	 * @param checkField 查找记录条件字段，如果存在记录则更新，否则有keyField值则新增。<br/>
	 *            多个主键字段请使用用半角逗号或者分号分隔，如“id”、“department_id,emp_id”
	 * @param data 更新的数据内容，如：<br/>
	 *            id: 1234567890<br/>
	 *            no: abcdefg<br/>
	 *            name: xxxxxx<br/>
	 *            主键字段也需包含，单主键字段为空值将自动新增数据，并且自动获取主键字段值。
	 * @return 返回更新结果，通过status=1表示更新成功，否则请检查msg参数
	 * @throws Exception
	 */
	public ParaMap save(String tableName, String keyField, String checkField, ParaMap data) throws Exception {
		return save(tableName, keyField, checkField, data, null);
	}
	
	public static void main(String[] args) throws Exception {
		//数据库服务器时间
//		Date now = DataSetDao.getDBServerDate();
//		System.out.println(StrUtils.dateToString(now));
		
		//mysql端整型日期赋值：UNIX_TIMESTAMP() * 1000
		//select FROM_UNIXTIME(875996580); --1997-10-05 04:23:00
//		Date date1 = new Date(875996580L);//1970-01-11 11:19:56
//		Date date2 = new Date(875996580L * 1000L);//1997-10-05 04:23:00
//		System.out.println(StrUtils.dateToString(date1));
//		System.out.println(StrUtils.dateToString(date2));
		
		String sql = null;
		String convertSql = null;
		ParaMap sqlParamsPos = null;
		//解析sql
//		sql = "select * from sys_param where name = :name and no = :no and param_type = :param_type";
//		sqlParamsPos = new ParaMap();
//		convertSql = DataSetDao.convertSqlParams(sql, sqlParamsPos, null);
//		System.out.println("isParamQuery = " + DataSetDao.isParamQuery(sql));
//		System.out.println("convertSql = " + convertSql);
//		System.out.println("sqlParamsPos = " + sqlParamsPos);
		
		AdvDataSetDao dao = new AdvDataSetDao();
		ParaMap data = null;
		SQLMap sqlMap = new SQLMap();
		//按原方式查询数据
//		sql = "select * from sys_bank where name like concat('%', ?, '%') and no like concat('%', ?, '%') and bank_type = ?";
//		sqlMap = new SQLMap();
//		sqlMap.setSQL(sql);
//		sqlMap.addParam("银行"); //参数必须按点位符顺序
//		sqlMap.addParam("cm");
//		sqlMap.addParam("11");
//		data = dao.query(sqlMap);
		
		//按参数查询数据
		sql = "select * from sys_bank where name like concat('%', :name, '%') and no like concat('%', :no, '%') and bank_type = :bank_type";
		sqlMap = new SQLMap();
		sqlMap.setSQL(sql);
		sqlMap.put("no", "cm");
		sqlMap.put("name", "银行");
		//sqlMap.put("bank_type", "11");
//		
//		//分页查询
//		sqlMap.put("isPage", "true");
//		sqlMap.put("pageSize", 5);
//		sqlMap.put("pageIndex", 1);
//		
		data = dao.query(sqlMap);
		//data = dao.queryForPage(sqlMap);
		
		//获取表结构数据
//		data = dao.getTableMetaData("sys_bank");
		
		//按表名等信息简单方式查询数据，不需要定义数据集sql
//		data = dao.querySimple("sys_bank", "no", "cmb");
		
		//执行数据集sql
//		sql = "update sys_bank set remark = :remark where no = :no and bank_type = :bank_type";
//		sqlMap = new SQLMap();
//		sqlMap.setSQL(sql);
//		sqlMap.put("no", "cmb");
//		sqlMap.put("remark", "测试11");
//		sqlMap.put("bank_type", "11");
//		data = dao.execute(sqlMap);
//		DataSourceManager.commit();
		
		//更新数据，不需要定义数据集sql
//		data = new ParaMap();
//		data.put("no", "cmb");
//		data.put("remark", "初始化");
//		data.put("bank_type", "11");
//		data = dao.update("sys_bank", "id", "no,bank_type", data);
//		DataSourceManager.commit();
		
		System.out.println("data = " + data);
	}
}
