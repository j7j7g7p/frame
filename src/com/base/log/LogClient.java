package com.base.log;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.base.http.HttpManager;
import com.base.utils.ParaMap;
import com.base.web.AppConfig;

public class LogClient extends TimerTask {
	private static Timer timer = new Timer();

	private static Stack<ParaMap> stack = new Stack();

	public static void addLog(ParaMap log) {
		stack.push(log);
	}

	public static void start() {
		LogClient logClient = new LogClient();
		timer.scheduleAtFixedRate(logClient, 0, 1000);
	}

	@Override
	public void run() {
		try {
			if (stack.isEmpty())
				return;
			StringBuffer sb = new StringBuffer();
			while (!stack.isEmpty()) {
				ParaMap logBean = stack.pop();
				sb.append(logBean.toString());
			}
			String content = sb.toString();
			String url = AppConfig.getStringPro("logUrl")
					+ "/upClient?module=base&service=LogServer&method=write";
			HttpManager.getData(url, content);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ParaMap map = new ParaMap();
		map.put("rid", System.currentTimeMillis());
		map.put("levle", "INFO");
		map.put("content", "...AAA...");
		map.put("catalog", "bill");
		LogClient.addLog(map);
		LogClient.start();
		//		Thread.sleep(Integer.MAX_VALUE);
		//		String content = "汉字测试ABCD:" + System.currentTimeMillis();
		//		String uri = "http://127.0.0.1:8080/pc/upClient?module=demo&service=LogServer&method=write";
		//		String res = HttpManager.getData(uri, content);

	}

}
