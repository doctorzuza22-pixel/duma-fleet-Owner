package com.duma.fleet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private SharedPreferences db;
    private LinearLayout body, root;
    private String tab="Dashboard", selectedPlate="";
    private final String[] nav={"Dashboard","Vehicles","Drivers","Freight","Money"};
    private final String[] vehicleTabs={"Documents","Maintenance","Fuel","Inspections","Trips","Duma Freight"};
    private int ink=Color.rgb(25,35,48), muted=Color.rgb(95,106,119), blue=Color.rgb(27,73,112);
    private int red=Color.rgb(177,39,39), green=Color.rgb(31,119,75);
    @Override public void onCreate(Bundle b){super.onCreate(b);db=getSharedPreferences("duma_fleet_data",MODE_PRIVATE);render();}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private String stamp(){return new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date());}
    private String get(String k){return db.getString(k,"");}
    private int count(String k){String s=get(k);if(s.trim().isEmpty())return 0;return s.split("\\n").length;}
    private void save(String k,String value){String old=get(k);db.edit().putString(k,(old.isEmpty()?"":old+"\n")+stamp()+" • "+value).apply();Toast.makeText(this,"Saved on this device",Toast.LENGTH_SHORT).show();render();}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(12),dp(8),dp(12),dp(8));return t;}
    private Button btn(String s,int color,Runnable run){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));b.setOnClickListener(v->run.run());return b;}
    private void addCard(String s){TextView t=label(s,14,ink);t.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(4),0,dp(4));body.addView(t,p);}
    private void heading(String s){TextView t=label(s,20,ink);t.setTypeface(null,1);body.addView(t);}
    private void action(String s,int color,Runnable r){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));body.addView(btn(s,color,r),p);}
    private void render(){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(244,246,248));
        LinearLayout header=new LinearLayout(this);header.setOrientation(1);header.setPadding(dp(16),dp(13),dp(16),dp(13));header.setBackgroundColor(0xFF142536);
        TextView title=label("DUMA FLEET MANAGEMENT",19,Color.WHITE);title.setTypeface(null,1);title.setPadding(0,0,0,dp(2));header.addView(title);
        TextView sub=label("by Duma Holdings",12,0xFFE0E7EF);sub.setPadding(0,0,0,dp(2));header.addView(sub);
        TextView tag=label("Smart Fleet Management. Built for Africa. Ready for the World.",10,0xFFCFD8E3);tag.setPadding(0,0,0,0);header.addView(tag);root.addView(header);
        ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(12),dp(10),dp(12),dp(18));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout bar=new LinearLayout(this);bar.setBackgroundColor(Color.WHITE);bar.setGravity(Gravity.CENTER);for(String n:nav){boolean freightTab=n.equals("Freight");TextView t=label(freightTab?"🟢 FREIGHT":n,11,freightTab?green:(tab.equals(n)?blue:muted));t.setGravity(Gravity.CENTER);t.setTypeface(null,(freightTab||tab.equals(n))?1:0);if(freightTab)t.setBackgroundColor(0xFFE5F3EA);bar.addView(t,new LinearLayout.LayoutParams(0,dp(54),1));t.setOnClickListener(v->{tab=n;render();});}root.addView(bar);setContentView(root);
        switch(tab){case "Dashboard":dashboard();break;case "Vehicles":vehicles();break;case "Drivers":drivers();break;case "Freight":freightScreen();break;case "Money":money();break;}
    }
    private void dashboard(){
        heading("Dashboard");
        addCard("MONEY THIS MONTH\nTotal earned: R"+moneyTotal("Earned")+"\nDiesel cost: R"+moneyTotal("Diesel")+"\nMaintenance cost: R"+moneyTotal("Maintenance")+"\nProfit: R"+profit());
        TextView rescue=label("🚨 I AM BROKEN DOWN - TAP FOR DUMA RESCUE\nTyre, Battery, Engine, Fuel, Towing. We come to you.",15,Color.WHITE);rescue.setTypeface(null,1);rescue.setBackgroundColor(red);body.addView(rescue);rescue.setOnClickListener(v->rescueForm());
        TextView freight=label("2 Trucks Idle - Put Them On Duma Freight To Earn\n\nPUT TRUCKS ON DUMA FREIGHT",14,Color.WHITE);freight.setTypeface(null,1);freight.setBackgroundColor(green);body.addView(freight);freight.setOnClickListener(v->{tab="Vehicles";render();});
        heading("Fleet overview");
        String[][] rows={{"Vehicles Registered", "Vehicles"},{"Drivers Registered","Drivers"},{"Trips Recorded","Trips"},{"Fuel Entries","Fuel"},{"Maintenance Records","Maintenance"},{"Inspections Completed","Inspections"},{"Documents Registered","Documents"}};
        for(String[] r:rows)addCard(r[0]+": "+count(r[1]));
        addCard("Upcoming expiry dates\n"+(get("Documents").isEmpty()?"No documents registered yet.":get("Documents")));
        addCard("Duma Fleet works offline. Your records will sync when you get signal.");
        action("+ ADD VEHICLE",blue,()->{tab="Vehicles";render();vehicleForm();});
    }
    private String moneyTotal(String kind){double n=0;for(String line:get("Money").split("\\n")){if(line.contains("TYPE="+kind)){String v=field(line,"AMOUNT");try{n+=Double.parseDouble(v);}catch(Exception e){}}}return String.format(Locale.US,"%.2f",n);}
    private String profit(){double earned=Double.parseDouble(moneyTotal("Earned"));double spent=Double.parseDouble(moneyTotal("Diesel"))+Double.parseDouble(moneyTotal("Maintenance"))+Double.parseDouble(moneyTotal("Rescue"));return String.format(Locale.US,"%.2f",earned-spent);}
    private String field(String line,String key){String token=key+"=";int i=line.lastIndexOf(token);if(i<0)return "";int e=line.indexOf(" • ",i);return line.substring(i+token.length(),e<0?line.length():e).trim();}
    private void vehicles(){
        heading("Vehicles");action("+ ADD VEHICLE",blue,this::vehicleForm);
        String[] records=get("Vehicles").split("\\n");if(get("Vehicles").isEmpty())addCard("No vehicles registered yet.");
        for(int i=records.length-1;i>=0;i--){String line=records[i];if(line.trim().isEmpty())continue;String plate=field(line,"PLATE");TextView item=label("🚚 "+(plate.isEmpty()?line:plate)+"\n"+field(line,"TYPE")+"  •  "+field(line,"MAKE")+" "+field(line,"MODEL")+"\nStatus: "+(field(line,"STATUS").isEmpty()?"Active":field(line,"STATUS"))+"\nTAP TO OPEN VEHICLE",14,ink);item.setBackgroundColor(Color.WHITE);body.addView(item);item.setOnClickListener(v->{selectedPlate=plate;vehicleDetail();});}
    }
    private void vehicleForm(){
        String[] f={"Photo of vehicle (optional note)","Vehicle type: Bakkie / 4 Ton / 8 Ton / Jumbo / Superlink / Tipper / Trailer / Crane Truck / Refrigerated Truck / Car","Number plate","Make","Model","Year","Owner name","Owner phone","Colour"};
        form("Add vehicle",f,vals->{save("Vehicles","PLATE="+vals[2]+" • TYPE="+vals[1]+" • MAKE="+vals[3]+" • MODEL="+vals[4]+" • YEAR="+vals[5]+" • OWNER="+vals[6]+" • PHONE="+vals[7]+" • COLOUR="+vals[8]+" • STATUS=Active");});
    }
    private void vehicleDetail(){
        heading(selectedPlate.isEmpty()?"Vehicle details":selectedPlate);
        for(String x:vehicleTabs)action(x,blue,()->vehicleSub(x));
        action("REPORT BREAKDOWN",red,this::rescueForm);
        action("Change vehicle status",muted,()->choose("Vehicle status",new String[]{"Active","Idle","Broken Down","On Trip"},s->save("VehicleStatus",selectedPlate+" • STATUS="+s)));
    }
    private void vehicleSub(String x){
        heading(selectedPlate+" • "+x);
        switch(x){
            case "Documents":form("Add vehicle document",new String[]{"Document: Licence Disc / Roadworthy / Insurance","Reference / notes","Expiry date (YYYY-MM-DD)","Photo reference"},v->save("Documents","PLATE="+selectedPlate+" • TYPE="+v[0]+" • EXPIRY="+v[2]+" • "+v[1]));break;
            case "Maintenance":form("Add service record",new String[]{"Last service date","Next service date","Description","Cost (R)","Garage","Invoice photo reference"},v->save("Maintenance","PLATE="+selectedPlate+" • DATE="+v[0]+" • NEXT="+v[1]+" • "+v[2]+" • AMOUNT="+v[3]+" • GARAGE="+v[4]));break;
            case "Fuel":form("Add fuel entry",new String[]{"Date","Litres","Cost (R)","Location","Fuel slip photo reference"},v->save("Fuel","PLATE="+selectedPlate+" • DATE="+v[0]+" • LITRES="+v[1]+" • AMOUNT="+v[2]+" • LOCATION="+v[3]));break;
            case "Inspections":form("Vehicle inspection",new String[]{"Date","Driver","Condition","Notes / defects","Photo reference"},v->save("Inspections","PLATE="+selectedPlate+" • DATE="+v[0]+" • DRIVER="+v[1]+" • CONDITION="+v[2]+" • "+v[3]));break;
            case "Trips":showRecords("Trips","PLATE="+selectedPlate);break;
            case "Duma Freight":freightForm();break;
        }
    }
    private void drivers(){
        heading("Drivers");action("+ ADD DRIVER",blue,()->form("Add driver",new String[]{"Photo reference","Full name","ID number","Phone","Driver licence number","Licence code","Licence expiry date"},v->save("Drivers","NAME="+v[1]+" • ID="+v[2]+" • PHONE="+v[3]+" • LICENCE="+v[4]+" • CODE="+v[5]+" • EXPIRY="+v[6]+" • RATING=New")));
        showRecords("Drivers","");
    }
    private void trips(){
        heading("Trips");action("+ START NEW TRIP",blue,()->form("Start trip",new String[]{"Vehicle number plate","Driver name","From location / address","To location / address","Load description","Load weight","Start km reading","Dashboard photo reference","Date and time"},v->save("Trips","STATUS=Active • PLATE="+v[0]+" • DRIVER="+v[1]+" • FROM="+v[2]+" • TO="+v[3]+" • LOAD="+v[4]+" • WEIGHT="+v[5]+" • STARTKM="+v[6]+" • START="+v[8])));
        showRecords("Trips","");
        action("END A TRIP",green,()->form("End trip",new String[]{"Trip / vehicle plate","End km reading","Diesel used (litres)","Delivery photo reference","Customer signature reference","Status: Completed / Cancelled"},v->save("TripEnds","PLATE="+v[0]+" • ENDKM="+v[1]+" • DIESEL="+v[2]+" • DELIVERY="+v[3]+" • SIGNATURE="+v[4]+" • STATUS="+v[5])));
    }
    private void freightScreen(){
        heading("DUMA FREIGHT");
        TextView intro=label("FREIGHT IS WHERE YOUR FLEET EARNS. Find loads, post loads and manage your earnings from here.",14,ink);
        intro.setBackgroundColor(0xFFE5F3EA);body.addView(intro);
        addCard("MY FREIGHT EARNINGS\\nR"+moneyTotal("Freight"));
        action("AVAILABLE LOADS",green,()->{heading("Available Loads");showRecords("AvailableLoads","");});
        action("MY LOADS",blue,()->{heading("My Loads");showRecords("MyLoads","");});
        action("MY EARNINGS",blue,()->{heading("My Freight Earnings");addCard("Recorded freight earnings: R"+moneyTotal("Freight"));showRecords("Money","TYPE=Freight");});
        action("+ POST A LOAD",green,()->form("Post a load",new String[]{"Pickup location","Delivery location","Load description / goods","Weight (tons)","Truck type needed","Price offered (R)","Pickup date","Contact name and phone"},v->save("AvailableLoads","FROM="+v[0]+" • TO="+v[1]+" • LOAD="+v[2]+" • WEIGHT="+v[3]+" • TRUCK="+v[4]+" • PRICE="+v[5]+" • DATE="+v[6]+" • CONTACT="+v[7]+" • STATUS=Available")));
        action("FIND A TRUCK",green,()->form("Find a truck",new String[]{"Pickup location","Delivery location","Truck type","Load description","Weight (tons)","Required date","Your name and phone"},v->save("FindTruckRequests","FROM="+v[0]+" • TO="+v[1]+" • TRUCK="+v[2]+" • LOAD="+v[3]+" • WEIGHT="+v[4]+" • DATE="+v[5]+" • CONTACT="+v[6]+" • STATUS=Requested")));
        addCard("Marketplace note: entries are saved on this phone for now. Publishing loads to other users, live matching, chat, booking and payment settlement require the connected DUMA Freight backend.");
    }
    private void money(){
        heading("Money");addCard("Freight loads earned: R"+moneyTotal("Freight")+"\nRentals earned: R"+moneyTotal("Rental")+"\nDiesel cost: R"+moneyTotal("Diesel")+"\nMaintenance cost: R"+moneyTotal("Maintenance")+"\nRescue cost: R"+moneyTotal("Rescue")+"\nNet recorded profit: R"+profit());
        action("+ ADD MONEY RECORD",blue,()->choose("Transaction type",new String[]{"Earned","Freight","Rental","Diesel","Maintenance","Rescue"},type->form("Add money record",new String[]{"Vehicle plate","Amount (R)","Description / reference","Date"},v->save("Money","TYPE="+type+" • PLATE="+v[0]+" • AMOUNT="+v[1]+" • DESCRIPTION="+v[2]+" • DATE="+v[3]))));
        showRecords("Money","");
        action("CASH OUT TO BANK - via DUMA PAY",green,()->new AlertDialog.Builder(this).setTitle("DUMA PAY").setMessage("DUMA PAY cash-out integration will be enabled when the approved payment partner connection is configured.").setPositiveButton("OK",null).show());
    }
    private void freightForm(){
        heading("Duma Freight • "+selectedPlate);
        addCard("Publish availability for loads or rental. This saves a local listing draft; customer marketplace publishing needs the Duma Freight service connection.");
        form("Freight availability",new String[]{"Available for loads: ON / OFF","Available for rental: ON / OFF","Price per trip (R) or customer bidding","Home base","Routes / service areas","Rental price per day (R)","Rental price per week (R)","Rental price per month (R)","With driver / Without driver","Available from date","Available to date"},v->save("FreightListings","PLATE="+selectedPlate+" • LOADS="+v[0]+" • RENTAL="+v[1]+" • TRIPPRICE="+v[2]+" • BASE="+v[3]+" • ROUTES="+v[4]+" • DAY="+v[5]+" • WEEK="+v[6]+" • MONTH="+v[7]+" • DRIVER="+v[8]+" • FROM="+v[9]+" • TO="+v[10]));
        action("PUBLISH TO DUMA FREIGHT",green,()->new AlertDialog.Builder(this).setTitle("Listing saved").setMessage("Your availability draft is saved on this phone. Live customer publishing requires the Duma Freight backend connection.").setPositiveButton("OK",null).show());
        showRecords("FreightListings","PLATE="+selectedPlate);
    }
    private void rescueForm(){
        heading("DUMA RESCUE");
        form("Report breakdown",new String[]{"Vehicle number plate","GPS location or manual address","Problem: Tyre / Battery / Engine / Overheating / Brakes / Fuel / Don't Know","Photo or video reference","Describe the problem"},v->save("Rescue","PLATE="+v[0]+" • LOCATION="+v[1]+" • PROBLEM="+v[2]+" • MEDIA="+v[3]+" • DESCRIPTION="+v[4]+" • STATUS=Reported"));
        addCard("Mechanic dispatch, live tracking, wallet settlement and the R99 Rescue Plan require the connected Duma Rescue service and payment partner.");
    }
    private interface Saved{void done(String[] v);}
    private void form(String title,String[] fields,Saved saved){
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(6),dp(4),dp(6),dp(4));EditText[] inputs=new EditText[fields.length];
        for(int i=0;i<fields.length;i++){inputs[i]=new EditText(this);inputs[i].setHint(fields[i]);inputs[i].setTextSize(14);box.addView(inputs[i]);}
        ScrollView sc=new ScrollView(this);sc.addView(box);
        new AlertDialog.Builder(this).setTitle(title).setView(sc).setNegativeButton("Cancel",null).setPositiveButton("Save", (d,w)->{String[] vals=new String[inputs.length];boolean any=false;for(int i=0;i<inputs.length;i++){vals[i]=inputs[i].getText().toString().trim();if(!vals[i].isEmpty())any=true;}if(!any){Toast.makeText(this,"Enter at least one detail",Toast.LENGTH_SHORT).show();return;}saved.done(vals);}).show();
    }
    private interface Chosen{void pick(String s);}
    private void choose(String title,String[] choices,Chosen chosen){new AlertDialog.Builder(this).setTitle(title).setItems(choices,(d,w)->chosen.pick(choices[w])).show();}
    private void showRecords(String key,String filter){
        String data=get(key);if(data.isEmpty()){addCard("No "+key.toLowerCase(Locale.ROOT)+" recorded yet.");return;}String[] lines=data.split("\\n");for(int i=lines.length-1;i>=0;i--)if(lines[i].contains(filter))addCard(lines[i]);
    }
}