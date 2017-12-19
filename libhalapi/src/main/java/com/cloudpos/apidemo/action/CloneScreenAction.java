
package com.cloudpos.apidemo.action;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera.Size;
import android.util.Log;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.common.BitmapConvert;
import com.cloudpos.apidemo.common.FileHelper;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.jniinterface.CloneScreenInterface;
import com.cloudpos.jniinterface.FingerPrintInterface;

public class CloneScreenAction extends ConstantAction {
    private void setParams(Map<String, Object> param, ActionCallbackImpl callback) {
        this.mCallback = callback;
    }

    public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        callback.sendImgVisible();
        if (isOpened) {
            callback.sendFailedMsg(mContext.getResources().getString(R.string.device_opened));
        } else {
            try {
                int result = CloneScreenInterface.open();
                if (result < 0) {
                    callback.sendFailedMsg(mContext.getResources().getString(
                            R.string.operation_with_error) + result);
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
        callback.sendImgInvisible();
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                isOpened = false;
                int result = CloneScreenInterface.close();
                return result;
            }
        });
    }

    /**
     * Zoom in or zoom out in equal scale
     */
    private Bitmap resizeBitmap(Bitmap srcBitmap, float size) {
        Matrix matrix = new Matrix();
        matrix.postScale(size, size); // The proportion of the height and width zoom in and out 
        Bitmap resizeBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                srcBitmap.getHeight(), matrix, true);
        return resizeBitmap;
    }

    public void show(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        // final Bitmap bitmap =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.printer_barcode_low);
        // final int[] arryBitmapData = BitmapConvert.bitmap2Ints(bitmap);
        // show(arryBitmapData, bitmap.getWidth(), bitmap.getHeight());

//        for (int j = 0; j < 100; j++) {
//            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                    R.drawable.printer_barcode_600x600);
//            int[] arryBitmapData = null;
//            for (int i = 0; i < 9; i++) {
//                float size = 0.1f * (10 - i);
//                bitmap = resizeBitmap(bitmap, size);
//                arryBitmapData = BitmapConvert.bitmap2Ints(bitmap);
//                mCallback.sendImgShow(bitmap);
//                show(arryBitmapData, bitmap.getWidth(), bitmap.getHeight());
//            }
//            bitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                    R.drawable.img_001);
//            for (int i = 0; i < 9; i++) {
//                float size = 0.1f * (10 - i);
//                bitmap = resizeBitmap(bitmap, size);
//                arryBitmapData = BitmapConvert.bitmap2Ints(bitmap);
//                mCallback.sendImgShow(bitmap);
//                show(arryBitmapData, bitmap.getWidth(), bitmap.getHeight());
//            }
//        }
        Bitmap bitmap02 =
                BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.img_001);
        int[] arryBitmapData02 = BitmapConvert.bitmap2Ints(bitmap02);
        show(arryBitmapData02, bitmap02.getWidth(), bitmap02.getHeight());
        
        
//         Bitmap bitmap1 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_100x100);
//         int[] arryBitmapData1 = BitmapConvert.bitmap2Ints(bitmap1);
//         Bitmap bitmap2 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_200x200);
//         int[] arryBitmapData2 = BitmapConvert.bitmap2Ints(bitmap2);
//         Bitmap bitmap3 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_low);
//         int[] arryBitmapData3 = BitmapConvert.bitmap2Ints(bitmap3);
//         Bitmap bitmap4 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_400x400);
//         int[] arryBitmapData4 = BitmapConvert.bitmap2Ints(bitmap4);
//         Bitmap bitmap5 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_500x500);
//         int[] arryBitmapData5 = BitmapConvert.bitmap2Ints(bitmap5);
//         Bitmap bitmap6 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_600x600);
//         int[] arryBitmapData6 = BitmapConvert.bitmap2Ints(bitmap6);
//         Bitmap bitmap7 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.printer_barcode_601x601);
//         int[] arryBitmapData7 = BitmapConvert.bitmap2Ints(bitmap7);
//        
//         Bitmap bitmap01 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_101);
//         int[] arryBitmapData01 = BitmapConvert.bitmap2Ints(bitmap01);
//        
//         Bitmap bitmap02 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001);
//         int[] arryBitmapData02 = BitmapConvert.bitmap2Ints(bitmap02);
//        
//         Bitmap bitmap03 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_90);
//         int[] arryBitmapData03 = BitmapConvert.bitmap2Ints(bitmap03);
//        
//         Bitmap bitmap04 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_80);
//         int[] arryBitmapData04 = BitmapConvert.bitmap2Ints(bitmap04);
//        
//         Bitmap bitmap05 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_70);
//         int[] arryBitmapData05 = BitmapConvert.bitmap2Ints(bitmap05);
//        
//         Bitmap bitmap06 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_60);
//         int[] arryBitmapData06 = BitmapConvert.bitmap2Ints(bitmap06);
//        
//         Bitmap bitmap07 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_50);
//         int[] arryBitmapData07 = BitmapConvert.bitmap2Ints(bitmap07);
//         Bitmap bitmap08 =
//         BitmapFactory.decodeResource(mContext.getResources(),
//         R.drawable.img_001_1);
//         int[] arryBitmapData08 = BitmapConvert.bitmap2Ints(bitmap08);
//        
//         for (int i = 0; i < 10; i++) {
//             show(arryBitmapData1, bitmap1.getWidth(), bitmap1.getHeight());
//             show(arryBitmapData2, bitmap2.getWidth(), bitmap2.getHeight());
//             show(arryBitmapData3, bitmap3.getWidth(), bitmap3.getHeight());
//             show(arryBitmapData4, bitmap4.getWidth(), bitmap4.getHeight());
//             show(arryBitmapData5, bitmap5.getWidth(), bitmap5.getHeight());
//             show(arryBitmapData6, bitmap6.getWidth(), bitmap6.getHeight());
//             show(arryBitmapData7, bitmap7.getWidth(), bitmap7.getHeight());
//             show(arryBitmapData01, bitmap01.getWidth(), bitmap01.getHeight());
//             show(arryBitmapData02, bitmap02.getWidth(), bitmap02.getHeight());
//             show(arryBitmapData03, bitmap03.getWidth(), bitmap03.getHeight());
//             show(arryBitmapData04, bitmap04.getWidth(), bitmap04.getHeight());
//             show(arryBitmapData05, bitmap05.getWidth(), bitmap05.getHeight());
//             show(arryBitmapData06, bitmap06.getWidth(), bitmap06.getHeight());
//             show(arryBitmapData07, bitmap07.getWidth(), bitmap07.getHeight());
//             show(arryBitmapData08, bitmap08.getWidth(), bitmap08.getHeight());
//        }
         
        // final Bitmap bitmap =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_001);
        // final int[] arryBitmapData = BitmapConvert.bitmap2Ints(bitmap);
        // final Bitmap bitmap2 =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_002);
        // final int[] arryBitmapData2 = BitmapConvert.bitmap2Ints(bitmap2);
        // final Bitmap bitmap3 =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_003);
        // final int[] arryBitmapData3 = BitmapConvert.bitmap2Ints(bitmap3);
        // final Bitmap bitmap4 =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_004);
        // final int[] arryBitmapData4 = BitmapConvert.bitmap2Ints(bitmap4);
        // final Bitmap bitmap5 =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_005);
        // final int[] arryBitmapData5 = BitmapConvert.bitmap2Ints(bitmap5);
        // final Bitmap bitmap6 =
        // BitmapFactory.decodeResource(mContext.getResources(),
        // R.drawable.img_006);
        // final int[] arryBitmapData6 = BitmapConvert.bitmap2Ints(bitmap6);
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // for (int i = 0; i < 200; i++) {
        // show(arryBitmapData, bitmap.getWidth(), bitmap.getHeight());
        // show(arryBitmapData2, bitmap2.getWidth(), bitmap2.getHeight());
        // show(arryBitmapData3, bitmap3.getWidth(), bitmap3.getHeight());
        // show(arryBitmapData4, bitmap4.getWidth(), bitmap4.getHeight());
        // show(arryBitmapData5, bitmap5.getWidth(), bitmap5.getHeight());
        // show(arryBitmapData6, bitmap6.getWidth(), bitmap6.getHeight());
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }
    }
    
    private void sleep(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void show(final int[] bitmap, final int width, final int height) {
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(bitmap, bitmap.length,
                        width, height);
                return result;
            }
        });
        sleep();
    }

    private void show1() {
        final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.img_001);
        final int[] arryBitmapData = BitmapConvert.bitmap2Ints(bitmap);
        Log.e(TAG, "length = " + arryBitmapData.length);

        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(arryBitmapData, arryBitmapData.length,
                        bitmap.getWidth(), bitmap.getHeight());
                return result;
            }
        });
    }

    private void show2() {
        final Bitmap bitmap2 = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.img_002);
        final int[] arryBitmapData2 = BitmapConvert.bitmap2Ints(bitmap2);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(arryBitmapData2, arryBitmapData2.length,
                        bitmap2.getWidth(), bitmap2.getHeight());
                return result;
            }
        });
    }

    private void show3() {
        final Bitmap bitmap3 = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.img_003);
        final int[] arryBitmapData3 = BitmapConvert.bitmap2Ints(bitmap3);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(arryBitmapData3, arryBitmapData3.length,
                        bitmap3.getWidth(), bitmap3.getHeight());
                return result;
            }
        });
    }

    private void show4() {
        final Bitmap bitmap4 = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.img_004);
        final int[] arryBitmapData4 = BitmapConvert.bitmap2Ints(bitmap4);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(arryBitmapData4, arryBitmapData4.length,
                        bitmap4.getWidth(), bitmap4.getHeight());
                return result;
            }
        });
    }

    private void show5() {
        final Bitmap bitmap5 = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.img_005);
        final int[] arryBitmapData5 = BitmapConvert.bitmap2Ints(bitmap5);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = CloneScreenInterface.show(arryBitmapData5, arryBitmapData5.length,
                        bitmap5.getWidth(), bitmap5.getHeight());
                return result;
            }
        });
    }

    private void show6() {
//        final Bitmap bitmap6 = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.img_006);
//        final int[] arryBitmapData6 = BitmapConvert.bitmap2Ints(bitmap6);
//        checkOpenedAndGetData(new DataAction() {
//
//            @Override
//            public int getResult() {
//                int result = CloneScreenInterface.show(arryBitmapData6, arryBitmapData6.length,
//                        bitmap6.getWidth(), bitmap6.getHeight());
//                return result;
//            }
//        });
    }
}
