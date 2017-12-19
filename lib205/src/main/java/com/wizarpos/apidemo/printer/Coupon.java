package com.wizarpos.apidemo.printer;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Coupon {
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getCash() {
		return cash;
	}

	public void setCash(double cash) {
		this.cash = cash;
	}

	public String getFoodDescription() {
		return foodDescription;
	}

	public void setFoodDescription(String foodDescription) {
		this.foodDescription = foodDescription;
	}

	public double getOriginCash() {
		return originCash;
	}

	public void setOriginCash(double originCash) {
		this.originCash = originCash;
	}

	int id = 0;
	String type = "TEST";
	double cash = 99;
	int num = 99;

	double originCash = 99;
	String foodDescription;

	static int autoIDCount = 0;
	static Random random = new Random();

	private Coupon() {
	}

	public static Coupon generateCoupon() {
		int num = random.nextInt(4);
		return new Coupon(num);
	}

	public String getQRString() throws UnsupportedEncodingException {
		// String qrString = String.format("id:%d\n", id) +
		// String.format("type:%s\n", type)
		// + String.format("cash:%.2f\n", cash)
		// + String.format("desc:%s", foodDescription);
		// String retString = new String(qrString.getBytes("UTF-8"));
		String retString = new String(
				"{\"ORCodeType\":\"download_coupons\",\"couponsInfo\":{\"couponsNo\":"
						+ Integer.toString(id) + "}}");

		return retString;
	}

	private static int getAutoID() {
		return autoIDCount++;
	}

	public String getBusinessUnit() {
		return num + 1 + "";
	}

	private Coupon(int num) {
		this.num = num;
		switch (num) {
		case 0:
			id = 101;
			type = "C101";
			cash = 12.5;
			originCash = 15.5;
			foodDescription = "黄金咖喱猪排饭";
			break;
		case 1:
			id = 102;
			type = "C102";
			cash = 15;
			originCash = 20;
			foodDescription = "狠大照烧鸡腿饭";
			break;
		case 2:
			id = 103;
			type = "C103";
			cash = 37;
			originCash = 45;
			foodDescription = "欧陆风情鲍菇培根披萨";
			break;
		case 3:
			id = 104;
			type = "C104";
			cash = 70;
			originCash = 100;
			foodDescription = "小肥羊全家乐享套餐";
			break;
		case 4:
			id = 205;
			type = "C205";
			cash = 48;
			originCash = 56;
			foodDescription = "新奥尔良风情烤肉披萨";
			break;
		case 5:
			id = 206;
			type = "C206";
			cash = 4.5;
			originCash = 6;
			foodDescription = "椰汁黑米露";
			break;
		// case 7:
		// id = getAutoID();
		// type = "C10";
		// cash = 6.5;
		// originCash = 8;
		// foodDescription = "香醇奶茶（热）";
		// case 8:
		// id = getAutoID();
		// type = "C11";
		// cash = 5;
		// originCash = 8;
		// foodDescription = "黄金海皇星（2个）";
		// case 9:
		// id = getAutoID();
		// type = "C17";
		// cash = 6.5;
		// originCash = 9;
		// foodDescription = "香辣鸡翅（2块）";
		// case 10:
		// id = getAutoID();
		// type = "C18";
		// cash = 7.5;
		// originCash = 10;
		// foodDescription = "新奥尔良烤翅（2块）";
		// break;
		}
	}
}
