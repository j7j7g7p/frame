package com.base.weixin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.log.Logging;
import com.base.service.BaseService;
import com.base.utils.ParaMap;
import com.base.weixin.utils.MessageUtil;
import com.base.weixin.utils.SignUtil;

/**
 * 微信消息服务器核心请求类
 * @ClassName: BaseMessageService
 * @date 2016年7月7日 下午5:42:04
 *
 */
public abstract class BaseMessageService extends BaseService {
	
	public static Logging log = Logging.getLogging("BaseMessageService");

	/**
	 * 
	 * doAction(处理微信的请求)
	 * @param inMap 
	 * @throws IOException 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午5:44:08
	 */
	public void doAction(ParaMap inMap) throws IOException {
		log.info("开始接受微信访问" + inMap.toString());
		String requestMethod = getRequest().getMethod().toUpperCase();
		HttpServletRequest request = this.getRequest();
		HttpServletResponse response = this.getResponse();
		PrintWriter  out = response.getWriter();
		if("GET".equals(requestMethod)){
        	// 确认请求来自微信服务器 用于微信公众平台后台配置验证
        	log.info("微信get请求验证开始......");
        	// 微信加密签名
    		String signature = request.getParameter("signature");
    		// 时间戳
    		String timestamp = request.getParameter("timestamp");
    		// 随机数
    		String nonce = request.getParameter("nonce");
    		// 随机字符串
    		String echostr = request.getParameter("echostr");
    	    //请求校验,若是校验成功则原样返回echostr 表示接入成功 否则失败
    	    if(SignUtil.checkSignature(getWeiXinToken(), signature, timestamp, nonce)){
    	    	out.print(echostr);
    	    }
        }else if("POST".equals(requestMethod)){
        	//处理微信服务器的请求
        	 log.info("post请求处理开始......");
        	 out.print(processRequest(request));
        }
		out.flush();  
		out.close();

	}
	
	/**
	 * 微信请求验证Token与开发模式接口配置信息中的Token保持一致
	 * getWeiXinToken()
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午7:16:22
	 */
	public abstract String getWeiXinToken();
	
	/**
	 * 
	 * processRequest(处理微信Post请求)
	 * @param request
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:00:31
	 */
	public  String processRequest(HttpServletRequest request) {
		String responseXML = "";
		ParaMap outMap=null;
	 try {
		// xml请求解析
		Map<String, String> requestMap = MessageUtil.parseXml(request);
		// 发送方帐号（open_id）
		String fromUserName = requestMap.get("FromUserName");
		// 公众号帐号
		String toUserName = requestMap.get("ToUserName");
		// 消息类型
		String msgType = requestMap.get("MsgType");
		log.info("消息类型是:"+msgType);
		ParaMap inMap =new ParaMap();
		inMap.put("fromUserName", fromUserName);
		inMap.put("toUserName", toUserName);
		switch (msgType) {
		case "text":
			//处理文字请求
			inMap.put("content", requestMap.get("Content"));
			outMap=innerText(inMap);
			break;
			
		case "image":
			//图片消息
			inMap.put("picUrl", requestMap.get("PicUrl"));
			outMap=innerImage(inMap);
			break;
			
		case "voice":
			//语音消息
			inMap.put("mediaId", requestMap.get("MediaId"));
			inMap.put("format", requestMap.get("Format"));
			outMap=innerVoice(inMap);
			break;
			
		case "video":
			//视频消息
			inMap.put("mediaId", requestMap.get("MediaId"));
			inMap.put("thumbMediaId", requestMap.get("ThumbMediaId"));
			outMap=innerVideo(inMap);
			break;
			
		case "shortvideo":
			//小视频消息
			inMap.put("mediaId", requestMap.get("MediaId"));
			inMap.put("thumbMediaId", requestMap.get("ThumbMediaId"));
			outMap=innerShortVideo(inMap);
			break;
			
		case "location":
			//地理位置
			inMap.put("locationX", requestMap.get("Location_X"));
			inMap.put("locationY", requestMap.get("Location_Y"));
			inMap.put("label", requestMap.get("Label"));
			outMap=innerLocation(inMap);
			break;
			
		case "link":
			//链接消息
			inMap.put("title", requestMap.get("Title"));
			inMap.put("description", requestMap.get("Description"));
			inMap.put("url", requestMap.get("Url"));
			outMap=innerLink(inMap);
			break;
			
		case "event":
			//事件类型
			String strEventType = requestMap.get("Event");
			log.info("事件类型:"+strEventType);
			switch (strEventType) {
			case "subscribe":
				//关注
				if(requestMap.containsKey("EventKey")){
					//表示是用户未关注公众号时扫描带参数二维码 进行关注后的事件推送
					inMap.put("eventKey", requestMap.get("EventKey"));//事件KEY值，qrscene_为前缀，后面为二维码的参数值
				}
				outMap=innerSubscribeEvent(inMap);
				break;

			case "unsubscribe":
				//取消关注后,用户不会收到消息
				outMap=innerUnSubscribeEvent(inMap);
				break;
				
			case "CLICK":
				//点击菜单事件
				inMap.put("eventKey", requestMap.get("EventKey"));
				outMap=innerClickEvent(inMap);
				break;
				
			case "VIEW":
				//点击菜单跳转链接时的事件推送
				inMap.put("eventKey", requestMap.get("EventKey"));
				outMap=innerViewEvent(inMap);
				break;
				
			case "LOCATION":
				//上报地理位置事件
				inMap.put("latitude", requestMap.get("Latitude"));
				inMap.put("longitude", requestMap.get("Longitude"));
				outMap=innerLocationEvent(inMap);
				break;	
				
			case "SCAN":
				//扫描带参数二维码事件用户已关注时的事件推送
				inMap.put("eventKey", requestMap.get("EventKey"));
				outMap=innerScanEvent(inMap);
				break;	
				
			default:
				break;
			}
			

		default:
			break;
		}
			
		 
	    if(outMap!=null){
	    	outMap.put("fromUserName", fromUserName);
	    	outMap.put("toUserName", toUserName);
	    	responseXML=MessageUtil.replyWeiXinMessage(outMap);
		}
	   
		} catch (Exception e) {
			responseXML="";
		   log.error("处理微信post消息异常:"+e.getMessage());
		}
	  
	    log.info("处理请求 返回的消息内容是:"+responseXML);
	    
		return responseXML;
		
	}

	/**
	 * 
	 * innerText(处理用户发的文本消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               content        文字消息内容
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap innerText(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerImage(处理用户发的图片消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               picUrl         用户发送的图片的url
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap innerImage(ParaMap inMap){
		return null;
	}
	
	
	 
	/**
	 * 
	 * innerVoice(处理用户发的语音消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               mediaId        语音消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap innerVoice(ParaMap inMap){
		return null;
	}
	
	 
	/**
	 * 
	 * innerVideo(处理用户发的视频消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               mediaId        视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 *               thumbMediaId   视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap innerVideo(ParaMap inMap){
		return null;
	}
   
	/**
	 * 
	 * innerShortVideo(处理用户发的视频消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               mediaId        视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 *               thumbMediaId   视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap innerShortVideo(ParaMap inMap){
		return null;
	}

	
	/**
	 * 
	 * innerLocation(处理用户发的地理位置消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               locationX     地理位置维度。
	 *               locationY     地理位置经度
	 *               label         地理位置信息
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public  ParaMap  innerLocation(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerLink(处理用户发的链接消息)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               title          消息标题
	 *               description    消息描述
	 *               url            消息链接
	 *               
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月7日 下午6:51:47
	 */
	public ParaMap  innerLink(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerSubscribeEvent(处理用户关注事件)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               eventKey       如果有该字段  表示是用户未关注时扫描带参数二维码事件 事件KEY值，qrscene_为前缀，后面为二维码的参数值 没有表示是普通的关注事件
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:02:35
	 */
	public ParaMap  innerSubscribeEvent(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerUnSubscribeEvent(处理用户取消关注事件)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:02:35
	 */
	public ParaMap  innerUnSubscribeEvent(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerClick(处理用户点击菜单拉取消息时的事件)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               eventKey       事件KEY值，与自定义菜单接口中KEY值对应
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:12:55
	 */
	public ParaMap innerClickEvent(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerClick(处理用户点击菜单跳转链接时的事件)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               eventKey       事件KEY值，设置的跳转URL
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:12:55
	 */
	public ParaMap innerViewEvent(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerLocationEvent(处理用户处理用户上报地理位置事件)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               latitude       地理位置纬度
	 *               longitude      地理位置经度
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:12:55
	 */
	public ParaMap innerLocationEvent(ParaMap inMap){
		return null;
	}
	
	
	/**
	 * 
	 * innerScanEvent(扫描带参数二维码用户已关注时的事件推送)
	 * @param inMap  fromUserName   发送方帐号（open_id）
	 *               toUserName     公众号帐号
	 *               eventKey       事件KEY值，是一个32位无符号整数，即创建二维码时的二维码scene_id
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月8日 上午10:12:55
	 */
	public ParaMap innerScanEvent(ParaMap inMap){
		return null;
	}
	
	
}
