package id.co.bri.brizzi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.handler.GPSService;
import id.co.bri.brizzi.handler.MenuListResolver;
import id.co.bri.brizzi.handler.WebSocketClient;

public class SocketService extends Service implements WebSocketClient.Listener {
    private static final String TAG = "WEBSOCKET";
    private IBinder mBinder;
    private boolean isConnect = false;
    private int retryConnect = 0;
    private boolean isLogin = false;
    private WebSocketClient client;
    private boolean ifForm = false;
    NotificationManager mNotificationManager;
    List<BasicNameValuePair> extraHeaders = new ArrayList<>();
    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private String serialNum = Build.SERIAL;
    private boolean DEBUG_MODE = CommonConfig.DEBUG_MODE;
    private static MenuListResolver mlr = new MenuListResolver();
    public enum MessageMethod {
        LOGIN, LOGOUT, UPDATE_SOFTWARE, UPDATE_MENU,UPDATE_SETTINGS, MESSAGE, HEARTBEAT, PENDING_MESSAGE, TEST_CONNECTION;
    }

    public enum MessageType {
        TOAST, NOTIFICATION, NOTIFICATION_STICKY, NOTIFICATION_TOAST, NOTIFICATION_STICKY_TOAST, TEST_CONNECTION;
    }

    public enum MessageStatus {
        DELIVERED, FAILED, DEVICE_NOT_ACTIVE, DEVICE_NOT_REGISTERED, DEVICE_REGISTERED, SEND, LOGIN_SUCCESS, LOGIN_FAILED, TEST_CONNECTION;
    }

    public void clientConnect(){
        client.connect();
    }

    private void reConnect() {
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        if (!isConnect) {
                            if (retryConnect>10) {
                                retryConnect = 0;
                            }
                            retryConnect++;
                            Log.d(TAG, "Retry connect #" + retryConnect);
                            SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                            client = new WebSocketClient(URI.create("ws://" + preferences.getString("hostname",CommonConfig.WEBSOCKET_URL) + "/push"), SocketService.this, extraHeaders);
                            client.connect();
                        }
                        if (!isConnect) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("type", "NOTIFICATION_TOAST");
                                jsonObject.put("message", "Tidak terkoneksi dengan jaringan BRI\nSilahkan hubungi administrator");
                                showNotification(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 0, 180, TimeUnit.SECONDS);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();
        SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        if (!preferences.contains("registered")) {
            preferences.edit().putBoolean("registered", false).apply();
        }
//        Log.i("SoSrv", "Starting socket service");
        if(!DEBUG_MODE){
            try {
//                client = null;
                client = new WebSocketClient(URI.create("ws://" + preferences.getString("hostname", CommonConfig.WEBSOCKET_URL) + "/push"),
                        this, extraHeaders);
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                if (!isConnect) {
                    client.connect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//        Log.d(TAG, "WebSocket instance created");
        }
        try {
            final Thread gpsService = new Thread(new GPSService(this));
            gpsService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "GPS Service started");
    }

    @Override
    public IBinder onBind(Intent intent) {
        SharedPreferences preferences = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        DEBUG_MODE = preferences.getBoolean("debug_mode",DEBUG_MODE);
        if(!DEBUG_MODE) {
            if (client==null) {
                client = new WebSocketClient(URI.create("ws://" + preferences.getString("hostname", CommonConfig.WEBSOCKET_URL) + "/push"),
                        this, extraHeaders);
            }
            if (!isConnect) {
                client.connect();
            }
        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SocketService getServerInstance() {
            return SocketService.this;
        }
    }


    @Override
    public void onConnect() {
        Log.d(TAG, "WEBSOCKET CONNECTED");
        isConnect = true;
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            retryConnect = 0;
        }
        doLogin();
    }

    private void doLogin() {
        if (!isLogin) {
            JSONObject object = new JSONObject();
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;
            String[] loc = null;
            try {
                FileInputStream fis = this.openFileInput("loc.txt");
                StringBuilder fileContent = new StringBuilder("");

                byte[] buffer = new byte[1024];
                int n = 0;
                while ((n = fis.read(buffer)) != -1)
                {
                    fileContent.append(new String(buffer, 0, n));
                }
                loc = fileContent.toString().split(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                object.put("snDevice", serialNum);
                object.put("method", MessageMethod.LOGIN.name());
                object.put("timestamp", new Date().getTime());
                object.put("status", MessageStatus.SEND);
                JSONObject addInfo = new JSONObject();

                if(loc != null){
                    JSONObject location = new JSONObject();
                    location.put("lat",loc[1]);
                    location.put("lon",loc[0]);
                    addInfo.put("location",location);
                }

                addInfo.put("version", version);
                object.put("additionalInfo", addInfo);
                Log.d("SEND_LOGIN", object.toString());
                client.send(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, message);
        try {
            JSONObject response = new JSONObject(message);
            MessageMethod method = MessageMethod.valueOf(response.getString("method"));
            MessageStatus status = MessageStatus.valueOf(response.getString("status"));
            String m = response.getString("message");
            boolean isNeedUpdateMenu = false;
            boolean isNeedUpdateSoftware = false;
            switch (method) {
                case LOGIN:
                    if (status == MessageStatus.LOGIN_SUCCESS) {
                        isLogin = true;
                        SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
//                        boolean regstate = preferencesSetting.getBoolean("registered", true);
                        preferencesSetting.edit().putBoolean("registered", true).apply();
//                        if (!regstate){
//                                reloadApp();
////                                deleteCache(this);
//                            }
                    } else if (status == MessageStatus.LOGIN_FAILED) {
                        isLogin = false;
                        showNotification(response);
                        String rspmsg = response.getString("message");
                        SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                        boolean regstate = preferencesSetting.getBoolean("registered", false);
                        if (rspmsg.startsWith("EDC tidak terdaftar")) {
                            preferencesSetting.edit().putBoolean("registered", false).apply();
                            if (!regstate) {
//                                reloadApp();
                                Log.d(TAG, "Reload Success");
//                                deleteCache(this);
                            }
                        }
                    }
                    else if (status == MessageStatus.DEVICE_REGISTERED) {
                        isLogin = true;
                        showNotification(response);
                        SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
                        preferencesSetting.edit().putBoolean("registered", true).apply();
                        client.connect();
                        Log.d(TAG, "Reload Success");
                    }
                    break;
                case MESSAGE:
                    showNotification(response);
                    break;
                case UPDATE_SOFTWARE:
                    isNeedUpdateSoftware = true;
                    break;
                case UPDATE_MENU:
                    isNeedUpdateMenu = true;
                    break;
                case HEARTBEAT:
                    if (isConnect) {
//                        client.send();
                    } else {
                        client.connect();
                        doLogin();
                    }
                    break;
                case TEST_CONNECTION:
                    if (isConnect) {
                        JSONObject object = new JSONObject();
                        object.put("snDevice", serialNum);
                        object.put("method", MessageMethod.TEST_CONNECTION.name());
                        object.put("timestamp", new Date().getTime());
                        object.put("status", MessageStatus.SEND);
                        JSONObject addInfo = new JSONObject();
                        Log.d("TEST_CON", object.toString());
                        client.send(object.toString());
                    } else {
                        client.connect();
                        doLogin();
                    }

                    break;
                case LOGOUT:
                    break;
                case UPDATE_SETTINGS:
                    JSONObject json = response.getJSONObject("additionalInfo");
                    updateSettings(json);
                    break;
            }

            if (isNeedUpdateMenu) {
                if (mlr.hasUnsettledData(this)) {
                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(this, UpdateAppActivity.class);
                    intent.putExtra("method", 1182);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(this)
                            .setContentTitle("Informasi")
                            .setContentIntent(pendingIntent)
                            .setContentText("Update Fitur EDC telah tersedia, silahkan lakukan settlement sebelum memasangkan update")
                            .setSmallIcon(R.drawable.logo_bri_002)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.if_email));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(1182, n);
                } else {

                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(this, UpdateAppActivity.class);
                    intent.putExtra("method", 123);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(this)
                            .setContentTitle("Update Fitur")
                            .setContentIntent(pendingIntent)
                            .setContentText("Update Fitur telah tersedia")
                            .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_autorenew_white_24dp));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(123, n);
                }
            }

            if (isNeedUpdateSoftware) {
                if (mlr.hasUnsettledData(this)) {
                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(this, UpdateAppActivity.class);
                    intent.putExtra("method", 1183);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(this)
                            .setContentTitle("Informasi")
                            .setContentIntent(pendingIntent)
                            .setContentText(m)
                            .setSmallIcon(R.drawable.logo_bri_002)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.if_email));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(1183, n);
                } else {

                    updateNotif(m, method);

//                    if (mNotificationManager == null) {
//                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                    }
//                    Intent intent = new Intent(this, UpdateAppActivity.class);
//                    intent.putExtra("method", 1234);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    Notification.Builder builder = new Notification.Builder(this)
//                            .setContentTitle("Update Software")
//                            .setContentIntent(pendingIntent)
//                            .setContentText("Update Software telah tersedia")
//                            .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
//                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_autorenew_white_24dp));
//                    Notification n;
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        n = builder.build();
//                    } else {
//                        n = builder.getNotification();
//                    }
//                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//                    mNotificationManager.notify(1234, n);
                }
            }

        } catch (JSONException e) {
//e.printStackTrace();
        }

    }

    private void updateSettings(JSONObject json) {
        SharedPreferences preferencesConfig = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferencesConfig.getString("hostname", CommonConfig.HTTP_REST_URL);
        try {
            Log.i("conf",json.toString(2));
            //checkIsNeedUpdate
            SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            SharedPreferences preferences = preferencesSetting;
            String ip = preferences.getString("ip",CommonConfig.DEV_SOCKET_IP);
            String port = preferences.getString("port",CommonConfig.DEV_SOCKET_PORT);
            String initScreen = preferences.getString("init_screen",CommonConfig.INIT_REST_ACT);
            String diskonId = preferences.getString("diskon_id",CommonConfig.DEFAULT_DISCOUNT_TYPE);
            String diskon = preferences.getString("diskon",CommonConfig.DEFAULT_DISCOUNT_RATE);
            String terminalId = preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID);
            String merchantId = preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID);
            String merchantName = preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME);
            String merchantAddr1 = preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1);
            String merchantAddr2 = preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2);
            String passSettlement = preferences.getString("pass_settlement", CommonConfig.DEFAULT_SETTLEMENT_PASS);
            String passSettings = preferences.getString("pass_settings", CommonConfig.PASS_SETTINGS);
            String minDeduct = preferences.getString("minimum_deduct", CommonConfig.DEFAULT_MIN_BALANCE_BRIZZI);
            String maxDeduct = preferences.getString("maximum_deduct", CommonConfig.DEFAULT_MAX_MONTHLY_DEDUCT);
            boolean isNeedUpdate = false;
            if (!ip.equals(json.getString("ip"))) {
                isNeedUpdate = true;
            }
            if (!port.equals(json.getString("port"))) {
                isNeedUpdate = true;
            }
            if (!diskonId.equals(json.getString("diskon_id"))) {
                isNeedUpdate = true;
            }
            if (!diskon.equals(json.getString("diskon"))) {
                isNeedUpdate = true;
            }
            if (!terminalId.equals(json.getString("terminalid"))) {
                isNeedUpdate = true;
            }
            if (!merchantId.equals(json.getString("merchantid"))) {
                isNeedUpdate = true;
            }
            if (!merchantName.equals(json.getString("merchantname"))) {
                isNeedUpdate = true;
            }
            if (!merchantAddr1.equals(json.getString("alamat"))) {
                isNeedUpdate = true;
            }
            if (!merchantAddr2.equals(json.getString("alamat_2"))) {
                isNeedUpdate = true;
            }
            if (!passSettlement.equals(json.getString("pass_settlement"))) {
                isNeedUpdate = true;
            }
            if (!passSettings.equals(json.getString("pass_settings"))) {
                isNeedUpdate = true;
            }
            if (!minDeduct.equals(json.getString("minimum_deduct"))) {
                isNeedUpdate = true;
            }
            if (!maxDeduct.equals(json.getString("maximum_deduct"))) {
                isNeedUpdate = true;
            }

//            if(!isNeedUpdate){
//                mNotificationManager.cancel(1181);
//                settingSuccess();
//            }

            if (isNeedUpdate || !isNeedUpdate) {
                if (mlr.hasUnsettledData(this)) {
                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(this, UpdateAppActivity.class);
                    intent.putExtra("method", 1181);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(this)
                            .setContentTitle("Informasi")
                            .setContentIntent(pendingIntent)
                            .setContentText("Update setting EDC telah tersedia, silahkan lakukan settlement sebelum memasangkan update")
                            .setSmallIcon(R.drawable.logo_bri_002)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.if_email));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(1181, n);
                } else {

                    // 15/03/2018
//                    preferencesSetting.edit().putString("merchant_name", json.getString("merchantname")).apply();
//                    preferencesSetting.edit().putString("merchant_address1", json.getString("alamat")).apply();
//                    preferencesSetting.edit().putString("merchant_address2", json.getString("alamat_2")).apply();
//                    preferencesSetting.edit().putString("terminal_id", json.getString("terminalid")).apply();
//                    preferencesSetting.edit().putString("merchant_id", json.getString("merchantid")).apply();
//                    preferencesSetting.edit().putString("init_phone", json.getString("phoneno")).apply();
//                    preferencesSetting.edit().putString("primary_phone", json.getString("primaryphone")).apply();
//                    preferencesSetting.edit().putString("secondary_phone", json.getString("secondaryphone")).apply();
//                    preferencesSetting.edit().putString("master", json.getString("master")).apply();
//                    preferencesSetting.edit().putString("ip", json.getString("ip")).apply();
//                    preferencesSetting.edit().putString("diskon_id", json.getString("diskon_id")).apply();
//                    preferencesSetting.edit().putString("diskon", json.getString("diskon")).apply();
//                    preferencesSetting.edit().putString("pass_settlement", json.getString("pass_settlement")).apply();
//                    preferencesSetting.edit().putString("minimum_deduct", json.getString("minimum_deduct")).apply();
//                    preferencesSetting.edit().putString("maximum_deduct", json.getString("maximum_deduct")).apply();
//                    preferencesSetting.edit().putString("port", json.getString("port")).apply();
//                    settingSuccess();
                    //reloadApp();

                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(this, UpdateAppActivity.class);
                    intent.putExtra("method", 9876);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(this)
                            .setContentTitle("Update Setting")
                            .setContentIntent(pendingIntent)
                            .setContentText("Setting EDC telah tersedia")
                            .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_autorenew_white_24dp));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(9876, n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
            Log.i("Clear Cache", context.toString());
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void reloadApp() {
        System.exit(1);
    }

//        Intent i = getBaseContext().getPackageManager()
//                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);

//        startActivity(new Intent(this, SplashScreen.class));


    public void settingSuccess() throws IOException, JSONException {
        SharedPreferences preferences = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
//        Log.d("SN_DEVICE",serialNum);
        URL url = new URL("http://" + hostname + "/device/" + serialNum + "/syncConfSuccess");
        url.openStream();
    }

    private void updateSettingsEx(JSONObject json) throws JSONException {
        SharedPreferences preferencesSetting = getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        preferencesSetting.edit().putString("merchant_name", json.getString("merchantname")).apply();
        preferencesSetting.edit().putString("merchant_address1", json.getString("alamat")).apply();
        preferencesSetting.edit().putString("merchant_address2", json.getString("alamat_2")).apply();
        preferencesSetting.edit().putString("terminal_id", json.getString("terminalid")).apply();
        preferencesSetting.edit().putString("merchant_id", json.getString("merchantid")).apply();
        preferencesSetting.edit().putString("init_phone", json.getString("phoneno")).apply();
        preferencesSetting.edit().putString("primary_phone", json.getString("primaryphone")).apply();
        preferencesSetting.edit().putString("secondary_phone", json.getString("secondaryphone")).apply();
        preferencesSetting.edit().putString("master", json.getString("master")).apply();
        preferencesSetting.edit().putString("ip", json.getString("ip")).apply();
        preferencesSetting.edit().putString("diskon_id", json.getString("diskon_id")).apply();
        preferencesSetting.edit().putString("diskon", json.getString("diskon")).apply();
        preferencesSetting.edit().putString("pass_settlement", json.getString("pass_settlement")).apply();
        preferencesSetting.edit().putString("pass_settings", json.getString("pass_settings")).apply();
        preferencesSetting.edit().putString("minimum_deduct", json.getString("minimum_deduct")).apply();
        preferencesSetting.edit().putString("maximum_deduct", json.getString("maximum_deduct")).apply();
        preferencesSetting.edit().putString("port", json.getString("port")).apply();
        Log.i("UPDATE","UPDATE SUCCESSFULLY");
    }

    @Override
    public void onMessage(byte[] data) {

    }

    @Override
    public void onDisconnect(int code, String reason) {
//        Log.d(TAG, reason);
        isConnect = false;
    }

    @Override
    public void onError(Exception error) {
        String reason = error.getMessage();
        Log.e(TAG + " ERROR", reason);
        if (reason.contains("Connection timed out") ||
                reason.contains("Connection reset by peer") ||
                reason.contains("Network is unreachable")) {
            isConnect = false;
            isLogin = false;
            scheduler = Executors.newSingleThreadScheduledExecutor();
            if (retryConnect == 0) {
                reConnect();
            }
            if (reason.contains("Network is unreachable")) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "NOTIFICATION_TOAST");
                    jsonObject.put("message", "Tidak terkoneksi dengan jaringan BRI\nSilahkan hubungi administrator");
                    showNotification(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            if (reason.startsWith("recvfrom")){
//                client.connect();
//            }
        }
    }

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(SocketService.this.getApplicationContext(), msg.getData().getString("message"), Toast.LENGTH_LONG).show();
        }
    };

    private void showNotification(JSONObject message) {
        try {
            MessageType type = MessageType.valueOf(message.getString("type"));
            if (mNotificationManager == null) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("Informasi")
                    .setContentText(message.getString("message"))
                    .setSmallIcon(R.drawable.logo_bri_002)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.if_email));
            Notification n;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                n = builder.build();
            } else {
                n = builder.getNotification();
            }

            Message data = new Message();
            Bundle bundle = new Bundle();
            switch (type) {
                case NOTIFICATION_STICKY:
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(12345, n);
                    break;
                case NOTIFICATION_TOAST:
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mNotificationManager.cancel(12345);
                    mHandler.sendMessage(data);
                    mNotificationManager.notify(12345, n);
                    break;
                case TOAST:
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mHandler.sendMessage(data);
                    break;
                case NOTIFICATION_STICKY_TOAST:
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(12345, n);
                    bundle.putString("message", message.getString("message"));
                    data.setData(bundle);
                    mHandler.sendMessage(data);
                    break;
                case NOTIFICATION:
                    mNotificationManager.notify(12345, n);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateNotif(String message, MessageMethod method) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        Intent intent = new Intent(this, UpdateAppActivity.class);
        int what = 0;
        String title = "Update Software";
        if (method == MessageMethod.UPDATE_MENU) {
            what = 123;
            title = "Update Fitur";
        } else if (method == MessageMethod.UPDATE_SOFTWARE) {
            what = 1234;
            title = "Update Software";
        }
        else if (method == MessageMethod.UPDATE_SETTINGS) {
            what = 9876;
            title = "Update Settings";
        }
        intent.putExtra("method", what);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_autorenew_white_24dp));
        Notification n;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = new Notification.BigTextStyle(builder).bigText(message).build();
        } else {
            n = builder.getNotification();
        }
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(what, n);
    }

    public void setIfForm() {
        this.ifForm = true;
    }

    public void setIfNotForm() {
        this.ifForm = false;
    }

    public boolean getIfForm() {
        return ifForm;
    }
}
