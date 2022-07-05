package com.ikno.itracclient.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.Earth.BMNGSurfaceLayer;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.UTMGraticuleLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.tracks.Track;
import itracclient.Activator;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.PolyLine;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.business.WWLayer;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingAssetTracker;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.ITrackListener;
import com.ikno.itracclient.MapPlace;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.worldwind.layers.DSHighRes;
import com.ikno.itracclient.worldwind.layers.StreetDetailMSLayer;
import com.ikno.itracclient.worldwind.layers.WorldBordersMetacartaLayer;
import com.ikno.itracclient.worldwind.layers.ZoomitLevel;

public class ActiveWorldWindView extends ViewPart implements ISelectionProvider, IMappingView, IEntityChangeListener {
	private static final Logger logger = Logging.getLogger(ActiveWorldWindView.class.getName());
	public static final String ID = "com.ikno.itracclient.worldwind.ActiveWorldWindView"; //$NON-NLS-1$
	public static final String VIEW_NAME = "Active WorldWind Map";
	private String subID = null;

	public String getViewId() {
		if (subID != null)
			return subID;
		return ActiveWorldWindView.ID;
	}
	public void setSubID(String subID) {
		this.subID = subID;
	}
	public int maxViewCount() { 
		return 5; 
	}
	private Position eyePosition = new Position(LatLon.fromDegrees(4.5, 20.5),5000000.0);
	private double eyePitch = 1.0;
	private double eyeZoom = 5000000.0;
	protected LayerList layerList = new LayerList();
	protected ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
	protected ArrayList<com.ikno.itracclient.Track> tracks = new ArrayList<com.ikno.itracclient.Track>();
	private ArrayList<ITrackListener> trackListeners = new ArrayList<ITrackListener>();
	public WorldWindWidget worldWindWidget;
    public AnnotationLayer annotationLayer = new AnnotationLayer();
    public RenderableLayer geoAreaLayer = new RenderableLayer();

	private Map<Long,AssetTrack> activeAssets = new HashMap<Long,AssetTrack>();
	private Queue<Long> queue = new LinkedList<Long>();
	private AnnotationLayer labelAnnotationLayer = new AnnotationLayer();
	private Timer timer = null;
	List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	private Asset selectedAsset = null;
	
	public class LabelAnnotation extends ScreenAnnotation {
		private WeakReference<Asset> asset = null;
		private int index = 0;
		public LabelAnnotation(String text, Point point, Asset asset) {
			super(text,point);
			this.asset = new WeakReference<Asset>(asset);
		}
		public Asset getAsset() {
			return this.asset.get();
		}
		public int getIndex() {
			return this.index;
		}
		public void setIndex(int value) {
			this.index = value;
		}
	}
	private static AssetTrack currentFollow = null;

	public class AssetTrack implements SelectListener, Comparable {
		private WeakReference<Asset> asset = null;
		private boolean follow = false;
		private Point drawOffset = null;
		private com.ikno.itracclient.Track lastTrack = null;
		private WeakReference<PointIncident> incident = new WeakReference<PointIncident>(null);
		private LabelAnnotation labelAnnotation = null;
		
		public AssetTrack(Asset asset, com.ikno.itracclient.Track track, boolean follow, boolean zoomTo) {
			this.asset = new WeakReference<Asset>(asset);
			this.follow = follow;
			this.lastTrack = track;
		}
		public Asset getAsset() {
			return this.asset.get();
		}
		public PointIncident getIncident() {
			synchronized(this.incident) {
				return this.incident.get();
			}
		}
		public com.ikno.itracclient.Track getLastTrack() {
			return lastTrack;
		}
		public void setLastTrack(com.ikno.itracclient.Track lastTrack) {
			this.lastTrack = lastTrack;
		}
		public LabelAnnotation getLabelAnnotation() {
			return labelAnnotation;
		}
		public void setLabelAnnotation(LabelAnnotation annotation) {
			this.labelAnnotation = annotation;
		}
		public void setFollow(boolean follow) {
			this.follow = follow;
		}
		public boolean follow() {
			return follow;
		}
		public void newPoint(PointIncident incident) {
			if (incident != null) {
				synchronized(this.incident) {
					if (this.incident.get() != null && incident.getTimestamp().before(this.incident.get().getTimestamp()))
						return;
					this.incident = new WeakReference<PointIncident>(incident);
					MapPlace newPlace = new MapPlace(ActiveWorldWindView.this,incident);
					com.ikno.itracclient.Track lastTrack = this.getLastTrack();
					if (lastTrack != null)
						lastTrack.getTrackLayer().moveLastPosition(newPlace);
//					double eyeAlt = incident.getAltitude()+6000.0;
					if (follow && !worldWindWidget.isWithinView(Position.fromDegrees(newPlace.getDegreesLatitude(), newPlace.getDegreesLongitude(), newPlace.getAltitude()))) {
						ActiveWorldWindView.this.gotoPlace(incident.getLatitude(), incident.getLongitude(), true);
					}
				}
			}
		}
		public void zoomToAsset() {
			if (this.incident != null) {
				PointIncident incident = this.incident.get();
				double eyeAlt = incident.getAltitude()+6000.0;
				ActiveWorldWindView.this.gotoPlace(incident.getLatitude(), incident.getLongitude(), eyeAlt, true);
			}
		}
		public void setHistory(PointIncident[] incidents) {
			MapPlace[] history = new MapPlace[incidents.length];
			for (int i=0;i<incidents.length;i++) {
				PointIncident incident = (PointIncident)incidents[i];
				history[i] = new MapPlace(ActiveWorldWindView.this,incident);
			}
			this.getLastTrack().getTrackLayer().setHistory(history);
		}
		public void clearHistory() {
			Asset asset = getAsset();
			PointIncident incident = DAO.localDAO().lastPointIncidentForAsset(asset);
			MapPlace place = new MapPlace(ActiveWorldWindView.this,incident);
			this.getLastTrack().getTrackLayer().setHistory(place);
		}
        public void selected(final SelectEvent event) {
            if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK)) {
                if (event.hasObjects()) {
                    if (event.getTopObject() instanceof LabelAnnotation && event.getTopObject().equals(labelAnnotation)) {
                    	LabelAnnotation selected = (LabelAnnotation)event.getTopObject();
                    	selectedAsset = selected.getAsset();
                    	fireSelectionChanged();
                    	com.ikno.itracclient.Track lastTrack = this.getLastTrack();
                    	if (lastTrack != null) {
                    		if (this.getIncident() != null) {
                        		lastTrack.zoomToTrack(ActiveWorldWindView.this, this.getIncident().getAltitude()+2000.0, true);
                    		} else
                    			lastTrack.zoomToTrack(ActiveWorldWindView.this, true);
                    	}
                    }
                }
            } else if (event.getEventAction().equals(SelectEvent.RIGHT_PRESS)) {
                if (event.hasObjects() && event.getTopObject() instanceof LabelAnnotation) {
                	if (event.getTopObject().equals(this.labelAnnotation)) {
                		final Shell shell = ActiveWorldWindView.this.getSite().getShell();
                		final Display display = shell.getDisplay();
                		
                		display.syncExec(new Runnable() {
                			public void run() {
                				Menu menu = new Menu(shell, SWT.POP_UP);
                				try {
                					MenuItem item = new MenuItem(menu, SWT.PUSH);
                					item.setText("Remove");
                					item.addListener(SWT.Selection, new Listener() {
                						public void handleEvent(Event e) {
                							com.ikno.itracclient.Track lastTrack = AssetTrack.this.getLastTrack();
                							if (lastTrack != null) {
                								lastTrack.remove(ActiveWorldWindView.this);
                							}
                						}
                					});
                					item = new MenuItem(menu, SWT.PUSH);
                					item.setText("Clear History");
                					item.addListener(SWT.Selection, new Listener() {
                						public void handleEvent(Event e) {
                							clearHistory();
                						}
                					});
                					item = new MenuItem(menu, SWT.PUSH);
                					item.setText("Toggle Follow");
                					item.addListener(SWT.Selection, new Listener() {
                						public void handleEvent(Event e) {
                							boolean handled = false;
                							if (currentFollow != null) {
                								currentFollow.follow = false;
                								if (currentFollow == AssetTrack.this)
                									handled = true;
                								currentFollow = null;
                							}
                							if (!handled) {
                								currentFollow = AssetTrack.this;
                								currentFollow.follow = true;
                							}
                							AssetTrack.this.buildLabel(-1);
                						}
                					});
                					item = new MenuItem(menu, SWT.PUSH);
                					item.setText("Zoom To");
                					item.addListener(SWT.Selection, new Listener() {
                						public void handleEvent(Event e) {
                							selectedAsset = AssetTrack.this.getAsset();
                							fireSelectionChanged();
                							double eyeAlt = getIncident().getAltitude()+6000.0;
                							getLastTrack().zoomToTrack(ActiveWorldWindView.this, eyeAlt, true);
                						}
                					});
                					Point clicked = event.getMouseEvent().getLocationOnScreen();
                					menu.setLocation(clicked.x,clicked.y);
                					menu.setVisible(true);
                					while (!menu.isDisposed() && menu.isVisible()) {
                						if (!display.readAndDispatch())
                							display.sleep();
                					}
                				} catch (Exception e) {
                					System.out.println("Caught: "+e);
                				} finally {
                					menu.dispose();
                				}
                				
                			}
                		});
                	}
                }
            }
        }
        public void buildLabel(int index) {
        	if (labelAnnotation != null) {
        		index = labelAnnotation.getIndex();
    	        labelAnnotationLayer.removeAnnotation(labelAnnotation);
        	} else {
        		drawOffset = new Point(0, ((index)*30)+5);
        	}
        	String text = getAsset().getAssetName();
			labelAnnotation = new LabelAnnotation(text, new Point(0, 0), getAsset());
			labelAnnotation.setIndex(index);
			labelAnnotation.getAttributes().setSize(new Dimension(300, 0));
			Insets insets = labelAnnotation.getAttributes().getInsets();
	        Dimension size = labelAnnotation.getAttributes().getSize();
	        Dimension minSize = new Dimension(20, 20);
	        // Clamp size
	        size.setSize(Math.max(size.getWidth(), minSize.getWidth()), size.getHeight() > 0 ?
	                Math.max(size.getHeight(), minSize.getHeight()) : 0);
	        // Draw area dimension - TODO: factor in border width
	        Dimension drawSize = new Dimension(
	                (int) size.getWidth() - insets.left - insets.right,
	                size.getHeight() > 0 ? Math.max((int) size.getHeight() - insets.top - insets.bottom, 1) : 0);
			Font font = Font.decode("Verdana-10");
			MultiLineTextRenderer mtr = new MultiLineTextRenderer(font);
			text = mtr.wrap(text, drawSize);
			Rectangle2D bounds = mtr.getBounds(text);
			int textWidth = (int)bounds.getWidth();
//			int textWidth = java.lang.Math.max(28, (text.length()*4)+4);
			Point screenPoint = new Point(textWidth,10);
			labelAnnotation.setScreenPoint(screenPoint);
			labelAnnotation.getAttributes().setFont(font);
			labelAnnotation.getAttributes().setTextAlign(MultiLineTextRenderer.ALIGN_LEFT);
//			labelAnnotation.getAttributes().setAdjustWidthToText("SIZE_FIT_TEXT");
			drawOffset.x = -(textWidth/2)+10;
			drawOffset.y = (index*((int)bounds.getHeight()+insets.bottom+insets.top));
			labelAnnotation.getAttributes().setDrawOffset(drawOffset); // screen point is annotation bottom left corner
			labelAnnotation.getAttributes().setScale(1);             			// No scaling
			labelAnnotation.getAttributes().setDistanceMaxScale(1);
			labelAnnotation.getAttributes().setDistanceMinScale(1);
			labelAnnotation.getAttributes().setHighlightScale(1);             	// No highlighting either
			int calloutWidth = (textWidth*2)-10;
//			labelAnnotation.getAttributes().setSize(new Dimension(calloutWidth, 30));
			labelAnnotation.getAttributes().setTextColor(java.awt.Color.BLACK);
			labelAnnotation.getAttributes().setBorderColor(java.awt.Color.BLACK);
			labelAnnotation.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_VLINEAR, new Dimension(30, 128), 1f, (follow == true) ? java.awt.Color.YELLOW : java.awt.Color.WHITE, new java.awt.Color(0f, 0f, 0f, 0f)));  // White to transparent
	        labelAnnotationLayer.addAnnotation(labelAnnotation);
        }
        
		public void add() {
			this.buildLabel(activeAssets.size()-1);
	        worldWindWidget.addSelectListener(this);
		}
		public void remove() {
			labelAnnotationLayer.removeAnnotation(labelAnnotation);
	        worldWindWidget.removeSelectListener(this);
		}
		public void rebuild(int index) {
			labelAnnotationLayer.removeAnnotation(labelAnnotation);
			labelAnnotation = null;
			buildLabel(index);
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
	}

	public ActiveWorldWindView() {
		logger.finest("ActiveWorldWindView()");
		logger.info("Free memory during allocation: "+Runtime.getRuntime().freeMemory());
		TracController.singleton().addMappingViewPart(this);
		TracController.singleton().setCurrentMappingView(this);
		logger.finest("exit ActiveWorldWindView()");
	}
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		logger.finest("createPartControl(parent)");
		worldWindWidget = new WorldWindWidget(this, parent, SWT.NONE, layerList, eyePosition, eyePitch, eyeZoom);
		logger.info("WorldWindWidget created...");
		worldWindWidget.setLayout(new FillLayout());
		initializeToolBar();
		worldWindWidget.contributeToStatusLine(getViewSite());
		worldWindWidget.contributeToActionBars(getViewSite());
		this.worldWindWidget.insertBeforeCompass(labelAnnotationLayer);
		getViewSite().setSelectionProvider(this);
		logger.finer("Free memory after starting: "+Runtime.getRuntime().freeMemory());
	}
	public void dispose() {
		logger.finer("WorldWindView being disposed...");
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		TracController.singleton().removeEntityChangeListener(this);
		activeAssets = null;
		queue = null;
		TracController.singleton().setCurrentMappingView(null);
		worldWindWidget.dispose();
		layerList = null;
		layers = null;
		tracks = null;
		selectionListeners = null;
		super.dispose();
		Runtime.getRuntime().gc();
		System.out.println("Free memory after dispose: "+Runtime.getRuntime().freeMemory());
	}
	public void setFocus() {
		logger.finest("setFocus()");
		worldWindWidget.requestFocus();
		logger.finest("exit setFocus()");
	}
	public void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		createActions();
	}
	private void createActions() {
		logger.finest("createActions()");
	}
	public boolean eyeElevationLowerThan(double eyeElevation) {
		return worldWindWidget.eyeElevationLowerThan(eyeElevation);
	}
	public boolean isVisible(double latitude, double longitude, double altitude) {
		return worldWindWidget.isVisible(latitude,longitude,altitude);
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		logger.finer("Notification received");
		if (PersistantObject.instanceOf(entityName, PointIncident.class)) {
			Long assetId = notification.getLongAttribute("assetId");
			if (assetId != null && activeAssets.containsKey(assetId)) {
				PointIncident incident = (PointIncident)notification.getResolved();
				activeAssets.get(assetId).newPoint(incident);
			}
		}
	}
	public void onEntityNotFound(EntityNotification notification) {}
	
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
	public MapPlace anchorAltitude(MapPlace place) {
		double globeElevation = this.worldWindWidget.getGlobeElevation(Angle.fromDegrees(place.getDegreesLatitude()), Angle.fromDegrees(place.getDegreesLongitude()));
		double maxElevation = this.worldWindWidget.getGlobeMaxElevation();
		System.out.println("anchorAltitude: compare place's "+place.getAltitude()+" with globe's "+globeElevation+", max elevation: "+maxElevation);
		place.setAltitude((place.getAltitude() < (globeElevation+30)) ? (globeElevation+30) : place.getAltitude());
		place.setElevation((place.getAltitude() < maxElevation) ? place.getAltitude()-globeElevation : place.getAltitude());
		return place;
	}
	private class GeoAreaShape extends SurfacePolygon {
		private GeoArea area = null;
		public GeoAreaShape(GeoArea area, List<LatLon> positions) {
			super(positions);
			this.area = area;
		}
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			return (this.area.equals(((GeoAreaShape)obj).area));
		}
	}
	public GeoArea[] getGeoAreas() {
		Iterable<Renderable> areas = this.geoAreaLayer.getRenderables();
		List<GeoArea> result = new ArrayList<GeoArea>();
		for (Renderable area : areas) {
			result.add(((GeoAreaShape)area).area);
		}
		return result.toArray(new GeoArea[]{});
	}
	public void showGeoArea(GeoArea geoArea) {
		if (geoArea instanceof PolygonArea) {
			List<LatLon> positions = new ArrayList<LatLon>();
			PolygonArea polygon = (PolygonArea)geoArea;
			PolyLine[] polyLines = polygon.getPolyLines();
			if (polyLines.length == 0)
				return;
			for (PolyLine line : polyLines) {
				positions.add(LatLon.fromDegrees(line.getFmLatitude(), line.getFmLongitude()));
			}
			PolyLine start = polyLines[0];
			Position first = Position.fromDegrees(start.getFmLatitude(), start.getFmLongitude(), 0);
			positions.add(first.getLatLon());
			final GeoAreaShape shape = new GeoAreaShape(geoArea,positions);
			Iterable<Renderable> areas = this.geoAreaLayer.getRenderables();
			for (Renderable area : areas) {
				if (area.equals(shape))
					return;
			}
			int currentPathOpacity = 5;
			float currentBorderWidth = (float)0.05;
			BasicStroke stroke = new BasicStroke(currentBorderWidth);
			float alpha = currentPathOpacity >= 10 ? 1f : currentPathOpacity <= 0 ? 0f
                    : currentPathOpacity / 10f;
			java.awt.Color borderColor = new java.awt.Color(1f, 0.2f, 0f);
			shape.setBorderColor(borderColor);
			shape.setStroke(stroke);
			java.awt.Color shapeColor = new java.awt.Color(1f, 1f, 0f, alpha);
			shape.setPaint(shapeColor);
			shape.setDrawInterior(true);
            if (shape != null)
                this.geoAreaLayer.addRenderable(shape);
            worldWindWidget.redraw();
            worldWindWidget.flytoPosition(first);
		}
	}
	public void removeGeoArea(GeoArea geoArea) {
		if (geoArea instanceof PolygonArea) {
			List<LatLon> positions = new ArrayList<LatLon>();
			PolygonArea polygon = (PolygonArea)geoArea;
			PolyLine[] polyLines = polygon.getPolyLines();
			if (polyLines.length == 0)
				return;
			for (PolyLine line : polyLines) {
				positions.add(LatLon.fromDegrees(line.getFmLatitude(), line.getFmLongitude()));
			}
			PolyLine start = polyLines[0];
			positions.add(LatLon.fromDegrees(start.getFmLatitude(), start.getFmLongitude()));
			GeoAreaShape shape = new GeoAreaShape(geoArea,positions);
			this.geoAreaLayer.removeRenderable(shape);
			worldWindWidget.redraw();
		}
	}
	public void geoAreaAdded(GeoArea geoArea) {
		
	}
	public void gotoPlace(double latitude, double longitude, double eyeElevation, boolean adjustIfAbove, boolean animate) {
		worldWindWidget.gotoPosition(Position.fromDegrees(latitude, longitude, 0.0), eyeElevation, adjustIfAbove, animate);
	}

	public void gotoPlace(double latitude, double longitude, double eyeElevation, boolean animate) {
		worldWindWidget.gotoPosition(Position.fromDegrees(latitude, longitude, 0.0), eyeElevation, true, animate);
	}

	public void gotoPlace(double latitude, double longitude, boolean animate) {
		worldWindWidget.gotoPosition(Position.fromDegrees(latitude, longitude, 0.0),animate);
	}

	private com.ikno.itracclient.Track buildAssetTrack(Asset asset, PointIncident[] incidents, Color pinColor, Color trackColor, double eyeElevation, boolean adjustEyeLevel, boolean followsTerrain, boolean animate) {
		MapPlace[] history = new MapPlace[incidents.length];
		for (int i=0;i<incidents.length;i++) {
			PointIncident incident = (PointIncident)incidents[i];
			history[i] = new MapPlace(ActiveWorldWindView.this,incident);
		}
		PointIncident incident = DAO.localDAO().lastPointIncidentForAsset(asset);
		MapPlace current = new MapPlace(ActiveWorldWindView.this,incident);
		try {
			TrackMarkerLineLayer trackLayer = new TrackMarkerLineLayer(this,asset.getAssetName(),current.getShortDescr(),current,history);
			trackLayer.setFollowsTerrain(followsTerrain);
			trackLayer.setLineWidth(2);
			java.awt.Color awtPinColor = new java.awt.Color(pinColor.getRed(),pinColor.getGreen(),pinColor.getBlue());
			trackLayer.setMarkerColor(awtPinColor);
			java.awt.Color awtLineColor = new java.awt.Color(trackColor.getRed(),trackColor.getGreen(),trackColor.getBlue());
			trackLayer.setLineColor(awtLineColor);
			if (animate)
				worldWindWidget.flytoPosition(Position.fromDegrees(current.getDegreesLatitude(), current.getDegreesLongitude(), current.getAltitude()));
			return trackLayer.itrack;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void AddAssetWithHistory(Asset asset, PointIncident[] history, boolean zoomTo) {
		if (this.activeAssets.containsKey(asset.getId())) {
			if (history != null && history.length > 0)
				this.activeAssets.get(asset.getId()).setHistory(history);
			if (zoomTo) {
				AssetTrack assetTrack = this.activeAssets.get(asset.getId());
				assetTrack.zoomToAsset();
			}
			return;
		}
		boolean followsTerrain = asset.followsTerrain();
		if (history == null) {
			history = new PointIncident[]{asset.getLastPointIncident()};
		}
		com.ikno.itracclient.Track track = buildAssetTrack(asset,history,TracController.pinColor(asset),TracController.trackColor(asset),50000.0,true,followsTerrain,zoomTo);
		AssetTrack assetTrack = new AssetTrack(asset,track,false,zoomTo);
		this.activeAssets.put(asset.getId(), assetTrack);
		assetTrack.add();
		
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE,
				EntityNotification.Type.DELETE
				};
		TracController.singleton().addEntityChangeListener(this,interest,PointIncident.class.getName(),null);
		logger.finer("Asset added, will monitor notifications");
	}

	public com.ikno.itracclient.Track ShowLineTrack(PointIncident[] incidents, String trackName, Color pinColor, Color trackColor, double eyeElevation, boolean adjustEyeLevel, boolean followsTerrain, boolean animate) {
		if (incidents.length == 0) {
			return null;
		}
		MapPlace[] places = new MapPlace[incidents.length];
		Position firstPosition = null;
		for (int i=0;i<incidents.length;i++) {
			PointIncident incident = (PointIncident)incidents[i];
			places[i] = new MapPlace(ActiveWorldWindView.this,incident);
			if (firstPosition == null)
				firstPosition = Position.fromDegrees(places[i].getDegreesLatitude(), places[i].getDegreesLongitude(), places[i].getAltitude());
		}
		try {
			TrackMarkerLineLayer trackLayer = new TrackMarkerLineLayer(this,trackName,places[0].getShortDescr(),null,places);
			java.awt.Color awtPinColor = new java.awt.Color(pinColor.getRed(),pinColor.getGreen(),pinColor.getBlue());
			trackLayer.setMarkerColor(awtPinColor);
			java.awt.Color awtLineColor = new java.awt.Color(trackColor.getRed(),trackColor.getGreen(),trackColor.getBlue());
			trackLayer.setLineColor(awtLineColor);
			if (animate)
				worldWindWidget.flytoPosition(firstPosition);
			return trackLayer.itrack;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public void GotoIncident(PointIncident place, double eyeElevation, boolean adjustIfAbove, boolean animate) {
		worldWindWidget.gotoPosition(Position.fromDegrees(place.getLatitude(), place.getLongitude(), 0.0), eyeElevation, adjustIfAbove, animate);
	}
	public List<MapLayer> layerList() {
		return this.layers;
	}
	public void addLayer(MapLayer mapLayer) {
		
	}

	public void removeLayer(MapLayer mapLayer) {
		
	}

	public List<com.ikno.itracclient.Track> trackList() {
		return this.tracks;
	}

	public void changeLayerVisibility(MapLayer layer, boolean visible) {
		((Layer)((WWLayer)layer).getReferenceLayer()).setEnabled(visible);
		this.worldWindWidget.redraw();
	}

	public void addTrackListener(ITrackListener listener) {
		if (!trackListeners.contains(listener))
			trackListeners.add(listener);
	}
	public void removeTrackListener(ITrackListener listener) {
		trackListeners.remove(listener);
	}
	
	public void removeTrack(com.ikno.itracclient.Track track) {
		Entry<Long,AssetTrack> found = null;
		for (Entry<Long,AssetTrack> mapEntry : activeAssets.entrySet()) {
			com.ikno.itracclient.Track lastTrack = mapEntry.getValue().lastTrack; 
			if (lastTrack != null && lastTrack.equals(track)) {
				found = mapEntry;
				break;
			}
		}
		if (found != null) {
			found.getValue().remove();
			activeAssets.remove(found.getKey());
			AssetTrack[] assetTracks = new AssetTrack[activeAssets.size()];
			int i = 0;
			for (Entry<Long,AssetTrack> mapEntry : activeAssets.entrySet()) {
				assetTracks[i++] = mapEntry.getValue();
			}
			Arrays.sort(assetTracks);
			for (i = 0;i<assetTracks.length;i++) {
				assetTracks[i].rebuild(i);
			}
		}
		this.tracks.remove(track);
    	for (Iterator<ITrackListener> li = trackListeners.iterator();li.hasNext();) {
    		li.next().tracksUpdated();
    	}
	}
	public void addTrack(com.ikno.itracclient.Track track) {
		this.tracks.add(track);
		for (Iterator<ITrackListener> li = trackListeners.iterator();li.hasNext();) {
			li.next().tracksUpdated();
		}
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			double latitude = 4.5;
			double longitude = 20.5;
			double elevation = 5000000.0;
			try {
				latitude = Double.parseDouble(memento.getString("eyeLatitude"));
				longitude = Double.parseDouble(memento.getString("eyeLongitude"));
				elevation = Double.parseDouble(memento.getString("eyeElevation"));
				this.eyePosition = new Position(LatLon.fromDegrees(latitude, longitude),elevation);
			} catch (Exception e) {}
			try {
				this.eyePitch = Double.parseDouble(memento.getString("eyePitch"));
			} catch (Exception e) {}
			try {
				this.eyeZoom = Double.parseDouble(memento.getString("eyeZoom"));
			} catch (Exception e) {}
			List<WWLayer> wwLayers = DAO.localDAO().getWWLayersForUser(TracController.getLoggedIn());
			for (WWLayer wwLayer : wwLayers) {
				if (wwLayer.isSelectable() && wwLayer.isVisible())
					this.layers.add(wwLayer);
				Layer referenceLayer = (Layer)wwLayer.getReferenceLayer();
				if (referenceLayer != null) {
					if (wwLayer.isVisible())
						this.layerList.add(referenceLayer);
					String senabled = memento.getString(wwLayer.getName());
					if (senabled != null) {
						boolean enabled = Boolean.parseBoolean(senabled);
						referenceLayer.setEnabled(enabled);
					}
				}
			}
		}
		if (!this.layerList.contains(geoAreaLayer))
			this.layerList.add(geoAreaLayer);
		if (!this.layerList.contains(annotationLayer))
			this.layerList.add(annotationLayer);
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		Position eyePosition = worldWindWidget.getEyeLatLonAltitude();
		double latitude = eyePosition.getLatitude().degrees;
		memento.putString("eyeLatitude", String.format("%.8f", latitude));
		double longitude = eyePosition.getLongitude().degrees;
		memento.putString("eyeLongitude", String.format("%.8f", longitude));
		double elevation = eyePosition.getElevation();
		memento.putString("eyeElevation", String.format("%.8f", elevation));
		double pitch = worldWindWidget.getEyePitch();
		memento.putString("eyePitch", String.format("%.8f", pitch));
		double zoom = worldWindWidget.getEyeZoom();
		memento.putString("eyeZoom", String.format("%.8f", zoom));
		for (MapLayer layer : this.layers) {
			boolean flag = layer.isEnabled();
			if (flag)
				memento.putString(layer.getName(), "true");
			else
				memento.putString(layer.getName(), "false");
		}
		super.saveState(memento);
	}
	@Override
	public HashMap<String, GeoArea> getAvailableFeatures() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void maximizeInfoBar() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void minimizeInfoBar() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeAssetTrack(IMappingAssetTracker assetTrack) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSelectedAsset(Asset asset) {
		// TODO Auto-generated method stub
		
	}
}
