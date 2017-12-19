package com.wizarpos.apidemo.printer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.util.StringUtil;

public class PrinterBitmapUtil {
    public static final int BIT_WIDTH = 384;

    private static final int WIDTH = 48;
    private static final int DOT_LINE_LIMIT=200;
    private static final int DC2V_HEAD=4;
    private static final int GSV_HEAD=8;
    public static byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33, (byte) 255, 3};

    private static String getSystemProperty(String name){
        try{
            Process du = Runtime.getRuntime().exec("getprop "+name);   
            BufferedReader in = new BufferedReader(new InputStreamReader(du.getInputStream()));
            String value = in.readLine();
            in.close();
            return value;
        }catch(Exception e){
            return null;
        }
    }
    public static void printBitmap(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
//        printBitmapGSVMSB(bm,bitMarginLeft,bitMarginTop);
//        printBitmapDV2VMSB(bm,bitMarginLeft,bitMarginTop);
//        printBitmapESCStar(bm,bitMarginLeft,bitMarginTop);
        String printerBaud=getSystemProperty("wp.printer.baud");
        if("115200".equals(printerBaud)){
            Log.d("PrintUI", "GSV "+printerBaud);
            printBitmapGSVMSB(bm,bitMarginLeft,bitMarginTop);
        }else if("9600".equals(printerBaud)){
            Log.d("PrintUI", "DC2V"+printerBaud);
            printBitmapDV2VMSB(bm,bitMarginLeft,bitMarginTop);
        }else{
            Log.d("PrintUI", "DC2V slow"+printerBaud);
            printBitmapDV2VMSBslow(bm,bitMarginLeft,bitMarginTop);
        }
    }

    public static void printBitmap(Bitmap bm) {
//        printBitmapGSVMSB(bm,bitMarginLeft,bitMarginTop);
//        printBitmapDV2VMSB(bm,bitMarginLeft,bitMarginTop);
//        printBitmapESCStar(bm,bitMarginLeft,bitMarginTop);
        printBitmapDV2VMSBslow(bm);
    }
    /**
     * print the bitmap by DC2 V nL nH command
     * 
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return
     */
    private static void printBitmapDV2VMSB(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result=generateBitmapArrayDC2V_MSB(bm, bitMarginLeft, bitMarginTop);
        // just for debug the bit array.
//        tracelogMSB(result, 48);
        
//        int resultJni = PrinterInterface.PrinterOpen();
//        if(resultJni <0){
//            Log.e("App", "don't  open twice this devices");
//            PrinterInterface.PrinterClose();
//            return ;
//        }
//        PrinterInterface.PrinterBegin();
        
//        // reset the printer
//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x40},2);
            // enhance the heating time
            PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x37, 7,(byte)240,2},5);
        // upgrade the print destiny
//        PrinterInterface.PrinterWrite(new byte[]{0x12, 0x23, 0x00 | (28)},3);
        
        // write print bitmap
        int lines = (result.length - DC2V_HEAD)/WIDTH;
        System.arraycopy(new byte[]{0x12,0x56,(byte)(lines&0xff),(byte)((lines>>8)&0xff)}, 0, result, 0, DC2V_HEAD);
        Log.d("printer", StringUtil.toFullString(result));
        PrinterInterface.PrinterWrite(result, result.length);
//        Vector<byte[]> vData=checkBufferLength(result);
//        for(int i=0;i<vData.size();i++){
//            byte[] temp=vData.elementAt(i);
//            int lines=temp.length/WIDTH;
//            PrinterInterface.PrinterWrite(new byte[]{0x12,0x56,(byte)(lines&0xff),(byte)((lines>>8)&0xff)},4 );
//            PrinterInterface.PrinterWrite(temp,temp.length);
//        }
        
//         restore the heating time
        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x37, 7,(byte)0x80,2},5);

//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x21, 0},3);
//        PrinterInterface.PrinterWrite("\n\n\n".getBytes(),3);
//      PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x4A, 8},3);
//        // reset the printer
//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x40},2);
       
//        PrinterInterface.PrinterEnd();
//        PrinterInterface.PrinterClose();
    }
    private static void printBitmapDV2VMSBslow(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result=generateBitmapArrayDC2V_MSB(bm, bitMarginLeft, bitMarginTop);
        // just for debug the bit array.
//        tracelogMSB(result, 48);
        
//        PrinterInterface.PrinterOpen();
//        PrinterInterface.PrinterBegin();
        
//        // reset the printer
//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x40},2);
            // enhance the heating time
//            PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x37, 7,(byte)240,2},5);
        PrinterInterface.PrinterWrite(SELECT_BIT_IMAGE_MODE,SELECT_BIT_IMAGE_MODE.length);
        // upgrade the print destiny
//        PrinterInterface.PrinterWrite(new byte[]{0x12, 0x23, 0x00 | (28)},3);
        
        // write print bitmap
//        int lines = (result.length - DC2V_HEAD)/WIDTH;
//        System.arraycopy(new byte[]{0x12,0x56,(byte)(lines&0xff),(byte)((lines>>8)&0xff)}, 0, result, 0, DC2V_HEAD);
//        PrinterInterface.PrinterWrite(result, result.length);
        Vector<byte[]> vData=checkBufferLength(result);
        for(int i=0;i<vData.size();i++){
            byte[] temp=vData.elementAt(i);
            int lines=temp.length/WIDTH;
            PrinterInterface.PrinterWrite(new byte[]{0x12,0x56,(byte)(lines&0xff),(byte)((lines>>8)&0xff)},4 );
            PrinterInterface.PrinterWrite(temp,temp.length);
        }
        
//         restore the heating time
        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x37, 7,(byte)0x80,2},5);

//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x21, 0},3);
//        PrinterInterface.PrinterWrite("\n\n\n".getBytes(),3);
//      PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x4A, 8},3);
//        // reset the printer
//        PrinterInterface.PrinterWrite(new byte[]{0x1B, 0x40},2);
       
//        PrinterInterface.PrinterEnd();
//        PrinterInterface.PrinterClose();
    }

    private static void printBitmapDV2VMSBslow(Bitmap bmp){
        BitSet dots = convertBitmap(bmp);
        int offset = 0;
        PrinterInterface.PrinterWrite(PrinterCommand.SET_LINE_SPACING_24,PrinterCommand.SET_LINE_SPACING_24.length);

        while (offset < bmp.getHeight()) {
            PrinterInterface.PrinterWrite(PrinterCommand.SELECT_BIT_IMAGE_MODE,PrinterCommand.SELECT_BIT_IMAGE_MODE.length);
            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * bmp.getWidth()];
            for (int x = 0; x < bmp.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * bmp.getWidth()) + x;
                        boolean v = false;
                        if (i < dots.length()) {
                            v = dots.get(i);
                        }
                        Log.i("PRINT_DOT",Boolean.toString(v));
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }
                    imageDataLine[imageDataLineIndex + k] = slice;
                }
                imageDataLineIndex += 3;
            }
            PrinterInterface.PrinterWrite(imageDataLine,imageDataLine.length);
            offset += 24;
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
            PrinterInterface.PrinterWrite(PrinterCommand.FEED_LINE,PrinterCommand.FEED_LINE.length);
        }
//        PrinterInterface.PrinterWrite(PrinterCommand.SET_LINE_SPACING_30,PrinterCommand.SET_LINE_SPACING_30.length);
    }

    private static void printSmallFont(String msg){
        byte [] contentbytes = null;
//		display small font
        byte [] cmds = new byte[]{ 0x1B, 0x21, 0x01};
        PrinterInterface.PrinterWrite(cmds, cmds.length );
        contentbytes = msg.getBytes();
        PrinterInterface.PrinterWrite(contentbytes, contentbytes.length );
    }


    public static BitSet convertBitmap(Bitmap inputBitmap) {

        int mWidth = inputBitmap.getWidth();
        int mHeight = inputBitmap.getHeight();
        return convertArgbToGrayscale(inputBitmap, mWidth, mHeight);

    }

    private static BitSet convertArgbToGrayscale(Bitmap image, int width,
                                        int height) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {

                int color = image.getPixel(x,y);
                int  red = (color & 0x00ff0000) >> 16;
                int  green = (color & 0x0000ff00) >> 8;
                int  blue = color & 0x000000ff;
                int luminance = (int)(red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }

    /**
     * print the bitmap by GS v 0 p wL wH hL hH
     * 
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return
     */
    private static void printBitmapGSVMSB(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result=generateBitmapArrayGSV_MSB(bm, bitMarginLeft, bitMarginTop);
        
//        int resultJni = PrinterInterface.PrinterOpen();
//        if(resultJni <0){
//            Log.e("App", "don't  open twice this devices");
//            PrinterInterface.PrinterClose();
//            return ;
//        }
//        PrinterInterface.PrinterBegin();

        int lines=(result.length - GSV_HEAD)/WIDTH;
        System.arraycopy(new byte[]{0x1D,0x76, 0x30, 0x00,0x30,0x00,(byte)(lines&0xff),(byte)((lines>>8)&0xff)},0,result,0,GSV_HEAD );
        
        
        Log.d("printer", StringUtil.toBestString(result));
        PrinterInterface.PrinterWrite(result,result.length);
//        PrinterInterface.PrinterEnd();
//        PrinterInterface.PrinterClose();
    }
    /**
     * generate the MSB buffer for bitmap printing DC2 V nL nH
     * 
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return buffer with DC2V_HEAD + image length
     */
    private static byte[] generateBitmapArrayDC2V_MSB(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result = null;
        int n = bm.getHeight() + bitMarginTop;
        result = new byte[n * WIDTH + DC2V_HEAD];
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                if (x + bitMarginLeft < BIT_WIDTH) {
                    int color=bm.getPixel(x, y);
                    int alpha=Color.alpha(color);
                    int red=Color.red(color);
                    int green=Color.green(color);
                    int blue=Color.blue(color);
                    if (red < 128) {
                        int bitX = bitMarginLeft + x;
                        int byteX = bitX / 8;
                        int byteY = y + bitMarginTop;
//                        result[ DC2V_HEAD + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                        result[ DC2V_HEAD + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                    }
                } else {
                    // ignore the rest data of this line
                    break;
                }
            }
        }
        return result;
    }

    private static byte[] generateBitmapArray(Bitmap bmp, int bitMarginLeft, int bitMarginTop) {
        byte[] result = null;
        int n = bmp.getHeight() + bitMarginTop;
        result = new byte[n * WIDTH + DC2V_HEAD];
        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                if (x + bitMarginLeft < BIT_WIDTH) {
                    int color=bmp.getPixel(x, y);
                    int alpha=Color.alpha(color);
                    int red=Color.red(color);
                    int green=Color.green(color);
                    int blue=Color.blue(color);
                    if (red < 128) {
                        int bitX = bitMarginLeft + x;
                        int byteX = bitX / 8;
                        int byteY = y + bitMarginTop;
//                        result[ DC2V_HEAD + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                        result[ DC2V_HEAD + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                    }
                } else {
                    // ignore the rest data of this line
                    break;
                }
            }
        }
        return result;
    }
    /**
     * generate the MSB buffer for bitmap printing GSV command
     * 
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return buffer with DC2V_HEAD + image length
     */
    private static byte[] generateBitmapArrayGSV_MSB(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result = null;
        int n = bm.getHeight() + bitMarginTop;
        int offset=GSV_HEAD;
        result = new byte[n * WIDTH + offset];
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                if (x + bitMarginLeft < BIT_WIDTH) {
                    int color=bm.getPixel(x, y);
                    int alpha=Color.alpha(color);
                    int red=Color.red(color);
                    int green=Color.green(color);
                    int blue=Color.blue(color);
                    if (red < 128) {
                        int bitX = bitMarginLeft + x;
                        int byteX = bitX / 8;
                        int byteY = y + bitMarginTop;
                        result[ offset + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                    }
                } else {
                    // ignore the rest data of this line
                    break;
                }
            }
        }
        return result;
    }
    /**
     * generate the MSB buffer for bitmap printing ESC * m n1 n2 d1 d2 … dk
     *                                             1B 2A m n1 n2 d1 d2 … dk
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return
     */
    public static void printBitmapESCStar(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        Vector<byte[]> vData=generateCmdBitmapArrayESCStar(bm, bitMarginLeft, bitMarginTop);
//        tracelogCmdBitmapArrayESCStar(vData);
        
//        PrinterInterface.PrinterOpen();
//        PrinterInterface.PrinterBegin();
        
        for(int i=0;i<vData.size();i++){
            byte[] temp=vData.elementAt(i);
            if(i>0){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            PrinterInterface.PrinterWrite(temp,temp.length);
        }
        //print several line text
        String text="This is a text tset...";
        byte[] bufText=text.getBytes();
        PrinterInterface.PrinterWrite(bufText, bufText.length );
        PrinterInterface.PrinterWrite("\n".getBytes(), 1);

//        PrinterInterface.PrinterEnd();
//        PrinterInterface.PrinterClose();
    }
    /**
     * generate the MSB buffer for bitmap printing ESC * m n1 n2 d1 d2 … dk<br/>
     *                                             1B 2A m n1 n2 d1 d2 … dk<br/>
     *                                               m =0x00,0x01,0x20, 0x21。<br/>
     *                                               n1=0-255<br/>
     *                                               n2=0-3<br/>
     *                                               dx=0-255<br/>
     *                                               k = n1+256*n2 (m=0,1)<br/>
     *                                               k = (n1+256*n2)*3 (m=32,33)<br/>
     *                                               m用于选择点图方式。<br/>
     *                                               0x00: 高度8点，水平方向需放大一倍<br/>
     *                                               0x01: 高度8点，水平方向不需放大<br/>
     *                                               0x20: 高度24点，水平方向需放大一倍<br/>
     *                                               0x21: 高度24点，水平方向不需放大<br/>
     *                                               点图顺序请参照自定义字符命令<br/>
     *                                               B0 B3 B6 ...      Bx: b7<br/>
     *                                               B1 B4 B7 ...          b6<br/>
     *                                               B2 B5 B8 ...          b5<br/>
     *                                                                     b4<br/>
     *                                                                     b3<br/>
     *                                                                     b2<br/>
     *                                                                     b1<br/>
     *                                                                     b0<br/>
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return  the vector of the byte[], each one means one block, 8 linesx384 or 24 linesx384
     */
    private static Vector<byte[]> generateCmdBitmapArrayESCStar(Bitmap bm, int bitMarginLeft,
            int bitMarginTop) {
        Vector<byte[]> v = new Vector<byte[]>();
        int line = 0;
        byte[] block = null;
        int n = bm.getHeight() + bitMarginTop;
        int pxHeight = bm.getHeight();
        int pxWidth = bm.getWidth();
        int blockWidth = BIT_WIDTH;
//        int blockWidth = pxWidth;
        while (line < pxHeight) {
            int blockHeight = 0;
//            if (line + 16 < pxHeight) {
//                // use 24 block
//                blockHeight = 24;
//                v.add(new byte[] {
//                        0x1B, 0x2A, 0x21, (byte) (blockWidth & 0xff),
//                        (byte) ((blockWidth >> 8) & 0xff)
//                });
//                block = new byte[3 * blockWidth];
//            } else {
//                // use 8 block
//                blockHeight = 8;
//                v.add(new byte[] {
//                        0x1B, 0x2A, 0x01, (byte) (blockWidth & 0xff),
//                        (byte) ((blockWidth >> 8) & 0xff)
//                });
//                block = new byte[blockWidth];
//            }
            // use 24 block
            blockHeight = 24;
            v.add(new byte[] {
                    0x1B, 0x2A, 0x21, (byte) (blockWidth & 0xff),
                    (byte) ((blockWidth >> 8) & 0xff)
            });
            block = new byte[3 * blockWidth];
            for (int y = 0; y + line < pxHeight && y < blockHeight; y++) {
                for (int x = 0; x < pxWidth; x++) {
                    int posBit = x * blockHeight + y;
                    int posByte = posBit/8;
                    // 每个byte中，从高位开始的第几个点
                    int posBitInByteLeft = posBit % 8;
                    if (x < BIT_WIDTH) {
                        int color = bm.getPixel(x, y + line);
                        int alpha = Color.alpha(color);
                        int red = Color.red(color);
                        int green = Color.green(color);
                        int blue = Color.blue(color);
                        if (red < 128) {
//                             Log.d("PrintPNG", x+","+y+": byte="+posByte+" bitleft="+posBitInByteLeft);
                            block[posByte] |= (0x80 >> posBitInByteLeft);
                        }
                    } else {
                        // ignore the rest data of this line
                        break;
                    }
                }
            }
            v.add(block);
            v.add(new byte[] {
                    0x0A
                });
            line += blockHeight;
        }
        return v;
    }

    private static void tracelogCmdBitmapArrayESCStar(Vector<byte[]> vData) {
        StringBuffer[] arysbBlock = null;
        int blockWidth = 0, m = 0;
        for (int v = 0; v < vData.size(); v++) {
            byte[] buffer = vData.elementAt(v);
            if (buffer.length < 5) {
                // skip other command, e.g. 0A
                continue;
            }else if(buffer.length==5){
                // it's the control command
                if (arysbBlock != null) {
                    // print block
                    for (int i = 0; i < arysbBlock.length; i++) {
                        Log.d("PrintPNG", arysbBlock[i].toString());
                    }
                }
                m = buffer[2];
                blockWidth = (buffer[3] & 0xff) + (buffer[4] & 0xff) * 256;
                if (m == 0x01) {
                    arysbBlock = new StringBuffer[8];
                } else if (m == 0x21) {
                    arysbBlock = new StringBuffer[24];
                }
                // reset the StringBuffers
                for (int i = 0; i < arysbBlock.length; i++) {
                    arysbBlock[i] = new StringBuffer();
                }
                continue;
            }
            int i = 0;
            while (i < buffer.length) {
                byte b = buffer[i];
                for (int pos = 0; pos < 8; pos++) {
                    if ((b & (0x80 >> pos)) != 0) {
                        arysbBlock[pos].append('*');
                    } else {
                        arysbBlock[pos].append(' ');
                    }
                }
                i++;
                if (m == 0x21) {
                    b = buffer[i];
                    for (int pos = 0; pos < 8; pos++) {
                        if ((b & (0x80 >> pos)) != 0) {
                            arysbBlock[pos + 8].append('*');
                        } else {
                            arysbBlock[pos + 8].append(' ');
                        }
                    }
                    i++;
                    b = buffer[i];
                    for (int pos = 0; pos < 8; pos++) {
                        if ((b & (0x80 >> pos)) != 0) {
                            arysbBlock[pos + 16].append('*');
                        } else {
                            arysbBlock[pos + 16].append(' ');
                        }
                    }
                    i++;
                }
            }
        }
        if (arysbBlock != null) {
            // print block
            for (int i = 0; i < arysbBlock.length; i++) {
                Log.d("PrintPNG", arysbBlock[i].toString());
            }
        }
    }
    /**
     * generate the dot array buffer for ESC & s n m w command
     * 
     * @param bm the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop the top white space in bits.
     * @return
     */
    private static byte[] generateBitmapArrayDot8(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        byte[] result = null;
        
        return result;
    }

    private static void tracelogMSB(byte[] bufMSB, int widthBytes) {
        StringBuffer sbline = new StringBuffer();
        for (int i = 0; i < bufMSB.length; i++) {
            if (i % widthBytes == 0) {
                Log.d("PrintPNG", sbline.toString());
                sbline = new StringBuffer();
            }
            byte b = bufMSB[i];
            for (int pos = 0; pos < 8; pos++) {
                if ((b & (0x80 >> pos)) != 0) {
                    sbline.append('*');
                } else {
                    sbline.append(' ');
                }
            }
        }
    }
    
    private static Vector<byte[]> checkBufferLength(byte[] buffer){
        Vector<byte[]> v=new Vector<byte[]>();
        int byteLimit=DOT_LINE_LIMIT * WIDTH;
        if(buffer.length <= byteLimit){
            v.add(buffer);
            return v;
        }else{
            int offset=DC2V_HEAD;
            while(offset<buffer.length){
                byte[] buftemp=new byte[offset + byteLimit < buffer.length?byteLimit:(buffer.length-offset)];
                System.arraycopy(buffer, offset, buftemp, 0, buftemp.length);
                v.add(buftemp);
                offset+=buftemp.length;
            }
            return v;
        }
    }

    private static Vector<byte[]> slice(byte[] buffer){
        Vector<byte[]> v=new Vector<byte[]>();
        int byteLimit=DOT_LINE_LIMIT * WIDTH;
        if(buffer.length <= byteLimit){
            v.add(buffer);
            return v;
        }else{
            int offset=DC2V_HEAD;
            while(offset<buffer.length){
                byte[] buftemp=new byte[offset + byteLimit < buffer.length?byteLimit:(buffer.length-offset)];
                System.arraycopy(buffer, offset, buftemp, 0, buftemp.length);
                v.add(buftemp);
                offset+=buftemp.length;
            }
            return v;
        }
    }

}
