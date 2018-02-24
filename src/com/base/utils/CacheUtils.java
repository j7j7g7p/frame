package com.base.utils;

import com.base.web.AppConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class CacheUtils {
	static ParaMap map = new ParaMap();

	static JedisPool pool;

	private static synchronized Jedis getJedis() {
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(5);
			config.setMaxTotal(500);
			config.setTestOnBorrow(true);
			String ip = AppConfig.getStringPro("redisIP");
			int port = AppConfig.getIntPro("redisPort");
			int timeout = AppConfig.getIntPro("redisTimeout");
			String auth = AppConfig.getStringPro("redisAuth");
			pool = new JedisPool(config, ip, port, timeout, auth);
		}
		Jedis client = pool.getResource();
		return client;
	}

	private static void returnClient(Jedis client) {
		pool.returnResourceObject(client);
	}

	public static void set(String key, String value) throws Exception {
		if (StrUtils.isNull(key))
			throw new Exception("key is empty");
		if (StrUtils.isNull(value))
			throw new Exception("value is empty");
		if (AppConfig.localCache()) {
			map.put(key, value);
		} else {
			Jedis client = getJedis();
			client.set(key, value);
			returnClient(client);
		}
	}

	public static void setTTL(String key, String value, int seconds)
			throws Exception {
		if (StrUtils.isNull(key))
			throw new Exception("key is empty");
		if (StrUtils.isNull(value))
			throw new Exception("value is empty");
		if (AppConfig.localCache()) {
			map.put(key, value);
		} else {
			Jedis client = getJedis();
			client.set(key, value);
			client.expire(key, seconds);
			returnClient(client);
		}
	}

	public static String get(String key) {
		if (AppConfig.localCache()) {
			return map.getString(key);
		} else {
			Jedis client = getJedis();
			String value = client.get(key);
			returnClient(client);
			return value;
		}

	}

	public static void remove(String key) {
		if (AppConfig.localCache()) {
			map.remove(key);
		} else {
			Jedis client = getJedis();
			client.del(key);
			returnClient(client);
		}
	}

	public static boolean check(String key, String value) {
		String value1 = get(key);
		if (value1.equals(value))
			return true;
		else
			return false;
	}

	public static void main(String[] args) throws Exception {
		// String u = "aaksajdfklaf";
		// String ut = CacheUtils.get(AccessCheck.getT(u));
		// System.out.println(ut);
		String value = "(我们是123)%%28";
		value = "<>!$&'()*+,-./:;=?@_~%#[]";
		value = "()=?&+";
		String v2 = java.net.URLEncoder.encode(value, "UTF-8");
		System.out.println(v2);
	}
}
