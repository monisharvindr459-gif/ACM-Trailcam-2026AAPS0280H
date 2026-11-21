package com.arrakis.ticketalarm;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class AvengersWatch {
 private AvengersWatch(){}
 private static final String DISTRICT =
  "https://www.district.in/movies/avengers-doomsday-movie-tickets-in-chennai-MV176735?fromdate=2026-12-";
 private static final String BOOKMYSHOW =
  "https://in.bookmyshow.com/movies/chennai/avengers-doomsday-ms-infinity-vsn-3d/buytickets/ET00526306/202612";
 private static final String CHANNEL = "avengers_regular_listing_alerts_v1";
 private static final Pattern SHOW = Pattern.compile(
  "\\b(?:0?[1-9]|1[0-2]):[0-5][0-9]\\s*(?:AM|PM)[\\s\\S]{0,180}?MS\\s*[- ]*INFINITY\\s*VISION\\s*3D\\b",
  Pattern.CASE_INSENSITIVE);
 public static String districtUrl(int day){return DISTRICT+day;}
 public static String bookingUrl(int day){return BOOKMYSHOW+day;}
 public static void check(Context context) {
  SharedPreferences p=MainActivity.prefs(context);
  if(!p.getBoolean("avengers_enabled",true)) {
   p.edit().putString("avengers_result","Avengers watch switched off").apply();
   return;
  }
  Calendar clock=Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
  if(clock.get(Calendar.YEAR)>2026||
     (clock.get(Calendar.YEAR)==2026&&clock.get(Calendar.MONTH)>Calendar.DECEMBER)||
     (clock.get(Calendar.YEAR)==2026&&clock.get(Calendar.MONTH)==Calendar.DECEMBER&&clock.get(Calendar.DAY_OF_MONTH)>27)){
   p.edit().putString("avengers_result","Avengers watch finished on 27 Dec 2026").apply();
   return;
  }
  StringBuilder summary=new StringBuilder();
  for(int day=21;day<=27;day++){
   if(summary.length()>0)summary.append("\n");
   summary.append(day).append(" Dec: ");
   if(p.getBoolean("avengers_alerted_"+day,false)){
    summary.append("listing notification already sent");
    continue;
   }
   try{
    String html=TicketJob.fetch(districtUrl(day));
    String visible=Html.fromHtml(html,Html.FROM_HTML_MODE_COMPACT).toString();
    String normalized=visible.toLowerCase(Locale.ROOT);
    int start=normalized.indexOf("almost full");
    int end=normalized.indexOf("frequently asked questions");
    // Do not count format tags that appear in filter menus or SEO descriptions.
    if(start<0||end<=start||!normalized.contains("avengers: doomsday")){
     summary.append("No verified date-specific listing data");
     continue;
    }
    String listings=visible.substring(start+("almost full").length(),end);
    Matcher match=SHOW.matcher(listings);
    boolean found=false;
    String firstTime="";
    while(match.find()){
     String nearby=listings.substring(match.end(),Math.min(listings.length(),match.end()+75))
        .toLowerCase(Locale.ROOT);
     if(nearby.contains("no tickets are available")||
        nearby.contains("booking window closed")||
        nearby.contains("not available"))continue;
     firstTime=match.group();
     int pos=firstTime.indexOf(' ');
     if(pos>0)firstTime=firstTime.substring(0,Math.min(firstTime.length(),pos+4)).trim();
     found=true;break;
    }
    if(found){
     boolean sent=notifyRegular(context,day);
     if(sent){
      p.edit().putBoolean("avengers_alerted_"+day,true).apply();
      summary.append("NEW Infinity Vision 3D listing — notification sent");
     }else summary.append("New listing, but Android notifications are disabled");
    }else summary.append("No available-format listing found (or checkout closed)");
   }catch(Exception e){
    summary.append("Listing inaccessible; will retry");
   }
  }
  SimpleDateFormat dt=new SimpleDateFormat("d MMM, h:mm a",Locale.ENGLISH);
  dt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
  p.edit().putString("avengers_result",summary.toString())
    .putString("avengers_last_check",dt.format(new Date())+" IST").apply();
 }
 private static boolean notifyRegular(Context c,int day){
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(nm==null)return false;
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   return false;
  if(!nm.areNotificationsEnabled())return false;
  NotificationChannel channel=new NotificationChannel(CHANNEL,
   "Avengers low-priority show listings", NotificationManager.IMPORTANCE_DEFAULT);
  channel.enableVibration(false);
  nm.createNotificationChannel(channel);
  Intent open=new Intent(Intent.ACTION_VIEW,Uri.parse(bookingUrl(day)));
  open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  PendingIntent pending=PendingIntent.getActivity(c,1200+day,open,
    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification note=new Notification.Builder(c,CHANNEL)
    .setSmallIcon(android.R.drawable.ic_menu_today)
    .setContentTitle("Avengers: Doomsday • "+day+" December")
    .setContentText("Chennai Infinity Vision 3D show listed. Tap to check seats.")
    .setStyle(new Notification.BigTextStyle().bigText(
      "MS Infinity Vision 3D appears on a Chennai cinema listing for "+day+
      " December. Actual seat selection and payment are NOT verified. Check BookMyShow."))
    .setCategory(Notification.CATEGORY_RECOMMENDATION)
    .setPriority(Notification.PRIORITY_DEFAULT)
    .setContentIntent(pending).setAutoCancel(true).build();
  try{nm.notify(221200+day,note);return true;}
  catch(SecurityException e){return false;}
 }
}
