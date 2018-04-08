package com.servlet.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.servlet.annotation.Entity;
import com.servlet.annotation.Format;
import com.servlet.entity.Page;
import com.servlet.entity.User;

public class BaseServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse respnose)
			throws ServletException, IOException {
		// 获取请求需要处理的方法名称
		String method = request.getParameter("method");
		if (method == null) {
			System.out.println("请求的方法名称不能为空！！！");
			return;
		}

		// 获得当前Servlet的Class对象
		Class c = this.getClass();
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			// 获得当前方法的修饰符
			int mod = m.getModifiers();

			// 处理的方法必须是共有的并且非静态的
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
				// 找到指定的处理方法
				if (m.getName().equals(method)) {

					// 处理可变化的参数
					Object[] params = parseParams(m, request, respnose);

					// 通过反射调用该方法进行处理
					try {
						// 获得返回值进行页面的跳转
						String result = (String) m.invoke(this, params);

						if (result.startsWith("redirect:")) {
							// 重定向
							respnose.sendRedirect(result.split(":")[1]);
						} else {
							// 转发
							request.getRequestDispatcher(result).forward(request, respnose);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}

	/**
	 * 解析请求的参数，封装到实体类中
	 * 
	 * @param request
	 * @return
	 */
	public <T> T parseRequest(HttpServletRequest request, Class<T> cls) {
		// 获得实体类的Class对象
		T t = null;
		try {
			t = cls.newInstance();
		} catch (Exception e1) {
			System.err.println(cls.getName() + "中必须包含一个无参构造方法！！！！！！");
		}

		// 反射获取所有实体类中的成员变量
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			int mod = field.getModifiers();
			// 只处理私有、非静态、非final的属性
			if (Modifier.isPrivate(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
				// 授权
				field.setAccessible(true);

				// 获取对应的参数数据
				String value = request.getParameter(field.getName());
				if (value != null) {
					// 设置到响应的字段中
					try {
						// ？？？？存在类型转换的问题
						Object v = null;

						// 判断是否有转换器
						Format format = field.getAnnotation(Format.class);
						if (format != null) {
							// 特殊的类型转换 - 通过用户提供的转换器自动转换
							RequestTypeHandler requestTypeHandler = format.value().newInstance();
							v = requestTypeHandler.string2Field(value);

						} else {
							// 常规的类型转换
							if (field.getType() == String.class) {
								// 字符串类型
								v = value;
							} else if (field.getType() == Integer.class || field.getType() == int.class) {
								v = Integer.parseInt(value);
							} else if (field.getType() == Double.class || field.getType() == double.class) {
								v = Double.parseDouble(value);
							} else if (field.getType() == Float.class || field.getType() == float.class) {
								v = Float.parseFloat(value);
							} else if (field.getType() == Short.class || field.getType() == short.class) {
								v = Short.parseShort(value);
							} else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
								v = Boolean.parseBoolean(value);
							} else if (field.getType() == Long.class || field.getType() == long.class) {
								v = Long.parseLong(value);
							} else if (field.getType() == Character.class || field.getType() == char.class) {
								v = value.toCharArray()[0];
							} else if (field.getType() == Byte.class || field.getType() == byte.class) {
								v = Byte.parseByte(value);
							}
						}

						field.set(t, v);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return t;
	}

	/**
	 * 解析方法的参数列表
	 * 
	 * @return
	 * @throws IOException
	 */
	private Object[] parseParams(Method m, HttpServletRequest request, HttpServletResponse respnose)
			throws IOException {
		List<Object> paramsList = new ArrayList<>();
		Class<?>[] types = m.getParameterTypes();// 获得当前方法的参数列表类型
		// 遍历当前参数列表类型
		for (Class<?> type : types) {
			if (type == HttpServletRequest.class) {
				paramsList.add(request);
			} else if (type == HttpServletResponse.class) {
				paramsList.add(respnose);
			} else if (type == HttpSession.class) {
				paramsList.add(request.getSession());
			} else if (type == PrintWriter.class) {
				paramsList.add(respnose.getWriter());
			} else if (type.isAnnotationPresent(Entity.class)) {
				paramsList.add(parseRequest(request, type));
			} else if (type == Page.class) {
				Page<User> pageObj = new Page<>();
				// 获取当前查询的页码
				String page = request.getParameter("page");
				if (page == null) {
					pageObj.setPage(1);
				} else {
					pageObj.setPage(Integer.parseInt(page));
				}
				// 获取当前的请求路径
				String uri = request.getRequestURI();
				pageObj.setUrl(uri);

				// 获取当前的请求参数
				String params = request.getQueryString();
				if (params != null && params.trim() != "") {
					if (params.lastIndexOf("page") != -1) {
						params = params.substring(0, params.lastIndexOf("page") - 1);
					}
				} else {
					params = "1=1";
				}
				pageObj.setParams(params);
				paramsList.add(pageObj);
			} else {
				paramsList.add(null);
			}
		}

		return paramsList.toArray();
	}
}
