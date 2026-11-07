package app.alarmchallenge;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i) {
  var p=c.getSharedPreferences(Alarms.PREF,0);
  if(p.getBoolean("ringing",false) || p.getBoolean("awaiting",false)) { p.edit().putBoolean("awaiting",true).apply(); Alarms.schedule(c,Alarms.MISSED,System.currentTimeMillis()+5000); }
  else if(p.getLong("checkDue",0)>0) Alarms.schedule(c,Alarms.CHECK,Math.max(System.currentTimeMillis()+5000,p.getLong("checkDue",0)));
  Alarms.next(c);
 }
}
