package com.josias.luma;

import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;
import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.*;

/** Luma : accueil natif, éditeur de masque et prévisualisation du verrouillage. */
public class MainActivity extends Activity {
    final ArrayList<ResolveInfo> apps=new ArrayList<>();
    final ExecutorService worker=Executors.newSingleThreadExecutor();
    final Handler handler=new Handler(Looper.getMainLooper());
    SceneView scene;
    Bitmap photo,mask;
    boolean locked=false,editing=false,erase=false;
    int page=0;
    String query="";
    EditText search;
    FrameLayout container;
    private final Runnable tick=new Runnable(){public void run(){if(scene!=null)scene.invalidate();handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.TRANSPARENT);getWindow().setNavigationBarColor(Color.TRANSPARENT);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);loadApps();loadImages();show(false);Weather.refresh(this);}
    @Override protected void onResume(){super.onResume();loadApps();handler.removeCallbacks(tick);handler.post(tick);if(scene!=null)scene.invalidate();}
    @Override protected void onPause(){handler.removeCallbacks(tick);super.onPause();}
    @Override protected void onDestroy(){worker.shutdown();super.onDestroy();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);editing=false;page=0;query="";show(false);}
    void loadApps(){apps.clear();apps.addAll(getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0));apps.removeIf(r->r.activityInfo.packageName.equals(getPackageName()));apps.sort(new ResolveInfo.DisplayNameComparator(getPackageManager()));}
    void loadImages(){photo=BitmapFactory.decodeFile(new File(getFilesDir(),"wallpaper.png").toString());mask=BitmapFactory.decodeFile(new File(getFilesDir(),"mask.png").toString());if(mask!=null)mask=mask.copy(Bitmap.Config.ARGB_8888,true);}
    void show(boolean lock){locked=lock;container=new FrameLayout(this);scene=new SceneView(this);container.addView(scene);if(Build.VERSION.SDK_INT>=35)container.setOnApplyWindowInsetsListener((v,i)->{Insets b=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(b.left,b.top,b.right,b.bottom);return i;});setContentView(container);}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    void launch(ResolveInfo r){try{startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(new ComponentName(r.activityInfo.packageName,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(RuntimeException e){toast("Cette application ne peut pas être ouverte.");}}
    void search(){if(search!=null&&search.getParent()!=null)return;search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);search.setHint("Rechercher une application");search.setBackgroundColor(0xEEFFFFFF);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,(int)(56*getResources().getDisplayMetrics().density),Gravity.TOP);container.addView(search,lp);search.requestFocus();search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int n){}public void afterTextChanged(Editable e){}public void onTextChanged(CharSequence s,int a,int b,int c){query=s.toString();page=0;scene.invalidate();}});((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(search,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);}
    void settings(){new AlertDialog.Builder(this).setTitle("Luma · Personnalisation").setItems(new String[]{"Choisir une photo","Détourage automatique · personnes","Retoucher la profondeur au pinceau","Régler la hauteur de l’horloge","Afficher / masquer la profondeur","Aperçu du verrouillage","Activer le fond animé Luma","Choisir les 4 applis du dock","Définir Luma comme accueil","Revenir au fond Luma"},(d,n)->{
        switch(n){case 0:pick();break;case 1:segment();break;case 2:editor();break;case 3:clockPosition();break;case 4:getPreferences(0).edit().putBoolean("depth",!getPreferences(0).getBoolean("depth",true)).apply();scene.invalidate();break;case 5:show(true);break;case 6:new AlertDialog.Builder(this).setMessage("Dans le sélecteur Android, applique Luma à l’écran verrouillé si cette option est disponible. XOS peut conserver sa propre horloge par-dessus ; Luma ne peut pas la masquer. Ton code et ton empreinte restent ceux du système.").setPositiveButton("Ouvrir",(a,b)->{try{startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,DepthWallpaper.class)));}catch(RuntimeException e){toast("Le sélecteur de fond animé n’est pas disponible sur cet appareil.");}}).setNegativeButton("Annuler",null).show();break;case 7:dockPicker(0);break;case 8:RoleManager r=getSystemService(RoleManager.class);if(r!=null&&r.isRoleAvailable(RoleManager.ROLE_HOME)&&!r.isRoleHeld(RoleManager.ROLE_HOME))startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_HOME),11);else startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));break;case 9:photo=null;mask=null;new File(getFilesDir(),"wallpaper.png").delete();new File(getFilesDir(),"mask.png").delete();show(locked);break;}
    }).show();}
    void dockPicker(int slot){String[] names=new String[apps.size()];for(int i=0;i<names.length;i++)names[i]=apps.get(i).loadLabel(getPackageManager()).toString();new AlertDialog.Builder(this).setTitle("Dock · emplacement "+(slot+1)).setItems(names,(d,n)->{ResolveInfo a=apps.get(n);getPreferences(0).edit().putString("dock"+slot,new ComponentName(a.activityInfo.packageName,a.activityInfo.name).flattenToString()).apply();if(slot<3)dockPicker(slot+1);else scene.invalidate();}).show();}
    void clockPosition(){SeekBar bar=new SeekBar(this);bar.setMax(180);bar.setProgress(getPreferences(0).getInt("clockY",30));new AlertDialog.Builder(this).setTitle("Hauteur de l’horloge").setView(bar).setPositiveButton("Enregistrer",(d,n)->{getPreferences(0).edit().putInt("clockY",bar.getProgress()).apply();show(true);}).show();}
    void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req!=10||result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();toast("Préparation de la photo…");worker.execute(()->{try{Bitmap b=ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(),uri),(decoder,info,s)->{int w=info.getSize().getWidth(),h=info.getSize().getHeight();float ratio=Math.min(1f,1800f/Math.max(w,h));decoder.setTargetSize(Math.max(1,(int)(w*ratio)),Math.max(1,(int)(h*ratio)));decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);});save(b,"wallpaper.png");new File(getFilesDir(),"mask.png").delete();runOnUiThread(()->{photo=b;mask=null;show(locked);new AlertDialog.Builder(this).setMessage("Photo ajoutée. Pour placer le sujet devant l’heure, choisis le détourage automatique d’une personne ou peins le sujet d’un paysage.").setPositiveButton("Personne · automatique",(d,n)->segment()).setNeutralButton("Paysage · pinceau",(d,n)->editor()).setNegativeButton("Plus tard",null).show();});}catch(Exception e){runOnUiThread(()->toast("Impossible de lire cette photo."));}});}
    void save(Bitmap b,String name)throws IOException{File tmp=new File(getFilesDir(),name+".tmp");try(FileOutputStream out=new FileOutputStream(tmp)){if(!b.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException();}if(!tmp.renameTo(new File(getFilesDir(),name)))throw new IOException();}
    void segment(){if(photo==null){toast("Choisis d’abord une photo de personne.");return;}Bitmap target=photo;toast("Détourage de la personne sur le téléphone…");Segmenter detector=Segmentation.getClient(new SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build());detector.process(InputImage.fromBitmap(target,0)).addOnSuccessListener(result->{worker.execute(()->{try{int w=result.getWidth(),h=result.getHeight();FloatBuffer buffer=result.getBuffer().asFloatBuffer();int[] px=new int[w*h];int count=0;for(int i=0;i<px.length;i++){float f=buffer.get();int alpha=(int)(255*Math.max(0,Math.min(1,(f-.35f)/.45f)));px[i]=(alpha<<24)|0xFFFFFF;if(alpha>128)count++;}if(count<px.length/100)throw new IOException("Aucune personne détectée");Bitmap m=Bitmap.createBitmap(px,w,h,Bitmap.Config.ARGB_8888);Bitmap scaled=Bitmap.createScaledBitmap(m,target.getWidth(),target.getHeight(),true).copy(Bitmap.Config.ARGB_8888,true);save(scaled,"mask.png");runOnUiThread(()->{if(photo==target){mask=scaled;getPreferences(0).edit().putBoolean("depth",true).apply();show(true);toast("Profondeur activée. Tu peux retoucher les contours au pinceau.");}});}catch(Exception e){runOnUiThread(()->toast("Détourage insuffisant : utilise le pinceau pour sélectionner le sujet."));}});}).addOnFailureListener(e->toast("Le détourage a échoué. Le pinceau reste disponible.")).addOnCompleteListener(t->detector.close());}
    void editor(){if(photo==null){toast("Choisis d’abord une photo.");return;}if(mask==null)mask=Bitmap.createBitmap(photo.getWidth(),photo.getHeight(),Bitmap.Config.ARGB_8888);editing=true;show(true);toast("Peins le sujet qui doit passer devant l’heure. Les boutons en bas permettent de gommer et d’enregistrer.");}
    void finishMask(){try{save(mask,"mask.png");getPreferences(0).edit().putBoolean("depth",true).apply();editing=false;show(true);}catch(IOException e){toast("Impossible d’enregistrer le détourage.");}}
    @Override public void onBackPressed(){if(editing)new AlertDialog.Builder(this).setMessage("Enregistrer le détourage ?").setPositiveButton("Enregistrer",(d,n)->finishMask()).setNegativeButton("Annuler les retouches",(d,n)->{editing=false;loadImages();show(false);}).show();else if(search!=null&&search.getParent()!=null){container.removeView(search);query="";scene.invalidate();}else{page=0;show(false);}}
}
