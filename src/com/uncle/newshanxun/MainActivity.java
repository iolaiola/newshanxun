package com.uncle.newshanxun;

import java.util.Timer;
import java.util.TimerTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.http.HttpParams;
import org.kymjs.kjframe.utils.PreferenceHelper;
import com.socks.library.KLog;
import com.uncle.newshanxun.R;
import com.uncle.newshanxun.bean.myMessage;
import com.uncle.newshanxun.constants.AppConstants;
import com.uncle.newshanxun.constants.URLConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {
	//3 editable place
	EditText edtUsername = null;
	EditText edtPassword = null;
	EditText edtRouter = null;
	//4 button
	Button btnInit = null;
	Button btnDial = null;
	Button btnState = null;
	TextView about = null;
	Button btnHeart = null;
	
	Context mContext;

	String strUser;
	String strPass;
	String strRouter;
	//double click return
	private static Boolean isExit = false;
	public static Boolean hea = false;
	
	HttpConfig hc;
	KJHttp kjh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_2);
		mContext=this;
		EventBus.getDefault().register(this);
		btnInit = (Button) findViewById(R.id.initation);
		btnDial = (Button) findViewById(R.id.Rdial);
		btnHeart = (Button) findViewById(R.id.heartb);
		btnState = (Button) findViewById(R.id.state);
		edtUsername = (EditText) findViewById(R.id.username);
		edtPassword = (EditText) findViewById(R.id.password);
		edtRouter = (EditText) findViewById(R.id.routerpass);
		about = (TextView) findViewById(R.id.guanyu);
		btnHeart.setOnClickListener(this);
		btnInit.setOnClickListener(this);
		btnDial.setOnClickListener(this);
		about.setOnClickListener(this);
		btnState.setOnClickListener(this);
		
		edtUsername.setText(Tools.PrefGetStr(mContext, AppConstants.K_EDITUSER));
		edtPassword.setText(Tools.PrefGetStr(mContext, AppConstants.K_EDITPASS));
		edtRouter.setText(Tools.PrefGetStr(mContext, AppConstants.K_EDITROUTER));
		
		
		hc = new HttpConfig();
		hc.cacheTime = 0;
		kjh = new KJHttp(hc);
	}

	@Override
	public void onClick(View v) {
		strUser = edtUsername.getText().toString();
		strUser = strUser.replaceAll(" ", "").toUpperCase();
		edtUsername.setText(strUser);

		strPass = edtPassword.getText().toString();
		strRouter = edtRouter.getText().toString();
		
		Tools.PrefSetStr(mContext, AppConstants.K_EDITUSER, strUser);
		Tools.PrefSetStr(mContext, AppConstants.K_EDITPASS, strPass);
		Tools.PrefSetStr(mContext, AppConstants.K_EDITROUTER, strRouter);
		
		if (v.getId() == R.id.initation) {
			if (strUser.equals("")) {
				Toast.makeText(this, "请输入闪讯号", Toast.LENGTH_SHORT).show();
				return;
			}
			getInit();
		}
		if (v.getId() == R.id.heartb) {
			if (!hea) {
				UDPClient.initContext(this);
				UDPClient.setOnOff(true);
				hea = true;
				String url = URLConstants.URL_GETHTURL + "?user="+strUser+"&type=phone&version="+Tools.getVersionName(mContext);
				kjh.get(url, new HttpCallBack() {
					@Override
					public void onSuccess(String t) {
						super.onSuccess(t);
						KLog.json(t);
						try {
							JSONObject jo = new JSONObject(t);
							int state = jo.getInt("state");
							String mess = jo.getString("message");
							if(state != 0){
								myMessage me = new myMessage();
								me.setType(myMessage.TYPE_DIALOG);
								me.setMess(mess);
								EventBus.getDefault().post(me);
							}
							else{
								Intent heartt = new Intent();// 心跳服务
								heartt.setClass(MainActivity.this, UDPClient.class);
								heartt.putExtra("info", strUser + " " + mess+" "+"test");
								startService(heartt);
								myMessage me = new myMessage();
								me.setType(myMessage.TYPE_DIALOG);
								me.setMess("开启成功。请保持运行");
								EventBus.getDefault().post(me);
							}
						} catch (JSONException e) {	}
					}
					@Override
					public void onFailure(int errorNo, String strMsg) {
						super.onFailure(errorNo, strMsg);
						myMessage me = new myMessage();
						me.setType(myMessage.TYPE_DIALOG);
						me.setMess("心跳开启失败，请关闭后重试。\n"+strMsg);
						EventBus.getDefault().post(me);
					}
				});
				btnHeart.setText("关闭心跳");
			} else {
				Intent mIntent = new Intent("HeartBeat");
    			mIntent.putExtra("bool",false);
    			sendBroadcast(mIntent);
				
    			UDPClient.setOnOff(false);
				Toast.makeText(this, "心跳已关闭", Toast.LENGTH_SHORT).show();
				btnHeart.setText("开始心跳");
				hea=false;
			}
		}
		if (v.getId() == R.id.Rdial) {
			if (strUser.equals("") || strPass.equals("")) {
				Toast.makeText(this, "请完整输入信息", Toast.LENGTH_SHORT).show();
			}
			else if(Tools.PrefGetBool(mContext, AppConstants.K_USED)){
				Toast.makeText(this, "不要重复点登陆", Toast.LENGTH_SHORT).show();
			}
			else if(!Tools.isWifiConnected(mContext)){
				Toast.makeText(this, "请连接路由器WIFI", Toast.LENGTH_SHORT).show();
			}
			else {
				SanXun.send(Tools.PrefGetStr(this, AppConstants.K_pinUSER),strPass,strRouter);
			}
		}
		if (v.getId() == R.id.guanyu) {
			Intent aboutIntent = new Intent();
			aboutIntent.setClass(MainActivity.this, AboutUs.class);
			startActivity(aboutIntent);
		}
		if (v.getId() == R.id.state) {
			String url = URLConstants.URL_USERSTATE + "?user="+ strUser +"&version="+Tools.getVersionName(mContext);
			kjh.get(url, new HttpCallBack() {
				@Override
				public void onSuccess(String t) {
					super.onSuccess(t);
					KLog.json(t);
					try {
						JSONObject jo = new JSONObject(t);
						int state = jo.getInt("state");
						String mess = jo.getString("message");
						myMessage me = new myMessage();
						me.setType(myMessage.TYPE_DIALOG);
						me.setMess("状态（0为正常）：" + String.valueOf(state) + "\n到期时间："	+ mess);
						EventBus.getDefault().post(me);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onFailure(int errorNo, String strMsg) {
					super.onFailure(errorNo, strMsg);
					myMessage me = new myMessage();
					me.setType(myMessage.TYPE_DIALOG);
					me.setMess("网络好像异常？\n" + strMsg);
					EventBus.getDefault().post(me);
				}
			});
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
	
	public void checkUpdate() {
		final String version = Tools.getVersionName(mContext);
		String visitUrl = URLConstants.URL_UPDATE + "?user=" + strUser + "&ver=" + version;
		kjh.get(visitUrl, new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.d(t);
				if(t.compareTo(version)>0){
					myMessage me = new myMessage();
					me.setType(myMessage.TYPE_DIALOG);
					me.setMess("发现新版本" + t + "，当前为" + version);
					EventBus.getDefault().post(me);
				}
			}
		});
	}
	
	
	public void getInit() {
		String url = URLConstants.URL_GETPIN + "?user=" + strUser + "&version="
				+ Tools.getVersionName(mContext);
		kjh.get(url, new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.json(t);
				try {
					JSONObject jo = new JSONObject(t);
					int state = jo.getInt("state");
					if (state == 0) {
						Tools.PrefSetStr(mContext, AppConstants.K_pinUSER,	jo.getString("message"));
						Tools.PrefSetStr(mContext, AppConstants.K_USED, false);
						myMessage me = new myMessage();
						me.setType(myMessage.TYPE_DIALOG);
						me.setMess("初始化成功");
						EventBus.getDefault().post(me);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
				myMessage me = new myMessage();
				me.setType(myMessage.TYPE_DIALOG);
				me.setMess("网络连接失败了");
				EventBus.getDefault().post(me);
			}
		});
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onUserEvent(myMessage event) {
		switch(event.getType()){
		case myMessage.TYPE_TOAST:
			Toast.makeText(this, event.getMess(), Toast.LENGTH_SHORT).show();
			break;
		case myMessage.TYPE_DIALOG:
			showdialog(event.getMess());
			break;
		case myMessage.TYPE_SXDIALSUCC:
			getInit();
			showdialog(event.getMess());
			Tools.PrefSetStr(this, AppConstants.K_USED, true);
			Tools.getIP();
			break;
		default:
			break;
		}
    }
	
	private void showdialog(String mess){  
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器  
        builder.setTitle("提示"); //设置标题  
        builder.setMessage(mess); //设置内容  
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss(); //关闭dialog
            }
        });
        builder.create().show();
    }
	
	
	
	public void test() {
		PreferenceHelper.write(this, AppConstants.PreferenceFileName, "", "");

		
		HttpParams params = new HttpParams();
	    params.putHeaders("Cookie", "cookie不能告诉你");
		kjh.get("https://www.v2ex.com/api/topics/hot.json", new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.json(t);
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
			}
		});
	}

}