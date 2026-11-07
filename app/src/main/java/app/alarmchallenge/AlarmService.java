package app.alarmchallenge;
import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import org.json.*;
import java.util.*;

public class AlarmService extends Service {
 private final ArrayList<MediaPlayer> players=new ArrayList<>();
 private PowerManager.WakeLock wake;
 @Override public int onStartCommand(Intent i,int flags,int id) {
  Alarms.channels(this);
  Intent open=new Intent(this,MainActivity.class).setAction("alarm").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent pi=PendingIntent.getActivity(this,5,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(this,"alarm").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Будильник звонит").setContentText("Решите пример, чтобы остановить сигнал").setOngoing(true).setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX).setContentIntent(pi).setFullScreenIntent(pi,true).build();
  startForeground(1,n);
  if(players.isEmpty()) {
   wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"alarmchallenge:ring"); wake.acquire(10*60*1000L);
   try { JSONArray a=new JSONArray(getSharedPreferences(Alarms.PREF,0).getString("tracks","[]")); for(int x=0;x<a.length();x++) add(Uri.parse(a.getString(x))); } catch(Exception ignored) {}
   if(players.isEmpty()) add(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
   for(MediaPlayer p:players) try { p.start(); } catch(Exception ignored) {}
  }
  return START_STICKY;
 }
 private void add(Uri uri) {
  MediaPlayer p=new MediaPlayer();
  try { p.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()); p.setDataSource(this,uri); p.setLooping(true); p.prepare(); players.add(p); } catch(Exception e) { p.release(); }
 }
 @Override public void onDestroy() { for(MediaPlayer p:players) p.release(); players.clear(); if(wake!=null&&wake.isHeld()) wake.release(); super.onDestroy(); }
 @Override public IBinder onBind(Intent i) { return null; }
}
