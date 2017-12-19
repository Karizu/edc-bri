
package com.cloudpos.apidemo.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.apidemo.function.printer.PrinterBitmapUtil;
import com.cloudpos.apidemo.function.printer.PrinterCommand;
import com.cloudpos.apidemo.util.StringUtility;
import com.cloudpos.jniinterface.PrinterInterface;

public class PrinterAction extends ConstantAction {

    private void setParams(Map<String, Object> param, ActionCallbackImpl callback) {
        this.mCallback = callback;
    }

    // query the battery voltage
    // This api just for WIZARHAND_Q1
    public void queryVoltage(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final int[] pCapacity = new int[1];
        final int[] pVoltage = new int[1];
        int result = getData(new DataAction() {

            @Override
            public int getResult() {

                int result = PrinterInterface.queryVoltage(pCapacity, pVoltage);
                return result;
            }
        });
        if (result >= 0) {
            mCallback.sendSuccessMsg("pCapacity = " + pCapacity[0] + ", Battery Voltage : "
                    + pVoltage[0]);
        }
    }

    public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        if (isOpened) {
            callback.sendFailedMsg(mContext.getResources().getString(R.string.device_opened));
        } else {
            try {
                int result = PrinterInterface.open();
                if (result < 0) {
                    callback.sendFailedMsg(mContext.getResources().getString(
                            R.string.operation_with_error)
                            + result);
                } else {
                    isOpened = true;
                    callback.sendSuccessMsg(mContext.getResources().getString(
                            R.string.operation_successful));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                callback.sendFailedMsg(mContext.getResources().getString(R.string.operation_failed));
            }
        }
    }

    public void close(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                isOpened = false;
                int result = PrinterInterface.close();
                return result;
            }
        });
    }

    public void queryStatus(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = PrinterInterface.queryStatus();
                if (result > 0) {
                    // has paper
                    mCallback.sendSuccessMsg("PAPER_ON");
                } else if (result == 0) {
                    // no paper
                    mCallback.sendFailedMsg("PAPER_OUT");
                }
                return result;
            }
        });
    }

    private void begin() {
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = PrinterInterface.begin();
                return result;
            }
        });
    }

    private void end() {
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = PrinterInterface.end();
                return result;
            }
        });
    }

    public void write(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.printer_barcode_low);
//        try {
//            bitmap = BitmapFactory.decodeStream(mContext.getResources().getAssets()
//                    .open("printer_barcode_low.png"));
//        } catch (IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }

        byte[] arryBeginText = null;
        byte[] arryEndText = null;
        try {
//            arryBeginText = mContext.getResources().getString(R.string.print_QR_code).getBytes("GB2312");
            arryEndText = "This is a Bitmap of Barcode".getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            callback.sendFailedMsg(mContext.getResources().getString(R.string.operation_failed));
        }
        begin();
//        write(getCmdBarcode(0));
        // print text
        // write(null);
        write(arryBeginText);
        // print line break
        writeLineBreak(2);
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        PrinterBitmapUtil.printBitmap(bitmap, 0, 0, true);
        // print line break
        writeLineBreak(2);
        // print text
        write(arryEndText);
        // print line break
        writeLineBreak(2);
        end();
        // Log.e(TAG, "++++++++++++++++sync");
        // PrinterInterface.sync();
        // Log.e(TAG, "----------------sync");
    }

    // print line break
    private void writeLineBreak(int lineNumber) {
        // write(new byte[]{'\n'});
        write(PrinterCommand.getCmdEscDN(lineNumber));
    }

    private void write(final byte[] arryData) {
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = 0;
                if (arryData == null) {
                    result = PrinterInterface.write(null, 0);
                } else {
                    Log.e("DEBUG",StringUtility.ByteArrayToString(arryData, arryData.length));
                    // byte [] testData = new byte[arryData.length + 10];
                    // System.arraycopy(arryData, 0, testData, 10,
                    // arryData.length);
                    // result = PrinterInterface.write(testData, 10,
                    // arryData.length);
                    result = PrinterInterface.write(arryData, arryData.length);
                }
                return result;
            }
        });
    }
    
    private byte[] getCmdBarcode(int m) {
        return new byte[] {
                (byte) 0x1d, (byte) 0x6b, (byte) m, (byte) 1, (byte) 2, (byte) 3, (byte) 4,
                (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 0, (byte) 0, (byte)00
        };
    }

}
