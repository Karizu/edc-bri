package id.co.bri.brizzi.layout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import id.co.bri.brizzi.ActivityList;
import id.co.bri.brizzi.AdminActivity;
import id.co.bri.brizzi.MainActivity;
import id.co.bri.brizzi.R;
import id.co.bri.brizzi.SuperVisorActivity;
import id.co.bri.brizzi.common.CommonConfig;
import id.co.bri.brizzi.customview.HelveticaTextView;
import id.co.bri.brizzi.handler.JsonCompHandler;

/**
 * Created by indra on 25/11/15.
 */
public class ListMenu extends LinearLayout implements ListView.OnItemClickListener {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Activity context;
    private String id;
    //    private ListView listView;
    private GridView listView;
//    private ViewPager pager;
//    private GridPagerAdapter gpa;
//    private LinearLayout lp;
    private SharedPreferences preferences;
    private boolean isBusy;
    private Handler doubleClickHandler;

    public ListMenu(Activity context, String id) {
        super(context);
        this.context = context;
        preferences  = context.getSharedPreferences(CommonConfig.SETTINGS_FILE, Context.MODE_PRIVATE);
        String opt = "";
        if (id.length()>7) {
            opt = id.substring(7);
            id = id.substring(0,7);
        }
        this.id = id;
        final Activity fCon = context;
        LayoutInflater li = LayoutInflater.from(context);
        LinearLayout ll = (LinearLayout) li.inflate(R.layout.grid_menu, this);
//        listView = (ListView) ll.findViewById(R.id.listView);
        listView = (GridView) ll.findViewById(R.id.listView);
//        lp = (LinearLayout) li.inflate(R.layout.view_pager, this);
//        pager = (ViewPager) lp.findViewById(R.id.menu_pager);
//        gpa = new GridPagerAdapter();
//        pager.setAdapter(gpa);
        try {
            JSONObject obj = JsonCompHandler.readJson(context, id);
            JSONArray comps = obj.getJSONObject("comps").getJSONArray("comp");
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int i = 0; i < comps.length(); i++) {
                String visb = ((JSONObject) comps.get(i)).getString("visible");
                if (visb.equals("t")||visb.equals("true")) {
                    jsonObjects.add((JSONObject) comps.get(i));
                }
            }
            //inject admin page
            if (!opt.equals("")) {
                if (opt.contains("ms=on")) {
                    JSONObject adm = new JSONObject("            {\n" +
                            "              \"seq\":\"100\",\n" +
                            "              \"comp_type\":\"0\",\n" +
                            "              \"comp_id\":\"ADM00\",\n" +
                            "              \"visible\":\"true\",\n" +
                            "              \"comp_lbl\":\"Settings\",\n" +
                            "              \"comp_act\":\"0A0D0M\"\n" +
                            "            }\n");
                    jsonObjects.add(adm);
                }
            }
            listView.setAdapter(new ListAdapter(context, R.layout.grid_item_layout, jsonObjects));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDrawableBuilder = TextDrawable.builder().round();
        listView.setOnItemClickListener(this);
        isBusy = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long ids) {
        if (!isBusy) {
            isBusy = true;
            JSONObject obj = (JSONObject) parent.getItemAtPosition(position);

            String act = null;
            try {
                act = obj.getString("comp_act");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Integer type = -1;
            try {
                type = obj.getInt("comp_type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
//        Log.d("ClickedItem", String.valueOf(type));
            if (type != -1 && !id.equals("")) {
                switch (type) {
                    case CommonConfig.MenuType.Form:
//                    Log.d("FORM","FORM CUY");

                        break;
                    case CommonConfig.MenuType.ListMenu:
                        if (act.equals("0A0D0M")) {
//                        Log.d("Setting", "Pressed");
                            final Dialog dialog = new Dialog(context);

                            dialog.setContentView(R.layout.dialog_login);
                            dialog.setTitle("Login");

                            // get the Refferences of views
                            final EditText editTextUserName = (EditText) dialog.findViewById(R.id.editTextUserNameToLogin);
                            final EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextPasswordToLogin);
                            android.widget.Button btnSignIn = (android.widget.Button) dialog.findViewById(R.id.buttonSignIn);

                            // Set On ClickListener
                            btnSignIn.setOnClickListener(new View.OnClickListener() {

                                public void onClick(View v) {
                                    // TODO Auto-generated method stub

                                    // get The User name and Password
                                    String userName = editTextUserName.getText().toString();
                                    String password = editTextPassword.getText().toString();
                                    String stored = preferences.getString("pass_settlement", CommonConfig.DEFAULT_SETTLEMENT_PASS);

                                    // fetch the Password form database for respective user name


                                    // check if the Stored password matches with  Password entered by user
                                    if (password.equals(CommonConfig.PASS_ADMIN) && userName.equals(CommonConfig.USERNAME_ADMIN)) {
                                        Toast.makeText(context, "Login Successfull", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        Intent i = new Intent(context, AdminActivity.class);
                                        context.startActivity(i);
                                    } else if (password.equals(stored) && userName.equals(stored)) {
                                        Toast.makeText(context, "Login Successfull", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        Intent a = new Intent(context, SuperVisorActivity.class);
                                        context.startActivityForResult(a, MainActivity.RESULT_CLOSE_SETTING);
                                    } else {

                                        Toast.makeText(context, "User Name and Does Not Matches", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });


                            dialog.show();

                        } else {
                            Intent intent = new Intent(context, ActivityList.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("comp_act", act);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                        break;
                    case CommonConfig.MenuType.SecuredForm:
//                    Log.d("Secured Form", "Pressed");
                        final Dialog dialog = new Dialog(context);

                        dialog.setContentView(R.layout.dialog_login);
                        dialog.setTitle("Input Password");

                        // get the Refferences of views
                        final EditText editTextUserName = (EditText) dialog.findViewById(R.id.editTextUserNameToLogin);
                        final EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextPasswordToLogin);
                        editTextUserName.setVisibility(GONE);
                        editTextPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                        final String realAct = act;
                        android.widget.Button btnSignIn = (android.widget.Button) dialog.findViewById(R.id.buttonSignIn);
                        btnSignIn.setText("OK");

                        // Set On ClickListener
                        btnSignIn.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                // TODO Auto-generated method stub

                                // get The User name and Password
                                String password = editTextPassword.getText().toString();
                                String stored = preferences.getString("pass_settlement", CommonConfig.DEFAULT_SETTLEMENT_PASS);

                                // fetch the Password form database for respective user name


                                // check if the Stored password matches with  Password entered by user
                                if (password.equals(stored)) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(context, ActivityList.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("comp_act", realAct);
                                    intent.putExtras(bundle);
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }

                            }
                        });


                        dialog.show();

                        break;
                    case CommonConfig.MenuType.PopupBerhasil:
                        break;
                    case CommonConfig.MenuType.PopupGagal:
                        break;
                    case CommonConfig.MenuType.PopupLogout:
                        break;
                }
            }
            doubleClickHandler = new Handler();
            doubleClickHandler.postDelayed(releaseBusy, 3000);
        }
    }

    private static class ViewHolder {

        private View view;
        private ImageView imageView;
        private HelveticaTextView textView;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (HelveticaTextView) view.findViewById(R.id.textView);
        }
    }

//    public LinearLayout getPager() {
//        return lp;
//    }
//
    public class ListAdapter extends ArrayAdapter<JSONObject> {

        public ListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public ListAdapter(Context context, int resource, List<JSONObject> jsonObjects) {
            super(context, resource, jsonObjects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = vi.inflate(R.layout.grid_item_layout, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            JSONObject obj = getItem(position);
            if (obj != null) {
                String lbl = "";
                try {
                    lbl = obj.get("comp_lbl").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int icons = CommonConfig.getIcon(lbl);
                Drawable drawable = mDrawableBuilder.build(String.valueOf(lbl.charAt(0)), mColorGenerator.getColor(lbl));
                if (icons != -1) {
                    drawable = getResources().getDrawable(icons);
                }
                holder.imageView.setImageDrawable(drawable);
                holder.view.setBackgroundColor(Color.TRANSPARENT);
                holder.textView.setText(lbl);
            }
            return convertView;
        }
    }

    public class GridPagerAdapter extends PagerAdapter {
        private ArrayList<View> views = new ArrayList<View>();
        private boolean doNotify = false;

        @Override
        public int getCount() {
            if (doNotify) {
                doNotify = false;
                notifyDataSetChanged();
            }
            return views.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return views.indexOf(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        public int addView(View v) {
            return addView(v, views.size());
        }

        public int addView(View v, int position) {
            doNotify = true;
            views.add(position, v);
            return position;
        }

        public int removeView(ViewPager pager, int position) {
            doNotify = true;
            pager.setAdapter(null);
            views.remove(position);
            pager.setAdapter(this);
            return position;
        }

        public int removeView(ViewPager pager, View v) {
            return removeView(pager, views.indexOf(v));
        }
    }

    Runnable releaseBusy = new Runnable() {
        @Override
        public void run() {
            try {
                isBusy = false;
            } catch (Exception e) {

            }
        }
    };
}
