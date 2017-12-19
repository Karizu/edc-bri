package com.wizarpos.function.contactless;

import android.app.Activity;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.ContactlessEvent;
import com.wizarpos.apidemo.jniinterface.ContactlessInterface;
import com.wizarpos.drivertest.util.StringUtil;

import java.util.Map;

public class ContactlessController {
    private static final String APP_TAG = "contactless demo";


    private static String testSuccessLog;

    private static String searchtarget;
    private Activity host;

    public static int[] hasMoreCards = new int[1];
    public static int[] cardType = new int[1];

    private static ContactlessController instance;


    /**
     * é¦–æ¬¡ä½¿ç”¨çš„æ—¶å€™è°ƒç”¨è¿™ä¸ªgetInstance(Map<String, Object> param, IActionCallback
     * callback)
     *
     * @param param
     * @param callback
     * @return ContactlessControllerå®žä¾‹
     */


    /**
     * å…³é—­é�žæŽ¥å�¡æŽ¥å�£çš„æ—¶å€™è°ƒç”¨
     *
     * @return ContactlessControllerå®žä¾‹
     */
    public static ContactlessController getInstance() {
        return instance;
    }

    private void setActivity(Map<String, Object> param) {
        Activity context = (Activity) param.get("host");
        this.host = context;
    }


    public boolean searchBegin() {
        Log.d(APP_TAG, "å¼€å§‹ä½¿ç”¨é�žæŽ¥å�¡");

        boolean isSuccess = false;
        /**
         * å¯¹é�žæŽ¥å�¡å� ç”¨
         * */
        int result = ContactlessInterface.Open();
        Log.d(APP_TAG, "open result = " + result);
        Log.e(APP_TAG, "å¯¹é�žæŽ¥è®¾å¤‡å� ç”¨" + (result != 0 ? "æˆ�åŠŸ" : "å¤±è´¥"));
        if (result == 0) {

            return false;
        }
        // é�žæŽ¥è®¾å¤‡å� ç”¨æˆ�åŠŸ

        /**
         * åœ¨å� ç”¨é�žæŽ¥å�¡è®¾å¤‡ä¸”å·²ç»�æ‰“å¼€é�žæŽ¥å�¡å°„é¢‘çš„æƒ…å†µä¸‹è¿›è¡Œå¯»å�¡ ï¼Œå½“å‰�æ˜¯å…¨é¢‘çŽ‡å¯»å�¡ï¼šCONTACTLESS_CARD_MODE_AUTOã€‚
         * public static int CONTACTLESS_CARD_MODE_AUTO = 0ï¼› å…¨é¢‘çŽ‡çš„è‡ªåŠ¨å¯»æ‰¾å�¡ç‰‡ã€‚ public
         * static int CONTACTLESS_CARD_MODE_TYPE_A = 1; å�ªå¯»æ‰¾TYPE_Aç±»åž‹çš„å�¡ç‰‡ã€‚ public
         * static int CONTACTLESS_CARD_MODE_TYPE_B = 2; å�ªå¯»æ‰¾TYPE_Aç±»åž‹çš„å�¡ç‰‡ã€‚ public
         * static int CONTACTLESS_CARD_MODE_TYPE_C = 3; å�ªå¯»æ‰¾TYPE_Aç±»åž‹çš„å�¡ç‰‡ã€‚
         * */
        result = ContactlessInterface.SearchTargetBegin(ContactlessInterface.CONTACTLESS_CARD_MODE_AUTO, 1, -1);
        Log.d(APP_TAG, "searchBegin result = " + result);
        isSuccess = (result >= 0);
        Log.e(APP_TAG, "å‡†å¤‡å¯»å�¡" + (isSuccess ? "æˆ�åŠŸ" : "å¤±è´¥"));

        /**
         * å�¯åŠ¨ä¸€ä¸ªçº¿ç¨‹ä½œä¸ºå¯»åˆ°å�¡äº‹ä»¶çš„ç›‘å�¬å™¨ã€‚ Touch Listener Thread
         * */
        if (isSuccess) {
            TouchListenerThread th = new TouchListenerThread();
            isExitThreadFlag = false;
            th.start();
        }
        return isSuccess;
    }

    private static boolean isExitThreadFlag = true;

    /**
     * ç›‘å�¬é�žæŽ¥å�¡æŽ¥è§¦äº‹ä»¶ï¼Œå¹¶è¿”å›žç»™æ“�ä½œè€…ã€‚ å¯¹æ‰€æœ‰å¯¹å�¡çš„æ“�ä½œéƒ½æ˜¯åœ¨è¿™é‡Œé�¢å¼€å§‹å�šçš„ã€‚
     */
    private class TouchListenerThread extends Thread {
        public void run() {
            while (!isExitThreadFlag) {
                int result = -1;
                ContactlessEvent event = new ContactlessEvent();
                result = ContactlessInterface.PollEvent(-1, event); // å�¯åŠ¨ä¸‹ä¸€æ¬¡ç›‘å�¬
                Log.i(APP_TAG, "poll event result = " + result);
                if (result >= 0) {
                    // æ£€æµ‹åˆ°å�¡ç‰‡æ”¾ç½®åˆ°é�žæŽ¥åˆ·å�¡å™¨ä¸Šã€‚å¼€å§‹å¯¹å�¡è¿›è¡Œæ ‡å‡†æ“�ä½œã€‚
                    result = ContactlessInterface.queryInfo(hasMoreCards, cardType);
                    Log.i(APP_TAG, "queryInfo result = " + result + "\thasMoreCards = " + hasMoreCards[0] + "\tcardType = " + cardType[0]);
                    if (result >= 0) {
                        notifyEvent(event, result);
                    } else {
                        // æ—§è®¾å¤‡,æ— æ³•åŒºåˆ†å�¡ç‰‡ç±»åž‹
                        notifyEvent(event, result);
                    }

                } else {
                    Log.i(APP_TAG, "poll event error ! result = " + result);
                }
                Log.i("APP", "poll event....\n");
            }
        }
    }

    // public int queryInfo() {
    // // int[] hasMoreCards = new int[1];
    // // int[] cardType = new int[1];
    // int result =
    // ContactlessInterface.queryInfo(ContactlessCardHandle2.hasMoreCards,
    // ContactlessCardHandle2.cardType);
    // Log.e(APP_TAG, "queryInfo result is " + result);
    // Log.e(APP_TAG, "" + ContactlessCardHandle2.hasMoreCards[0] + ", " +
    // ContactlessCardHandle2.cardType[0]);
    // return result;
    // }

    public boolean verify(byte[] data) {
        // ContactlessInterface.Open();
        boolean isSuccess = false;
        byte[] bytes = new byte[6];
        if (data == null) {
            bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        } else {
            bytes = data;
        }

        int result = ContactlessInterface.VerifyPinMemory(0, 0, bytes, bytes.length);
        Log.d(APP_TAG, "virify result =" + result + ", bytes.length = " + bytes.length);
        if (result >= 0) {
            isSuccess = true;
        }
        return isSuccess;
    }

    public String read(int sectorIndex, int blockIndex) {
        byte[] pDataBuffer = new byte[16];
        int result = ContactlessInterface.ReadMemory(sectorIndex, blockIndex, pDataBuffer, pDataBuffer.length);
        String temp = null;
        if (result > 0) {
            for (byte b : pDataBuffer) {
                temp = temp + " " + b;
            }
            temp = getFormatString(pDataBuffer);
        }
        Log.d(APP_TAG, "ReadMemory " + temp + ", result = " + result);
        return temp;
    }

    private String getFormatString(byte[] bytes) {
        String value = "";
        for (byte b : bytes) {
            value += String.format("%02X ", b);
        }
        return value;
    }

    public static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1) + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    public static boolean hasRead = false;

    private void notifyEvent(ContactlessEvent event, int queryInfoResult) {
        // åˆ¤æ–­æ˜¯å�¦å·²ç»�æ˜¯è¿‡æ—¶çš„æ•°æ�®ã€‚
        if (isExitThreadFlag) {
            Log.e(APP_TAG, "å·²ç»�é€€å‡ºç›‘å�¬å™¨ï¼Œevent " + event);
            return;
        }

        int nEventID = event.nEventID;
        int nEventDataLength = event.nEventDataLength;
        byte[] arryEventData = event.arryEventData;
        if (nEventID == 0 && nEventDataLength > 0) {
            String strDisplay = new String();
            for (int i = 0; i < nEventDataLength; i++)
                strDisplay += String.format("%02X ", arryEventData[i]);

        }

        if (event.nEventID == 3) {
            return;
        }
//		if (queryInfoResult >= 0) {
//			if (cardType[0] == 1 || cardType[0] == 2 || cardType[0] == 3) {
//				readMifareCard();
//			} else {
//				attachCPUCard();
//			}
//		} else {
//			Log.e(APP_TAG, "ç¬¬5ä¸ªbit = " + byteToBit(arryEventData[0]).toCharArray()[2] + "éªŒè¯�ç»“æžœ: " + (byteToBit(arryEventData[0]).toCharArray()[2] =='1'));
//			if (byteToBit(arryEventData[0]).toCharArray()[2] == '1') {
//				attachCPUCard(); // CPUå�¡
//			} else {
//				readMifareCard();
//			}
//		}

        if (isExitThreadFlag) {
            return;
        }

        int result = ContactlessInterface.SearchTargetEnd();
        Log.e(APP_TAG, "å�–æ¶ˆé�žæŽ¥è®¾å¤‡å¯¹é�žæŽ¥å�¡çš„æ�œå¯»" + (result >= 0 ? "æˆ�åŠŸ" : "å¤±è´¥"));

        byte[] arryData = new byte[1];


    }

    private void readMifareCard() {
        if (verify(null)) {
            Log.i(APP_TAG, "verify pin success");
            String readMsg = read(0, 0);
            if (readMsg != null) {
                Log.i(APP_TAG, "MiFare one card read Success ! got date is : " + readMsg);
            } else {
                Log.i(APP_TAG, "MiFare one card read Failed !");
            }
        } else {
            Log.e("virify pin Failed", "virify pin Failed");
        }
    }

    private void attachCPUCard() {
        String attatchMsg = attatch();
        if (attatchMsg == null) {
            Log.i(APP_TAG, "response Attach = null , may be this card can't support !");
        } else {
            Log.i(APP_TAG, "response Attach = " + attatchMsg);
        }

        String reApdu = transmit(null);
        if (reApdu == null) {
            Log.i(APP_TAG, "response APDU = null , may be this card can't support !");
        } else {
            Log.i(APP_TAG, "response APDU = " + reApdu);
        }

        boolean isSuccess = dettatch();
        Log.i(APP_TAG, "dettatch result = " + isSuccess);
        if (isSuccess) {
            Log.i(APP_TAG, "response Dettatch Success!");
        } else {
            Log.i(APP_TAG, "response Dettatch Failed!");
        }
    }

    /**
     * å¯¹é�žæŽ¥å�¡è¿›è¡Œtouch
     */
    private String attatch() {
        String reValue = null;
        byte arryATR[] = new byte[255];
        int nResult = ContactlessInterface.AttachTarget(arryATR);
        Log.i("ASD", "" + nResult);
        if (nResult > 0) {
            reValue = StringUtil.getFormatString(arryATR, nResult);
            Log.i(APP_TAG, "attatch " + reValue);
        } else {
            reValue = null;
            Log.i(APP_TAG, String.format("AttachTarget return value = %d\n", nResult));
        }
        return reValue;
    }


    /**
     * byte arryAPDU_selectAP[] = new byte[]{0x00, (byte)0xA4, 0x04, 0x00, 0x0E,
     * 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46,
     * 0x30, 0x31}; byte arryAPDU_GPO[] = new byte[]{ (byte)0x80, (byte)0xA8,
     * 0x00, 0x00, 0x23, (byte)0x83, 0x21, 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00,
     * 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x56,
     * 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x56, 0x19, 0x70, 0x03, 0x00,
     * (byte)0xEF, 0x39, (byte)0xAD, (byte)0xCE};
     */
    private String transmit(byte[] apdu) {
        String res = null;
        byte[] apduCommand = null;
        byte[] apduResponse = new byte[255];
        if (apdu != null) {
            apduCommand = apdu;
        } else {
            apduCommand = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, (byte) 0x08};// this
            // is
            // a
            // get
            // random
            // aptu
            // command
            // .
            // but
            // some
            // card
            // maybe
            // can't
            // support.
        }

        int result = ContactlessInterface.Transmit(apduCommand, apduCommand.length, apduResponse);
        if (result < 0) {
            res = null;
        } else {
            res = StringUtil.getFormatString(apduResponse, result);
        }

        return res;
    }

    /**
     *
     * */
    private boolean dettatch() {
        int result = ContactlessInterface.DetachTarget();
        return result >= 0;
    }

    public boolean searchEnd() {
        /**
         * é€€å‡ºç›‘å�¬å™¨
         * */
        isExitThreadFlag = true;
        Log.e(APP_TAG, "é€€å‡ºé�žæŽ¥è®¾å¤‡çš„äº‹ä»¶ç›‘å�¬çº¿ç¨‹");
        /**
         * ç»“æ�Ÿå¯»å�¡
         * */
        int result = ContactlessInterface.SearchTargetEnd();
        Log.e(APP_TAG, "å�–æ¶ˆé�žæŽ¥è®¾å¤‡å¯¹é�žæŽ¥å�¡çš„æ�œå¯»" + (result >= 0 ? "æˆ�åŠŸ" : "å¤±è´¥"));
        /**
         * é‡Šæ”¾å¯¹é�žæŽ¥å�¡è®¾å¤‡çš„å� ç”¨ã€‚
         * */
        result = ContactlessInterface.Close();
        Log.e(APP_TAG, "å�–æ¶ˆå¯¹é�žæŽ¥è®¾å¤‡çš„å� ç”¨" + (result >= 0 ? "æˆ�åŠŸ" : "å¤±è´¥"));
        return result >= 0;
    }

    /**
     * MiFare å�¡çš„è°ƒç”¨æ–¹æ³•
     *
     * */
    // /**
    // * ä½¿ç”¨é»˜è®¤å¯†ç �å¯¹MiFare è¿›è¡Œç¡®è®¤ã€‚
    // * */
    // private boolean virifyMifareCard(byte [] data){
    // boolean isSuccess = false;
    // byte[] bytes =
    // {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
    // /*"FFFFFF".getBytes();*/
    // int result = ContactlessInterface.VerifyPinMemory(0, 0, bytes,
    // bytes.length);
    // Log.d(APP_TAG, "virify result ="+ result +", bytes.length = " +
    // bytes.length);
    // if(result >= 0){
    // isSuccess = true;
    // }
    // return isSuccess;
    // }
    //
    // /**
    // * å¯¹MiFareè¿›è¡Œè¯»æ“�ä½œã€‚
    // * @param sectorIndex MiFareå�¡çš„æŸ�ä¸€å�—æ‰‡åŒºã€‚
    // * @param blockIndex MiFareå�¡çš„æŸ�ä¸€å¿«ã€‚
    // * */
    // private String read(int sectorIndex, int blockIndex){
    // byte[] pDataBuffer = new byte [16];
    // int result = ContactlessInterface.ReadMemory(sectorIndex, blockIndex,
    // pDataBuffer, pDataBuffer.length);
    // String temp = null;
    // if(result > 0){
    // temp = StringUtil.getFormatString( pDataBuffer);
    // }
    // Log.d(APP_TAG, "ReadMemory " + temp +", result = " + result);
    // return temp;
    // }
    // /**
    // * å¯¹MiFareè¿›è¡Œå†™æ“�ä½œã€‚
    // * @param sectorIndex MiFareå�¡çš„æŸ�ä¸€å�—æ‰‡åŒºã€‚
    // * @param blockIndex MiFareå�¡çš„æŸ�ä¸€å¿«ã€‚
    // * @param str å¾€MiFareçš„æŒ‡å®šåŒºåŸŸå¼€å§‹å†™æ•°æ�®ã€‚
    // * */
    // public boolean write(int sectorIndex, int blockIndex , byte [] str){
    // boolean isSuccess = false;
    // byte [] bytes = str;
    // int result = ContactlessInterface.WriteMemory(sectorIndex, blockIndex,
    // bytes, bytes.length);
    // Log.d(APP_TAG, "write result ="+ result);
    // if(result >= 0){
    // isSuccess = true;
    // }
    // return isSuccess;
    // }
}
