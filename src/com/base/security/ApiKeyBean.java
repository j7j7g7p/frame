package com.base.security;

import com.alibaba.fastjson.JSONObject;

public class ApiKeyBean {
	public String id;
	public String comId;
	public String appId;
	public String secretKey;
	public String clientType;
	public String sourceIp;
	public String memo;
	public String creator;
	
	@Override
	public String toString() {
		return JSONObject.toJSON(this).toString();
	}
}


