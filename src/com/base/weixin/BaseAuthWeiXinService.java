package com.base.weixin;

import java.net.URLDecoder;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.base.annotation.Desc;
import com.base.annotation.Input;
import com.base.annotation.Output;
import com.base.service.BaseService;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;
import com.base.weixin.utils.SignUtil;
import com.base.weixin.utils.WeiXinUtil;

/**
 * 微信网页授权  JSSDK注入  创建菜单等
 * @ClassName: BaseWeiXinService
 * @date 2016年7月7日 下午2:22:31
 *
 */
public class BaseAuthWeiXinService extends BaseService{
	
	
	@Input({
	    "nowUrl:String:当前页面的URL 前端页面传递过来需要对url进行encodeURIComponent()编码",
	    "jsapiTicket:String:从缓存中获取公众号用于调用微信JS接口的临时票据 缓存的key值为 appid的值+“jsapi_ticket”",
		"wxappid:String:微信公众号appid"
    })
	@Output({ 
		"state:Int:状态值 1成功", 
		"appId:String:公众号的唯一标识" ,
		"timestamp:String:生成签名的时间戳" ,
		"nonceStr:String:生成签名的随机串" ,
		"signature:String:签名" 
		
	})
	@Desc("微信JSSDK注入权限验证配置")
	public  ParaMap  getJsSdkConfig(ParaMap inMap) throws Exception{
		ParaMap outMap=new ParaMap();
		String appid=inMap.getString("wxappid");
		String jsapiTicket=inMap.getString("jsapiTicket");
		String nowUrl=URLDecoder.decode(inMap.getString("nowUrl"), "UTF-8"); //对URL进行解码
		Map<String, String> map=SignUtil.jssdkSign(jsapiTicket, nowUrl);
		outMap.put("state", 1);
		outMap.put("appId", appid);
		outMap.put("timestamp", map.get("timestamp"));
        outMap.put("nonceStr", map.get("nonceStr"));
        outMap.put("signature", map.get("signature"));
        return outMap;
	}
	
	
	@Input({ 
		"redirectUrl:String:前端页面的回调地址",
		"wxappid:String:微信公众号appid",
		"scope:String:应用授权作用域，snsapi_base(不弹出授权页面，直接跳转，只能获取用户openid), snsapi_userinfo(弹出授权页面，可通过openid拿到昵称、性别等)"
	})
	@Output({ 
		"state:Int:状态值 1成功", 
		"oauthUrl:String:页面授权地址,用来获取用户信息" 
	})
	@Desc("创建微信网页授权URL")
	public ParaMap getOauthUrl(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String redirectUrl=inMap.getString("redirectUrl");
		String wxappid=inMap.getString("wxappid");
		String scope=inMap.getString("scope");
		String resultUrl=WeiXinUtil.createOauthUrl(redirectUrl,wxappid,scope);
		outMap.put("oauthUrl", resultUrl);
		outMap.put("state", 1);
		return outMap;
	}
	
	
	@Input({ 
		"code:String:换取access_token的票据",
		"wxappid:String:微信公众号appid",
		"wxappsecret:String:微信公众号应用密钥"
	})
	@Output({ 
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明",
		"openid:String:用户的openId",  
		"userinfo:用户详细资料 当应用授权作用域scope为snsapi_userinfo时返回"
	})
	@Desc("获取用户的openid")
	public ParaMap getWeiXinUserOpenId(ParaMap inMap) {
		ParaMap outMap = new ParaMap();
		String code = inMap.getString("code");
		String wxappid=inMap.getString("wxappid");
		String wxappsecret=inMap.getString("wxappsecret");
		if(StrUtils.isNotNull(code)){
			JSONObject jsonObject = WeiXinUtil.getUserOpenId(code,wxappid,wxappsecret);
			if (null != jsonObject && !jsonObject.containsKey("errcode")) {
				String openid = jsonObject.getString("openid");
				String scope=jsonObject.getString("scope");
				if("snsapi_userinfo".equals(scope)){
					//如果是需要用户同意授权获取用户的详细资料
					String accessToken=jsonObject.getString("access_token");
					JSONObject userInfo=WeiXinUtil.getUserInfo(openid, accessToken);
					outMap.put("userinfo", userInfo);
				}
				outMap.put("state", 1);
				outMap.put("openid", openid);
			}else{
				outMap.put("state", 2);
				outMap.put("msg", "获取失败");
			}
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "获取失败");
		}
		
		return outMap;
	}
	
	
	@Input({
	    "accessToken:String:接口凭证  从缓存中获取 缓存的key值为 appid的值+“access_token”",  
	    "menuButtonJson:String:菜单内容 json 字符串，格式参考文档http://mp.weixin.qq.com/wiki/6/95cade7d98b6c1e1040cde5d9a2f9c26.html"
    })
	@Output({ 
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明"
	})
    @Desc("创建自定义菜单")
	public ParaMap createWeiXinMenu(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String accessToken=inMap.getString("accessToken");
		String menuButtonJson=inMap.getString("menuButtonJson");
		Boolean bool=WeiXinUtil.createMenu(accessToken, menuButtonJson);
		if(bool){
			outMap.put("state", 1);
			outMap.put("msg", "创建菜单成功");
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "创建菜单失败");
		}
		return outMap;
	}
	
	@Input({
	    "accessToken:String:接口凭证  从缓存中获取 缓存的key值为 appid的值+“access_token”"
    })
	@Output({ 
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明",
		"data:String:菜单的json格式字符串"
	})
    @Desc("查询自定义菜单")
	public ParaMap getWeiXinMenu(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String accessToken=inMap.getString("accessToken");
		JSONObject jsonObject=WeiXinUtil.getMenu(accessToken);
		if (null != jsonObject && !jsonObject.containsKey("errcode")) {
			outMap.put("state", 1);
			outMap.put("data", jsonObject);
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "查询失败");
		}
		
		return outMap;
	}
	
	@Input({
	    "accessToken:String:接口凭证  从缓存中获取 缓存的key值为 appid的值+“access_token”"
    })
	@Output({ 
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明"
	})
    @Desc("删除自定义菜单")
	public ParaMap  deleteWeiXinMenu(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String accessToken=inMap.getString("accessToken");
		Boolean bool=WeiXinUtil.deleteMenu(accessToken);
		if(bool){
			outMap.put("state", 1);
			outMap.put("msg", "删除菜单成功");
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "删除菜单失败");
		}
		return outMap;
	}
	
	@Input({
	    "accessToken:String :接口凭证",  
	    "expireSeconds:int :该二维码有效时间    以秒为单位。 永久二维码此字段可不传  最大不超过2592000（即30天），此字段如果不填，则默认有效期为30秒。(选填)",
	    "actionName:String :二维码类型，QR_SCENE为临时,QR_LIMIT_SCENE为永久, QR_LIMIT_STR_SCENE 为永久的字符串参数值sceneSstr",
	    "sceneId:int :场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000",
	    "sceneSstr:String :场景值ID（字符串形式的ID），字符串类型，长度限制为1到64，仅永久二维码支持此字段 临时二维码此字段可不传 (选填)",
	})
	@Output({
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明",
		"url:String:二维码图片解析后的地址，开发者可根据该地址自行生成需要的二维码图片"
	})
	@Desc("创建二维码ticket")//可参考文档http://mp.weixin.qq.com/wiki/18/167e7d94df85d8389df6c94a7a8f78ba.html
	public ParaMap createQrcode(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String accessToken=inMap.getString("accessToken");
		ParaMap qrcodeMap=new ParaMap();
		if(inMap.containsKey("expireSeconds")){
			qrcodeMap.put("expire_seconds", inMap.getInt("expireSeconds"));
		}
		qrcodeMap.put("action_name", inMap.getString("actionName"));
		ParaMap sceneMap=new ParaMap();
		if(inMap.containsKey("sceneId")){
			sceneMap.put("scene_id", inMap.getInt("sceneId"));
			
		}
		if(inMap.containsKey("sceneSstr")){
			sceneMap.put("scene_str", inMap.getString("sceneSstr"));
		}
		ParaMap actionInfoMap=new ParaMap();
        actionInfoMap.put("scene", sceneMap);
		qrcodeMap.put("action_info", actionInfoMap);
		JSONObject jsonObject=WeiXinUtil.createQrcode(accessToken, qrcodeMap);
		if (null != jsonObject && !jsonObject.containsKey("errcode")) {
			outMap.put("state", 1);
			outMap.put("msg", "创建成功");
			outMap.put("url", jsonObject.getString("url"));
			
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "创建失败");
		}
		return outMap;
	}
	
	
	@Input({
	    "accessToken:String:接口凭证  从缓存中获取 缓存的key值为 appid的值+“access_token”",
		"openid:String:用户在公众号内的唯一标识"
    })
	@Output({ 
		"state:Int:状态值 1成功  2失败", 
		"msg:String:返回中文说明",
		"userinfo:String:用户详细资料"
	})
    @Desc("获取已经关注用户的详细资料") //参考文档http://mp.weixin.qq.com/wiki/17/c807ee0f10ce36226637cebf428a0f6d.html
	public ParaMap getAttentionUserInfo(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String openid=inMap.getString("openid");
		String accessToken=inMap.getString("accessToken");
		JSONObject jsonObject=WeiXinUtil.getAttentionUserInfo(openid, accessToken);
		if (null != jsonObject && !jsonObject.containsKey("errcode")) {
			outMap.put("state", 1);
			outMap.put("msg", "获取成功");
			outMap.put("userinfo", jsonObject);
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "获取失败");
		}
		
		return outMap;
		
	}
	
	@Input({
	    "accessToken:String:接口凭证",
	    "touser:String:接受消息用户的OPENID",
	    "templateId:String:行业模板id",
	    "url:String :点击详情的url",
	    "data:String:模板消息内容 格式可参考文档http://mp.weixin.qq.com/wiki/12/bd383158b0f8435c07b8b6bc7cdbac9c.html"
    })
    @Desc("发送模板消息")
	public  ParaMap  sendTemplateMessge(ParaMap inMap){
		ParaMap outMap = new ParaMap();
		String accessToken=inMap.getString("accessToken");
		ParaMap templateMap=new ParaMap();
		templateMap.put("touser", inMap.getString("touser"));
		templateMap.put("template_id", inMap.getString("templateId"));
		templateMap.put("url", inMap.getString("url"));
		templateMap.put("data", JSONObject.parse(inMap.getString("data")));
		Boolean bool=WeiXinUtil.sendTemplate(accessToken, templateMap);
		if(bool){
			outMap.put("state", 1);
			outMap.put("msg", "发送模板消息成功");
		}else{
			outMap.put("state", 2);
			outMap.put("msg", "发送模板消息失败");
		}
		return outMap;
		
	}
	
	
}
