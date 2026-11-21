package com.arrakis.ticketalarm;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Switch;

public class MainActivity extends Activity {
 public static final String BOOK="https://in.bookmyshow.com/movies/chennai/dune-part-three/ET00491771";
 private static final String CHANNEL="dune_loud_alarm_2026";
 private static MediaPlayer player;
 private static Vibrator vibrator;
 private static AudioManager audio;
 private static int priorVolume=-1;
 private static final Handler H=new Handler(Looper.getMainLooper());
 private TextView results;
 public static SharedPreferences prefs(Context c){return c.getSharedPreferences("dune",MODE_PRIVATE);}
 @Override protected void onCreate(Bundle b){super.onCreate(b);
  setShowWhenLocked(true);setTurnScreenOn(true);
  TicketJob.schedule(this);
  render();
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);
 }
 @Override protected void onResume(){super.onResume();if(results!=null)showResults();}
 private TextView label(String content,int size,int color){
  TextView t=new TextView(this);t.setText(content);t.setTextSize(size);t.setTextColor(color);
  t.setPadding(0,12,0,10);return t;
 }
 private Button button(String text,LinearLayout parent,Runnable action){
  Button b=new Button(this);b.setText(text);b.setAllCaps(false);
  parent.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->action.run());return b;
 }
 private void render(){
  ScrollView sc=new ScrollView(this);sc.setBackgroundColor(Color.rgb(9,16,30));
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);
  root.setPadding(34,50,34,55);sc.addView(root);setContentView(sc);
  root.addView(label("ARRAKIS / ALERT SYSTEM",14,0xfff5be64));
  root.addView(label("Dune IMAX\nTicket Alarm",34,Color.WHITE));
  root.addView(label("CHENNAI  •  18 / 19 / 20 DECEMBER 2026",15,0xfff5be64));
  root.addView(label("DUNE: PVR PALAZZO / NEXUS VIJAYA\nDUNE: INOX LUXE / PHOENIX MARKETCITY",15,0xffbbccdd));
  root.addView(label("DUNE • HIGH PRIORITY: manually test loud alarm. Public listing checks for 18-20 Dec cannot prove ticket checkout is open. The app no longer reads other apps\u0027 notifications.\n\nAVENGERS • NORMAL NOTIFICATIONS: MS Infinity Vision 3D listings for 21-27 Dec. No siren. A listing is NOT confirmed booking.",14,0xffbbccdd));
  Switch monitoring=new Switch(this);monitoring.setText("15-minute public-listing checks");monitoring.setTextColor(-1);
  monitoring.setChecked(prefs(this).getBoolean("polling",true));
  monitoring.setOnCheckedChangeListener((b,on)->{prefs(this).edit().putBoolean("polling",on).apply();TicketJob.schedule(this);});
  root.addView(monitoring);
  root.addView(label("The loud Dune siren works with TEST LOUD ALARM, but automatic loud ringing is unavailable without a trusted booking push service.",12,0xfff5be64));
  Switch avengersWatch=new Switch(this);avengersWatch.setText("Avengers normal alerts • 21–27 Dec");avengersWatch.setTextColor(-1);
  avengersWatch.setChecked(prefs(this).getBoolean("avengers_enabled",true));
  avengersWatch.setOnCheckedChangeListener((b,on)->prefs(this).edit().putBoolean("avengers_enabled",on).apply());
  root.addView(avengersWatch);
  results=label("",14,Color.WHITE);root.addView(results);
  button("CHECK DUNE + AVENGERS NOW",root,()->{
   results.setText("Checking cinema pages...");
   new Thread(()->{TicketJob.check(this);runOnUiThread(this::showResults);},"manual-check").start();
  });
  button("TEST LOUD ALARM",root,()->ring(this,"Test Dune IMAX loud alarm",0));
  button("STOP ALARM",root,()->{stop(this);showResults();});
  button("OPEN BOOKMYSHOW",root,()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(BOOK))));
  button("OPEN PALAZZO ON DISTRICT",root,()->startActivity(new Intent(Intent.ACTION_VIEW,
   Uri.parse(TicketJob.CINEMA+"18"))));
  button("OPEN PHOENIX ON DISTRICT",root,()->startActivity(new Intent(Intent.ACTION_VIEW,
   Uri.parse(TicketJob.PHOENIX+"18"))));
  button("OPEN PHOENIX ON BOOKMYSHOW",root,()->startActivity(new Intent(Intent.ACTION_VIEW,
   Uri.parse("https://in.bookmyshow.com/cinemas/chennai/inox-luxe-phoenix-market-city-velachery/buytickets/INPR/20261218"))));
  button("OPEN AVENGERS • INFINITY VISION 3D",root,()->startActivity(new Intent(Intent.ACTION_VIEW,
   Uri.parse(AvengersWatch.bookingUrl(21)))));
  button("OPEN AVENGERS ON DISTRICT",root,()->startActivity(new Intent(Intent.ACTION_VIEW,
   Uri.parse(AvengersWatch.districtUrl(21)))));
  button("ALLOW FULL-SCREEN ALERTS",root,()->{
   if(Build.VERSION.SDK_INT>=34)startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
    Uri.parse("package:"+getPackageName())));
   else startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));
  });
  button("SET UNRESTRICTED BATTERY USE",root,()->startActivity(
   new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));
  root.addView(label("No notification-reading or accessibility access is requested. Android may delay background checks; listing pages may block requests. No real phone call is placed. TEST LOUD ALARM rings locally only.",12,0xffbbccdd));
 }
 private void showResults(){
  SharedPreferences p=prefs(this);
  results.setText("DUNE HIGH PRIORITY\nLast check: "+p.getString("last_check","not run")+"\n"+
   p.getString("check_result","No results yet")+
   "\nAlarm sent: "+(p.getBoolean("fired_18",false)?"18 ":"")+
   (p.getBoolean("fired_19",false)?"19 ":"")+
   (p.getBoolean("fired_20",false)?"20":"")+
   "\nDune seat checkout: not independently verified by this app"+
   "\n\nAVENGERS • NORMAL ALERTS (21–27 DEC)\nLast check: "+
   p.getString("avengers_last_check","not run")+"\n"+
   p.getString("avengers_result","No results yet"));
 }
 public static synchronized void ring(Context c,String details,int day){
  SharedPreferences p=prefs(c);
  if(day!=0){
   if(!p.getBoolean("armed",true)||p.getBoolean("fired_"+day,false))return;
   p.edit().putBoolean("fired_"+day,true).apply();
  }
  p.edit().putBoolean("ringing",true).putString("alert_details",details).apply();
  try{
   audio=(AudioManager)c.getSystemService(AUDIO_SERVICE);
   if(audio!=null){
    priorVolume=audio.getStreamVolume(AudioManager.STREAM_ALARM);
    audio.setStreamVolume(AudioManager.STREAM_ALARM,
     audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
   }
   if(player==null){
    Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    if(uri==null)uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    player=new MediaPlayer();
    player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
    player.setDataSource(c,uri);player.setLooping(true);player.prepare();player.start();
   }
  }catch(Exception ex){if(player!=null){player.release();player=null;}}
  try{
   vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
   if(vibrator!=null)vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,900,350,900,350},0));
  }catch(Exception ignored){}
  NotificationManager nm=(NotificationManager)c.getSystemService(NOTIFICATION_SERVICE);
  if(nm!=null){
   NotificationChannel ch=new NotificationChannel(CHANNEL,"Dune urgent ticket alarm",
    NotificationManager.IMPORTANCE_HIGH);
   ch.setSound(null,null);ch.enableVibration(true);nm.createNotificationChannel(ch);
   Intent open=new Intent(c,MainActivity.class);
   open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
   PendingIntent pi=PendingIntent.getActivity(c,21,open,
    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification note=new Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
    .setContentTitle("DUNE IMAX — BOOK NOW").setContentText(details)
    .setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX)
    .setContentIntent(pi).setFullScreenIntent(pi,true).setOngoing(true).build();
   try{nm.notify(202618,note);}catch(SecurityException ignored){}
  }
  H.postDelayed(()->stop(c),360000);
 }
 public static synchronized void stop(Context c){
  if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}
  if(vibrator!=null){vibrator.cancel();vibrator=null;}
  if(audio!=null&&priorVolume>=0){try{audio.setStreamVolume(AudioManager.STREAM_ALARM,priorVolume,0);}catch(Exception ignored){}}
  priorVolume=-1;audio=null;
  prefs(c).edit().putBoolean("ringing",false).apply();
  NotificationManager nm=(NotificationManager)c.getSystemService(NOTIFICATION_SERVICE);
  if(nm!=null)nm.cancel(202618);
 }
}
