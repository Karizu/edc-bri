package id.co.bri.brizzi.module;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//import android.support.v7.widget.Tint;

/**
 * Created by indra on 26/11/15.
 */
public class ComboBox extends com.rey.material.widget.Spinner {
    private JSONObject comp;

    public ComboBox(Context context) {
        super(context);
    }

    public ComboBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComboBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ComboBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(JSONObject comp){
        this.comp = comp;
        try {
//            this.setTag(comp.getString("comp_id"));
            setLabel(comp.getString("comp_lbl"));
            if (comp.get("comp_act") != null) {
                String predefined = (String) comp.get("comp_act");
                String[] pvalues = predefined.split("\\|");
                List<String> list = Arrays.asList(pvalues);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.setAdapter(dataAdapter);
            }
            if (comp.getBoolean("visible")) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
