/**
 * 
 */
package com.wizarpos.apidemo.activity;



import java.io.UnsupportedEncodingException;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wizarpos.apidemo.jniinterface.PinpadInterface;
import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.util.StringUtility;

/**
 * @author john
 *
 */
public class CustomDialog extends Dialog{
	
	private int mID;
	final CharSequence[] strTitle = {"Encrypt", "PINBLOCK", "MAC", "PrintDate"};
	private  EditText etInput = null;
	private static TextView txvResult = null;
//	private 
	public CustomDialog(Context context ,int mID) {
		super(context);
		etInput = ResourceManager.getEditTextFromCustomDialog(this);
		this.mID = mID;
		if(mID == 1 && etInput != null){
			etInput.setText("0000000000000000");
		}
	}
	
	
	//自动转换不同测试蕾蓉的标题。设计强用到这里不合理
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.custom_dlg);
		etInput = ResourceManager.getEditTextFromCustomDialog(this);
		txvResult = ResourceManager.getResultTextFViewFromCustomDialog(this);
		Button ok = ResourceManager.getOkBtnFromCustomDialog(this);
		Button cancel = ResourceManager.getCancelBtnFromCustomDialog(this);
		if(mID == 1){
			etInput.setText("0000000000000000");
		}
		
		//对于ok的处理方式不好。但是暂时这样。
		ok.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				switch(mID)
				{
				case 0:
					calculate_encrypt();
					break;
				case 1:
				{
					
					calculate_pinblock();
					break;
				}
				case 2:
					calculate_mac();
					break;
				case 3:
					printDateItem(etInput.getText().toString());
					break;
				default:
					break;
				}
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				CustomDialog.this.dismiss();
			}
		});
		
		handle = new Handler(){
		    
		    @Override
            public void handleMessage(Message msg) {
		        String strInput = "123456789012345678";
                etInput.setText(strInput);
                if(msg.what == RESULT_INPUT){
                    txvResult.setText(msg.obj.toString());
                }
            }
		};
		
	}
	
	Handler handle ;
	//clear text
	public void clearFields(){
		etInput.setText("");
		txvResult.setText("");
	}
	
	private void printDateItem(String strInput){
		Log.i("APP", strInput);
		strInput = strInput+ "\n\t天空1" + "\n\t()天空2";
		int resultJni = PrinterInterface.PrinterOpen();
        if(resultJni <0){
            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return ;
        }
		PrinterInterface.PrinterBegin();

		byte[] arryData = null;
		int nLength = 0;
		try {
			arryData = strInput.getBytes("gb2312");//gb2313
			nLength = arryData.length;
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		PrinterInterface.PrinterWrite(arryData, nLength );

//		PrinterInterface.PrinterWrite("\n".getBytes(), 1);

		PrinterInterface.PrinterEnd();
		PrinterInterface.PrinterClose();
	}
	static boolean isFinish = true;
	static final int ERR_INPUT = -1;
	static final int RESULT_INPUT = 0;
	Runnable th = new Runnable(){
		public void run(){
			byte[] arryPinBlockBuffer = new byte[32];
			String strInput = etInput.getText().toString();
			if(strInput.equals("")|| strInput.length()!=18){
			    strInput = "123456789012345678";
			    handle.sendEmptyMessage(ERR_INPUT);
			}
			int nResult = PinpadInterface.PinpadCalculatePinBlock(strInput.getBytes(), strInput.getBytes().length, arryPinBlockBuffer, -1, 0);
			if(nResult < 0)
				return;
			
			String strShow = StringUtility.ByteArrayToString(arryPinBlockBuffer, nResult);
			
			Message msg = new Message();
			msg.what =RESULT_INPUT ;
			msg.obj = strShow;
			handle.sendMessage(msg);
			isFinish = true;
		}
	};;
	protected void calculate_pinblock()
	{
	    Log.e("APP", "calculate_pinblock , isFinish = " + isFinish);
		if(isFinish){
			isFinish = false;
			new Thread(th).start();
		}
		return;
	}
	
	protected void calculate_mac()
	{
		byte[] arryByte = new byte[255];
		String strInput = etInput.getText().toString();
		int nResult = 0/*StringUtility.StringToByteArray(strInput, arryByte)*/;
//		Log.i("APP", String.format("nResult = %d\n", nResult));
//		if(nResult < 0)
//			return;
		
		if(strInput.equals("")){
		    strInput = "0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38";
		    etInput.setText(strInput);
		}
		
		byte[] arryMACOutBuffer = new byte[32];
		
		arryByte = new byte[]{0x30,0x31,0x32,0x33 , 0x34 , 0x35 , 0x36, 0x37};
		nResult = PinpadInterface.PinpadCalculateMac(arryByte, arryByte.length, PinpadInterface.MAC_METHOD_X99, arryMACOutBuffer);
		Log.i("APP", "PinpadCalculateMac: nResult = " + nResult);
		if(nResult < 0)
			return;
				
		String strShow = StringUtility.ByteArrayToString(arryMACOutBuffer, nResult);
		txvResult.setText(strShow);
		return;
	}
	
	protected void calculate_encrypt()
	{
		byte[] arryByte = new byte[255];
		String strInput = etInput.getText().toString();
		Log.i("APP", "strInput:"+strInput);
		strInput = strInput.trim();
		if(strInput.equals("")){
		    strInput = "0x38 0x38 0x38 0x38 0x38 0x38 0x38 0x38";
		    etInput.setText(strInput);
		}
		int nResult = StringUtility.StringToByteArray(strInput, arryByte);
		Log.i("APP", String.format("nResult = %d\n", nResult));
//		if(nResult < 0)
//			return;
		/* String strLog = StringUtility.ByteArrayToString(arryByte, nResult);
		 * Log.i("APP", strLog);
		*/
		arryByte = new byte[]{0x38,0x38,0x38,0x38,0x38,0x38,0x38,0x38};
		
		byte[] arryCipherTextBuffer = new byte[255];
		nResult = PinpadInterface.PinpadEncryptString(arryByte, arryByte.length, arryCipherTextBuffer);
		if(nResult < 0)
			return;
		String strShow = StringUtility.ByteArrayToString(arryCipherTextBuffer, nResult);
		txvResult.setText(strShow);
		return;
	}
}
