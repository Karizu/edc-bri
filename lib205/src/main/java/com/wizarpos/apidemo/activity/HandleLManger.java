package com.wizarpos.apidemo.activity;

import android.content.Context;

import com.wizarpos.apidemo.contactlesscard.ContactlessCardHandle2;
import com.wizarpos.apidemo.money.MoneyBoxHandle;
import com.wizarpos.apidemo.msr.MSRHandle;
import com.wizarpos.apidemo.pinpad.PinpadHandle;
import com.wizarpos.apidemo.printer.PrinterHandle;
import com.wizarpos.apidemo.serial.SerialTestHandle;
import com.wizarpos.apidemo.smartcard.SmartCardHandle;


public class HandleLManger {
	public static HandleL getSuitableHandle(Context context){
//		根据activity的状态来创建handle
		HandleL handle = null;
		if(SecondMainActivity.state == SecondMainActivity.StateType.contactless1){
			handle = new ContactlessCardHandle2(context);
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.msr){
			handle = new MSRHandle();
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.printer){
			handle = new PrinterHandle();
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.pinpad){
			handle = new PinpadHandle();
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.smartcard){
			handle = new SmartCardHandle();
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.serial){
			handle = new SerialTestHandle();
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.contactless2){
			handle = new ContactlessCardHandle2(context);
		}else if(SecondMainActivity.state == SecondMainActivity.StateType.moneybox){
			handle = new MoneyBoxHandle();
		}
		return handle;
	}
}
