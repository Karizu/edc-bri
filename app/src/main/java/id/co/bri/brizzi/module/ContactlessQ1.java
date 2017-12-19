package id.co.bri.brizzi.module;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.ContactlessEvent;
import com.wizarpos.apidemo.jniinterface.ContactlessInterface;
import com.wizarpos.apidemo.util.StringUtil;

/**
 * Created by indra on 13/01/16.
 */
public class ContactlessQ1 implements ContactlessConstant {
    protected static Handler myHandler;
    boolean isExitThreadFlag;
    protected static CardData cData = new CardData();
    public static int[] hasMoreCards = new int[1];
    public static int[] cardType = new int[1];
    protected final String TAG ="BRIZZI";

    public void searchBegin() {
        boolean isSuccess = false;
        int result = ContactlessInterface.Open();
//        Log.d("BRIZZI", "open result = " + result);

        if (true) {
            result = ContactlessInterface.SearchTargetBegin(ContactlessInterface.CONTACTLESS_CARD_MODE_AUTO, 1, 1000);
//            Log.d("BRIZZI", "searchBegin result = " + result);
            isSuccess = (result >= 0);
            if(isSuccess){
                TouchListenerThread th = new TouchListenerThread();
                isExitThreadFlag = false;
                th.start();
            }
        }
    }

    private class TouchListenerThread extends Thread {
        public void run() {
            while (!isExitThreadFlag) {
                int result = -1;
                ContactlessEvent event = new ContactlessEvent();
                result = ContactlessInterface.PollEvent(-1, event);
                Log.i("BRIZZI", "poll event result = " + result);
                if (result >= 0) {
                    result = ContactlessInterface.queryInfo(hasMoreCards, cardType);
                    Log.i("BRIZZI", "queryInfo result = " + result + " | thasMoreCards = " + hasMoreCards[0] + " | tcardType = " + cardType[0]);
                    if (result >= 0) {
                        notifyEvent(event, result);
                    } else {
                        notifyEvent(event, result);
                    }

                } else {
                    Log.i("BRIZZI", "poll event error ! result = " + result);
                }
                Log.i("BRIZZI", "poll event....\n");
            }
            //searchBegin(); --> setelah proses jika ingin nge-tap lagi
        }

        public void notifyEvent(ContactlessEvent event, int queryInfoResult) {

            if (isExitThreadFlag) {
                Log.e("BRIZZI", "event" + event);
                return;
            }
            int nEventID = event.nEventID;
            int nEventDataLength = event.nEventDataLength;
            byte[] arryEventData = event.arryEventData;
            if (nEventID == 0 && nEventDataLength > 0) {
                String uid = "";
                for (int i = 3; i < nEventDataLength; i++)
                    uid += String.format("%02X ", arryEventData[i]);
                Log.d("BRIZZI", "Event Data = " + uid);
                cData.setUid(uid.replace(" ", ""));
            }
            if (event.nEventID == 3) {
                Log.d("BRIZZI", "c-3");
                return;
            }
            if (queryInfoResult >= 0) {
                if (cardType[0] == 1 || cardType[0] == 2 || cardType[0] == 3) {
                    //readMifareCard();
                } else {
                    notifyHandler(CARD_TAP_NOTIFIER);
                }
            } else {
                if (byteToBit(arryEventData[0]).toCharArray()[2] == '1') {
                    notifyHandler(CARD_TAP_NOTIFIER);
                }
            }
        }
    }


    protected void notifyHandler(int val) {
        Message msg = new Message();
        msg.what = val;
        myHandler.sendMessage(msg);
    }

    protected String transmitCmd(String apdu) {
        String res = "";
        byte[] apduCommand = hexStringToByteArray(apdu);
        byte[] apduResponse = new byte[255];
        int result = ContactlessInterface.Transmit(apduCommand, apduCommand.length, apduResponse);
        if (result < 0) {
            res = "";
        } else {
            res = StringUtil.getFormatString(apduResponse, result);
        }
        return res.replace(" ", "");
    }

    protected boolean dettatch() {
        int result = ContactlessInterface.DetachTarget();
        endSearch();
        return result >= 0;
    }

    protected String attatch() {
        String reValue = null;
        byte arryATR[] = new byte[255];
        int nResult = ContactlessInterface.AttachTarget(arryATR);
        Log.i("ASD", "" + nResult);
        if (nResult > 0) {
            reValue = StringUtil.getFormatString(arryATR, nResult);
            Log.i("BRIZZI", "attatch " + reValue);
        } else {
            reValue = null;
            Log.i("BRIZZI", String.format("AttachTarget return value = %d\n", nResult));
        }
        return reValue;
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

    public static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    public static void endSearch() {
        ContactlessInterface.SearchTargetEnd();
        byte[] arryData = new byte[1];
        arryData[0] = 0x00;
        ContactlessInterface.SendControlCommand(ContactlessInterface.RC500_COMMON_CMD_RF_CONTROL, arryData, 1);
        arryData[0] = 0x01;
        ContactlessInterface.SendControlCommand(ContactlessInterface.RC500_COMMON_CMD_RF_CONTROL, arryData, 1);
        ContactlessInterface.SearchTargetBegin(ContactlessInterface.CONTACTLESS_CARD_MODE_AUTO, 1, -1);
        ContactlessInterface.Close();
    }


}

interface ContactlessConstant {

    int SAM_READY_NOTIFIER = 1;
    int CARD_TAP_NOTIFIER = 2;
    int HOST_REPLY_NOTIFIER = 3;
    int CARD_RESPONSE_ERROR = 4;
    int CARD_RESPONSE_FINISH = 5;
    int SAM_NOT_READY = 6;
}