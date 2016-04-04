package com.uncle.newshanxun;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.http.HttpParams;
import org.kymjs.kjframe.utils.PreferenceHelper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.socks.library.KLog;
import com.uncle.newshanxun.R;
import com.uncle.newshanxun.constants.AppConstants;
import com.uncle.newshanxun.constants.URLConstants;

public class AboutUs extends Activity implements OnClickListener{
	public Context mContext;
	private TextView tvNews;
	private Button btnUpdate;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		tvNews = (TextView) findViewById(R.id.news);
		btnUpdate = (Button) findViewById(R.id.newsUpdate);
		KLog.d("ssss");
		NewsUpdate();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.newsUpdate){
			NewsUpdate();
		}
	}
	
	private void NewsUpdate(){
		HttpConfig hc = new HttpConfig();
		hc.cacheTime = 0;
		KJHttp kjh = new KJHttp(hc);
		HttpParams params = new HttpParams();
		KLog.d("11111111s");
	    params.put("user", PreferenceHelper.readString(this, AppConstants.PreferenceFileName, AppConstants.PreferenceSX_user,"unknown"));
	    KLog.d("s222222");
	    kjh.get(URLConstants.URL_news , params , new HttpCallBack() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				KLog.d(t);
				tvNews.setText(t);
			}
			@Override
			public void onFailure(int errorNo, String strMsg) {
				super.onFailure(errorNo, strMsg);
				KLog.e(String.valueOf(errorNo), strMsg);
				tvNews.setText("网络好像出现异常了？\n" + strMsg);
			}
		});
	}
}
