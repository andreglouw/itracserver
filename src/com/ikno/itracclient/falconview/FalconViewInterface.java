package com.ikno.itracclient.falconview;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.eclipse.swt.widgets.Composite;

import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.mapping.widgets.Position;

public class FalconViewInterface {
	private static final Logger logger = Logging.getLogger(FalconViewInterface.class.getName());
	
	protected FVWILayer iLayer = null;
	protected FVWIMap iMap = null;
	private WebServer webServer = null;
	private Integer placemarkLayerHandle;
	private boolean connected = false;
	private boolean errorHandled = false;
	private static XmlRpcClient fvwClient = null;
	private static boolean clientDisconnected = false;
	private static Map<String, List<GeoObject>> lineCache = new HashMap<String, List<GeoObject>>();
	private static Map<String, GeoObject> objectCache = new HashMap<String, GeoObject>();
	private static Map<Integer, String> idCache = new HashMap<Integer, String>();
	
	private class GeoObject {
		public double lat;
		public double lon;
		public double elev;
		public int handle;
		public String name;
		public String description;
		public GeoObject(int handle, String name, String description, double lat, double lon, double elev) {
			this.handle = handle;
			this.name = name;
			this.description = description;
			this.lat = lat;
			this.lon = lon;
			this.elev = elev;
		}
		public GeoObject(int handle, double lat, double lon, double elev) {
			this.handle = handle;
			this.name = null;
			this.description = null;
			this.lat = lat;
			this.lon = lon;
			this.elev = elev;
		}
	}
	public FalconViewInterface(Composite parent, IMappingView mappingView) {
		iLayer = new FVWILayer();
		iMap = new FVWIMap();
		webServer = new WebServer(9190);
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        try {
			phm.addHandler("FVWICallback",com.ikno.itracclient.falconview.FalconViewInterface.FVWICallback.class);
			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);
			webServer.start();
        } catch (IOException e) {
        	e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean connected() {
		return connected;
	}
	public void openConnection() throws Exception {
    	if (!connected) {
    		connected = true;
    		errorHandled = false;
    		Object[] result = iLayer.CreateLayer("iTrac Placemark Layer");
    		if (result != null) {
    			if ((Integer)result[0] >= 0) {
    				placemarkLayerHandle = (Integer)result[0];
    				iLayer.MoveLayerToTop(placemarkLayerHandle);
    				iLayer.ShowLayer(placemarkLayerHandle, true);
	    		} else {
	    			if ((Integer)result[0] == -1)
	    				throw new Exception("Error Creating Placemark Layer");
	    			else if ((Integer)result[0] == -99)
	    				throw new Exception("Exception Creating Placemark Layer: "+result[1]);
	    		}
    		}
    	}
	}
	public boolean supportsKMLMaps() {
		return false;
	}
    public boolean removePlacemark(String id) {
		GeoObject object = objectCache.get(id);
		if (object != null) {
			iLayer.DeleteObject(placemarkLayerHandle, object.handle);
			iLayer.Refresh(placemarkLayerHandle);
		}
		return true;
	}		
	public boolean updatePlacemark(String id,String name,String description,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude) {
		GeoObject object = objectCache.get(id);
		if (object != null) {
			iLayer.DeleteObject(placemarkLayerHandle, object.handle);
			this.addPlacemark(id, name, description, true, true, iconHref, iconScale, latitude, longitude, altitude, null);
		}
		return true;
	}
	public boolean addPlacemark(String id,String name,String description,
			boolean isOpen,boolean isVisible,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude,String altitudeMode) {
		if (!iconHref.endsWith(".ico")) {
			int dot = iconHref.lastIndexOf(".");
			iconHref = iconHref.substring(0,dot)+".ico";
		}
		Object[] result = iLayer.AddIcon(placemarkLayerHandle, iconHref, latitude, longitude, name);
		if (result != null && (Integer)result[0] >= 0) {
			int object_handle = (Integer)result[0];
			objectCache.put(id, new GeoObject(object_handle, name, description, latitude, longitude, altitude));
			idCache.put(object_handle, id);
			iLayer.Refresh(placemarkLayerHandle);
			return true;
		}
		return false;
	}
	public boolean addWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			String altitudeMode) {
		String call = "addWaypoint('%s','%s','%s',%s,%.2f,'%s')";
		return true;
	}
	public boolean addMoveableWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			String altitudeMode) {
		String call = "addMoveableWaypoint('%s','%s','%s',%s,%.2f,'%s')";
		return true;
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
		return true;
	}
	public boolean deselectPlacemark(String id) {
		String call = "deselectPlacemark('%s')";
		return true;
	}
	public boolean fixWaypoint(String id,String name,String description,
			String iconHref,double iconScale,
			double latitude,double longitude,double altitude) {
		String call = "fixWaypoint('%s','%s','%s',%s,%.2f,%.5f,%.5f,%.2f)";
		return true;
	}
	public boolean zoomTo(double latitude,double longitude,double eyeAltitude) {
		Object[] parms = iMap.GetMapDisplay();
		if ((Integer)parms[0] == 0) {
			int map_handle = (Integer)parms[5];
			int zoom = (Integer)parms[6];
			int mask = 3;
			Object[] result = iMap.SetMapDisplay(latitude, longitude, 0.0, 0, map_handle, zoom, mask, 0);
			return (result != null && (Integer)result[0] == 0);
		} else
			System.out.println("GetMapDisplay Aaah!");
		return false;
	}
	public boolean changeVisibility(String id,boolean visible) {
		String call = "changeVisibility('%s',%s)";
		return true;
	}
	public boolean fetchKML(String url) {
		String call = "fetchKML('%s')";
		return true;
	}
	public boolean parseKML(String kml) {
		String call = "parseKML('%s')";
		return true;
	}
	public boolean raiseAlert(String text) {
		String call = "raiseAlert('%s')";
		return true;
	}
	public boolean resizeMap() {
		String call = "resizeMap()";
		return true;
	}
	public Position currentEyePosition() {
		return null;
	}
	public String softwareVersion() {
		Object[] result = iLayer.GetVersion();
		if (result != null && (Integer)result[0] == 0)
			return String.format("%d.%d.%d",result[1],result[2],result[3]);
		return "N/A";
	}
	public boolean clonePlacemark(String id,String newId,boolean showName,String iconHref,double iconScale) {
		String call = "clonePlacemark('%s','%s',%s,%s,%.2f)";
		return true;
	}
	private Object[] parseColor(String color) {
		if (color.length() == 8) {
			int alpha = 255;
			int blue = Short.parseShort(color.substring(2, 4),16);
			int green = Short.parseShort(color.substring(4, 6),16);
			int red = Short.parseShort(color.substring(6, 8),16);
			return new Object[] {red, green, blue};
		}
		return null;
	}
	public boolean addLinePoint(String lineId,int width,String color,double lat,double lon,double alt) {
		List<GeoObject> points = lineCache.get(lineId);
		if (points == null) {
			points = new ArrayList<GeoObject>();
			lineCache.put(lineId, points);
		}
		GeoObject to = new GeoObject(-1, lat, lon, alt);
		points.add(to);
		if (points.size() >= 2) {
			GeoObject from = points.get(points.size()-2);
			Object[] colors = this.parseColor(color);
			if (colors == null)
				colors = new Object[] {255, 0, 0};
			iLayer.SetPen(placemarkLayerHandle, (Short)colors[0], (Short)colors[1], (Short)colors[2], (short)0, (short)0, (short)0, true, (long)width, 0l);
			Object[] result = iLayer.AddLine(placemarkLayerHandle, from.lat, from.lon, lat, lon, 0, 0);
			if (result != null && (Integer)result[0] >= 0) {
				if (from.handle == -1)
					from.handle = (Integer)result[0];
				to.handle = (Integer)result[0];
			}
			return (result != null && (Integer)result[0] >= 0);
		}
		return true;
	}
	public boolean showLinePoints(String lineId,String altitudeMode) {
		String call = "showLinePoints('%s','%s')";
		return true;
	}
	public boolean clearLine(String lineId) {
		List<GeoObject> points = lineCache.get(lineId);
		if (points != null) {
			int last_handle = -2;
			for (GeoObject object : points) {
				if (object.handle != -1 && object.handle != last_handle) {
					iLayer.DeleteObject(placemarkLayerHandle, object.handle);
					last_handle = object.handle;
				}
			}
			lineCache.remove(lineId);
		}
		return true;
	}
	public boolean clearFirstLinePoint(String lineId) {
		String call = "clearFirstLinePoint('%s')";
		return true;
	}
	public boolean updatePolygon(String id,String name,String description,String iconHref,double iconScale,double labelScale,String labelColor,String lineColor,String fillColor) {
		String call = "updatePolygon('%s','%s','%s',%s,%.2f,%.2f,'%s','%s','%s')";
		return true;
	}
	public boolean startPolygon(String id,int lineWidth,String fillColor) {
		String call = "startPolygon('%s',%d,'%s')";
		return true;
	}
	public List<Position> fixPolygon(String id) {
		return null;
	}
	public boolean removePolygon(String id) {
		String call = "removePolygon('%s')";
		return true;
	}
	public void refresh() {
		errorHandled = false;
	}
	
	public static class FVWICallback {
		
		public Object[] GetHelpText(int layer_handle, int object_handle) {
			return new Object[] {0,"Help Text"};
		}

		public Object[] GetInfoText(int layer_handle, int object_handle) {
			String id = FalconViewInterface.idCache.get(object_handle);
			GeoObject object = null; 
			if (id != null)
				object = objectCache.get(id);
			if (object != null && object.name != null && object.description != null)
				return new Object[] {0, object.name, object.description, 0};
			return new Object[] {-1, "Unknown", "None", 0};
		}

		public String GetMenuItems(int layer_handle, int object_handle) {
			return "<result>0</><menu_text></>";
		}

		public Object[] GetTimeSpan(int layer_handle) {
			return new Object[] {0, "",""};
		}

		public Object[] GetToolTip(int layer_handle, int object_handle) {
			return new Object[] {0, ""};
		}

		public int OnDoubleClicked(int layer_handle, int object_handle, int fvw_parent_hWnd, double lat, double lon) {
			return 0;
		}

		public int OnFalconViewExit(int layer_handle) {
			FalconViewInterface.clientDisconnected = true;
			FalconViewInterface.fvwClient = null;
			return 0;
		}

		public int OnGeoCircleBounds(int click_id, double lat, double lon, double radius) {
			return 0;
		}

		public int OnGeoCircleBoundsCanceled(int click_id) {
			return 0;
		}

		public String OnGeoRectBounds(int click_id, double NW_lat, double NW_lon, double SE_lat, double SE_lon) {
			return "<result>0</>";
		}

		public String OnGeoRectBoundsCanceled(int click_id) {
			return "<result>0</>";
		}

		public String OnMouseClick(int click_id, double latitude, double longitude) {
			return "<result>0</>";
		}

		public String OnMouseClickCanceled(int click_id) {
			return "<result>0</>";
		}

		public String OnOverlayClose(int layer_handle) {
			return "<result>0</>";
		}

		public String OnPreClose(int layer_handle) {
			return "<result>0</><cancel>1</>";
		}

		public String OnSelected(int layer_handle, int object_handle, int fv_parent_hWnd, double latitude, double longitude) {
			return "<result>0</>";
		}

		public String OnSnapToInfo(int click_id, double lat, double lon, int point_type, String key_text) {
			return "<result>0</>";
		}

		public String OnSnapToInfoCanceled(int click_id) {
			return "<result>0</>";
		}

		public String OnToolbarButtonPressed(int toolbar_id, int button_number) {
			return "<result>0</>";
		}

		public String SetCurrentViewTime(int layer_handle, String date) {
			return "<result>0</>";
		}
	}

	public static XmlRpcClient fvwClient() {
		if (fvwClient == null && clientDisconnected == false) {
	          // create configuration
	          XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	          try {
				config.setServerURL(new URL("http://localhost:9191/RPC2"));
				config.setEnabledForExtensions(true);  
				config.setConnectionTimeout(60 * 1000);
				config.setReplyTimeout(60 * 1000);
				fvwClient = new XmlRpcClient();
				// use Commons HttpClient as transport
				fvwClient.setTransportFactory(new XmlRpcCommonsTransportFactory(fvwClient));
				// set configuration
				fvwClient.setConfig(config);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return fvwClient;
	}

	public class FVWIMap {
		public Object[] CAPSPrint(String text, boolean print_to_scale, boolean show_map_info) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CAPSPrint", new Object[] {text, ""+print_to_scale, ""+show_map_info});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
		}

	    public Object[] GetElevation(double lat, double lon) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetElevation", new Object[] {""+lat, ""+lon});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetMapDisplay() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetMapDisplay", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetMapDisplayString(long category, long map_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetMapDisplayString", new Object[] {""+category, ""+map_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetMapDisplay(double lat, double lon, double rotation, long client_category, long map_handle, long zoom, long mask, long projection_type) {
	        /*Change Map Display.

	        mask specifies which parameters to use:
	        Type               Value
	        
	        LAT                1
	        LON                2
	        ROTATION           4
	        CATEGORY           8
	        MAP_HANDLE         16
	        ZOOM               32
	        PROJECTION         64
	        */
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetMapDisplay", new Object[] {""+lat, ""+lon, ""+rotation, ""+client_category, ""+map_handle, ""+zoom, ""+mask, ""+projection_type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }
	}
	
	public class FVWILayer {
	    public Object[] AddArc(long layer_handle, double lat, double lon, double radius, double heading_to_start, double turn_arc, boolean clockwise) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddArc", new Object[] {""+layer_handle, ""+lat, ""+lon, ""+radius, ""+heading_to_start, ""+turn_arc, ""+clockwise});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddBitmap(long layer_handle, String filename, double lat, double lon) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddBitmap", new Object[] {""+layer_handle,""+filename,""+lat,""+lon});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddCircleToSymbol(long symbol_handle, long center_x, long center_y, long radius) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddCircleToSymbol", new Object[] {""+symbol_handle, ""+center_x, ""+center_y, ""+radius});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddDotToSymbol(long symbol_handle, long x, long y) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddDotToSymbol", new Object[] {""+symbol_handle, ""+x, ""+y});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddEditorButton(long layer_handle, String button_text, String bitmap_filename, String class_ID_string) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddEditorButton", new Object[] {""+layer_handle, ""+button_text, ""+bitmap_filename, ""+class_ID_string});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddEllipse(long layer_handle, double lat, double lon, double vertical, double horizontal, double rotation) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddEllipse", new Object[] {""+layer_handle, ""+lat, ""+lon, ""+vertical, ""+horizontal, ""+rotation});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddGeoRect(long layer_handle, double NW_lat, double NW_lon, double SE_lat, double SE_lon) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddGeoRect", new Object[] {""+layer_handle, ""+NW_lat, ""+NW_lon, ""+SE_lat, ""+SE_lon});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddIcon(long layer_handle, String filename, double lat, double lon, String icon_text) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddIcon", new Object[] {""+layer_handle, ""+filename, ""+lat, ""+lon, ""+icon_text});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddIconEx(long layer_handle, long icon_set_handle, double lat, double lon, double rotation, String icon_text) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddIconEx", new Object[] {""+layer_handle, ""+icon_set_handle, ""+lat, ""+lon, ""+rotation, ""+icon_text});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddIconOffset(long layer_handle, String filename, double lat, double lon, String icon_text, long x_offset, long y_offset) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddIconOffset", new Object[] {""+layer_handle, ""+filename, ""+lat, ""+lon, ""+icon_text, ""+x_offset, ""+y_offset});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddIconToSet(long layer_handle, long icon_set_handle, long rotation, String filename) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddIconToSet", new Object[] {""+layer_handle, ""+icon_set_handle, ""+rotation, ""+filename});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddLine(long layer_handle, double lat1, double lon1, double lat2, double lon2, long x_offset, long y_offset) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddLine", new Object[] {""+layer_handle, ""+lat1, ""+lon1, ""+lat2, ""+lon2, ""+x_offset, ""+y_offset});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddLineToSymbol(long symbol_handle, long x1, long y1, long x2, long y2) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddLineToSymbol", new Object[] {""+symbol_handle, ""+x1, ""+y1, ""+x2, ""+y2});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddLineWithEmbeddedText(long layer_handle, double lat1, double lon1, double lat2, double lon2, long x_offset, long y_offset, String embedded_text) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddLineWithEmbeddedText", new Object[] {""+layer_handle, ""+lat1, ""+lon1, ""+lat2, ""+lon2, ""+x_offset, ""+y_offset, ""+embedded_text});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddMilStd2525Symbol(long layer_handle, double lat, double lon, String symbol_id) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddMilStd2525Symbol", new Object[] {""+layer_handle, ""+lat, ""+lon, ""+symbol_id});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddObjectToGroup(long layer_handle, long object_handle, long group_id) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddObjectToGroup", new Object[] {""+layer_handle, ""+object_handle, ""+group_id});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddPolygon(long layer_handle, Object lat_lon_array, long num_points) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddPolygon", new Object[] {""+layer_handle, ""+lat_lon_array, ""+num_points});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddPolyline(long layer_handle, Object lat_lon_array, long num_points) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddPolyline", new Object[] {""+layer_handle, ""+lat_lon_array, ""+num_points});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddSymbol(long layer_handle, long symbol_handle, double latitude, double longitude, double scale_factor, double rotation) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddSymbol", new Object[] {""+layer_handle, ""+symbol_handle, ""+latitude, ""+longitude, ""+scale_factor, ""+rotation});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddText(long layer_handle, double lat, double lon, String text, long x_offset, long y_offset) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddText", new Object[] {""+layer_handle, ""+lat, ""+lon, ""+text, ""+x_offset, ""+y_offset});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] AddToolbar(String filename, long num_buttons, String toolbar_name, short initial_position, Object separator_list, long num_separators) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("AddToolbar", new Object[] {""+filename, ""+num_buttons, ""+toolbar_name, ""+initial_position, ""+separator_list, ""+num_separators});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CloseOverlay(long type, String filespec) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CloseOverlay", new Object[] {""+type, ""+filespec});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CloseOverlayEx(long overlay_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CloseOverlayEx", new Object[] {""+overlay_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CreateIconSet(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CreateIconSet", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CreateLayer(String layer_name) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CreateLayer", new Object[] {""+layer_name});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CreateLayerEx(String layer_name, String icon_name) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CreateLayerEx", new Object[] {""+layer_name, ""+icon_name});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CreateOverlay(long type) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CreateOverlay", new Object[] {""+type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] CreateSymbol(String symbol_name) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("CreateSymbol", new Object[] {""+symbol_name});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] DeleteAllObjects(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("DeleteAllObjects", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] DeleteGroup(long layer_handle, long group_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("DeleteGroup", new Object[] {""+layer_handle, ""+group_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] DeleteLayer(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("DeleteLayer", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] DeleteObject(long layer_handle, long object_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("DeleteObject", new Object[] {""+layer_handle, ""+object_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetActiveOverlay() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetActiveOverlay", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetModified(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetModified", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetObjectData(long layer_handle, long object_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetObjectData", new Object[] {""+layer_handle, ""+object_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetOverlayInfo(long overlay_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetOverlayInfo", new Object[] {""+overlay_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetProducer() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetProducer", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] GetVersion() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("GetVersion", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] ModifyObject(long layer_handle, long object_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("ModifyObject", new Object[] {""+layer_handle, ""+object_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveBitmap(long layer_handle, long object_handle, double lat, double lon) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveBitmap", new Object[] {""+layer_handle, ""+object_handle, ""+lat, ""+lon});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveIcon(long layer_handle, long object_handle, double lat, double lon, double rotation) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveIcon", new Object[] {""+layer_handle, ""+object_handle, ""+lat, ""+lon, ""+rotation});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveLayerAbove(long layer_handle, long overlay_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveLayerAbove", new Object[] {""+layer_handle, ""+overlay_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveLayerBelow(long layer_handle, long overlay_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveLayerBelow", new Object[] {""+layer_handle, ""+overlay_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveLayerToBottom(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveLayerToBottom", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveLayerToTop(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveLayerToTop", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveObjectToBottom(long layer_handle, long object_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveObjectToBottom", new Object[] {""+layer_handle, ""+object_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveObjectToTop(long layer_handle, long object_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveObjectToTop", new Object[] {""+layer_handle, ""+object_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] MoveSymbol(long layer_handle, long object_handle, double latitude, double longitude, double rotation) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("MoveSymbol", new Object[] {""+layer_handle, ""+object_handle, ""+latitude, ""+longitude, ""+rotation});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] OpenOverlay(long type, String filespec) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("OpenOverlay", new Object[] {""+type, ""+filespec});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] PlaybackBegin() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("PlaybackBegin", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] PlaybackEnd() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("PlaybackEnd", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] Refresh(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("Refresh", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] RegisterWithMapServer(String client_name, long window_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("RegisterWithMapServer", new Object[] {""+client_name, ""+window_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] RemoveEditorButton(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("RemoveEditorButton", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] RemoveToolbar(long toolbar_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("RemoveToolbar", new Object[] {""+toolbar_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SaveOverlay(long layer_handle) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SaveOverlay", new Object[] {""+layer_handle});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetAbsoluteTime(long layer_handle, Date date_time) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetAbsoluteTime", new Object[] {""+layer_handle, ""+date_time});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetAnchorType(long layer_handle, long anchor_type) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetAnchorType", new Object[] {""+layer_handle, ""+anchor_type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetFillType(long layer_handle, long fill_type) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetFillType", new Object[] {""+layer_handle, fill_type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetFont(long layer_handle, String font_name, long size, long attributes, long fg_color, long bg_color, long bg_type) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetFont", new Object[] {""+layer_handle, ""+font_name, ""+size, ""+attributes, ""+fg_color, ""+bg_color, ""+bg_type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetGroupThreshold(long layer_handle, long group_handle, long scale_denominator) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetGroupThreshold", new Object[] {""+layer_handle, ""+group_handle, ""+scale_denominator});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetIsTimeSensitive(long layer_handle, boolean enabled) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetIsTimeSensitive", new Object[] {""+layer_handle, ""+enabled});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetLayerThreshold(long layer_handle, long scale_denominator) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetLayerThreshold", new Object[] {""+layer_handle, ""+scale_denominator});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetLineType(long layer_handle, long line_type) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetLineType", new Object[] {""+layer_handle, ""+line_type});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetModified(long layer_handle, short modified) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetModified", new Object[] {""+layer_handle, ""+modified});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetObjectData(long layer_handle, long object_handle, long object_data) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetObjectData", new Object[] {""+layer_handle, ""+object_handle, ""+object_data});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetObjectThreshold(long layer_handle, long object_handle, long scale_denominator) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetObjectThreshold", new Object[] {""+layer_handle, ""+object_handle, ""+scale_denominator});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetPen(long layer_handle, short fg_red, short fg_green, short fg_blue, short bg_red, short bg_green, short bg_blue, boolean turn_off_background, long line_width, long line_style) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetPen", new Object[] {""+layer_handle, ""+fg_red, ""+fg_green, ""+fg_blue, ""+bg_red, ""+bg_green, ""+bg_blue, ""+turn_off_background, ""+line_width, ""+line_style});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetPlaybackRate(long playback_rate) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetPlaybackRate", new Object[] {""+playback_rate});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetTextRotation(long layer_handle, double rotation_degrees) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetTextRotation", new Object[] {""+layer_handle, ""+rotation_degrees});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetToolbarButtonState(long toolbar_handle, long button_number, short button_down) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetToolbarButtonState", new Object[] {""+toolbar_handle, ""+button_number, ""+button_down});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] SetupToolbarButton(long toolbar_handle, long button_number, String tooltip_text, String status_bar_text) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("SetupToolbarButton", new Object[] {""+toolbar_handle, ""+button_number, ""+tooltip_text, ""+status_bar_text});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] ShowEditorToolbar(boolean show) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("ShowEditorToolbar", new Object[] {""+show});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] ShowLayer(long layer_handle, boolean show_layer) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("ShowLayer", new Object[] {""+layer_handle, ""+show_layer});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] ShowMainToolbar(boolean show) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("ShowMainToolbar", new Object[] {""+show});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] ShowStatusBar(boolean show) {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("ShowStatusBar", new Object[] {""+show});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] StartPlayback() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("StartPlayback", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }

	    public Object[] StopPlayback() {
			if (fvwClient() == null)
				return new Object[] {-1};
			try {
				return (Object[])fvwClient().execute("StopPlayback", new Object[] {});
			} catch (XmlRpcException e) {
				e.printStackTrace();
			}
			return new Object[] {-1};
	    }
	}
}
