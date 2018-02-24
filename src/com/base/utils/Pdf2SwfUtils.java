package com.base.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.base.web.AppConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Pdf2SwfUtils {

	private static Logger log = Logger.getLogger(Pdf2SwfUtils.class);

	public static void pdf2swf(String pdfFullName, String swfFullDir)
			throws Exception {
		String exeName = AppConfig.getStringPro("pdf2swf");
		File exeFile = new File(exeName);
		if (!exeFile.exists())
			throw new Exception(exeName + " 文件不存在,请按照swftools.");
		File pdfFile = new File(pdfFullName);
		if (!pdfFile.exists())
			throw new Exception(pdfFullName + " 文件不存在");
		File swfDirFile = new File(swfFullDir);
		if (!swfDirFile.exists())
			FileUtils.forceMkdir(swfDirFile);
		swfDirFile.delete();
		FileUtils.forceMkdir(swfDirFile);
		String cmd = exeName + " " + pdfFullName + " -o " + swfFullDir
				+ "/Paper%.swf -f -T 9 -t -s storeallcharacters";
		Process process = Runtime.getRuntime().exec(cmd);
		final InputStream is1 = process.getInputStream();
		final InputStream is2 = process.getErrorStream();
		log.debug(pdfFullName + " 开始生成 " + swfFullDir + " ...");
		// 启动单独的线程来清空输出的缓冲区
		new Thread(new Runnable() {
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is1));
				try {
					String line = null;
					while ((line = br.readLine()) != null)
						log.debug(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		// 启动单独的线程来清空错误的缓冲区
		new Thread() {
			public void run() {
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						is2));
				try {
					String line = null;
					while ((line = br2.readLine()) != null) {
						log.debug(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}.start();
		process.waitFor();
		log.debug(pdfFullName + "...切割完成... ");
	}

	public static void main(String[] args) throws Exception {
		String pdfFullName = "C:\\TEMP\\长沙市国有建设用地使用权网上挂牌出让系统.pdf";
		pdfFullName = "C:\\TEMP\\025012360000000132.pdf";
		String swfFullDir = "C:\\TEMP\\swf\\12345678";
		pdf2swf(pdfFullName, swfFullDir);

	}

}
