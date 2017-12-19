package com.wizarpos.apidemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.wizarpos.apidemo.jniinterface.SerialPortInterface;
import com.wizarpos.apidemo.util.StringUtility;

public class SecondMainActivity extends Activity {
	
	//按照jni借口放入按钮名子。
	private String btnList_1 ="SearchBegin,Attach,TransmitAPDU,Dettatch,SearchEnd";
	private String btnList_7 ="SearchBegin,Verify,Write,Read,SearchEnd";
	
	
	private String btnList_2 ="SwipCard,SwipCardFinish";
	private String btnList_3 ="OpenDrive,ShowText,EncryptText,CalculatePINBLOCK,CalculateMAC,UpdateUserKey,getSerialNo,CloseDrive";
	private String btnList_4 ="PrintData,PrintPurchaseBill,PrintFont,PrintQRCode,PrinterOutOfPaper";
	private String btnList_5 ="OpenDriver,Write,Read,CloseDrive";
	private String btnList_6 ="PowerOn,GetRandom,PowerOff";
	private String btnList_8 ="Open Money Box";
	
	//dialog
	private Dialog currentDlg = null;

	// 存储每一次进入测试界面所需要的的按钮。
	public static List<String> arryBtn;
	
	public enum StateType {
		contactless1,contactless2, msr, pinpad, printer, serial, smartcard,moneybox
	};
	public static StateType state = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second_main);
		TextView textView = ResourceManager.getTextViewFromSecondMainActivity(this);
		textView.setMovementMethod(ScrollingMovementMethod.getInstance());
		arryBtn = new ArrayList<String>();// return 后销毁原先的activity
		stateHandle();
		ListView buttonListView = ResourceManager.getListViewFromSecondMainActivity(this);
		buttonListView.setAdapter(new MyAdapter(this, R.layout.group_list_item,
				arryBtn));
		// buttonListView.getvi
		buttonListView.setOnItemClickListener(new ListViewItemListener(this));
		// return
		Button reBtn = ResourceManager.getReturnButtonFromSecondMainActivity(this);
		reBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.exit(0);
			}
		});
		// exit
		Button exit = ResourceManager.getExitButtonFromSecondMainActivity(this);
		exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(state.equals(StateType.serial)){
					SerialPortInterface.flush_io();//flush_io
				}
				System.exit(0);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_second_main, menu);
		return true;
	}
	
	private void stateHandle() {
		String temp = "No data,Check secondmain state";
		if (state.equals(StateType.contactless1)) {
			temp = btnList_1;
		} else if (state.equals(StateType.msr)) {
			temp = btnList_2;
		} else if (state.equals(StateType.pinpad)) {
			temp = btnList_3;
		} else if (state.equals(StateType.printer)) {
			temp = btnList_4;
		} else if (state.equals(StateType.serial)) {
			temp = btnList_5;
		} else if(state.equals(StateType.smartcard)){
			temp = btnList_6;
		} else if(state.equals(StateType.contactless2)){
			temp = btnList_7;
		} else if(state.equals(StateType.moneybox)){
			temp = btnList_8;
		}
		initArryBtn(temp);
	}

	private void initArryBtn(String temp) {
		arryBtn.clear();
		String[] strs = StringUtility.spiltStrings(temp, ",");
		for (String s : strs) {
			arryBtn.add(s);
		}
	}
	// 为Dialog服务的功能
		protected Dialog onCreateDialog(int id) {
			currentDlg = new CustomDialog(SecondMainActivity.this, id);
			currentDlg.show();
			return currentDlg;
		}

		@Override
		protected void onPrepareDialog(int id, Dialog dialog) {
			if (currentDlg != null) {
				((CustomDialog) currentDlg).clearFields();
			}
		}

}
