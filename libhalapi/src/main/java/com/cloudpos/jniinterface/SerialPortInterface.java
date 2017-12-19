
package com.cloudpos.jniinterface;

public class SerialPortInterface
{
    static
    {
    	String fileName = "jni_cloudpos_serial";
		JNILoad.jniLoad(fileName);
    }

    /* native interface */
    public synchronized native static int open(String deviceName);

    public synchronized native static int close();

    public synchronized native static int read(byte pDataBuffer[], int nExpectedDataLength, int nTimeout_MS);

    public synchronized native static int write(byte pDataBuffer[], int offset, int nDataLength);

    public synchronized native static int setBaudrate(int nBaudrate);

    public synchronized native static int flushIO();

}
