package com.cloudpos.apidemo.activity;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudpos.apidemo.common.Enums.StateLog;
import com.cloudpos.apidemo.function.ListViewAdapter;
import com.cloudpos.mvc.base.ActionCallback;

public abstract class ConstantActivity extends Activity {

	protected static final String TAG = "MainActivity";
	public static Handler handler;
	public static ActionCallback callback;
	protected ListViewAdapter adapter;
	
	protected Map<String, Object> param;

	// controls
	protected LinearLayout layoutJianjie; // control the show and hidden of introduction controls
	protected TextView txtJianjie;// introduction
	protected Button btnClean;
	protected Button btnExit;
	public static TextView txtResult;// log output log helper  use 
	protected ListView lvwMain;
	public static TextView txtVersion;
	ImageView imgShow;

	public static Activity host;
	protected Context context;
	
	// set the control's ID of click events 
	protected int[] btnWidget;
	protected boolean isMain = true;

	public static String mainMenu = "Main";

	/**
	 *  the postion of ListView
	 */
	public static int position;

	//store informations get from 'strings.xml' and 'arrays.xml'  
	protected List<String> arryTag;
	protected List<String> arryText;

	// whether handheld device 
	public static boolean isHand = false;
	// whether Q1
	public static boolean isQ1 = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_libhal);
		initView();

		
	}

	private void initView() {
		layoutJianjie = (LinearLayout) findViewById(R.id.layout_jianjie);
		txtJianjie = (TextView) findViewById(R.id.txt_jianjie);
		txtVersion = (TextView) findViewById(R.id.txt_version);
		txtResult = (TextView) findViewById(R.id.txt_result); 
		txtResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		lvwMain = (ListView) findViewById(R.id.lvw_main);
		btnExit = (Button) findViewById(R.id.btn_exit);
		btnClean = (Button) findViewById(R.id.btn_clean);
		imgShow = (ImageView) findViewById(R.id.img_show);
	}

	

	public static void writeLog(String obj) {
		Message msg = new Message();
		msg.what = StateLog.LOG;
		msg.obj = obj + "\n";
		handler.sendMessage(msg);
	}

}
