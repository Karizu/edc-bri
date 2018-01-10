package id.co.bri.brizzi.module;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TextView;

import com.wizarpos.apidemo.contactlesscard.ContactlessControler;
import com.wizarpos.apidemo.contactlesscard.RFCardControler;
import com.wizarpos.apidemo.printer.ESCPOSApi;
import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.apidemo.smartcard.NeoSmartCardController;
//import com.wizarpos.apidemo.smartcard.SmartCardController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import id.co.bri.brizzi.R;
import id.co.bri.brizzi.common.BrizziCiHeader;
import id.co.bri.brizzi.common.BrizziCiStatus;
import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.handler.ConAsync;
import id.co.bri.brizzi.handler.DataBaseHelper;
import id.co.bri.brizzi.handler.ISO8583Parser;
import id.co.bri.brizzi.handler.LogHandler;
import id.co.bri.brizzi.handler.MenuListResolver;
import id.co.bri.brizzi.handler.txHandler;
import id.co.bri.brizzi.module.listener.FinishedPrint;
import id.co.bri.brizzi.module.listener.ReqListener;
import id.co.bri.brizzi.module.listener.TapListener;

/**
 * Created by indra on 24/11/15.
 */
public class TapCard extends RelativeLayout implements ReqListener, FinishedPrint {

    public static final String INITIALIZE = "2100000";
    public static final String INFO_SALDO = "2200000";
    public static final String INFO_DEPOSIT = "2300000";
    public static final String PEMBAYARAN_NORMAL = "2410000";
    public static final String PEMBAYARAN_DISKON = "2420000";
    public static final String TOPUP_ONLINE = "2500000";
    public static final String TOPUP_DEPOSIT = "2600000";
    public static final String AKTIFASI_DEPOSIT = "2700000";
    public static final String SETTLEMENT = "2800000";
    public static final String REDEEM = "2900000";
    public static final String REDEEM_NEXT = "292000F";
    public static final String AKTIFKAN = "290000A";
    public static final String REAKTIVASI = "2A00000";
    public static final String REAKTIVASI_PAY = "2A1000F";
    public static final String INFO_KARTU = "2B00000";
    public static final String VOID_BRIZZI = "2C00000";
    public static final String VOID_REFUND = "2C10000";
    public static final String PRINT_LOG = "2D00000";
    public static final String AKTIF_STATUS = "6161";
    public static final String NONAKTIF_STATUS = "636C";
    public static final String[] BRIZZI_MENU =
            {
                    // INITIALIZE,
              INFO_SALDO, INFO_DEPOSIT, PEMBAYARAN_DISKON,
                    REDEEM, REDEEM_NEXT, REAKTIVASI, INFO_KARTU, VOID_REFUND,
                    PEMBAYARAN_NORMAL, TOPUP_ONLINE, TOPUP_DEPOSIT, AKTIFASI_DEPOSIT,
                    AKTIFKAN,
//                    SETTLEMENT,
                    PRINT_LOG};
    public static final String SI_TOPUP_ONLINE = "A25100";
    public static final String SI_AKTIFASI_DEPOSIT = "A27100";
    public static final String SI_INFO_DEPOSIT = "A23100";
    public static final String SI_SETTLEMENT = "A28100";
    public static final String SI_VOID = "A2C100";
    public static final String SI_VOID_REFUND = "A2C200";
    public static final String SI_REAKTIVASI = "A2A100";
    public static final String SI_REAKTIVASI_PAY = "A2A200";
    public static final String SI_INFO_KARTU = "A2B100";
    public static final String SI_PEMBAYARAN = "A24100";
    public static final String SI_DISKON = "A24210";
    public static final String SI_REDEEM = "A29100";
    public static final String SI_REDEEM_NEXT = "A29200";
    public static final String SI_PRINTLOG = "A2D000";
    public static final String[] FINANCIALTX = {
            SI_PEMBAYARAN, SI_DISKON, SI_TOPUP_ONLINE, SI_AKTIFASI_DEPOSIT, SI_VOID
    };
    public final String TAG = "BRIZZI";
    private final SimpleDateFormat DATE = new SimpleDateFormat("ddMMyy");
    private final SimpleDateFormat DATE_TOCARD = new SimpleDateFormat("yyMMdd");
    private final SimpleDateFormat TIME = new SimpleDateFormat("HHmmss");
    private final SimpleDateFormat DATE_TOCOMP = new SimpleDateFormat("dd-MM-yyyy");
    public static final String cardType = "BRIZZI CARD (FLY)";

    boolean DEBUG_LOG = true;
    boolean DEBUG_MODE = false;
    /* Setup Aktivasi Kartu Close
    boolean DEBUG_MODE = true; */
    SQLiteDatabase clientDB = null;
    List<String> mdata = new ArrayList<>();
    String tid = null;
    String mid = null;
    String stan = null;
    String svrDt = "";
    String svrTm = "";
    private int printcount = 0;
    private int printcountbutton = 0;
    private Boolean footerAdded = false;
    private android.widget.Button btnOk, btnPrint, btnNoPrint;
    private android.widget.TextView confirmationText;
    private String[] printConfirm = {
            "Print Customer Copy ?",
            "Print Bank Copy ?",
            "Print Merchant Copy ?", "",
            "Print Duplicate Copy ?", "", "", ""
    };
    private boolean printInUse = false;
    private boolean isAntiDDOSPrint = true;

    private boolean enablePrint = false;
    private JSONObject formReponse = new JSONObject();
    private List<PrintSize> printSizes = new ArrayList<>();
    private android.widget.TextView txtMessage;
    private List<TapListener> tapListeners = new ArrayList<>();
    private Context context;
    DataBaseHelper helperDb = new DataBaseHelper(context);
    private CardData cData = new CardData();
    private ContactlessControler cc;
//    private RFCardControler cc;
    private NeoSmartCardController smc;
    private ConAsync con;
    private JSONObject printData = new JSONObject();
    private txHandler tx = txHandler.getInstance();
    private LogHandler EDCLog;
    private FormListener formListener;
    private String logid;
    private String nomorKartu;
    private Long maxDeduct;
    private boolean traceAdded = false;
    private String gtmStamp;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            Bundle bundle = msg.getData();
            try {
                try {
                    helperDb.openDataBase();
                    clientDB = helperDb.getActiveDatabase();
                } catch (Exception ex) {
                    Log.e("TX", "DB error");
                }
                int msgStan = 0;
                if (!(cData.getWhatToDo().equals(VOID_BRIZZI)||
                        cData.getWhatToDo().equals(VOID_REFUND)||
                        cData.getWhatToDo().equals(REDEEM_NEXT))) {
                    String getStanSeq = "select seq msgSequence from holder";
//                    String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                            "from edc_log where date(rqtime) = date('now') and rc = '00' ";
                    Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
                    if (stanSeq != null) {
                        stanSeq.moveToFirst();
                        msgStan = stanSeq.getInt(0);
                        if (msgStan > 999999) {
                            msgStan = 0;
                        }
                        msgStan += 1;
                        stanSeq.close();
                    }
//                    String uStanSeq = "update holder set "
//                            + "seq = " + String.valueOf(msgStan);
//                    writeDebugLog("UPDATING", "HOLDER (181T)");
//                    writeDebugLog("By ", cData.getWhatToDo());
//                    clientDB.execSQL(uStanSeq);
//                    traceAdded = true;
                }
                if (svrDt.equals("")) {
                    svrDt = StringLib.getSQLiteTimestamp().substring(0, 10);
                }
                if (svrTm.equals("")) {
                    svrTm = StringLib.getSQLiteTimestamp().substring(11);
                }
                stan = StringLib.fillZero(String.valueOf(msgStan),6);
                String uid = bundle.getString("uid");
                ContentValues contentValues = new ContentValues();
                contentValues.put("screen_id", cData.getWhatToDo());
                contentValues.put("uid", uid);
                contentValues.put("timestamp", new Date().getTime());
                cData.setBrizziIdLog(tx.insertIntoAidLog(contentValues));
                cData.setUid(uid);
                cData.setTerminalID(StringLib.fillZero(
                        preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID), 16));
                cData.setMerchanID(StringLib.fillZero(
                        preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID), 16));
                maxDeduct = Long.parseLong(
                        preferences.getString("maximum_deduct", CommonConfig.DEFAULT_MAX_MONTHLY_DEDUCT));
                writeDebugLog(TAG, "TAP PLEASE");
                for (TapListener tapListener : tapListeners) {
                    tapListener.onTap();
                }
                writeDebugLog(TAG, cData.getWhatToDo());
                if (cData.getWhatToDo().equals(INFO_SALDO)) {
                    cData.settDate(DATE.format(new Date()));
                    cData.settTime(TIME.format(new Date()));
                    infoSaldo();
//                    changeStatus("cl");
                } else if (cData.getWhatToDo().equals(TOPUP_ONLINE)) {
                    doTopup();
                } else if (cData.getWhatToDo().equals(PEMBAYARAN_NORMAL)) {
                    cData.settDate(DATE.format(new Date()));
                    cData.settTime(TIME.format(new Date()));
                    deduct(false);
                } else if (cData.getWhatToDo().equals(INFO_DEPOSIT)) {
                    if (DEBUG_MODE) {
                        changeStatus("aa");
                    } else {
                        cData.setMsgSI(SI_INFO_DEPOSIT);
                        infoDeposit();
                    }
                } else if (cData.getWhatToDo().equals(PEMBAYARAN_DISKON)) {
                    if (DEBUG_MODE) {
                        changeStatus("ps");
                    } else {
                        cData.settDate(DATE.format(new Date()));
                        cData.settTime(TIME.format(new Date()));
                        deduct(true);
                    }
                } else if (cData.getWhatToDo().equals(AKTIFASI_DEPOSIT)) {
                    cData.setMsgSI(SI_AKTIFASI_DEPOSIT);
                    aktifasiDeposit();
                } else if (cData.getWhatToDo().equals(SETTLEMENT)) {

                } else if (cData.getWhatToDo().equals(REDEEM)) {
                    cData.setMsgSI(SI_REDEEM);
                    doRedeem();
                } else if (cData.getWhatToDo().equals(REDEEM_NEXT)) {
                    writeDebugLog("REDEEM", cData.gettTime());
                    writeDebugLog("REDEEM", cData.gettDate());
                    writeDebugLog("REDEEM", cData.getServerRef());
                    redeem();
                } else if (cData.getWhatToDo().equals(AKTIFKAN)) {
                    changeStatus("aa");
                } else if (cData.getWhatToDo().equals(REAKTIVASI)) {
                    cData.settDate(DATE.format(new Date()));
                    cData.settTime(TIME.format(new Date()));
                    cData.setMsgSI(SI_REAKTIVASI);
                    reaktivasi();
                } else if (cData.getWhatToDo().equals(REAKTIVASI_PAY)) {
                    cData.settDate(DATE.format(new Date()));
                    cData.settTime(TIME.format(new Date()));
                    cData.setMsgSI(SI_REAKTIVASI_PAY);
                    nextReaktivasi();
                } else if (cData.getWhatToDo().equals(INFO_KARTU)) {
                    cData.settDate(DATE.format(new Date()));
                    cData.settTime(TIME.format(new Date()));
                    cData.setMsgSI(SI_INFO_KARTU);
                    infoKartu();
                } else if (cData.getWhatToDo().equals(VOID_REFUND)) {
                    cData.setMsgSI(SI_VOID_REFUND);
                    doVoidSecond();
                } else if (cData.getWhatToDo().equals(PRINT_LOG)) {
                    cData.setMsgSI(SI_PRINTLOG);
                    printLogKartu();
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                setMessage("Tidak dapat melakukan transaksi,\nsilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
            }
        }
    };

    public TapCard(Context context) {
        super(context);
        this.context = context;
    }

    public TapCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public TapCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void searchBegin() {
        boolean openDevice = smc.starting(1);
        openDevice = openDevice && cc.searchBegin();
        if (!openDevice) {
            setMessage("Terjadi kesalahan.\n ERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "ERROR WHEN OPENING DEVICES");
        }
    }

    public void searchEnd() {
        if (clientDB!=null) {
            if (clientDB.isOpen()) {
                clientDB.close();
            }
        }
        if (helperDb!=null) {
            helperDb.close();
        }
        cc.searchEnd();
        smc.closedevice();
    }

    public void init(CardData cardData) {
        //test uncaughterror
//        Integer.valueOf("asd");
        txtMessage = (android.widget.TextView) findViewById(R.id.txtMessage);
//        cc = new RFCardControler(handler, context);
        cc = new ContactlessControler(handler, context);
        smc = new NeoSmartCardController(context);
        this.cData = cardData;
        tx.setContext(context);
        isAntiDDOSPrint = true;
        btnOk = (android.widget.Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkListener(v);
            }
        });
        btnOk.setVisibility(GONE);
        btnPrint = (android.widget.Button) findViewById(R.id.btnCetak);
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAntiDDOSPrint) {
                    onPrintListener(v);
                }
                isAntiDDOSPrint = false;
            }
        });
        confirmationText = (TextView) findViewById(R.id.printConfirmText);
        btnNoPrint = (android.widget.Button) findViewById(R.id.btnNoPrint);
        btnNoPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOk.performClick();
            }
        });
        printPanelVisibility(GONE);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2000);
        DATE_TOCOMP.set2DigitYearStart(cal.getTime());
        DATE.set2DigitYearStart(cal.getTime());
        //init print header
        SharedPreferences preferences;
        preferences  = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        boolean deviceRegistered = false;
        if (preferences.contains("registered")) {
            deviceRegistered = preferences.getBoolean("registered", false);
        }
        boolean noTMSMODE = preferences.getBoolean("debug_mode", false);
        if (!noTMSMODE) {
            if (!deviceRegistered) {
                setMessage("EDC tidak terdaftar, tidak dapat melakukan transaksi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
        }
        mdata.add(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
        mdata.add(preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1));
        mdata.add(preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2));
        tid = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);
        mid = preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID);
        nomorKartu = "";
        maxDeduct = Long.parseLong(preferences.getString("maximum_deduct", CommonConfig.DEFAULT_MAX_MONTHLY_DEDUCT));
    }

    private void onOkListener(View v) {

    }

    private void printPanelVisibility(int visibility) {
        btnPrint.setVisibility(visibility);
        confirmationText.setVisibility(visibility);
        btnNoPrint.setVisibility(visibility);
    }

    private void refreshConfirmation() {
        confirmationText.setText(printConfirm[printcountbutton]);
    }

    public void setOkListener(OnClickListener onClickListener) {
        btnOk.setOnClickListener(onClickListener);
        btnNoPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cData.getWhatToDo().equals(INFO_SALDO)) {
                    btnOk.performClick();
                }
                if (printcountbutton > 1) {
                    btnOk.performClick();
                }
                if (printcountbutton == 0) {
                    footerAdded = false;
                }
                printcountbutton++;
                printcount++;
                refreshConfirmation();
            }
        });
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!printInUse) {
                    if (printcountbutton > 1) {
                        btnOk.performClick();
                    }
                    if (printcountbutton == 0) {
                        footerAdded = false;
                    }
                    try {
                        nomorKartu = cardNumber();
                    } catch (Exception exception) {

                    }
                    new Thread(new PrintData(printSizes, mdata, tid, mid, stan, nomorKartu, TapCard.this)).start();
                    if (cData.getWhatToDo().equals(INFO_SALDO)) {
                        btnOk.performClick();
                    }
                    printcountbutton++;
                    refreshConfirmation();
                }
            }
        });
    }

    private void onPrintListener(View v) {
        if (!printInUse) {
            if (printcountbutton > 1) {
                printPanelVisibility(GONE);
            }
            if (printcountbutton == 0) {
                footerAdded = false;
            }
            try {
                nomorKartu = cardNumber();
            } catch (Exception exception) {

            }
            new Thread(new PrintData(printSizes, mdata, tid, mid, stan, nomorKartu, TapCard.this)).start();
            printcountbutton++;
        }
    }

    public void addTapListener(TapListener tapListener) {
        tapListeners.add(tapListener);
    }

    public void setMessage(String message) {
        txtMessage.setText(message);
    }

    public void setMessage(String message, int mode) {
        txtMessage.setGravity(mode);
        setMessage(message);
    }

    @Override
    public void onReqCompleted(String result) {
        writeDebugLog(TAG, "HOST RESPONSE " + result);
        if (result.equals("ReversalRQ")) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONObject("screen").getJSONObject("comps").getJSONArray("comp");
            if(obj.getJSONObject("screen").getString("id").equals("000000F")){
                String resp = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                setMessage(resp);
                cleanUpFailedVoidLog();
                btnOk.setVisibility(VISIBLE);
                return;
            }
            String resp = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
            String date = StringLib.getDDMMYY();
            String time = StringLib.getStringTime();

            if (arr.length()>3&&
                    !cData.getMsgSI().equals(SI_TOPUP_ONLINE)) {
                date = arr.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                writeDebugLog(TAG, "HOST RESPONSE DATE " + date);
                time = arr.getJSONObject(3).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                writeDebugLog(TAG, "HOST RESPONSE TIME " + time);
            }
            if (obj.has("server_ref")) {
                cData.setServerRef(obj.getString("server_ref"));
            }
            if (obj.has("server_date")) {
                svrDt = obj.getString("server_date");
            }
            if (obj.has("server_time")) {
                svrTm = obj.getString("server_time");
            }
            if (obj.has("logid")) {
                logid = obj.getString("logid");
            }
            cData.setHostResponse(resp);
            cData.settTime(time);
            cData.settDate(date);
            if (cData.getMsgSI().equals(SI_TOPUP_ONLINE)) {
                if (resp.equalsIgnoreCase("time out")) {
                    setMessage("Terjadi kesalahan.\nERROR [TIME OUT]");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                date = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                time = arr.getJSONObject(1).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                resp = arr.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cData.setHostResponse(resp);
                cData.setTimeFromIso(time);
                cData.setDateFromIso(date);
                try {
                    nextTopupStep();
                } catch (Exception e) {
                    setMessage("Tidak dapat melakukan transaksi\nError saat membaca kartu BRIZZI");
//                    sendReversal(cData.getRandomSam24B(), cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
                    tx.reverseLastTransaction(context);
                }
            } else if (cData.getMsgSI().equals(SI_AKTIFASI_DEPOSIT)) {
                if (arr.length()==1) {
                    setMessage("Terjadi kesalahan.\nERROR [TIME OUT]");
                    setMessage(resp);
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                String saldoDeposit = arr.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                String sz = arr.getJSONObject(3).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                String rnd = arr.getJSONObject(4).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                String az = sz.replaceAll("[,.]", "");
                saldoDeposit = saldoDeposit.replaceAll("[,.]","");
                writeDebugLog(TAG, az);
                writeDebugLog(TAG, cData.getCardBalanceInt());
                cData.setSaldoDeposit(saldoDeposit);
                double d = Double.parseDouble(az);
                cData.setHostResponse(rnd);
                cData.setTopupAmount(String.valueOf((int) d));
                if (Integer.parseInt(cData.getCardBalanceInt())+(int) d>1000000) {
//                    sendReversal(cData.getRandomSam24B(), cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
                    tx.reverseLastTransaction(context);
                    setMessage("Aktif deposit akan melebihi limit");
                    Log.e(TAG, "Aktif Deposit Over Limit");
                    btnOk.setVisibility(VISIBLE);
                    return;
                } else {
                    setMessage("Processing Next Step");
                    Log.e(TAG, "Aktif Deposit Not Over Limit");
                }
                date = StringLib.getDDMMYY();
                time = StringLib.getStringTime();
                cData.settTime(time);
                cData.settDate(date);
                try {
                    nextAktifasiDeposit();
                } catch (Exception e) {
                    setMessage("Tidak dapat melakukan transaksi\nError saat membaca kartu BRIZZI");
//                    sendReversal(cData.getRandomSam24B(), cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
                    tx.reverseLastTransaction(context);
                }
            } else if (cData.getMsgSI().equals(SI_REAKTIVASI)) {
                writeDebugLog(TAG, obj.getJSONObject("screen").toString());
                if(result.contains("RC")){
                    setMessage("Terjadi kesalahan.\nERROR [TIME OUT]");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                String lama_pasif = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                writeDebugLog(TAG, "Lama Pasif : " + lama_pasif);
                if (lama_pasif.startsWith("0")) {
                    lama_pasif = lama_pasif.substring(1);
                }
                cData.setLamaPasif(lama_pasif);
                String statusAfter = arr.getJSONObject(4).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                writeDebugLog(TAG, "Status After : " +  statusAfter);
                statusAfter = (statusAfter.equals("AKTIF")) ? "6161" : "636C";
                cData.setStatusAfter(statusAfter);
                String saldoDeposit = arr.getJSONObject(2).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                saldoDeposit = saldoDeposit.replace("\\,","").replace("\\.","");
                writeDebugLog(TAG, "Saldo Deposit : "+saldoDeposit);
                cData.setSaldoDeposit(saldoDeposit);
                obj.put("server_date", svrDt);
                obj.put("server_time", svrTm);
                formListener.onSuccesListener(obj);
            } else if (cData.getMsgSI().equals(SI_VOID_REFUND)) {
                if (resp.equalsIgnoreCase("time out") || resp.length() < 48) {
                    setMessage("Terjadi kesalahan.\nERROR [TIME OUT]");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                String key = arr.getJSONObject(0).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                String nom = arr.getJSONObject(1).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                nom = nom.replace(".","").replace(",00","");
                cData.setHostResponse(key);
                cData.setTopupAmount(nom);
                try {
                    nextVoidSecond();
                } catch (Exception e) {
                    setMessage("Tidak dapat melakukan transaksi\nError saat membaca kartu BRIZZI");
                    tx.reverseLastTransaction(context);
                }
            } else if (cData.getMsgSI().equals(SI_INFO_DEPOSIT)) {
                String saldo_deposit = arr.getJSONObject(1).getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0).getString("value").trim();
                cData.setSaldoDeposit(saldo_deposit);
                try {
                    formReponse = obj;
                    writeDebugLog(TAG, "OBJ " + formReponse.toString());
                    btnOk.setVisibility(VISIBLE);
                    printPanelVisibility(VISIBLE);
                    writeMessageLog();
                    formReponse.put("server_date", svrDt);
                    formReponse.put("server_time", svrTm);
                    formReponse.put("card_type", cardType);
                    formReponse.put("nomor_kartu", cardNumber());
                    formListener.onSuccesListener(formReponse);
                } catch (Exception e) {
                    setMessage("Error Parsing Reply : " + e.getMessage());
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    btnOk.setVisibility(VISIBLE);
                }
            } else if (cData.getMsgSI().equals(SI_REDEEM)) {
                obj.put("server_date", svrDt);
                obj.put("server_time", svrTm);
                obj.put("server_ref", cData.getServerRef());
                cData.settDate(svrDt);
                cData.settTime(svrTm);
                formListener.onSuccesListener(obj);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "REPLY HANDLING ERROR : " + ex.getMessage());
        }
    }

    @Override
    public void onNoInternetConnection() {
        setMessage("Terjadi kesalahan.\nERROR [TIME OUT]");
    }

    private void aktifasiDeposit() {
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        try {
            if (Integer.parseInt(cData.getCardBalanceInt())>1000000) {
                setMessage("Isi ulang akan melebihi limit");
                Log.e(TAG, "Topup Over Limit");
                btnOk.setVisibility(VISIBLE);
                return;
            }
        } catch (Exception e) {
            setMessage("Terjadi kesalahan.\nERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String vStat = cData.getValidationStatus();
        if (!vStat.equals("00")) {
            return;
        }
        Log.i(TAG, "AKTIVASI DEPOSIT");
//        writeDebugLog(TAG, "Saldo Awal : " + cData.getCardBalanceInt());
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
        String CardResponse = "";
        String cmd = "";
        Calendar startCalendar = Calendar.getInstance();
        try {
            startCalendar.setTime(DATE_TOCARD.parse(cData.getLastTransDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar endCalendar = Calendar.getInstance();
        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        if (diffMonth > 12&&!cData.getLastTransDate().equals("000000")) {
            setMessage("Transaksi tidak bisa dilakukan, kartu ditolak");
            Log.e(TAG, "CARD REJECTED CAUSE LAST TRANS DATE > 12 months");
            btnOk.setVisibility(VISIBLE);
            return;
        }

        cmd = "80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000";
        String SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", cmd);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\n ERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String randomSam24B = SamResponse.substring(0, SamResponse.length() - 4);
        cData.setRandomSam24B(randomSam24B);
//        writeDebugLog(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + randomSam24B);
        // 13. HOST Get Auth Key Topup
        writeDebugLog("KIRIM", "MSG_SI " + cData.getMsgSI());
        sendToServer(randomSam24B, cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
    }

    private void nextAktifasiDeposit() {
        Log.i(TAG, "NEXT AKTIFASI DEPOSIT");
        String cmd = "";
        String CardResponse = "";
        ContentValues contentValues;
//        writeDebugLog(TAG, "NEXT AKTIFASI DEPOSIT STEP, HOST RESPONSE: " + cData.getHostResponse());
        String keyTopup24B = cData.getHostResponse();
        if (keyTopup24B.length()>48) {
            keyTopup24B = keyTopup24B.substring(0,48);
        }
        Log.d("Key", keyTopup24B);
        if (!(keyTopup24B.length() == 48)) {
            Log.e(TAG, "Error Response from server !!" + " || response: " + keyTopup24B);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        // 14. Card � Select AID 3
        CardResponse = cc.transmitCmd("5A030000");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "5A030000");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);


        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Select AID 3 error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: 5A030000" + " || response: " + CardResponse);
        //15. Card � Request Key Card 01
        CardResponse = cc.transmitCmd("0A01");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);


//        writeDebugLog(TAG, "cmd: 0A01" + " || response: " + CardResponse);
        int i = 0;
        String keyCard08B = CardResponse.substring(2);

        // 16. SAM � Authenticate Topup
        String sendtosam = "80B2000037" + keyTopup24B +
                cData.getCardNumber() + cData.getUid() + "0000030180000000" + keyCard08B;
        String SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", sendtosam);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);

        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\nERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Sam Authenticate Topup error " + SamResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: Authenticate Topup || response: " + SamResponse);
        String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//        writeDebugLog(TAG, "cmd: RandomKey16B || send: AF" + RandomKey16B);
        // 17. Card � Authenticate Card
        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Authenticate Card error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String RandomNumber8B = CardResponse.substring(2);
//        writeDebugLog(TAG, "cmd: AF" + RandomKey16B + " || response: " + CardResponse);
        // 18. Card � Credit Balance
        String topupAmount = StringLib.ItoH(cData.getTopupAmount()); //--- nilai yg akan di topup
        String transmit = "0C00" + topupAmount + "00";
        CardResponse = cc.transmitCmd(transmit);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", transmit);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Credit Balance error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String CardBalance4B = CardResponse.substring(2);
//        writeDebugLog(TAG, "cmd: " + transmit + " || response: " + CardResponse + "| Card Balance = " + CardBalance4B);

        // 19. SAM � Create Hash

        String transactionData = StringLib.Hex3(cData.getCardNumber())
                + StringLib.Hex3(StringLib.nominalTransaksi(cData.getTopupAmount()))
                + StringLib.Hex3(cData.gettDate())
                + StringLib.Hex3(cData.gettTime())
                + StringLib.Hex3("818001") + StringLib.Hex3("000036")
                + StringLib.Hex3("03") + "FFFFFFFF";

//        sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid()
//                + "FF0000030080000000" + RandomNumber8B + transactionData;
//        Log.i(TAG,"SEMD TO SAM "+sendtosam);
//        SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
//        Log.i(TAG,"SAM – Create Hash "+SamResponse);
//        String Hash4B = SamResponse.substring(0, SamResponse.length() - 4);
//        if (!SamResponse.endsWith("9000")) {
//            Log.e(TAG, "SAM – Create Hash error " + SamResponse);
//            Log.e(TAG, "Hash "+ Hash4B);
//            return;
//        }
//        cData.setHash4BTopup(Hash4B);
        // 20. Card � Write Log
        String balanceBeforeint = StringLib.HtoI(cData.getCardBalance4B());
//        writeDebugLog(TAG, "balanceBefore3B step 1 : " + balanceBeforeint);
        int bAfter = Integer.parseInt(balanceBeforeint) + Integer.parseInt(cData.getTopupAmount());
//        writeDebugLog(TAG, "BalanceAfter int : " + bAfter);
        String bAfter3B = StringLib.ItoH(Integer.toString(bAfter));
//        writeDebugLog(TAG, "BalanceAfter3B : " + bAfter3B);
        String balanceBefore3B = StringLib.ItoH(balanceBeforeint);
//        writeDebugLog(TAG, "balanceBefore3B step 2 : " + balanceBefore3B);
        String jamTopup = svrTm.replaceAll(":","");
        if (jamTopup!=null) {
            if (jamTopup.equals("")) {
                jamTopup = cData.gettTime();
            }
        } else {
            jamTopup = cData.gettTime();
        }
        String cmdWritelog = "3B01000000200000" + cData.getMerchanIdForCardLog()
                + cData.getTerminalIdForCardLog() + cData.gettDate() + jamTopup
                + "EF" + StringLib.ItoH(cData.getTopupAmount()) + balanceBefore3B + bAfter3B;

        CardResponse = cc.transmitCmd(cmdWritelog);
//        writeDebugLog(TAG, "cmd : " + cmdWritelog);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", cmdWritelog);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
//        writeDebugLog(TAG, "CardResponse : " + CardResponse);
        if (CardResponse.startsWith("AF")) {
            CardResponse = cc.transmitCmd("AF");
//            writeDebugLog(TAG, "CardResponse after AF : " + CardResponse);
        }

        if (!(CardResponse.equals("00"))) {
            Log.e(TAG, "Write log error " + CardResponse);
//            return;
        }
        // 21. Card � Write Last transaction
//		String lastTransMonth = cData.getLastTransDate().substring(0,4);
//		String nowMonth = cData.gettDate().substring(0,4);
        String akumdebet = "00000000";
        if (cData.getLastTransDate().substring(0, 4).equals(cData.gettDate().substring(0, 4))) {
            akumdebet = Integer.toString(Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getTopupAmount()));
        } else {
            akumdebet = cData.getTopupAmount();
        }
        akumdebet = StringLib.nominalUntukLog(StringLib.Int2Hex(akumdebet));
//        writeDebugLog(TAG, "akumdebet : " + akumdebet);
        CardResponse = cc.transmitCmd("3D03000000070000" + cData.getYYMMDD() + akumdebet);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "3D03000000070000" + cData.getYYMMDD() + akumdebet);
//        writeDebugLog(TAG, "cmd : " + "3D03000000070000" + cData.getYYMMDD() + akumdebet);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!(CardResponse.equals("00"))) {
            Log.e(TAG, "Write Last transaction error " + CardResponse);
//            return;
        }
//        writeDebugLog(TAG, "CardResponse : " + CardResponse);
        CardResponse = cc.transmitCmd("C7");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "C7");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!(CardResponse.equals("00"))) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            Log.e(TAG, "Commit Transaction error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);

        printSizes.clear();
        StringBuilder builder = new StringBuilder();
        builder.append("Jumlah aktivasi deposit adalah " + StringLib.strToCurr(cData.getTopupAmount(),"Rp"));
        builder.append("\n");
        builder.append("Sisa saldo deposit adalah " + StringLib.strToCurr(cData.getSaldoDeposit(), "Rp"));
        builder.append("\n");
        builder.append("Jumlah saldo anda sekarang adalah " +
                StringLib.strToCurr(cekSaldo().getCardBalanceInt(), "Rp"));
        setMessage(builder.toString());
        printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu         : " + cardNumber() + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Saldo Awal       : " +
                StringLib.strToCurr(String.valueOf(balanceBeforeint), "Rp") + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Aktivasi Deposit : " +
                StringLib.strToCurr(cData.getTopupAmount(), "Rp") + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Saldo Akhir      : " +
                StringLib.strToCurr(String.valueOf(bAfter), "Rp") + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Sisa Deposit     : " +
                StringLib.strToCurr(cData.getSaldoDeposit(), "Rp") + "\n"));
        printPanelVisibility(VISIBLE);
        btnOk.setVisibility(VISIBLE);
        try {
            formReponse = new JSONObject();
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "270000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24301");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cardNumber());
            cmVal.put("print", cardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Aktivasi Deposit";
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Saldo Awal          : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(balanceBeforeint, "Rp"));
            cmVal.put("print", StringLib.strToCurr(balanceBeforeint, "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24303");
            component.put("comp_lbl", "Aktivasi Deposit    : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cData.getTopupAmount(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cData.getTopupAmount(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24304");
            component.put("comp_lbl", "Saldo Akhir         : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(String.valueOf(bAfter), "Rp"));
            cmVal.put("print", StringLib.strToCurr(String.valueOf(bAfter), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24305");
            component.put("comp_lbl", "Sisa Deposit        : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cData.getSaldoDeposit(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cData.getSaldoDeposit(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            screen.put("title", mTitle);
            comps.put("comp", comp);
            screen.put("comps", comps);
            formReponse.put("screen", screen);
            if (!cData.getServerRef().equals("")) {
                formReponse.put("server_ref", cData.getServerRef());
            }
            formReponse.put("server_date", svrDt);
            formReponse.put("server_time", svrTm);
            writeMessageLog();
            formListener.onSuccesListener(formReponse);
        } catch (Exception e) {
            setMessage(e.getMessage());
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            e.printStackTrace();
            CardResponse = cc.transmitCmd("A7");
            btnOk.setVisibility(VISIBLE);
        }
    }

    private JSONObject parseCiHeader(final String pPrivateData) throws JSONException {
        JSONObject tJSONMessage = new JSONObject();
        for (BrizziCiHeader ciHeader : BrizziCiHeader.values()) {
            String bit48DataValue = getCiHeaderString(ciHeader, pPrivateData);
            tJSONMessage.put(ciHeader.toString(), bit48DataValue);
        }

        return tJSONMessage;
    }

    private String getCiHeaderString(BrizziCiHeader resp, String pBit48) {
        int length = resp.getCode();
        String tReturn = "";
        int start = 0;

        for (BrizziCiHeader brizziCiHeader : BrizziCiHeader.values()) {
            int end = start + length;

            if ((brizziCiHeader == resp) && (end <= pBit48.length())) {
                tReturn = pBit48.substring(start, end);
                break;
            }
            start += brizziCiHeader.getCode();
        }
        return tReturn;

    }

    private JSONObject parseCiStatus(final String pPrivateData) throws JSONException {
        JSONObject tJSONMessage = new JSONObject();
        for (BrizziCiStatus ciHeader : BrizziCiStatus.values()) {
            String bit48DataValue = getCiHeaderStatus(ciHeader, pPrivateData);
            tJSONMessage.put(ciHeader.toString(), bit48DataValue);
        }

        return tJSONMessage;
    }

    private String getCiHeaderStatus(BrizziCiStatus resp, String pBit48) {
        int length = resp.getCode();
        String tReturn = "";
        int start = 0;

        for (BrizziCiStatus brizziCiHeader : BrizziCiStatus.values()) {
            int end = start + length;

            if ((brizziCiHeader == resp) && (end <= pBit48.length())) {
                tReturn = pBit48.substring(start, end);
                break;
            }
            start += brizziCiHeader.getCode();
        }
        return tReturn;

    }

    private CardData cekSaldo() {
        Log.i(TAG, "CEK SALDO");
        setMessage("Silahkan Tunggu");
        btnOk.setVisibility(GONE);
        try {
            String CardResponse = "";
            String SamResponse = "";
            String cmd = "";
            //2. Card � Select AID 1
            String aid = smc.sendCmd(StringLib.hexStringToByteArray("00A4040C09A00000000000000011"));
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 0);
            contentValues.put("cmd", "00A4040C09A00000000000000011");
            contentValues.put("rc", aid);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (aid.equals("9000")) {
                CardResponse = cc.transmitCmd("5A010000");
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "5A010000");
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                writeDebugLog(TAG, "cmd:5A010000 || " + CardResponse);
                // 3. Card � Get Card Number
                cmd = cc.getCommand(0, 23);
                CardResponse = cc.transmitCmd(cmd);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", cmd);
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() < 24) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                if (CardResponse.length() < 24) {
                    setMessage("Terjadi Kesalahan.\nERROR [" + CardResponse + "]");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    Log.e(TAG, "Error when Get Card number, response " + CardResponse);
                    btnOk.setVisibility(VISIBLE);
                    cData.setValidationStatus("17");
                    return null;
                }
                JSONObject ciHeader = null;
                contentValues = new ContentValues();
                ciHeader = parseCiHeader(CardResponse);
                String CardNumber = ciHeader.getString(BrizziCiHeader.CardNumber.name());
                contentValues.put("card_number", ciHeader.getString(BrizziCiHeader.CardNumber.name()));
                cData.setCardNumber(CardNumber);
                writeDebugLog(TAG, "cmd:" + cmd + " || " + CardResponse + " CardNumber:" + CardNumber);
                tx.updateAidById(contentValues, cData.getBrizziIdLog());
//                Log.d(TAG, "CardNumber 0" + CardNumber + "0");

                //invalid card handler
                if (CardNumber.length()<16) {
                    Log.e(TAG, "Error when Get Card Number, Read " + CardNumber);
//                        throw new Exception("Transaksi Gagal. Status Kartu Close");
//                        setMessage("Kartu brizzi anda sudah tidak bisa digunakan.");
//                        return null;
                    btnOk.setVisibility(VISIBLE);
                    cData.setValidationStatus("17");
                    return null;

                }
//            String expireDate =


                // 4. Card � Get Card Status
                CardResponse = cc.transmitCmd("BD01000000200000");
                JSONObject cardStatus = parseCiStatus(CardResponse);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "BD01000000200000");
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", cardStatus.getString(BrizziCiStatus.RC.name()));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse);
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);

                if (!(cData.getMsgSI().equals(SI_REAKTIVASI_PAY)||
                        cData.getMsgSI().equals(SI_REAKTIVASI)||
                        cData.getMsgSI().equals(SI_PRINTLOG))) {

                    if (!(CardResponse != null ? CardResponse.startsWith("00") : false) ||
                            !CardResponse.substring(8, 12).equals("6161")) {
                        Log.e(TAG, "Error when Get Card Status, response " + CardResponse);
//                        throw new Exception("Transaksi Gagal. Status Kartu Close");
//                        setMessage("Kartu brizzi anda sudah tidak bisa digunakan.");
//                        return null;
                        btnOk.setVisibility(VISIBLE);
                        cData.setValidationStatus("16");
                        return null;
                    }
                }
                if (false) {
//                    if (cData.getMsgSI().equals(SI_REAKTIVASI_PAY)||cData.getMsgSI().equals(SI_REAKTIVASI)) {

                    if (CardResponse.substring(8, 12).equals("6161")) {
                        Log.e(TAG, "Status Active, cannot reactivate. Response " + CardResponse);
//                        setMessage("Transaksi Gagal (aa)");
                        throw new Exception("Transaksi Gagal. Status Kartu Aktif");
//                        return null;
                    }
                }
                writeDebugLog(TAG, "cmd:BD0100000200000 || " + CardResponse);
                // 5. Card � Select AID 3
                CardResponse = cc.transmitCmd("5A030000");

                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "5A030000");
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse);
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);

                if (!CardResponse.startsWith("00")) {
                    setMessage("Terjadi kesalahan.\nERROR [" + CardResponse.substring(0, 2) + "]");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    Log.e(TAG, "Select AID 3, RESPONSE " + CardResponse);
                    return null;
                }

                writeDebugLog(TAG, "cmd: 5A030000 || " + CardResponse);
                // 6. Card � Request Key Card
                cmd = "0A00";
//                cmd = "906000000000";
                CardResponse = cc.transmitCmd(cmd);

                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", cmd);
                String rc = "";
                String rv = "";
                int rctr = 0;
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            rc = CardResponse.substring(0,2);
                            rv = CardResponse.substring(2);
                            contentValues.put("rc", rc);
                            contentValues.put("response", rv);
                        } else {
                            contentValues.put("rc", CardResponse);
                        }
                    }
                }
                writeDebugLog(TAG, cmd + " (" +String.valueOf(rctr)+ "x) || " + rv);
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);

                if((!CardResponse.startsWith("AF")) || rv.equals("")){
                    setMessage("Terjadi kesalahan.\nERROR ["+rv+"]");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    Log.e(TAG,"Error when request key card, response "+CardResponse);
                    return null;
                }
                String Keycard = rv;
                writeDebugLog(TAG, "cmd: " + cmd + " || " + Keycard);
                // 7. Card � Get UID
                writeDebugLog(TAG, "UID || " + cData.getUid());
                cmd = "80B0000020" + CardNumber + cData.getUid() + "FF0000030080000000" + Keycard;
                SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 0);
                contentValues.put("cmd", cmd);
                if (SamResponse != null) {
                    Log.i(TAG, SamResponse);
                    if (SamResponse.length() > 0) {
                        if (SamResponse.length() > 2) {
                            contentValues.put("rc", SamResponse);
                            contentValues.put("response", SamResponse);
                        } else {
                            contentValues.put("rc", SamResponse);
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                if (!SamResponse.startsWith("6D")) {
//                    writeDebugLog(TAG, "SAM Authenticate Key : " + SamResponse);
                    String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//                    writeDebugLog(TAG, "Randomkey16B : " + RandomKey16B);
                    // 9. Card � Authenticate Card
                    CardResponse = cc.transmitCmd("AF" + RandomKey16B);
                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 1);
                    contentValues.put("cmd", "AF" + RandomKey16B);
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                contentValues.put("rc", CardResponse.substring(0, 2));
                                contentValues.put("response", CardResponse);
                            } else {
                                contentValues.put("rc", CardResponse);
                            }
                        }
                    }
                    contentValues.put("timestamp", new Date().getTime());
                    tx.insertIntoCmdLog(contentValues);
                    String RandomNumber8B = CardResponse.substring(2);
                    cData.setRandomNumber8B(RandomNumber8B);
//                    writeDebugLog(TAG, "cmd: AF+" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
                    //  10. Card � Get Last Transaction Date
                    CardResponse = cc.transmitCmd("BD03000000070000");
                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 1);
                    contentValues.put("cmd", "BD03000000070000");
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                contentValues.put("rc", CardResponse.substring(0, 2));
                                contentValues.put("response", CardResponse);
                            } else {
                                contentValues.put("rc", CardResponse);
                            }
                        }
                    }
                    contentValues.put("timestamp", new Date().getTime());
                    tx.insertIntoCmdLog(contentValues);

                    if (CardResponse.length() == 2) {
                        setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
                        setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                        btnOk.setVisibility(VISIBLE);
                        Log.e(TAG, "Select Get Last Transaction Date, RESPONSE " + CardResponse);
                        return null;
                    }
                    cData.setLastTransDate(CardResponse.substring(2, 8));
                    cData.setAkumDebet(CardResponse.substring(8, 16));
//                    writeDebugLog(TAG, "cmd: BD03000000070000 || " + CardResponse + " || last trans: " + cData.getLastTransDate() + " akumdebet: " + cData.getAkumDebet());
                    // 11. Card � Get Balance
                    CardResponse = cc.transmitCmd("6C00");
                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 1);
                    contentValues.put("cmd", "6C00");
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                contentValues.put("rc", CardResponse.substring(0, 2));
                                contentValues.put("response", CardResponse);
                            } else {
                                contentValues.put("rc", CardResponse);
                            }
                        }
                    }
                    contentValues.put("timestamp", new Date().getTime());
                    cData.setCardBalance4B(CardResponse.substring(2));
                    cData.setCardBalanceInt(StringLib.HtoI(CardResponse.substring(2)));
                    Calendar startCalendar = Calendar.getInstance();
                    try {
                        startCalendar.setTime(DATE_TOCARD.parse(cData.getLastTransDate()));
//                        startCalendar.setTime(DATE_TOCARD.parse("150801"));//Test Pasif
                    } catch (ParseException e) {

                    }
                    Calendar endCalendar = Calendar.getInstance();
                    long daysFromLastTx = ISO8583Parser.getDateDiff(startCalendar, endCalendar);
//                    writeDebugLog(TAG, "Unused for " + String.valueOf(daysFromLastTx) + " days");
                    if (Arrays.asList(FINANCIALTX).contains(cData.getMsgSI())&&!cData.getLastTransDate().equals("000000")) {
                        if (daysFromLastTx>365) {
                            String chRes = "00";
//                            String chRes = changeStatus("cl");
//                            String chRes = "XX";//Test No Stat Change
                            if (chRes.equals("00")) {
                                setMessage("Kartu anda tidak dapat digunakan.\n" +
                                        "Silahkan lakukan reaktivasi kartu\n" +
                                        "Kartu anda terakhir digunakan" +
                                        String.valueOf(daysFromLastTx)
                                        + " hari\n" +
                                        "yang lalu");
                                btnOk.setVisibility(VISIBLE);
                                cData.setValidationStatus("06");
                                return null;
                            } else {
                                setMessage("Kartu anda tidak dapat digunakan.\n" +
                                        "Silahkan lakukan reaktivasi kartu\n" +
                                        "Kartu anda terakhir digunakan" +
                                        String.valueOf(daysFromLastTx)
                                        + " hari\n" +
                                        "yang lalu");
                                Log.e(TAG, "Penutupan Kartu Gagal : " + chRes);
                                btnOk.setVisibility(VISIBLE);
                                cData.setValidationStatus("06");
                                return null;
                            }
                        }
                    }
                    boolean akumTime = startCalendar.get(Calendar.MONTH)==endCalendar.get(Calendar.MONTH);
                    if (akumTime) {
                        akumTime = startCalendar.get(Calendar.YEAR)==endCalendar.get(Calendar.YEAR);
                    }
                    if (((cData.getMsgSI().equals(SI_PEMBAYARAN)||
                            cData.getMsgSI().equals(SI_DISKON))
                            && (Integer.valueOf(cData.getAkumDebet())
                            +Integer.valueOf(cData.getDeductAmount())
                            > maxDeduct)&&akumTime)||((cData.getMsgSI().equals(SI_PEMBAYARAN)||
                            cData.getMsgSI().equals(SI_DISKON))&&
                            Integer.valueOf(cData.getDeductAmount())>maxDeduct)) {
                        String akumValue = StringLib.strToCurr(cData.getAkumDebet(),"Rp");
                        if (!akumTime) {
                            akumValue = StringLib.strToCurr("0","Rp");
                        }
                        setMessage("Kartu anda tidak dapat digunakan.\n" +
                                "Transaksi akan melebihi limit\n" +
                                "maksimum debit per bulan.\n" +
                                "Akumulasi debit anda pada bulan ini : " +
                                akumValue);
//                        writeDebugLog(TAG, "Akumulasi Debet : " + StringLib.strToCurr(cData.getAkumDebet(), "Rp"));
                        btnOk.setVisibility(VISIBLE);
                        cData.setValidationStatus("55");
                        return null;
                    }

                    cData.setValidationStatus("00");
//                    writeDebugLog("LTD", cData.getLastTransDate());
                    return cData;
                } else {
                    setMessage("Terjadi kesalahan.\nERROR [" + SamResponse + "]");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    Log.e(TAG, "SAM AUTH ERROR : " + SamResponse);
                    btnOk.setVisibility(VISIBLE);
                    cData.setValidationStatus("05");
                    return null;
                }
            } else {
                setMessage("Terjadi kesalahan.\nERROR [" + SamResponse + "]");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                Log.e(TAG, "AID : " + SamResponse);
                btnOk.setVisibility(VISIBLE);
                cData.setValidationStatus("05");
                return null;
            }
        } catch (JSONException e) {
            setMessage(e.getMessage());
            cData.setValidationStatus("88");
            e.printStackTrace();
        } catch (Exception e) {
            setMessage(e.getMessage());
            cData.setValidationStatus("87");
            e.printStackTrace();
            return cData;
        }
        return null;
    }

    private void doRedeem() {
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String vStat = cData.getValidationStatus();
        if (!vStat.equals("00")) {
            return;
        }
        Log.i(TAG, "INQUIRY REDEEM");
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
        String CardResponse = "";
        String cmd = "";
        Calendar startCalendar = Calendar.getInstance();
        try {
            startCalendar.setTime(DATE_TOCARD.parse(cData.getLastTransDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar endCalendar = Calendar.getInstance();
        long daysFromLastTx = ISO8583Parser.getDateDiff(startCalendar, endCalendar);
        if (daysFromLastTx > 365&&!cData.getLastTransDate().equals("000000")) {
            setMessage("Transaksi tidak bisa dilakukan, kartu ditolak");
            Log.e(TAG, "CARD REJECTED CAUSE LAST TRANS DATE > 365 days");
            btnOk.setVisibility(VISIBLE);
            return;
        }

//        writeDebugLog("KIRIM", "MSG_SI " + cData.getMsgSI());
        sendToServer("", cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
    }

    private void redeem() {
        try {
            cekSaldo();
            String vStat = cData.getValidationStatus();
            if (!vStat.equals("00")) {
                writeDebugLog(TAG, "Validation status not zero");
                throw new Exception("Validation status not zero");
            }
            Log.i(TAG, "REDEEM");
            String redeemAmount = cData.getCardBalanceInt();
            cData.setDeductAmount(redeemAmount);
            String CardResponse = "";
            ContentValues contentValues;
            String SamResponse = "";
            setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
            btnOk.setVisibility(GONE);

            // 12. Card � Debit Balance
            String deductAmount = cData.getDeductAmount();
            String reverseAmount3B = StringLib.ItoH(deductAmount);
            CardResponse = cc.transmitCmd("DC00" + reverseAmount3B + "00");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "DC00" + reverseAmount3B + "00");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);

//            writeDebugLog(TAG, "cmd: " + "DC00" + StringLib.ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);

            // 13. SAM � Create Hash
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String lDate = sdf.format(d).substring(2) + cData.gettDate().substring(0,2) + cData.gettDate().substring(2,4);
            String nominalTransaksi = StringLib.nominalTransaksi(deductAmount);
            String TansactionData = StringLib.Hex3(cData.getCardNumber())
                    + StringLib.Hex3(nominalTransaksi) + StringLib.Hex3(lDate)
                    + StringLib.Hex3(cData.gettTime()) + StringLib.Hex3("818001")
                    + StringLib.Hex3(zeroLeftPadding(stan, 6)) + StringLib.Hex3("00")
                    + "FFFFFFFF";
//            writeDebugLog(TAG, "TansactionData : " + TansactionData);
            String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000"
                    + cData.getRandomNumber8B() + TansactionData;
            SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", SamResponse);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 4) {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                        contentValues.put("response", SamResponse);
                    } else {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(2, 10));
            cData.setHash4B(SamResponse.substring(2, 10));
            if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
                Log.e(TAG, "ERROR RESPONSE " + SamResponse);
                btnOk.setVisibility(VISIBLE);
                throw new Exception("Error key from SAM");
            }
            // 14. Card � Write Log
            String CardBalanceBefore3B = StringLib.ItoH(cData.getCardBalanceInt());
            int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(cData.getCardBalanceInt());
            cData.setNewBalance(Integer.toString(balanceAfter));
            String balanceAfter3B = StringLib.ItoH(Integer.toString(balanceAfter));
            String tglReverse = lDate.substring(4,6) + cData.gettDate().substring(2,4)+cData.gettDate().substring(0,2);
            String jamRedeem = cData.gettTime();
            String sendTocard = "3B01000000200000" + cData.getMerchanIdForCardLog() + cData.getTerminalIdForCardLog()
                    + tglReverse + jamRedeem + "FA" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
//            + cData.gettDate() + cData.gettTime() + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
            CardResponse = cc.transmitCmd(sendTocard);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", sendTocard);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "ERROR RESPONSE " + CardResponse);
                btnOk.setVisibility(VISIBLE);
                throw new Exception("Cannot write log transaction");
            }
            String amount = "";
            if (!cData.getLastTransDate().substring(2, 4).equals(lDate.substring(2, 4))) {
                amount = cData.getDeductAmount();
            } else {
                int tmp = Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getDeductAmount());
                amount = tmp + "";
            }
    //        amount = zeroLeftPadding(amount, 8);
            amount = StringLib.nominalUntukLog(StringLib.Int2Hex(amount));
            CardResponse = cc.transmitCmd("3D03000000070000" + DATE_TOCARD.format(new Date()) + amount);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "3D03000000070000" + DATE_TOCARD.format(new Date()) + amount);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                btnOk.setVisibility(VISIBLE);
                throw new Exception("Cannot write log transaction");
            }
            CardResponse = cc.transmitCmd("C7");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "C7");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
//            writeDebugLog(TAG, "new Balance after Deduct: " + cData.getNewBalance());
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                btnOk.setVisibility(VISIBLE);
                throw new Exception("Cannot commit transaction");
            }
            printSizes.clear();
            printSizes.add(new PrintSize(FontSize.TITLE, "Redeem BRIZZI\n"));
            printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu BRIZZI     : ****************\n"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "Nominal Redeem      : " +
                    StringLib.strToCurr(cData.getCardBalanceInt(), "Rp") + "\n"));
            printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "Status Kartu         : Close\n"));
            setMessage("Redeem kartu Brizzi anda berhasil.\n" +
                    "Saldo anda yang di debit adalah Rp. " + cData.getCardBalanceInt());
            btnOk.setVisibility(VISIBLE);
            printPanelVisibility(VISIBLE);
            formReponse = new JSONObject();
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "290000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24301");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cData.getCardNumber());
            cmVal.put("print", cData.getCardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Redeem BRIZZI";
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Saldo Kartu         : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", "Rp " + cData.getRedCardBalance());
            cmVal.put("print", "Rp " + cData.getRedCardBalance());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24303");
            component.put("comp_lbl", "Saldo Deposit       : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", "Rp " + cData.getRedDepoBalance());
            cmVal.put("print", "Rp " + cData.getRedDepoBalance());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Fee                 : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", "Rp " + cData.getRedFee());
            cmVal.put("print", "Rp " + cData.getRedFee());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Total Redeem        : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", "Rp " + cData.getRedTotal());
            cmVal.put("print", "Rp " + cData.getRedTotal());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Status Kartu        : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", "CLOSE");
            cmVal.put("print", "CLOSE");
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            screen.put("title", mTitle);
            comps.put("comp", comp);
            screen.put("comps", comps);
            screen.put("server_date", cData.gettDate());
            screen.put("server_time", jamRedeem);
            screen.put("server_ref", cData.getServerRef());
            formReponse.put("screen", screen);
            formReponse.put("server_date", cData.gettDate());
            formReponse.put("server_time", jamRedeem);
            formReponse.put("server_ref", cData.getServerRef());
        } catch (Exception e) {
//            sendReversal(cData.getRandomSam24B(), cData.getCardBalanceInt(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
            tx.reverseLastTransaction(context);
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            e.printStackTrace();
        } finally {
            changeStatus("cl");
//            writeMessageLog();
            btnOk.setVisibility(VISIBLE);
            printPanelVisibility(VISIBLE);
            cData.setMsgSI(SI_REDEEM_NEXT);
            writeMessageLog();
            formListener.onSuccesListener(formReponse);
        }
    }

    private void doTopup() {
        String CardResponse = "";

        String SamResponse = "";
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        try {
            if (Integer.parseInt(cData.getTopupAmount())+Integer.parseInt(cData.getCardBalanceInt())>1000000) {
                setMessage("Isi ulang akan melebihi limit");
                Log.e(TAG, "Topup Over Limit");
                btnOk.setVisibility(VISIBLE);
                return;
            }
        } catch (Exception e) {
            setMessage("Terjadi kesalahan.\nERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String vStat = cData.getValidationStatus();
        if (!vStat.equals("00")) {
            return;
        }
        Log.i(TAG, "TOPUP ONLINE");
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
        btnOk.setVisibility(GONE);
        // 12. SAM � Get Key Topup
        String cmd = "80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000";
        SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", cmd);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\n ERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        String randomSam24B = SamResponse.substring(2, SamResponse.length() - 4);
        String randomSam24B = SamResponse.substring(0, SamResponse.length() - 4);
        randomSam24B = randomSam24B + zeroLeftPadding(cData.getCardBalanceInt()+"00", 12);
//        writeDebugLog(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + randomSam24B);
        // 13. HOST Get Auth Key Topup
        sendToServer(randomSam24B, cData.getTopupAmount(), cData.getTrack2Data(), cData.getPin(), cData.getMsgSI());
    }

    private void nextVoidSecond() {
//        writeDebugLog(TAG, "NEXT VOID STEP, HOST RESPONSE: " + cData.getHostResponse());
        String keyTopup24B = cData.getHostResponse();
        if (keyTopup24B.length()>48) {
            keyTopup24B = keyTopup24B.substring(0,48);
        }
        Log.i("Key", keyTopup24B);
        if (!(keyTopup24B.length() == 48)) {
            Log.e(TAG, "Error Response from server !!" + " || response: " + keyTopup24B);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        // 14. Card � Select AID 3
        String CardResponse = cc.transmitCmd("5A030000");
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "5A030000");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Select AID 3 error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: 5A030000" + " || response: " + CardResponse);
        //15. Card � Request Key Card 01
        CardResponse = cc.transmitCmd("0A01");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
//        writeDebugLog(TAG, "cmd: 0A01" + " || response: " + CardResponse);
        int i = 0;
        String keyCard08B = CardResponse.substring(2);
        // 16. SAM � Authenticate Topup
        String sendtosam = "80B2000037" + keyTopup24B + cData.getCardNumber() + cData.getUid() + "0000030180000000" + keyCard08B;
        String SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", sendtosam);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);

        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\nERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Sam Authenticate Topup error " + SamResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: Authenticate Topup || response: " + SamResponse);
        String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//        writeDebugLog(TAG, "cmd: RandomKey16B || send: AF" + RandomKey16B);
        // 17. Card � Authenticate Card
        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Authenticate Card error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String RandomNumber8B = CardResponse.substring(2);
//        writeDebugLog(TAG, "cmd: AF" + RandomKey16B + " || response: " + CardResponse);
        // 18. Card � Credit Balance
        String topupAmount = StringLib.ItoH(cData.getTopupAmount()); //--- nilai yg akan di topup
        String transmit = "0C00" + topupAmount + "00";
        CardResponse = cc.transmitCmd(transmit);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", transmit);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Credit Balance error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String CardBalance4B = CardResponse.substring(2);
//        writeDebugLog(TAG, "cmd: " + transmit + " || response: " + CardResponse + "| Card Balance = " + CardBalance4B);

        // 19. SAM � Create Hash

//        String transactionData = StringLib.Hex3(cData.getCardNumber())
//                + StringLib.Hex3(StringLib.nominalTransaksi(cData.getTopupAmount()))
//                + StringLib.Hex3(cData.gettDate()) + StringLib.Hex3(cData.gettTime())
//                + StringLib.Hex3("818001") + StringLib.Hex3(zeroLeftPadding(stan, 6))
//                + StringLib.Hex3("00") + "FFFFFFFF";

//        sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000" + RandomNumber8B + transactionData;
//        Log.i(TAG,"SEMD TO SAM "+sendtosam);
//        SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
//        Log.i(TAG,"SAM – Create Hash "+SamResponse);
//        String Hash4B = SamResponse.substring(0, SamResponse.length() - 4);
//        if (!SamResponse.endsWith("9000")) {
//            Log.e(TAG, "SAM – Create Hash error " + SamResponse);
//            Log.e(TAG, "Hash "+ Hash4B);
//            return;
//        }
//        cData.setHash4BTopup(Hash4B);
        // 20. Card � Write Log
        String balanceBeforeint = StringLib.HtoI(cData.getCardBalance4B());
//        writeDebugLog(TAG, "balanceBefore3B step 1 : " + balanceBeforeint);
        int bAfter = Integer.parseInt(balanceBeforeint) - Integer.parseInt(cData.getTopupAmount());
//        writeDebugLog(TAG, "BalanceAfter int : " + bAfter);
        String bAfter3B = StringLib.ItoH(Integer.toString(bAfter));
//        writeDebugLog(TAG, "BalanceAfter3B : " + bAfter3B);
        String balanceBefore3B = StringLib.ItoH(balanceBeforeint);
//        writeDebugLog(TAG, "balanceBefore3B step 2 : " + balanceBefore3B);
        String jamVoid = svrTm.replaceAll(":","");
        if (jamVoid!=null) {
            if (jamVoid.equals("")) {
                jamVoid = cData.gettTime();
            }
        } else {
            jamVoid = cData.gettTime();
        }
        String cmdWritelog = "3B01000000200000" + cData.getMerchanIdForCardLog() + cData.getTerminalIdForCardLog() + cData.gettDate() + jamVoid + "ED" + StringLib.ItoH(cData.getTopupAmount()) + balanceBefore3B + bAfter3B;

        CardResponse = cc.transmitCmd(cmdWritelog);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", cmdWritelog);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
//        writeDebugLog(TAG, "CardResponse : " + CardResponse);
        if (CardResponse.startsWith("AF")) {
            CardResponse = cc.transmitCmd("AF");
//            writeDebugLog(TAG, "CardResponse after AF : " + CardResponse);
        }

        if (!(CardResponse.equals("00"))) {
            Log.e(TAG, "Write log error " + CardResponse);
//            return;
        }

        // 21. Card � Write Last transaction
        String akumdebet = "00000000";
        if (cData.getLastTransDate().substring(0, 4).equals(cData.gettDate().substring(0, 4))) {
            akumdebet = Integer.toString(Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getTopupAmount()));
        } else {
            akumdebet = cData.getTopupAmount();
        }
        akumdebet = StringLib.nominalUntukLog(StringLib.Int2Hex(akumdebet));
//        writeDebugLog(TAG, "akumdebet : " + akumdebet);
        CardResponse = cc.transmitCmd("3D03000000070000" + cData.getYYMMDD() + akumdebet);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "3D03000000070000" + cData.getYYMMDD() + akumdebet);
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!(CardResponse.equals("00"))) {
            Log.e(TAG, "Write Last transaction error " + CardResponse);
//            return;
        }
//        writeDebugLog(TAG, "CardResponse : " + CardResponse);
        CardResponse = cc.transmitCmd("C7");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "C7");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!(CardResponse.equals("00"))) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Commit Transaction error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
        writeDebugLog("EDCLOG", "update void read (2148)");
        String updLog = "update edc_log set reversed = 't' where log_id = " + logid;
        String q = "select * from edc_log where log_id = " + logid;
        Cursor getstan = clientDB.rawQuery(q, null);
        String vstan = "";
        if (getstan.moveToFirst()) {
            vstan = getstan.getString(getstan.getColumnIndex("stan"));
        }
        getstan.close();
        clientDB.execSQL(updLog);
        clientDB.close();

        StringBuilder builder = new StringBuilder();

        builder.append("Void sukses");
        printSizes.clear();
        printSizes.add(new PrintSize(FontSize.TITLE, "Void Transaksi BRIZZI\n"));
        printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu     : " + cData.getCardNumber() + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Trace #      : " + vstan + "\n"));
        CardData cd = cekSaldo();
        printSizes.add(new PrintSize(FontSize.NORMAL, "Jumlah Void  : " +
                StringLib.strToCurr(cData.getTopupAmount(), "Rp") + "\n"));
        builder.append("\n");
        builder.append("Jumlah saldo anda sekarang adalah " + StringLib.strToCurr(cd.getCardBalanceInt(), "Rp"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Saldo Akhir  : " +
                StringLib.strToCurr(cd.getCardBalanceInt(), "Rp") + "\n"));
        setMessage(builder.toString());
        printPanelVisibility(VISIBLE);
        btnOk.setVisibility(VISIBLE);
        formReponse = new JSONObject();
        try {
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "2C1000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "2C101");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cData.getCardNumber());
            cmVal.put("print", cData.getCardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Void BRIZZI";
            component = new JSONObject();
//            component.put("visible", "true");
//            component.put("comp_type", "1");
//            component.put("comp_id", "2C102");
//            component.put("comp_lbl", "Trace #             : ");
//            component.put("seq", "1");
//            compValues = new JSONObject();
//            compValue = new JSONArray();
//            cmVal = new JSONObject();
//            cmVal.put("value", vstan);
//            cmVal.put("print", vstan);
//            compValue.put(cmVal);
//            compValues.put("comp_value", compValue);
//            component.put("comp_values", compValues);
//            comp.put(component);
            component = createComponent("2C103","Jumlah Void         : ",StringLib.strToCurr(cData.getTopupAmount(), "Rp"),"1");
            comp.put(component);
            component = createComponent("2C104","Saldo Akhir         : ",StringLib.strToCurr(cd.getCardBalanceInt(), "Rp"),"2");
            comp.put(component);
            screen.put("title", mTitle);
            comps.put("comp", comp);
            screen.put("comps", comps);
            screen.put("server_date", svrDt);
            screen.put("server_time", svrTm);
            screen.put("server_ref", cData.getServerRef());
            formReponse.put("screen", screen);

            formReponse.put("server_date", svrDt);
            formReponse.put("server_time", svrTm);
            formReponse.put("server_ref", cData.getServerRef());
            updateVoidLog();
            formListener.onSuccesListener(formReponse);
        } catch (Exception e) {
            setMessage("Terjadi Kesalahan");
            btnOk.setVisibility(VISIBLE);
            e.printStackTrace();
        }
    }

    private void reaktivasi() {
        if(cekSaldo() == null){
            setMessage("Terjadi kesalahan....");
            return;
        }
        if (cData.getValidationStatus().equals("87")) {
            return;
        }
        Log.i(TAG, "REAKTIVASI");
        btnOk.setVisibility(GONE);
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
//        writeDebugLog(TAG, cData.getMsgSI());
//        writeDebugLog(TAG, cData.getCardNumber());
//        writeDebugLog(TAG, cData.getLastTransDate());
        sendToServer(cData.getLastTransDate(), cData.getTopupAmount(), cData.getTrack2Data(), cData.getPin(), cData.getMsgSI());

    }

    protected String zeroLeftPadding(final String pParam, final int pLength) {
        String tAmount = pParam;

        while (tAmount.length() < pLength) {
            tAmount = "0" + tAmount;
        }

        return tAmount;
    }

    private void nextTopupStep() throws Exception {
        Log.i(TAG, "NEXT TOPUP STEP");
//        writeDebugLog(TAG, "NEXT TOPUP STEP, HOST RESPONSE: " + cData.getHostResponse());
        String keyTopup24B = cData.getHostResponse();
        if (keyTopup24B.length()>48) {
            keyTopup24B = keyTopup24B.substring(0,48);
        }
        Log.i("Key", keyTopup24B);
        if (!(keyTopup24B.length() == 48)) {
            Log.e(TAG, "Error Response from server !!" + " || response: " + keyTopup24B);
            btnOk.setVisibility(VISIBLE);
            throw new Exception("Error server response");
        }
        // 14. Card � Select AID 3
        String CardResponse = cc.transmitCmd("5A030000");
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "5A030000");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);


        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            Log.e(TAG, "Select AID 3 error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            throw new Exception("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
        }
//        writeDebugLog(TAG, "cmd: 5A030000" + " || response: " + CardResponse);
        //15. Card � Request Key Card 01
        CardResponse = cc.transmitCmd("0A01");
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);

//        writeDebugLog(TAG, "cmd: 0A01" + " || response: " + CardResponse);
        int i = 0;
        String keyCard08B = CardResponse.substring(2);
        // 16. SAM � Authenticate Topup
        String sendtosam = "80B2000037" + keyTopup24B + cData.getCardNumber() + cData.getUid() + "0000030180000000" + keyCard08B;
        String SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", sendtosam);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);

        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\nERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Sam Authenticate Topup error " + SamResponse);
            btnOk.setVisibility(VISIBLE);
            throw new Exception("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
        }
//        writeDebugLog(TAG, "cmd: Authenticate Topup || response: " + SamResponse);
        String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//        writeDebugLog(TAG, "cmd: RandomKey16B || send: AF" + RandomKey16B);
        // 17. Card � Authenticate Card
        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
        contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 1);
        contentValues.put("cmd", "0A01");
        if (CardResponse != null) {
            if (CardResponse.length() > 0) {
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!CardResponse.startsWith("00")) {
            setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            Log.e(TAG, "Authenticate Card error " + CardResponse);
            btnOk.setVisibility(VISIBLE);
            throw new Exception("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
        }
        String RandomNumber8B = CardResponse.substring(2);
//        writeDebugLog(TAG, "cmd: AF" + RandomKey16B + " || response: " + CardResponse);
        try {
            // 18. Card � Credit Balance
            String topupAmount = StringLib.ItoH(cData.getTopupAmount()); //--- nilai yg akan di topup
            String transmit = "0C00" + topupAmount + "00";
            CardResponse = cc.transmitCmd(transmit);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", transmit);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!CardResponse.startsWith("00")) {
                throw new Exception("Tidak dapat melakukan credit pada kartu");
            }
            String CardBalance4B = CardResponse.substring(2);
//            writeDebugLog(TAG, "cmd: " + transmit + " || response: " + CardResponse + "| Card Balance = " + CardBalance4B);

            // 19. SAM � Create Hash

            String transactionData = StringLib.Hex3(cData.getCardNumber())
                    + StringLib.Hex3(StringLib.nominalTransaksi(cData.getTopupAmount()))
                    + StringLib.Hex3(cData.gettDate())
                    + StringLib.Hex3(cData.gettTime())
                    + StringLib.Hex3("818001")
                    + StringLib.Hex3(stan)
                    + StringLib.Hex3("00") + "FFFFFFFF";

//            sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid()
//                    + "FF0000030080000000" + RandomNumber8B + transactionData;
//            Log.i(TAG,"SEMD TO SAM "+sendtosam);
//            SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
//            Log.i(TAG,"SAM – Create Hash "+SamResponse);
//            String Hash4B = SamResponse.substring(0, SamResponse.length() - 4);
//            if (!SamResponse.endsWith("9000")) {
//                Log.e(TAG, "SAM – Create Hash error " + SamResponse);
//                Log.e(TAG, "Hash "+ Hash4B);
//                return;
//            }
//            cData.setHash4BTopup(Hash4B);
            // 20. Card � Write Log
            String balanceBeforeint = StringLib.HtoI(cData.getCardBalance4B());
//            writeDebugLog(TAG, "balanceBefore3B step 1 : " + balanceBeforeint);
            int bAfter = Integer.parseInt(balanceBeforeint) + Integer.parseInt(cData.getTopupAmount());
//            writeDebugLog(TAG, "BalanceAfter int : " + bAfter);
            String bAfter3B = StringLib.ItoH(Integer.toString(bAfter));
//            writeDebugLog(TAG, "BalanceAfter3B : " + bAfter3B);
            String balanceBefore3B = StringLib.ItoH(balanceBeforeint);
//            writeDebugLog(TAG, "balanceBefore3B step 2 : " + balanceBefore3B);
            String jamAktivasi = svrTm.replaceAll(":","");
            if (jamAktivasi!=null) {
                if (jamAktivasi.equals("")) {
                    jamAktivasi = cData.gettTime();
                }
            } else {
                jamAktivasi = cData.gettTime();
            }
            String cmdWritelog = "3B01000000200000" +
                    cData.getMerchanIdForCardLog() +
                    cData.getTerminalIdForCardLog() +
                    cData.gettDate() +
                    jamAktivasi + "EC" +
                    StringLib.ItoH(cData.getTopupAmount()) +
                    balanceBefore3B +
                    bAfter3B;

//            writeDebugLog(TAG, "cmd Write log : " + cmdWritelog);
            CardResponse = cc.transmitCmd(cmdWritelog);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", cmdWritelog);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "CardResponse : " + CardResponse);

            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "Write log error " + CardResponse);
            }

            // 21. Card � Write Last transaction
            String akumdebet = "00000000";
            if (cData.getLastTransDate().substring(0, 4).equals(cData.gettDate().substring(0, 4))) {
                akumdebet = Integer.toString(Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getTopupAmount()));
            } else {
                akumdebet = cData.getTopupAmount();
            }
            akumdebet = StringLib.nominalUntukLog(StringLib.Int2Hex(akumdebet));
//            writeDebugLog(TAG, "akumdebet : " + akumdebet);
            CardResponse = cc.transmitCmd("3D03000000070000" + cData.getYYMMDD() + akumdebet);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "3D03000000070000" + cData.getYYMMDD() + akumdebet);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
//            writeDebugLog(TAG, "cmd : " + "3D03000000070000" + cData.getYYMMDD() + akumdebet + " || CardResponse : " + CardResponse);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "Write Last transaction error " + CardResponse);
            }
//            writeDebugLog(TAG, "CardResponse : " + CardResponse);
            CardResponse = cc.transmitCmd("C7");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "C7");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!(CardResponse.equals("00"))) {
                throw new Exception("Tidak dapat melakukan commit transaksi");
            }
//            writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);

            StringBuilder builder = new StringBuilder();

            builder.append("Jumlah topup yang anda masukkan adalah " + StringLib.strToCurr(cData.getTopupAmount(), "Rp"));
            printSizes.clear();
            printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu : " + cardNumber() + "\n"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "Nilai Topup : " +
                    StringLib.strToCurr(cData.getTopupAmount(), "Rp") + "\n"));
            builder.append("\n");
            CardData cd = cekSaldo();
            builder.append("Jumlah saldo anda sekarang adalah " + StringLib.strToCurr(cd.getCardBalanceInt(), "Rp"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "Jumlah Saldo : " + StringLib.strToCurr(cd.getCardBalanceInt(), "Rp") + "\n"));
            setMessage(builder.toString(), Gravity.CENTER);
            printPanelVisibility(VISIBLE);
            btnOk.setVisibility(VISIBLE);
            formReponse = new JSONObject();
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "250000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24301");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cardNumber());
            cmVal.put("print", cardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Isi Ulang BRIZZI";
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Nilai Topup         : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cData.getTopupAmount(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cData.getTopupAmount(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Jumlah Saldo        : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cd.getCardBalanceInt(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cd.getCardBalanceInt(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            screen.put("title", mTitle);
            comps.put("comp", comp);
            screen.put("comps", comps);
//            screen.put("server_date", svrDt);
//            screen.put("server_time", svrTm);
//            screen.put("server_ref", cData.getServerRef());
            formReponse.put("screen", screen);
            if (!cData.getServerRef().equals("")) {
                formReponse.put("server_ref", cData.getServerRef());
            }
            formReponse.put("server_date", svrDt);
            formReponse.put("server_time", svrTm);
            writeMessageLog();
            formListener.onSuccesListener(formReponse);
        } catch (Exception e) {
            setMessage(e.getMessage());
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            e.printStackTrace();
            CardResponse = cc.transmitCmd("A7");
            btnOk.setVisibility(VISIBLE);
        }
    }

    private String sendToServer(String randomSam24B, String amount, String track2Data, String pin, String msgSI) {
        con = new ConAsync(this, context);
        con.setRequestMethod("POST", getPostData(randomSam24B, amount, track2Data, pin, msgSI));
        con.execute(CommonConfig.HTTP_POST);
        return "sudah kirim";
    }

    private String sendReversal(String randomSam24B, String amount, String track2Data, String pin, String msgSI) {
        con = new ConAsync(this, context);
        con.setAsReversal();
        con.setRequestMethod("POST", getPostData(randomSam24B, amount, track2Data, pin, msgSI));
        con.execute(CommonConfig.HTTP_POST);
        return "sudah kirim";
    }

    private String getPostData(String randomSam24B, String amount, String cardNumber, String pin, String msgSI) {
        String retval = null;
        TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        if (imei.length() <= 2)
            imei = "358586060001548";
        JSONObject rootObj = new JSONObject();
        JSONObject obj = new JSONObject();
        String datas = "";
        if (msgSI.equals(SI_TOPUP_ONLINE)) {
            datas = amount + "|" + cardNumber + "|" + pin + "|" + randomSam24B; // + "0043349600    ";//zeroLeftPadding(amount, 12);
        } else if (msgSI.equals(SI_AKTIFASI_DEPOSIT)) {
            datas = cardNumber + "|" + randomSam24B + "|" + zeroLeftPadding(amount+"00", 10);
        } else if (msgSI.equals(SI_INFO_DEPOSIT)) {
            datas = cardNumber;
        }  else if ( msgSI.equals(SI_VOID_REFUND)) {
            datas = zeroLeftPadding(amount.substring(0,amount.length()-2), 10) + "|" + randomSam24B + "|" + zeroLeftPadding(cData.getCardBalanceInt() + "00", 10);
        } else if (msgSI.equals(SI_REDEEM)) {
            datas = cData.getCardNumber() + "|" + zeroLeftPadding(cData.getCardBalanceInt() + "00", 10);
        } else if (msgSI.equals(SI_REAKTIVASI)) {
            String lastTransDate = null;
            try {
                lastTransDate = DATE.format(DATE_TOCARD.parse(cData.getLastTransDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            lastTransDate = "250115";
            datas = cData.getCardNumber() + "|" + zeroLeftPadding(cData.getCardBalanceInt(), 10) + "|" + lastTransDate;
        }
        try {
            obj.put("msg_id", imei + StringLib.getStringDate());
            obj.put("msg_ui", imei);
            obj.put("msg_si", msgSI);
            obj.put("msg_dt", datas);
            if (stan!=null) {
                obj.put("msg_stan", stan);
                writeDebugLog("TAP2SERV", "Forced stan " + stan);
            }
            rootObj.put("msg", obj);

            retval = rootObj.toString();
        } catch (JSONException e) {
            Log.e("POSTBRIZZI", datas + " " + msgSI);
            e.printStackTrace();
        }

        return retval;
    }

    private void nextReaktivasi() {
        cekSaldo();
        Log.i(TAG, "PROSES REAKTIVASI");
        btnOk.setVisibility(GONE);
        printPanelVisibility(GONE);
        try {
            printData = new JSONObject();
            printData.put("title", "Reaktivasi Kartu");
            String CardResponse = "";
            String SamResponse = "";
            setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
            try {
                if (DATE.format(DATE_TOCARD.parse(cData.getLastTransDate())).substring(2).equals(cData.gettDate().substring(2))
                        && Integer.parseInt(cData.getAkumDebet()) > maxDeduct) {
                    setMessage("Kartu anda ditolak.\nSilahkan Menghubungi BRI Call Center");
                Log.e(TAG, "Card Rejected Akumulasi Debet" + cData.getAkumDebet() + "|last Trans Date " + cData.getLastTransDate());
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                setMessage("Transaksi tidak dapat dilakukan\nSilahkan mencoba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }

            // 12. Card � Debit Balance
            String deductAmount = cData.getDeductAmount();
            String reverseAmount3B = StringLib.ItoH(deductAmount);
            CardResponse = cc.transmitCmd("DC00" + reverseAmount3B + "00");
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "DC00" + reverseAmount3B + "00");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);

//            writeDebugLog(TAG, "cmd: " + "DC00" + StringLib.ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);
            // 13. SAM � Create Hash
            cData.settDate(StringLib.getStringDate2());
            cData.settTime(StringLib.getStringTime());
            String nominalTransaksi = StringLib.nominalTransaksi(deductAmount);
            String TansactionData = StringLib.Hex3(cData.getCardNumber()) +
                    StringLib.Hex3(nominalTransaksi) + StringLib.Hex3(cData.gettDate()) +
                    StringLib.Hex3(cData.gettTime())
                    + StringLib.Hex3("818001") //proc code old -> 818001
                    + StringLib.Hex3(zeroLeftPadding(stan, 6)) //ref no
                    + StringLib.Hex3("00")  // batch no
                    + "FFFFFFFF";
//            writeDebugLog(TAG, "TansactionData : " + TansactionData);
            String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() +
                    "FF0000030080000000" + cData.getRandomNumber8B() + TansactionData;
            SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", SamResponse);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 4) {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                        contentValues.put("response", SamResponse);
                    } else {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(2, 10));
            cData.setHash4B(SamResponse.substring(2, 10));
            if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
                Log.e(TAG, "ERROR RESPONSE " + SamResponse);
                setMessage("Transaksi tidak dapat dilakukan\nSilahkan mencoba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            // 14. Card � Write Log
            String CardBalanceBefore3B = StringLib.ItoH(cData.getCardBalanceInt());
            int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(deductAmount);
            if (balanceAfter < 0) {
                balanceAfter = 0;
            }
            cData.setNewBalance(Integer.toString(balanceAfter));
            String balanceAfter3B = StringLib.ItoH(Integer.toString(balanceAfter));
            String tglReverse = cData.gettDate().substring(4,6) + cData.gettDate().substring(2,4)+cData.gettDate().substring(0,2);
            String jamReaktivasi = svrTm.replaceAll(":","");
            if (jamReaktivasi!=null) {
                if (jamReaktivasi.equals("")) {
                    jamReaktivasi = cData.gettTime();
                }
            } else {
                cData.gettTime();
            }
            String sendTocard = "3B01000000200000" + cData.getMerchanIdForCardLog() +
                    cData.getTerminalIdForCardLog() + tglReverse +
                    jamReaktivasi + "5F" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
            CardResponse = cc.transmitCmd(sendTocard);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", sendTocard);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "ERROR RESPONSE " + CardResponse);
                setMessage("Cannot write into card storage");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            String amount = "";
            if (!cData.getLastTransDate().substring(2, 4).equals(cData.gettDate().substring(2, 4))) {
                amount = cData.getDeductAmount();
            } else {
                int tmp = Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getDeductAmount());
                amount = tmp + "";
            }
            amount = StringLib.nominalUntukLog(StringLib.Int2Hex(amount));
            CardResponse = cc.transmitCmd("3D03000000070000" + DATE_TOCARD.format(new Date()) + amount);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "3D03000000070000" + DATE_TOCARD.format(new Date()) + amount);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                setMessage("Cannot write into card storage");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            CardResponse = cc.transmitCmd("C7");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "C7");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);

            if (!(CardResponse.equals("00")||CardResponse.equals("0C"))) {
                Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                setMessage("Cannot write into card storage");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }

            CardResponse = cc.transmitCmd("5A010000");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "5A010000");
            if (CardResponse.length() > 2) {
                contentValues.put("rc", CardResponse.substring(0, 2));
                contentValues.put("response", CardResponse);
            } else {
                contentValues.put("rc", CardResponse);
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd:5A010000 || " + CardResponse);
            // 4. Card � Get Card Status
            CardResponse = cc.transmitCmd("BD01000000200000");
            String statusAfter = cData.getStatusAfter().equals("AKTIF") ? AKTIF_STATUS : NONAKTIF_STATUS;
            String cardStatus = CardResponse.substring(8,12);
            String cardIssueDate = StringLib.getDDMMYY();
//            writeDebugLog(TAG, "Current Status : " + cardStatus);

            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "BD01000000200000");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
//            writeDebugLog(TAG, "cmd:BD01000000200000 || " + CardResponse);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            CardResponse = cc.transmitCmd("0A00");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "0A00");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
//            writeDebugLog(TAG, "cmd:0A00 || " + CardResponse);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues); //
            String rc = "";
            String rv = "";
            int rctr = 0;
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        rc = CardResponse.substring(0, 2);
                        rv = CardResponse.substring(2);
                        contentValues.put("rc", rc);
                        contentValues.put("response", rv);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
//            writeDebugLog(TAG, "0A00 (" + String.valueOf(rctr) + "x) || " + rv);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);

            if (!(CardResponse.startsWith("00") || CardResponse.startsWith("AF"))) {
                setMessage("Terjadi kesalahan.\nERROR [" + CardResponse.substring(0, 2) + "]");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                Log.e(TAG, "Error when request key card, response " + CardResponse);
                return;
            }
            String Keycard = rv;
//            writeDebugLog(TAG, "cmd: 0A00 || " + Keycard);
            // 7. Card � Get UID
//            writeDebugLog(TAG, "UID || " + cData.getUid());
            String cmd = "80B0000020" + cData.getCardNumber() + cData.getUid() + "FF0000010080000000" + Keycard;
//            writeDebugLog(TAG, cmd);
            SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 0);
            contentValues.put("cmd", cmd);
            if (SamResponse != null) {
                writeDebugLog(TAG, SamResponse);
                if (SamResponse.length() > 0) {
                    if (SamResponse.length() > 2) {
                        contentValues.put("rc", SamResponse);
                        contentValues.put("response", SamResponse);
                    } else {
                        contentValues.put("rc", SamResponse);
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!SamResponse.startsWith("6D")) {
                writeDebugLog(TAG, "SAM Authenticate Key : " + SamResponse);
                String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
                writeDebugLog(TAG, "Randomkey16B : " + RandomKey16B);
                // 9. Card � Authenticate Card
                CardResponse = cc.transmitCmd("AF" + RandomKey16B);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "AF" + RandomKey16B);
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse);
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                String RandomNumber8B = CardResponse.substring(2);
                CardResponse = CardResponse.substring(0, 2);
                cData.setRandomNumber8B(RandomNumber8B);
                writeDebugLog(TAG, "cmd: AF" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
                if (!(CardResponse.equals("00"))) {
                    Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                    setMessage("Authentication failed");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }

                String statusSuffix = StringLib.fillZero("0", 54);
                cmd = "3D01000000200000" + cardIssueDate + statusAfter + statusSuffix;
                writeDebugLog(TAG, cmd);
                CardResponse = cc.transmitCmd(cmd);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", cmd);
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                        }
                    }
                }
                writeDebugLog(TAG, "cmd:" + cmd + " || " + CardResponse);
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                if (!(CardResponse.equals("00"))) {
                    Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                    setMessage("Change status failed");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                CardResponse = cc.transmitCmd("C7");
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "C7");
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
                writeDebugLog(TAG, "new Balance after Reaktivasi: " + cData.getNewBalance());
                if (!(CardResponse.equals("00") || CardResponse.equals("0C"))) {
                    Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                    setMessage("Commit Transaction Error");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
            }
            JSONObject resp = new JSONObject();
            resp.put("messageId", "1234");
            resp.put("no_kartu", cData.getCardNumber());
            resp.put("lama_pasif", cData.getLamaPasif());
            resp.put("nom_sisa", cData.getNewBalance());
            resp.put("nom_depo", cData.getSaldoDeposit());
            resp.put("nom_admin", cData.getDeductAmount());
            resp.put("statusafter", StringLib.hexStringToAscii(statusAfter));
            MenuListResolver mlr = new MenuListResolver();
            formReponse = new JSONObject();
            formReponse = mlr.loadMenu(context, "2A2000F", resp);
//            writeDebugLog(TAG, formReponse.toString());
            setMessage("Reaktivasi Sukses");
            printPanelVisibility(VISIBLE);
            formReponse.put("server_date", svrDt.replaceAll("-","").substring(4));
            formReponse.put("server_time", svrTm.replaceAll(":",""));
            writeMessageLog();
            formListener.onSuccesListener(formReponse);
        } catch (JSONException ex) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            String CardResponse = cc.transmitCmd("A7");
            Log.e(TAG, "Status Rollback : " + CardResponse);
            ex.printStackTrace();
            printPanelVisibility(GONE);
            return;
        } catch (Exception ex) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            String CardResponse = cc.transmitCmd("A7");
            Log.e(TAG, "Status Rollback : " + CardResponse);
            ex.printStackTrace();
            printPanelVisibility(GONE);
            return;
        }
        btnOk.setVisibility(VISIBLE);
    }

    private void deduct(boolean diskon) {
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String minDeduct = preferences.getString("minimum_deduct",CommonConfig.DEFAULT_MIN_BALANCE_BRIZZI);
        String amtDeduct = cData.getDeductAmount();
        // start check limit tx count
        writeDebugLog("EDCLOG", "read (3106)");
        String qry = "select count(*) unsettled from edc_log where service_id like 'A24%' " +
                "and (settled <> 't' or settled is null)";
        Cursor sData = clientDB.rawQuery(qry, null);
        if (sData.moveToFirst()) {
            int unsettled = sData.getInt(0);
            if (unsettled>499) {
                setMessage("Data transaksi telah mencapai jumlah maksimum\nSilahkan lakukan settlement");
                btnOk.setVisibility(VISIBLE);
                sData.close();
                return;
            }
        }
        sData.close();
        // end check limit count
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String vStat = cData.getValidationStatus();
        if (!vStat.equals("00")) {
            return;
        }
        if (!diskon) {
            Log.i(TAG, "PEMBAYARAN_NORMAL");
        } else {
            Log.i(TAG, "PEMBAYARAN_DISKON");
        }
        btnOk.setVisibility(GONE);
        printPanelVisibility(GONE);
        try {
            printData = new JSONObject();
            printData.put("title", "Pembayaran BRIZZI");
            String CardResponse = "";
            String SamResponse = "";
            setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
            try {
                if (DATE.format(DATE_TOCARD.parse(cData.getLastTransDate())).substring(2).equals(cData.gettDate().substring(2))
                        && Integer.parseInt(cData.getAkumDebet()) > maxDeduct) {
                    setMessage("Kartu anda ditolak.\nSilahkan Menghubungi BRI Call Center");
                    Log.e(TAG, "Card Rejected Akumulasi Debet" + cData.getAkumDebet() + "|last Trans Date " + cData.getLastTransDate());
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                btnOk.setVisibility(VISIBLE);
                return;
            }

            // 12. Card � Debit Balance
            String deductAmount = cData.getDeductAmount();
            Discount dc = new Discount();
            dc.setContext(context);
            String[] diskonData = dc.getDiscount(deductAmount);
            if (diskon) {
                deductAmount = diskonData[2];
                if (deductAmount.startsWith("-")) {
                    Log.e(TAG, "NEGATIVE AMOUNT : " + deductAmount);
                    setMessage("Nominal harus lebih besar dari diskon");
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                cData.setDeductAmount(deductAmount);
            }
            try {
                if (Integer.parseInt(cData.getCardBalanceInt())-Integer.parseInt(deductAmount)<Integer.parseInt(minDeduct)) {
                    setMessage("Saldo kurang dari saldo minimal BRIZZI");
                    Log.e(TAG, "Tx Rejected : saldo  " + cData.getCardBalanceInt() + " < min allowed " + minDeduct);
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                btnOk.setVisibility(VISIBLE);
                return;
            }
            String reverseAmount3B = StringLib.ItoH(deductAmount);
            CardResponse = cc.transmitCmd("DC00" + reverseAmount3B + "00");
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "DC00" + reverseAmount3B + "00");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);

//            writeDebugLog(TAG, "cmd: " + "DC00" + StringLib.ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);

            // 13. SAM � Create Hash
            cData.settDate(StringLib.getStringDate2());
            cData.settTime(StringLib.getStringTime());
            int batchno = 0;
            String getBatchNo = "select batch from holder";
            Cursor batchNo = clientDB.rawQuery(getBatchNo, null);
            if (batchNo!=null) {
                batchNo.moveToFirst();
                batchno = batchNo.getInt(0);
            }
            batchNo.close();
            String batchNumber = ("00" + String.valueOf(batchno));
            batchNumber = batchNumber.substring(batchNumber.length()-2);
            String nominalTransaksi = StringLib.nominalTransaksi(deductAmount);
            String txDate = cData.gettDate().substring(4,6) + cData.gettDate().substring(2,4) + cData.gettDate().substring(0,2);
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String gtxYear = sdf.format(d);
            String gtxDate = gtxYear +"-"+ cData.gettDate().substring(2,4) +"-"+ cData.gettDate().substring(4,6);
            String TansactionData = StringLib.valToHexString(cData.getCardNumber()) +
//                    StringLib.valToHexString(nominalTransaksi) +
                    StringLib.valToHexString(nominalTransaksi.substring(2) + "00") +
//                    StringLib.valToHexString(txDate) +
                    StringLib.valToHexString(cData.gettDate()) +
                    StringLib.valToHexString(cData.gettTime())
                    + StringLib.valToHexString("808000") // proccode 801020 808000
                    + StringLib.valToHexString(stan) + StringLib.valToHexString(batchNumber) + "FFFFFFFF";
//            TansactionData = StringLib.valToHexString("6013500601496707") +
//                    StringLib.valToHexString("0000002500") +
//                    StringLib.valToHexString("160119") +
//                    StringLib.valToHexString("065942") +
//                    StringLib.valToHexString("808000") +
//                    StringLib.valToHexString("100081") +
//                    StringLib.valToHexString("00") +
//                    "FFFFFFFF";
//            writeDebugLog(TAG, "TansactionData : " + TansactionData);
            String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() +
                    "FF0000030080000000" + cData.getRandomNumber8B() + TansactionData;
            SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", SamResponse);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 4) {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                        contentValues.put("response", SamResponse);
                    } else {
                        contentValues.put("rc", SamResponse.substring(0, 4));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            writeDebugLog(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(0, 8));
            cData.setHash4B(SamResponse.substring(0, 8));
            if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
                Log.e(TAG, "ERROR RESPONSE " + SamResponse);
                btnOk.setVisibility(VISIBLE);
                return;
            }

            // 14. Card � Write Log
            String CardBalanceBefore3B = StringLib.ItoH(cData.getCardBalanceInt());
            int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(deductAmount);
            if (balanceAfter < 0) {
                setMessage("Saldo anda tidak mencukupi untuk melakukan transaksi.\nSisa Saldo anda Rp. " + cData.getCardBalanceInt());
                Log.e(TAG, "BALANCE MINUS " + balanceAfter);
                btnOk.setVisibility(VISIBLE);
                return;
            }
            cData.setNewBalance(Integer.toString(balanceAfter));
            cData.settDate(DATE.format(new Date()));
            String balanceAfter3B = StringLib.ItoH(Integer.toString(balanceAfter));
            String jamDeduct = svrTm.replaceAll(":","");
            if (jamDeduct!=null) {
                if (jamDeduct.equals("")) {
                    jamDeduct = cData.gettTime();
                }
            } else {
                jamDeduct = cData.gettTime();
            }
            String sendTocard = "3B01000000200000"
                    + cData.getMerchanIdForCardLog()
                    + cData.getTerminalIdForCardLog()
                    + cData.gettDate()
                    + jamDeduct
                    + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
            CardResponse = cc.transmitCmd(sendTocard);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", sendTocard);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "ERROR RESPONSE " + CardResponse);
                setMessage("Tidak dapat melakukan transaksi");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            String amount = "";
            if (!cData.getLastTransDate().substring(2, 4).equals(cData.gettDate().substring(2, 4))) {
                amount = cData.getDeductAmount();
            } else {
                int tmp = Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getDeductAmount());
                amount = tmp + "";
            }
            amount = StringLib.nominalUntukLog(StringLib.Int2Hex(amount));
            CardResponse = cc.transmitCmd("3D03000000070000" + cData.getYYMMDD() + amount);
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "3D03000000070000" + cData.getYYMMDD() + amount);
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                setMessage("Tidak dapat melakukan transaksi");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            CardResponse = cc.transmitCmd("C7");
            contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "C7");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
//            writeDebugLog(TAG, "new Balance after Deduct: " + cData.getNewBalance());

            if (!(CardResponse.equals("00"))) {
                Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            if (diskon) {
                setMessage("Pembayaran senilai " + StringLib.strToCurr(cData.getDeductAmount(), "Rp") +
                        "\n(Discount " + diskonData[1] + " dari " + StringLib.strToCurr(diskonData[0], "Rp") +
                        " )" + "\nBerhasil\n" +
                        "Sisa saldo BRIZZI anda adalah " + StringLib.strToCurr(cData.getNewBalance(), "Rp"));
            } else {
                setMessage("Pembayaran senilai " + StringLib.strToCurr(cData.getDeductAmount(), "Rp") + "\nBerhasil\n" +
                        "Sisa saldo BRIZZI anda adalah " + StringLib.strToCurr(cData.getNewBalance(), "Rp"));
            }
            printSizes.clear();
            formReponse = new JSONObject();
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "243000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            printSizes.add(new PrintSize(FontSize.TITLE, "Pembayaran Brizzi\n"));
            printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
            printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu : " + cardNumber() + "\n"));
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24301");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cardNumber());
            cmVal.put("print", cardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Pembayaran BRIZZI";
            if (diskon) {
                mTitle = "Pembayaran Diskon BRIZZI";
                printSizes.add(new PrintSize(FontSize.NORMAL, "Pembayaran : " +
                        StringLib.strToCurr(diskonData[0],"Rp") + "\n"));
                component = new JSONObject();
                component.put("visible", "true");
                component.put("comp_type", "1");
                component.put("comp_id", "24302");
                component.put("comp_lbl", "Pembayaran          : ");
                component.put("seq", "1");
                compValues = new JSONObject();
                compValue = new JSONArray();
                cmVal = new JSONObject();
                cmVal.put("value", StringLib.strToCurr(diskonData[0],"Rp"));
                cmVal.put("print", StringLib.strToCurr(diskonData[0],"Rp"));
                compValue.put(cmVal);
                compValues.put("comp_value", compValue);
                component.put("comp_values", compValues);
                comp.put(component);
                printSizes.add(new PrintSize(FontSize.NORMAL, "Diskon : " + diskonData[1] + "\n"));
                component = new JSONObject();
                component.put("visible", "true");
                component.put("comp_type", "1");
                component.put("comp_id", "24303");
                component.put("comp_lbl", "Diskon              : ");
                component.put("seq", "1");
                compValues = new JSONObject();
                compValue = new JSONArray();
                cmVal = new JSONObject();
                cmVal.put("value", diskonData[1]);
                cmVal.put("print", diskonData[1]);
                compValue.put(cmVal);
                compValues.put("comp_value", compValue);
                component.put("comp_values", compValues);
                comp.put(component);
                printSizes.add(new PrintSize(FontSize.NORMAL, "Total Pembayaran : " +
                        StringLib.strToCurr(diskonData[2],"Rp") + "\n"));
                component = new JSONObject();
                component.put("visible", "true");
                component.put("comp_type", "1");
                component.put("comp_id", "24304");
                component.put("comp_lbl", "Total Pembayaran    : ");
                component.put("seq", "1");
                compValues = new JSONObject();
                compValue = new JSONArray();
                cmVal = new JSONObject();
                cmVal.put("value", StringLib.strToCurr(diskonData[2],"Rp"));
                cmVal.put("print", StringLib.strToCurr(diskonData[2],"Rp"));
                compValue.put(cmVal);
                compValues.put("comp_value", compValue);
                component.put("comp_values", compValues);
                comp.put(component);
            } else {
                printSizes.add(new PrintSize(FontSize.NORMAL, "Pembayaran : " +
                        StringLib.strToCurr(cData.getDeductAmount(),"Rp") + "\n"));
                component = new JSONObject();
                component.put("visible", "true");
                component.put("comp_type", "1");
                component.put("comp_id", "24302");
                component.put("comp_lbl", "Pembayaran          : ");
                component.put("seq", "1");
                compValues = new JSONObject();
                compValue = new JSONArray();
                cmVal = new JSONObject();
                cmVal.put("value", StringLib.strToCurr(cData.getDeductAmount(), "Rp"));
                cmVal.put("print", StringLib.strToCurr(cData.getDeductAmount(), "Rp"));
                compValue.put(cmVal);
                compValues.put("comp_value", compValue);
                component.put("comp_values", compValues);
                comp.put(component);
            }
            screen.put("title", mTitle);
            printSizes.add(new PrintSize(FontSize.NORMAL, "Sisa Saldo : " +
                    StringLib.strToCurr(cData.getNewBalance(),"Rp") + "\n"));
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24305");
            component.put("comp_lbl", "Sisa Saldo          : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cData.getNewBalance(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cData.getNewBalance(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            comps.put("comp", comp);
            screen.put("comps", comps);
            screen.put("server_date", svrDt);
            screen.put("server_time", svrTm);
            formReponse.put("screen", screen);
            btnOk.setVisibility(VISIBLE);
            printPanelVisibility(VISIBLE);
            formReponse.put("server_date", svrDt);
            formReponse.put("server_time", svrTm);
            formReponse.put("server_appr", cData.getHash4B());
            formReponse.put("card_type", cardType);
            formReponse.put("nomor_kartu", cardNumber());
            writeMessageLog(StringLib.toSQLiteTimestamp(gtxDate, cData.gettTime()));
            formListener.onSuccesListener(formReponse);
        } catch (JSONException ex) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
        }
    }

    private void infoDeposit() {
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String SamResponse = "";
        Log.i(TAG, "Info Deposit");
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
        // 12. SAM � Get Key Topup
        String cmd = "80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000";
        SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", cmd);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\n ERROR [05]");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String randomSam24B = SamResponse.substring(0, SamResponse.length() - 4);
//        writeDebugLog(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + randomSam24B);
        // 13. HOST Get Auth Key Topup
        sendToServer(randomSam24B, cData.getTopupAmount(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
    }

    private void infoSaldo() {
        cekSaldo();
        if(cData.getValidationStatus().equals("16")) {
            setMessage("Transaksi Gagal. \nStatus kartu close");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        if(cData.getValidationStatus().equals("17")) {
            setMessage("Transaksi Gagal. \nKartu tidak dapat diakses");
            btnOk.setVisibility(VISIBLE);
            return;
        }
//        writeDebugLog(TAG, "cmd: 6C00 || Balance:  " + cData.getCardBalance4B());
//        writeDebugLog(TAG, "Balance integer: " + StringLib.HtoI(cData.getCardBalance4B()));
        setMessage("Sisa saldo brizzi anda adalah " + StringLib.strToCurr(cData.getCardBalanceInt(), "Rp"));
        printSizes.clear();
        printSizes.add(new PrintSize(FontSize.TITLE, "INFO SALDO BRIZZI\n"));
        printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu BRIZZI  : " + cardNumber() + "\n"));
        printSizes.add(new PrintSize(FontSize.NORMAL, "Saldo Kartu      : " +
                StringLib.strToCurr(cData.getCardBalanceInt(), "Rp") + "\n"));
        formReponse = new JSONObject();
        try {
            JSONObject screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "220000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "WF");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            JSONObject component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24301");
            component.put("comp_lbl", "No Kartu            : ");
            component.put("seq", "0");
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", cardNumber());
            cmVal.put("print", cardNumber());
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            String mTitle = "Info Saldo BRIZZI";
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", "24302");
            component.put("comp_lbl", "Saldo               : ");
            component.put("seq", "1");
            compValues = new JSONObject();
            compValue = new JSONArray();
            cmVal = new JSONObject();
            cmVal.put("value", StringLib.strToCurr(cData.getCardBalanceInt(), "Rp"));
            cmVal.put("print", StringLib.strToCurr(cData.getCardBalanceInt(), "Rp"));
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
            comp.put(component);
            screen.put("title", mTitle);
            comps.put("comp", comp);
            screen.put("comps", comps);
            formReponse.put("screen", screen);
            btnOk.setVisibility(VISIBLE);
            printPanelVisibility(VISIBLE);
            formReponse.put("server_date", svrDt);
            formReponse.put("server_time", svrTm);
            formReponse.put("card_type", cardType);
            formReponse.put("nomor_kartu", cardNumber());
            writeMessageLog();
            formListener.onSuccesListener(formReponse);
        } catch (Exception e) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            e.printStackTrace();
        }
    }

    private void infoKartu() {
        Log.i(TAG, "Info Kartu");
//        changeStatus("cl");
        setMessage("Silahkan Tunggu");
        btnOk.setVisibility(GONE);
        printPanelVisibility(GONE);
        try {
            String CardResponse = "";
            String SamResponse = "";
            String cmd = "";
            StringBuilder result = new StringBuilder();
            //2. Card � Select AID 1
            String aid = smc.sendCmd(StringLib.hexStringToByteArray("00A4040C09A00000000000000011"));
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 0);
            contentValues.put("cmd", "00A4040C09A00000000000000011");
            contentValues.put("rc", aid);
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
            if (aid.equals("9000")) {
                CardResponse = cc.transmitCmd("5A010000");
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "5A010000");
                if (CardResponse.length() > 2) {
                    contentValues.put("rc", CardResponse.substring(0, 2));
                    contentValues.put("response", CardResponse);
                } else {
                    contentValues.put("rc", CardResponse);
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                writeDebugLog(TAG, "cmd:5A010000 || " + CardResponse);
                // 3. Card � Get Card Number
                cmd = cc.getCommand(0, 23);
                CardResponse = cc.transmitCmd(cmd);
                JSONObject ciHeader = parseCiHeader(CardResponse);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", cmd);
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() < 24) {
                            contentValues.put("rc", ciHeader.getString(BrizziCiHeader.RC.name()));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", ciHeader.getString(BrizziCiHeader.RC.name()));
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                if (CardResponse.length() < 24) {
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    Log.e(TAG, "Error when Get Card number, response " + CardResponse);
                    btnOk.setVisibility(VISIBLE);
                    return;
                }
                String CardNumber = CardResponse.substring(8, 8 + 16);

                contentValues = new ContentValues();
                contentValues.put("card_number", CardNumber);
                tx.updateAidById(contentValues, cData.getBrizziIdLog());
                cData.setCardNumber(CardNumber);
                writeDebugLog(TAG, "cmd:" + cmd + " || " + CardResponse + " CardNumber:" + CardNumber);
                // 4. Card � Get Card Status
                CardResponse = cc.transmitCmd("BD01000000200000");
                JSONObject ciStatus = parseCiStatus(CardResponse);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "BD01000000200000");
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse);
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);

                cData.setCardStatus(CardResponse.substring(8, 12).equals("6161"));
                writeDebugLog(TAG, "cmd:BD0100000200000 || " + CardResponse);
//Cek Pasif
                boolean isPasive = false;
                try {
                    CardResponse = cc.transmitCmd("5A030000");

                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 1);
                    contentValues.put("cmd", "5A030000");
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                contentValues.put("rc", CardResponse.substring(0, 2));
                                contentValues.put("response", CardResponse);
                            } else {
                                contentValues.put("rc", CardResponse);
                            }
                        }
                    }
                    contentValues.put("timestamp", new Date().getTime());
                    tx.insertIntoCmdLog(contentValues);

                    if (!CardResponse.startsWith("00")) {
                        setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                        Log.e(TAG, "Select AID 3, RESPONSE " + CardResponse);
                        return;
                    }
//                    writeDebugLog(TAG, "cmd: 5A030000 || " + CardResponse);
                    // 6. Card � Request Key Card
                    CardResponse = cc.transmitCmd("0A00");

                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 1);
                    contentValues.put("cmd", "0A00");
                    String rc = "";
                    String rv = "";
                    int rctr = 0;
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                rc = CardResponse.substring(0, 2);
                                rv = CardResponse.substring(2);
                                contentValues.put("rc", rc);
                                contentValues.put("response", rv);
                            } else {
                                contentValues.put("rc", CardResponse);
                            }
                        }
                    }
//                    writeDebugLog(TAG, "0A00 (" + String.valueOf(rctr) + "x) || " + rv);
                    contentValues.put("timestamp", new Date().getTime());
                    tx.insertIntoCmdLog(contentValues);

                    if (!CardResponse.startsWith("AF")) {
                        setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                        Log.e(TAG, "Error when request key card, response " + CardResponse);
                        return;
                    }
                    String Keycard = rv;
//                    writeDebugLog(TAG, "cmd: 0A00 || " + Keycard);
                    // 7. Card � Get UID
//                    writeDebugLog(TAG, "UID || " + cData.getUid());
                    cmd = "80B0000020" + CardNumber + cData.getUid() + "FF0000030080000000" + Keycard;
                    SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
                    contentValues = new ContentValues();
                    contentValues.put("id_aid_log", cData.getBrizziIdLog());
                    contentValues.put("device", 0);
                    contentValues.put("cmd", cmd);
                    if (SamResponse != null) {
//                        writeDebugLog(TAG, SamResponse);
                        if (SamResponse.length() > 0) {
                            if (SamResponse.length() > 2) {
                                contentValues.put("rc", SamResponse);
                                contentValues.put("response", SamResponse);
                            } else {
                                contentValues.put("rc", SamResponse);
                            }
                        }
                    }
                    contentValues.put("timestamp", new Date().getTime());
                    tx.insertIntoCmdLog(contentValues);
                    if (!SamResponse.startsWith("6D")) {
//                        writeDebugLog(TAG, "SAM Authenticate Key : " + SamResponse);
                        String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//                        writeDebugLog(TAG, "Randomkey16B : " + RandomKey16B);
                        // 9. Card � Authenticate Card
                        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
                        String RandomNumber8B = CardResponse.substring(2);
                        cData.setRandomNumber8B(RandomNumber8B);
//                        writeDebugLog(TAG, "cmd: AF+" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
                        //  10. Card � Get Last Transaction Date
                        CardResponse = cc.transmitCmd("BD03000000070000");

                        if (CardResponse.length() == 2) {
                            isPasive = false;
                        } else {
                            cData.setLastTransDate(CardResponse.substring(2, 8));
                            Calendar startCalendar = Calendar.getInstance();
                            try {
                                startCalendar.setTime(DATE_TOCARD.parse(cData.getLastTransDate()));
//                                startCalendar.setTime(DATE_TOCARD.parse("150801"));//Test Pasif
                            } catch (ParseException e) {

                            }
                            Calendar endCalendar = Calendar.getInstance();
                            Long dayDiff = ISO8583Parser.getDateDiff(startCalendar, endCalendar);
                            if (dayDiff > 365&&!cData.getLastTransDate().equals("000000")) {
                                isPasive = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    isPasive = false;
                }

                StringBuilder resp = new StringBuilder();
                JSONObject objResp = new JSONObject();
                objResp.put("messageId", "1234");
                objResp.put("no_kartu", cardNumber());

                printSizes.clear();
                resp.append("Informasi Kartu Brizzi Anda\n\n");
                printSizes.add(new PrintSize(FontSize.TITLE, "Informasi Kartu Brizzi Anda\n"));
                printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
                resp.append("No Kartu         : " + cardNumber(ciHeader.getString(BrizziCiHeader.CardNumber.name())) + "\n");
                printSizes.add(new PrintSize(FontSize.NORMAL, "No Kartu            : " +
                        cardNumber(ciHeader.getString(BrizziCiHeader.CardNumber.name())) + "\n"));
                String tgl = DATE_TOCOMP.format(DATE.parse(ciStatus.getString(BrizziCiStatus.ActivationCode.name())));
                objResp.put("tgl_aktivasi", tgl);
                resp.append("Tanggal Aktifasi : " + tgl + "\n");
                printSizes.add(new PrintSize(FontSize.NORMAL, "Tanggal Aktifasi    : " + tgl + "\n"));
//                tgl = DATE_TOCOMP.format(DATE.parse(ciHeader.getString(BrizziCiHeader.ExpireDate.name())));
                tgl = ciHeader.getString(BrizziCiHeader.BranchIssue.name());
                objResp.put("aktif_sd", tgl);
                resp.append("Aktif s/d        : " + tgl + "\n");
                printSizes.add(new PrintSize(FontSize.NORMAL, "Aktif s/d           : " + tgl + "\n"));
                String status = ciStatus.getString(BrizziCiStatus.Status.name()).equals("6161") ? "Aktif" : "Close";
                if (isPasive&&status.equals("Aktif")) {
                    status = "Pasif";
                }
                resp.append(status + "\n");
                printSizes.add(new PrintSize(FontSize.NORMAL, "Status Aktif        : " + status + "\n"));
                printSizes.add(new PrintSize(FontSize.EMPTY, "\n"));
                objResp.put("status", status);
                MenuListResolver mlr = new MenuListResolver();
                formReponse = mlr.loadMenu(context, "2B0000F", objResp);
                writeMessageLog();
                writeDebugLog(TAG, "OBJ " + formReponse.toString());
                setMessage(resp.toString());
                btnOk.setVisibility(VISIBLE);
                printPanelVisibility(VISIBLE);
                formReponse.put("server_date", svrDt);
                formReponse.put("server_time", svrTm);
                formReponse.put("card_type", cardType);
                formReponse.put("nomor_kartu", cardNumber());
                formListener.onSuccesListener(formReponse);
            } else {
                setMessage("Terjadi kesalahan.\nERROR [" + SamResponse + "]");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                Log.e(TAG, "AID : " + SamResponse);
//            cc.dettatch();
                btnOk.setVisibility(VISIBLE);
                return;
            }
        } catch (JSONException ex) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
        } catch (ParseException e) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            e.printStackTrace();
        } catch (Exception e) {
            setMessage("Terjadi Kesalahan");
            setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
            btnOk.setVisibility(VISIBLE);
            e.printStackTrace();
        }
    }

    private void printLogKartu() {
        cekSaldo();
        Log.i(TAG, "Print LogTrx");
        setMessage("Silahkan Tunggu");
        btnOk.setVisibility(GONE);
        printPanelVisibility(GONE);
        try {
            String CardResponse = "";
            String SamResponse = "";
            String cmd = "";
            StringBuilder result = new StringBuilder();
            //  1. Card � Get Last Log Position
            CardResponse = cc.transmitCmd("BD02000000010000");
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_aid_log", cData.getBrizziIdLog());
            contentValues.put("device", 1);
            contentValues.put("cmd", "BD02000000010000");
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        contentValues.put("rc", CardResponse.substring(0, 2));
                        contentValues.put("response", CardResponse);
                    } else {
                        contentValues.put("rc", CardResponse);
                    }
                }
            }
            contentValues.put("timestamp", new Date().getTime());
            tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: BD02000000010000 || " + CardResponse);
            if (CardResponse.length() == 2) {
//                setMessage("Terjadi kesalahan.\nERROR [" + CardResponse + "]");
                Log.e(TAG, "Select Get Last Log Position, RESPONSE not OK" + CardResponse);
                setMessage("Transaksi Gagal. \nPosisi Log Terakhir tidak terbaca");
                setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
//                return;
            }
            //  2. Card � Get Log Data
            cmd = "BB01000000000000";
            CardResponse = cc.transmitCmd(cmd);
//            writeDebugLog(TAG, "cmd: " + cmd + " || " + CardResponse);
            String rc = "BE";
            String rv = "";
            if (CardResponse != null) {
                if (CardResponse.length() > 0) {
                    if (CardResponse.length() > 2) {
                        rc = CardResponse.substring(0, 2);
                        rv = CardResponse.substring(2);
                    } else {
                        rc = CardResponse;
                    }
                }
            }
            if (rc.equals("BE")) {
                setMessage("Transaction Log is Empty");
                btnOk.setVisibility(VISIBLE);
            } else {
                if (rc.equals("AF")) {
                    result.append(rv);
                    while (rc.equals("AF")) {
                        cmd = "AF";
                        CardResponse = cc.transmitCmd(cmd);
//                        writeDebugLog(TAG, "cmd: " +cmd + " || " + CardResponse);
                        rc = "BE";
                        rv = "";
                        if (CardResponse != null) {
                            if (CardResponse.length() > 0) {
                                if (CardResponse.length() > 2) {
                                    rc = CardResponse.substring(0, 2);
                                    rv = CardResponse.substring(2);
                                } else {
                                    rc = CardResponse;
                                }
                            }
                        }
                        if (rc.equals("AF")||rc.equals("00")) {
                            result.append(rv);
                        }
                    }
                    formReponse = new JSONObject();
                    formReponse.put("screen",formatTxLog(result.toString()));
//                    writeDebugLog(TAG, formReponse.toString());
                    btnOk.setVisibility(VISIBLE);
                    printPanelVisibility(VISIBLE);
                    formReponse.put("server_date", svrDt);
                    formReponse.put("server_time", svrTm);
                    formReponse.put("card_type", cardType);
                    formReponse.put("nomor_kartu", cardNumber());
                    formListener.onSuccesListener(formReponse);
                } else {
//                    setMessage("Error Response Unknown [code : " + rc + "]");
                    setMessage("Transaksi Gagal.\nData log pada kartu tidak terbaca");
                    setMessage("Tidak dapat melakukan transaksi\nSilahkan coba beberapa saat lagi");
                    btnOk.setVisibility(VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doVoidSecond() {
        cekSaldo();
        String sv = cData.getstanvoid();
        Log.i(TAG, "Void");
//        writeDebugLog(TAG, "Log ID : " + sv);
        writeDebugLog("EDCLOG", "read (3996)");
        String qry = "select * from edc_log where service_id like 'A24%' " +
                "and (settled <> 't' or settled is null) and rc = '00' " +
                "and (reversed <> 't' or reversed is null) and log_id = " + sv + " order by rqtime desc";
        Cursor sData = clientDB.rawQuery(qry, null);
        if (sData.moveToFirst()) {
            String cardno = sData.getString(sData.getColumnIndex("track2"));
            stan = sData.getString(sData.getColumnIndex("stan"));
            if (!cardno.equals(cData.getCardNumber())) {
                setMessage("Kartu yang ditap bukan kartu yang digunakan untuk bertransaksi");
                btnOk.setVisibility(VISIBLE);
                return;
            }
            stan = sData.getString(sData.getColumnIndex("stan"));
            double amt = sData.getDouble(sData.getColumnIndex("amount"));
            cData.setDeductAmount(StringLib.fillZero(String.valueOf((int) amt), 10));
            logid = String.valueOf(sData.getInt(sData.getColumnIndex("log_id")));
        } else {
            setMessage("Tidak terdapat data transaksi");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
        String cmd = "";
        ContentValues contentValues = new ContentValues();
        cmd = "80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000";
        String SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
        contentValues.put("id_aid_log", cData.getBrizziIdLog());
        contentValues.put("device", 0);
        contentValues.put("cmd", cmd);
        if (SamResponse != null) {
            if (SamResponse.length() > 0) {
                if (SamResponse.length() > 4) {
                    contentValues.put("rc", SamResponse.substring(SamResponse.length() - 4));
                    contentValues.put("response", SamResponse);
                } else {
                    contentValues.put("rc", SamResponse);
                }
            }
        }
        contentValues.put("timestamp", new Date().getTime());
        tx.insertIntoCmdLog(contentValues);
        if (!SamResponse.endsWith("9000")) {
            setMessage("Terjadi kesalahan.\n ERROR [05]");
            btnOk.setVisibility(VISIBLE);
            return;
        }
        String randomSam24B = SamResponse.substring(0, SamResponse.length() - 4);
//        writeDebugLog(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + randomSam24B);
        // 13. HOST Get Auth Key Topup
//        writeDebugLog("KIRIM", "MSG_SI " + cData.getMsgSI());
        sData.close();
        sendToServer(randomSam24B, cData.getDeductAmount(), cData.getCardNumber(), cData.getPin(), cData.getMsgSI());
    }

    private String changeStatus(String target) {
        Log.i(TAG, "Change Status");
        String CardResponse = "";
        String SamResponse = "";
        try {
            if (target.equals("ps")) {
                cekSaldo();
                String vStat = cData.getValidationStatus();
                if (!vStat.equals("00")) {
                    writeDebugLog(TAG, "Validation status not zero");
                    throw new Exception("Validation status not zero");
                }
                Log.i(TAG, "SET PASIF");
                cData.setDeductAmount("100");
                ContentValues contentValues;
                setMessage("Silahkan tunggu.\nHarap tidak melepaskan kartu brizzi anda pada device");
                btnOk.setVisibility(GONE);

//                // 12. Card � Debit Balance
//                String deductAmount = cData.getDeductAmount();
//                String reverseAmount3B = StringLib.ItoH(deductAmount);
//                CardResponse = cc.transmitCmd("DC00" + reverseAmount3B + "00");
//                contentValues = new ContentValues();
//                contentValues.put("id_aid_log", cData.getBrizziIdLog());
//                contentValues.put("device", 1);
//                contentValues.put("cmd", "DC00" + reverseAmount3B + "00");
//                if (CardResponse != null) {
//                    if (CardResponse.length() > 0) {
//                        if (CardResponse.length() > 2) {
//                            contentValues.put("rc", CardResponse.substring(0, 2));
//                            contentValues.put("response", CardResponse);
//                        } else {
//                            contentValues.put("rc", CardResponse.substring(0, 2));
//                        }
//                    }
//                }
//                contentValues.put("timestamp", new Date().getTime());
//                tx.insertIntoCmdLog(contentValues);
//
////            writeDebugLog(TAG, "cmd: " + "DC00" + StringLib.ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);
//
//                // 13. SAM � Create Hash
//                String lDate = "150801";
//                cData.settTime("071012");
//                String nominalTransaksi = StringLib.nominalTransaksi(deductAmount);
//                String TansactionData = StringLib.Hex3(cData.getCardNumber())
//                        + StringLib.Hex3(nominalTransaksi) + StringLib.Hex3(lDate)
//                        + StringLib.Hex3(cData.gettTime()) + StringLib.Hex3("818001")
//                        + StringLib.Hex3(zeroLeftPadding(stan, 6)) + StringLib.Hex3("00")
//                        + "FFFFFFFF";
////            writeDebugLog(TAG, "TansactionData : " + TansactionData);
//                String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000"
//                        + cData.getRandomNumber8B() + TansactionData;
//                SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(sendtosam));
//                contentValues = new ContentValues();
//                contentValues.put("id_aid_log", cData.getBrizziIdLog());
//                contentValues.put("device", 1);
//                contentValues.put("cmd", SamResponse);
//                if (CardResponse != null) {
//                    if (CardResponse.length() > 0) {
//                        if (CardResponse.length() > 4) {
//                            contentValues.put("rc", SamResponse.substring(0, 4));
//                            contentValues.put("response", SamResponse);
//                        } else {
//                            contentValues.put("rc", SamResponse.substring(0, 4));
//                        }
//                    }
//                }
//                contentValues.put("timestamp", new Date().getTime());
//                tx.insertIntoCmdLog(contentValues);
////            writeDebugLog(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(2, 10));
//                cData.setHash4B(SamResponse.substring(2, 10));
//                if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
//                    Log.e(TAG, "ERROR RESPONSE " + SamResponse);
//                    btnOk.setVisibility(VISIBLE);
//                    throw new Exception("Error key from SAM");
//                }
//                // 14. Card � Write Log
//                String CardBalanceBefore3B = StringLib.ItoH(cData.getCardBalanceInt());
//                int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(cData.getCardBalanceInt());
//                cData.setNewBalance(Integer.toString(balanceAfter));
//                String balanceAfter3B = StringLib.ItoH(Integer.toString(balanceAfter));
//                String tglReverse = lDate.substring(4,6) + cData.gettDate().substring(2,4)+cData.gettDate().substring(0,2);
//                String sendTocard = "3B01000000200000" + cData.getMerchanIdForCardLog() + cData.getTerminalIdForCardLog()
//                        + tglReverse + cData.gettTime() + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
////            + cData.gettDate() + cData.gettTime() + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
//                CardResponse = cc.transmitCmd(sendTocard);
//                contentValues = new ContentValues();
//                contentValues.put("id_aid_log", cData.getBrizziIdLog());
//                contentValues.put("device", 1);
//                contentValues.put("cmd", sendTocard);
//                if (CardResponse != null) {
//                    if (CardResponse.length() > 0) {
//                        if (CardResponse.length() > 2) {
//                            contentValues.put("rc", CardResponse.substring(0, 2));
//                            contentValues.put("response", CardResponse);
//                        } else {
//                            contentValues.put("rc", CardResponse.substring(0, 2));
//                        }
//                    }
//                }
//                contentValues.put("timestamp", new Date().getTime());
//                tx.insertIntoCmdLog(contentValues);
////            writeDebugLog(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
//                if (!(CardResponse.equals("00"))) {
//                    Log.e(TAG, "ERROR RESPONSE " + CardResponse);
//                    btnOk.setVisibility(VISIBLE);
//                    throw new Exception("Cannot write log transaction");
//                }
//                String amount = "";
//                if (!cData.getLastTransDate().substring(2, 4).equals(lDate.substring(2, 4))) {
//                    amount = cData.getDeductAmount();
//                } else {
//                    int tmp = Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getDeductAmount());
//                    amount = tmp + "";
//                }
                //        amount = zeroLeftPadding(amount, 8);
                String amount = StringLib.nominalUntukLog(StringLib.Int2Hex(cData.getDeductAmount()));
                CardResponse = cc.transmitCmd("3D03000000070000" + "150802" + amount);
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "3D03000000070000" + DATE_TOCARD.format(new Date()) + amount);
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
                if (!(CardResponse.equals("00"))) {
                    Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                    btnOk.setVisibility(VISIBLE);
                    throw new Exception("Cannot write log transaction");
                }
                CardResponse = cc.transmitCmd("C7");
                contentValues = new ContentValues();
                contentValues.put("id_aid_log", cData.getBrizziIdLog());
                contentValues.put("device", 1);
                contentValues.put("cmd", "C7");
                if (CardResponse != null) {
                    if (CardResponse.length() > 0) {
                        if (CardResponse.length() > 2) {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                            contentValues.put("response", CardResponse);
                        } else {
                            contentValues.put("rc", CardResponse.substring(0, 2));
                        }
                    }
                }
                contentValues.put("timestamp", new Date().getTime());
                tx.insertIntoCmdLog(contentValues);
//            writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
//            writeDebugLog(TAG, "new Balance after Deduct: " + cData.getNewBalance());
                if (!(CardResponse.equals("00"))) {
                    Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                    btnOk.setVisibility(VISIBLE);
                    throw new Exception("Cannot commit transaction");
                }
                setMessage("Setup kartu pasif ok");
                btnOk.setVisibility(VISIBLE);
                return "00";
            } else {
                String cmd = "";
                //2. Card � Select AID 1
                String aid = smc.sendCmd(StringLib.hexStringToByteArray("00A4040C09A00000000000000011"));
                if (aid.equals("9000")) {
                    CardResponse = cc.transmitCmd("5A010000");
                    writeDebugLog(TAG, "cmd:5A010000 || " + CardResponse);
                    // 3. Card � Get Card Number
                    cmd = cc.getCommand(0, 23);
                    CardResponse = cc.transmitCmd(cmd);
                    if (CardResponse.length() < 24) {
                        setMessage("Terjadi Kesalahan.\nERROR [" + CardResponse + "]");
                        Log.e(TAG, "Error when Get Card number, response " + CardResponse);
                        return null;
                    }
                    String CardNumber = CardResponse.substring(8, 24);
                    cData.setCardNumber(CardNumber);
                    writeDebugLog(TAG, "cmd:" + cmd + " || " + CardResponse + " CardNumber:" + CardNumber);
                    CardResponse = cc.transmitCmd("5A010000");
                    writeDebugLog(TAG, "cmd:5A010000 || " + CardResponse);
                    // 4. Card � Get Card Status
                    CardResponse = cc.transmitCmd("BD01000000200000");
                    String statusAfter = ISO8583Parser.bytesToHex(target.getBytes());
                    String cardStatus = CardResponse.substring(8, 12);
                    String cardIssueDate = CardResponse.substring(2, 8);
                    writeDebugLog(TAG, "Desired Status : " + statusAfter);
                    writeDebugLog(TAG, "Current Status : " + cardStatus);
                    if (cardStatus.equals(statusAfter)) {
                        throw new Exception("Card already has " + target + "status");
                    }
                    if (cardIssueDate.equals("000000") && statusAfter.equals("6161")) {
                        cardIssueDate = StringLib.getDDMMYY();
                    }
                    writeDebugLog(TAG, "cmd:BD01000000200000 || " + CardResponse);
                    CardResponse = cc.transmitCmd("0A00");

                    writeDebugLog(TAG, "cmd:0A00 || " + CardResponse);
                    String rc = "";
                    String rv = "";
                    int rctr = 0;
                    if (CardResponse != null) {
                        if (CardResponse.length() > 0) {
                            if (CardResponse.length() > 2) {
                                rc = CardResponse.substring(0, 2);
                                rv = CardResponse.substring(2);
                            }
                        }
                    }
                    writeDebugLog(TAG, "0A00 (" + String.valueOf(rctr) + "x) || " + rv);

                    if (!(CardResponse.startsWith("00") || CardResponse.startsWith("AF"))) {
                        Log.e(TAG, "Error when request key card, response " + CardResponse);
                        throw new Exception("Error request key from card");
                    }
                    String Keycard = rv;
                    writeDebugLog(TAG, "cmd: 0A00 || " + Keycard);
                    // 7. Card � Get UID
                    writeDebugLog(TAG, "UID || " + cData.getUid());
                    cmd = "80B0000020" + cData.getCardNumber() + cData.getUid() + "FF0000010080000000" + Keycard;
//                Log.i(TAG, cmd);
                    SamResponse = smc.sendCmd(StringLib.hexStringToByteArray(cmd));
                    if (!SamResponse.startsWith("6D")) {
//                    writeDebugLog(TAG, "SAM Authenticate Key : " + SamResponse);
                        String RandomKey16B = SamResponse.substring(32, SamResponse.length() - 4);
//                    writeDebugLog(TAG, "Randomkey16B : " + RandomKey16B);
                        // 9. Card � Authenticate Card
                        CardResponse = cc.transmitCmd("AF" + RandomKey16B);
                        String RandomNumber8B = CardResponse.substring(2);
                        CardResponse = CardResponse.substring(0, 2);
                        cData.setRandomNumber8B(RandomNumber8B);
//                    writeDebugLog(TAG, "cmd: AF" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
                        if (!(CardResponse.equals("00"))) {
                            Log.i(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                            throw new Exception("Sam authentication said invalid");
                        }

                        String statusSuffix = StringLib.fillZero("0", 54);
                        cmd = "3D01000000200000" + cardIssueDate + statusAfter + statusSuffix;
//                    writeDebugLog(TAG, cmd);
                        CardResponse = cc.transmitCmd(cmd);
//                    writeDebugLog(TAG, "cmd:" + cmd + " || " + CardResponse);

                        if (!(CardResponse.equals("00"))) {
                            Log.e(TAG, "cmd: WRITELOG ERROR RESPONSE " + CardResponse);
                            throw new Exception("Unable to write into card");
                        }
                        CardResponse = cc.transmitCmd("C7");

//                    writeDebugLog(TAG, "cmd: C7 | response: " + CardResponse);
                        if (!(CardResponse.equals("00") || CardResponse.equals("0C"))) {
                            Log.e(TAG, "cmd: C7 ERROR RESPONSE " + CardResponse);
                            throw new Exception("Commit transaction error");
                        }
                    }
                    String statusName = target.equals("aa") ? "aktif" : "pasif";
                    setMessage("Setup kartu " + statusName + " ok");
                    btnOk.setVisibility(VISIBLE);
                    return "00";
                } else {
                    return "05";
                }
            }
        } catch (Exception e) {
            setMessage(e.getMessage());
            btnOk.setVisibility(VISIBLE);
            return e.getMessage();
        }
    }

    public void writeDebugLog(String category, String msg) {
        if (DEBUG_LOG) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            Log.d("DEBUG", "[" + sdf.format(d) + "] " + category + " - " + msg);
        }
    }

    private String cardNumber() {
        return cardNumber(cData.getCardNumber());
    }

    private String cardNumber(String cardData) {
        StringBuilder result = new StringBuilder();
        int it = 0;
        for (int i = 0; i < cardData.length(); i++) {
            if (it == 4) {
                result.append(" ");
                it = 0;
            }
            result.append(cardData.charAt(i));
            it++;
        }
        return result.toString();
    }

    private void updateVoidLog() {
        try {
            helperDb = new DataBaseHelper(context);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        int logId;
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String repDate = sdf.format(d) + "-";
        String tmStamp = null;
        try {
            repDate = repDate + formReponse.getString("server_date").substring(0,2) + "-" +
                    formReponse.getString("server_date").substring(2,4);
            tmStamp = StringLib.toSQLiteTimestamp(repDate, formReponse.getString("server_time"));
        } catch (JSONException e) {
            Log.e("UPD", "JSON ERROR " + e.getMessage());
        }
        logId = Integer.parseInt(this.logid);
        writeDebugLog("EDCLOG", "read (4373)");
        String getStan = "select stan from edc_log where log_id = " + logId;
        Cursor eStan = clientDB.rawQuery(getStan, null);
        if (eStan!=null) {
            eStan.moveToFirst();
            logId = Integer.parseInt(eStan.getString(0));
        }
        eStan.close();
        String serviceId = "A"+cData.getWhatToDo().substring(0,5);
        writeDebugLog("MSGLOG", "update (4382)");
        String iMsgLog = "update messagelog set response_message = '"
                +  formReponse.toString() + "', request_time = '" + tmStamp + "', print = 2 "
                + "where cast(message_id as integer)  = " + logId;
        clientDB.execSQL(iMsgLog);
        int elogId = 1;
        writeDebugLog("EDCLOG", "read (4388)");
        String getElogId = "select max(log_id) last_id from edc_log";
        Cursor stanSeq = clientDB.rawQuery(getElogId, null);
        if (stanSeq != null) {
            stanSeq.moveToFirst();
            elogId = stanSeq.getInt(0);
        }
        stanSeq.close();
        writeDebugLog("EDCLOG", "update (4396)");
        String elog = "update edc_log set rqtime = '" + tmStamp + "', track2 = '' " +
                "where log_id = " + elogId;
        clientDB.execSQL(elog);
        clientDB.close();
        helperDb.close();
        clientDB.close();
        helperDb.close();
    }

    private void writeMessageLog(String tmStamp) {
        gtmStamp = tmStamp;
        writeMessageLog();
    }

    private void writeMessageLog() {
        try {
            helperDb = new DataBaseHelper(context);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        int logId = 0;
//        writeDebugLog("SI", cData.getMsgSI());
        if (cData.getMsgSI().equals("A25100")||
                cData.getMsgSI().equals("A27100")||
                cData.getMsgSI().equals("A29200")) {
//            writeDebugLog("LOG", "INTRC");
//            writeDebugLog("UPD", formReponse.toString());
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String repDate = sdf.format(d) + "-";
            String tmStamp = null;
            try {
                repDate = repDate + formReponse.getString("server_date").substring(0,2) + "-" +
                        formReponse.getString("server_date").substring(2,4);
                tmStamp = StringLib.toSQLiteTimestamp(repDate, formReponse.getString("server_time"));
            } catch (JSONException e) {
                Log.e("UPD", "JSON ERROR " + e.getMessage());
            }
            String serviceId = "A"+cData.getWhatToDo().substring(0,5);
            try {
                logId = Integer.parseInt(this.logid);
            } catch (Exception e) {
                String getLastId = "select max(log_id) logid from messagelog";
                Cursor lstId = clientDB.rawQuery(getLastId, null);
                if (lstId.moveToFirst()) {
                    logId = lstId.getInt(0);
                }
                lstId.close();
                lstId = null;
            }

            //JANGAN UPDATE MESSAGE LOG
            writeDebugLog("MSGLOG", "update (4482)");
            String iMsgLog = "update messagelog set response_message = '"
                    +  formReponse.toString() + "', request_time = '" + tmStamp + "', print = 2 "
                    + "where cast(log_id as integer)  = " + logId;
            clientDB.execSQL(iMsgLog);
            writeDebugLog("WL", iMsgLog);
            int elogId = 1;
            writeDebugLog("EDCLOG", "read (4489)");
            String getElogId = "select max(log_id) last_id from edc_log";
            Cursor stanSeq = clientDB.rawQuery(getElogId, null);
            if (stanSeq != null) {
                stanSeq.moveToFirst();
                elogId = stanSeq.getInt(0);
            }
            stanSeq.close();
            writeDebugLog("EDCLOG", "read (4497)");
            String elog = "update edc_log set rqtime = '" + tmStamp + "' " +
                    "where log_id = " + elogId;
            clientDB.execSQL(elog);
            clientDB.close();
            helperDb.close();
            return;
        }
//        writeDebugLog("LOG", "NOT INTRC");
        writeDebugLog("MSGLOG", "read (4506)");
        String getLogId = "select max(log_id) nextseq from messagelog ";
        Cursor cLogId = clientDB.rawQuery(getLogId, null);
        if (cLogId.moveToFirst()) {
            logId = cLogId.getInt(cLogId.getColumnIndex("nextseq"));
            logId += 1;
        } else {
            logId = 1;
        }
        cLogId.close();
        String tmStamp = "";
        if (cData.getMsgSI().equals("A2A200")) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String repDate = sdf.format(d) + "-";
            try {
                repDate = repDate + formReponse.getString("server_date").substring(0,2) + "-" +
                        formReponse.getString("server_date").substring(2,4);
                tmStamp = StringLib.toSQLiteTimestamp(repDate, formReponse.getString("server_time"));
            } catch (JSONException e) {
                Log.e("UPD", "JSON ERROR " + e.getMessage());
            }
        } else if (gtmStamp!=null){
            tmStamp = gtmStamp;
        } else {
            Date d = new Date();
            SimpleDateFormat sdftm = new SimpleDateFormat("HHmmss");
            String dtm = sdftm.format(d);
            if (!svrTm.equals("")) {
                dtm = svrTm.replaceAll(":","");
            }
            tmStamp = StringLib.toSQLiteTimestamp(StringLib.getYYYYMMDD(), dtm);
        }
        svrDt = tmStamp.substring(0,10);
        svrTm = tmStamp.substring(11);

        String serviceId = "A"+cData.getWhatToDo().substring(0,5);
        if (cData.getMsgSI().equals(SI_REAKTIVASI_PAY)) {
            serviceId = SI_REAKTIVASI_PAY;
        }
        //jika void maka stan print 0
        writeDebugLog("MSGLOG", "insert (4544)");
        String iMsgLog = "insert or replace into messagelog "
                + "(message_id, service_id, terminal_id, request_time, log_id, "
                + "response_message, message_status, print) values ('" + stan + "', "
//                + "request_message) values ('" + txElements[0] + "', "
                + "'" + serviceId + "', "
                + "'" + tid + "', '" + tmStamp
                + "', " + String.valueOf(logId) + ", '"
                + formReponse.toString() +"', '00', 2 )";
        clientDB.execSQL(iMsgLog);
        int elogId = 1;
        writeDebugLog("EDCLOG", "read (4555)");
        String getElogId = "select max(log_id) last_id from edc_log";
        Cursor stanSeq = clientDB.rawQuery(getElogId, null);
        if (stanSeq != null) {
            stanSeq.moveToFirst();
            elogId = stanSeq.getInt(0);
            elogId+=1;
        }
        stanSeq.close();
        int batchno = 0;
        String getBatchNo = "select batch from holder";
        Cursor batchNo = clientDB.rawQuery(getBatchNo, null);
        if (batchNo!=null) {
            batchNo.moveToFirst();
            batchno = batchNo.getInt(0);
        }
        batchNo.close();
        String batchNumber = ("00" + String.valueOf(batchno));
        batchNumber = batchNumber.substring(batchNumber.length()-2);
        String amount = "000000000000";
        if (cData.getDeductAmount()!=null) {
            if (!cData.getDeductAmount().equals("")) {
                amount = StringLib.fillZero(cData.getDeductAmount().replace("\\,","").replace("\\.",""), 12);
                amount = amount.substring(2) + "00";
            }
        }
        writeDebugLog("EDCLOG", "insert (4581)");
        String elog = "insert or replace into edc_log " +
                "(log_id, service_id, stan, track2, amount, rc, proccode, batchno, hash, voidhash, rqtime) values (" +
                String.valueOf(elogId) + ",'" + serviceId + "', '" +
                stan + "', '" + cData.getCardNumber() + "', " + amount + ", '00', '808000', '" + batchNumber + "', '" +
                cData.getHash4B() + "', '" + cData.getHashVoid() + "', '" + tmStamp + "');";
        String array[] = {"L00001",
                "A54911", "A51410", "A53100", "A53211", "A53221", "A54921", "A54931",
                "A54941", "A54B11", "A54A10", "A54110", "A54211", "A54221", "A54311", "A54321",
                "A54410", "A54431", "A54433", "A54441", "A54443", "A54451", "A54453", "A54461",
                "A54510", "A54520", "A54530", "A54540", "A54550", "A54560", "A57000", "A57200",
                "A57400", "A58000", "A54421", "A54423", "A54C10", "A54C20", "A54C51", "A54C52",
                "A54C53", "A54C54", "A52100", "A52210", "A52220", "A52300", "A54950", "A54710",
                "A54720", "A54800", "A59000", "A54331",

                "A71001", "A72000", "A72001", "A73000",

                "A61000", "A62000", "A63000",

                "A2C000", "A2C100", "A2C200", "A21100", "A22000", "A22100", "A23000", "A23100",
                "A29100", "A2A100", "A2B000", "A2B100", "A2D100",
                "A91000", "A92000", "A93000", "A94000"};
        boolean matched_array = false;
        for(int i=0; i < array.length; i++){
            if(serviceId.equals(array[i])){
                matched_array = true;
                i = array.length;
//                Log.d("ARRAY", "LIAT ARRAY : " + array[array.length - 1]);
            }
        }
        if (!matched_array){
            Log.d("ELOG", "Inserted by TapCard : " + elog);
            clientDB.execSQL(elog);

            String uStanSeq = "update holder set "
                    + "seq = " + stan;
            writeDebugLog("UPDATING", "HOLDER (1255)");
//                writeDebugLog("By ", serviceid);
            clientDB.execSQL(uStanSeq);
        }
//        clientDB.execSQL(elog);
        clientDB.close();
        helperDb.close();
    }

    private JSONObject formatTxLog(String alldata) {
        JSONObject screen = null;
        try {
            screen = new JSONObject();
            screen.put("type", "1");
            screen.put("id", "2A2000F");
            screen.put("ver", "1.0");
            screen.put("print", "2");
            screen.put("print_text", "");
            screen.put("title", "Log Transaksi BRIZZI");
            JSONArray comp = new JSONArray();
            JSONObject comps = new JSONObject();
            //card no
            JSONObject component = createComponent("2A201", "No Kartu : ", cardNumber(), "0");
            comp.put(component);
            //separator
            component = createComponent("2A202", " ", " ", "1");
            comp.put(component);
            //
            component = createComponent("2A203", " Date       Time     Terminal ID  ", "", "2");
            comp.put(component);
            component = createComponent("2A204", " Transaksi           Nominal      ", "", "3");
            comp.put(component);
            component = createComponent("2A205","----------------------------------------","","4");
            comp.put(component);
            String tlmid = "";
            String tltid = "";
            String tldate = "";
            String tltime = "";
            String tltcode = "";
            String tlamt = "";
            String tlbalb = "";
            String tlbala = "";
            int seq = 5;
            //throw first row
            alldata = alldata.substring(64);

            while (alldata.length() > 63) {
                String data = alldata.substring(0, 64);
                writeDebugLog(TAG, "Row : " + data);
                alldata = alldata.substring(64);
                try {
                    tlmid = data.substring(0, 16);
                    tlmid = StringLib.hexStringToAscii(tlmid).replaceAll("[^\\d.]", " ");
                    tltid = data.substring(16, 32);
                    tltid = StringLib.hexStringToAscii(tltid).replaceAll("[^\\d.]", " ");
                    tldate = data.substring(32, 38);
                    tldate = tldate.substring(0,2) + "/" + tldate.substring(2,4) + "/" + tldate.substring(4);
                    tltime = data.substring(38, 44);
                    tltime = tltime.substring(0,2) + ":" + tltime.substring(2,4) + ":" + tltime.substring(4);
                    tltcode = data.substring(44, 46);
                    tltcode = tltcode
                            .replace("EB", "Pembayaran          ")
                            .replace("EC", "Topup Online        ")
                            .replace("EF", "Aktivasi Deposit    ")
                            .replace("ED", "Void                ")
                            .replace("5F", "Reaktivasi          ")
                            .replace("FA", "Redeem              ");
                    tlamt = data.substring(46, 52);
                    tlamt = StringLib.HtoI(tlamt);
                    tlamt = StringLib.strToCurr(tlamt, "Rp");
                    tlbalb = data.substring(52, 58);
                    tlbala = data.substring(58);
                } catch (Exception e) {
                    // data too short
                }
                component = createComponent("2A2" + StringLib.fillZero(String.valueOf(seq),2),
                        "", tldate + " " + tltime + "    " + tltid,
                        String.valueOf(seq));
                comp.put(component);
                seq++;
                component = createComponent("2A2" + StringLib.fillZero(String.valueOf(seq),2),
                        "", tltcode + " " + tlamt,
                        String.valueOf(seq));
                comp.put(component);
            }
            comps.put("comp", comp);
            screen.put("comps", comps);
        } catch (JSONException e) {
            Log.e(TAG, "Compose Log Trx Error : " + e.getMessage());
        }
        return screen;
    }

    private JSONObject createComponent(String id, String label, String value, String seq) {
        JSONObject component = null;
        try {
            component = new JSONObject();
            component.put("visible", "true");
            component.put("comp_type", "1");
            component.put("comp_id", id);
            component.put("comp_lbl", label);
            component.put("seq", seq);
            JSONObject compValues = new JSONObject();
            JSONArray compValue = new JSONArray();
            JSONObject cmVal = new JSONObject();
            cmVal.put("value", value);
            cmVal.put("print", value);
            compValue.put(cmVal);
            compValues.put("comp_value", compValue);
            component.put("comp_values", compValues);
        } catch (JSONException e) {
            Log.e(TAG, "Compose Component Error : " + e.getMessage());
        }
        return component;
    }

    public void setFormListener(FormListener formListener) {
        this.formListener = formListener;
    }

    @Override
    public void imBegin() {
        printInUse = true;
    }

    @Override
    public void imFinised() {
        printInUse = false;
    }

    public interface FormListener {
        public void onSuccesListener(JSONObject obj);
    }

    private class PrintData implements Runnable {
        private List<PrintSize> data;
        private List<String> mdata;
        private String tid;
        private String mid;
        private String stan;
        private String nomorKartu;
        private boolean isRunning;
        private FinishedPrint flagMe;

        public PrintData(List<PrintSize> data) {
            this.data = data;
            this.isRunning = false;
        }

        public PrintData(List<PrintSize> data, List<String> mdata, String tid, String mid, String stan, String nomorKartu, FinishedPrint flagMe) {
            if (!footerAdded) {
                data = addStandardFooter(data);
                footerAdded = true;
            }
            this.data = data;
            this.mdata = mdata;
            this.tid = tid;
            this.mid = mid;
            this.stan = stan;
            this.nomorKartu = nomorKartu;
            this.flagMe = flagMe;
            this.isRunning = false;
            flagMe.imBegin();
        }

        public List<PrintSize> addStandardFooter(List<PrintSize> data) {
            data.add(new PrintSize(FontSize.NORMAL, "START_FOOTER"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "TRANSAKSI BERHASIL\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi\n"));
            data.add(new PrintSize(FontSize.NORMAL, "CONTACT BRI di 14017 atau 1500017,\n"));
            data.add(new PrintSize(FontSize.NORMAL, "***Terima Kasih***\n"));
            return data;
        }

        @Override
        public void run() {
            try {
                this.isRunning = true;
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
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Log.d("SWIPE", nomorKartu);
                if (tid != null) {
                    String cardType = "BRIZZI CARD (FLY)";
                    String txRefNum = "000000000000";
                    String svrDate = "0";
                    String svrTime = "0";
                    String svrAppr = "00000000";
                    if (cData.getHash4B() != null) {
                        if (!cData.getHash4B().equals("")) {
                            svrAppr = cData.getHash4B();
                        }
                    }
                    ESCPOSApi.printStruk(bitmap, data, mdata, tid, mid, stan, printcount, txRefNum,
                            svrDate, svrTime, cardType, nomorKartu, "TAPDIALOG", "000000", svrAppr);
                } else {
                    ESCPOSApi.printStruk(bitmap, data);
                }
                printcount++;
            } catch (Exception e) {
                this.isRunning = false;
                flagMe.imFinised();
            } finally {
                this.isRunning = false;
                flagMe.imFinised();
                isAntiDDOSPrint = true;
            }
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    public void cleanUpFailedVoidLog() {
        try {
            helperDb = new DataBaseHelper(context);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        writeDebugLog("EDCLOG", "read (4735)");
        String getLast = "select service_id from edc_log where log_id = " +
                "(select max(log_id) from edc_log)";
        Cursor eLast = clientDB.rawQuery(getLast, null);
        String lastServiceId = "";
        if (eLast!=null) {
            eLast.moveToFirst();
            lastServiceId = eLast.getString(0);
        }
        eLast.close();
        if (lastServiceId.equals("A2C200")) {
            writeDebugLog("MSGLOG", "update false service (4745)");
            String iMsgLog = "update messagelog set service_id = 'X2C200' where log_id = " +
                    "(select max(log_id) from messagelog)";
            clientDB.execSQL(iMsgLog);
            writeDebugLog("EDCLOG", "update false service (4749)");
            String elog = "update edc_log set service_id = 'X2C200' where log_id = " +
                    "(select max(log_id) from edc_log)";
            clientDB.execSQL(elog);
        }
        clientDB.close();
        helperDb.close();
    }

}
