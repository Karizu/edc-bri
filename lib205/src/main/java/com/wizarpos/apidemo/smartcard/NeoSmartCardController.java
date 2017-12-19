package com.wizarpos.apidemo.smartcard;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.cloudpos.jniinterface.SmartCardInterface;
import com.cloudpos.jniinterface.SmartCardSlotInfo;
import com.wizarpos.drivertest.util.StringUtil;
import com.wizarpos.function.smr.SmartCardMagicConvert;

/**
 * Created by indra on 04/12/15.
 */
public class NeoSmartCardController {
    int SAM_READY_NOTIFIER = 1;
    int CARD_TAP_NOTIFIER = 2;
    int HOST_REPLY_NOTIFIER = 3;
    int CARD_RESPONSE_ERROR = 4;
    int CARD_RESPONSE_FINISH = 5;
    int SAM_NOT_READY = 6;
    private String TAG = "SAMCARD";
    private static int nCardHandle = 0;
    private Context host;
    //protected static CardData cData ;
//    private static SmartCardMagicConvert jni = SmartCardMagicConvert.getSingleTon();
    private boolean isOpen = false;

    protected void notifyHandler(int val) {
        Message msg = new Message();
        msg.what = val;
//        myHandler.sendMessage(msg);
    }

    public NeoSmartCardController(Context context){
        this.host = context;
    }

    public synchronized boolean starting(int index) {
        Log.d(TAG, "Smartcard start");
        if (isOpen) {
            Log.d(TAG, "Interface already opened");
            return true;
        } else {
            int result = SmartCardInterface.open(index);
            Log.d(TAG, "Smartcard interface open = " + result);
            if (result<0) {
                return false;
            }
            nCardHandle = result;
            isOpen = true;
        }

        byte[] byteArrayATR = new byte[64];
        SmartCardSlotInfo mSlotInfo = new SmartCardSlotInfo();
        int result = SmartCardInterface.powerOn(nCardHandle, byteArrayATR, mSlotInfo);
        Log.d(TAG, "Power on result = " + result);
        if (result < 0) {
            Log.d(TAG, "Power on failed, closing interface...");
            if (isOpen) {
                result  = SmartCardInterface.close(nCardHandle);
                Log.d(TAG, "Smartcard interface close = " + result);
                if (result>=0) {
                    isOpen = false;
                }
            }
            return false;
        }
        return true;
    }

    public String sendCmd(byte[] byteArrayAPDU) {
        byte[] byteArrayResponse = new byte[129];
        int result = SmartCardInterface.transmit(nCardHandle, byteArrayAPDU, byteArrayResponse);
        String transmitResult = "";
        String command = StringLib.toHexString(byteArrayAPDU, 0, byteArrayAPDU.length, false);
        Log.d(TAG, "Transmit result = "+ result);
        if (result >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, result).replace(" ", "");
            Log.d(TAG, "Cmd :" + command + " || sResult : " + transmitResult + " | " + transmitResult.length());
            Log.i(TAG, "depan:" + transmitResult.substring(0, 2) + "|" + transmitResult.length());
            if (transmitResult.length() == 4 && transmitResult.substring(0, 2).equals("61")) {
                transmitResult = sendReqResponse(transmitResult);
            }

            return transmitResult;
        } else {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, byteArrayResponse.length);
            Log.d(TAG, "Cmd :" + command + " || Error transmitResult: " + transmitResult);
            return transmitResult;
        }
    }

    public String sendReqResponse(String apduCommand) {
        Log.d(TAG, "kirim sam query : 00C00000" + apduCommand.substring(2, 4));
        byte[] byteArrayAPDU = hexStringToByteArray("00C00000" + apduCommand.substring(2, 4));
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[129];
        int result = SmartCardInterface.transmit(nCardHandle, byteArrayAPDU, byteArrayResponse);
        String transmitResult = "";
        String command = StringLib.toHexString(byteArrayAPDU, 0, byteArrayAPDU.length, false);
        if (result >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, result).replace(" ", "");
            Log.d(TAG, "Cmd :" + command + " || sResult : " + transmitResult + " | " + transmitResult.substring(0, 1));
            return transmitResult;
        } else {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, result);
            Log.d(TAG, "Cmd :" + command + " || Error transmitResult: " + transmitResult);
            return null;
        }
    }

    public void closedevice() {
        int result = SmartCardInterface.powerOff(nCardHandle);
        Log.d(TAG, "Power off result = " + result);
        if (result >= 0) {
            Log.d(TAG, "Power off success, closing controller");
            result = SmartCardInterface.close(nCardHandle);
            Log.d(TAG, "Smartcard interface close = " + result);
            if (result>=0) {
                isOpen = false;
            }
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
