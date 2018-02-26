/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package id.co.bri.brizzi.handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.bri.brizzi.common.StringLib;

/**
 *
 * @author Ahmad
 */
public class LogHandler {

    String serviceid;
    private Cursor c;
    private Context ctx;
    private DataBaseHelper helperDb;
    SQLiteDatabase clientDB = null;
    private Boolean ignoreReplyAmount;

    public LogHandler(Context ctx) {
        this.ctx = ctx;
        try {
            helperDb = new DataBaseHelper(ctx);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            //
        }
        ignoreReplyAmount = false;
    }

//    public String getLastAmountDeduct(String cardNumber){
//        Log.d("EDCLOG", "read amount from handler (44)");
//        String qry = "select amount from edc_log  where track2  = '"+cardNumber+"' and service_id = 'A24100' order by rqtime desc limit 1;";
//
//        Cursor c = clientDB.rawQuery(qry, null);
//        if (c.moveToFirst()) {
//            return c.getString(0);
//        } else {
//            return "";
//        }
//    }
//
//    public void writeOfflineLog(String service_id, String NomorKartu, String amount, String Proccode, String rc) {
//        int logid = 0;
//        Log.d("EDCLOG", "read seq from handler (57)");
//        String qry = "select max(log_id) from edc_log ";
//        Cursor mxid = clientDB.rawQuery(qry, null);
//        if (mxid.moveToFirst()) {
//            logid = mxid.getInt(0) + 1;
//        } else {
//            logid = 1;
//        }
//        Log.d("EDCLOG", "insert from handler (65)" + service_id + ";" + "000012");
//        String newLog = "insert or replace into edc_log("
//                + "log_id, service_id, stan, track2, amount, proccode, rc) values "
//                + "("+String.valueOf(logid)
//                +",'"+service_id+"',"
//                + "'000012"
//                +"','"+NomorKartu
//                +"',"+amount
//                +", '"+Proccode
//                +"', '"+rc
//                +"')";
//        clientDB.execSQL(newLog);
//        clientDB.close();
//    }
//
    public int writePreLog(String[] IsoBitValues, String serviceId, String messageId) {
        int logid = 0;
        Log.d("EDCLOG", "read seq from handler (82)");
        String qry = "select max(log_id) from edc_log ";
        Cursor mxid = clientDB.rawQuery(qry, null);
        if (mxid.moveToFirst()) {
            if (mxid !=null) {
                logid = mxid.getInt(0) + 1;
            } else {
                logid = 1;
            }
        } else {
            logid = 1;
        }
        if(IsoBitValues != null){
            String amount = null;
            if (IsoBitValues[4]!=null) {
                amount = IsoBitValues[4];
            }
            if (IsoBitValues[48] != null) {
                if (serviceId.equals("A54322") || serviceId.equals("A54331")) {
                    amount = IsoBitValues[48].substring(88, 97) + "00";
                }
            }
            Log.d("EDCLOG", "insert  from handler (44)");
            String newLog = "insert or replace into edc_log("
                    + "log_id, service_id, stan, track2, amount, messageid, proccode, rqtime) values "
                    + "("+String.valueOf(logid)
                    +",'"+serviceId+"',"
                    + "'"+IsoBitValues[11]
                    +"','"+IsoBitValues[35]
                    +"',"+amount
                    +",'"+messageId
                    +"','"+IsoBitValues[3]
                    + "', current_timestamp)";
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

                    "A21100", "A22000", "A22100", "A23000", "A23100",
                    "A29100", "A2A100", "A2B000", "A2B100", "A2D100",
                    "A91000", "A92000", "A93000", "A94000"};
            boolean matched_array = false;
            for(int i=0; i < array.length; i++){
                if(serviceId.equals(array[i])){
                    matched_array = true;
                    i = array.length;
                }
            }
            if (!matched_array){
                Log.d("ELOG", "Inserted by LogHandler : " + newLog);
                clientDB.execSQL(newLog);
            }
//        clientDB.execSQL(newLog);
        }
        this.serviceid = serviceId;
        return logid;
    }
    
    public void writePostLog(String[] IsoBitValues, int logid) {
        if (serviceid!=null) {
            return;
        }
        if (!clientDB.isOpen()) {
            try {
                helperDb = new DataBaseHelper(ctx);
                clientDB = helperDb.getActiveDatabase();
            } catch (Exception ex) {
                //
            }
        }
        if (IsoBitValues[63]!=null) {
            Log.d("LGH", IsoBitValues[63]);
        }
        String amount = IsoBitValues[4];
        if (amount==null) {
            amount = "0";
        }
        if (IsoBitValues[3]!=null&&IsoBitValues[63]!=null) {
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("203TELKOMSEL")) {
                //set amount HALO
                amount = IsoBitValues[48].substring(24,36);
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54212")) {
                //set amount HALO
                amount = IsoBitValues[48].substring(24,36);
                Log.d("AMT", amount);
            }
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("202INDOSAT")) {
                //set amount MATRIX
                amount = IsoBitValues[48].substring(11,23);
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54222")) {
                //set amount MATRIX
                amount = IsoBitValues[48].substring(11,23);
                Log.d("AMT", amount);
            }
            if (IsoBitValues[3].equals("111000") && IsoBitValues[63].startsWith("204BRICC")) {
                //set amount BRICC
                amount = "x100";
                Log.d("AMT", amount);
            }
            if (serviceid.equals("A54411")) {
                //set amount BRICC
                amount = "x100";
                Log.d("AMT", amount);
            }
        }
        if (amount.equals("x100")) {
            amount = "amount * 100";
        } else if (!amount.replaceAll("[0123456789]","").equals("")) {
            amount = "0";
        }
        String updAmount = "";
        if (!ignoreReplyAmount) {
            updAmount = "amount = " + amount + ", ";
            Log.i("LOG", "Apply reply amount");
        }
        if (serviceid.equals("A54322")||serviceid.equals("A54331")||serviceid.equals("A54312")) {
//            updAmount = "amount = (amount-2500) * 100, ";
            updAmount = " ";
        }
        String updRqTime = "";
        if (IsoBitValues[13]!=null) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String repDate = sdf.format(d) + "-";
            String tmStamp = null;
            repDate = repDate + IsoBitValues[13].substring(0, 2) + "-" +
                    IsoBitValues[13].substring(2, 4);
            if (IsoBitValues[12] != null) {
                tmStamp = StringLib.toSQLiteTimestamp(repDate, IsoBitValues[12]);
            } else {
                SimpleDateFormat stf = new SimpleDateFormat("HHmmss");
                tmStamp = StringLib.toSQLiteTimestamp(repDate, stf.format(d));
            }
            updRqTime = ", rqtime = '" + tmStamp + "' ";
        }
        String updLog = "update edc_log set " + updAmount
                + " rran = '"+ IsoBitValues[37]
                + "', rc = '"+ IsoBitValues[39]
                + "', rptime = current_timestamp "
                + updRqTime
                + " where log_id = "+ logid;
        Log.d("UQL", updLog);
        clientDB.beginTransaction();
        try {
            clientDB.execSQL(updLog);
            clientDB.setTransactionSuccessful();
            clientDB.endTransaction();
            Log.d("UQL", "Update OK");
        } catch (Exception e) {
            clientDB.endTransaction();
            Log.d("UQL", "Update not OK");
        } finally {
            clientDB.close();
        }
    }

    public void writeRevResponse(String rc, String oriMsg, int logid) {
        if (!clientDB.isOpen()) {
            try {
                helperDb = new DataBaseHelper(ctx);
                clientDB = helperDb.getActiveDatabase();
            } catch (Exception ex) {
                //
            }
        }
//        String revStatus = rc.equals("00") ? "T" : "P";
        String revStatus = "T";
        String updLog = "update edc_log set "
                + "reversed = '"+ revStatus
                + "' where log_id = "+ logid;
        clientDB.execSQL(updLog);
        String saveStack = "insert or replace into reversal_stack("
                + "elogid, orimessage, revstatus) values "
                + "(" + String.valueOf(logid)
                + ",'" + oriMsg + "',"
                + "'" + revStatus + "')";
        clientDB.execSQL(saveStack);

        if(rc.equals("00")){
            String updlnv = "update holder set invnum = case when invnum = 999999 then 0 else invnum + 1 end ";
            clientDB.execSQL(updlnv);
        }
        clientDB.close();
    }

    public void closeDB() {
        if (clientDB!=null) {
            clientDB.close();
        }
    }

    public Context getCtx() {
        return ctx;
    }

    public String[] getLastRevStatus() {
        String gq = "select elogid, orimessage from reversal_stack where revstatus='P'";
        Cursor gd = clientDB.rawQuery(gq, null);
        String[] rets = new String[3];
        if (gd.moveToFirst()) {
            if (gd!=null) {
                rets[0] = "1";
                rets[1] = gd.getString(gd.getColumnIndex("elogid"));
                rets[2] = gd.getString(gd.getColumnIndex("orimessage"));
            } else {
                rets[0] = "0";
            }
        } else {
            rets[0] = "0";
        }
        return rets;
    }

    public void setIgnoreReplyAmount(Boolean ignoreReplyAmount) {
        this.ignoreReplyAmount = ignoreReplyAmount;
    }
}
