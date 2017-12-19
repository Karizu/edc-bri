package com.wizarpos.function.printer;

import android.graphics.Bitmap;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;

import java.io.UnsupportedEncodingException;
//import com.wizarpos.drivertest.util.StringUtility;

public class PrinterHelper {
    /* ç­‰å¾…æ‰“å�°ç¼“å†²åˆ·æ–°çš„æ—¶é—´ */
    // private static final int PRINTER_BUFFER_FLUSH_WAITTIME = /*300*/150;

    /**
     * ç”¨æ�¥æŽ§åˆ¶æ‰“å�°ä»»åŠ¡çš„å�•ç‹¬æ€§
     */
    public static boolean isPrint = false;

    private static PrinterHelper _instance;

    private PrinterHelper() {
    }

    synchronized public static PrinterHelper getInstance() {
        if (null == _instance)
            _instance = new PrinterHelper();
        return _instance;
    }


    /**
     * æ‰“å�°ä¸€ä¸ªå‘½ä»¤ ä¸�æ�¢è¡Œ
     */
    public int printText(String string) {
        byte[] text = null;
        try {
            text = string.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int total = 0;
        total += printText(text);
        return total;
    }

    /**
     * æ‰“å�°ä¸€ä¸ªå‘½ä»¤ å¹¶æ�¢è¡Œ
     */
    public int printText2(String string) {
        byte[] text = null;
        try {
            text = string.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int total = 0;
        total += printText(text);
		/* æ�¢è¡Œ */
        total += printText(PrinterCommand.getCmdLf());
        return total;
    }

    /**
     * æ‰“å�°å­—ä½“ åŠ ç²— åŠ å®½ åŠ é«˜ å�„ä¸€å€�
     *
     * @param string
     * @param cmd
     * @return
     */
    public int printBigText(String string, byte[] cmd) {
        int total = 0;
		/* å�–æ¶ˆä¸­æ–‡å°�å­—ä½“ */
        total += printText(PrinterCommand.getCmdCancelSmallFont_CN());
		/* å�–æ¶ˆè‹±æ–‡å°�å­—ä½“ */
        total += printText(PrinterCommand.getCmdCancelSmallFont_EN());
		/* è®¾ç½®å­—ä½“åŠ ç²—*/
//		total += printText(PrinterCommand.getCmdEscEN(1));
        total += printText2(string);

//		/* å­—ä½“å�Œå€�å®½åº¦ï¼Œå�Œå€�é«˜åº¦ */
//		total += printText(cmd);
//		total += printText2(string);
//		/* æ�¢å¤�é»˜è®¤å­—ä½“ */
//		total += printText(PrinterCommand.getCmdEsc_N(Integer.parseInt("0000000", 2)));
		/* è®¾ç½®ä¸­æ–‡å°�å­—ä½“ */
        total += printText(PrinterCommand.getCmdSetSmallFont_CN());
		/* è®¾ç½®è‹±æ–‡å°�å­—ä½“ */
        total += printText(PrinterCommand.getCmdSetSmallFont_EN());
		/* å�–æ¶ˆå­—ä½“åŠ ç²—*/
//		total += printText(PrinterCommand.getCmdEscEN(0));
        return total;
    }

    public int printText(byte[] text) {
        if (text == null || text.length == 0) {
            // æ‰“å�°ç©ºå­—ç¬¦

        } else {
            return PrinterInterface.PrinterWrite(text, text.length);
        }
        return -1;
    }

    /**
     * æ‰“å�°ä¸¤ä¸ªå­—ç¬¦å¹¶æ�¢è¡Œ
     *
     * @param string1
     * @param string2
     * @return
     */
    public int printText(String string1, String string2) {
        int total = 0;
        if (string1 == null && string2 == null) {
			/* æ�¢è¡Œ */
            total += printText(PrinterCommand.getCmdLf());
        } else {
            byte[] text1 = null;
            byte[] text2 = null;
            try {
                if (string1 != null) {
                    text1 = string1.getBytes("GB2312");
                }
                if (string2 != null) {
                    text2 = string2.getBytes("GB2312");
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (string1 != null) {
                total += printText(text1);
            }
            if (string2 != null) {
				/* è®¾ç½®å�³å¯¹é½� */
                total += printText(PrinterCommand.getCmdEscAN(2));
                total += printText(text2);
            }
			
			/* æ�¢è¡Œ */
            total += printText(PrinterCommand.getCmdLf());
			/* è®¾ç½®å·¦å¯¹é½� */
            total += printText(PrinterCommand.getCmdEscAN(0));
        }
        return total;
    }


    /**
     * æ‰“å�°å›¾ç‰‡
     *
     * @param bm
     * @param bitMarginLeft
     * @param bitMarginTop
     */

    synchronized public void printBitmap(Bitmap bm, int bitMarginLeft, int bitMarginTop) {
        if (isPrint == true) {
            return;
        }
        isPrint = true;
        Log.e("aotTag", "------------------printBitmap()------------------");
        try {
            PrinterBitmapUtil.printBitmap(bm, bitMarginLeft, bitMarginTop);

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            isPrint = false;
        }

    }

    synchronized public int printQuery() {
        try {
            int result = PrinterInterface.PrinterOpen();
            if (result < 0) {
                return -1;
            } else {
                return PrinterInterface.PrinterQuery();
            }

        } catch (Exception e) {
            // TODO: handle exception
            return -2;
        } finally {
            PrinterInterface.PrinterClose();
            // return -3;
        }

    }

    synchronized public void printSelfTestPage() {
        if (isPrint == true) {
            return;
        }
        isPrint = true;
        int nTotal = 0;

        try {
            int result = PrinterInterface.PrinterOpen();
            if (result < 0) {
                return;
            }
            PrinterInterface.PrinterBegin();

			/* åˆ�å§‹åŒ–æ‰“å�°æœº */
            nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdEsc_(), PrinterCommand.getCmdEsc_().length);
			/* å�‘å‰�èµ°çº¸2è¡Œ */
            nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdEscDN(2), PrinterCommand.getCmdEscDN(2).length);
            byte[] command = new byte[]{(byte) 0x12, (byte) 0x54};
            nTotal += PrinterInterface.PrinterWrite(command, command.length);

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            isPrint = false;
            PrinterInterface.PrinterEnd();
            PrinterInterface.PrinterClose();
        }
    }

    /**
     * èµ°çº¸ä¸‰è¡Œ ä¸�å�«open begin end close
     */

    synchronized public void zouzhi(int sum) {
        // PrinterInterface.PrinterOpen();
        // PrinterInterface.PrinterBegin();
        try {
            PrinterInterface.PrinterWrite(PrinterCommand.getCmdEscDN(sum), PrinterCommand.getCmdEscDN(sum).length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // PrinterInterface.PrinterEnd();
            // PrinterInterface.PrinterClose();
        }
    }

    synchronized public void qiezhi() {
        try {
            PrinterInterface.PrinterOpen();
            PrinterInterface.PrinterBegin();
            PrinterInterface.PrinterWrite(PrinterCommand.aaa(), PrinterCommand.aaa().length);

        } catch (Exception e) {
        } finally {
            PrinterInterface.PrinterEnd();
            PrinterInterface.PrinterClose();
        }
    }
}
