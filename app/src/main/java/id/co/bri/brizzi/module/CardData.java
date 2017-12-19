package id.co.bri.brizzi.module;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import id.co.bri.brizzi.common.StringLib;

/**
 * Created by indra on 13/01/16.
 */
public class CardData {
    private String uid;
    //private int invokeResult;
    private String ReverseAmount3B = "000000";
    private String CardNumber = "";
    private String RandomNumber8B = "";
    private String cardBalance4B = "";
    private String hostResponse = "";
    private String keyCard8B = "";
    private String deductAmount = "";
    private String tDate = "";
    private String tTime = "";
    private String whatToDo = "";
    private String akumDebet = "";
    private String cardBalanceInt = "";
    private String hash4B = "";
    private String topupAmount = "";
    private String MerchanID = "";
    private String TerminalID = "";
    private String lastTransDate = "";
    private String Hash4BTopup = "";
    private String newBalance = "";
    private String track2Data = "";
    private boolean cardStatus = false;
    private String pin = "";
    private long brizziIdLog = -1;
    private String msgSI = "";
    private String saldoDeposit = "";
    private String lamaPasif = "";
    private String statusAfter = "";
    private String validationStatus = "";
    private String hashVoid = "";
    private String redCardBalance = "";
    private String redDepoBalance = "";
    private String redFee = "";
    private String redTotal = "";
    private final SimpleDateFormat DATE_TOCARD = new SimpleDateFormat("yyMMdd");
    private String serverRef = "";
    private String stanvoid = "";
    private String randomSam24B = "";

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {

        this.uid = uid;
    }

    public String getRedTotal() {
        return redTotal;
    }

    public void setRedTotal(String redTotal) {
        this.redTotal = redTotal;
    }

    public String getRedFee() {
        return redFee;
    }

    public void setRedFee(String redFee) {
        this.redFee = redFee;
    }

    public String getRedCardBalance() {
        return redCardBalance;
    }

    public void setRedCardBalance(String redCardBalance) {
        this.redCardBalance = redCardBalance;
    }

    public String getRedDepoBalance() {
        return redDepoBalance;
    }

    public void setRedDepoBalance(String redDepoBalance) {
        this.redDepoBalance = redDepoBalance;
    }

    public String getReverseAmount3B() {
        return ReverseAmount3B;
    }

    public void setReverseAmount3B(String reverseAmount3B) {
        ReverseAmount3B = reverseAmount3B;
    }

    public String getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(String cardNumber) {
        CardNumber = cardNumber;
    }

    public String getRandomNumber8B() {
        return RandomNumber8B;
    }

    public void setRandomNumber8B(String randomNumber8B) {
        RandomNumber8B = randomNumber8B;
    }

    public String getCardBalance4B() {
        return cardBalance4B;
    }

    public void setCardBalance4B(String cardBalance4B) {
        this.cardBalance4B = cardBalance4B;
    }

    public String getHostResponse() {
        return hostResponse;
    }

    public void setHostResponse(String hostResponse) {
        this.hostResponse = hostResponse;
    }

    public String getKeyCard8B() {
        return keyCard8B;
    }

    public void setKeyCard8B(String keyCard8B) {
        this.keyCard8B = keyCard8B;
    }

    public String getDeductAmount() {
        return deductAmount;
    }

    public void setDeductAmount(String deductAmount) {
        this.deductAmount = deductAmount;
    }

    public String getHashVoid() {
        return hashVoid;
    }

    public void setHashVoid(String hashVoid) {
        this.hashVoid = hashVoid;
    }

    public String gettDate() {
        return tDate;
    }

    public String getYYMMDD() {
        String yymmdd = tDate;
        if (tDate.length()==6) {
            String yy = tDate.substring(4,6);
            String mm = tDate.substring(2,4);
            String dd = tDate.substring(0,2);
            yymmdd = yy+mm+dd;
        }
        return yymmdd;
    }

    public void settDate(String tDate) {
//        this.tDate = tDate.substring(4)+tDate.substring(2,4)+tDate.substring(0,2);
        this.tDate = tDate;
    }

    public void setDateFromIso(String isoDate) {
        isoDate += "000000";
        isoDate = isoDate.substring(0,6);
        String dd = isoDate.substring(2,4);
        String mm = isoDate.substring(0,2);
        String yy = isoDate.substring(4,6);
        String localDate = StringLib.getDDMMYY();
        if (dd.equals("00")) {
            dd = localDate.substring(0,2);
            mm = localDate.substring(2,4);
            yy = localDate.substring(4,6);
        }
        if (mm.equals("00")) {
            mm = localDate.substring(2,4);
            yy = localDate.substring(4,6);
        }
        if (yy.equals("00")) {
            yy = localDate.substring(4,6);
        }
        String tDate = dd + mm + yy;
        this.tDate = tDate;
    }

    public String gettTime() {
        return tTime;
    }

    public void settTime(String tTime) {
        this.tTime = tTime;
    }

    public void setTimeFromIso(String isoTime) {
        isoTime += "000000";
        isoTime = isoTime.substring(0,6);
        String hh = isoTime.substring(0,2);
        String mi = isoTime.substring(2,4);
        String ss = isoTime.substring(4,6);
        String localTime = StringLib.getStringTime();
        if (hh.equals("00")) {
            hh = localTime.substring(0,2);
            mi = localTime.substring(2,4);
            ss = localTime.substring(4,6);
        }
        if (mi.equals("00")) {
            mi = localTime.substring(2,4);
            ss = localTime.substring(4,6);
        }
        if (ss.equals("00")) {
            ss = localTime.substring(4,6);
        }
        String tTime = hh + mi + ss;
        this.tTime = tTime;
    }

    public String getWhatToDo() {
        return whatToDo;
    }

    public void setWhatToDo(String whatToDo) {
        this.whatToDo = whatToDo;
    }

    public String getAkumDebet() {
        return akumDebet;
    }

    public void setAkumDebet(String akumDebet) {
//        Log.d("AKUM", akumDebet);
        this.akumDebet = String.valueOf(Integer.parseInt(akumDebet, 16));
    }

    public String getCardBalanceInt() {
        return cardBalanceInt;
    }

    public void setCardBalanceInt(String cardBalanceInt) {
        this.cardBalanceInt = cardBalanceInt;
    }

    public String getHash4B() {
        return hash4B;
    }

    public void setHash4B(String hash4B) {
        this.hash4B = hash4B;
    }

    public String getTopupAmount() {
        return topupAmount;
    }

    public void setTopupAmount(String topupAmount) {
        this.topupAmount = topupAmount;
    }


    public String getLastTransDate() {
        return lastTransDate;
    }

    public void setLastTransDate(String lastTransDate) {
        this.lastTransDate = lastTransDate;
    }

    public String getHash4BTopup() {
        return Hash4BTopup;
    }

    public void setHash4BTopup(String hash4BTopup) {
        Hash4BTopup = hash4BTopup;
    }

    public String getTerminalID() {
        return TerminalID;
    }

    public String getTerminalIdForCardLog() {
        String tidForLog = StringLib.fillZero(StringLib.Hex3(StringLib.fillZero(TerminalID,8)),16);
        return tidForLog;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }

    public String getMerchanID() {
        return MerchanID;
    }

    public String getMerchanIdForCardLog() {
        String midForLog = StringLib.fillZero(MerchanID,8);
        midForLog = StringLib.fillZero(StringLib.Hex3(midForLog),16);
        return midForLog;
    }

    public void setMerchanID(String merchanID) {
        MerchanID = merchanID;
    }

    public String getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(String newBalance) {
        this.newBalance = newBalance;
    }

    public void setTrack2Data(String track2Data){
        this.track2Data = track2Data;
    }

    public String getTrack2Data(){
        return this.track2Data;
    }

    public void setPin(String pin){
        this.pin = pin;
    }

    public String getPin(){
        return this.pin = pin;
    }

    public long getBrizziIdLog() {
        return brizziIdLog;
    }

    public void setBrizziIdLog(long brizziIdLog) {
        this.brizziIdLog = brizziIdLog;
    }

    public String getMsgSI() {
        return msgSI;
    }

    public void setMsgSI(String msgSI) {
        this.msgSI = msgSI;
    }

    public boolean isCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(boolean cardStatus) {
        this.cardStatus = cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus.equals("6161");
    }

    public void setSaldoDeposit(String saldo) {
        this.saldoDeposit = saldo;
    }

    public String getSaldoDeposit() {
        return saldoDeposit;
    }

    public void setLamaPasif(String lamaPasif) {
        this.lamaPasif = lamaPasif;
    }

    public String getLamaPasif() {
        return lamaPasif;
    }

    public void setStatusAfter(String statusAfter) {
        this.statusAfter = statusAfter;
    }

    public String getStatusAfter() {
        return statusAfter;
    }

    public Calendar getLastTxDate() {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(DATE_TOCARD.parse(lastTransDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setServerRef(String serverRef) {
        this.serverRef = serverRef;
    }

    public String getServerRef() {
        return serverRef;
    }

    public void setStanVoid(String stanvoid) {
        this.stanvoid = stanvoid;
    }

    public String getstanvoid() {
        return stanvoid;
    }

    public void setRandomSam24B(String randomSam24B) {
        this.randomSam24B = randomSam24B;
    }

    public String getRandomSam24B() {
        return randomSam24B;
    }
}