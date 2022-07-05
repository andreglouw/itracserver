package com.ikno.itracclient.worldwind;

import java.text.SimpleDateFormat;

import com.ikno.itracclient.MapPlace;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;

public class DBTrackPoint implements TrackPoint {

	private MapPlace place;
    private String time;

    public DBTrackPoint(MapPlace place) {
    	this(place,new SimpleDateFormat("yy/MM/dd\nHH:mm").format(place.getTimestamp()));
    }
    public DBTrackPoint(MapPlace place, String time) {
    	this.place = place;
    	this.time = time;
    }
	public double getElevation() {
		return place.getAltitude();
	}
	public void setElevation(double elevation) {
		place.setAltitude(elevation);
	}
	public double getLatitude() {
		return place.getDegreesLatitude();
	}
	public void setLatitude(double latitude) {
		place.setDegreesLatitude(latitude);
	}

	public double getLongitude() {
		return place.getDegreesLongitude();
	}
	public void setLongitude(double longitude) {
		place.setDegreesLongitude(longitude);
	}

	public Position getPosition() {
        return Position.fromDegrees(this.getLatitude(), this.getLongitude(), this.getElevation());
	}
	public void setPosition(Position position) {
		place.setAltitude(position.getElevation());
		place.setDegreesLatitude(position.getLatitude().getDegrees());
		place.setDegreesLongitude(position.getLongitude().getDegrees());
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	public MapPlace getPlace() {
		return place;
	}
	public void setPlace(MapPlace place) {
		this.place = place;
	}
}
