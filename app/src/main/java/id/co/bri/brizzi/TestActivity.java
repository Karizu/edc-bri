package id.co.bri.brizzi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.wizarpos.apidemo.contactlesscard.ContactlessControler;
import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.printer.ESCPOSApi;
import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.apidemo.smartcard.SmartCardController;
import com.wizarpos.jni.PINPadInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.handler.ConAsync;
import id.co.bri.brizzi.module.CardData;
import id.co.bri.brizzi.module.listener.ReqListener;

public class TestActivity extends Activity implements ReqListener {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private ConAsync con;
    public final String TAG = "BRIZZI";
    private Button btnPrintImage, btnPrintStruk, btnTap, btnSmartCard;
    private ContactlessControler cc;
    private CardData cData = new CardData();
    private AlertDialog alertTap;
    private AlertDialog dialogChoice;
    private ListAdapter brizziChoice;
    private TextView alertMessage;
    private List<String> pilihan = new ArrayList<>(Arrays.asList("CEK SALDO", "TOPUP", "GET KEY", "PEMBAYARAN_NORMAL", "AKTIF"));
    private SmartCardController smc;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int nEventID = bundle.getInt("nEventID");
            int dataEvenLeng = bundle.getInt("nEventDataLength");
            byte[] dataByteArray = bundle.getByteArray("arryEventData");
            String uid = bundle.getString("uid");
            cData.setUid(uid);
            Log.i(TAG, "TAP PLEASE");
            if (cData.getWhatToDo().equals("CekSaldo")) {
                cekSaldo();
            } else if (cData.getWhatToDo().equals("Topup")) {
                cData.setTopupAmount("25000");
                topup();
            } else if (cData.getWhatToDo().equals("Getkey")) {
                getKey();
            } else if (cData.getWhatToDo().equals("Deduct")) {
                deduct();
            } else if (cData.getWhatToDo().equals("AKTIF")) {
                aktif();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        btnPrintImage = (Button) findViewById(R.id.btnPrint);
//        btnPrintImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                printImage(v);
//            }
//        });
//        btnPrintStruk = (Button) findViewById(R.id.btnPrintStruk);
//        btnPrintStruk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                printStruk(v);
//            }
//        });
        btnTap = (Button) findViewById(R.id.btn_tap);
        btnTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTap(v);
            }
        });
//        btnSmartCard = (Button) findViewById(R.id.btnSmartCard);
//        btnSmartCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                doSmartCard(v);
//            }
//        });
//        cc = new ContactlessControler(handler, getApplication());
//        smc = new SmartCardController(this);
//
//        brizziChoice = new ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice,pilihan);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        cData.setWhatToDo("Topup");
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.swipe_dialog, null);
        alertMessage = (TextView) promptsView.findViewById(R.id.pinPass);
        alertMessage.setText("SILAHKAN TAP KARTU ANDA");
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        // set prompts.xml to alertdialog builder
//        alertDialogBuilder.setView(promptsView);
//        //alertDialogBuilder.setCancelable(false);
//        // create alert dialog
//        alertTap = alertDialogBuilder.create();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(alertTap.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        // show it
        showPinDialog();

    }

    private int pinDialogCounter = 0;
    private int pinModuleCounter = 0;
    private id.co.bri.brizzi.module.EditText pinpadText;
    private List pinpadTextList = new ArrayList();
    private String pinblockHolder;
    private int pinDialogCloseCounter = 0;

    private void showPinDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.pinpad_dialog, null);
        final android.widget.TextView alertText = (android.widget.TextView) promptsView.findViewById(R.id.pinAlert);
        alertText.setVisibility(View.GONE);
        pinDialogCounter++;
        String passHint = "";
        if (pinModuleCounter > 1) {
            pinpadText = (id.co.bri.brizzi.module.EditText) pinpadTextList.get(pinModuleCounter - pinDialogCounter);
            final android.widget.TextView passLabel = (android.widget.TextView) promptsView.findViewById(R.id.pinPass);
            passLabel.setText(pinpadText.getHint());
            passHint = pinpadText.getHint().toString();
        }
        final String superPassHint = passHint;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final android.widget.EditText userInput = (android.widget.EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        final android.widget.Button btnOk = (android.widget.Button) promptsView
                .findViewById(R.id.btnOk);

//        userInput.setKeyListener(null);
        // set dialog message
        if (!CommonConfig.getDeviceName().equals("LGE Nexus 5")) {
            userInput.setInputType(InputType.TYPE_NULL);
        } else {
            userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        userInput.setTransformationMethod(new id.co.bri.brizzi.module.EditText.MyPasswordTransformationMethod());
        alertDialogBuilder
                .setCancelable(false);


        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        final id.co.bri.brizzi.module.Button realProcess = (id.co.bri.brizzi.module.Button) v;

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pinpadTextList.size() >= 0) {
                    if (userInput.length() < 5) {
                        userInput.setText("");
                        alertText.setVisibility(View.VISIBLE);
                        userInput.requestFocus();
                        return;
                    }
//                        String encPins = encPinblock(userInput.getText().toString());
                    String encPin = pinblockHolder;
                    Log.e("PINPAD", encPin);
                    Log.e("PINPAD", "Hint : " + superPassHint);
                    if (pinDialogCloseCounter > 0) {
                        encPin = userInput.getText().toString();
                    }
//                    pinpadText = (id.co.bri.brizzi.module.EditText) pinpadTextList.get(pinDialogCloseCounter);
//                    pinpadText.setText(encPin);
                    pinDialogCloseCounter++;
                    alertDialog.dismiss();
                    Log.e("PINPAD", String.valueOf(pinModuleCounter));
                    if (pinModuleCounter == 1) {

                    } else {
                        pinModuleCounter--;
                    }
                }

            }
        });
        // show it
        alertDialog.show();
        alertDialog.getWindow().setAttributes(lp);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                byte[] data = msg.getData().getByteArray("data");
                Log.e("PINPAD_VIEW", "data = " + data[0] + " || " + data[1]);
                Log.i("PINPAD_VIEW", msg.getData().getString("key") + " ASD");
                userInput.setText(msg.getData().getString("key"));
            }
        };
//        new Thread(new ReadPINThread(handler)).start();
    }

    private boolean isOpened = false;

    //    private boolean
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


//    class ReadPINThread implements Runnable {
//        private final Handler handlers;
//
//        public ReadPINThread(Handler handler) {
//            this.handlers = handler;
//        }
//
//        @Override
//        public void run() {
//
//            open();
//            final StringBuilder pinpad = new StringBuilder();
//            pinblockHolder = "";
//            int result = PINPadInterface.setKey(2, 0, 0, PINPadInterface.ALGORITH_DES);
//            Log.e(TAG, "setKey result = " + result);
//            if (result < 0) {
//                return;
//            }
//
//            PINPadInterface.setupCallbackHandler(new PinPadCallbackHandler() {
//
//                @Override
//                public void processCallback(byte[] data) {
//                    Log.e(TAG, "data = " + data[0] + " || " + data[1]);
//                    pinpad.append("*");
////                    Log.i("PINPAD_VIEW", pinpad.toString());
//                    Message m = new Message();
//                    Bundle b = new Bundle();
//                    b.putByteArray("data",data);
//                    b.putString("key", pinpad.toString());
//                    m.setData(b);
//                    handlers.sendMessage(m);
//                }
//            });
//            String pan = "5221842001365318";
//
//            Log.i("PAN",pan);
//            byte[] pinBlock = new byte[8];
//            int ret = PINPadInterface.inputPIN(pan.getBytes(), pan.length(), pinBlock, -1, 0);
//            Log.e(TAG, "inputPIN result = " + ret);
//            String strShow = StringUtility.ByteArrayToString(pinBlock, ret);
//
//
//            Log.e(TAG, "PINBlock is " + strShow);
//            pinblockHolder = strShow;
//            close();
//
//        }
//    }

    @Override
    protected void onDestroy() {
        cc.dettatch();
        smc.closedevice();
        super.onDestroy();

    }

    private void printStruk(View v) {
        if (false) {
            PrinterInterface.PrinterEnd();
            PrinterInterface.PrinterClose();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/Pictures/bri-small.jpg");
        List<PrintSize> data = new ArrayList<>();
        data.add(new PrintSize(FontSize.NORMAL, "Informasi Saldo" + "\n"));
        data.add(new PrintSize(FontSize.EMPTY, "\n"));
        FontSize size = FontSize.NORMAL;
        String lbl = "Nama : ";
        String value = "[2]Muhammad Indra";
        if (lbl.startsWith("[")) {
            String tag = lbl.substring(1, lbl.indexOf("]"));
            if (tag.matches(".*\\d+.*")) {

                if (tag.startsWith("B")) {
                    tag = tag.substring(1);
                    tag = "BOLD_" + tag;

                } else {
                    tag = "NORMAL_" + tag.substring(1);
                }
            } else {

                if (tag.startsWith("B")) {
                    tag = "BOLD";

                } else {
                    tag = "NORMAL";
                }
            }
            Log.i("zzzz", tag);
            size = FontSize.valueOf(tag);
            lbl = lbl.substring(lbl.indexOf("]") + 1);
        }
        data.add(new PrintSize(size, lbl));
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
                    tag = "BOLD";

                } else {
                    tag = "NORMAL";
                }
            }
            Log.i("zzzz", tag);
            size = FontSize.valueOf(tag);
            value = value.substring(value.indexOf("]") + 1);
        }
        data.add(new PrintSize(size, value + "\n"));
        data.add(new PrintSize(FontSize.EMPTY, "\n"));

        size = FontSize.NORMAL;
        lbl = "[3]Sisa Saldo : ";
        value = "Rp. 1.234.567";
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
                    tag = "BOLD";

                } else {
                    tag = "NORMAL";
                }
            }
            Log.i("zzzz", tag);
            size = FontSize.valueOf(tag);
            lbl = lbl.substring(lbl.indexOf("]") + 1);
        }
        data.add(new PrintSize(size, lbl));
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
                    tag = "BOLD";

                } else {
                    tag = "NORMAL";
                }
            }
            Log.i("zzzz", tag);
            size = FontSize.valueOf(tag);
            value = value.substring(value.indexOf("]") + 1);
        }
        data.add(new PrintSize(size, value + "\n"));
        data.add(new PrintSize(FontSize.EMPTY, "\n"));
        ESCPOSApi.printStruk(bitmap, data);
    }

    @Override
    public void onReqCompleted(String result) {
        Log.i("BRIZZI", "HOST RESPONSE " + result);
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONObject("screen").getJSONObject("comps").getJSONArray("comp");

            String resp = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
            String date = arr.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
            Log.i(TAG, "HOST RESPONSE DATE " + date);
            String time = arr.getJSONObject(3).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
            Log.i(TAG, "HOST RESPONSE TIME " + time);
            cData.setHostResponse(resp);
            cData.settTime(time);
            cData.settDate("150116");
            nextTopupStep();
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onNoInternetConnection() {

    }

    public static class MyPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }

            public char charAt(int index) {
                return '*'; // This is the important part
            }

            public int length() {
                return mSource.length(); // Return default
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }

    private void printImage(View v) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.pinpad_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        final Button btnOk = (Button) promptsView
                .findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("PINPADS", userInput.getText().toString());
            }
        });
//        userInput.setKeyListener(null);
        // set dialog message
        userInput.setInputType(InputType.TYPE_NULL);
        userInput.setTransformationMethod(new MyPasswordTransformationMethod());
        alertDialogBuilder
                .setCancelable(false);


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        // show it
        alertDialog.show();
        alertDialog.getWindow().setAttributes(lp);
    }

    public void cekSaldo() {
        Log.i(TAG, "CEK SALDO");
        String CardResponse = "";
        String SamResponse = "";
        //2. Card � Select AID 1
        String aid = smc.sendCmd(hexStringToByteArray("00A4040C09A00000000000000011"));
        if (aid.equals("9000")) {
            CardResponse = cc.transmitCmd("5A010000");
            Log.d(TAG, "cmd:5A010000 || " + CardResponse);
            // 3. Card � Get Card Number
            CardResponse = cc.transmitCmd("BD00000000170000");
            String CardNumber = CardResponse.substring(8, 8 + 16);
            cData.setCardNumber(CardNumber);
            Log.d(TAG, "cmd:BD00000000170000 || " + CardResponse + " CardNumber:" + CardNumber);
            // 4. Card � Get Card Status
            CardResponse = cc.transmitCmd("BD01000000200000").substring(8, 12);
            Log.d(TAG, "cmd:BD0100000200000 || " + CardResponse);
            // 5. Card � Select AID 3
            CardResponse = cc.transmitCmd("5A030000");
            Log.d(TAG, "cmd: 5A030000 || " + CardResponse);
            // 6. Card � Request Key Card
            CardResponse = cc.transmitCmd("0A00");
            String Keycard = CardResponse.substring(2);
            Log.d(TAG, "cmd: 0A00 || " + Keycard);
            // 7. Card � Get UID
            Log.d(TAG, "UID || " + cData.getUid());

            SamResponse = smc.sendCmd(hexStringToByteArray("80B0000020" + CardNumber + cData.getUid() + "FF0000030080000000" + Keycard));
            if (!SamResponse.startsWith("6D")) {
                Log.d(TAG, "SAM Authenticate Key : " + SamResponse);
                String RandomKey16B = SamResponse.substring(SamResponse.length() - 36);
                RandomKey16B = RandomKey16B.substring(0, RandomKey16B.length() - 4);
                Log.d(TAG, "Randomkey16B : " + RandomKey16B);
                // 9. Card � Authenticate Card
                CardResponse = cc.transmitCmd("AF" + RandomKey16B);
                String RandomNumber8B = CardResponse.substring(2);
                cData.setRandomNumber8B(RandomNumber8B);
                Log.d(TAG, "cmd: AF+" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
                //  10. Card � Get Last Transaction Date
                CardResponse = cc.transmitCmd("BD03000000070000");
                Log.d(TAG, "cmd: BD03000000070000 || " + CardResponse + " || last trans: " + cData.getLastTransDate() + " akundebet: " + cData.getAkumDebet());
                cData.setLastTransDate(CardResponse.substring(2, 8));
                cData.setAkumDebet(CardResponse.substring(8, 16));
                // 11. Card � Get Balance
                CardResponse = cc.transmitCmd("6C00");
                cData.setCardBalance4B(CardResponse.substring(2));
                cData.setCardBalanceInt(HtoI(CardResponse.substring(2)));
                Log.d(TAG, "cmd: 6C00 || Balance:  " + cData.getCardBalance4B());
                Log.d(TAG, "Balance integer: " + HtoI(cData.getCardBalance4B()));
                StringBuilder sb = new StringBuilder();
                sb.append("NOMOR KARTU ANDA =" + cData.getCardNumber());
                sb.append("\n");
//                sb.append("SISA SALDO ANDA Rp. "+HtoI(cData.getCardBalance4B()));
                alertMessage.setText(sb.toString());
            } else {
                Log.e(TAG, "SAM AUTH ERROR : " + SamResponse);
            }
        } else {
            Log.e(TAG, "AID : " + SamResponse);
        }
    }

    private int choice = -1;

    private void doTap(View v) {
        dialogChoice = new AlertDialog.Builder(TestActivity.this)
                .setSingleChoiceItems(brizziChoice, 0, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        smc.starting(1);
                        cc.searchBegin();
//                        alertTap.show();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        Log.i(TAG, "IS " + selectedPosition);
                        switch (selectedPosition) {
                            case 0:
                                Log.i(TAG, "CEK SALDO SELECTED");
                                cData.setWhatToDo("CekSaldo");
                                break;
                            case 1:
                                Log.i(TAG, "TOPUP SELECTED");
                                cData.setWhatToDo("Topup");
                                cData.settDate("150116");
                                cData.settTime("161616");
                                break;
                            case 2:
                                Log.i(TAG, "GET KEY SELECTED");
                                cData.setWhatToDo("Getkey");
                                break;
                            case 3:
                                Log.i(TAG, "PEMBAYARAN_NORMAL SELECTED");
                                cData.setWhatToDo("Deduct");
                                cData.setDeductAmount("1");
                                cData.settDate("150116");
                                cData.settTime("104635");
                                break;
                            case 4:
                                Log.i(TAG, "GET KEY SELECTED");
                                cData.setWhatToDo("AKTIF");
                                break;
                        }
                    }
                })
                .show();


    }

    private void getKey() {
        String CardResponse = cc.transmitCmd("5A030000");
        if (!CardResponse.startsWith("00")) {
            Log.e(TAG, "Select AID 3 error " + CardResponse);
            return;
        }
        Log.d(TAG, "cmd: 5A030000" + " || response: " + CardResponse);

        CardResponse = cc.transmitCmd("0A00");
        Log.d(TAG, "cmd: 0A01" + " || response: " + CardResponse);

//        CardResponse = cc.transmitCmd("AF");
//        Log.d(TAG, "cmd: AF" + " || response: " + CardResponse);

    }

    private void topup() {
        Log.i(TAG, "TOPUP");
        String CardResponse = "";
        String SamResponse = "";
        cekSaldo();
        // 12. SAM � Get Key Topup
        SamResponse = smc.sendCmd(hexStringToByteArray("80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000"));
        String RandomSam24B = SamResponse.substring(2, SamResponse.length() - 4);
        Log.d(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + RandomSam24B);
        // 13. HOST Get Auth Key Topup
        sendToServer(RandomSam24B + "00000" + cData.getTopupAmount());

//        Log.d(TAG, "cmd: server Reply" + CardResponse + " || keyTopup24B:  " + keyTopup24B + "|  ");

    }

    private void nextTopupStep() {
        Log.d(TAG, "NEXT TOPUP STEP, HOST RESPONSE: " + cData.getHostResponse());
        String keyTopup24B = cData.getHostResponse();
        if (!(keyTopup24B.length() == 48)) {
            Log.e(TAG, "Error Response from server !!" + " || response: " + keyTopup24B);
            return;
        }
        // 14. Card � Select AID 3
        String CardResponse = cc.transmitCmd("5A030000");
        if (!CardResponse.startsWith("00")) {
            Log.e(TAG, "Select AID 3 error " + CardResponse);
            return;
        }
        Log.d(TAG, "cmd: 5A030000" + " || response: " + CardResponse);
        //15. Card � Request Key Card 01
        CardResponse = cc.transmitCmd("0A01");
//        if(!CardResponse.startsWith("00")){
//            Log.e(TAG, "Request Key Card 01 error " + CardResponse);
//            return;
//        }


        Log.d(TAG, "cmd: 0A01" + " || response: " + CardResponse);
        int i = 0;
//        while(!CardResponse.startsWith("00")){
//            CardResponse = cc.transmitCmd(CardResponse);
//            Log.i(TAG,"AF RESPONSE "+CardResponse);
//            if(i == 20){
//                break;
//            }
//            i++;
//        }
        String keyCard08B = CardResponse.substring(2);
        // 16. SAM � Authenticate Topup
        String sendtosam = "80B2000037" + keyTopup24B + cData.getCardNumber() + cData.getUid() + "0000030180000000" + keyCard08B;
        String SamResponse = smc.sendCmd(hexStringToByteArray(sendtosam));
        if (!SamResponse.endsWith("9000")) {
            Log.e(TAG, "Sam Authenticate Topup error " + SamResponse);
            return;
        }
        Log.d(TAG, "cmd: Authenticate Topup || response: " + SamResponse);
        String RandomKey16B = SamResponse.substring(34, SamResponse.length() - 4);
        Log.d(TAG, "cmd: RandomKey16B || send: AF" + RandomKey16B);
        // 17. Card � Authenticate Card
        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
        if (!CardResponse.startsWith("00")) {
            Log.e(TAG, "Authenticate Card error " + CardResponse);
            return;
        }
        String RandomNumber8B = CardResponse.substring(2);
        Log.d(TAG, "cmd: AF" + RandomKey16B + " || response: " + CardResponse);
        // 18. Card � Credit Balance
        String topupAmount = ItoH(cData.getTopupAmount()); //--- nilai yg akan di topup
        String transmit = "0C00" + topupAmount + "00";
        CardResponse = cc.transmitCmd(transmit);
        if (!CardResponse.startsWith("00")) {
            Log.e(TAG, "Credit Balance error " + CardResponse);
            return;
        }
        String CardBalance4B = CardResponse.substring(2);
        Log.d(TAG, "cmd: " + transmit + " || response: " + CardResponse + "| Card Balance = " + CardBalance4B);
//        if (CardResponse.length() < 10) {
//            Log.e(TAG, "Credit Balance Error "+ CardResponse);
//            return;
//        }
        // 19. SAM � Create Hash

        String transactionData = Hex3(cData.getCardNumber()) + Hex3(nominalTransaksi(cData.getTopupAmount())) + Hex3(cData.gettDate()) + Hex3(cData.gettTime()) + Hex3("818001") + Hex3("000036") + Hex3("03") + "FFFFFFFF";

//        sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000" + RandomNumber8B + transactionData;
//        Log.i(TAG,"SEMD TO SAM "+sendtosam);
//        SamResponse = smc.sendCmd(hexStringToByteArray(sendtosam));
//        Log.i(TAG,"SAM – Create Hash "+SamResponse);
//        String Hash4B = SamResponse.substring(0, SamResponse.length() - 4);
//        if (!SamResponse.endsWith("9000")) {
//            Log.e(TAG, "SAM – Create Hash error " + SamResponse);
//            Log.e(TAG, "Hash "+ Hash4B);
//            return;
//        }
//        cData.setHash4BTopup(Hash4B);
        // 20. Card � Write Log
        String balanceBeforeint = HtoI(cData.getCardBalance4B());
        Log.d(TAG, "balanceBefore3B step 1 : " + balanceBeforeint);
        int bAfter = Integer.parseInt(balanceBeforeint) - Integer.parseInt(cData.getTopupAmount());
        Log.d(TAG, "BalanceAfter int : " + bAfter);
        String bAfter3B = ItoH(Integer.toString(bAfter));
        Log.d(TAG, "BalanceAfter3B : " + bAfter3B);
        String balanceBefore3B = ItoH(balanceBeforeint);
        Log.d(TAG, "balanceBefore3B step 2 : " + balanceBefore3B);
//        String cmdWritelog = "3B01000000200000" + cData.getMerchanID() + cData.getMerchanID() + cData.gettDate() + cData.gettTime() + "EB" + ItoH(cData.getTopupAmount()) + balanceBefore3B + bAfter3B;
//        CardResponse = cc.transmitCmd(cmdWritelog);
//        Log.d(TAG, "CardResponse : " + CardResponse);
//        if(CardResponse.startsWith("AF")){
//            CardResponse = cc.transmitCmd("AF");
//            Log.d(TAG, "CardResponse after AF : " + CardResponse);
//        }
//
//        if (!(CardResponse.equals("00"))) {
//            Log.e(TAG, "Write log error " + CardResponse);
////            return;
//        }
//        // 21. Card � Write Last transaction
////		String lastTransMonth = cData.getLastTransDate().substring(0,4);
////		String nowMonth = cData.gettDate().substring(0,4);
//        String akumdebet = "00000000";
//        if (cData.getLastTransDate().substring(0, 4).equals(cData.gettDate().substring(0, 4))) {
//            akumdebet = Integer.toString(Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getTopupAmount()));
//        } else {
//            akumdebet = cData.getTopupAmount();
//        }
//        akumdebet = this.nominalTransaksi(akumdebet);
//        Log.d(TAG, "akumdebet : " + akumdebet);
//        CardResponse = cc.transmitCmd("3D03000000070000" + cData.gettDate() + akumdebet);
//
//        if (!(CardResponse.equals("00"))) {
//            Log.e(TAG, "Write Last transaction error " + CardResponse);
////            return;
//        }
//        Log.d(TAG, "CardResponse : " + CardResponse);
        CardResponse = cc.transmitCmd("C7");

        if (!(CardResponse.equals("00"))) {
            Log.e(TAG, "Commit Transaction error " + CardResponse);
            return;
        }
        Log.d(TAG, "cmd: C7 | response: " + CardResponse);
    }

    private String sendToServer(String dataTosend) {
        con = new ConAsync(this);
        con.setRequestMethod("POST", getPostData(dataTosend));
        con.execute(CommonConfig.HTTP_POST);
        return "sudah kirim";
    }

    private String getPostData(String dataToSend) {

        String retval = null;
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        if (imei.length() <= 2)
            imei = "358586060001548";
        JSONObject rootObj = new JSONObject();
        JSONObject obj = new JSONObject();
        String datas = "25000|6013010612791674=17121200000071100000|666666|" + dataToSend;
        try {
            obj.put("msg_id", imei + getStringDate());
            obj.put("msg_ui", imei);
            obj.put("msg_si", "A25100");
            obj.put("msg_dt", datas);
            rootObj.put("msg", obj);

            retval = rootObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retval;
    }

    //--yyyymmdd
    private String getStringDate() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE)) +
                String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));
        return retval;
    }

    private void aktif() {
        cekSaldo();
        String CardResponse = "";
        String SamResponse = "";
        CardResponse = cc.transmitCmd("5A010000");

        Log.d(TAG, "cmd:5A010000 || " + CardResponse);
        // 4. Card � Get Card Status
        CardResponse = cc.transmitCmd("BD01000000200000");
        String cardStatus = CardResponse;
        Log.i(TAG, "Get Card Status :" + CardResponse);
        CardResponse = cc.transmitCmd("0A01");
        String Keycard = CardResponse.substring(2);
        Log.i(TAG, "GET KEY : " + CardResponse);

        SamResponse = smc.sendCmd(hexStringToByteArray("80B0000020" + cData.getCardNumber()
                + cData.getUid() + "FF0000030080000000" + Keycard));
        if (!SamResponse.startsWith("6D")) {
//            Log.i(TAG, "SamResponse : " + SamResponse);
//            CardResponse = cc.transmitCmd("AF"+SamResponse);
//            Log.i(TAG, "cc : " + CardResponse);
            cardStatus = cardStatus.replace("6161", "6161");
            CardResponse = cc.transmitCmd("3D010000002000000207146161000000000000000000000000000000000000000000000000000000");
            Log.i(TAG, "WRITE STATUS : " + "3D010000002000000207146161000000000000000000000000000000000000000000000000000000");
            if (!(CardResponse.equals("00"))) {
                Log.i(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                return;
            }
            Log.i(TAG, "WRITE STATUS :" + CardResponse);
            CardResponse = cc.transmitCmd("C7");

            Log.d(TAG, "cmd: C7 | response: " + CardResponse);
            Log.d(TAG, "new Balance after Deduct: " + cData.getNewBalance());
            if (!(CardResponse.equals("00"))) {
                Log.i(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                return;
            }
            Log.i(TAG, CardResponse);
        } else {
            Log.e(TAG, "SAM AUTH ERROR : " + SamResponse);
        }


    }

    private void deduct() {
        Log.i(TAG, "PEMBAYARAN_NORMAL");
        String CardResponse = "";
        String SamResponse = "";
        cekSaldo();
        // 12. Card � Debit Balance
        String deductAmount = cData.getDeductAmount();
        String reverseAmount3B = ItoH(deductAmount);
        CardResponse = cc.transmitCmd("DC00" + reverseAmount3B + "00");
        //CardResponse = cc.transmitCmd("DC0032000000");
        String cardBalanceRslt = CardResponse.substring(2); //-- balance kartu setelah di potong deductAmount
        Log.d(TAG, "cmd: " + "DC00" + ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);
//		if (CardResponse.length()<10){
//			sendMsgWhat(CARD_RESPONSE_ERROR);
//			return;
//		}
        // 13. SAM � Create Hash
        cData.settDate(getStringDate2());
        cData.settTime(getStringTime());
        String NominalTransaksi = nominalTransaksi(deductAmount);
        String TansactionData = Hex3(cData.getCardNumber()) + Hex3(NominalTransaksi) + Hex3(cData.gettDate()) + Hex3(cData.gettTime()) + Hex3("818001") + Hex3("000036") + Hex3("03") + "FFFFFFFF";
        Log.d(TAG, "TansactionData : " + TansactionData);
        String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000" + cData.getRandomNumber8B() + TansactionData;
        SamResponse = smc.sendCmd(hexStringToByteArray(sendtosam));
        Log.d(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(2, 10));
        cData.setHash4B(SamResponse.substring(2, 10));
        if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
            Log.i(TAG, "ERROR RESPONSE " + SamResponse);
            return;
        }
        // 14. Card � Write Log
        String CardBalanceBefore3B = ItoH(cData.getCardBalanceInt());
        int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(deductAmount);
        if (balanceAfter < 0) {
            Log.i(TAG, "BALANCE MINUS " + balanceAfter);
            return;
        }
        cData.setNewBalance(Integer.toString(balanceAfter));
        String balanceAfter3B = ItoH(Integer.toString(balanceAfter));
        String sendTocard = "3B01000000200000" + cData.getMerchanID() + cData.getTerminalID() + cData.gettDate() + cData.gettTime() + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
        CardResponse = cc.transmitCmd(sendTocard);
        Log.d(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
        if (!(CardResponse.equals("00"))) {
            Log.i(TAG, "ERROR RESPONSE " + CardResponse);
            return;
        }
        CardResponse = cc.transmitCmd("C7");
        Log.d(TAG, "cmd: C7 | response: " + CardResponse);
        Log.d(TAG, "new Balance after Deduct: " + cData.getNewBalance());
        if (!(CardResponse.equals("00"))) {
            Log.i(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
            return;
        }
    }

    private String getStringDate2() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE));

        return retval.substring(2);
    }

    //--His
    private String getStringTime() {
        Calendar c = Calendar.getInstance();
        String retval = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));

        return retval;
    }

    public String ItoH(String intval) {
        intval = intval.replace(" ", ""); // 1
        Log.i(TAG, "TOPUP " + intval);
        int iBalance = Integer.parseInt(intval); // 1
        String Hbal = "000000" + Integer.toString(iBalance, 16); // H 1
        Hbal = Hbal.substring(Hbal.length() - 6);
        String Hbal1 = Hbal.substring(0, 2);
        String Hbal2 = Hbal.substring(2, 6);
        Hbal2 = Hbal2.substring(2, 4) + Hbal2.substring(0, 2);
        String result = Hbal2 + Hbal1;
        Log.i(TAG, "REVERSE TOPUP " + result);
        return result;
    }

    //-- Reverse amount (4B) to Integer , Misal 27 10 00 00 menjadi 10000
    public String HtoI(String hVal) {
        if (hVal.length() == 6)
            hVal = hVal + "00";
        //hVal1 = hVal.substring(4,8)+hVal.substring(0,4);
        String hVal1 = hVal.substring(4, 8);
        hVal1 = hVal1.substring(2, 4) + hVal1.substring(0, 2);
        String hVal2 = hVal.substring(0, 4);
        hVal2 = hVal2.substring(2, 4) + hVal2.substring(0, 2);
        String hv = hVal1 + hVal2;
        int ival = Integer.parseInt(hv, 16);
        return "" + ival;
    }

    public String Hex3(String data) {
        String newData = "";
        for (int i = 0; i < data.length(); i++) {
            newData += "3" + data.charAt(i);
        }
        return newData;
    }

    public String nominalTransaksi(String data) {
        String dt = "0000000000" + data;
        return dt.substring(dt.length() - 10);
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

    private void doSmartCard(View v) {
//        Log.i(SmartCardEvent.TAG_SMART_CARD, SmartCardEvent.TAG_POWER_ON + " " + smc.powerOn());
//        Log.i(SmartCardEvent.TAG_SMART_CARD, SmartCardEvent.TAG_GET_RANDOM + " " + smc.getRandom());
    }

    @Override
    protected void onPause() {
        super.onPause();
        cc.searchEnd();
        smc.closedevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cc.searchEnd();
        smc.closedevice();
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
