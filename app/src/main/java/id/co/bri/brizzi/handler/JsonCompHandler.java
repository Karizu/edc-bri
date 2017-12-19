/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.handler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import id.co.bri.brizzi.R;
import id.co.bri.brizzi.UpdateAppActivity;
import id.co.bri.brizzi.common.CommonConfig;

/**
 * @author indra
 */
public class JsonCompHandler {

    private static MenuListResolver mlr = new MenuListResolver();
    private static NotificationManager mNotificationManager;

    public JsonCompHandler() {

    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject checkUpdate(Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
//        Log.d("SN_DEVICE",serialNum);
        URL url = new URL("http://" + hostname + "/device/" + serialNum + "/checkVer");
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static void settingSuccess(Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
//        Log.d("SN_DEVICE",serialNum);
        URL url = new URL("http://" + hostname + "/device/" + serialNum + "/syncConfSuccess");
        url.openStream();
    }

    public static void fiturSuccess(Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
//        Log.d("SN_DEVICE",serialNum);
        URL url = new URL("http://" + hostname + "/device/" + serialNum + "/syncMenuSuccess");
        url.openStream();
    }

    public static void loadConf(Context ctx) throws IOException {
        SharedPreferences preferencesConfig = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = preferencesConfig.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
        URL url = new URL("http://" + hostname + "/device/" + serialNum + "/loadConf");
        InputStream is = url.openStream();
        JSONObject json = new JSONObject();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            json = new JSONObject(jsonText);
            Log.i("conf",json.toString(2));
            //checkIsNeedUpdate
            SharedPreferences preferencesSetting = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
            SharedPreferences preferences = preferencesSetting;
            String ip = preferences.getString("ip",CommonConfig.DEV_SOCKET_IP);
            String port = preferences.getString("port",CommonConfig.DEV_SOCKET_PORT);
            String initScreen = preferences.getString("init_screen",CommonConfig.INIT_REST_ACT);
            String diskonId = preferences.getString("diskon_id",CommonConfig.DEFAULT_DISCOUNT_TYPE);
            String diskon = preferences.getString("diskon",CommonConfig.DEFAULT_DISCOUNT_RATE);
            String terminalId = preferences.getString("terminal_id",CommonConfig.DEV_TERMINAL_ID);
            String merchantId = preferences.getString("merchant_id",CommonConfig.DEV_MERCHANT_ID);
            String merchantName = preferences.getString("merchant_name",CommonConfig.INIT_MERCHANT_NAME);
            String merchantAddr1 = preferences.getString("merchant_address1",CommonConfig.INIT_MERCHANT_ADDRESS1);
            String merchantAddr2 = preferences.getString("merchant_address2",CommonConfig.INIT_MERCHANT_ADDRESS2);
            String passSettlement = preferences.getString("pass_settlement", CommonConfig.DEFAULT_SETTLEMENT_PASS);
            String minDeduct = preferences.getString("minimum_deduct", CommonConfig.DEFAULT_MIN_BALANCE_BRIZZI);
            String maxDeduct = preferences.getString("maximum_deduct", CommonConfig.DEFAULT_MAX_MONTHLY_DEDUCT);
            boolean isNeedUpdate = false;
            if (!ip.equals(json.getString("ip"))) {
                isNeedUpdate = true;
            }
            if (!port.equals(json.getString("port"))) {
                isNeedUpdate = true;
            }
            if (!hostname.equals(json.getString("hostname"))) {
                isNeedUpdate = true;
            }
            if (!diskonId.equals(json.getString("diskon_id"))) {
                isNeedUpdate = true;
            }
            if (!diskon.equals(json.getString("diskon"))) {
                isNeedUpdate = true;
            }
            if (!terminalId.equals(json.getString("terminal_id"))) {
                isNeedUpdate = true;
            }
            if (!merchantId.equals(json.getString("merchant_id"))) {
                isNeedUpdate = true;
            }
            if (!merchantName.equals(json.getString("merchant_name"))) {
                isNeedUpdate = true;
            }
            if (!merchantAddr1.equals(json.getString("merchant_address1"))) {
                isNeedUpdate = true;
            }
            if (!merchantAddr2.equals(json.getString("merchant_address2"))) {
                isNeedUpdate = true;
            }
            if (!passSettlement.equals(json.getString("pass_settlement"))) {
                isNeedUpdate = true;
            }
            if (!minDeduct.equals(json.getString("minimum_deduct"))) {
                isNeedUpdate = true;
            }
            if (!maxDeduct.equals(json.getString("maximum_deduct"))) {
                isNeedUpdate = true;
            }
            if (isNeedUpdate) {
                if (mlr.hasUnsettledData(ctx)) {
                    if (mNotificationManager == null) {
                        mNotificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
                    }
                    Intent intent = new Intent(ctx, UpdateAppActivity.class);
                    intent.putExtra("method", 1181);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ctx,
                            (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(ctx)
                            .setContentTitle("Informasi")
                            .setContentIntent(pendingIntent)
                            .setContentText("Update setting EDC telah tersedia, silahkan lakukan settlement sebelum memasangkan update")
                            .setSmallIcon(R.drawable.logo_bri_002)
                            .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.if_email));
                    Notification n;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        n = builder.build();
                    } else {
                        n = builder.getNotification();
                    }
                    n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(1181, n);
                } else {
                    preferencesSetting.edit().putString("merchant_name", json.getString("merchantname")).apply();
                    preferencesSetting.edit().putString("merchant_address1", json.getString("alamat")).apply();
                    preferencesSetting.edit().putString("merchant_address2", json.getString("kanwil")).apply();
                    preferencesSetting.edit().putString("terminal_id", json.getString("terminalid")).apply();
                    preferencesSetting.edit().putString("merchant_id", json.getString("merchantid")).apply();
                    preferencesSetting.edit().putString("init_phone", json.getString("phoneno")).apply();
                    preferencesSetting.edit().putString("primary_phone", json.getString("primaryphone")).apply();
                    preferencesSetting.edit().putString("secondary_phone", json.getString("secondaryphone")).apply();
                    preferencesSetting.edit().putString("master", json.getString("master")).apply();
                    preferencesSetting.edit().putString("ip", json.getString("ip")).apply();
                    preferencesSetting.edit().putString("diskon_id", json.getString("diskon_id")).apply();
                    preferencesSetting.edit().putString("diskon", json.getString("diskon")).apply();
                    preferencesSetting.edit().putString("pass_settlement", json.getString("pass_settlement")).apply();
                    preferencesSetting.edit().putString("minimum_deduct", json.getString("minimum_deduct")).apply();
                    preferencesSetting.edit().putString("maximum_deduct", json.getString("maximum_deduct")).apply();
                    preferencesSetting.edit().putString("port", json.getString("port")).apply();
                    settingSuccess(ctx);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        url = new URL("http://" + hostname + "/device/" + serialNum + "/loadMenuSuccess");
        url.openStream();
    }

    public static JSONObject readJsonFromUrl(String id, Context ctx) throws IOException, JSONException {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String hostname = "http://" + preferences.getString("hostname", CommonConfig.HTTP_REST_URL);
        String serialNum = Build.SERIAL;
        // Create an unbound socket
        if(id.contains("Rp")){
            return new JSONObject();
        }
        Log.d("LOAD URL",hostname + "/device/" + serialNum + "/loadMenu/" + id);
        URL url = new URL(hostname + "/device/" + serialNum + "/loadMenu/" + id);
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return (JSONObject) json.get("screen");
        } finally {
            is.close();
        }
    }

    public static boolean saveJson(Context context, String id) throws IOException, Exception {
        try {
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(readJsonFromUrl(id, context).toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveJsonNoTms(Context context, String id) throws IOException, Exception {
        try {
            boolean isdel = context.deleteFile(id + ".json");
        } catch (Exception e) {
            e.printStackTrace();
            //pass
        }
        try {
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(mlr.loadMenu(context, id).get("screen").toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveJson(Context context, JSONObject obj) throws IOException {
        try {
            String id = obj.getString("id");
            FileOutputStream fos = context.openFileOutput(id + ".json", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(obj.toString());
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject readJson(Context context, String id) throws IOException, JSONException {
//        BufferedReader rd = new BufferedReader(new InputStreamReader(context.openFileInput(id + ".json")));
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream("/data/data/"+context.getPackageName()+"/files/" + id + ".json")));
        return new JSONObject(readAll(rd));
    }

    public static void checkVer(Context ctx,
                                String id, String[] arrays1,
                                String[] arrays2) throws IOException, JSONException, Exception {
        JSONObject json = JsonCompHandler.readJsonFromUrl(id, ctx);
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            checkVer(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {

                        }
                    }
                }
            }
        }
        if (json.get("ver") != null) {
            String ver = json.getString("ver");
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
//            Log.d("VER", "New Version");
//            Log.d("VER", "ID :" + id);
//            Log.d("VER", "VER :" + ver);
            preferences.edit().putString(id, ver).apply();
            saveJson(ctx, id);
        }
    }

    public static void checkVerNoTms(Context ctx,
                                String id, String[] arrays1,
                                String[] arrays2) throws IOException, JSONException, Exception {
        JSONObject json=null;
        try {
            json = mlr.loadMenu(ctx, id).getJSONObject("screen");
        } catch (Exception e) {
            Log.e("M2J", "Error cannot load menu : " + e.getMessage());
//            e.printStackTrace();
        }
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            checkVerNoTms(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {
//                            Log.e("JPR", "Cannot parse JSON : " + ex.getMessage());
                        }
                    }
                }
            }
        }
        if (json.get("ver") != null) {
            String ver = json.getString("ver");
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
            if(preferences.getString(id,"0").equals("0")){
//                Log.d("VER","Create new menu from Rest");
//                Log.d("VER","ID :"+id);
//                Log.d("VER","VER :"+ver);
                preferences.edit().putString(id,ver).apply();
                saveJsonNoTms(ctx,id);
            }else if(!preferences.getString(id,"0").equals(ver)){
//                Log.d("VER","New Version");
//                Log.d("VER","ID :"+id);
//                Log.d("VER","VER :"+ver);
                preferences.edit().putString(id,ver).apply();
                saveJsonNoTms(ctx,id);
            }
        }
    }

    public static void jsonRebuild(Context ctx,
                                     String id, String[] arrays1,
                                     String[] arrays2) throws IOException, JSONException, Exception {
        JSONObject json=null;
        try {
            json = mlr.loadMenu(ctx, id).getJSONObject("screen");
        } catch (Exception e) {
            Log.e("M2J", "Error cannot load menu : " + e.getMessage());
//            e.printStackTrace();
        }
        if (json.get("comps") != null) {
            JSONObject obj = (JSONObject) json.get("comps");
            JSONArray ar = obj.getJSONArray("comp");
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj2 = ar.getJSONObject(i);
                for (String str : arrays2) {
                    if (str.equals("comp_act")) {
                        try {
                            jsonRebuild(ctx, obj2.get(str).toString(), CommonConfig.FORM_MENU_KEY, CommonConfig.FORM_MENU_COMP_KEY);
                        } catch (JSONException ex) {
//                            Log.e("JPR", "Cannot parse JSON : " + ex.getMessage());
                        }
                    }
                }
            }
        }
        if (json.get("ver") != null) {
            String ver = json.getString("ver");
            SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.VER_FILE, Context.MODE_PRIVATE);
            preferences.edit().putString(id,ver).apply();
            saveJsonNoTms(ctx,id);
        }
    }

    public static void checkVer(Context ctx) throws IOException, JSONException, Exception {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        checkVer(ctx, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), CommonConfig.LIST_MENU_KEY, CommonConfig.LIST_MENU_COMP_KEY);
//        loadConf(ctx);

    }

    public static void checkVerNoTms(Context ctx) throws IOException, JSONException, Exception {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        checkVerNoTms(ctx, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), CommonConfig.LIST_MENU_KEY, CommonConfig.LIST_MENU_COMP_KEY);
    }

    public static void jsonRebuild(Context ctx) throws IOException, JSONException, Exception {
        SharedPreferences preferences = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        jsonRebuild(ctx, preferences.getString("init_screen", CommonConfig.INIT_REST_ACT), CommonConfig.LIST_MENU_KEY, CommonConfig.LIST_MENU_COMP_KEY);
    }

    public static JSONObject postData(String data) throws IOException, JSONException {
//        Log.d("JSON_REQUEST", data);
        StringEntity input = new StringEntity(data);
        HttpClient httpClient = new DefaultHttpClient();
//        input.setContentType("application/json");preferences
        HttpPost postRequest = new HttpPost(
                CommonConfig.HTTP_POST);
        postRequest.setEntity(input);
        HttpResponse response = httpClient.execute(postRequest);
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String resp = readAll(br).trim();
//        Log.d("JSON_RESPONSE", resp);
        return new JSONObject(resp);
    }

    public static void main(String[] args) throws IOException, JSONException, Exception {
    }
}
