
package com.cloudpos.apidemo.activity;

import java.util.HashMap;

import com.android.common.utils.PackageUtils;
import com.cloudpos.apidemo.action.IDCardAction;
import com.cloudpos.apidemo.common.Enums.StateLog;
import com.cloudpos.apidemo.common.OnClicker;
import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.apidemo.function.ActionReflect;
import com.cloudpos.apidemo.function.ListViewAdapter;
import com.cloudpos.apidemo.manager.AppManager;
import com.cloudpos.apidemo.util.LogHelper;
import com.cloudpos.mvc.base.ActionManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends ConstantActivity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();

        OnClicker.setOnClickListenerByIds(host, btnWidget, new OnClickListener() {

            @Override
            public void onClick(View v) {
                keyPressed(v.getId());
            }
        });

        setData(lvwMain);
        lvwMain.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String command = view.findViewById(R.id.lvw_button).getTag().toString();
                itemPressed(command, position);
            }
        });

        lvwMain.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    // position records the position of the visible top line
                    if (isMain) {
                        position = lvwMain.getFirstVisiblePosition();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isMain) {
                System.exit(0);
            } else {
                keyBack();
            }
        } else {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    private void keyBack() {
        btnExit.setText(getResources().getString(R.string.uninstall));
        // handheld device don't have this layout
        if (layoutJianjie != null) {
            layoutJianjie.setVisibility(View.GONE);
        }

        refreshListView(AppManager.model);
        lvwMain.setSelection(position);

        isMain = true;
        closeAllDriver();
        writeLog(getResources().getString(R.string.testEnd));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isHand) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void initParams() {
        handler = new Handler(handlerCallback);
        callback = new ActionCallbackImpl(handler);

        arryTag = ActionReflect.getArraysXml(this, AppManager.model);
        arryText = ActionReflect.getStringsXml(this, AppManager.model);

        txtVersion.setText(getResources().getString(R.string.version));

        host = (Activity) this;
        context = this;
        btnWidget = new int[] {
                R.id.btn_clean, R.id.btn_exit
        };

        param = new HashMap<String, Object>();

        // AppManager.model = "WIZARPOS";
    }

    private void setData(ListView listView) {
        adapter = new ListViewAdapter(arryText, arryTag, host);
        lvwMain.setAdapter(adapter);
    }

    private void keyPressed(int id) {
        switch (id) {
//            case R.id.btn_clean:
//                txtResult.setText("");
//                break;
//            case R.id.btn_exit:
//                if (isMain) {
//                    PackageUtils.uninstallNormal(this, this.getPackageName());
//                } else {
//                    keyBack();
//                }
//                break;
            default:
                break;
        }
    }

    private void itemPressed(String command, int position) {
        Log.e(TAG, command);
        if (isMain) {
            String commandText = arryText.get(position);
            writeLog(getResources().getString(R.string.welcome) + "\t" + commandText);

            if (command.equals(getResources().getString(R.string.IDCardAction))) {
                // IDCard option don't have sub-menu
                startActivityForResult((new Intent(MainActivity.this, IDCardAction.class)), 0);
            } else {
                // main-menu interface
                isMain = false;
                btnExit.setText(getResources().getString(R.string.back));
                // The selected action you pressed
                mainMenu = command;
                if (layoutJianjie != null) {
                    layoutJianjie.setVisibility(View.VISIBLE);
                    txtJianjie.setText(getResources().getString(R.string.jianjie) + "\n"
                            + commandText);
                }
                refreshListView(command);
            }
        } else {
            // sub-menu interface
            param.clear();
            param.put("host", host);
            ActionManager.doSubmit(mainMenu + "/" + command, context, param, callback);
        }
    }

    private void closeAllDriver() {
        // boolean isOpened = Common.getActionField(context, mainMenu,
        // "isOpened");
        // if (isOpened) {
        // param.clear();
        // param.put("host", host);
        // ActionManager.doSubmit(mainMenu + "/" + "close", context, param,
        // callback);
        // }
        // Log.e(TAG, "" + isOpened);
    }

    private void refreshListView(String command) {
        arryTag.clear();
        arryText.clear();
        // firstly judge whether main-menu option + model (such as
        // PrinterActionWIZARPOS_1) exists,
        // if exists, obtain informations inner.
        String priorCommand = command + AppManager.model;
        if (ActionReflect.getStringsXml(host, priorCommand) != null) {
            arryText = ActionReflect.getStringsXml(host, priorCommand);
            arryTag = ActionReflect.getArraysXml(host, priorCommand);
        } else {
            arryText = ActionReflect.getStringsXml(host, command);
            arryTag = ActionReflect.getArraysXml(host, command);
        }
        adapter.refreshData(arryText, arryTag);
        lvwMain.setAdapter(adapter);
    }

    private Handler.Callback handlerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case StateLog.LOG:
                    LogHelper.infoAppendMsg((String) msg.obj, txtResult);
                    break;
                case StateLog.LOG_SUCCESS:
                    LogHelper.infoAppendMsgForSuccess((String) msg.obj, txtResult);
                    break;
                case StateLog.LOG_FAILED:
                    LogHelper.infoAppendMsgForFailed((String) msg.obj, txtResult);
                    break;
                case StateLog.IMG_VISIBLE:
                    Log.e(TAG, "IMG_VISIBLE");
                    txtResult.setVisibility(View.GONE);
                    imgShow.setVisibility(View.VISIBLE);
                    break;
                case StateLog.IMG_INVISIBLE:
                    Log.e(TAG, "IMG_INVISIBLE");
                    txtResult.setVisibility(View.VISIBLE);
                    imgShow.setVisibility(View.GONE);
                    break;
                case StateLog.IMG_SHOW:
                    Log.e(TAG, "IMG_SHOW");
                    imgShow.setImageBitmap((Bitmap)msg.obj);
                    break;
                default:
                    LogHelper.infoAppendMsg((String) msg.obj, txtResult);
                    break;
            }
            return true;
        }
    };
}
