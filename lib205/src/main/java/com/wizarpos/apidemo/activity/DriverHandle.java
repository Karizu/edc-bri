/**
 * 
 */
package com.wizarpos.apidemo.activity;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

/**
 * @author john
 *
 */
public class DriverHandle implements HandleL{
	
	protected TextView textView = null;
	protected Handler mHandler = null;

//	template method
	@Override
	public void executeClickItemOperate(String command, Context context) {
//		((Activity)context).findViewById(R.id.);
		textView.setText("Welcome!\n");
		if(command.equals("1")){
			
		}else if(command.equals("2")){
			
		}else if(command.equals("3")){
			
		}else if(command.equals("4")){
			
		}else{
			textView.setText("can't open test program");
		}
	}

}