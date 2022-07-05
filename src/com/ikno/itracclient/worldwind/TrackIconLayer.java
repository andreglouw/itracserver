package com.ikno.itracclient.worldwind;

import java.util.Iterator;

import com.ikno.itracclient.MapPlace;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackPointIterator;
import gov.nasa.worldwind.tracks.TrackPointIteratorImpl;

public class TrackIconLayer extends IconLayer {
	private java.util.Collection<Track> tracks;
	public TrackIconLayer() {}
	public TrackIconLayer(java.util.Collection<Track> tracks) {
		this.setTracks(tracks);
	}
    public TrackPointIterator iterator() {
    	return new TrackPointIteratorImpl(this.tracks);
    }
    public void setTracks(java.util.Collection<Track> tracks) {
    	this.removeAllIcons();
		this.tracks = tracks;
		Iterator<TrackPoint> trackPositions = this.iterator();
		for (int index = 0; trackPositions.hasNext(); index++) {
            DBTrackPoint tp = (DBTrackPoint)trackPositions.next();
            MapPlace place = tp.getPlace();
            try {
            	UserFacingIcon icon = new UserFacingIcon(place.getRotationImage(),Position.fromDegrees(place.getDegreesLatitude(), place.getDegreesLongitude(), place.getElevation()));
            	icon.setValue("place", place);
            	this.addIcon(icon);
            } catch (Exception e) {
            	
            }
        }
    }
    public Iterable<Track> getTracks() {
    	return this.tracks;
    }
}
