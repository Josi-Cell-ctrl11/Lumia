package com.josias.luma;

import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Première base native de Luma. Aucun service de verrouillage n'est activé. */
public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<ResolveInfo> apps = new ArrayList<>();
    private FrameLayout root;
    private LinearLayout content;
    private GridLayout grid;
    private ImageView wallpaper;
    private TextView clock, date;
    private boolean preview;
    private final Runnable ticker = new Runnable() {
        @Override public void run() { updateClock(); handler.postDelayed(this, 1000); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(222,235,255));
        getWindow().setNavigationBarColor(Color.rgb(238,229,249));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        home();
    }
    @Override protected void onResume() { super.onResume(); handler.removeCallbacks(ticker); handler.post(ticker); if (!preview && grid != null) { loadApps(); renderApps(""); } }
    @Override protected void onPause() { handler.removeCallbacks(ticker); super.onPause(); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); home(); }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String value, int size) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size);
        view.setTextColor(Color.rgb(30,45,65)); view.setGravity(Gravity.CENTER); return view;
    }
    // Surface translucide légère. Le vrai flou du fond sera une étape visuelle ultérieure.
    private GradientDrawable glass() {
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xBFFFFFFF,0x55FFFFFF});
        shape.setCornerRadius(dp(28)); shape.setStroke(dp(1),0xDFFFFFFF); return shape;
    }
    private void base() {
        root = new FrameLayout(this);
        // Android 15 impose le dessin sous les barres : conserver des marges tactiles.
        if (Build.VERSION.SDK_INT >= 35) root.setOnApplyWindowInsetsListener((view,insets) -> {
            android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
            view.setPadding(bars.left,bars.top,bars.right,bars.bottom); return insets;
        });
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFFBFE5FF,0xFFE2DEFF,0xFFFFDFCE}));
        wallpaper = new ImageView(this); wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaper,new FrameLayout.LayoutParams(-1,-1)); restoreWallpaper();
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(18),dp(12),dp(18),dp(12));
        root.addView(content,new FrameLayout.LayoutParams(-1,-1));
        root.setOnLongClickListener(v -> { settings(); return true; });
        setContentView(root);
    }
    private void home() {
        preview = false; base();
        TextView heading = text("Luma    ·    Personnaliser",16); heading.setGravity(Gravity.END); heading.setOnClickListener(v -> settings());
        content.addView(heading,new LinearLayout.LayoutParams(-1,dp(42)));
        LinearLayout widget = new LinearLayout(this); widget.setOrientation(LinearLayout.VERTICAL); widget.setBackground(glass()); widget.setPadding(dp(8),dp(10),dp(8),dp(10));
        clock = text("",62); date = text("",17); widget.addView(clock); widget.addView(date);
        content.addView(widget,new LinearLayout.LayoutParams(-1,dp(156))); updateClock();
        EditText search = new EditText(this); search.setSingleLine(true); search.setTextSize(16); search.setHint("Rechercher une application"); search.setPadding(dp(20),0,dp(20),0); search.setBackground(glass());
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1,dp(48)); searchParams.setMargins(0,dp(16),0,dp(8)); content.addView(search,searchParams);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(false); grid = new GridLayout(this); grid.setColumnCount(4); scroll.addView(grid);
        content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        loadApps(); renderApps("");
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { renderApps(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        TextView bottom = text("Fond d’écran     ·     Aperçu du verrouillage",14); bottom.setBackground(glass()); bottom.setOnClickListener(v -> settings());
        content.addView(bottom,new LinearLayout.LayoutParams(-1,dp(54)));
    }
    private void loadApps() {
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        apps.clear(); apps.addAll(getPackageManager().queryIntentActivities(launcher,0));
        apps.removeIf(info -> info.activityInfo.packageName.equals(getPackageName()));
        apps.sort(new ResolveInfo.DisplayNameComparator(getPackageManager()));
    }
    private void renderApps(String query) {
        grid.removeAllViews(); String term = query.toLowerCase(Locale.ROOT).trim();
        for (ResolveInfo info : apps) {
            String label = info.loadLabel(getPackageManager()).toString(); if (!label.toLowerCase(Locale.ROOT).contains(term)) continue;
            LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(3),dp(10),dp(3),dp(8));
            ImageView icon = new ImageView(this); icon.setImageDrawable(info.loadIcon(getPackageManager())); icon.setBackground(glass()); icon.setPadding(dp(7),dp(7),dp(7),dp(7)); tile.addView(icon,new LinearLayout.LayoutParams(dp(60),dp(60)));
            TextView name = text(label,12); name.setMaxLines(1); name.setEllipsize(TextUtils.TruncateAt.END); tile.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(); params.width=0; params.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); grid.addView(tile,params);
            tile.setOnClickListener(v -> { try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(new ComponentName(info.activityInfo.packageName,info.activityInfo.name))); } catch (RuntimeException e) { Toast.makeText(this,"Cette application ne peut pas être ouverte.",Toast.LENGTH_SHORT).show(); } });
            tile.setOnLongClickListener(v -> { startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+info.activityInfo.packageName))); return true; });
        }
        if (grid.getChildCount()==0) { TextView empty=text("Aucune application trouvée",16); grid.addView(empty); }
    }
    private void updateClock() {
        if(clock==null || date==null) return; Date now=new Date();
        clock.setText(new SimpleDateFormat("HH:mm",Locale.FRANCE).format(now)); date.setText(new SimpleDateFormat("EEEE d MMMM",Locale.FRANCE).format(now));
    }
    private void settings() {
        new AlertDialog.Builder(this).setTitle("Luma Launcher").setItems(new String[]{"Choisir mon fond d’écran","Retirer le fond personnalisé","Aperçu du verrouillage","Définir comme écran d’accueil"},(dialog,index) -> {
            if(index==0) { Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE); pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(pick,10); }
            else if(index==1) { getPreferences(0).edit().remove("wallpaper").apply(); home(); }
            else if(index==2) lockPreview();
            else { RoleManager roles=getSystemService(RoleManager.class); if(roles!=null && roles.isRoleAvailable(RoleManager.ROLE_HOME) && !roles.isRoleHeld(RoleManager.ROLE_HOME)) startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME),11); else startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS)); }
        }).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==10 && result==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            try { getContentResolver().takePersistableUriPermission(uri,data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION); getPreferences(0).edit().putString("wallpaper",uri.toString()).apply(); home(); }
            catch(RuntimeException e) { Toast.makeText(this,"Impossible de conserver cette photo. Choisis une autre image.",Toast.LENGTH_LONG).show(); }
        }
    }
    // Décodage redimensionné pour éviter de charger une photo pleine résolution en mémoire.
    private void restoreWallpaper() {
        String saved=getPreferences(0).getString("wallpaper",null); if(saved==null) return;
        try { ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(),Uri.parse(saved));
            Bitmap bitmap=ImageDecoder.decodeBitmap(source,(decoder,info,src) -> { int width=info.getSize().getWidth(),height=info.getSize().getHeight(); float scale=Math.min(1f,1920f/Math.max(width,height)); decoder.setTargetSize(Math.max(1,(int)(width*scale)),Math.max(1,(int)(height*scale))); });
            wallpaper.setImageBitmap(bitmap);
        } catch(Exception e) { Toast.makeText(this,"Fond indisponible : sélectionne à nouveau ta photo.",Toast.LENGTH_SHORT).show(); }
    }
    private void lockPreview() {
        preview=true; base();
        TextView note=text("APERÇU · Le verrouillage Infinix reste actif",13); note.setBackground(glass()); content.addView(note,new LinearLayout.LayoutParams(-1,dp(44)));
        date=text("",19); clock=text("",90); content.addView(date); content.addView(clock); updateClock();
        TextView info=text("L’effet de profondeur sera ajouté\navec le détourage du sujet de ta photo.",15); info.setPadding(dp(16),dp(16),dp(16),dp(16)); info.setBackground(glass());
        LinearLayout.LayoutParams space=new LinearLayout.LayoutParams(-1,0,1); content.addView(new View(this),space); content.addView(info);
        TextView back=text("Revenir à l’accueil",17); back.setOnClickListener(v -> home()); content.addView(back,new LinearLayout.LayoutParams(-1,dp(64)));
    }
    @Override public void onBackPressed() { home(); }
}
