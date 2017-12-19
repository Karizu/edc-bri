package com.wizarpos.apidemo.serial;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.SerialPortInterface;
import com.wizarpos.apidemo.util.LogHelper;

public class SerialTestHandle extends DriverHandle{
	
	@Override
	public void executeClickItemOperate(String command, Context context) {
//		OpenDriver	Write		Read		CloseDrive
		textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);//这个取值需要做些修改。
		Activity host = (Activity)context;
		if(command.equals("OpenDriver")){
			openDriver(host);
		}else if(command.equals("Write")){
			writeSerial(host);
		}else if(command.equals("Read")){
			readSerial(host);
		}else if(command.equals("CloseDrive")){
			closeDriver(host);
		}
	}
	
	private int openDriver(Activity host){
		int result = 0;
		try {
			result = SerialPortInterface.open();
		} catch (Exception e) {
			Toast.makeText(host, "不能打开:"+result, Toast.LENGTH_LONG).show();
		}
		if(result > 0){
		    LogHelper.infoMsgAndColor(textView, "\nOpen Serial Port Success!", 3);
		}else{
		    LogHelper.infoMsgAndColor(textView, "\nOpen Serial Port Failed!", 4);
		}
		return result;
	}
	private int closeDriver(Activity host){
		int result = 0 ;
		try {
			result= SerialPortInterface.close();
			LogHelper.infoAppendMsgAndColor(textView, "\n Close driver:result code :"+result, 3);
		} catch (Exception e) {
			Toast.makeText(host, "不能关闭:"+result, Toast.LENGTH_LONG).show();
			LogHelper.infoAppendMsgAndColor(textView, "\nClose Serial happened exception! errorcode"+result, 4);
		}
		return result;
	}
	
	private void readSerial(Activity host){
		int result = 0;
//		result = SerialPortInterface.set_baudrate(115200);
        byte [] data = new byte[8];
        result = SerialPortInterface.read(data, 0, 8, 1000);
        LogHelper.infoAppendMsgAndColor(textView, "\nRead Serial result is " + result + ", content is " + new String (data), 3);
        Toast.makeText(host, "ERRORCODE:" + result, Toast.LENGTH_LONG).show();
		
	}
	
	//利用串口写数据。
	private static final String TESTSTR = "send Test Data";
	private void writeSerial(Activity host){
		int result = 0;
		try {
//            SerialPortInterface.set_baudrate(115200);
            byte [] data = new String (TESTSTR).getBytes();
            result = SerialPortInterface.write(data, data.length);
            LogHelper.infoAppendMsgAndColor(textView, "\n发送的数据如下：‘" + TESTSTR + "'", 3);
		} catch (Exception e) {
			LogHelper.infoMsgAndColor(textView, "\n writer Serial happened exception! errorcode" + result, 4);
		}
	}

}
