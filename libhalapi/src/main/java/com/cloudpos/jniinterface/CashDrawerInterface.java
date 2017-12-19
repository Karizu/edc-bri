package com.cloudpos.jniinterface;

public class CashDrawerInterface {
	static {
		String fileName = "jni_cloudpos_cashdrawer";
		JNILoad.jniLoad(fileName);
	}
	/*
	 * open the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;	
	 */
	public synchronized native static int open();
	/*
	 * close the money box device
	 * @return value : < 0 : error code
	 * 				   >= 0 : success;
	 */
	
	public synchronized native static int close();
	/*
	 * open money box
	 * @return value : < 0 : error code;
	 *                 >= 0 : success
	 */
	public synchronized native static int kickOut();
}
