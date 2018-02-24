package com.base.mq;

import com.alibaba.fastjson.JSONObject;

public abstract class MessageRece {

	public abstract String appName();

	public abstract String queue();

	public abstract void doAction(JSONObject inMap) throws Exception;

}
