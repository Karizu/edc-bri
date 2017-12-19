/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package id.co.bri.brizzi.handler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.IOException;

/**
 *
 * @author Ahmad
 */
public class Track2BINChecker {
    private Cursor c;
    private Context ctx;
    private DataBaseHelper helperDb;
    private SQLiteDatabase clientDB = null;
    private boolean briCard = false;

    public Track2BINChecker(Context ctx, String track2) {
        this.ctx = ctx;
        try {
            helperDb = new DataBaseHelper(ctx);
            clientDB = helperDb.getActiveDatabase();
            String qry = "select * from binbri";
            c = clientDB.rawQuery(qry, null);
            String binnumber = "";
            if (c.moveToFirst()) {
                do {
                    binnumber = c.getString(c.getColumnIndex("binnumber"));
                    if (track2.startsWith(binnumber)) {
                        briCard = true;
                    }
                } while (c.moveToNext()&&!briCard);
            }
        } catch (Exception ex) {
            //
        }
    }
    
    public boolean isExternalCard() {
        return !briCard;
    }

}
