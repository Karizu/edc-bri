package com.wizarpos.apidemo.contactlesscard;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.ContactlessEvent;
import com.wizarpos.apidemo.jniinterface.ContactlessInterface;
import com.wizarpos.apidemo.util.StringUtil;

public class ContactlessControler {
	public static int[] hasMoreCards = new int[1];
	public static int[] cardType = new int[1];
	private String TAG = "CONTACTLESS";
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private Handler mHandler;
//	private Context host;
	private boolean isAttach = false;
	private static boolean bExitThreadFlag = false;
	private PollThread pollThread = null;
	
	public class PollThread extends Thread
	{
		public void run(){
			while(!bExitThreadFlag){
				int result = -1;
				ContactlessEvent event = new ContactlessEvent();
				result = ContactlessInterface.PollEvent( -1, event); // 启动下一次监听
				if(result >= 0){
					result = ContactlessInterface.queryInfo(hasMoreCards, cardType);
					Log.i(TAG, "queryInfo result = " + result + " | thasMoreCards = " + hasMoreCards[0] + " | tcardType = " + cardType[0]);
					if(result >= 0){
						notifyEvent(event);
					}
				}else{
					Log.i(TAG, "poll event error ! result = " + result);
				}
				Log.i("APP", "poll event....\n");
			}
		}
	}
	private void  notifyEvent(ContactlessEvent event){
		Message msg = new Message();
    	Bundle bundle = new Bundle();
		int nEventID = event.nEventID;
		int nEventDataLength = event.nEventDataLength;
		byte[] arryEventData = event.arryEventData;
    	bundle.putInt("nEventID", nEventID);
    	bundle.putInt("nEventDataLength", nEventDataLength);
    	bundle.putByteArray("arryEventData", arryEventData);
		String uid = "";
		for (int i = 3; i < nEventDataLength; i++)
			uid += String.format("%02X ", arryEventData[i]);
		bundle.putString("uid", uid.replace(" ", ""));

		msg.setData(bundle);
		String result = attatch();
		Log.i(TAG,"CONTACT_LESS ATTACH "+result);
    	if(result != null){
			mHandler.sendMessage(msg);
		}
	}
	
	
	
	public ContactlessControler(Handler mHandler, Context host){
		this.mHandler = mHandler;
	}
	
	
	public boolean searchBegin(){
		
		bExitThreadFlag = false;
		int result = ContactlessInterface.Open();
		Log.d(TAG, "open result = " + result);

		
		result = ContactlessInterface.SearchTargetBegin(ContactlessInterface.CONTACTLESS_CARD_MODE_AUTO, 1, -1);
		Log.d(TAG, "searchBegin result = " + result);
		if(result >=0){

			pollThread = new PollThread();
			pollThread.start();
		}
    	
    	return result >=0 ;
	}
	public boolean searchEnd(){
		bExitThreadFlag = true;
		pollThread = null;
		int result = ContactlessInterface.SearchTargetEnd();
		Log.e(TAG, "searchEnd result is " + result);
		result = ContactlessInterface.Close();
		return result >= 0;
	}

	public String attatch(){
		String reValue = null;
		byte arryATR[] = new byte[255]; 
		int nResult = ContactlessInterface.AttachTarget(arryATR);
//		reValue = StringUtil.getFormatString( arryATR , nResult);
		Log.i(TAG, "is attatch " + StringUtil.getFormatString(arryATR, nResult));
		Log.i(TAG, "CONTACT LESS " + nResult);
		if(nResult > 0)
		{
			reValue = StringUtil.getFormatString( arryATR , nResult);
			Log.i(TAG, "attatch "+reValue);
		}else{
			reValue = null;
			Log.i(TAG, String.format("AttachTarget return value = %d\n", nResult));
		}
		return reValue;
	}
	
    public boolean virify(byte [] data){
//		ContactlessInterface.Open();
		boolean isSuccess = false;
		byte[] bytes = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};	/*"FFFFFF".getBytes();*/
		int result = ContactlessInterface.VerifyPinMemory(0, 0, bytes, bytes.length);
		Log.d(TAG, "virify result =" + result + ", bytes.length = " + bytes.length);
		if(result >= 0){
			isSuccess = true;
		}
		return isSuccess;
	}
	public String read(int sectorIndex, int blockIndex){
		byte[] pDataBuffer =  new byte [16];
		int result = ContactlessInterface.ReadMemory(sectorIndex, blockIndex, pDataBuffer, pDataBuffer.length);
		String temp = null;
		if(result > 0){
			for(byte b : pDataBuffer){
				temp =temp + " " + b;
			}
			temp = getFormatString( pDataBuffer);
		}
		Log.d(TAG, "ReadMemory " + temp +", result = " + result);
		return temp;
	}
	public boolean write(int sectorIndex, int blockIndex , byte [] str){
		boolean isSuccess = false;
		byte [] bytes = str;
		int result = ContactlessInterface.WriteMemory(sectorIndex, blockIndex, bytes, bytes.length);
		Log.d(TAG, "write result ="+ result);
		if(result >= 0){
			isSuccess = true;
		}
		return isSuccess;
	}
	
	public String read15693(int sectorIndex, int blockIndex){
		byte[] pDataBuffer =  new byte [16];
		int result = ContactlessInterface.Read15693Memory(sectorIndex, blockIndex, pDataBuffer, pDataBuffer.length);
		String temp = null;
		if(result > 0){
			for(byte b : pDataBuffer){
				temp =temp + " " + b;
			}
			temp = getFormatString( pDataBuffer);
		}
		Log.d(TAG, "ReadMemory " + temp +", result = " + result);
		return temp;
	}
	
	public boolean write15693(/*int sectorIndex,*/ int blockIndex , byte [] str){
		boolean isSuccess = false;
		byte [] bytes = str;
		Log.d(TAG, "invoke write15693 data size is " + bytes.length);
		int result = ContactlessInterface.Write15693Memory(blockIndex, bytes, bytes.length);
		Log.d(TAG, "write result ="+ result +", data size is " + bytes.length);
		if(result >= 0){
			isSuccess = true;
		}
		return isSuccess;
	}
	
	public boolean dettatch(){
		int result =  ContactlessInterface.DetachTarget();
		return result >= 0;
	}
	
	/**
	 * byte arryAPDU_selectAP[] = new byte[]{0x00, (byte)0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31};
	 * byte arryAPDU_GPO[] = new byte[]{
		(byte)0x80, (byte)0xA8, 0x00, 0x00, 0x23, (byte)0x83, 0x21, 0x7E, 
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
		0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x56,
		0x19, 0x70, 0x03, 0x00, (byte)0xEF, 0x39, (byte)0xAD, (byte)0xCE};
	 * */
	
	public String transmit(byte [] apdu){
		String res = null;
		byte[] apduCommand = null;
		byte[] apduResponse = new byte[255];
		if(apdu != null){
			apduCommand = apdu ;
		}else{
			apduCommand = new byte[]{0x00, (byte)0x84, 0x00, 0x00, (byte)0x08};// this is a get random aptu command . but some card may be can't support.
		}

		int result = ContactlessInterface.Transmit(apduCommand, apduCommand.length, apduResponse);
		if(result < 0){
			res = null;
		}else{
			String strDisplay = new String();
			for(int i = 0; i < result; i++)
				strDisplay += String.format("%02X ", apduResponse[i]);
			res = strDisplay;
		}
		
		return res;
	}

	public String transmitCmd(String apdu) {
		String res = "";
		byte[] apduCommand = hexStringToByteArray(apdu);
		byte[] apduResponse = new byte[255];
		int result = ContactlessInterface.Transmit(apduCommand, apduCommand.length, apduResponse);
		Log.i(TAG,"CONTACTLESS RESULT "+StringUtil.getFormatString(apduResponse, result));
		if (result < 0) {
			res = "";
		} else {
			res = StringUtil.getFormatString(apduResponse, result);
		}
		return res.replace(" ", "");
	}

	public String getCommand(int fileId,int length){
		byte[] bts = {
				-67,
				(byte)fileId,
				0,
				0,
				0,
				(byte)length,
				0,
				0};
		return bytesToHex(bts);
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexStringToByteArray(String s) {
		if ((s.length()%2)!=0) {
			s = s+"F";
		}
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	public String getRenderVersion(){
		String renderVersion = null;
		byte arryData[] = new byte[255];
		int nResult = ContactlessInterface.SendControlCommand(ContactlessInterface.RC500_COMMON_CMD_GET_READER_VERSION, arryData, arryData.length);
		if(nResult > 0){
			String strDisplay = new String();
			for(int i = 0; i < nResult; i++){
				strDisplay += String.format("%02X ", arryData[i]);
			}
			renderVersion = strDisplay;
		}else{
			renderVersion = null;
		}
		return renderVersion;
	}
	
	private  String getFormatString(byte [] bytes){
		String value = "";
		for(byte b : bytes){
			value += String.format("%02X ", b);
		}
		return value;
	}
	
}
