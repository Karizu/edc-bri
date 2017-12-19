package id.co.bri.brizzi.module;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by indra on 27/11/15.
 */
public class Button extends com.rey.material.widget.Button {
    private JSONObject comp;
    public Button(Context context) {
        super(context);
    }

    public Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(JSONObject comp){
//        Log.d("BUTTON", comp.toString());
        this.comp = comp;
        try {
            setTag(comp.getString("comp_id"));
            setText(comp.getString("comp_lbl"));

            if (comp.has("comp_values")) {

            }
            if (comp.getBoolean("visible")) {
                setVisibility(View.VISIBLE);
            } else {
                setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
