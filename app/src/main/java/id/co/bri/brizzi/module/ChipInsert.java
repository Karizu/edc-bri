package id.co.bri.brizzi.module;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

//import vpos.apipackage.APDU_RESP;
//import vpos.apipackage.APDU_SEND;
//import vpos.apipackage.Icc;
//import vpos.util.ByteUtil;

/**
 * Created by indra on 24/11/15.
 */
public class ChipInsert extends com.rey.material.widget.EditText {
    final static int MSG_WHAT_ICC = 1;
    final int SINGLE = 0;
    final int CIRCLE = 1;
    private final String tag = "IccActivity";
    private final byte[] SELECT_APPLET_APDU_COMMAND = new byte[]
            {
                    (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
                    (byte) 0x0D, (byte) 0xD4, (byte) 0x99, (byte) 0x00,
                    (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                    (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x01
            };
    private final byte[] SELECT_CPR_DF_APDU_COMMAND = new byte[]
            {
                    (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C,
                    (byte) 0x02, (byte) 0x01, (byte) 0x01
            };
    private final byte[] SELECT_AND_READ = new byte[]
            {
                    (byte) 0x00, (byte) 0xB0, (byte) 0x82, (byte) 0x00,
                    (byte) 0x00
            };
    private final byte[] SELECT_AND_READ_DO_NOT_WORK = new byte[]
            {
                    (byte) 0x00, (byte) 0xB0, (byte) 0x81, (byte) 0x00,
                    (byte) 0x00
            };
//    private ICCThread iccThread = new ICCThread();
    public byte dataIn[] = new byte[512];
    public byte ATR[] = new byte[40];
    public byte vcc_mode = 1;
    int iccSuccessCount;
    int iccErrorCount;
    //	boolean isSleep = false;
    int iccNoDataCount;
    boolean isOut = false;
    private boolean isIccChecked;
    private int ret;
    private boolean m_bThreadFinished = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String strInfo = b.getString("MSG");
            int type = b.getInt("type");
            switch (type){
                case 0:
                    setText(strInfo + "\nsuccess " + iccSuccessCount + "  error " + iccErrorCount + "  cmdFail " + iccNoDataCount);
                    break;
                case 1:
                    setHint(strInfo);
                    break;
                case 2:
                    setError(strInfo);
            }
            Log.d("Icc", strInfo);
        }
    };

    public ChipInsert(Context context) {
        super(context);
    }

    public ChipInsert(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    private class ICCThread implements Runnable {
//
//        public boolean isThreadFinished() {
//            return m_bThreadFinished;
//        }
//        @Override
//        public void run() {
//            m_bThreadFinished = false;
//            byte slot = (byte) 0;
//            int what = MSG_WHAT_ICC;
//            ret = 1;
//            while (ret != 0 && !isOut) {
//                ret = Icc.Lib_IccCheck(slot);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                setTextHint("Insert card, please!");
//                SendMsg("Insert card, please!",1, MSG_WHAT_ICC);
//            }
//
//            ret = Icc.Lib_IccOpen(slot, vcc_mode, ATR);
//            if (ret != 0) {
////                errorCount(slot);
////                setTextError("Lib_IccOpen() fail!");
//                SendMsg("Lib_IccOpen() fail!",2, what);
//                Log.e(tag, "Lib_IccOpen failed!");
//                m_bThreadFinished = true;
//
//                return;
//            }
//
//            byte cmd[] = new byte[4];
//            cmd[0] = 0x00;            //0-3 cmd
//            cmd[1] = (byte) 0xa4;
//            cmd[2] = 0x04;
//            cmd[3] = 0x00;
//            short lc = 0x0e;
//            short le = 256;
//
//            String sendmsg = "1PAY.SYS.DDF01";
//            dataIn = sendmsg.getBytes();
//
//            APDU_SEND ApduSend = new APDU_SEND(cmd, lc, dataIn, le);
//            APDU_RESP ApduResp = null;
//            byte[] resp = new byte[516];
//
//            if (0 == ret) {
////                successCount(slot);
//                String strInfo = "";
//                ApduResp = new APDU_RESP(resp);
//
////                Log.i("RESPONSE",new String(ApduResp.getDataOut()));
//                strInfo = ByteUtil.bytearrayToHexString(ApduResp.DataOut, ApduResp.LenOut) + "SWA:"
//                        + ByteUtil.byteToHexString(ApduResp.SWA) + " SWB:" + ByteUtil.byteToHexString(ApduResp.SWB);
//                SendMsg(strInfo,0, what);
//            } else {
////                noDataCount(slot);
////                setTextError("Lib_IccCommand() fail!");
//                SendMsg("Lib_IccCommand() fail!",2, what);
//                Log.e(tag, "Icc_Command failed!");
//            }
//
//            Icc.Lib_IccClose(slot);
//
//            SleepMs(300);
//        }
//    }

    public ChipInsert(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChipInsert(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init() {

        iccSuccessCount = 0;
        iccErrorCount = 0;
        iccNoDataCount = 0;
        Log.w(tag, "Leave onStart");

//        if (null != iccThread && !iccThread.isThreadFinished()) {
//            Log.e("onClickTest", "return return");
//            return;
//        }
        Log.w(tag, "Start");
//        Thread t1 = new Thread(iccThread);
//        t1.start();
//        getContext()
    }

    public void SendMsg(String strInfo, int type, int what) {

        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        b.putInt("type", type);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private void SleepMs(int Ms) {
        try {
            Thread.sleep(Ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
