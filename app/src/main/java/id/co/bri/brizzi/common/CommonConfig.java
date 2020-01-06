/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.common;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import id.co.bri.brizzi.R;

/**
 * @author indra
 */
public class  CommonConfig {

//    public static final String HTTP_REST_URL = "http://103.14.45.67:8085/ARRest/screen";
//    public static final String HTTP_REST_URL = "http://192.168.1.10:8088";
//    public static final String KONFIRM_UPDATE_URL = "http://192.168.1.10:8088";
//    public static final String WEBSOCKET_URL = "ws://192.168.1.10:8088/push";
    public static final String HTTP_REST_URL = "172.18.44.114:8080";//"172.18.44.134:8080";
    public static final String KONFIRM_UPDATE_URL = "172.18.44.114:8080";//"172.18.44.134:8080";
    public static final String WEBSOCKET_URL = "172.18.44.114:8080";//"172.18.44.134:8080";
//    public static final String HTTP_POST = "http://103.14.45.67:8085/ARRest/";
//    public static final String HTTP_REST_URL = "http://10.107.11.206:8085/ARRest/screen";
    public static final String HTTP_POST = "http://10.107.11.206:8085/ARRest/";
    public static final String INIT_REST_ACT = "0000000";
    public static final String[] LIST_MENU_KEY = {"type", "title", "id", "ver", "comps"};
    public static final String[] LIST_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_lbl", "comp_act", "seq"};
    public static final String[] FORM_MENU_KEY = {"type", "title", "id", "print", "print_text", "ver", "comps"};
    public static final String[] FORM_MENU_COMP_KEY = {"visible", "comp_type", "comp_id", "comp_opt", "comp_act", "seq", "comp_values"};
    public static final String[] FORM_MENU_COMP_VALUES_KEY = {"comp_value", "value", "print"};
    public static final String VER_FILE = "menu_ver";
    public static final String SETTINGS_FILE = "settings";
    public static final int TIME_OUT = 60;//SECOND
    public static final String DB_FILE_UPDATE_NAME = "dbupdate.sql";
    public static final String BRANCH = "303514";
    public static final String USERNAME_ADMIN = "admin";
    public static final String PASS_ADMIN = "admin";
    public static final String PASS_SETTINGS = "1234";
    public static final String DEV_SOCKET_IP = "172.18.37.28"; //"10.35.65.209";//172.21.64.83//172.18.37.12
    public static final String DEV_SOCKET_PORT = "5707"; //"1402";//
    public static final String DEFAULT_DISCOUNT_TYPE = "Rupiah";
    public static final String DEFAULT_DISCOUNT_RATE = "0";
    public static final String EXPD = "2111";
    public static final String DEV_TERMINAL_ID = "26077634"; //"00000023"; //"00000004";
    public static final String DEV_MERCHANT_ID = "000001370076089"; //"000001210000020";
    public static final String INIT_MERCHANT_NAME = "DUMMY BRILINK 2 DBO";//"GTA DEVICE";
    public static final String INIT_MERCHANT_ADDRESS1 = "GTA TEST DEVICE";
    public static final String INIT_MERCHANT_ADDRESS2 = "GTI LANTAI 7";
    public static final String CVA = "12600000";
    public static final String DEFAULT_SETTLEMENT_PASS = "1234";
    public static final String DEFAULT_MIN_BALANCE_BRIZZI = "2500";
    public static final String DEFAULT_MAX_MONTHLY_DEDUCT = "20000000";
    public static final Boolean DEBUG_MODE = true;
    public static final String SQUEN = "8977";
    public static final int CAPTURE_PINBLOCK = 52;
    public static final int CALLBACK_KEYPRESSED = 53;
    public static final int CALLBACK_RESULT = 54;
    public static final int CAPTURE_CANCEL = 55;
    public static final int FLAG_INUSE = 56;
    public static final int FLAG_READY = 57;
    public static final String VTB = "22300000";
    public static final int UPDATE_FLAG_RECEIVER = 58;
    public static final int MODE_CALCULATE = 59;
    public static final int CALLBACK_CANCEL = 60;
    public static final int CALLBACK_CANCEL_DONE = 61;
    public static final String ONE_BIN = "522184";


    public static Object[] getOpt(String compOpt) {
        Object[] result = new Object[5];
        Boolean bool = compOpt.substring(1, 2).equals("1");
//        Log.d("COMP", bool.toString());
        result[CompOption.MANDATORY] = compOpt.substring(0, 1).equals("1");
        result[CompOption.DISABLED] = compOpt.substring(1, 2).equals("1");
        result[CompOption.TYPE] = Integer.parseInt(compOpt.substring(2, 3));
        result[CompOption.MIN_LENGTH] = Integer.parseInt(compOpt.substring(3, 6));
        result[CompOption.MAX_LENGTH] = Integer.parseInt(compOpt.substring(6, 9));
        return result;
    }

    public static Map<String, Integer> ICONS() {
        Map<String, Integer> icons = new HashMap<>();
        icons.put("Mini ATM", R.drawable.info_atm);
        icons.put("Informasi", R.drawable.ic_info);
        //ICON MENU REGISTRASI
        icons.put("Registrasi", R.drawable.ic_registrasi);
//        icons.put("Registrasi",R.drawable.ic_s);
        //END ICON MENU REGISTRASI
        //ICON MENU TRANSFER
        icons.put("Transfer", R.drawable.ic_transfer);
        icons.put("Transfer2", R.drawable.mb_transfersesama);
        icons.put("Transfer Antar Bank", R.drawable.mb_transferbanklain);
        icons.put("Info Kode Bank", R.drawable.info_petunjuk);
        //END ICON MENU TRANSFER
        //ICON MENU PEMBAYARAN
        icons.put("Payment", R.drawable.mb_pembayaran);
        icons.put("Pembayaran", R.drawable.mb_pembayaran);
        icons.put("Pembayaran Telkom", R.drawable.mb_pembayaran_telkom);
        icons.put("Pascabayar", R.drawable.mb_info_telsel);
        icons.put("PLN", R.drawable.mb_pembayaran_pln);
        icons.put("Kartu Kredit/KTA", R.drawable.mb_pembayaran_cc_kta);
        icons.put("Cicilan Motor", R.drawable.mb_pembayaran_cicilan);
        icons.put("Zakat", R.drawable.mb_pembayaran_zakat);
        icons.put("Infaq", R.drawable.mb_pembayaran_infaq);
        icons.put("DPLK", R.drawable.mb_pembayaran_dplk);
        icons.put("Tiket", R.drawable.mb_pembayaran_tiket);
        icons.put("Briva", R.drawable.mb_pembayaran_briva);
        icons.put("Pendidikan", R.drawable.mb_pembayaran_pendidikan);
        //END ICON MENU PEMBAYARAN
        //ICON MENU ISI ULANG
        icons.put("Isi ulang", R.drawable.mb_isi_ulang_pulsa);
        //END ICON MENU ISI ULANG
        //ICON MENU ISI ULANG
        icons.put("Informasi Saldo", R.drawable.mb_info_saldo);
        icons.put("Informasi Saldo Bank Lain", R.drawable.mb_info_saldo);
        //END ICON MENU ISI ULANG

        icons.put("Ubah Pin MATM", R.drawable.mb_pelayanan_nasabah_ganti_pin);
        icons.put("Reprint", R.drawable.ic_reprint);
        icons.put("MPN", R.drawable.ic_mpn);
        icons.put("PPH", R.drawable.ic_pph);
        icons.put("Setor Pasti", R.drawable.ic_setor);
        icons.put("Report", R.drawable.ic_report);
        icons.put("Logon", R.drawable.ic_network);
        icons.put("Absen", R.drawable.ic_admin);
        icons.put("T-Bank", R.drawable.ic_tbank);
        icons.put("BRIZZI", R.drawable.ic_brizzi);
        icons.put("Tunai", R.drawable.mb_info_pinjaman);
        icons.put("Settings", R.drawable.mb_info_mutasi);
        //ICON BRIZZI
        icons.put("Info Saldo", R.drawable.mb_info_saldo);
        icons.put("Info Tertunda", R.drawable.mb_info_saldo);
        //END ICON BRIZZI
        return icons;
    }

    public static int getIcon(String name) {
        return ICONS().containsKey(name) ? ICONS().get(name) : -1;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Get Imei Device
     *
     * @param ctx
     * @return
     */
    public static String getImei(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * Get current location of device
     *
     * @param ctx Android Context
     * @return Array double of long and lat. Index 0 is longtitude and index 1 is latitude
     */
    public static double[] getLocation(Context ctx) {
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double[] d = new double[2];
        d[0] = location.getLongitude();
        d[1] = location.getLatitude();
        return d;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static class ComponentType {
        //Component Type
        public static final int ListMenuItem = 0;
        public static final int TextView = 1;
        public static final int EditText = 2;
        public static final int PasswordField = 3;
        public static final int ComboBox = 4;
        public static final int CheckBox = 5;
        public static final int RadioButton = 6;
        public static final int Button = 7;
        public static final int MagneticSwipe = 8;
        public static final int ChipInsert = 9;
        public static final int TapCard = 10;
        public static final int SwipeInsert = 11;
        public static final int SwipeTap = 12;
        public static final int InsertTap = 13;
        public static final int SwipeInsertTap = 14;
    }

    public static class MenuType {
        //Menu Type
        public static final int ListMenu = 0;
        public static final int Form = 1;
        public static final int PopupBerhasil = 2;
        public static final int PopupGagal = 3;
        public static final int PopupLogout = 4;
        public static final int SecuredForm = 5;
    }

    public static class CompOption {
        public static final int MANDATORY = 0;
        public static final int DISABLED = 1;
        public static final int TYPE = 2;
        public static final int MIN_LENGTH = 3;
        public static final int MAX_LENGTH = 4;
    }

    public static class TextType {
        public static final int ALPHA_NUMERIC = 0;
        public static final int ALPHA = 1;
        public static final int NUMERIC = 2;
        public static final int NO_CONSTRAINT = 3;
    }
}
