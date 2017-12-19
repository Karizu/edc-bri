package com.wizarpos.apidemo.activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyAdapter extends ArrayAdapter<String>{

	private List<String> listTag ;
	public MyAdapter(Context context,  int textViewResourceId,
			List<String> objects) {
		super(context,  textViewResourceId, objects);
		this.listTag = objects;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false; 
		// 不是所有项都可选
	}

	@Override
	public boolean isEnabled(int position) {
		return !getItem(position).startsWith("-"); 
		// 如果-开头，则该项不可选
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view = convertView;
	    //根据标签类型加载不通的布局模板
	    if(listTag.contains(getItem(position))){
	        //如果是标签项
	        view = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item, null);
	    }else{              
	        //否则就是数据项了      
	        view = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item, null);
	    }
	    //显示名称
	    TextView textView = (TextView) view.findViewById(R.id.group_list_item_text);
	    textView.setText(getItem(position));
	    //返回重写的view
	    return view;
	}

}
