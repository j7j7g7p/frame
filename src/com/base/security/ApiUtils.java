package com.base.security;

import java.util.ArrayList;
import java.util.List;

import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.web.AppConfig;

public class ApiUtils {
	private static ThreadLocal<String> secretKeyThreadLocal = new ThreadLocal<String>();
	private static List<ApiKeyBean> keyList = new ArrayList<ApiKeyBean>();

	public static void notify(ParaMap map) {
		if (map != null && map.getRecordCount() > 0) {
			List<ApiKeyBean> keyListParam = new ArrayList<ApiKeyBean>();
			ApiKeyBean bean;
			for (int i = 0, size = map.getRecordCount(); i < size; i++) {
				bean = new ApiKeyBean();
				bean.id = map.getRecordString(i, "id");
				bean.comId = map.getRecordString(i, "comid");
				bean.clientType = map.getRecordString(i, "clienttype");
				bean.appId = map.getRecordString(i, "appid");
				bean.secretKey = map.getRecordString(i, "secretkey");
				bean.sourceIp = map.getRecordString(i, "sourceip");
				bean.memo = map.getRecordString(i, "memo");
				bean.creator = map.getRecordString(i, "creator");
				keyListParam.add(bean);
			}
			keyList = keyListParam;
		}
	}

	public static void initSecretKey(ParaMap inMap) throws Exception {
		String appId = inMap.getString("appId");
		String secretkey = "";
		if (StrUtils.isNull(appId)) {
			secretkey = AppConfig.getStringPro("md5Key");
		} else {
			if (!AppConfig.release())
				return;
			boolean bkeyList = keyList == null || keyList.size() == 0;
			if (bkeyList)
				throw new Exception("keyList为空！");
			for (ApiKeyBean bean : keyList) {
				if (appId.equals(bean.appId)) {
					secretkey = bean.secretKey;
					break;
				}
			}
			if (StrUtils.isNull(secretkey))
				throw new Exception("[" + appId + "]私钥非法!");
		}
		secretKeyThreadLocal.set(secretkey);

	}

	public static ApiKeyBean find(String appId) {
		for (ApiKeyBean bean : keyList) {
			if (appId.equals(bean.appId))
				return bean;
		}
		return null;
	}

	public static String getSecretKey() {
		return secretKeyThreadLocal.get();
	}

	public static void main(String[] args) throws Exception {
		//		ParaMap inMap = new ParaMap();
		//		initSecretKey(inMap);
		System.out.println(getSecretKey());

	}
}
