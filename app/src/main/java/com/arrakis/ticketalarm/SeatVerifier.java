package com.arrakis.ticketalarm;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import java.util.*;
import java.util.regex.*;
public final class SeatVerifier {
 private SeatVerifier(){}
 private static final String BASE="https://in.bookmyshow.com";
 private static final Pattern LINKS=Pattern.compile("(?:https?://in\\.bookmyshow\\.com)?/movies/chennai/seat-layout/ET00526290/(PVPZ|INPR)/([0-9]{3,9})/202612(18|19|20)",Pattern.CASE_INSENSITIVE);
 private static final Pattern SEAT=Pattern.compile("(?i)(?:\"(?:seatId|seatNo|seatNumber)\"\\s*:\\s*\"[A-Z]{1,3}[- ]?[0-9]{1,3}\".{0,180}?\"(?:status|availability|seatStatus)\"\\s*:\\s*\"(?:available|free|open)\"|\"(?:status|availability|seatStatus)\"\\s*:\\s*\"(?:available|free|open)\".{0,180}?\"(?:seatId|seatNo|seatNumber)\"\\s*:\\s*\"[A-Z]{1,3}[- ]?[0-9]{1,3}\"|data-seat-(?:id|number)=[\"'][A-Z]{1,3}[0-9]{1,3}[\"'].{0,140}?data-seat-status=[\"']available[\"'])");
 private static final Pattern COUNT=Pattern.compile("(?i)\"(?:availableSeatCount|availableSeatsCount|seatsAvailable)\"\\s*:\\s*[1-9][0-9]*");
 private static final Pattern BLOCKED=Pattern.compile("(?i)(?:bookings for this cinema are not going through|connectivity issue with cinema|booking window closed|no seats available|show is sold out|booking currently unavailable|session expired)");
 public static String moviePage(int day){return BASE+"/movies/chennai/dune-part-three-imax-2d/buytickets/ET00526290/202612"+day;}
 public static boolean validSeatLink(String url){return url!=null&&LINKS.matcher(url).matches();}
 public static boolean positiveSeatEvidence(String html,String code,int day){
  if(html==null)return false;
  String raw=html.replace("\\/","/").replace("&quot;","\"");
  String body=Html.fromHtml(raw,Html.FROM_HTML_MODE_COMPACT).toString();
  String low=(body+" "+raw).toLowerCase(Locale.ROOT);
  if(BLOCKED.matcher(low).find())return false;
  if(!low.contains("dune: part three")||!low.contains("imax"))return false;
  if("PVPZ".equalsIgnoreCase(code)&&!(low.contains("palazzo")||low.contains("nexus vijaya")))return false;
  if("INPR".equalsIgnoreCase(code)&&!(low.contains("phoenix")||low.contains("inox: luxe")))return false;
  if(!(low.contains(day+" december")||low.contains("202612"+day)||low.contains("2026-12-"+day)))return false;
  // A generic AVAILABLE legend, seat price or button is never sufficient.
  return SEAT.matcher(raw).find()||(COUNT.matcher(raw).find()&&low.contains("select seats"));
 }
 public static void check(Context c){
  SharedPreferences p=MainActivity.prefs(c);
  LinkedHashSet<String> urls=new LinkedHashSet<>();
  String manual=p.getString("manual_seat_url","");
  if(validSeatLink(manual))urls.add(manual);
  String[] sources={"https://ticketradar.in/f/dune-part-three",
   "https://in.bookmyshow.com/cinemas/chennai/pvr-palazzo-the-nexus-vijaya-mall/buytickets/PVPZ/20261218",
   "https://in.bookmyshow.com/cinemas/chennai/inox-luxe-phoenix-market-city-velachery/buytickets/INPR/20261218",
   moviePage(18),moviePage(19),moviePage(20)};
  for(String src:sources){
   try{
    String page=TicketJob.fetch(src).replace("\\/","/").replace("&quot;","\"");
    Matcher m=LINKS.matcher(page);
    while(m.find()&&urls.size()<40){
     String url=m.group();urls.add(url.startsWith("http")?url:BASE+url);
    }
   }catch(Exception ignored){}
  }
  StringBuilder report=new StringBuilder();
  for(int day:new int[]{18,19,20}){
   if(report.length()>0)report.append("\n");
   report.append(day).append(" Dec: ");
   if(p.getBoolean("fired_"+day,false)){report.append("Alarm already sent");continue;}
   boolean found=false,verified=false;
   for(String url:urls){
    Matcher m=LINKS.matcher(url);
    if(!m.matches()||Integer.parseInt(m.group(3))!=day)continue;
    found=true;
    try{
     if(positiveSeatEvidence(TicketJob.fetch(url),m.group(1),day)){
      String cinema=m.group(1).equalsIgnoreCase("PVPZ")?"Palazzo":"Phoenix";
      p.edit().putString("verified_dune_url",url)
       .putString("verified_dune_details",cinema+" "+day+" December").apply();
      MainActivity.ring(c,"Dune IMAX SELECTABLE SEATS: "+cinema+", "+day+" Dec. Book now!",day);
      report.append("Explicit seat data verified at "+cinema+" (alarm requested)");
      verified=true;break;
     }
    }catch(Exception ignored){}
   }
   if(!verified)report.append(found?"Seat links found; selectable seats NOT verified":"Seat-layout link not exposed — UNVERIFIED");
  }
  p.edit().putString("seat_verification",report.toString())
   .putInt("seat_discovered_links",urls.size()).apply();
 }
}
