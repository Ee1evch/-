package app.alarmchallenge;

import android.app.*;
import android.content.*;
import android.os.Build;
import android.provider.Settings;
import java.util.Calendar;

final class Alarms {
 static final long CHECK_DELAY_MS = 120_000L;
 static final String PREF="alarm", RING="ring", CHECK="check", MISSED="missed";
 static PendingIntent intent(Context c, String action) {
  return PendingIntent.getBroadcast(c, action.equals(RING)?1:action.equals(CHECK)?2:3,
    new Intent(c,AlarmReceiver.class).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }
 static boolean schedule(Context c, String action, long time) {
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if (Build.VERSION.SDK_INT>=31 && !a.canScheduleExactAlarms()) return false;
  a.setAlarmClock(new AlarmManager.AlarmClockInfo(time, PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT)),intent(c,action));
  return true;
 }
 static void cancel(Context c,String action) { ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(intent(c,action)); }
 static void next(Context c) {
  var p=c.getSharedPreferences(PREF,0); if (!p.getBoolean("enabled",false)) return;
  Calendar t=Calendar.getInstance(); t.set(Calendar.HOUR_OF_DAY,p.getInt("hour",7)); t.set(Calendar.MINUTE,p.getInt("minute",0)); t.set(Calendar.SECOND,0); t.set(Calendar.MILLISECOND,0); if(t.getTimeInMillis()<=System.currentTimeMillis()) t.add(Calendar.DATE,1);
  schedule(c,RING,t.getTimeInMillis());
 }
 static void channels(Context c) {
  NotificationManager n=c.getSystemService(NotificationManager.class);
  NotificationChannel alarm=new NotificationChannel("alarm", "Будильник",NotificationManager.IMPORTANCE_HIGH); alarm.setSound(null,null); alarm.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); n.createNotificationChannel(alarm);
  NotificationChannel check=new NotificationChannel("check", "Проверка пробуждения",NotificationManager.IMPORTANCE_HIGH); n.createNotificationChannel(check);
 }
}
