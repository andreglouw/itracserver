package com.ikno.itracclient;

import com.ikno.dao.business.Asset;
import com.ikno.dao.persistance.PersistantObject;

public class Track {
	private String trackName;
	private ITrackLayer trackLayer;
	private boolean enabled;
	
	public Track(String trackName, ITrackLayer trackLayer, boolean enabled) {
		this.trackName = trackName;
		this.trackLayer = trackLayer;
		this.enabled = enabled;
	}
	public String getTrackName() {
		return trackName;
	}
	public void setTrackName(String layerName) {
		this.trackName = layerName;
	}
	public ITrackLayer getTrackLayer() {
		return trackLayer;
	}
	public void setTrackLayer(ITrackLayer reference) {
		this.trackLayer = reference;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String toString() {
		return trackName;
	}
	public void remove(IMappingView mappingView) {
		this.trackLayer.removeTrack(this);
	}
	public void redraw(IMappingView mappingView) {
		this.trackLayer.redrawTrack(this);
	}
	public void zoomToTrack(IMappingView mappingView, double eyeElevation, boolean animate) {
		this.trackLayer.zoomToTrack(this,eyeElevation,animate);
	}
	public void zoomToTrack(IMappingView mappingView, boolean animate) {
		this.trackLayer.zoomToTrack(this,animate);
	}
	public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
    	return (((Track)o).getTrackName() == this.getTrackName());
    }
}
