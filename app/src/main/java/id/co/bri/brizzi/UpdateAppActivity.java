package id.co.bri.brizzi;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.customview.HelveticaTextView;
import id.co.bri.brizzi.handler.DownloadSoftware;
import id.co.bri.brizzi.handler.JsonCompHandler;

public class UpdateAppActivity extends AppCompatActivity {
    private HelveticaTextView txtDot, txtConnecting;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message != null) {
                if (message.getData() != null) {
                    String result = message.getData().getString("result");

                    if (result.equals("SUKSES")) {
                        Toast.makeText(UpdateAppActivity.this, "File downloaded " + result, Toast.LENGTH_SHORT).show();
                        File file = new File(DownloadSoftware.FILE_NAME);
                        if (file.exists()) {
                            install();
                        }

                    } else {
                        Toast.makeText(UpdateAppActivity.this, "Download error: " + result, Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int method = getIntent().getIntExtra("method",0);
        setContentView(R.layout.splash_screen);
        DialogInterface.OnClickListener click = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (method){
            case 123://UPDATE MENU
                click = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try {

//                                    final JSONObject json = JsonCompHandler.checkUpdate(UpdateAppActivity.this);

                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String version = pInfo.versionName;
                                    Log.i("ANDROID_VER", version);
//                                    Log.i("JSON_CHECK", json.toString());
                                    if (/**!json.getString("software").equals(version)**/true) {
                                        final ProgressDialog mProgressDialog;
                                        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
                                        mProgressDialog.setMessage("Synchronizing data.\nDon't turn off your device");
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.show();
                                        Thread t1 = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    JsonCompHandler.checkVer(UpdateAppActivity.this.getApplication());
                                                    mProgressDialog.dismiss();
                                                    String ns = Context.NOTIFICATION_SERVICE;
                                                    NotificationManager nMgr = (NotificationManager) UpdateAppActivity.this.getSystemService(ns);
                                                    nMgr.cancel(method);
                                                    JsonCompHandler.fiturSuccess(UpdateAppActivity.this.getApplication());
                                                    Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        t1.start();
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                };

                builder.setTitle("Konfirmasi update fitur");

                break;
            case 1234://Update Software
                click = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                txtConnecting = (HelveticaTextView) findViewById(R.id.txtConnecting);
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try {

                                    final JSONObject json = JsonCompHandler.checkUpdate(UpdateAppActivity.this);

                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String version = pInfo.versionName;
                                    Log.i("ANDROID_VER", version);
                                    Log.i("JSON_CHECK", json.toString());
                                    if (/**!json.getString("software").equals(version)**/true) {
                                        if (new File(DownloadSoftware.FILE_NAME).exists()) {
                                            String hash = StringLib.fileToMD5(DownloadSoftware.FILE_NAME);
                                            Log.i("FILE_HASH", hash);
                                            if (hash.equals(json.getString("hash"))) {
                                                install();
                                            } else {
                                                downloadNewUpdate();
                                            }
                                        } else {
                                            downloadNewUpdate();
                                        }
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                };
                builder.setTitle("Konfirmasi update software");
                break;
            case 1181://Update Setting
                click = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                try {
                                    Context ctx = UpdateAppActivity.this;
                                    if (JsonCompHandler.hasUnsettleData(ctx)) {
                                        AlertDialog.Builder warn = new AlertDialog.Builder(ctx);
                                        warn.setTitle("Gagal");
                                        warn.setMessage("Tidak dapat melakukan update, masih terdapat data transaksi yang belum dilakukan settelement");
                                        warn.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        }).show();
                                    } else {
                                        JsonCompHandler.loadConf(ctx);
                                        Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                } catch (Exception e) {
                                    Log.i("UPDS", "Error " + e.getMessage());
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Intent intent = new Intent(UpdateAppActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                };
                builder.setTitle("Konfirmasi update setting");
                break;
        }
        builder.setMessage("Lakukan update sekarang?");
        if (method==1181) {
            builder.setMessage("Lakukan update sekarang?\n\nPastikan settlement telah dilakukan. Update tidak dapat dilakukan apabila terdapat transaksi yang belum settlement.");
        }
        builder.setPositiveButton("Sekarang", click).setNegativeButton("Tidak Sekarang", click).show();
    }

    private void downloadNewUpdate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
        mProgressDialog.setMessage("Downloading new software.\nDon't turn off your device");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        final DownloadSoftware downloadTask = new DownloadSoftware(UpdateAppActivity.this, mProgressDialog, handler);
        downloadTask.execute();
    }

    public class Update extends AsyncTask<Integer, Void, Boolean> {
        private ProgressDialog pg;

        public Update(ProgressDialog progressDialog) {
            this.pg = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            pg.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (params[0] == 2) {
                try {
                    Log.i("EXEC", "[RUNNING] " + "pm install -r " + DownloadSoftware.FILE_NAME);
                    Runtime.getRuntime().exec("chmod 777 " + DownloadSoftware.FILE_NAME);
                    Process proc = Runtime.getRuntime().exec("pm install -r " + DownloadSoftware.FILE_NAME);
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(proc.getInputStream()));
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(proc.getInputStream()));
                    String line;
                    while ((line = stdError.readLine()) != null) {
                        Log.i("EXEC", line);
                    }
                    while ((line = stdInput.readLine()) != null) {
                        Log.e("EXEC", line);
                    }
                    proc.waitFor();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(DownloadSoftware.FILE_NAME)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
//            isUpdateCompleted();
            super.onPostExecute(aBoolean);
            pg.dismiss();
        }
    }

    private boolean isUpdateCompleted() {
        final ProgressDialog pg = new ProgressDialog(this);
        pg.setTitle("Verifikasi");
        pg.setMessage("Mengirimkan Update Status");
        pg.setIndeterminate(true);
        pg.show();
        final AtomicBoolean b = new AtomicBoolean();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    SharedPreferences preferences = UpdateAppActivity.this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                    String hostname = "http://"+preferences.getString("hostname", CommonConfig.KONFIRM_UPDATE_URL);
                    Log.i("HOSTNAME", hostname);
                    hostname += "/device/";
                    String serialNum = Build.SERIAL;
                    hostname += serialNum;
                    hostname += "/updateVer?ver=";
                    PackageInfo pInfo = null;
                    try {
                        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String version = pInfo.versionName;
                    hostname += version;
                    url = new URL(hostname);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(2 * 250);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    boolean tt = connection.getResponseCode() == HttpURLConnection.HTTP_OK;
                    b.set(tt);
                    pg.dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                    b.set(false);
                    pg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    b.set(false);
                    pg.dismiss();
                }
            }
        });
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return b.get();
    }

    private void install() {
        ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
        mProgressDialog.setMessage("Installing update.\nDon't turn off your device");
        mProgressDialog.setIndeterminate(true);
        Update syncMenu = new Update(mProgressDialog);
        syncMenu.execute(2);
    }
}
