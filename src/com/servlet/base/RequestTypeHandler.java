package com.servlet.base;

/**
 * 请求的类型转换器
 * 
 * @author ken
 *
 */
public interface RequestTypeHandler<T> {

	/*
	 * 类型转换方法
	 */
	T string2Field(String s);
}
