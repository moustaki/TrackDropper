package org.moustaki.trackdropper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.moustaki.trackdropper.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.provider.MediaStore.Audio.Media;

public class TrackDropper extends MapActivity {
    
    private static final int MENU_DROP_TRACK = 0;
    private static final int MENU_QUIT = 1;
    
    private MapController mc;
    private MapView mv;
    private LocationManager lm;
    private TrackDropperLocationListener ll;
    private TrackOverlay trackOverlay;
    private boolean running;
    private long lastUpdate = 0;
    
    private String base = "http://piracy.heroku.com"; //"http://10.195.80.235:6666";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Setting default zoom level
        this.mv = (MapView) findViewById(R.id.mapview);
        this.mv.setBuiltInZoomControls(true);
        this.mc = this.mv.getController();
        this.mc.setZoom(16);
        
        // Setting location tracking
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Drawable selfDrawable = this.getResources().getDrawable(R.drawable.self);
        SelfOverlay selfOverlay = new SelfOverlay(selfDrawable, this);
        this.mv.getOverlays().add(selfOverlay);
        this.ll = new TrackDropperLocationListener(this, selfOverlay);
        this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        
        // Setting track overlay
        Drawable trackDrawable = this.getResources().getDrawable(R.drawable.music);
        this.trackOverlay = new TrackOverlay(trackDrawable, this);
        this.mv.getOverlays().add(this.trackOverlay);
        
        this.running = true;
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public MapView getMapView() {
        return this.mv; 
     }
     
     public MapController getMapController() {
         return this.mc;
     }
     
     public TrackDropperLocationListener getLocationListener() {
         return this.ll;
     }
     
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         menu.add(0, MENU_DROP_TRACK, 0, "Drop a track!");
         menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_QUIT:
                 this.running = false;
                 this.finish();
                 return true;
             case MENU_DROP_TRACK:
                 this.chooseArtist();
                 return true;
         }
         return false;
     }
     
     private boolean chooseArtist() {
         String[] projection = new String[] {
                 Media._ID,
                 Media.TITLE,
                 Media.DATA,
                 Media.ARTIST,
                 Media.ALBUM,
              };
         Uri music = Media.EXTERNAL_CONTENT_URI;
         Cursor cursor = managedQuery(music, projection, null, null, Media.TITLE + " ASC");
         ArrayList<String> titles = new ArrayList<String>();
         ArrayList<String> trackTitles = new ArrayList<String>();
         ArrayList<String> artists = new ArrayList<String>();
         if (cursor.moveToFirst()) {
             int titleColumn = cursor.getColumnIndex(Media.TITLE);
             int artistColumn = cursor.getColumnIndex(Media.ARTIST);
             do {
                 titles.add(cursor.getString(artistColumn) + " - " + cursor.getString(titleColumn));
                 trackTitles.add(cursor.getString(titleColumn));
                 artists.add(cursor.getString(artistColumn));
             } while (cursor.moveToNext());
         }
         final ArrayList<String> finaltitles =  trackTitles;
         final ArrayList<String> finalartists = artists;
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Pick a track:");
         String[] s = (String[]) titles.toArray(new String[titles.size()]);
         builder.setItems(s, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 String title = finaltitles.get(item);
                 String artist = finalartists.get(item);
                 GeoPoint point = getLocationListener().getCurrentLocation();
                 HashMap<String,String> data = new HashMap<String,String>();
                 
                 data.put("lat", ""+point.getLatitudeE6() / 1000000.0);
                 data.put("lng", ""+point.getLongitudeE6()/ 1000000.0);
                 data.put("artist_name", artist);
                 data.put("track_name", title);
                 final HashMap<String,String> dataToPost = data;
                 Thread post = new Thread() {
                     public void run() {
                         postJSON("/tracks", dataToPost);
                     }
                 };
                 post.start();
                 Toast.makeText(getTrackDropper(), "Posted!", Toast.LENGTH_SHORT).show();
                 return;
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
         return true;
     }
     
     public void updateNearbyTracks() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 1000 * 10) { 
            return;
        }
        GeoPoint point = getLocationListener().getCurrentLocation();
        JSONObject response = getJSON("/tracks.json?lat="+point.getLatitudeE6() / 1000000.0+"&lng="+point.getLongitudeE6()/ 1000000.0);
        trackOverlay.reset();
        try {
            for (int i = 0; i < response.getJSONArray("tracks").length(); i++) {
                JSONObject track = response.getJSONArray("tracks").getJSONObject(i);
                double lat = track.optDouble("lat");
                double lng = track.optDouble("lng");
                Double lat2 = lat * 1E6;
                Double lng2 = lng * 1E6;
                GeoPoint tp = new GeoPoint(lat2.intValue(), lng2.intValue());
                String trackName = track.getString("track_name");
                String artistName = track.getString("artist_name");
                String url = track.getString("url");
                double distance = track.optDouble("distance") * 1000;
                Track trackItem = new Track(tp, artistName, trackName, distance, url);
                trackOverlay.add(trackItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
     }
     
     public TrackDropper getTrackDropper() {
         return this;
     }
     
     private JSONObject getJSON(String path) {
         try {
             URL url = new URL(this.base + path);
             URLConnection connection = url.openConnection();
             String line;
             StringBuilder builder = new StringBuilder();
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             while((line = reader.readLine()) != null) {
                 builder.append(line);
             }
             String response = builder.toString();
             return new JSONObject(response);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     private JSONObject postJSON(String path, HashMap<String,String> data) {
         try {
             String post = "";
             for (String key : data.keySet()) {
                 if (post != "") {
                     post += "&";
                 }
                 post += URLEncoder.encode(key, "UTF-8") + '=' + URLEncoder.encode(data.get(key), "UTF-8");
             }
             URL url = new URL(this.base + path);
             URLConnection connection = url.openConnection();
             connection.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream()); 
             wr.write(post);
             wr.flush();
             
             String line;
             StringBuilder builder = new StringBuilder();
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             while((line = reader.readLine()) != null) {
                 builder.append(line);
             }
             String response = builder.toString();
             return new JSONObject(response);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
}
