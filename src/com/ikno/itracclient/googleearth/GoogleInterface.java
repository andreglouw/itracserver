package com.ikno.itracclient.googleearth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.dialogs.PolygonDialog;
import com.ikno.itracclient.dialogs.WaypointDialog;
import com.ikno.itracclient.mapping.widgets.Feature;
import com.ikno.itracclient.mapping.widgets.Position;

public class GoogleInterface {
	private static final Logger logger = Logging.getLogger(GoogleInterface.class.getName());

	private Browser browser;
	private WaypointDialog waypointDialog = null;
	private PolygonDialog polygonDialog = null;
	private String functionName = null;
	private String functionResult = null;
	private GoogleEarth mappingView;
	private Feature selected = null;
	private boolean connected = false;
	private boolean googleReady = false;
	private boolean errorHandled = false;
	
	public GoogleInterface(Composite parent, IMappingView mappingView) {
		this.mappingView = (GoogleEarth)mappingView;
		browser = new Browser(parent, SWT.NONE);
		Browser.clearSessions();
		browser.addStatusTextListener(new StatusTextListener() {
			public void changed(StatusTextEvent event) {
				final String text = event.text;
				if (text.equals("googleLoaded:true") && googleReady == false) {
					logger.severe("Status text: "+text);
					googleReady = true;
					GoogleInterface.this.mappingView.refreshAction.setEnabled(true);
					GoogleInterface.this.mappingView.interfaceReady();
				} else if ((text.startsWith("Opening page http://www.google.com/earth/plugin/error.html") ||
						text.startsWith("Downloading picture http://www.google.com/earth/plugin/images/globe_background.jpg") ||
						text.startsWith("Failure when initializing Google Earth")) && 
						errorHandled == false) {
					logger.severe("Status text: "+text);
					errorHandled = true;
					logger.severe("Error has not been reported, reporting to user...");
					googleReady = false;
					GoogleInterface.this.mappingView.refreshAction.setEnabled(true);
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error loading", "Error initialising Google, click OK to try again");
					errorHandled = false;
					browser.refresh();
					GoogleInterface.this.mappingView.refreshAction.setEnabled(false);
					logger.severe("Error during load of Google Earth, will try refreshing browser");
				} else if (text.startsWith("locationUpdate:")) {
					try {
						Position pos = new Position().parsePosition(text);
						GoogleInterface.this.mappingView.statusBar.handleCursorPositionChange(pos);
					} catch (Exception e) {
					}
				} else if (text.startsWith("kmlSuccess:")) {
					logger.severe("Status text: "+text);
					try {
						String[] tokens = text.split(":");
						GoogleInterface.this.mappingView.kmlLayerLoaded(tokens[1].trim());
					} catch (Exception e) {
					}
				} else if (text.startsWith("mouseClicked:")) {
					logger.severe("Status text: "+text);
					GoogleInterface.this.mappingView.requestFocus();
				} else if (text.startsWith("placemarkSelected:")) {
					logger.severe("Status text: "+text);
					try {
						GoogleInterface.this.mappingView.requestFocus();
						selected = new Feature().parsePlacemark(text);
						logger.finer("placemarkSelected received, selected: "+selected);
						Display.getCurrent().asyncExec(new Runnable() {
							public void run() {
								if (selected != null) {
									if (selected.resolved(GoogleInterface.this.mappingView.getAvailableFeatures()) == null) {
										logger.finer("Non-GeoPoint, deselecting placemark");
										GoogleInterface.this.deselectPlacemark(selected.id);
									} else {
										if (waypointDialog == null)
											waypointDialog = new WaypointDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),GoogleInterface.this.mappingView);
										if (waypointDialog.isOpen) {
											waypointDialog.setPlacemark(selected);
										} else
											waypointDialog.open(selected);
									}
								} else {
									if (waypointDialog != null) {
										waypointDialog.setPlacemark(null);
									}										
								}
							}
						});
					} catch (Exception e) {
					}
				} else if (text.startsWith("placemarkDeselected:")) {
					try {
						if (selected != null) {
							logger.finer("Deselected "+text+": "+selected+", resolved to "+selected.resolved(GoogleInterface.this.mappingView.getAvailableFeatures()));
							if (waypointDialog != null) {
								waypointDialog.setPlacemark(null);
							}										
							if (selected.modified == true) {
								GoogleInterface.this.revertWaypoint(selected.id, selected.position.latitude, selected.position.longitude);
							}
						}
					} catch (Exception e) {
						logger.severe("Unexpected exception :"+e);
					}
				} else if (text.startsWith("placemarkMoved:")) {
					try {
						logger.finer("Moved "+selected);
						if (waypointDialog != null) {
							Feature placemark = new Feature().parsePlacemark(text);
							waypointDialog.updatePosition(placemark.position.latitude, placemark.position.longitude);
						}
						selected.modified = true;
					} catch (Exception e) {
					}
				} else if (text.startsWith("polygonSelected:")) {
					try {
						GoogleInterface.this.mappingView.requestFocus();
						selected = new Feature().parsePlacemark(text);
						logger.finer("polygonSelected received, selected: "+selected);
						Display.getCurrent().asyncExec(new Runnable() {
							public void run() {
								if (selected != null) {
									if (selected.resolved(GoogleInterface.this.mappingView.getAvailableFeatures()) == null) {
										logger.finer("Non-GeoArea, deselecting placemark");
										GoogleInterface.this.deselectPlacemark(selected.id);
									} else {
										if (polygonDialog == null)
											polygonDialog = new PolygonDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),GoogleInterface.this.mappingView);
										if (polygonDialog.isOpen) {
											polygonDialog.setFeature(selected);
										} else
											polygonDialog.open(selected);
									}
								} else {
									if (polygonDialog != null) {
										polygonDialog.setFeature(null);
									}										
								}
							}
						});
					} catch (Exception e) {
					}
				} else {
					if (functionName != null) {
						logger.severe("Status text: "+text);
						if (text.startsWith(functionName)) {
							functionName = null;
							String[] tokens = text.split(":");
							if (tokens.length > 1)
								functionResult = tokens[1];
						}
					} else if (text.startsWith("log:")){
						logger.severe(text.substring(4));
					} else if (!text.equals("")){
						System.out.println("Status text: "+text);
					}
				}
			}
		});
	}
	public boolean connected() {
		return connected;
	}
	public boolean googleReady() {
		return googleReady;
	}
	public void openConnection() throws Exception {
    	if (browser.getUrl() == null || browser.getUrl().isEmpty()) {
    		browser.setUrl(System.getProperty("com.ikno.mapping.googleurl","http://www.i-see.co.za/itrac/index.html"));
    		connected = true;
    		googleReady = false;
    		errorHandled = false;
    	}
	}
	public void openConnection_new() throws Exception {
		String url = browser.getUrl();
    	if (url == null || url.isEmpty() || url.equals("about:blank")) {
    		browser.setUrl(System.getProperty("com.ikno.mapping.googleurl","http://www.i-see.co.za/itrac/index.html"));
    		connected = true;
    		googleReady = false;
    		errorHandled = false;
    	}
	}
	public boolean supportsKMLMaps() {
		return true;
	}
	public boolean removePlacemark(String id) {
		return browser.execute("removePlacemark('"+id+"');");
	}		
	public boolean updatePlacemark(String id,String name,String description,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude) {
		String call = "updatePlacemark('%s','%s','%s',%s,%.2f,%.5f,%.5f,%.2f)";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,latitude,longitude,altitude));
	}
	public boolean updatePlacemark(String id,GeoPoint geoPoint) {
		return this.updatePlacemark(id,geoPoint.getAreaName(),geoPoint.getDescription(),
				(geoPoint.getIconUrl() == null) ? "null" : geoPoint.getIconUrl(),geoPoint.getIconScale(),
				geoPoint.getLatitude(),geoPoint.getLongitude(),geoPoint.getAltitude());
	}
	public boolean updatePlacemarkIcon(String id,boolean showName,String iconHref,double iconScale) {
		String call = "updatePlacemarkIcon('%s',%s,%s,%.2f)";
		return browser.execute(String.format(call,id,(showName == true ? "true" : "false"),
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale));
	}
	private boolean createPlacemark(String id,String name,String description,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude,String altitudeMode) {
		String call = "createPlacemark('%s','%s','%s',%s,%.2f,%.5f,%.5f,%.2f,'%s')";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,latitude,longitude,altitude,altitudeMode));
	}
	public boolean addPlacemark(String id,String name,String description,
			boolean isOpen,boolean isVisible,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude,String altitudeMode) {
		String call = "addPlacemark('%s','%s','%s',%s,%s,%s,%.2f,%.5f,%.5f,%.2f,'%s')";
		return browser.execute(String.format(call,id,name,description,
				(isOpen == true) ? "true" : "false", (isVisible == true) ? "true" : "false",
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,latitude,longitude,altitude,altitudeMode));
	}
	public boolean addWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			String altitudeMode) {
		String call = "addWaypoint('%s','%s','%s',%s,%.2f,'%s')";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,altitudeMode));
	}
	public boolean addMoveableWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			String altitudeMode) {
		String call = "addMoveableWaypoint('%s','%s','%s',%s,%.2f,'%s')";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,altitudeMode));
	}
	public boolean addWaypoint(String id,GeoPoint geoPoint) {
		return this.addWaypoint(id, geoPoint.getAreaName(), geoPoint.getDescription(), geoPoint.getIconUrl(), 
				geoPoint.getIconScale(), "CLAMP_TO_GROUND");
	}
	public boolean addMoveableWaypoint(String id,GeoPoint geoPoint) {
		return this.addMoveableWaypoint(id, geoPoint.getAreaName(), geoPoint.getDescription(), geoPoint.getIconUrl(), 
				geoPoint.getIconScale(), "CLAMP_TO_GROUND");
	}
	public boolean revertWaypoint(String id,double latitude,double longitude) {
		String call = "revertWaypoint('%s',%.5f,%.5f)";
		return browser.execute(String.format(call,id,latitude,longitude));
	}
	public boolean deselectPlacemark(String id) {
		String call = "deselectPlacemark('%s')";
		return browser.execute(String.format(call,id));
	}
	public boolean fixWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude) {
		String call = "fixWaypoint('%s','%s','%s',%s,%.2f,%.5f,%.5f,%.2f)";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,latitude,longitude,altitude));
	}
	public boolean zoomTo(double latitude,double longitude,double eyeAltitude) {
		String call = "zoomTo(%.5f,%.5f,%.2f)";
		return browser.execute(String.format(call,latitude,longitude,eyeAltitude));
	}
	public boolean changeVisibility(String id,boolean visible) {
		String call = "changeVisibility('%s',%s)";
		return browser.execute(String.format(call,id,(visible == true) ? "true" : "false"));
	}
	public boolean fetchKML(String url, String rootId, boolean mangleURL) {
		String call = "fetchKMLMangled('%s','%s',%s)";
		return browser.execute(String.format(call,url,rootId,(mangleURL == true ? "true" : "false")));
	}
	public boolean removeKML(String rootId) {
		String call = "removeKML('%s')";
		return browser.execute(String.format(call,rootId));
	}
	public boolean parseKML(String kml) {
		String call = "parseKML('%s')";
		return browser.execute(String.format(call,kml));
	}
	public boolean raiseAlert(String text) {
		String call = "raiseAlert('%s')";
		return browser.execute(String.format(call,text));
	}
	public boolean resizeMap(int x, int y, int width, int height) {
		browser.setBounds(x, y, width, height);
		String call = "resizeMap()";
		return browser.execute(String.format(call));
	}
	public Position currentEyePosition() {
		functionName = "currentEyePosition";
		functionResult = null;
		boolean result = browser.execute("currentEyePosition()");
		if (result == true && functionResult != null) {
			String[] tokens = functionResult.split(",");
			if (tokens.length == 4) {
				double latitude = Double.parseDouble(tokens[0]);
				double longitude = Double.parseDouble(tokens[1]);
				double altitude = Double.parseDouble(tokens[2]);
				double range = Double.parseDouble(tokens[3]);
				return new Position(latitude,longitude,altitude,range);
			}
		}
		return null;
	}
	public String softwareVersion() {
		functionName = "googlePluginVersion";
		functionResult = null;
		boolean result = browser.execute("googlePluginVersion()");
		if (result == true && functionResult != null) {
			return functionResult;
		}
		return null;
	}
	public boolean clonePlacemark(String id,String newId,boolean showName,String iconHref,double iconScale) {
		String call = "clonePlacemark('%s','%s',%s,%s,%.2f)";
		return browser.execute(String.format(call,id,newId,(showName == true ? "true" : "false"),
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale));
	}
	public boolean addLinePoint(String lineId,int width,String color,double lat,double lon,double alt) {
		String call = "addLinePoint('%s',%d,'%s',%.5f,%.5f,%.2f)";
		return browser.execute(String.format(call,lineId,width,color,lat,lon,alt));
	}
	public boolean showLinePoints(String lineId,String altitudeMode) {
		String call = "showLinePoints('%s','%s')";
		return browser.execute(String.format(call,lineId,altitudeMode));
	}
	public boolean clearLine(String lineId) {
		String call = "clearLine('%s')";
		return browser.execute(String.format(call,lineId));
	}
	public boolean clearFirstLinePoint(String lineId) {
		String call = "clearFirstLinePoint('%s')";
		return browser.execute(String.format(call,lineId));
	}
	public boolean updatePolygon(String id,String name,String description,String iconHref,double iconScale,double labelScale,String labelColor,String lineColor,String fillColor) {
		String call = "updatePolygon('%s','%s','%s',%s,%.2f,%.2f,'%s','%s','%s')";
		return browser.execute(String.format(call,id,name,description,
				(iconHref == null) ? "null" : "'"+iconHref+"'",iconScale,labelScale,labelColor,lineColor,fillColor));
	}
	public boolean startPolygon(String id,int lineWidth,String fillColor) {
		String call = "startPolygon('%s',%d,'%s')";
		return browser.execute(String.format(call,id,lineWidth,fillColor));
	}
	public List<Position> fixPolygon(String id) {
		functionName = "fixPolygon";
		functionResult = null;
		String call = "fixPolygon('%s')";
		boolean success = browser.execute(String.format(call,id));
		if (success == true && functionResult != null) {
			String[] positions = functionResult.split(";");
			if (positions.length > 0) {
				List<Position> result = new ArrayList<Position>();
				for (int i=0; i<positions.length; i++) {
					String[] tokens = positions[i].split(","); 
					if (tokens.length == 3) {
						double latitude = Double.parseDouble(tokens[0]);
						double longitude = Double.parseDouble(tokens[1]);
						double altitude = Double.parseDouble(tokens[2]);
						result.add(new Position(latitude,longitude,altitude));
					}
				}
				return result;
			}
		}
		return null;
	}
	public boolean removePolygon(String id) {
		String call = "removePolygon('%s')";
		return browser.execute(String.format(call,id));
	}
	public void refresh() {
		browser.refresh();
		errorHandled = false;
	}
}
