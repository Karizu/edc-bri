/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.handler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import id.co.bri.brizzi.common.StringLib;

/**
 * @author Ahmad
 */
public class ISO8583Parser {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final int Request = 1;
    private final int Response = 2;
    private final int LLVAR = 99;
    private final int LLLVAR = 999;
    private final int ValueFromParam = 1;
    private final int ValueFromService = 2;
    private final int ValueFromDefinition = 3;
    private Collection BitDef;
    private Cursor bitDefCursor;
    private Collection serviceMeta;
    private Cursor serviceMetaCursor;
    private String serviceId;
    private String messageId;
    private String message;
    private int msgSequence = 0;
    private int ParseMode;
    private String mti;
    private String stan;
    private String header = "";
    private String[] IsoBitValue;
    private String[] IsoBitValueReply;
    private String responseCode = "";
    private Context context;
    private DataBaseHelper helperDb;
    private String serviceName;
    private boolean reversable = false;
    private String currentLength = "";

    public ISO8583Parser() {
    }

    public ISO8583Parser(Context ctx, String header, String serviceId, String message, int ParseMode, String stan) {
        if (header != null) {
            this.header = header;
        }
        this.serviceId = serviceId;
        this.message = message;
        this.ParseMode = ParseMode;
        this.stan = stan;
        this.context = ctx;
        this.helperDb = new DataBaseHelper(ctx);

        updateBitDef();
        updateServiceMeta();
        setMti();
    }

    public ISO8583Parser(Context ctx, String header, String message, int ParseMode) {
        if (header != null) {
            this.header = header;
        }
        this.context = ctx;
        this.helperDb = new DataBaseHelper(ctx);
        this.message = message;
        this.ParseMode = ParseMode;
        int MSGLENGTH = message.length();
        int LENMSGLENGTH = 4;
        int LENMSGHEADER = header.length();
        int LENMTI = 4;
        int LENBITMAP = 16;
        //prepare tmp flow message
        String restMessage = message;
//        String restMessage = "007A609000000602102038010002810002808201000004153918102900063030323630373736333400603139313032393133353633353830383030303030303030303031303030303030303330303630313335303031333038313332383138354136353832380020303033454D4F4E45592020202020202020202020";
        Log.d("MSG", restMessage);
        try {
            //pop length
            String msgLen = restMessage.substring(0, LENMSGLENGTH);
            restMessage = restMessage.substring(LENMSGLENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //pop header
            String msgHeader = restMessage.substring(0, LENMSGHEADER);
            restMessage = restMessage.substring(LENMSGHEADER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //pop mti
            this.mti = restMessage.substring(0, LENMTI);
            restMessage = restMessage.substring(LENMTI);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String msgBitmap = null;

        try {
            //pop primary bitmap
            msgBitmap = restMessage.substring(0, LENBITMAP);
            restMessage = restMessage.substring(LENBITMAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<Integer> bitList = readBitmap(msgBitmap, false);
            if (bitList.contains(1)) {
                //if has extended, pop extended bitmap
                String bmpExtended = restMessage.substring(0, LENBITMAP);
                restMessage = restMessage.substring(LENBITMAP);
                bitList.addAll(readBitmap(bmpExtended, true));
            }

            updateValueFromBitList(bitList, restMessage);
            this.stan = IsoBitValue[11];
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public static byte[] hexStringToByteArray(String s) {
        if ((s.length() % 2) != 0) {
            s = s + "F";
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setParseMode(int ParseMode) {
        this.ParseMode = ParseMode;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String[] getIsoBitValue() {
        return IsoBitValue;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public boolean isReversable() {
        return reversable;
    }

    private Boolean checkParam() {
        if (serviceId == null) {
            Log.i("ISO8583P", "Service ID not sets, parsing canceled");
            return false;
        }
        if (message == null) {
            Log.i("ISO8583P", "Source message not sets, parsing canceled");
            return false;
        }
        if (!(ParseMode == Request | ParseMode == Response)) {
            Log.i("ISO8583P", "Parser mode incorrect or not sets");
            return false;
        }
        return true;
    }

    private void updateBitDef() {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        if (checkParam()) {
            String Select = "";
            if (ParseMode == Request) {
                Select = "select * from iso_data "
                        + "where influx = 1 "
                        + "and service_id = '" + serviceId + "' "
                        + "order by iso_bit_uid ";
            } else {
                Select = "select * from iso_data "
                        + "where influx = 2 "
                        + "and service_id = '" + serviceId + "' "
                        + "order by iso_bit_uid ";
            }
            bitDefCursor = clientDB.rawQuery(Select, null);
            if (bitDefCursor.getCount() < 1) {
                Log.i("ISO8583P", "Bit def is empty for service " + serviceId);
            }
        }
        helperDb.close();
        clientDB.close();
    }

    private void updateValueFromBitList(List<Integer> bitList, String isoMessage) {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        bitList.remove(Integer.valueOf(1));
        Collections.sort(bitList);
        int CUTHERE;
        String bitFormat;
        String msgScrap = isoMessage;
        String elementValue;
        String lenScrap;
        IsoBitValue = new String[128];
        Iterator bitIter = bitList.iterator();
        Log.i("ISO_DUMP", " ");
        Log.i("ISO_DUMP", "Message received");
        while (bitIter.hasNext()) {
            int bitPK = (int) bitIter.next();
            String Select = "select * from iso_8583 a, reff_meta_type b "
                    + "where a.meta_type_uid = b.meta_type_uid "
                    + "and a.iso_bit_uid = " + String.valueOf(bitPK);
            Cursor thisBit = clientDB.rawQuery(Select, null);
            thisBit.moveToFirst();
            CUTHERE = thisBit.getInt(thisBit.getColumnIndex("meta_length"));
            bitFormat = thisBit.getString(thisBit.getColumnIndex("meta_alias"));
            currentLength = "";
            if (CUTHERE == LLVAR) {
                lenScrap = msgScrap.substring(0, 2);
                msgScrap = msgScrap.substring(2);
                CUTHERE = Integer.parseInt(lenScrap);
                currentLength = lenScrap;
            }
            if (CUTHERE == LLLVAR) {
                lenScrap = msgScrap.substring(0, 4);
                msgScrap = msgScrap.substring(4);
                CUTHERE = Integer.parseInt(lenScrap);
                if (bitFormat.equals("b") | bitFormat.equals("n")) {
                    CUTHERE *= 2;
                }
                currentLength = lenScrap;
            }
            if (bitFormat.equals("b") | bitFormat.equals("n")) {
                if (CUTHERE % 2 > 0) {
                    CUTHERE += 1;
                }
                elementValue = msgScrap.substring(0, CUTHERE);
                msgScrap = msgScrap.substring(CUTHERE);
            } else if (bitFormat.equals("z")) {
                lenScrap = msgScrap.substring(0, 2);
                msgScrap = msgScrap.substring(2);
                currentLength = lenScrap;
                if (CUTHERE % 2 > 0) {
                    CUTHERE += 1;
                }
                elementValue = msgScrap.substring(0, CUTHERE);
                msgScrap = msgScrap.substring(CUTHERE);
            } else {
                elementValue = msgScrap.substring(0, CUTHERE * 2);
                try {
                    elementValue = new String(hexStringToByteArray(elementValue), "Cp1252");
                } catch (UnsupportedEncodingException ex) {
                    Log.i("ISO8583P", "Parse value error bit " + String.valueOf(bitPK));
                }
                msgScrap = msgScrap.substring(CUTHERE * 2);
            }
            if (elementValue.contains("'")) {
                elementValue = elementValue.replaceAll("'", "''") + " ";
            }
            IsoBitValue[thisBit.getInt(thisBit.getColumnIndex("iso_bit_uid"))] = elementValue;
            thisBit.close();
            Log.i("ISO_DUMP", "Bit " + String.valueOf(bitPK));
            Log.i("ISO_DUMP", (currentLength.equals("") ? "" : "[" + currentLength + "]") + elementValue);
        }
        Log.i("ISO_DUMP", "Reply for STAN : " + IsoBitValue[11]);
        helperDb.close();
        clientDB.close();
    }

    private void updateServiceMeta() {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        if (checkParam()) {
            String Select = "";
            if (ParseMode == Request) {
                Select = "SELECT * FROM service_meta "
                        + "WHERE service_id = '" + serviceId + "' and influx = 1";
            } else {
                Select = "SELECT * FROM service_meta "
                        + "WHERE service_id = '" + serviceId + "' and influx = 2";
            }
            if (serviceMetaCursor != null) {
                serviceMetaCursor.close();
            }
            serviceMetaCursor = clientDB.rawQuery(Select, null);
            if (serviceMetaCursor.getCount() < 1) {
                Log.i("ISO8583P", "Service Meta is empty for service " + serviceId);
            }
        }
        helperDb.close();
        clientDB.close();
    }

    private void setMti() {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        if (checkParam()) {
            String Select = "";
            Select = "select * from service where service_id = '" + serviceId + "'";
            Cursor c = clientDB.rawQuery(Select, null);
            if (c.getCount() < 1) {
                Log.i("ISO8583P", "Service not found (set mti)");
            } else {
                c.moveToFirst();
                if (ParseMode == Request) {
                    mti = c.getString(c.getColumnIndex("param1"));
                } else {
                    mti = c.getString(c.getColumnIndex("param2"));
                }
                serviceName = c.getString(c.getColumnIndex("service_name"));
            }
            c.close();
        }
        helperDb.close();
    }

    private List<Integer> getBitList() {
        LinkedList<Integer> bl = new LinkedList<Integer>();
        if (bitDefCursor.moveToFirst()) {
            do {
                bl.add(Integer.valueOf(bitDefCursor.getString(
                        bitDefCursor.getColumnIndex("iso_bit_uid"))));
            } while (bitDefCursor.moveToNext());
        }
        return bl;
    }

    public byte[] parseJSON() throws IOException, JSONException {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        String[] mpart = message.split("\\|");
        messageId = mpart[0];
        String qData = "select * from service_data "
                + "where message_id = '" + mpart[0] + "'";
        Cursor cData = clientDB.rawQuery(qData, null);
        JSONObject msg = new JSONObject();
        if (cData.moveToFirst()) {
            do {
                msg.put(cData.getString(cData.getColumnIndex("name")),
                        cData.getString(cData.getColumnIndex("value")));
            } while (cData.moveToNext());
        }
        cData.close();
        //prepare edc_log
        int elogId = 1;
        String getElogId = "select max(log_id) last_id from edc_log";
        Cursor stanSeq = clientDB.rawQuery(getElogId, null);
        if (stanSeq != null) {
            stanSeq.moveToFirst();
            elogId = stanSeq.getInt(0);
            elogId += 1;
        }
        stanSeq.close();

        int seqId = 1;
        String getEseqId = "select max(stan) last_id from edc_log";
        Cursor sSeq = clientDB.rawQuery(getEseqId, null);
        if (sSeq != null) {
            sSeq.moveToFirst();
            seqId = sSeq.getInt(0);
            seqId += 1;
        }
        sSeq.close();

        String elog = "insert or replace into edc_log " +
                "(log_id, service_id, stan, track2, amount) values (" +
                String.valueOf(elogId) + ",'" + serviceId + "', '" +
                stan + "', 'bit35', bit4);";
//        Log.d("ELOG", "ID " + String.valueOf(elogId));
//        Log.d("ELOG", "QRY " + elog);
        //compose
        //prepare tmp
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        //update header
        String Select = "";
        Select = "select iso_value from iso_data where service_id = '" + serviceId + "' "
                + "and iso_bit_uid = 24 and influx = 1";
        Cursor B24 = clientDB.rawQuery(Select, null);
        if (B24.getCount() > 0) {
            // hc2an
            B24.moveToFirst();
            String DestNii = B24.getString(B24.getColumnIndex("iso_value"));
            DestNii = StringLib.fillZero(DestNii, 4);
//            if (serviceId.equals("A11200")) {
//                header = "600" + DestNii + "9000";
//            } else {
//                header = header.substring(0, header.length() - DestNii.length()) + DestNii;
//            }
            header = "60" + DestNii + "9000";
//            if (serviceId.equals("A25100")) {
//                header = "6000070000";
//            }
        }
        B24.close();
        //inject header
        byte[] headerValue = hexStringToByteArray(header);
        tmp.write(headerValue);
        //inject mti
        byte[] mtiValue = hexStringToByteArray(mti);
        tmp.write(mtiValue);
        //inject bitmap
        String bmp = createBitmap(getBitList());
        byte[] bmpValue = hexStringToByteArray(bmp);
        tmp.write(bmpValue);
        Log.i("ISO_DUMP", " ");
        Log.i("ISO_DUMP", "Send request for service " + serviceId);
        //inject corresponding bits
        if (bitDefCursor.moveToFirst()) {
            do {
                int valMode = bitDefCursor.getInt(bitDefCursor.getColumnIndex("meta_length"));
                String elementValue = "";
                String valueFromDB;
                int bitId = bitDefCursor.getInt(bitDefCursor.getColumnIndex("iso_bit_uid"));
                Log.i("ISO_DUMP", "BIT " + String.valueOf(bitId));
                String rowData;

                if (bitId == 41 || bitId == 42) {
                    //pass
                } else {
                    switch (valMode) {
                        case ValueFromParam:
                            //dynamic value
                            String qAdd = "select * from iso_additional "
                                    + "where iso_bit_uid = " + String.valueOf(bitId)
                                    + " and service_id = '" + serviceId + "' "
                                    + " and influx in (1,3) "
                                    + "order by iso_bit_uid, iso_seq";
                            Cursor addList = clientDB.rawQuery(qAdd, null);
                            if (addList.getCount() < 1) {
                                String qMeta = "select meta_id from service_meta "
                                        + "where service_id = '" + serviceId + "' "
                                        + "and influx in (1,3) "
                                        + "and iso_bit_uid = " + String.valueOf(bitId);
                                Cursor metaList = clientDB.rawQuery(qMeta, null);
                                //to go
                                if (metaList.getCount() < 1) {
                                    Log.i("ISO8583P", "Required meta not found (sid " + serviceId + ")" + String.valueOf(bitId));
                                }
                                String lookupId = "";
                                if (metaList.moveToFirst()) {
                                    lookupId = metaList.getString(metaList.getColumnIndex("meta_id"));
                                }
                                metaList.close();
//                                Log.d("MSG", msg.toString());
                                if (serviceId.equals("A54331") && String.valueOf(bitId).equals("35")) {
                                    elementValue = mpart[0];// mpart[0];
                                }
//                                else if (serviceId.equals("A54331") && String.valueOf(bitId).equals("52")){
//                                    elementValue = "E8A3B62B50A29D5A";
//                                }
                                else {
                                    elementValue = (String) msg.get(lookupId);
                                    if (lookupId.equals("no_va")) {
                                        elementValue += "    ";
                                    }
                                }

                                Log.d("PICK", elementValue);
                            } else {
                                addList.moveToFirst();
                                do {
                                    int metaType = addList.getInt(addList.getColumnIndex("meta_type_uid"));
                                    String addValue = "";
                                    char padder = (char) 0x20;
                                    char padto = " ".charAt(0);
                                    String qmt = "select * from reff_meta_type where meta_type_uid = "
                                            + String.valueOf(metaType);
                                    Cursor cmt = clientDB.rawQuery(qmt, null);
                                    if (cmt.moveToFirst()) {
                                        if (cmt.isNull(cmt.getColumnIndex("meta_format"))) {
                                            if (addList.isNull(addList.getColumnIndex("iso_value"))) {
                                                addValue = (String) msg.get(addList.getString(addList.getColumnIndex("iso_element")));
                                            } else {
                                                addValue = addList.getString(addList.getColumnIndex("iso_value"));
                                            }
                                        } else {
                                            if (msg.has(cmt.getString(cmt.getColumnIndex("meta_format")))) {
                                                addValue = (String) msg.get(cmt.getString(cmt.getColumnIndex("meta_format")));
                                            } else {
                                                addValue = (String) cmt.getString(cmt.getColumnIndex("meta_format"));
                                            }

                                        }
                                        if (!(cmt.isNull(cmt.getColumnIndex("pader")))) {
                                            padder = cmt.getString(cmt.getColumnIndex("pader")).charAt(0);
                                        }
                                        if (!(cmt.isNull(cmt.getColumnIndex("padto")))) {
                                            padto = cmt.getString(cmt.getColumnIndex("padto")).charAt(0);
                                        }
                                    } else {
                                        addValue = (String) msg.get(addList.getString(addList.getColumnIndex("iso_element")));
                                    }
                                    if (addValue == null) {
                                        addValue = addList.getString(addList.getColumnIndex("iso_value"));
                                    }
                                    if (!(cmt.isNull(cmt.getColumnIndex("meta_alias")))) {
                                        if (padto == "R".charAt(0)) {
                                            addValue = padRight(addValue,
                                                    Integer.valueOf(cmt.getString(
                                                            cmt.getColumnIndex("meta_alias"))),
                                                    padder);
                                        } else {
                                            addValue = padLeft(addValue,
                                                    Integer.valueOf(cmt.getString(
                                                            cmt.getColumnIndex("meta_alias"))),
                                                    padder);
                                        }
                                    }
                                    cmt.close();
                                    if (addValue.equals("hashCode")) {
                                        addValue = addHash(elementValue);
                                    }
                                    elementValue = elementValue.concat(addValue);
//                                    Log.d("ADD", addValue);
                                } while (addList.moveToNext());
                            }
                            addList.close();
                            break;
                        case ValueFromService:
                            valueFromDB = bitDefCursor.getString(bitDefCursor.getColumnIndex("iso_value"));
                            switch (valueFromDB) {
                                case "currTime":
                                    valueFromDB = getCurrTime();
                                    break;
                                case "invoiceNum":
                                    valueFromDB = getInvoiceNumber();
                                    break;
                                case "currDate":
                                    valueFromDB = getCurrDate();
                                    break;
                                case "stan":
                                    valueFromDB = stan;
                                    break;
                                case "currDateTime":
                                    valueFromDB = getCurrDateTime();
                                    break;
                            }
                            elementValue = valueFromDB;
                            break;
                        case ValueFromDefinition:
                            String qIso = "select * from iso_8583 "
                                    + "where iso_bit_uid = " + String.valueOf(bitId);
                            Cursor cIso = clientDB.rawQuery(qIso, null);
                            cIso.moveToFirst();
                            valueFromDB = cIso.getString(cIso.getColumnIndex("default_value"));
                            switch (valueFromDB) {
                                case "currTime":
                                    valueFromDB = getCurrTime();
                                    break;
                                case "invoiceNum":
                                    valueFromDB = getInvoiceNumber();
                                    break;
                                case "currDate":
                                    valueFromDB = getCurrDate();
                                    break;
                                case "stan":
                                    valueFromDB = stan;
                                    break;
                                case "currDateTime":
                                    valueFromDB = getCurrDateTime();
                                    break;
                            }
                            cIso.close();
                            elementValue = valueFromDB;
                            break;
                    }
                }
//                if (bitId == 48 && serviceId.equals("A63001")){
//                    int saldo = Integer.parseInt(elementValue.substring(0, 16));
//                    String fee = elementValue.substring(16);
//                    int saldoA = saldo - 297000;
//                    String saldoAkhir = String.valueOf(saldoA);
//                    elementValue = saldoAkhir + fee;
//                }
                //Transfer ANtar bank Giro
                if (bitId == 3 && serviceId.equals("A53221")){
                    elementValue = "304020";
                }
                if (bitId == 63 && serviceId.equals("A53221")) {
                    elementValue = elementValue+"        ";
                    Log.d("elementValue", elementValue);
                }
                if (bitId == 3 && serviceId.equals("A53222")){
                    elementValue = "402000";
                }
                if (bitId == 63 && serviceId.equals("A53222")) {
                    elementValue = elementValue+"        ";
                    Log.d("elementValue", elementValue);
                }
                if (bitId == 48 && serviceId.equals("A53222")) {
                    elementValue = elementValue+"       ";
                    Log.d("elementValue", elementValue);
                }


                //Topup Online
                if (bitId == 48 && serviceId.equals("A25100")) {
                    String eV1, eV2;
                    eV1 = elementValue.substring(0, 48);
                    eV2 = elementValue.substring(50);
                    elementValue = eV1 +eV2;
                    Log.d("EV1", eV1.length()+"" +"EV2 "+eV2.length()+"");
                    Log.d("elementValue", elementValue.length()+"");
                }

                if (bitId == 48 && serviceId.equals("A54311")) {
                    elementValue = StringUtils.rightPad(elementValue, 60);
                }
                if (bitId == 48 && serviceId.equals("A54321")) {
                    elementValue = StringUtils.rightPad(elementValue, 60);
                }
                if (bitId == 48 && serviceId.equals("A54322")) {
                    elementValue = elementValue + "C010002" + msg.get("nominal");
                }
                if (bitId == 48 && serviceId.equals("A54331")) {
                    elementValue = elementValue + " " + msg.get("tx_ref");
                }
                if (bitId == 48 && serviceId.equals("A5C210")) {
                    elementValue = elementValue + msg.get("kd_billing");
//                    elementValue = StringUtils.rightPad(elementValue + msg.get("kd_billing"), 25);
                }
                if (bitId == 48 && serviceId.equals("A54341")) {
                    elementValue = StringUtils.rightPad(elementValue, 60);
                }
                if (bitId == 48 && serviceId.equals("A53211")) {
                    if (elementValue.startsWith("002")){
                        elementValue = StringUtils.rightPad(elementValue, 39);
                    } else {
                        elementValue = StringUtils.rightPad(elementValue, 60);
                    }
                }

                if (bitId == 48 && serviceId.equals("A53221")) {
//                    if (elementValue.startsWith("002")){
//                        elementValue = StringUtils.rightPad(elementValue, 39);
//                    } else {
                        elementValue = StringUtils.rightPad(elementValue, 60);
//                    }
                }

                if (bitId == 48 && serviceId.equals("A28100")) {
                    rowData = msg.get("rowdata").toString();
                    if (rowData.length() > 900) {
                        elementValue = elementValue.substring(0, 900);
                    }
                }

                if (bitId == 48 && serviceId.equals("A54B11")) {
                    String eV1, eV2;
                    eV1 = elementValue.substring(0, 5);
                    eV2 = elementValue.substring(7);
                    elementValue = eV1 + eV2 + "  ";
                }

                //log.info(elementValue);
//                if (bitId == 35) {
//                    try {
//                        String validDate = elementValue.substring(17, 23);
//                        SimpleDateFormat formatValidDate = new SimpleDateFormat("yyMMdd");
//                        Date dateValid = formatValidDate.parse(validDate);
//                        if (new Date().after(dateValid)) {
//                            return new JSONObject("{\"screen\":{\"ver\":\"1\",\"comps\":{\"comp\":[{\"visible\":true,\"comp_values\":{\"comp_value\":[{\"print\":\"Kartu telah kadaluarsa\",\n" +
//                                    "\"value\":\"Kartu telah kadaluarsa\"}]},\"comp_lbl\":\" \",\"comp_type\":\"1\",\"comp_id\":\"P00001\",\"seq\":0}]},\"id\":\"000000F\",\n" +
//                                    "\"type\":\"3\",\"title\":\"Gagal\"}}");
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

                if (bitId == 35) {
                    elog = elog.replace("bit35", elementValue);
                    elementValue = elementValue.replace("=".charAt(0), "D".charAt(0));
                }


//                if (bitId == 35 && serviceId.equals("A52210")) {
//                        elementValue = elementValue.substring(0, 27);
//                }

                // -- Add cent to bit 4
                if (bitId == 4) {
                    long e4log = 0;
                    if (serviceId.equals("A54A20")) {
                        elementValue = elementValue.substring(2) + "00";
                    }
                    if (elementValue.matches("-?\\d+(\\.\\d+)?")) {
                        e4log = Long.valueOf(elementValue);
                    }
                    if (serviceId.equals("A54322") || serviceId.equals("A54331") || serviceId.equals("A5C220") || serviceId.equals("A5C230")) {
//                        e4log = (e4log - 2500) * 100;
                        e4log = (e4log) * 100;
                    }
                    elog = elog.replace("bit4", String.valueOf(e4log));
//                    if (serviceId.equals("A54312")) {
//                        elementValue = elementValue.substring(2) + "00";
//                    }
                    if (elementValue.length() < 12) {
                        elementValue = padRight(elementValue, 12, '0');
//                        elementValue = elementValue.substring(2) + "00";
                        if (!serviceId.equals("A54312")) {
                            elementValue = elementValue.substring(2) + "00";
                        }
                    }
                    // Fixed Nominal Bansos
                    if (serviceId.equals("A92001")) {
                        elementValue = "00" + elementValue.substring(0, 10);
                    }
                    if (serviceId.equals("A93001")) {
                        elementValue = "00" + elementValue.substring(0, 10);
                    }
                }
//                ISO8583Parser rpParser = null;
//                String[] replyValues = rpParser.getIsoBitValue();
//                if (serviceId.equals("A92001")) {
//                    replyValues[4] = "00" + replyValues[4].substring(0,10);
//                }


//               && !serviceId.equals("A92001") && !serviceId.equals("A93001")
//                                && !serviceId.equals("A92000") && !serviceId.equals("A93000")
                //log.info(elementValue);
                // -- Uncomment for serverside encrypt pinblock --
//                if (bitId == 52) {
//                    try {
//                        elementValue = encPinblock(elementValue);
//                    } catch (Exception ex) {
//                        Log.e("ENC", ex.getMessage());
//                    }
//                }
                if (bitId == 41) {
                    elementValue = mpart[1];
                }
                if (bitId == 42) {
                    elementValue = mpart[2];
                }
                byte[] cBitValue = assignElement(bitId, elementValue);
                tmp.write(cBitValue);
                Log.i("ISO_DUMP", (currentLength.equals("") ? "" : "[" + currentLength + "]") + elementValue);
                if (bitId == 4) {
                    if (Long.parseLong(elementValue) > 0) {
                        reversable = true;
                    }
                }
            } while (bitDefCursor.moveToNext());
        }
        elog = elog.replace("bit35", "0000000000000000=0000");
        elog = elog.replace("bit4", "0");
//        Log.d("ELOG", "Updated : " + elog);

//        String array[] = {"A54911", "A51410", "A53100", "A53211", "A53221", "A54921", "A54931",
//                "A54941", "A54B11", "A54A10", "A54110", "A54211", "A54221", "A54311", "A54321", "A54410",
//                "A54431", "A54433", "A54441", "A54443", "A54451", "A54453", "A54461", "A54510",
//                "A54520", "A54530", "A54540", "A54550", "A54560", "A57000", "A57200", "A57400",
//                "A58000", "A54421", "A54423", "A54C10", "A54C20", "A54C51", "A54C52", "A54C53",
//                "A54C54", "A52100", "A52210", "A52220", "A52300", "A54950", "A54710", "A54720",
//                "A54800", "A59000", "A54331", "A71001", "A72000", "A72001", "A73000", "A61000",
//                "A62000", "A63000", "A2A100","A29100","A23100", "A22000","A23000","A22100","A2B000","A2B100"};

//        String array[] = {"L00001",
//                "A54941", "A54B11", "A54A10", "A54110", "A54211", "A54221", "A54321", "A54341", "A5C210",
//                "A54911", "A51410", "A53221", "A54921", "A54931",
//                "A54410", "A54431", "A54433", "A54441", "A54443", "A54451", "A54453", "A54461",
//                "A54510", "A54520", "A54530", "A54540", "A54550", "A54560", "A57000", "A57200",
//                "A57400", "A58000", "A54421", "A54423", "A54C10", "A54C20", "A54C51", "A54C52",
//                "A54C53", "A54C54", "A52210", "A52220", "A54950", "A54800", "A54710", "A54720",
//
//                "A71001", "A72000", "A72001", "A73000",
//
//                "A61000", "A62000", "A63000",
//
//                "A21100", "A22000", "A22100", "A23000", "A23100",
//                "A29100", "A2A100", "A2B000", "A2B100", "A2D100",
//                "A92000", "A93000", "A94000"};

        String array[] = {"L00001",
                "", "", "", "", "", "", "", "", "",
                "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",

                "", "", "", "",

                "", "", "", "",

                "", "", "", "", "",
                "", "", "", "", "",
                "", "", ""};

//                "A91000", "A52100","A52300","A59000", "A53100", "A53211", "A54311" ,,  };
        boolean matched_array = false;
        for (int i = 0; i < array.length; i++) {
            if (serviceId.equals(array[i])) {
                matched_array = true;
                i = array.length;
            }
        }
        if (!matched_array) {
            Log.d("ELOG", "Inserted by ISO8583Parser : " + elog);
            clientDB.execSQL(elog);
            String uStanSeq = "update holder set "
                    + "seq = " + stan;
            String invNum = "update holder set invnum = case when invnum = 999999 then 0 else invnum + 1 end ";
//            writeDebugLog("UPDATING", "HOLDER (1255)");
//                writeDebugLog("By ", serviceid);
            clientDB.execSQL(uStanSeq);
            clientDB.execSQL(invNum);
        }

        if (serviceId.equals("A2C200")) {
//            int numId = elogId + 10;
//            System.out.println("numId = "+numId);

            String uStanSeq = "update holder set "
                    + "seq = " + elogId;
            clientDB.execSQL(uStanSeq);
        }

//        clientDB.execSQL(elog);
        byte[] results = tmp.toByteArray();
        bitDefCursor.close();
        helperDb.close();
        clientDB.close();
        return results;
    }

    public byte[] parseJSONString() throws IOException, JSONException {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        String[] mpart = message.split("\\|");
        messageId = mpart[0];
        String qData = "select * from service_data "
                + "where message_id = '" + mpart[0] + "'";
        Cursor cData = clientDB.rawQuery(qData, null);
        JSONObject msg = new JSONObject();
        if (cData.moveToFirst()) {
            do {
                msg.put(cData.getString(cData.getColumnIndex("name")),
                        cData.getString(cData.getColumnIndex("value")));
            } while (cData.moveToNext());
        }
        cData.close();
        //compose
        //prepare tmp
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        //update header
        String Select = "";
        Select = "select iso_value from iso_data where service_id = '" + serviceId + "' "
                + "and iso_bit_uid = 24";
        Cursor B24 = clientDB.rawQuery(Select, null);
        if (B24.getCount() > 0) {
            // hc2an
            B24.moveToFirst();
            String DestNii = B24.getString(B24.getColumnIndex("iso_value"));
            if (!serviceId.equals("A11200")) {
                header = "600" + DestNii + "9000";
            } else {
                header = header.substring(0, header.length() - DestNii.length()) + DestNii;
            }
        }
        B24.close();
        //inject header
        byte[] headerValue = hexStringToByteArray(header);
        tmp.write(headerValue);
        //inject mti
        byte[] mtiValue = hexStringToByteArray(mti);
        tmp.write(mtiValue);
        //inject bitmap
        String bmp = createBitmap(getBitList());
        byte[] bmpValue = hexStringToByteArray(bmp);
        tmp.write(bmpValue);
        Log.i("ISO_DUMP", " ");
        Log.i("ISO_DUMP", "Send request for service " + serviceId + " - " + serviceName);
        //inject corresponding bits
        if (bitDefCursor.moveToFirst()) {
            do {
                int valMode = bitDefCursor.getInt(bitDefCursor.getColumnIndex("meta_length"));
                String elementValue = "";
                String valueFromDB;
                int bitId = bitDefCursor.getInt(bitDefCursor.getColumnIndex("iso_bit_uid"));
                Log.i("ISO_DUMP", "BIT " + String.valueOf(bitId));
                if (bitId == 41 || bitId == 42) {
                    //pass
                } else {
                    switch (valMode) {
                        case ValueFromParam:
                            //dynamic value
                            String qAdd = "select * from iso_additional "
                                    + "where iso_bit_uid = " + String.valueOf(bitId)
                                    + " and service_id = '" + serviceId + "' "
                                    + " and influx in (1,3) "
                                    + "order by iso_bit_uid, iso_seq";
                            Cursor addList = clientDB.rawQuery(qAdd, null);
                            if (addList.getCount() < 1) {
                                String qMeta = "select meta_id from service_meta "
                                        + "where service_id = '" + serviceId + "' "
                                        + "and influx in (1,3) "
                                        + "and iso_bit_uid = " + String.valueOf(bitId);
                                Cursor metaList = clientDB.rawQuery(qMeta, null);
                                //to go
                                if (metaList.getCount() < 1) {
                                    Log.i("ISO8583P", "Required meta not found (sid " + serviceId + ")" + String.valueOf(bitId));
                                }
                                String lookupId = "";
                                if (metaList.moveToFirst()) {
                                    lookupId = metaList.getString(metaList.getColumnIndex("meta_id"));
                                }
                                metaList.close();
                                elementValue = (String) msg.get(lookupId);
                            } else {
                                addList.moveToFirst();
                                do {
                                    int metaType = addList.getInt(addList.getColumnIndex("meta_type_uid"));
                                    String addValue = "";
                                    char padder = (char) 0x20;
                                    char padto = " ".charAt(0);
                                    String qmt = "select * from reff_meta_type where meta_type_uid = "
                                            + String.valueOf(metaType);
                                    Cursor cmt = clientDB.rawQuery(qmt, null);
                                    if (cmt.moveToFirst()) {
                                        if (cmt.isNull(cmt.getColumnIndex("meta_format"))) {
                                            if (addList.isNull(addList.getColumnIndex("iso_value"))) {
                                                addValue = (String) msg.get(addList.getString(addList.getColumnIndex("iso_element")));
                                            } else {
                                                addValue = addList.getString(addList.getColumnIndex("iso_value"));
                                            }
                                        } else {
                                            addValue = (String) msg.get(cmt.getString(cmt.getColumnIndex("meta_format")));
                                        }
                                        if (!(cmt.isNull(cmt.getColumnIndex("pader")))) {
                                            padder = cmt.getString(cmt.getColumnIndex("pader")).charAt(0);
                                        }
                                        if (!(cmt.isNull(cmt.getColumnIndex("padto")))) {
                                            padto = cmt.getString(cmt.getColumnIndex("padto")).charAt(0);
                                        }
                                    } else {
                                        addValue = (String) msg.get(addList.getString(addList.getColumnIndex("iso_element")));
                                    }
                                    if (addValue == null) {
                                        addValue = addList.getString(addList.getColumnIndex("iso_value"));
                                    }
                                    if (!(cmt.isNull(cmt.getColumnIndex("meta_alias")))) {
                                        if (padto == "R".charAt(0)) {
                                            addValue = padRight(addValue,
                                                    Integer.valueOf(cmt.getString(
                                                            cmt.getColumnIndex("meta_alias"))),
                                                    padder);
                                        } else {
                                            addValue = padLeft(addValue,
                                                    Integer.valueOf(cmt.getString(
                                                            cmt.getColumnIndex("meta_alias"))),
                                                    padder);
                                        }
                                    }
                                    cmt.close();
                                    elementValue = elementValue.concat(addValue);
                                    Log.d("ADD", addValue);
                                } while (addList.moveToNext());
                            }
                            addList.close();
                            break;
                        case ValueFromService:
                            valueFromDB = bitDefCursor.getString(bitDefCursor.getColumnIndex("iso_value"));
                            switch (valueFromDB) {
                                case "currTime":
                                    valueFromDB = getCurrTime();
                                    break;
                                case "invoiceNum":
                                    valueFromDB = getInvoiceNumber();
                                    break;
                                case "currDate":
                                    valueFromDB = getCurrDate();
                                    break;
                                case "stan":
                                    valueFromDB = stan;
                                    break;
                                case "currDateTime":
                                    valueFromDB = getCurrDateTime();
                                    break;
                            }
                            elementValue = valueFromDB;
                            break;
                        case ValueFromDefinition:
                            String qIso = "select * from iso_8583 "
                                    + "where iso_bit_uid = " + String.valueOf(bitId);
                            Cursor cIso = clientDB.rawQuery(qIso, null);
                            cIso.moveToFirst();
                            valueFromDB = cIso.getString(cIso.getColumnIndex("default_value"));
                            switch (valueFromDB) {
                                case "currTime":
                                    valueFromDB = getCurrTime();
                                    break;
                                case "invoiceNum":
                                    valueFromDB = getInvoiceNumber();
                                    break;
                                case "currDate":
                                    valueFromDB = getCurrDate();
                                    break;
                                case "stan":
                                    valueFromDB = stan;
                                    break;
                                case "currDateTime":
                                    valueFromDB = getCurrDateTime();
                                    break;
                            }
                            cIso.close();
                            elementValue = valueFromDB;
                            break;
                    }
                }
                //log.info(elementValue);
                if (bitId == 35) {
                    elementValue = elementValue.replace("=".charAt(0), "D".charAt(0));
                }

//                if (bitId == 35 && serviceId.equals("A52210")) {
//                    elementValue = elementValue.substring(0, 27);
//                }

                // -- Add cent to bit 4
                if (bitId == 4) {
                    elementValue = elementValue.substring(2) + "00";
                }
                //log.info(elementValue);
                // -- Uncomment for serverside encrypt pinblock --
            /*
            if (cBit.getIsoDataPK().getIsoBitUid()==52) {
                try {
                    elementValue = encPinblock(elementValue);
                } catch (Exception ex) {
                    log.debug(ex);
                }
            }
                    */
                if (bitId == 41) {
                    elementValue = mpart[1];
                }
                if (bitId == 42) {
                    elementValue = mpart[2];
                }
                byte[] cBitValue = assignElement(bitId, elementValue);
                tmp.write(cBitValue);
                Log.i("ISO_DUMP", (currentLength.equals("") ? "" : "[" + currentLength + "]") + elementValue);
            } while (bitDefCursor.moveToNext());
        }
        byte[] results = tmp.toByteArray();
        bitDefCursor.close();
        helperDb.close();
        clientDB.close();
        return results;
    }

    private String encPinblock(String plainKey) throws InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            BadPaddingException {
        String dummyKey = "1C1C1C1C1F1F1F1F";
        DESKeySpec dks = new DESKeySpec(hexStringToByteArray(dummyKey));
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE
        cipher.init(Cipher.ENCRYPT_MODE, desKey);
        plainKey = plainKey + "FFFFFFFFFFFFFFFF";
        plainKey = plainKey.substring(0, 16);
        byte[] pinBlock = cipher.doFinal(hexStringToByteArray(plainKey));
        return bytesToHex(pinBlock);
    }

    public JSONObject parseISO() throws JSONException {
        updateBitDef();
        updateServiceMeta();
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        //update edclog
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String tgl = simpleDateFormat.format(date);
        String edclog = "select log_id from edc_log " +
                "where service_id = '" + serviceId + "' " +
                "and stan = '" + stan + "' " +
                "and date(rqtime) = '" + tgl + "'";
//        Log.d("ELOG", "Qry : "+edclog);
        Cursor eLog = clientDB.rawQuery(edclog, null);
        int elogId = 0;
        if (eLog != null) {
            if (eLog.moveToFirst()) {
                elogId = eLog.getInt(eLog.getColumnIndex("log_id"));
//                Log.d("ELOG", "Elog found");
            }
        }
        eLog.close();
        String updAmount = "";
        Long plnAmt = 0L;
//        String[] plnSvc = {"A54312","A54322","A54331"};
        String[] plnSvc = {"A54322", "A54331"};
        if (IsoBitValue[4] != null) {
            Long longAmount = Long.parseLong(IsoBitValue[4]);
            if (longAmount > 0) {
                updAmount = ", amount = " + String.valueOf(longAmount);
            }
//            if (Arrays.asList(plnSvc).contains(serviceId)) {
//                plnAmt = (longAmount) * 100;
//                updAmount = ", amount = " + String.valueOf(plnAmt);
//                if (IsoBitValue[48]!=null) {
//                    plnAmt = (Long.parseLong(IsoBitValue[48].substring(88,97))) * 100;
//                    updAmount = ", amount = " + String.valueOf(plnAmt);
//                }
//            }
            if (serviceId.equals("A54312") || serviceId.equals("A54322") || serviceId.equals("A54342")) {
                Log.d("TEST", "MASUK");
                updAmount = ", amount = " + String.valueOf(longAmount * 100);
            }
//            if (serviceId.equals("A92001")) {
//                updAmount = ", amount = " + String.valueOf(longAmount/10000);
//            }
//            if (serviceId.equals("A93000")) {
//                updAmount = ", amount = " + String.valueOf(longAmount*100);
//            }
        }

        //Bug Fix RC CP dan TL pada fitur Absen
        if (serviceId.startsWith("A3") && (IsoBitValue[39].equals("CP") || IsoBitValue[39].equals("TL"))){
            IsoBitValue[39] = "00";
            responseCode = "00";
        }

        if (!responseCode.equals("00") && elogId != 0) {
            String deleteelog = "delete from edc_log where log_id = " + String.valueOf(elogId);
            Log.d("DELETE ELOG", "UPD : " + deleteelog);
            clientDB.execSQL(deleteelog);
            String uStanSeq = "update holder set "
                    + "seq = " + Integer.toString((Integer.parseInt(stan) - 1));
//            writeDebugLog("UPDATING", "HOLDER (1255)");
//                writeDebugLog("By ", serviceid);
            clientDB.execSQL(uStanSeq);
        } else if (elogId != 0) {
            String updRCelog = "update edc_log set rc = '" + responseCode + "' " + updAmount + " where log_id = " + String.valueOf(elogId);
            Log.d("ELOG", "UPD : " + updRCelog);
            clientDB.execSQL(updRCelog);
        }
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("messageId", messageId);
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
        SimpleDateFormat stf = new SimpleDateFormat("HHmmss");
        String localDate = sdf.format(d);
        String localTime = stf.format(d);
        if (IsoBitValue[37] != null) {
            resultJSON.put("server_ref", IsoBitValue[37]);
        }
        if (!serviceId.equals("A26100")) {
            if (IsoBitValue[12] != null) {
                resultJSON.put("server_time", IsoBitValue[12]);
            } else {
                resultJSON.put("server_time", localTime);
            }
        } else {
            resultJSON.put("server_time", localTime);
        }

        if (IsoBitValue[13] != null) {
            resultJSON.put("server_date", IsoBitValue[13]);
        } else {
            resultJSON.put("server_date", localDate);
        }
        if (IsoBitValue[38] != null) {
            resultJSON.put("server_air", IsoBitValue[38]);
            resultJSON.put("server_appr", IsoBitValue[38]);
        }
        if (!responseCode.equals("00")) {
            String qRC = "select * from responsecode "
                    + "where response_uid = '" + responseCode + "' "
                    + "and service_id in ('" + serviceId + "', '0') "
                    + "order by service_id desc";
            Cursor cRC = clientDB.rawQuery(qRC, null);
            if (cRC.moveToFirst()) {
                String msgRC = cRC.getString(cRC.getColumnIndex("ina_msg"));
                resultJSON.put("badRC", responseCode);
                resultJSON.put("badRM", msgRC);
                resultJSON.put("msg_rc", responseCode);
                resultJSON.put("msg_resp", msgRC);
                resultJSON.put("msg_rc_48", IsoBitValue[48]);
                if (cRC != null) {
                    cRC.close();
                }
//                Log.d("ISO8583P", "SID : " + serviceId);
//                Log.d("ISO8583P", "RC  : " + responseCode);
                if (!((serviceId.equals("A54322") && responseCode.equals("02"))
                        || (serviceId.equals("A54322") && responseCode.equals("68"))
                        || (serviceId.equals("A56000") && responseCode.equals("68")))) {
                    return resultJSON;
                }
            } else {
                resultJSON.put("badRC", responseCode);
                resultJSON.put("badRM", "Server response code :" + responseCode + "");
                resultJSON.put("msg_rc", responseCode);
                resultJSON.put("msg_resp", "Server response code :" + responseCode + "");
                resultJSON.put("msg_rc_48", IsoBitValue[48]);
                if (cRC != null) {
                    cRC.close();
                }
//                Log.d("ISO8583P", "SID : "+serviceId);
//                Log.d("ISO8583P", "RC  : "+responseCode);
                if (!((serviceId.equals("A54322") && responseCode.equals("02"))
                        || (serviceId.equals("A54322") && responseCode.equals("02"))
                        || (serviceId.equals("A56000") && responseCode.equals("68")))) {
                    return resultJSON;
                }
            }
        }
        serviceMetaCursor.moveToFirst();
        do {
            String fieldName = serviceMetaCursor.getString(serviceMetaCursor.getColumnIndex("meta_id"));
            int bitContainer = Integer.valueOf(
                    serviceMetaCursor.getString(serviceMetaCursor.getColumnIndex("iso_bit_uid")));
            String qAdd = "select * from iso_additional "
                    + "where iso_bit_uid = " + String.valueOf(bitContainer)
                    + " and service_id = '" + serviceId + "' "
                    + " and influx = 2 "
                    + "order by iso_bit_uid, iso_seq";
            Cursor addList = clientDB.rawQuery(qAdd, null);
            if (addList.moveToFirst()) {
                int prefixLength = 0;


                int flightCounter = 1;
                String flightContent = "";
                String sbValue = "";
                do {
                    String subBit = addList.getString(addList.getColumnIndex("iso_element"));
                    int metaType = addList.getInt(addList.getColumnIndex("meta_type_uid"));
                    String qmt = "select * from reff_meta_type where meta_type_uid = "
                            + String.valueOf(metaType);
                    Cursor crmt = clientDB.rawQuery(qmt, null);
                    crmt.moveToFirst();
                    int sbLength = Integer.valueOf(crmt.getString(crmt.getColumnIndex("meta_alias")));
                    if (subBit.equals(fieldName)) {
                        sbValue = "";
                        if (fieldName.equals("flight_data")) {
                            sbLength = sbLength * flightCounter;
                        }
                        Log.d("ERR", fieldName);
                        Log.d("ERR", String.valueOf(bitContainer));
                        if (IsoBitValue[bitContainer] != null) {
                            try {
                                sbValue = IsoBitValue[bitContainer].substring(prefixLength, prefixLength + sbLength);
                            } catch (Exception e) {
                                Log.e("ISO8583P", "Value of Bit " + String.valueOf(bitContainer) + " is too short.");
                                try {
                                    sbValue = IsoBitValue[bitContainer].substring(prefixLength);
                                } catch (Exception ex) {
                                    Log.e("ISO8583P", "Value of Bit " + String.valueOf(bitContainer) + " even not in range.");
                                    sbValue = "";
                                }
                            }
                        }
                        if (fieldName.equals("flight_data")) {
                            flightContent = sbValue;
                        }
                        if (fieldName.equals("flight_count") && sbValue.matches("-?\\d+(\\.\\d+)?")) {
                            flightCounter = Integer.valueOf(sbValue);
                        }
                        if (fieldName.equals("periode_input") && serviceId.startsWith("A581")) {
                            String parsedValue = "";
                            if (parsedValue.length() > 1) {
                                parsedValue += sbValue.substring(0, 2);
                            }
                            parsedValue += "/";
                            if (parsedValue.length() > 5) {
                                parsedValue += sbValue.substring(4);
                            }
                            sbValue = parsedValue;
                        }
                        if (fieldName.equals("status") & serviceId.startsWith("A3")) {
                            String parsedValue = "";
                            switch (sbValue) {
                                case "TL":
                                    parsedValue = "Terlambat ";
                                    break;
                                case "CP":
                                    parsedValue = "Lebih Cepat ";
                                    break;
                                default:
                                    parsedValue = "Ok ";
                            }
                            sbValue = parsedValue;
                        }
                        if (fieldName.equals("late") & serviceId.startsWith("A3")) {
                            String parsedValue = "";
                            if (sbValue.contains(":")) {
                                String[] tmValue = sbValue.split(":");
                                if (Integer.valueOf(tmValue[0]) != 0) {
                                    parsedValue = Integer.valueOf(tmValue[0]).toString() + " jam ";
                                }
                                if (Integer.valueOf(tmValue[1]) != 0) {
                                    parsedValue += Integer.valueOf(tmValue[1]).toString() + " menit ";
                                }
                                if (Integer.valueOf(tmValue[2]) != 0) {
                                    parsedValue += Integer.valueOf(tmValue[2]).toString() + " detik ";
                                }
                                sbValue = parsedValue;
                                Log.i("ERR", sbValue);
                            }
                        }
                        if (fieldName.equals("nom_rptok")) {
                            String nomstr = String.valueOf(plnAmt);
                            int nomlen = nomstr.length();
                            if (sbValue.charAt(0) > nomstr.charAt(0)) {
                                nomlen = nomlen - 1;
                            }
                            sbValue = sbValue.substring(0, nomlen); //+ "00";
                        }
//                        if (fieldName.equals("nom_ppj")) {
//                            sbValue = sbValue + "0";
//                        }
                        if (fieldName.equals("switcher")) {
                            if (sbValue.startsWith("02")) {
                                sbValue = "PRIMA";
                            } else if (sbValue.startsWith("03")) {
                                sbValue = "BERSAMA";
                            } else if (sbValue.startsWith("01")) {
                                sbValue = "LINK";
                            } else if (sbValue.startsWith("00")) {
                                sbValue = "BRI";
                            }
                        }
                        break;
                    } else {
                        prefixLength += sbLength;
//                        Log.d("ERR", "PL : " + String.valueOf(prefixLength));
                    }
                    crmt.close();
                    //Log.i("PARSER", fieldName+" = "+sbValue);
                } while (addList.moveToNext());
                if (serviceId.startsWith("A549")) {
//                    Log.d("FLIGHT", "Data Count   : " + String.valueOf(flightCounter));
//                    Log.d("FLIGHT", "Data Content : " + String.valueOf(flightContent));
                }
                resultJSON.put(fieldName, sbValue);
            } else {
                String bValue = "";
                if (IsoBitValue[bitContainer] != null) {
                    bValue = IsoBitValue[bitContainer];
                }
                resultJSON.put(fieldName, bValue);
            }
            addList.close();
        } while (serviceMetaCursor.moveToNext());
        //String result = resultJSON;
        serviceMetaCursor.close();
        helperDb.close();
        clientDB.close();
        return resultJSON;
    }

    private String getCurrTime() {
        SimpleDateFormat timef = new SimpleDateFormat("HHmmss");
        Date date = new Date();
        return timef.format(date);
    }

    private String getInvoiceNumber() {
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        Cursor c = clientDB.rawQuery("select invnum from holder", null);

        int invNo = 0;
        if (c != null) {
            c.moveToFirst();
            invNo = c.getInt(0);
        }
        c.close();

        if (clientDB.isOpen()) {

            clientDB.close();
        }

        return String.format("%06d", invNo);
    }

    private String getCurrDate() {
        SimpleDateFormat datef = new SimpleDateFormat("MMdd");
        Date date = new Date();
        return datef.format(date);
    }

    private String getCurrDateTime() {
        SimpleDateFormat datef = new SimpleDateFormat("MMddHHmmss");
        Date date = new Date();
        return datef.format(date);
    }

    private String padLeft(String str, int length, char padder) {
        //log.info(str.length());
        if (length == str.length()) {
            return str;
        }
        int bitLength = length;
        if ((bitLength % 2) > 1) {
            bitLength += 1;
        }
        //log.info(bitLength);
        if (bitLength < str.length()) {
            return str.substring(0, bitLength + 1);
        }
        int nlen = str.length();
        return str + String.format("%" + (length - nlen) + "s", "").replace(" ", String.valueOf(padder));
    }

    private String padRight(String str, int length, char padder) {
        if (length == str.length()) {
            return str;
        }
        int bitLength = length;
        if ((bitLength % 2) > 1) {
            bitLength += 1;
        }
        if (bitLength < str.length()) {
            return str.substring(0, bitLength + 1);
        }
        return String.format("%" + (length - str.length()) + "s", "").replace(" ", String.valueOf(padder)) + str;
    }

    private String createBitmap(List<Integer> bitList) {
        bitList.remove(Integer.valueOf(1));
        int maxBitmap;
        if (Collections.max(bitList) <= 64) {
            maxBitmap = 64;
        } else {
            maxBitmap = 128;
            bitList.add(1);
        }
        String sbits = "";
        String sbytes = "";
        String sbitmap = "";
        for (int i = 1; i <= maxBitmap; i++) {
            if (bitList.contains(i)) {
                sbits += "1";
            } else {
                sbits += "0";
            }
            if (sbits.length() == 8) {
                String cbytes = Integer.toHexString(Integer.parseInt(sbits, 2));
                if (cbytes.length() < 2) {
                    cbytes = "0" + cbytes;
                }
                sbytes += cbytes;
                sbits = "";
            }
            if (sbytes.length() == 16) {
                sbitmap += sbytes;
                sbytes = "";
            }
        }
        return sbitmap;
    }

    private List<Integer> readBitmap(String bitmap, Boolean isExt) {
        LinkedList<Integer> bl = new LinkedList<Integer>();
        int startVal = 0;
        if (isExt) {
            startVal = 64;
        }
        try {
            if (bitmap.length() != 16) {
                Log.e("BMP", "Invalid bitmap");
                return bl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 8; i++) {
            //String thisbit = String.format("%08s", Integer.toBinaryString(Integer.parseInt(bitmap.substring(i*2, (i*2)+2), 16)));
            String thisbit = padRight(Integer.toBinaryString(Integer.parseInt(bitmap.substring(i * 2, (i * 2) + 2), 16)), 8, "0".charAt(0));
            for (int j = 0; j < thisbit.length(); j++) {
                if (thisbit.charAt(j) == "1".charAt(0)) {
                    bl.add((i * 8) + j + 1 + startVal);
                }
            }
        }
        return bl;
    }

    private byte[] assignElement(int bitId, String bitValue) {
        //get this bit definition
        SQLiteDatabase clientDB = null;
        helperDb.openDataBase();
        try {
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception e) {

        }
        byte[] elementValue = null;
        String qFB = "select * from iso_8583 "
                + "where iso_bit_uid = " + String.valueOf(bitId);
        Cursor cFB = clientDB.rawQuery(qFB, null);
        if (cFB.getCount() < 1) {
            return elementValue;
        }
        int bitLength = 0;
        int metaType = 0;
        if (cFB.moveToFirst()) {
            bitLength = cFB.getInt(cFB.getColumnIndex("meta_length"));
            metaType = cFB.getInt(cFB.getColumnIndex("meta_type_uid"));
        }
        cFB.close();
        String qMT = "select * from reff_meta_type "
                + "where meta_type_uid = " + String.valueOf(metaType);
        Cursor cMT = clientDB.rawQuery(qMT, null);
        cMT.moveToFirst();

        String padder = " ";
        if (cMT.getString(cMT.getColumnIndex("pader")) != null) {
            padder = cMT.getString(cMT.getColumnIndex("pader"));
        }
        String padTo = "L";
        if (cMT.getString(cMT.getColumnIndex("padto")) != null) {
            padTo = cMT.getString(cMT.getColumnIndex("padto"));
        }
        String bitFormat = cMT.getString(cMT.getColumnIndex("meta_alias"));
        cMT.close();

        // Fixed Bni
        if (bitFormat.equals("z")) {
            padTo = "L";
            padder = "0";
        }

//        if (bitFormat.equals("z")) {
//            if (serviceId.equals("A52210")){
//                padTo = "L";
//                padder = " ";
//            } else {
//                padTo = "L";
//                padder = "0";
//            }
//        }

        switch (bitLength) {
            case LLVAR:
                if (bitFormat.equals("z")) { //Fixed Bni
                    bitLength = bitValue.length();
                    String bitLengthChar = String.valueOf(bitLength);
                    if ((bitLength % 2) > 0) {
                        bitLength += 1;
                        bitValue = padLeft(bitValue, bitLength, "0".charAt(0));
                    }
                    elementValue = new byte[(bitLength / 2) + 1];
                    elementValue[0] = (byte) ((Character.digit(bitLengthChar.charAt(0), 16) << 4) + (Character.digit(bitLengthChar.charAt(1), 16)));
                    for (int i = 0; i < bitLength; i += 2) {
                        elementValue[(i / 2) + 1] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
                    }
                } else {
                    String valLength1 = String.format("%02d", bitValue.length());
                    elementValue = new byte[bitValue.length() + 1];
                    System.arraycopy(hexStringToByteArray(valLength1), 0, elementValue, 0, 1);
                    System.arraycopy(bitValue.getBytes(), 0, elementValue, 1, bitValue.length());
                    currentLength = valLength1;
                }
                break;
            case LLLVAR:
                if (bitFormat.equals("b")) {
                    bitLength = bitValue.length();
                    if ((bitLength % 2) > 0) {
                        bitLength += 1;
                        bitValue = padLeft(bitValue, bitLength, "F".charAt(0));
                    }
                    byte[] lllBitValue = new byte[bitLength / 2];
                    for (int i = 0; i < bitLength; i += 2) {
                        lllBitValue[i / 2] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
                    }
                    String valLength2 = String.format("%04d", bitLength / 2);
                    elementValue = new byte[(bitLength / 2) + 2];
                    System.arraycopy(hexStringToByteArray(valLength2), 0, elementValue, 0, 2);
                    System.arraycopy(lllBitValue, 0, elementValue, 2, bitLength / 2);
                    currentLength = valLength2;
                } else {
                    String valLength2 = String.format("%04d", bitValue.length());
                    elementValue = new byte[bitValue.length() + 2];
                    System.arraycopy(hexStringToByteArray(valLength2), 0, elementValue, 0, 2);
                    System.arraycopy(bitValue.getBytes(), 0, elementValue, 2, bitValue.length());
                    currentLength = valLength2;
                }
                break;
//            case 37:
//                bitLength = bitValue.length();
//                if ((bitLength % 2) > 0) {
//                    bitLength += 1;
//                    bitValue = padLeft(bitValue, bitLength, "F".charAt(0));
//                }
//                byte[] lllBitValue = new byte[bitLength / 2];
//                for (int i = 0; i < bitLength; i += 2) {
//                    lllBitValue[i / 2] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
//                }
//                String valLength2 = String.format("%04d", bitLength);
//                elementValue = new byte[(bitLength / 2) + 2];
//                System.arraycopy(hexStringToByteArray(valLength2), 0, elementValue, 0, 2);
//                System.arraycopy(lllBitValue, 0, elementValue, 2, bitLength / 2);
//                currentLength = valLength2;
//                break;
            default:
                if (padTo.equals("L")) {
                    bitValue = padLeft(bitValue, bitLength, padder.charAt(0));
                } else {
                    bitValue = padRight(bitValue, bitLength, padder.charAt(0));
                }
                if (bitFormat.equals("b")) {
                    if ((bitLength % 2) > 0) {
                        bitLength += 1;
                        bitValue = padLeft(bitValue, bitLength, "F".charAt(0));
                    }
                    elementValue = new byte[bitLength / 2];
                    for (int i = 0; i < bitLength; i += 2) {
                        elementValue[i / 2] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
                    }
                } else if (bitFormat.equals("n")) {
                    if ((bitLength % 2) > 0) {
                        bitLength += 1;
                        bitValue = padRight(bitValue, bitLength, "F".charAt(0));
                    }
                    elementValue = new byte[bitLength / 2];
                    for (int i = 0; i < bitLength; i += 2) {
                        elementValue[i / 2] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
                    }
                } else if (bitFormat.equals("z")) { //Fixed Bni
                    if ((bitLength % 2) > 0) {
                        bitLength += 1;
                        bitValue = padLeft(bitValue, bitLength, "0".charAt(0));
                    }
                    elementValue = new byte[(bitLength / 2) + 1];
                    elementValue[0] = (byte) ((Character.digit("3".charAt(0), 16) << 4) + (Character.digit("7".charAt(0), 16)));
                    for (int i = 0; i < bitLength; i += 2) {
                        elementValue[(i / 2) + 1] = (byte) ((Character.digit(bitValue.charAt(i), 16) << 4) + Character.digit(bitValue.charAt(i + 1), 16));
                    }
                } else {
                    elementValue = new byte[bitLength];
                    System.arraycopy(padLeft(bitValue, bitLength, " ".charAt(0)).substring(0, bitLength).getBytes(), 0, elementValue, 0, bitLength);
                }
                currentLength = "";
                break;
        }
        helperDb.close();
        clientDB.close();
        return elementValue;
    }

    public String addHash(String toHash) {
        String hashCode;
        hashCode = padLeft(toHash, 64, 'F');
        int a = hexStringToByteArray(hashCode).hashCode();
        hashCode = Integer.toHexString(a);
        return hashCode;
    }

    public static long getDateDiff(Calendar earlier, Calendar later) {
        long diff = 0;
        Log.d("DATEDIFF", earlier.toString());
        Log.d("DATEDIFF", later.toString());
        long timeDiff = Math.abs(later.getTimeInMillis() - earlier.getTimeInMillis());
        Log.d("DATEDIFF", "ms diff : " + String.valueOf(timeDiff));
        diff = TimeUnit.MILLISECONDS.toDays(timeDiff);
        Log.d("DATEDIFF", "day diff : " + String.valueOf(diff));
        return diff;
    }

}