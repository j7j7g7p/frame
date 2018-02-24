package com.base.log;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

public class ExPatternLayout extends PatternLayout {
	public static String contextPath = "";

	public String format(LoggingEvent event) {
		String originalLog = super.format(event);
		String retval = contextPath + "--" + originalLog;
		return retval;
	}
}
