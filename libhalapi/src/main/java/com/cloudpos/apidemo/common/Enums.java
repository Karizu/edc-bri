package com.cloudpos.apidemo.common;

public class Enums {
	/**
	 * Log type
	 * @author zxd
	 *
	 */
	public class StateLog{
		public static final int LOG = 1;
		public static final int LOG_SUCCESS = 2;
		public static final int LOG_FAILED = 3;
		public static final int IMG_VISIBLE = 4;
		public static final int IMG_INVISIBLE = 5;
		public static final int IMG_SHOW = 6;
	}
	
	/**
	 * the netType using
	 */
	public class NetType{
		public static final int WIFI = 1;
		public static final int ETH0 = 2;
		public static final int G3 = 3;
		public static final int ALL = 0;
	}
	
	/**
	 * main menu type 
	 * @author zxd
	 *
	 */
	public enum StateType {
		pinpad, 
		printer, 
		smartcard, 
		contactless, 
		msr, 
		networktest, 
		soundtest, 
		serial, 
		psam, 
		sdcard, 
		led, 
		about, 
		btntest, 
		moneybox, 
		serialno, 
		hsm1850, 
		customer, 
		yali
	}

}
