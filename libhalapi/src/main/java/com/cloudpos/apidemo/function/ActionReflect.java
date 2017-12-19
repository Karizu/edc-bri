package com.cloudpos.apidemo.function;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.cloudpos.apidemo.activity.R;
import com.cloudpos.apidemo.util.StringUtility;

public class ActionReflect {

	//Find attribute name and attribute value by reflection.
	/**
	 * 
	 * @param obj--the instance of the class to find，model-- the attribute name to find
	 * @return  attribute value   0--not find the corresponding attribute name of model
	 */
	public static int getFields(Object obj, String model) {

		int stringId = 0;
		Field[] fields = obj.getClass().getFields();
		for (Field field : fields) {
			if (model.equals(field.getName())) {
				try {
					stringId = field.getInt(obj);
					return stringId;
				} catch (IllegalAccessException e1) {
					// e1.printStackTrace();
				} catch (IllegalArgumentException e2) {
					// e2.printStackTrace();
				}
			}
		}
		return stringId;
	}
	
	public static List<String> getStringsXml(Context con, String name){
		R.string strs = new R.string();
		int stringId = getFields(strs, name); //  Find the position of 'R' file in device and get informations of directory.  
		List<String> array = new ArrayList<String>();
		try{
		String temp = con.getString(stringId);
		String[] strs1 = StringUtility.spiltStrings(temp, ",");
		for (String string : strs1) {
			array.add(string);
		}
		}catch(Exception e){
			array = null;
		}
		return array;
	}
	
	public static List<String> getArraysXml(Context con, String name){
		R.array strs = new R.array();
		int stringId = getFields(strs, name); // Find the position of 'R' file in device and get informations of directory.  
		List<String> array = new ArrayList<String>();
		try{
		String[] classItems = con.getResources().getStringArray(stringId);
		for(String classItem: classItems){
			array.add(classItem);
		}
		}catch(Exception e){
			array = null;
		}
		return array;
	}
	
}
