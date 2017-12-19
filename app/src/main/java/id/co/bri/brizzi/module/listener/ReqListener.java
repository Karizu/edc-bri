package id.co.bri.brizzi.module.listener;

/**
 * Created by indra on 13/01/16.
 */
public interface ReqListener {
    void onReqCompleted(String result);

    void onNoInternetConnection();
}
