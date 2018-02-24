package test2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class DataGen {

	public static void main(String[] args) throws Exception {
		List list = new ArrayList();
		long start = 155000000;
		long len = 5000000;
		long count = 0;
		while (count < len) {
			count++;
			long now = System.currentTimeMillis();
			StringBuffer sb = new StringBuffer();
			sb.append((start + count) + ",");
			sb.append((10 + start) + ",");
			sb.append((20 + start) + ",");
			sb.append((30 + start) + ",");
			sb.append((13700000000L + start) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count) + ",");
			sb.append(("S" + now + count));
			list.add(sb.toString());
			System.out.println(count);

		}
		FileUtils.writeLines(new File("c:/tra(" + (start + 1) + "-"
				+ (start + len) + ").txt"), list);

	}
}
