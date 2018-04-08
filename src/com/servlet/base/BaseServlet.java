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
		// ��ȡ������Ҫ����ķ�������
		String method = request.getParameter("method");
		if (method == null) {
			System.out.println("����ķ������Ʋ���Ϊ�գ�����");
			return;
		}

		// ��õ�ǰServlet��Class����
		Class c = this.getClass();
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			// ��õ�ǰ���������η�
			int mod = m.getModifiers();

			// ����ķ��������ǹ��еĲ��ҷǾ�̬��
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
				// �ҵ�ָ���Ĵ�����
				if (m.getName().equals(method)) {

					// ����ɱ仯�Ĳ���
					Object[] params = parseParams(m, request, respnose);

					// ͨ��������ø÷������д���
					try {
						// ��÷���ֵ����ҳ�����ת
						String result = (String) m.invoke(this, params);

						if (result.startsWith("redirect:")) {
							// �ض���
							respnose.sendRedirect(result.split(":")[1]);
						} else {
							// ת��
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
	 * ��������Ĳ�������װ��ʵ������
	 * 
	 * @param request
	 * @return
	 */
	public <T> T parseRequest(HttpServletRequest request, Class<T> cls) {
		// ���ʵ�����Class����
		T t = null;
		try {
			t = cls.newInstance();
		} catch (Exception e1) {
			System.err.println(cls.getName() + "�б������һ���޲ι��췽��������������");
		}

		// �����ȡ����ʵ�����еĳ�Ա����
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			int mod = field.getModifiers();
			// ֻ����˽�С��Ǿ�̬����final������
			if (Modifier.isPrivate(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
				// ��Ȩ
				field.setAccessible(true);

				// ��ȡ��Ӧ�Ĳ�������
				String value = request.getParameter(field.getName());
				if (value != null) {
					// ���õ���Ӧ���ֶ���
					try {
						// ����������������ת��������
						Object v = null;

						// �ж��Ƿ���ת����
						Format format = field.getAnnotation(Format.class);
						if (format != null) {
							// ���������ת�� - ͨ���û��ṩ��ת�����Զ�ת��
							RequestTypeHandler requestTypeHandler = format.value().newInstance();
							v = requestTypeHandler.string2Field(value);

						} else {
							// ���������ת��
							if (field.getType() == String.class) {
								// �ַ�������
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
	 * ���������Ĳ����б�
	 * 
	 * @return
	 * @throws IOException
	 */
	private Object[] parseParams(Method m, HttpServletRequest request, HttpServletResponse respnose)
			throws IOException {
		List<Object> paramsList = new ArrayList<>();
		Class<?>[] types = m.getParameterTypes();// ��õ�ǰ�����Ĳ����б�����
		// ������ǰ�����б�����
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
				// ��ȡ��ǰ��ѯ��ҳ��
				String page = request.getParameter("page");
				if (page == null) {
					pageObj.setPage(1);
				} else {
					pageObj.setPage(Integer.parseInt(page));
				}
				// ��ȡ��ǰ������·��
				String uri = request.getRequestURI();
				pageObj.setUrl(uri);

				// ��ȡ��ǰ���������
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
