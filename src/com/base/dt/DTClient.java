package com.base.dt;

import com.base.utils.ParaMap;
import com.base.utils.ThreadUtils;

import java.util.Hashtable;

/**
 * 分布式事物--客户端
 * Created by Administrator on 2016/3/22.
 */
public class DTClient {

	//是否为分布式事物
	private static Hashtable<Long, Boolean> dtTable = new Hashtable<Long, Boolean>();
	//是否有insert,update提交动作
	private static Hashtable<Long, Boolean> dtCommit = new Hashtable<Long, Boolean>();

	public static void validIUAction(String sql) {
		String sql1 = sql.toLowerCase();
		Boolean b = sql1.indexOf("insert") >= 0 || sql1.indexOf("update") >= 0;
		b = b && dtTable.containsKey(ThreadUtils.id())
				&& dtTable.get(ThreadUtils.id());
		if (b)
			dtCommit.put(ThreadUtils.id(), true);
	}

	public static Boolean validDt(ParaMap inMap) {
		Boolean result = false;
		if ("1".equals(inMap.getString(DTUtils.flag)))
			result = true;
		return result;

	}

}
