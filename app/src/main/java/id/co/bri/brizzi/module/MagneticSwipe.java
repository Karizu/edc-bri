package id.co.bri.brizzi.module;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.wizarpos.apidemo.jniinterface.HALMsrInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.bri.brizzi.module.listener.SwipeListener;


/**
 * Created by indra on 24/11/15.
 */
public class MagneticSwipe extends com.rey.material.widget.EditText {
    public String tag = "MsrActivity";
    private byte track1[] = new byte[250];
    private byte track2[] = new byte[250];
    private byte track3[] = new byte[250];
    private byte msrKey[] = new byte[20];
    //    private Thread threadData = new Thread(new MSRRead());
    private boolean isQuit = false;
    private boolean isOpen = false;
    private int ret = -1;
    private int checkCount = 0;
    private int successCount = 0;
    private int failCount = 0;
    private List<SwipeListener> swipeListeners = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            if (b != null) {
                HashMap data = (HashMap) b.getSerializable("DATA");
                if (data.get("RC").toString().equals("00")) {
                    setText(data.get("track2").toString());
                    isQuit = true;
                    if (closeDriver()) {
                        setHint("Swipe data completed");
                        for(SwipeListener listener : swipeListeners){

                            listener.onSwipeComplete(MagneticSwipe.this, data.get("track2").toString());
                        }
                        swipe(getText().toString());
                        LinearLayout menu = (LinearLayout) getParent();
                        int childCount = menu.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            if (menu.getChildAt(i) instanceof MagneticSwipe) {
                                int nextFocus = i + 1;
                                if (menu.getChildAt(nextFocus) != null) {
                                    View v = menu.getChildAt(nextFocus);
//                                    if (v instanceof EditText) {
//                                        EditText text = (EditText) v;
//                                        String initHint = text.getHint().toString();
//                                        if (text.isNumber()) {
//                                            initHint = initHint + " number";
//                                        }
//                                        text.setHint("Please input " + initHint);
//                                    }
                                    v.requestFocus();
                                }
                            }
                        }

                    }
//                    Log.d("Msr", data + "\n\ncheckCount = " + checkCount + "\nsuccessCount = " + successCount + "\nfailCount = " + failCount);
//                    openDriver();
//                    closeDriver();
                } else {
                    // setHint("Please swipe again");
//                    Log.d("Msr", "Canceled");
//                    openDriver();
                    closeDriver();
                    isQuit = true;
                    isOpen = false;
                }
            }
        }
    };

    public MagneticSwipe(Context context) {
        super(context);
    }
    private void swipe(String string){
        Log.i("INFO_SWIPE","DODOL");

    }
    public MagneticSwipe(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MagneticSwipe(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MagneticSwipe(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean closeDriver() {
        int val=0;
        try {
            val=HALMsrInterface.msr_close();
        } catch (Exception e) {
            val=-1;
        }
        return val >= 0;
    }

    public boolean openDriver() {
        int val=0;
        try {
            val=HALMsrInterface.msr_open();
        } catch (Exception e) {
            val=-1;
        }
        return val >= 0;
    }


    public void addSwipeListener(SwipeListener swipeListener){
        swipeListeners.add(swipeListener);
    }
    public void init() {
        setEnabled(false);
        setKeyListener(null);
        if (!openDriver()) {
            Log.d("MSR DRIVER", "Open Driver failed");
            setError("Open Driver failed!");

        } else {
            setHint("Swipe Card Please!");
            Log.d("MSR DRIVER", "Open Driver succeed!");
            Thread t1 = new Thread(new GetData());
            t1.start();
            isOpen = true;
        }
    }

    private void readTrackData() {
        int ret;
        byte[] byteArry = new byte[255];
        int length = 255;

        ret = HALMsrInterface.msr_get_track_data(1, byteArry, length);
        HashMap<String, String> data = new HashMap<>();
        if (ret > 0) {
            String str = new String(byteArry, 0, ret);
            Message msg = new Message();
            Bundle b = new Bundle();

            data.put("RC", "00");
            data.put("track2", str);
            b.putSerializable("DATA", data);

            msg.setData(b);
            handler.sendMessage(msg);

            Log.i("MSR_DRIVER", new String(byteArry, 0, ret));
        }else{
            Log.i("MSR_DRIVER", "No Input");
            data.put("RC","05");
        }
    }

    public void setIsQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    private class GetData implements Runnable {

        @Override
        public void run() {
            while (true) {
                int nReturn = -1;

                if (isQuit)
                    break;
                nReturn = HALMsrInterface.msr_poll(2000);
                if (nReturn >= 0) {
                    Log.i("MSR_DRIVER", "New data found");
                    try {
                        readTrackData();
                    } catch (Exception e) {
                        //read failed
                    }
                }

            }
        }

    }

}
