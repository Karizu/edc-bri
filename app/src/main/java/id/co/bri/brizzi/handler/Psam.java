package id.co.bri.brizzi.handler;

import android.app.Activity;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.SmartCardSlotInfo;
import com.wizarpos.drivertest.util.ByteConvert;
import com.wizarpos.drivertest.util.StringUtil;
import com.wizarpos.function.smr.SmartCardMagicConvert;

import id.co.bri.brizzi.common.StringLib;


public class Psam {
    private String TAG = "aotTag";
    private static int nCardHandle = -1;
    private Activity host;
    private static SmartCardMagicConvert jni = SmartCardMagicConvert.getSingleTon();

    public synchronized boolean testPsamcard(int index) {
        int result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardInit);
        Log.d(TAG, "init result " + index + " =" + result);
        jni.setSmartCardOpen(index);
        result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardOpen);
        if (result >= 0) {
            Log.d(TAG, "init result = " + result);
        } else {
            Log.d(TAG, "init result = " + result);
        }

        Log.d(TAG, "open result/nCardHandle = " + result);
        if (result < 0) {
            return false;
        } else {

            nCardHandle = result;
        }
        notifyEventPsam();
        jni.setSmartCardClose(nCardHandle);
        jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardClose);
        closedevice();
        return true;
    }

    private String str1;

    private void notifyEventPsam() {
        byte[] byteArrayATR = new byte[64];
        SmartCardSlotInfo mSlotInfo = new SmartCardSlotInfo();
        jni.setSmartCardPowerOn(nCardHandle, byteArrayATR, mSlotInfo);
        int result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardPowerOn);
        if (result >= 0) {
            String powerOnResult = StringUtil.getFormatString(byteArrayATR, result);
            Log.d(TAG, "PowerOnSuccess:" + powerOnResult);


            //boolean isSuccess = sendCmdForGetRandompasm();

            // isSuccess =sendCmd(hexStringToByteArray("00A4040C09A00000000000000011"),1);
            Log.d(TAG, "send 00A4 040C 09A0 0000 0000 0000 0011");
            boolean isSuccess = sendCmd(hexStringToByteArray("00A4040C09A00000000000000011"));
            Log.d(TAG, "send 80B0000020601350060149670734054B8A472880FF00000300800000006160FB32D2F6EAF1");
//			if (isSuccess)
//				 isSuccess =sendCmd(hexStringToByteArray("80B00000206013500601496707 34054B8A472880 FF0000030080000000 6160FB32D2F6EAF1"));
//			if (isSuccess)
//				 isSuccess =sendCmd(hexStringToByteArray("00C0000020"));
            //Log.d(TAG,"00A4040C09A00000000000000011");
            //boolean isSuccess =sendCmd(new byte[]{(byte)0x80, (byte)0xB0, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            //boolean isSuccess =sendCmd(new byte[]{0x00, (byte) 0xA4, 0x04, 0x0C, 0x09, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11});
            if (isSuccess)
                Log.d(TAG, "sam kses");
            // if (isSuccess) {
            jni.setSmartCardPowerOff(nCardHandle);
            result = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardPowerOff);
            if (result >= 0) {

                Log.d(TAG, "SmartCardPowerOff sukses");
            } else {

                Log.d(TAG, "powerOffFailure");
            }
            // }
        } else {

            Log.d(TAG, "powerOffFailure");
        }
    }

    public int sendCmdForGetRandompasm() {

        Log.d(TAG, "sendrandom");


        //byte[] byteArrayAPDU = new byte[] { (byte) 0x80, (byte) 0xB5, 0x00, 0x00,(byte) 0x02 };
        byte[] byteArrayAPDU = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x08};
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[32];
        jni.setSmartCardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        int nResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTransmit);
        if (nResult > 0) {
            String transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);

            Log.d(TAG, "sendRandomSuccess : " + transmitResult);
            return 1;
        } else {

            Log.d(TAG, "sendRandomFailure : " + ByteConvert.buf2String("", ByteConvert.int2byte2(-nResult)));
            return 0;
        }

    }

    public boolean sendCmd(byte[] byteArrayAPDU) {
        int nAPDULength = byteArrayAPDU.length;
        byte[] byteArrayResponse = new byte[129];
        jni.setSmartCardTransmit(nCardHandle, byteArrayAPDU, nAPDULength, byteArrayResponse);
        int nResult = jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTransmit);
        if (nResult >= 0) {
            String transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);
            String hasil = StringLib.toHexString(byteArrayResponse, 0, byteArrayResponse.length, false);
            Log.d(TAG, "sendRandomSuccess : " + transmitResult);
            return true;
        } else {
            String transmitResult = StringUtil.getFormatString(byteArrayResponse, nResult);
            Log.d(TAG, "error  transmitResult: " + transmitResult);
            Log.d(TAG, "sendRandomFailure : " + ByteConvert.buf2String("", ByteConvert.int2byte2(-nResult)));
            return false;
        }
    }

    public static void closedevice() {
        jni.invokeJNIMethod(SmartCardMagicConvert.SmartCardTerminate);
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

interface mCallback {
    void onFinish();
}
