package com.base.utils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.base.log.ReqUtils;
import com.base.security.ApiUtils;

public class ParaMap extends TreeMap {
	private static final Logger log = Logger.getLogger(ParaMap.class);
	private static final long serialVersionUID = 1L;
	private static final String boolTrueValue = "true;1;yes;ok";
	private static final boolean boolNullValue = false;
	private static final boolean boolEmptyValue = false;

	public ParaMap() {

	}

	public ParaMap(Map map) {
		if (map != null)
			this.putAll(map);
	}

	public Object put(Object key, Object value) {
		String k = String.valueOf(key);
		return super.put(k, value);
	}

	public Object get(Object key) {
		return super.get(key);
	}

	public String getString(String key) {
		String v = null;
		if (this.containsKey(key)) {
			v = String.valueOf(get(key));
		}
		return v;
	}

	public Double getDouble(String key) {
		if (this.containsKey(key)) {
			String v = getString(key);
			Double d = Double.parseDouble(v);
			return d;
		} else {
			return null;
		}
	}

	public Double getDouble(String key, Double defaultValue) {
		Double d = getDouble(key);
		if (d != null) {
			return d;
		} else {
			return defaultValue;
		}
	}

	public int getInt(String key, int defaultValue) {
		Integer v = getInteger(key);
		if (v == null)
			return defaultValue;
		else
			return v;
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public Integer getInteger(String key) {
		if (this.containsKey(key)) {
			String v = getString(key);
			try {
				int i = Integer.parseInt(v);
				return i;
			} catch (Exception ex) {
				return null;
			}
		} else
			return null;
	}

	public Long getLong(String key) {
		if (this.containsKey(key)) {
			String v = getString(key);
			long l = Long.parseLong(v);
			return l;
		} else
			return null;
	}

	public BigDecimal getBigDecimal(String key) {
		BigDecimal b = null;
		if (this.containsKey(key)) {
			String v = getString(key);
			if (v != null && !"".equals(v) && !v.equalsIgnoreCase("null")) {
				b = new BigDecimal(v);
			}
		}
		return b;
	}

	public Date getDate(String key) {
		if (this.containsKey(key)) {
			String v = getString(key);
			return DateUtils.getDate(v);
		} else
			return null;
	}

	public List getList(String key) {
		Object obj = get(key);
		return (List) obj;
	}

	public List getFields() {
		return getList("fs");
	}

	public int getFieldCount() {
		List fs = getFields();
		if (fs == null)
			return -1;
		else
			return fs.size();
	}

	public boolean hasField(String fieldName) {
		return getFieldIndex(fieldName) != -1;
	}

	public int getFieldIndex(String fieldName) {
		List fs = getFields();
		if (fs == null)
			return -1;
		else {
			for (int i = 0; i < fs.size(); i++) {
				ParaMap field = (ParaMap) fs.get(i);
				if (fieldName.equals(field.getString("name")))
					return i;
			}
		}
		return -1;
	}

	public String getField(int i) {
		List fs = getFields();
		if (fs == null)
			return null;
		else
			return String.valueOf(fs.get(i));
	}

	public List getRecords() {
		return getList("rs");
	}

	public int getRecordCount() {
		List rs = getRecords();
		if (rs == null)
			return -1;
		else
			return rs.size();
	}

	public ParaMap clone() {
		ParaMap out = new ParaMap();
		out.putAll(this);
		return out;
	}

	public Object getRecordValue(int recordNo, String fieldName) {
		return getRecordValue(recordNo, getFieldIndex(fieldName));
	}

	public Object getRecordValue(int recordNo, int fieldIndex) {
		if (fieldIndex < 0)
			return null;
		List fs = getFields();
		if (fs == null || fs.size() == 0 || fieldIndex >= fs.size())
			return null;
		List record = (List) getRecords().get(recordNo);
		return record.get(fieldIndex);
	}

	public String getRecordString(int recordNo, String fieldName) {
		return getRecordString(recordNo, getFieldIndex(fieldName));
	}

	public String getRecordString(int recordNo, int fieldIndex) {
		Object value = getRecordValue(recordNo, fieldIndex);
		if (value == null)
			return null;
		else
			return value.toString();
	}

	public Integer getRecordInteger(int recordNo, String fieldName) {
		return getRecordInteger(recordNo, getFieldIndex(fieldName));
	}

	public Integer getRecordInteger(int recordNo, int fieldIndex) {
		String value = getRecordString(recordNo, fieldIndex);
		if (StrUtils.isNull(value) || !StrUtils.isInteger(value))
			return null;
		else
			return Integer.parseInt(value);
	}

	public int getRecordInt(int recordNo, String fieldName, int defaultValue) {
		return getRecordInt(recordNo, getFieldIndex(fieldName), defaultValue);
	}

	public int getRecordInt(int recordNo, String fieldName) {
		return getRecordInt(recordNo, getFieldIndex(fieldName));
	}

	public int getRecordInt(int recordNo, int fieldIndex, int defaultValue) {
		Integer value = getRecordInteger(recordNo, fieldIndex);
		if (value == null)
			return defaultValue;
		else
			return value;
	}

	public int getRecordInt(int recordNo, int fieldIndex) {
		return getRecordInt(recordNo, fieldIndex, 0);
	}

	public Double getRecordDouble(int recordNo, String fieldName) {
		return getRecordDouble(recordNo, getFieldIndex(fieldName));
	}

	public Double getRecordDouble(int recordNo, int fieldIndex) {
		String value = getRecordString(recordNo, fieldIndex);
		if (StrUtils.isNull(value))
			return null;
		BigDecimal bigDecimal = new BigDecimal(value);
		return bigDecimal.doubleValue();
	}

	public BigDecimal getRecordBigDecimal(int recordNo, String fieldName) {
		return getRecordBigDecimal(recordNo, getFieldIndex(fieldName));
	}

	public BigDecimal getRecordBigDecimal(int recordNo, int fieldIndex) {
		String value = getRecordString(recordNo, fieldIndex);
		return new BigDecimal(value);
	}

	public double getRecordDoubleBase(int recordNo, String fieldName) {
		return getRecordDoubleBase(recordNo, getFieldIndex(fieldName));
	}

	public double getRecordDoubleBase(int recordNo, int fieldIndex,
			double defaultValue) {
		Double value = getRecordDouble(recordNo, fieldIndex);
		if (value == null)
			return defaultValue;
		else
			return value;
	}

	public double getRecordDoubleBase(int recordNo, int fieldIndex) {
		return getRecordDoubleBase(recordNo, fieldIndex, 0);
	}

	public Date getRecordDateTime(int recordNo, String fieldName,
			String dateFormat) {
		return getRecordDateTime(recordNo, getFieldIndex(fieldName), dateFormat);
	}

	public Date getRecordDateTime(int recordNo, String fieldName) {
		return getRecordDateTime(recordNo, getFieldIndex(fieldName));
	}

	public Date getRecordDateTime(int recordNo, int fieldIndex,
			String dateFormat) {
		String value = getRecordString(recordNo, fieldIndex);
		// if (StrUtils.isNull(value) || !StrUtils.isDateTime(value))
		// return null;
		// else
		return StrUtils.stringToDate(value, dateFormat);
	}

	public Date getRecordDateTime(int recordNo, int fieldIndex) {
		return getRecordDateTime(recordNo, fieldIndex, "yyyy-MM-dd HH:mm:ss");
	}

	public boolean getRecordBool(int recordNo, String fieldName) {
		return getRecordBool(recordNo, getFieldIndex(fieldName));
	}

	public boolean getRecordBool(int recordNo, int fieldIndex) {
		String value = StrUtils.trim(getRecordString(recordNo, fieldIndex));
		if (value == null)
			return boolNullValue;
		else if (value.equals(""))
			return boolEmptyValue;
		else
			return StrUtils.paramExists(boolTrueValue, value, true);
	}

	public boolean setRecordValue(int recordNo, String fieldName, Object value) {
		return setRecordValue(recordNo, getFieldIndex(fieldName), value);
	}

	public boolean setRecordValue(int recordNo, int fieldIndex, Object value) {
		if (fieldIndex < 0)
			return false;
		List fs = getFields();
		if (fs == null || fs.size() == 0 || fieldIndex >= fs.size())
			return false;
		List record = (List) getRecords().get(recordNo);
		record.set(fieldIndex, value);
		return true;
	}

	private static ValueFilter filter = new ValueFilter() {
		@Override
		public Object process(Object obj, String name, Object value) {
			if (value == null)
				return "";
			if (value instanceof BigDecimal || value instanceof Double
					|| value instanceof Float) {
				return new BigDecimal(value.toString());
			}
			return value;

		}
	};

	public String toString() {
		return JSON.toJSONString(this, filter);
	}

	/**
	 * 返回Map中指定路径的对象值，用于json转换的Map对象。要求键都为字符串
	 * 
	 * @param path
	 *            对象值路径，如：aaaa.bbbb[2].cccc。如果某步为List可以使用索引
	 * @return
	 * @throws Exception
	 */
	public Object getDataByPath(String path) throws Exception {
		if (size() == 0 || StrUtils.isNull(path))
			return this;
		Object result = null;
		ParaMap item = this;
		List<String> configPathList = StrUtils.getSubStrs(path, ".", true);
		for (int i = 0; i < configPathList.size(); i++) {
			String subPath = configPathList.get(i);
			int itemIndex = -1;
			if (subPath.indexOf("[") != -1
					&& subPath.substring(subPath.length() - 1).equals("]")) {
				String strIndex = subPath.substring(subPath.indexOf("["),
						subPath.length() - 1);
				if (!StrUtils.isInteger(strIndex))
					throw new Exception(subPath + "中索引值错误");
				itemIndex = Integer.parseInt(strIndex);
				subPath = subPath.substring(0, subPath.indexOf("["));
			}
			if (item.containsKey(subPath)) {
				Object subItem = item.get(subPath);
				if (i == configPathList.size() - 1) {
					if (itemIndex == -1) {
						result = subItem;
					} else if (subItem instanceof List) {
						List subItemList = (List) subItem;
						if (itemIndex >= 0 && itemIndex < subItemList.size()) {
							result = subItemList.get(itemIndex);
						} else {
							throw new Exception(itemIndex + "超出" + subPath
									+ "的索引范围");
						}
					} else {
						throw new Exception(subPath
								+ "类型错误，并非列表对象。无法获取指定索引位置的对象");
					}
				} else if (subItem instanceof ParaMap) {
					item = (ParaMap) subItem;
				} else {
					throw new Exception(subPath + "类型错误。无法获取指定位置的对象进行下一轮读取数据");
				}
			} else {
				throw new Exception("取值路径" + subPath + "不存在");
			}
		}
		return result;
	}

	public List<ParaMap> getListObj() {
		List resultList = new ArrayList();
		List filds = this.getFields();
		for (int i = 0; i < this.getRecordCount(); i++) {
			ParaMap out = new ParaMap();
			for (int j = 0; j < filds.size(); j++) {
				Object fieldObj = filds.get(j);
				String fieldName = fieldObj.toString();
				if (fieldObj instanceof ParaMap)
					fieldName = ((ParaMap) fieldObj).getString("name");
				out.put(fieldName, this.getRecordValue(i, j));
			}
			resultList.add(out);
		}
		return resultList;
	}

	public byte[] getBytes(String key) {
		byte[] buf = null;
		if (this.containsKey(key)) {
			buf = (byte[]) get(key);
		}
		return buf;
	}

	/**
	 * 查找符合字段值匹配的记录，返回记录索引
	 * 
	 * @param fieldName
	 *            字段名
	 * @param fieldValue
	 *            字段值
	 * @param ignoreCase
	 *            字段值忽略大小写
	 * @param partial
	 *            字段值部分匹配
	 * @return
	 */
	public int locate(String fieldName, Object fieldValue, boolean ignoreCase,
			boolean partial) {
		int result = -1;
		int recordCount = getRecordCount();
		int fieldIndex = getFieldIndex(fieldName);
		if (recordCount <= 0 || StrUtils.isNull(fieldName) || fieldIndex == -1)
			return result;
		boolean isNull = fieldValue == null
				|| StrUtils.isNull(fieldValue.toString());
		String strValue = isNull ? null : (ignoreCase ? fieldValue.toString()
				.toLowerCase() : fieldValue.toString());
		for (int i = 0; i < recordCount; i++) {
			String recordValue = getRecordString(i, fieldIndex);
			if (isNull) {
				if (StrUtils.isNull(recordValue)) {
					result = i;
					break;
				}
			} else if (StrUtils.isNotNull(recordValue)) {
				if (ignoreCase)
					recordValue = recordValue.toLowerCase();
				if (partial && recordValue.indexOf(strValue) != -1) {
					result = i;
					break;
				} else if (!partial && recordValue.equals(strValue)) {
					result = i;
					break;
				}
			}
		}
		return result;
	}

	public int locate(String fieldName, Object fieldValue) {
		return locate(fieldName, fieldValue, false, false);
	}

	public int locate(Map<String, Object> fields) {
		int result = -1;
		int recordCount = getRecordCount();
		if (recordCount <= 0)
			return result;
		List fieldIndexs = new ArrayList();
		Iterator it = fields.keySet().iterator();
		while (it.hasNext()) {
			String fieldName = it.next().toString();
			if (StrUtils.isNull(fieldName))
				return result;
			int fieldIndex = getFieldIndex(fieldName);
			if (fieldIndex == -1)
				return result;
			Object fieldValue = fields.get(fieldName);
			List field = new ArrayList();// 字段索引、字段值、是否null、是否忽略大小写、是否部分匹配
			field.add(fieldIndex);
			if (fieldValue == null) {
				field.add(fieldValue);
				field.add(true);
			} else {
				List fieldValueList = (List) fieldValue;
				if (fieldValueList.size() == 0) {
					field.add(null);
					field.add(true);
				} else {
					Object fv = fieldValueList.get(0);
					if (fv == null || StrUtils.isNull(fv.toString())) {
						field.add(null);
						field.add(true);
					} else {
						field.add(fieldValueList.get(0));
						field.add(false);
						if (fieldValueList.size() > 1)
							field.add(fieldValueList.get(1));
						if (fieldValueList.size() > 2)
							field.add(fieldValueList.get(2));
					}
				}
			}
			fieldIndexs.add(field);
		}
		for (int i = 0; i < recordCount; i++) {
			int located = 0;
			for (int j = 0; j < fieldIndexs.size(); j++) {
				if (located < j)
					break;
				List field = (List) fieldIndexs.get(j);
				int fieldIndex = (Integer) field.get(0);
				Object fieldValue = field.get(1);
				boolean isNull = (Boolean) field.get(2);
				boolean ignoreCase = field.size() > 3 ? (Boolean) field.get(3)
						: false;
				boolean partial = field.size() > 4 ? (Boolean) field.get(4)
						: false;
				String strValue = isNull ? null : (ignoreCase ? fieldValue
						.toString().toLowerCase() : fieldValue.toString());
				String recordValue = getRecordString(i, fieldIndex);
				if (isNull) {
					if (StrUtils.isNull(recordValue)) {
						located++;
						continue;
					}
				} else if (StrUtils.isNotNull(recordValue)) {
					if (ignoreCase)
						recordValue = recordValue.toLowerCase();
					if (partial && recordValue.indexOf(strValue) != -1) {
						located++;
						continue;
					} else if (!partial && recordValue.equals(strValue)) {
						located++;
						continue;
					}
				}
			}
			if (located == fieldIndexs.size()) {
				result = i;
				break;
			}
		}
		return result;
	}

	public String md5() {
		String ve = getString("ve");
		String va = "";
		if ("2".equals(ve))
			va = encode2();
		else
			va = encode1();
		return va;
	}

	private String encode1() {
		StringBuffer sb = new StringBuffer();
		try {
			Iterator it = this.keySet().iterator();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if ("sign".equals(key))
					continue;
				if ("requestKey".equals(key))
					continue;
				if (ReqUtils.getRId().equals(key))
					continue;
				String value = this.getString(key);
				value = EnCodeUtils.encode(value);
				if (sb.toString().length() == 0)
					sb.append(key + "=" + value);
				else
					sb.append("&" + key + "=" + value);
			}
			sb.append("&requestKey=" + ApiUtils.getSecretKey());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return MD5.MD5Encode(sb.toString());
	}

	private String encode2() {
		StringBuffer sb = new StringBuffer();
		try {
			Iterator it = this.keySet().iterator();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if ("sign".equals(key))
					continue;
				if ("requestKey".equals(key))
					continue;
				if (ReqUtils.getRId().equals(key))
					continue;
				String value = this.getString(key);
				if (sb.toString().length() == 0)
					sb.append(key + "=" + value);
				else
					sb.append("&" + key + "=" + value);
			}
			sb.append("&requestKey=" + ApiUtils.getSecretKey());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// System.out.println(sb.toString());
		return MD5.MD5Encode(sb.toString());
	}

	public String encode() {
		StringBuffer sb = new StringBuffer();
		try {
			Iterator it = keySet().iterator();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if ((!"sign".equals(key)) && (!"requestKey".equals(key))
						&& ReqUtils.getRId().equals(key)) {
					String value = getString(key);
					value = URLEncoder.encode(value, "UTF-8");
					if (sb.toString().length() == 0) {
						sb.append(key + "=" + value);
					} else
						sb.append("&" + key + "=" + value);
				}
			}
			sb.append("&requestKey=" + ApiUtils.getSecretKey());
		} catch (Exception ex) {
		}

		return sb.toString();
	}

	public String toMd5String() {
		String md5Key = ApiUtils.getSecretKey();
		if (StrUtils.isNotNull(md5Key)) {
			this.put("ngis", md5Key);
			String json = this.toString();
			String md5 = MD5.MD5Encode(json);
			this.put("ngis", md5);
			// String json2 = json.replaceFirst("\"ngis\":\"" + md5Key + "\"",
			// "\"ngis\":\"" + md5 + "\"");
		}
		String json2 = this.toString();
		return json2;
	}

	public static void main(String[] args) {

		ParaMap map = new ParaMap();
		map.put("d1", 1 * 1.0);
		System.out.println(map);

		//		String s1 = "module=act&service=Activity&method=doActivity&businesstype=4&u=20150708113311633-16170&v=20150710162538032-97205&sign=ad6f5be4f9054f85fb80d37daa3fdf58";
		//		s1 = "businesstype=4&method=doActivity&module=act&service=Activity&u=20150708113311633-16170&v=20150710162538032-97205&sign=827549979e9c44284c5ee14d628898b8";
		//		s1 = "module=upp&service=Device&method=uploadInPlateNumber&comid=bc9bc0b6f5f011e4b907000c29a0f956&equipmentid=f88850c9f94311e4b907000c29a0f956&factoryid=1&equipmentversion=1&equipmentip=192.168.0.100&platenumber=粤B88883&intime=1430896200888&parkinglotid=c69a3ad7f5f011e4b907000c29a0f956&entryid=d0c8605cf39011e4b907000c29a0f956&ms=1430894700755";
		//		s1 = "comid=bc9bc0b6f5f011e4b907000c29a0f956&entryid=d0c8605cf39011e4b907000c29a0f956&equipmentid=f88850c9f94311e4b907000c29a0f956&equipmentip=192.168.0.100&equipmentversion=1&factoryid=1&intime=1430896200888&method=uploadInPlateNumber&module=upp&ms=1430894700755&parkinglotid=c69a3ad7f5f011e4b907000c29a0f956&platenumber=粤B88883&service=Device&sign=6026d112a7a5dbc4e7eb80a45db88135";
		//		s1 = "address=!$'()* ,-./:;=@_~%#[]&clientType=html&comname=()=&comtel=13290907654&id=&method=addComInfo&module=comp&ms=1443409143072&service=Com&t=7383402718860602916892&u=1&updatetime=&ve=2&requestKey=D3029C73406221B02026B684BB00579C";
		//		// String[] pairs = s1.split("&");
		//		// for (int i = 0; i < pairs.length; i++) {
		//		// String[] pv = pairs[i].split("=");
		//		// map.put(pv[0], pv[1]);
		//		// }
		//		// System.out.println("after:" + map.md5());
		//
		//		// System.out.println(MD5.MD5Encode(s1));
		//
		//		// String s2 =
		//		// "{\"message\":\"管理公司不可为空\",\"ngis\":\"\",\"state\":2,\"ts\":1466996778577}";
		//
		//		map.put("message", "管理公司不可为空");
		//		map.put("state", 2);
		//		map.put("ts", 1466996778577L);
		//
		//		String s2 = map.toMd5String();
		//		System.out.println(s2);

	}
}
