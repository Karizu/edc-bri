package id.co.bri.brizzi;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wizarpos.apidemo.util.StringUtility;
import com.wizarpos.jni.PINPadInterface;
import com.wizarpos.jni.PinPadCallbackHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.bri.brizzi.common.CommonConfig;

public class InputPinService extends Service {
    final private String TAG = "PPService";
    private class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonConfig.UPDATE_FLAG_RECEIVER:
                    flagReceiver = msg.replyTo;
                    break;
                case CommonConfig.CAPTURE_PINBLOCK:
                    doCapture(msg);
                    break;
                case CommonConfig.CAPTURE_CANCEL:
                    doCancel();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private final Messenger syncMessenger = new Messenger(new MsgHandler());
    private boolean isOpened = false;
    private String panHolder;
    private String formId;
    private Messenger replyHandler = null;
    private Messenger flagReceiver = null;
    private static InputPinService instance;

    public InputPinService() {
    }

    public static InputPinService getInstance() {
        if (instance==null) {
            instance = new InputPinService();
        }
        return instance;
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return syncMessenger.getBinder();
    }

    private void doCapture(final Message message) {
        if (flagReceiver!=null) {
            Message setFlag = Message.obtain(null, CommonConfig.FLAG_INUSE);
            try {
                flagReceiver.send(setFlag);
            } catch (Exception e) {

            }
        }
        replyHandler = message.replyTo;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                Message redirect = null;
                switch (m.what) {
                    case CommonConfig.CALLBACK_KEYPRESSED:
                        redirect = Message.obtain(null, CommonConfig.CALLBACK_KEYPRESSED);
                        redirect.setData(m.getData());
                        try {
                            replyHandler.send(redirect);
                            Log.i(TAG, "Send Callback");
                        } catch (Exception e) {
                            Log.e("REPLY", "Failed : " + e.getMessage());
                        }
                        break;
                    case CommonConfig.CALLBACK_RESULT:
                        redirect = Message.obtain(null, CommonConfig.CALLBACK_KEYPRESSED);
                        redirect.setData(m.getData());
                        try {
                            replyHandler.send(redirect);
                            if (flagReceiver != null) {
                                Message setFlag = Message.obtain(null, CommonConfig.FLAG_READY);
                                flagReceiver.send(setFlag);
                            }
                            Log.i(TAG, "Send Result");
                        } catch (Exception e) {
                            Log.e("REPLY", "Failed : " + e.getMessage());
                        }
                        close();
                        break;
                    default:
                        super.handleMessage(m);
                }
          }
        };
        Bundle b = message.getData();
        panHolder = b.getString("pan");
        formId = b.getString("formid");
        Handler process = new Handler();
        process.post(new ReadPINThread(handler));
    }

    class ReadPINThread implements Runnable {
        private final Handler handler;
        private Messenger msgHandler = null;

        public ReadPINThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            msgHandler = new Messenger(handler);
            boolean pinpadReady = false;
            int pinpadRetryCounter = 0;
            final StringBuilder pinpad = new StringBuilder();
            while (!pinpadReady&&pinpadRetryCounter<5) {
                close();

                open();
                int result = PINPadInterface.setKey(2, 0, 0, PINPadInterface.ALGORITH_DES);
                Log.e(TAG, "setKey result = " + result);
                if (result < 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {

                    }
                } else {
                    pinpadReady = true;
                }
                pinpadRetryCounter++;
            }
            PINPadInterface.setupCallbackHandler(new PinPadCallbackHandler() {

                @Override
                public void processCallback(byte[] data) {
                    pinpad.append("*");
                    Message m = new Message();
                    m.what = CommonConfig.CALLBACK_KEYPRESSED;
                    Bundle b = new Bundle();
                    b.putString("key", pinpad.toString());
                    b.putByteArray("data", data);
                    m.setData(b);
                    try {
                        msgHandler.send(m);
                        Log.i(TAG, "send callback");
                    } catch (Exception e) {

                    }
                }
            });
            String pan = "123456789012345678";
            if (!panHolder.equals("")) {
                pan = panHolder;
            }
            if (formId.equals("7100000")||formId.equals("7300000")) {
                pan = "9999999999999999";
            }
            int ret = PINPadInterface.setPinLength(6,0);
            ret = PINPadInterface.setPinLength(6,1);
            new ReadingTask().execute(CommonConfig.MODE_CALCULATE, pan.getBytes(), pan.length(), -2, 0);
        }
    }

    private void open() {
        if (isOpened) {
            Log.e(TAG, "PINPad is opened");
        } else {
            try {
                int result = PINPadInterface.open();
                if (result < 0) {
                    Log.e(TAG, "open() error! Error code = " + result);
                } else {
                    isOpened = true;
                    Log.e(TAG, "open() success!");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        if (isOpened) {
            try {
                int result = PINPadInterface.close();
                if (result < 0) {
                    Log.e(TAG, "close() error! Error code = " + result);
                } else {
                    isOpened = false;
                    Log.e(TAG, "close() success!");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "PINPad is not opened");
        }
    }

    private void doCancel() {
        Message m = Message.obtain(null, CommonConfig.CALLBACK_CANCEL);
        try {
            close();
            replyHandler.send(m);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmmss");
            Log.i(TAG, "send cancel @" + sdf.format(new Date()));
        } catch (Exception e) {

        }
    }

    private void doneCancel() {
        Message m = Message.obtain(null, CommonConfig.CALLBACK_CANCEL_DONE);
        try {
            replyHandler.send(m);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmmss");
            Log.i(TAG, "cancel finished @" + sdf.format(new Date()));
        } catch (Exception e) {

        }
    }

    class ReadingTask extends AsyncTask {
        int mode;

        @Override
        protected Pinblock doInBackground(Object[] params) {
            mode = (int) params[0];
            Pinblock result = new Pinblock(-1, null);
            if (mode==CommonConfig.MODE_CALCULATE) {
                byte[] pinBlock = new byte[8];
                try {
                    int ret = PINPadInterface.inputPIN(
                            (byte[]) params[1],
                            (int) params[2],
                            pinBlock,
                            (int) params[3],
                            (int) params[4]
                    );
                    if (ret>=0) {
                        result = new Pinblock(ret, pinBlock);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Input PIN Failed");
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object result) {
            Pinblock res = (Pinblock) result;
            String pinBlockString = "";
            if (res.getRc()>0) {
                pinBlockString = StringUtility.ByteArrayToString(res.getData(), res.getRc());
                Message m = Message.obtain(null, CommonConfig.CALLBACK_RESULT);
                Bundle b = new Bundle();
                String data = pinBlockString;
                b.putString("data", data);
                m.setData(b);
                close();
                try {
                    replyHandler.send(m);
                    Log.i(TAG, "send reply");
                } catch (Exception e) {

                }
            } else {
                doneCancel();
                Log.i(TAG, "Bad result");
            }
        }
    }

    class Pinblock {
        private int rc;
        private byte[] data;
        Pinblock(int rc, byte[] data) {
            this.rc = rc;
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public int getRc() {
            return rc;
        }

        public void setRc(int rc) {
            this.rc = rc;
        }
    }

}
