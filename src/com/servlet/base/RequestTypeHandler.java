package com.servlet.base;

/**
 * ���������ת����
 * 
 * @author ken
 *
 */
public interface RequestTypeHandler<T> {

	/*
	 * ����ת������
	 */
	T string2Field(String s);
}
