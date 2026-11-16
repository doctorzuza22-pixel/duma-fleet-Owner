package com.duma.fleet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout content;
    private SharedPreferences prefs;
    private String section = "Dashboard";
    private static final int GPS_PERMISSION = 41;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("duma_fleet_data", MODE_PRIVATE);
        render();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(dp(12), dp(9), dp(12), dp(9)); return t;
    }
    private Button button(String value, Runnable action) {
        Button b = new Button(this); b.setText(value); b.setAllCaps(false);
        b.setOnClickListener(v -> action.run()); return b;
    }
    private void render() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F5F7);
        TextView header = text("DUMA FLEET", 23, 0xFFFFFFFF); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF111827); header.setPadding(dp(18), dp(18), dp(18), dp(18));
        root.addView(header);
        TextView sub = text("Fleet management • Africa to the world", 12, 0xFF64748B);
        root.addView(sub);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(4), dp(12), dp(18)); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout nav = new LinearLayout(this); nav.setBackgroundColor(0xFFFFFFFF);
        String[] tabs = {"Dashboard","Vehicles","Drivers","Trips","Fuel","Maintenance"};
        for (String tab : tabs) {
            Button b = new Button(this); b.setText(tab); b.setTextSize(10); b.setAllCaps(false);
            b.setPadding(dp(2),dp(8),dp(2),dp(8)); b.setOnClickListener(v -> { section=tab; render(); });
            nav.addView(b, new LinearLayout.LayoutParams(0, dp(54), 1));
        }
        root.addView(nav); setContentView(root);
        if (section.equals("Dashboard")) dashboard(); else listSection(section);
    }
    private void heading(String s) { content.addView(text(s,20,0xFF111827)); }
    private void card(String s) {
        TextView t=text(s,15,0xFF1F2937); t.setBackgroundColor(0xFFFFFFFF);
        t.setPadding(dp(14),dp(14),dp(14),dp(14));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5)); content.addView(t,p);
    }
    private String records(String key) { return prefs.getString(key, ""); }
    private int count(String key) { String s=records(key); return s.isEmpty()?0:s.split("\\n").length; }
    private void dashboard() {
        heading("Your fleet at a glance");
        card("Vehicles registered     " + count("Vehicles"));
        card("Drivers registered       " + count("Drivers"));
        card("Trip records             " + count("Trips"));
        card("Fuel entries             " + count("Fuel"));
        card("Maintenance records      " + count("Maintenance"));
        card("Records are stored on this phone. Internet is not required for basic record keeping.");
        content.addView(button("＋ Add a vehicle", () -> { section="Vehicles"; render(); showForm("Vehicles"); }));
        content.addView(button("＋ Add a driver", () -> { section="Drivers"; render(); showForm("Drivers"); }));
        content.addView(button("＋ Record a trip", () -> { section="Trips"; render(); showForm("Trips"); }));
    }
    private void listSection(String key) {
        heading(key);
        content.addView(button("＋ Add " + singular(key), () -> showForm(key)));
        if (key.equals("Trips")) content.addView(button("Capture current GPS location", this::captureGps));
        String data=records(key);
        if (data.isEmpty()) card("No " + key.toLowerCase(Locale.ROOT) + " recorded yet.");
        else {
            String[] lines=data.split("\\n");
            for(int i=lines.length-1;i>=0;i--) card(lines[i]);
        }
    }
    private String singular(String key) {
        switch(key) { case "Vehicles": return "vehicle"; case "Drivers": return "driver"; case "Trips": return "trip"; case "Fuel": return "fuel entry"; default: return "maintenance record"; }
    }
    private void showForm(String key) {
        LinearLayout form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(8),dp(4),dp(8),dp(4));
        String[] labels;
        switch(key) {
            case "Vehicles": labels=new String[]{"Registration number","Make and model","Year","Vehicle notes"}; break;
            case "Drivers": labels=new String[]{"Driver full name","Phone number","Licence number","Licence expiry (YYYY-MM-DD)"}; break;
            case "Trips": labels=new String[]{"Vehicle registration","Driver name","Start location","Destination","Distance (km)"}; break;
            case "Fuel": labels=new String[]{"Vehicle registration","Date (YYYY-MM-DD)","Litres","Total cost (R)","Odometer (km)"}; break;
            default: labels=new String[]{"Vehicle registration","Service / repair description","Date (YYYY-MM-DD)","Cost (R)","Next service due (YYYY-MM-DD)"}; break;
        }
        EditText[] fields=new EditText[labels.length];
        for(int i=0;i<labels.length;i++) {
            fields[i]=new EditText(this); fields[i].setSingleLine(true); fields[i].setHint(labels[i]); form.addView(fields[i]);
        }
        new AlertDialog.Builder(this).setTitle("Add " + singular(key)).setView(form)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                StringBuilder row=new StringBuilder();
                for(int i=0;i<fields.length;i++){ if(i>0)row.append(" • "); row.append(labels[i]).append(": ").append(fields[i].getText().toString().trim()); }
                if(row.toString().replaceAll("[^:]*: ?","").trim().isEmpty()) { Toast.makeText(this,"Enter details first",Toast.LENGTH_SHORT).show(); return; }
                String stamp=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date());
                String old=records(key); prefs.edit().putString(key,(old.isEmpty()?"":old+"\n")+stamp+" • "+row).apply();
                Toast.makeText(this,"Saved on this device",Toast.LENGTH_SHORT).show(); render();
            }).show();
    }
    private void captureGps() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
           checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},GPS_PERMISSION); return;
        }
        try {
            LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc==null) loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(loc==null) { Toast.makeText(this,"No recent location yet. Turn on location and try again.",Toast.LENGTH_LONG).show(); return; }
            String value=String.format(Locale.US,"GPS: %.6f, %.6f (accuracy %.0f m)",loc.getLatitude(),loc.getLongitude(),loc.getAccuracy());
            String old=records("Trips"); prefs.edit().putString("Trips",(old.isEmpty()?"":old+"\n")+new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date())+" • "+value).apply();
            Toast.makeText(this,"Location saved to trip log",Toast.LENGTH_LONG).show(); render();
        } catch(SecurityException e) { Toast.makeText(this,"Location permission is required",Toast.LENGTH_SHORT).show(); }
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==GPS_PERMISSION && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) captureGps();
    }
}
