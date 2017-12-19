package com.cloudpos.jniinterface;

public class LEDInterface {
	static{
		String fileName = "jni_cloudpos_led";
		JNILoad.jniLoad(fileName);
	}
    public synchronized native static int open();
    public synchronized native static int close();
    public synchronized native static int turnOn(int index);
    public synchronized native static int turnOff(int index);
    /**
     * get the status of led
     * @param[in] : unsigned int nLedIndex : index of led, >= 0 && < MAX_LED_COUNT
     * @return value : == 0 : turn off
     *                 > 0 : turn on
     *                 < 0 : error code
     */
    public synchronized native static int getStatus(int index);

}
