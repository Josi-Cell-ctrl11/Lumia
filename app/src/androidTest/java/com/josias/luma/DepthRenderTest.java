package com.josias.luma;

import android.graphics.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.io.*;

@RunWith(AndroidJUnit4.class)
public class DepthRenderTest {
    @Test public void maskOnlyCoversSelectedPixels(){
        Bitmap photo=Bitmap.createBitmap(100,200,Bitmap.Config.ARGB_8888);photo.eraseColor(Color.RED);
        Bitmap mask=Bitmap.createBitmap(100,200,Bitmap.Config.ARGB_8888);Canvas mc=new Canvas(mask);Paint p=new Paint();p.setColor(Color.WHITE);mc.drawRect(0,100,100,200,p);
        Bitmap out=Bitmap.createBitmap(100,200,Bitmap.Config.ARGB_8888);out.eraseColor(Color.BLUE);DepthRenderer.subject(new Canvas(out),photo,mask,100,200);
        assertEquals("Horloge visible hors du sujet",Color.BLUE,out.getPixel(50,50));assertEquals("Sujet devant l’horloge",Color.RED,out.getPixel(50,150));
    }
    @Test public void centerCropKeepsLayersAligned(){
        Bitmap b=Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);RectF r=DepthRenderer.crop(b,100,200);assertEquals(-50f,r.left,.01f);assertEquals(150f,r.right,.01f);assertEquals(0f,r.top,.01f);
    }
    @Test public void renderHomeAndLockAtPhoneSizes(){
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            scenario.onActivity(a->{int[] widths={360,412};for(int width:widths){a.show(false);a.scene.layout(0,0,width,width*2);Bitmap home=Bitmap.createBitmap(width,width*2,Bitmap.Config.ARGB_8888);a.scene.draw(new Canvas(home));write(a,home,"home-"+width+".png");assertTrue(a.scene.hits.size()>4);a.show(true);a.scene.layout(0,0,width,width*2);Bitmap lock=Bitmap.createBitmap(width,width*2,Bitmap.Config.ARGB_8888);a.scene.draw(new Canvas(lock));write(a,lock,"lock-"+width+".png");assertNotEquals(Color.TRANSPARENT,lock.getPixel(width/2,width));}});
        }
    }
    void write(MainActivity a,Bitmap b,String name){try(FileOutputStream f=new FileOutputStream(new File(a.getExternalFilesDir(null),name))){b.compress(Bitmap.CompressFormat.PNG,100,f);}catch(IOException e){throw new AssertionError(e);}}
}
