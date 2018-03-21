package com.base.mq;

import java.util.Map;

import com.base.utils.ParaMap;

public abstract class MQSender {

	public abstract void send(String queue, ParaMap sendContent)
			throws Exception;
	
	public abstract void send(String queue, ParaMap sendContent,Map map)
			throws Exception;
}
