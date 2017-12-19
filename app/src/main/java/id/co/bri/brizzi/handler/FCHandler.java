package id.co.bri.brizzi.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by nubono on 10/19/2017.
 */

public class FCHandler implements java.lang.Thread.UncaughtExceptionHandler {

    private Activity ctx;

    public FCHandler(Activity ctx) {
        this.ctx = ctx;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
//        AlertDialog.Builder errDialog = new AlertDialog.Builder(this.ctx);
//        errDialog.setMessage("Ieu error");
//        errDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(10);
//            }
//        });
        Log.d("FCH", throwable.getMessage());
        throwable.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
