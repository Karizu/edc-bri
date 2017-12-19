package id.co.bri.brizzi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rey.material.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.handler.JsonCompHandler;
import id.co.bri.brizzi.layout.FormMenu;
import id.co.bri.brizzi.module.MagneticSwipe;

/**
 * Created by indra on 25/11/15.
 */
public class MenuFragment extends Fragment {
    private View previousView;
    private LinearLayout linearLayout;
    private String id = "";
    private Button btnBack;

    public MenuFragment() {

    }


    public View getPreviousView() {
        return previousView;
    }

    public void backPreviousState() {
//        getC
//        Log.d("FRAGMENT", "JUMLAH CHILD = " + linearLayout.getChildCount());
        if (linearLayout != null && previousView != null) {
            linearLayout.removeAllViews();
            linearLayout.addView(previousView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.base_fragment, container, false);

        btnBack = (Button) v.findViewById(R.id.btn_back);
//        Log.d("FRAGMENT", "INIT_FRAGMENT =");
        linearLayout = (LinearLayout) v.findViewById(R.id.base_layout);
        try {
            SharedPreferences preferences = getContext().getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            setMenu(JsonCompHandler.readJson(this.getActivity(), preferences.getString("init_screen","0000000")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBack(v);
            }
        });
        return v;
    }

    private void onClickBack(View v) {
        if(linearLayout.getChildAt(0) instanceof  FormMenu){
            FormMenu base = (FormMenu) linearLayout.getChildAt(0);
            LinearLayout menu = base.getBaseLayout();
            int viewSize = menu.getChildCount();
            for(int i = 0;i<viewSize;i++){
//                Log.d("CLASS NAME","INDEX = "+i+", CLASSNAME = "+menu.getChildAt(i).getClass().getCanonicalName());
                if(menu.getChildAt(i) instanceof MagneticSwipe){
//                    Log.d("MENU_FRAGMENT","MAGNETIC SWIPE FOUND");
                    MagneticSwipe swipe = (MagneticSwipe) menu.getChildAt(i);
                    swipe.setIsQuit(true);
                    swipe.closeDriver();
                }
        }


        }
        SharedPreferences preferences = getContext().getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        if (v.getTag().toString().equals(preferences.getString("init_screen","0000000"))) {
            this.getActivity().finish();
        } else {
            try {
                setMenu(JsonCompHandler.readJson(MenuFragment.this.getActivity(), preferences.getString("init_screen","0000000")));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMenu(JSONObject obj) {
        SharedPreferences preferences = getContext().getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
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
            try {
                getActivity().setTitle(obj.getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            btnBack.setTag(id);
            if (id.equals(preferences.getString("init_screen","0000000"))) {
                btnBack.setText("Keluar");
            } else {
                btnBack.setText("Kembali ke menu utama");
            }
            linearLayout.removeAllViews();
            linearLayout.addView(child);
        }

    }
}
