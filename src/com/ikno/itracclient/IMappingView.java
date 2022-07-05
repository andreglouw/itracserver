package com.ikno.itracclient;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.PointIncident;

public interface IMappingView {

	public String getViewId();
	public void setSubID(String subId);
	public HashMap<String,GeoArea> getAvailableFeatures();
	public MapPlace anchorAltitude(MapPlace place);
	public boolean eyeElevationLowerThan(double eyeElevation);
	public boolean isVisible(double latitude, double longitude,double altitude);
	public void gotoPlace(double latitude, double longitude, double eyeElevation, boolean adjustIfAbove, boolean animate);
	public void gotoPlace(double latitude, double longitude, double eyeElevation, boolean animate);
	public void gotoPlace(double latitude, double longitude, boolean animate);
	public void AddAssetWithHistory(Asset asset, PointIncident[] history, boolean zoomTo);
	public void GotoIncident(PointIncident place, double eyeElevation, boolean adjustIfAbove, boolean animate);
	public Track ShowLineTrack(PointIncident[] incidents, String trackName, Color pinColor, Color trackColor, double eyeElevation, boolean adjustEyeLevel, boolean followsTerrain, boolean animate);
	public List<MapLayer> layerList();
	public void addLayer(MapLayer mapLayer);
	public void removeLayer(MapLayer mapLayer);
	public List<Track> trackList();
	public void changeLayerVisibility(MapLayer layer, boolean visible);
	public void addTrackListener(ITrackListener listener);
	public void removeTrackListener(ITrackListener listener);
    public void addTrack(Track track);
	public GeoArea[] getGeoAreas();
	public void showGeoArea(GeoArea geoArea);
	public void removeGeoArea(GeoArea geoArea);
	public void geoAreaAdded(GeoArea geoArea);
	public void setSelectedAsset(Asset asset);
	public void removeAssetTrack(IMappingAssetTracker assetTrack);
	public void maximizeInfoBar();
	public void minimizeInfoBar();
	
}
