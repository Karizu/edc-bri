/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.handler;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.wizarpos.apidemo.printer.FontSize;
import com.wizarpos.apidemo.printer.PrintSize;
import com.wizarpos.apidemo.smartcard.NeoSmartCardController;
//import com.wizarpos.apidemo.smartcard.SmartCardController;
import com.wizarpos.jni.PINPadInterface;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import id.co.bri.brizzi.ActivityList;
import id.co.bri.brizzi.SocketService;
import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.layout.FormMenu;
import id.co.bri.brizzi.module.listener.ChannelClient;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * @author Ahmad
 */
public class txHandler {

    static int POSUSER = 1;
    static int ACTIVEUSER = 1;
    static int BLOCKEDUSER = 3;
    static int PASSWORDRETRY = 3;
    static int IN_USE = 1;
    static int IDLE = 0;
    static byte[] busyResponse = ISO8583Parser.hexStringToByteArray("FF");
    String serviceid;
    private Cursor c;
    private Context ctx;
    private DataBaseHelper helperDb;
    private LogHandler EDCLog;
    private JSONObject jroot;
    private Date date = new Date();
    private int msgSequence = 0;
    private NeoSmartCardController smc;
    private ChannelClient cNio = null;
    private String stanvoid = "";
    String hsToHost;
    int elogid;
    int socket_status;
    boolean predefined_stan = false;
    boolean DEBUG_LOG = true;
    private List<PrintSize> printData;
    private boolean hasPrintData = false;
    private String printText = "";
    boolean matched_array = false;

    private static txHandler instance;
    private SQLiteDatabase clientDB;
    private int logId;
    private String trace_no;
    private String[] replyValues;

    private txHandler() {
        socket_status = IDLE;
    }

    public static txHandler getInstance() {
        if (instance==null) {
            instance = new txHandler();
        }
        return instance;
    }

    private static void logTrace(String pMessage) {
        Log.i("SOCKET", pMessage);
    }

    public void setContext(Context context) {
        this.ctx = context;

        this.ctx.bindService(new Intent(this.ctx, SocketService.class), myConnection, Context.BIND_AUTO_CREATE);
    }
    private SocketService myServiceBinder;

    public ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            myServiceBinder = ((SocketService.LocalBinder) binder).getServerInstance();
        }

        public void clientConnect(){
            myServiceBinder.clientConnect();
        }

        public void onServiceDisconnected(ComponentName className) {
//            Log.d("ServiceConnection","disconnected");
            myServiceBinder = null;
        }
    };

    //Main handler for service request (transaction API)
    @SuppressLint("CommitPrefEdits")
    public JSONObject processTransaction(Context context, String content) throws JSONException, Exception, IOException {
        //prepare return object

        SharedPreferences cekstatus = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        boolean deviceRegistered = false;
        boolean lastKeyChanged = false;
        if (cekstatus.contains("registered")) {
            cekstatus.edit().putBoolean("registered", true).apply();
            deviceRegistered = cekstatus.getBoolean("registered", false);
        }
        if (cekstatus.contains("lastkeychanged")) {
            lastKeyChanged = cekstatus.getBoolean("lastkeychanged", false);
        }
        boolean DEBUG_MODE = cekstatus.getBoolean("debug_mode", false);
        if (!DEBUG_MODE) {
            if (!deviceRegistered) {
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"EDC belum terdaftar\",\n" +
                        "\"value\":\"EDC tidak terdaftar\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Transaksi Gagal\"}}");
            }
        }
        helperDb = new DataBaseHelper(context);
        EDCLog = new LogHandler(context);
        clientDB = null;
        jroot = new JSONObject();
        setHasPrintData(false);
        JSONObject jmsg = new JSONObject();
        jmsg.put("msg_rc", "05");
        JSONObject jrequest = new JSONObject();
        JSONObject rqContent = new JSONObject();
        //parsing input request
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            jrequest = new JSONObject(content);
        } catch (Exception ex) {
            Log.i("TX", "DB error");
        }
        //get request from msg
        rqContent = (JSONObject) jrequest.get("msg");

        Log.d("MSGG", rqContent.toString());
        //use local function to parse transaction header
        String[] txElements = getTransactionElements(rqContent);
        jmsg.put("msg_id", txElements[0]);
        jmsg.put("msg_ui", txElements[1]);
        jmsg.put("msg_si", txElements[2]);
        boolean isLogon = txElements[2].equals("L00001");
        boolean isInitBrizzi = txElements[2].equals("A21100");
        boolean isNoInput = txElements[2].equals("A95000");
        boolean isNoInput2 = txElements[2].equals("A5C110");
        boolean isNoInput3 = txElements[2].equals("A24100");
        boolean isNoInput4 = txElements[2].equals("A24200");
        boolean isBrizziSettlement = txElements[2].equals("A28100");
        boolean isBrizziVoid = txElements[2].equals("A2C100");
        boolean isTunaiVoid = txElements[2].equals("A64000");
        boolean isBrizziSettlementConf = txElements[2].equals("A28100C");
        boolean isBrizziVoidConf = txElements[2].equals("A2C100C");
        boolean isTunaiVoidConf = txElements[2].equals("A64000C");
        serviceid = txElements[2];
        predefined_stan = false;
        if (isLogon) {
            cekstatus.edit().putBoolean("lastkeychanged", false).apply();
        } else {
            if (!lastKeyChanged) {
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak dapat melakukan transaksi, silahkan logon terlebih dahulu\",\n" +
                        "\"value\":\"Tidak dapat melakukan transaksi, silahkan logon terlebih dahulu\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Transaksi Gagal\"}}");
            }
        }
        if (rqContent.has("msg_stan")) {
            msgSequence = Integer.parseInt(rqContent.getString("msg_stan"));
            writeDebugLog("TAP2SERV", "Forced stan received : " + rqContent.getString("msg_stan"));
            writeDebugLog("TAP2SERV", "Int : " + String.valueOf(msgSequence));
            //Calibrating sequence
            if (!isBrizziVoid) {
                msgSequence--;
            }
            predefined_stan = true;
        }
        writeDebugLog("STAN : ", msgSequence+"");

        if (isInitBrizzi) {
            return doInitBrizzi(context);
        }
        if (isBrizziSettlement || isBrizziSettlementConf) {
            writeDebugLog("EDCLOG", "read (163)");
            String qry = "select * from edc_log where service_id like 'A24%' " +
                    "and (lower(settled) <> 't' or settled is null) and rc = '00' " +
                    "and (lower(reversed) <> 't' or reversed is null)";
            Cursor sData = clientDB.rawQuery(qry, null);
            StringBuilder idlist = new StringBuilder();
            idlist.append(" (");
            double totalSettlement = 0;
            String date;
            String time;
            String cardno;
            if (sData.moveToFirst()) {
                boolean onProgress = false;
                int i = 0;
                int j = 0;
                StringBuilder stringBuilder = new StringBuilder();
                do {
                    j = 0;
                    do {
                        String tms = sData.getString(sData.getColumnIndex("rqtime"));
                        date = "000000";
                        time = "000000";
                        if (tms.length() == 19) {
                            date = tms.substring(2, 4) + tms.substring(5, 7) + tms.substring(8, 10);
                            time = tms.substring(11, 13) + tms.substring(14, 16) + tms.substring(17);
                        }
                        stringBuilder.append(date);
                        stringBuilder.append(time);
                        String proccode = sData.getString(sData.getColumnIndex("proccode"));
                        stringBuilder.append(proccode);
                        double amt = sData.getDouble(sData.getColumnIndex("amount"));
                        totalSettlement += (amt/100);
                        stringBuilder.append(StringLib.fillZero(String.valueOf((int) amt), 10));
                        String stan = sData.getString(sData.getColumnIndex("stan"));
                        stringBuilder.append(stan);
                        String batchno = sData.getString(sData.getColumnIndex("batchno"));
                        stringBuilder.append(batchno.substring(batchno.length() - 2));
                        cardno = sData.getString(sData.getColumnIndex("track2"));
                        stringBuilder.append(cardno);
                        String hash = sData.getString(sData.getColumnIndex("hash"));
                        stringBuilder.append(hash);
                        idlist.append(String.valueOf(sData.getInt(sData.getColumnIndex("log_id"))));
                        if (!sData.isLast()) {
                            idlist.append(",");
                        }
                        j++;
                    } while (j<50&&sData.moveToNext());
                    i++;
                    String iServiceData = "insert or replace into service_data (message_id, name, value) "
                            + "values ('" + jmsg.get("msg_id") + "', 'rowdata', '"
                            + stringBuilder.toString() + "')";
                    clientDB.execSQL(iServiceData);
                    iServiceData = "insert or replace into service_data (message_id, name, value) "
                            + "values ('" + jmsg.get("msg_id") + "', 'pcode', '"
                            + "808200" + "')";
                    clientDB.execSQL(iServiceData);
//                    if (msgSequence==0&&!predefined_stan) {
                    String getStanSeq = "select seq msgSequence from holder";
//                        String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                                "from edc_log where date(rqtime) = date('now') and rc = '00' ";
                        Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
                        if (stanSeq != null) {
                            stanSeq.moveToFirst();
                            msgSequence = stanSeq.getInt(0);
                        }
                        stanSeq.close();
//                    }
                    String trace_no = generateStan();
                    String toParse = jmsg.get("msg_id") + "|" + txElements[3] + "|" + txElements[4];
                    if (isBrizziSettlementConf) continue;

                    ISO8583Parser rqParser = new ISO8583Parser(context, "6000070000", serviceid, toParse, 1, trace_no);
//                    String uStanSeq = "update holder set "
//                            + "seq = " + msgSequence;
//                    writeDebugLog("UPDATING", "HOLDER (229)");
//                    clientDB.execSQL(uStanSeq);
                    onProgress = true;
                    byte[] toHost = rqParser.parseJSON();
                    int cLen = toHost.length;
                    byte[] hLen = Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(cLen).array(), 2, 4);
                    byte[] formattedContent = ByteBuffer.allocate(2 + cLen).put(hLen).put(toHost).array();
                    hsToHost = ISO8583Parser.bytesToHex(formattedContent);
                    SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                    String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
                    int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
                    AsyncMessageWrapper amw = new AsyncMessageWrapper(host_ip, host_port, hsToHost);
                    byte[] fromHost = sendMessage(amw, 30000);
                    while (onProgress) {
                        Thread.sleep(1000);
                        if (socket_status==IDLE) {
                            onProgress = false;
                        }
                    }
                    if (fromHost==null) {
                        if (clientDB.isOpen()) {
                            clientDB.close();
                        }
                        return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak mendapat response settlement\",\n" +
                                "\"value\":\"Tidak mendapat response settlement\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                "\"type\":\"3\",\"title\":\"Settlement\"}}");
                    }
                } while (sData.moveToNext());
                String baris = String.valueOf(((i-1)*50)+j);
                idlist.append(") ");
                int ts = (int) totalSettlement;
                String stringTs = StringLib.strToCurr(String.valueOf(ts), "Rp");
                if (isBrizziSettlementConf){
//                    return new JSONObject("{\"screen\":{\"ver\":\"1\", \"comps\":{\"comp\":[{\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\""+cardno+"\", \"value\":\""+cardno+"\"} ] }, \"comp_lbl\":\"No. Kartu BRIZZI\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + j + "\", \"value\":\"" + j + "\"} ] }, \"comp_lbl\":\"Jumlah Transaksi\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + stringTs + "\", \"value\":\"" + stringTs + "\"} ] }, \"comp_lbl\":\"Total Settlement\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":1 }, {\"visible\":true, \"comp_lbl\":\"Proses\", \"comp_type\":\"7\", \"comp_id\":\"\", \"seq\":3 } ] }, \"id\":\"2800000C\", \"type\":\"1\", \"action_url\":\"A28100\", \"title\":\"Konfirmasi Settlement BRIZZI\", \"server_date\":\"" + date + "\", \"server_time\":\"" + time + "\"} }");
                    return new JSONObject("{\"screen\":{\"ver\":\"1\", \"comps\":{\"comp\":[{\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + j + "\", \"value\":\"" + j + "\"} ] }, \"comp_lbl\":\"Jumlah Transaksi\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + stringTs + "\", \"value\":\"" + stringTs + "\"} ] }, \"comp_lbl\":\"Total Settlement\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":1 }, {\"visible\":true, \"comp_lbl\":\"Proses\", \"comp_type\":\"7\", \"comp_id\":\"\", \"seq\":3 } ] }, \"id\":\"2800000C\", \"type\":\"1\", \"action_url\":\"A28100\", \"title\":\"Konfirmasi Settlement BRIZZI\", \"server_date\":\"" + date + "\", \"server_time\":\"" + time + "\"} }");
                }
                //fix dont display all brizzi report after settlement
//                String updDB = "update edc_log set settled = 't' where log_id in " + idlist.toString() + ";";
                writeDebugLog("EDCLOG", "update settled (264)");
                String updDB = "update edc_log set settled = 't' where service_id like 'A2%';";
                clientDB.execSQL(updDB);
                String updBatch = "update holder set batch = case batch when 99 then 0 else batch + 1 end";
                writeDebugLog("UPDATING", "HOLDER BATCH (266)");
                clientDB.execSQL(updBatch);
                clientDB.close();
//start mod settlement printout
                String cmp = "";
                int sq = 0;
                String ssq = String.valueOf(sq);
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"TRANSAKSI           COUNT         AMOUNT\",\n"
                        + "\"value\":\"TRANSAKSI           COUNT         AMOUNT\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                String clab = "PEMBAYARAN";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"" + stringTs + "\",\n"
                        + "\"value\":\"" + stringTs + "\"}"
                        + "]},\"comp_lbl\":\"" + clab + " :"+ baris +"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"[B]"+ stringTs +"\",\n"
                        + "\"value\":\"[B]"+ stringTs +"\"}"
                        + "]},\"comp_lbl\":\"[B]TOTAL :" + baris + "\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":["
                        + cmp
                        + "]},\"id\":\"281000F\",\n" +
                        "\"type\":\"1\",\"title\":\"Settlement BRIZZI\",\"print\":\"2\",\"print_text\":\"STL\"}}");
//end mod settlement printout
//                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
//                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\""+baris+" transaksi\"," +
//                        "\"value\":\""+baris+" transaksi\"}]},\"comp_lbl\":\"Jumlah : \",\"comp_type\":\"1\",\"comp_id\":\"281001\",\"seq\":0}," +
//                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\""+stringTs+"\"," +
//                        "\"value\":\""+stringTs+"\"}]},\"comp_lbl\":\"Total  : \",\"comp_type\":\"1\",\"comp_id\":\"281002\",\"seq\":1}," +
//                        "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\" \"," +
//                        "\"value\":\" \"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"281003\",\"seq\":2}" +
//                        "]},\"id\":\"281000F\",\"type\":\"1\",\"title\":\"Settlement BRIZZI Report\",\"print\":\"2\",\"print_text\":\"STL\"}}");
            } else {
                if (clientDB.isOpen()) {

                    clientDB.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak terdapat data settlement\",\n" +
                        "\"value\":\"Tidak terdapat data settlement\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Settlement\"}}");
            }
        }
        if (isBrizziVoid || isBrizziVoidConf) {
            writeDebugLog("VOID", (String) rqContent.get("msg_dt"));
            String trace_number = (String) rqContent.get("msg_dt");
            writeDebugLog("EDCLOG", "read (362)");
            String qry = "select * from edc_log where service_id like 'A24%' " +
                    "and (lower(settled) <> 't' or settled is null) and rc = '00' " +
                    "and (lower(reversed) <> 't' or reversed is null) " +
                    "and date(rqtime) = date('now') " +
                    "and cast(stan as integer) = " + "'" + trace_number + "'" + " order by rqtime desc";
            Cursor sData = clientDB.rawQuery(qry, null);
            StringBuilder stringBuilder = new StringBuilder();
            boolean onProgress = false;
            if (sData.moveToFirst()) {
                stanvoid = String.valueOf(sData.getInt(sData.getColumnIndex("log_id")));
                String tms = sData.getString(sData.getColumnIndex("rqtime"));
                String date = "000000";
                String time = "000000";
                if (tms.length() == 19) {
                    date = tms.substring(2, 4) + tms.substring(5, 7) + tms.substring(8, 10);
//                    date = tms.substring(8, 10) + tms.substring(5, 7) + tms.substring(2, 4);
                    time = tms.substring(11, 13) + tms.substring(14, 16) + tms.substring(17);
                }
                stringBuilder.append(date);
                stringBuilder.append(time);
                String proccode = sData.getString(sData.getColumnIndex("proccode"));
                stringBuilder.append(proccode);
                double amt = sData.getDouble(sData.getColumnIndex("amount"));
                stringBuilder.append(StringLib.fillZero(String.valueOf((int) amt), 10));
                String stan = sData.getString(sData.getColumnIndex("stan"));
                stringBuilder.append(stan);
                String batchno = sData.getString(sData.getColumnIndex("batchno"));
                stringBuilder.append(batchno.substring(batchno.length() - 2));
                String cardno = sData.getString(sData.getColumnIndex("track2"));
                stringBuilder.append(cardno);
                String hash = sData.getString(sData.getColumnIndex("hash"));
                stringBuilder.append(hash);
                String iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'rowdata', '"
                        + stringBuilder.toString() + "')";
                clientDB.execSQL(iServiceData);
                iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'pcode', '"
                        + "808201" + "')";
                clientDB.execSQL(iServiceData);
                if (stan != null && !stan.trim().equals("")) {
                    msgSequence = Integer.parseInt(stan);
                    writeDebugLog("TAP2SERV", "Forced stan received : " + stan);
                    writeDebugLog("TAP2SERV", "Int : " + String.valueOf(msgSequence));
                    predefined_stan = true;
                }

                if (!predefined_stan) {
                    String getStanSeq = "select seq msgSequence from holder";
                    //STAN DAN WAKTU SESUAI DENGAN YANG DI VOID
                    //HOLDER GA NAMBAh\
//                    String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                            "from edc_log where date(rqtime) = date('now') and rc = '00' ";
                    Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
                    if (stanSeq != null) {
                        stanSeq.moveToFirst();
                        msgSequence = stanSeq.getInt(0);
                    }
                    stanSeq.close();
                }
                msgSequence = Integer.valueOf(stanvoid);
//                String trace_no = String.format("%06d", msgSequence);
                String trace_no = generateStan();
                if (!serviceid.equals("A2C100") && !serviceid.equals("A2C200") && !serviceid.equals("A2C000") && !serviceid.equals("A2C100C")) {
                    String uStanSeq = "update holder set " + "seq = " + trace_no;
                    clientDB.execSQL(uStanSeq);
                }
                String toParse = jmsg.get("msg_id") + "|" + txElements[3] + "|" + txElements[4];

                String stringTs = StringLib.strToCurr(String.valueOf(amt / 100), "Rp");
                if (isBrizziVoidConf) {
                    return new JSONObject("{\"screen\":{\"ver\":\"1\", \"comps\":{\"comp\":[{\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\""+cardno+"\", \"value\":\""+cardno+"\"} ] }, \"comp_lbl\":\"No. Kartu BRIZZI\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + stan + "\", \"value\":\"" + stan + "\"} ] }, \"comp_lbl\":\"Trace No.\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + stringTs + "\", \"value\":\"" + stringTs + "\"} ] },\"comp_lbl\":\"Jumlah Void\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":1 }, {\"visible\":false, \"comp_values\":{\"comp_value\":[{\"print\":\"" + Integer.valueOf(stan) + "\", \"value\":\"" + Integer.valueOf(stan) + "\"} ] }, \"comp_lbl\":\"Trce No\", \"comp_type\":\"2\", \"comp_id\":\"\", \"seq\":2 }, {\"visible\":true, \"comp_lbl\":\"Proses\", \"comp_type\":\"7\", \"comp_id\":\"\", \"seq\":3 } ] }, \"id\":\"2C10000C\", \"type\":\"1\", \"action_url\":\"A2C100\", \"title\":\"Konfirmasi Void BRIZZI\", \"server_date\":\"" + date + "\", \"server_time\":\"" + time + "\" } }");
                }

                ISO8583Parser rqParser = new ISO8583Parser(context, "6000070000", serviceid, toParse, 1, trace_no);
                onProgress = true;
                byte[] toHost = rqParser.parseJSON();
                int cLen = toHost.length;
                byte[] hLen = Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(cLen).array(), 2, 4);
                byte[] formattedContent = ByteBuffer.allocate(2 + cLen).put(hLen).put(toHost).array();
                hsToHost = ISO8583Parser.bytesToHex(formattedContent);
                SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
                int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
                AsyncMessageWrapper amw = new AsyncMessageWrapper(host_ip, host_port, hsToHost);
                byte[] fromHost = sendMessage(amw, 30000);
                while (onProgress) {
                    Thread.sleep(1000);
                    if (socket_status == IDLE) {
                        onProgress = false;
                    }
                }
                if (fromHost == null) {
                    if (clientDB.isOpen()) {
                        clientDB.close();
                    }
                    return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak mendapat response void\",\n" +
                            "\"value\":\"Tidak mendapat response void\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                            "\"type\":\"3\",\"title\":\"Void BRIZZI\"}}");
                } else {

                    //menambahkan popup FF
                    if (ISO8583Parser.bytesToHex(fromHost).equals("FF")){
                        return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Terjadi kesalahan pada host\",\n" +
                                "\"value\":\"Terjadi kesalahan pada host\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                "\"type\":\"3\",\"title\":\"Void BRIZZI\"}}");
                    }

                    ISO8583Parser rpParser = new ISO8583Parser(context, "6000070000", ISO8583Parser.bytesToHex(fromHost), 2);
                    String[] replyValues = rpParser.getIsoBitValue();
                    String serverRef = replyValues[37];
                    String serverApr = replyValues[38];
                    String serverDate = replyValues[13];
                    String serverTime = replyValues[12];
                    String voidTime = sData.getString(sData.getColumnIndex("rqtime"));
                    if (clientDB.isOpen()) {
                        clientDB.close();
                    }

                    //Menambahkan popup NV
                    if (replyValues[39].equals("00")) {

                    return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"void telah terkirim\",\n" +
                            "\"value\":\"void telah terkirim\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}," +
                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + stanvoid + "\",\n" +
                            "\"value\":\"" + stanvoid + "\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00002\",\"seq\":1}" +
                            "]},\"id\":\"2C10000\",\n" +
                            "\"type\":\"1\",\"action_url\":\"A2C100\",\"title\":\"Void BRIZZI\",\"server_date\":\"" + serverDate + "\"" +
                            ",\"server_time\":\"" + serverTime + "\",\"server_ref\":\"" + serverRef + "\",\"server_appr\":\"" + serverApr + "\"}}");
                }

                    else {
                        return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak terdapat data transaksi\",\n" +
                                "\"value\":\"Transaksi Not Valid\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                                "\"type\":\"3\",\"title\":\"Void BRIZZI\"}}");
                    }
                }
            } else {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak terdapat data transaksi\",\n" +
                        "\"value\":\"Tidak terdapat data transaksi\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Void BRIZZI\"}}");
            }
        }
        if (isTunaiVoid || isTunaiVoidConf) {
            String trace_number = (String) rqContent.get("msg_dt");
            writeDebugLog("MSGLOG", "read (466)");
            writeDebugLog("EDCLOG", "read (467)");
            String qry = "select a.track2, a.log_id, b.response_message, a.amount, a.stan, b.log_id mlog from edc_log a, messagelog b where " +
                    "a.stan = b.message_id and date(a.rqtime)=date(b.request_time) and a.service_id=b.service_id " +
                    "and a.service_id = 'A63001' " +
                    "and (lower(a.settled) <> 't' or a.settled is null) and rc = '00' " +
                    "and (lower(a.reversed) <> 't' or a.reversed is null) " +
                    "and cast(a.stan as integer) = " + trace_number + " order by a.rqtime desc";
            Cursor sData = clientDB.rawQuery(qry, null);
            StringBuilder stringBuilder = new StringBuilder();
            boolean onProgress = false;
            if (sData.moveToFirst()) {
                // do void tarik tunai here (reversal)
                stanvoid = String.valueOf(sData.getInt(sData.getColumnIndex("log_id")));
                String mlogid = String.valueOf(sData.getInt(sData.getColumnIndex("mlog")));
                String ttResp = sData.getString(sData.getColumnIndex("response_message"));
                JSONObject ttScreen = new JSONObject(ttResp);
                double amt = sData.getDouble(sData.getColumnIndex("amount"))/100;
                String ttAmount = String.valueOf((int) amt);
                String iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'sal_amount', '"
                        + ttAmount + "')";
                clientDB.execSQL(iServiceData);
                String ttDate = (String) ttScreen.get("server_date");
                iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'old_date', '"
                        + ttDate + "')";
                clientDB.execSQL(iServiceData);
                String ttTime = (String) ttScreen.get("server_time");
                iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'old_time', '"
                        + ttTime + "')";
                clientDB.execSQL(iServiceData);
                String ttRef = sData.getString(sData.getColumnIndex("stan"));
                iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'old_ref', '"
                        + ttRef + "')";
                clientDB.execSQL(iServiceData);
                String ttAir = (String) ttScreen.get("server_air");
                iServiceData = "insert or replace into service_data (message_id, name, value) "
                        + "values ('" + jmsg.get("msg_id") + "', 'old_air', '"
                        + ttAir + "')";
                clientDB.execSQL(iServiceData);
                String ttTrack2 = sData.getString(sData.getColumnIndex("track2"));
                stringBuilder.append(ttTrack2);
                String cardNo = ttTrack2.substring(0, ttTrack2.indexOf("="));
                stringBuilder.append(cardNo);
//                iServiceData = "insert or replace into service_data (message_id, name, value) "
//                        + "values ('" + jmsg.get("msg_id") + "', 'nomor_kartu', '"
//                        + cardNo + "')";
//                clientDB.execSQL(iServiceData);

//                if (msgSequence == 0&&!predefined_stan) {
                    String getStanSeq = "select seq msgSequence from holder";
//                    String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                            "from edc_log where date(rqtime) = date('now') and rc = '00' ";
                    Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
                    if (stanSeq != null) {
                        stanSeq.moveToFirst();
                        msgSequence = stanSeq.getInt(0);
                    }
                    stanSeq.close();
//                }
//                String trace_no = generateStan();

                String stringTs = StringLib.strToCurr(String.valueOf(amt), "Rp");

                if (isTunaiVoidConf){
                    return new JSONObject("{\"screen\":{\"ver\":\"1\", \"comps\":{\"comp\":[{\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\""+cardNo+"\", \"value\":\""+cardNo+"\"} ] }, \"comp_lbl\":\"No. Kartu\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + trace_number + "\", \"value\":\"" + trace_number + "\"} ] }, \"comp_lbl\":\"Trace No.\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":0 }, {\"visible\":true, \"comp_values\":{\"comp_value\":[{\"print\":\"" + stringTs + "\", \"value\":\"" + stringTs + "\"} ] },\"comp_lbl\":\"Jumlah Void\", \"comp_type\":\"1\", \"comp_id\":\"\", \"seq\":1 }, {\"visible\":false, \"comp_values\":{\"comp_value\":[{\"print\":\"" + Integer.valueOf(trace_number) + "\", \"value\":\"" + Integer.valueOf(trace_number) + "\"} ] }, \"comp_lbl\":\"Trce No\", \"comp_type\":\"2\", \"comp_id\":\"\", \"seq\":2 }, {\"visible\":true, \"comp_lbl\":\"Proses\", \"comp_type\":\"7\", \"comp_id\":\"\", \"seq\":3 } ] }, \"id\":\"6400000C\", \"type\":\"1\", \"action_url\":\"A64000\", \"title\":\"Konfirmasi Void Tunai\", \"server_date\":\"" + ttDate + "\", \"server_time\":\"" + ttTime + "\" } }");
                }

                String toParse = jmsg.get("msg_id") + "|" + txElements[3] + "|" + txElements[4];
                ISO8583Parser rqParser = new ISO8583Parser(context, "6000070000", serviceid, toParse, 1, trace_number);
                onProgress = true;
                byte[] toHost = rqParser.parseJSON();
                int cLen = toHost.length;
                byte[] hLen = Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(cLen).array(), 2, 4);
                byte[] formattedContent = ByteBuffer.allocate(2 + cLen).put(hLen).put(toHost).array();
                hsToHost = ISO8583Parser.bytesToHex(formattedContent);
                SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
                int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
                AsyncMessageWrapper amw = new AsyncMessageWrapper(host_ip, host_port, hsToHost);
                byte[] fromHost = sendMessage(amw, 30000);
                while (onProgress) {
                    Thread.sleep(1000);
                    if (socket_status == IDLE) {
                        onProgress = false;
                    }
                }
                if (fromHost == null) {
                    if (clientDB.isOpen()) {
                        clientDB.close();
                    }
                    return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak mendapat response void\",\n" +
                            "\"value\":\"Tidak mendapat response void\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                            "\"type\":\"3\",\"title\":\"Void Tarik Tunai\"}}");
                } else {
                    ISO8583Parser rpParser = new ISO8583Parser(context, "6000070000", ISO8583Parser.bytesToHex(fromHost), 2);
                    replyValues = rpParser.getIsoBitValue();
                    String msg_rc = "";
                    if (replyValues[39] != null) {
                        msg_rc = replyValues[39];
                        if (!((msg_rc.equals("00")))) {
                            rpParser.setServiceId(serviceid);
                            rpParser.setMessageId((String) jmsg.get("msg_id"));
                            rpParser.setResponseCode(msg_rc);
                            JSONObject replyJSON = rpParser.parseISO();
                            MenuListResolver mlr = new MenuListResolver();
                            jroot = mlr.loadMenu(context, "000000F", replyJSON);
                            if (clientDB.isOpen()) {
                                clientDB.close();
                            }
                            return jroot;
                        }
                    }
//                    String uStanSeq = "update holder set "
//                            + "seq = " + msgSequence;
//                    writeDebugLog("UPDATING", "HOLDER (519)");
//                    clientDB.execSQL(uStanSeq);
                    String serverRef = replyValues[37];
                    String serverApr = replyValues[38];
                    String serverDate = replyValues[13];
                    String serverTime = replyValues[12];
                    String voidAmount = replyValues[4];
                    String addData = replyValues[48];
                    String saldo = "0000000000000000";
                    String fee = "        ";
                    try {
                        saldo = addData.substring(0, 16);
                        fee = addData.substring(16, 24);
                    } catch (Exception e) {
                        //pass
                    }
                    Date dt = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String repDate = sdf.format(dt) + "-";
                    String tmStamp = null;
                    repDate = repDate + serverDate.substring(0,2) + "-" +
                            serverDate.substring(2,4);
                    tmStamp = StringLib.toSQLiteTimestamp(repDate, serverTime);
                    double d = Double.parseDouble(voidAmount);
                    d = d/100;
                    voidAmount = StringLib.strToCurr(String.valueOf((int) d),"Rp");
                    d = Double.parseDouble(saldo);
                    d = d/100;
                    saldo = StringLib.strToCurr(String.valueOf((int) d),"Rp");
                    if (fee.replaceAll(" ","").equals("")) {
                        fee = "0";
                    }
                    d = Double.parseDouble(fee);
                    fee = StringLib.strToCurr(String.valueOf((int) d),"Rp");
                    JSONObject returnScreen = new JSONObject("{\"nomor_kartu\":\""+ cardNo +"\",\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + voidAmount + "\",\n" +
                            "\"value\":\"" + voidAmount + "\"}]},\"comp_lbl\":\"Jumlah Void : \",\"comp_type\":\"1\",\"comp_id\":\"P00002\",\"seq\":0}," +
                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + fee + "\",\n" +
                            "\"value\":\"" + fee + "\"}]},\"comp_lbl\":\"Fee         : \",\"comp_type\":\"1\",\"comp_id\":\"P00003\",\"seq\":1}," +
                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + saldo + "\",\n" +
                            "\"value\":\"" + saldo + "\"}]},\"comp_lbl\":\"Saldo       : \",\"comp_type\":\"1\",\"comp_id\":\"P00004\",\"seq\":2}" +
                            "]},\"id\":\"640000F\",\n" +
                            "\"type\":\"1\",\"title\":\"Void Tarik Tunai\",\"print\":\"2\",\"print_text\":\"WF\",\"server_date\":\"" + serverDate + "\"" +
                            ",\"server_time\":\"" + serverTime + "\",\"server_ref\":\"" + serverRef +"\",\"server_appr\":\""+serverApr+ "\"},\"server_date\":\""+serverDate+"\"," +
                            "\"server_time\":\""+serverTime+"\",\"server_ref\":\""+serverRef+"\",\"server_appr\":\""+serverApr+"\"}");
//                    JSONObject returnScreen = new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[" +
//                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + voidAmount + "\",\n" +
//                            "\"value\":\"" + voidAmount + "\"}]},\"comp_lbl\":\"Jumlah Void : \",\"comp_type\":\"1\",\"comp_id\":\"P00002\",\"seq\":0}," +
//                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + fee + "\",\n" +
//                            "\"value\":\"" + fee + "\"}]},\"comp_lbl\":\"Fee         : \",\"comp_type\":\"1\",\"comp_id\":\"P00003\",\"seq\":1}," +
//                            "{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"" + saldo + "\",\n" +
//                            "\"value\":\"" + saldo + "\"}]},\"comp_lbl\":\"Saldo       : \",\"comp_type\":\"1\",\"comp_id\":\"P00004\",\"seq\":2}" +
//                            "]},\"id\":\"640000F\",\n" +
//                            "\"type\":\"1\",\"title\":\"Void Tarik Tunai\",\"print\":\"2\",\"print_text\":\"WF\",\"server_date\":\"" + serverDate + "\"" +
//                            ",\"server_time\":\"" + serverTime + "\",\"server_ref\":\"" + serverRef +"\",\"server_appr\":\""+serverApr+ "\"},\"server_date\":\""+serverDate+"\"," +
//                            "\"server_time\":\""+serverTime+"\",\"server_ref\":\""+serverRef+"\",\"server_appr\":\""+serverApr+"\"}");
                    writeDebugLog("EDCLOG", "update (588)");
                    String q = "update edc_log set service_id = 'A64000', rran = '" + serverRef +
                            "', rqtime = '"+tmStamp+"' where log_id = '" + stanvoid + "';";
                    clientDB.execSQL(q);
                    q = "select max(log_id) newid from messagelog";
                    Cursor getnewlogid = clientDB.rawQuery(q, null);
                    String newlogid = mlogid;
                    if (getnewlogid.moveToFirst()) {
                        newlogid = String.valueOf(getnewlogid.getInt(0)+1);
                    }
                    getnewlogid.close();
                    writeDebugLog("MSGLOG", "update (590)");
                    String logUpdate = "update messagelog set response_message = '"+returnScreen.toString()+"', " +
                            "service_id = 'A64000', request_time = '" + tmStamp + "', log_id = " + newlogid +
                            " where log_id = " + mlogid;
                    clientDB.execSQL(logUpdate);
                    if (clientDB.isOpen()) {
                        clientDB.close();
                    }
                    return returnScreen;
                }
            } else {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Tidak terdapat data transaksi\",\n" +
                        "\"value\":\"Tidak terdapat data transaksi\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Void Tarik Tunai\"}}");
            }
        }
        //handler for incomplete header including unregistered terminal
        if (Arrays.asList(txElements).contains("") && !isLogon ) {
            return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak lengkap\",\n" +
                    "\"value\":\"Header transaksi tidak lengkap atau terminal tidak terdaftar\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
        }
        // intercept reprint last transaction
        if (txElements[2].startsWith("P")) {
            writeDebugLog("MSGLOG", "read (610)");
            writeDebugLog("EDCLOG", "read (611)");
            String qLog = "select a.*, b.track2 from messagelog a left outer join edc_log b on (a.message_id=b.stan and a.service_id=b.service_id)"
                    + "where a.service_id like 'A" + txElements[2].substring(1) + "%' "
                    + "and a.message_status = '00' "
                    + "and a.print > 0 "
                    + "and (lower(b.settled) <> 't' or b.settled is null) "
                    + "and a.service_id not in ('A2A100','A29100','A23100',"
                    + "'A22000','A23000','A22100','A2B000','A2B100', 'A52100', 'A52210', 'A52220', 'A52300', 'A59000', 'A91000', 'A95000', 'A5C110', 'A54331') "
                    + "order by a.log_id desc";
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String screen_value = cLog.getString(cLog.getColumnIndex("response_message"));
                String screen_trace = cLog.getString(cLog.getColumnIndex("message_id"));
                String cardUsed = cLog.getString(cLog.getColumnIndex("track2"));
                String cardType = "DEBIT (SWIPE)";
                if (cardUsed != null) {
                    if (!cardUsed.contains("=")) {
                        cardType = "BRIZZI CARD (FLY)";
                    }
                }
                if (cardType.equals("")) {
                    cardType = "";
                }
                if (cLog!=null) {
                    cLog.close();
                }
                JSONObject rps = new JSONObject(screen_value);
                rps.put("reprint", 1);
                rps.put("rstan", screen_trace);
                if (cardUsed!=null) {
//                    if(!cardUsed.equals("") && cardType.equals("SMART CARD (FLY)")) {
//                        rps.put("nomor_kartu", cardUsed);
//                    }
                    if(!cardUsed.equals("") && cardType.equals("DEBIT (SWIPE)")){
                        rps.put("nomor_kartu", cardUsed);
                    }
                }
                rps.put("card_type", cardType);
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                return rps;
            } else {
                if (cLog!=null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        }
        if (txElements[2].startsWith("Q")) {
            String trace_number = (String) rqContent.get("msg_dt");
            writeDebugLog("MSGLOG", "read (656)");
            writeDebugLog("EDCLOG", "read (657)");
            String qLog = "select a.*, b.track2 from messagelog a left outer join edc_log b on (a.message_id=b.stan  and a.service_id=b.service_id)"
                    + "where cast(a.message_id as integer) = " + trace_number + " "
                    + "and a.service_id like 'A" + txElements[2].substring(1) + "%' "
                    + "and a.message_status = '00' "
                    + "and a.print > 0 "
                    + "and (lower(b.settled) <> 't' or b.settled is null) "
                    + "and a.service_id not in ('A2A100','A29100','A23100',"
                    + "'A22000','A23000','A22100','A2B000','A2B100', 'A52100', 'A52210', 'A52220', 'A52300', 'A59000', 'A91000', 'A95000', 'A5C110', 'A54331') "
                    + "order by a.log_id desc";
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String screen_value = cLog.getString(cLog.getColumnIndex("response_message"));
                String screen_trace = cLog.getString(cLog.getColumnIndex("message_id"));
                String cardUsed = cLog.getString(cLog.getColumnIndex("track2"));
                String cardType = "DEBIT (SWIPE)";
                if (cardUsed!= null && !cardUsed.contains("=")) {
                    cardType = "BRIZZI CARD (FLY)";
                }
                if (cardType == null || cardType.equals("")) {
                    cardType = "";
                }
                if (cLog!=null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                JSONObject rps = new JSONObject(screen_value);
                rps.put("reprint", 1);
                rps.put("rstan", screen_trace);
//                if (cardUsed!=null) {
//                    if (!cardUsed.equals("")) {
//                        rps.put("nomor_kartu", cardUsed);
//                    }
//                }
                if (cardUsed!=null) {
//                    if (!cardUsed.equals("") && cardType.equals("SMART CARD (FLY)")) {
//                        rps.put("nomor_kartu", cardUsed);
//                    }
                    if(!cardUsed.equals("") && cardType.equals("DEBIT (SWIPE)")){
                        rps.put("nomor_kartu", cardUsed);
                }
                }
                rps.put("card_type", cardType);
                return rps;
            } else {
                if (cLog!=null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        }
        if (txElements[2].startsWith("A5B1")||
                txElements[2].startsWith("A661")||
                txElements[2].startsWith("A751")||
                txElements[2].startsWith("A2F1")||
                txElements[2].startsWith("A9B1")){
            Log.d("INI MAH YG DI ATAS", "MASUK IF");
            // Report Summary intercept
            String dateFilter = "";
            String tgl = "";
//            if (txElements[2].endsWith("10")||!txElements[2].startsWith("A2")) {
            if (txElements[2].endsWith("10")) {
                tgl = (String) rqContent.get("msg_dt");
                tgl = tgl.substring(4)+"-"+tgl.substring(2,4)+"-"+tgl.substring(0,2);
                dateFilter = "and date(a.rqtime)='" + tgl + "'\n";
            }
            if (txElements[3].endsWith("30")) {
                dateFilter = "and date(a.rqtime)='" + StringLib.getYYYYMMDD() + "'\n";
            }
            String modBase = "edc_log";
            String excludeList = "";
            if (txElements[2].startsWith("A2F1")) {
                modBase = "(select service_id, rc, rqtime, reversed, settled, (amount*sign) as amount from (select *, " +
                        "case service_id when 'A24100' then 1 when 'A24200' then 1 else 1 end sign " +
                        "from edc_log))";
//                excludeList = "and a.service_id not in ('A2C100', 'A2C200')\n";
                excludeList = "and (lower(a.settled) <> 't' or a.settled is null) \n" +
                        "and a.service_id not in ('A2A100','A29100','A23100'," +
                        "'A22000','A23000','A22100','A2B000','A2B100')\n";
            }
            if (txElements[2].startsWith("A6")) {
                Log.d("INI MAH YG DI ATAS", "MASUK IF A6");
                excludeList = "and a.service_id not in ('A61000', 'A62000', 'A63000')\n";
            }
            if (txElements[2].startsWith("A7")) {
                Log.d("INI MAH YG DI ATAS", "MASUK IF A7");
                excludeList = "and a.service_id not in ('A71001', 'A72000', 'A72001', 'A73000')\n";
            }
            if (txElements[2].startsWith("A5")) {
                Log.d("INI MAH YG DI ATAS", "MASUK IF A5");
                excludeList = "and a.service_id not in ('A54911', 'A51410', 'A53100', 'A53211', 'A53221', 'A54921', 'A54931', 'A54941', \n" +
                        "'A54B11', 'A54A10', 'A54110', 'A54211', 'A54221', 'A54311', 'A54321', 'A54331', 'A54341', 'A54410', 'A5C210', 'A5C110', \n" +
                        "'A54431', 'A54433', 'A54441', 'A54443', 'A54451', 'A54453', 'A54461', 'A54510', \n" +
                        "'A54520', 'A54530', 'A54540', 'A54550', 'A54560', 'A57000', 'A57200', 'A57400', \n" +
                        "'A58000', 'A54421', 'A54423', 'A54C10', 'A54C20', 'A54C51', 'A54C52', 'A54C53', \n" +
                        "'A54C54', 'A52100', 'A52210', 'A52220', 'A52300', 'A54950', 'A54710', 'A54720', " +
                        "'A54800', 'A59000') \n";
            }
            if (txElements[2].startsWith("A9")) {
                excludeList = "and a.service_id not in ('A91000', 'A92000', 'A93000', 'A94000', 'A95000')\n";
            }
            String siLimit = txElements[2].substring(0,2);
            String nGrand = "0";
            String jGrand = "0";
            writeDebugLog("EDCLOG", "read grand (744)");
            String qGrand = ""
                    + "select sum(a.amount) tot, count(*) jml, service_name from " + modBase + " a left outer join service b\n" +
                    "on (a.service_id = b.service_id)\n" +
                    "where a.rc= '00'\n" +
                    dateFilter +
//                    "and a.amount is not null\n" +
//                    "and a.amount <> 0\n" +
                    "and (lower(a.reversed) <> 't' or a.reversed is null)\n" +
                    "and a.service_id like '" + siLimit + "%'\n" +
                    excludeList;
            writeDebugLog("RPT", "Query Grand \n" + qGrand);
            Cursor cGrand = clientDB.rawQuery(qGrand, null);
            if (cGrand.moveToFirst()) {
                nGrand = String.valueOf(cGrand.getInt(cGrand.getColumnIndex("tot")));
                jGrand = String.valueOf(cGrand.getInt(cGrand.getColumnIndex("jml")));
                String cTx = cGrand.getString(cGrand.getColumnIndex("service_name"));
                if (nGrand.matches("-?\\d+(\\.\\d+)?")) {
                    double d = Double.parseDouble(nGrand);
                    DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                    idrFormat.setDecimalSeparator(',');
                    DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
//                    if (cTx!=null){
//                        if (!cTx.startsWith("Pembayaran PLN") && !cTx.startsWith("Pembayaran Non-")) {
//                            d = d/100;
//                        }
//                    }
                    d = d/100;
                    nGrand = formatter.format(d);
                }
            }
            if (cGrand!=null) {
                cGrand.close();
            }
            writeDebugLog("EDCLOG", "read (772)");
            String qLog = ""
                    + "select (case when b.service_name is null then a.service_id else b.service_name end)" +
                    " as service_name, count(*) jml, sum(a.amount) tot from edc_log a left outer join service b\n" +
                    "on (a.service_id = b.service_id)\n" +
                    "where a.rc= '00'\n" +
                    dateFilter +
//                    "and a.amount is not null\n" +
//                    "and a.amount > 0\n" +
                    "and (lower(a.reversed) <> 't' or a.reversed is null)\n" +
                    "and a.service_id like '" + siLimit + "%'\n" +
                    excludeList +
                    "group by b.service_name;";
            writeDebugLog("RPT", "Query Data \n" + qLog);
            Cursor cLog = clientDB.rawQuery(qLog, null);
            if (cLog.moveToFirst()) {
                String cmp = "";
                int sq = 0;
                String ssq = String.valueOf(sq);
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"TRANSAKSI           COUNT         AMOUNT\",\n"
                        + "\"value\":\"TRANSAKSI           COUNT         AMOUNT\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                do {
                    if (!cmp.equals("")) {
                        cmp += ",";
                    }
                    String cval = String.valueOf(cLog.getInt(cLog.getColumnIndex("tot")));
                    String clab = cLog.getString(cLog.getColumnIndex("service_name"));
                    if (cval.matches("-?\\d+(\\.\\d+)?")) {
                        double d = Double.parseDouble(cval);
                        DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                        idrFormat.setDecimalSeparator(',');
                        DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
//                        if (!clab.startsWith("Pembayaran PLN") && !clab.startsWith("Pembayaran Non-Taglis")) {
//                            d = d/100;
//                        }
                        d = d/100;
                        cval = formatter.format(d);
                        cval = StringLib.strToCurr(String.valueOf(d), "Rp");
                    }

                    if (clab.startsWith("Transaksi ")) {
                        clab = clab.substring(10);
                    }
                    int ccount = cLog.getInt(cLog.getColumnIndex("jml"));
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"" + cval + "\",\n"
                        + "\"value\":\"" + cval + "\"}"
                        + "]},\"comp_lbl\":\"" + clab + " :"+ String.valueOf(ccount) +"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                } while (cLog.moveToNext());
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                    + "{\"comp_value\":["
                    + "{\"print\":\"----------------------------------------\",\n"
                    + "\"value\":\"----------------------------------------\"}"
                    + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                    + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                nGrand = StringLib.strToCurr(nGrand.replaceAll("[,.]",""),"Rp");
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                    + "{\"comp_value\":["
                    + "{\"print\":\"[B]"+nGrand+"\",\n"
                    + "\"value\":\"[B]"+nGrand+"\"}"
                    + "]},\"comp_lbl\":\"[B]GRAND TOTAL :" + jGrand + "\",\"comp_type\":\"1\","
                    + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                sq++;
                ssq = String.valueOf(sq);
                cmp += ",";
                cmp += "{\"visible\":true,\"comp_values\":"
                        + "{\"comp_value\":["
                        + "{\"print\":\"----------------------------------------\",\n"
                        + "\"value\":\"----------------------------------------\"}"
                        + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                        + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"}";
                if (cLog!=null) {
                    cLog.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1.5\",\"print\":\"1\",\"print_text\":\"RPT" + tgl
                        + "\",\"comps\":{\"comp\":["
                        + cmp
                        + "]},\"id\":\"RS00001\",\n" +
                        "\"type\":\"1\",\"title\":\"Summary Report\"}}");
            } else {
                if (cLog!=null) {
                    cLog.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        }
        //start mod report
        if (txElements[2].startsWith("A5B2")||
                txElements[2].startsWith("A662")||
                txElements[2].startsWith("A752")||
                txElements[2].startsWith("A2F2")||
                txElements[2].startsWith("A9B2")) {
            // Report Audit intercept
            String dateFilter = "";
            String tgl = "";
            if (txElements[2].endsWith("10")) {
                tgl = (String) rqContent.get("msg_dt");
                tgl = tgl.substring(4)+"-"+tgl.substring(2,4)+"-"+tgl.substring(0,2);
                dateFilter = "and date(a.rqtime)='" + tgl + "'\n";
            }
            if (txElements[3].endsWith("30")) {
                dateFilter = "and date(a.rqtime)='" + StringLib.getYYYYMMDD() + "'\n";
            }
            String excludeList = "";
            if (txElements[2].startsWith("A2F2")) {
//                excludeList = "and a.service_id not in ('A2C100', 'A2C200')\n";
                excludeList = "and (lower(a.settled) <> 't' or a.settled is null) \n" +
                        "and a.service_id not in ('A2A100','A29100','A23100'," +
                        "'A22000','A23000','A22100','A2B000','A2B100')\n";
            }
            if (txElements[2].startsWith("A6")) {
                excludeList = "and a.service_id not in ('A61000', 'A62000', 'A63000')\n";
            }
            if (txElements[2].startsWith("A7")) {
                excludeList = "and a.service_id not in ('A71001', 'A72000', 'A72001', 'A73000')\n";
            }
            if (txElements[2].startsWith("A5")) {
                excludeList = "and a.service_id not in ('A54911', 'A51410', 'A53100', 'A53211', 'A53221', 'A54921', 'A54931', 'A54941', \n" +
                        "'A54B11', 'A54A10', 'A54110', 'A54211', 'A54221', 'A54311', 'A54321', 'A54331', 'A54341', 'A54410', 'A5C210', 'A5C110', \n" +
                        "'A54431', 'A54433', 'A54441', 'A54443', 'A54451', 'A54453', 'A54461', 'A54510', \n" +
                        "'A54520', 'A54530', 'A54540', 'A54550', 'A54560', 'A57000', 'A57200', 'A57400', \n" +
                        "'A58000', 'A54421', 'A54423', 'A54C10', 'A54C20', 'A54C51', 'A54C52', 'A54C53', \n" +
                        "'A54C54', 'A52100', 'A52210', 'A52220', 'A52300', 'A54950', 'A54710', 'A54720', " +
                        "'A54800', 'A59000') \n";
            }
            if (txElements[2].startsWith("A9")) {
                excludeList = "and a.service_id not in ('A91000', 'A92000', 'A93000', 'A94000', 'A95000')\n";
            }
            String siLimit = txElements[2].substring(0,2);
            writeDebugLog("MSGLOG", "read (923)");
            writeDebugLog("EDCLOG", "read (924)");
            String qList = ""
                    + "select distinct a.rqtime,a.service_id,c.request_time,(case when b.service_name is null then a.service_id else"
                    + " b.service_name end) as service_name,substr(track2,1,16) cno, substr(track2,18,21) cexp,"
                    + "a.stan, a.rc, a.amount, c.response_message from edc_log a left outer join service b\n" +
                    "on (a.service_id = b.service_id) \n" +
                    "left outer join messagelog c \n" +
                    "on (a.stan = c.message_id and date(a.rqtime)=date(c.request_time) and a.service_id=c.service_id) \n" +
                    "where a.rc= '00'\n" +
                    "and c.print > 0\n" +
                    dateFilter + " \n" +
//                    "and a.amount is not null\n" +
//                    "and a.amount > 0\n" +
                    "and (lower(a.reversed) <> 't' or a.reversed is null)\n" +
                    "and a.service_id like '" + siLimit + "%'\n" +
                    excludeList;
            writeDebugLog("RPT", "Query Detail \n" + qList);
            List<PrintSize> data = new ArrayList<>();
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            data.add(new PrintSize(FontSize.TITLE, "Detail Report\n"));
            data.add(new PrintSize(FontSize.EMPTY, "\n"));
            Cursor cList = clientDB.rawQuery(qList, null);
            if (cList.moveToFirst()) {
                String cmp = "";
                int sq = 0;
                int dataCount = 0;
//                String clab = "No\tTanggal\tJam\tTransaksi";
//                String cval = "Kartu\tSTAN\tRC\tNominal";
                data.add(new PrintSize(FontSize.NORMAL, ""));
                data.add(new PrintSize(FontSize.NORMAL, "----------------------------------------\n"));
                String ssq = String.valueOf(sq);
                sq++;
                cmp += "{\"visible\":true,\"comp_values\":"
                    + "{\"comp_value\":["
                    + "{\"print\":\"----------------------------------------\",\n"
                    + "\"value\":\"----------------------------------------\"}"
                    + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                    + "\"comp_id\":\"R00002\",\"seq\":"+ssq+"}";
                do {
                    if (!cmp.equals("")) {
                        cmp += ",";
                    }
                    String cNo = ssq;
                    String cTgl = cList.getString(cList.getColumnIndex("rqtime")).substring(0,10);
                    String cJam = cList.getString(cList.getColumnIndex("rqtime")).substring(11);
                    if(cList.getString(cList.getColumnIndex("service_id")).equals("A26100")){
                        cJam = cList.getString(cList.getColumnIndex("request_time")).substring(11);
                    }
                    String cTx = cList.getString(cList.getColumnIndex("service_name"));
                    if (cTx.startsWith("Transaksi ")) {
                        cTx = cTx.substring(10);
                    }
                    String cCard = cList.getString(cList.getColumnIndex("cno"));
//                    String cExp = cList.getString(cList.getColumnIndex("cexp"));
//                    if (cExp!=null) {
//                        if (!cExp.equals("0000") && !cExp.equals("")) {
//                            cExp = cExp.substring(0,2) + "/" + cExp.substring(2,4);
//                        } else {
//                            cExp = "";
//                        }
//                    } else {
//                        cExp = "";
//                    }

                    String cExp = "";
                    if (cCard.length() != 16 && cCard.length() != 19){
                        cExp = cList.getString(cList.getColumnIndex("cexp"));
                        if (cExp!=null) {
                            if (!cExp.equals("0000") && !cExp.equals("")) {
                                cExp = cExp.substring(0,2) + "/" + cExp.substring(2,4);
                            } else {
                                cExp = "";
                            }
                        } else {
                            cExp = "";
                        }
                    }

                    String cStan = cList.getString(cList.getColumnIndex("stan"));
                    String cRc = cList.getString(cList.getColumnIndex("rc"));
                    String cAmo = String.valueOf(cList.getInt(cList.getColumnIndex("amount")));
                    String cSResp = cList.getString(cList.getColumnIndex("response_message"));
                    String cSRef = "000000000000";
                    String cAppr = "00000000";
                    if (cSResp!=null) {
                        if (!cSResp.equals("")) {
                            JSONObject respData = new JSONObject(cSResp);
                            if (respData.has("server_ref")) {
                                cSRef = respData.getString("server_ref");
                            }
                            if (respData.has("server_appr")) {
                                cAppr = respData.getString("server_appr");
                            }
                        }
                    }
                    if (cAmo.matches("-?\\d+(\\.\\d+)?")) {
                        double d = Double.parseDouble(cAmo);
                        DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
                        idrFormat.setDecimalSeparator(',');
                        DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
//                        if (cTx!=null) {
//                            if (!cTx.startsWith("Pembayaran PLN") && !cTx.startsWith("Pembayaran Non-")) {
//                                d = d / 100;
//                            }
//                        }
                        d = d/100;
                        cAmo = formatter.format(d);
                        cAmo = StringLib.strToCurr(String.valueOf(d), "Rp");
                    }
//                    clab = cNo+"\t"+cTgl+"\t"+cJam+"\t"+cTx;
//                    cval = cCard+"\t"+cStan+"\t"+cRc+"\t"+cAmo;
                    data.add(new PrintSize(FontSize.NORMAL, cTx + "|:"));
                    data.add(new PrintSize(FontSize.NORMAL,":|"+cAmo+"\n"));
                    data.add(new PrintSize(FontSize.NORMAL, cCard + "|:"));
                    data.add(new PrintSize(FontSize.NORMAL,":|"+cExp+"\n"));
                    data.add(new PrintSize(FontSize.NORMAL, " TRACE NO : "+cStan));
                    data.add(new PrintSize(FontSize.NORMAL, "\n"));
                    data.add(new PrintSize(FontSize.NORMAL, "TGL  : "+cTgl+"|:"));
                    data.add(new PrintSize(FontSize.NORMAL, ":|JAM  : "+cJam+"\n"));
                    data.add(new PrintSize(FontSize.NORMAL, "REF# : " + cSRef + "|:"));
                    data.add(new PrintSize(FontSize.NORMAL, ":|APPR : "+cAppr+"\n"));
                    data.add(new PrintSize(FontSize.NORMAL, ""));
                    data.add(new PrintSize(FontSize.NORMAL, "----------------------------------------\n"));
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\":|" + cAmo + "\",\n"
                            + "\"value\":\":|" + cAmo + "\"}"
                            + "]},\"comp_lbl\":\"" + cTx + "|:\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"},";
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\":|" + cExp + "\",\n"
                            + "\"value\":\":|" + cExp + "\"}"
                            + "]},\"comp_lbl\":\"" + cCard + "|:\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"},";
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\"\",\n"
                            + "\"value\":\"\"}"
                            + "]},\"comp_lbl\":\"STAN : "+cStan+"\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"},";
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\":|JAM  : " + cJam + "\",\n"
                            + "\"value\":\":|JAM  : " + cJam + "\"}"
                            + "]},\"comp_lbl\":\"TGL  : " + cTgl + "|:\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"},";
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\":|APPR : 000000  \",\n"
                            + "\"value\":\":|APPR : 000000  \"}"
                            + "]},\"comp_lbl\":\"REF# : " + cSRef + "|:\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00001\",\"seq\":"+ssq+"},";
                    ssq = String.valueOf(sq);
                    sq++;
                    cmp += "{\"visible\":true,\"comp_values\":"
                            + "{\"comp_value\":["
                            + "{\"print\":\"----------------------------------------\",\n"
                            + "\"value\":\"----------------------------------------\"}"
                            + "]},\"comp_lbl\":\"\",\"comp_type\":\"1\","
                            + "\"comp_id\":\"R00002\",\"seq\":"+ssq+"}";
                    dataCount++;
                } while (cList.moveToNext());
//                data.add(new PrintSize(FontSize.NORMAL, "START FOOTER"));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                data.add(new PrintSize(FontSize.NORMAL, "Informasi lebih lanjut, silahkan hubungi"));
//                data.add(new PrintSize(FontSize.NORMAL, "\n"));
//                data.add(new PrintSize(FontSize.NORMAL, "Call BRI di 14017, 021-500017,"));
//                data.add(new PrintSize(FontSize.NORMAL, "\n"));
//                data.add(new PrintSize(FontSize.NORMAL, "atau 021-57987400"));
//                data.add(new PrintSize(FontSize.NORMAL, "\n"));
//                data.add(new PrintSize(FontSize.EMPTY, "\n"));
//                data.add(new PrintSize(FontSize.NORMAL, "*** Terima Kasih ***"));
//                data.add(new PrintSize(FontSize.NORMAL, "\n"));
                setHasPrintData(true);
                setPrintData(data);
                setPrintText("RPD"+tgl);
                if (cList!=null) {
                    cList.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1.5\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Mencetak "+dataCount+" data transaksi\",\n" +
                        "\"value\":\"Mencetak "+dataCount+" data transaksi\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"RPTND0F\",\n" +
                        "\"type\":\"2\",\"title\":\"Detail Report\"}}");
//                return new JSONObject("{\"screen\":{\"ver\":\"1.5\",\"print\":\"1\",\"print_text\":\"RPD" + tgl
//                        + "\",\"comps\":{\"comp\":["
//                        + cmp
//                        + "]},\"id\":\"RD00001\",\n" +
//                        "\"type\":\"1\",\"title\":\"Detail Report\"}}");
            } else {
                if (cList!=null) {
                    cList.close();
                }
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak ditemukan\",\n" +
                        "\"value\":\"Data transaksi tidak ditemukan\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        }
        //end mod report
        String qServ = "select * from service "
                + "where service_id = '" + txElements[2] + "'";
        Cursor cServ = clientDB.rawQuery(qServ, null); //msgService
        /*
        String qTerm = "select * from terminal "
                + "where terminal_imei = '"+txElements[1]+"'";
        Cursor cTerm = clientDB.rawQuery(qTerm, null); //termList
                */
        String[] tmid = new String[2];// = getTerminalMerchantId(rqElements[1]);
        tmid[0] = "00000023";
        tmid[1] = "000001210000020";
        // update stan
        if (!predefined_stan) {
                    String getStanSeq = "select seq msgSequence from holder";
//            String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                    "from edc_log where date(rqtime) = date('now') and rc = '00' ";
            Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
            if (stanSeq != null) {
                stanSeq.moveToFirst();
                msgSequence = stanSeq.getInt(0);
            }
            stanSeq.close();
        }
        //check reversal stack
        String[] revData = EDCLog.getLastRevStatus();
        if (revData[0].equals("1")) {
            jmsg.put("msg_rc", "05");
            jmsg.put("msg_resp", "Memproses pending reversal, silahkan coba beberapa saat lagi");
            MenuListResolver mlr = new MenuListResolver();
            //Handle Reversal
            int elid = Integer.parseInt(revData[1]);
            Thread doReversal = new Thread(new handleReversal(context, revData[2], EDCLog, elid));
            doReversal.start();
            return mlr.loadMenu(context, "000000F", jmsg);
        }
        //end of check reversal stack disini pake if
        trace_no = generateStan();
        //create message logger
        writeDebugLog("MSGLOG", "read seq (1141)");
        String getLogId = "select max(log_id) nextseq from messagelog ";
        Cursor cLogId = clientDB.rawQuery(getLogId, null);
        if (cLogId.moveToFirst()) {
            logId = cLogId.getInt(cLogId.getColumnIndex("nextseq"));
            logId += 1;
        } else {
            logId = 1;
        }
        cLogId.close();

        writeDebugLog("MSGLOG", "insert (1153)");
        String iMsgLog = "insert or replace into messagelog "
                + "(message_id, service_id, terminal_id, request_time, log_id, "
                + "request_message) values ('" + trace_no + "', "
//                + "request_message) values ('" + txElements[0] + "', "
                + "'" + txElements[2] + "', "
                + "'" + txElements[1] + "', '" + StringLib.getSQLiteTimestamp()
                + "', " + String.valueOf(logId) + ", '"
                + content + "')";
        clientDB.execSQL(iMsgLog);
        //commit changes


        //get request data
        String reqData = getData(rqContent);

        //add respon kadaluarsa
        try {
            String reqDataTrack2 = rqContent.getString("msg_dt");
            if (reqDataTrack2.contains("=")){
                String validDate = reqDataTrack2.substring(reqDataTrack2.indexOf("=")+1, reqDataTrack2.indexOf("=")+7);
                Log.d("TAG reqDataTrack2", reqDataTrack2);
                Log.d("TAG validDate", validDate);
                SimpleDateFormat formatValidDate = new SimpleDateFormat("yyMMdd");
                Date dateValid = formatValidDate.parse(validDate);
                if (new Date().after(dateValid)) {
                    return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu telah kadaluarsa\",\n" +
                            "\"value\":\"Kartu telah kadaluarsa\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                            "\"type\":\"3\",\"title\":\"Gagal\"}}");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if (reqData.equals("") && !isLogon && !isNoInput && !isNoInput2 && isNoInput3 && isNoInput4) {
            return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Data transaksi tidak lengkap\",\n" +
                    "\"value\":\"Data transaksi tidak lengkap\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
        }

        //do process here
        //prepare query for service meta
        String qMeta = "select * from service_meta "
                + "where service_id = '" + serviceid + "' "
                + "and influx = 1 ";
        Cursor cMeta = clientDB.rawQuery(qMeta, null);
        //get query result
        int metaCount = cMeta.getCount();

        //handler for empty meta
        if (metaCount < 1) {
            Log.e("TX", "Service Meta request not found.");
        }
        //handler for unmatch field count
        String[] requestData = reqData.split("\\|");
        Log.wtf("req",reqData);
        Log.wtf("meta", String.valueOf(metaCount));
        //handle popup unk

            if (requestData.length != metaCount) {
//            if (txElements[2].startsWith("A53"))
                Log.e("TX", "Request field count does not matched");
                jmsg.put("msg_rc", "05");
                jmsg.put("msg_resp", "Jumlah data transaksi tidak sesuai");
                MenuListResolver mlr = new MenuListResolver();
                JSONObject replyJSON = mlr.loadMenu(context, "000000F", jmsg);
                return replyJSON;
            }

        // temp var for validation
        String[] serviceMeta = new String[metaCount];
        if (cMeta.moveToFirst()) {
            do {
                String metaIsoBit = cMeta.getString(cMeta.getColumnIndex("iso_bit_uid"));
                int seqMeta = cMeta.getInt(cMeta.getColumnIndex("seq"));
                String metaId = cMeta.getString(cMeta.getColumnIndex("meta_id"));
                if (metaIsoBit != null) {
                    if (metaIsoBit.equals("V")) {
                        serviceMeta[seqMeta] = "XV " + metaId;
                    } else {
                        serviceMeta[seqMeta] = metaId;
                    }
                } else {
                    serviceMeta[seqMeta] = metaId;
                }
            } while (cMeta.moveToNext());

        }

        //compose internal message
        String prevData = "";
        for (int i = 0; i < metaCount; i++) {
            if (serviceMeta[i].length() > 2) {
                if (serviceMeta[i].substring(0, 3).equals("XV ") && !requestData[i].equals(prevData)) {
                    Log.e("TX", "Cross validation failed.");
                    jmsg.put("msg_rc", "05");
                    jmsg.put("msg_resp", serviceMeta[i].substring(3) + " tidak sama dengan " + serviceMeta[i - 1]);
                    MenuListResolver mlr = new MenuListResolver();
                    JSONObject replyJSON = mlr.loadMenu(context, "000000F", jmsg);
                    return replyJSON;
                }
            }
            prevData = requestData[i];
            String iServiceData = "insert or replace into service_data (message_id, name, value) "
                    + "values ('" + jmsg.get("msg_id") + "', '" + serviceMeta[i] + "', '"
                    + requestData[i] + "')";
            clientDB.execSQL(iServiceData);
        }
        //tosend
        String toParse = jmsg.get("msg_id") + "|" + txElements[3] + "|" + txElements[4];
        if (Arrays.asList(serviceMeta).contains("id")) {
            writeDebugLog("COPY", "Service Data from : " + requestData[Arrays.asList(serviceMeta).indexOf("id")] + "to" + jmsg.get("msg_id"));
            String uPrev = "insert into service_data (message_id, name, value) "
                    + "select '" + jmsg.get("msg_id") + "', name, value from service_data "
                    + "where message_id = '" + requestData[Arrays.asList(serviceMeta).indexOf("id")] + "' "
                    + "and name not in (select name from service_data "
                    + "where message_id = '" + jmsg.get("msg_id") + "')";
            writeDebugLog("with", uPrev);
            clientDB.execSQL(uPrev);
        }
        //send to parser
        cServ.moveToFirst();
        boolean directReply = cServ.getString(cServ.getColumnIndex("is_to_core")).equals("f");
        JSONObject replyJSON = null;
        if (directReply) {
            String screenResponse = cServ.getString(cServ.getColumnIndex("screen_response"));
            replyJSON = new JSONObject();
            replyJSON.put("messageId",jmsg.getString("msg_id"));
            for (int r = 0; r < metaCount; r++) {
                String currentValue = requestData[r];
                if ((currentValue.matches("-?\\d+(\\.\\d+)?"))
                        &&(serviceMeta[r].startsWith("nom")
                        ||serviceMeta[r].startsWith("sal")
                        ||serviceMeta[r].startsWith("amo"))) {
                    currentValue = currentValue + "00";
                }
                replyJSON.put(serviceMeta[r],currentValue);
            }
            MenuListResolver mlr = new MenuListResolver();
            jroot = mlr.loadMenu(context, screenResponse, replyJSON);
            return jroot;
        }

        ISO8583Parser rqParser = new ISO8583Parser(context, "6000070000", serviceid, toParse, 1, trace_no);
        if (!serviceid.equals("A2C200")) {
//            String uStanSeq = "update holder set "
//                    + "seq = " + msgSequence;
//            writeDebugLog("UPDATING", "HOLDER (1255)");
//            writeDebugLog("By ", serviceid);
//            clientDB.execSQL(uStanSeq);
        }
        if (serviceid.equals("A54322")||serviceid.equals("A54312")||serviceid.equals("A54331")) {
            EDCLog.setIgnoreReplyAmount(true);
        }
//        elogid = EDCLog.writePreLog(
//                rqParser.getIsoBitValue(),
//                serviceid,
//                (String) jmsg.get("msg_id"));
        byte[] toHost = rqParser.parseJSON();

        // send to host
//        byte[] fromHost = sendMessage(context, toHost);
        int cLen = toHost.length;
        byte[] hLen = Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(cLen).array(), 2, 4);
        byte[] formattedContent = ByteBuffer.allocate(2 + cLen).put(hLen).put(toHost).array();
        hsToHost = ISO8583Parser.bytesToHex(formattedContent);
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
        int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
        AsyncMessageWrapper amw = new AsyncMessageWrapper(host_ip, host_port, hsToHost);
        byte[] fromHost = sendMessage(amw);
        //byte[] fromHost = null;

        Boolean txState = false;
        boolean reversable = rqParser.isReversable();

        /*if ((serviceid == "A27100") || (serviceid == "A92001") || (serviceid == "A93001") || (serviceid == "A94001")){
            reversable = true;
        }*/
        if (serviceid.equals("A27100")){
            reversable = true;
        }

        if (serviceid.equals("A25100") ||
                serviceid.equals("A92001") ||
                serviceid.equals("A93001") ||
                serviceid.equals("A94001") ||
                serviceid.equals("A5C220") ||
                serviceid.equals("A5C230")){
            reversable = true;
        }

        if (fromHost == null && !isBrizziVoid) {
            jmsg.put("msg_rc", "05");
            String reversalInfo = "";
            if (reversable) {
                reversalInfo = "\nMengirim reversal";
            }
            jmsg.put("msg_resp", "Tidak dapat terhubung ke server" + reversalInfo);
            MenuListResolver mlr = new MenuListResolver();
            replyJSON = mlr.loadMenu(context, "000000F", jmsg);
            //Handle Reversal
            if (reversable) {
                Thread doReversal = new Thread(new handleReversal(context, hsToHost, EDCLog, elogid));
                try {
                    doReversal.start();
                } catch (Exception er) {
                    //pass cannot send reversal due invalid data
                }
            }
            return replyJSON;
        } else {
            if (fromHost==busyResponse || ISO8583Parser.bytesToHex(fromHost).equals("FF")) {
                jmsg.put("msg_rc", "05");
                jmsg.put("msg_resp", "Koneksi sedang sibuk, coba beberapa saat lagi");
                MenuListResolver mlr = new MenuListResolver();
                replyJSON = mlr.loadMenu(context, "000000F", jmsg);
                return replyJSON;
            }
            ISO8583Parser rpParser = null;
            try {
                rpParser = new ISO8583Parser(context, "6000070000", ISO8583Parser.bytesToHex(fromHost), 2);
            } catch (Exception pe) {
                jmsg.put("msg_rc", "05");
                jmsg.put("msg_resp", "Invalid server response");
                MenuListResolver mlr = new MenuListResolver();
                replyJSON = mlr.loadMenu(context, "000000F", jmsg);
                return replyJSON;
            }
            try{
//                EDCLog.writePostLog(rpParser.getIsoBitValue(), elogid);
            }catch(Exception e){

            }
            String[] replyValues = rpParser.getIsoBitValue();
            String mid = replyValues[11];
            String msg_rc = "";
            txState = true;
            if (replyValues[39] != null) {
                msg_rc = replyValues[39];
                if (!((msg_rc.equals("00"))||(msg_rc.equals("02"))||(msg_rc.equals("68")))) {
                    txState = false;
//                    String uStanSeq = "update holder set "
//                            + "seq = " + msgSequence;
//                    clientDB.execSQL(uStanSeq);
                }
            }
            // Fixed Summary Pembelian Bansos
            if (serviceid.equals("A92001")) {
                    replyValues[4] = "00" + replyValues[4].substring(0,10);
                }

            rpParser.setServiceId(serviceid);
            rpParser.setMessageId((String) jmsg.get("msg_id"));
            rpParser.setResponseCode(msg_rc);
            replyJSON = rpParser.parseISO();
        }
        //mark
        if (isBrizziVoid) {
            writeDebugLog("EDCLOG", "update void (1340)");
            String q = "update edc_log set rran = 'o' where log_id = '" + stanvoid + "';";
            clientDB.execSQL(q);
        }
        //store orimessage on reversal stack as storage
        if (serviceid.equals("A25100")||
                serviceid.equals("A27100")||
                serviceid.equals("A29200")||
                serviceid.equals("A2A200")) {
            String saveStack = "insert or replace into reversal_stack("
                    + "elogid, orimessage, revstatus) values "
                    + "(" + String.valueOf(elogid)
                    + ",'" + hsToHost + "',"
                    + "'B')";
            clientDB.execSQL(saveStack);
            jroot.put("elogid", elogid);
        }
        MenuListResolver mlr = new MenuListResolver();
        String msgStatus = "";
        Log.d("MASUK KE MSG_RC", "MASUK DILUAR DILUAR IF");
        if (replyJSON.has("msg_rc")
                &&!(serviceid.equals("A54322")&&(txState))
                &&!(serviceid.equals("A56000")&&(txState))
                ) {
            //jroot.put("msg", jmsg);
            Log.d("MASUK KE MSG_RC", "MASUK DILUAR IF");
            if (((String) replyJSON.get("msg_rc")).equals("00") || ((String) replyJSON.get("msg_rc")).equals("CP") || ((String) replyJSON.get("msg_rc")).equals("TL")) {
                Log.d("MASUK KE MSG_RC", "MASUK IF");
                jroot = mlr.loadMenu(context, "000000D", replyJSON);
//                String array[] = {"A54911", "A51410", "A53100", "A53211", "A53221", "A54921", "A54931",
//                        "A54941", "A54B11", "A54A10", "A54110", "A54211", "A54221", "A54311", "A54321", "A54410",
//                        "A54431", "A54433", "A54441", "A54443", "A54451", "A54453", "A54461", "A54510",
//                        "A54520", "A54530", "A54540", "A54550", "A54560", "A57000", "A57200", "A57400",
//                        "A58000", "A54421", "A54423", "A54C10", "A54C20", "A54C51", "A54C52", "A54C53",
//                        "A54C54", "A52100", "A52210", "A52220", "A52300", "A54950", "A54710", "A54720",
//                        "A54800", "A59000", "A54331", "A71001", "A72000", "A72001", "A73000", "A61000",
//                        "A62000", "A63000", "A2A100","A29100","A23100", "A22000","A23000","A22100","A2B000","A2B100"};
                String array[] = {"A54911", "A51410", "A53100", "A53211", "A53221", "A54921", "A54931",
                        "A54941", "A54B11", "A54A10", "A54110", "A54211", "A54221", "A54311", "A54321", "A54331", "A54341",
                        "A54410", "A54431", "A54433", "A54441", "A54443", "A54451", "A54453", "A54461",
                        "A54510", "A54520", "A54530", "A54540", "A54550", "A54560", "A57000", "A57200",
                        "A57400", "A58000", "A54421", "A54423", "A54C10", "A54C20", "A54C51", "A54C52",
                        "A54C53", "A54C54", "A52220", "A52300", "A54950", "A54710",
                        "A54720", "A54800", "A59000",

                        "A71001", "A72000", "A72001", "A73000",

                        "A61000", "A62000", "A63000", "A52100", "A5C210",

                        "A2C200",

                        "A21100", "A22000", "A22100", "A23000", "A23100",
                        "A29100", "A2A100", "A2B000", "A2B100", "A2D100",
                        "A91000", "A92000", "A93000", "A94000"};

//                 "A52100", "A52210",

//                boolean matched_array = false;
                for(int i=0; i < array.length; i++){
                    if(serviceid.equals(array[i])){
                        matched_array = true;
                        i = array.length;
                    }
                }
                if (!matched_array){
                    String getStanSeq = "select seq msgSequence from holder";

//                        String getStanSeq = "select cast(max(stan) as number) as msgSequence " +
//                                "from edc_log where date(rqtime) = date('now') and rc = '00' ";
                    Cursor stanSeq = clientDB.rawQuery(getStanSeq, null);
                    if (stanSeq != null) {
                        stanSeq.moveToFirst();
                        msgSequence = stanSeq.getInt(0);
                    }

                    stanSeq.close();
                    String trace = generateStan();
                    String uStanSeq = "update holder set "
                            + "seq = " + trace;
                    writeDebugLog("UPDATING", "HOLDER (1473)");
                    clientDB.execSQL(uStanSeq);

                }
            } else {
                jroot = mlr.loadMenu(context, "000000F", replyJSON);
            }
            msgStatus = (String) jmsg.get("msg_rc");
        } else {
            Iterator replyKeys = replyJSON.keys();
            while (replyKeys.hasNext()) {
                String k = (String) replyKeys.next();
                String uData = "insert or replace into service_data(message_id, name, value) "
                        + "values ('" + replyJSON.get("messageId") + "', '" + k + "', '"
                        + replyJSON.get(k) + "')";
                clientDB.execSQL(uData);
            }
            cServ.moveToFirst();
            String screenResponse = cServ.getString(cServ.getColumnIndex("screen_response"));
            String updReplyData = "select * from service_data "
                    + "where message_id = '" + replyJSON.get("messageId") + "' ";
            if (replyJSON.has("id")) {
                updReplyData += "or message_id = '" + replyJSON.get("id") + "' ";
            }
            Cursor cRD = clientDB.rawQuery(updReplyData, null);
            if (cRD.moveToFirst()) {
                do {
                    replyJSON.put(cRD.getString(cRD.getColumnIndex("name")),
                            cRD.getString(cRD.getColumnIndex("value")));
                } while (cRD.moveToNext());
            }
            cRD.close();
            writeDebugLog("RESP_DATA", replyJSON.toString());
//            if (serviceid.equals("A54312")) {
//                String tunggakan = ((String) replyJSON.get("tunggakan")).trim();
//                if (tunggakan.matches("-?\\d+(\\.\\d+)?")) {
//                    int tgk = Integer.parseInt(tunggakan);
//                    if (tgk<1) {
//                        jroot = mlr.loadMenu(context, "543120E", replyJSON);
//                    } else {
//                        jroot = mlr.loadMenu(context, screenResponse, replyJSON);
//                    }
//                } else {
//                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
//                }
//            } else
            if (serviceid.equals("A54322")) {
                String txrc = "00";
                if (replyJSON.has("msg_rc")) {
                    txrc = (String) replyJSON.get("msg_rc");
                }
                if (txrc.equals("02")) {
                    jroot = mlr.loadMenu(context, "543220E", replyJSON);
                } else if (txrc.equals("68")) {
                    jroot = mlr.loadMenu(context, "543220E", replyJSON);
                } else {
                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
                }
            } else if (serviceid.equals("A56000")) {
                String txrc = "00";
                if (replyJSON.has("msg_rc")) {
                    txrc = (String) replyJSON.get("msg_rc");
                }
                if (txrc.equals("68")) {
                    jroot = mlr.loadMenu(context, "560000E", replyJSON);
                } else {
                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
                }
            } else if (serviceid.equals("A56100")) {
                String txrc = (String) replyJSON.get("tx_rc");
                if (txrc.equals("68")) {
                    jroot = mlr.loadMenu(context, "561000E", replyJSON);
                } else {
                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
                }
            } else if (serviceid.equals("A54A10")) {
                String pay_mode = (String) replyJSON.get("pay_stat");
                if (pay_mode.equals("Y")) {
                    jroot = mlr.loadMenu(context, "54A111F", replyJSON);
                } else {
                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
                }
//            } else if (serviceid.equals("A58100")) {
//                String periode = (String) replyJSON.get("periode");
//                String pBulan = periode.substring(0,2);
//                String pTahun = periode.substring(2);
//                periode = pBulan + "/" + pTahun.substring(2);
//                replyJSON.put("periode", periode);
//            } else if (serviceid.equals("A5C210")) {
//
//                String pay_mode = (String) replyJSON.get("pay_stat");
//                if (pay_mode.equals("Y")) {
//                    jroot = mlr.loadMenu(context, "5C2100F", replyJSON);
//                } else {
//                    jroot = mlr.loadMenu(context, screenResponse, replyJSON);
//                }
            } else {
                if (isLogon) {
                    //save key here
                    try {
                        PINPadInterface.open();
                        String wk = (String) replyJSON.getString("work_key");
                        //override wk
//                        wk = "376EB729FB11373BC0F097ECE49F6A25";
                        if (wk.length()==16) {
                            wk = wk+wk;
                        }
                        writeDebugLog("LOGON", wk);
                        byte[] newKey = ISO8583Parser.hexStringToByteArray(wk);
                        int ret = PINPadInterface.updateUserKey(0,0, newKey, newKey.length);
                        writeDebugLog("LOGON", "Status : "+String.valueOf(ret));
                        cekstatus.edit().putBoolean("lastkeychanged", true).apply();
                        String trace = generateStan();
//                        String uStanSeq = "update holder set "+"seq= "+trace;
//                        writeDebugLog("UPDATING", "HOLDER(1473)");
//                        clientDB.execSQL(uStanSeq);
//                        clientDB.close();
                        helperDb.close();
                    } catch (Exception e) {
                        //teu bisa update
                        Log.e("LOGON", e.getMessage());
                    } finally {
                        PINPadInterface.close();
//                        myServiceBinder.clientConnect();
                    }

                    return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Logon Succesfull\",\n" +
                            "\"value\":\"Logon Succesfull\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"F0003\",\"seq\":0}]},\"id\":\"F000002\",\n" +
                            "\"type\":\"2\",\"title\":\"Sukses\"}}");
                }
// Nambah stan info depo & saldo
//                if(screenResponse.equals("231000F")){
//                    String updInv = "update holder set seq = case when seq = 999999 then 0 else seq + 1 end ";
//                    clientDB.execSQL(updInv);
//                }

                if(screenResponse.equals("920000F")
                        || screenResponse.equals("921000F")
                        || screenResponse.equals("930000F")
                        || screenResponse.equals("931000F")){
                    String updInv = "update holder set invnum = case when invnum = 999999 then 0 else invnum + 1 end ";
                    clientDB.execSQL(updInv);
                }

                jroot = mlr.loadMenu(context, screenResponse, replyJSON);

                if (jroot.has("to_be_id") && jroot.get("to_be_id").equals("5C2100F")){
                    jroot = mlr.loadMenu(context, "5C2100F", replyJSON);
                }
            }
            if (replyJSON.has("server_ref")) {
                jroot.put("server_ref",replyJSON.get("server_ref"));
            }
            if (replyJSON.has("server_appr")) {
                jroot.put("server_appr",replyJSON.get("server_appr"));
            }
            if (replyJSON.has("server_time")) {
                jroot.put("server_time",replyJSON.get("server_time"));
            }
            if (replyJSON.has("server_date")) {
                jroot.put("server_date",replyJSON.get("server_date"));
            }
            if (serviceid.equals("A25100")||
                    serviceid.equals("A27100")||
                    serviceid.equals("A29200")||
                    serviceid.equals("A2A200")) {
                jroot.put("logid", logId);
            }
            String updRqTime = "";
            String updElogtime = "";
            if (replyJSON.has("server_time")&&replyJSON.has("server_date")) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                String repDate = sdf.format(d) + "-";
                String tmStamp = null;
                repDate = repDate + replyJSON.getString("server_date").substring(0,2) + "-" +
                        replyJSON.getString("server_date").substring(2,4);
                tmStamp = StringLib.toSQLiteTimestamp(repDate, replyJSON.getString("server_time"));
                updRqTime = ", request_time = '"+ tmStamp + "' ";
                updElogtime = " rqtime = '"+ tmStamp + "' ";
            }
            msgStatus = "00";
            int prcount = getPrintFromScreen(jroot);
            writeDebugLog("MSGLOG", "update (1475)");
            String uMsgLog = "update messagelog set "
                    + "message_status = '" + msgStatus + "', "
                    + "reply_time = time('now'), "
                    + "response_message = '" + jroot.toString().replaceAll("'","''") + "', "
                    + "print = " + String.valueOf(prcount) + " "
                    + updRqTime
                    + "where log_id = " + logId;
            clientDB.execSQL(uMsgLog);

                try {
                    writeDebugLog("EDCLOGUPDATE", "update (1490)");
                    String carilogid = "select log_id from edc_log where service_id = '"+serviceid+"' order by log_id desc limit 1";
                    Cursor carilog = clientDB.rawQuery(carilogid, null);
                    int edclogid;
                    if (carilog != null) {
                        carilog.moveToFirst();
                        edclogid = carilog.getInt(0);

                        String updateelog = "update edc_log set "
                                + updElogtime
                                + "where log_id = " + edclogid;
                        clientDB.execSQL(updateelog);
                    }
                    carilog.close();
                }catch (Exception e){

                }
        }
        writeDebugLog("JSON_RETURN", jroot.toString());
        clientDB.close();
        helperDb.close();
        return jroot;
    }

    public String reverseLastTransaction(Context context) {
        writeDebugLog("REVERSAL", hsToHost);
        Thread doReversal = new Thread(new handleReversal(context, hsToHost, EDCLog, elogid));
        doReversal.start();
        //fix update status data summary after reversal
        if (serviceid.equals("A25100")){
            writeDebugLog("EDCLOG", "update status after reversal");
            try {
                String stan = String.format("%06d", msgSequence);
                Log.d("LOG STAN", String.format("%06d", msgSequence));
                helperDb.openDataBase();
                clientDB = helperDb.getActiveDatabase();
                String updDB = "update edc_log set settled = 't' where stan = '"+stan+"';";
//                String updDB = "delete from edc_log where stan = '"+stan+"';";
                clientDB.execSQL(updDB);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        clientDB.close();
        return "ReversalRQ";
    }

    public long insertIntoAidLog(ContentValues contentValues){
        helperDb = new DataBaseHelper(ctx);
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getWritableDatabase();
            return clientDB.insert("brizzi_aid_log", null, contentValues);
        } catch (Exception ex) {
            Log.e("TX", "DB error "+ex.toString());
            return -1;
        }finally {
            clientDB.close();
            helperDb.close();
        }
    }

    public long updateAidById(ContentValues contentValues,long id){
        helperDb = new DataBaseHelper(ctx);
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getWritableDatabase();
            return clientDB.update("brizzi_aid_log", contentValues, "id=" + id, null);
        } catch (Exception ex) {
            Log.e("TX", "DB error");
            return -1;
        }finally {
            clientDB.close();
            helperDb.close();
        }

    }

    public long insertIntoCmdLog(ContentValues contentValues){
        helperDb = new DataBaseHelper(ctx);
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getWritableDatabase();
            return clientDB.insert("brizzi_cmd_log", null, contentValues);
        } catch (Exception ex) {
            Log.e("CMD_LOG", "DB error "+ex.toString());
            return -1;
        }finally {
            clientDB.close();
            helperDb.close();
        }

    }

    private String getIMEI(JSONObject request) throws JSONException {
        String imei = "";
        if (request.has("msg_ui")) {
            imei = (String) request.get("msg_ui");
        } else {
            Log.e("TX", "Parse Request Error : Request contains no client IMEI");
        }
        return imei;
    }

    private String getSTAN(JSONObject request) throws JSONException {
        String stan = "";
        if (request.has("msg_id")) {
            stan = (String) request.get("msg_id");
        } else {
            Log.e("TX", "Parse Request Error : Request contains no message ID");
        }
        return stan;
    }

    private String getData(JSONObject request) throws JSONException {
        String body = "";
        if (request.has("msg_dt")) {
            body = (String) request.get("msg_dt");
        } else {
            Log.e("TX", "Parse Request Error : Request contains no message data");
        }
        return body;
    }

    private String getServiceId(JSONObject request) throws JSONException {
        String serviceId = "";
        if (request.has("msg_si")) {
            serviceId = (String) request.get("msg_si");
        } else {
            Log.e("TX", "Parse Request Error : Request contains no service ID");
        }
        return serviceId;
    }

    private String[] getTransactionElements(JSONObject request) throws JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String[] rqElements = new String[5];
        rqElements[0] = getSTAN(request);
        rqElements[1] = getIMEI(request);
        rqElements[2] = getServiceId(request);
        String[] tmid = new String[2];// = getTerminalMerchantId(rqElements[1]);
        tmid[0] = preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID);//"00000006";
        tmid[1] = preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID);//"000001210000020";
        rqElements[3] = tmid[0];
        rqElements[4] = tmid[1];
        return rqElements;
    }

    public String generateStan() {
        msgSequence++;
        if (msgSequence > 999999) {
            msgSequence = 0;
        }
        return String.format("%06d", msgSequence);
    }

    private int getPrintFromScreen(JSONObject msg) throws JSONException {
        int print;
        JSONObject screen;
        if (msg.has("screen")) {
            screen = msg.getJSONObject("screen");
        } else {
            screen = msg;
        }
        if (screen.has("print")) {
            print = screen.getInt("print");
        } else {
            print = 0;
        }
        return print;
    }

    public byte[] sendMessage(final AsyncMessageWrapper pMessage) {
        int timeout = 30000;
        return sendMessage(pMessage, timeout);
    }

    public byte[] sendMessage(final AsyncMessageWrapper pMessage, int timeout) {
        if (socket_status==IN_USE) {
            return busyResponse;
        }
        Log.i("CONN STS DONE", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        socket_status = IN_USE;
        final InetSocketAddress tGatewaySocketAddress = pMessage.getDestination();
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
        int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
        String tRequestStream = pMessage.getMessageStream();
        Log.i("GET MSG DONE", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        byte[] respBytes = new byte[0];
        Log.i("CR8SOC START", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        Socket tGatewaySocket = new Socket();
        Log.i("CR8SOC DONE", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        byte[] message = ISO8583Parser.hexStringToByteArray(tRequestStream);
//        logTrace("Connecting to " + tGatewaySocketAddress.getHostName() + ":" + tGatewaySocketAddress.getPort());
        logTrace("Connecting to " + host_ip + ":" + host_port);
        Log.i("CONN AT", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(host_ip, host_port);

        try {
            tGatewaySocket.setSoTimeout(timeout);
//            tGatewaySocket.connect(tGatewaySocketAddress, timeout);
            tGatewaySocket.connect(inetSocketAddress, timeout);

        } catch (IOException ex) {
            logTrace("IOException on SendMessage() when connecting to Gateway at " + tGatewaySocketAddress);
            socket_status = IDLE;
            return null;
        }

//        logTrace("Connected to " + tGatewaySocketAddress.getHostName() + ":" + tGatewaySocketAddress.getPort());
        logTrace("Connected to " + host_ip + ":" + host_port);
        ByteArrayOutputStream tRequestByteStream = new ByteArrayOutputStream();

        try {

//            tRequestByteStream.write(cEndMessageByte);
            tRequestByteStream.write(message);
        } catch (IOException ex) {
            logTrace("IOException on SendMessage() when writing request stream " + tRequestStream + " to byte array output stream.");
            socket_status = IDLE;
            return null;
        }

        logTrace("Request : [" + tRequestStream + "]");

        try {
            tGatewaySocket.getOutputStream().write(message);
        } catch (IOException ex) {
            logTrace("IOException on SendMessage() when writing stream " + tRequestStream + " + to outgoing socket at " + tGatewaySocketAddress);
            socket_status = IDLE;
            return null;
        }
        try {
            MyWrapper pbi = new MyWrapper(tGatewaySocket.getInputStream());
            DataInputStream reader = new DataInputStream(pbi);

            int available = reader.available();
            int tMessageByte = -1;
            respBytes = new byte[available];
            for (int i = 0; i < available; i++) {
                tMessageByte = reader.read();
                respBytes[i] = (byte) tMessageByte;
            }

            String s = new String(respBytes);
            s = ISO8583Parser.bytesToHex(respBytes);
            logTrace("Response : [" + s + "]");
            reader.close();
        } catch (IOException ex) {
            System.err.println(ex);
            socket_status = IDLE;
            return null;
        }
//        logTrace("Disconnected from " + tGatewaySocketAddress.getHostName() + ":" + tGatewaySocketAddress.getPort());
        logTrace("Disconnected from " + host_ip + ":" + host_port);
        socket_status = IDLE;
        return respBytes;
    }

    private JSONObject doInitBrizzi(Context context) throws JSONException {
        boolean openDevice = false;
        try {
            smc = new NeoSmartCardController(context);
            openDevice = smc.starting(1);
            writeDebugLog("SAM", String.valueOf(openDevice));
            if (!openDevice) {
                throw new Exception("No SAM Card");
            }
            //2. Card � Select AID 1
            String aid = smc.sendCmd(ISO8583Parser.hexStringToByteArray("00A4040C09A00000000000000011"));
//            String aid = smc.sendCmd(ISO8583Parser.hexStringToByteArray("0084000008"));
            if (aid.startsWith("9000")) {
                smc.closedevice();
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Proses Init Sukses\",\n" +
                        "\"value\":\"Proses Init Sukses\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"I00001\",\"seq\":0}]},\"id\":\"000000E\",\n" +
                        "\"type\":\"2\",\"title\":\"Sukses\"}}");
            } else {
                smc.closedevice();
                return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Proses Init Gagal\",\n" +
                        "\"value\":\"Proses Init Gagal\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"I00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                        "\"type\":\"3\",\"title\":\"Gagal\"}}");
            }
        } catch (Exception e) {
            if (openDevice) {
                smc.closedevice();
            }
            return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Proses Init Gagal\",\n" +
                    "\"value\":\"Proses Init Gagal\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"I00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
        }
    }


    class MyWrapper extends PushbackInputStream {

        MyWrapper(InputStream in) {
            super(in);
        }

        @Override
        public int available() throws IOException {
            int b = super.read();
            super.unread(b);
            return super.available();
        }
    }

    class handleReversal implements Runnable {
        private Context context;
        private String oriMsg;
        private LogHandler EDCLog;
        private int elogid;

        public handleReversal(Context context, String oriMsg, LogHandler EDCLog, int elogid) {
            this.context = context;
            this.oriMsg = oriMsg;
            this.EDCLog = EDCLog;
            this.elogid = elogid;
        }

        @Override
        public void run() {
            sendReversal();
        }

        private String reversalMessage(String originalMessage) {
            String isoBitlength = "0000";
            String isoHeader = "6000070000";
            String isoMti = "0400";
            return originalMessage.substring(0, isoBitlength.length()+isoHeader.length())
                    + isoMti + originalMessage.substring(isoBitlength.length()
                    +isoHeader.length()+isoMti.length());
        }

        private void sendReversal() {
            String revMsgToHost = reversalMessage(oriMsg);
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            String host_ip = preferences.getString("ip", CommonConfig.DEV_SOCKET_IP);
            int host_port = Integer.valueOf(preferences.getString("port", CommonConfig.DEV_SOCKET_PORT));
            AsyncMessageWrapper amw = new AsyncMessageWrapper(host_ip, host_port, revMsgToHost);
            byte[] revResponse = sendMessage(amw);
            if (revResponse == null) {
                //reversal no response
                EDCLog.writeRevResponse("PE", oriMsg, elogid);
                return;
            } else {
                if (revResponse==busyResponse) {
                    return;
                }
                ISO8583Parser rpParser = new ISO8583Parser(context, "6000070000", ISO8583Parser.bytesToHex(revResponse), 2);
//                EDCLog.writePostLog(rpParser.getIsoBitValue(), elogid);
                String[] replyValues = rpParser.getIsoBitValue();
                if (replyValues[39] != null) {
                    EDCLog.writeRevResponse(replyValues[39], oriMsg, elogid);
                    return;
                }
                EDCLog.writeRevResponse("PE", oriMsg, elogid);
            }
        }
    }

    public List<PrintSize> getPrintData() {
        return printData;
    }

    public boolean isHasPrintData() {
        return hasPrintData;
    }

    public void setPrintData(List<PrintSize> printData) {
        this.printData = printData;
    }

    public void setHasPrintData(boolean hasPrintData) {
        this.hasPrintData = hasPrintData;
    }

    public void setPrintText(String printText) {
        this.printText = printText;
    }

    public String getPrintText() {
        return printText;
    }

    public void writeDebugLog(String category, String msg) {
        if (DEBUG_LOG) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            Log.d("DEBUG", "[" + sdf.format(d) + "] " + category + " - " + msg);
        }
    }
}

