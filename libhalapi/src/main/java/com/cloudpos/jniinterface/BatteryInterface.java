package com.cloudpos.jniinterface;

public class BatteryInterface {
    static {
    	String fileName = "jni_cloudpos_battery";
        JNILoad.jniLoad(fileName);
    }
    
    public synchronized native static int open();
    
    public synchronized native static int close();
    
    public synchronized native static int queryInfo(int[] capacity, int[] voltage);
}
