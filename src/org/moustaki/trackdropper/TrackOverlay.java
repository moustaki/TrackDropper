package org.moustaki.trackdropper;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TrackOverlay extends ItemizedOverlay {

    private ArrayList<Track> tracks = new ArrayList<Track>();
    private TrackDropper context;
    private final MediaPlayer mp = new MediaPlayer();
    
    public TrackOverlay(Drawable d, TrackDropper t) {
        super(boundCenterBottom(d));
        this.context = t;
        populate();
    }
    
    public void reset() {
        this.tracks.clear();
    }
    
    public void add(Track overlay) {
        this.tracks.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return this.tracks.get(i);
    }

    @Override
    public int size() {
        return this.tracks.size();
    }
    
    @Override
    protected boolean onTap(int index) {
        Track track = this.tracks.get(index);
        if (track.getDistance() < 20.0) {
            try {
                this.mp.setDataSource(track.getUrl());
                this.mp.prepare();
                this.mp.start();
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Playing...");
                dialog.setMessage(track.getTitle() + " - " + track.getSnippet());
                dialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(track.getTitle());
            dialog.setMessage(track.getSnippet());
            dialog.show();
        }
        return true;
    }
    
}
