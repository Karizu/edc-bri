package com.cloudpos.jniinterface;

public class IDCardInterface {
	static{
		String fileName = "jni_cloudpos_idcard";
		JNILoad.jniLoad(fileName);
	}
	/*native interface */
	public synchronized native static int open();
	public synchronized native static int close();
	public synchronized native static int getInformation(IDCardProperty data);
	public synchronized native static int searchTarget();

}
