package id.co.bri.brizzi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import id.co.bri.brizzi.handler.DataBaseHelper;


public class SuperVisorActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_visor);
        Button btnClearR = (Button) findViewById(R.id.btnClearR);
        btnClearR.setOnClickListener(this);
        Button btnbackR = (Button) findViewById(R.id.btnbackR);
        btnbackR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v){
        DataBaseHelper helperDb;
        SQLiteDatabase clientDB = null;

        try {
            helperDb = new DataBaseHelper(this);
            clientDB = helperDb.getActiveDatabase();
        } catch (Exception ex) {
            //
        }

        String clrReversal = " update reversal_stack set revstatus = 'C' where revstatus = 'P';";
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_clearreversal);
        dialog.setTitle("Clear Reversal");

        TextView message = (TextView) dialog.findViewById(R.id.statusreversal);
        Button ok = (Button) dialog.findViewById(R.id.okbutton);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        clientDB.beginTransaction();
        String status = "";
        try {
            clientDB.execSQL(clrReversal);
            clientDB.setTransactionSuccessful();
            clientDB.endTransaction();
            Log.d("UQL", "Clear Reversal OK");
            status = "Clear Reversal OK";
        } catch (Exception e) {
            clientDB.endTransaction();
            status = "Clear Reversal Gagal";
            Log.d("UQL", "Clear Reversal not OK");
        } finally {
            clientDB.close();
            message.setText(status);
            dialog.show();
        }

    }


}
