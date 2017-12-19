package com.wizarpos.function.printer;

import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;


public class PrinterMagicConvert {

    //	PrinterWrite(byte arryData[], int nDataLength);
    private byte arryData[];
    private int nDataLength;

    public int invokeJNIMethod(int nameNumber) {

        int result = 0;
        String methodName = "";
        switch (nameNumber) {
            case 1:
                result = PrinterInterface.PrinterOpen();
                methodName = "Open Driver";
                break;
            case 2:
                result = PrinterInterface.PrinterBegin();
                methodName = "PrinterBegin";
                break;
            case 3:
                result = PrinterInterface.PrinterEnd();
                methodName = "PrinterEnd";
                break;
            case 4:
                result = PrinterInterface.PrinterWrite(arryData, nDataLength);
                methodName = "PrinterWrite";
                break;
            case 5:
                result = PrinterInterface.PrinterClose();
                methodName = "Close Driver";
                break;
        }

        if (result == 0) {

            Log.d("aotTag", methodName + " success!code=" + result);
        } else {

            Log.d("aotTag", methodName + methodName + " failed!code=" + result);
        }
        return result;
    }

}
