package com.wizarpos.apidemo.jniinterface;

public class SmartCardEvent 
{
	public static int SMART_CARD_EVENT_INSERT_CARD = 0;
	public static int SMART_CARD_EVENT_REMOVE_CARD	= 1;
	public static final String TAG_OPEN_DRIVE = "OPEN_DRIVE";
	public static final String TAG_POWER_ON = "POWER_ON";
	public static final String TAG_GET_RANDOM = "GET_RANDOM";
	public static final String TAG_SMART_CARD = "BRIZZI";

	public int nEventID;
	public int nSlotIndex;

	public static final int EVENT_POWER = 50;
	public static final int EVENT_GETRANDOM = 51;
	public static final int EVENT_DRIVE = 52;
	public SmartCardEvent()
	{
		nEventID = -1;
		nSlotIndex = -1;
	}
}
