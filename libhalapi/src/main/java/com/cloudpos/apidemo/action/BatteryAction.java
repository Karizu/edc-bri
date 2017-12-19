package com.cloudpos.apidemo.action;

import java.util.Map;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.jniinterface.BatteryInterface;

public class BatteryAction extends ConstantAction {
    
    private void setParams(Map<String, Object> param, ActionCallbackImpl callback) {
        this.mCallback = callback;
    }

    public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        if (isOpened) {
            callback.sendFailedMsg(mContext.getResources().getString(R.string.device_opened));
        } else {
            try {
                int result = BatteryInterface.open();
                if (result < 0) {
                    callback.sendFailedMsg(mContext.getResources().getString(
                            R.string.operation_with_error) + result);
                } else {
                    isOpened = true;
                    callback.sendSuccessMsg(mContext.getResources().getString(
                            R.string.operation_successful));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                callback.sendFailedMsg(mContext.getResources().getString(R.string.operation_failed));
            }
        }
    }

    public void close(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                isOpened = false;
                int result = BatteryInterface.close();
                return result;
            }
        });
    }
    
    public void queryInfo(Map<String, Object> param, ActionCallbackImpl callback) {
        setParams(param, callback);
        final int[] capacity = new int[1];
        final int[] voltage = new int[1];
        int result = checkOpenedAndGetData(new DataAction() {

            @Override
            public int getResult() {
                int result = BatteryInterface.queryInfo(capacity, voltage);
                return result;
            }
        });
        if(result >= 0){
            mCallback.sendSuccessMsg("pCapacity = " + capacity[0] + ", Battery Voltage : "
                    + voltage[0]);
        }
    }

}
