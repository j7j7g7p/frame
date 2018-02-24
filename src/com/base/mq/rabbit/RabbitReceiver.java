package com.base.mq.rabbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.base.mq.MQReceiver;
import com.base.mq.MessageRece;
import com.base.utils.ParaMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitReceiver extends MQReceiver {
	private static final Logger log = Logger.getLogger(RabbitReceiver.class);
	public static final String exchange = "parkCloud";// exchange1
	public static final String exchange_type = "direct";
	public static List<Connection> connectionList = new ArrayList<Connection>();
	public static List<Channel> channelList = new ArrayList<Channel>();

	public void destroy() {
		for (Channel channel : channelList) {
			try {
				channel.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//
		for (Connection conn : connectionList) {
			try {
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 判断网络失效
	 * 
	 * @return
	 * @throws Exception
	 */

	public void startRece() {
		// destroy();
		log.info("Rabbit rece[" + receList.size() + "]...");
		try {
			ConnectionFactory factory = RabbitConfig.getConnectionFactory();
			// factory.setRequestedHeartbeat(5);
			// factory.setConnectionTimeout(5000);
			factory.setAutomaticRecoveryEnabled(true);
			// factory.setTopologyRecoveryEnabled(true);
			Connection connection = factory.newConnection();
			connectionList.add(connection);
			for (final MessageRece rece : receList) {
				log.info("startRece ..." + rece.getClass().getName());
				final Channel channel = connection.createChannel();
				channelList.add(channel);
				channel.exchangeDeclare(exchange, exchange_type);
				channel.queueDeclare(rece.queue(), true, false, false, null);
				//
				channel.addShutdownListener(new ShutdownListener() {
					public void shutdownCompleted(ShutdownSignalException cause) {
						log.info("channel.shutdownCompleted:"
								+ cause.getMessage());
					}
				});
				//
				boolean autoAck = false;
				channel.basicQos(1);
				channel.basicConsume(rece.queue(), autoAck, rece.queue()
						+ "Tag", new DefaultConsumer(channel) {
					public void handleDelivery(String consumerTag,
							Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						RabbitThread thread = new RabbitThread(channel,
								envelope, rece, body);
						thread.start();
					}
				});
			}
		} catch (Exception ex) {
			log.info("startRece failed....");
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ParaMap inMapOrder = new ParaMap();
		inMapOrder.put("orderid", "orderid111");
		// inMapOrder.put("parkinglotid", "parkinglotid111");
		// inMapOrder.put("entryid", "entryid111");
		// inMapOrder.put("platenumber", "platenumber111");
		// inMapOrder.put("inouttime", "inouttime111测试");
		System.out.println("ok");
	}

}
