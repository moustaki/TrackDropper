package org.moustaki.trackdropper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Track extends OverlayItem {
    
    private double distance;
    private String url;
    
    public Track(GeoPoint gp, String title, String desc, double distance, String url) {
        super(gp, title, desc);
        this.distance = distance;
        this.url = url;
    }
    
    public double getDistance() {
        return this.distance;
    }
    
    public String getUrl() {
        return url;
    }
}
