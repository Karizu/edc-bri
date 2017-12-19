package com.wizarpos.apidemo.jniinterface;

import android.util.Log;


public class IDCardJniInterface {
	static
	{
	    try{	        
	        System.loadLibrary("jni_wizarpos_IdentityCard");
	    }catch(Throwable th){
	        Log.e("IDCardJniInterface", "i can't find this libwizarposIdentityCard.so. so you  need to check libs Directory");
	    }
	}
	public native static int open();
	public native static int close();
	public native static int getInformation(IDCardProperty data);
	public native static int searchTarget();

}
