package com.wizarpos.apidemo.printer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wizarpos.apidemo.activity.DriverHandle;
import com.wizarpos.apidemo.activity.ResourceManager;
import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.printer.entity.PurchaseBill;
import com.wizarpos.apidemo.util.LogHelper;

public class PrinterHandle extends DriverHandle{
	
    boolean isPrintPurchaseBillFinished = true;
	private static final int PRINT_DATA = 3;
	@Override
	public void executeClickItemOperate(String command, final Context context) {
		textView = ResourceManager.getTextViewFromSecondMainActivity((Activity)context);//这个取值需要做些修改。
		if(command.equals("PrintData")){
			printDateItem(context);
		}else if(command.equals("PrintPurchaseBill")){
		    if(isPrintPurchaseBillFinished){
		        isPrintPurchaseBillFinished = false;
		        new Thread(){
		            public void run(){
		                printPurchaseBillItem(context);
		                isPrintPurchaseBillFinished = true;
		            }
		        }.start();
		    }
		}else if(command.equals("PrintQRCode")){
			printQRCodeItem((Activity)context);
		}else if(command.equals("PrintFont")){
			String test_1 ="0123456789abcdefghijklmnopgrstuvwxyz123456";
			
			int resultJni = PrinterInterface.PrinterOpen();
	        if(resultJni <0){
	            Log.e("App", "don't  open twice this devices");
	            PrinterInterface.PrinterClose();
	            return ;
	        }
			PrinterInterface.PrinterBegin();
			
			printNormalFont(test_1+"\n");
			
			printSmallFont(test_1+"\n");
			
			printNormalFont(test_1+"\n");
			
			PrinterInterface.PrinterEnd();
			PrinterInterface.PrinterClose();
			
		}else if(command.equals("PrinterOutOfPaper")){
			PrinterInterface.PrinterOpen();
			int result = 0;
			result = PrinterInterface.PrinterQuery();
			if(result ==0){
				LogHelper.infoMsgAndColor(textView, "Printer out of paper ， you need add paper for print!",4);
			}else {
				LogHelper.infoMsgAndColor(textView, "printer status is good ", 3);
			}
			PrinterInterface.PrinterClose();
		}
	}
	
	private void printDateItem(Context context) {
		((Activity) context).showDialog(PRINT_DATA);
	}

	public void closeDriveItem(){
		PrinterInterface.PrinterEnd();
		PrinterInterface.PrinterClose();
	}

	/**异常处理需要一个更好的机制，来完成。
	 * @param args
	 */
	
	private void printPurchaseBillItem(Context host){
		try {
			/*-----------demo data-----------*/
		    PurchaseBill purchaseBill = new PurchaseBill();
		    purchaseBill.setMerchantName("人民商场/REN MIN STORE");
		    purchaseBill.setMerchantNo("800201020800201");
		    purchaseBill.setTerminalNo("20063201");
		    purchaseBill.setOperator("01");
		    purchaseBill.setCardNumber("5359 18** **** 8888   MCC");
		    purchaseBill.setIssNo("01021000");
		    purchaseBill.setAcqNo("01031000");
		    purchaseBill.setTxnType("消费/SALE");
		    purchaseBill.setExpDate("2006/12");
		    purchaseBill.setBatchNo("000122");
		    purchaseBill.setVoucherNo("105233");
		    purchaseBill.setAuthNo("384928");
		    purchaseBill.setDataTime("2005/01/31 19:20:18");
		    purchaseBill.setRefNo("123456123456");
		    purchaseBill.setAmout("RMB 1234.56");
		    // purchaseBill.setTips("RMB 123.56");
		    // purchaseBill.setTotal("RMB 1358.12");
		    purchaseBill.setReference("重打印凭证/DUPLICATD");
		    
		    
		    /*-----------demo data-----------*/
		    Coupon coupon = Coupon.generateCoupon();
		    Bitmap bm=BitmapFactory.decodeResource(host.getResources(),com.wizarpos.apidemo.activity.R.drawable.test2dbarcode_trim);
		    PrinterHelper.getInstance().printerPurchaseBill(purchaseBill, coupon, bm);
		} catch (PrinterException e)
		{
		    e.printStackTrace();
		}
	}
	
	private void printNormalFont(String msg){
		byte [] contentbytes = null;
//		display normal font
		byte [] cmds = new byte[]{ 0x1B, 0x21, 0x00};
		PrinterInterface.PrinterWrite(cmds, cmds.length );
//		insert content to text;
		byte [] tempBytes = null;
		String msg_1 = "display normal font!\nsum the number of words in the line is 32 !\n";
		tempBytes = msg_1.getBytes();
		PrinterInterface.PrinterWrite(tempBytes, tempBytes.length);//
		contentbytes = msg.getBytes();
		PrinterInterface.PrinterWrite(contentbytes, contentbytes.length );
		
	}
	
	private void printSmallFont(String msg){
		byte [] contentbytes = null;
//		display small font
		byte [] cmds = new byte[]{ 0x1B, 0x21, 0x01};
		PrinterInterface.PrinterWrite(cmds, cmds.length );
//		insert content to text;
		byte [] tempBytes = null;
		String msg_1 = "display small font!\nsum the number of words in the line is 42 !\n";
		tempBytes = msg_1.getBytes();
		PrinterInterface.PrinterWrite(tempBytes, tempBytes.length);//
		contentbytes = msg.getBytes();
		PrinterInterface.PrinterWrite(contentbytes, contentbytes.length );
	}
	
	
	
	private Thread thread = null;
	boolean isFinished = true;
	private void printQRCodeItem(final Activity host){
		if(!isFinished){
		    return ;
		}
		
		thread = new Thread(){
			public void run(){
			    isFinished = false;
				try{
//		          Bitmap bm=BitmapFactory.decodeFile("/data/data/com.wizarpos.printer/test2dbarcode.png");
			        Bitmap bm=BitmapFactory.decodeResource(host.getResources(),com.wizarpos.apidemo.activity.R.drawable.test2dbarcode_trim);
//			        Bitmap bm=BitmapFactory.decodeStream(getResources().getAssets().open("test2dbarcode.png"));
//		          Bitmap bm=BitmapFactory.decodeResource(getResources(),R.drawable.triangle_center);
//			        PrinterBitmapUtil.printBitmapMSB(bm, 0, 0);//old way for print image
			        PrinterBitmapUtil.printBitmap(bm, 0, 0);
//			        PrinterBitmapUtil.printBitmap(Bitmap.createBitmap(1, 100, Config.ARGB_4444), 0, 0);
				}catch(Exception e){
					LogHelper.printerLog("Printer错误:无二维码文件");
				}finally{
				    isFinished = true;
				}
			}
		};
		thread.start();
		
	}
}
