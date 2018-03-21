package com.base.mq.rabbit;

import java.util.Map;

import org.apache.log4j.Logger;

import com.base.mq.MQSender;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RabbitSender extends MQSender {
	private static final Logger log = Logger.getLogger("mqIn");
	public static final String exchange = "parkCloud";// exchange1
	public static final String exchange_type = "direct";
	public static final String routingkey = "routingkey";

	private static Channel getChannel() throws Exception {
		ConnectionFactory factory = RabbitConfig.getConnectionFactory();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, exchange_type);
		return channel;
	}

	public void send(String queue, ParaMap sendContent) throws Exception {
		Channel channel = getChannel();
		//
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, exchange, routingkey + queue);
		//
		String content = sendContent.toString();
		if (StrUtils.isNull(content))
			throw new Exception("消息为空");
		channel.basicPublish(exchange, routingkey + queue,
				MessageProperties.PERSISTENT_TEXT_PLAIN, content.getBytes());
		log.info("send content:" + content);
		//
		channel.close();
		channel.getConnection().close();
	}
	
	public void send(String queue, ParaMap sendContent,Map map) throws Exception {
		Channel channel = getChannel();
		//
		channel.queueDeclare(queue, true, false, false, map);//map延时参数
		channel.queueBind(queue, exchange, routingkey + queue);
		//
		String content = sendContent.toString();
		if (StrUtils.isNull(content))
			throw new Exception("消息为空");
		channel.basicPublish(exchange, routingkey + queue,
				MessageProperties.PERSISTENT_TEXT_PLAIN, content.getBytes());
		log.info("send content:" + content);
		//
		channel.close();
		channel.getConnection().close();
	}
	
	
}
