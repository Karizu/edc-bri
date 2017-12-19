package id.co.bri.brizzi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by indra on 5/24/2016.
 */
public class OnUpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String msg = "intent:" + intent + " action:" + intent.getAction();
//        Log.d("DEBUG", msg);
//        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
    }
}

