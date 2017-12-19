package com.wizarpos.function.smr;

import com.wizarpos.apidemo.jniinterface.SmartCardEvent;
import com.wizarpos.apidemo.jniinterface.SmartCardInterface;
import com.wizarpos.apidemo.jniinterface.SmartCardSlotInfo;


public class SmartCardMagicConvert {

    // SmartCardOpen(int nSlotIndex);
    private int nSlotIndex;
    // SmartCardClose(int handle);
    // SmartCardPollEvent(int nTimeout_MS, SmartCardEvent event);
    private int nTimeout_MS;
    private SmartCardEvent event;
    // SmartCardPowerOn(int Handle, byte byteArrayATR[], SmartCardSlotInfo
    // info);
    // private int Handle;
    private byte byteArrayATR[];
    private SmartCardSlotInfo info;
    // SmartCardTransmit(int Handle, byte byteArrayAPDU[], int nAPDULength, byte
    // byteArrayResponse[]);
    private byte byteArrayAPDU[];
    private int nAPDULength;
    private byte byteArrayResponse[];

    public final static int SmartCardOpen = 1;
    public final static int SmartCardInit = 2;
    public final static int SmartCardTerminate = 3;
    public final static int SmartCardPollEvent = 4;
    public final static int SmartCardQueryMaxNumber = 5;
    public final static int SmartCardQueryPresence = 6;
    public final static int SmartCardPowerOn = 7;
    public final static int SmartCardPowerOff = 8;
    public final static int SmartCardSetSlotInfo = 9;
    public final static int SmartCardTransmit = 10;
    public final static int SmartCardClose = 11;


    private SmartCardMagicConvert() {

    }

    private static SmartCardMagicConvert singleton = null;

    public static SmartCardMagicConvert getSingleTon() {
        if (singleton == null) {
            singleton = new SmartCardMagicConvert();
        }
        return singleton;
    }


    public int invokeJNIMethod(int nameNumber) {

        int result = 0;
        String methodName = "";
        switch (nameNumber) {
            case 1:
                result = SmartCardInterface.smartcardOpen(nSlotIndex);
                methodName = "Open SmartCard Driver";
                break;
            case 2:
                result = SmartCardInterface.smartcardInit();
                methodName = "SmartCardInit";
                break;
            case 3:
                result = SmartCardInterface.smartcardTerminate();
                methodName = "SmartCardTerminate";
                break;
            case 4:
                result = SmartCardInterface.smartcardPollEvent(nTimeout_MS, event);
                methodName = "SmartCardPollEvent";
                break;
            case 5:
                result = SmartCardInterface.smartcardQueryMaxNumber();
                methodName = "SmartCardQueryMaxNumber";
                break;
            case 6:
                result = SmartCardInterface.smartcardQueryPresence(nSlotIndex);
                methodName = "SmartCardQueryPresence";
                break;
            case 7:
                result = SmartCardInterface.smartcardPowerOn(nSlotIndex, byteArrayATR, info);
                methodName = "SmartCardPowerOn";
                break;
            case 8:
                result = SmartCardInterface.smartcardPowerOff(nSlotIndex);
                methodName = "SmartCardPowerOff";
                break;
            case 9:
                result = SmartCardInterface.smartcardSetSlotInfo(nSlotIndex, info);
                methodName = "SmartCardSetSlotInfo";
                break;
            case 10:
                result = SmartCardInterface.smartcardTransmit(nSlotIndex, byteArrayAPDU, nAPDULength, byteArrayResponse);
                methodName = "SmartCardTransmit";
                break;
            case 11:
                result = SmartCardInterface.smartcardClose(nSlotIndex);
                methodName = "Close SmartCard Driver";
                break;
        }


        return result;
    }

    public void setSmartCardOpen(int nSlotIndex) {
        this.nSlotIndex = nSlotIndex;
    }

    public void setSmartCardClose(int nSlotIndex) {
        this.nSlotIndex = nSlotIndex;
    }

    public void setSmartCardPollEvent(int nTimeout_MS, SmartCardEvent event) {
        this.nTimeout_MS = nTimeout_MS;
        this.event = event;
    }

    public void setSmartCardPowerOn(int nSlotIndex, byte byteArrayATR[], SmartCardSlotInfo info) {
        this.nSlotIndex = nSlotIndex;
        this.byteArrayATR = byteArrayATR;
        this.info = info;
    }

    public void setSmartCardTransmit(int nSlotIndex, byte byteArrayAPDU[], int nAPDULength, byte byteArrayResponse[]) {
        this.nSlotIndex = nSlotIndex;
        this.byteArrayAPDU = byteArrayAPDU;
        this.nAPDULength = nAPDULength;
        this.byteArrayResponse = byteArrayResponse;
    }

    public void setSmartCardQueryPresence(int nSlotIndex) {
        this.nSlotIndex = nSlotIndex;
    }

    public void setSmartCardPowerOff(int nSlotIndex) {
        this.nSlotIndex = nSlotIndex;
    }

}
