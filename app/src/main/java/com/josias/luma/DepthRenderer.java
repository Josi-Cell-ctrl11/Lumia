package com.josias.luma;

import android.graphics.*;

/** Composition commune : fond, horloge, puis pixels du sujet découpé. */
final class DepthRenderer {
    static void background(Canvas c,Bitmap photo,float w,float h){
        Paint p=new Paint(3);
        if(photo!=null){c.drawBitmap(photo,null,crop(photo,w,h),p);return;}
        p.setShader(new LinearGradient(0,0,w,h,new int[]{0xFF93D7FF,0xFFE3DEFF,0xFFFFD9C0},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        // Courbes translucides du fond clair, dessinées à la résolution de l’écran.
        for(int i=0;i<4;i++){Path wave=new Path();wave.moveTo(-80,h*(.15f+i*.22f));wave.cubicTo(w*.8f,h*(-.15f+i*.24f),w*.15f,h*(.8f+i*.05f),w+100,h*(.3f+i*.19f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(38-i*7);p.setColor(0x30FFFFFF);c.drawPath(wave,p);p.setStrokeWidth(1.3f);p.setColor(0xAFFFFFFF);c.drawPath(wave,p);}p.setStyle(Paint.Style.FILL);
    }
    static RectF crop(Bitmap b,float w,float h){float s=Math.max(w/b.getWidth(),h/b.getHeight());float bw=b.getWidth()*s,bh=b.getHeight()*s;return new RectF((w-bw)/2,(h-bh)/2,(w+bw)/2,(h+bh)/2);}
    static void subject(Canvas c,Bitmap photo,Bitmap mask,float w,float h){
        if(photo==null||mask==null)return;Paint p=new Paint(3);int layer=c.saveLayer(0,0,w,h,null);RectF bounds=crop(photo,w,h);c.drawBitmap(photo,null,bounds,p);p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));c.drawBitmap(mask,null,bounds,p);p.setXfermode(null);c.restoreToCount(layer);
    }
    static void clock(Canvas c,String time,float y,float w){Paint p=new Paint(3);p.setTypeface(Typeface.create("sans-serif-thin",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(w*.267f);p.setShader(new LinearGradient(0,y-100,0,y,new int[]{0xF5FFFFFF,0xAADCEDFF,0xFFFFFFFF},null,Shader.TileMode.CLAMP));p.setShadowLayer(3,0,2,0x180B3D61);c.drawText(time,w/2,y,p);p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.65f);p.setColor(0xCFFFFFFF);c.drawText(time,w/2,y,p);}
    // Paysage vectoriel de démonstration : le relief fournit un premier plan précis.
    static void landscape(Canvas c,float w,float h,boolean foreground){
        Paint p=new Paint(3);float sx=w/400f,sy=h/840f;c.save();c.scale(sx,sy);
        if(!foreground){p.setShader(new LinearGradient(0,0,0,840,new int[]{0xFF87CFFC,0xFFC0E9F7,0xFF3BC6C2},null,Shader.TileMode.CLAMP));c.drawRect(0,0,400,840,p);p.setShader(null);p.setColor(0xA5FFFFFF);c.drawCircle(310,80,38,p);}
        Path mountain=new Path();mountain.moveTo(-20,480);mountain.lineTo(45,360);mountain.lineTo(88,385);mountain.lineTo(192,175);mountain.lineTo(236,260);mountain.lineTo(263,242);mountain.lineTo(350,380);mountain.lineTo(420,430);mountain.lineTo(420,625);mountain.lineTo(-20,625);mountain.close();
        p.setShader(new LinearGradient(0,200,400,625,new int[]{0xFF7BBA89,0xFF285F66,0xFF123D58},null,Shader.TileMode.CLAMP));c.drawPath(mountain,p);p.setShader(null);
        Path ridge=new Path();ridge.moveTo(192,175);ridge.lineTo(172,252);ridge.lineTo(193,229);ridge.lineTo(215,277);ridge.lineTo(205,235);ridge.lineTo(236,260);ridge.close();p.setColor(0xFFEBE3BE);c.drawPath(ridge,p);
        p.setShader(new LinearGradient(0,540,0,840,new int[]{0xFF41B9BE,0xFF8AE0D4,0xFFF8E7C1},null,Shader.TileMode.CLAMP));Path sea=new Path();sea.moveTo(0,575);sea.cubicTo(150,590,220,620,400,552);sea.lineTo(400,840);sea.lineTo(0,840);sea.close();c.drawPath(sea,p);p.setShader(null);for(int i=0;i<7;i++){p.setColor(0x75FFFFFF);p.setStrokeWidth(1.2f);c.drawLine(20+i*4,650+i*24,370-i*9,636+i*24,p);}c.restore();
    }
}
