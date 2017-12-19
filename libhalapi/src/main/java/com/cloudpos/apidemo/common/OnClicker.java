package com.cloudpos.apidemo.common;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class OnClicker {
	/**
	 * The control group bind events.
	 */
	public static void setOnClickListenerByIds(Activity host, int[] widget, OnClickListener listener) {
		for (int viewId : widget) {
			setOnClickListenerById(host, viewId, listener);
		}
	}
	
	/**
	 * Control bind events.
	 * 
	 * @param viewId  control id
	 * @param listener
	 *            
	 */
	public static void setOnClickListenerById(Activity host, int viewId, View.OnClickListener listener) {
		host.findViewById(viewId).setOnClickListener(listener);
	}
}
