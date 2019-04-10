package id.co.bri.brizzi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.wizarpos.jni.PINPadInterface;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.handler.DataBaseHelper;
import id.co.bri.brizzi.handler.FCHandler;
import id.co.bri.brizzi.module.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminActivity extends Activity implements View.OnClickListener {
    private EditText txtIp,txtPort,txtHostname, txtInitScreen,txtDiskon,txtTerminalId,txtMerchantId,
            txtMerchantName,txtMerchantAddrees1,txtMerchantAddrees2,txtPassSettlement,
            txtMinDeduct,txtMaxDeduct;
    private android.widget.TextView txtConnectedWifi;
    private CheckBox debugMode;
    private Button scanWifiButton, apnSettingsButton, openSetting;
    private Activity context;
    private RelativeLayout wifiLayout;
    private Switch wifiSwitch;
    private Spinner spinnerDiskonId;
    private SharedPreferences preferences;
    private DataBaseHelper helperDb;
    private SQLiteDatabase clientDB = null;
    private WifiManager mWifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private BroadcastReceiver wifiReceiver;
    private String wifiSSID;
    private WifiConfiguration conf;
    private boolean isWantToConnectWEP;
    private boolean isWantToConnectWPA;
    private boolean isWantToScanWiFi;
    private boolean showingWiFiListDialog;
    private List<ScanResult> scanResults;
    private WifiAdapter wifiArrayAdapter;

    private final int REQUEST_WRITE_APN_SETTINGS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new FCHandler(this));


        setContentView(R.layout.activity_admin);
        debugMode = (CheckBox) findViewById(R.id.debug_mode);
        txtIp = (EditText) findViewById(R.id.txtIp);
        txtPort = (EditText) findViewById(R.id.txtPort);
        txtHostname = (EditText) findViewById(R.id.txtHostname);
        txtInitScreen = (EditText) findViewById(R.id.txtInitScreen);
        txtTerminalId = (EditText) findViewById(R.id.txtTerminalId);
        txtMerchantId = (EditText) findViewById(R.id.txtMerchantId);
        txtMerchantName = (EditText) findViewById(R.id.txtMerchantName);
        txtMerchantAddrees1 = (EditText) findViewById(R.id.txtMerchantAddress1);
        txtMerchantAddrees2 = (EditText) findViewById(R.id.txtMerchantAddress2);
        spinnerDiskonId = (Spinner) findViewById(R.id.spinnerDiskonId);
        txtDiskon = (EditText) findViewById(R.id.txtDiskon);
        txtPassSettlement = (EditText) findViewById(R.id.passSettlement);
        txtMinDeduct = (EditText) findViewById(R.id.txtMinDeduct);
        txtMaxDeduct = (EditText) findViewById(R.id.txtMaxDeduct);


        scanWifiButton = (Button) findViewById(R.id.scanWifiButton);
        openSetting = (Button) findViewById(R.id.openSetting);

//        apnSettingsButton = (Button) findViewById(R.id.btnApnSettings);
//        apnSettingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int permissionCheck = ContextCompat.checkSelfPermission(AdminActivity.this, android.Manifest.permission.WRITE_APN_SETTINGS);
//                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                    //requesting permission
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(AdminActivity.this,
//                            android.Manifest.permission.WRITE_APN_SETTINGS)) {
//                        showExplanation("Permission Needed", "Rationale", android.Manifest.permission.WRITE_APN_SETTINGS, REQUEST_WRITE_APN_SETTINGS);
//                    } else {
//                        requestPermission(android.Manifest.permission.WRITE_APN_SETTINGS, REQUEST_WRITE_APN_SETTINGS);
//                    }
//                } else {
//                    //permission is granted and you can change APN settings
//                    Intent i = new Intent(AdminActivity.this, ApnSettingsActivity.class);
//                    startActivity(i);
//                }
//
//            }
//        });

        wifiLayout = (RelativeLayout) findViewById(R.id.wifiLayout);
        wifiSwitch = (Switch) findViewById(R.id.wifiSwitch);
        txtConnectedWifi = (android.widget.TextView) findViewById(R.id.tvWifiSSID);

        Button btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(this);
        btnSimpan.setTag(0);
        Button btnMKey = (Button) findViewById(R.id.btnMKey);
        btnMKey.setOnClickListener(this);
        btnMKey.setTag(1);
        helperDb = new DataBaseHelper(this.getApplicationContext());
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            Toast.makeText(this,"Could not open key holder",Toast.LENGTH_LONG).show();
            btnMKey.setClickable(false);
        }


        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                mWifiManager.setWifiEnabled(isChecked);
                updateWifiStatus(isChecked);
            }
        });

        wifiReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent intent){
                if(mWifiManager != null) {
                    if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals (intent.getAction())) {
                        NetworkInfo netInfo = intent.getParcelableExtra (WifiManager.EXTRA_NETWORK_INFO);
                        if (ConnectivityManager.TYPE_WIFI == netInfo.getType ()) {
                            updateWifiStatus(wifiSwitch.isChecked());
                        }
                    }
                }
            }
        };
        registerReceiver(wifiReceiver,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        wifiScanReceiver = new BroadcastReceiver(){
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context c, Intent intent){
                if(mWifiManager != null && isWantToScanWiFi) {
                    if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals (intent.getAction())) {
                        scanResults = mWifiManager.getScanResults();
                        if (!showingWiFiListDialog){
                            showWifiListDialog();
                        }
                        else{
                            wifiArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        };

        wifiSwitch.setChecked(getWifiAdapterStat());
        updateWifiStatus(wifiSwitch.isChecked());



        scanWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiStartScan();
            }
        });

        openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                launchCall();
                callLoginDialog();
            }
        });

        preferences  = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
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
        boolean isDebug = preferences.getBoolean("debug_mode",CommonConfig.DEBUG_MODE);
        txtIp.setText(ip);
        txtPort.setText(port);
        txtHostname.setText(hostname);
        txtInitScreen.setText(initScreen);
        txtTerminalId.setText(terminalId);
        txtMerchantId.setText(merchantId);
        txtMerchantName.setText(merchantName);
        txtMerchantAddrees1.setText(merchantAddr1);
        txtMerchantAddrees2.setText(merchantAddr2);
        txtPassSettlement.setText(passSettlement);
        txtMinDeduct.setText(minDeduct);
        txtMaxDeduct.setText(maxDeduct);
        debugMode.setChecked(isDebug);
        List<String> list = new ArrayList<String>();
	list.add("Rupiah");
	list.add("Persen");
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spinnerDiskonId.setAdapter(dataAdapter);
        if (diskonId.equals("Rupiah")) {
            spinnerDiskonId.setSelection(0);
        } else {
            spinnerDiskonId.setSelection(1);
        }
        txtDiskon.setText(diskon);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_APN_SETTINGS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(AdminActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(AdminActivity.this, ApnSettingsActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(AdminActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void callLoginDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_login);
        dialog.setTitle("Login");

        // get the Refferences of views
        final EditText editTextUserName = (EditText) dialog.findViewById(R.id.editTextUserNameToLogin);
        final EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextPasswordToLogin);
        android.widget.Button btnSignIn = (android.widget.Button) dialog.findViewById(R.id.buttonSignIn);

        // Set On ClickListener
        btnSignIn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                // get The User name and Password
                String userName = editTextUserName.getText().toString();
                String password = editTextPassword.getText().toString();
                String stored = preferences.getString("pass_settings", CommonConfig.PASS_SETTINGS);

                // fetch the Password form database for respective user name


                // check if the Stored password matches with  Password entered by user
                if (password.equals(stored) && userName.equals(CommonConfig.USERNAME_ADMIN)) {
                    Toast.makeText(getApplicationContext(), "Login Successfull", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                } else {
                    Toast.makeText(getApplicationContext(), "User Name and Does Not Matches", Toast.LENGTH_LONG).show();
                }

            }
        });


        dialog.show();
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //premission granted by user
//                    Intent i = new Intent(AdminActivity.this, ApnSettingsActivity.class);
//                    startActivity(i);
//                } else {
//
//                    //permission denied by user
//                }
//            }
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    @Override
    public void onClick(View v) {
        Button callerButton = (Button) v;
        int bTag = (int) v.getTag();
        switch (bTag) {
            case 0 :
                String ip = txtIp.getText().toString();
                String port = txtPort.getText().toString();
                String hostname = txtHostname.getText().toString();
                String initScreen = txtInitScreen.getText().toString();
                String terminalId = txtTerminalId.getText().toString();
                String merchantId = txtMerchantId.getText().toString();
                String merchantName = txtMerchantName.getText().toString();
                String merchantAddress1 = txtMerchantAddrees1.getText().toString();
                String merchantAddress2 = txtMerchantAddrees2.getText().toString();
                String diskonId = String.valueOf(spinnerDiskonId.getSelectedItem());
                String diskon = txtDiskon.getText().toString();
                String passSettlement = txtPassSettlement.getText().toString();
                String minDeduct = txtMinDeduct.getText().toString();
                String maxDeduct = txtMaxDeduct.getText().toString();
                boolean isDebug = debugMode.isChecked();
                preferences.edit().putString("ip",ip).apply();
                preferences.edit().putString("port",port).apply();
                preferences.edit().putString("hostname", hostname).apply();
                preferences.edit().putString("init_screen",initScreen).apply();
                preferences.edit().putString("diskon_id",diskonId).apply();
                preferences.edit().putString("diskon",diskon).apply();
                preferences.edit().putString("terminal_id",terminalId).apply();
                preferences.edit().putString("merchant_id",merchantId).apply();
                preferences.edit().putString("merchant_name",merchantName).apply();
                preferences.edit().putString("merchant_address1",merchantAddress1).apply();
                preferences.edit().putString("merchant_address2",merchantAddress2).apply();
                preferences.edit().putString("pass_settlement", passSettlement).apply();
                preferences.edit().putString("minimum_deduct", minDeduct).apply();
                preferences.edit().putString("maximum_deduct", maxDeduct).apply();
                preferences.edit().putBoolean("debug_mode", isDebug).apply();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AdminActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdminActivity.this,"Data berhasil disimpan",Toast.LENGTH_LONG).show();
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        restart(AdminActivity.this,100);
                    }
                });
                ((Button)findViewById(R.id.btnSimpan)).setText("Processing");
                ((Button)findViewById(R.id.btnSimpan)).setEnabled(false);
                t.start();
                break;
            case 1 :
                final Dialog updateKey = new Dialog(this);

                updateKey.setContentView(R.layout.dialog_mkey);
                updateKey.setTitle("Update Master Key");

                // get the Refferences of views
                final EditText editTextOldKey = (EditText) updateKey.findViewById(R.id.editTextOldKey);
                final EditText editTextNewKey = (EditText) updateKey.findViewById(R.id.editTextNewKey);
                Button btnUpdate = (android.widget.Button) updateKey.findViewById(R.id.buttonUpdateKey);

                // Set On ClickListener
                btnUpdate.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        // get the old and new key
                        String oldKey = editTextOldKey.getText().toString();
                        String newKey = editTextNewKey.getText().toString();
//                        Log.d("MK", "O : " + oldKey);
//                        Log.d("MK", "N : " + newKey);
                        byte[] boKey = StringLib.hexStringToByteArray(oldKey);
                        byte[] bnKey = StringLib.hexStringToByteArray(newKey);
                        byte[] tmKey = null;
                        if (boKey.length==8) {
                            tmKey = boKey;
                            boKey = ByteBuffer.allocate(16).put(tmKey).put(tmKey).array();
                        }
                        if (bnKey.length==8) {
                            tmKey = bnKey;
                            bnKey = ByteBuffer.allocate(16).put(tmKey).put(tmKey).array();
                        }
                        try {
                            PINPadInterface.open();

                            int ret = PINPadInterface.updateMasterKey(0, boKey, boKey.length, bnKey, bnKey.length);
//                            Log.d("PINPAD", "UPD MK STAT : " + String.valueOf(ret));
                            if (ret > -1) {
                                Toast.makeText(AdminActivity.this, "Master Key successfully updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Update Master Key failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(AdminActivity.this, "Update Master Key failed", Toast.LENGTH_LONG).show();
                        } finally {
                            PINPadInterface.close();
                        }

                        // fetch the old key form database
//                        String q = "select mk from holder where kid = 0";
//                        Cursor ck = clientDB.rawQuery(q, null);
//                        if (ck.moveToFirst()) {
//                            String existingKey = ck.getString(ck.getColumnIndex("mk"));
//                            if (existingKey.equals(oldKey)) {
//                                String w = "update holder set mk = '" + newKey + "' "
//                                        + "where kid = 0";
//                                clientDB.execSQL(w);
//                                Toast.makeText(AdminActivity.this, "Key successfully updated", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(AdminActivity.this, "Old key do not matched", Toast.LENGTH_LONG).show();
//                            }
//                            clientDB.close();
//                        } else {
//                            Toast.makeText(AdminActivity.this, "Secure module controller not responding", Toast.LENGTH_LONG).show();
//                        }
                        updateKey.dismiss();
                    }
                });
                updateKey.show();
                break;
        }
    }

    public void restart(final Context context, int delay) {
        if (delay == 0) {
            delay = 1;
        }

        Log.e("", "restarting app");
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName() );
        restartIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent,0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        Process.killProcess(Process.myPid());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this,"Please wait until saved",Toast.LENGTH_SHORT);
    }

    private class WifiAdapter extends ArrayAdapter<ScanResult> {

        public WifiAdapter(Context context, int resource, List<ScanResult> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.wifi_item, parent, false);
            }
            ScanResult result = getItem(position);
            android.widget.TextView tvWifiName =  ((android.widget.TextView) convertView.findViewById(R.id.wifi_name));
            tvWifiName.setText(formatSSDI(result));
            ((ImageView) convertView.findViewById(R.id.wifi_img)).setImageLevel(getNormalizedLevel(result));
            return convertView;
        }

        private int getNormalizedLevel(ScanResult r) {
            int level = WifiManager.calculateSignalLevel(r.level,
                    5);
            Log.e(getClass().getSimpleName(), "level " + level);
            return level;
        }

        private String formatSSDI(ScanResult r) {
            if (r == null || r.SSID == null || "".equalsIgnoreCase(r.SSID.trim())) {
                return "no data";
            }
            return r.SSID.replace("\"", "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(wifiReceiver);
        }catch (Exception e){
        }

        try{
            unregisterReceiver(wifiScanReceiver);
        }catch (Exception e){
        }
    }

    private void showWifiListDialog() {
        Collections.sort(scanResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return rhs.level > lhs.level ? 1 : rhs.level < lhs.level ? -1 : 0;
            }
        });
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
        wifiArrayAdapter = new WifiAdapter(
                this,
                android.R.layout.select_dialog_item, scanResults);

        builderSingle.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showingWiFiListDialog = false;
                        wifiStopScan();
                    }
                });
        builderSingle.setAdapter(wifiArrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String capabilities = wifiArrayAdapter.getItem(which).capabilities;
                        String strName = wifiArrayAdapter.getItem(which).SSID;

                        conf = new WifiConfiguration();
                        conf.SSID = "\"" + strName + "\"";
                        dialog.dismiss();
                        wifiStopScan();
                        showingWiFiListDialog = false;

                        if (capabilities.toUpperCase().contains("WEP")) {
                            // WEP Network
                            conf.wepTxKeyIndex = 0;
                            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                            isWantToConnectWEP = true;
                            isWantToConnectWPA = false;
                            showWifiLoginDialog();
                        } else if (capabilities.toUpperCase().contains("WPA")
                                || capabilities.toUpperCase().contains("WPA2")) {
                            // WPA or WPA2 Network
                            isWantToConnectWEP = false;
                            isWantToConnectWPA = true;
                            showWifiLoginDialog();
                        } else {
                            // Open Network
                            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                            connectToWifi (conf);
                        }
//                        Toast.makeText(getApplicationContext(), "Selected " + strName, Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = builderSingle.create();
        dialog.show();
        showingWiFiListDialog = true;
        isWantToScanWiFi = false;
    }

    private void updateWifiStatus(boolean isChecked) {
        if (isChecked){
            wifiLayout.setVisibility(View.VISIBLE);
            wifiSSID = "";
            if (getWifiStat()){
                WifiInfo wifiInfo;

                wifiInfo = mWifiManager.getConnectionInfo();
                wifiSSID = wifiInfo.getSSID();
            }

            if (wifiSSID.trim().equals("")){
                txtConnectedWifi.setText("Not Connected");
            }
            else{
                txtConnectedWifi.setText(wifiSSID);
            }
        }
        else{
            wifiLayout.setVisibility(View.GONE);
        }
    }

    private void connectToWifi(WifiConfiguration wifiConfig){
        showingWiFiListDialog = false;
        mWifiManager.setWifiEnabled(true);
        int netId = mWifiManager.addNetwork(wifiConfig);
        mWifiManager.disconnect();
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect();
//        txtConnectedWifi.setText(wifiConfig.SSID);
        updateWifiStatus(wifiSwitch.isChecked());
    }

    private void showWifiLoginDialog(){
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_login);
        dialog.setTitle("Connect to Wi-Fi");

        // get the Refferences of views
        final EditText editTextUserName = (EditText) dialog.findViewById(R.id.editTextUserNameToLogin);
        final EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextPasswordToLogin);
        editTextUserName.setVisibility(View.GONE);

        android.widget.Button btnSignIn = (android.widget.Button) dialog.findViewById(R.id.buttonSignIn);
        btnSignIn.setText("Connect");

        // Set On ClickListener
        btnSignIn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String networkPass = editTextPassword.getText().toString();
                if (isWantToConnectWPA){
                    conf.preSharedKey = "\""+ networkPass +"\"";
                }
                else if (isWantToConnectWEP){
                    conf.wepKeys[0] = "\"" + networkPass + "\"";
                }
                connectToWifi (conf);
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private void wifiStartScan(){
        isWantToScanWiFi = true;
        registerReceiver(wifiScanReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();
    }

    /* private void settingStartOpen(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example","com.example.checkbox.testquicksettingbutton"));
        startActivity(intent);
    } */

    protected void launchCall(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName("com.example.checkbox.testquicksettingbutton","com.example.checkbox.testquicksettingbutton.MainActivity"));
        /*intent.setComponent(new ComponentName("com.example.pro.secondapp","com.example.pro.secondapp.SecondApp")); */
        try{
            startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void wifiStopScan(){
        unregisterReceiver(wifiScanReceiver);
    }

    private boolean getWifiStat(){
        if (mWifiManager.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    private boolean getWifiAdapterStat(){
        if (mWifiManager.isWifiEnabled()) { // Wi-Fi adapter is ON
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }
}
