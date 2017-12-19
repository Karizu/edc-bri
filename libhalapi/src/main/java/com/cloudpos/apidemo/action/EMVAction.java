package com.cloudpos.apidemo.action;

import java.util.Map;

import com.cloudpos.apidemo.function.ActionCallbackImpl;
import com.cloudpos.jniinterface.EMVKernelInterface;

public class EMVAction extends ConstantAction {
	
	public void open(Map<String, Object> param, ActionCallbackImpl callback) {
        EMVKernelInterface.loadEMVKernel();
    }
	
	public void close(Map<String, Object> param, ActionCallbackImpl callback) {
		EMVKernelInterface.exitEMVKernel();
	}

}
