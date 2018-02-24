package com.base.mq;

import com.base.mq.kafka.KafkaSender;
import com.base.mq.rabbit.RabbitSender;
import com.base.utils.ParaMap;
import com.base.web.AppConfig;

public class MQSendUtils {
	private static MQSender sender = null;

	private static MQSender getSender() {
		if (sender == null) {
			String mqType = AppConfig.getStringPro("mqType");
			if ("kafka".equals(mqType))
				sender = new KafkaSender();
			else if ("rabbit".equals(mqType))
				sender = new RabbitSender();
		}
		return sender;
	}

	public static void send(String queue, ParaMap sendContent) throws Exception {
		MQSender sender = getSender();
		sender.send(queue, sendContent);
	}

	public static void sendKafka(String queue, ParaMap sendContent)
			throws Exception {
		MQSender sender = new KafkaSender();
		sender.send(queue, sendContent);
	}

	public static void main(String[] args) throws Exception {
		String queue = "test3";
		ParaMap inMap = new ParaMap();
		long now = System.currentTimeMillis();
		inMap.put("id", now);
		send(queue, inMap);
		System.out.println(now);
	}
}
