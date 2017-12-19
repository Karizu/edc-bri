package com.wizarpos.apidemo.smartcard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.SmartCardEvent;
import com.wizarpos.apidemo.jniinterface.SmartCardInterface;
import com.wizarpos.apidemo.jniinterface.SmartCardSlotInfo;
import com.wizarpos.apidemo.util.LogHelper;

public class SmartCardHandle extends DriverHandle {

    private static int nCardHandle = -1;
    private static boolean bOpenFlag = false;
    private static SmartCardSlotInfo mSlotInfo = null;

    private static boolean bExitThreadFlag = false;
    private static PollThread pollThread = null;
    private static Handler mHandler = null;


    private static boolean isFirst = true;

    @Override
    public void executeClickItemOperate(String command, Context context) {

        textView = ResourceManager.getTextViewFromSecondMainActivity((Activity) context);//这个取值需要做些修改。
        if (isFirst) {
            isFirst = false;
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    int nEventID = bundle.getInt("nEventID");
                    int nSlotIndex = bundle.getInt("nSlotIndex");
                    String strDisplay = new String();
                    strDisplay += String.format("SlotIndex : %d Event : %s", nSlotIndex, nEventID == SmartCardEvent.SMART_CARD_EVENT_INSERT_CARD ? "inserted" : "removed");
                    int nResult = SmartCardInterface.smartcardQueryPresence(0);
                    Log.i("APP", String.format("SmartCardQueryPresence(0) return value = %d\n", nResult));
                    textView.setText("Status:" + strDisplay);
                    return;
                }
            };


            int nResult = SmartCardInterface.smartcardInit();
            if (nResult >= 0) {
                nResult = SmartCardInterface.smartcardQueryMaxNumber();
                Log.i("APP", String.format("SmartCardQueryMaxNumber() return value = %d\n", nResult));
                nResult = SmartCardInterface.smartcardQueryPresence(0);
                Log.i("APP", String.format("SmartCardQueryPresence(0) return value = %d\n", nResult));
                textView.setText(String.format("Status:" + "SlotIndex : 0 Event : %s", nResult >= 1 ? "inserted" : "removed"));

            }
            nResult = openDrive((Activity) context);
            if (nResult >= 0) {
                bOpenFlag = true;
                nCardHandle = nResult;
                pollThread = new PollThread();
                pollThread.start();
            }
        }


        if (command.equals("PowerOn")) {
            try {
                powerOn();
            } catch (Exception e) {
                LogHelper.infoAppendMsgAndColor(textView, "\nerror:Not connected\n", 4);
            }
        } else if (command.equals("GetRandom")) {
            getRandom();
        } else if (command.equals("PowerOff")) {
            closeDriveItem();
        }
    }

    private void closeDriveItem() {
        bExitThreadFlag = true;
        try {
            pollThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (bOpenFlag) {
            SmartCardInterface.smartcardPowerOff(nCardHandle);
        }
        SmartCardInterface.smartcardTerminate();
    }

    private void powerOn() {
        if (!bOpenFlag)
            return;

        Log.i("APP", "power on\n");
        byte[] byteArrayATR = new byte[64];
        Log.i("APP", String.format("a.length = %d", byteArrayATR.length));

        mSlotInfo = new SmartCardSlotInfo();
        int nResult = SmartCardInterface.smartcardPowerOn(nCardHandle, byteArrayATR, mSlotInfo);
//		Loo
        if (nResult > 0) {
            String strDisplay = new String();
            for (int i = 0; i < nResult; i++)
                strDisplay += String.format("%02X ", byteArrayATR[i]);
            textView.setText("message:" + strDisplay);
            Log.i("APP", String.format("protocol = %d nSlotInfoItem = 0x%X\n", mSlotInfo.protocol, mSlotInfo.nSlotInfoItem));
        }
    }

    private void getRandom() {
        Log.i("APP", "get random\n");
        byte[] byteArrayAPDU = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x08};//
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[32];

        int nResult = SmartCardInterface.smartcardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        if (nResult > 0) {
            String strDisplay = new String();
            for (int i = 0; i < nResult; i++)
                strDisplay += String.format("%02X ", byteArrayResponse[i]);
            textView.setText(textView.getText() + "\nRandom:" + strDisplay);
        }
    }

    private void sendAPDU(byte[]byteArrayAPDU) {
        Log.i("APP", "get random\n");
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[32];

        int nResult = SmartCardInterface.smartcardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        if (nResult > 0) {
            String strDisplay = new String();
            for (int i = 0; i < nResult; i++)
                strDisplay += String.format("%02X ", byteArrayResponse[i]);
           Log.i("BRIZZI","Random:" + strDisplay);
        }
    }


    public class PollThread extends Thread {
        public void run() {
            while (true) {
                int nReturn = -1;
                if (bExitThreadFlag) {
                    Log.i("APP", "exit thread....\n");
                    break;
                }
                SmartCardEvent event = new SmartCardEvent();
                nReturn = SmartCardInterface.smartcardPollEvent(2000, event);
                if (nReturn >= 0) {
                    NotifyEvent(event);
                }
                //Log.i("APP", "poll event....\n");
            }
        }
    }

    private void NotifyEvent(SmartCardEvent event) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("nEventID", event.nEventID);
        bundle.putInt("nSlotIndex", event.nSlotIndex);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public int openDrive(Activity host) {
        int result = 0;
        try {
            result = SmartCardInterface.smartcardOpen(0);
        } catch (Exception e) {
            Toast.makeText(host, "can't open driver：" + this, Toast.LENGTH_LONG).show();
        }

        return result;
    }

}
