package com.base.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.base.utils.CacheUtils;
import com.base.utils.DateUtils;
import com.base.utils.IDGenerator;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.utils.TokenUtils;

public class AccessCheck {
	static Logger log = Logger.getLogger(AccessCheck.class);
	public static String msg1 = "0x01 非法访问:用户信息为空";
	public static String msg2 = "0x03 非法访问:URL的MD5错误";
	public static String msg3 = "0x02 非法访问:令牌t信息错误";
	public static String msg4 = "0x04 非法访问:令牌v信息错误";
	public static String msg5 = "0x05 非法访问:没有令牌t或v";
	public static String msg6 = "0x06 重复访问:令牌t";

	public static List<String> synList = new ArrayList<String>();

	private static String getMsm(ParaMap inMap) {
		String module = inMap.getString("module");
		String service = inMap.getString("service");
		String method = inMap.getString("method");
		String mergeStr = module + "_" + service + "_" + method;
		return mergeStr;
	}

	public static void checkUV(ParaMap inMap) throws Exception {
		if (AppConfig.release()) {
			String u = inMap.getString("u");
			String v = inMap.getString("v");
			if (StrUtils.isNull(u))
				throw new Exception(msg1);
			if (StrUtils.isNull(v))
				throw new Exception(msg4);
			String uv = CacheUtils.get(getV(u));
			if (!uv.equals(v))
				throw new Exception(msg4);
		}

	}

	public static void checkUTV(ParaMap inMap) throws Exception {
		if (AppConfig.release()) {
			String md5 = inMap.md5();
			String sign = inMap.getString("sign");
			if (!md5.equals(sign)) {
				throw new Exception(msg2);
			}
			//
			String msm = getMsm(inMap);
			String u = inMap.getString("u");
			String t = inMap.getString("t");
			String v = inMap.getString("v");

			if (StrUtils.isNotNull(u)) {
				if (StrUtils.isNull(t) && StrUtils.isNull(v)) {
					throw new Exception(msg5);
				} else if (synList.contains(msm) || StrUtils.isNotNull(t)) {
					String ut = CacheUtils.get(getT(u));
					if (!ut.equals(t)) {
						boolean b = TokenUtils.checkToken(ut, t, ut);
						if (b)// t1与t2连续
							throw new Exception(msg6);
						else
							throw new Exception(msg3);
					}
				} else if (StrUtils.isNotNull(v)) {
					String uv = CacheUtils.get(getV(u));
					if (!uv.equals(v))
						throw new Exception(msg4);
				}
			}
		}
	}

	public static String getT(String u) {
		return "T-" + u;
	}

	public static String getV(String u) {
		return "V-" + u;
	}

	public static void login(ParaMap outMap) throws Exception {
		outMap.remove("t");
		outMap.remove("v");
		String u = outMap.getString("u");
		if (StrUtils.isNotNull(u)) {
			String tmpId = DateUtils.nowStrYYYYMMddHHmmssSSS()
					+ DateUtils.nowStrYYYYMMddHHmmssSSS();
			String ut = TokenUtils.getToken(u, tmpId);
			String uv = IDGenerator.newGUID();
			outMap.put("t", ut);
			outMap.put("v", uv);
			CacheUtils.set(getT(u), ut);
			CacheUtils.set(getV(u), uv);
		}
	}

	public static void logout(String u) {
		CacheUtils.remove(getT(u));
		CacheUtils.remove(getV(u));
	}

	public static String addToken(ParaMap inMap, ParaMap outMap)
			throws Exception {
		String ut = null;
		if (AppConfig.release()) {
			String inu = inMap.getString("u");
			String t = inMap.getString("t");
			if (StrUtils.isNotNull(inu) && StrUtils.isNotNull(t)) {
				ut = TokenUtils.getToken(inu, t);
				outMap.put("t", ut);
			}
		}
		return ut;
	}

	public static void main(String[] args) {
		try {
			throw new Exception(msg6);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}
}
