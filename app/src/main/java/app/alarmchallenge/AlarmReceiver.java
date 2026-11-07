package app.alarmchallenge;
import android.app.*;
import android.content.*;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i) {
  String action=i.getAction(); var p=c.getSharedPreferences(Alarms.PREF,0);
  if (Alarms.CHECK.equals(action)) {
   p.edit().putBoolean("awaiting",true).remove("checkDue").apply();
   Alarms.channels(c);
   Intent open=new Intent(c,MainActivity.class).setAction("confirm").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
   PendingIntent pi=PendingIntent.getActivity(c,4,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
   Notification n=new Notification.Builder(c,"check").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Вы проснулись?").setContentText("Нажмите и подтвердите пробуждение в течение минуты").setContentIntent(pi).setAutoCancel(false).setOngoing(true).build();
   try { c.getSystemService(NotificationManager.class).notify(2,n); } catch(SecurityException ignored) {}
   Alarms.schedule(c,Alarms.MISSED,System.currentTimeMillis()+60_000);
  } else if (Alarms.RING.equals(action) || (Alarms.MISSED.equals(action) && p.getBoolean("awaiting",false))) {
   if(Alarms.RING.equals(action)) { Alarms.next(c); Alarms.cancel(c,Alarms.CHECK); Alarms.cancel(c,Alarms.MISSED); }
   p.edit().putBoolean("awaiting",false).putBoolean("ringing",true).remove("checkDue").apply();
   c.getSystemService(NotificationManager.class).cancel(2);
   Intent service=new Intent(c,AlarmService.class);
   if(Build.VERSION.SDK_INT>=26) c.startForegroundService(service); else c.startService(service);
  }
 }
}
