package com.base.mq.kafka;

import java.util.Properties;

import kafka.producer.BaseProducer;
import kafka.producer.NewShinyProducer;

import org.apache.commons.lang.StringUtils;

import com.base.mq.MQSendUtils;
import com.base.mq.MQSender;
import com.base.utils.ParaMap;

public class KafkaSender extends MQSender {

	public static BaseProducer producer = null;

	public static BaseProducer init() {
		if (producer == null) {
			Properties props = new Properties();
			props.put("bootstrap.servers", KafkaConfig.brokerList());
			props.put("key.serializer",
					"org.apache.kafka.common.serialization.ByteArraySerializer");
			props.put("value.serializer",
					"org.apache.kafka.common.serialization.ByteArraySerializer");
			props.put("request.required.acks", "1");
			props.put("client.id", "scala-producer");
			props.put("producer.type", "sync");
			producer = new NewShinyProducer(props);
		}
		return producer;

	}

	@Override
	public void send(String queue, ParaMap sendContent) throws Exception {
		BaseProducer producer = init();
		String key = sendContent.getString("key");
		if (StringUtils.isEmpty(key))
			key = "";
		String value = sendContent.toString();
		producer.send(queue, key.getBytes(), value.getBytes());
	}

	public static void main(String[] args) throws Exception {
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			ParaMap map = new ParaMap();
			map.put("key", "key1");
			map.put("p1", "..." + i + "...");
			map.put("p2", "..." + System.currentTimeMillis() + "..");
			System.out.println(map);
			MQSendUtils.sendKafka("traOrderTemp", map);
		}
		long end = System.currentTimeMillis();
		System.out.println("ms:" + (end - begin));

	}
}
