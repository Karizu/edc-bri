
package com.cloudpos.jniinterface;

import android.util.Log;

public class MSRInterface
{
    static {
    	String fileName = "jni_cloudpos_msr";
		JNILoad.jniLoad(fileName);
    }
    
    public static int CONTACTLESS_CARD_EVENT_FOUND_CARD = 0;
    public static int CONTACTLESS_CARD_EVENT_USER_CANCEL = 3;
    public static int TRACK_COUNT = 3;

    /**
     * open the device
     * 
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int open();

    /**
     * close the device
     * 
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int close();

    /**
     * @param nTimeout_MS : time out in milliseconds. if nTimeout_MS is less
     *            then zero, the searching process is infinite.
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int poll(int nTimeout_MS);

    /**
     * get track error
     * 
     * @param nTrackIndex : Track index
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int getTrackError(int nTrackIndex);

    /**
     * get length of track data
     * 
     * @param nTrackIndex : Track index
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int getTrackDataLength(int nTrackIndex);

    /**
     * get track data.
     * 
     * @param nTrackIndex : Track index
     * @param byteArry : Track data
     * @param nLength : Length of track data
     * @return value >= 0, success in starting the process; value < 0, error
     *         code
     */
    public synchronized native static int getTrackData(int nTrackIndex, byte byteArry[], int nLength);

//    public static Object object = new Object();
//    public static int eventID = -4;
//
//    public static void callBack(int nEventID) {
//        synchronized (object) {
//            Log.i("MSRCard", "notify");
//            eventID = nEventID;
//            object.notifyAll();
//        }
//    }
    private static final int EVENT_NONE = -1;
    public static Object object = new Object();
    public static int eventID = EVENT_NONE;

    public static void callBack(int nEventID) {
        synchronized (object) {
            Log.i("MSRCard", "notify");
            eventID = nEventID;
            object.notifyAll();
        }
    }

    public static void waitForSwip(int timeout) throws InterruptedException {
        synchronized (object) {
            object.wait(timeout);
        }
    }

    public static void cancelWait() {
        synchronized (object) {
            object.notifyAll();
            eventID = CONTACTLESS_CARD_EVENT_USER_CANCEL;
        }
    }

    public static void clear() {
        eventID = EVENT_NONE;
    }
}
