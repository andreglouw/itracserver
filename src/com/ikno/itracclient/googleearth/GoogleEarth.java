package com.ikno.itracclient.googleearth;

import itracclient.Activator;

import java.awt.EventQueue;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.WWLayer;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.GeoArea.Centroid;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Configuration;
import com.ikno.dao.utils.GeoUtilities;
import com.ikno.dao.utils.KMLUtilities;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingAssetTracker;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.ITrackListener;
import com.ikno.itracclient.Layer;
import com.ikno.itracclient.MapPlace;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.Track;
import com.ikno.itracclient.dialogs.LayerViewDialog;
import com.ikno.itracclient.dialogs.PolygonDialog;
import com.ikno.itracclient.dialogs.WaypointDialog;
import com.ikno.itracclient.mapping.widgets.Feature;
import com.ikno.itracclient.mapping.widgets.InfoBar;
import com.ikno.itracclient.mapping.widgets.Position;
import com.ikno.itracclient.mapping.widgets.Separator;
import com.ikno.itracclient.utils.Formatting;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class GoogleEarth extends ViewPart implements ISelectionProvider,IMappingView, IEntityChangeListener {
	private static final Logger logger = Logging.getLogger(GoogleEarth.class.getName());
	public static final String ID = "com.ikno.itracclient.googleearth.GoogleEarth"; //$NON-NLS-1$
	private String imageURL = null;
	private Composite container;
	private InfoBar infoBar;
	private int infoBarWidth;
	public StatusBar statusBar = null;
	private Feature selected = null;
	private Separator separator;
	private Action pluginVersionAction;
	private Action layersAction;
	private Action openKMLAction;
	public Action refreshAction;
	private Action addWaypointAction;
	private Action createPolygonAction;
	private String lastURL = "";
	private LayerViewDialog layerViewDialog = null;
	private WaypointDialog waypointDialog = null;
	private PolygonDialog polygonDialog = null;
	public GoogleInterface gi = null;
	protected ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
	private Map<Long,IMappingAssetTracker> activeAssets = new HashMap<Long,IMappingAssetTracker>();
	List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	private Asset selectedAsset = null;
	private Position eyePosition = null;
	private HashMap<String,GeoArea> features = new HashMap<String,GeoArea>();
	private long featureCount = 0;
	private String subID = null;

	public String getViewId() {
		if (subID != null)
			return subID;
		return GoogleEarth.ID;
	}
	public void setSubID(String subID) {
		this.subID = subID;
	}
	
	public class AssetTrack implements Comparable, IMappingAssetTracker {
		private WeakReference<Asset> asset = null;
		private boolean follow = false;
		private boolean showHistory = true;
		private boolean showLimitedHistory = false;
		private String altitudeMode = "CLAMP_TO_GROUND";
		private boolean hasForcedFollow = false;
		private WeakReference<PointIncident> current = new WeakReference<PointIncident>(null);
		List<MapPlace> history = null;
		
		public AssetTrack(Asset asset, PointIncident current, PointIncident[] history, boolean follow, boolean zoomTo) {
			this.asset = new WeakReference<Asset>(asset);
			this.follow = follow;
			altitudeMode = "CLAMP_TO_GROUND";
			if (asset instanceof Aircraft) {
				altitudeMode = "ABSOLUTE";
			}
			this.history = new ArrayList<MapPlace>();
			this.setCurrent(current);
			this.setHistory(history);
		}
		public Asset getAsset() {
			return this.asset.get();
		}
		private PointIncident getCurrent() {
			synchronized(this.current) {
				return this.current.get();
			}
		}
		private void setCurrent(PointIncident incident) {
			this.current = new WeakReference<PointIncident>(incident);
		}
		public void moveCurrentPoint(PointIncident newIncident) {
			synchronized(this.current) {
				PointIncident current = this.getCurrent();
				if (current != null && newIncident.getTimestamp().before(current.getTimestamp()))
					return;
				if (current == null || newIncident == null) {
					logger.severe("Error: lastIncident == null || incident == null");
					return;
				}
				MapPlace newPlace = new MapPlace(GoogleEarth.this,current);
				this.history.add(newPlace);
				Asset asset = this.getAsset();
				Unit unit = newIncident.getUnit();
				Calendar start = Calendar.getInstance();
				start.setTime(newIncident.getTimestamp());
				start.add(Calendar.HOUR, -12);
				boolean alarmed = DAO.localDAO().alarmActivatedForAssetAfter(asset, start.getTime());
				if (alarmed && hasForcedFollow == false) {
					hasForcedFollow = true;
					GoogleEarth.this.infoBar.switchOnFollowAssetTrack(this);
				}
				// Replace existing placemark with history placemark
				String imageName = asset.getRotatedImageName(current,asset.historyImageName());
				if (alarmed) {
					String[] tokens = imageName.split("\\.");
					imageName = String.format("%s_alarm.%s", tokens[0],tokens[1]);
				}
				String iconHref = imageURL+imageName;
				boolean result = gi.updatePlacemarkIcon(unit.getUnitName()+"_"+current.getId(),false,iconHref,0.5);
				if (result != true)
					logger.severe("Error with running 'replacePlacemark' script");
				this.createPoint(newIncident, false);
				this.setCurrent(newIncident);
				if (follow) {
					GoogleEarth.this.gotoPlace(newIncident.getLatitude(), newIncident.getLongitude(), true);
				}
				if (this.showLimitedHistory && history.size() > 5) {
					this.removeFirstPoint();
				}
			}
		}
		private void createPoint(PointIncident incident, boolean historyPoint) {
			synchronized(this.current) {
				PointIncident current = this.current.get();
				if (historyPoint == false && current != null && incident.getTimestamp().before(current.getTimestamp()))
					return;
				Asset asset = this.getAsset();
				Unit unit = incident.getUnit();
				SimpleStatus event = null;
				Calendar start = Calendar.getInstance();
				start.add(Calendar.HOUR, -12);
				String status = KMLUtilities.statusDescription(asset,event);
				String cdata = "<html>"+
				"Unit %s<br>Speed %d %s<br>Altitude %d metres / %d feet<br>"+
				"Lat/Lon %s (%s)<br>"+
				"Last message:<b>%s</b><br>Incident: %s"+
				"</html>";
				cdata = String.format(cdata, unit.getUnitName(),
						(int)asset.getIncidentSpeed(incident),asset.getSpeedIndicator(),
						(int)incident.getAltitude(),(int)(incident.getAltitude()*3.28),
						GeoUtilities.formatLatLon(incident.getLatitude(), incident.getLongitude(), GeoUtilities.DEGREES_MINS_SECS),
						GeoUtilities.formatLatLon(incident.getLatitude(), incident.getLongitude(), GeoUtilities.DECIMAL_DEGREES),
						Formatting.format(incident.getTimestamp(),"yyyy/MM/dd HH:mm:ss"),status).replace("'", "");
				boolean alarmed = DAO.localDAO().alarmActivatedForAssetAfter(asset, start.getTime());
				if (alarmed && hasForcedFollow == false) {
					hasForcedFollow = true;
					GoogleEarth.this.infoBar.switchOnFollowAssetTrack(this);
				}
				String iconHref = null;
				String imageName = null; 
				double iconScale = 0.75;
				String name = "";
				if (historyPoint) {
					iconScale = 0.5;
					imageName = asset.getRotatedImageName(incident,asset.historyImageName());
				} else {
					imageName = asset.getImageName(incident,asset.assetImageName());
					name = asset.getAssetName();
				}
				if (alarmed) {
					String[] tokens = imageName.split("\\.");
					imageName = String.format("%s_alarm.%s", tokens[0],tokens[1]);
				}
				iconHref = imageURL+imageName;
				boolean result = gi.addPlacemark(unit.getUnitName()+"_"+incident.getId(),name,cdata, 
						true,true, 
						iconHref,iconScale, 
						incident.getLatitude(),incident.getLongitude(),incident.getAltitude(),altitudeMode);
				if (result != true)
					logger.severe("Error with running 'addPlacemark' script");
				if (this.showHistory == true) {
					result = gi.addLinePoint(this.lineId(), 2, this.trackColor(), 
							incident.getLatitude(), incident.getLongitude(), incident.getAltitude());
					if (result != true)
						logger.severe("Error with running 'addLinePoint' script");
				}
			}
		}
		public void setHistory(PointIncident[] incidents) {
			this.removeHistory();
			if (incidents != null && incidents.length > 0) {
				logger.fine("Start building...");
				PointIncident current = this.getCurrent();
				for (int i=0;i<incidents.length;i++) {
					PointIncident incident = (PointIncident)incidents[i];
					if (incident.equals(current)) {
						logger.finer("Current incident found in history, skipping");
						continue;
					}
					MapPlace newPlace = new MapPlace(GoogleEarth.this,incident);
					this.history.add(newPlace);
					System.out.println("Creating history point for incident "+incident);
					this.createPoint(incident, true);
				}
			}
			this.createPoint(this.getCurrent(),false);
			gi.showLinePoints(this.lineId(),altitudeMode);
		}
		private String lineId() {
			return this.getAsset().getId()+"_line";
		}
		private String trackColor() {
			Color trackColor = TracController.trackColor(this.getAsset());
			return String.format("%02X%02X%02X%02X",255,trackColor.getBlue(),trackColor.getGreen(),trackColor.getRed());
		}
		private void removeFirstPoint() {
			if (history != null && history.size() > 0) {
				MapPlace mapPlace = history.get(0);
				Unit unit = history.get(0).getIncident().getUnit();
				PointIncident incident = mapPlace.getIncident();
				String identifier = unit.getUnitName()+"_"+incident.getId();
				gi.removePlacemark(identifier);
				gi.clearFirstLinePoint(this.lineId());
				history.remove(0);
			}
		}
		private void removeHistory() {
			if (history.size() > 0) {
				Unit unit = history.get(0).getIncident().getUnit();
				for (MapPlace mapPlace : history) {
					PointIncident incident = mapPlace.getIncident();
					String identifier = unit.getUnitName()+"_"+incident.getId();
					gi.removePlacemark(identifier);
				}
			}
			gi.clearLine(this.lineId());
		}
		public void removeAll() {
			PointIncident incident = this.getCurrent();
			Unit unit = incident.getUnit();
			String identifier = unit.getUnitName()+"_"+incident.getId();
			gi.removePlacemark(identifier);
			removeHistory();
		}
		public void zoomToAsset() {
			PointIncident current = this.getCurrent();
			if (current != null) {
				Position currentPosition = gi.currentEyePosition();
				if (currentPosition != null) {
					double eyeElev = currentPosition.range;
					if (eyeElev < current.getAltitude()+1000)
						eyeElev = current.getAltitude()+1000;
					GoogleEarth.this.gotoPlace(current.getLatitude(), current.getLongitude(), eyeElev, true);
				} else {
					double eyeElev = current.getAltitude()+6000.0;
					GoogleEarth.this.gotoPlace(current.getLatitude(), current.getLongitude(), eyeElev, true);
				}
			}
		}
		public void clearHistory() {
			this.removeHistory();
			this.history = new ArrayList<MapPlace>();
			PointIncident current = this.getCurrent();
			boolean result = gi.addLinePoint(this.lineId(), 2, this.trackColor(), 
					current.getLatitude(), current.getLongitude(), current.getAltitude());
			if (result != true)
				logger.severe("Error with running 'addLinePoint' script");
		}
		public boolean equals(Object o) {
	        if (this == o)
	            return true;
	        if (o == null || getClass() != o.getClass())
	            return false;
	    	return (((AssetTrack)o).asset.get().getId() == this.asset.get().getId());
	    }
		public int compareTo(Object arg0) {
			return this.getAsset().getAssetName().compareTo(((AssetTrack)arg0).getAsset().getAssetName());
		}
		public boolean isFollow() {
			return follow;
		}
		public void setFollow(boolean follow) {
			this.follow = follow;
		}
		public boolean isShowHistory() {
			return showHistory;
		}
		public void setShowHistory(boolean showLine) {
			this.showHistory = showLine;
			gi.clearLine(this.lineId());
			if (showLine == true) {
				if (history.size() > 0) {
					for (MapPlace mapPlace : history) {
						PointIncident incident = mapPlace.getIncident();
						boolean result = gi.addLinePoint(this.lineId(), 2, this.trackColor(), 
								incident.getLatitude(), incident.getLongitude(), incident.getAltitude());
						if (result != true)
							logger.severe("Error with running 'addLinePoint' script");
					}
				}
			}
		}
		public boolean isShowLimitedHistory() {
			return showLimitedHistory;
		}
		public void setShowLimitedHistory(boolean showLine) {
			this.showLimitedHistory = showLine;
			if (showLine == true)
				this.clearHistory();
			else {
				if (history.size() > 0) {
					for (MapPlace mapPlace : history) {
						PointIncident incident = mapPlace.getIncident();
						boolean result = gi.addLinePoint(this.lineId(), 2, this.trackColor(), 
								incident.getLatitude(), incident.getLongitude(), incident.getAltitude());
						if (result != true)
							logger.severe("Error with running 'addLinePoint' script");
					}
				}
			}
		}
	}
	
	class StatusBar {
		private StatusLineContributionItem latlonItem;
		private StatusLineContributionItem eleItem;
		private StatusLineContributionItem eyeItem;
		private SubStatusLineManager statusManager;

		StatusBar(final IStatusLineManager statusLineManager) {
			this.latlonItem = new StatusLineContributionItem("latlonItem",true,30); //$NON-NLS-1$
			this.eleItem = new StatusLineContributionItem("eleItem",true,15); //$NON-NLS-1$
			this.eyeItem = new StatusLineContributionItem("eyeItem",true,18); //$NON-NLS-1$
			this.statusManager = new SubStatusLineManager(statusLineManager);
			this.statusManager.add(this.latlonItem);
			this.statusManager.add(this.eleItem);
			this.statusManager.add(this.eyeItem);
			this.statusManager.setVisible(true);
		}

		public void handleCursorPositionChange(final Position newPos) {
			Display.getDefault().asyncExec( new Runnable() {
				public void run() {
					if (newPos != null) {
						String lls = Formatting.formatLatLon(newPos.latitude,newPos.longitude);
						String els = Formatting.formatElevation(newPos.altitude);
				        String eye = "Eye: N/A";
				        eye = Formatting.formatEyeAltitude(newPos.range);
						StatusBar.this.latlonItem.setText(lls);
						StatusBar.this.eleItem.setText(els);
						StatusBar.this.eyeItem.setText(eye);
					} else {
						StatusBar.this.latlonItem.setText(""); //$NON-NLS-1$
						StatusBar.this.eleItem.setText(""); //$NON-NLS-1$
						StatusBar.this.eyeItem.setText(""); //$NON-NLS-1$
					}
				}
			} );
		}
	}
	public GoogleEarth() {
		logger.finest("GoogleEarth()");
		logger.info("Free memory during allocation: "+Runtime.getRuntime().freeMemory());
		TracController.singleton().setCurrentMappingView(this);
		logger.finest("exit GoogleEarth()");
	}
	public void dispose() {
		logger.finer("GoogleEarth being disposed...");
		getSite().getPage().removePartListener(partlistener);
		partlistener = null;
		activeAssets = null;
		selectionListeners = null;
		TracController.singleton().setCurrentMappingView(null);
		super.dispose();
		Runtime.getRuntime().gc();
		logger.finer("Free memory after dispose: "+Runtime.getRuntime().freeMemory());
	}
	
	public HashMap<String,GeoArea> getAvailableFeatures() {
		return features;
	}
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
        imageURL = System.getProperty("com.ikno.imageurl", "http://www.i-see.co.za/images/");
        if (!imageURL.endsWith("/"))
        	imageURL = imageURL+"/";
		container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// $hide<<$
		gi = new GoogleInterface(container, this);
		
		infoBar = new InfoBar(GoogleEarth.this, container, SWT.NONE);
		infoBar.setBackground(SWTResourceManager.getColor(192,192,192));
		infoBarWidth = 0;
		infoBar.minimized = true;

		separator = new Separator(GoogleEarth.this, container, SWT.NONE);
		separator.setBackground(SWTResourceManager.getColor(227,222,202));
		
		IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		statusBar = new StatusBar(statusLineManager);
		// $hide>>$
		
		createActions();
		initializeToolBar();
		initializeMenu();

		container.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				logger.finer("Resize event: "+container.getSize());
	        	if (gi != null && gi.googleReady() == true) {
	        		Point size = container.getSize();
	        		infoBar.setBounds(0, 0, infoBarWidth, size.y);
	        		separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
	        		gi.resizeMap((infoBarWidth+Separator.BARWIDTH), 0, (size.x-infoBarWidth-Separator.BARWIDTH), size.y);
	        	}
			}
		});
		getSite().getPage().addPartListener(partlistener);
		getViewSite().setSelectionProvider(this);
		Type[] interest = new Type[]{
				EntityNotification.Type.SAVE
				};
		TracController.singleton().addEntityChangeListener(this,interest,GeoPoint.class.getName(),null);
	}

    IPartListener2 partlistener = new IPartListener2() {
        public void partActivated( IWorkbenchPartReference partRef ) {
        }

        public void partBroughtToTop( IWorkbenchPartReference partRef ) {
        }

        public void partClosed( IWorkbenchPartReference partRef ) {
        }

        public void partDeactivated( IWorkbenchPartReference partRef ) {
        }

        public void partOpened( IWorkbenchPartReference partRef ) {
        	if (gi != null && gi.connected() == false) {
        		try {
					gi.openConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		refreshAction.setEnabled(true);
			}
        }

        public void partHidden( IWorkbenchPartReference partRef ) {
        }

        public void partVisible( IWorkbenchPartReference partRef ) {
        }

        public void partInputChanged( IWorkbenchPartReference partRef ) {
        }
    };

	public void requestFocus() {
		System.out.println("Requesting focus");
		GoogleEarth.this.getSite().getPage().activate(GoogleEarth.this.getSite().getPart());
		System.out.println("Requested focus");
	}
	/**
	 * Create the actions
	 */
	private void createActions() {
		pluginVersionAction = new Action("Plugin") {
			public void run() {
				String version = gi.softwareVersion();
				System.out.println("Plugin Version: "+version);
				MessageBox messageBox = new MessageBox (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_INFORMATION);
				messageBox.setText("Plugin Version");
				messageBox.setMessage("Version: "+version);
				messageBox.open();
			}
		};
		refreshAction = new Action("Refresh") {
			public void run() {
				gi.refresh();
				refreshAction.setEnabled(true);
			}
		};
		refreshAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Refresh Document 16 n p8.png"));
		refreshAction.setEnabled(false);
		layersAction = new Action("Layers") {
			public void run() {
				if (layerViewDialog == null)
					layerViewDialog = new LayerViewDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),GoogleEarth.this);
				if (layerViewDialog.isOpen)
					layerViewDialog.getParent().setFocus();
				else
					layerViewDialog.open();
			}
		};
		layersAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Layers 16.png"));
		openKMLAction = new Action("Open KML") {
			public void run() {
				InputDialog dlg = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"URL","Please supply a URL to fetch",lastURL,null);
				int result = dlg.open();
				if (result == Window.OK) {
					String url = dlg.getValue();
					if (url != null && !url.equals("")) {
						lastURL = url;
						gi.fetchKML(url,url,false);
					}
				}
			}
		};
		openKMLAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Download from Web 16 n p8.png"));
		addWaypointAction = new Action("Add Waypoint") {
			public void run() {
				Position current = gi.currentEyePosition();
				GeoPoint waypoint = new GeoPoint("New Waypoint", "A New Waypoint",imageURL+"Flag_Yellow_32_n_p8.png",(float)0.6,(float)current.latitude,(float)current.longitude,0,10);
				String id = "WPT_"+featureCount++;
				features.put(id, waypoint);
				gi.addMoveableWaypoint(id, waypoint);
				selected = new Feature(id,waypoint);
				if (waypointDialog == null)
					waypointDialog = new WaypointDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),GoogleEarth.this);
				if (waypointDialog.isOpen) {
					waypointDialog.setPlacemark(selected);
				} else
					waypointDialog.open(selected);
			}
		};
		addWaypointAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Flag Yellow 16 n p8.png"));
		createPolygonAction = new Action("Add Area") {
			public void run() {
				PolygonArea polygon = new PolygonArea("New Area", "A New Area");
				String id = "POLY_"+featureCount++;
				features.put(id, polygon);
				selected = new Feature(id,polygon);
				if (polygonDialog == null)
					polygonDialog = new PolygonDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),GoogleEarth.this);
				if (polygonDialog.isOpen) {
					polygonDialog.setFeature(selected);
				} else
					polygonDialog.open(selected);
			}
		};
		createPolygonAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Shapes Colored 16 h p8.png"));
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(pluginVersionAction);
		toolbarManager.add(refreshAction);
		toolbarManager.add(layersAction);
		toolbarManager.add(openKMLAction);
		toolbarManager.add(addWaypointAction);
		toolbarManager.add(createPolygonAction);
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setFocus() {
		logger.info("Received a setFocus() call");
		TracController.singleton().setCurrentMappingView(this);
	}
	
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		if (PersistantObject.instanceOf(entityName, PointIncident.class)) {
			logger.finer("PointIncident Notification received");
			Long assetId = notification.getLongAttribute("assetId");
			if (assetId != null && activeAssets.containsKey(assetId)) {
				PointIncident incident = (PointIncident)notification.getResolved();
				activeAssets.get(assetId).moveCurrentPoint(incident);
			}
		} else if (PersistantObject.instanceOf(entityName, GeoPoint.class)) {
			logger.finer("GeoPoint Notification received");
			Long layerId = notification.getLongAttribute("mapLayerId");
			this.updateLayerWithId(layerId);
		}

	}
	public void onEntityNotFound(EntityNotification notification) {
		// TODO Auto-generated method stub
		
	}
	public void minimizeInfoBar() {
		if (this.infoBarWidth == 0)
			return;
		this.infoBarWidth = 0;
		Point size = container.getSize();
		infoBar.setBounds(0, 0, infoBarWidth, size.y);
		infoBar.minimized = true;
		separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
		gi.resizeMap((infoBarWidth+Separator.BARWIDTH), 0, (size.x-infoBarWidth-Separator.BARWIDTH), size.y);
	}
	public void maximizeInfoBar() {
		if (this.infoBarWidth == InfoBar.CANVASWIDTH)
			return;
		this.infoBarWidth = InfoBar.CANVASWIDTH;
		infoBar.minimized = false;
		Point size = container.getSize();
		infoBar.setBounds(0, 0, infoBarWidth, size.y);
		separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
		gi.resizeMap((infoBarWidth+Separator.BARWIDTH), 0, (size.x-infoBarWidth-Separator.BARWIDTH), size.y);
	}
	List<MapLayer> interimLayers = new ArrayList<MapLayer>();
	
	public void addLayer(MapLayer mapLayer) {
		if (!this.layers.contains(mapLayer))
			this.layers.add(mapLayer);
		KMLLayer kmlLayer = (KMLLayer)mapLayer;
		String url = kmlLayer.getUrl(); 
		if (url != null) {
			url = url.replace("%USERID%", ""+TracController.getLoggedIn().getId());
			logger.finer("Fetching KML file with URL "+url);
			boolean result = gi.fetchKML(url,kmlLayer.getRootIdentifier(),!kmlLayer.isLinkedToKML());
			if (result != true)
				logger.finer("Error fetching KML file with URL "+url);
		}
	}

	public void removeLayer(MapLayer mapLayer) {
		if (this.layers.contains(mapLayer)) {
			KMLLayer kmlLayer = (KMLLayer)mapLayer;
			String rootId = kmlLayer.getRootIdentifier();
			boolean result = gi.removeKML(rootId);
			if (result != true)
				logger.finer("Error removing KML file with RootID "+rootId);
			this.layers.remove(mapLayer);
		}
	}

	public void updateLayerWithId(long id) {
		logger.finer("Update maplayer with id "+id);
		KMLLayer layer = (KMLLayer)mapLayerWithId(id);
		if (layer != null) {
			gi.removePlacemark(layer.getRootIdentifier());
			this.addLayer(layer);
		} else
			logger.finer("Layer with id "+id+" not found");
	}
	public MapLayer mapLayerWithId(long id) {
		for (MapLayer layer : layers) {
			if (layer.getId() == id)
				return layer;
		}
		return null;
	}
	public void interfaceReady() {
		logger.finer("Google loaded...");
		if (eyePosition != null) {
			gi.zoomTo(eyePosition.latitude, eyePosition.longitude, eyePosition.range);
		}
		this.layers.clear();
		for (MapLayer layer : interimLayers) {
			this.addLayer(layer);
		}
	}
	public void interfaceReady_new() {
		logger.finer("Google loaded...");
		Point size = container.getSize();
		infoBar.setBounds(0, 0, infoBarWidth, size.y);
		separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
		gi.resizeMap((infoBarWidth+Separator.BARWIDTH), 0, (size.x-infoBarWidth-Separator.BARWIDTH), size.y);
		if (eyePosition != null) {
			gi.zoomTo(eyePosition.latitude, eyePosition.longitude, eyePosition.range);
		}
		this.layers.clear();
		for (MapLayer layer : interimLayers) {
			this.addLayer(layer);
		}
	}

	public void kmlLayerLoaded(String id) {
		boolean found = false;
		for (MapLayer layer : layers) {
			KMLLayer kmlLayer = (KMLLayer)layer;
			if (kmlLayer.getRootIdentifier().equals(id)) {
				found = true;
				GoogleEarth.this.changeLayerVisibility(kmlLayer, kmlLayer.isVisible());
			}
		}
		if (found) {
			logger.finer("Processed KML with ID "+id);
		}
			
	}
	public void AddAssetWithHistory(Asset asset, PointIncident[] history, boolean zoomTo) {
		if (this.activeAssets.containsKey(asset.getId())) {
			this.activeAssets.get(asset.getId()).setHistory(history);
			if (zoomTo) {
				IMappingAssetTracker assetTrack = this.activeAssets.get(asset.getId());
				assetTrack.zoomToAsset();
			}
			return;
		}
		PointIncident incident = (PointIncident) asset.getLastIncident(PointIncident.class.getSimpleName());
		if (incident != null) {
			AssetTrack assetTrack = new AssetTrack(asset,incident,history,false,zoomTo);
			this.activeAssets.put(asset.getId(), assetTrack);
			this.infoBar.addAssetTrack(assetTrack);
			if (zoomTo)
				gotoPlace(incident.getLatitude(), incident.getLongitude(), incident.getAltitude()+6000, true);
			Type[] interest = new Type[]{
					EntityNotification.Type.UPDATE,
					EntityNotification.Type.SAVE_OR_UPDATE,
					EntityNotification.Type.SAVE,
					EntityNotification.Type.DELETE
					};
			TracController.singleton().addEntityChangeListener(this,interest,PointIncident.class.getName(),null);
			logger.finer("Asset added, will monitor notifications");
		}
	}
	public void removeAssetTrack(IMappingAssetTracker assetTrack) {
		this.activeAssets.remove(assetTrack.getAsset().getId());
		this.infoBar.removeAssetTrack(assetTrack);
		assetTrack.removeAll();
	}
	public void GotoIncident(PointIncident place, double eyeElevation,
			boolean adjustIfAbove, boolean animate) {
		// TODO Auto-generated method stub
		
	}
	public Track ShowLineTrack(PointIncident[] incidents, String trackName,
			Color pinColor, Color trackColor, double eyeElevation,
			boolean adjustEyeLevel, boolean followsTerrain, boolean animate) {
		// TODO Auto-generated method stub
		return null;
	}
	public void addTrack(Track track) {
		// TODO Auto-generated method stub
		
	}
	public void addTrackListener(ITrackListener listener) {
		// TODO Auto-generated method stub
		
	}
	public MapPlace anchorAltitude(MapPlace place) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean eyeElevationLowerThan(double eyeElevation) {
		// TODO Auto-generated method stub
		return false;
	}
	public GeoArea[] getGeoAreas() {
		// TODO Auto-generated method stub
		return null;
	}
	public void gotoPlace(double latitude, double longitude,double eyeElevation, boolean adjustIfAbove, boolean animate) {
		gi.zoomTo(latitude,longitude, eyeElevation);
	}
	public void gotoPlace(double latitude, double longitude,double eyeElevation, boolean animate) {
		gi.zoomTo(latitude,longitude, eyeElevation);
	}
	public void gotoPlace(double latitude, double longitude, boolean animate) {
		Position eyePosition = gi.currentEyePosition();
		double eyeAltitude = 10000;
		if (eyePosition != null)
			eyeAltitude = eyePosition.range;
		gi.zoomTo(latitude,longitude, eyeAltitude);
	}
	public boolean isVisible(double latitude, double longitude,double altitude) {
		// TODO Auto-generated method stub
		return false;
	}
	public List<MapLayer> layerList() {
		List<MapLayer> selectable = new ArrayList<MapLayer>();
		for (MapLayer layer : this.layers) {
			if (layer.isSelectable())
				selectable.add(layer);
		}
		return selectable;
	}
	public List<com.ikno.itracclient.Track> trackList() {
		return null;
	}

	public void changeLayerVisibility(MapLayer layer, boolean visible) {
		boolean result = gi.changeVisibility(((KMLLayer)layer).getRootIdentifier(), visible);
		layer.setVisible(visible);
		if (result != true)
			logger.finer("Error changing visibility");
	}

	public void removeGeoArea(GeoArea geoArea) {
		// TODO Auto-generated method stub
		
	}
	public void showGeoArea(GeoArea geoArea) {
		// TODO Auto-generated method stub
		
	}
	public void geoAreaAdded(GeoArea geoArea) {
		
	}
	public void removeTrackListener(ITrackListener listener) {
		// TODO Auto-generated method stub
		
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			double latitude = 4.5;
			double longitude = 20.5;
			double range = 5000000.0;
			try {
				latitude = Double.parseDouble(memento.getString("eyeLatitude"));
				longitude = Double.parseDouble(memento.getString("eyeLongitude"));
				range = Double.parseDouble(memento.getString("eyeElevation"));
				this.eyePosition = new Position(latitude,longitude,0,range);
			} catch (Exception e) {}
		}
		List<KMLLayer> kmlLayers = DAO.localDAO().getSharedKMLLayersForUser(TracController.getLoggedIn());
		if (kmlLayers != null) {
			for (KMLLayer kmlLayer : kmlLayers) {
				interimLayers.add(kmlLayer);
				if (memento != null) {
					String senabled = memento.getString(kmlLayer.getName().replace(" ", "_"));
					if (senabled != null) {
						boolean visible = Boolean.parseBoolean(senabled);
						kmlLayer.setVisible(visible);
					}
				}
			}
		}
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		try {
			if (memento != null && gi != null) {
				Position eyePosition = gi.currentEyePosition();
				if (eyePosition != null) {
					double latitude = eyePosition.latitude;
					memento.putString("eyeLatitude", String.format("%.8f", latitude));
					double longitude = eyePosition.longitude;
					memento.putString("eyeLongitude", String.format("%.8f", longitude));
					double range = eyePosition.range;
					memento.putString("eyeElevation", String.format("%.8f", range));
				}
				if (this.layers != null) {
					for (MapLayer layer : this.layers) {
						boolean flag = layer.isVisible();
						if (flag)
							memento.putString(layer.getName().replace(" ", "_"), "true");
						else
							memento.putString(layer.getName().replace(" ", "_"), "false");
					}
				}
			}
			super.saveState(memento);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error saving state: ",e);
		}
	}
	public void setSelectedAsset(Asset asset) {
		this.selectedAsset = asset;
		this.fireSelectionChanged();
	}
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null && !selectionListeners.contains(listener))
			selectionListeners.add(listener);
	}
	public ISelection getSelection() {
		if (selectedAsset != null)
			return new StructuredSelection(selectedAsset);
		return null;
	}
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}
	public void setSelection(ISelection selection) {
		this.selectedAsset = (Asset)((IStructuredSelection)selection).getFirstElement();
	}
	private void fireSelectionChanged() {
		AssetWrapper wrapper = new AssetWrapper(selectedAsset.getIdentifier(),selectedAsset.getId());
		final SelectionChangedEvent e = new SelectionChangedEvent(this,new StructuredSelection(wrapper));
		for (final ISelectionChangedListener l : selectionListeners) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}
}
