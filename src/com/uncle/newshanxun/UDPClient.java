package com.uncle.newshanxun;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

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

import com.uncle.newshanxun.R;

public class UDPClient extends Service {
	public final String ACTION_NAME = "HeartBeat";
	public static String username = null;
	public static String message = "default";
	public static String heartUrl = null;
	private static String send = "";
	private static int errornum = 0;
	public static Context mContext = null;
	
	private Timer timer = new Timer();
	private TimerTask task;
	
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void initContext(Context mc){
		mContext = mc;
	}
	
	
	public void onCreate() {

	}
	
	public void onStop(){
		
	}
	
	public void onDestroy(){
		stopForeground(true);
	}
	
	public void onStart(Intent intent, int startId) {
		registerBoradcastReceiver();
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
				Message message = mHandler.obtainMessage();
				message.what = 789;
				mHandler.sendMessage(message);
			}
		};
		timer.schedule(task, 0, 3*1000*60);
	}
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 789:
				errornum = 0;
				Log.d("timer","this is from timer heart();");
				heart();
				//String str1 = msg.getData().getString("toast");//接受msg传递过来的参数   
				break;
			default:
				break;
			}
		}
	};
	public void heart(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (Tools.isWifiConnected(mContext)) {
					try {
						send = getNewHeart();
						if(!send.equals(""))
							sendUdp(send);
					} catch (Exception e) {}
				}
			}
		}).start();
	}
	
	public String getNewHeart(){
		Map<String, String> paramValues = new HashMap<String, String>();  
        paramValues.put("message", message);
        //heartUrl="http://newdial.sinaapp.com/httest.php";
        String url=heartUrl+"?user="+username+"&version="+Tools.getVersionName(mContext);
        String postReturn = Tools.sendPost(url, Tools.getParams(true, paramValues));
        String mess="";
		try {
			JSONObject jo = new JSONObject(postReturn);
			int state = jo.getInt("state");
			mess = jo.getString("message");
			Log.d("getNewHeart()", mess);
			if(state == 0)
				return mess;
			Intent mIntent = new Intent("HeartBeat");
			mIntent.putExtra("bool",false);
			sendBroadcast(mIntent);
			return "";
		} catch (Exception e) {}
		return "";
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v("TrafficService", "startCommand");
		
		PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), Notification.FLAG_FOREGROUND_SERVICE);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(this);
		mBuilder.setContentTitle("newsx2.1.2");
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setContentText("请保持后台运行！");
		mBuilder.setTicker("请保持后台运行！");
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
		mBuilder.setOngoing(true);
		mBuilder.setSmallIcon(R.drawable.ic);
		//mBuilder.setAutoCancel(true);
		Notification nf=mBuilder.build();
		nf.flags=Notification.FLAG_ONGOING_EVENT;
		
		mNotificationManager.notify(0x111, nf);
		
		startForeground(0x111, nf);
		
		flags = START_REDELIVER_INTENT;//重传Intent。使用这个返回值时，如果在执行完onStartCommand后，
		//服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		return super.onStartCommand(intent, flags, startId);
		// return START_REDELIVER_INTENT;
	}

	public int sendUdp(String mes) throws SocketException{
		DatagramSocket s = new DatagramSocket();
		if(errornum > 5)
			return -1;
		try {
			String m = mes;
			byte[] sendBuf = Tools.hexStr2Bytes(m);
			int server_port = 8080;
			//Log.d("sendUdp", m);
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
	
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();
            if(action.equals(ACTION_NAME)){
            	boolean ht = intent.getBooleanExtra("bool", true);
        		if(!ht){
        			unregisterReceiver(mBroadcastReceiver);
        			timer.cancel();
        			stopSelf();
        		}
            }
        }
    };  
    
    public void registerBoradcastReceiver(){
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        //myIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        //注册广播        
        registerReceiver(mBroadcastReceiver, myIntentFilter);  
    }
}
