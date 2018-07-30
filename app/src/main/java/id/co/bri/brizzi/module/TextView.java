package id.co.bri.brizzi.module;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import id.co.bri.brizzi.R;
import id.co.bri.brizzi.common.StringLib;

/**
 * Created by indra on 26/11/15.
 */
public class TextView extends LinearLayout {
    private JSONObject comp;
    private com.rey.material.widget.TextView txtLabel,txtValues;

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void init(JSONObject comp){
        txtLabel = (com.rey.material.widget.TextView) findViewById(R.id.lbl);
        txtValues = (com.rey.material.widget.TextView) findViewById(R.id.lbl_values);
        this.comp = comp;
        try {
            this.setTag(comp.getString("comp_id"));
            String tag = "";
            String value = "";
            value = comp.getString("comp_lbl");
            value = value.replace("[T]", "");
            value = value.replace("[C]", "");
            value = value.replace("[B]", "");
            value = value.replace("[START_LINE_PARSE]", "");
//            value = value.replace("\\[pad_40\\]","");
            String lbl = value;

            txtLabel.setText(value);

//            Log.d("COMP_VALUES",comp.get("comp_values").toString());
            if (comp.has("comp_values")) {

                JSONObject val = comp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0);
                value = val.getString("value");
                if(value.startsWith("[")){
                    value = value.substring(value.indexOf("]")+1);
                }
//                Log.d("TEXT_VIEW",val.toString());
//                Log.d("TEXT_VIEW", value);
                String tmp = value; //val.getString("value");
                if(tmp.equals("aa") || tmp.equals("cl")){
                    tmp = tmp.equals(TapCard.AKTIF_STATUS) ? "AKTIF" : "NON AKTIF";
                }
                txtValues.setText(tmp);
            }
            if (comp.getBoolean("visible")
                    &&(!lbl.startsWith("START"))
                    &&(!lbl.startsWith("STOP"))
                    &&(!(lbl.startsWith("Nomor Penerbang")&&txtValues.getText().length()<1))) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void init(String text, String id, boolean visible, JSONObject comp){
        txtLabel = (com.rey.material.widget.TextView) findViewById(R.id.lbl);
        txtValues = (com.rey.material.widget.TextView) findViewById(R.id.lbl_values);
        this.comp = comp;
        this.setTag(id);
        String tag = "";
        String value = "";
        value = text;
        String lbl = value;
//
//        String [] pad40Spli = value.split("\\[pad_40\\]");
//        if (pad40Spli.length>1){
//            String tmp = value;
//            value = "";
//            for (int i = 0 ; i < pad40Spli.length ; i++){
//                String[] valArr = pad40Spli[i].split(":");
//                if (valArr.length == 2){
//                    String strLbl = valArr[0].trim();
//                    String strVal = valArr[1].trim();
////
////
////                    if (strLbl.length() <= 16){
////                        value += strLbl+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal;
////                    }
////                    else
////                    {
////                        value += strLbl+ getResources().getString(R.string.tab) + ": " + strVal;
////                    }
////                    String space = "";
////                    for (int j = 0 ; j < 40 - strLbl.length(); j++){
////                        space += getResources().getString(R.string.space);
////                    }
////                    value += strLbl + space + ": "+ strVal +"\n";
//
//                    if (strLbl.length() <= 3) {
//                        value += strLbl+  getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal +"\n";
//
//                    }else
//                    if (strLbl.length() <= 4) {
//                        value += strLbl+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal +"\n";
//
//                    }
//                    else if (strLbl.length() <= 15) {
//                        value += strLbl+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+  getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal +"\n";
//
//                    }
//                    else if (strLbl.length() <= 16) {
//                        value += strLbl+  getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal +"\n";
//
//                    }
//                    else if (strLbl.length() <= 17) {
//                        value += strLbl+  getResources().getString(R.string.tab) +getResources().getString(R.string.tab)+ getResources().getString(R.string.tab)+ getResources().getString(R.string.tab) + ": " + strVal +"\n";
//                    }
//                    else if (strLbl.length() <= 18) {
//                        value += strLbl+getResources().getString(R.string.tab) + ": " + strVal +"\n";
//                    }
//                    else {
//                        value += strLbl+  getResources().getString(R.string.tab)+ ": " + strVal +"\n";
//                    }
//
//                }
//                else{
//                    value += "\n";
//                }
//            }
//        }

        txtLabel.setText(value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        if (value.contains("INFO KUOTA BERHASIL") || value.contains("Transaksi Berhasil")) {
            params.setMargins(0,0,0,0);
            txtLabel.setLayoutParams(params);
            txtLabel.setGravity(Gravity.CENTER);
            txtLabel.setPadding(0,0,0,0);
        }

        if (visible
                &&(!lbl.startsWith("START"))
                &&(!lbl.startsWith("STOP"))
                &&(!(lbl.startsWith("Nomor Penerbang")&&txtValues.getText().length()<1))) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
        }
    }
}
