package com.wizarpos.apidemo.money;

import android.app.Activity;
import android.content.Context;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.R;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.MoneyboxInterface;

public class MoneyBoxHandle extends DriverHandle{
	
	@Override
	public void executeClickItemOperate(String command, Context context) {
		if(command.equals("Open Money Box")){
			textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);
			((Activity)context).findViewById(R.id.txv_result);
			int result = MoneyboxInterface.open();
			String msg = "";
			if(result < 0){
				String errorMessage = "error log";
				textView.setText(errorMessage);
				MoneyboxInterface.close();
				return ;
			}
			result =  MoneyboxInterface.openMoneyBox();
			msg = "success open Money , and  debug information is " + result;
			result =  MoneyboxInterface.close();
			if(result < 0){
				String errorMessage = "can't close money box.";
				textView.setText(errorMessage);
				MoneyboxInterface.close();
				return ;
			}
			
			textView.setText(msg);
		}
	}

}
