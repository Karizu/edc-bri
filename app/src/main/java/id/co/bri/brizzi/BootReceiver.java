package id.co.bri.brizzi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import id.co.bri.brizzi.handler.JsonCompHandler;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent i) {
        if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            context.startService(new Intent(context,SocketService.class));
        }
    }
}
