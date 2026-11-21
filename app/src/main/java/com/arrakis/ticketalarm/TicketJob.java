package com.arrakis.ticketalarm;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TicketJob extends JobService {
 public static final int ID=18820;
 public static final String CINEMA="https://www.district.in/movies/pvr-palazzo-the-nexus-vijaya-mall-chennai-CD1022274?fromdate=2026-12-";
 public static final String PHOENIX="https://www.district.in/movies/inox-phoenix-market-city-formerly-jazz-cinemas-velachery-chennai-CD1020779?fromdate=2026-12-";
 public static void schedule(Context c) {
  JobScheduler js=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  if(js==null)return;
  if(!MainActivity.prefs(c).getBoolean("polling",true)){js.cancel(ID);return;}
  JobInfo info=new JobInfo.Builder(ID,new ComponentName(c,TicketJob.class))
   .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(900000L).build();
  js.schedule(info);
 }
 @Override public boolean onStartJob(JobParameters params){
  new Thread(()->{try{check(this);}finally{jobFinished(params,false);}},"dune-check").start();return true;
 }
 @Override public boolean onStopJob(JobParameters params){return true;}
 public static void check(Context context) {
  SharedPreferences p=MainActivity.prefs(context);
  StringBuilder result=new StringBuilder();
  String[][] cinemas={{"PVR Palazzo (Nexus Vijaya)",CINEMA},{"INOX LUXE (Phoenix Marketcity)",PHOENIX}};
  for(int day:new int[]{18,19,20}){
   for(String[] cinema:cinemas){
    if(result.length()>0)result.append("\n");
    result.append(cinema[0]).append(" | ").append(day).append(" Dec: ");
    try{
     String html=fetch(cinema[1]+day).toLowerCase(Locale.ROOT);
     int cutoff=html.indexOf("frequently asked questions");
     if(cutoff<0){result.append("Listing response unverified");continue;}
     String shows=html.substring(0,cutoff);
     if(shows.contains("no shows playing at the moment")){
      result.append("No shows posted");continue;
     }
     int movie=shows.indexOf("dune: part three");
     if(movie<0){result.append("No Dune show listed");continue;}
     // The published cinema page is NOT a seat-selection or checkout session.
     // IMAX wording within a nearby movie listing may itself be ambiguous.
     String around=shows.substring(movie,Math.min(shows.length(),movie+2000));
     if(around.contains("booking window closed") || around.contains("connectivity issue with cinema")
          || around.contains("no tickets are available")){
      result.append("Dune listed; booking closed / unavailable");
     }else if(around.contains("imax 2d") || around.contains("imax 3d")){
      result.append("Dune and IMAX appear; live checkout UNVERIFIED");
     }else{
      result.append("Dune mentioned; IMAX / checkout UNVERIFIED");
     }
    }catch(Exception e){
     result.append("Source unavailable (network / access)");
    }
   }
  }
  SimpleDateFormat fmt=new SimpleDateFormat("d MMM, h:mm a",Locale.ENGLISH);
  fmt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
  p.edit().putString("last_check",fmt.format(new Date())+" IST")
   .putString("check_result",result.toString()).apply();
 }
 private static String fetch(String target)throws Exception{
  HttpURLConnection conn=(HttpURLConnection)new URL(target).openConnection();
  conn.setConnectTimeout(12000);conn.setReadTimeout(12000);
  conn.setRequestProperty("Accept","text/html,application/xhtml+xml");
  conn.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 Chrome/129.0 Mobile Safari/537.36");
  try{
   if(conn.getResponseCode()!=200)throw new IllegalStateException("HTTP "+conn.getResponseCode());
   BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
   StringBuilder body=new StringBuilder();char[] b=new char[4096];int n;
   while((n=reader.read(b))!=-1&&body.length()<500000)body.append(b,0,n);
   reader.close();return body.toString();
  }finally{conn.disconnect();}
 }
}
