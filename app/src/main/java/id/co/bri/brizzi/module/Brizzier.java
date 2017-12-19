package id.co.bri.brizzi.module;

import id.co.bri.brizzi.handler.ConAsync;
import id.co.bri.brizzi.handler.PsamCard;
import id.co.bri.brizzi.module.listener.ReqListener;
import id.co.bri.brizzi.module.listener.TapListener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by indra on 13/01/16.
 */
public class Brizzier extends ContactlessQ1 implements ReqListener {
    private final static String url = "http://150.129.189.5:14911//ARRest";
    private PsamCard psam;
    private List<TapListener> tapListeners = new ArrayList<>();
    //BCD bcd = new BCD();
    private ConAsync con;
    private Context context;

    public Brizzier(Context context){
        this.context = context;
        cData.setWhatToDo("CekSaldo");
        cData.setMerchanID("1000000000000001");
        cData.setTerminalID("2000000000000001");

        initContactless();
        psam = new PsamCard();
        psam.starting(1);
        searchBegin();
    }

    private void initContactless() {
        myHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SAM_NOT_READY:
                        Log.d(TAG, "Sam Card Not Ready");
                        break;
                    case CARD_TAP_NOTIFIER:

                        String whattodo = cData.getWhatToDo();
                        processCMD(cData.getWhatToDo());

                        break;
                    case HOST_REPLY_NOTIFIER:
                        NextTopupStep();
                        dettatch();
                        break;
                    case SAM_READY_NOTIFIER:
                        processCMD("SAM");

                        break;
                    case CARD_RESPONSE_ERROR:
                        UncommitTransaction();
                        EndThenRestart();
                        break;
                    case CARD_RESPONSE_FINISH:
                        EndThenRestart();
                        break;
                }
            }
        };
    }
    private void EndThenRestart() {
        psam.closedevice();
        dettatch();
        isExitThreadFlag = true;
    }

    private void processCMD(String cmdType) {
        if (cmdType.equals("SAM")) {
            // 1. SAM � Select AID SAM
            psam.sendCmd(hexStringToByteArray("00A4040C09A00000000000000011"));
        } else if (cmdType.equals("Topup")) {
            cData.setTopupAmount("10000");
            attatch();
            Topup();
        } else if (cmdType.equals("Deduct")) {
            attatch();
            cData.setDeductAmount("1");
            Deduct();
        } else if (cmdType.equals("CekSaldo")) {
            attatch();
            CekSaldoOnly();
        } else {
            // do nothing
        }
    }

    public void Topup() {
        String CardResponse = "";
        String SamResponse = "";
        CekSaldo();
        // 12. SAM � Get Key Topup
        SamResponse = psam.sendCmd(hexStringToByteArray("80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000"));
        String RandomSam24B = SamResponse.substring(2, SamResponse.length() - 4);
        Log.d(TAG, "cmd: 80B3000013" + cData.getCardNumber() + cData.getUid() + "00000000 || RandomSam24B :" + RandomSam24B);
        // 13. HOST � Get Auth Key Topup
        sendToServer(RandomSam24B + "0000010000");
        String keyTopup24B = CardResponse;
        Log.d(TAG, "cmd: server Reply" + CardResponse + " || keyTopup24B:  " + keyTopup24B + "|  ");


    }

    public void NextTopupStep() {
        Log.d(TAG, "Lanjut: " + cData.getHostResponse());
        String keyTopup24B = cData.getHostResponse();
        if (!(keyTopup24B.length() == 48)) {
            Log.e(TAG, "Error Response from server !!" + " || response: " + keyTopup24B);
            return;
        }
        // 14. Card � Select AID 3
        String CardResponse = transmitCmd("5A030000");
        Log.d(TAG, "cmd: 5A030000" + " || response: " + CardResponse);
        //15. Card � Request Key Card 01
        CardResponse = transmitCmd("0A01");
        String keyCard018B = CardResponse.substring(2);
        Log.d(TAG, "cmd: 0A01" + " || response: " + keyCard018B);
        // 16. SAM � Authenticate Topup
        String sendtosam = "80B2000037" + keyTopup24B + cData.getCardNumber() + cData.getUid() + "FF0000030180000000" + keyCard018B;
        String SamResponse = psam.sendCmd(hexStringToByteArray(sendtosam));
        String RandomKey16B = SamResponse.substring(0, SamResponse.length() - 4);
        // 17. Card � Authenticate Card
        CardResponse = transmitCmd("AF" + RandomKey16B);
        String RandomNumber8B = CardResponse.substring(2);
        Log.d(TAG, "cmd: AF" + RandomKey16B + " || response: " + CardResponse);
        // 18. Card � Credit Balance
        String topupAmount = cData.getTopupAmount(); //--- nilai yg akan di topup
        CardResponse = transmitCmd("0C00" + ItoH(topupAmount) + "00");
        String CardBalance4B = CardResponse.substring(2);
        Log.d(TAG, "cmd: 0A01" + " || response: " + CardResponse + "| Card Balance = " + CardBalance4B);
        if (CardResponse.length() < 10) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        // 19. SAM � Create Hash
        String transactionData = Hex3(cData.getCardNumber()) + Hex3(nominalTransaksi(cData.getTopupAmount())) + Hex3(cData.gettDate()) + Hex3(cData.gettTime()) + Hex3("818001") + Hex3("000036") + Hex3("03") + "FFFFFFFF";
        sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000" + RandomNumber8B + transactionData;
        SamResponse = psam.sendCmd(hexStringToByteArray(sendtosam));
        String Hash4B = SamResponse.substring(0, SamResponse.length() - 4);
        if (!(SamResponse.substring(SamResponse.length() - 4).equals("90000"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        cData.setHash4BTopup(Hash4B);
        // 20. Card � Write Log
        String balanceBeforeint = HtoI(cData.getCardBalance4B());
        Log.d(TAG, "balanceBefore3B step 1 : " + balanceBeforeint);
        int bAfter = Integer.parseInt(balanceBeforeint) - Integer.parseInt(cData.getTopupAmount());
        Log.d(TAG, "BalanceAfter int : " + bAfter);
        String bAfter3B = ItoH(Integer.toString(bAfter));
        Log.d(TAG, "BalanceAfter3B : " + bAfter3B);
        String balanceBefore3B = ItoH(balanceBeforeint);
        Log.d(TAG, "balanceBefore3B step 2 : " + balanceBefore3B);
        String cmdWritelog = "3B01000000200000" + cData.getMerchanID() + cData.getMerchanID() + cData.gettDate() + cData.gettTime() + "EB" + ItoH(cData.getDeductAmount()) + balanceBefore3B + bAfter3B;
        CardResponse = transmitCmd(cmdWritelog);
        Log.d(TAG, "CardResponse : " + CardResponse);
        if (!(CardResponse.equals("00"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        // 21. Card � Write Last transaction
//		String lastTransMonth = cData.getLastTransDate().substring(0,4);
//		String nowMonth = cData.gettDate().substring(0,4);
        String akumdebet = "00000000";
        if (cData.getLastTransDate().substring(0, 4).equals(cData.gettDate().substring(0, 4))) {
            akumdebet = Integer.toString(Integer.parseInt(cData.getAkumDebet()) + Integer.parseInt(cData.getTopupAmount()));
        } else {
            akumdebet = cData.getTopupAmount();
        }
        akumdebet = this.nominalTransaksi(akumdebet);
        Log.d(TAG, "akumdebet : " + akumdebet);
        CardResponse = transmitCmd("3D03000000070000" + cData.gettDate() + akumdebet);
        Log.d(TAG, "CardResponse : " + CardResponse);
        if (!(CardResponse.equals("00"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        CardResponse = transmitCmd("C7");
        Log.d(TAG, "cmd: C7 | response: " + CardResponse);
        if (!(CardResponse.equals("00"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
    }

    private void Deduct() {
        String CardResponse = "";
        String SamResponse = "";
        CekSaldo();
        // 12. Card � Debit Balance
        String deductAmount = cData.getDeductAmount();
        String reverseAmount3B = ItoH(deductAmount);
        CardResponse = transmitCmd("DC00" + reverseAmount3B + "00");
        //CardResponse = transmitCmd("DC0032000000");
        String cardBalanceRslt = CardResponse.substring(2); //-- balance kartu setelah di potong deductAmount
        Log.d(TAG, "cmd: " + "DC00" + ItoH(deductAmount) + "00 | " + "DEBIT BALANCE CardResponse: " + CardResponse);
		if (CardResponse.length()<10){
			sendMsgWhat(CARD_RESPONSE_ERROR);
			return;
		}
        // 13. SAM � Create Hash
        cData.settDate(getStringDate2());
        cData.settTime(getStringTime());
        String NominalTransaksi = nominalTransaksi(deductAmount);
        String TansactionData = Hex3(cData.getCardNumber()) + Hex3(NominalTransaksi) + Hex3(cData.gettDate()) + Hex3(cData.gettTime()) + Hex3("818001") + Hex3("000036") + Hex3("03") + "FFFFFFFF";
        Log.d(TAG, "TansactionData : " + TansactionData);
        String sendtosam = "80B4000058" + cData.getCardNumber() + cData.getUid() + "FF0000030080000000" + cData.getRandomNumber8B() + TansactionData;
        SamResponse = psam.sendCmd(hexStringToByteArray(sendtosam));
        Log.d(TAG, "SamResponse : " + SamResponse + "| hash 4B: " + SamResponse.substring(2, 10));
        cData.setHash4B(SamResponse.substring(2, 10));
        if (!(SamResponse.substring(SamResponse.length() - 4).equals("9000"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        // 14. Card � Write Log
        String CardBalanceBefore3B = ItoH(cData.getCardBalanceInt());
        int balanceAfter = Integer.parseInt(cData.getCardBalanceInt()) - Integer.parseInt(deductAmount);
        cData.setNewBalance(Integer.toString(balanceAfter));
        String balanceAfter3B = ItoH(Integer.toString(balanceAfter));
        String sendTocard = "3B01000000200000" + cData.getMerchanID() + cData.getTerminalID() + cData.gettDate() + cData.gettTime() + "EB" + reverseAmount3B + CardBalanceBefore3B + balanceAfter3B;
        CardResponse = transmitCmd(sendTocard);
        Log.d(TAG, "cmd: " + sendTocard + " | response: " + CardResponse);
        if (!(CardResponse.equals("00")||(CardResponse.equals("AF")))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
        CardResponse = transmitCmd("C7");
        Log.d(TAG, "cmd: C7 | response: " + CardResponse);
        Log.d(TAG, "new Balance after Deduct: " + cData.getNewBalance());
        if (!(CardResponse.equals("00"))) {
            sendMsgWhat(CARD_RESPONSE_ERROR);
            return;
        }
    }

    public void CekSaldo() {
        String CardResponse = "";
        String SamResponse = "";
        //2. Card � Select AID 1
        CardResponse = transmitCmd("5A010000");
        Log.d(TAG, "cmd:5A010000 || " + CardResponse);
        // 3. Card � Get Card Number
        CardResponse = transmitCmd("BD00000000170000");
        String CardNumber = CardResponse.substring(8, 8 + 16);
        cData.setCardNumber(CardNumber);
        Log.d(TAG, "cmd:BD00000000170000 || " + CardResponse + " CardNumber:" + CardNumber);
        // 4. Card � Get Card Status
        CardResponse = transmitCmd("BD01000000200000").substring(8, 12);
        Log.d(TAG, "cmd:BD0100000200000 || " + CardResponse);
        // 5. Card � Select AID 3
        CardResponse = transmitCmd("5A030000");
        Log.d(TAG, "cmd: 5A030000 || " + CardResponse);
        // 6. Card � Request Key Card
        CardResponse = transmitCmd("0A00");
        String Keycard = CardResponse.substring(2);
        Log.d(TAG, "cmd: 0A00 || " + Keycard);
        // 7. Card � Get UID
        Log.d(TAG, "UID || " + cData.getUid());
        // 8. SAM � Authenticate Key
        SamResponse = psam.sendCmd(hexStringToByteArray("80B0000020" + CardNumber + cData.getUid() + "FF0000030080000000" + Keycard));
        Log.d(TAG, "SAM � Authenticate Key : " + SamResponse);
        String RandomKey16B = SamResponse.substring(SamResponse.length() - 36);
        RandomKey16B = RandomKey16B.substring(0, RandomKey16B.length() - 4);
        Log.d(TAG, "Randomkey16B : " + RandomKey16B);
        // 9. Card � Authenticate Card
        CardResponse = transmitCmd("AF" + RandomKey16B);
        String RandomNumber8B = CardResponse.substring(2);
        cData.setRandomNumber8B(RandomNumber8B);
        Log.d(TAG, "cmd: AF+" + RandomKey16B + " || " + CardResponse + " || RandomNumber8B: " + RandomNumber8B);
        //  10. Card � Get Last Transaction Date
        CardResponse = transmitCmd("BD03000000070000");
        Log.d(TAG, "cmd: BD03000000070000 || " + CardResponse + " || last trans: " + cData.getLastTransDate() + " akundebet: " + cData.getAkumDebet());
        cData.setLastTransDate(CardResponse.substring(2, 8));
        cData.setAkumDebet(CardResponse.substring(8, 16));

        // 11. Card � Get Balance
        CardResponse = transmitCmd("6C00");
        cData.setCardBalance4B(CardResponse.substring(2));
        cData.setCardBalanceInt(HtoI(CardResponse.substring(2)));
        Log.d(TAG, "cmd: 6C00 || Balance:  " + cData.getCardBalance4B());
        Log.d(TAG, "Balance integer: " + HtoI(cData.getCardBalance4B()));
//        for(TapListener tapListener : tapListeners){
//            tapListener.onTapCompleted("Cek Saldo",cData);
//        }
    }

    public void addTapListener(TapListener tapListener){
        this.tapListeners.add(tapListener);
    }

    public void CekSaldoOnly() {
        String CardResponse = "";
        String SamResponse = "";
        //2. Card � Select AID 1
        CardResponse = transmitCmd("5A010000");
//		Log.d(TAG,"cmd:5A010000 || "+CardResponse);
        // 3. Card � Get Card Number
        CardResponse = transmitCmd("BD00000000170000");
        String CardNumber = CardResponse.substring(8, 8 + 16);
//		cData.setCardNumber(CardNumber);
//		Log.d(TAG,"cmd:BD00000000170000 || "+CardResponse+ " CardNumber:"+CardNumber);
//		// 4. Card � Get Card Status
//		CardResponse = transmitCmd("BD01000000200000").substring(8,12);
//		Log.d(TAG,"cmd:BD0100000200000 || "+CardResponse);
        // 5. Card � Select AID 3
        CardResponse = transmitCmd("5A030000");
//		Log.d(TAG,"cmd: 5A030000 || "+CardResponse);
        // 6. Card � Request Key Card
        CardResponse = transmitCmd("0A00");
        String Keycard = CardResponse.substring(2);
//		Log.d(TAG,"cmd: 0A00 || "+Keycard);
        // 7. Card � Get UID
//		Log.d(TAG,"UID || "+cData.getUid());
        // 8. SAM � Authenticate Key
        SamResponse = psam.sendCmd(hexStringToByteArray("80B0000020" + CardNumber + cData.getUid() + "FF0000030080000000" + Keycard));
//		Log.d(TAG,"SAM � Authenticate Key : "+SamResponse);
        String RandomKey16B = SamResponse.substring(SamResponse.length() - 36);
        RandomKey16B = RandomKey16B.substring(0, RandomKey16B.length() - 4);
//		Log.d(TAG,"Randomkey16B : "+RandomKey16B);
        // 9. Card � Authenticate Card
        CardResponse = transmitCmd("AF" + RandomKey16B);
        String RandomNumber8B = CardResponse.substring(2);
        cData.setRandomNumber8B(RandomNumber8B);
//		Log.d(TAG,"cmd: AF+"+RandomKey16B+" || "+CardResponse+ " || RandomNumber8B: "+RandomNumber8B);
//		//  10. Card � Get Last Transaction Date
//		CardResponse = transmitCmd("BD03000000070000");
//		Log.d(TAG,"cmd: BD03000000070000 || "+ CardResponse+ " || last trans: "+cData.getLastTransDate() + " akundebet: "+cData.getAkumDebet());
//		cData.setLastTransDate(CardResponse.substring(2,8));
//		cData.setAkumDebet(CardResponse.substring(8,16));

        // 11. Card � Get Balance
        CardResponse = transmitCmd("6C00");
        cData.setCardBalance4B(CardResponse.substring(2));
//		Log.d(TAG,"response Balance 6C00 = "+CardResponse);
        cData.setCardBalanceInt(HtoI(CardResponse.substring(2)));
//		Log.d(TAG,"cmd: 6C00 || Balance:  "+cData.getCardBalance4B());
        Log.d(TAG, "Balance integer: " + HtoI(cData.getCardBalance4B()));

//		Log.d(TAG, "Balance hex: "+ItoH("25000"));
//		Log.d(TAG, "Balance hex: "+HtoI(ItoH("25000")+"00"));
    }

    private void UncommitTransaction() {
        // unCommit transaction
        String CardResponse = transmitCmd("A7");
        Log.d(TAG, "cmd: A7 | response: " + CardResponse);
    }

    private String sendToServer(String dataTosend) {
        con = new ConAsync(Brizzier.this);
        con.setRequestMethod("POST", getPostData(dataTosend));
        con.execute(url);
        return "sudah kirim";
    }

    private String getPostData(String dataToSend) {

        String retval = null;
        TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        if (imei.length() <= 2)
            imei = "358586060001548";
        JSONObject rootObj = new JSONObject();
        JSONObject obj = new JSONObject();
        String datas = "25000|6013010612791674=17121200000071100000|666666|" + dataToSend;
        try {
            obj.put("msg_id", imei + getStringDate());
            obj.put("msg_ui", imei);
            obj.put("msg_si", "A25100");
            obj.put("msg_dt", datas);
            rootObj.put("msg", obj);

            retval = rootObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retval;
    }

    //--yyyymmdd
    private String getStringDate() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE)) +
                String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));
        return retval;
    }

    //--- yymmdd
    private String getStringDate2() {
        Calendar c = Calendar.getInstance();
        String retval = Integer.toString(c.get(Calendar.YEAR)) +
                String.format("%02d", c.get(Calendar.MONTH) + 1) +
                String.format("%02d", c.get(Calendar.DATE));

        return retval.substring(2);
    }

    //--His
    private String getStringTime() {
        Calendar c = Calendar.getInstance();
        String retval = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d", c.get(Calendar.MINUTE)) +
                String.format("%02d", c.get(Calendar.SECOND));

        return retval;
    }

    private void sendMsgWhat(int msgwhat) {
        Message msg = new Message();
        msg.what = msgwhat;
        myHandler.sendMessage(msg);
    }

    @Override
    public void onReqCompleted(String result) {
        //Toast.makeText(Dynamic_form.this, result, Toast.LENGTH_LONG).show();
        //JsonParser jp = new JsonParser(result);
        cData.setHostResponse(result);
        Message msg = new Message();
        msg.what = HOST_REPLY_NOTIFIER;
        myHandler.sendMessage(msg);
        Log.i(TAG, "Result :" + result);

    }

    @Override
    public void onNoInternetConnection() {
        //---??
    }

    // Generate Reverse amount, Misal Amount = 50, maka Reverse Amount = 32 00 00
    public String ItoH(String intval) {
        intval = intval.replace(" ", ""); // 1
        int iBalance = Integer.parseInt(intval); // 1
        String Hbal = "000000" + Integer.toString(iBalance, 16); // H 1
        Hbal = Hbal.substring(Hbal.length() - 6);
        String Hbal1 = Hbal.substring(0, 2);
        String Hbal2 = Hbal.substring(2, 6);
        Hbal2 = Hbal2.substring(2, 4) + Hbal2.substring(0, 2);
        return Hbal2 + Hbal1;
    }

    //-- Reverse amount (4B) to Integer , Misal 27 10 00 00 menjadi 10000
    public String HtoI(String hVal) {
        if (hVal.length() == 6)
            hVal = hVal + "00";
        //hVal1 = hVal.substring(4,8)+hVal.substring(0,4);
        String hVal1 = hVal.substring(4, 8);
        hVal1 = hVal1.substring(2, 4) + hVal1.substring(0, 2);
        String hVal2 = hVal.substring(0, 4);
        hVal2 = hVal2.substring(2, 4) + hVal2.substring(0, 2);
        String hv = hVal1 + hVal2;
        int ival = Integer.parseInt(hv, 16);
        return "" + ival;
    }

    public String Hex3(String data) {
        String newData = "";
        for (int i = 0; i < data.length(); i++) {
            newData += "3" + data.charAt(i);
        }
        return newData;
    }

    public String nominalTransaksi(String data) {
        String dt = "0000000000" + data;
        return dt.substring(dt.length() - 10);
    }
}
