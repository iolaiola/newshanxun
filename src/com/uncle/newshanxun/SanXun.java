package com.uncle.newshanxun;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class SanXun {
	String password = null;
	String username = null;

	public void send(String fa_userName, String fa_password, String fa_rout)
			throws IOException {

		password = fa_password;
		username = fa_userName;
		String router_key = "admin:" + fa_rout;
		String router = Base64.encode(router_key.getBytes());
		if (password != null) {
			username = Base64.decode(username).toString();
			String after = "\r\n" + username;
			Log.d("SanXun", "start");
			String url1 = "http://192.168.1.1/userRpm/PPPoECfgRpm.htm?wan=0&wantype=2&acc=";
			String url2 = Tools.bin2hex(after);
			String url3 = "&psw=";
			String url4 = "&confirm=";
			String url5 = "&SecType=0&sta_ip=0.0.0.0&sta_mask=0.0.0.0&linktype=4&waittime2=0&Connect=%C1%AC+%BD%D3";
			String u = url1 + url2 + url3 + password + url4 + password + url5;
			URL url = new URL(u);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", "192.168.1.1");
			connection.setRequestProperty("Connection", "close");
			connection.setRequestProperty("Authorization", "Basic " + router);
			connection
					.setRequestProperty("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
			// connection.setRequestProperty("Referer","http://192.168.1.1/userRpm/PPPoECfgRpm.htm");
			connection
					.setRequestProperty(
							"Referer",
							url1
									+ url2
									+ url3
									+ password
									+ url4
									+ password
									+ "&SecType=0&sta_ip=0.0.0.0&sta_mask=0.0.0.0&linktype=4&waittime2=0&Disconnect=%B6%CF+%CF%DF");
			connection.setRequestProperty("Accept-Encoding",
					"gzip,deflate,sdch");
			connection.setRequestProperty("Accept-Language",
					"zh-CN,zh;q=0.8,en;q=0.6");
			connection.setRequestProperty("Cookie", "Authorization=Basic "
					+ router);
			connection.setConnectTimeout(1000);
			connection.connect();
			connection.getHeaderFields();
		}
	}

	public String inputStream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}
}