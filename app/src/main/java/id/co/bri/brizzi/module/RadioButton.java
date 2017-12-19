package id.co.bri.brizzi.module;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by indra on 27/11/15.
 */
public class RadioButton extends com.rey.material.widget.RadioButton {
    private JSONObject comp;
    public RadioButton(Context context) {
        super(context);
    }

    public RadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
