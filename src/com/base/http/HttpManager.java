package com.base.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import com.base.utils.CharsetUtils;
import com.base.utils.StreamUtils;
import org.apache.log4j.Logger;

public class HttpManager {
	private static final Logger log = Logger.getLogger(HttpManager.class);

	static {
		try {
			TrustManager[] trustAllCerts = new TrustManager[1];
			trustAllCerts[0] = new SSLTrustManager();
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String getData(String uri, String content) throws Exception {
		URL url = new URL(uri);
		HttpURLConnection urlConn = null;
		String outContent;
		try {
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setRequestMethod("POST");
			urlConn.setUseCaches(false);
			urlConn.setReadTimeout(30000);
			urlConn.setRequestProperty("Accept-Charset", CharsetUtils.utf);
			urlConn.setRequestProperty("contentType", CharsetUtils.utf);
			urlConn.connect();
			OutputStream outs = urlConn.getOutputStream();
			outs.write(content.getBytes(CharsetUtils.utf));
			InputStream ins = urlConn.getInputStream();
			outContent = StreamUtils.InputStreamToString(ins);
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				urlConn.disconnect();
			} catch (Exception ex1) {
			}
		}
		return outContent;

	}

	/**
	 * 获取请求返回内容
	 * 
	 * @param uri
	 *            请求地址
	 * @param params
	 *            更多的参数，目前仅处理headers以添加头信息
	 * @return
	 */
	public static byte[] getDataByte(String uri, Map params) {
		log.debug("getData_url:" + uri);
		long begin = System.currentTimeMillis();
		byte[] buf = null;
		try {
			URL url = new URL(uri);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setRequestMethod("POST");
			urlConn.setUseCaches(false);
			urlConn.setReadTimeout(30000);
			urlConn.setRequestProperty("Accept-Charset", CharsetUtils.utf);
			urlConn.setRequestProperty("contentType", CharsetUtils.utf);
			if (params != null) {
				Map headers = (Map) params.get("headers");
				if (headers != null) {
					// 检查有无头信息
					Iterator it = headers.keySet().iterator();
					while (it.hasNext()) {
						String name = it.next().toString();
						String value = String.valueOf(headers.get(name));
						urlConn.setRequestProperty(name, value);
					}
				}
			}
			InputStream ins = urlConn.getInputStream();
			buf = StreamUtils.InputStreamToByte(ins);
			if (params != null) {
				Map<String, String> headers = getHttpResponseHeaders(urlConn);
				if (headers != null && headers.size() > 0)
					params.put("responseHeaders", headers);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		log.debug("end_url: time(" + (System.currentTimeMillis() - begin)
				+ ") size(" + buf.length + ")b");
		return buf;
	}

	/**
	 * 获取请求返回内容
	 * 
	 * @param uri
	 *            请求地址
	 * @return
	 */
	public static byte[] getDataByte(String uri) {
		return getDataByte(uri, null);
	}

	/**
	 * 获取请求返回字符串内容
	 * 
	 * @param uri
	 *            请求地址
	 * @param params
	 *            更多的参数，目前仅处理headers以添加头信息
	 * @return
	 */
	public static String getDataString(String uri, Map params) {
		byte[] buf = getDataByte(uri, params);
		return new String(buf);
	}

	/**
	 * 获取请求返回字符串内容
	 * 
	 * @param uri
	 *            请求地址
	 * @return
	 */
	public static String getDataString(String uri) {
		return getDataString(uri, null);
	}

	private static Map<String, String> getHttpResponseHeaders(
			HttpURLConnection urlConn) throws UnsupportedEncodingException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = urlConn.getHeaderField(i);
			if (mine == null)
				break;
			result.put(urlConn.getHeaderFieldKey(i), mine);
		}
		return result;
	}

	public static void main(String[] args) {
		// String
		// cc=getDataString("http://192.168.5.240/std/data?module=portal&service=Query&method=queryCanton");
		// System.out.println(cc);
		// System.out.println(CharsetUtils.getEncoding(cc));
		// String url =
		// "http://192.168.5.240/std/download?module=trademan&service=DocConfig&method=directDownload";
		// url += "&path=config\\济南市国土资源土地储备交易中心\\门户文档\\成交动画.html";
		// byte[] buf = getDataByte(url);

	}
}
