package com.ikno.itracclient.worldwind;

import java.util.ArrayList;
import java.util.List;

import com.ikno.itracclient.MapPlace;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackSegment;

public class DBTrackSegment implements TrackSegment {

    private List<TrackPoint> points = new ArrayList<TrackPoint>();

    public DBTrackSegment(MapPlace[] places) {
    	for (MapPlace place : places) {
    		this.points.add(new DBTrackPoint(place));
    	}
    }
    public DBTrackSegment(MapPlace place) {
    	this.points.add(new DBTrackPoint(place));
    }

    public TrackPoint getFirstPoint() {
		if (this.points.size() > 0)
			return this.points.get(0);
		return null;
    }
    public TrackPoint getLastPoint() {
		if (this.points.size() > 0)
			return this.points.get(this.points.size()-1);
		return null;
    }
    public List<TrackPoint> getPoints() {
        return this.points;
    }
	public void addPoint(TrackPoint point) {
		this.points.add(point);
	}
	public List<Position> getPositions() {
		ArrayList<Position> positions = new ArrayList<Position>();
		for (TrackPoint point : this.points) {
			positions.add(point.getPosition());
		}
		return positions;
	}

}
