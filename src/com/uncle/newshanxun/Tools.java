package com.uncle.newshanxun;

import org.greenrobot.eventbus.EventBus;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.http.HttpParams;
import org.kymjs.kjframe.utils.PreferenceHelper;

import com.socks.library.KLog;
import com.uncle.newshanxun.bean.myMessage;
import com.uncle.newshanxun.constants.AppConstants;
import com.uncle.newshanxun.constants.URLConstants;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Tools {
	public static String PrefGetStr(Context con,String key){
		return PreferenceHelper.readString(con, AppConstants.PreferenceFileName, key ,"");
	}
	public static boolean PrefGetBool(Context con,String key){
		return PreferenceHelper.readBoolean(con, AppConstants.PreferenceFileName, key, false);
	}
	public static void PrefSetStr(Context con,String key,Object v){
		String type = v.getClass().getName();
		if(type.contains("Int"))
			PreferenceHelper.write(con, AppConstants.PreferenceFileName, key, Integer.parseInt(v.toString()));
		else if(type.contains("String"))
			PreferenceHelper.write(con, AppConstants.PreferenceFileName, key, v.toString());
		else if(type.contains("Bool"))
			PreferenceHelper.write(con, AppConstants.PreferenceFileName, key, Boolean.parseBoolean(v.toString()));
	}
	
	public static void getIP(){
		String visitUrl = "http://ip.6655.com/ip.aspx?area=1";
		HttpConfig hc = new HttpConfig();
		hc.cacheTime = 0;
		KJHttp kjh = new KJHttp(hc);
	    kjh.get(visitUrl , new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.d(t);
				myMessage me = new myMessage();
				me.setType(myMessage.TYPE_TOAST);
				me.setMess(t);
				EventBus.getDefault().post(me);
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
				KLog.e(String.valueOf(errorNo), strMsg);
				myMessage me = new myMessage();
				me.setType(myMessage.TYPE_TOAST);
				me.setMess("获取IP失败\n" + strMsg);
				EventBus.getDefault().post(me);
			}
		});
	}
	
	/*
	 * sanxun need
	 */
	public static String bin2hex(String bin) {
		char[] digital = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer("");
		byte[] bs = bin.getBytes();
		for (int i = 0; i < bs.length; i++) {
			sb.append('%');
			int bit = (bs[i] & 0xF0) >> 4;
			sb.append(digital[bit]);
			bit = bs[i] & 0xF;
			sb.append(digital[bit]);
		}
		return sb.toString();
	}
	
	public static boolean isWifiConnected(Context context){
		context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected()){
            return true;
        }
        return false;
    }
	
	public static boolean isMyServiceRunning(Context context,String packegename) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (packegename.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static String byte2HexStr(byte[] b) {
        String hs="";
        String stmp="";
        for (int n=0;n<b.length;n++) {
            stmp=(Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) 
            	hs=hs+"0"+stmp;
            else 
            	hs=hs+stmp;
        }
        return hs.toLowerCase();
    }

	public static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1).byteValue();
		byte ret = (byte) (b0 | b1);
		return ret;
	}

	public static byte[] hexStr2Bytes(String src) {
		int m = 0, n = 0;
		int l = src.length() / 2;
		System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}
		return ret;
	}

//	public static String origin(byte[] from) {
//		String a = null;
//		StringBuilder aa = new StringBuilder();
//		for (int i = 0; i < from.length; i++) {
//			aa.append(from[0]);
//		}
//		a = aa.toString();
//		return a;
//	}
	
	
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (Exception e) {
			return "";
		}
	}
}
