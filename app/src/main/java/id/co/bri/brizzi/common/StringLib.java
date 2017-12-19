package id.co.bri.brizzi.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by indra on 13/01/16.
 */
public class StringLib {
    public static final int LCD_WIDTH = 16;
    /**
     * A table of hex digits
     */
    public static final char[] hexDigit =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String HexChars = "1234567890abcdefABCDEF";
    private String value;

    public StringLib(String value) {
        this.value = value;
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    public static char toHexChar(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    public static String trimSpace(String oldString) {
        if (null == oldString)
            return null;
        if (0 == oldString.length())
            return "";

        StringBuffer sbuf = new StringBuffer();
        int oldLen = oldString.length();
        for (int i = 0; i < oldLen; i++) {
            if (' ' != oldString.charAt(i))
                sbuf.append(oldString.charAt(i));
        }
        String returnString = sbuf.toString();
        sbuf = null;
        return returnString;
    }

    public static String toString(byte abyte0[]) {
        if (null == abyte0)
            return null;
        else
            return new String(abyte0);
    }

    public static String fillString(String formatString, int length,
                                    char fillChar, boolean leftFillFlag) {
        if (null == formatString) {
            formatString = "";
        }
        int strLen = formatString.length();
        if (strLen >= length) {
            if (true == leftFillFlag) // left fill
                return formatString.substring(strLen - length, strLen);
            else
                return formatString.substring(0, length);
        } else {
            StringBuffer sbuf = new StringBuffer();
            int fillLen = length - formatString.length();
            for (int i = 0; i < fillLen; i++) {
                sbuf.append(fillChar);
            }

            if (true == leftFillFlag) // left fill
            {
                sbuf.append(formatString);
            } else {
                sbuf.insert(0, formatString);
            }
            String returnString = sbuf.toString();
            sbuf = null;
            return returnString;
        }
    }

    public static String fillSpace(String formatString, int length) {
        return fillString(formatString, length, ' ', false);
    }

    public static String fillZero(String formatString, int length) {
        return fillString(formatString, length, '0', true);
    }

    /**
     * @param s source string (with Hex representation)
     * @return byte array
     */
    public static byte[] hexString2bytes(String s) {
        if (null == s)
            return null;

        s = trimSpace(s);

        if (false == isHexChar(s, false))
            return null;

        return hex2byte(s, 0, s.length() >> 1);
    }

    /**
     * @param s      source string
     * @param offset starting offset
     * @param len    number of bytes in destination (processes len*2)
     * @return byte[len]
     */
    public static byte[] hex2byte(String s, int offset, int len) {
        byte[] d = new byte[len];
        int byteLen = len * 2;
        for (int i = 0; i < byteLen; i++) {
            int shift = (i % 2 == 1) ? 0 : 4;
            d[i >> 1] |= Character.digit(s.charAt(offset + i), 16) << shift;
        }
        return d;
    }

    private static void appendHex(StringBuffer stringbuffer, byte byte0) {
        stringbuffer.append(toHexChar(byte0 >> 4));
        stringbuffer.append(toHexChar(byte0));
    }

    public static String toHexString(byte abyte0[], int beginIndex,
                                     int endIndex, boolean spaceFlag) {
        if (null == abyte0)
            return null;
        if (0 == abyte0.length)
            return "";
        StringBuffer sbuf = new StringBuffer();
        appendHex(sbuf, abyte0[beginIndex]);
        for (int i = (beginIndex + 1); i < endIndex; i++) {
            if (spaceFlag)
                sbuf.append(" ");
            appendHex(sbuf, abyte0[i]);
        }
        String returnString = sbuf.toString();
        sbuf = null;
        return returnString;
    }

    public static String toHexString(byte abyte0[], boolean spaceFlag) {
        if (null == abyte0)
            return null;
        return toHexString(abyte0, 0, abyte0.length, spaceFlag);
    }

    public static boolean isHexChar(String hexString, boolean trimSpaceFlag) {
        if (null == hexString || 0 == hexString.length())
            return false;

        if (trimSpaceFlag)
            hexString = trimSpace(hexString);

        if (hexString.length() % 2 != 0)
            return false;
        int hexLen = hexString.length();
        for (int i = 0; i < hexLen; i++) {
            if (HexChars.indexOf(hexString.charAt(i)) < 0)
                return false;
        }

        return true;
    }

    public static boolean isHexChar(String hexString) {
        return isHexChar(hexString, true);
    }

    public static StringLib valueOf(String value) {
        return new StringLib(value);
    }

    public static String thousandSeperator(String amount) {
        long longValue = Long.parseLong(amount);
        String fixAmount = String.valueOf(longValue);
        StringBuffer fixedAmount = new StringBuffer();
        fixedAmount.append(fixAmount.substring(0, fixAmount.length()));

        for (int i = fixedAmount.length() - 3; i > 0; i -= 3) {
            fixedAmount.insert(i, '.');
        }
        return "Rp. " + fixedAmount.toString();
    }

    public static String substring(String value, int beginIndex, int length) {
        return value.substring(beginIndex, beginIndex + length);
    }

    public static String getFormatString(byte[] bytes, int length) {
        String value = "";
        for (int i = 0; i < length; i++) {
            value += String.format("%02X ", bytes[i]);
        }
        return value;
    }

    //--yyyymmdd
    public static String getStringDate() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE)) +
                String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));
        return retval;
    }

//    public static final String[] splitString(final String data,
//            final char splitChar, final boolean allowEmpty)
//    {
//        VectorLib v = new VectorLib();
//
//        int indexStart = 0;
//        int indexEnd = data.indexOf(splitChar);
//        if (indexEnd != -1)
//        {
//            while (indexEnd != -1)
//            {
//                String s = data.substring(indexStart, indexEnd);
//                if (allowEmpty || s.length() > 0)
//                {
//                    v.addElement(s);
//                }
//                indexStart = indexEnd + 1;
//                indexEnd = data.indexOf(splitChar, indexStart);
//            }
//
//            if (indexStart != data.length())
//            {
//                // Add the rest of the string
//                String s = data.substring(indexStart);
//                if (allowEmpty || s.length() > 0)
//                {
//                    v.addElement(s);
//                }
//            }
//        }
//        else
//        {
//            if (allowEmpty || data.length() > 0)
//            {
//                v.addElement(data);
//            }
//        }
//
//        String[] result = new String[v.size()];
//        v.copyInto(result);
//        return result;
//    }

    public static String getStringDate2() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE));

        return retval.substring(2);
    }

    public static String getDDMMYY() {
        Calendar c = Calendar.getInstance();
        String retval = String.format("%02d", c.get(Calendar.DATE)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                Integer.toString(c.get(Calendar.YEAR)).substring(2);
        return retval;
    }

    public static String getYYYYMMDD() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) + "-" +
                String.format("%02d", c.get(Calendar.MONTH) + 1) + "-" +
                String.format("%02d", c.get(Calendar.DATE));
        return retval;
    }

    public static String toSQLiteTimestamp(String d, String t) {
        String stime = d + " " + t.substring(0, 2) + ":" + t.substring(2, 4) + ":" + t.substring(4, 6);
        return stime;
    }

    public static String getSQLiteTimestamp() {
        String d = getYYYYMMDD();
        String t = getStringTime();
        String stime = d + " " + t.substring(0, 2) + ":" + t.substring(2, 4) + ":" + t.substring(4, 6);
        return stime;
    }

    //--His
    public static String getStringTime() {
        Calendar c = Calendar.getInstance();
        String retval = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));

        return retval;
    }

    public static String Int2Hex(String intval) {
        intval = intval.replace(" ", "");
        int iBalance = Integer.parseInt(intval); // 1
        String Hbal = "000000" + Integer.toString(iBalance, 16); // H 1
        Hbal = Hbal.substring(Hbal.length() - 6).toUpperCase();
        return Hbal;
    }

    public static String ItoH(String intval) {
        intval = intval.replace(" ", "");
        int iBalance = Integer.parseInt(intval); // 1
        String Hbal = "000000" + Integer.toString(iBalance, 16); // H 1
        Hbal = Hbal.substring(Hbal.length() - 6).toUpperCase();
        String Hbal1 = Hbal.substring(0, 2);
        String Hbal2 = Hbal.substring(2, 6);
        Hbal2 = Hbal2.substring(2, 4) + Hbal2.substring(0, 2);
        String result = Hbal2 + Hbal1;
        return result;
    }

    //-- Reverse amount (4B) to Integer , Misal 27 10 00 00 menjadi 10000
    public static String HtoI(String hVal) {
        if (hVal.length() == 6)
            hVal = hVal + "00";
        //hVal1 = hVal.substring(4,8)+hVal.substring(0,4);
        String hVal1 = hVal.substring(4, 8);
        hVal1 = hVal1.substring(2, 4) + hVal1.substring(0, 2);
        String hVal2 = hVal.substring(0, 4);
        hVal2 = hVal2.substring(2, 4) + hVal2.substring(0, 2);
        String hv = hVal1 + hVal2;
        int ival = Integer.parseInt(hv, 16);
        return "" + ival;
    }

    public static String Hex3(String data) {
        String newData = "";
        for (int i = 0; i < data.length(); i++) {
            newData += "3" + data.charAt(i);
        }
        return newData;
    }

    public static String valToHexString(String val) {
        return bytesToHex(val.getBytes());
    }

    public static String nominalTransaksi(String data) {
        String dt = "0000000000" + data;
        return dt.substring(dt.length() - 10);
    }

    public static String nominalUntukLog(String data) {
        String dt = "0000000000" + data;
        return dt.substring(dt.length() - 8);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hexStringToAscii(String hs) {
        if (hs.length() % 2 > 0) {
            hs = fillZero(hs, hs.length() + 1);
        }
        StringBuilder sb = new StringBuilder();
        while (hs.length() > 1) {
            int i = Integer.valueOf(hs.substring(0, 2), 16);
            sb.append(Character.toChars(i));
            hs = hs.substring(2);
        }
        return sb.toString();
    }

    public static String strToCurr(String amt, String csym, Boolean hasCent) {
        if (amt.matches("-?\\d+(\\.\\d+)?")) {
            double d = Double.parseDouble(amt);
            DecimalFormatSymbols idrFormat = new DecimalFormatSymbols(Locale.getDefault());
            idrFormat.setDecimalSeparator(',');
            idrFormat.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("###,###,##0", idrFormat);
            if (hasCent) {
                d = d / 100;
            }
            if (!csym.equals("")) {
                csym += " ";
            }
            amt = csym + formatter.format(d);
        }
        return amt;
    }

    public static String strToCurr(String amt, String csym) {
        return strToCurr(amt, csym, false);
    }

    public static String strToCurr(String amt) {
        return strToCurr(amt, "", false);
    }

    public static String strToCurr(String amt, Boolean hasCent) {
        return strToCurr(amt, "", hasCent);
    }

    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    public String getString() {
        return value;
    }

    public void setString(String value) {
        this.value = value;
    }

    public Integer getInteger() {
        return Integer.valueOf(value);
    }

    public int getInt() {
        return getInteger().intValue();
    }
}