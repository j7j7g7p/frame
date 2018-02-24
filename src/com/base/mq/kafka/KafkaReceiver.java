package com.base.mq.kafka;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;

import com.base.mq.MQReceiver;
import com.base.mq.MessageRece;

public class KafkaReceiver extends MQReceiver {
	private Logger log = Logger.getLogger(KafkaReceiver.class);
	private KafkaConsumer<String, String> consumer;

	@Override
	public void startRece() {
		System.out.println("Kafka rece[" + receList.size() + "]...");
		Properties props = new Properties();
		props.put("group.id", KafkaConfig.groupId);
		props.put("bootstrap.servers", KafkaConfig.brokerList());
		props.put("enable.auto.commit", true);
		props.put("auto.offset.reset", "earliest");
		StringDeserializer deserializer = new StringDeserializer();
		consumer = new KafkaConsumer<>(props, deserializer, deserializer);
		//
		List<String> topicList = new ArrayList<String>();
		for (MessageRece rece : receList) {
			topicList.add(rece.queue());
		}
		consumer.subscribe(topicList);
		//
		while (true) {
			ConsumerRecords<String, String> records = consumer
					.poll(Long.MAX_VALUE);
			Iterator<ConsumerRecord<String, String>> it = records.iterator();
			while (it.hasNext()) {
				ConsumerRecord<String, String> record = it.next();
				long offset = record.offset();
				String topic = record.topic();
				String value = record.value();
				Thread t1 = new KafkaThread(this, topic, offset, value);
				t1.start();
			}

		}

	}

	@Override
	public void destroy() {
		consumer.close();
	}

}
