package id.co.bri.brizzi.handler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import id.co.bri.brizzi.module.listener.ReqListener;

public class ConAsync extends AsyncTask<String, Integer, String> {
    private ReqListener listener;
    private String request_method;
    private String json_string;
    private boolean isReversal = false;
    private final static int timeOut = 70100;
    private final static int SocketTimeOut = 70100;
    private Context act;
    private  ProgressDialog dialog;

    public ConAsync(Object listener) {
        this.listener = (ReqListener) listener;
        this.act = (Context) listener;
        this.request_method = "GET";
    }

    public ConAsync(ReqListener reqListener,Context context) {
        this.listener = reqListener;
        this.act = context;
        this.request_method = "GET";
    }

    private String getMethod(String... uri) {
        HttpParams httpParameters = new BasicHttpParams();
        //timeout untuk melakukan koneksi
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeOut);
        //timeout saat menunggu data
        HttpConnectionParams.setSoTimeout(httpParameters, SocketTimeOut);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse response;
        String responseString = null;

        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (UnsupportedEncodingException e) {
            return "Request Failed";
        } catch (ConnectTimeoutException e) {
            return "Connection Timeout, Please check your connection";
        } catch (ClientProtocolException e) {
            return "Request Failed";
        } catch (IOException e) {
            return "ERROR" + e.toString();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return responseString;
    }

    private String postMethod(String... uri) {
        HttpParams httpParameters = new BasicHttpParams();
        //timeout untuk melakukan koneksi
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeOut);
        //timeout saat menunggu data
        HttpConnectionParams.setSoTimeout(httpParameters, SocketTimeOut);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse response;
        String responseString = null;

        try {
            HttpPost hp = new HttpPost(uri[0]);
            hp.setEntity(new StringEntity(json_string, "UTF8"));
            hp.setHeader("Content-type", "text/plain");

            response = httpclient.execute(hp);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Request Failed";
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "Connection Timeout, Please check your connection";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return "Request Failed";
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR" + e.toString();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return responseString;
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onPreExecute() {
       dialog = ProgressDialog.show(act, "Silahkan Tunggu....", "Data sedang dikirim", true);
        Log.i("INFO_FORM", "Silahkan Tunggu....");
    }

    @Override
    protected String doInBackground(String... uri) {

        if (!URLUtil.isValidUrl(uri[0])) {
            return "ERRORInvalid URL";
        }
        if (!checkConnection()) {
            return "NoConnection";
        }
        String retval = null;
//        if (request_method.equals("POST")) return postMethod(uri);//jika method post stop disini
//
//        retval = getMethod(uri);

        txHandler txh = txHandler.getInstance();
        try {
            Log.i("KIRIM", json_string);
            //return JsonCompHandler.postData(params[0].toString()).getJSONObject("screen");
            txh.setContext(act);
            if (isReversal) {
                return txh.reverseLastTransaction(act);
            }
            return txh.processTransaction(act, json_string).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if (result.equals("NoConnection")) {
            listener.onNoInternetConnection();
        } else {
            listener.onReqCompleted(result);
        }
    }

    public void setRequestMethod(String request_method, String data) {
        this.request_method = request_method;
        this.json_string = data;
    }

    public String getRequestMethod() {
        return request_method;
    }

    public String getJsonString() {
        return json_string;
    }

    public void setActivity(Activity act) {
        this.act = act;
    }

    public void setAsReversal() {
        this.isReversal = true;
    }

    public void setAsTransaction() {
        this.isReversal = false;
    }
}