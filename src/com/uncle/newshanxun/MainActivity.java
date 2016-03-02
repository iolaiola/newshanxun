package com.uncle.newshanxun;

import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import com.uncle.newshanxun.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;


public class MainActivity extends Activity implements OnClickListener {
	//3 editable place
	EditText username = null;
	EditText password = null;
	EditText root = null;
	
	//4 button
	Button confirm = null;
	Button login = null;
	Button state = null;
	TextView about = null;
	TextView beatH = null;
	
	String new_pass = null;
	String new_user = null;
	String new_route = null;
	
	//preferrences
	public static SharedPreferences sharedPre=null;
	public static Editor editor=null;
	Context mContext;
	
	
	boolean hea = false;
	

	String usetag=null;
	String real =null;
	
	//double click return
	private static Boolean isExit = false;
	public static final int MessageToast = 0;
	public static final int MessageConfig = 1;
	
	
	
	public void saveLoginInfo( String username,String password, String root) {
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("router", root);
		editor.commit();
	}
	
	public static void changevalue(String key,String value){
		editor.putString(key, value);
		editor.commit();
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("super.onCreate","savedInstanceState");
		setContentView(R.layout.activity_main_2);
		Log.d("setContentView","R.layout.activity_main_2");
		mContext=this;
		confirm = (Button) findViewById(R.id.initation);
		login = (Button) findViewById(R.id.Rdial);
		beatH = (Button) findViewById(R.id.heartb);
		state = (Button) findViewById(R.id.state);
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		root = (EditText) findViewById(R.id.editText3);
		
		about = (TextView) findViewById(R.id.guanyu);
		
		beatH.setOnClickListener(this);
		confirm.setOnClickListener(this);
		login.setOnClickListener(this);
		about.setOnClickListener(this);
		state.setOnClickListener(this);
		sharedPre = getSharedPreferences("config",MODE_PRIVATE);
		editor = sharedPre.edit();
		String u = sharedPre.getString("username", "chinanet@XY");
		username.setText(u.toCharArray(), 0, u.length());
		String p = sharedPre.getString("password", "123456");
		password.setText(p.toCharArray(), 0, p.length());
		String r = sharedPre.getString("router", "admin");
		root.setText(r.toCharArray(), 0, r.length());
		
		JPushInterface.setDebugMode(false);
		JPushInterface.init(this);
	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		super.onResume();
	}
	@Override
	protected void onPause() {
		JPushInterface.onPause(this);
		super.onPause();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		new_user = username.getText().toString();
		new_user=new_user.replaceAll(" ", "").toUpperCase();
		
		new_pass = password.getText().toString();
		new_route = root.getText().toString();
		
		saveLoginInfo(new_user, new_pass, new_route);
		usetag = sharedPre.getString("usetag", "none");
		real = sharedPre.getString("real", "none");
		
		if (v.getId() == R.id.initation) {//init
			new Thread(new Runnable() {//检查是否有更新
				@Override
				public void run() {
					update();
					chushihua();
				}
			}).start();
		}
		if (v.getId() == R.id.heartb) {
			if (!hea) {
				UDPClient.initContext(this);
				
				hea = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						String url = "http://newdial.sinaapp.com/2015/getht_url.php";
						url=url+"?user="+new_user+"&type=phone&version="+Tools.getVersionName(mContext);
						try {
							String line = Tools.HttpGet(url);
							JSONObject jo = new JSONObject(line);
							int state = jo.getInt("state");
							String mess = jo.getString("message");
							if(state != 0){
								Message msg=mHandler.obtainMessage();
								msg.what=MessageToast;
								msg.obj = mess;
								mHandler.sendMessage(msg);
							}
							else{
								Intent heartt = new Intent();// 心跳服务
								heartt.setClass(MainActivity.this, UDPClient.class);
								heartt.putExtra("info", new_user + " " + mess+" "+"test");
								startService(heartt);
								
								Message msg=mHandler.obtainMessage();
								msg.what=MessageToast;
								msg.obj = "开启成功。如需返回桌面，请按HOME键，请勿杀后台";
								mHandler.sendMessage(msg);
							}
						} catch (Exception e) {
							Message msg=mHandler.obtainMessage();
							msg.what=MessageToast;
							msg.obj = "心跳开启失败，请关闭后重试。";
							mHandler.sendMessage(msg);
						}
					}
				}).start();
				beatH.setText("关闭心跳");
			} else {
				Intent mIntent = new Intent("HeartBeat");
    			mIntent.putExtra("bool",false);
    			sendBroadcast(mIntent);
				
    			
				Toast.makeText(this, "心跳已关闭", Toast.LENGTH_SHORT).show();
				beatH.setText("开始心跳");
				hea=false;
			}
		}
		if (v.getId() == R.id.Rdial) {
			if (new_user.equals("") || new_pass.equals("")) {
				Toast.makeText(this, "请完整输入信息", Toast.LENGTH_SHORT).show();
			} 
			else if(usetag.equals("true")){
				Toast.makeText(this, "不要重复点登陆", Toast.LENGTH_SHORT).show();
			}
			else if(usetag.equals("none")){
				Toast.makeText(this, "请先初始化", Toast.LENGTH_SHORT).show();
			}
			else if(!Tools.isWifiConnected(mContext)){
				Toast.makeText(this, "请连接路由器WIFI", Toast.LENGTH_SHORT).show();
			}
			else {
				Log.d("real",real);

				new Thread(new Runnable() {//检查是否有更新
					@Override
					public void run() {
						try {
							SanXun sx=new SanXun();
							sx.send(real,new_pass,new_route);
							
							Log.d("ROUTER_DIAL","SUCCESS");
							Message ms=mHandler.obtainMessage();
							ms.what=MessageToast;
							ms.obj = "拨号信息已传输给路由器。";
							mHandler.sendMessage(ms);
							
							
							Message m2 = mHandler.obtainMessage();
							m2.what = MessageConfig;
							Bundle bundle2 = new Bundle();
							bundle2.putString("key", "usetag");
							bundle2.putString("value", "true");
							m2.setData(bundle2);
							mHandler.sendMessage(m2);
							
							Thread.sleep(1500);
							String visitUrl = "http://ip.6655.com/ip.aspx?area=1";
							String result = Tools.HttpGet(visitUrl);
							Message mss=mHandler.obtainMessage();
							mss.what=MessageToast;
							mss.obj = result;
							mHandler.sendMessage(mss);
							
							chushihua();
						} catch (Exception e) {
							Log.d("ROUTER_DIAL","FAILED");
							Message ms=mHandler.obtainMessage();
							ms.what=MessageToast;
							ms.obj = "拨号信息发送失败";
							mHandler.sendMessage(ms);
						}
					}
				}).start();
				
				//changevalue("usetag","true");
				//Toast.makeText(getApplicationContext(), "拨号信息已传输给路由器。",Toast.LENGTH_SHORT).show();
				
				/*new Thread(new Runnable() {//检查是否有更新
					@Override
					public void run() {
						try {
							Thread.sleep(1500);
							String visitUrl = "http://ip.6655.com/ip.aspx?area=1";
							String result = Tools.HttpGet(visitUrl);
							Message ms=mHandler.obtainMessage();
							ms.what=MessageToast;
							ms.obj = result;
							mHandler.sendMessage(ms);
							
							chushihua();
						} catch (Exception e) {	}
					}
				}).start();*/
			}
		}
		if (v.getId() == R.id.guanyu) {
			Toast.makeText(MainActivity.this, new_user , Toast.LENGTH_SHORT).show(); 
			Intent aboutIntent = new Intent();
			aboutIntent.putExtra("username", new_user);
			aboutIntent.setClass(MainActivity.this, AboutUs.class);
			startActivity(aboutIntent);
		}
		if (v.getId() == R.id.state) {//done
			new Thread(new Runnable() {//检查是否有更新
				@Override
				public void run() {
					try {
						String url="http://newdial.sinaapp.com/2015/user_state.php";
						String line = Tools.HttpGet(url+"?user="+new_user+"&version="+Tools.getVersionName(mContext));//modified
						JSONObject jo = new JSONObject(line);
						//int state = jo.getInt("state");
						String mess = jo.getString("message");
						Message ms=mHandler.obtainMessage();
						ms.what=MessageToast;
						ms.obj = mess;
						mHandler.sendMessage(ms);
					} catch (Exception e) {	}
				}
			}).start();
		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){    
			exitBy2Click();      //调用双击退出函数  
			return false;
	    }
		else
			return super.onKeyDown(keyCode, event);
	}

	private void exitBy2Click() {  
	    Timer tExit = null;  
	    if (isExit == false) {  
	        isExit = true; // 准备退出  
	        Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();  
	        tExit = new Timer();
	        tExit.schedule(new TimerTask() {  
	            @Override  
	            public void run() {  
	                isExit = false; // 取消退出  
	            }  
	        }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务  
	    } else {  
	        finish();
	        System.exit(0);
	    }  
	}  
	
	protected void onDestroy() {
		super.onDestroy();
	}
	

	public void update() {
		try {
			String version = Tools.getVersionName(mContext);
			String visitUrl = "http://newdial.sinaapp.com/update.php?user="
					+ new_user + "&ver=" + version;
			String result = Tools.HttpGet(visitUrl);
			if (result.compareTo(version) > 0) {
				String qq = "发现新版本" + result + "，当前为" + version;
				Message ms = mHandler.obtainMessage();
				ms.what = MessageToast;
				ms.obj = qq;
				mHandler.sendMessage(ms);
			}
		} catch (Exception e) {
		}
	}
	
	
	public void chushihua() {
		if (new_user.equals("") || new_pass.equals("")) {
			Toast.makeText(getApplicationContext(), "请输入用户名密码",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// else if(usetag.equals("false")){
		// Toast.makeText(getApplicationContext(),"不需要重复初始化",
		// Toast.LENGTH_SHORT).show();
		// }
		try {
			String line = "";
			String url = "http://newdial.sinaapp.com/2015/sx_getpin.php";
			line = Tools.HttpGet(url + "?user=" + new_user+"&version="+Tools.getVersionName(mContext));
			JSONObject jo = new JSONObject(line);
			int state = jo.getInt("state");
			String mess = jo.getString("message");
			if (state == 0) {
				Message m1 = mHandler.obtainMessage();
				m1.what = MessageConfig;
				Bundle bundle1 = new Bundle();
				bundle1.putString("key", "real");
				bundle1.putString("value", mess);
				m1.setData(bundle1);
				mHandler.sendMessage(m1);

				Message m2 = mHandler.obtainMessage();
				m2.what = MessageConfig;
				Bundle bundle2 = new Bundle();
				bundle2.putString("key", "usetag");
				bundle2.putString("value", "false");
				m2.setData(bundle2);
				mHandler.sendMessage(m2);

				Message msg = mHandler.obtainMessage();
				msg.what = MessageToast;
				msg.obj = "初始化成功";
				mHandler.sendMessage(msg);
			} else {
				Message msg = mHandler.obtainMessage();
				msg.what = MessageToast;
				msg.obj = mess;
				mHandler.sendMessage(msg);
			}
		} catch (Exception e) {
		}
	}
	
	 private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageToast:
				//String str1 = msg.getData().getString("toast");//接受msg传递过来的参数   
				String show=(String) msg.obj;
            	Toast.makeText(MainActivity.this, show , Toast.LENGTH_SHORT).show(); 
				break;
			case MessageConfig:
				Bundle bundle = msg.getData();
				String key = bundle.getString("key");
				String value = bundle.getString("value");
				changevalue(key,value);
				Log.d("changevalue",key+" "+value);
				break;
			default:
				break;
			}
		}
		
	};
}