package id.co.bri.brizzi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.handler.DataBaseHelper;

/**
 * Created by Ahmad on 10/12/2016.
 */
public class UpdateDatabase {
    private Activity context;
    private DataBaseHelper helper;
    private SQLiteDatabase sqLiteDatabase;
    private boolean status=false;

    public UpdateDatabase(Activity context) {
        this.context=context;
    }

    public boolean doUpdate() {
        try {
            //get data from db
            helper = new DataBaseHelper(context);
            sqLiteDatabase = helper.getActiveDatabase();
            Cursor dbVersionRow = sqLiteDatabase.rawQuery("select * from responsecode where response_uid='ver'", null);
            String dbVersion = "0.1b";
            int dbBuild = 0;
            boolean initver = false;
            if (dbVersionRow.moveToFirst()) {
                dbVersion = dbVersionRow.getString(dbVersionRow.getColumnIndex("ina_msg"));
                dbBuild = Integer.parseInt(dbVersionRow.getString(dbVersionRow.getColumnIndex("eng_msg")));
            } else {
                //beta ver, use predefined value
                //pre-versioning
                initver = true;
            }
            dbVersionRow.close();
            if (initver) {
                sqLiteDatabase.execSQL("insert into responsecode (service_id, response_uid, ina_msg, eng_msg) " +
                        "values ('0', 'ver', '0.1b', '0')");
            }
            //get data from file
            InputStream inputStream = context.getAssets().open(CommonConfig.DB_FILE_UPDATE_NAME);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            String fileVersion = "";
            if ((line = bufferedReader.readLine()) != null) {
                fileVersion = line;
            }
            int maxLineBuild = 0;
            int rowId=1;
            while ((line = bufferedReader.readLine()) != null) {
                int lineBuild = Integer.parseInt(line.substring(0,line.indexOf(":")));
                if (lineBuild>dbBuild) {
                    sqLiteDatabase.execSQL(line.substring(line.indexOf(":")+1));
                    System.out.println("Update query v." +lineBuild+ " #" + rowId);
                    if (line.contains("screen")||line.contains("component")) {
                        status = true;
                    }
                    rowId++;
                } else {
                    System.out.println("Update skipped v." +lineBuild+ " #" + rowId);
                    rowId++;
                }
                if (lineBuild>maxLineBuild) {
                    maxLineBuild = lineBuild;
                }
            }
            sqLiteDatabase.execSQL("update responsecode set ina_msg = '" + fileVersion + "', eng_msg = '"+maxLineBuild+"' where " +
                    "service_id = '0' and response_uid = 'ver'");
        } catch (Exception e) {
            e.printStackTrace();
            status=false;
        } finally {
            if (sqLiteDatabase!=null) {
                sqLiteDatabase.close();
            }
        }
        return status;
    }
}
