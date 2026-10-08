package com.shubham.ambientlight;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import java.nio.ByteBuffer;

public class AmbientService extends Service {
    private MediaProjection projection; private VirtualDisplay display; private ImageReader reader;
    private WindowManager wm; private GlowView glow; private Handler handler; private int strength = 65;

    @Override public void onCreate() {
        super.onCreate(); handler = new Handler(Looper.getMainLooper());
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel("ambient","Ambient Light",NotificationManager.IMPORTANCE_LOW));
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this,"ambient") : new Notification.Builder(this);
        startForeground(7, nb.setContentTitle("Ambient Light for YouTube").setContentText("Running").setSmallIcon(android.R.drawable.ic_menu_view).build());
    }
    @Override public int onStartCommand(Intent in, int flags, int id) {
        if (in == null) return START_NOT_STICKY;
        strength = in.getIntExtra("strength",65);
        int code = in.getIntExtra("code",Activity.RESULT_CANCELED);
        Intent data = in.getParcelableExtra("data");
        MediaProjectionManager m = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection = m.getMediaProjection(code,data);
        if (projection == null) { stopSelf(); return START_NOT_STICKY; }
        setupCapture(); setupOverlay(); return START_STICKY;
    }
    private void setupCapture() {
        DisplayMetrics dm = getResources().getDisplayMetrics(); int w=Math.max(320,dm.widthPixels/2), h=Math.max(180,dm.heightPixels/2);
        reader=ImageReader.newInstance(w,h,PixelFormat.RGBA_8888,2); reader.setOnImageAvailableListener(this::sample,handler);
        display=projection.createVirtualDisplay("AmbientCapture",w,h,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
    }
    private void sample(ImageReader r) {
        Image im=null; try {
            im=r.acquireLatestImage(); if(im==null)return; Image.Plane p=im.getPlanes()[0]; ByteBuffer b=p.getBuffer();
            int ps=p.getPixelStride(), rs=p.getRowStride(), w=im.getWidth(), h=im.getHeight(); int rr=0,gg=0,bb=0,n=0;
            int[] xs={w/5,w/2,4*w/5}, ys={h/5,h/2,4*h/5};
            for(int y:ys) for(int x:xs){int q=y*rs+x*ps;if(q+2<b.capacity()){rr+=b.get(q)&255;gg+=b.get(q+1)&255;bb+=b.get(q+2)&255;n++;}}
            if(n>0){int c=Color.rgb(rr/n,gg/n,bb/n); handler.post(()->{if(glow!=null)glow.setGlow(c,strength);});}
        } catch(Throwable ignored){} finally {if(im!=null)im.close();}
    }
    private void setupOverlay() {
        if(!Settings.canDrawOverlays(this))return; wm=(WindowManager)getSystemService(WINDOW_SERVICE); glow=new GlowView(this);
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);
        wm.addView(glow,lp);
    }
    @Override public void onDestroy(){try{if(wm!=null&&glow!=null)wm.removeView(glow);}catch(Throwable ignored){} if(display!=null)display.release();if(reader!=null)reader.close();if(projection!=null)projection.stop();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}

    public static class GlowView extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private int color=Color.BLUE, alpha=90;
        public GlowView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        public void setGlow(int c,int s){color=c;alpha=Math.max(20,Math.min(150,(int)(s*1.5f)));invalidate();}
        private int a(int c,int x){return Color.argb(x,Color.red(c),Color.green(c),Color.blue(c));}
        @Override protected void onDraw(Canvas c){int w=getWidth(),h=getHeight(),d=80;
            p.setShader(new LinearGradient(0,0,0,d,a(color,alpha),a(color,0),Shader.TileMode.CLAMP));c.drawRect(0,0,w,d,p);
            p.setShader(new LinearGradient(0,h-d,0,h,a(color,0),a(color,alpha),Shader.TileMode.CLAMP));c.drawRect(0,h-d,w,h,p);
            p.setShader(new LinearGradient(0,0,d,0,a(color,alpha),a(color,0),Shader.TileMode.CLAMP));c.drawRect(0,0,d,h,p);
            p.setShader(new LinearGradient(w-d,0,w,0,a(color,0),a(color,alpha),Shader.TileMode.CLAMP));c.drawRect(w-d,0,w,h,p); p.setShader(null);
        }
    }
}
