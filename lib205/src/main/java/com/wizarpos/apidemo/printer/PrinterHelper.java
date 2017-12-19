package com.wizarpos.apidemo.printer;

import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.util.Log;

import com.wizarpos.apidemo.jniinterface.PrinterInterface;
import com.wizarpos.apidemo.printer.entity.PurchaseBill;
import com.wizarpos.apidemo.util.StringUtility;

public class PrinterHelper {
	/* 等待打印缓冲刷新的时间 */
	private static final int PRINTER_BUFFER_FLUSH_WAITTIME = /* 300 */150;

	private static PrinterHelper _instance;

	private PrinterHelper() {
	}

	synchronized public static PrinterHelper getInstance() {
		if (null == _instance)
			_instance = new PrinterHelper();
		return _instance;
	}

	/**
     * 
     */
	synchronized public void printerCoupon(Coupon coupon)
			throws PrinterException {
		int nTotal = 0;
		try {
//			PrinterInterface.PrinterOpen();
//			PrinterInterface.PrinterBegin();
			/*------------------------------------TOP------------------------------------*/
			// /* 向前走纸2行 */
			// nTotal += PrinterInterface.PrinterWrite(
			// PrinterCommand.getCmdEscDN(2),
			// PrinterCommand.getCmdEscDN(2).length);
			/* 居中对齐 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscAN(1),
					PrinterCommand.getCmdEscAN(1).length);
			/* 字体双倍宽度，双倍高度，加粗 */
			nTotal += PrinterInterface
					.PrinterWrite(PrinterCommand.getCmdEsc_N(Integer.parseInt(
							"0111000", 2)), PrinterCommand.getCmdEsc_N(Integer
							.parseInt("0111000", 2)).length);
			byte[] couponTitle = "优惠券".getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(couponTitle,
					couponTitle.length);

			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			/* 设置左对齐 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscAN(0),
					PrinterCommand.getCmdEscAN(0).length);
			/* 恢复默认字体 */
			nTotal += PrinterInterface
					.PrinterWrite(PrinterCommand.getCmdEsc_N(Integer.parseInt(
							"0000000", 2)), PrinterCommand.getCmdEsc_N(Integer
							.parseInt("0000000", 2)).length);
			byte[] type = String.format("编号:%s", coupon.getType()).getBytes(
					"GB2312");
			nTotal += PrinterInterface.PrinterWrite(type, type.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] desc = coupon.getFoodDescription().getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(desc, desc.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			/* 字体加粗 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscEN(1),
					PrinterCommand.getCmdEscEN(1).length);
			String rmbString = String.format("RMB:%.2f (原价:%.2f)", coupon.getCash(), coupon.getOriginCash());
			byte[] cash = rmbString.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(cash, cash.length);
			
//			Thread.sleep(PRINTER_BUFFER_FLUSH_WAITTIME);
			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
//			PrinterInterface.PrinterEnd();
//			PrinterInterface.PrinterClose();

			Log.i("APP", String.format("nTotal = %d\n", nTotal));
		} catch (UnsupportedEncodingException e) {
			throw new PrinterException("PrinterHelper.printerPurchaseBill():"
					+ e.getMessage(), e);
//		} catch (InterruptedException e) {
//			throw new PrinterException("PrinterHelper.printerPurchaseBill():"
//					+ e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new PrinterException(e.getMessage(), e);
		} finally {
//			PrinterInterface.PrinterEnd();
//			PrinterInterface.PrinterClose();
		}
	}

	/**
	 * 打印签购单
	 * 
	 * @throws PrinterException
	 */
	synchronized public void printerPurchaseBill(PurchaseBill purchaseBill, Coupon coupon, Bitmap bm)
			throws PrinterException {
		int nTotal = 0;
		Log.i("APP",
				"------------------printerPurchaseBill()------------------");
		try {
		    int resultJni = PrinterInterface.PrinterOpen();
	        if(resultJni <0){
	            Log.e("App", "don't  open twice this devices");
	            PrinterInterface.PrinterClose();
	            return ;
	        }
			PrinterInterface.PrinterBegin();

//			if (!purchaseBill.checkValidity()) {
//				throw new IllegalArgumentException(
//						"purchase bill check validity fail");
//			}

			/*------------------------------------TOP------------------------------------*/
			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			/* 居中对齐 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscAN(1),
					PrinterCommand.getCmdEscAN(1).length);
			/* 字体双倍宽度，双倍高度，加粗 */
			nTotal += PrinterInterface
					.PrinterWrite(PrinterCommand.getCmdEsc_N(Integer.parseInt(
							"0111000", 2)), PrinterCommand.getCmdEsc_N(Integer
							.parseInt("0111000", 2)).length);
			byte[] purchaseBillTitle = PrintTag.PurchaseBillTag.PURCHASE_BILL_TITLE
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(purchaseBillTitle,
					purchaseBillTitle.length);

			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			/* 设置左对齐 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscAN(0),
					PrinterCommand.getCmdEscAN(0).length);
			/* 恢复默认字体 */
			nTotal += PrinterInterface
					.PrinterWrite(PrinterCommand.getCmdEsc_N(Integer.parseInt(
							"0000000", 2)), PrinterCommand.getCmdEsc_N(Integer
							.parseInt("0000000", 2)).length);
			byte[] merchantNameTag = PrintTag.PurchaseBillTag.MERCHANT_NAME_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(merchantNameTag,
					merchantNameTag.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			/* 字体加粗 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscEN(1),
					PrinterCommand.getCmdEscEN(1).length);
			byte[] merchantNameValue = purchaseBill.getMerchantName().getBytes(
					"GB2312");
			nTotal += PrinterInterface.PrinterWrite(merchantNameValue,
					merchantNameValue.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			/* 恢复字体粗细 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscEN(0),
					PrinterCommand.getCmdEscEN(0).length);
			byte[] merchantNoTag = PrintTag.PurchaseBillTag.MERCHANT_NO_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(merchantNoTag,
					merchantNoTag.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] merchantNoValue = purchaseBill.getMerchantNo().getBytes();
			nTotal += PrinterInterface.PrinterWrite(merchantNoValue,
					merchantNoValue.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] terminalNoTag = PrintTag.PurchaseBillTag.TERMINAL_NO_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(terminalNoTag,
					terminalNoTag.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] terminalValue = purchaseBill.getTerminalNo().getBytes();
			nTotal += PrinterInterface.PrinterWrite(terminalValue,
					terminalValue.length);

			/* 等待打印buffer flush */
			Thread.sleep(PRINTER_BUFFER_FLUSH_WAITTIME);
			Log.i("APP", String.format("nTotal = %d\n", nTotal));

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] operatorTagValue = (PrintTag.PurchaseBillTag.OPERATOR_TAG + purchaseBill
					.getOperator()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(operatorTagValue,
					operatorTagValue.length);

			/*------------------------------------MIDDLE------------------------------------*/

			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			
			byte[] issNoTagValue = (PrintTag.PurchaseBillTag.ISS_NO_TAG + purchaseBill
					.getIssNo()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(issNoTagValue,
					issNoTagValue.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] acqNoTagValue = (PrintTag.PurchaseBillTag.ACQ_NO_TAG + purchaseBill
					.getAcqNo()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(acqNoTagValue,
					acqNoTagValue.length);

			String cardNoString = purchaseBill.getCardNumber();
			if (!StringUtility.isEmpty(cardNoString)) {
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] cardNumberTag = PrintTag.PurchaseBillTag.CARD_NUMBER_TAG
						.getBytes("GB2312");
				nTotal += PrinterInterface.PrinterWrite(cardNumberTag,
						cardNumberTag.length);
	
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] cardNumberValue = purchaseBill.getCardNumber().getBytes();
				nTotal += PrinterInterface.PrinterWrite(cardNumberValue,
						cardNumberValue.length);
			}

			String phoneNoString = purchaseBill.getPhoneNumber();
			if (!StringUtility.isEmpty(phoneNoString)) {
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] phoneNumberTag = PrintTag.PurchaseBillTag.PHONE_NUMBER_TAG
						.getBytes("GB2312");
				nTotal += PrinterInterface.PrinterWrite(phoneNumberTag,
						phoneNumberTag.length);
	
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] phoneNumberValue = purchaseBill.getPhoneNumber().getBytes();
				nTotal += PrinterInterface.PrinterWrite(phoneNumberValue,
						phoneNumberValue.length);
			}

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] txnTypeTagValue = (PrintTag.PurchaseBillTag.TXN_TYPE_TAG + purchaseBill
					.getTxnType()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(txnTypeTagValue,
					txnTypeTagValue.length);

			if (false) {
			if (!StringUtility.isEmpty(purchaseBill.getExpDate())) {
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] expDateTagValue = (PrintTag.PurchaseBillTag.EXP_DATE_TAG + purchaseBill
						.getExpDate()).getBytes("GB2312");
				nTotal += PrinterInterface.PrinterWrite(expDateTagValue,
						expDateTagValue.length);
			}

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] batchNoTagValue = (PrintTag.PurchaseBillTag.BATCH_NO_TAG + purchaseBill
					.getBatchNo()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(batchNoTagValue,
					batchNoTagValue.length);

			/* 等待打印buffer flush */
			Thread.sleep(PRINTER_BUFFER_FLUSH_WAITTIME);
			Log.i("APP", String.format("nTotal = %d\n", nTotal));

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] voucherNoTagValue = (PrintTag.PurchaseBillTag.VOUCHER_NO_TAG + purchaseBill
					.getVoucherNo()).getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(voucherNoTagValue,
					voucherNoTagValue.length);

			if (!StringUtility.isEmpty(purchaseBill.getAuthNo())) {
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] authNoTagValue = (PrintTag.PurchaseBillTag.AUTH_NO_TAG + purchaseBill
						.getAuthNo()).getBytes("GB2312");
				nTotal += PrinterInterface.PrinterWrite(authNoTagValue,
						authNoTagValue.length);
			}

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] refNoTag = PrintTag.PurchaseBillTag.REF_NO_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(refNoTag, refNoTag.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] refNoValue = purchaseBill.getRefNo().getBytes();
			nTotal += PrinterInterface.PrinterWrite(refNoValue,
					refNoValue.length);
			}
			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] dateTimeTag = PrintTag.PurchaseBillTag.DATE_TIME_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(dateTimeTag,
					dateTimeTag.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] dateTimeValue = purchaseBill.getDataTime().getBytes();
			nTotal += PrinterInterface.PrinterWrite(dateTimeValue,
					dateTimeValue.length);

			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			/* 字体加粗 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscEN(1),
					PrinterCommand.getCmdEscEN(1).length);
			byte[] amoutTag = PrintTag.PurchaseBillTag.AMOUT_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(amoutTag, amoutTag.length);
			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] amoutValue = purchaseBill.getAmout().getBytes();
			nTotal += PrinterInterface.PrinterWrite(amoutValue,
					amoutValue.length);
			
			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);

			if (false) {
			if (!StringUtility.isEmpty(purchaseBill.getTips())) {
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] tipsTag = PrintTag.PurchaseBillTag.TIPS_TAG
						.getBytes("GB2312");
				nTotal += PrinterInterface
						.PrinterWrite(tipsTag, tipsTag.length);
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] tipsValue = purchaseBill.getTips().getBytes();
				nTotal += PrinterInterface.PrinterWrite(tipsValue,
						tipsValue.length);
			}
			}

			/* 等待打印buffer flush */
			Thread.sleep(PRINTER_BUFFER_FLUSH_WAITTIME);
			Log.i("APP", String.format("nTotal = %d\n", nTotal));

			if (!StringUtility.isEmpty(purchaseBill.getTotal())) {

				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] totalTag = PrintTag.PurchaseBillTag.TOTAL_TAG
						.getBytes("GB2312");
				nTotal += PrinterInterface.PrinterWrite(totalTag,
						totalTag.length);
				/* 换行 */
				nTotal += PrinterInterface.PrinterWrite(
						PrinterCommand.getCmdLf(),
						PrinterCommand.getCmdLf().length);
				byte[] totalValue = purchaseBill.getTotal().getBytes();
				nTotal += PrinterInterface.PrinterWrite(totalValue,
						totalValue.length);
			}

			/*------------------------------------BOTTOM------------------------------------*/

			/* 恢复字体粗细 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscEN(0),
					PrinterCommand.getCmdEscEN(0).length);
			if (false) {
				if (!StringUtility.isEmpty(purchaseBill.getReference())) {
	
					/* 向前走纸2行 */
					nTotal += PrinterInterface.PrinterWrite(
							PrinterCommand.getCmdEscDN(2),
							PrinterCommand.getCmdEscDN(2).length);
					byte[] referenceTag = PrintTag.PurchaseBillTag.REFERENCE_TAG
							.getBytes("GB2312");
					nTotal += PrinterInterface.PrinterWrite(referenceTag,
							referenceTag.length);
					/* 换行 */
					nTotal += PrinterInterface.PrinterWrite(
							PrinterCommand.getCmdLf(),
							PrinterCommand.getCmdLf().length);
					byte[] referenceValue = purchaseBill.getReference().getBytes(
							"GB2312");
					nTotal += PrinterInterface.PrinterWrite(referenceValue,
							referenceValue.length);
				}
			}
			
			if (false) {
			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			byte[] cCardHolderSignatureTAG = PrintTag.PurchaseBillTag.C_CARD_HOLDER_SIGNATURE_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(cCardHolderSignatureTAG,
					cCardHolderSignatureTAG.length);
			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] eCardHolderSignatureTAG = PrintTag.PurchaseBillTag.E_CARD_HOLDER_SIGNATURE_TAG
					.getBytes();
			nTotal += PrinterInterface.PrinterWrite(eCardHolderSignatureTAG,
					eCardHolderSignatureTAG.length);
			/* 向前走纸4行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(4),
					PrinterCommand.getCmdEscDN(4).length);
			byte[] cAgreeTradeTag = PrintTag.PurchaseBillTag.C_AGREE_TRADE_TAG
					.getBytes("GB2312");
			nTotal += PrinterInterface.PrinterWrite(cAgreeTradeTag,
					cAgreeTradeTag.length);
			/* 换行 */
			nTotal += PrinterInterface.PrinterWrite(PrinterCommand.getCmdLf(),
					PrinterCommand.getCmdLf().length);
			byte[] eAgreeTradeTag = PrintTag.PurchaseBillTag.E_AGREE_TRADE_TAG
					.getBytes();
			nTotal += PrinterInterface.PrinterWrite(eAgreeTradeTag,
					eAgreeTradeTag.length);
			}

			Thread.sleep(PRINTER_BUFFER_FLUSH_WAITTIME);

//			/* 向前走纸2行 */
//			nTotal += PrinterInterface.PrinterWrite(
//					PrinterCommand.getCmdEscDN(2),
//					PrinterCommand.getCmdEscDN(2).length);
			
			
			printerCoupon(coupon);
//			PrinterBitmapUtil.printBitmap(bm, 0, 0);
			
			/* 向前走纸2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			
			/* 再向前走2行 */
			nTotal += PrinterInterface.PrinterWrite(
					PrinterCommand.getCmdEscDN(2),
					PrinterCommand.getCmdEscDN(2).length);
			
			Log.i("APP", "end of the printing action!\n");
			Log.i("APP", String.format("nTotal = %d\n", nTotal));

			PrinterInterface.PrinterEnd();
			PrinterInterface.PrinterClose();

		} catch (UnsupportedEncodingException e) {
			throw new PrinterException("PrinterHelper.printerPurchaseBill():"
					+ e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new PrinterException("PrinterHelper.printerPurchaseBill():"
					+ e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new PrinterException(e.getMessage(), e);
		} finally {
			PrinterInterface.PrinterEnd();
			PrinterInterface.PrinterClose();
		}
	}
}