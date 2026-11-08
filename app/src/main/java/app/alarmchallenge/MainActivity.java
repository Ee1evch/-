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
 private static final int BG=0xff060606, CARD=0xff111113, FG=0xffeeedf5, MUTED=0xff8c8b95, ACCENT=0xffc3a3ed;
 private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
 private android.graphics.drawable.GradientDrawable shape(int color,int radius) { android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
 private LinearLayout column(LinearLayout parent,int padding) { LinearLayout box=new LinearLayout(this); box.setOrientation(1); box.setPadding(dp(padding),dp(padding),dp(padding),dp(padding)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(12); parent.addView(box,lp); return box; }
 private TextView label(LinearLayout parent,String value,int size,int color) { TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings("tnum"); t.setPadding(0,dp(4),0,dp(6)); parent.addView(t); return t; }
 private void section(String title,String description) { TextView t=text(title,20); t.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0)); if(description!=null) label(root,description,13,MUTED); }
 private void pickTime() { int h=prefs.getInt("hour",7),m=prefs.getInt("minute",0); new TimePickerDialog(this,(v,hour,minute)->{prefs.edit().putInt("hour",hour).putInt("minute",minute).apply(); if(prefs.getBoolean("enabled",false)) Alarms.next(this);render();},h,m,true).show(); }
 private void toggleAlarm() { if(prefs.getBoolean("enabled",false)) {prefs.edit().putBoolean("enabled",false).apply();Alarms.cancel(this,Alarms.RING);} else {if(!permissions())return;prefs.edit().putBoolean("enabled",true).apply();Alarms.next(this);}render(); }

 @Override public void onCreate(Bundle b) { super.onCreate(b); if(Build.VERSION.SDK_INT>=27) { setShowWhenLocked(true); setTurnScreenOn(true); } else { getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); } prefs=getSharedPreferences(Alarms.PREF,0); Alarms.channels(this); render(); }
 @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); render(); }
 @Override protected void onResume() { super.onResume(); if(prefs!=null) render(); }
 private TextView text(String s,int size) { return label(root,s,size,FG); }
 private Button button(String s,Runnable action) {
  Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(FG); b.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));
  b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x33ffffff),shape(CARD,12),null));
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));lp.topMargin=dp(8);lp.bottomMargin=dp(4);root.addView(b,lp);b.setOnClickListener(v->action.run());return b;
 }
 private void primary(String title,Runnable action) { Button b=button(title,action);b.setTextColor(BG);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x33000000),shape(0xffeae8f1,24),null)); }
 private void base(String title) {
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(0);
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);scroll.setFitsSystemWindows(true);scroll.setFillViewport(true);
  root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(8),dp(16),dp(16));scroll.addView(root);setContentView(scroll);
  TextView heading=text(title,18);heading.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));
 }
 private void hero(String eyebrow,String main,String caption,int height) {
  FrameLayout frame=new FrameLayout(this); frame.setBackground(shape(BG,0));frame.setClipToOutline(true);
  LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(height));fp.topMargin=dp(16);fp.bottomMargin=dp(16);root.addView(frame,fp);

  frame.addView(new FlowView(this),new FrameLayout.LayoutParams(-1,-1));
  LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setGravity(Gravity.CENTER);content.setPadding(dp(12),0,dp(12),0);frame.addView(content,new FrameLayout.LayoutParams(-1,-1));
  TextView e=label(content,eyebrow,11,ACCENT);e.setLetterSpacing(.18f);e.setGravity(Gravity.CENTER);
  TextView clock=label(content,main,main.length()>7?46:72,FG);clock.setTypeface(android.graphics.Typeface.create("sans-serif-light",0));clock.setGravity(Gravity.CENTER);
  TextView c=label(content,caption,12,MUTED);c.setGravity(Gravity.CENTER);
 }
 private void render() {
  if(prefs.getBoolean("ringing",false)) {challenge();return;}
  if(prefs.getBoolean("awaiting",false)) {confirmation();return;}
  base("✳  П Р О С Н И С Ь");
  boolean enabled=prefs.getBoolean("enabled",false);
  LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);root.addView(status);
  TextView everyday=new TextView(this);everyday.setText(enabled?"Каждый день · включён":"Каждый день · выключен");everyday.setTextColor(MUTED);everyday.setTextSize(12);status.addView(everyday,new LinearLayout.LayoutParams(0,dp(36),1));everyday.setGravity(Gravity.CENTER_VERTICAL);
  Switch toggle=new Switch(this);toggle.setContentDescription("Включить будильник");toggle.setChecked(enabled);toggle.setThumbTintList(android.content.res.ColorStateList.valueOf(enabled?ACCENT:0xff8c8b95));status.addView(toggle);toggle.setOnCheckedChangeListener((v,on)->toggleAlarm());
  hero("БУДИЛЬНИК",String.format(Locale.getDefault(),"%02d:%02d",prefs.getInt("hour",7),prefs.getInt("minute",0)),"Нажми на время, чтобы изменить",310);
  root.getChildAt(root.getChildCount()-1).setOnClickListener(v->pickTime());
  int count=0;try{count=new JSONArray(prefs.getString("tracks","[]")).length();}catch(Exception ignored){}
  divider();
  setting("♫", "Мелодии",count==0?"Системный сигнал":count+" треков · играют вместе",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,42);});
  if(count>0){TextView clear=label(root,"Очистить список",12,MUTED);clear.setOnClickListener(v->{prefs.edit().putString("tracks","[]").apply();render();});}
  divider();
  setting("◷", "Проверка пробуждения","Через 2 минуты после решения",()->new AlertDialog.Builder(this).setTitle("Проверка через 2 минуты").setMessage("После правильного ответа придёт уведомление. Подтверди пробуждение за минуту, иначе будильник зазвонит снова.").setPositiveButton("Понятно",null).show());
  divider();
  long due=prefs.getLong("checkDue",0);if(due>System.currentTimeMillis())label(root,"Следующая проверка в "+new java.text.SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(due)),12,ACCENT);
  primary("▷   Проверить сигнал",()->{if(permissions()){new AlarmReceiver().onReceive(this,new Intent(this,AlarmReceiver.class).setAction(Alarms.RING));render();}});
 }
 private void divider(){View line=new View(this);line.setBackgroundColor(0xff252529);root.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));}

 private void setting(String icon,String title,String subtitle,Runnable action){
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(10),0,dp(10));root.addView(row,new LinearLayout.LayoutParams(-1,-2));
  TextView symbol=new TextView(this);symbol.setText(icon);symbol.setTextSize(20);symbol.setTextColor(ACCENT);symbol.setGravity(Gravity.CENTER);symbol.setBackground(shape(CARD,10));row.addView(symbol,new LinearLayout.LayoutParams(dp(42),dp(42)));
  LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(14),0,0,0);row.addView(content,new LinearLayout.LayoutParams(0,-2,1));label(content,title,14,FG);label(content,subtitle,12,MUTED);
  TextView arrow=new TextView(this);arrow.setText("›");arrow.setTextSize(24);arrow.setTextColor(MUTED);row.addView(arrow);row.setOnClickListener(v->action.run());row.setContentDescription(title+". "+subtitle);row.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x20c3a3ed),null,shape(CARD,12)));
 }

 private boolean permissions() {
  if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7); Toast.makeText(this,"Разрешите уведомления и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager.class).canScheduleExactAlarms()) { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Разрешите точные будильники и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  if(Build.VERSION.SDK_INT>=34 && !getSystemService(NotificationManager.class).canUseFullScreenIntent()) { startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Разрешите полноэкранные уведомления и нажмите ещё раз",Toast.LENGTH_LONG).show(); return false; }
  return true;
 }
 private void challenge() {
  base("Пора вставать");
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  int a=prefs.getInt("a",-1),b=prefs.getInt("b",-1);if(a<0){Random r=new Random();a=12+r.nextInt(38);b=11+r.nextInt(39);prefs.edit().putInt("a",a).putInt("b",b).apply();} answer=a+b;
  hero("РЕШИ ПРИМЕР",a+" + "+b,"Правильный ответ выключит сигнал",170);
  TextView input=text("Твой ответ",32);input.setGravity(Gravity.CENTER);input.setTextColor(MUTED);input.setContentDescription("Поле ответа");
  StringBuilder digits=new StringBuilder();
  for(String[] row:new String[][]{{"1","2","3"},{"4","5","6"},{"7","8","9"},{"C","0","⌫"}}){
   LinearLayout line=new LinearLayout(this);root.addView(line,new LinearLayout.LayoutParams(-1,dp(58)));
   for(String key:row){Button k=new Button(this);k.setAllCaps(false);k.setText(key);k.setTextSize(23);k.setTextColor(FG);k.setContentDescription(key.equals("⌫")?"Удалить цифру":key.equals("C")?"Очистить ответ":key);k.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x22c3a3ed),shape(CARD,16),null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));line.addView(k,lp);
    k.setOnClickListener(v->{if(key.equals("C"))digits.setLength(0);else if(key.equals("⌫")){if(digits.length()>0)digits.deleteCharAt(digits.length()-1);}else if(digits.length()<4)digits.append(key);input.setText(digits.length()==0?"Твой ответ":digits.toString());input.setTextColor(FG);});
   }
  }
  primary("Проверить ответ",()->{if(digits.toString().equals(String.valueOf(answer))){long when=System.currentTimeMillis()+Alarms.CHECK_DELAY_MS;if(!Alarms.schedule(this,Alarms.CHECK,when)){Toast.makeText(this,"Нет разрешения на точные будильники",Toast.LENGTH_LONG).show();return;}prefs.edit().putBoolean("ringing",false).putLong("checkDue",when).remove("a").remove("b").apply();try{stopLockTask();}catch(Exception ignored){}stopService(new Intent(this,AlarmService.class));render();}else{input.setText("Ещё попытка");input.setTextColor(0xffb4473f);digits.setLength(0);}});
  button("Закрепить экран",()->{try{startLockTask();}catch(Exception e){Toast.makeText(this,"Не удалось закрепить экран",Toast.LENGTH_SHORT).show();}});
 }
 private void confirmation(){base("Ты проснулся?");hero("ПРОШЛО 2 МИНУТЫ","Доброе утро","Подтверди, что не уснул снова",230);label(root,"Нажми кнопку. Без ответа сигнал прозвучит снова.",14,MUTED);primary("Да, я проснулся",()->{Alarms.cancel(this,Alarms.MISSED);prefs.edit().putBoolean("awaiting",false).apply();getSystemService(NotificationManager.class).cancel(2);render();});}
 @Override public void onBackPressed() { if(prefs.getBoolean("ringing",false)) { Toast.makeText(this,"Решите пример, чтобы остановить сигнал",Toast.LENGTH_SHORT).show(); } else super.onBackPressed(); }
 @Override protected void onActivityResult(int request,int result,Intent data) { super.onActivityResult(request,result,data); if(request!=42||result!=RESULT_OK||data==null)return;
  try { JSONArray a=new JSONArray(prefs.getString("tracks","[]")); ArrayList<Uri> selected=new ArrayList<>(); if(data.getClipData()!=null) for(int n=0;n<data.getClipData().getItemCount();n++) selected.add(data.getClipData().getItemAt(n).getUri()); else if(data.getData()!=null) selected.add(data.getData());
   for(Uri u:selected) { try { getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION); boolean exists=false; for(int x=0;x<a.length();x++) if(a.getString(x).equals(u.toString())) exists=true; if(!exists)a.put(u.toString()); } catch(SecurityException ignored) {} }
   prefs.edit().putString("tracks",a.toString()).apply(); render();
  } catch(Exception e) { Toast.makeText(this,"Не удалось добавить мелодии",Toast.LENGTH_SHORT).show(); }
 }
}
