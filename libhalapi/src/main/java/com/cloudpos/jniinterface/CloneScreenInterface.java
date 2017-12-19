
package com.cloudpos.jniinterface;

public class CloneScreenInterface {

    static {
    	String fileName = "jni_cloudpos_clonescreen";
		JNILoad.jniLoad(fileName);
    }

    public synchronized native static int open();

    public synchronized native static int close();

    public synchronized native static int show(int[] bitmap, int bitmapLength, int bitmapWidth, int bitmapHeight);
}
