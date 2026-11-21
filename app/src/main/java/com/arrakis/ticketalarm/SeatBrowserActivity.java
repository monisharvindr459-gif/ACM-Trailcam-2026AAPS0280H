package com.arrakis.ticketalarm;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.*;
import android.widget.*;
import org.json.JSONObject;
import org.json.JSONTokener;
public class SeatBrowserActivity extends Activity{
 private WebView web;
 private TextView info;
 private Handler handler=new Handler(Looper.getMainLooper());
 private boolean alive=true;
 private final Runnable check=new Runnable(){public void run(){
  if(!alive||web==null)return;
  inspect();handler.postDelayed(this,4500);
 }};
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff081325);setContentView(root);
  info=new TextView(this);info.setTextColor(Color.WHITE);info.setTextSize(15);
  info.setPadding(18,20,18,20);
  info.setText("UNVERIFIED: Navigate to a Dune IMAX seat map. Only actual selectable seat widgets can trigger the alarm.");
  root.addView(info);
  web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
  web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
  CookieManager.getInstance().setAcceptCookie(true);
  web.setWebChromeClient(new WebChromeClient());
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest req){
    String host=req.getUrl().getHost();
    if(host!=null&&(host.equals("in.bookmyshow.com")||host.equals("bookmyshow.com")))return false;
    try{startActivity(new Intent(Intent.ACTION_VIEW,req.getUrl()));}catch(Exception ignored){}
    return true;
   }
  });
  web.loadUrl(SeatVerifier.moviePage(18));
  handler.postDelayed(check,6500);
 }
 private void inspect(){
  String url=web.getUrl();
  if(url==null||!url.matches("https://in\\.bookmyshow\\.com/movies/chennai/seat-layout/ET00526290/(PVPZ|INPR)/[0-9]{3,9}/202612(18|19|20)([?#].*)?"))return;
  String script="""
  (function(){
    try{
      var content=(document.body?document.body.innerText:'').toLowerCase();
      if(!content.includes('dune: part three')||!content.includes('imax'))return JSON.stringify({ok:false});
      if(/booking window closed|no seats available|connectivity issue with cinema|bookings for this cinema are not going through/.test(content))return JSON.stringify({ok:false});
      var selectors=['[data-seat-id][data-seat-status="available"]','[data-seat-id][data-status="available"]','[data-seat-number][data-seat-status="available"]','[aria-label*="Seat"][aria-label*="Available"]'];
      var matches=selectors.flatMap(s=>Array.from(document.querySelectorAll(s))).filter(e=>{
        var name=e.getAttribute('data-seat-id')||e.getAttribute('data-seat-number')||e.getAttribute('aria-label')||'';
        var state=(String(e.className)+' '+String(e.getAttribute('aria-disabled'))).toLowerCase();
        return /[A-Z]{1,3}[- ]?[0-9]{1,3}/i.test(name) && !e.disabled && !/sold|disabled|unavailable|true/.test(state);
      });
      return JSON.stringify({ok:matches.length>0});
    }catch(e){return JSON.stringify({ok:false});}
  })()
  """;
  web.evaluateJavascript(script,response->{
   try{
    JSONObject o=new JSONObject(String.valueOf(new JSONTokener(response).nextValue()));
    if(!o.optBoolean("ok"))return;
    String actual=web.getUrl();
    if(actual==null)return;
    String[] segments=actual.split("/");
    int day=Integer.parseInt(segments[segments.length-1].split("[?#]")[0].substring(6));
    if(day<18||day>20)return;
    MainActivity.prefs(this).edit().putString("verified_dune_url",actual)
      .putString("verified_dune_details","Live seat map "+day+" December").apply();
    info.setText("Positive seat widgets found. Alarm triggered. Continue booking in this page.");
    MainActivity.ring(this,"Dune IMAX seats selectable "+day+" December. Book now!",day);
   }catch(Exception ignored){}
  });
 }
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){alive=false;handler.removeCallbacks(check);if(web!=null)web.destroy();super.onDestroy();}
}
