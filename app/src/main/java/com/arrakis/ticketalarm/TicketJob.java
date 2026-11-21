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
  for(int day:new int[]{18,19,20}){
   if(result.length()>0)result.append("\n");
   result.append(day).append(" Dec: ");
   try{
    String html=fetch(CINEMA+day).toLowerCase(Locale.ROOT);
    int cutoff=html.indexOf("frequently asked questions");
    if(cutoff<0){result.append("Site response cannot be verified");continue;}
    String shows=html.substring(0,cutoff);
    int movie=shows.indexOf("dune: part three");
    if(movie<0){result.append("No Dune listing found");continue;}
    int to=Math.min(shows.length(),movie+2500);
    String movieBlock=shows.substring(movie,to);
    // Never let a different movie's IMAX format count.
    int next=movieBlock.indexOf("ua16+", 20);
    if(next>0)movieBlock=movieBlock.substring(0,next);
    if(!(movieBlock.contains("imax 2d")||movieBlock.contains("imax 3d"))){
     result.append("Dune appears, IMAX unconfirmed");
    } else if(movieBlock.contains("booking window closed")||movieBlock.contains("no tickets are available")){
     result.append("IMAX appears, booking closed");
    } else {
     result.append("IMAX listed; seat checkout UNVERIFIED");
    }
   }catch(Exception e){result.append("Site inaccessible / network error");}
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
