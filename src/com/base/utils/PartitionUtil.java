package com.base.utils;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

public class PartitionUtil {
	static ConcurrentSkipListMap<Integer, Integer> map = new ConcurrentSkipListMap<Integer, Integer>();

	/*
	 * 增加分区的时候，得保障分裂的大分区待消费的队列为空
	 */
	public static void main(String[] args) {
		init();
		for (int i = 0; i < 100; i++) {
			String code = "abc" + i;
			int partitionNum = getPartition(code);
			System.out.println(code + ":" + partitionNum);
		}
	}

	public static void init() {
		map.put(1024, 4);
		map.put(878, 3);
		map.put(732, 2);
		map.put(586, 2);
		map.put(440, 1);
		map.put(295, 1);
		map.put(150, 0);
	}

	public static int getPartition(String content) {
		Entry<Integer, Integer> higher = map.higherEntry(hash(content));
		if (higher == null) {
			higher = map.higherEntry(0);
		}
		return higher.getValue();
	}

	public static int hash(String content) {
		if (content == null) {
			return 0;
		}
		return Math.abs(content.hashCode()) % 1024;
	}

	public static int createNewNode(int virNode, int realNode) {
		map.put(virNode, realNode);
		return map.size();
	}

	public static String getAllNode(int virNode, int realNode) {
		map.put(virNode, realNode);
		return map.toString();
	}
}
