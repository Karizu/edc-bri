package com.wizarpos.apidemo.pinpad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.R;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.PinpadInterface;

public class PinpadHandle extends DriverHandle{

	private static boolean bOpened = false;
	private static boolean bShowTextFlag = true;
	
	static final int ENCRYPT_TEXT_DIALOG = 0;
	static final int PINBLOCK_DIALOG = 1;
	static final int MAC_DIALOG = 2;
	
	
	@Override
	public void executeClickItemOperate(String command, Context context) {
		textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);
		((Activity)context).findViewById(R.id.txv_result);
		if(command.equals("ShowText")){
			showText();
		}else if(command.equals("EncryptText")){
			encryptText(context);
		}else if(command.equals("CalculatePINBLOCK")){
			calculatePINBLOCK(context);
		}else if(command.equals("CalculateMAC")){
			calculateMAC(context);
		}else if(command.equals("CloseDrive")){
			closeDriveItem();
		}else if(command.equals("OpenDrive")){
			int nResult = PinpadInterface.PinpadOpen();
			Log.i("APP", String.format("PinpadOpen() return value = %d\n", nResult));
	        if(nResult >= 0)
	        {
	        	bOpened = true;
	        	nResult = PinpadInterface.PinpadSelectKey(PinpadInterface.KEY_TYPE_MASTER, 0, 0, 1);
	        	Log.i("APP", String.format("PinpadSelectKey() return value = %d\n", nResult));
	        	//PinpadInterface.PinpadClose();
	        }
		}else if(command.equals("UpdateUserKey")){
			int nMasterKeyID = 100;
			int nUserKeyID = 30;
			byte [] userkey = new byte[]{
					0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
					0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
			};
			int result = PinpadInterface.PinpadUpdateUserKey(nMasterKeyID, nUserKeyID, userkey, userkey.length);
			Log.i("APP", "pinpad： result = " + result);
		}else if(command.equals("getSerialNo")){
			byte[] arrySerialNo = new byte[40];
//			PinpadInterface.PinpadOpen(); 
			int length = PinpadInterface.getSerialNo(arrySerialNo);
			Log.d("APP", "length = " + length);
//			PinpadInterface.PinpadClose();
			
			textView.setText("\nSerialNo = " + new String(arrySerialNo));
		}
		
	}
	
	public void closeDriveItem(){
		if(bOpened)
			PinpadInterface.PinpadClose();
	}
	
	private void showText(){
		if(!bOpened)
			return;
		if(bShowTextFlag){
			//Show text in line 1 and line 2// (byte)0x80, (byte)0x81, (byte)0x82, (byte)0x83
			byte[] arryTextLine1 = "Show Test:".getBytes();
			PinpadInterface.PinpadShowText(0, arryTextLine1, arryTextLine1.length, 0);
			byte[] arryTextLine2 = new byte[]{(byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87};
			int rs = PinpadInterface.PinpadShowText(1, arryTextLine2, arryTextLine2.length, 0);
			Log.i("PINPAD",rs+"");
			bShowTextFlag = false;
		}else{
			//clear screen
			PinpadInterface.PinpadShowText(0, null, 0, 0);
			bShowTextFlag = true;
		}
	}
	
	private void encryptText(Context context){
		((Activity) context).showDialog(ENCRYPT_TEXT_DIALOG);//
	}
	
	private void calculatePINBLOCK(Context context){
		((Activity) context).showDialog(PINBLOCK_DIALOG);
	}
	
	private void calculateMAC(Context context){
		((Activity) context).showDialog(MAC_DIALOG);
	}
	
}
