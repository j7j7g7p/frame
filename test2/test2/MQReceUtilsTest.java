package test2;

import com.alibaba.fastjson.JSONObject;
import com.base.mq.MQReceUtils;
import com.base.mq.MessageRece;

class A extends MessageRece {

	@Override
	public String appName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String queue() {
		return "test3";
	}

	@Override
	public void doAction(JSONObject inMap) throws Exception {
		System.out.println(inMap);

	}

}

public class MQReceUtilsTest {

	public static void main(String[] args) {
		MQReceUtils.addRece(new A());
		MQReceUtils.startRece();
	}

}
