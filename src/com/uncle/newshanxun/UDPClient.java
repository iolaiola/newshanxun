package com.uncle.newshanxun;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.http.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.socks.library.KLog;
import com.uncle.newshanxun.R;
import com.uncle.newshanxun.bean.myMessage;
import com.uncle.newshanxun.constants.AppConstants;
import com.uncle.newshanxun.constants.URLConstants;

public class UDPClient extends Service {
	public static String username = null;
	public static String message = "default";
	public static String heartUrl = null;
	private static String send = "";
	private static int errornum = 0;
	public static Context mContext = null;
	private Timer timer = new Timer();
	private TimerTask task;
	
	private static boolean OnOff = false;
	
	public static void initContext(Context mc){
		mContext = mc;
	}
	
	public static void setOnOff(boolean k){
		OnOff = k;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		String[] info = intent.getStringExtra("info").split(" ");
		username = info[0];
		heartUrl = info[1];
		message = info[2];
		errornum = 0;
		//heart();
		//clock = 2;

		task = new TimerTask() {
			@Override
			public void run() {
				if(!OnOff)
					stopSelf();
				Message message = mHandler.obtainMessage();
				message.what = 789;
				mHandler.sendMessage(message);
			}
		};
		timer.schedule(task, 0, 3*1000);
	}
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 789:
				errornum = 0;
				Log.d("timer","this is from timer heart();");
				heart();
				break;
			default:
				break;
			}
		}
	};

	public void heart() {
		if (!Tools.isWifiConnected(mContext)) {
			SimpleDateFormat formatter = new SimpleDateFormat ("MM月dd日 HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间
			String dateStr = formatter.format(curDate);
			myMessage me = new myMessage();
			me.setType(myMessage.TYPE_DIALOG);
			me.setMess(dateStr + ":WIFI连接已断开");
			EventBus.getDefault().post(me);
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						getNewHeart();
					} catch (Exception e) {
					}
				}
			}).start();
			
		}
	}
	
	public void getNewHeart(){
		String url=heartUrl+"?user="+username+"&version="+Tools.getVersionName(mContext);
		HttpConfig hc = new HttpConfig();
		hc.cacheTime = 0;
		KJHttp kjh = new KJHttp(hc);
		HttpParams params = new HttpParams();
	    params.put("message", message);
	    kjh.get(url , params , new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.json(t);
				try {
					JSONObject jo = new JSONObject(t);
					int state = jo.getInt("state");
					String mess = jo.getString("message");
					Log.d("getNewHeart()", mess);
					if(state == 0){
						send = mess;
						sendUdp(send);
					}
				} catch (Exception e) {}
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
				KLog.e(String.valueOf(errorNo), strMsg);
			}
		});
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v("TrafficService", "startCommand");
		
		PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), Notification.FLAG_FOREGROUND_SERVICE);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(this);
		mBuilder.setContentTitle("Router_uncle");
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setContentText("请保持后台运行！");
		mBuilder.setTicker("请保持后台运行！");
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
		mBuilder.setOngoing(true);
		mBuilder.setSmallIcon(R.drawable.ic);
		Notification nf=mBuilder.build();
		nf.flags=Notification.FLAG_ONGOING_EVENT;
		
		mNotificationManager.notify(0x111, nf);
		
		startForeground(0x111, nf);
		
		flags = START_REDELIVER_INTENT;//重传Intent。使用这个返回值时，如果在执行完onStartCommand后，
		//服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		return super.onStartCommand(intent, flags, startId);
	}

	public int sendUdp(String mes) throws SocketException{
		KLog.d("sendUdp(String mes)");
		DatagramSocket s = new DatagramSocket();
		if(errornum > 5)
			return -1;
		try {
			String m = mes;
			byte[] sendBuf = Tools.hexStr2Bytes(m);
			int server_port = 8080;
			s.setSoTimeout(5000);
			InetAddress local = InetAddress.getByName("115.239.134.167");
			int msg_length = sendBuf.length;
			DatagramPacket p = new DatagramPacket(sendBuf, msg_length, local,
					server_port);
			s.send(p);
			byte[] getBuf = new byte[1024];
			DatagramPacket getPacket = new DatagramPacket(getBuf, getBuf.length);
			s.receive(getPacket);
			message = Tools.byte2HexStr(getBuf).substring(0,
					getPacket.getLength() * 2);
			errornum = 0;
			Log.d("return", message);
			s.close();
			return 0;
		} catch (Exception e) {
			Log.e("sendudp error happen","happen"+errornum);
			s.close();
			errornum++;
			return sendUdp(send);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
