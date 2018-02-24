package com.base.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.base.dt.DTBean;
import com.base.dt.DTUtils;
import com.base.service.BaseService;
import com.base.utils.DateUtils;
import com.base.utils.ParaMap;
import com.base.web.AppConfig;
import com.base.web.filter.BaseFilter;

import java.lang.reflect.Method;

public class HttpUtils {
    public static String localjvm = "localhost";
    public static String rollback = "Rollback";

    public static ParaMap getData(ParaMap inMap) throws Exception {
        ParaMap outMap = new ParaMap();
        //
        String url = AppConfig.getStringPro("api_url");
        if (localjvm.equals(url)) {
            String module = inMap.getString("module");
            String service = inMap.getString("service");
            String method = inMap.getString("method");
            String system = inMap.getString("system");
            url += "/" + system + "/data?";
            url += "module=" + module;
            url += "&service=" + service;
            url += "&method" + method;
            url += "&" + DTUtils.flag + "=1";
            String outString = HttpManager.getDataString(url, inMap);
            JSONObject outMap1 = JSON.parseObject(outString);
            outMap.putAll(outMap1);
        } else {
            BaseService service = BaseFilter.getServiceInstance(inMap);
            Method m = BaseFilter.getMethod(inMap);
            outMap = (ParaMap) m.invoke(service, inMap);
            outMap.put("ts", DateUtils.nowTime());
        }
        return outMap;
    }

    public static ParaMap rollback(DTBean bean) throws Exception {
        String url = AppConfig.getStringPro("api_url");
        url += "/" + bean.system + "/data?";
        url += "module=" + bean.module;
        url += "&service=" + bean.service;
        url += "&method" + bean.method + rollback;
        String outString = HttpManager.getDataString(url, bean.map);
        JSONObject outMap1 = JSON.parseObject(outString);
        ParaMap outMap = new ParaMap();
        outMap.putAll(outMap1);
        return outMap;
    }
}
