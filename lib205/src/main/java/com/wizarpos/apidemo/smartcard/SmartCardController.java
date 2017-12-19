package com.wizarpos.apidemo.smartcard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wizarpos.drivertest.util.StringUtil;
import com.wizarpos.function.smr.SmartCardMagicConvert;
import com.wizarpos.apidemo.jniinterface.SmartCardSlotInfo;

/**
 * Created by indra on 04/12/15.
 */
public class SmartCardController {
    int SAM_READY_NOTIFIER = 1;
    int CARD_TAP_NOTIFIER = 2;
    int HOST_REPLY_NOTIFIER = 3;
    int CARD_RESPONSE_ERROR = 4;
    int CARD_RESPONSE_FINISH = 5;
    int SAM_NOT_READY = 6;
    private String TAG = "SAMCARD";
    private static int nCardHandle = -1;
    private Context host;
    //protected static CardData cData ;
    private static SmartCardMagicConvert jni = SmartCardMagicConvert.getSingleTon();

    protected void notifyHandler(int val) {
        Message msg = new Message();
        msg.what = val;
//        myHandler.sendMessage(msg);
    }

    public SmartCardController(Context context){
        this.host = context;
    }

    public synchronized boolean starting(int index) {
        Log.d(TAG, "sam start");
        int result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardInit);
        Log.d(TAG, "init result " + index + " =" + result);
        jni.setSmartCardOpen(index);
        result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardOpen);
        if (result >= 0) {
            Log.d(TAG, "Init SAMCARD = " + result);
        } else {
            Log.d(TAG, "init SAMCARD = " + result);

        }

        if (result < 0) {
            notifyHandler(SAM_NOT_READY);
            return false;
        } else {

        nCardHandle = result;
        }
        byte[] byteArrayATR = new byte[64];
        SmartCardSlotInfo mSlotInfo = new SmartCardSlotInfo();
        jni.setSmartCardPowerOn(nCardHandle, byteArrayATR, mSlotInfo);
        int invokeResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardPowerOn);
        if (invokeResult < 0) {
            notifyHandler(SAM_NOT_READY);
            return false;
        }

        notifyHandler(SAM_READY_NOTIFIER);
        return true;
    }

    public String sendCmd(byte[] byteArrayAPDU) {
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[129];
        jni.setSmartCardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        int nResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTransmit);
        String transmitResult = "";
        String command = StringLib.toHexString(byteArrayAPDU, 0, byteArrayAPDU.length, false);
        Log.d(TAG, "RESULT AFTER TRANSACT "+nResult);
        if (nResult >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult).replace(" ", "");
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
        jni.setSmartCardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        int nResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTransmit);
        String transmitResult = "";
        String command = StringLib.toHexString(byteArrayAPDU, 0, byteArrayAPDU.length, false);
        if (nResult >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult).replace(" ", "");
            Log.d(TAG, "Cmd :" + command + " || sResult : " + transmitResult + " | " + transmitResult.substring(0, 1));
            return transmitResult;
        } else {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);
            Log.d(TAG, "Cmd :" + command + " || Error transmitResult: " + transmitResult);
            return null;
        }
    }

    public void closedevice() {
        jni.setSmartCardPowerOff(nCardHandle);
        int result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardPowerOff);
        if (result >= 0) {
            jni.setSmartCardClose(nCardHandle);
            jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardClose);
            jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTerminate);
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
}
