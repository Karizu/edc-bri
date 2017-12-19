package com.cloudpos.apidemo.function;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.cloudpos.apidemo.common.Enums.StateLog;
import com.cloudpos.apidemo.util.LogHelper;

public class HandlerImpl extends Handler {

	private TextView txtResult;

	/**
	 * Output the information to display.
	 */
	public HandlerImpl(TextView textView) {
		this.txtResult = textView;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case StateLog.LOG:
			LogHelper.infoAppendMsg((String) msg.obj, txtResult);
			break;
		case StateLog.LOG_SUCCESS:
			LogHelper.infoAppendMsgForSuccess((String) msg.obj, txtResult);
			break;
		case StateLog.LOG_FAILED:
			LogHelper.infoAppendMsgForFailed((String) msg.obj, txtResult);
			break;
		case StateLog.IMG_VISIBLE:
		    
		default:
			LogHelper.infoAppendMsg((String) msg.obj, txtResult);
			break;
		}
	}

}
