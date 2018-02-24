package com.base.mq.rabbit;

import java.io.IOException;

import com.base.ds.DataSourceManager;
import com.base.mq.MessageRece;
import com.base.web.filter.AppFilter;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

public class RabbitThread extends Thread {
	private static final Logger log = Logger.getLogger("mqOut");
	private Envelope envelope;
	private MessageRece rece;
	private Channel channel;
	private byte[] body;

	public RabbitThread(Channel channel, Envelope envelope, MessageRece rece,
			byte[] body) {
		this.channel = channel;
		this.envelope = envelope;
		this.rece = rece;
		this.body = body;
	}

	public void run() {
		String routingKey = envelope.getRoutingKey();
		long deliveryTag = envelope.getDeliveryTag();
		String message = new String(body);
		try {
			log.info("threadId:" + Thread.currentThread().getId()
					+ " routingKey:" + routingKey + " rece content["
					+ body.length + "]:" + message);
			Object obj = JSON.parse(message);
			JSONObject jsonMap = (JSONObject) obj;
			rece.doAction(jsonMap);
			DataSourceManager.commit();
			channel.basicAck(deliveryTag, false);
		} catch (Exception ex) {
			try {
				DataSourceManager.rollback();
				AppFilter.app.saveMQException(deliveryTag, rece.queue(),
						message, ex.getMessage());
				DataSourceManager.commit();
				channel.basicAck(deliveryTag, false);
			} catch (Exception e) {
				try {
					channel.basicReject(deliveryTag, true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			ex.printStackTrace();
		} finally {
		}
	}
}
