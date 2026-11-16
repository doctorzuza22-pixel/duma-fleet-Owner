package com.duma.fleet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private LinearLayout content;
    private SharedPreferences prefs;
    private String section = "Dashboard";
    private static final int GPS_PERMISSION = 41;
    private final String[] modules = {"Dashboard","Vehicles","Drivers","Trips","Fuel","Maintenance","Inspections","Documents","Reports"};

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
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF111827); header.setPadding(dp(14), dp(10), dp(14), dp(10));
        ImageView logo = new ImageView(this); logo.setImageResource(R.drawable.duma_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, new LinearLayout.LayoutParams(dp(54), dp(54)));
        LinearLayout brand = new LinearLayout(this); brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(10),0,0,0);
        TextView name=text("DUMA FLEET",20,0xFFFFFFFF); name.setPadding(0,0,0,dp(2));
        TextView tagline=text("Smart Fleet Management. Built for Africa. Ready for the World.",10,0xFFE5E7EB); tagline.setPadding(0,0,0,0);
        brand.addView(name); brand.addView(tagline); header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(header);
        ScrollView scroll=new ScrollView(this); content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(12),dp(12),dp(12),dp(18));
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView navScroll=new HorizontalScrollView(this); navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav=new LinearLayout(this); nav.setBackgroundColor(0xFFFFFFFF);
        for(String tab:modules) {
            Button b=new Button(this); b.setText(tab); b.setTextSize(10); b.setAllCaps(false);
            b.setPadding(dp(5),dp(7),dp(5),dp(7));
            b.setOnClickListener(v->{section=tab;render();});
            nav.addView(b,new LinearLayout.LayoutParams(-2,dp(52)));
        }
        navScroll.addView(nav); root.addView(navScroll); setContentView(root);
        if(section.equals("Dashboard")) dashboard(); else if(section.equals("Reports")) reports(); else listSection(section);
    }
    private void heading(String s) { content.addView(text(s,20,0xFF111827)); }
    private void card(String s) {
        TextView t=text(s,14,0xFF1F2937); t.setBackgroundColor(0xFFFFFFFF);
        t.setPadding(dp(14),dp(13),dp(14),dp(13));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5)); content.addView(t,p);
    }
    private String records(String key) { return prefs.getString(key, ""); }
    private int count(String key) { String s=records(key); return s.isEmpty()?0:s.split("\\n").length; }
    private String stamp() { return new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date()); }
    private void save(String key,String row) {
        String old=records(key); prefs.edit().putString(key,(old.isEmpty()?"":old+"\n")+stamp()+" • "+row).apply();
        Toast.makeText(this,"Saved on this device",Toast.LENGTH_SHORT).show(); render();
    }
    private void dashboard() {
        heading("Fleet overview");
        card("Vehicles registered     "+count("Vehicles"));
        card("Drivers registered       "+count("Drivers"));
        card("Trips recorded           "+count("Trips"));
        card("Fuel entries             "+count("Fuel"));
        card("Maintenance records      "+count("Maintenance"));
        card("Inspections completed    "+count("Inspections"));
        card("Documents registered    "+count("Documents"));
        int alerts=expiryAlerts();
        card((alerts==0?"No upcoming expiry dates found in saved records.":alerts+" expiry / service item(s) need attention. Review the alerts below."));
        showAlerts();
        card("OFFLINE MODE • Your records are saved on this phone. Online accounts and cloud sync are not connected in this version.");
        content.addView(button("＋ Add vehicle",()->{section="Vehicles";render();showForm("Vehicles");}));
        content.addView(button("＋ Add driver",()->{section="Drivers";render();showForm("Drivers");}));
        content.addView(button("＋ Record trip",()->{section="Trips";render();showForm("Trips");}));
    }
    private void showAlerts() {
        String[] keys={"Drivers","Maintenance","Documents"};
        String[] dateLabels={"Licence expiry (YYYY-MM-DD)","Next service due (YYYY-MM-DD)","Expiry date (YYYY-MM-DD)"};
        for(int k=0;k<keys.length;k++) {
            String data=records(keys[k]); if(data.isEmpty()) continue;
            for(String line:data.split("\\n")) {
                int at=line.indexOf(dateLabels[k]); if(at<0) continue;
                int start=at+dateLabels[k].length(); while(start<line.length() && (line.charAt(start)==':'||line.charAt(start)==' ')) start++;
                if(start+10<=line.length()) {
                    String date=line.substring(start,start+10);
                    long days=daysUntil(date);
                    if(days>=0 && days<=30) card("⚠ "+keys[k]+": "+days+" day(s) until "+date+" • "+line.substring(0,Math.min(48,line.length())));
                    else if(days<0 && days>-3650) card("OVERDUE • "+keys[k]+": "+date);
                }
            }
        }
    }
    private int expiryAlerts() {
        int total=0; String[] keys={"Drivers","Maintenance","Documents"};
        String[] labels={"Licence expiry (YYYY-MM-DD)","Next service due (YYYY-MM-DD)","Expiry date (YYYY-MM-DD)"};
        for(int i=0;i<keys.length;i++) for(String line:records(keys[i]).split("\\n")) {
            int at=line.indexOf(labels[i]); if(at>=0) { int s=at+labels[i].length(); while(s<line.length()&&(line.charAt(s)==':'||line.charAt(s)==' '))s++; if(s+10<=line.length()&&daysUntil(line.substring(s,s+10))<=30) total++; }
        }
        return total;
    }
    private long daysUntil(String value) {
        try {
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US); f.setLenient(false);
            Date d=f.parse(value); Calendar a=Calendar.getInstance(); a.set(Calendar.HOUR_OF_DAY,0);a.set(Calendar.MINUTE,0);a.set(Calendar.SECOND,0);a.set(Calendar.MILLISECOND,0);
            return TimeUnit.MILLISECONDS.toDays(d.getTime()-a.getTimeInMillis());
        } catch(Exception e) { return Long.MAX_VALUE; }
    }
    private void listSection(String key) {
        heading(key);
        if(key.equals("Trips")) content.addView(button("Capture current GPS location",this::captureGps));
        content.addView(button("＋ Add "+singular(key),()->showForm(key)));
        String data=records(key);
        if(data.isEmpty()) card("No "+key.toLowerCase(Locale.ROOT)+" recorded yet.");
        else { String[] lines=data.split("\\n"); for(int i=lines.length-1;i>=0;i--) card(lines[i]); }
    }
    private String singular(String key) {
        switch(key) {
            case "Vehicles":return "vehicle"; case "Drivers":return "driver"; case "Trips":return "trip";
            case "Fuel":return "fuel entry"; case "Maintenance":return "maintenance record";
            case "Inspections":return "inspection"; default:return "document";
        }
    }
    private String[] fieldsFor(String key) {
        switch(key) {
            case "Vehicles":return new String[]{"Registration number","Make and model","Year","Vehicle type","Odometer (km)","Vehicle notes"};
            case "Drivers":return new String[]{"Driver full name","Phone number","Licence number","Licence expiry (YYYY-MM-DD)"};
            case "Trips":return new String[]{"Vehicle registration","Driver name","Start location","Destination","Distance (km)","Trip date (YYYY-MM-DD)"};
            case "Fuel":return new String[]{"Vehicle registration","Date (YYYY-MM-DD)","Litres","Total cost (R)","Odometer (km)","Fuel station"};
            case "Maintenance":return new String[]{"Vehicle registration","Service / repair description","Date (YYYY-MM-DD)","Cost (R)","Next service due (YYYY-MM-DD)","Odometer (km)"};
            case "Inspections":return new String[]{"Vehicle registration","Driver name","Inspection date (YYYY-MM-DD)","Tyres (OK / Issue)","Lights (OK / Issue)","Brakes (OK / Issue)","Body / glass (OK / Issue)","Defects / notes"};
            default:return new String[]{"Vehicle registration","Document type","Document reference","Issue date (YYYY-MM-DD)","Expiry date (YYYY-MM-DD)","Notes"};
        }
    }
    private void showForm(String key) {
        LinearLayout form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(8),dp(4),dp(8),dp(4));
        String[] labels=fieldsFor(key); EditText[] inputs=new EditText[labels.length];
        for(int i=0;i<labels.length;i++) { inputs[i]=new EditText(this); inputs[i].setSingleLine(true); inputs[i].setHint(labels[i]); form.addView(inputs[i]); }
        ScrollView wrapper=new ScrollView(this); wrapper.addView(form);
        new AlertDialog.Builder(this).setTitle("Add "+singular(key)).setView(wrapper).setNegativeButton("Cancel",null)
            .setPositiveButton("Save",(d,w)->{
                StringBuilder row=new StringBuilder(); boolean any=false;
                for(int i=0;i<inputs.length;i++) { String v=inputs[i].getText().toString().trim(); if(!v.isEmpty())any=true; if(i>0)row.append(" • "); row.append(labels[i]).append(": ").append(v); }
                if(!any) { Toast.makeText(this,"Enter at least one detail",Toast.LENGTH_SHORT).show(); return; }
                save(key,row.toString());
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
            if(loc==null)loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(loc==null){Toast.makeText(this,"No recent location. Turn on phone location and try again.",Toast.LENGTH_LONG).show();return;}
            String v=String.format(Locale.US,"GPS: %.6f, %.6f (accuracy %.0f m)",loc.getLatitude(),loc.getLongitude(),loc.getAccuracy());
            save("Trips",v);
        } catch(SecurityException e){Toast.makeText(this,"Location permission is required",Toast.LENGTH_SHORT).show();}
    }
    private void reports() {
        heading("Fleet reports & backups");
        card("Export saved fleet records as a CSV spreadsheet or a PDF summary. Files are saved to your Downloads folder.");
        content.addView(button("Export all records as CSV",this::exportCsv));
        content.addView(button("Export fleet summary as PDF",this::exportPdf));
        content.addView(button("View storage summary",()->{
            String s="Vehicles: "+count("Vehicles")+"\nDrivers: "+count("Drivers")+"\nTrips: "+count("Trips")+"\nFuel: "+count("Fuel")+"\nMaintenance: "+count("Maintenance")+"\nInspections: "+count("Inspections")+"\nDocuments: "+count("Documents");
            new AlertDialog.Builder(this).setTitle("Local records").setMessage(s).setPositiveButton("OK",null).show();
        }));
        card("Keep exported files somewhere safe. These exports are not cloud backups.");
    }
    private String csvEscape(String s){return "\""+s.replace("\"","\"\"")+"\"";}
    private void exportCsv() {
        StringBuilder csv=new StringBuilder("Module,Record\n");
        for(String key:modules) if(!key.equals("Dashboard")&&!key.equals("Reports"))
            for(String line:records(key).split("\\n")) if(!line.isEmpty()) csv.append(csvEscape(key)).append(",").append(csvEscape(line)).append("\n");
        writeDownload("DUMA-Fleet-Records.csv","text/csv",csv.toString());
    }
    private void exportPdf() {
        PdfDocument pdf=new PdfDocument(); Paint p=new Paint(); p.setTextSize(12);
        int pageNo=1,y=50; PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo).create()); Canvas c=page.getCanvas();
        p.setTextSize(20); c.drawText("DUMA FLEET",40,y,p); y+=28; p.setTextSize(10);
        c.drawText("Smart Fleet Management. Built for Africa. Ready for the World.",40,y,p); y+=25;
        c.drawText("Generated: "+stamp(),40,y,p); y+=30; p.setTextSize(12);
        for(String key:modules) if(!key.equals("Dashboard")&&!key.equals("Reports")) {
            c.drawText(key+" ("+count(key)+")",40,y,p); y+=18;
            for(String line:records(key).split("\\n")) if(!line.isEmpty()) {
                String shortLine=line.length()>82?line.substring(0,82)+"...":line;
                if(y>800){pdf.finishPage(page);pageNo++;page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo).create());c=page.getCanvas();y=50;}
                c.drawText(shortLine,52,y,p); y+=16;
            }
            y+=8;
        }
        pdf.finishPage(page);
        try {
            ContentValues v=new ContentValues(); v.put(MediaStore.Downloads.DISPLAY_NAME,"DUMA-Fleet-Summary.pdf");v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");v.put(MediaStore.Downloads.IS_PENDING,1);
            Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
            if(uri==null)throw new Exception("Could not create file");
            OutputStream out=getContentResolver().openOutputStream(uri); pdf.writeTo(out); out.close();v.clear();v.put(MediaStore.Downloads.IS_PENDING,0);getContentResolver().update(uri,v,null,null);
            Toast.makeText(this,"PDF saved in Downloads",Toast.LENGTH_LONG).show();
        } catch(Exception e){Toast.makeText(this,"PDF export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        finally{pdf.close();}
    }
    private void writeDownload(String name,String mime,String data) {
        try {
            ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,name);v.put(MediaStore.Downloads.MIME_TYPE,mime);v.put(MediaStore.Downloads.IS_PENDING,1);
            Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Could not create file");
            OutputStream out=getContentResolver().openOutputStream(uri);out.write(data.getBytes("UTF-8"));out.close();
            v.clear();v.put(MediaStore.Downloads.IS_PENDING,0);getContentResolver().update(uri,v,null,null);
            Toast.makeText(this,"CSV saved in Downloads",Toast.LENGTH_LONG).show();
        } catch(Exception e){Toast.makeText(this,"CSV export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==GPS_PERMISSION&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)captureGps();
    }
}
