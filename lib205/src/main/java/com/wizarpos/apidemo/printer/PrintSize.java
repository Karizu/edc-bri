package com.wizarpos.apidemo.printer;

/**
 * Created by indra on 06/01/16.
 */
public class PrintSize {
    private FontSize fontSize;
    private String message;

    public PrintSize(){

    }
    public PrintSize(FontSize fontSize, String message){
        this.fontSize = fontSize;
        this.message = message;
    }

    public FontSize getFontSize() {
        return fontSize;
    }

    public String getMessage() {
        return message;
    }
}
