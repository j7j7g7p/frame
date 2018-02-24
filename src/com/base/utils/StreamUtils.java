package com.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class StreamUtils {
	private static final String charset = "UTF-8";
	final static int BUFFER_SIZE = 4096;

	/**
	 * 将InputStream转换成String
	 * 
	 * @param in
	 *            InputStream
	 * @return String
	 * @throws Exception
	 * 
	 */
	public static String InputStreamToString(InputStream in) throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
			outStream.write(data, 0, count);

		data = null;
		return new String(outStream.toByteArray(), charset);
	}

	/**
	 * 将String转换成InputStream
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static InputStream StringToInputStream(String in) throws Exception {

		ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes(charset));
		return is;
	}

	/**
	 * 将InputStream转换成byte数组
	 * 
	 * @param in
	 *            InputStream
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] InputStreamToByte(InputStream in) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
			outStream.write(data, 0, count);

		data = null;
		outStream.close();
		return outStream.toByteArray();
	}

	/**
	 * 将byte数组转换成InputStream
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static InputStream byteToInputStream(byte[] in) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(in);
		return is;
	}

	/**
	 * 将byte数组转换成String
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static String byteToString(byte[] in) throws Exception {
		InputStream is = byteToInputStream(in);
		return InputStreamToString(is);
	}

	public static int getLen(List<String> lines) {
		int count = 0;
		for (int i = 1; i < lines.size(); i++) {
			int offset = Integer.parseInt(lines.get(i).split(",")[0]);
			int len = Integer.parseInt(lines.get(i).split(",")[1]);
			if (offset != count)
				return offset;
			else {
				count = offset + len;
			}

		}
		return count;
	}

	public static void main(String[] args) throws Exception {
		// List<String> lines = FileUtils.readLines(new
		// File("c:/2992668B3CEA42818D91E28B6D16EDA2.pdf.cfg"));
		// System.out.println(getLen(lines));

		long begin = System.currentTimeMillis();
		File file = new File("c:/test.dat");
		long count = 1024 * 1024 * 1024 * 2;
		RandomAccessFile r = new RandomAccessFile(file, "rw");
		r.setLength(count);
		r.close();

		// FileOutputStream fos = new FileOutputStream(file);
		// FileChannel fileChannel = fos.getChannel();
		// for (int i = 0; i < count / (1024 * 1024); i++) {
		// ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
		// fileChannel.write(buffer);
		// System.out.println("..." + i);
		// }
		// fileChannel.close();
		// fos.close();
		long end = System.currentTimeMillis();
		System.out.println(end - begin);

	}

	public static void main2(String[] args) throws Exception {
		int count = 99;
		byte[] buf = new byte[count];
		File file = new File("c:/aa.txt");
		FileUtils.writeByteArrayToFile(file, buf);
		FileOutputStream fos;
		String s1;
		FileChannel fc;
		ByteBuffer bb;
		int len = 0;

		//
		// fos = new FileOutputStream(file, true);
		// s1 = "这是测试数据1!";
		// buf = s1.getBytes();
		// int len = buf.length;
		// fos.write(buf, 0, len);
		// fos.close();
		//

		fos = new FileOutputStream(file, true);
		s1 = "这是测试数据1!";
		buf = s1.getBytes();
		fc = fos.getChannel();
		bb = ByteBuffer.wrap(buf);
		fc.write(bb, len);
		fc.close();
		fos.close();
		len += buf.length;

		fos = new FileOutputStream(file, true);
		s1 = "这是测试数据2!";
		buf = s1.getBytes();
		fc = fos.getChannel();
		bb = ByteBuffer.wrap(buf);
		fc.write(bb, 50);
		fc.close();
		fos.close();
		len += buf.length;

		//
		System.out.println("close");

	}

	public static void main1(String[] args) {
		List<String> lines = new ArrayList<String>();
		lines.add("0,1213123");
		lines.add("2,12");
		lines.add("01,56");
		lines.add("3,78");
		lines.add("1,59");
		lines.add("03,178");
		Collections.sort(lines, new Comparator() {
			public int compare(Object o1, Object o2) {
				String line1 = String.valueOf(o1);
				int offset1 = Integer.parseInt(line1.split(",")[0]);
				String line2 = String.valueOf(o2);
				int offset2 = Integer.parseInt(line2.split(",")[0]);
				if (offset1 < offset2)
					return -1;
				else if (offset1 > offset2)
					return 1;
				else
					return 0;

			}
		});

		for (int i = 0; i < lines.size() - 1;) {
			String line1 = lines.get(i);
			int offset1 = Integer.parseInt(line1.split(",")[0]);
			int len1 = Integer.parseInt(line1.split(",")[1]);
			String line2 = lines.get(i + 1);
			int offset2 = Integer.parseInt(line2.split(",")[0]);
			int len2 = Integer.parseInt(line2.split(",")[1]);
			if (offset1 == offset2) {
				if (len1 < len2)
					lines.remove(i);
				else
					lines.remove(i + 1);
			} else
				i++;
		}

		for (int i = 0; i < lines.size(); i++) {
			System.out.println(lines.get(i));
		}
	}

}
