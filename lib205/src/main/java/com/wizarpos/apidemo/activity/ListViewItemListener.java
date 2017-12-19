package com.wizarpos.apidemo.activity;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class ListViewItemListener  implements OnItemClickListener{

	private boolean isConnection = true;
	private Context context;//为了方便实用dialog
	public ListViewItemListener(Context context){
		this.context = context;
	}
	
	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		if(isConnection){
			HandleL handle = HandleLManger.getSuitableHandle(context);
			handle.executeClickItemOperate(l.getItemAtPosition(position).toString(), context);
		}else{
			System.err.println("没有硬件连接,请检查");
		}
	}
}
