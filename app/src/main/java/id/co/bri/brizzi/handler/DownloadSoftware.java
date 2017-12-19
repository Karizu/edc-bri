package id.co.bri.brizzi.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import id.co.bri.brizzi.common.CommonConfig;

/**
 * Created by indra on 5/20/2016.
 */
public class DownloadSoftware extends AsyncTask<Void, Integer, String> {
    public static final String FOLDER_LOCATION = "/sdcard/android/data/id.co.bri.brizzi/update";
    public static final String FILE_NAME = FOLDER_LOCATION + "/latest.apk";
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private Handler handler;

    public DownloadSoftware(Context context, ProgressDialog mProgressDialog, Handler handler) {
        this.context = context;
        this.mProgressDialog = mProgressDialog;
        this.handler = handler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        SharedPreferences preferences = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            String root = Environment.getExternalStorageState().toString();
            URL url = new URL("http://"+preferences.getString("hostname",  CommonConfig.HTTP_REST_URL) + "/device/" + Build.SERIAL + "/file/latest.apk");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            File outputDir = new File(FOLDER_LOCATION);
            if (outputDir.mkdirs()) {

            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(FILE_NAME);

            byte data[] = new byte[8192];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) {
                    int persen = (int) (total * 100 / fileLength);
                    publishProgress(persen);
                }

                output.write(data, 0, count);
            }
            return "SUKSES";
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mProgressDialog.dismiss();
        Message m = new Message();
        Bundle b = new Bundle();
        b.putString("result", result);
        m.setData(b);
        handler.sendMessage(m);
    }
}
