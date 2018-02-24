package com.base.mq.rabbit;

import com.base.web.AppConfig;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitConfig {

	protected static ConnectionFactory getConnectionFactory() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		String host = AppConfig.getStringPro("mqIP");
		int port = AppConfig.getIntPro("mqPort");
		String username = AppConfig.getStringPro("mqUserName");
		String password = AppConfig.getStringPro("mqPassWord");
		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}

}
