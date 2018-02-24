package com.base.weixin;

import com.base.task.MyTimerTask;
import com.base.utils.CacheUtils;
import com.base.utils.ParaMap;
import com.base.weixin.utils.WeiXinUtil;


/**
 * 微信定时刷新Token 定时任务
 * @ClassName: WeiXinTokenTimerTask
 * @date 2016年6月13日 上午10:09:55
 *
 */
public class WeiXinTokenTimerTask extends MyTimerTask{
	private String appid;
	private String appSecret;
	public WeiXinTokenTimerTask(String id, String appid,String appSecret){
		super(id);
		this.appid=appid;
		this.appSecret=appSecret;
	}
	
	@Override
	public void execute() throws Exception{
		 ParaMap tokenMap=WeiXinUtil.getAccessToken(appid,appSecret);
         if(1==tokenMap.getInt("state")){
        	CacheUtils.set(appid+"access_token", tokenMap.getString("access_token"));//保存access_token到缓存
         	ParaMap ticketMap=WeiXinUtil.getJSTicket(tokenMap.getString("access_token"));
        	if(1==ticketMap.getInt("state")){
        		CacheUtils.set(appid+"jsapi_ticket", ticketMap.getString("jsapi_ticket"));//保存jsapi_ticket到缓存
        	}
         }
		
	}
	
}
