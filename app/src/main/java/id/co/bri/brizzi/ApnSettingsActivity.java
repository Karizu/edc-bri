package id.co.bri.brizzi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Telephony;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import android.widget.ListView;
import com.wizarpos.jni.PINPadInterface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.common.StringLib;
import id.co.bri.brizzi.handler.DataBaseHelper;
import id.co.bri.brizzi.module.ApnPreference;

/**
 * Created by rizkyazhary on 8/23/17.
 */

public class ApnSettingsActivity extends Activity implements View.OnClickListener {
    private ListView apnListView;
    private APNListAdapter apnListAdapter;
    private ArrayList<ApnPreference> apnPreferences;
    private String mSelectedKey;

    public static final String APN_ID = "apn_id";
    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_apn_settings);
        apnListView = (ListView) findViewById(R.id.lv_apn_settings);

        mSelectedKey = getSelectedApnKey();

        int id = -1;
        boolean existing = false;

        apnPreferences = new ArrayList<ApnPreference>();

        Cursor cursor = getContentResolver().query(APN_TABLE_URI, null, null, null, null);
        cursor.moveToLast();
        while (cursor.isBeforeFirst() == false){
            String name = cursor.getString(NAME_INDEX);
            String apn = cursor.getString(APN_INDEX);
            String key = cursor.getString(ID_INDEX);
            String type = cursor.getString(TYPES_INDEX);

            ApnPreference pref = new ApnPreference();
            pref.setName(name);
            pref.setApn(apn);
            pref.setKey(key);
            pref.setType(type);

            boolean selectable = ((type == null) || !type.equals("mms"));
            pref.setSelectable(selectable);
            if (selectable) {
                boolean selected = (mSelectedKey != null) && mSelectedKey.equals(key);
                pref.setSelected(selected);
            } else {
                pref.setSelected(false);
            }
            apnPreferences.add(pref);
        }

        apnListAdapter = new APNListAdapter(
                this, R.id.lv_apn_settings, apnPreferences);

    }

    private String getSelectedApnKey() {
        String key = null;

        Cursor cursor = getContentResolver().query(PREFERRED_APN_URI, new String[] {"_id"},
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(ID_INDEX);
        }
        cursor.close();
        return key;
    }

    @Override
    public void onClick(View v) {
        Button callerButton = (Button) v;
        int bTag = (int) v.getTag();
        switch (bTag) {
            case 0 :

                break;
            case 1 :

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this,"Please wait until saved",Toast.LENGTH_SHORT);
    }

    private class APNListAdapter extends ArrayAdapter <ApnPreference> {

        public APNListAdapter(Context context, int resource, ArrayList<ApnPreference> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.apn_item, parent, false);
            }
            ApnPreference result = getItem(position);
            android.widget.TextView tvWifiName =  ((android.widget.TextView) convertView.findViewById(R.id.apn_name));
            tvWifiName.setText(result.getName());
            android.widget.RadioButton radioButton = (android.widget.RadioButton) convertView.findViewById(R.id.apn_radio_button);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    selectApn(position);
                }
            });
            if (result.isSelectable()){
                radioButton.setVisibility(View.VISIBLE);
                radioButton.setSelected(result.isSelected());
            }
            else{
                radioButton.setVisibility(View.GONE);
            }
//            ((ImageView) convertView.findViewById(R.id.wifi_img)).setImageLevel(getNormalizedLevel(result));
            return convertView;
        }

    }

    private void selectApn(int index){
        if (apnPreferences == null) return;
        if (apnPreferences.size() > index && index > 0){
            ApnPreference pref = apnPreferences.get(index);
            if (pref.isSelectable()){
                pref.setSelected(true);
                for (int i = 0; i<apnPreferences.size(); i++){
                    ApnPreference rPref = apnPreferences.get(i);
                    if (i != index){
                        rPref.setSelected(false);
                    }
                }
            }
        }
        apnListAdapter.notifyDataSetChanged();
    }
}
