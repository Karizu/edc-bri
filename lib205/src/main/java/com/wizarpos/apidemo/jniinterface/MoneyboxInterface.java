package com.wizarpos.apidemo.jniinterface;

public class MoneyboxInterface {
	static {
		System.loadLibrary("jni_wizarpos_moneybox");
	}
	/*
	 * open the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;	
	 */
	public native static int open();
	/*
	 * close the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;
	 */
	
	public native static int close();
	/*
	 * open money box
	 * @return value : < 0 : error code;
	 *                 >= 0 : success
	 */
	public native static int openMoneyBox();
}
