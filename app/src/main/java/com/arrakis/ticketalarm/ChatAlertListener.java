package com.arrakis.ticketalarm;
import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.Locale;
public class ChatAlertListener extends NotificationListenerService {
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  if(!"com.openai.chatgpt".equals(sbn.getPackageName()))return;
  Notification n=sbn.getNotification();if(n==null)return;
  Bundle e=n.extras;if(e==null)return;
  String raw=String.valueOf(e.getCharSequence(Notification.EXTRA_TITLE,""))+" "
   +String.valueOf(e.getCharSequence(Notification.EXTRA_TEXT,""))+" "
   +String.valueOf(e.getCharSequence(Notification.EXTRA_BIG_TEXT,""));
  String s=raw.toLowerCase(Locale.ROOT);
  MainActivity.prefs(this).edit().putString("last_chatgpt",raw.substring(0,Math.min(raw.length(),400))).apply();
  if(!s.contains("dune")||!s.contains("chennai")||!s.contains("imax"))return;
  if(s.contains("not bookable")||s.contains("not available")||s.contains("unverified")
   ||s.contains("not verified")||s.contains("not confirmed")||s.contains("booking window closed")
   ||s.contains("server")||s.contains("connectivity issue")||s.contains("still closed")
   ||s.contains("no tickets")||s.contains("cannot verify"))return;
  boolean positive=s.contains("tickets are bookable")||s.contains("bookings are open")||
   s.contains("tickets are now available")||s.contains("seat selection is working")||
   s.contains("confirmed bookable")||s.contains("verified bookable")||
   s.contains("ticket sales are open");
  if(!positive)return;
  for(int day:new int[]{18,19,20}){
   if(s.contains(day+" december")||s.contains(day+" dec")||s.contains("december "+day)||
     s.contains("dec "+day)||s.contains("2026-12-"+day)||s.contains("202612"+day)){
    MainActivity.ring(this,"Confirmed Chennai IMAX Dune booking — "+day+" December",day);
   }
  }
 }
}
