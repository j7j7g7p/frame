package com.base.mq;

import com.base.utils.ParaMap;

public abstract class MQSender {

	public abstract void send(String queue, ParaMap sendContent)
			throws Exception;
}
