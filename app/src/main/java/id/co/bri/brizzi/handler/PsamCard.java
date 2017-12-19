package id.co.bri.brizzi.handler;

import android.app.Activity;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.SmartCardSlotInfo;
import com.wizarpos.drivertest.util.StringUtil;
import com.wizarpos.function.smr.SmartCardMagicConvert;

import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.module.ContactlessQ1;

public class PsamCard extends ContactlessQ1 {
    private String TAG = "BRIZZI";
    private static int nCardHandle = -1;
    private Activity host;
    //protected static CardData cData ;
    private static SmartCardMagicConvert jni = SmartCardMagicConvert.getSingleTon();

    public synchronized boolean starting(int index) {
//        Log.d(TAG, "sam start");
        int result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardInit);
//        Log.d(TAG, "init result " + index + " =" + result);
        jni.setSmartCardOpen(index);
        result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardOpen);
        if (result >= 0) {
//            Log.d(TAG, "Init SAMCARD = " + result);
        } else {
//            Log.d(TAG, "init SAMCARD = " + result);

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
        if (nResult >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult).replace(" ", "");
//            Log.d(TAG, "Cmd :" + command + " || sResult : " + transmitResult + " | " + transmitResult.length());
//            Log.d(TAG, "depan:" + transmitResult.substring(0, 2) + "|" + transmitResult.length());
            if (transmitResult.length() == 4 && transmitResult.substring(0, 2).equals("61")) {
                transmitResult = sendReqResponse(transmitResult);
            }

            return transmitResult;
        } else {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);
//            Log.d(TAG, "Cmd :" + command + " || Error transmitResult: " + transmitResult);
            return null;
        }
    }

    public String sendReqResponse(String apduCommand) {
//        Log.d(TAG, "kirim sam query : 00C00000" + apduCommand.substring(2, 4));
        byte[] byteArrayAPDU = hexStringToByteArray("00C00000" + apduCommand.substring(2, 4));
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[129];
        jni.setSmartCardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        int nResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTransmit);
        String transmitResult = "";
        String command = StringLib.toHexString(byteArrayAPDU, 0, byteArrayAPDU.length, false);
        if (nResult >= 0) {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult).replace(" ", "");
//            Log.d(TAG, "Cmd :" + command + " || sResult : " + transmitResult + " | " + transmitResult.substring(0, 1));
            return transmitResult;
        } else {
            transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);
//            Log.d(TAG, "Cmd :" + command + " || Error transmitResult: " + transmitResult);
            return null;
        }
    }

    public static void closedevice() {
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
