package com.servlet.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间的类型转换器
 * 
 * @author ken
 *
 */
public class DateTypeHandler implements RequestTypeHandler<Date> {

	private String format;

	public DateTypeHandler(String format) {
		this.format = format;
	}

	public DateTypeHandler() {
		this.format = "yyyy-MM-dd";
	}

	@Override
	public Date string2Field(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}
}
