package com.wizarpos.apidemo.contactlesscard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.activity.SecondMainActivity;

/**@author john
 * */
public class ContactlessCardHandle2 extends DriverHandle{
	
	private static final String APP_TAG = "APP";
	
	private static ContactlessControler controler = null;
	
	
	
	public ContactlessCardHandle2(Context context){
		textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);//这个取值需要做些修改。
		mHandler = new Handler(){
        	@Override
        	public void handleMessage(Message msg){
        		
        		if(msg.arg1 == 1){
        			textView.append("\n\t\t "+ msg.obj);
        			return;
        		}
        		
        		Bundle bundle = msg.getData();
        		int nEventID = bundle.getInt("nEventID");
        		int nEventDataLength = bundle.getInt("nEventDataLength");
        		byte arryEventData[] = bundle.getByteArray("arryEventData");
        		
        		
        		if(nEventID == 0 && nEventDataLength > 0)
        		{
        			String strDisplay = new String();
        			boolean isMiFare = false;
        			boolean is15693 = false;
        			// 0x08 0x18
        			if(arryEventData[0]==0x08 || arryEventData[0]==0x18){
    					isMiFare = true;
    				}else if(arryEventData[0]==0x60){
    					is15693 = true;
    				}
        			for(int i = 0; i < nEventDataLength; i++){
        				strDisplay += String.format("%02X ", arryEventData[i]);
        			}
        			
        			Log.i(APP_TAG, "arryEventData = " + strDisplay);
        			Log.i(APP_TAG, "nEventID = " + nEventID);
            		Log.i(APP_TAG, "nEventDataLength = " + nEventDataLength);
            		textView.append("\n\t\t got event date : " + strDisplay);
            		if(isMiFare){
            			Log.e(APP_TAG, "card type is MiFare one card");
            			if(controler.virify(null)){
            				Log.i(APP_TAG, "virify pin Success");
            				textView.append("\n\t\t MiFare one card virify pin Success !");
            				
            				String readMsg = controler.read(0,0);
            				if(readMsg != null){
            					textView.append("\n\t\t MiFare one card read Success ! got date is : " + readMsg);
            				}else{
            					textView.append("\n\t\t MiFare one card read Failed !");
            				}
            				
//            				byte [] bytes = new byte[]{0x01,0x01,0x02,0x03,0x04,0x05,0x05,0x06,0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15};
//            				controler.write(bytes);
            				
            			}else{
            				Log.e(APP_TAG, "virify pin Failed");
            				textView.append("\n\t\t MiFare one card virify pin Failed , may be this card can't support !");
            			}
            		}else if(is15693){
            			Log.e(APP_TAG, "card type is 15693 one card");
            			int sectorIndex = 0;
            			int blockIndex = 0;
            			String msg2 = "";
            			msg2 = controler.read15693(sectorIndex, blockIndex);
            			if(msg2 == null){
            				textView.append("\n\t\t read 15693 card Failed , may be this card can't support !");
            			}else{
            				textView.append("\n\t\t read 15693 card success , data : " + msg2);
            				msg2 = "ABCD";
            				textView.append("\n\t\t write data to 15693 card  , data : " + msg2);
            				boolean result = controler.write15693(/*sectorIndex,*/ blockIndex, msg2.getBytes());
            				if(result){
            					textView.append("\n\t\t read 15693 card success , data : " + msg2);
            					msg2 = controler.read15693(sectorIndex, blockIndex);
            					if(msg2 == null){
                    				textView.append("\n\t\t read 15693 card Failed , may be this card can't support !");
                    			}else{
                    				textView.append("\n\t\t read 15693 card success , data : " + msg2);
                    			}
            				}else{
            					textView.append("\n\t\t write 15693 card Failed , may be this card can't support or removed !");
            				}
            			}
            			
            		}else{
            			textView.append("\n\t\t card type is normal contactless1 card!");
            			Log.e(APP_TAG, "card type is normal contactless1 card");
            			
            			if(SecondMainActivity.state == SecondMainActivity.StateType.contactless2){
            				return;
            			}
            			String attatchMsg = controler.attatch();
            			if(attatchMsg == null){
            				textView.append("\n\t\t response Attach = null , may be this card can't support !");
            			}else{
            				textView.append("\n\t\t response Attach = " + attatchMsg);
            			}
            			
            			
            			String reApdu = controler.transmit(null);
            			if(reApdu == null){
            				textView.append("\n\t\t response APDU = null , may be this card can't support !");
            			}else{
            				textView.append("\n\t\t response APDU = " + reApdu);
            			}
            			
            			
            			boolean isSuccess = controler.dettatch();
            			if(isSuccess){
            				textView.append("\n\t\t response Dettatch Success!");
            			}else{
            				textView.append("\n\t\t response Dettatch Failed!");
            			}
            			
            		}
        			
        		}
//        		controler.searchBegin();
        		}
        	};
        	controler = new ContactlessControler(mHandler, context);
	}
	
//	SearchBegin,Virify,Write,Read,SearchBegin
	@Override
	public void executeClickItemOperate(String command, Context context) {

        Log.e(APP_TAG, "command = " + command);
		if(command.equals("SearchBegin")){
			boolean isSuccess = controler.searchBegin();
			if(isSuccess){
				textView.append("\n\t\t response SearchBegin Success!");
			}else{
				textView.append("\n\t\t response SearchBegin Failed!");
			}
		}else if(command.equals("Verify")){
			if(controler.virify(null)){
				textView.append("\n\t\t MiFare one card virify pin Success !");
			}else{
				textView.append("\n\t\t response Attach = null , may be this card can't support or pin error !");
			}
		}else if(command.equals("Read")){
			String readMsg = controler.read(0,1);
			if(readMsg != null){
				textView.append("\n\t\t MiFare one card read Success ! [" + readMsg +"]");
			}else{
				textView.append("\n\t\t MiFare one card read Failed !");
			}
		}else if(command.equals("Write")){
			byte [] bytes = new byte[]{0x00,0x01,0x02,0x03,0x04,0x05,0x05,0x06,0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15};
			if(controler.write(0 , 1 , bytes)){
				textView.append("\n\t\t MiFare one card write Success !");
			}else{
				textView.append("\n\t\t MiFare one card write Failed !");
			}
		}else if(command.equals("SearchEnd")){
			boolean isSuccess = controler.searchEnd();
			if(isSuccess){
				textView.append("\n\t\t response SearchEnd Success!");
			}else{
				textView.append("\n\t\t response SearchEnd Failed!");
			}
		}
	}
	
	
	public void clear(){
		textView.setText("清理完成");
	}
	

}
