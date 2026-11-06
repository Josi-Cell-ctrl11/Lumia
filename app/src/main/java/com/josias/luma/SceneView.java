package com.josias.luma;

import android.content.*;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.view.*;
import java.text.*;
import java.util.*;

/** Interface dessinée en une passe : verre, icônes et composition de profondeur. */
final class SceneView extends View {
    final MainActivity a;final Paint p=new Paint(3);final ArrayList<Hit> hits=new ArrayList<>();
    final GestureDetector gestures;float scale=1,h=840,startX,startY,lastX,lastY;boolean moved;Bitmap blurred;Bitmap blurSource;
    static class Hit{RectF rect;Runnable action;Hit(RectF r,Runnable f){rect=r;action=f;}}
    SceneView(MainActivity host){super(host);a=host;setLayerType(View.LAYER_TYPE_SOFTWARE,null);setContentDescription("Accueil Luma. Glisser pour changer de page. Appui long pour personnaliser.");gestures=new GestureDetector(host,new GestureDetector.SimpleOnGestureListener(){@Override public boolean onDown(MotionEvent e){return true;}@Override public void onLongPress(MotionEvent e){if(!a.editing)a.settings();}});setFocusable(true);}
    void text(Canvas c,String s,float x,float y,float size,int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,p);}
    void button(RectF r,Runnable action){hits.add(new Hit(r,action));}
    void glass(Canvas c,RectF r,float radius){
        // Un échantillon réduit du fond est agrandi sous le panneau pour diffuser ses détails.
        if(a.photo!=null){if(blurSource!=a.photo){blurSource=a.photo;blurred=Bitmap.createScaledBitmap(a.photo,Math.max(1,a.photo.getWidth()/60),Math.max(1,a.photo.getHeight()/60),true);}c.save();Path clip=new Path();clip.addRoundRect(r,radius,radius,Path.Direction.CW);c.clipPath(clip);p.setColor(-1);p.setShader(null);c.drawBitmap(blurred,null,DepthRenderer.crop(a.photo,400,h),p);c.restore();}
        p.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,new int[]{0xB8FFFFFF,0x40FFFFFF,0x8AFFFFFF},null,Shader.TileMode.CLAMP));p.setStyle(Paint.Style.FILL);c.drawRoundRect(r,radius,radius,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xDDFFFFFF);c.drawRoundRect(r,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);scale=getWidth()/400f;if(scale<=0)return;h=getHeight()/scale;canvas.save();canvas.scale(scale,scale);hits.clear();DepthRenderer.background(canvas,a.photo,400,h);
        if(a.locked){if(a.photo==null)DepthRenderer.landscape(canvas,400,h,false);lock(canvas);}else home(canvas);
        if(a.editing){if(a.mask!=null){int l=canvas.saveLayer(0,0,400,h,null);p.setColor(0xA04B93FF);canvas.drawRect(0,0,400,h,p);p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));canvas.drawBitmap(a.mask,null,DepthRenderer.crop(a.photo,400,h),p);p.setXfermode(null);canvas.restoreToCount(l);}glass(canvas,new RectF(10,h-80,390,h-8),24);text(canvas,a.erase?"Gomme":"Pinceau",70,h-48,15,0xFF17344C);text(canvas,"Effacer tout",200,h-48,14,0xFF17344C);text(canvas,"Enregistrer",326,h-48,14,0xFF17344C);text(canvas,"Peins le sujet devant l’heure · pinceau 24 px",200,h-24,11,0xFF17344C);button(new RectF(10,h-80,130,h-8),()->{a.erase=!a.erase;invalidate();});button(new RectF(135,h-80,265,h-8),()->{a.mask.eraseColor(Color.TRANSPARENT);invalidate();});button(new RectF(270,h-80,390,h-8),a::finishMask);}
        canvas.restore();}
    String date(String pattern){return new SimpleDateFormat(pattern,Locale.FRANCE).format(new Date());}
    void home(Canvas c){
        glass(c,new RectF(22,30,194,192),28);glass(c,new RectF(206,30,378,192),28);
        text(c,date("EEEE").toUpperCase(Locale.FRANCE),108,63,14,0xFFE34E57);text(c,date("d"),108,133,65,0xFF223449);text(c,date("MMMM"),108,167,18,0xFF475670);
        android.content.SharedPreferences weather=a.getSharedPreferences("weather",0);long updated=weather.getLong("updated",0);boolean fresh=System.currentTimeMillis()-updated<3600000;
        text(c,"Cotonou",292,65,20,0xFF23364A);text(c,updated>0?weather.getInt("temperature",0)+"°":"—°",292,123,49,0xFF23364A);text(c,updated>0?Weather.label(weather.getInt("code",0)):"Météo indisponible",292,153,12,0xFF475670);text(c,updated>0?(fresh?"Actualisé":"Dernière mesure"):"Connexion nécessaire",292,175,10,0xFF596680);
        button(new RectF(22,30,194,192),()->a.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)));button(new RectF(206,30,378,192),()->{Weather.refresh(a);a.toast("Météo de Cotonou · données Open-Meteo");});
        ArrayList<ResolveInfo> filtered=new ArrayList<>();for(ResolveInfo r:a.apps)if(r.loadLabel(a.getPackageManager()).toString().toLowerCase(Locale.ROOT).contains(a.query.toLowerCase(Locale.ROOT)))filtered.add(r);
        int pages=Math.max(1,(filtered.size()+15)/16);a.page=Math.min(a.page,pages-1);float rowH=Math.min(107,(h-385)/4f);float iconSize=Math.min(66,rowH-26);
        for(int i=0;i<16;i++){int n=a.page*16+i;if(n>=filtered.size())break;ResolveInfo r=filtered.get(n);float x=56+(i%4)*96,y=219+(i/4)*rowH;icon(c,r,x,y,iconSize,true);button(new RectF(x-42,y-5,x+42,y+rowH-2),()->a.launch(r));}
        if(filtered.isEmpty())text(c,"Aucune application trouvée",200,300,17,0xFF223449);
        text(c,(a.page+1)+" / "+pages,200,h-151,12,0xFF56677D);
        glass(c,new RectF(126,h-136,274,h-102),18);text(c,"⌕  Rechercher",200,h-114,15,0xFF526076);button(new RectF(126,h-136,274,h-102),a::search);
        glass(c,new RectF(16,h-92,384,h-6),30);
        String[] categories={Intent.CATEGORY_APP_CONTACTS,Intent.CATEGORY_APP_MESSAGING,Intent.CATEGORY_APP_BROWSER,Intent.CATEGORY_APP_MUSIC};
        for(int i=0;i<4;i++){ResolveInfo item=dock(i,categories[i]);if(item!=null){icon(c,item,62+i*92,h-81,60,false);final ResolveInfo chosen=item;button(new RectF(25+i*92,h-92,100+i*92,h-6),()->a.launch(chosen));}else{final int slot=i;text(c,"+",62+i*92,h-38,32,0xFF547CA5);button(new RectF(25+i*92,h-92,100+i*92,h-6),()->a.dockPicker(slot));}}
    }
    ResolveInfo dock(int slot,String category){String saved=a.getPreferences(0).getString("dock"+slot,null);if(saved!=null)for(ResolveInfo r:a.apps)if(new ComponentName(r.activityInfo.packageName,r.activityInfo.name).flattenToString().equals(saved))return r;Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(category);ResolveInfo resolved=a.getPackageManager().resolveActivity(intent,0);if(resolved!=null)for(ResolveInfo r:a.apps)if(r.activityInfo.packageName.equals(resolved.activityInfo.packageName))return r;return slot<a.apps.size()?a.apps.get(slot):null;}
    void icon(Canvas c,ResolveInfo r,float x,float y,float size,boolean label){RectF tile=new RectF(x-size/2,y,x+size/2,y+size);glass(c,tile,size*.24f);Drawable drawable=r.loadIcon(a.getPackageManager());c.save();Path clip=new Path();clip.addRoundRect(tile,size*.24f,size*.24f,Path.Direction.CW);c.clipPath(clip);int inset=3;drawable.setBounds((int)tile.left+inset,(int)y+inset,(int)tile.right-inset,(int)(y+size)-inset);drawable.draw(c);p.setShader(new LinearGradient(0,y,0,y+size,new int[]{0x60FFFFFF,0x00FFFFFF},null,Shader.TileMode.CLAMP));c.drawRoundRect(tile,16,16,p);p.setShader(null);c.restore();if(label){String name=r.loadLabel(a.getPackageManager()).toString();p.setTextSize(12);if(p.measureText(name)>88)while(name.length()>1&&p.measureText(name+"…")>88)name=name.substring(0,name.length()-1);if(!name.equals(r.loadLabel(a.getPackageManager()).toString()))name+="…";text(c,name,x,y+size+19,12,0xFF24364E);}}
    void lock(Canvas c){int offset=a.getPreferences(0).getInt("clockY",30);float clockY=150+offset;
        text(c,"▣",200,31,24,0xFFFFFFFF);text(c,date("EEEE d MMMM"),200,67,19,0xF5FFFFFF);DepthRenderer.clock(c,date("HH:mm"),clockY,400);
        if(a.getPreferences(0).getBoolean("depth",true)){if(a.photo==null)DepthRenderer.landscape(c,400,h,true);else DepthRenderer.subject(c,a.photo,a.mask,400,h);}
        int battery=a.getSystemService(BatteryManager.class).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        String[] values={date("d MMM"),date("EEE"),battery>=0?battery+" %":"Batterie"};for(int i=0;i<3;i++){float x=113+i*87;glass(c,new RectF(x-34,clockY+36,x+34,clockY+104),34);text(c,values[i],x,clockY+77,14,0xFF253D52);}
        if(!a.editing){glass(c,new RectF(38,h-179,362,h-122),23);text(c,"Aperçu du verrouillage Luma",200,h-154,15,0xFF234157);text(c,"Appui long pour personnaliser",200,h-135,11,0xFF536980);
        glass(c,new RectF(22,h-77,79,h-20),28);text(c,"☼",50,h-40,26,0xFF20384C);button(new RectF(20,h-80,82,h-18),()->a.toast("Raccourci lampe : utilise le raccourci système sur l’écran verrouillé."));glass(c,new RectF(321,h-77,378,h-20),28);text(c,"◎",350,h-39,28,0xFF20384C);button(new RectF(320,h-80,380,h-18),()->{try{a.startActivity(new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));}catch(RuntimeException e){a.toast("Appareil photo indisponible.");}});text(c,"Balayer vers le haut pour revenir",200,h-36,11,0xFF263E52);}
    }
    void stroke(float x,float y,boolean down){if(a.photo==null||a.mask==null)return;RectF bounds=DepthRenderer.crop(a.photo,400,h);float ratio=a.photo.getWidth()/bounds.width();float mx=(x-bounds.left)*ratio,my=(y-bounds.top)*ratio;Paint brush=new Paint(3);brush.setColor(Color.WHITE);brush.setStrokeWidth(24*ratio);brush.setStrokeCap(Paint.Cap.ROUND);brush.setStyle(Paint.Style.STROKE);if(a.erase)brush.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));Canvas mc=new Canvas(a.mask);if(down){lastX=mx;lastY=my;}mc.drawLine(lastX,lastY,mx,my,brush);lastX=mx;lastY=my;invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX()/scale,y=e.getY()/scale;gestures.onTouchEvent(e);if(e.getAction()==MotionEvent.ACTION_DOWN){startX=x;startY=y;moved=false;if(a.editing&&y<h-85)stroke(x,y,true);return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){if(Math.abs(x-startX)+Math.abs(y-startY)>10)moved=true;if(a.editing&&y<h-85)stroke(x,y,false);return true;}if(e.getAction()==MotionEvent.ACTION_UP){performClick();if(a.editing&&startY<h-85)return true;if(moved){if(a.locked&&!a.editing&&startY-y>60){a.show(false);return true;}if(!a.locked&&Math.abs(x-startX)>50){a.page=Math.max(0,a.page+(x<startX?1:-1));invalidate();}return true;}for(Hit hit:hits)if(hit.rect.contains(x,y)){try{hit.action.run();}catch(RuntimeException ex){a.toast("Cette fonction n’est pas disponible sur ce téléphone.");}return true;}}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
}
