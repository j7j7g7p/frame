<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>      
<!-- 分页导航 -->
	<div>
		<c:if test="${page.pageCount > 1 }">
			<!-- 总页码大于1再显示分页导航 -->
			<c:if test="${page.page == 1 }">
				<a>首页</a>
				<a>上一页</a>
			</c:if>
			<c:if test="${page.page > 1 }">
				<a href="${page.url }?${page.params }&page=1">首页</a>
				<a href="${page.url }?${page.params }&page=${page.page-1 }">上一页</a>
			</c:if>
			
			<c:if test="${page.page < page.pageCount }">
				<a href="${page.url }?${page.params }&page=${page.page+1 }">下一页</a>
				<a href="${page.url }?${page.params }&page=${page.pageCount }">尾页</a>
			</c:if>
			<c:if test="${page.page == page.pageCount }">
				<a>下一页</a>
				<a>尾页</a>
			</c:if>
			
			当前第${page.page }页/共${page.pageCount }页
		</c:if>
	</div>