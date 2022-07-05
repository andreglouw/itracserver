package com.ikno.itracclient.worldwind;

import java.util.ArrayList;
import java.util.List;

import com.ikno.itracclient.MapPlace;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackSegment;

public class DBTrack implements Track {

	private String name;
    private List<TrackSegment> segments = new ArrayList<TrackSegment>();
	
	public DBTrack(String trackName, MapPlace[] places) {
		this.name = trackName;
		DBTrackSegment segment = new DBTrackSegment(places);
		this.segments.add(segment);
	}
	public DBTrack(String trackName, MapPlace place) {
		this.name = trackName;
		DBTrackSegment segment = new DBTrackSegment(place);
		this.segments.add(segment);
	}
	
    public List<TrackSegment> getSegments() {
        return segments;
    }

    public String getName() {
        return name;
    }

    public TrackPoint getFirstPoint() {
		if (this.segments.size() > 0)
			return ((DBTrackSegment)this.segments.get(0)).getFirstPoint();
		return null;
    }
    
    public TrackPoint getLastPoint() {
		if (this.segments.size() > 0)
			return ((DBTrackSegment)this.segments.get(this.segments.size()-1)).getLastPoint();
		return null;
    }
    
    public int getNumPoints() {
        if (this.segments == null)
            return 0;
        int numPoints = 0;
        for (TrackSegment segment : this.segments) {
            for (TrackPoint point : segment.getPoints()) {
                ++numPoints;
            }
        }
        return numPoints;
    }

    public List<Position> getPositions() {
		ArrayList<Position> positions = new ArrayList<Position>();
		for (TrackSegment segment : this.segments) {
			for (TrackPoint point : segment.getPoints()) {
				positions.add(point.getPosition());
			}
		}
		return positions;
    }
    public MapPlace getPlace(int index) {
		int cnt = 0;
		for (TrackSegment segment : this.segments) {
			for (TrackPoint point : segment.getPoints()) {
				if (cnt++ == index)
					return ((DBTrackPoint)point).getPlace();
			}
		}
		return null;
    }
}
