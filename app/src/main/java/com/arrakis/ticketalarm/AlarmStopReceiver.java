package com.arrakis.ticketalarm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class AlarmStopReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  MainActivity.stop(context);
 }
}
