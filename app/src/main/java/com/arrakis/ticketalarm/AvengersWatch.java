package com.arrakis.ticketalarm;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import java.util.*;
import java.util.regex.*;
import java.text.*;

public final class AvengersWatch{
 private AvengersWatch(){}
 private static final String DISTRICT="https://www.district.in/movies/avengers-doomsday-movie-tickets-in-chennai-MV176735?fromdate=2026-12-";
 private static final String MOVIE="https://in.bookmyshow.com/movies/chennai/avengers-doomsday-ms-infinity-vsn-3d/buytickets/ET00526306/202612";
 private static final String CHANNEL="avengers_verified_seats_normal_v2";
 private static final Pattern LINKS=Pattern.compile("(?:https?://in\\.bookmyshow\\.com)?/movies/chennai/seat-layout/ET00526306/([A-Z0-9]{4})/[0-9]{3,9}/202612(2[1-7])",Pattern.CASE_INSENSITIVE);
 private static final Pattern AVAILABLE=Pattern.compile("(?i)(?:\"(?:seatId|seatNo|seatNumber)\"\\s*:\\s*\"[A-Z]{1,3}[- ]?[0-9]{1,3}\".{0,180}?\"(?:status|seatStatus|availability)\"\\s*:\\s*\"(?:available|free|open)\"|\"(?:status|seatStatus|availability)\"\\s*:\\s*\"(?:available|free|open)\".{0,180}?\"(?:seatId|seatNo|seatNumber)\"\\s*:\\s*\"[A-Z]{1,3}[- ]?[0-9]{1,3}\"|data-seat-(?:id|number)=[\"'][A-Z]{1,3}[0-9]{1,3}[\"'].{0,140}?data-seat-status=[\"']available[\"']|\"(?:availableSeatCount|availableSeatsCount)\"\\s*:\\s*[1-9][0-9]*)");
 private static final Pattern BLOCKED=Pattern.compile("(?i)(?:booking window closed|no seats available|show is sold out|bookings for this cinema are not going through|connectivity issue with cinema)");
 public static String districtUrl(int day){return DISTRICT+day;}
 public static String bookingUrl(int day){return MOVIE+day;}
 public static boolean validSeatLink(String url){return url!=null&&LINKS.matcher(url).matches();}
 private static boolean verified(String html,int day){
  String content=html.replace("\\/","/").replace("&quot;","\"");
  String low=(Html.fromHtml(content,Html.FROM_HTML_MODE_COMPACT).toString()+" "+content).toLowerCase(Locale.ROOT);
  if(!low.contains("avengers: doomsday"))return false;
  if(!(low.contains("infinity vsn 3d")||low.contains("infinity vision 3d")))return false;
  if(!(low.contains(day+" december")||low.contains("202612"+day)))return false;
  // A count or generic legend might describe a different show in the same response.
  return !BLOCKED.matcher(low).find()&&AVAILABLE.matcher(content).find()
    && (low.contains("seat") || low.contains("book tickets"));
 }
 public static void check(Context c){
  SharedPreferences p=MainActivity.prefs(c);
  if(!p.getBoolean("avengers_enabled",true)){
   p.edit().putString("avengers_result","Avengers checks disabled").apply();return;
  }
  Calendar now=Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
  if(now.get(Calendar.YEAR)>2026||now.get(Calendar.YEAR)==2026&&
   (now.get(Calendar.MONTH)>Calendar.DECEMBER||
   now.get(Calendar.MONTH)==Calendar.DECEMBER&&now.get(Calendar.DAY_OF_MONTH)>27)){
   p.edit().putString("avengers_result","Watch ended").apply();return;
  }
  StringBuilder report=new StringBuilder();
  for(int day=21;day<=27;day++){
   if(report.length()>0)report.append("\n");
   report.append(day).append(" Dec: ");
   if(p.getBoolean("avengers_alerted_"+day,false)){report.append("verified notification already sent");continue;}
   LinkedHashSet<String> urls=new LinkedHashSet<>();
   String userLink=p.getString("manual_avengers_url","").trim();
   if(validSeatLink(userLink))urls.add(userLink);
   try{
    String html=TicketJob.fetch(bookingUrl(day)).replace("\\/","/").replace("&quot;","\"");
    Matcher m=LINKS.matcher(html);
    while(m.find()&&urls.size()<25){
     String s=m.group();urls.add(s.startsWith("http")?s:"https://in.bookmyshow.com"+s);
    }
   }catch(Exception ignored){}
   // District can show listings, but does not prove purchasability.
   if(urls.isEmpty()){report.append("No seat-layout link exposed; UNVERIFIED");continue;}
   String confirmed=null;
   for(String u:urls){
    Matcher m=LINKS.matcher(u);
    if(!m.matches()||Integer.parseInt(m.group(2))!=day)continue;
    try{if(verified(TicketJob.fetch(u),day)){confirmed=u;break;}}catch(Exception ignored){}
   }
   if(confirmed!=null){
    if(notifyRegular(c,day,confirmed)){
     p.edit().putBoolean("avengers_alerted_"+day,true).apply();
     report.append("Positive seat-level evidence; notification sent");
    }else report.append("Seat evidence seen; notification permission missing");
   }else report.append("Seat layout links found, seats UNVERIFIED");
  }
  SimpleDateFormat fmt=new SimpleDateFormat("d MMM, h:mm a",Locale.ENGLISH);
  fmt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
  p.edit().putString("avengers_result",report.toString()).putString("avengers_last_check",fmt.format(new Date())+" IST").apply();
 }
 private static boolean notifyRegular(Context c,int day,String url){
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(nm==null)return false;
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;
  if(!nm.areNotificationsEnabled())return false;
  NotificationChannel channel=new NotificationChannel(CHANNEL,"Avengers seat verification (normal)",NotificationManager.IMPORTANCE_DEFAULT);
  channel.enableVibration(false);nm.createNotificationChannel(channel);
  PendingIntent tap=PendingIntent.getActivity(c,1700+day,
    new Intent(Intent.ACTION_VIEW,Uri.parse(url)),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_today)
    .setContentTitle("Avengers Infinity Vision 3D • "+day+" Dec")
    .setContentText("Positive seat evidence detected. Tap to try booking.")
    .setStyle(new Notification.BigTextStyle().bigText(
     "Chennai MS Infinity Vision 3D: direct seat page contains explicit available-seat evidence for "+day+
     " December. Final checkout/payment not tested."))
    .setContentIntent(tap).setCategory(Notification.CATEGORY_RECOMMENDATION)
    .setAutoCancel(true).build();
  try{nm.notify(221200+day,n);return true;}catch(SecurityException e){return false;}
 }
}
