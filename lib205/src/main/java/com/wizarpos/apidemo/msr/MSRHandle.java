package com.wizarpos.apidemo.msr;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.HALMsrInterface;
import com.wizarpos.apidemo.util.LogHelper;

/**@author john
 * 处理与界面无关的逻辑
 * */
public class MSRHandle extends DriverHandle{
	
	private static boolean bExit;
	
	private static Handler mHandler = null;
	
	private static boolean isFirst = true;
	
	private TextView textView ;
//	String temp5 = "OpenDrive,CloseDrive,开始刷卡,结束刷卡";//MSR
	@Override
	public void executeClickItemOperate(String command, Context context) {
		if(isFirst){
			isFirst = false;
			mHandler = new Handler()
	        {
	        	@Override
	        	public void handleMessage(Message msg)
	        	{
	        		switch(msg.what)
	        		{
	        			case 0:
	        				break;
	        			case 1:
	        				break;
	        			case 2:
	        			{
	        				break;
	        			}
	        			case 3:
	        			{
	        				Log.i("HAL", "java callback, receive a message!\n");
	        				read_track_data();
	        				break;
	        			}
	        				
	        		}
	        	}
	        };
		}
		textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);//这个取值需要做些修改。
		
		if(command.equals("SwipCard")){
			startPos((Activity)context);
		}else if(command.equals("SwipCardFinish")){
			closeDriveItem();
//			System.exit(0);
		}else if(command.equals("CloseDrive")){
			closeDriveItem();
		}
	}

	public void closeDriveItem(){
		bExit = true;
		int errorCode = HALMsrInterface.msr_close();
		if(errorCode < 0){
			LogHelper.infoAppendMsgAndColor(textView, "\nClose driver happened error!\nError Code:" + errorCode, 4);
		}else{
			LogHelper.infoAppendMsgAndColor(textView, "\nClose driver success", 3);
		}
	}
	
	private void startPos(Activity host){
		int nResult = -1;
		//callJNIInt(1);
		bExit = false;
		nResult = openDrive(host);
		
		if(nResult >= 0)
		{
			LogHelper.infoMsgAndColor(textView, "Open MSR Driver success:", 3);
			new Thread()
			{
				@Override  
				public void run() 
				{
					while(true)
					{
						int nReturn = -1;
						
						if(bExit)
							break;
						nReturn = HALMsrInterface.msr_poll(2000);
						if(nReturn >= 0)
							notify_has_data();
					}
				}  
			 }.start();  

		}else{
			LogHelper.infoMsgAndColor(textView, "Open MSR Driver error!\nIf this is the first you're seen this stop error screen restart your pos.\nError code:" +
					nResult, 4);
		}
	}
	
	private void notify_has_data()
    {
    	Message msg = new Message();
    	msg.what = 3;
    	mHandler.sendMessage(msg);
    }
	
	private void read_track_data()
    {
    	int ret;
		byte[] byteArry = new byte[255];
		int length = 255;
		
		for(int i = 0; i < 3; i++)
		{
			ret = HALMsrInterface.msr_get_track_data(i, byteArry, length);
		
			if(ret > 0)
			{
				String strText = new String(byteArry, 0, ret);
				strText = "\nTrack"+"("+(i+1)+"):" + strText;
				LogHelper.infoAppendMsgAndColor(textView, strText, 3);
			}
		}
    }
	
	private int openDrive(Activity host){
		int result = 0;
		try {
			result = HALMsrInterface.msr_open();
		} catch (Exception e) {
			Toast.makeText(host, "不能打开驱动，请加权限或加入硬件:", Toast.LENGTH_LONG).show();
		}
		
		return result;
	}

}
