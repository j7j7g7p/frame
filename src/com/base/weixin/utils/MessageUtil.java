package com.base.weixin.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.base.annotation.Desc;
import com.base.annotation.Input;
import com.base.utils.DateUtils;
import com.base.utils.ParaMap;

/**
 * 消息工具类  用于自动回复各类消息 可参考文档http://mp.weixin.qq.com/wiki/1/6239b44c206cab9145b1d52c67e6c551.html
 * @ClassName: MessageUtil  
 * @date 2016年6月6日 上午10:49:29
 *
 */
public class MessageUtil {
	
	/**
	 * 消息类型：文本
	 */
	public static final String TEXT = "text";

	/**
	 * 消息类型：音乐
	 */
	public static final String MUSIC = "music";

	/**
	 * 消息类型：图文
	 */
	public static final String NEWS = "news";

	/**
	 * 消息类型：图片
	 */
	public static final String IMAGE = "image";

	/**
	 * 消息类型：链接
	 */
	public static final String LINK = "link";

	/**
	 * 消息类型：地理位置
	 */
	public static final String LOCATION = "location";

	/**
	 * 消息类型：音频
	 */
	public static final String VOICE = "voice";
	
	/**
	 * 消息类型：视频
	 */
	public static final String VIDEO = "video";

	/**
	 * 消息类型：推送
	 */
	public static final String EVENT = "event";

	/**
	 * 事件类型：subscribe(订阅)
	 */
	public static final String SUBSCRIBE = "subscribe";

	/**
	 * 事件类型：unsubscribe(取消订阅)
	 */
	public static final String UNSUBSCRIBE = "unsubscribe";

	/**
	 * 事件类型：CLICK(自定义菜单点击事件)
	 */
	public static final String CLICK = "CLICK";
	
	/**
	 * 事件类型：View(自定义菜单跳转事件)
	 */
	public static final String VIEW = "VIEW";

	/**
	 * 
	 * parseXml(解析微信发来的请求（XML）)
	 * @param request
	 * @return
	 * @throws Exception 
	 * @throws
	 * @author chenlin
	 * @date 2016年6月6日 上午10:49:53
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
		// 将解析结果存储在HashMap中
		Map<String, String> map = new HashMap<String, String>();

		// 从request中取得输入流
		InputStream inputStream = request.getInputStream();
		// 读取输入流
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		// 得到xml根元素
		Element root = document.getRootElement();
		// 得到根元素的所有子节点
		List<Element> elementList = root.elements();

		// 遍历所有子节点
		for (Element e : elementList)
			map.put(e.getName(), e.getText());

		// 释放资源
		inputStream.close();
		inputStream = null;

		return map;
	}
	
	@Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"content:String:回复的文本消息内容"
	})
	@Desc("回复文本消息")
	public static String replyTextMessage(ParaMap inMap){
		
    String result = "<xml>"
    		      + "<ToUserName><![CDATA[" + inMap.getString("fromUserName") + "]]></ToUserName>"
				  + "<FromUserName><![CDATA[" + inMap.getString("toUserName") + "]]></FromUserName>"
				  + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>"
				  + "<MsgType><![CDATA[text]]></MsgType>"
				  + "<Content><![CDATA["+inMap.getString("content")+"]]></Content>"
				  + "</xml>";
	  return result;
	}
	
	@Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"mediaId:String:通过素材管理中的接口上传多媒体文件，得到的id"
	})
	@Desc("回复图片消息")
	public static String replyImageMessage(ParaMap inMap){

		   String result = "<xml>"
				         + "<ToUserName><![CDATA["+inMap.getString("fromUserName")+"]]></ToUserName>" 
		                 + "<FromUserName><![CDATA[" + inMap.getString("toUserName") + "]]></FromUserName>" 
				         + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>" 
		                 + "<MsgType><![CDATA[image]]></MsgType>" 
				         + "<Image>" 
		                 + "<MediaId><![CDATA["+inMap.getString("mediaId")+"]]></MediaId>" 
				         + "</Image>" 
		                 + "</xml>";
		return result;
	}
	
	@Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"mediaId:String:通过素材管理中的接口上传多媒体文件，得到的id"
	})
	@Desc("回复语音消息")
	public static String replyVoiceMessage(ParaMap inMap){
	 String result="<xml>" 
				  + "<ToUserName><![CDATA["+inMap.getString("fromUserName")+"]]></ToUserName>" 
                  + "<FromUserName><![CDATA[" + inMap.getString("toUserName") + "]]></FromUserName>" 
                  + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>" 
                  + "<MsgType><![CDATA[voice]]></MsgType>" 
                  + "<Voice>" 
                  + "<MediaId><![CDATA["+inMap.getString("mediaId")+"]]></MediaId>" 
                  + "</Voice>" 
                  + "</xml>";
		return result;
	}
	
	@Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"mediaId:String:通过素材管理中的接口上传多媒体文件,得到的id",
		"title:String:视频标题",
		"description:String:视频描述"
	})
	@Desc("回复视频消息")
	public static  String replyVideoMessage(ParaMap inMap){
		String result="<xml>"
				    + "<ToUserName><![CDATA["+inMap.getString("fromUserName")+"]]></ToUserName>"
                    + "<FromUserName><![CDATA["+inMap.getString("toUserName")+"]]></FromUserName>"
                    + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>"
                    + "<MsgType><![CDATA[video]]></MsgType>"
                    + "<Video>"
                    + "<MediaId><![CDATA["+inMap.getString("mediaId")+"]]></MediaId>"
                    + "<Title><![CDATA["+inMap.getString("title")+"]]></Title>"
                    + "<Description><![CDATA["+inMap.getString("description")+"]]></Description>"
                    + "</Video> "
                    + "</xml>";
		return result;
	 }
	
	@Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"mediaId:String:通过素材管理中的接口上传多媒体文件，得到的id",
		"title:String:音乐标题",
		"description:String:音乐描述",
		"musicURL:String:音乐链接",
		"hqMusicUrl:String:高质量音乐链接，WIFI环境优先使用该链接播放音乐",
		"thumbMediaId:String:缩略图的媒体id，通过素材管理中的接口上传多媒体文件，得到的id",
		
	})
	@Desc("回复音乐消息")
    public static String replyMusicMessage(ParaMap inMap){
    	String result="<xml>"
    			    + "<ToUserName><![CDATA["+inMap.getString("fromUserName")+"]]></ToUserName>"
                    + "<FromUserName><![CDATA["+inMap.getString("toUserName")+"]]></FromUserName>"
                    + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>"
                    + "<MsgType><![CDATA[music]]></MsgType>"
                    + "<Music>"
                    + "<Title><![CDATA["+inMap.getString("title")+"]]></Title>"
                    + "<Description><![CDATA["+inMap.getString("description")+"]]></Description>"
                    + "<MusicUrl><![CDATA["+inMap.getString("musicURL")+"]]></MusicUrl>"
                    + "<HQMusicUrl><![CDATA["+inMap.getString("hqMusicUrl")+"]]></HQMusicUrl>"
                    + "<ThumbMediaId><![CDATA["+inMap.getString("thumbMediaId")+"]]></ThumbMediaId>"
                    + "</Music>"
                    + "</xml>";
    	
    	return  result;
    }
	
    
    @SuppressWarnings("unchecked")
    @Input({
		"fromUserName:String:接收方帐号 (收到的OpenID)",
		"toUserName:String:开发者微信号(公众号ID)",
		"articles:List:图文消息的集合【title:String:图文消息标题,description:String:图文消息描述,picUrl:String:图片链接,url:String:点击图文消息跳转链接】"
	})
	@Desc("回复多图文消息,最多10条")// 默认第一张图片为封面大图 //格式参考文档http://mp.weixin.qq.com/wiki/1/6239b44c206cab9145b1d52c67e6c551.html
    public static String replyNewsMessage(ParaMap inMap){
		List<ParaMap> paraMapList=inMap.getList("articles");
    	StringBuilder sb=new StringBuilder();
    	int articleCount=0;
    	if(!paraMapList.isEmpty()){
    		for (ParaMap article : paraMapList){
    			 sb.append("<item>"
                         + "<Title><![CDATA["+article.getString("title")+"]]></Title>"
                         + "<Description><![CDATA["+article.getString("description")+"]]></Description>"
                         + "<PicUrl><![CDATA["+article.getString("picUrl")+"]]></PicUrl>"
                         + "<Url><![CDATA["+article.getString("url")+"]]></Url>"
                         + "</item>");
			}
    		
    		articleCount=paraMapList.size();
    	}
    	  String  result="<xml>"
    			       + "<ToUserName><![CDATA["+inMap.getString("fromUserName")+"]]></ToUserName>"
                       + "<FromUserName><![CDATA["+inMap.getString("toUserName")+"]]></FromUserName>"
                       + "<CreateTime>" + DateUtils.nowTime() + "</CreateTime>"
                       + "<MsgType><![CDATA[news]]></MsgType>"
                       + "<ArticleCount>"+articleCount+"</ArticleCount>"
                       + "<Articles>"+sb.toString()+"</Articles>"
                       + "</xml>";
    	
    	return  result;
    }
    
    
    /**
     * 
     * replyWeiXinMessage(回复各类消息)
     * @param inMap  msgType  消息类型  text文本   image图片  voice语音 video视频 music音乐 news图文
     * @return 
     * @throws
     * @author chenlin
     * @date 2016年7月7日 下午7:42:03
     */
    public static String replyWeiXinMessage(ParaMap inMap){
    	String replyMessage=null;
    	String  msgType = inMap.getString("msgType");
		switch (msgType) {
		case "text":
			//如果回复文字消息
			replyMessage=replyTextMessage(inMap);
			break;
			
		case "image":
			//如果回复图片消息
			replyMessage=replyImageMessage(inMap);
			break;
			
			
		case "voice":
			//如果回复语音消息
			replyMessage=replyVoiceMessage(inMap);
			break;
			
			
		case "video":
			//如果回复视频消息
			replyMessage=replyVideoMessage(inMap);
			break;
			
		case "music":
			//如果回复音乐消息
			replyMessage=replyMusicMessage(inMap);
			break;
			
		case "news":
			//如果回复文字消息
			replyMessage=replyNewsMessage(inMap);
			break;

		default:
			break;
		}
    	return replyMessage;
    }
}
