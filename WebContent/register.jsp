<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>用户注册</title>
</head>
<body>
	<form action="UserServlet?method=register" method="post">
		用户名:<input name="username"/><br/>
		密码:<input name="password"/><br/>
		姓名:<input name="name"/><br/>
		年龄:<input name="age"/><br/>
		生日:<input name="birthday"/><br/>
		<button type="submit">注册</button>
	</form>
</body>
</html>