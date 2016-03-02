package com.uncle.newshanxun;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Tools {
	public static String HttpGet(String urlString) throws Exception{
		URL url = new URL(urlString);
		String line = null;
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setReadTimeout(5000);
		urlConnection.setRequestMethod("GET");
		if (urlConnection.getResponseCode() == 200) {
			InputStream inStream = urlConnection.getInputStream();
			byte[] data = Tools.read(inStream);
			line = new String(data);
		}
		Log.d("HTTP_GET",line);
		return line;
	}
	
	public static String getMD5(String str) throws Exception {
		/** 创建MD5加密对象 */
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		/** 进行加密 */
		md5.update(str.getBytes());
		/** 获取加密后的字节数组 */
		byte[] md5Bytes = md5.digest();
		String res = "";
		for (int i = 0; i < md5Bytes.length; i++) {
			int temp = md5Bytes[i] & 0xFF;
			if (temp <= 0XF) { // 转化成十六进制不够两位，前面加零
				res += "0";
			}
			res += Integer.toHexString(temp);
		}
		return res;
	}
	public static String getString(byte[] b){  
        StringBuffer sb = new StringBuffer();  
         for(int i = 0; i < b.length; i ++){  
          sb.append(b[i]);  
         }  
         return sb.toString();  
	}  
	
	/**
     * 向指定 URL 发送POST方法的请求
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
	
    
    public static String getParams(boolean isPost,Map<String, String> paramValues)  
    {  
        String params="";
        Set<String> key = paramValues.keySet();  
        String beginLetter="";  
        if (!isPost){  
            beginLetter="?";  
        }  
        for (Iterator<String> it = key.iterator(); it.hasNext();) {  
            String s = (String) it.next();
            if (params.equals("")){
                params += beginLetter + s + "=" + paramValues.get(s);  
            }
            else{
                params += "&" + s + "=" + paramValues.get(s);
            }
        }
        return params;
    }
	
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
	
	public static boolean isWifiConnected(Context context)
    {
		context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
        return false ;
    }
	
	
	public static byte[] read(InputStream inStream)throws Exception{
		ByteArrayOutputStream outp=new ByteArrayOutputStream();
		byte[] buffer= new byte[1024];
		int len=0;
		while((len=inStream.read(buffer))!=-1)
		{
			outp.write(buffer,0,len);
			
		}
		inStream.close();
		return outp.toByteArray();
	}
	
	public static boolean isMyServiceRunning(Context context,String packegename) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (packegename.equals(service.service.getClassName())) {
	        	//"com.example.newshanxun.HeartGuard"
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

	public static String origin(byte[] from) {
		String a = null;
		StringBuilder aa = new StringBuilder();
		for (int i = 0; i < from.length; i++) {
			aa.append(from[0]);
		}
		a = aa.toString();
		return a;
	}
	
	
	public static String getVersionName(Context context){
		// 获取packagemanager的实例
		try{
		PackageManager packageManager = context.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
		String version = packInfo.versionName;
		Log.d("version",version);
		return version;
		}
		catch(Exception e){
			return "";
		}
	}
}
