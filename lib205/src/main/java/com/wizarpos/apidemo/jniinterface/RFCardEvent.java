package com.wizarpos.apidemo.jniinterface;

import android.util.Log;

import com.wizarpos.apidemo.util.StringUtil;
import com.wizarpos.apidemo.util.StringUtility;

public class RFCardEvent
{
	private static int nMaxEventDataLength = 0xFF;
	private int nEventID;
	private byte arryEventData[];
	private int nEventDataLength;
    private String TAG = "RFEvent";

	public RFCardEvent(int nEventID, byte[] arryEventData, int nEventDataLength) {
		this.nEventID = nEventID;
        Log.d(TAG, "Event ID : " + nEventID);
		this.arryEventData = arryEventData;
        Log.d(TAG, "Event Data : " + StringUtility.ByteArrayToString(arryEventData, nEventDataLength));
		this.nEventDataLength = nEventDataLength;
        Log.d(TAG, "Data Length : " + nEventDataLength);
	}

	public int getnEventID() {
		return nEventID;
	}

	public void setnEventID(int nEventID) {
		this.nEventID = nEventID;
	}

	public byte[] getArryEventData() {
		return arryEventData;
	}

	public int getnEventDataLength() {
		return nEventDataLength;
	}
}
