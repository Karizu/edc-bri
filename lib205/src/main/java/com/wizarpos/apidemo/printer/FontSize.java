package com.wizarpos.apidemo.printer;

/**
 * Created by indra on 06/01/16.
 */
public enum FontSize {

    EMPTY((byte) 0x1B, (byte) 0x21, (byte) 0x00),

    NORMAL((byte) 0x1B, (byte) 0x21, (byte) 0x01),

    NORMAL_2((byte) 0x1B, (byte) 0x21, (byte) 0x11),

    NORMAL_3((byte) 0x1B, (byte) 0x21, (byte) 0x21),

    BOLD((byte) 0x1B, (byte) 0x45, (byte) 0x00),

    BOLD_2((byte) 0x1B, (byte) 0x45, (byte) 0x11),

    BOLD_3((byte) 0x1B, (byte) 0x45, (byte) 0x21),

    TITLE((byte) 0x1B, (byte) 0x45, (byte) 0x31);

    private FontSize(byte... bytes) {
        this.bytes = bytes;
    }

    public byte[] getByte() {
        return bytes;
    }


    private byte[] bytes;
}
