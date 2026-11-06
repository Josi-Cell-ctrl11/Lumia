package com.josias.luma;

import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/** Fond animé optionnel. La sécurité et les notifications restent gérées par XOS. */
public class DepthWallpaper extends WallpaperService {
    @Override public Engine onCreateEngine(){return new DepthEngine();}
    class DepthEngine extends Engine {
        final Handler handler=new Handler(Looper.getMainLooper());boolean visible;Bitmap photo,mask;long imageStamp=-1,maskStamp=-1;
        final Runnable frame=()->draw();
        @Override public void onVisibilityChanged(boolean value){visible=value;handler.removeCallbacks(frame);if(value)draw();}
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){draw();}
        @Override public void onSurfaceDestroyed(SurfaceHolder holder){visible=false;handler.removeCallbacks(frame);super.onSurfaceDestroyed(holder);}
        @Override public void onDestroy(){handler.removeCallbacks(frame);super.onDestroy();}
        void reload(){File f=new File(getFilesDir(),"wallpaper.png"),m=new File(getFilesDir(),"mask.png");if(f.lastModified()!=imageStamp){imageStamp=f.lastModified();photo=BitmapFactory.decodeFile(f.toString());}if(m.lastModified()!=maskStamp){maskStamp=m.lastModified();mask=BitmapFactory.decodeFile(m.toString());}}
        void draw(){handler.removeCallbacks(frame);if(!getSurfaceHolder().getSurface().isValid())return;reload();Canvas c=null;try{c=getSurfaceHolder().lockCanvas();if(c==null)return;float w=c.getWidth(),h=c.getHeight();DepthRenderer.background(c,photo,w,h);boolean lock=isPreview()||getSystemService(KeyguardManager.class).isKeyguardLocked();if(lock){float factor=w/400f;c.save();c.scale(factor,factor);SharedPreferences prefs=getSharedPreferences("MainActivity",0);if(photo==null)DepthRenderer.landscape(c,400,h/factor,false);Paint p=new Paint(3);p.setColor(Color.WHITE);p.setTextSize(19);p.setTextAlign(Paint.Align.CENTER);c.drawText(new SimpleDateFormat("EEEE d MMMM",Locale.FRANCE).format(new Date()),200,95,p);DepthRenderer.clock(c,new SimpleDateFormat("HH:mm",Locale.FRANCE).format(new Date()),180+prefs.getInt("clockY",30),400);if(prefs.getBoolean("depth",true)){if(photo==null)DepthRenderer.landscape(c,400,h/factor,true);else DepthRenderer.subject(c,photo,mask,400,h/factor);}c.restore();}}catch(RuntimeException ignored){}finally{if(c!=null)getSurfaceHolder().unlockCanvasAndPost(c);}if(visible)handler.postDelayed(frame,1000);}
    }
}
