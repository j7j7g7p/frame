package test2;

import java.nio.ByteBuffer;

import sun.nio.ch.DirectBuffer;

public class ByteBufferTest {

	public static void clean(final ByteBuffer byteBuffer) {
		if (byteBuffer.isDirect()) {
			DirectBuffer db = (DirectBuffer) byteBuffer;
			db.cleaner().clean();
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("maxMemoryValue:" + sun.misc.VM.maxDirectMemory()
				/ 1024 / 1024);
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024 * 100);
		buffer.putLong(1000);
		System.out.println(buffer);
		System.out.println("start");
		Thread.sleep(10000);
		clean(buffer);
		System.out.println("end");
		Thread.sleep(10000);
	}

}
