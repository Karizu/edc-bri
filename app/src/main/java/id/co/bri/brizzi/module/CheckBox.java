package id.co.bri.brizzi.module;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by indra on 27/11/15.
 */
public class CheckBox extends com.rey.material.widget.CheckBox {
    private JSONObject comp;
    public CheckBox(Context context) {
        super(context);
    }

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(JSONObject comp){
        this.comp = comp;
        try {
            this.setTag(comp.getString("comp_id"));
            setText(comp.getString("comp_lbl"));
            if (comp.get("comp_value") != null) {

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
