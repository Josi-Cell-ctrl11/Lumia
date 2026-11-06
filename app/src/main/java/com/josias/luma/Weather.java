package com.josias.luma;

import android.content.Context;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Météo publique de Cotonou, sans permission de localisation ni clé privée. */
final class Weather {
    static void refresh(MainActivity a){a.worker.execute(()->{HttpURLConnection connection=null;try{connection=(HttpURLConnection)new URL("https://api.open-meteo.com/v1/forecast?latitude=6.3654&longitude=2.4183&current=temperature_2m,weather_code&timezone=Africa%2FPorto-Novo").openConnection();connection.setConnectTimeout(8000);connection.setReadTimeout(8000);String data;try(InputStream in=connection.getInputStream()){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);data=bytes.toString("UTF-8");}JSONObject current=new JSONObject(data).getJSONObject("current");int temperature=(int)Math.round(current.getDouble("temperature_2m"));int code=current.getInt("weather_code");a.getSharedPreferences("weather",Context.MODE_PRIVATE).edit().putInt("temperature",temperature).putInt("code",code).putLong("updated",System.currentTimeMillis()).apply();a.runOnUiThread(()->{if(a.scene!=null)a.scene.invalidate();});}catch(Exception ignored){}finally{if(connection!=null)connection.disconnect();}});}
    static String label(int code){if(code==0)return "Ensoleillé";if(code<=3)return "Éclaircies";if(code<=48)return "Brume";if(code<=67)return "Pluie";if(code<=77)return "Neige";if(code<=82)return "Averses";return "Orage";}
}
