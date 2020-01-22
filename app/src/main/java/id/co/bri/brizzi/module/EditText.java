package id.co.bri.brizzi.module;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.co.bri.brizzi.R;
import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.module.listener.PinpadListener;

/**
 * Created by indra on 25/11/15.
 */
public class EditText extends com.rey.material.widget.EditText implements
        View.OnKeyListener,
        View.OnFocusChangeListener {
    private JSONObject comp;
    private int maxLength, minLength;
    private boolean mandatory;
    private boolean number;
    private boolean password;
    private boolean isEditText;
    private AlertDialog.Builder alert;
    private List<PinpadListener> pinpadListeners = new ArrayList<>();

    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(JSONObject comp) {
        this.comp = comp;

        alert = new AlertDialog.Builder(getContext());
        alert.setCancelable(false).setTitle("Alert!").setIcon(android.R.drawable.ic_dialog_alert);

        try {
//            this.setTag(comp.getString("comp_id"));
            this.setHint(comp.getString("comp_lbl"));
            if (comp.has("comp_values")) {
                JSONObject object = comp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0);
                this.setText(object.getString("value"));
//                Log.d("COMP_VALUES", object.getString("value"));
            }
            if (comp.getBoolean("visible")) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.INVISIBLE);
            }

            Object[] compOpts = CommonConfig.getOpt(comp.getString("comp_opt"));
            maxLength = (int) compOpts[CommonConfig.CompOption.MAX_LENGTH];
            minLength = (int) compOpts[CommonConfig.CompOption.MIN_LENGTH];
            mandatory = (boolean) compOpts[CommonConfig.CompOption.MANDATORY];
            mandatory = true;
            if (comp.get("comp_id").equals("I000D")) {
                mandatory = false;
            }
//            setEnabled((boolean) compOpts[CommonConfig.CompOption.DISABLED]);
            isEditText = comp.getInt("comp_type") == CommonConfig.ComponentType.EditText;
            int type = (int) compOpts[CommonConfig.CompOption.TYPE];
            number = type == 2;

            if (isEditText) {
                switch (type) {
                    case 0:
                        setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                        break;
                    case 1:
                        setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                        setKeyListener(DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
                        break;
                    case 2:
//                        Log.d("TEST DEVICE", CommonConfig.getDeviceName());
                        if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                            setInputType(InputType.TYPE_CLASS_NUMBER);
                            setInputType(InputType.TYPE_NULL);
                        } else {
//                            Log.d("HORE DEVICE", CommonConfig.getDeviceName());
                            setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                        break;
                    case 3:
                        break;
                }
            } else {
                switch (type) {
                    case 0:
                        setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                        break;
                    case 1:
                        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        setKeyListener(DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
                        break;
                    case 2:
                        setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                    case 3:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        setOnFocusChangeListener(this);
        setOnKeyListener(this);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        setFilters(filters);

        if (number) {
            int inputType = InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD;
            if (!isEditText) {
                setTransformationMethod(new MyPasswordTransformationMethod());

            }
            for(PinpadListener pl : pinpadListeners){
                pl.onInput(this);
            }
            if (CommonConfig.getDeviceName().startsWith("WizarPOS")) {
                setInputType(InputType.TYPE_NULL);
            } else {
                setInputType(InputType.TYPE_CLASS_PHONE);
            }
        }
    }

    public void addPinpadListener(PinpadListener pl){
        pinpadListeners.add(pl);
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        setFilters(filters);
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public boolean onKey(final View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
            if (mandatory) {
                int size = getText().length();
                if (size < minLength && size > maxLength) {
                    setError("Panjang text harus diantara " + minLength + " dan " + maxLength);
                }
            }
        return false;
    }

    @Override
    public void onFocusChange(final View v, boolean hasFocus) {
//        Log.d("EDIT_TEXT", "IS NUMBER ?" + number);
//        if (mandatory) {
//            int size = getText().length();
//            if (size < minLength && size > maxLength) {
//
//                alert.setMessage("Panjang text harus diantara " + minLength + " dan " + maxLength);
//                alert.setPositiveButton("OK",new DialogInterface.OnClickListener(){
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        v.requestFocus();
//                    }
//                });
//                alert.show();
//            }
//        }
        if (!hasFocus) {
            final android.widget.EditText vv = (android.widget.EditText) v;
            int thisLength = vv.getText().toString().trim().length();
            if (thisLength>0) {
                if (thisLength < minLength) {
                    alert.setIcon(R.drawable.mb_info_poin);
                    alert.setTitle("Informasi");
                    alert.setMessage("Panjang text harus diantara " + minLength + " dan " + maxLength);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vv.requestFocus();
                        }
                    });
                    alert.show();
                }
            }
        }
    }

    public boolean isNumber() {
        return number;
    }

    public boolean isEditText(){return isEditText;}

    public static class MyPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }

            public char charAt(int index) {
                return '*'; // This is the important part
            }

            public int length() {
                return mSource.length(); // Return default
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }
}
