package jpush;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class Main {

	public static void main(String[] args)throws Exception {
		String master_secret = "69cabe3521b012f8f94a7f43";
		String app_key = "c34254c3039cf74ed472fe06";
		JPushClient jpushClient = new JPushClient(master_secret, app_key);
		Audience audience = null;
		//	        audience = Audience.registrationId("1517bfd3f7c6012ecdf");//ios
		//	        audience=Audience.tag("123_android");
		audience = Audience.alias("id123456");
		//	        audience = Audience.registrationId("170976fa8a81038e5ac");//android1
		String msg = audience.toJSON().toString() + System.currentTimeMillis();
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.all()).setAudience(audience)
				//	                .setMessage(Message.content(msg)).build();
				.setNotification(Notification.alert(msg)).build();
		PushResult result = jpushClient.sendPush(payload);
		System.out.println("Got result - " + result);
		///test
	}

}
