package test2;

import com.base.utils.ParaMap;
import com.base.weixin.BaseAuthWeiXinService;

public class Test2 {

	public  static  String  appid="wx5fcdae1c96e91941";
	public  static  String  appsecret="72f42abd07bd44a36d20a3d8b6fb0f4e";
	public static void main(String[] args) {
		ParaMap inMap=new ParaMap();
		BaseAuthWeiXinService weixinService=new BaseAuthWeiXinService();
		inMap.put("redirectUrl", "http://www.szkljy.com/wechat/parkbees/unbindcarnum.html");
		inMap.put("wxappid", appid);
		inMap.put("scope","snsapi_base");
		ParaMap outMap=weixinService.getOauthUrl(inMap);
		System.out.println(outMap);
		//testestes
   
	}

}
