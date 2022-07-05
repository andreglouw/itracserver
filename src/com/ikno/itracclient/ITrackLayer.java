package com.ikno.itracclient;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;

import java.awt.Color;

import com.ikno.itracclient.worldwind.DBTrack;

public interface ITrackLayer {
	public void setMarkerColor(Color color);
	public void setLineColor(Color color);
	public void moveLastPosition(MapPlace newPlace);
	public void redrawTrack(com.ikno.itracclient.Track track);
	public void zoomToTrack(com.ikno.itracclient.Track track, boolean animate);
	public void zoomToTrack(com.ikno.itracclient.Track track, double eyeElevation, boolean animate);
    public void removeTrack(com.ikno.itracclient.Track track);
	public void setHistory(MapPlace place);
	public void setHistory(MapPlace[] history);
}
