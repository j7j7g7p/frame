package com.base.service;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.base.annotation.Author;
import com.base.annotation.ClientType;
import com.base.annotation.Desc;
import com.base.annotation.Example;
import com.base.annotation.IO;
import com.base.annotation.Input;
import com.base.annotation.Location;
import com.base.annotation.Output;
import com.base.annotation.ServiceDesc;
import com.base.annotation.UpdateTime;
import com.base.annotation.Version;
import com.base.utils.ParaMap;
import com.base.utils.StreamUtils;
import com.base.web.filter.BaseFilter;

public class AnnotationsService extends BaseService {
	private ArrayList<Method> annotatedMethods = new ArrayList<Method>();

	public AnnotationsService() throws Exception {
		try {
			init();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private void init() throws Exception {
		Enumeration<URL> ps = Thread.currentThread().getContextClassLoader()
				.getResources("annotationInfo.txt");
		while (ps.hasMoreElements()) {
			InputStream in = null;
			try {
				in = ps.nextElement().openStream();
				addFileToMap(in);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			} finally {
				in.close();
			}
		}
	}

	private void addFileToMap(InputStream in) throws Exception {
		String json = StreamUtils.InputStreamToString(in);
		HashMap<String, ArrayList<String>> jsonMap = JSON.parseObject(json,
				new TypeReference<HashMap<String, ArrayList<String>>>() {
				});
		in.close();

		ArrayList<String> servicesList = jsonMap.get("services");
		ArrayList<String> methodList = jsonMap.get("methods");
		for (String servName : servicesList) {
			if (!servName.endsWith("Service") || !servName.contains(".")) {
				continue;
			}

			Class<?> serviceClazz = Class.forName(servName);

			Method[] methods = serviceClazz.getMethods();

			for (Method m : methods) {
				String mName = m.getName();
				if (methodList.contains(mName)) {
					annotatedMethods.add(m);
				}
			}
		}
	}

	public ParaMap findMethods(ParaMap inMap) throws Exception {
		Class[] supportAnnos = { Author.class, ClientType.class, Input.class,
				Output.class, Desc.class, Location.class }; // 增加搜索项
		ArrayList<String> requestParams = new ArrayList<String>();
		requestParams.add(inMap.getString("author"));
		String client = inMap.getString("client");
		if (client != null && client.toUpperCase().equals("ALL")) {
			client = null;
		}
		requestParams.add(client);
		requestParams.add(inMap.getString("input"));
		requestParams.add(inMap.getString("output"));
		requestParams.add(inMap.getString("desc"));
		requestParams.add(inMap.getString("loc"));

		// requestParams.add(inMap.getString("state")); //增加搜索项 - 2
		ArrayList<Method> resultMethods = new ArrayList<Method>();
		resultMethods.addAll(annotatedMethods);

		for (int i = 0; i < requestParams.size(); i++) {
			if (StringUtils.isNotEmpty(requestParams.get(i))) {
				ArrayList<Method> matchMethods = filterMethods(resultMethods,
						supportAnnos[i], requestParams.get(i));
				resultMethods.clear();
				resultMethods.addAll(matchMethods);
			}
		}

		String mName = inMap.getString("mname");
		// 格式
		ArrayList<ParaMap> resultList = new ArrayList<ParaMap>();
		for (Method m : resultMethods) {
			if (StringUtils.isEmpty(mName)
					|| m.getName().toLowerCase().contains(mName.toLowerCase())) {
				ParaMap item = new ParaMap();
				item.put("name", m.getName());
				item.put("class", m.getDeclaringClass().getName());
				resultList.add(item);
			}
		}

		ParaMap subInfoMap = new ParaMap();
		subInfoMap.put("methodsList", resultList);

		return subInfoMap;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Method> filterMethods(ArrayList<Method> orgList,
			Class annoClazz, String searchKey) throws Exception {// supportAnnos[i]
		ArrayList<Method> list = new ArrayList<Method>();
		for (Method method : orgList) {

			// Author的特殊处理。方法上没有就找类作者，都有的话以方法为准。
			Annotation anno = null;
			Class declaringClass = method.getDeclaringClass();
			if (method.isAnnotationPresent(annoClazz)) {
				anno = method.getAnnotation(annoClazz);
			} else if (annoClazz == Author.class
					&& declaringClass.isAnnotationPresent(annoClazz)) {
				anno = declaringClass.getAnnotation(annoClazz);
			} else if (annoClazz == Location.class
					&& declaringClass.isAnnotationPresent(annoClazz)) {
				anno = declaringClass.getAnnotation(annoClazz);
			} else {
				continue;
			}

			// 取注解的值
			Method v = annoClazz.getMethod("value");
			Object value = v.invoke(anno);

			// 筛选
			if (searchValue(searchKey, value)) {
				list.add(method);
			}

		}
		return list;
	}

	public ParaMap findAnnotations(ParaMap inMap) throws Exception {
		String moduleName = inMap.getString("moduleName");
		String className = inMap.getString("className");
		String methodName = inMap.getString("methodName");

		ParaMap paraMap = new ParaMap();
		paraMap.put("module", moduleName);
		paraMap.put("service", className);

		AnnotatedElement target;
		Class[] supportAnnos;
		if (StringUtils.isNotEmpty(methodName)) {
			paraMap.put("method", methodName);
			target = BaseFilter.getMethod(paraMap);
			supportAnnos = new Class[] { Author.class, ClientType.class,
					Input.class, Output.class, Desc.class, Example.class };
		} else {
			target = BaseFilter.getServiceInstance(paraMap).getClass();
			supportAnnos = new Class[] { Author.class, Version.class,
					UpdateTime.class, ServiceDesc.class };
		}

		ArrayList<ParaMap> annotationList = new ArrayList<ParaMap>();
		for (Class<? extends Annotation> annoClazz : supportAnnos) {
			if (target.isAnnotationPresent(annoClazz)) {
				ParaMap annoBean = new ParaMap();
				Annotation anno = target.getAnnotation(annoClazz);
				Method method = annoClazz.getMethod("value");

				annoBean.put("name", annoClazz.getSimpleName());

				Object value = method.invoke(anno);

				Object formatValue;

				try {

					formatValue = formatValue(annoClazz, value); // 处理注解值格式

					annoBean.put("value", formatValue);

					annotationList.add(annoBean);

				} catch (IllegalArgumentException ie) {
					throw new IllegalArgumentException(moduleName + "_"
							+ className + "_" + methodName + " 注解：" + annoClazz
							+ " 格式不正确.");
				}
			}
		}
		ParaMap outMap = new ParaMap();
		outMap.put("annotationList", annotationList);
		return outMap;
	}

	private ArrayList processValue(Object value) throws Exception {
		ArrayList list = new ArrayList();
		if (value instanceof IO[]) {
			IO[] tempValue = (IO[]) value;
			for (IO io : tempValue) {
				ParaMap ioMap = new ParaMap();
				ioMap.put("input", io.inPut());
				ioMap.put("output", io.outPut());
				list.add(ioMap);
			}
		} else if (value instanceof Object[]) {
			Object[] tempValue = (Object[]) value;
			for (Object obj : tempValue) {
				list.add(String.valueOf(obj));
			}
		} else {
			list.add(value);
		}
		return list;
	}

	private boolean searchValue(String key, Object value) throws Exception {
		ArrayList list = processValue(value);
		if (key != null) {
			key = key.toLowerCase();
		}
		return list.toString().toLowerCase().contains(key);
	}

	private Object formatValue(Class<? extends Annotation> annoClazz,
			Object value) throws Exception {
		ArrayList list = processValue(value);
		if (annoClazz == Input.class || annoClazz == Output.class) { // 隐含条件:值类型为String[]
			ArrayList nList = new ArrayList();
			for (Object o : list) {
				String s = o.toString();
				ParaMap inOut = new ParaMap();
				String[] tempParam = s.split(":");
				if (tempParam.length >= 3) {
					inOut.put("name", tempParam[0]);
					inOut.put("type", tempParam[1]);
					String paraDesc = "";
					for (int i = 2; i < tempParam.length; i++) {
						paraDesc += tempParam[i];
					}
					inOut.put("desc", paraDesc);
					nList.add(inOut);
				} else {
					throw new IllegalArgumentException();
				}
			}
			return nList;
		} else {
			return list;
		}
	}

	public ParaMap callMethod(ParaMap in) {

		return in;
	}
}
