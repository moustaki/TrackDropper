package org.moustaki.trackdropper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;

import org.moustaki.trackdropper.R;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TrackDropper extends MapActivity {
    
    private static final int MENU_ADD_OBJECTIVES = 0;
    
    private MapController mc;
    private MapView mv;
    private LocationManager lm;
    private TrackDropperLocationListener ll;
    
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
        return true;
     }
}
