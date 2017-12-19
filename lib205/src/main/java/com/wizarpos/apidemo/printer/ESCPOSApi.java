package com.wizarpos.apidemo.printer;

import android.graphics.Bitmap;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by indra on 30/11/15.
 */
public class ESCPOSApi {

    public static final byte[] INITIALIZE_PRINTER = new byte[]{0x1B, 0x40};

    public static final byte[] PRINT_AND_FEED_PAPER = new byte[]{0x0A};

    public static final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 0x1B, (byte) 0x2A};
    public static final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};
    public static final byte[] CENTER_ALIGN = {0x1B, 0x61, 1};
    public static final byte[] LEFT_ALIGN = {0x1B, 0x61, 0};
    public static final byte[] RIGHT_ALIGN = {0x1B, 0x61, 2};
    public static final byte[] TEXT_BOLD = {0x1B,0x45};

    public static final byte[] LINE_SPACING_24DOTS = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
    public static final byte[] LINE_SPACING_30DOTS = buildPOSCommand(SET_LINE_SPACING, (byte) 30);


    public static int maxBitsWidth = 255;

    public static byte[] setLineSpacing(int size){
        return buildPOSCommand(SET_LINE_SPACING, (byte) size);
    }

    private static byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];

        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);

        return posCommand;
    }

    private static BitSet getBitsImageData(Bitmap image) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixel(x, y);
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = color & 0x000000ff;
                int luminance = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }

    public static void printImage(Bitmap image) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }


        printCommands(LINE_SPACING_30DOTS);
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printStruk(Bitmap image,List<PrintSize>data) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }
        printCommands(LINE_SPACING_30DOTS);
        printCommands("\n");
        printCommandsSmall("MERCHANT LOREM IPSUM\n");
        printCommandsSmall("JL. ANTAH BERANTAH\n");
        printCommandsSmall("NO 34A\n");
        printCommandsSmall("KOTA BERANTAH\n");
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        String sameLine = addSpaceBetween("TID : 123456","MID : 123456789",false);
        printCommandsSmall(sameLine + "\n");
        sameLine = addSpaceBetween("BATCH : 000000","TRACE NO : 002102",false);
        printCommandsSmall(sameLine+"\n");
        Date d = new Date();
        sameLine = addSpaceBetween("TANGGAL : "+printDate(d),"JAM : "+printTime(d),false);
        printCommandsSmall(sameLine + "\n");
        printCommands("\n");
        for(PrintSize pz : data){
//            Log.d("zzzz", pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            }
            if (pz.getMessage().equals("Info Kuota Bansos")) {}
            else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands(CENTER_ALIGN);
        printCommandsSmall("----CUSTOMER COPY----\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printStruk(Bitmap image,List<PrintSize>data,List<String> mdata,
                                  String tid, String mid, String stan, int pcopy, String svrRef,
                                  String svrDate, String svrTime, String cardType, String cardNumber,
                                  String screenLoader, String batchNumber, String svrAppr) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }
        printCommands(LINE_SPACING_30DOTS);
        printCommands("\n");
        for (String md : mdata) {
            printCommandsSmall(md+"\n");
        }
//        printCommandsSmall("MERCHANT LOREM IPSUM\n");
//        printCommandsSmall("JL. ANTAH BERANTAH\n");
//        printCommandsSmall("NO 34A\n");
//        printCommandsSmall("KOTA BERANTAH\n");
        if (pcopy>2) {
            printCommands("\n");
            printCommandsSmall("**********DUPLICATE**********\n");
        }
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        String sameLine = addSpaceBetween("TERMINAL ID : ", tid, false);
        printCommandsSmall(sameLine + "\n");
        sameLine = addSpaceBetween("MERCHANT ID : ", mid, false);
        printCommandsSmall(sameLine + "\n");
        printCommandsSmall("\n");
        if (!cardNumber.equals("")) {
            printCommandsSmall(cardType + "\n");
            if (!cardType.contains("FLY")) {
                printBoldCommands(" "+cardNumber + "\n");
            }
            printCommandsSmall("\n");
        }
        Date d = new Date();
        String strDate = printDate(d);
        String strTime = printTime(d);
        if (svrDate.length()==4) {
            strDate = grabDate(d, svrDate);
        } else if (svrDate.length()==10) {
            strDate = grabSQLDate(d, svrDate);
        }
        if (svrTime.length()==6) {
            strTime = grabTime(d, svrTime);
        } else if (svrTime.length()==8) {
            strTime = svrTime;
        }
        printCommandsSmall(strDate + ", " +strTime + "\n");
        sameLine = addSpaceBetween("BATCH  : " + batchNumber,"TRACE NO : " + stan + "  ",false);
        printCommandsSmall(sameLine + "\n");
        if (true) {
//            if (!svrRef.equals("000000000000")) {
            sameLine = addSpaceBetween("REF NO : " + svrRef, "APPR : " + svrAppr, false);
            printCommandsSmall(sameLine + "\n");
        }
        for(PrintSize pz : data){
//            Log.d("zzzz",pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            }
//            else if (pz.getMessage().equalsIgnoreCase("Bantuan Sosial - Pencairan Tunai\n") || pz.getMessage().equalsIgnoreCase("Info Kuota Bansos\n") || pz.getMessage().equalsIgnoreCase("Bantuan Sosial - Pembelian\n")) {
//                Log.d("TE", "SKIP TITLE");
//            }
            else if (pz.getMessage().contains("INFO KUOTA BERHASIL")) {
                String content = pz.getMessage().substring(0, pz.getMessage().indexOf("INFO KUOTA BERHASIL"));
                PrintSize cPz = new PrintSize(FontSize.NORMAL, content);
                printCommands(cPz);
                printCommands(CENTER_ALIGN);
                PrintSize fPz = new PrintSize(FontSize.NORMAL, pz.getMessage().replace(content,""));
                printCommands(fPz);
            }
            else if (pz.getMessage().contains("Transaksi Berhasil")) {
                String content = pz.getMessage().substring(0, pz.getMessage().indexOf("Transaksi Berhasil"));
                PrintSize cPz = new PrintSize(FontSize.NORMAL, content);
                printCommands(cPz);
                printCommands(CENTER_ALIGN);
                PrintSize fPz = new PrintSize(FontSize.NORMAL, pz.getMessage().replace(content,""));
                printCommands(fPz);
            }
            else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands(CENTER_ALIGN);
        String copyTypeText = "--DUPLICATE COPY--\n";
        if (pcopy==0) {
            copyTypeText = "--CUSTOMER COPY--\n";
            if (screenLoader.equals("71000FF")||screenLoader.equals("721000F")||screenLoader.equals("731000F")) {
                copyTypeText = "--AGENT COPY--\n";
            }
        } else if (pcopy==1) {
            copyTypeText = "--BANK COPY--\n";
        } else if (pcopy==2) {
            copyTypeText = "--MERCHANT COPY--\n";
            if (screenLoader.equals("71000FF")||screenLoader.equals("721000F")||screenLoader.equals("731000F")) {
                copyTypeText = "--CUSTOMER COPY--\n";
            }
        } else {
            copyTypeText = "--DUPLICATE COPY--\n";
        }
        printCommandsSmall(copyTypeText);
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printSettlement(Bitmap image,List<PrintSize>data,List<String> mdata,
                                       String tid, String mid, String stan, String svrDate, String svrTime) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }
        printCommands(LINE_SPACING_30DOTS);
        printCommands("\n");
        for (String md : mdata) {
            printCommandsSmall(md+"\n");
        }
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        printCommandsSmall(addSpaceBetween("TERMINAL ID  : ", tid, false) + "\n");
        printCommandsSmall(addSpaceBetween("MERCHANT ID  : ", mid, false) + "\n");
        printCommandsSmall("\n");
        Date d = new Date();
        String strDate = printDate(d);
        String strTime = printTime(d);
        if (svrDate.length()==4) {
            strDate = grabDate(d, svrDate);
        }
        if (svrTime.length()==6) {
            strTime = grabTime(d, svrTime);
        }
        printCommandsSmall("DATE/TIME    : " + strDate + " " + strTime + "\n");
        printCommands("\n");
        for(PrintSize pz : data){
            Log.d("zzzz",pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if(pz.getMessage().contains(":")) {
                printCommandsSmall(setHalfSide(pz.getMessage(), false, false));
            } else if(pz.getMessage().contains("Rp")) {
                printCommandsSmall(setHalfSide(pz.getMessage(), false, true));
            } else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printReport(Bitmap image,List<PrintSize>data,List<String> mdata, String tid, String mid, String stan) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }
        printCommands(LINE_SPACING_30DOTS);
        printCommands("\n");
        for (String md : mdata) {
            printCommandsSmall(md+"\n");
        }
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        printCommandsSmall(addSpaceBetween("TERMINAL ID     : ", tid, false) + "\n");
        printCommandsSmall(addSpaceBetween("MERCHANT ID     : ", mid, false) + "\n");
        printCommandsSmall("\n");
        Date d = new Date();
        printCommandsSmall("PRINT DATE/TIME : " + printDate(d) + " " + printTime(d) + "\n");
        if (!stan.equals("")) {
            printCommandsSmall("REPORT DATE     : " + stan + "\n");
        }
        printCommands("\n");
        for(PrintSize pz : data){
            Log.d("zzzz",pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if(pz.getMessage().contains(":")) {
                printCommandsSmall(setHalfSide(pz.getMessage(), false, false));
            } else if(pz.getMessage().contains("Rp")) {
                printCommandsSmall(setHalfSide(pz.getMessage(), false, true));
            } else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    public static void printDetailReport(Bitmap image,List<PrintSize>data,List<String> mdata, String tid, String mid, String stan) {
        BitSet imageBits = getBitsImageData(image);

        byte widthLSB = (byte) (image.getWidth() & 0xFF);
        byte widthMSB = (byte) ((image.getWidth() >> 8) & 0xFF);

        // COMMANDS
        byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);

        int resultJni = PrinterInterface.PrinterOpen();
        if (resultJni < 0) {
//            Log.e("App", "don't  open twice this devices");
            PrinterInterface.PrinterClose();
            return;
        }
        PrinterInterface.PrinterBegin();
        printCommands(INITIALIZE_PRINTER);
        printCommands(LINE_SPACING_24DOTS);
        printCommands(CENTER_ALIGN);
        int offset = 0;
        while (offset < image.getHeight()) {
            printCommands(selectBitImageModeCommand);

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * image.getWidth()) + x;
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;
                }

                imageDataLineIndex += 3;
            }

            printCommands(imageDataLine);
            offset += 24;
            printCommands(PRINT_AND_FEED_PAPER);
        }
        printCommands(LINE_SPACING_30DOTS);
        printCommands("\n");
        for (String md : mdata) {
            printCommandsSmall(md+"\n");
        }
        printCommands("\n");
        printCommands(LEFT_ALIGN);
        printCommandsSmall(addSpaceBetween("TERMINAL ID     : ", tid, false) + "\n");
        printCommandsSmall(addSpaceBetween("MERCHANT ID     : ", mid, false) + "\n");
        Date d = new Date();
        printCommandsSmall("PRINT DATE/TIME : " + printDate(d) + " " + printTime(d) + "\n");
        if (!stan.equals("")) {
            printCommandsSmall("REPORT DATE     : " + stan + "\n");
        }
        printCommands("\n");
        for(PrintSize pz : data){
            Log.d("zzzz",pz.getMessage());
            if (pz.getMessage().equals("START_FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("START FOOTER")) {
                printCommands(CENTER_ALIGN);
            } else if (pz.getMessage().equals("STOP_FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if (pz.getMessage().equals("STOP FOOTER")) {
                printCommands(LEFT_ALIGN);
            } else if(pz.getMessage().contains("|:")) {
                printCommandsSmall(setHalfDetailSide(pz.getMessage(), false, false));
            } else if(pz.getMessage().contains(":|")) {
                printCommandsSmall(setHalfDetailSide(pz.getMessage(), false, true));
            } else {
                printCommands(pz);
            }
        }
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        printCommands("\n");
        PrinterInterface.PrinterEnd();
        PrinterInterface.PrinterClose();
    }

    private static String printDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(d);
    }

    private static String printTime(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    private static String grabSQLDate(Date d, String svrDate) {
        SimpleDateFormat idf = new SimpleDateFormat("yyyyMMdd");
        Date sv = new Date();
        try {
            sv = idf.parse(svrDate);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(sv);
    }

    private static String grabDate(Date d, String svrDate) {
        SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
        String year = ydf.format(d) + svrDate;
        SimpleDateFormat idf = new SimpleDateFormat("yyyyMMdd");
        Date sv = new Date();
        try {
            sv = idf.parse(year);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(sv);
    }

    private static String grabTime(Date d, String svrTime) {
        SimpleDateFormat idf = new SimpleDateFormat("HHmmss");
        Date sv = new Date();
        try {
            sv = idf.parse(svrTime);
        } catch (Exception e) {
            return printDate(d);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(sv);
    }

    private static String printFullDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        return sdf.format(d);
    }

    private static String addSpaceBetween(String txt1,String txt2, boolean normal){
        String space = " ";
//        int lineSize = 32;
//        if(!normal){
//            lineSize = 42;
//        }
        int lineSize = 30;
        if(!normal){
            lineSize = 40;
        }
        int totalLen = txt1.length()+txt2.length();
        for(int i = 0;i<lineSize;i++){
            totalLen = txt1.length()+txt2.length()+space.length();
            if(totalLen == lineSize)
                break;
            space += " ";
        }
        return txt1+space+txt2;
    }

    private static String setHalfSide(String txt, boolean normal, boolean right) {
        String space = " ";
        int lineSize = 30;
        if(!normal) {
            lineSize = 40;
        }
        int totalLen = lineSize/2;
        String cnt = "";
        if (right) {
            totalLen = 10;
            txt = txt.substring(3);
        } else {
            totalLen = 23;
            cnt = "   " + txt.substring(txt.indexOf(":")+1);
            cnt = cnt.substring(cnt.length()-3);
            txt = txt.substring(0, txt.indexOf(":") - 1);
        }
        for (int i = 0;i < totalLen; i++) {
            if (right) {
                txt = space + txt;
            } else {
                txt = txt + space;
            }
        }
        if (right) {
            txt = txt.substring(txt.length()-totalLen);
        } else {
            txt = txt.substring(0, totalLen) + cnt + "  Rp";
        }
        return txt;
    }

    private static String setHalfDetailSide(String txt, boolean normal, boolean right) {
        String space = " ";
        int lineSize = 30;
        if(!normal) {
            lineSize = 40;
        }
        int totalLen = lineSize/2;
        if (right) {
            txt = txt.substring(2);
        } else {
            txt = txt.substring(0, txt.indexOf("|:"));
        }
        for (int i = 0;i < totalLen; i++) {
            if (right) {
                txt = space + txt;
            } else {
                txt = txt + space;
            }
        }
        if (right) {
            txt = txt.substring(txt.length()-totalLen);
        } else {
            txt = txt.substring(0, totalLen);
        }
        return txt;
    }

    private static void printCommands(byte[] data){
        PrinterInterface.PrinterWrite(data,data.length);
    }

    private static void printCommands(String data){
        byte[] cmds = new byte[]{0x1B, 0x21, 0x00};
        printCommands(cmds);
        data = "  " + data;
        printCommands(data.getBytes());
    }

    private static void printBoldCommands(String data) {
        printCommands(new PrintSize(FontSize.BOLD_2, data));
    }

    private static void printCommands(PrintSize pz){
        printCommands(pz.getFontSize().getByte());
        printCommands(pz.getMessage().getBytes());
    }

    private static void printCommandsSmall(String data){
        byte[] cmds = new byte[]{0x1B, 0x21, 0x01};
        printCommands(cmds);
        data = " " + data;
        printCommands(data.getBytes());
    }
}