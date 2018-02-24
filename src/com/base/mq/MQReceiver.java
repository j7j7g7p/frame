package com.base.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MQReceiver {

	public List<MessageRece> receList = new ArrayList<MessageRece>();
	public Map<String, MessageRece> receMap = new HashMap();

	//添加待消费的处理程序
	public void addRece(MessageRece rece) {
		receList.add(rece);
		receMap.put(rece.queue(), rece);
	}

	//
	public abstract void startRece();

	public abstract void destroy();

}
