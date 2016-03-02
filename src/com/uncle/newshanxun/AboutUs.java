package com.uncle.newshanxun;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.bmob.pay.tool.BmobPay;
import com.bmob.pay.tool.PayListener;
import com.uncle.newshanxun.R;

public class AboutUs extends Activity implements OnClickListener {
	Button submit = null;
	Button updatebtn = null;
	Button pay = null;
	public String username = null;
	private static String APPID = "XXXXXXX";//隐藏
	public static String morderId;
	BmobPay bmobPay;
	public Context mContext;
	public static SharedPreferences sharedPre=null;
	public static Editor editor=null;
	
	public static double price_onemonth;
	public static double price_sixmonth;
	public static String text_one;
	public static String text_six;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		submit = (Button) findViewById(R.id.submit);//six
		updatebtn = (Button) findViewById(R.id.update);
		pay = (Button) findViewById(R.id.buttononemonth);//one
		pay.setOnClickListener(this);
		submit.setOnClickListener(this);
		updatebtn.setOnClickListener(this);
		username = getIntent().getStringExtra("username");
		mContext=this;
		BmobPay.init(this, APPID);
		sharedPre = getSharedPreferences("config",MODE_PRIVATE);
		editor = sharedPre.edit();
		price_onemonth=8.0;
		price_sixmonth=30.0;
		text_one = "拨号授权-一月(8元)";
		text_six = "拨号授权-半年(30元)";
		
		//get product name and price
		init();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.submit) {//30yuan
			submitpay(price_sixmonth,text_six);
		}
		if (v.getId() == R.id.update) {//补发 
			mNotify();
		}
		if (v.getId() == R.id.buttononemonth) {//8yuan
			//pay
			submitpay(price_onemonth,text_one);
		}
	}
	public void init(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String re=Tools.HttpGet("http://newdial.sinaapp.com/2015/getprice.php?user="+username+"&version="+Tools.getVersionName(mContext));
					JSONObject jo = new JSONObject(re);
					if(jo.getInt("state")==0){
						price_onemonth = jo.getDouble("price_onemonth");
						text_one = jo.getString("text_one");
						price_sixmonth = jo.getDouble("price_sixmonth");
						text_six = jo.getString("text_six");
						Message msg = msgHandler.obtainMessage();
						msg.what = 4;//update ui
						msgHandler.sendMessage(msg);
					}
					else{
						Message msg = msgHandler.obtainMessage();
						msg.what = 1;
						msg.obj=jo.getString("message");
						msgHandler.sendMessage(msg);
					}
				} catch (Exception e) {}
			}
		}).start();
	}
	
	public void submitpay(double price,String te){
		final double p=price;
		final String t=te;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String re=Tools.HttpGet("http://newdial.sinaapp.com/2015/canpay.php?user="+username+"&version="+Tools.getVersionName(mContext));
					JSONObject jo = new JSONObject(re);
					if(jo.getInt("state")==0){
						Message msg = msgHandler.obtainMessage();
						msg.what = 3;
						Bundle bundle1 = new Bundle();
						bundle1.putDouble("price", p);
						bundle1.putString("te", t);
						msg.setData(bundle1);
						msgHandler.sendMessage(msg);
					}
					else
					{
						Message msg = msgHandler.obtainMessage();
						msg.what = 1;
						msg.obj=jo.getString("message");
						msgHandler.sendMessage(msg);
					}
				} catch (Exception e) {}
			}
		}).start();
	}
	
	
	
	
	public void pay(double price,String te){
		bmobPay = new BmobPay((Activity) mContext);
		bmobPay.pay(price, te, username, new PayListener() {
			// 因为网络等原因,支付结果未知(小概率事件),出于保险起见稍后手动查询
			@Override
			public void unknow() {
				Toast.makeText(mContext, "支付结果未知,请稍后手动补发",
						Toast.LENGTH_SHORT).show();
			}
			// 支付成功,如果金额较大请手动查询确认
			@Override
			public void succeed() {
				Toast.makeText(mContext, "支付成功!,等待发货。。。", Toast.LENGTH_SHORT)
						.show();
				mNotify();
			}

			// 无论成功与否,返回订单号
			@Override
			public void orderId(String orderId) {
				// 此处应该保存订单号,比如保存进数据库等,以便以后查询
				Log.d("orderId",orderId);
				morderId=orderId;
				
				Message msg = msgHandler.obtainMessage();
				msg.what = 2;
				Bundle bundle1 = new Bundle();
				bundle1.putString("key", "orderid");
				bundle1.putString("value", orderId);
				msg.setData(bundle1);
				msgHandler.sendMessage(msg);
			}

			// 支付失败,原因可能是用户中断支付操作,也可能是网络原因
			@Override
			public void fail(int code, String reason) {
				Toast.makeText(mContext, "支付中断!", Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
	public void mNotify(){
		final String od = sharedPre.getString("orderid","");
		
		if(od.equals("")){
			Message msg1 = msgHandler.obtainMessage();
			msg1.what = 1;
			msg1.obj="没有需要补发的订单。";
			msgHandler.sendMessage(msg1);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> paramValues = new HashMap<String, String>();  
					paramValues.put("user", username);
					paramValues.put("orderid", od);
					String sign = username+od+APPID+"uncle";
					String sig = Tools.getMD5(sign);
					paramValues.put("sign", sig);
					String postReturn = Tools.sendPost("http://newdial.sinaapp.com/2015/check.php", Tools.getParams(true, paramValues));
					JSONObject jo = new JSONObject(postReturn);
					int state = jo.getInt("state");
					String mess=jo.getString("message");
					if(state==0){
						Message msg = msgHandler.obtainMessage();
						msg.what = 2;
						Bundle bundle1 = new Bundle();
						bundle1.putString("key", "orderid");
						bundle1.putString("value", "");
						msg.setData(bundle1);
						msgHandler.sendMessage(msg);
					}
					Message msg1 = msgHandler.obtainMessage();
					msg1.what = 1;
					msg1.obj=mess;
					msgHandler.sendMessage(msg1);
				} catch (Exception e) {}
			}
		}).start();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	public static void changevalue(String key,String value){
		editor.putString(key, value);
		editor.commit();
	}
	private final Handler msgHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Bundle bundle = msg.getData();
				String key = bundle.getString("key");
				String value = bundle.getString("value");
				changevalue(key,value);
				Log.d("changevalue",key+" "+value);
				break;
			case 3:
				Bundle bundle3 = msg.getData();
				double p = bundle3.getDouble("price");
				String t = bundle3.getString("te");
				pay(p,t);
				break;
			case 4:
				pay.setText(text_one);
				submit.setText(text_six);
				break;
			default:
				break;
			}
		}
	};
}
