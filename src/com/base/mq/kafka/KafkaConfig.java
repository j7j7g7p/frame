package com.base.mq.kafka;

import com.base.web.AppConfig;

public class KafkaConfig {

	public final static String groupId = "groupId1";

	public static String brokerList() {
		String brokerlist = AppConfig.getStringPro("kafka.bootstrap.servers");
		return brokerlist;
	}

}
