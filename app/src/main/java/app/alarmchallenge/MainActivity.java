package app.alarmchallenge;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
 private android.content.SharedPreferences prefs;
 private LinearLayout root; private int answer;
 @Override public void onCreate(Bundle b) { super.onCreate(b); if(Build.VERSION.SDK_INT>=27) { setShowWhenLocked(true); setTurnScreenOn(true); } else { getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); } prefs=getSharedPreferences(Alarms.PREF,0); Alarms.channels(this); render(); }
 @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); render(); }
 @Override protected void onResume() { super.onResume(); if(prefs!=null) render(); }
 private TextView text(String s,int size) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(0xff16213b); t.setPadding(0,12,0,12); root.addView(t); return t; }
 private Button button(String s,Runnable action) { Button b=new Button(this); b.setText(s); root.addView(b); b.setOnClickListener(v->action.run()); return b; }
 private void base(String title) {
  ScrollView scroll=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(1); root.setPadding(32,32,32,32); root.setBackgroundColor(0xfff5f7ff); scroll.addView(root); setContentView(scroll); text(title,28);
 }
 private void render() {
  if(prefs.getBoolean("ringing",false)) { getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); challenge(); return; }
  getWindow().getDecorView().setSystemUiVisibility(0);
  if(prefs.getBoolean("awaiting",false)) { confirmation(); return; }
  base("Проснись ☀");
  text("Будильник с примером и проверкой пробуждения",16);
  int h=prefs.getInt("hour",7), m=prefs.getInt("minute",0);
  text(String.format(Locale.getDefault(),"Время: %02d:%02d • %s",h,m,prefs.getBoolean("enabled",false)?"включён":"выключен"),20);
  button("Выбрать время",()->new TimePickerDialog(this,(v,hour,minute)->{prefs.edit().putInt("hour",hour).putInt("minute",minute).apply(); if(prefs.getBoolean("enabled",false)) Alarms.next(this); render();},h,m,true).show());
  button(prefs.getBoolean("enabled",false)?"Выключить ежедневный будильник":"Включить ежедневный будильник",()->{
   if(prefs.getBoolean("enabled",false)) { prefs.edit().putBoolean("enabled",false).apply(); Alarms.cancel(this,Alarms.RING); render(); return; }
   if(!permissions()) return;
   prefs.edit().putBoolean("enabled",true).apply(); Alarms.next(this); render();
  });
  text("Мелодии играют одновременно и повторяются. Если файлы не выбраны или недоступны, используется системный сигнал.",16);
  try { JSONArray a=new JSONArray(prefs.getString("tracks","[]")); text("Выбрано мелодий: "+a.length(),16); } catch(Exception ignored) {}
  button("Добавить мелодии",()->{ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(i,42); });
  button("Очистить список мелодий",()->{ prefs.edit().putString("tracks","[]").apply(); render(); });
  int delay=prefs.getInt("delay",5); text("Проверка через: "+delay+" мин. На ответ даётся 1 минута.",16);
  button("Изменить время проверки",()->{ String[] values={"1 минута","5 минут","10 минут","15 минут","30 минут"}; int[] mins={1,5,10,15,30}; new AlertDialog.Builder(this).setTitle("Проверка пробуждения").setItems(values,(d,which)->{prefs.edit().putInt("delay",mins[which]).apply();render();}).show(); });
  button("Проверить сигнал сейчас",()->{ if(permissions()) { new AlarmReceiver().onReceive(this,new Intent(this,AlarmReceiver.class).setAction(Alarms.RING)); render(); } });
  text("Разрешите уведомления, точные будильники и полноэкранные уведомления в настройках. Система может ограничивать сигнал; протестируйте его на своём телефоне.",14);
 }
 private boolean permissions() {
  if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7); Toast.makeText(this,"Разрешите уведомления и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager.class).canScheduleExactAlarms()) { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Разрешите точные будильники и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  if(Build.VERSION.SDK_INT>=34 && !getSystemService(NotificationManager.class).canUseFullScreenIntent()) { startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Разрешите полноэкранные уведомления и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  return true;
 }
 private void challenge() {
  base("Пора просыпаться!");
  int a=prefs.getInt("a",-1),b=prefs.getInt("b",-1);
  if(a<0) { Random r=new Random(); a=12+r.nextInt(38); b=11+r.nextInt(39); prefs.edit().putInt("a",a).putInt("b",b).apply(); }
  answer=a+b; text("Решите пример, чтобы остановить сигнал:",18); text(a+" + "+b+" = ?",30);
  EditText input=new EditText(this); input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); input.setHint("Ответ"); root.addView(input);
  button("Ответить",()->{ if(input.getText().toString().equals(String.valueOf(answer))) {
   long when=System.currentTimeMillis()+prefs.getInt("delay",5)*60_000L;
   if(!Alarms.schedule(this,Alarms.CHECK,when)) { Toast.makeText(this,"Нет разрешения на точные будильники",Toast.LENGTH_LONG).show(); return; }
   prefs.edit().putBoolean("ringing",false).putLong("checkDue",when).remove("a").remove("b").apply(); try { stopLockTask(); } catch (Exception ignored) {} stopService(new Intent(this,AlarmService.class)); render();
  } else { input.setError("Попробуйте ещё раз"); } });
  button("Закрепить экран",()->{ try { startLockTask(); } catch(Exception e) { Toast.makeText(this,"Не удалось закрепить экран",Toast.LENGTH_SHORT).show(); } });
  text("Система попросит подтвердить закрепление. Выйти из него можно системным жестом.",14);
 }
 private void confirmation() { base("Вы проснулись?"); text("Подтвердите, что вы не уснули снова. Иначе сигнал повторится.",18); button("Да, я проснулся",()->{ Alarms.cancel(this,Alarms.MISSED); prefs.edit().putBoolean("awaiting",false).apply(); getSystemService(NotificationManager.class).cancel(2); render(); }); }
 @Override public void onBackPressed() { if(prefs.getBoolean("ringing",false)) { Toast.makeText(this,"Решите пример, чтобы остановить сигнал",Toast.LENGTH_SHORT).show(); } else super.onBackPressed(); }
 @Override protected void onActivityResult(int request,int result,Intent data) { super.onActivityResult(request,result,data); if(request!=42||result!=RESULT_OK||data==null)return;
  try { JSONArray a=new JSONArray(prefs.getString("tracks","[]")); ArrayList<Uri> selected=new ArrayList<>(); if(data.getClipData()!=null) for(int n=0;n<data.getClipData().getItemCount();n++) selected.add(data.getClipData().getItemAt(n).getUri()); else if(data.getData()!=null) selected.add(data.getData());
   for(Uri u:selected) { try { getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION); boolean exists=false; for(int x=0;x<a.length();x++) if(a.getString(x).equals(u.toString())) exists=true; if(!exists)a.put(u.toString()); } catch(SecurityException ignored) {} }
   prefs.edit().putString("tracks",a.toString()).apply(); render();
  } catch(Exception e) { Toast.makeText(this,"Не удалось добавить мелодии",Toast.LENGTH_SHORT).show(); }
 }
}
