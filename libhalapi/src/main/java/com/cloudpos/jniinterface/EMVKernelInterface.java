package com.cloudpos.jniinterface;

public class EMVKernelInterface {
 	static
 	{
 		String fileName = "jni_cloudpos_emv";
		JNILoad.jniLoad(fileName);
 	}
// 	public static byte bStatus = -1;
// 	public static byte bRetCode = -1;
// 	public static EMVTransListener   sEmvTransListener 	= null;
//	public static EMVTransData		  sEMVTransData 		= null;
//	public static boolean needInit = true;
	public static byte[] termInfoByte = new byte[77];
 	
	public synchronized native static byte loadEMVKernel();
	public synchronized native static byte exitEMVKernel();
	
	// Card Functions
	public synchronized native static int open_reader(int reader);
	public synchronized native static int open_reader_ex(int reader, int extraParam);	// extraParam : 0 - need look for card; 1 - Card Inserted
	public synchronized native static void close_reader(int reader);
	public synchronized native static int get_card_type();
	public synchronized native static int get_card_atr(byte[] atr);
	public synchronized native static int transmit_card(byte[] cmd, int cmdLength, byte[] respData, int respDataLength);
	// EMV Functions
	public synchronized native static void emv_kernel_initialize();                                                          // 0
	public synchronized native static int emv_is_tag_present(int tag);                              	                        // 1
	public synchronized native static int emv_get_tag_data(int tag, byte[] data, int dataLength);	                        // 2
	public synchronized native static int emv_get_tag_list_data(int[] tagList, int tagCount, byte[] data, int dataLength);	// 3
	public synchronized native static int emv_set_tag_data(int tag, byte[] data, int dataLength);                     		// 4
	public synchronized native static int emv_preprocess_qpboc();                                                            // 5
	public synchronized native static void emv_trans_initialize();                                                           // 6
	public synchronized native static int emv_get_version_string(byte[] data, int dataLength);					            // 7
	public synchronized native static int emv_set_trans_amount(byte[] amount); 							                    // 8  ASC 以分为单位
	public synchronized native static int emv_set_other_amount(byte[] amount);						                        // 9
	public synchronized native static int emv_set_trans_type(byte transType);							                    //10
	public synchronized native static int emv_set_kernel_type(byte kernelType);							                    //11
	public synchronized native static int emv_get_kernel_type();                                                             //12
	public synchronized native static int emv_process_next();												                //13: 仅此项为会触发emvProcessCallback
	public synchronized native static int emv_is_need_advice();							                                    //14
	public synchronized native static int emv_is_need_signature();							                                //15
	public synchronized native static int emv_set_force_online(int flag);							                        //16
	public synchronized native static int emv_get_card_record(byte[] data, int dataLength);							        //17
	public synchronized native static int emv_get_candidate_list(byte[] data, int dataLength);							    //18
	public synchronized native static int emv_set_candidate_list_result(int index);							                //19
	public synchronized native static int emv_set_id_check_result(int result);							                    //20
	public synchronized native static int emv_set_online_pin_entered(int result);							                //21
	public synchronized native static int emv_set_pin_bypass_confirmed(int result);							                //22
	public synchronized native static int emv_set_online_result(int result, byte[] respCode, byte[] issuerRespData, int issuerRespDataLength); // 23
	
	public synchronized native static int emv_aidparam_clear();                             							        //24
	public synchronized native static int emv_aidparam_add(byte[] data, int dataLength);							            //25
	public synchronized native static int emv_capkparam_clear();							                                    //26
	public synchronized native static int emv_capkparam_add(byte[] data, int dataLength);							        //27
	public synchronized native static int emv_terminal_param_set(byte[] TerminalParam);							            //28
	public synchronized native static int emv_terminal_param_set2(byte[] TerminalParam, int paramLength);                    //29
	public synchronized native static int emv_exception_file_clear();							                            //30
	public synchronized native static int emv_exception_file_add(byte[] exceptFile);							                //31
	public synchronized native static int emv_revoked_cert_clear();							                                //32
	public synchronized native static int emv_revoked_cert_add(byte[] revokedCert);							                //33
	public synchronized native static int emv_reload_upcash_balance();							                            //34
	public synchronized native static int emv_set_fastest_qpboc_process(int result);                                  //35
	
	public  void emvProcessCallback(byte[] data)
	{
//		bStatus = data[0];
//		bRetCode= data[1];
//		ReadCardThread tmpThread = new ReadCardThread();
//		tmpThread.start();
	}
	public static void cardEventOccured(int eventType)
	{
//		SDK_Utils.onCardEvent(eventType);
	}
	
//	public class  ReadCardThread extends Thread{
//		public void run() {
//			SDK_Utils tmpUtils = new SDK_Utils();
//			tmpUtils.onEmvProcessCallback(bStatus, bRetCode);
//		}
//	}
}
