/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package id.co.bri.brizzi.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import id.co.bri.brizzi.common.CommonConfig;

/**
 *
 * @author Ahmad
 */
public class Discount {
    private Context ctx;
    private SharedPreferences preferences;
    
    public void Discount() {
    }
    
    public void setContext(Context ctx) {
        this.ctx = ctx;
    }
    
    public String[] getDiscount(String amount) {
        String[] after = new String[3];
        preferences  = ctx.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String diskonId = preferences.getString("diskon_id","Rupiah");
        String diskon = preferences.getString("diskon","0");
        double amt = 0;
        double disc = 0;
        try {
            amt = Double.valueOf(amount);
            disc = Double.valueOf(diskon);
        } catch (Exception e) {
            after[0] = amount;
            after[1] = "N/A";
            after[2] = amount;
            return after;
        }
        double discAmo = 0;
        String diskName = "";
        if (diskonId.equals("Rupiah")) {
            discAmo = amt - disc;
            diskName = "Rp " + diskon;
        } else {
            discAmo = amt - (amt * (disc/100));
            diskName = diskon + "%";
        }
        after[0] = amount;
        after[1] = diskName;
        after[2] = String.valueOf((int) discAmo);
//        Log.d("DC","Discount : [0] " + after[0]);
//        Log.d("DC","Discount : [1] " + after[1]);
//        Log.d("DC","Discount : [2] " + after[2]);
        return after;
    }

}
