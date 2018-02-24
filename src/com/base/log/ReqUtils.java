package com.base.log;

import com.base.utils.IDGenerator;
import com.base.utils.ParaMap;
import com.base.utils.StrUtils;

public class ReqUtils {
	private static ThreadLocal<String> rid = new ThreadLocal<String>();
	public static String ridKey = "_rid";

	public static String getRId() {
		return rid.get();
	}

	public static void initRId(ParaMap inMap) {
		String ridValue = inMap.getString(ridKey);
		if (StrUtils.isNull(ridValue))
			ridValue = IDGenerator.newNo("R");
		rid.set(ridValue);
	}

}
