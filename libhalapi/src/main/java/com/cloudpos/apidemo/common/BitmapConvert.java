
package com.cloudpos.apidemo.common;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BitmapConvert {
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();

        // if (w > 480 || h > 800) {
        // bm = comp(bm);
        // w = bm.getWidth();
        // h = bm.getHeight();
        // }

        ByteBuffer dst = ByteBuffer.allocate(w * h * 4);
        bm.copyPixelsToBuffer(dst);
        // dst.flip();
        byte[] pData = dst.array();
        // Log.e("DEBUG", "" + StringUtility.ByteArrayToString(pData, 100 *
        // 1024, 101 * 1024));
        return pData;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // return baos.toByteArray();
        // bm.getPixels(pixels, offset, stride, x, y, width, height)
        byte[] result = null;
        int w = bm.getWidth();
        int h = bm.getHeight();
        result = new byte[w * h * 4];
        int color, alpha, red, green, blue;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                color = bm.getPixel(x, y);
                alpha = Color.alpha(color);
                red = Color.red(color);
                green = Color.green(color);
                blue = Color.blue(color);
                result[y * w * 4 + x * 4 + 3] = int2Byte(alpha);
                result[y * w * 4 + x * 4 + 2] = int2Byte(red);
                result[y * w * 4 + x * 4 + 1] = int2Byte(green);
                result[y * w * 4 + x * 4 + 0] = int2Byte(blue);
            }
        }
        // Log.e("DEBUG", "" + StringUtility.ByteArrayToString(result, 100 *
        // 1024, 101 * 1024));
        return result;
    }

    public static int[] bitmap2Ints(Bitmap bm) {
        int[] result = null;
        int w = bm.getWidth();
        int h = bm.getHeight();
        result = new int[w * h];
        bm.getPixels(result, 0, w, 0, 0, w, h);
        return result;
    }

    private static byte int2Byte(int data) {
        byte result = (byte) (data & 0xff);
        return result;
    }
}
