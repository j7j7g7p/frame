package com.base.ant;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Task;

public class VersionTask extends Task {
	private File file;

	public void setFile(File file) {
		this.file = file;
	}

	public void execute() {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String dateStr = df.format(new Date());
			FileUtils.writeStringToFile(file, dateStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
