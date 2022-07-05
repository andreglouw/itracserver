package com.ikno.itracclient.worldwind;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.TrackMarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackSegment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ikno.dao.business.PointIncident;
import com.ikno.itracclient.ITrackLayer;
import com.ikno.itracclient.MapPlace;

public class TrackMarkerLineLayer extends RenderableLayer implements SelectListener, ITrackLayer {
	private TrackIconLayer historyMarkerLayer = null;
	private TrackMarkerLayer currentMarkerLayer = null;
	private List<MapPlace> history = new ArrayList<MapPlace>();
	private MapPlace lastPosition;
	private List<Position> positions = new ArrayList<Position>();
	private Polyline polyline = new Polyline();
	private ActiveWorldWindView worldWindView = null;
	private boolean followsTerrain = true;
	private PickedObject lastPicked = null;
	private GlobeAnnotation annotation = null;
	public com.ikno.itracclient.Track itrack = null;
	UserFacingIcon icon = null;
	private String trackName;
	
	public TrackMarkerLineLayer(ActiveWorldWindView worldWindView, String trackName, String shortName, MapPlace lastPosition, MapPlace[] history) {
		super();
		this.worldWindView = worldWindView;
		this.trackName = trackName;
		for (MapPlace place : history) {
			Position position = Position.fromDegrees(place.getDegreesLatitude(), place.getDegreesLongitude(), place.getAltitude());
			double globeEle = this.worldWindView.worldWindWidget.getGlobeElevation(position);
			double posEle = position.getElevation();
			double diff = java.lang.Math.abs(posEle-globeEle);
			if (diff > 50.0)
				followsTerrain = false;
			positions.add(new Position(position.getLatLon(),posEle));
			this.history.add(place);
		}
		
		polyline.setPositions(positions);
		polyline.setFollowTerrain(followsTerrain);
        if (followsTerrain)
        	polyline.setNumSubsegments(30);
        else
        	polyline.setNumSubsegments(1);
		this.addRenderable(polyline);
		this.historyMarkerLayer = new TrackIconLayer(Arrays.asList((Track)new DBTrack(trackName,history)));
		this.lastPosition = lastPosition;
		if (lastPosition != null) {
			this.currentMarkerLayer = new TrackMarkerLayer(Arrays.asList((Track)new DBTrack(trackName,lastPosition)));
			try {
		    	icon = new UserFacingIcon(lastPosition.getImage(),Position.fromDegrees(lastPosition.getDegreesLatitude(), lastPosition.getDegreesLongitude(), lastPosition.getElevation()));
		    	icon.setValue("place", lastPosition);
			} catch (Exception e) {
				icon = null;
			}
			if (icon != null) {
				this.currentMarkerLayer.setMarkerPixels(0);
				this.currentMarkerLayer.setMinMarkerSize(0);
				icon.setToolTipText(trackName);
				icon.setToolTipFont(Font.decode("Verdana-10"));
				icon.setToolTipTextColor(Color.BLACK);
				icon.setToolTipOffset(new Point(15,25));
				icon.setShowToolTip(true);
				icon.setHighlightScale(1.5);
				icon.setValue("place", lastPosition);
				currentMarkerLayer.setIcon(icon);
			}
		}
		this.worldWindView.worldWindWidget.addSelectListener(this);
		this.itrack = new com.ikno.itracclient.Track(trackName, this, true);
		this.worldWindView.addTrack(itrack);
		this.addLayers();
	}
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.historyMarkerLayer.setEnabled(enabled);
		if (currentMarkerLayer != null)
			this.currentMarkerLayer.setEnabled(enabled);
	}
	public List<Track> getHistoryTrack() {
		return Arrays.asList((Track)new DBTrack(trackName,history.toArray(new MapPlace[]{})));
	}
	public List<Track> getLastPositionTrack() {
		return Arrays.asList((Track)new DBTrack(trackName,lastPosition));
	}
	public void setFollowsTerrain(boolean followsTerrain) {
		this.followsTerrain = followsTerrain;
	}
	public void setLineWidth(double lineWidth) {
		polyline.setLineWidth(lineWidth);
	}
	public void setLineColor(Color color) {
		polyline.setColor(color);
	}
	public void setMarkerColor(Color color) {
		if (currentMarkerLayer != null)
			currentMarkerLayer.setMaterial(new Material(color));
	}
	private void addTrackPosition(MapPlace newPlace) {
		Position position = Position.fromDegrees(newPlace.getDegreesLatitude(), newPlace.getDegreesLongitude(), newPlace.getAltitude());
		positions.add(position);
		polyline.setPositions(positions);
		history.add(newPlace);
		this.historyMarkerLayer.setTracks(getHistoryTrack());
		if (icon != null)
			icon.setValue("place", newPlace);
		this.worldWindView.worldWindWidget.redraw();
	}
	public void moveLastPosition(MapPlace newPlace) {
		if (currentMarkerLayer == null)
			return;
		this.lastPosition = newPlace;
		this.currentMarkerLayer.setTracks(getLastPositionTrack());
		this.addTrackPosition(newPlace);
	}
	public void addLayers() {
		this.worldWindView.worldWindWidget.insertBeforeLayer(this.worldWindView.annotationLayer,this);
		this.worldWindView.worldWindWidget.insertBeforeLayer(this.worldWindView.annotationLayer,this.historyMarkerLayer);
		if (currentMarkerLayer != null)
			this.worldWindView.worldWindWidget.insertBeforeLayer(this.worldWindView.annotationLayer,this.currentMarkerLayer);
	}
	public void removeLayers() {
		this.worldWindView.worldWindWidget.removeLayer(this);
		this.worldWindView.worldWindWidget.removeLayer(this.historyMarkerLayer);
		if (currentMarkerLayer != null)
			this.worldWindView.worldWindWidget.removeLayer(this.currentMarkerLayer);
	}
	public void redrawTrack(com.ikno.itracclient.Track track) {
		this.setEnabled(track.isEnabled());
		this.worldWindView.worldWindWidget.redraw();
	}
	public void zoomToTrack(com.ikno.itracclient.Track track, boolean animate) {
		Position zoomPosition = null;
		if (lastPosition == null) {
			if (history == null || history.isEmpty())
				return;
			zoomPosition = Position.fromDegrees(history.get(history.size()-1).getDegreesLatitude(), history.get(history.size()-1).getDegreesLongitude(), history.get(history.size()-1).getAltitude());
		} else
			zoomPosition = Position.fromDegrees(lastPosition.getDegreesLatitude(), lastPosition.getDegreesLongitude(), lastPosition.getAltitude());
		this.worldWindView.worldWindWidget.gotoPosition(zoomPosition, animate);
	}
	public void zoomToTrack(com.ikno.itracclient.Track track, double eyeElevation, boolean animate) {
		Position zoomPosition = null;
		if (lastPosition == null) {
			if (history == null || history.isEmpty())
				return;
			zoomPosition = Position.fromDegrees(history.get(history.size()-1).getDegreesLatitude(), history.get(history.size()-1).getDegreesLongitude(), history.get(history.size()-1).getAltitude());
		} else
			zoomPosition = Position.fromDegrees(lastPosition.getDegreesLatitude(), lastPosition.getDegreesLongitude(), lastPosition.getAltitude());
		this.worldWindView.worldWindWidget.gotoPosition(zoomPosition, eyeElevation, true, animate);
	}
	public void setHistory(MapPlace place) {
		positions.clear();
		this.history.clear();
		Position position = Position.fromDegrees(place.getDegreesLatitude(), place.getDegreesLongitude(), place.getAltitude());
		double globeEle = this.worldWindView.worldWindWidget.getGlobeElevation(position);
		double posEle = position.getElevation();
		double diff = java.lang.Math.abs(posEle-globeEle);
		if (diff > 50.0)
			followsTerrain = false;
		positions.add(new Position(position.getLatLon(),posEle));
		this.history.add(place);
		polyline.setPositions(positions);
		this.historyMarkerLayer.setTracks(getHistoryTrack());
	}
	public void setHistory(MapPlace[] history) {
		positions.clear();
		this.history.clear();
		for (MapPlace place : history) {
			Position position = Position.fromDegrees(place.getDegreesLatitude(), place.getDegreesLongitude(), place.getAltitude());
			double globeEle = this.worldWindView.worldWindWidget.getGlobeElevation(position);
			double posEle = position.getElevation();
			double diff = java.lang.Math.abs(posEle-globeEle);
			if (diff > 50.0)
				followsTerrain = false;
			positions.add(new Position(position.getLatLon(),posEle));
			this.history.add(place);
		}
		polyline.setPositions(positions);
		this.historyMarkerLayer.setTracks(getHistoryTrack());
	}
    public void removeTrack(com.ikno.itracclient.Track track) {
    	this.removeLayers();
		if (annotation != null)
			this.worldWindView.annotationLayer.removeAnnotation(annotation);
		this.worldWindView.removeTrack(track);
    }
	public void selected(SelectEvent event) {
		if (event.getEventAction().equals(SelectEvent.ROLLOVER)) {
            PickedObject po = null;
            PickedObjectList objects = event.getObjects();
            for (PickedObject poi : objects) {
            	if (poi.getObject() instanceof UserFacingIcon) {
            		po = poi;
            		break;
            	} else if (poi.getParentLayer() instanceof TrackMarkerLayer) {
            		po = poi;
            		break;
            	}
            }
            MapPlace place = null;
            if (po == null && annotation != null) {
            	this.worldWindView.annotationLayer.removeAnnotation(annotation);
            	if (lastPicked != null && lastPicked.getObject() instanceof UserFacingIcon) {
            		UserFacingIcon icon = (UserFacingIcon)lastPicked.getObject();
            		icon.setHighlighted(false);
            	}
            	annotation = null;
            	lastPicked = null;
            }
            if (po != null) {
            	if (lastPicked == null) {
            		if (po.getObject() instanceof UserFacingIcon) {
            			lastPicked = po;
            			UserFacingIcon icon = (UserFacingIcon)po.getObject();
            			icon.setHighlighted(true);
            			place = (MapPlace)icon.getValue("place");
//            			place = ((DBTrackPoint)((DBTrack)lastPosition).getLastPoint()).getPlace();
            		} else if (po.getParentLayer() instanceof TrackMarkerLayer) {
            			lastPicked = po;
            			int idx = Integer.parseInt(po.getValue(AVKey.PICKED_OBJECT_ID).toString());
                		TrackMarkerLayer obj = (TrackMarkerLayer)po.getObject();
                		for (Track track : obj.getTracks()) {
                			DBTrack dbtrack = (DBTrack)track;
                			if (idx < track.getNumPoints()) {
                				place = dbtrack.getPlace(idx);
                				break;
                			}
                			idx = idx-track.getNumPoints();
                		}
            		}
            		if (place != null) {
            			AnnotationAttributes geoAttr = new AnnotationAttributes();
            			geoAttr.setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
            			geoAttr.setEffect(MultiLineTextRenderer.EFFECT_OUTLINE);  // Black outline
            			geoAttr.setBorderColor(Color.BLACK);
            			geoAttr.setBackgroundColor(new Color(1f, 1f, 1f, .9f));
            			geoAttr.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
            			geoAttr.setFont(Font.decode("Arial-PLAIN-11"));
            			geoAttr.setScale(1);             			// No scaling
            			geoAttr.setHighlightScale(1);             	// No highlighting either
            			geoAttr.setDistanceMaxScale(1);
            			geoAttr.setDistanceMinScale(1);
            			geoAttr.setDrawOffset(new Point(-10, 50)); // centered just above
            			geoAttr.setTextColor(Color.DARK_GRAY);
            			annotation = new GlobeAnnotation(place.getFullDescr(), Position.fromDegrees(place.getDegreesLatitude(), place.getDegreesLongitude(), place.getElevation()));
            			annotation.setAlwaysOnTop(true);
            			this.worldWindView.annotationLayer.addAnnotation(annotation);
            		}
            	} else {
            		int idx;
            		try {
            			idx = Integer.parseInt(po.getValue(AVKey.PICKED_OBJECT_ID).toString());
            		} catch (Exception e) {
            			return;
            		}
            		int lidx;
            		try {
            			lidx = Integer.parseInt(lastPicked.getValue(AVKey.PICKED_OBJECT_ID).toString());
            		} catch (Exception e) {
            			return;
            		}
            		if (lidx != idx) {
            			if (annotation != null) {
            				if (po.getObject() instanceof UserFacingIcon) {
            					UserFacingIcon icon = (UserFacingIcon)po.getObject();
            					icon.setHighlighted(false);
            				}
            				this.worldWindView.annotationLayer.removeAnnotation(annotation);
            			}
            			annotation = null;
            			lastPicked = null;
            		}
            	}
            }
        }
	}
    protected void doRender(DrawContext dc) {
    	super.doRender(dc);
    }
}
