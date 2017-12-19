package id.co.bri.brizzi;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.rey.material.app.ThemeManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.handler.FCHandler;
import id.co.bri.brizzi.handler.JsonCompHandler;
import id.co.bri.brizzi.handler.MenuListResolver;
import id.co.bri.brizzi.layout.FormMenu;
import id.co.bri.brizzi.layout.ListMenu;

public class MainActivity extends Activity implements KeyEvent.Callback {
    //    private MagneticSwipe swipe = new MagneticSwipe();
    private LinearLayout linearLayout;
    private String id = "";
    private MenuListResolver mlr = new MenuListResolver();
    private SharedPreferences preferences;
    private String codeHolder = "";
    private boolean showSetting = false;
    private JSONObject currentScreen;
    public static final int RESULT_CLOSE_SETTING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new FCHandler(this));

        ThemeManager.init(this, 1, 0, null);
        setContentView(R.layout.activity_main);
        showSetting = false;
        currentScreen = new JSONObject();
        preferences = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        android.widget.TextView txTid = (android.widget.TextView) findViewById(R.id.textViewTID);
        android.widget.TextView txMid = (android.widget.TextView ) findViewById(R.id.textViewMID);
        android.widget.TextView txMName = (android.widget.TextView ) findViewById(R.id.textViewMName);
        android.widget.TextView txFcopy = (android.widget.TextView ) findViewById(R.id.textViewCopy);
        android.widget.TextView txFsn = (android.widget.TextView ) findViewById(R.id.textViewSN);
        android.widget.TextView txFsv = (android.widget.TextView ) findViewById(R.id.textViewSV);
        txTid.setText("TID : " + preferences.getString("terminal_id", CommonConfig.DEV_TERMINAL_ID));
        txMid.setText("MID : " + preferences.getString("merchant_id", CommonConfig.DEV_MERCHANT_ID));
        txMName.setText(preferences.getString("merchant_name", CommonConfig.INIT_MERCHANT_NAME));
        SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
        String year = ydf.format(new Date());
        txFcopy.setText("\u00a9 BANK BRI " + year);
        String serialNum = Build.SERIAL;
        txFsn.setText(serialNum);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        txFsv.setText("Version " + version);
        linearLayout = (LinearLayout) findViewById(R.id.base_layout);
          try {
              Log.i("Set Menu", preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
              currentScreen = JsonCompHandler.readJson(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
              setMenu(currentScreen);
        } catch (IOException e) {
              e.printStackTrace();
        } catch (JSONException e) {
              e.printStackTrace();
        }
        Thread thread = new Thread(new VersionChecker(version));
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CLOSE_SETTING:
                showSetting = false;
                try {
                    currentScreen = JsonCompHandler.readJson(this, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT));
                    setMenu(currentScreen);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSetting = false;
        preferences = this.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMenu(JSONObject obj) {
        View child = null;
        Integer type = -1;
//        Log.d("JSON_MENU", obj.toString());
        try {
            type = obj.getInt("type");
            id = obj.get("id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (type != -1 && !id.equals("")) {
            switch (type) {
                case CommonConfig.MenuType.Form:
                    child = new FormMenu(this, id);

                    break;
                case CommonConfig.MenuType.ListMenu:
                    if (showSetting) {
                        id += "opt:ms=on";
                    }
                    ListMenu lm = new ListMenu(this, id);
//                    LinearLayout vp = lm.getPager();
//                    child = vp;
                    child = lm;
                    break;
                case CommonConfig.MenuType.PopupBerhasil:
                    break;
                case CommonConfig.MenuType.PopupGagal:
                    break;
                case CommonConfig.MenuType.PopupLogout:
                    break;
                case CommonConfig.MenuType.SecuredForm:
                    child = new FormMenu(this, id);
                    break;
            }
            linearLayout.removeAllViews();
            linearLayout.addView(child);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i("KEY", "Code : " + keyCode + ", Event : " + event);
        if (keyCode==232) {
            return false;
        }
        if (codeHolder.length()>=6) {
            codeHolder = "";
        }
        if (keyCode==56) {
            codeHolder = "";
        }
        if (keyCode==67) {
            if (codeHolder.length()>0) {
                codeHolder = codeHolder.substring(0, codeHolder.length() - 1);
            }
        }
        if (keyCode>6 && keyCode<17) {
            codeHolder += String.valueOf(keyCode-7);
        }
        if (codeHolder.equals("147258")) {
            if (!showSetting) {
                showSetting = true;
                setMenu(currentScreen);
            }
        }
        if (codeHolder.equals("8888")) {
            if (showSetting) {
                showSetting = false;
                setMenu(currentScreen);
            }
        }
        if (keyCode==7) {
            Log.d("CD", codeHolder);
        }
        return super.onKeyDown(keyCode, event);
    }

    class VersionChecker implements Runnable {
        private String version;

        public VersionChecker(String version) {
            this.version = version;
        }
        @Override
        public void run() {
            try {
                if(!preferences.getBoolean("debug_mode",CommonConfig.DEBUG_MODE)){
                    if(JsonCompHandler.checkUpdate(MainActivity.this).getString("software").equals(version)){

                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(1234);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
