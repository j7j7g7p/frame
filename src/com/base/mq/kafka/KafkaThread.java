package com.base.mq.kafka;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.base.ds.DataSourceManager;
import com.base.mq.MQReceiver;
import com.base.mq.MessageRece;
import com.base.utils.ParaMap;
import com.base.web.filter.AppFilter;

public class KafkaThread extends Thread {
	private Logger log = Logger.getLogger("mqOut");
	private MQReceiver receiver;
	private String topic;
	private long offset;
	private String value;

	public KafkaThread(MQReceiver receiver, String topic, long offset,
			String value) {
		this.receiver = receiver;
		this.topic = topic;
		this.offset = offset;
		this.value = value;
	}

	public void run() {
		try {
			log.info(value);
			MessageRece rece = receiver.receMap.get(topic);
			Object obj = JSON.parse(value);
			JSONObject jsonMap = (JSONObject) obj;
			rece.doAction(jsonMap);
			DataSourceManager.commit();
		} catch (Exception ex1) {
			try {
				DataSourceManager.rollback();
				AppFilter.app.saveMQException(offset, topic, value,
						ex1.getMessage());
				DataSourceManager.commit();
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

	}
}
