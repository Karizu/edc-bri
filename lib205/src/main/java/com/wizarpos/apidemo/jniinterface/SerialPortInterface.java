package com.wizarpos.apidemo.jniinterface;

public class SerialPortInterface 
{
	static
	{
		System.loadLibrary("jni_wizarpos_serial");
	}
	/*native interface */
	public native static int open();       // 对设备占用
	public native static int close();      // 对设备解除占用
	public native static int read(byte pDataBuffer[], int offset, int nExpectedDataLength, int nTimeout_MS);       // 读取设备的返回。
	public native static int write(byte pDataBuffer[], int nDataLength);                                                                       // 写入设备数据。
	public native static int set_baudrate(int nBaudrate);                                                                                                  // 设置波特率
	public native static int flush_io();                                                                                                                                   //  清空数据缓存

}
