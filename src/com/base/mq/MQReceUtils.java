package com.base.mq;

import com.base.mq.kafka.KafkaReceiver;
import com.base.mq.rabbit.RabbitReceiver;
import com.base.web.AppConfig;

public class MQReceUtils {
	private static MQReceiver receiver = null;

	private static MQReceiver getReceiver() {
		if (receiver == null) {
			String mqType = AppConfig.getStringPro("mqType");
			if ("kafka".equals(mqType))
				receiver = new KafkaReceiver();
			else if ("rabbit".equals(mqType))
				receiver = new RabbitReceiver();
		}
		return receiver;
	}

	//添加rece
	public static void addRece(MessageRece rece) {
		getReceiver().addRece(rece);
	}

	//启动所有接收器
	public static void startRece() {
		getReceiver().startRece();
	}

	//销毁所有资源
	public static void destroy() {
		getReceiver().destroy();

	}

	public static void main(String[] args) throws Exception {

	}

}
