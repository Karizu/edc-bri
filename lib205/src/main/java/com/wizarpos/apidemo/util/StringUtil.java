package com.wizarpos.apidemo.util;


public class StringUtil {
    private static final String HexChars = "1234567890abcdefABCDEF";
    public static final int LCD_WIDTH = 16;

//	public static String getFormatString(byte [] bytes){
//		String value = "";
//		for(byte b : bytes){
//			value += String.format("%02X", b);
//		}
//		return value;
//	}

    /**
     * A table of hex digits
     */
    public static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    public static char toHexChar(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    public static int getSaveConvertLength(String theString) {
        return saveConvert(theString, null, 0, true, true, true);
    }

    public static int saveConvert(String theString, byte[] dst, int offset) {
        return saveConvert(theString, dst, offset, true, true, false);
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and writes out any of the
     * characters in specialSaveChars with a preceding slash
     *
     * @param theString
     *            the String needing convert.
     * @param dst
     *            Save of the result
     * @param offset
     *            the offset of result
     * @param escapeSpace
     *            if <code>true</code>, escape Space
     * @param lengthFlag
     *            Whether add one byte of length in the result.
     *            <code>true</code> add one byte of length in the result
     * @param getLengthFlag
     *            Calculate the length of result, if <code>true</code>, theString length that return.
     * @return  if getLengthFlag = false, return offset of the result.
     *          if getLengthFlag = true, the length of the sequence of characters represented by this
     *          object.
     */
    public static int saveConvert(String theString, byte[] dst, int offset, boolean escapeSpace, boolean lengthFlag, boolean getLengthFlag) {
        if (false == getLengthFlag
                && (null == dst
                || dst.length < (offset + (lengthFlag ? 1 : 0))
                || dst.length < 1
                || offset < 0
        )
                ) {
            return -1;
        }
        if (null == theString) {
            theString = "";
        }
        int length;
        byte[] outBuffer = null;
        if (getLengthFlag) {
            length = UTF8Util.char2ByteUTF8(theString, 0, theString.length(), null, 0, 0, true);
        } else {
            outBuffer = UTF8Util.string2BytesUTF8(theString);
            length = outBuffer.length;
        }

        if (length > 255) {
            length = 255;
        }
        if (getLengthFlag) {
            if (lengthFlag) {
                length = length + 1;
            }
        } else {
            if (dst.length >= offset + length + (lengthFlag ? 1 : 0)) {
                if (lengthFlag) {
                    dst[offset] = (byte) (length & 0xFF);
                    offset++;
                }
                System.arraycopy(outBuffer, 0, dst, offset, length);
                offset += length;

                length = offset;
            } else {
                length = -1;
            }
        }

        outBuffer = null;

        return length;
    }


    /*
     * Converts encoded &#92;uxxxx to unicode chars and changes special saved
     * chars to their original forms
     *
     * @param s
     *            the  byte arrary needing convert.
     * @param offset
     *            the offset of byte arrary
     * @param lengthFlag
     *            Whether add one byte of length in the result.
     *            <code>true</code> add one byte of length in the result
     * @return  the convert result of the byte arrary.
     */
    public static String loadConvert(byte[] s, int offset, boolean lengthFlag)
            throws IllegalArgumentException {
        if (null == s || (offset + (lengthFlag ? 1 : 0)) > s.length)
            throw new IllegalArgumentException("invalid byte arrary");

        int len = (s.length - offset);

        if (lengthFlag) {
            len = s[offset] & 0xFF;
            offset++;
        }

        return UTF8Util.bytes2StringUTF8(s, offset, len, true);
    }

    public static String returnString(String str) {
        if (null == str)
            return "";
        else
            return str;
    }

    public static String returnString(int intValue) {
        if (intValue < 0)
            return "";
        else
            return "" + intValue;
    }

    public static String returnString(short shortValue) {
        return returnString((int) shortValue);
    }

    public static String returnString(byte byteValue) {
        return returnString((int) byteValue);
    }

    /**
     * Method trim space
     *
     * @param The string to be format.
     */
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

    /**
     * Method trim space
     *
     * @param oldString The string to be format.
     * @param trimFlag  The trim flag,
     *                  =0:trim both sides,
     *                  >0:trim right sides,
     *                  <0:trim left sides,
     */
    public static String trimSpace(String oldString, int trimFlag) {
        if (null == oldString)
            return null;
        if (0 == oldString.length())
            return "";

        int length = oldString.length();
        int j = 0;
        for (j = 0; j < length && (oldString.charAt(j) == ' ' || oldString.charAt(j) == '\0'); j++)
            ;
        if (trimFlag < 0) // trim left sides
            return (j <= 0 ? oldString : oldString.substring(j));
        for (; j < length && (oldString.charAt(length - 1) == ' ' || oldString.charAt(length - 1) == '\0'); length--)
            ;
        if (trimFlag > 0) //trim right sides
            return (length >= oldString.length() ? oldString : oldString.substring(0, length));

        return (j <= 0 && length >= oldString.length() ? oldString : oldString.substring(j, length));
    }

    /**
     * Method convert byte[] to String
     *
     * @param The string to be format.
     */
    public static String toString(byte abyte0[]) {
        if (null == abyte0)
            return null;
        else
            return new String(abyte0);
    }

    public static String[] buffer2Message(String bufferString, int width, int height) {
        int buffLen;
        int i = 0;
        int h, w;
        if (null == bufferString)
            buffLen = 0;
        else
            buffLen = bufferString.length();

        if (height < 1 && width > 0) {
            if (0 == (buffLen % width))
                h = buffLen / width;
            else
                h = (buffLen / width) + 1;
            w = width;
        } else {
            if (height > 0 && width < 1) {
                if (0 == (buffLen % height))
                    w = buffLen / height;
                else
                    w = (buffLen / height) + 1;
                h = height;
            } else {
                if (height > 0 && width > 0) {
                    h = height;
                    w = width;
                } else {
                    return null;
                }
            }
        }

        String[] buff = new String[h];

        for (i = 0; i < h; i++) {
            if ((w * (i + 1)) < buffLen)
                buff[i] = bufferString.substring(w * i, w * (i + 1));
            else if ((w * (i + 1)) >= buffLen && (w * i) < buffLen)
                buff[i] = bufferString.substring(w * i, buffLen);
            else
                buff[i] = "";
        }

        return buff;
    }

    /**
     * Method Format string
     *
     * @param The string to be format.
     */
    public static String[] buffer2Message(String bufferString) {
        return buffer2Message(bufferString, LCD_WIDTH, 3);
    }

    /**
     * Method fill string
     *
     * @param The string to be format.
     */
    public static String fillString(String formatString, int length, char fillChar, boolean leftFillFlag) {
        if (null == formatString) {
            formatString = "";
        }
        int strLen = formatString.length();
        if (strLen >= length) {
            if (true == leftFillFlag)  // left fill
                return formatString.substring(strLen - length, strLen);
            else
                return formatString.substring(0, length);
        } else {
            StringBuffer sbuf = new StringBuffer();
            int fillLen = length - formatString.length();
            for (int i = 0; i < fillLen; i++) {
                sbuf.append(fillChar);
            }

            if (true == leftFillFlag)  // left fill
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

    /**
     * Method fill string
     *
     * @param The string to be format.
     */
    public static String fillSpace(String formatString, int length) {
        return fillString(formatString, length, ' ', false);
    }

    /**
     * Method Format string
     *
     * @param The string to be format.
     */
    public static String formatLine(String formatString, boolean leftFillFlag) {
        return fillString(formatString, LCD_WIDTH, ' ', leftFillFlag);
    }


    private static final char[] space8 = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

    /**
     * Method fill space , converted String lenth to LCD_WIDTH
     *
     * @param The string to be format.
     */
    public static String fillShowSpace(String formatString) {
        if (null == formatString)
            return "";

        if (formatString.length() <= LCD_WIDTH) {
            int len = 8 - (formatString.length() / 2);
            StringBuffer sbuf = new StringBuffer();
            sbuf.append(space8, 0, len);
            sbuf.append(formatString);
            sbuf.append(space8, 0, len);
            sbuf.setLength(LCD_WIDTH);

            String returnString = sbuf.toString();
            sbuf = null;
            return returnString;
        } else {
            return formatString.substring(0, LCD_WIDTH);
        }
    }

    /**
     * Method Format string
     *
     * @param The string to be format.
     */
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

    public static String toHexString(byte abyte0[], int beginIndex, int endIndex, boolean spaceFlag) {
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

    public static String toHexString(byte abyte0[], int beginIndex, int endIndex) {
        if (null == abyte0)
            return null;
        return toHexString(abyte0, beginIndex, endIndex, true);
    }

    public static String toHexString(byte abyte0[], boolean spaceFlag) {
        if (null == abyte0)
            return null;
        return toHexString(abyte0, 0, abyte0.length, spaceFlag);
    }

    /**
     * Method convert byte[] to HexString
     *
     * @param The string to be format.
     */

    public static String toHexString(byte abyte0[]) {
        if (null == abyte0)
            return null;
        return toHexString(abyte0, 0, abyte0.length, true);
    }

    public static String toHexString(byte abyte0) {
        StringBuffer sbuf = new StringBuffer();
        appendHex(sbuf, abyte0);

        String returnString = sbuf.toString();
        sbuf = null;
        return returnString;
    }

    /**
     * Method Method convert byte[] to HexString and String
     *
     * @param The string to be format.
     */
    public static String toFullString(byte abyte0[]) {
        return "(" + toHexString(abyte0) + ") " + toString(abyte0);
    }

    public static String getFormatString(byte[] bytes) {
        String value = "";
        for (byte b : bytes) {
            value += String.format("%02x ", b);
        }
        return value;
    }

    public static String getFormatString(byte[] bytes, int length) {
        String value = "";
        for (int i = 0; i < length; i++) {
            value += String.format("%02X ", bytes[i]);
        }
        return value;
    }

    /**
     * Method Format string
     *
     * @param The string to be format.
     */
    public static String toBestString(byte abyte0[]) {
        boolean flag = false;
        if (abyte0 != null) {
            int byteLen = abyte0.length;
            for (int i = 0; !flag && i < byteLen; i++)
                if (abyte0[i] != 32 && (abyte0[i] != 33) & (abyte0[i] != 63))
                    flag = abyte0[i] < 48;

        }
        if (flag)
            return toHexString(abyte0);
        else
            return toString(abyte0);
    }

    /**
     * Method Check String
     *
     * @param The string to be format.
     */
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

    /**
     * Return true if the string is alphanum.
     * <code>{letter digit }</code>
     **/
    public static boolean isLetterNumeric(String s) {
        int i = 0, len = s.length();
        while (i < len && (Character.isLowerCase(s.charAt(i)) ||
                Character.isUpperCase(s.charAt(i)) ||
                Character.isDigit(s.charAt(i)))) {
            i++;
        }
        return (i >= len);
    }

    /**
     * 获取字符串的长度，如果有中文，则每个中文字符计为2位
     *
     * @param value 指定的字符串
     * @return 字符串的长度
     */
    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
      /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
          /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
          /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
              /* 中文字符长度为2 */
                valueLength += 2;
            } else {
              /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
}