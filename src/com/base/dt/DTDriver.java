package com.base.dt;

import com.base.http.HttpUtils;
import com.base.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 分布式事物--调用端
 * Created by Administrator on 2016/3/22.
 */
public class DTDriver {
    private static Map<Long, List<DTBean>> commitMap = new Hashtable<Long, List<DTBean>>();

    public static void addDTBean(DTBean bean) {
        List list = commitMap.get(ThreadUtils.id());
        if (list == null) {
            list = new ArrayList<DTBean>();
            commitMap.put(ThreadUtils.id(), list);
        }
        list.add(bean);
    }

    public static void rollback() throws Exception{
        List<DTBean> list = commitMap.get(ThreadUtils.id());
        for (DTBean bean : list) {
            HttpUtils.rollback(bean);
        }
        clear();
    }

    public static void clear() {
        commitMap.remove(ThreadUtils.id());
    }


}
