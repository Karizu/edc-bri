package com.wizarpos.apidemo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ResourceManager {
	
	public static Button getExitBtnFromMainActivity(Activity host){
		Button exitbtn = (Button)host.findViewById(R.id.exitbtn);
		return exitbtn;
	}
	
	public static ListView getListViewFromMainActivity(Activity host){
		ListView demoListView = (ListView)host.findViewById(R.id.demolistview);
		return demoListView;
	}
	
	public static TextView getTextViewFromMainActivity(Activity host){
		TextView messageTextView = (TextView) host.findViewById(R.id.messagetext);
		return messageTextView;
	}
	/** CustomDialog */
	public static EditText getEditTextFromCustomDialog(Dialog host){
		EditText editText = (EditText) host.findViewById(R.id.editText_input);
		return editText;
	}
	public static TextView getResultTextFViewFromCustomDialog(Dialog host){
		TextView resultTextView = (TextView) host.findViewById(R.id.txv_result);
		return resultTextView;
	}
	public static Button getOkBtnFromCustomDialog(Dialog host){
		Button exitbtn = (Button)host.findViewById(R.id.button_ok);
		return exitbtn;
	}
	public static Button getCancelBtnFromCustomDialog(Dialog host){
		Button exitbtn = (Button)host.findViewById(R.id.button_cancel);
		return exitbtn;
	}
	/** SecondMainActivity */
	
	public static Button getReturnButtonFromSecondMainActivity(Activity host){
		Button reBtn = (Button)host.findViewById(R.id.widget45);
		return reBtn;
	}
	
	public static Button getExitButtonFromSecondMainActivity(Activity host){
		Button exit = (Button)host.findViewById(R.id.widget46);
		return exit;
	}

	public static RadioButton getRadioButtonFromSecondMainActivity(Activity host){
		RadioButton ready = (RadioButton)host.findViewById(R.id.widget33);
		return ready;
	}
	
	public static ListView getListViewFromSecondMainActivity(Activity host){
		ListView buttonListView = (ListView)host.findViewById(R.id.widget39);
		return buttonListView;
	}
	
	public static TextView getTextViewFromSecondMainActivity(Activity host){
		TextView textView = (TextView) host.findViewById(R.id.widget47);
		return textView;
	}

}
