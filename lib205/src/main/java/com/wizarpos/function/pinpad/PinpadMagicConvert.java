package com.wizarpos.function.pinpad;

import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PinpadInterface;

public class PinpadMagicConvert {


    //	PinpadShowText(int nLineIndex, byte arryText[], int nTextLength, int nFlagSound);
    private int nLineIndex;
    private byte arryText[];
    private int nTextLength;
    private int nFlagSound;
    //	PinpadSelectKey(int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith);
    private int nKeyType;
    private int nMasterKeyID;
    private int nUserKeyID;
    private int nAlgorith;
    //	 PinpadEncryptString(byte arryPlainText[], int nTextLength, byte arryCipherTextBuffer[]);
    private byte arryPlainText[];
    //	private int nTextLength;
    private byte arryCipherTextBuffer[];
    //	PinpadCalculatePinBlock(byte arryASCIICardNumber[], int nCardNumberLength, byte arryPinBlockBuffer[], int nTimeout_MS, int nFlagSound);
    private byte arryASCIICardNumber[];
    private int nCardNumberLength;
    private byte arryPinBlockBuffer[];
    private int nTimeout_MS;
    //	private int nFlagSound;
//	PinpadCalculateMac(byte arryData[], int nDataLength, int nMACFlag, byte arryMACOutBuffer[]);
    private byte arryData[];
    private int nDataLength;
    private int nMACFlag;
    private byte arryMACOutBuffer[];
    //	PinpadUpdateUserKey(int nMasterKeyID, int nUserKeyID, byte arryCipherNewUserKey[], int nCipherNewUserKeyLength);
//	private int nMasterKeyID;
//	private int nUserKeyID;
    private byte arryCipherNewUserKey[];
    private int nCipherNewUserKeyLength;
    //	PinpadSetPinLength(int nLength, int nFlag);
    private int nLength;
    private int nFlag;

    public final static int PinpadOpen = 1;
    public final static int PinpadShowText = 2;
    public final static int PinpadSelectKey = 3;
    public final static int PinpadEncryptString = 4;
    public final static int PinpadCalculatePinBlock = 5;
    public final static int PinpadCalculateMac = 6;
    public final static int PinpadUpdateUserKey = 7;
    public final static int PinpadSetPinLength = 8;
    public final static int PinpadClose = 9;

    private static PinpadMagicConvert singleton = null;

    public static PinpadMagicConvert getSingleTon() {
        if (singleton == null) {
            singleton = new PinpadMagicConvert();
        }
        return singleton;
    }

    private PinpadMagicConvert() {

    }

    public int invokeJNIMethod(int nameNumber) {

        int result = 0;
        String methodName = "";
        switch (nameNumber) {
            case 1:
                result = PinpadInterface.PinpadOpen();
                methodName = "Open Driver";
                break;
            case 2:
                result = PinpadInterface.PinpadShowText(nLineIndex, arryText, nTextLength, nFlagSound);
                methodName = "PinpadShowText";

                break;
            case 3:
                result = PinpadInterface.PinpadSelectKey(nKeyType, nMasterKeyID, nUserKeyID, nAlgorith);
                methodName = "PinpadSelectKey";
                break;
            case 4:
                result = PinpadInterface.PinpadEncryptString(arryPlainText, nTextLength, arryCipherTextBuffer);
                methodName = "PinpadEncryptString";
                break;
            case 5:

                result = PinpadInterface.PinpadCalculatePinBlock(arryASCIICardNumber, nCardNumberLength, arryPinBlockBuffer, nTimeout_MS, nFlagSound);
                methodName = "PinpadCalculatePinBlock";
                break;
            case 6:
                result = PinpadInterface.PinpadCalculateMac(arryData, nDataLength, nMACFlag, arryMACOutBuffer);
                methodName = "PinpadCalculateMac";
                break;
            case 7:
                result = PinpadInterface.PinpadUpdateUserKey(nMasterKeyID, nUserKeyID, arryCipherNewUserKey, nCipherNewUserKeyLength);
                methodName = "PinpadUpdateUserKey";
                break;
            case 8:
                result = PinpadInterface.PinpadSetPinLength(nLength, nFlag);
                methodName = "PinpadSetPinLength";
                break;
            case 9:
                result = PinpadInterface.PinpadClose();
                methodName = "Close Driver";
                break;
        }

        if (result >= 0) {
            Log.i("aotTag", methodName + " success!code=" + result);
        } else {
            Log.i("aotTag", methodName + " failed!code=" + result);
        }
        return result;
    }

    public void setPinpadShowText(int nLineIndex, byte arryText[], int nTextLength, int nFlagSound) {
        this.nLineIndex = nLineIndex;
        this.arryText = arryText;
        this.nTextLength = nTextLength;
        this.nFlagSound = nFlagSound;
    }

    public void setPinpadSelectKey(int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith) {
        this.nKeyType = nKeyType;
        this.nMasterKeyID = nMasterKeyID;
        this.nUserKeyID = nUserKeyID;
        this.nAlgorith = nAlgorith;
    }

    public void setPinpadEncryptString(byte arryPlainText[], int nTextLength, byte arryCipherTextBuffer[]) {
        this.arryPlainText = arryPlainText;
        this.nTextLength = nTextLength;
        this.arryCipherTextBuffer = arryCipherTextBuffer;
    }

    public void setPinpadCalculatePinBlock(byte arryASCIICardNumber[], int nCardNumberLength, byte arryPinBlockBuffer[], int nTimeout_MS, int nFlagSound) {
        this.arryASCIICardNumber = arryASCIICardNumber;
        this.nCardNumberLength = nCardNumberLength;
        this.arryPinBlockBuffer = arryPinBlockBuffer;
        this.nTimeout_MS = nTimeout_MS;
        this.nFlagSound = nFlagSound;
    }

    public void setPinpadCalculateMac(byte arryData[], int nDataLength, int nMACFlag, byte arryMACOutBuffer[]) {
        this.arryData = arryData;
        this.nDataLength = nDataLength;
        this.nMACFlag = nMACFlag;
        this.arryMACOutBuffer = arryMACOutBuffer;
    }

    public void setPinpadUpdateUserKey(int nMasterKeyID, int nUserKeyID, byte arryCipherNewUserKey[], int nCipherNewUserKeyLength) {
        this.nMasterKeyID = nMasterKeyID;
        this.nUserKeyID = nUserKeyID;
        this.arryCipherNewUserKey = arryCipherNewUserKey;
        this.nCipherNewUserKeyLength = nCipherNewUserKeyLength;
    }

    public void setPinpadSetPinLength(int nLength, int nFlag) {
        this.nLength = nLength;
        this.nFlag = nFlag;
    }

}
