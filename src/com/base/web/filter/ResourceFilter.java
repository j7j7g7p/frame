package com.base.web.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.base.utils.CharsetUtils;
import com.base.web.ContentTypes;
import com.base.web.ResourceUtils;
import org.apache.commons.lang.StringUtils;

public class ResourceFilter extends BaseFilter {

	public static void doFilter(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		long ifModifiedSince = httpReq.getDateHeader("If-Modified-Since");
		ServletContext sc = httpReq.getSession().getServletContext();
		String uri = httpReq.getServletPath();
		String fileName = StringUtils.substring(uri, uri.lastIndexOf("/base/"));
		String extName = StringUtils.substring(uri, uri.lastIndexOf(".") + 1);
		String contentType = ContentTypes.getContentType(extName);
		httpRes.setContentType(contentType + ";charset=" + CharsetUtils.utf);
		long lastModified = ResourceUtils.getModified(sc, fileName);
		if (lastModified != ifModifiedSince) {
			byte[] buf = ResourceUtils.getBytes(sc, fileName);
			httpRes.setHeader("ETag", String.valueOf(lastModified));
			httpRes.setDateHeader("Expires", lastModified + 24 * 60 * 60 * 1000
					* 365);
			httpRes.setDateHeader("Last-Modified", lastModified);
			httpRes.setHeader("Cache-Control", "max-age=" + 24 * 60 * 60
					+ ",private");
			write(httpReq, httpRes, buf);
		} else {
			httpRes.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		}

	}

	public static void jsFilter(HttpServletRequest httpReq,
			HttpServletResponse httpRes, FilterChain filterChain)
			throws Exception {
		long ifModifiedSince = httpReq.getDateHeader("If-Modified-Since");
		ServletContext sc = httpReq.getSession().getServletContext();
		String uri = httpReq.getServletPath();
		String fileName = uri;
		String extName = StringUtils.substring(uri, uri.lastIndexOf(".") + 1);
		String contentType = ContentTypes.getContentType(extName);
		httpRes.setContentType(contentType + ";charset=" + CharsetUtils.utf);
		long lastModified = ResourceUtils.getModified(sc, fileName);
		if (lastModified != ifModifiedSince) {
			byte[] buf = ResourceUtils.getBytes(sc, fileName);
			httpRes.setHeader("ETag", String.valueOf(lastModified));
			httpRes.setDateHeader("Expires", lastModified + 24 * 60 * 60 * 1000
					* 365);
			httpRes.setDateHeader("Last-Modified", lastModified);
			httpRes.setHeader("Cache-Control", "max-age=" + 24 * 60 * 60
					+ ",private");
			write(httpReq, httpRes, buf);
		} else {
			httpRes.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		}

	}
}
