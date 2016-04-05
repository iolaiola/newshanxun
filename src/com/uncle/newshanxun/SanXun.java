package com.uncle.newshanxun;
import java.io.UnsupportedEncodingException;

import org.greenrobot.eventbus.EventBus;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.http.HttpParams;
import com.socks.library.KLog;
import com.uncle.newshanxun.bean.myMessage;

import android.util.Log;

public class SanXun {
	public static void send(String username, String password, String fa_rout) {
		String router_key = "admin:" + fa_rout;
		String router = Base64.encode(router_key.getBytes());
		username = Base64.decode(username).toString();
		String after = "\r\n" + username;
		String url1 = "http://192.168.1.1:8081/cgi-bin/luci/userRpm/PPPoECfgRpm.htm?wan=0&wantype=2&acc=";
		String url2 = Tools.bin2hex(after);
		String url3 = "&psw=";
		String url4 = "&confirm=";
		String url5 = "&SecType=0&sta_ip=0.0.0.0&sta_mask=0.0.0.0&linktype=4&waittime2=0&Connect=%C1%AC+%BD%D3";
		String u = url1 + url2 + url3 + password + url4 + password + url5;

		HttpConfig hc = new HttpConfig();
		hc.cacheTime = 0;
		KJHttp kjh = new KJHttp(hc);
		HttpParams params = new HttpParams();
//		params.putHeaders("Host", "192.168.1.1");
//		params.putHeaders("Connection", "close");
//		params.putHeaders("Authorization", "Basic " + router);
//		params.putHeaders("Accept",
//				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		params.putHeaders(
//				"User-Agent",
//				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
//		params.putHeaders(
//				"Referer",	url1 + url2	+ url3	+ password	+ url4	+ password
//						+ "&SecType=0&sta_ip=0.0.0.0&sta_mask=0.0.0.0&linktype=4&waittime2=0&Disconnect=%B6%CF+%CF%DF");
//		params.putHeaders("Accept-Encoding", "gzip,deflate,sdch");
//		params.putHeaders("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
//		params.putHeaders("Cookie", "Authorization=Basic " + router);
		kjh.get("http://192.168.1.1:8081", params, new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				myMessage me = new myMessage();
				me.setType(myMessage.TYPE_SXDIALSUCC);
				me.setMess("拨号信息发送成功");
				EventBus.getDefault().post(me);
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
				KLog.e(String.valueOf(errorNo), strMsg);
				
				myMessage me = new myMessage();
				me.setType(myMessage.TYPE_DIALOG);
				me.setMess("拨号信息发送失败\n"+strMsg);
				EventBus.getDefault().post(me);
			}
		});
	}
}