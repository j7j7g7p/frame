package com.base.service;

import com.base.utils.ParaMap;

public class TestService extends BaseService {
	public ParaMap t1(ParaMap inMap) throws Exception {
		ParaMap outMap = new ParaMap();
		outMap.put("ts", System.currentTimeMillis());
		outMap.put("context", this.getRequest().getRealPath("/"));
		return outMap;
	}
}
