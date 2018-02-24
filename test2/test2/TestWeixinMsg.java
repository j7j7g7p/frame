package test2;

import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import com.base.weixin.BaseAuthWeiXinService;

public class TestWeixinMsg {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		for (int i = 0; i < 15; i++) {
			
			long start=DateUtils.nowTime();
			ParaMap outMap=new ParaMap();
			outMap.put("accessToken", "kVj5dTtQWVUPt-yw67VuIVKemADGUZYk1Pc0xB5jvtlP0p63l7d2Ke4lRuAGrvsLRA0b0isjJM8u1tW3HKFRvy7Fw1hrTGZEyUDNHB6FtI45KZ93FGtq3WdgF-fWkrJPPWMhAAAAHX");
			outMap.put("touser", "ooF1Ew-YaJGVbUY0YB5ATazNnD_8");	//接受消息用户的OPENID
			outMap.put("templateId", "tvzv2GW3SN60sk4uRBtZ1G2cm9y7tBybVCp8o5ekMsQ"); //行业模板id 在微信公众平台后台申请设置
			//outMap.put("url", "");//点击详情的url 没有传空字符串
			ParaMap dataMap=new ParaMap(); //内容参数
			dataMap.putAll(addItem("first",    "亲爱的张三，您的爱车<a>粤B2365C</a>已进入停车场，欢迎使用蜜蜂服务！\n", "#000000"));//首行内容
			dataMap.putAll(addItem("keyword1", "财富港停车场", "#000000"));  //停车场名称
			dataMap.putAll(addItem("keyword2", "2016-09-19 12:00", "#000000"));   //入场时间
			dataMap.putAll(addItem("remark", "测测嗯嗯嗯哈哈啊哈哈哈哈哈哈哈哈啊哈哈 ", "#D2691E")); //备注
			outMap.put("data", dataMap);
			BaseAuthWeiXinService baseWeiXin = new BaseAuthWeiXinService();
			outMap = baseWeiXin.sendTemplateMessge(outMap);
			long end=DateUtils.nowTime();
			System.out.println("请求时间:"+(end-start));
			System.out.println(outMap);
		}
    	
	}
	
	/**
	 * 
	 * addItem(用于微信模板消息添加消息)
	 * @param key
	 * @param value
	 * @param color
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年9月7日 下午7:05:52
	 */
	public static ParaMap addItem(String key, String value, String color) {
		ParaMap outMap=new ParaMap();
		ParaMap dataMap=new ParaMap();
		dataMap.put("value", value);
		dataMap.put("color", color);
		outMap.put(key, dataMap);
		return outMap;
	}
}
