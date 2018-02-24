package com.base.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;

/**
 * JSON工具类
 * @author SAMONHUA
 *
 */
public class JsonUtils {
	static private Object convertJson(Object json) {
		if (json == null)
			return null;
		else if (json instanceof JSONArray) {
			List list = jsonToList((JSONArray) json);
			return list;
		} else if (json instanceof JSONObject) {
			ParaMap map = jsonToMap((JSONObject) json);
			return map;
		} else {
			return json;
		}
	}
	
	/**
	 * json对象转换为List对象
	 * @param json json对象
	 * @return
	 */
	static public List jsonToList(JSONArray json) {
		if (json == null)
			return null;
		List result = new ArrayList();
		for(int i = 0; i < json.size(); i++) {
			result.add(convertJson(json.get(i)));
		}
		return result;
	}
	
	/**
	 * json对象转换为ParaMap对象
	 * @param json json对象
	 * @return
	 */
	static public ParaMap jsonToMap(JSONObject json) {
		if (json == null || json.isEmpty())
			return null;
		ParaMap result = new ParaMap();
		Iterator it = json.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			result.put(key, convertJson(json.get(key)));
		}
		return result;
	}
	
	/**
	 * 从JSON字段串返回ParaMap对象
	 * @param json json对象，数组等不支持
	 * @return
	 */
	static public ParaMap strToMap(String json) {
		if (StrUtils.isNull(json))
			return null;
		try {
			return jsonToMap(JSONObject.parseObject(json));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 从JSON字段串返回List对象
	 * @param json json对象，数组等不支持
	 * @return
	 */
	static public List strToList(String json) {
		if (StrUtils.isNull(json))
			return null;
		try {
			return jsonToList(JSONArray.parseArray(json));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		String json1 = "{'fs':[{'name':'id','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'no','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'name','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'bank_type','precision':4,'scale':0,'size':4,'type':4,'typeName':'INT'},{'name':'creator','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'createtime','precision':20,'scale':0,'size':20,'type':-5,'typeName':'BIGINT'},{'name':'updatetime','precision':20,'scale':0,'size':20,'type':-5,'typeName':'BIGINT'},{'name':'remark','precision':100,'scale':0,'size':100,'type':12,'typeName':'VARCHAR'}],'num':10,'pageCount':2,'pageIndex':1,'pageSize':10,'rowCount':10,'rs':[['0005','spdb','浦发银行',10,'init',1459159402000,1459159402000,'初始化'],['0006','cmb','招商银行',11,'init',1459159402000,1459162069000,'测试11'],['0007','icbc','工商银行',12,'init',1459159402000,1459159402000,'初始化'],['0008','ccb','建设银行',13,'init',1459159402000,1459159402000,'初始化'],['0009','abc','农业银行',14,'init',1459159402000,1459159402000,'初始化'],['0010','cmbc','民生银行',15,'init',1459159402000,1459159402000,'初始化'],['0011','hxb','华夏银行',16,'init',1459159402000,1459159402000,'初始化'],['0012','cgb','广东发展银行',17,'init',1459159402000,1459159402000,'初始化'],['0013','cib','兴业银行',18,'init',1459159402000,1459159402000,'初始化'],['0014','ceb','光大银行',19,'init',1459159402000,1459159402000,'初始化']],'status':1,'totalCount':15}";
		ParaMap map = JsonUtils.strToMap(json1);
		System.out.println("map = " + map);
		String json2 = "[{'name':'id','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'no','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'name','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'bank_type','precision':4,'scale':0,'size':4,'type':4,'typeName':'INT'},{'name':'creator','precision':50,'scale':0,'size':50,'type':12,'typeName':'VARCHAR'},{'name':'createtime','precision':20,'scale':0,'size':20,'type':-5,'typeName':'BIGINT'},{'name':'updatetime','precision':20,'scale':0,'size':20,'type':-5,'typeName':'BIGINT'},{'name':'remark','precision':100,'scale':0,'size':100,'type':12,'typeName':'VARCHAR'}]";
		List list = JsonUtils.strToList(json2);
		System.out.println("list = " + list);
	}
}
