package org.moustaki.trackdropper;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class SelfOverlay extends ItemizedOverlay {
    
    private ArrayList<OverlayItem> selves = new ArrayList<OverlayItem>();
    private Context context;
    
    public SelfOverlay(Drawable d, Context c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }
    
    public void addOverlay(OverlayItem overlay) {
        this.selves.clear();
        this.selves.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return this.selves.get(i);
    }

    @Override
    public int size() {
        return this.selves.size();
    }
}
