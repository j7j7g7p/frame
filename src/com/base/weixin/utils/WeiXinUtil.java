package com.base.weixin.utils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.alibaba.fastjson.JSONObject;
import com.base.annotation.Desc;
import com.base.annotation.Input;
import com.base.annotation.Output;
import com.base.log.Logging;
import com.base.utils.ParaMap;



/**
 * 
 * @ClassName: WeiXinUtil
 * @Desc: 微信公众平台工具类
 * @date 2016年6月6日 上午9:43:39
 *
 */
public class WeiXinUtil {
	
	public static Logging log = Logging.getLogging("weiXinUtil");
	
	
	// 获取access_token的接口地址（GET） 限200（次/天）                     
	  public final static String access_token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	
	//获取获得jsapi_ticket的接口地址(GET) 有效期7200秒
	  public final static String get_jsapi_ticket_url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	
	//上传多媒体文件接口地址                                                                              
	  public final static String upload_media_url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
	 
	  //下载多媒体文件
	  public final static String download_media_url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
	  
	//上传图文消息素材接口地址         
	  public final static String upload_news_url = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=ACCESS_TOKEN";
	  
	 //根据分组进行群发消息接口地址
	 public final static  String send_message_url="https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=ACCESS_TOKEN";
	  
	//网页授权地址
	 public final static  String oauth_user_Info="https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
	  
	//通过code换取网页授权access_token与openid
	 public final static  String get_user_openId="https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	  
	  
	 //上传图文消息内的图片获取URL
	 public final static String upload_image_url="https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN";
	  
	  
	 //预览图文消息接口
	 public final static  String preview_message_url="https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token=ACCESS_TOKEN";
	  
	 //发送模板消息url
	 public final static String template_send_url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
	  
	 //根据openID 网页授权获取用户的个人详细信息
	 public final static  String get_user_Info="https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		
	 //用户关注之后根据openId 获取用户的个人详细信息
	 public final static  String get_attention_user_Info="https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
	  
	 //菜单创建（post）
	 public final static String menu_create_url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
		
	 //菜单查询（GET）
	 public final static String menu_get_url = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
		
	 //菜单删除（GET）
	 public final static String menu_delete_url = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
	 
	 //发送客服消息
	 public final static String send_customer_url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";
	
	 //创建带参数的二维码
	 public final static String  qrcode_create_url="https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
	 
	   @Output({
			"state:String:执行返回标识(1成功,2失败)",
			"msg:String:返回中文说明",
			"access_token:String:获取到的凭证,access_token的有效期目前为2个小时，需定时刷新",
			"expires_in:int:凭证有效时间，单位：秒"
		})
	    @Input({
		    "appid:String:微信公众号的Appid",
		    "appSecret:String:微信公众号的AppSecret}"
	    })
		@Desc("获取公众号的接口凭证,access_token是公众号的全局唯一票据，公众号调用各接口时都需使用access_token")
		public static ParaMap getAccessToken(String appid,String appSecret){
			ParaMap outMap = new ParaMap();
			String requestUrl = access_token_url.replace("APPID", appid).replace("APPSECRET", appSecret);
			JSONObject jsonObject = HttpRequestUtil.httpRequest(requestUrl, "GET", null);
			if (null != jsonObject && !jsonObject.containsKey("errcode")) {
				outMap.put("state", 1);	
				outMap.put("access_token", jsonObject.getString("access_token"));
				outMap.put("expires_in", jsonObject.getIntValue("expires_in"));
			} else {
				outMap.put("state", 2);
				outMap.put("msg", "获取微信token失败");
			}
			return outMap;
		}
	
	    
	    @Input({
		    "accessToken:String:微信公众号接口凭证"
	    })
	    @Output({
			"state:String:执行返回标识(1成功,2失败)",
			"msg:String:返回中文说明",
			"ticket:String:公众号用于调用微信JS接口的临时票据,正常情况下，jsapi_ticket的有效期为7200秒，需定时刷新",
			"expires_in:int:凭证有效时间，单位：秒"
		})
		@Desc("获取JSSDK接口的临时票据")
		public static ParaMap getJSTicket(String accessToken) {
			ParaMap outMap = new ParaMap();
			String ticketUrl = get_jsapi_ticket_url.replace("ACCESS_TOKEN", accessToken);
			JSONObject jsonObject = HttpRequestUtil.httpRequest(ticketUrl, "GET", null);
			if (null != jsonObject && jsonObject.getIntValue("errcode") == 0) {
				outMap.put("state", 1);	
				outMap.put("jsapi_ticket", jsonObject.getString("ticket"));
				outMap.put("expires_in", jsonObject.getIntValue("expires_in"));
			} else {
				outMap.put("state", 2);
				outMap.put("msg", "获取ticket失败");
			}
			return outMap;
		}
		
		/**
		 * 
		 * uploadMedia(上传多媒体文件 本地文件)
		 * @param accessToken   接口调用凭证
		 * @param type          媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb，主要用于视频与音乐格式的缩略图）
		 * @param file          上传的文件对象
		 * @return              返回null表示上传失败
		 * @throws
		 * @author chenlin
		 */
		public static String uploadMedia(String accessToken, String type, File file) {
			String url = upload_media_url.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);
			String mediaId=null;
			try {
				HttpPostUtil post = new HttpPostUtil(url);
				post.addParameter("media", file);
				String s = post.send();
				JSONObject jsonObject = JSONObject.parseObject(s);
				if(null!=jsonObject){
					if(jsonObject.containsKey("media_id")){
						mediaId=jsonObject.getString("media_id");
					}
					log.info("上传媒体文件：jsonObject"+jsonObject.toString());
				}
				
			} catch (Exception e) {
				mediaId=null;
				log.error("上传媒体文件失败 "+e.getMessage());
			}
			return mediaId;
		}
		
	   /**
	    * 
	    * uploadFileByInputStream(上传文件，根据二进制流上传)
	    * @param accessToken     接口调用凭证
	    * @param type            媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb，主要用于视频与音乐格式的缩略图）
	    * @param fileName        上传文件名
	    * @param content         上传文件二进制流
	    * @return 
	    * @throws
	    * @author chenlin
	    * @date 2016年6月15日 下午6:05:35
	    */
	   public static String  uploadFileByInputStream(String accessToken, String type,String fileName,byte[] content){
			String mediaId=null;  
	        String url = upload_media_url.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);  
	        // 定义数据分割符  
	        String boundary = "----------tianba";  
	        try {  
	            URL uploadUrl = new URL(url);  
	            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();  
	            uploadConn.setDoOutput(true);  
	            uploadConn.setDoInput(true);  
	            uploadConn.setRequestMethod("POST");  
	            // 设置请求头Content-Type  
	            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);  
	            // 获取媒体文件上传的输出流（往微信服务器写数据）  
	            OutputStream outputStream = uploadConn.getOutputStream();  
	            // 请求体开始  
	            outputStream.write(("--" + boundary + "\r\n").getBytes());  
	            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"%s\"\r\n", fileName).getBytes());  
	            outputStream.write(String.format("Content-Type: %s\r\n\r\n", "application/octet-stream").getBytes());  
				outputStream.write(content);
	            // 请求体结束  
	            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());  
	            outputStream.close();  
	    
	            // 获取媒体文件上传的输入流（从微信服务器读数据）  
	            InputStream inputStream = uploadConn.getInputStream();  
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
	            StringBuffer buffer = new StringBuffer();  
	            String str = null;  
	            while ((str = bufferedReader.readLine()) != null) {  
	                buffer.append(str);  
	            }  
	            bufferedReader.close();  
	            inputStreamReader.close();  
	            // 释放资源  
	            inputStream.close();  
	            inputStream = null;  
	            uploadConn.disconnect();  
	            // 使用json解析  
	            JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
	            if(null!=jsonObject){
					if(jsonObject.containsKey("media_id")){
						mediaId=jsonObject.getString("media_id");
					}
					log.info("上传媒体文件：jsonObject"+jsonObject.toString());
				}
	            
	        } catch (Exception e) {  
	        	mediaId=null;
				log.error("上传媒体文件失败 "+e.getMessage());
	        }  
	        return mediaId;  
		}
		
		/**
		 * 
		 * uploadServerFile(上传多媒体文件 上传服务器文件 通过文件的url)
		 * @param accessToken   接口调用凭证
		 * @param type          媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb，主要用于视频与音乐格式的缩略图）
		 * @param fileUrl       文件在服务器的url路径
		 * @return              返回null表示上传失败
		 * @throws
		 * @author chenlin
		 */
		 public static String uploadServerFile(String accessToken, String type, String fileUrl) {  
			    String mediaId=null;  
		        String url = upload_media_url.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);  
		        // 定义数据分割符  
		        String boundary = "----------sunlight";  
		        try {  
		            URL uploadUrl = new URL(url);  
		            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();  
		            uploadConn.setDoOutput(true);  
		            uploadConn.setDoInput(true);  
		            uploadConn.setRequestMethod("POST");  
		            // 设置请求头Content-Type  
		            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);  
		            // 获取媒体文件上传的输出流（往微信服务器写数据）  
		            OutputStream outputStream = uploadConn.getOutputStream();  
		  
		            URL mediaUrl = new URL(fileUrl);  
		            HttpURLConnection meidaConn = (HttpURLConnection) mediaUrl.openConnection();  
		            meidaConn.setDoOutput(true);  
		            meidaConn.setRequestMethod("GET");  
		  
		            // 从请求头中获取内容类型  
		            String contentType = meidaConn.getHeaderField("Content-Type");  
		            String filename=getFileName(fileUrl,contentType);  
		            // 请求体开始  
		            outputStream.write(("--" + boundary + "\r\n").getBytes());  
		            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"%s\"\r\n", filename).getBytes());  
		            outputStream.write(String.format("Content-Type: %s\r\n\r\n", contentType).getBytes());  
		  
		            // 获取媒体文件的输入流（读取文件）  
		            BufferedInputStream bis = new BufferedInputStream(meidaConn.getInputStream());  
		            byte[] buf = new byte[1024 * 8];  
		            int size = 0;  
		            while ((size = bis.read(buf)) != -1) {  
		                // 将媒体文件写到输出流（往微信服务器写数据）  
		                outputStream.write(buf, 0, size);  
		            }  
		            // 请求体结束  
		            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());  
		            outputStream.close();  
		            bis.close();  
		            meidaConn.disconnect();  
		  
		            // 获取媒体文件上传的输入流（从微信服务器读数据）  
		            InputStream inputStream = uploadConn.getInputStream();  
		            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
		            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
		            StringBuffer buffer = new StringBuffer();  
		            String str = null;  
		            while ((str = bufferedReader.readLine()) != null) {  
		                buffer.append(str);  
		            }  
		            bufferedReader.close();  
		            inputStreamReader.close();  
		            // 释放资源  
		            inputStream.close();  
		            inputStream = null;  
		            uploadConn.disconnect();  
		            // 使用json解析  
		            JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
		            if(null!=jsonObject){
						if(jsonObject.containsKey("media_id")){
							mediaId=jsonObject.getString("media_id");
						}
						log.info("上传媒体文件：jsonObject"+jsonObject.toString());
					}
		            
		        } catch (Exception e) {  
		        	mediaId=null;
					log.error("上传媒体文件失败 "+e.getMessage());
		        }  
		        return mediaId;  
		    }  
		
		/**
		 * 在图文消息的具体内容中，将过滤外部的图片链接，开发者可以通过下述接口上传图片得到URL，放到图文内容中使用。
		 * uploadServerImage(上传图文消息内的图片获取URL)
		 * @param accessToken  接口调用凭证
		 * @param fileUrl      文件在服务器的url路径
		 * @return             返回null表示上传失败
		 * @throws
		 * @author chenlin
		 */
		 public static String uploadServerImage(String accessToken,String fileUrl) {  
			    String imageUrl=null;  
		        String url = upload_image_url.replace("ACCESS_TOKEN", accessToken);
		        // 定义数据分割符  
		        String boundary = "----------sunlight";  
		        try {  
		            URL uploadUrl = new URL(url);  
		            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();  
		            uploadConn.setDoOutput(true);  
		            uploadConn.setDoInput(true);  
		            uploadConn.setRequestMethod("POST");  
		            // 设置请求头Content-Type  
		            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);  
		            // 获取媒体文件上传的输出流（往微信服务器写数据）  
		            OutputStream outputStream = uploadConn.getOutputStream();  
		  
		            URL mediaUrl = new URL(fileUrl);  
		            HttpURLConnection meidaConn = (HttpURLConnection) mediaUrl.openConnection();  
		            meidaConn.setDoOutput(true);  
		            meidaConn.setRequestMethod("GET");  
		  
		            // 从请求头中获取内容类型  
		            String contentType = meidaConn.getHeaderField("Content-Type");  
		            String filename=getFileName(fileUrl,contentType);  
		            // 请求体开始  
		            outputStream.write(("--" + boundary + "\r\n").getBytes());  
		            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"%s\"\r\n", filename).getBytes());  
		            outputStream.write(String.format("Content-Type: %s\r\n\r\n", contentType).getBytes());  
		  
		            // 获取媒体文件的输入流（读取文件）  
		            BufferedInputStream bis = new BufferedInputStream(meidaConn.getInputStream());  
		            byte[] buf = new byte[1024 * 8];  
		            int size = 0;  
		            while ((size = bis.read(buf)) != -1) {  
		                // 将媒体文件写到输出流（往微信服务器写数据）  
		                outputStream.write(buf, 0, size);  
		            }  
		            // 请求体结束  
		            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());  
		            outputStream.close();  
		            bis.close();  
		            meidaConn.disconnect();  
		  
		            // 获取媒体文件上传的输入流（从微信服务器读数据）  
		            InputStream inputStream = uploadConn.getInputStream();  
		            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
		            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
		            StringBuffer buffer = new StringBuffer();  
		            String str = null;  
		            while ((str = bufferedReader.readLine()) != null) {  
		                buffer.append(str);  
		            }  
		            bufferedReader.close();  
		            inputStreamReader.close();  
		            // 释放资源  
		            inputStream.close();  
		            inputStream = null;  
		            uploadConn.disconnect();  
		            // 使用json解析  
		            JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
		            if(null!=jsonObject){
						if(jsonObject.containsKey("url")){
							imageUrl=jsonObject.getString("url");
						}
						log.info("上传媒体文件：jsonObject"+jsonObject.toString());
					}
		            
		        } catch (Exception e) {  
		        	imageUrl=null;
					log.error("上传媒体文件失败 "+e.getMessage());
		        }  
		        return imageUrl;  
		    }  
		 
		 @Input({
			    "accessToken:String:接口凭证",
				"articles:List:图文消息的集合【title:String:图文消息标题,"
				                        + "thumb_media_id:String:图文消息的封面图片素材id,"
				                        + "author:String:作者,digest:String:图文消息的摘要，仅有单图文消息才有摘要，多图文此处为空,"
				                        + "show_cover_pic:int:是否显示封面，0为false，即不显示，1为true，即显示,"
				                        + "content:String:图文消息的具体内容，支持HTML标签，必须少于2万字符，小于1M，且此处会去除JS"
				                        + "content_source_url:String:图文消息的原文地址，即点击“阅读原文”后的URL】"
		 })
		@Desc("上传图文消息素材")//格式参考文档 http://mp.weixin.qq.com/wiki/10/10ea5a44870f53d79449290dfd43d006.html
		public static  String uploadNewsMessage(String accessToken,ParaMap inMap){
			String url = upload_news_url.replace("ACCESS_TOKEN", accessToken);
			String mediaId=null;
			try {
				JSONObject jsonObject=HttpRequestUtil.httpRequest(url, "POST", JSONObject.toJSONString(inMap));
				if(null!=jsonObject){
					if(jsonObject.containsKey("media_id")){
						mediaId=jsonObject.getString("media_id");
					}
					log.info("上传图文消息素材：jsonObject"+jsonObject.toString());
				}
			} catch (Exception e) {
				mediaId=null;
				log.error("上传图文消息素材失败： "+e.getMessage());
			}
			return mediaId;
		}
		
		 @Input({
			    "accessToken:String:接口凭证",
			    "filter:Object:用于设定图文消息的接收者{is_to_all:boolean:用于设定是否向全部用户发送，值为true或false，选择true该消息群发给所有用户，选择false可根据tag_id发送给指定群组的用户,tag_id:String:群发到的标签的tag_id，参加用户管理中用户分组接口，若is_to_all值为true，可不填写tag_id}",
			    "mpnews:Object:用于设定即将发送的图文消息{media_id:String:用于群发的消息的media_id 通过上传图文消息接口得到}",
			    "msgtype:String 群发的消息类型，图文消息为mpnews，文本消息为text，语音为voice，音乐为music，图片为image，视频为video，卡券为wxcard"
		 })
		 @Desc("群发消息,返回0表示成功")
		 //格式参考文档http://mp.weixin.qq.com/wiki/14/0c53fac3bdec3906aaa36987b91d64ea.html
		public static int sendMessage(String accessToken,ParaMap inMap){	
			String url =send_message_url.replace("ACCESS_TOKEN", accessToken);
			int errcode=-1;
			JSONObject jsonObject =HttpRequestUtil.httpRequest(url, "POST", JSONObject.toJSONString(inMap));
			if(null!=jsonObject){
				log.info("群发图文消息:"+jsonObject.toString());
				errcode=jsonObject.getIntValue("errcode");
			}
			
			return errcode;
			
		}
		
		 @Input({
			    "accessToken:String:接口凭证",
			    "towxname:接受消息用户的微信号",
			    "mpnews:Object:用于设定即将发送的图文消息{media_id:String:用于群发的消息的media_id 通过上传图文消息接口得到}",
			    "msgtype:String 群发的消息类型，图文消息为mpnews，文本消息为text，语音为voice，音乐为music，图片为image，视频为video，卡券为wxcard"
		 })
		 @Desc("预览图文消息接口,返回0表示成功")//格式参考文档http://mp.weixin.qq.com/wiki/15/40b6865b893947b764e2de8e4a1fb55f.html
		public static int previewNewsMessage(String accessToken,ParaMap inMap){
			String url=preview_message_url.replace("ACCESS_TOKEN", accessToken);
			int errcode=-1;
			JSONObject jsonObject =HttpRequestUtil.httpRequest(url, "POST", JSONObject.toJSONString(inMap));
			if(null!=jsonObject){
				log.info("预览图文消息:"+jsonObject.toString());
				errcode=jsonObject.getIntValue("errcode");
			}
			
			return errcode;
		}
		
    /**
     * 
     * createOauthUrl(构造网页授权的url)
     * @param redirectUrl    授权成功之后跳转的url
     * @param appid          微信公众号的Appid",
     * @param scope          应用授权作用域，snsapi_base （不弹出授权页面，直接跳转，只能获取用户openid），snsapi_userinfo （弹出授权页面，可通过openid拿到昵称、性别、
     * @return 
     * @throws
     * @author chenlin
     */
	public static String createOauthUrl(String redirectUrl,String appid,String scope) {
		//url 编码
		redirectUrl=urlEncodeUTF8(redirectUrl);
		return oauth_user_Info.replace("APPID", appid).replace("REDIRECT_URI", redirectUrl).replace("SCOPE", scope);

	}

	/**
	 * 
	 * getUserOpenId(根据code获取网页授权token)
	 * 参考文档https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140842&token=&lang=zh_CN
	 * @param appid         微信公众号的Appid",
	 * @param appsecret     微信公众号秘钥
	 * @param code          换取access_token的票据，每次用户授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期。
	 * @return
	 * @throws
	 * @author chenlin
//	 */
	public static JSONObject getUserOpenId(String code,String appid,String appSecret) {
		String url = get_user_openId.replace("APPID", appid).replace("SECRET", appSecret).replace("CODE", code);
		return  HttpRequestUtil.httpRequest(url,"POST", null);
	}
	
	/**
	 * 
	 * urlEncodeUTF8(url 编码)
	 * @param url
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月6日 上午11:46:36
	 */
	public static String urlEncodeUTF8(String url){
		try {
			url=java.net.URLEncoder.encode(url,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	@Input({
	    "accessToken:String:接口凭证",
	    "touser:String:接受消息用户的OPENID",
	    "template_id:String:行业模板id",
	    "url:String :点击详情的url",
	    "data:List:模板消息内容 格式可参考文档http://mp.weixin.qq.com/wiki/12/bd383158b0f8435c07b8b6bc7cdbac9c.html"
    })
    @Desc("发送模板消息")
	public static boolean sendTemplate(String accessToken, ParaMap inMap) {
		String url = template_send_url.replace("ACCESS_TOKEN", accessToken);
		JSONObject jsonObject = HttpRequestUtil.httpRequest(url,"POST",JSONObject.toJSONString(inMap));
		log.info("发送模板消息的内容:"+JSONObject.toJSONString(inMap).toString());
		if(null != jsonObject){
			if(jsonObject.getIntValue("errcode") == 0){
				log.info("发送模板消息成功:"+jsonObject.toString());
				return true;
			}else{
				log.info("发送模板消息失败:"+jsonObject.toString());
			}
		}else{
			log.info("发送模板消息请求微信接口失败");
		}
		return  false;
	}
	
	@Input({
	    "accessToken:String:接口凭证",
	    "touser:String:接受消息用户的OPENID",
	    "msgtype:String:发送消息类型",
	    "text:object:{content:String 文字消息内容}消息内容  格式可参考文档https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140547&token=&lang=zh_CN"
    })
    @Desc("发送客服消息")
	//当用户主动发消息给公众号的时候开发者可以在(48小时内)调用该接口回复消息给用户
	public static boolean sendCustomerMessage(String accessToken, ParaMap inMap) {
		String url = send_customer_url.replace("ACCESS_TOKEN", accessToken);
		JSONObject jsonObject = HttpRequestUtil.httpRequest(url, "POST", JSONObject.toJSONString(inMap));
		if(null!=jsonObject){
			if(jsonObject.getIntValue("errcode") == 0){
				log.info("发送客服消息成功:"+jsonObject.toString());
				return true;
			}else{
				log.info("发送客服消息失败:"+jsonObject.toString());
			}
		}else{
			log.info("发送客服消息请求微信接口失败");
		}
		return false;
	}

	
	
	/**
	 * 
	 * getUserInfo(网页授权获取用户个人详细信息 scope为 snsapi_userinfo 时)
	 * @param openId       用户的唯一标识
	 * @param accessToken  网页授权接口调用凭证 与公众号接口凭证不一样
	 * @return 
	 * @throws
	 * @author chenlin
	 */
	public static JSONObject getUserInfo(String openId,String accessToken){
		String url = get_user_Info.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
		JSONObject jsonObject = HttpRequestUtil.httpRequest(url, "GET", null);
		if(null!=jsonObject){
			log.info("网页授权用户详细资料:jsonObject:"+jsonObject.toString());
		}
		return jsonObject;
	}
	
	
	/**
	 * 
	 * getAttentionUserInfo (调公众号接口用户个人详细信息  只有对已关注公众号的用户有效)
	 * @param openId        用户的唯一标识
	 * @param accessToken   公众号接口调用凭证
	 * @return 
	 * @throws
	 * @author chenlin
	 */
	public static JSONObject getAttentionUserInfo(String openId,String accessToken){
		String url = get_attention_user_Info.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
		JSONObject jsonObject = HttpRequestUtil.httpRequest(url, "GET", null);
		if(null!=jsonObject){
			log.info("调公众号接口用户详细资料:jsonObject:"+jsonObject.toString());
		}
		return jsonObject;
	}
	
	/**
	 * 下载文件
	 * 
	 * @param accessToken    接口凭证
	 * @param media_id       媒体文件ID 通过上传多媒体文件得到
	 * @return 返回字节数组
	 */
	public static byte[] downloadFile(String accessToken, String media_id) {
		String url = download_media_url.replace("ACCESS_TOKEN", accessToken).replace("MEDIA_ID", media_id);
		byte[] data = HttpRequestUtil.httpRequest_byte(url, "GET", null);
		return data;
	}
	

	@Input({
	    "accessToken:String:接口凭证",  
	    "button:String:菜单内容 json字符串，格式参考文档http://mp.weixin.qq.com/wiki/6/95cade7d98b6c1e1040cde5d9a2f9c26.html"
    })
    @Desc("创建菜单")
	public static boolean createMenu(String accessToken,String menuButtonJson){
		// 拼装创建菜单的url  
	    String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);  
	    // 调用接口创建菜单  
	    JSONObject jsonObject = HttpRequestUtil.httpRequest(url, "POST", menuButtonJson);  
	    if(null!=jsonObject){
	    	if(0==jsonObject.getIntValue("errcode")){
	    		return true;
	    	}else{
	    		log.error("创建菜单失败:jsonObject:"+jsonObject.toString());
	    	}
	    }
		return false;
	}
	
	
	/**
	 * 
	 * getMenu(查询菜单)
	 * @param accessToken  接口凭证
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月6日 下午2:52:36
	 */
	public static JSONObject getMenu(String accessToken){
		String requestUrl = menu_get_url.replace("ACCESS_TOKEN", accessToken);
		return HttpRequestUtil.httpRequest(requestUrl, "GET",null);
	}
	
	
	/**
	 * 
	 * deleteMenu(删除菜单)
	 * @param accessToken  接口凭证
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2016年7月6日 下午2:53:17
	 */
	public static boolean deleteMenu(String accessToken){
		// 拼装删除菜单的url  
	    String requestUrl = menu_delete_url.replace("ACCESS_TOKEN", accessToken);  
	    // 调用接口删除菜单  
	    JSONObject jsonObject = HttpRequestUtil.httpRequest(requestUrl, "GET", null);  
	    if(null!=jsonObject){
	    	if(0==jsonObject.getIntValue("errcode")){
	    		//删除成功
	    		return true;
	    	}else{
	    		log.info("删除菜单失败:jsonObject:"+jsonObject.toString());
	    	}
	    }
		return false;
	}
	
	@Input({
	    "accessToken:String :接口凭证",  
	    "expire_seconds:int :该二维码有效时间   此字段当二维码为临时可不传",
	    "action_name:String :二维码类型，QR_SCENE为临时,QR_LIMIT_SCENE为永久,QR_LIMIT_STR_SCENE为永久的字符串参数值",
        "action_info:Object :二维码详细信息 {scene_id:场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）"+
	                                      "scene_str:场景值ID（字符串形式的ID），字符串类型，长度限制为1到64，仅永久二维码支持此字段}"
    })
	@Output({
		"ticket:String:获取的二维码ticket，凭借此ticket可以在有效时间内换取二维码。",
		"expire_seconds:int:该二维码有效时间，以秒为单位。 最大不超过2592000（即30天）。",
		"url:String:二维码图片解析后的地址，开发者可根据该地址自行生成需要的二维码图片"
	})
	@Desc("创建二维码ticket")//可参考文档http://mp.weixin.qq.com/wiki/18/167e7d94df85d8389df6c94a7a8f78ba.html
	public static JSONObject createQrcode(String accessToken,ParaMap inMap){
		String requestUrl=qrcode_create_url.replace("ACCESS_TOKEN", accessToken);
		return HttpRequestUtil.httpRequest(requestUrl, "POST", JSONObject.toJSONString(inMap));
	}
	
	
	/**
	 * 
	 * getFileName(获取文件的后缀名)
	 * @param fileUrl         文件的url路径
	 * @param contentType     请求头中的内容类型
	 * @return 
	 * @throws
	 * @author chenlin
	 */
	 public static String getFileName(String fileUrl,String contentType) {  
	        String filename="";  
	        if (fileUrl != null && !"".equals(fileUrl)) {  
	        	filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1); 
	        	if (filename.contains("?")) {
            		filename = filename.substring(0, filename.indexOf("?"));
				}
	            if(!filename.contains(".")){  
	                if(contentType==null || "".equals(contentType)){  
	                    return "";  
	                }  
	                String fileExt="";  
	                if ("image/jpeg".equals(contentType)) {  
	                    fileExt = ".jpg";  
	                } else if ("audio/mpeg".equals(contentType)) {  
	                    fileExt = ".mp3";  
	                } else if ("audio/amr".equals(contentType)) {  
	                    fileExt = ".amr";  
	                } else if ("video/mp4".equals(contentType)) {  
	                    fileExt = ".mp4";  
	                } else if ("video/mpeg4".equals(contentType)) {  
	                    fileExt = ".mp4";  
	                } else if ("text/plain".equals(contentType)) {  
	                    fileExt = ".txt";  
	                } else if ("text/xml".equals(contentType)) {  
	                    fileExt = ".xml";  
	                } else if ("application/pdf".equals(contentType)) {  
	                    fileExt = ".pdf";  
	                } else if ("application/msword".equals(contentType)) {  
	                    fileExt = ".doc";  
	                } else if ("application/vnd.ms-powerpoint".equals(contentType)) {  
	                    fileExt = ".ppt";  
	                } else if ("application/vnd.ms-excel".equals(contentType)) {  
	                    fileExt = ".xls";  
	                }  
	                filename=filename+fileExt;  
	            }  
	        }  
	        
	        log.info("filename:"+filename);
	        return filename;  
	    }  
}
