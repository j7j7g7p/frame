package com.base.utils;

import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CRCUtils {

	public static long crc32(byte[] buf) {
		long ll = -1;
		try {
			CRC32 crc32 = new CRC32();
			InputStream in = StreamUtils.byteToInputStream(buf);
			CheckedInputStream checked = new CheckedInputStream(in, crc32);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = checked.read(buffer)) != -1) {
				crc32.update(buffer, 0, length);
			}
			checked.close();
			in.close();
			ll = crc32.getValue();
			// checksum = cis.getChecksum().getValue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ll;
	}

	public static void main(String[] args) {
		String s1 = "test测试fasdfasdffdasdfasfasdaf";
		long l = crc32(s1.getBytes());
		System.out.println(l);

	}
}
