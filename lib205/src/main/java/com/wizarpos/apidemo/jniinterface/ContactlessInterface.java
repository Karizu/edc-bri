package com.wizarpos.apidemo.jniinterface;

import android.util.Log;

public class ContactlessInterface {
	// load *.so library for use
	static {
		try {
			System.loadLibrary("jni_wizarpos_contactlesscard");
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("JAVA_JNI", "load jni_wizarpos_contactlesscard failed");
			e.printStackTrace();
		}

	}

	public static int CONTACTLESS_CARD_MODE_AUTO = 0;
	public static int CONTACTLESS_CARD_MODE_TYPE_A = 1;
	public static int CONTACTLESS_CARD_MODE_TYPE_B = 2;
	public static int CONTACTLESS_CARD_MODE_TYPE_C = 3;

	public static int RC500_COMMON_CMD_GET_READER_VERSION = 0x40;
	public static int RC500_COMMON_CMD_RF_CONTROL = 0x38; // command data :
	// 0x01(turn on),
	// 0x00(turn off)

	/**
	 * Initialize the contactless1 card reader
	 *
	 * @return value == 0, error ; value != 0 , correct handle
	 */
	public synchronized native static int Open();

	/**
	 * Close the contactless1 card reader
	 *
	 * @return value >= 0, success ; value < 0, error code
	 */
	public synchronized native static int Close();

	/**
	 * Start searching the contactless1 card If you set the nCardMode is auto,
	 * reader will try to activate card in type A, type B and type successively;
	 * If you set the nCardMode is type A, type B, or type C, reader only try to
	 * activate card in the specified way.
	 *
	 * @param :              nCardMode : handle of this card reader possible value :
	 *                       CONTACTLESS_CARD_MODE_AUTO = 0 CONTACTLESS_CARD_MODE_TYPE_A = 1;
	 *                       CONTACTLESS_CARD_MODE_TYPE_B = 2; CONTACTLESS_CARD_MODE_TYPE_C =
	 *                       3;
	 * @param nFlagSearchAll : 0 : signal user if we find one card in the field 1 : signal
	 *                       user only we find all card in the field
	 * @param nTimeout_MS    : time out in milliseconds. if nTimeout_MS is less then zero,
	 *                       the searching process is infinite.
	 * @return value >= 0, success in starting the process; value < 0, error
	 * code
	 */
	public native static int SearchTargetBegin(int nCardMode, int nFlagSearchAll, int nTimeout_MS);

	/**
	 * Stop the process of searching card.
	 *
	 * @return :value >= 0, success in starting the process; value < 0, error
	 * code
	 */

	public native static int SearchTargetEnd();

	/**
	 * Attach the target before transmitting apdu command In this process, the
	 * target(card) is activated and return ATR
	 *
	 * @param byteArrayATR ATR buffer, if you set it null, you can not get the data.
	 * @return :value >= 0, success in starting the process and return length of
	 * ATR; value < 0, error code
	 */
	public native static int AttachTarget(byte byteArrayATR[]);

	/**
	 * Detach the target. If you want to send APDU again, you should attach it.
	 *
	 * @return :value >= 0, success in starting the process; value < 0, error
	 * code
	 */
	public native static int DetachTarget();

	/**
	 * Transmit APDU command and get the response
	 *
	 * @param byteArrayAPDU     : command of APDU
	 * @param nAPDULength       : length of command of APDU
	 * @param byteArrayResponse : response of command of APDU
	 * @return value >= 0, success in starting the process; value < 0, error
	 * code
	 */
	public native static int Transmit(byte byteArrayAPDU[], int nAPDULength, byte byteArrayResponse[]);

	/**
	 * Send control command.
	 *
	 * @param nCmdID           : id of command
	 * @param byteArrayCmdData : data associated with command, if no data, you can set it
	 *                         NULL
	 * @param nDataLength      : data length of command
	 * @return value >= 0, success in starting the process; value < 0, error
	 * code
	 */

	public native static int SendControlCommand(int nCmdID, byte byteArrayCmdData[], int nDataLength);

	/**
	 * å›žè°ƒå‡½æ•°ä¸­pEventDataä¸­çš„æ•°æ�®å®šä¹‰ï¼š SAK ATQA[0] ATQA[1] uid
	 * <p>
	 * SAK ï¼š 1å­—èŠ‚<br/>
	 * ATQA[0] ï¼š 1å­—èŠ‚<br/>
	 * ATQA[1] : 1å­—èŠ‚<br/>
	 * UID ï¼š 4å­—èŠ‚æˆ–è€…7å­—èŠ‚æˆ–è€…10å­—èŠ‚
	 * </p>
	 *
	 * @param nTimeout_MS : time out in milliseconds. if nTimeout_MS is less then zero,
	 *                    the event process is infinite.
	 * @param event       :
	 * @return value >= 0, success in starting the process; value < 0, error
	 * code
	 */
	public native static int PollEvent(int nTimeout_MS, ContactlessEvent event);

	/**
	 * Verify pin only for MiFare one card
	 *
	 * @param[in] : unsigned int nSectorIndex : sector index
	 * @param[in] : unsigned int nPinType : 0 : A type 1 : B type
	 * @param[in] : unsigned char* strPin : password of this pin
	 * @param[in] : unsigned int nPinLength : length of password return value :
	 * >= 0 : success
	 */
	public synchronized native static int VerifyPinMemory(int nSectorIndex, int nPinType, byte[] strPin, int nPinLength);

	/**
	 * Read data only for MiFare one card
	 *
	 * @param[in] : unsigned int nSectorIndex : sector index
	 * @param[in] : unsigned int nBlockIndex : block index
	 * @param[out] : unsigned char* pDataBuffer : data buffer
	 * @param[in] : unsigned int nDataBufferLength : buffer length return value
	 * : >= 0 : data length < 0 : error code
	 */
	public synchronized native static int ReadMemory(int nSectorIndex, int nBlockIndex, byte[] pDataBuffer, int nDataBufferLength);

	/**
	 * Write data only for MiFare one card
	 *
	 * @param[in] : unsigned int nSectorIndex : sector index
	 * @param[in] : unsigned int nBlockIndex : block index
	 * @param[in] : unsigned char* pData : data
	 * @param[in] : unsigned int nDataLength : data length return value : >= 0 :
	 * success < 0 : error code
	 */
	public synchronized native static int WriteMemory(int nSectorIndex, int nBlockIndex, byte[] pData, int nDataLength);

	/**
	 * Read data from 15693 card
	 *
	 * @param[in] : int nHandle : handle of this card reader
	 * @param[in] : unsigned int nBlockIndex : block index
	 * @param[in] : unsigned int nBlockCount : block count
	 * @param[in] : unsigned char* pDataBuffer : data buffer for saving data
	 * @param[in] : unsigned int nDataBufferLength : length of data buffer
	 * return value : >= 0 : success < 0 : error code
	 */
	public native static int Read15693Memory(int nBlockIndex, int nBlockCount, byte[] pData, int nDataLength);

	/**
	 * Write data to 15693 MiFare card
	 *
	 * @param[in] : int nHandle : handle of this card reader
	 * @param[in] : unsigned int nBlockIndex : block index
	 * @param[in] : unsigned char* pData : data
	 * @param[in] : unsigned int nDataLength : data length, max data length is
	 * 32 return value : >= 0 : success < 0 : error code
	 */
	public native static int Write15693Memory(int nBlockIndex, byte[] pData, int nDataLength);

	/**
	 * @param[in] : int nHandle : handle of this card reader
	 * @param[out] : unsigned int* pHasMoreCards : 0 : only one PICC in the
	 * field 0x0A : more cards in the field(type A) 0x0B : more
	 * cards in the field(type B) 0xAB : more cards in the
	 * field(type A and type B)
	 * @param[out] : unsigned int * pCardType :
	 * CONTACTLESS_CARD_TYPE_A_CPU 0x0000
	 * CONTACTLESS_CARD_TYPE_B_CPU 0x0100
	 * CONTACTLESS_CARD_TYPE_A_CLASSIC_MINI 0x0001
	 * CONTACTLESS_CARD_TYPE_A_CLASSIC_1K 0x0002
	 * CONTACTLESS_CARD_TYPE_A_CLASSIC_4K 0x0003
	 * CONTACTLESS_CARD_TYPE_A_UL_64 0x0004
	 * CONTACTLESS_CARD_TYPE_A_UL_192 0x0005
	 * CONTACTLESS_CARD_TYPE_A_MP_2K_SL1 0x0006
	 * CONTACTLESS_CARD_TYPE_A_MP_4K_SL1 0x0007
	 * CONTACTLESS_CARD_TYPE_A_MP_2K_SL2 0x0008
	 * CONTACTLESS_CARD_TYPE_A_MP_4K_SL2 0x0009
	 * CONTACTLESS_CARD_UNKNOWN 0x00FF return value : >= 0 : success
	 * < 0 : error code
	 */
	public synchronized native static int queryInfo(int[] pHasMoreCards, int[] pCardType);

}