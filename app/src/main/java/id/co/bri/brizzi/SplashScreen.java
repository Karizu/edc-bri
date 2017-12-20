package id.co.bri.brizzi;

import android.app.Activity;
import android.app.NotificationManager;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.customview.HelveticaTextView;
import id.co.bri.brizzi.handler.DataBaseHelper;
import id.co.bri.brizzi.handler.DownloadSoftware;
import id.co.bri.brizzi.handler.FCHandler;
import id.co.bri.brizzi.handler.JsonCompHandler;

/**
 * Created by indra on 24/11/15.
 */
public class SplashScreen extends Activity {

    private HelveticaTextView txtConnecting;
    private boolean DEBUG_MODE = CommonConfig.DEBUG_MODE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new FCHandler(this));
        SharedPreferences preferences = SplashScreen.this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
//        Log.d("DEVICE", CommonConfig.getDeviceName());
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        Log.i("OP", "S-Pay Application");
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        String version = pInfo.versionName;


        Log.i("OP", "Version : " + version);
        Log.i("OP", "");
        Log.i("OP", "©GTA 2016");
        Log.i("OP", "---------------------------");
        Log.i("OP", "S/N : " + Build.SERIAL);
        Log.i("OP", "Application start");
        setContentView(R.layout.splash_screen);
        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean("installed", false)) {
            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean("installed", true).commit();
            if (!DEBUG_MODE) {
                copyAssetFolder(getAssets(), "files",
                        "/data/data/"+getPackageName()+"/files");
            }
        }
//        Log.i("OP", "Preparing data");
        txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
//        Log.i("OP", "Set thread");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        Log.i("OP", "Prepare DB");
        DataBaseHelper helperDb = new DataBaseHelper(this.getApplicationContext());
        try {
            helperDb.createDataBase();
        } catch (IOException e) {
            Log.e("DB", "Cannot access database : " + e.getMessage());
//            e.printStackTrace();
        } finally {
            helperDb.close();
            helperDb = null;
        }
        if (true) {
//            Log.i("Op", "Starting service : Socket");
            startService(new Intent(this,SocketService.class));
            recheck();
        } else {
            Intent intent = new Intent(SplashScreen.this, TestActivity.class);
            startActivity(intent);
        }
    }

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void recheck() {
        if (DEBUG_MODE) {
            txtConnecting.setText("DEBUG MODE : Init from local DB");
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = SplashScreen.this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                    //inject init preferences values
                    String ip = preferences.getString("ip",CommonConfig.DEV_SOCKET_IP);
                    String port = preferences.getString("port",CommonConfig.DEV_SOCKET_PORT);
                    String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
                    String initScreen = preferences.getString("init_screen",CommonConfig.INIT_REST_ACT);
                    String diskonId = preferences.getString("diskon_id",CommonConfig.DEFAULT_DISCOUNT_TYPE);
                    String diskon = preferences.getString("diskon",CommonConfig.DEFAULT_DISCOUNT_RATE);
                    String terminalId = preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID);
                    String merchantId = preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID);
                    String merchantName = preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME);
                    String merchantAddr1 = preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1);
                    String merchantAddr2 = preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2);
                    String passSettlement = preferences.getString("pass_settlement", CommonConfig.DEFAULT_SETTLEMENT_PASS);
                    String minDeduct = preferences.getString("minimum_deduct", CommonConfig.DEFAULT_MIN_BALANCE_BRIZZI);
                    String maxDeduct = preferences.getString("maximum_deduct", CommonConfig.DEFAULT_MAX_MONTHLY_DEDUCT);
                    preferences.edit().putString("ip",ip).apply();
                    preferences.edit().putString("port",port).apply();
                    preferences.edit().putString("hostname", hostname).apply();
                    preferences.edit().putString("init_screen",initScreen).apply();
                    preferences.edit().putString("diskon_id",diskonId).apply();
                    preferences.edit().putString("diskon",diskon).apply();
                    preferences.edit().putString("terminal_id",terminalId).apply();
                    preferences.edit().putString("merchant_id",merchantId).apply();
                    preferences.edit().putString("merchant_name",merchantName).apply();
                    preferences.edit().putString("merchant_address1",merchantAddr1).apply();
                    preferences.edit().putString("merchant_address2",merchantAddr2).apply();
                    preferences.edit().putString("pass_settlement", passSettlement).apply();
                    preferences.edit().putString("minimum_deduct", minDeduct).apply();
                    preferences.edit().putString("maximum_deduct", maxDeduct).apply();
                    //end of inject
                    try {
                        JsonCompHandler.readJson(SplashScreen.this, preferences.getString("init_screen", "0000000"));
//                        Log.d("Data check", "Ready to go");
                    } catch (IOException e) {
//                        Log.d("Data check", "Not Initialized");
                        txtConnecting.setText("DEBUG MODE : Init from local DB");
                        try {
                            JsonCompHandler.checkVerNoTms(SplashScreen.this.getApplication());
                        } catch (Exception ex) {
                            Log.e("INIT", "FAILED");
                        }
                    } catch (Exception e) {
//                        Log.d("Data check", "Not Initialized");
                        txtConnecting.setText("DEBUG MODE : Init from local DB");
                        try {
                            JsonCompHandler.checkVerNoTms(SplashScreen.this.getApplication());
                        } catch (Exception ex) {
                            Log.e("INIT", "FAILED");
                        }
                    } finally {
                        UpdateDatabase updateDatabase = new UpdateDatabase(SplashScreen.this);
                        boolean jsonRebuild = false;
                        jsonRebuild = updateDatabase.doUpdate();
                        if (jsonRebuild) {
//                            txtConnecting.setText("DEBUG MODE : Rebuild from local DB");
                            try {
                                Log.d("INIT", "REBUILD");
                                JsonCompHandler.jsonRebuild(SplashScreen.this.getApplication());
                                Log.d("INIT", "REBUILD DONE");
                            } catch (Exception ex) {
                                Log.e("INIT", "FAILED");
                            }
                        }
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
            thread.start();
        } else {
//        }
//            PackageInfo pInfo = null;
            try {
//                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                JsonCompHandler.loadConf(this);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }
    }

}