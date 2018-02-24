package com.base.weixin.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.alibaba.fastjson.JSONObject;
import com.base.log.Logging;
/**
 * 用来发送Https请求
 *
 */
public class HttpRequestUtil {
	
	public static Logging log = Logging.getLogging("weiXinHttpRequestUtil");
	
	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		HttpsURLConnection httpUrlConn=null;
		try {
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpsURLConnection) url.openConnection();
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			System.setProperty("https.protocols", "TLSv1.2"); // jdk1.7默认是TSLv1, 但是可以支持TSLv1.1,TSLv1.2,jdk1.8默认是TSLv1.2
			httpUrlConn.setSSLSocketFactory(ssf);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			httpUrlConn.setConnectTimeout(5000);  //设置连接主机超时（单位：毫秒）
			httpUrlConn.setReadTimeout(5000);    //设置从主机读取数据超时（单位：毫秒）
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("utf-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			jsonObject=JSONObject.parseObject(buffer.toString());
		} catch (Exception e) {
			jsonObject=null;
			StringWriter sw = new StringWriter();
            PrintWriter pw =  new PrintWriter(sw);
            //将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            log.info("异常信息如下："+sw.toString());
		}finally{
			// 释放资源
			if(httpUrlConn!=null){
				httpUrlConn.disconnect();
			}
		}
		return jsonObject;
	}

	
	/**
	 * 发起https请求并获取字节数组结果
	 * @param requestUrl
	 * @param requestMethod
	 * @param data
	 * @return
	 */
	public static byte[] httpRequest_byte(String requestUrl, String requestMethod, byte[] data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			if ("GET".equals(requestMethod) && data != null && data.length > 0) {
				if (requestUrl.indexOf('?') > 0) {
					requestUrl += '&';
				} else {
					requestUrl += '?';
				}
				requestUrl += new String(data);
			}
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
			if (httpUrlConn instanceof HttpsURLConnection) {
				// 创建SSLContext对象，并使用我们指定的信任管理器初始化
				TrustManager[] tm = { new MyX509TrustManager() };
				SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
				sslContext.init(null, tm, new SecureRandom());
				// 从上述SSLContext对象中得到SSLSocketFactory对象
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				((HttpsURLConnection) httpUrlConn).setSSLSocketFactory(ssf);
			}
			boolean truePost ="POST".equals(requestMethod)&& data != null && data.length > 0;
			httpUrlConn.setDoOutput(truePost);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ( "GET".equals(requestMethod)) {
				httpUrlConn.connect();
			} else if (truePost) {
				// 提交数据
				OutputStream outputStream = httpUrlConn.getOutputStream();
				outputStream.write(data);
				outputStream.close();
			}

			// 读取返回数据
			InputStream inputStream = httpUrlConn.getInputStream();
			byte[] buf = new byte[1024 * 2];
			int len;
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			// 释放资源
			out.close();
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
		} catch (ConnectException ce) {
		} catch (Exception e) {
		}
		return out.toByteArray();
	}
	
	
	/**
	 * 
	 * getInputStream(从服务器获得一个输入流(本例是指从服务器获得一个image输入流))
	 * @param requestUrl  图片的url地址
	 * @return 
	 * @throws
	 * @author chenlin
	 * @date 2015年9月11日 上午9:58:48
	 */
	public static InputStream getInputStream(String requestUrl) {
		InputStream in = null;
		HttpURLConnection httpURLConnection = null;
		try {
			URL url = new URL(requestUrl);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			// 设置网络连接超时时间
			httpURLConnection.setConnectTimeout(5000);
			// 设置应用程序要从网络连接读取数据
			httpURLConnection.setDoInput(true);
			httpURLConnection.setRequestMethod("GET");
			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode == 200) {
				// 从服务器返回一个输入流
				InputStream inputStream = httpURLConnection.getInputStream();
				//得到图片的二进制数据，以二进制封装得到数据，具有通用性
				byte[] data = readInputStream(inputStream);
				in=new ByteArrayInputStream(data);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in;
	}
	
	 public static byte[] readInputStream(InputStream inStream){  
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
	        //创建一个Buffer字符串  
	        byte[] buffer = new byte[1024];  
	        //每次读取的字符串长度，如果为-1，代表全部读取完毕  
	        int len = 0;  
	        //使用一个输入流从buffer里把数据读取出来  
	        try {
				while( (len=inStream.read(buffer)) != -1 ){  
				    //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度  
				    outStream.write(buffer, 0, len);  
				}
			} catch (IOException e) {
				e.printStackTrace();
			}  
	        //关闭输入流  
	        try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}  
	        //把outStream里的数据写入内存  
	        return outStream.toByteArray();  
	    }  
}
