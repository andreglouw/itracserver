package com.ikno.itracclient.mapping.widgets;

import com.ikno.dao.business.GeoPoint;

public class Position {
	public double latitude;
	public double longitude;
	public double altitude;
	public double range;
	public Position(GeoPoint geoPoint) {
		this.latitude = geoPoint.getLatitude();
		this.longitude = geoPoint.getLongitude();
		this.altitude = geoPoint.getAltitude();
	}
	public Position(double latitude,double longitude,double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	public Position(double latitude,double longitude,double altitude,double range) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.range = range;
	}
	public Position() {}
	public Position parsePosition(String value) throws Exception {
		String[] tokens = value.split(":");
		if (tokens.length == 2) {
			String doubles[] = tokens[1].split(",");
			if (doubles.length >= 3) {
				this.latitude = Double.parseDouble(doubles[0]);
				this.longitude = Double.parseDouble(doubles[1]);
				this.altitude = Double.parseDouble(doubles[2]);
				if (doubles.length == 4)
					this.range = Double.parseDouble(doubles[3]);
			}
			return this;
		}
		return null;
	}
	public String toString() {
		return String.format("lat: %.3f, lon: %.3f, alt: %.0f, range: %.0f",this.latitude,this.longitude,this.altitude,this.range);
	}
}
