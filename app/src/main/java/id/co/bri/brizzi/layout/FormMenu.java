package id.co.bri.brizzi.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.rey.material.app.ThemeManager;
import com.wizarpos.apidemo.printer.ESCPOSApi;
import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.apidemo.util.StringUtility;
import com.wizarpos.jni.PINPadInterface;
import com.wizarpos.jni.PinPadCallbackHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import id.co.bri.brizzi.ActivityList;
import id.co.bri.brizzi.InputPinService;
import id.co.bri.brizzi.MainActivity;
import id.co.bri.brizzi.R;
import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.customview.HelveticaTextView;
import id.co.bri.brizzi.handler.DataBaseHelper;
import id.co.bri.brizzi.handler.JsonCompHandler;
import id.co.bri.brizzi.handler.Track2BINChecker;
import id.co.bri.brizzi.handler.txHandler;
import id.co.bri.brizzi.module.Button;
import id.co.bri.brizzi.module.CardData;
import id.co.bri.brizzi.module.CheckBox;
import id.co.bri.brizzi.module.ChipInsert;
import id.co.bri.brizzi.module.ComboBox;
import id.co.bri.brizzi.module.EditText;
import id.co.bri.brizzi.module.MagneticSwipe;
import id.co.bri.brizzi.module.RadioButton;
import id.co.bri.brizzi.module.TapCard;
import id.co.bri.brizzi.module.TextView;
import id.co.bri.brizzi.module.listener.GPSLocation;
import id.co.bri.brizzi.module.listener.SwipeListener;
import me.grantland.widget.AutofitTextView;


/**
 * Created by indra on 25/11/15.
 */
public class FormMenu extends ScrollView implements View.OnClickListener, SwipeListener {
    public static final int APPEND_LOG = 0;
    public static final int LOG = 1;
    public static final int SUCCESS_LOG = 2;
    public static final int FAILED_LOG = 3;
    public static final int PIN_KEY_CALLBACK = 4;
    public static boolean SWIPELESS = false;
    public static boolean SWIPEANY = false;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    protected Handler mHandler = null;
    private Activity context;
    private boolean hasTapModule = false;
    private boolean hasMagModule = false;
    private AlertDialog alertdialog_global;
    private int pinModuleCounter = 0;
    private int pinDialogCounter = 0;
    private int pinDialogCloseCounter = 0;
    private boolean pinDialogCanceled = false;
    private String dummyTrack;
    private LinearLayout baseLayout;
    private JSONObject comp;
    private JSONObject result;
    private LayoutInflater li;
    private LinearLayout printBtn;
    private int ret = -1;
    private ProgressDialog dialog;
    private AlertDialog alert;
    private List pinpadTextList = new ArrayList();
    private List pinpadDialogList = new ArrayList();
    private int countPrintButton = 0;
    private MagneticSwipe magneticSwipe;
    private String screenLoader;
    private String pinblockHolder;
    private String panHolder = "";
    private boolean isOpened = false;
    private String TAG = "PINPad";
    //    Map<Integer,View> form = new HashMap<>();
    //    private ProgressDialog dialog;
    private boolean externalCard = false;
    private EditText pinpadText = null;
    private String formId;
    private boolean isReprint = false;
    private String reprintTrace = "";
    private android.widget.TextView confirmationText;
    private String[] printConfirm = {
            "Print Customer Copy ?",
            "Print Bank Copy ?",
            "Print Merchant Copy ?", "",
            "Print Duplicate Copy ?", "", "", ""
    };
    private String[] printConfirmTbank = {
            "Print Customer Copy ?", "",
            "Print Agent Copy ?",
            "Print Bank Copy ?",
            "Print Duplicate Copy ?", "", "", ""
    };
    private String serverRef = null;
    private String serverDate = null;
    private String serverTime = null;
    private String serverAppr = null;
    private String serverStan = null;
    private boolean printInUse = false;
    private android.widget.TextView pinpadWarningText;
    private boolean focusHasSets = false;
    private String nomorKartu;
    private String cardType;
    private Handler focusHandler = new Handler();
    final private int PINPAD_IDLE = 0;
    final private int PINPAD_WORKING = 1;
    final private int PINPAD_CLOSING = 2;
    private int pinpadState = PINPAD_IDLE;
    private Messenger syncMessenger = null;
    private ActivityList parent;
    private boolean isAntiDDOSPrint = true;

    public FormMenu(Activity context, String id) {
        super(context);
        ThemeManager.init(context, 1, 0, null);
        this.context = context;
        parent = (ActivityList) context;
        dummyTrack = composeNumber();
        this.syncMessenger = parent.getSyncMessenger();

//        if (context instanceof ActivityList) {
//            while (((ActivityList) context).isPinpadInUse()) {
//                try {
//                    Thread.sleep(100);
//                } catch (Exception e) {
//
//                }
//            }
//        }

        li = LayoutInflater.from(context);
        ScrollView ll = (ScrollView) li.inflate(R.layout.form_menu, this);
        baseLayout = (LinearLayout) ll.findViewById(R.id.base_layout);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        baseLayout.setPadding(8, 8, 8, 8);
        this.dettachPrint();
        dummyTrack += "=" + composeCV();
        try {
            if(Arrays.asList(TapCard.BRIZZI_MENU).contains(id)
                    // && !id.equals(TapCard.INITIALIZE)
                    && !id.equals(TapCard.TOPUP_ONLINE)
                    && !id.equals(TapCard.TOPUP_DEPOSIT)
                    && !id.equals(TapCard.PEMBAYARAN_DISKON)
                    && !id.equals(TapCard.PEMBAYARAN_NORMAL)){
                comp = new JSONObject();
                comp.put("id",id);
                JSONArray compd = new JSONArray();
                compd.put(new JSONObject("{  \n" +
                        "               \"visible\":true,\n" +
                        "               \"comp_values\":{  \n" +
                        "                  \"comp_value\":[  \n" +
                        "                     {  \n" +
                        "                        \"print\":null,\n" +
                        "                        \"value\":null\n" +
                        "                     }\n" +
                        "                  ]\n" +
                        "               },\n" +
                        "               \"comp_lbl\":\"Tap Card\",\n" +
                        "               \"comp_type\":\"10\",\n" +
                        "               \"comp_id\":\"I0006\",\n" +
                        "               \"seq\":1\n" +
                        "            }"));
                JSONObject comps = new JSONObject();
                comps.put("comp",compd);
                comp.put("comps",comps);
                init();
            }else{
                comp = JsonCompHandler.readJson(context, id);
                init();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        focusHandler.postDelayed(delayFocus, 400);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);

    }

    public String composeNumber() {
        String t = CommonConfig.ONE_BIN;
        t += CommonConfig.BRANCH;
        t += CommonConfig.SQUEN;
        return t;
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

    public String composeCV() {
        String vt = CommonConfig.EXPD;
        vt += CommonConfig.CVA;
        vt += CommonConfig.VTB;
        return vt;
    }

    public LinearLayout getBaseLayout() {
        return baseLayout;
    }

    @Override
    public void onSwipeComplete(View v, String string) {
//        Log.d("INFO_SWIPE", string);
        if (SWIPEANY) {
            string = "5221842001365318=18111260000058300000";
            magneticSwipe.setText("5221842001365318=18111260000058300000");
        }
        Track2BINChecker tbc = new Track2BINChecker(this.context, string);
        this.externalCard = tbc.isExternalCard();
        if (string==null||string.equals("")) {
//          Log.d("SWIPE", "BACK PRESSED");
        }
        try {
            magneticSwipe.openDriver();
            magneticSwipe.closeDriver();
            magneticSwipe.setIsQuit(true);
        } catch (Exception e) {
            //failed to close, maybe already closed or not open yet
        }
        alert.dismiss();

        Log.e("BERHASIL SWIPE","ntap");
        focusHandler.postDelayed(delayFocus, 400);
        panHolder = string.substring(0,16);
        // showPinDialog();
        try {
            JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
            if (SWIPELESS) {
                if (formId.startsWith("52")) {
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (array.getJSONObject(ch)
                                .getInt("comp_type") == CommonConfig.ComponentType.Button) {
                            Button proses = (Button) baseLayout.getChildAt(ch);
                            proses.setVisibility(VISIBLE);
                            Log.i("SWL", "SWIPELESS Button visible");
                        }
                    }
                }
            } else {
                if (formId.startsWith("52")) {
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                        if (array.getJSONObject(ch)
                                .getInt("comp_type") == CommonConfig.ComponentType.Button) {
                            Button proses = (Button) baseLayout.getChildAt(ch);
                            proses.setVisibility(GONE);
                            if (proses.performClick()) {
                                //ok
                            } else {
                                //at least we tried
                            }
                            break;
                        }
                    }
                } else {
                    int viscount = 0;
                    int visindex = 0;
                    for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {

                        Log.e("masuk for child","ok");
                        if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {

                            Log.e("if baselayout","yeah");
                            viscount++;
                            visindex = ch;
                        }
                    }
                    if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {

                        Log.e("viscount & hasButton","yeah");
                        Button proses = (Button) baseLayout.getChildAt(visindex);
                        proses.setVisibility(GONE);
                        if (proses.performClick()) {
                            Log.e("performclick","yeah");
                            //ok
                        } else {
                            //at least we tried
                        }
                    }
//                Log.d(TAG, "P-Check : " + String.valueOf(viscount) + String.valueOf(visindex));
                }
            }
        } catch (Exception e) {
            //pass
        }
    }

    private void showChangePinDialog(final View v) {
        try {
            final boolean isChangePIN = formId.equals("5900000");
            if (syncMessenger==null) {
                Log.i("PINPAD", "Refresh sync messenger");
                syncMessenger = parent.getSyncMessenger();
            }
            pinDialogCanceled = false;
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.pinpad_dialog, null);
            final android.widget.TextView alertText = (android.widget.TextView) promptsView.findViewById(R.id.pinAlert);
            alertText.setVisibility(View.GONE);
            pinpadWarningText = alertText;
            if (isChangePIN) {
                pinDialogCounter = pinModuleCounter - pinDialogCloseCounter;
                pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
            } else {
                pinDialogCounter++;
                if (pinModuleCounter > 1) {
                    pinpadText = (EditText) pinpadTextList.get(pinModuleCounter - pinDialogCounter);
                } else {
                    pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                }
            }
            android.widget.TextView dialogLabel = (android.widget.TextView) promptsView.findViewById(R.id.pinPass);
            if (pinpadText.getHint()!=null) {
                if (!pinpadText.getHint().equals("PIN")) {
                    dialogLabel.setText(pinpadText.getHint());
                }
            }
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final android.widget.EditText userInput = (android.widget.EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);

            final android.widget.Button btnOk = (android.widget.Button) promptsView
                    .findViewById(R.id.btnOk);
            Boolean doIneedHSM = pinModuleCounter==pinDialogCounter;
//            Log.d("PINPAD", String.valueOf(pinModuleCounter) + String.valueOf(pinDialogCounter));
            if (isChangePIN) {
                doIneedHSM = true;
            }
            if (!CommonConfig.getDeviceName().startsWith("WizarPOS")){
                doIneedHSM = false;
            }
            final Boolean needHsm = doIneedHSM;
            if (needHsm) {
                if (formId.equals("7100000")||formId.equals("7300000")) {
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                } else {
                    android.widget.TextView cardLabel = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardLabel);
                    android.widget.TextView cardNumber = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardNumber);
                    cardLabel.setVisibility(VISIBLE);
                    cardNumber.setText(panHolder);
                    cardNumber.setVisibility(VISIBLE);
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                }
            }

//        userInput.setKeyListener(null);
            // set dialog message
            if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                userInput.setInputType(InputType.TYPE_NULL);
            } else {
                userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            userInput.setTransformationMethod(new EditText.MyPasswordTransformationMethod());
            alertDialogBuilder
                    .setCancelable(false);
            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            final Button realProcess = (Button) v;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case CommonConfig.CALLBACK_KEYPRESSED:
                            Log.i(TAG, "receive callback");
                            byte[] data = msg.getData().getByteArray("data");
                            int datanol = 0;
                            if (data != null) {
                                datanol = Integer.valueOf(String.valueOf(data[0]));
                            } else {
                                if (btnOk.getVisibility() == View.GONE) {
                                    btnOk.callOnClick();
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < datanol; i++) {
                                sb.append("*");
                            }
                            userInput.setText(sb.toString());
                            break;
                        case CommonConfig.CALLBACK_RESULT:
                            Log.i(TAG, "receive result");
                            if (pinpadTextList.size() >= 0) {
                                try {
                                    String encPin = msg.getData().getString("data");
                                    pinblockHolder = encPin;
                                    pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                                    if (!needHsm) {
                                        encPin = userInput.getText().toString();
                                    } else {
                                        pinpadText.setMaxLength(16);
                                    }
                                    pinpadText.setText(encPin.replaceAll(" ", ""));
                                    pinDialogCloseCounter++;
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e("PINPAD", "Dialog closed already");
                                }

                                if (pinModuleCounter == 1) {
                                    try {
                                        if (!pinDialogCanceled) {
                                            actionUrl(realProcess, realProcess.getTag().toString());
                                            Log.i("PINPAD", "Kirim");
                                        }
                                    } catch (JSONException e) {
                                        Log.e("PINPAD", "Post act failed");
                                    }
                                } else {
                                    pinModuleCounter--;
                                    if (isChangePIN) {
                                        showChangePinDialog(v);
                                    }
                                }
//                                if (!formId.equals("2500000")) {
//                                    alertdialog_global.dismiss();
//                                }else if(formId.equals("5900000")){
//                                    alertDialog.dismiss();
//                                }
                                alertDialog.dismiss();
                            }
                            break;
                        case CommonConfig.CALLBACK_CANCEL:
                            dialog = ProgressDialog.show(context, "Clean Up", "Clearing Input Cache", true);
                            break;
                        case CommonConfig.CALLBACK_CANCEL_DONE:
                            try {
                                dialog.dismiss();
                            } catch (Exception e) {
                                //no dialog
                            }
                            context.onBackPressed();
                            alertDialog.dismiss();
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            };
            final Messenger pinblockReceiver = new Messenger(handler);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("PINPAD", "Dialog # " + pinModuleCounter + " ok click");
                    if (pinpadTextList.size() >= 0) {
                        try {
                            String encPin = pinblockHolder;
                            pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                            if (!needHsm) {
                                encPin = userInput.getText().toString();
                            } else {
                                pinpadText.setMaxLength(16);
                            }
                            pinpadText.setText(encPin.replaceAll(" ", ""));
                            pinDialogCloseCounter++;
                        } catch (IndexOutOfBoundsException e) {
                            Log.e("PINPAD", "Dialog closed already");
                        }
                        alertDialog.dismiss();
                        if (pinModuleCounter == 1) {
                            try {
                                if (!pinDialogCanceled) {
                                    actionUrl(realProcess, realProcess.getTag().toString());
                                }
                            } catch (JSONException e) {
                                Log.e("PINPAD", "Post act failed");
                            }
                        } else {
                            pinModuleCounter--;
                            if (isChangePIN) {
                                showChangePinDialog(v);
                            }
                        }
                    } else {
                        return;
                    }

                }
            });
            // show it
            if (needHsm) {
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        try {
                            Log.i("PINPAD", "send message");
                            Message message = Message.obtain(null, CommonConfig.CAPTURE_PINBLOCK);
                            Bundle bundle = new Bundle();
                            bundle.putString("pan", panHolder);
                            bundle.putString("formid", formId);
                            message.setData(bundle);
                            message.replyTo = pinblockReceiver;
                            syncMessenger.send(message);
                        } catch (Exception e) {
                            //cannot start pinpad
                        }
                    }
                });

                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            Log.i("PINPAD", "Back pressed");
                            try {
                                pinDialogCanceled = true;
//                                t1.interrupt();
                                Message message = Message.obtain(null, CommonConfig.CAPTURE_CANCEL);
                                message.replyTo = pinblockReceiver;
                                syncMessenger.send(message);
                                btnOk.setVisibility(GONE);
                            } catch (Exception e) {
                                //failed to close, maybe already closed or not open yet
                            }
                        } else {

                        }
                        return true;
                    }
                });
            }
            alertdialog_global = alertDialog;
            alertDialog.show();
            alertDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.e(TAG, "Error create pinpad dialog: " + e.getMessage());
            context.onBackPressed();
        }
    }


    private void showPinDialog(View v) {
        try {
            pinDialogCanceled = false;
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.pinpad_dialog, null);
            final android.widget.TextView alertText = (android.widget.TextView) promptsView.findViewById(R.id.pinAlert);
            alertText.setVisibility(View.GONE);
            pinpadWarningText = alertText;
            pinDialogCounter++;
            if (pinModuleCounter > 1) {
                pinpadText = (EditText) pinpadTextList.get(pinModuleCounter - pinDialogCounter);
            } else {
                pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
            }
            android.widget.TextView dialogLabel = (android.widget.TextView) promptsView.findViewById(R.id.pinPass);
            if (pinpadText.getHint()!=null) {
                if (!pinpadText.getHint().equals("PIN")) {
                    dialogLabel.setText(pinpadText.getHint());
                }
            }
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final android.widget.EditText userInput = (android.widget.EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);

            final android.widget.Button btnOk = (android.widget.Button) promptsView
                    .findViewById(R.id.btnOk);
            Boolean doIneedHSM = pinModuleCounter==pinDialogCounter;
//            Log.d("PINPAD", String.valueOf(pinModuleCounter) + String.valueOf(pinDialogCounter));
            if (!CommonConfig.getDeviceName().startsWith("WizarPOS")){
                doIneedHSM = false;
            }
            final Boolean needHsm = doIneedHSM;
            if (needHsm) {
                if (formId.equals("7100000")||formId.equals("7300000")) {
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                } else {
                    android.widget.TextView cardLabel = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardLabel);
                    android.widget.TextView cardNumber = (android.widget.TextView) promptsView
                            .findViewById(R.id.cardNumber);
                    cardLabel.setVisibility(VISIBLE);
                    cardNumber.setText(panHolder);
                    cardNumber.setVisibility(VISIBLE);
                    btnOk.setVisibility(GONE);
                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
                            .findViewById(R.id.dialogFoot);
                    dialogFoot.setVisibility(VISIBLE);
                }
            }

//        userInput.setKeyListener(null);
            // set dialog message
            if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                userInput.setInputType(InputType.TYPE_NULL);
            } else {
                userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            userInput.setTransformationMethod(new EditText.MyPasswordTransformationMethod());
            alertDialogBuilder
                    .setCancelable(false);
            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            final Button realProcess = (Button) v;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
//                    Log.d("HANDLER", msg.getData().getString("key"));
                    try {
                        byte[] data = msg.getData().getByteArray("data");
                        int datanol = 0;
                        if (data != null) {
                            datanol = Integer.valueOf(String.valueOf(data[0]));
                        } else {
                            if (btnOk.getVisibility() == View.GONE) {
                                btnOk.callOnClick();
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < datanol; i++) {
                            sb.append("*");
                        }
                        userInput.setText(sb.toString());
                    } catch (Exception e) {

                    }
                }
            };

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("PINPAD", "Dialog # " + pinModuleCounter + " ok click");
                    if (pinpadTextList.size() >= 0) {
                        try {
                            String encPin = pinblockHolder;
                            pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
                            if (!needHsm) {
                                encPin = userInput.getText().toString();
                            } else {
                                pinpadText.setMaxLength(16);
                            }
//                            Log.d("PINPAD", "Ieu Pinblock na : " + encPin);
                            pinpadText.setText(encPin.replaceAll(" ", ""));
                            pinDialogCloseCounter++;
                        } catch (IndexOutOfBoundsException e) {
                            Log.e("PINPAD", "Dialog closed already");
                        }
                        alertDialog.dismiss();
//                        Log.d("PINPAD", String.valueOf(pinModuleCounter));
                        if (pinModuleCounter == 1) {
                            try {
                                if (!pinDialogCanceled) {
                                    actionUrl(realProcess, realProcess.getTag().toString());
                                }
                            } catch (JSONException e) {
                                Log.e("PINPAD", "Post act failed");
                            }
                        } else {
                            pinModuleCounter--;
//                            if (pinModuleCounter>1) {
//                                AlertDialog cad = (AlertDialog) pinpadDialogList.get(pinModuleCounter - 1);
//                                cad.show();
//                                cad.getWindow().setAttributes(lp);
//                                Log.i("PINPAD", "Dialog list pick #" + pinModuleCounter);
//                            }
                        }
                    } else {
                        return;
                    }

                }
            });
            // show it
            if (needHsm) {
                final Thread t1 = new Thread(new ReadPINThread(handler));
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
//                        Log.d("PINPAD", "START READ HANDLER");
                        try {
                            t1.start();
                        } catch (Exception e) {
                            //cannot start pinpad
                        }
                    }
                });

                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Log.d("BACK", "FROM DIALOG");
                            try {
                                pinDialogCanceled = true;
                                t1.interrupt();
                                btnOk.setVisibility(GONE);
//                                PINPadInterface.setupCallbackHandler(null);
//                                PINPadInterface.inputPIN(null, 0, new byte[8], 0,0);
//                                close();
                                alertDialog.dismiss();
//                                open();
                                context.finish();
                            } catch (Exception e) {
                                //failed to close, maybe already closed or not open yet
                            }
                        } else {

                        }
                        return true;
                    }
                });
            }
            alertDialog.show();
            alertDialog.getWindow().setAttributes(lp);
//            if (pinModuleCounter==pinDialogCounter) {
//                alertDialog.show();
//                alertDialog.getWindow().setAttributes(lp);
//            } else {
//                pinpadDialogList.add(alertDialog);
//                Log.i("PINPAD", "Dialog list #" + pinpadDialogList.size());
//            }
        } catch (Exception e) {
            Log.e(TAG, "Error create pinpad dialog: " + e.getMessage());
            context.onBackPressed();
        }
    }

//    private void showSinglePinDialog(View v) {
//        try {
//            if (syncMessenger==null) {
//                Log.i("PINPAD", "Refresh sync messenger");
//                syncMessenger = parent.getSyncMessenger();
//            }
//            pinDialogCanceled = false;
//            LayoutInflater li = LayoutInflater.from(context);
//            View promptsView = li.inflate(R.layout.pinpad_dialog, null);
//            final android.widget.TextView alertText = (android.widget.TextView) promptsView.findViewById(R.id.pinAlert);
//            alertText.setVisibility(View.GONE);
//            pinpadWarningText = alertText;
//            pinDialogCounter++;
//            if (pinModuleCounter > 1) {
//                pinpadText = (EditText) pinpadTextList.get(pinModuleCounter - pinDialogCounter);
//            } else {
//                pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
//            }
//            android.widget.TextView dialogLabel = (android.widget.TextView) promptsView.findViewById(R.id.pinPass);
//            if (pinpadText.getHint()!=null) {
//                if (!pinpadText.getHint().equals("PIN")) {
//                    dialogLabel.setText(pinpadText.getHint());
//                }
//            }
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//            // set prompts.xml to alertdialog builder
//            alertDialogBuilder.setView(promptsView);
//
//            final android.widget.EditText userInput = (android.widget.EditText) promptsView
//                    .findViewById(R.id.editTextDialogUserInput);
//
//            final android.widget.Button btnOk = (android.widget.Button) promptsView
//                    .findViewById(R.id.btnOk);
//            Boolean doIneedHSM = pinModuleCounter==pinDialogCounter;
////            Log.d("PINPAD", String.valueOf(pinModuleCounter) + String.valueOf(pinDialogCounter));
//            if (!CommonConfig.getDeviceName().startsWith("WizarPOS")){
//                doIneedHSM = false;
//            }
//            final Boolean needHsm = doIneedHSM;
//            if (needHsm) {
//                if (formId.equals("7100000")||formId.equals("7300000")) {
//                    btnOk.setVisibility(GONE);
//                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
//                            .findViewById(R.id.dialogFoot);
//                    dialogFoot.setVisibility(VISIBLE);
//                } else {
//                    android.widget.TextView cardLabel = (android.widget.TextView) promptsView
//                            .findViewById(R.id.cardLabel);
//                    android.widget.TextView cardNumber = (android.widget.TextView) promptsView
//                            .findViewById(R.id.cardNumber);
//                    cardLabel.setVisibility(VISIBLE);
//                    cardNumber.setText(panHolder);
//                    cardNumber.setVisibility(VISIBLE);
//                    btnOk.setVisibility(GONE);
//                    android.widget.TextView dialogFoot = (android.widget.TextView) promptsView
//                            .findViewById(R.id.dialogFoot);
//                    dialogFoot.setVisibility(VISIBLE);
//                }
//            }
//
////        userInput.setKeyListener(null);
//            // set dialog message
//            if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
//                userInput.setInputType(InputType.TYPE_NULL);
//            } else {
//                userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
//            }
//            userInput.setTransformationMethod(new EditText.MyPasswordTransformationMethod());
//            alertDialogBuilder
//                    .setCancelable(false);
//            // create alert dialog
//            final AlertDialog alertDialog = alertDialogBuilder.create();
//            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//            lp.copyFrom(alertDialog.getWindow().getAttributes());
//            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//            final Button realProcess = (Button) v;
//            final Handler handler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    switch (msg.what) {
//                        case CommonConfig.CALLBACK_KEYPRESSED:
//                            Log.i(TAG, "receive callback");
//                            byte[] data = msg.getData().getByteArray("data");
//                            int datanol = 0;
//                            if (data != null) {
//                                datanol = Integer.valueOf(String.valueOf(data[0]));
//                            } else {
//                                if (btnOk.getVisibility() == View.GONE) {
//                                    btnOk.callOnClick();
//                                }
//                            }
//                            StringBuilder sb = new StringBuilder();
//                            for (int i = 0; i < datanol; i++) {
//                                sb.append("*");
//                            }
//                            userInput.setText(sb.toString());
//                            break;
//                        case CommonConfig.CALLBACK_RESULT:
//                            Log.i(TAG, "receive result");
//                            if (pinpadTextList.size() >= 0) {
//                                try {
//                                    String encPin = msg.getData().getString("data");
//                                    pinblockHolder = encPin;
//                                    pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
//                                    if (!needHsm) {
//                                        encPin = userInput.getText().toString();
//                                    } else {
//                                        pinpadText.setMaxLength(16);
//                                    }
//                                    pinpadText.setText(encPin.replaceAll(" ", ""));
//                                    pinDialogCloseCounter++;
//                                } catch (IndexOutOfBoundsException e) {
//                                    Log.e("PINPAD", "Dialog closed already");
//                                }
//                                if (pinModuleCounter == 1) {
//                                    try {
//                                        if (!pinDialogCanceled) {
//                                            actionUrl(realProcess, realProcess.getTag().toString());
//                                        }
//                                    } catch (JSONException e) {
//                                        Log.e("PINPAD", "Post act failed");
//                                    }
//                                } else {
//                                    pinModuleCounter--;
//                                }
//                            }
//                            break;
//                        default:
//                            super.handleMessage(msg);
//                    }
//                }
//            };
//            final Messenger pinblockReceiver = new Messenger(handler);
//            btnOk.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.i("PINPAD", "Dialog # " + pinModuleCounter + " ok click");
//                    if (pinpadTextList.size() >= 0) {
//                        try {
//                            String encPin = pinblockHolder;
//                            pinpadText = (EditText) pinpadTextList.get(pinDialogCloseCounter);
//                            if (!needHsm) {
//                                encPin = userInput.getText().toString();
//                            } else {
//                                pinpadText.setMaxLength(16);
//                            }
//                            pinpadText.setText(encPin.replaceAll(" ", ""));
//                            pinDialogCloseCounter++;
//                        } catch (IndexOutOfBoundsException e) {
//                            Log.e("PINPAD", "Dialog closed already");
//                        }
//                        alertDialog.dismiss();
//                        if (pinModuleCounter == 1) {
//                            try {
//                                if (!pinDialogCanceled) {
//                                    actionUrl(realProcess, realProcess.getTag().toString());
//                                }
//                            } catch (JSONException e) {
//                                Log.e("PINPAD", "Post act failed");
//                            }
//                        } else {
//                            pinModuleCounter--;
//                        }
//                    } else {
//                        return;
//                    }
//
//                }
//            });
//            // show it
//            if (needHsm) {
//                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        try {
//                            Log.i("PINPAD", "send message");
//                            Message message = Message.obtain(null, CommonConfig.CAPTURE_PINBLOCK);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("pan", panHolder);
//                            bundle.putString("formid", formId);
//                            message.setData(bundle);
//                            message.replyTo = pinblockReceiver;
//                            syncMessenger.send(message);
//                        } catch (Exception e) {
//                            //cannot start pinpad
//                        }
//                    }
//                });
//
//                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK) {
//                            Log.i("PINPAD", "Back pressed");
//                            try {
//                                pinDialogCanceled = true;
////                                t1.interrupt();
//                                Message message = Message.obtain(null, CommonConfig.CAPTURE_CANCEL);
//                                message.replyTo = pinblockReceiver;
//                                syncMessenger.send(message);
//                                btnOk.setVisibility(GONE);
//                                context.onBackPressed();
//                                alertDialog.dismiss();
//                            } catch (Exception e) {
//                                //failed to close, maybe already closed or not open yet
//                            }
//                        } else {
//
//                        }
//                        return true;
//                    }
//                });
//            }
//            alertDialog.show();
//            alertDialog.getWindow().setAttributes(lp);
//        } catch (Exception e) {
//            Log.e(TAG, "Error create pinpad dialog: " + e.getMessage());
//            context.onBackPressed();
//        }
//    }

    public void actionUrl(Button button, final String actionUrl) throws JSONException {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final JSONObject msg = new JSONObject();
        final List<String> data = new ArrayList<>();
//        StringBuilder data = new StringBuilder();
        int size = baseLayout.getChildCount();
        boolean isError = true;
        String message = "";
        screenLoader = comp.getString("id");

//        deviceLocation = getDeviceLocation();
//        Log.i("GPS", deviceLocation);
        if(hasTapModule){
            CardData cardData = new CardData();
            String id = comp.getString("id");
            cardData.setWhatToDo(id);
            cardData.setMsgSI(actionUrl);
            AlertDialog.Builder alertTap = new AlertDialog.Builder(context);
            String btnText = "";
            for (int i = 0;i<baseLayout.getChildCount();i++) {
                for(int j = 0;j<baseLayout.getChildCount();j++){
                    View v = baseLayout.getChildAt(i);
                    try{
                        int childIndex = (int) v.getTag();
                        if(childIndex == j){
                            if (v instanceof EditText) {
                                EditText editText = (EditText) v;
//                                Log.d("TOPUP_ELE",editText.getText().toString());
                                if(editText.getText().toString().equals("")){
                                    continue;
                                }
                                if(!editText.isEditText() && editText.isNumber()){
                                    cardData.setPin(editText.getText().toString());
                                }
                                if(editText.isEditText()&& editText.isNumber()){
                                    if(id.equals(TapCard.TOPUP_DEPOSIT) || id.equals(TapCard.TOPUP_ONLINE)){
                                        cardData.setTopupAmount(editText.getText().toString());
                                        Log.d("TOPUP_NOM",editText.getText().toString());
                                        btnText = "Kirim";

                                    }else if(id.equals(TapCard.PEMBAYARAN_NORMAL) || id.equals(TapCard.PEMBAYARAN_DISKON)){
                                        cardData.setDeductAmount(editText.getText().toString());
                                    }
                                }

                            }
                            if (v instanceof MagneticSwipe) {
                                MagneticSwipe magneticSwipe = (MagneticSwipe) v;
                                cardData.setTrack2Data(magneticSwipe.getText().toString());
                            }
                        }

                    }catch (ClassCastException ex){

                    }
                }
            }

            LayoutInflater li = LayoutInflater.from(context);
            final TapCard promptsView = (TapCard)li.inflate(R.layout.tap_card, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            final AlertDialog alertTaps = alertDialogBuilder.create();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertTaps.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            if(id.equals(TapCard.PEMBAYARAN_NORMAL) ||
                    id.equals(TapCard.PEMBAYARAN_DISKON) ||
                    id.equals(TapCard.TOPUP_ONLINE) ||
                    id.equals(TapCard.AKTIFASI_DEPOSIT) ||
                    id.equals(TapCard.VOID_REFUND)) {
                promptsView.setFormListener(new TapCard.FormListener() {
                    @Override
                    public void onSuccesListener(JSONObject obj) {

                        promptsView.searchEnd();
                        try {
                            comp = obj.getJSONObject("screen");
                            if (obj.has("server_ref")) {
                                serverRef = obj.getString("server_ref");
                            }
                            if (obj.has("server_appr")) {
                                serverAppr = obj.getString("server_appr");
                            }
                            if (obj.has("server_date")) {
                                serverDate = obj.getString("server_date");
                            }
                            if (obj.has("server_time")) {
                                serverTime = obj.getString("server_time");
                            }
                            if (obj.has("card_type")) {
                                cardType = obj.getString("card_type");
                            }
                            if (obj.has("nomor_kartu")) {
                                nomorKartu = obj.getString("nomor_kartu");
                            }
                            FormMenu.this.init();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        alertTaps.dismiss();
                    }
                });
            } else {
                promptsView.setFormListener(new TapCard.FormListener() {
                    @Override
                    public void onSuccesListener(JSONObject obj) {

                        promptsView.searchEnd();
                        try {
                            JSONArray tmp = new JSONArray();
                            JSONArray arr = comp.getJSONObject("comps").getJSONArray("comp");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject resp = arr.getJSONObject(i);
                                if (resp.getString("comp_lbl").contains("Proses") && resp.getString("comp_type").equals("7")) {
                                    continue;
                                }
                                tmp.put(resp);
                            }
                            comp.getJSONObject("comps").put("comp", tmp);
                            if (obj.has("server_ref")) {
                                serverRef = obj.getString("server_ref");
                            }
                            if (obj.has("server_appr")) {
                                serverAppr = obj.getString("server_appr");
                            }
                            if (obj.has("server_date")) {
                                serverDate = obj.getString("server_date");
                            }
                            if (obj.has("server_time")) {
                                serverTime = obj.getString("server_time");
                            }
                            if (obj.has("card_type")) {
                                cardType = obj.getString("card_type");
                            }
                            if (obj.has("nomor_kartu")) {
                                nomorKartu = obj.getString("nomor_kartu");
                            }
                            FormMenu.this.init();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        alertTaps.dismiss();
                    }
                });
            }
            promptsView.init(cardData);
            promptsView.searchBegin();
            promptsView.setOkListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertTaps.dismiss();
                    promptsView.searchEnd();
                    ((Activity) context).finish();
                }
            });
            alertTaps.show();
            try {
                if (alertdialog_global.isShowing()) {
                    alertdialog_global.dismiss();
                }
            }catch(Exception e){

            }


            alertTaps.getWindow().setAttributes(lp);

        }else{
            if(actionUrl.equals(TapCard.SI_REAKTIVASI_PAY)){
                CardData cardData = new CardData();
                String id = TapCard.REAKTIVASI_PAY;
                cardData.setWhatToDo(id);
                cardData.setMsgSI(actionUrl);
                AlertDialog.Builder alertTap = new AlertDialog.Builder(context);
                String btnText = "";
                String f = "Saldo Deposit";
                String e = "Lama Pasif";
                String d = "Biaya Admin";
                String c = "Status Kartu Setelah Reaktivasi";
                JSONArray arr = comp.getJSONObject("comps").getJSONArray("comp");
                int intDeduct = 0;
                for(int i = 0;i<arr.length();i++){
                    JSONObject resp = arr.getJSONObject(i);
                    if(resp.getString("comp_lbl").contains(d)){
                        String deduct = resp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                        deduct = deduct.split(",")[0];
                        cardData.setDeductAmount(deduct.replace(".",""));
                    }else if(resp.getString("comp_lbl").contains(c)){
                        String aktif = resp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
//                        Log.d("RQ", "Status After : " + aktif);
                        cardData.setStatusAfter(aktif);
                    }else if(resp.getString("comp_lbl").contains(e)){
                        String lamaPasif = resp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                        cardData.setLamaPasif(lamaPasif);
//                        Log.d("RQ", "Lama Pasif : " + lamaPasif);
                    }else if(resp.getString("comp_lbl").contains(f)){
                        String saldoDeposit = resp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                        saldoDeposit = saldoDeposit.replace("\\,","").replace("\\.","");
                        if (saldoDeposit.length()>2) {
                            saldoDeposit = saldoDeposit.substring(0, saldoDeposit.length() - 2);
                        }
                        cardData.setSaldoDeposit(saldoDeposit);
//                        Log.d("RQ", "Saldo Deposit : " + saldoDeposit);
                    }
                }
                LayoutInflater li = LayoutInflater.from(context);
                final TapCard promptsView = (TapCard)li.inflate(R.layout.tap_card, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final AlertDialog alertTaps = alertDialogBuilder.create();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(alertTaps.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                // show it
                promptsView.setFormListener(new TapCard.FormListener() {
                    @Override
                    public void onSuccesListener(JSONObject obj) {

                        promptsView.searchEnd();
                        try {
                            comp = obj.getJSONObject("screen");
                            if (obj.has("server_ref")) {
                                serverRef = obj.getString("server_ref");
                            }
                            if (obj.has("server_appr")) {
                                serverAppr = obj.getString("server_appr");
                            }
                            if (obj.has("server_date")) {
                                serverDate = obj.getString("server_date");
                            }
                            if (obj.has("server_time")) {
                                serverTime = obj.getString("server_time");
                            }
                            if (obj.has("card_type")) {
                                cardType = obj.getString("card_type");
                            }
                            if (obj.has("nomor_kartu")) {
                                nomorKartu = obj.getString("nomor_kartu");
                            }
                            if (obj.has("stan")) {
                                serverStan = obj.getString("stan");
                            }
                            FormMenu.this.init();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        alertTaps.dismiss();
                    }
                });
                promptsView.init(cardData);
                promptsView.searchBegin();
                promptsView.setOkListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertTaps.dismiss();
                        promptsView.searchEnd();
                        ((Activity) context).finish();
                    }
                });
                alertTaps.show();
                alertTaps.getWindow().setAttributes(lp);
            }else{
                String newAct = null;
                for (int i = 0;i<baseLayout.getChildCount();i++) {
                    for(int j = 0;j<baseLayout.getChildCount();j++){
                        View v = baseLayout.getChildAt(i);
                        try{
                            int childIndex = (int) v.getTag();
                            if(childIndex == j){
                                if (v instanceof EditText) {
                                    EditText editText = (EditText) v;
                                    if(editText.getText().toString().equals("")&&editText.isMandatory()){
                                        continue;
                                    }
                                    if (actionUrl.equals("A57000")) {
                                        if (((int) editText.getTag())==2) {
                                            String idwp = editText.getText().toString();
                                            if (idwp.startsWith("4")||idwp.startsWith("5")||idwp.startsWith("6")) {
                                                newAct = "A57200";
                                            }
                                            if (idwp.startsWith("7")||idwp.startsWith("8")||idwp.startsWith("9")) {
                                                newAct = "A57400";
                                            }

                                        }
                                    }
//                                    Log.d("EDIT READ", editText.getText().toString());
                                    data.add(editText.getText().toString());
                                }
                                if (v instanceof ComboBox) {
                                    ComboBox comboBox = (ComboBox) v;
                                    String cdata = comboBox.getSelectedItem().toString();
//                                    Log.d("EDIT READ", cdata);
                                    if (actionUrl.equals("A54321")) {
                                        cdata = cdata.replace("Rp ","").replace(".","").replace(",00","");
                                    }
                                    data.add(cdata);
                                }
                                if (v instanceof RadioButton) {
                                    RadioButton radioButton = (RadioButton) v;
                                    data.add(radioButton.isChecked() + "");
                                }
                                if (v instanceof CheckBox) {
                                    CheckBox checkBox = (CheckBox) v;
                                    data.add(checkBox.isChecked() + "");
                                }
                                if (v instanceof MagneticSwipe) {
                                    MagneticSwipe magneticSwipe = (MagneticSwipe) v;
                                    data.add(magneticSwipe.getText().toString());
                                }
                                if (v instanceof ChipInsert) {
                                    ChipInsert chipInsert = (ChipInsert) v;
                                    data.add(chipInsert.getText().toString());
                                }
                            }

                        }catch (ClassCastException ex){
//                            ex.printStackTrace();
                        }
                    }
                }
                String dataOutput = TextUtils.join("|",data);
                if (actionUrl.equals("L00001")) {
                    Location xLocation;
                    GPSLocation gpsLocation = new GPSLocation();
                    String gloc = "";
                    try {
                        InputStream inputStream = context.openFileInput("loc.txt");
                        if (inputStream!=null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String receiveString = "";
                            StringBuilder stringBuilder = new StringBuilder();
                            while ((receiveString = bufferedReader.readLine())!=null) {
                                stringBuilder.append(receiveString);
                            }
                            inputStream.close();
                            gloc = stringBuilder.toString();
                        }
                    } catch (FileNotFoundException e) {
                        gloc = "0,0";
                        try {
                            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("loc.txt",Context.MODE_PRIVATE));
                            osw.write("0,0");
                            osw.close();
                        } catch (FileNotFoundException ee) {
                            ee.printStackTrace();
                        } catch (IOException ee) {
                            ee.printStackTrace();
                        }
                        e.printStackTrace();
                    } catch (IOException e) {
                        gloc = "0,0";
                        e.printStackTrace();
                    }
                    if (gpsLocation!=null) {
//                        gloc += String.valueOf(xLocation.getLongitude());
//                        gloc += ",";
//                        gloc += String.valueOf(xLocation.getLatitude());
//                        Log.d("GPS", gloc);
                    }
                    String serialNum = Build.SERIAL;
                    PackageInfo pInfo = null;
                    try {
                        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String version = pInfo.versionName;
                    String ldata = "v2016.";
                    ldata += version;
                    ldata += ".6PL" + serialNum.substring(1);
                    ldata += gloc;
                    ldata = ldata + "                                                            ";
                    ldata = ldata.substring(0,60);
                    dataOutput = ldata;
                }
                try {
                    msg.put("msg_id", telephonyManager.getDeviceId() + sdf.format(new Date()));
                    msg.put("msg_ui", telephonyManager.getDeviceId());
                    msg.put("msg_si", actionUrl);
                    //ovride
                    if (newAct!=null) {
                        msg.put("msg_si", newAct);
                    }
                    msg.put("msg_dt", dataOutput);
                    JSONObject msgRoot = new JSONObject();
                    msgRoot.put("msg", msg);
//                    if(formId.equalsIgnoreCase("5A20000")){

                    if(formId.equalsIgnoreCase("9B11000")
                            || formId.equalsIgnoreCase("9B21000") || formId.equalsIgnoreCase("9A20000")
                            || formId.equalsIgnoreCase("2F11000") || formId.equalsIgnoreCase("5B11000")
                            || formId.equalsIgnoreCase("6611000") || formId.equalsIgnoreCase("7511000")
                            || formId.equalsIgnoreCase("5A20000") || formId.equalsIgnoreCase("2E20000")
                            || formId.equalsIgnoreCase("6520000") || formId.equalsIgnoreCase("7420000")
                            || formId.equalsIgnoreCase("2900000") || formId.equalsIgnoreCase("5B21000")
                            || formId.equalsIgnoreCase("6621000") || formId.equalsIgnoreCase("7521000")
                            || formId.equalsIgnoreCase("2F21000")){
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Peringatan");
                        if(!dataOutput.equals("")) {
                            if ((formId.equalsIgnoreCase("9B11000")
                                    || formId.equalsIgnoreCase("9B21000") || formId.equalsIgnoreCase("2F11000")
                                    || formId.equalsIgnoreCase("5B11000") || formId.equalsIgnoreCase("6611000")
                                    || formId.equalsIgnoreCase("7511000") || formId.equalsIgnoreCase("2900000")
                                    || formId.equalsIgnoreCase("5B21000") || formId.equalsIgnoreCase("6621000")
                                    || formId.equalsIgnoreCase("7521000") || formId.equalsIgnoreCase("2F21000")) && dataOutput.length() < 8) {
                                alertDialog.setMessage("Format isian salah");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            } else{
                                new PostData().execute(msgRoot.toString());
                            }
                        }else{
                            if(formId.equalsIgnoreCase("9B11000")
                                    || formId.equalsIgnoreCase("9B21000") || formId.equalsIgnoreCase("2F11000")
                                    || formId.equalsIgnoreCase("5B11000") || formId.equalsIgnoreCase("6611000")
                                    || formId.equalsIgnoreCase("7511000") || formId.equalsIgnoreCase("2900000")
                                    || formId.equalsIgnoreCase("5B21000") || formId.equalsIgnoreCase("6621000")
                                    || formId.equalsIgnoreCase("7521000") || formId.equalsIgnoreCase("2F21000")){
                                alertDialog.setMessage("Isian tanggal tidak boleh kosong");
                            }else {
                                alertDialog.setMessage("No trace tidak boleh kosong");
                            }
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }else{
                        new PostData().execute(msgRoot.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Log.d("PINPAD", "KEYCODE = " + keyCode);
//        Log.d("PINPAD", "EVENT = " + event.toString());
        return super.onKeyUp(keyCode, event);
    }

    private void init() throws JSONException {
        String id = comp.getString("id");
        if (comp.has("title")) {
            AutofitTextView tv = (AutofitTextView) context.findViewById(R.id.title_list);
            tv.setText(comp.getString("title"));
//            Log.e("INI TITLE", comp.getString("title"));
//            Log.d("TITH", tv.getHeight()+"");
//            Log.d("TITCH", comp.getString("title").length()+"");
        }
        formId = id;
        Log.i("FORM", "Init : " + id);
        if (nomorKartu==null) {
            nomorKartu = "";
        }
        if (cardType==null) {
            cardType = "";
        }
        JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
        hasTapModule = false;
        hasMagModule = false;
        pinModuleCounter = 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 20, 0, 0);
        baseLayout.removeAllViews();
        if(Arrays.asList(TapCard.BRIZZI_MENU).contains(id)
                && !id.equals(TapCard.TOPUP_ONLINE)
                && !id.equals(TapCard.TOPUP_DEPOSIT)
                && !id.equals(TapCard.PEMBAYARAN_NORMAL)
                && !id.equals(TapCard.PEMBAYARAN_DISKON)
                && !id.equals(TapCard.SETTLEMENT)){
            LayoutInflater li = LayoutInflater.from(context);
            final TapCard promptsView = (TapCard)li.inflate(R.layout.tap_card, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            // create alert dialog
            final AlertDialog alertTap = alertDialogBuilder.create();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertTap.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            // show it
            CardData cardData = new CardData();
            cardData.setWhatToDo(id);
            if (id.equals(TapCard.REDEEM_NEXT)) {
//                Log.d("REDEEM", comp.toString());
                String vl = array.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setCardNumber(vl);
                vl = array.getJSONObject(1).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setRedCardBalance(vl);
                vl = array.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setRedDepoBalance(vl);
                vl = array.getJSONObject(3).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setRedFee(vl);
                vl = array.getJSONObject(4).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setRedTotal(vl);
                vl = comp.getString("server_date");
                cardData.settDate(vl);
//                Log.d("REDEEM", vl);
                vl = comp.getString("server_time");
                cardData.settTime(vl);
//                Log.d("REDEEM", vl);
                vl = comp.getString("server_ref");
                cardData.setServerRef(vl);
//                Log.d("REDEEM", vl);
            }
            if (id.equals(TapCard.VOID_REFUND)) {
                String sv = array.getJSONObject(1).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cardData.setStanVoid(sv);
            }
            promptsView.setFormListener(new TapCard.FormListener() {
                @Override
                public void onSuccesListener(JSONObject obj) {

                    promptsView.searchEnd();
                    try {
                        comp = obj.getJSONObject("screen");
                        if (obj.has("server_ref")) {
                            serverRef = obj.getString("server_ref");
                        }
                        if (obj.has("server_appr")) {
                            serverAppr = obj.getString("server_appr");
                        }
                        if (obj.has("server_date")) {
                            serverDate = obj.getString("server_date");
                        }
                        if (obj.has("server_time")) {
                            serverTime = obj.getString("server_time");
                        }
                        if (obj.has("card_type")) {
                            cardType = obj.getString("card_type");
                        }
                        if (obj.has("nomor_kartu")) {
                            nomorKartu = obj.getString("nomor_kartu");
                        }
                        if (obj.has("stan")) {
                            serverStan = obj.getString("stan");
                        }
                        FormMenu.this.init();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    alertTap.dismiss();
                }
            });
            promptsView.init(cardData);
            if (!id.equals(TapCard.INITIALIZE)) {
                promptsView.searchBegin();
            }
            promptsView.setOkListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertTap.dismiss();
                    promptsView.searchEnd();
                    ((Activity) context).finish();
                }
            });
            alertTap.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode==KeyEvent.KEYCODE_BACK) {
//                        Log.d("BACK", "FROM DIALOG");
                        try {
                            // magneticSwipe.closeDriver();
                            // magneticSwipe.setIsQuit(true);
                            promptsView.searchEnd();
                            alertTap.dismiss();
                            ((Activity) context).finish();
//                            context.onBackPressed();
                        } catch (Exception e) {
                            //failed to close, maybe already closed or not open yet
                            e.printStackTrace();
                            Log.e("BACK", "FAILED");
                        }
                    }
                    return true;
                }
            });

            alertTap.show();
            alertTap.getWindow().setAttributes(lp);
        }else{
            for (int j = 0; j < array.length(); j++) {
                final JSONObject data = array.getJSONObject(j);
                int seq = data.getInt("seq");
//                Log.d("ITERASI2", ""+j);
                int type = data.getInt("comp_type");
                String value = "";
                value = data.getString("comp_lbl");
                Object[] compOpts = null;
                int maxLength = 0;
                switch (type) {
                    case CommonConfig.ComponentType.TextView:
                        if (value.contains("INFO KUOTA BERHASIL")) {

                            String content = value.substring(0, value.indexOf("INFO KUOTA BERHASIL"));
                            TextView textView = (TextView) li.inflate(R.layout.text_view, null);
                            textView.init(content, data.getString("comp_id"), data.getBoolean("visible"), data);
                            textView.setTag(seq);

                            textView.setLayoutParams(params);
                            baseLayout.addView(textView);

                            TextView textView2 = (TextView) li.inflate(R.layout.text_view, null);
                            textView2.init(value.replace(content,""), data.getString("comp_id"), data.getBoolean("visible"), data);
                            textView2.setTag(seq);

                            params.setMargins(0,0,0,0);
                            textView2.setLayoutParams(params);
                            textView2.setGravity(Gravity.CENTER_HORIZONTAL);
                            textView2.setPadding(0,0,0,0);
                            baseLayout.addView(textView2);

                        }
                        else if (value.contains("Transaksi Berhasil")) {
                            String content = value.substring(0, value.indexOf("Transaksi Berhasil"));
                            TextView textView = (TextView) li.inflate(R.layout.text_view, null);
                            textView.init(content, data.getString("comp_id"), data.getBoolean("visible"), data);
                            textView.setTag(seq);

                            textView.setLayoutParams(params);
                            baseLayout.addView(textView);

                            TextView textView2 = (TextView) li.inflate(R.layout.text_view, null);
                            textView2.init(value.replace(content,""), data.getString("comp_id"), data.getBoolean("visible"), data);
                            textView2.setTag(seq);

                            params.setMargins(0,0,0,0);
                            textView2.setLayoutParams(params);
                            textView2.setGravity(Gravity.CENTER_HORIZONTAL);
                            textView2.setPadding(0,0,0,0);
                            baseLayout.addView(textView2);
                        }
                        else {
                            TextView textView = (TextView) li.inflate(R.layout.text_view, null);
                            textView.init(data);
                            textView.setTag(seq);

                            textView.setLayoutParams(params);
                            baseLayout.addView(textView);
                        }
                        break;
                    case CommonConfig.ComponentType.EditText:
                        EditText editText = (EditText) li.inflate(R.layout.edit_text, null);
                        editText.init(data);
                        String txt = editText.getText().toString();
                        editText.setTag(seq);
                        editText.setLayoutParams(params);

                        if (data.getString("comp_lbl").equals("Kode Bansos")){
                            editText.setMaxLength(4);
                        }
                        else if (data.getString("comp_lbl").equals("Nominal Pencairan")) {
                            editText.setMaxLength(12);
                        }
                        else if (data.getString("comp_lbl").equals("Nominal Beli")) {
                            editText.setMaxLength(12);
                        }
                        else if (data.getString("comp_lbl").equalsIgnoreCase("Masukan Trace Number : ")) {
                            editText.setMaxLength(7);
                        }
                            baseLayout.addView(editText);
                        break;
                    case CommonConfig.ComponentType.PasswordField:
                        EditText pinpadText = null;
                        pinpadText = new EditText(context);
                        pinpadText.init(data);
                        pinpadText.setTag(seq);
                        pinpadText.setLayoutParams(params);
                        if (pinpadText.isNumber()) {
                            pinpadText.setVisibility(GONE);
                        }

                        baseLayout.addView(pinpadText);
                        pinpadTextList.add(pinpadText);
                        pinModuleCounter++;
                        break;
                    case CommonConfig.ComponentType.Button:
                        final String actionURl = comp.has("action_url") ? comp.getString("action_url") : "";
                        Button button = (Button) li.inflate(R.layout.button, null);
                        button.init(data);
                        button.setTag(seq);
                        button.setLayoutParams(params);
                        button.setTag(actionURl);
                        button.setOnClickListener(FormMenu.this);
                        baseLayout.addView(button);
                        break;
                    case CommonConfig.ComponentType.CheckBox:
                        CheckBox checkBox = (CheckBox) li.inflate(R.layout.check_box, null);
                        checkBox.init(data);
                        checkBox.setTag(seq);
                        checkBox.setLayoutParams(params);
                        baseLayout.addView(checkBox);
                        break;
                    case CommonConfig.ComponentType.RadioButton:
                        RadioButton radioButton = (RadioButton) li.inflate(R.layout.radio_button, null);
                        radioButton.setTag(seq);
                        radioButton.init(data);
                        radioButton.setLayoutParams(params);
                        baseLayout.addView(radioButton, seq);
                        break;
                    case CommonConfig.ComponentType.ComboBox:
                        ComboBox comboBox = (ComboBox) li.inflate(R.layout.spinner, null);
                        comboBox.setTag(seq);
                        comboBox.init(data);
                        comboBox.setLayoutParams(params);
                        baseLayout.addView(comboBox, seq);
                        break;
                    case CommonConfig.ComponentType.ChipInsert:
                        ChipInsert chipInsert = (ChipInsert) li.inflate(R.layout.chip_insert, null);
                        chipInsert.setTag(seq);
                        chipInsert.init();
                        chipInsert.setLayoutParams(params);
                        baseLayout.addView(chipInsert, seq);
                        break;
                    case CommonConfig.ComponentType.InsertTap:
                        break;
                    case CommonConfig.ComponentType.MagneticSwipe:
                        magneticSwipe = (MagneticSwipe) li.inflate(R.layout.magnetic_swipe, null);
                        magneticSwipe.init();
                        magneticSwipe.setTag(seq);
                        magneticSwipe.addSwipeListener(this);
                        LayoutInflater li = LayoutInflater.from(context);
                        View promptsView = li.inflate(R.layout.swipe_dialog, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);
                        //alertDialogBuilder.setCancelable(false);
                        // create alert dialog
                        alert = alertDialogBuilder.create();
                        hasMagModule = true;
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(alert.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                        // show it
                        alert.setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                                    Log.d("BACK", "FROM DIALOG");
                                    try {
                                        magneticSwipe.openDriver();
                                        magneticSwipe.closeDriver();
                                        magneticSwipe.setIsQuit(true);
                                        alert.dismiss();
                                        context.onBackPressed();
                                    } catch (Exception e) {
                                        //failed to close, maybe already closed or not open yet
                                    }
                                }
//                                Log.e("key", String.valueOf(event));
                                return true;
                            }
                        });
                        if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                            alert.show();
                            if (SWIPELESS) {
                                magneticSwipe.setText(dummyTrack);

//                                magneticSwipe.setText("5221842001365318=18111260000058300000");
//                                onSwipeComplete(magneticSwipe, "5221842001365318=18111260000058300000");
                            }
                            if (SWIPEANY) {
                                magneticSwipe.setText(dummyTrack);
                            }
                        } else {
//                            magneticSwipe.setText("6013010612793951=17121200000075600000");
                            magneticSwipe.setText(dummyTrack);
                            SWIPELESS = true;
                            SWIPEANY = true;
                        }

                        alert.getWindow().setAttributes(lp);

                        baseLayout.addView(magneticSwipe);
                        magneticSwipe.setVisibility(GONE);
                        break;
                    case CommonConfig.ComponentType.SwipeInsert:
                        break;
                    case CommonConfig.ComponentType.SwipeInsertTap:
                        break;
                    case CommonConfig.ComponentType.SwipeTap:
                        break;
                    case CommonConfig.ComponentType.TapCard:
                        hasTapModule =  true;
                        break;
                }

            }
        }
        if (comp.has("print")) {
            if (!comp.get("print").equals(null)) {
                int print = comp.getInt("print");
//                Log.d(TAG, "SET PRINT " + print);
                if (print > 0) {
                    switch (print) {
                        case 1:
                            printBtn = printView(li);
                            attachPrint();
                            if (countPrintButton > 1) {
                                dettachPrint();
                            }
                            break;
                        case 2:
                            printBtn = printView(li);
                            attachPrint();
                            break;
                        case 3://isi ulang 1x lsg cetak, + 2x print CMB
                            print();
                            printBtn = printView(li);
                            attachPrint();
                            if (countPrintButton > 2) {
                                dettachPrint();
                            }
                            break;
                        case 4://info saldo print 2x optional
                            printBtn = printView(li);
                            attachPrint();
                            if (countPrintButton > 2) {
                                dettachPrint();
                            }
                            break;
                        case 5://trf print 2x wajib
                            for (int i = 0; i < 2; i++) {
                                print();
                            }
                    }
                    if (comp.has("server_ref")) {
                        serverRef = comp.getString("server_ref");
                    }
                    if (comp.has("server_appr")) {
                        serverAppr = comp.getString("server_appr");
                    }
                    if (comp.has("server_date")) {
                        serverDate = comp.getString("server_date");
                    }
                    if (comp.has("server_time")) {
                        serverTime = comp.getString("server_time");
                    }
                    if (comp.has("stan")) {
                        serverStan = comp.getString("server_stan");
                    }
                }
            }
        }
        if (SWIPELESS) {
            if (hasMagModule) {
                onSwipeComplete(magneticSwipe, dummyTrack);
            } else {
                int viscount = 0;
                int visindex = 0;
                for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                    if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {
                        viscount++;
                        visindex = ch;
                    }
                }
                if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {
                    Button proses = (Button) baseLayout.getChildAt(visindex);
                    proses.setVisibility(GONE);
                    if (proses.performClick()) {
                        //ok
                    } else {
                        //at least we tried
                    }
                }
            }
//            Log.d(TAG, "P-Check : " + String.valueOf(viscount) + String.valueOf(visindex));
        } else {
            if (!hasMagModule) {
                int viscount = 0;
                int visindex = 0;
                for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
                    if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE) {
                        viscount++;
                        visindex = ch;
                    }
                }
                if (viscount == 1 && array.getJSONObject(visindex).getInt("comp_type") == CommonConfig.ComponentType.Button) {
                    Button proses = (Button) baseLayout.getChildAt(visindex);
                    proses.setVisibility(GONE);
                    if (proses.performClick()) {
                        //ok
                    } else {
                        //at least we tried
                    }
                }
            }
        }
        if (formId.equals("2A2000F")) {
            print();
            context.onBackPressed();
        }
        else if (comp.has("print") && !formId.equals("DE11") &&
                !formId.equals("DF11") &&
                !formId.equals("DE31") &&
                !formId.equals("220000F") &&
                !formId.equals("5200000") &&
                !formId.equals("5210000") &&
                !formId.equals("521000F") &&
                !formId.equals("5220000") &&
                !formId.equals("5221000") &&
                !formId.equals("522100F") &&
                !formId.equals("5222000") &&
                !formId.equals("522200F") &&
                !formId.equals("5330000") &&
                !formId.equals("2300000") &&
                !formId.equals("2B00000") &&
                !formId.equals("231000F") &&
                !formId.equals("2B0000F") &&
                !formId.equals("9100000") &&
                !formId.equals("910000F") &&
                !formId.equals("9500000") &&
                !formId.equals("950000F") &&
                !formId.equals("5900000") &&
                !formId.equals("590000F") &&
                !formId.equals("5100000") &&
                !formId.equals("5110000") &&
                !formId.equals("511000F") &&
                !formId.equals("5120000") &&
                !formId.equals("5121000") &&
                !formId.equals("5122000") &&
                !formId.equals("512000F") &&
                !formId.equals("5130000") &&
                !formId.equals("513000F") &&
                !formId.equals("5140000") &&
                !formId.equals("514100F") &&
                !formId.equals("514110F") &&
                !formId.equals("5142000") &&
                !formId.equals("514200F") &&
                !formId.equals("512100F")) {
            print();
        }
        focusHandler.postDelayed(delayFocus, 400);
        if (formId.equals("292000F")) {
            dettachPrint();
        }
    }

    private void attachPrint() {
//        Log.d("FORM", "set footer");
        isAntiDDOSPrint = true;
        if (context instanceof ActivityList) {
            ((ActivityList) context).attachFooter(printBtn);
        } else {
            baseLayout.addView(printBtn);
        }
    }

    private void dettachPrint() {
//        Log.d("FORM", "unset footer");
        if (context instanceof ActivityList) {
            ((ActivityList) context).detachFooter();
        } else {
            printBtn.setVisibility(GONE);
        }

    }

    private LinearLayout printView(LayoutInflater li) {
        LinearLayout printConfirmationView = new LinearLayout(context);
        confirmationText = new android.widget.TextView(context);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        printConfirmationView.setOrientation(LinearLayout.VERTICAL);
        printConfirmationView.setLayoutParams(nlp);
        if (isReprint) {
            countPrintButton = 4;
        }
        confirmationText.setText(printConfirm[countPrintButton]);
        if (formId.equals("281000F")) {
            confirmationText.setText("Print Settlement ?");
        }
        if (formId.startsWith("R")) {
            confirmationText.setText("Print Report ?");
        }
        if (formId.equals("71000FF")||formId.equals("721000F")||formId.equals("731000F")) {
            confirmationText.setText(printConfirmTbank[countPrintButton]);
        }
        Button printBtn = (Button) li.inflate(R.layout.button, null);
        printBtn.setText("Ya");
        printBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAntiDDOSPrint) {
                    try {
                        print();
//                    countPrint++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                isAntiDDOSPrint = false;
            }
        });
        Button noBtn = (Button) li.inflate(R.layout.button, null);
        noBtn.setText("Tidak");
        noBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doNotPrint();
            }
        });
        printConfirmationView.addView(confirmationText);
        printConfirmationView.addView(printBtn);
        printConfirmationView.addView(noBtn);
        return printConfirmationView;
    }

    public void printReport(List<PrintSize> data, String ptx) {
        SharedPreferences preferences;
        DataBaseHelper helperDb = new DataBaseHelper(context);
        SQLiteDatabase clientDB = null;
        List<String> mdata = new ArrayList<>();
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            Log.e("TX", "DB error");
        }
                    String getStanSeq = "select seq msgSequence from holder";
//        String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                "from edc_log where date(rqtime) = date('now') and rc = '00' ";
        Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
        int msgStan = 0;
        if (stanSeq!=null) {
            stanSeq.moveToFirst();
            msgStan = stanSeq.getInt(0);
        }
        String batchNo = "";
        if (formId.startsWith("2")) {
            String getBatchNo = "select batch from holder";
            Cursor batchSeq = clientDB.rawQuery(getBatchNo, null);
            int b = 0;
            if (batchSeq!=null) {
                batchSeq.moveToFirst();
                b = batchSeq.getInt(0);
                batchNo = StringLib.fillZero(String.valueOf(b),6);
            }
            batchSeq.close();
            batchSeq = null;
        }
        stanSeq.close();
        stanSeq = null;
        preferences  = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        mdata.add(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
        mdata.add(preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1));
        mdata.add(preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2));
        String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
        String mid = preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID);
        String stan = StringLib.fillZero(String.valueOf(msgStan),6);
        Thread thread = new Thread(new PrintData(data, mdata, tid, mid, stan, ptx,
                countPrintButton, serverRef, serverDate, serverTime, nomorKartu, cardType, batchNo, serverAppr));
        thread.start();
    }

    private void print() throws JSONException {
        SharedPreferences preferences;
        DataBaseHelper helperDb = new DataBaseHelper(context);
        SQLiteDatabase clientDB = null;
        List<PrintSize> data = new ArrayList<>();
        List<String> mdata = new ArrayList<>();
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            Log.e("TX", "DB error");
        }
        String getStanSeq = "select stan from edc_log order by log_id desc";
        Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
        int msgStan = 0;
        if (stanSeq != null) {
            if (stanSeq.moveToFirst()) {
                msgStan = Integer.parseInt(stanSeq.getString(0));
            } else {
                msgStan = 1;
            }
        }
//        Log.d("DEBUG", "STAN : " + msgStan);
        stanSeq.close();

        String batchNo = "";
        if (formId.startsWith("2")) {
            String getBatchNo = "select batch from holder";
            Cursor batchSeq = clientDB.rawQuery(getBatchNo, null);
            int b = 0;
            if (batchSeq != null) {
                if (batchSeq.moveToFirst()) {
                    b = batchSeq.getInt(0);
                    batchNo = StringLib.fillZero(String.valueOf(b), 6);
                }
            }
            batchSeq.close();
            batchSeq = null;
        }
        preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        mdata.add(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
        mdata.add(preferences.getString("merchant_address1", CommonConfig.INIT_MERCHANT_ADDRESS1));
        mdata.add(preferences.getString("merchant_address2", CommonConfig.INIT_MERCHANT_ADDRESS2));
        String tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
        String mid = preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID);
        String stan = "";

        String array_sID[] = {"00001",
                "549410", "54B110", "54A100", "541100", "542110", "542210", "543110", "543210",
                "549110", "514100", "531000", "532110", "532210", "549210", "549310",
                "544100", "544310", "544330", "544410", "544430", "544510", "544530", "544610",
                "545100", "545200", "545300", "545400", "545500", "545600", "570000", "572000",
                "574000", "580000", "544210", "544230", "54C100", "54C200", "54C510", "54C520",
                "54C530", "54C540", "521000", "522100", "522200", "523000", "549500", "547100",
                "547200", "548000", "590000", "543310",

                "710010", "720000", "720010", "730000",

                "610000", "620000", "630000",

                "211000", "220000", "221000",
                // 14032018 #2
//                "230000", "231000",
                "291000", "2A1000", "2B0000", "2B1000", "2D1000", "2A2000",
                "910000", "920000", "930000", "940000"};
        Log.d("PRINT ARRAY", "DILUAR FOR");
        boolean matched_array = false;
        for (int i = 0; i < array_sID.length; i++) {
//            Log.d("LOOP ARRAY",formId +"  "+array_sID[i]);
            if (formId.contains(array_sID[i])) {
                matched_array = true;
//                Log.d("PRINT ARRAY",formId +"  "+array_sID[i]);
                i = array_sID.length;
            }
        }
        if(this.serverStan != null){
            stan = serverStan;
        }
        else if (!matched_array) {
            Log.d("PRINT ARRAY", "Dalam if");
            stan = StringLib.fillZero(String.valueOf(msgStan), 6);
        } else {
            Log.d("PRINT ARRAY", "Dalam else");
            stan = "000000";
        }

        data.add(new PrintSize(FontSize.EMPTY, "\n"));
        // 14032018 #5
        if (magneticSwipe != null) { // && (!formId.equals("640000F"))) {
            String track2Data = magneticSwipe.getText().toString();
            if (!track2Data.equals("")) {
                track2Data = track2Data.split("=")[0];
                track2Data = track2Data.substring(0, 6) + "******" + track2Data.substring(12);
//                data.add(new PrintSize(FontSize.BOLD, "No Kartu : "));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                data.add(new PrintSize(FontSize.BOLD, track2Data + "\n"));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                nomorKartu = "************" + track2Data.substring(12);
                nomorKartu = track2Data;
            }
        }

        // 14032018 #5
//        if (formId.equals("640000F")) {
//            nomorKartu = "";
//        }
        data.add(new PrintSize(FontSize.TITLE, comp.getString("title") + "\n"));
        data.add(new PrintSize(FontSize.EMPTY, "\n"));
        JSONArray array = comp.getJSONObject("comps").getJSONArray("comp");
        for (int i = 0; i < array.length(); i++) {
            for (int j = 0; j < array.length(); j++) {
                final JSONObject dataArr = array.getJSONObject(j);
                int seq = dataArr.getInt("seq");
                if (seq == i) {
                    if (dataArr.isNull("comp_values")) continue;
                    JSONObject val = dataArr.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0);
                    FontSize size = FontSize.NORMAL;
                    String lbl = dataArr.getString("comp_lbl");
                    String value = val.getString("print");
                    if (lbl.startsWith("[")) {
                        String tag = lbl.substring(1, lbl.indexOf("]"));
                        if (tag.matches(".*\\d+.*")) {

                            if (tag.startsWith("B")) {
                                tag = tag.substring(1);
                                tag = "BOLD_" + tag;

                            } else {
                                tag = "NORMAL_" + tag;
                            }
                        } else {

                            if (tag.startsWith("B")) {
                                tag = "BOLD_2";

                            } else {
                                tag = "NORMAL";
                            }
                        }
                        Log.i("zzzz", tag);
                        size = FontSize.valueOf(tag);
                        lbl = lbl.substring(lbl.indexOf("]") + 1);
                    }

                    if (lbl.equals("TRANSAKSI BERHASIL") && formId.equals("721000F")) {
                        data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
                        data.add(new PrintSize(size, lbl));
                        data.add(new PrintSize(size, " "));
                    } else {
                        data.add(new PrintSize(size, lbl));
                        data.add(new PrintSize(size, " "));
                    }

                    if (value.startsWith("[")) {
                        String tag = value.substring(1, value.indexOf("]"));
                        if (tag.matches(".*\\d+.*")) {

                            if (tag.startsWith("B")) {
                                tag = tag.substring(1);
                                tag = "BOLD_" + tag;

                            } else {
                                tag = "NORMAL_" + tag;
                            }
                        } else {

                            if (tag.startsWith("B")) {
                                tag = "BOLD_2";

                            } else {
                                tag = "NORMAL";
                            }
                        }
                        Log.i("zzzz", tag);
                        size = FontSize.valueOf(tag);
                        value = value.substring(value.indexOf("]") + 1);
                    }
                    data.add(new PrintSize(size, value + "\n"));
//                    data.add(new PrintSize(FontSize.EMPTY, "\n"));
                }
            }
        }
//        Thread thread = new Thread(new PrintData(data));
        String ptx = "";
        if (comp.has("print_text")) {
            ptx = comp.getString("print_text");
        }
//            if (serverAppr == null && serverRef!=null) {
//                if (!serverRef.isEmpty() && !serverRef.matches("[0]+")) {
//                    serverAppr = serverRef.replaceFirst("^0+(?!$)", "");
//                }
//            }
//            if (serverAppr.equals("") && serverRef!=null) {
//                if (!serverRef.isEmpty() && !serverRef.matches("[0]+")) {
//                    serverAppr = serverRef.replaceFirst("^0+(?!$)", "");
//                }
//            }
//            if (serverAppr.matches("[0]+") && serverRef!=null) {
//                if (!serverRef.isEmpty() && !serverRef.matches("[0]+")) {
//                    serverAppr = serverRef.replaceFirst("^0+(?!$)", "");
//                }
//            }
        if (countPrintButton < 5) {
            Thread thread = new Thread(new PrintData(data, mdata, tid, mid, stan, ptx,
                    countPrintButton, serverRef, serverDate, serverTime, nomorKartu, cardType, batchNo, serverAppr));
            thread.start();
        }
        countPrintButton++;

        if (countPrintButton < 5) {
            confirmationText.setText(printConfirm[countPrintButton]);
        }

        if (!(formId.equals("71000FF") || formId.equals("721000F") ||
                formId.equals("731000F") || formId.equals("521000F") ||
                ptx.startsWith("STL") || ptx.startsWith("RP"))) {
            confirmationText.setText(printConfirm[countPrintButton]);
        }
        if (formId.equals("71000FF") || formId.equals("721000F") || formId.equals("731000F")) {
            confirmationText.setText(printConfirmTbank[countPrintButton]);
        }
        if (formId.equals("290000F")) {
            confirmationText.setText(printConfirm[countPrintButton]);
        }
        if (formId.equals("521000F") ||
//                formId.equals("220000F")||
                formId.equals("2B0000F") ||
                formId.equals("231000F")) {
//            printBtn.setVisibility(GONE);
            if (clientDB.isOpen()) {
                clientDB.close();
            }
            context.onBackPressed();
        }
        if (ptx.startsWith("STL")) {
//            printBtn.setVisibility(GONE);
            if (clientDB.isOpen()) {
                clientDB.close();
            }
            context.onBackPressed();
        }
        if (ptx.startsWith("RP")) {
            if (clientDB.isOpen()) {
                clientDB.close();
            }
            context.onBackPressed();
        }
//        Log.d(TAG, "PTEXT    : " + ptx);
//        Log.d(TAG, "Count PB : " + String.valueOf(countPrintButton));
//        Log.d(TAG, "PB Label : " + printConfirm[countPrintButton]);
        if (countPrintButton > 2) {
//            printBtn.setVisibility(GONE);
            if (clientDB.isOpen()) {
                clientDB.close();
            }
            context.onBackPressed();
        }
    }

    public void doNotPrint() {
        Log.d("FF KLIK", "Do Not Print");
        String ptx = "";
        if (comp.has("print_text")) {
            try {
                ptx = comp.getString("print_text");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        countPrintButton++;
        if (!(formId.equals("71000FF")||formId.equals("721000F")||formId.equals("731000F")||
                formId.equals("521000F")||ptx.startsWith("STL")||ptx.startsWith("RP"))) {
            confirmationText.setText(printConfirm[countPrintButton]);
        }
        if (formId.equals("71000FF")||formId.equals("721000F")||formId.equals("731000F")) {
            confirmationText.setText(printConfirmTbank[countPrintButton]);
        }
        if (formId.equals("521000F")) {
            context.onBackPressed();
//            printBtn.setVisibility(GONE);
        }
        if (ptx.startsWith("STL")) {
//            printBtn.setVisibility(GONE);
            context.onBackPressed();
        }
        if (ptx.startsWith("RP")) {
            context.onBackPressed();
        }
//        Log.d(TAG, "Count PB : " + String.valueOf(countPrintButton));
        if (countPrintButton>2) {
            context.onBackPressed();
//            printBtn.setVisibility(GONE);
        }
        isAntiDDOSPrint = true;
    }

    @Override
    public void onClick(View v) {
        String serviceId = v.getTag().toString();
        if (serviceId.equals("A56000")&&externalCard) {
            v.setTag("A56100");
        }
        if (pinModuleCounter<1) {
            try {
                actionUrl((Button) v, v.getTag().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (formId.equals("5900000")) {
            showChangePinDialog(v);
        } else {
            for (int w=0;w<pinModuleCounter;w++) {
//                showPinDialog(v);
                showChangePinDialog(v);
                Log.e("PINPAD", "Create Dialog" + String.valueOf(w));
            }
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

    private class PostData extends AsyncTask<String, Void, JSONObject> {
        private txHandler txh;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(context, "Silahkan Tunggu....", "Data sedang dikirim", true);
//            Log.d("INFO_FORM", "Silahkan Tunggu....");
        }

        @Override
        protected JSONObject doInBackground(String[] params) {
            txh = txHandler.getInstance();
            try {
//                Log.d("KIRIM", params[0].toString());
                txh.setContext(context);
                JSONObject scret = txh.processTransaction(context, params[0]);
                if (scret.has("reprint")) {
                    isReprint = true;
                    reprintTrace = scret.getString("rstan");
                } else {
                    isReprint = false;
                }
                if (scret.has("server_ref")) {
                    serverRef = scret.getString("server_ref");
                }
                if (scret.has("server_date")) {
                    serverDate = scret.getString("server_date");
                }
                if (scret.has("server_time")) {
                    serverTime = scret.getString("server_time");
                }
                if (scret.has("card_type")) {
                    cardType = scret.getString("card_type");
                }
                if (scret.has("server_appr")) {
                    serverAppr = scret.getString("server_appr");
                }
                if (scret.has("nomor_kartu")) {
                    nomorKartu = scret.getString("nomor_kartu");
                    //Kondisi jika BRIZZI CARD (FLY), nomor kartu tanpa bintang
                    if (cardType.equals("DEBIT (SWIPE)")) {
                        nomorKartu = nomorKartu.split("=")[0];
                        nomorKartu = nomorKartu.substring(0, 6) + "******" + nomorKartu.substring(12);
                    }
                }
//                if(isReprint && cardType.equals("SMART CARD (FLY)")){
//                    nomorKartu = "";
//                }
                JSONObject retScreen = scret.getJSONObject("screen");
                return retScreen;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject responObj) {
            try {
//                Log.d("NAHA KADIEU", responObj.toString());
                if (txh.isHasPrintData()) {
                    printReport(txh.getPrintData(), txh.getPrintText());
                }
                JsonCompHandler.saveJson(context, responObj);
                int type = responObj.getInt("type");
                if (type == CommonConfig.MenuType.Form) {
                    comp = responObj;
                    init();
                } else if (type == CommonConfig.MenuType.PopupGagal) {
                    JSONObject val = responObj
                            .getJSONObject("comps")
                            .getJSONArray("comp").getJSONObject(0)
                            .getJSONObject("comp_values")
                            .getJSONArray("comp_value").getJSONObject(0);
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(responObj.getString("title"));
                    if (val.has("value")) {
                        alertDialog.setMessage(val.getString("value"));
                    } else {
                        alertDialog.setMessage("Error belum terdefinisi");
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    context.onBackPressed();
                                    context.finish();
                                }
                            });
                    alertDialog.show();
                } else if (type == CommonConfig.MenuType.PopupBerhasil) {
                    JSONObject val = responObj
                            .getJSONObject("comps")
                            .getJSONArray("comp").getJSONObject(0)
                            .getJSONObject("comp_values")
                            .getJSONArray("comp_value").getJSONObject(0);
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(responObj.getString("title"));
                    if (val.has("value")) {
                        alertDialog.setMessage(val.getString("value"));
                    } else {
                        alertDialog.setMessage("Error belum terdefinisi");
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (screenLoader.equals("2100000")) {
                                        context.onBackPressed();
                                    } else {
                                        Intent myIntent = new Intent(context, MainActivity.class);
                                        context.startActivityForResult(myIntent, 0);
                                    }
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class PrintData implements Runnable {
        private List<PrintSize> data;
        private List<String> mdata;
        private String tid;
        private String mid;
        private String stan;
        private boolean isStl;
        private boolean isReport;
        private boolean isDetail;
        private int countPrint;
        private String svrAppr;
        private String svrRef;
        private String svrDate;
        private String svrTime;
        private String reportDate;
        private String nomorKartu;
        private String jenisKartu;
        private String batchNumber;

        public PrintData(List<PrintSize> data, List<String> mdata, String tid, String mid,
                         String stan, String wf, int countPrint, String svrRef, String svrDate,
                         String svrTime, String nomorKartu, String cardType, String batch, String appr) {


            this.isStl = false;
            this.isReport = false;
            this.isDetail = false;
            this.reportDate = "";
            if (wf.equals("WF")) {
                ///
                if (formId.equals("71000FF") || formId.equals("721000F")){
                    data = addTbankFooter(data);
                }
                else{
                    data = addStandardFooter(data);
                }
            } else if (wf.equals("PF")) {
                data = addPulsaFooter(data);
            } else if (wf.equals("STL")) {
                this.isStl = true;
                data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                data.add(new PrintSize(FontSize.NORMAL, "Settlement BERHASIL\n"));
                data.add(new PrintSize(FontSize.EMPTY, "\n"));
                data = addReportFooter(data);
            } else if (wf.startsWith("RPT")) {
                this.isReport = true;
                if (wf.length()>3) {
                    this.reportDate = wf.substring(3);
                }
                data = addReportFooter(data);
            } else if (wf.startsWith("RPD")) {
                this.isDetail = true;
                if (wf.length()>3) {
                    this.reportDate = wf.substring(3);
                }
                data = addReportFooter(data);
            }
            this.data = data;
            this.mdata = mdata;
            this.tid = tid;
            this.mid = mid;
            this.stan = stan;
            this.countPrint = countPrint;
            this.nomorKartu = nomorKartu;
//            Log.d("PRINT INIT", "card number : " + nomorKartu);
            this.jenisKartu = cardType;
            if (svrRef!=null) {
                this.svrRef = svrRef;
            } else {
                this.svrRef = "000000000000";
            }
            if (svrDate!=null) {
                this.svrDate = svrDate;
            } else {
                this.svrDate = "0";
            }
            if (svrTime!=null) {
                this.svrTime = svrTime;
            } else {
                this.svrTime = "0";
            }
            if (appr!=null) {
                this.svrAppr = appr;
            } else {
                this.svrAppr = "00000000";
            }
            this.batchNumber = StringLib.fillZero(String.valueOf(batch),6);
        }

        public List<PrintSize> addTbankFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017 atau 1500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017, 021-500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "atau 021-57987400\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addStandardFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017 atau 1500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017, 021-500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "atau 021-57987400\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addPulsaFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Pulsa Otomatis Bertambah\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017 atau 1500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017, 021-500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "atau 021-57987400\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        public List<PrintSize> addReportFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017 atau 1500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017, 021-500017,\n"));
//            data.add(new PrintSize(FontSize.NORMAL, "atau 021-57987400\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        @Override
        public void run() {
            while (printInUse) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Log.e("PRINT", "CANCELED");
                    return;
                }
            }
            printInUse = true;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            AssetManager assetManager = context.getAssets();
            String bmp_path = "bri-small.jpg";
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open(bmp_path);
            } catch (IOException e) {
                Log.e("PRINT", "CANNOT OPEN BITMAP");
            }
//            Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/Pictures/bri-small.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (isReprint) {
                countPrint = 4;
                stan = reprintTrace;
            }
            if (isStl) {
                ESCPOSApi.printSettlement(bitmap, data, mdata, tid, mid, stan, svrDate, svrTime);
            } else if(isReport) {
                ESCPOSApi.printReport(bitmap, data, mdata, tid, mid, reportDate);
            } else if (isDetail) {
                ESCPOSApi.printDetailReport(bitmap, data, mdata, tid, mid, reportDate);
            } else {
                if (tid != null) {
//                    Log.d(TAG, "Count Print : " + String.valueOf(countPrint));
//                    Log.d(TAG, "Start Print @"+svrTime + " " + svrDate);
                    String cardType = "DEBIT (SWIPE)";
                    if (!jenisKartu.equals("")) {
                        cardType = jenisKartu;
                    }
                    // 14032018 #7
                    if (formId.equals("71000FF") || formId.equals("721000F")){
                        cardType = "";
                        nomorKartu = "99999******99999";
                    }
                    if (formId.equals("270000F") || formId.equals("2C1000F")){
                        cardType = "BRIZZI CARD (FLY)";
                    }
                    if (formId.equals("250000F")){
                        cardType = "DEBIT (SWIPE)";
                    }
                    ESCPOSApi.printStruk(bitmap, data, mdata, tid, mid, stan, countPrint,
                            svrRef, svrDate, svrTime, cardType, nomorKartu, formId, batchNumber, svrAppr);
                } else {
                    ESCPOSApi.printStruk(bitmap, data);
                }
            }
            countPrint++;
//            Log.d("PRINT", "FINISHED");
            printInUse = false;
            isAntiDDOSPrint = true;
        }
    }

    class ReadPINThread implements Runnable {
        private final Handler handler;

        public ReadPINThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            boolean pinpadReady = false;
            int pinpadRetryCounter = 0;
            final StringBuilder pinpad = new StringBuilder();
            while (!pinpadReady&&pinpadRetryCounter<5) {
                close();

                open();
                pinblockHolder = "";
                int result = PINPadInterface.setKey(2, 0, 0, PINPadInterface.ALGORITH_DES);
                Log.e(TAG, "setKey result = " + result);
                if (result < 0) {
//                return;
                } else {
                    pinpadReady = true;
                }
                pinpadRetryCounter++;
            }
            if (!pinpadReady) {
                Log.e("PINPAD", "Cannot start");
                context.finish();
            }
            PINPadInterface.setupCallbackHandler(new PinPadCallbackHandler() {

                @Override
                public void processCallback(byte[] data) {
//                    Log.d(TAG, "data = " + data[0] + " || " + data[1]);
//                    Log.d(TAG, StringUtility.ByteArrayToString(data, data.length));
                    pinpad.append("*");
//                    Log.d("PINPAD_VIEW", pinpad.toString());
                    Message m = new Message();
                    Bundle b = new Bundle();
                    b.putString("key", pinpad.toString());
                    b.putByteArray("data", data);
                    m.setData(b);
                    handler.sendMessage(m);
                }
            });
            String pan = "123456789012345678";
            if (!panHolder.equals("")) {
                pan = panHolder;
            }
            if (formId.equals("7100000")||formId.equals("7300000")) {
                pan = "9999999999999999";
            }
            byte[] pinBlock = new byte[8];
            int ret = PINPadInterface.setPinLength(6,0);
            String strShow = "";
//            ((ActivityList) context).setPinpadInUse(true);
            while (strShow.equals("")&&!Thread.currentThread().isInterrupted()) {
                ret = PINPadInterface.inputPIN(pan.getBytes(), pan.length(), pinBlock, -1, 0);
                if (ret>-1) {
                    strShow = StringUtility.ByteArrayToString(pinBlock, ret);
                }
                Log.i("PP", "meanwhile..");
            }

//            ((ActivityList) context).setPinpadInUse(false);
//            Log.d(TAG, "inputPIN result = " + ret);
//            Log.d(TAG, "PINBlock is " + strShow);
            if (!Thread.currentThread().isInterrupted()) {
                Log.i("PP", "no interupted");
                Message m = new Message();
                Bundle b = new Bundle();
                byte[] data = null;
                b.putString("key", pinpad.toString());
                b.putByteArray("data", data);
                m.setData(b);
                close();
                pinblockHolder = strShow;
                handler.sendMessage(m);
            } else {
                Log.i("PP", "interupted");
            }

        }

    }

    public void reFocus() {
        focusHasSets = false;
        for (int ch = 0; ch < baseLayout.getChildCount(); ch++) {
            if (baseLayout.getChildAt(ch).getVisibility() == VISIBLE &&
                    baseLayout.getChildAt(ch) instanceof EditText) {
                if (!focusHasSets) {
                    focusHasSets = true;
                    baseLayout.getChildAt(ch).requestFocus();
                    Log.i("FOCUS", baseLayout.getChildAt(ch).toString() + " had focus is #" + ch);
                }
            }
        }
    }

    Runnable delayFocus = new Runnable() {
        @Override
        public void run() {
            try {
//                while (((ActivityList) context).isPinpadInUse()) {
//                    Thread.sleep(1000);
//                }
                reFocus();
            } catch (Exception e) {
                //pass
            }
        }
    };

}

