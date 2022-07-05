package com.ikno.itracclient.falconview;

import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.KMLUtilities;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingAssetTracker;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.ITrackListener;
import com.ikno.itracclient.MapPlace;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.Track;
import com.ikno.itracclient.mapping.widgets.InfoBar;
import com.ikno.itracclient.mapping.widgets.Position;
import com.ikno.itracclient.mapping.widgets.Separator;
import com.ikno.itracclient.utils.Formatting;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class FalconView extends ViewPart implements ISelectionProvider,IMappingView, IEntityChangeListener {
	private static final Logger logger = Logging.getLogger(FalconView.class.getName());
	public static final String ID = "com.ikno.itracclient.falconview.FalconView"; //$NON-NLS-1$
	private String subID = null;

	public String getViewId() {
		if (subID != null)
			return subID;
		return FalconView.ID;
	}
	public void setSubID(String subID) {
		this.subID = subID;
	}
	private Map<Long,AssetTrack> activeAssets = new HashMap<Long,AssetTrack>();
	List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	private Action pluginVersionAction;
	private Asset selectedAsset = null;
	FalconViewInterface fi = null;
	private Composite container;
	private InfoBar infoBar;
	private int infoBarWidth;
	private Separator separator;
	private String imageURL = null;
    private Text falconViewHost;
	private Position eyePosition = null;
    private Label lblFalconviewHost;
    private FormData fd_lblFalconviewHost;
	private HashMap<String,GeoArea> features = new HashMap<String,GeoArea>();
	
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
				MapPlace newPlace = new MapPlace(FalconView.this,current);
				this.history.add(newPlace);
				Asset asset = this.getAsset();
				Unit unit = newIncident.getUnit();
				Calendar start = Calendar.getInstance();
				start.setTime(newIncident.getTimestamp());
				start.add(Calendar.HOUR, -12);
				SimpleStatus event = null;
				String status = KMLUtilities.statusDescription(asset,event);
				String cdata = "<html>"+
				"Unit %s<br>Speed %d %s<br>Altitude %d metres / %d feet<br>"+
				"Last message:<b>%s</b><br>Incident: %s"+
				"</html>";
				cdata = String.format(cdata, unit.getUnitName(),
						(int)asset.getIncidentSpeed(newIncident),asset.getSpeedIndicator(),
						(int)newIncident.getAltitude(),(int)(newIncident.getAltitude()*3.28),
						Formatting.format(newIncident.getTimestamp(),"yyyy/MM/dd HH:mm:ss"),status).replace("'", "");
				boolean alarmed = DAO.localDAO().alarmActivatedForAssetAfter(asset, start.getTime());
				if (alarmed && hasForcedFollow == false) {
					hasForcedFollow = true;
					FalconView.this.infoBar.switchOnFollowAssetTrack(this);
				}
				// Replace existing placemark with history placemark
				String imageName = asset.getRotatedImageName(current,asset.historyImageName());
				if (alarmed) {
					String[] tokens = imageName.split("\\.");
					imageName = String.format("%s_alarm.%s", tokens[0],tokens[1]);
				}
				double iconScale = 0.5;
				String name = "";
				String iconHref = imageURL+imageName;

				boolean result = fi.updatePlacemark(unit.getUnitName()+"_"+current.getId(),name,cdata, 
						iconHref,iconScale, 
						newIncident.getLatitude(),newIncident.getLongitude(),newIncident.getAltitude());
				if (result != true)
					logger.severe("Error with running 'replacePlacemark' script");
				this.createPoint(newIncident, false);
				this.setCurrent(newIncident);
				if (follow) {
					FalconView.this.gotoPlace(newIncident.getLatitude(), newIncident.getLongitude(), true);
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
				Calendar start = Calendar.getInstance();
				start.add(Calendar.HOUR, -12);
				SimpleStatus event = null;
				String status = KMLUtilities.statusDescription(asset,event);
				String cdata = "<html>"+
				"Unit %s<br>Speed %d %s<br>Altitude %d metres / %d feet<br>"+
				"Last message:<b>%s</b><br>Incident: %s"+
				"</html>";
				cdata = String.format(cdata, unit.getUnitName(),
						(int)asset.getIncidentSpeed(incident),asset.getSpeedIndicator(),
						(int)incident.getAltitude(),(int)(incident.getAltitude()*3.28),
						Formatting.format(incident.getTimestamp(),"yyyy/MM/dd HH:mm:ss"),status).replace("'", "");
				boolean alarmed = DAO.localDAO().alarmActivatedForAssetAfter(asset, start.getTime());
				if (alarmed && hasForcedFollow == false) {
					hasForcedFollow = true;
					FalconView.this.infoBar.switchOnFollowAssetTrack(this);
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
				boolean result = fi.addPlacemark(unit.getUnitName()+"_"+incident.getId(),name,cdata, 
						true,true, 
						iconHref,iconScale, 
						incident.getLatitude(),incident.getLongitude(),incident.getAltitude(),altitudeMode);
				if (result != true)
					logger.severe("Error with running 'addPlacemark' script");
				if (this.showHistory == true) {
					result = fi.addLinePoint(this.lineId(), 2, this.trackColor(), 
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
					MapPlace newPlace = new MapPlace(FalconView.this,incident);
					this.history.add(newPlace);
					System.out.println("Creating history point for incident "+incident);
					this.createPoint(incident, true);
				}
			}
			this.createPoint(this.getCurrent(),false);
			fi.showLinePoints(this.lineId(),altitudeMode);
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
				fi.removePlacemark(identifier);
				fi.clearFirstLinePoint(this.lineId());
				history.remove(0);
			}
		}
		private void removeHistory() {
			if (history.size() > 0) {
				Unit unit = history.get(0).getIncident().getUnit();
				for (MapPlace mapPlace : history) {
					PointIncident incident = mapPlace.getIncident();
					String identifier = unit.getUnitName()+"_"+incident.getId();
					fi.removePlacemark(identifier);
				}
			}
			fi.clearLine(this.lineId());
		}
		public void removeAll() {
			PointIncident incident = this.getCurrent();
			Unit unit = incident.getUnit();
			String identifier = unit.getUnitName()+"_"+incident.getId();
			fi.removePlacemark(identifier);
			removeHistory();
		}
		public void zoomToAsset() {
			PointIncident current = this.getCurrent();
			if (current != null) {
				Position currentPosition = fi.currentEyePosition();
				if (currentPosition != null) {
					double eyeElev = currentPosition.range;
					if (eyeElev < current.getAltitude()+1000)
						eyeElev = current.getAltitude()+1000;
					FalconView.this.gotoPlace(current.getLatitude(), current.getLongitude(), eyeElev, true);
				} else {
					double eyeElev = current.getAltitude()+6000.0;
					FalconView.this.gotoPlace(current.getLatitude(), current.getLongitude(), eyeElev, true);
				}
			}
		}
		public void clearHistory() {
			this.removeHistory();
			this.history = new ArrayList<MapPlace>();
			PointIncident current = this.getCurrent();
			boolean result = fi.addLinePoint(this.lineId(), 2, this.trackColor(), 
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
			fi.clearLine(this.lineId());
			if (showLine == true) {
				if (history.size() > 0) {
					for (MapPlace mapPlace : history) {
						PointIncident incident = mapPlace.getIncident();
						boolean result = fi.addLinePoint(this.lineId(), 2, this.trackColor(), 
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
						boolean result = fi.addLinePoint(this.lineId(), 2, this.trackColor(), 
								incident.getLatitude(), incident.getLongitude(), incident.getAltitude());
						if (result != true)
							logger.severe("Error with running 'addLinePoint' script");
					}
				}
			}
		}
	}

	public FalconView() {
		logger.finest("FalconView()");
		logger.info("Free memory during allocation: "+Runtime.getRuntime().freeMemory());
		TracController.singleton().addMappingViewPart(this);
		TracController.singleton().setCurrentMappingView(this);
		logger.finest("exit FalconView()");
	}

	public void dispose() {
		logger.finer("FalconView being disposed...");
		getSite().getPage().removePartListener(partlistener);
		partlistener = null;
		activeAssets = null;
		selectionListeners = null;
		TracController.singleton().setCurrentMappingView(null);
		super.dispose();
		Runtime.getRuntime().gc();
		logger.finer("Free memory after dispose: "+Runtime.getRuntime().freeMemory());
	}
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
        imageURL = System.getProperty("com.ikno.imageurl", "c:/Program Files/PFPS/falcon/data/icons/user");
        if (!imageURL.endsWith("/"))
        	imageURL = imageURL+"/";
		container = new Composite(parent, SWT.NONE);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));

		fi = new FalconViewInterface(container, this);
		
		// $hide<<$
		infoBar = new InfoBar(FalconView.this, container, SWT.NONE);
		infoBar.setBackground(SWTResourceManager.getColor(192,192,192));
		infoBarWidth = 0;
		infoBar.minimized = true;

		separator = new Separator(FalconView.this, container, SWT.NONE);
		separator.setBackground(SWTResourceManager.getColor(227,222,202));
		// $hide>>$

		createActions();
		initializeToolBar();
		initializeMenu();
		
		container.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				logger.finer("Resize event: "+container.getSize());
				Point size = container.getSize();
				infoBar.setBounds(0, 0, infoBarWidth, size.y);
				separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
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
        	if (fi != null && fi.connected() == false) {
        		try {
					fi.openConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }

        public void partHidden( IWorkbenchPartReference partRef ) {
        }

        public void partVisible( IWorkbenchPartReference partRef ) {
        }

        public void partInputChanged( IWorkbenchPartReference partRef ) {
        }
    };
	private void createActions() {
		pluginVersionAction = new Action("Plugin") {
			public void run() {
				String version = fi.softwareVersion();
				System.out.println("Plugin Version: "+version);
				MessageBox messageBox = new MessageBox (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_INFORMATION);
				messageBox.setText("Falconview Version");
				messageBox.setMessage("Version: "+version);
				messageBox.open();
			}
		};
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
			.getToolBarManager();
		toolbarManager.add(pluginVersionAction);
	}

	/**
	 * Initialize the menu.
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
			// TODO Layer updated - update FalconView?
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
	}
	public void maximizeInfoBar() {
		if (this.infoBarWidth == InfoBar.CANVASWIDTH)
			return;
		this.infoBarWidth = InfoBar.CANVASWIDTH;
		infoBar.minimized = false;
		Point size = container.getSize();
		infoBar.setBounds(0, 0, infoBarWidth, size.y);
		separator.setBounds(infoBarWidth, 0, Separator.BARWIDTH, size.y);
	}

	public HashMap<String, GeoArea> getAvailableFeatures() {
		return features;
	}
	
	public void interfaceReady() {
		logger.finer("FalconView loaded...");
		if (eyePosition != null) {
			fi.zoomTo(eyePosition.latitude, eyePosition.longitude, eyePosition.range);
		}
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
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		try {
			if (memento != null && fi != null) {
				Position eyePosition = fi.currentEyePosition();
				if (eyePosition != null) {
					double latitude = eyePosition.latitude;
					memento.putString("eyeLatitude", String.format("%.8f", latitude));
					double longitude = eyePosition.longitude;
					memento.putString("eyeLongitude", String.format("%.8f", longitude));
					double range = eyePosition.range;
					memento.putString("eyeElevation", String.format("%.8f", range));
				}
			}
			super.saveState(memento);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error saving state: ",e);
		}
	}
	public void removeTrackListener(ITrackListener listener) {
		// TODO Auto-generated method stub
	}
	public void removeGeoArea(GeoArea geoArea) {
		// TODO Auto-generated method stub
	}
	public void showGeoArea(GeoArea geoArea) {
		// TODO Auto-generated method stub
	}
	public void geoAreaAdded(GeoArea geoArea) {
		// TODO Auto-generated method stub
	}
	public void addLayer(MapLayer mapLayer) {
		// TODO Auto-generated method stub
	}
	public void removeLayer(MapLayer mapLayer) {
		// TODO Auto-generated method stub
	}
	public boolean isVisible(double latitude, double longitude,double altitude) {
		// TODO Auto-generated method stub
		return false;
	}
	public List<MapLayer> layerList() {
		return null;
	}
	public List<com.ikno.itracclient.Track> trackList() {
		return null;
	}

	public void changeLayerVisibility(MapLayer layer, boolean visible) {
		boolean result = fi.changeVisibility(((KMLLayer)layer).getRootIdentifier(), visible);
		layer.setVisible(visible);
		if (result != true)
			logger.finer("Error changing visibility");
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
		fi.zoomTo(latitude,longitude, eyeElevation);
	}
	public void gotoPlace(double latitude, double longitude,double eyeElevation, boolean animate) {
		fi.zoomTo(latitude,longitude, eyeElevation);
	}
	public void gotoPlace(double latitude, double longitude, boolean animate) {
		Position eyePosition = fi.currentEyePosition();
		double eyeAltitude = 10000;
		if (eyePosition != null)
			eyeAltitude = eyePosition.range;
		fi.zoomTo(latitude,longitude, eyeAltitude);
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
