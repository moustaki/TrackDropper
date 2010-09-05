package org.moustaki.trackdropper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class TrackDropperLocationListener implements LocationListener {
    
	private MapController mc;
	private MapView mv;
	private GeoPoint currentLocation;
	private TrackDropper context;
	private boolean firstUpdateReceived = false;
	private SelfOverlay selfOverlay;
	
	public TrackDropperLocationListener(TrackDropper context, SelfOverlay selfOverlay) {
		this.mc = context.getMapController();
		this.mv = context.getMapView();
		this.context = context;
		this.selfOverlay = selfOverlay;
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		if (loc != null) {
            GeoPoint p = new GeoPoint(
                    (int) (loc.getLatitude() * 1E6), 
                    (int) (loc.getLongitude() * 1E6)
            );
            // only animate to current location for the first update
            this.currentLocation = p;
            if (!this.firstUpdateReceived) {
                this.mc.animateTo(p);
                this.firstUpdateReceived = true;
            }
            // this.mc.setZoom(16);       
            this.mv.invalidate();
            
            OverlayItem self = new OverlayItem(this.currentLocation, "", "");
            this.selfOverlay.addOverlay(self);
            
            Thread update = new Thread() {
                public void run() {
                    context.updateNearbyTracks();
                }
            };
            update.start();
		}
	}
	
	public GeoPoint getCurrentLocation() {
	    return this.currentLocation;
	}
	
	@Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
	
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	// TODO Auto-generated method stub	
    }
}
