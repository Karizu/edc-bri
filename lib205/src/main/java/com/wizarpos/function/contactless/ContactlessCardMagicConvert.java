package com.wizarpos.function.contactless;

import com.wizarpos.apidemo.jniinterface.ContactlessEvent;
import com.wizarpos.apidemo.jniinterface.ContactlessInterface;


public class ContactlessCardMagicConvert {

    private byte byteArrayAPDU[];
    private int nAPDULength;
    private byte byteArrayResponse[];

    private int nCardMode;
    private int nFlagSearchAll;
    private int nTimeout_MS1;
    private int nTimeout_MS2;
    private byte byteArrayATR[];

    private int nCmdID;
    private byte byteArrayCmdData[];
    private int nDataLength;

    private ContactlessEvent event;
    public final static int Open = 1;
    public final static int SearchTargetBegin = 2;
    public final static int SearchTargetEnd = 3;
    public final static int AttachTarget = 4;
    public final static int DetachTarget = 5;
    public final static int Transmit = 6;
    public final static int SendControlCommand = 7;
    public final static int PollEvent = 8;
    public final static int Close = 9;

    private ContactlessCardMagicConvert() {

    }

    private static ContactlessCardMagicConvert singleton = null;

    public static ContactlessCardMagicConvert getSingleTon() {
        if (singleton == null) {
            singleton = new ContactlessCardMagicConvert();
        }
        return singleton;
    }

    //
    public int invokeJNIMethod(int nameNumber) {
        int result = 0;
        String methodName = "";
        switch (nameNumber) {
            case Open:
                result = ContactlessInterface.Open();
                methodName = "Open Driver";
                break;
            case SearchTargetBegin:
                // int nCardMode, int nFlagSearchAll, int nTimeout_MS
                result = ContactlessInterface.SearchTargetBegin(nCardMode,
                        nFlagSearchAll, nTimeout_MS1);
                methodName = "SearchTargetBegin";
                break;
            case SearchTargetEnd:
                result = ContactlessInterface.SearchTargetEnd();
                methodName = "SearchTargetEnd";
                break;
            case AttachTarget:
                // byte byteArrayATR[]
                result = ContactlessInterface.AttachTarget(byteArrayATR);
                methodName = "AttachTarget";
                break;
            case DetachTarget:
                result = ContactlessInterface.DetachTarget();
                methodName = "DetachTarget";
                break;
            case Transmit:
                // byte byteArrayAPDU[], int nAPDULength, byte byteArrayResponse[]
                result = ContactlessInterface.Transmit(byteArrayAPDU, nAPDULength,
                        byteArrayResponse);
                methodName = "Transmit";
            case SendControlCommand:
                // int nCmdID, byte byteArrayCmdData[], int nDataLength
                result = ContactlessInterface.SendControlCommand(nCmdID,
                        byteArrayCmdData, nDataLength);
                methodName = "SendControlCommand";
            case PollEvent:
                // int nTimeout_MS, ContactlessEvent event
                result = ContactlessInterface.PollEvent(nTimeout_MS2, event);
                methodName = "PollEvent";
            case Close:
                result = ContactlessInterface.Close();
                methodName = "Close Driver";
                break;
        }


        return result;
    }

    public void setPollEventValue(int nTimeout_MS, ContactlessEvent event) {
        this.nTimeout_MS2 = nTimeout_MS;
        this.event = event;
    }

    public void setSendControlCommand(int nCmdID, byte byteArrayCmdData[],
                                      int nDataLength) {
        this.nCmdID = nCmdID;
        this.byteArrayCmdData = byteArrayCmdData;
        this.nDataLength = nDataLength;
    }

    public void setTransmit(byte byteArrayAPDU[], int nAPDULength,
                            byte byteArrayResponse[]) {
        this.byteArrayAPDU = byteArrayAPDU;
        this.nAPDULength = nAPDULength;
        this.byteArrayResponse = byteArrayResponse;
    }

    public void setSearchTargetBegin(int nCardMode, int nFlagSearchAll,
                                     int nTimeout_MS) {
        this.nCardMode = nCardMode;
        this.nFlagSearchAll = nFlagSearchAll;
        this.nTimeout_MS1 = nTimeout_MS;
    }

    public void setAttachTarget(byte byteArrayATR[]) {
        this.byteArrayATR = byteArrayATR;
    }

}
