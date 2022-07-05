package com.ikno.itracclient.mapping.widgets;

import java.util.HashMap;
import java.util.logging.Logger;

import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.business.GeoArea.Centroid;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;

public class Feature {
	private static final Logger logger = Logging.getLogger(Feature.class.getName());

	public String id;
	public Position position;
	private GeoArea resolved = null;
	public boolean modified = false;
	public Feature() {}
	public Feature(String id,GeoPoint geoPoint) {
		this.id = id;
		this.resolved = geoPoint;
		this.position = new Position(geoPoint);
	}
	public Feature(String id,PolygonArea geoPoint) {
		this.id = id;
		this.resolved = geoPoint;
		Centroid centroid = geoPoint.getCentroid();
		if (centroid != null)
			this.position = new Position(centroid.getLatitude(),centroid.getLongitude(),centroid.getAltitude());
	}
	public Feature parsePlacemark(String value) throws Exception {
		String[] tokens = value.split(":");
		if (tokens.length == 2) {
			int endId = tokens[1].indexOf(",");
			if (endId <= 0)
				endId = tokens[1].length();
			id = tokens[1].substring(0,endId);
			if (endId < tokens[1].length()) {
				String doubles[] = tokens[1].substring(endId+1).split(",");
				if (doubles.length == 3) {
					position = new Position(Double.parseDouble(doubles[0]),Double.parseDouble(doubles[1]),Double.parseDouble(doubles[2]));
				}
			}
			return this;
		}
		return null;
	}
	public GeoArea resolved(HashMap<String,GeoArea> features) {
		if (resolved == null) {
			resolved = features.get(this.id);
			if (resolved == null) {
				try {
					resolved = DAO.localDAO().getGeoAreaById(Long.parseLong(id));
				} catch (Exception e) {
					logger.severe("Invalid Placemark id "+id+", unable to resolve");
				}
			}
		}
		return resolved;
	}
	public String toString() {
		return String.format("Id %s @ %s",this.id,this.position);
	}
}
