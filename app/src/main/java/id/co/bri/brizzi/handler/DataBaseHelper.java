/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.handler;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Ahmad
 */
public class DataBaseHelper  extends SQLiteOpenHelper{
    private static String DB_PATH = "/data/data/id.co.bri.brizzi/databases/";
    private static String DB_NAME = "client.db";
    private SQLiteDatabase liteDb;
    private Context myContext;
    
    
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }
    
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
//            Log.e("Test", "Database already exists");
        } else {
            this.getReadableDatabase();
            try {
//                Log.d("Test", "DB Not exist, copy from source");
                copyDatabase();
//                Log.d("Test", "Database created");
            } catch (IOException e) {
                Log.e("Test", "Error copying database");
                Log.e("Test", e.toString());
                throw new Error("Error copying database");
            }
        }
    }
    
    public SQLiteDatabase getActiveDatabase() throws Exception {
        if (liteDb==null) {
            openDataBase();
        }
        return liteDb;
    }
    
    private boolean checkDataBase() {
//        Log.d("Test", "Check DB Status");
        try {
            String myPath = DB_PATH + DB_NAME;
            liteDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
//            Log.d("Test", "Check status : DB Exists");
            liteDb.close();
            return true;
        } catch (Exception e) {
            Log.e("Test", "Check status : DB not Exists");
            return false;
        }
    }
    
    private void copyDatabase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outputFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outputFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length=myInput.read(buffer))>0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
//        Log.d("Test", "Copy DB done");
    }
    
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        liteDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
    
    @Override
    public synchronized void close() {
        if (liteDb!=null) {
            liteDb.close();
            liteDb = null;
        }
        super.close();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
