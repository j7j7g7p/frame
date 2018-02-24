package test.t1;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Encoder;

public class Base64Test {

	public static void main(String[] args) throws Exception {
		String s1 = "我们是chinese6412...》》";
		String s3 = Base64.encodeBase64String(s1.getBytes());
		System.out.println(new String(Base64.decodeBase64(s3)));

		BASE64Encoder encoder = new BASE64Encoder();
		String s2 = encoder.encode(s1.getBytes());
		System.out.println(s2);

	}
}
