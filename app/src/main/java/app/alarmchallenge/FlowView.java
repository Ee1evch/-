package app.alarmchallenge;
import android.content.Context;
import android.graphics.*;
import android.view.View;
/** Lightweight procedural vector field, inspired by the supplied motion reference. */
final class FlowView extends View {
 private final Paint p=new Paint(3); private final long started=android.os.SystemClock.uptimeMillis();
 FlowView(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight();p.setStrokeWidth(d*.5f);p.setColor(0xff20232a);
 for(float x=0;x<w;x+=28*d)c.drawLine(x,0,x,h,p);for(float y=0;y<h;y+=28*d)c.drawLine(0,y,w,y,p);
 float t=android.animation.ValueAnimator.areAnimatorsEnabled()?(android.os.SystemClock.uptimeMillis()-started)/1000f:0;
 for(int i=0;i<21;i++){float x=((i*79*d+t*(10+i%5*4)*d)%(w+60*d))-30*d;float y=((i*47+17)*d)%h;int color=i%3==0?0xff66c6c4:0xffbfa1ec;
 p.setShader(new LinearGradient(x-38*d,y,x+9*d,y,new int[]{0x00000000,color},null,Shader.TileMode.CLAMP));p.setAlpha(80);p.setStrokeWidth(d);c.drawLine(x-38*d,y,x,y,p);p.setShader(null);p.setColor(color);p.setAlpha(100);Path a=new Path();a.moveTo(x+8*d,y);a.lineTo(x-4*d,y-3*d);a.lineTo(x,y);a.lineTo(x-4*d,y+3*d);a.close();c.drawPath(a,p);}
 p.setAlpha(255);if(android.animation.ValueAnimator.areAnimatorsEnabled()&&isShown())postInvalidateDelayed(40);
 }
}
