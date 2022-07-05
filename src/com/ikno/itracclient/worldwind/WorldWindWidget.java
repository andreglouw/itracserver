package com.ikno.itracclient.worldwind;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.ScheduledOrbitViewStateIterator;

import itracclient.Activator;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.dialogs.FindDialog;
import com.ikno.itracclient.dialogs.LayerViewDialog;
import com.ikno.itracclient.dialogs.TrackViewDialog;
import com.ikno.itracclient.views.widgets.MeasurementPanel;
import com.ikno.itracclient.views.widgets.PolygonWidget;
import com.swtdesigner.ResourceManager;

public class WorldWindWidget extends Composite {
	private static final Logger logger = Logging.getLogger(WorldWindWidget.class.getName());

	private WorldWindowGLCanvas worldWindow = new WorldWindowGLCanvas();
	Frame awtFrame = null;
	private Action tracksAction;
	private Action layersAction;
	private Action findPlacesAction;
	private Action measureAction;
	private Action areaAction;
	private WeakReference<ActiveWorldWindView> controller;
	private LayerViewDialog layerViewDialog = null;
	private TrackViewDialog trackViewDialog = null;
	private FindDialog findPlacesDialog = null;
	private MeasurementPanel measurementPanel = null;
	private PolygonWidget polygonPanel = null;

	public WorldWindWidget(ActiveWorldWindView controller, Composite parent, int style, LayerList layerList, Position eyePosition, double eyePitch, double eyeZoom) {
		super(parent, style | SWT.EMBEDDED);
		this.controller = new WeakReference<ActiveWorldWindView>(controller);
		this.setLayout(new FillLayout());
		awtFrame = SWT_AWT.new_Frame(this);
		awtFrame.add(this.worldWindow);
		Model model = (Model)WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		model.setLayers(layerList);
		model.setShowWireframeExterior(false);
		model.setShowWireframeInterior(false);
		model.setShowTessellationBoundingVolumes(false);
		this.worldWindow.setModel(model);
		worldWindow.getView().setEyePosition(eyePosition);

		Transfer[] types = new Transfer[]{TextTransfer.getInstance()};
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		final DropTarget target = new DropTarget(this, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				try {
					long assetId = Long.parseLong((String)event.data);
					Asset asset = DAO.localDAO().getAssetById(assetId);
					if (asset != null) {
						WorldWindWidget.this.controller.get().AddAssetWithHistory(asset,null,false);
					}
				} catch (Exception e) {
					event.detail = DND.DROP_NONE;
					return;
				}
			}
			
		});
	}
	public void dispose() {
		this.worldWindow = null;
		this.controller = null;
		tracksAction = null;
		layersAction = null;
		findPlacesAction = null;
		measureAction = null;
		areaAction = null;
       EventQueue.invokeLater(new Runnable() {
            public void run() {
            	awtFrame.dispose();
            }
        });
		super.dispose();
	}
	public ActiveWorldWindView getController() {
		return this.controller.get();
	}
	
	public void requestFocus() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				WorldWindWidget.this.worldWindow.requestFocus();
			}
		});
	}
	public void addSelectListener(SelectListener listener) {
		this.worldWindow.addSelectListener(listener);
	}
	public void removeSelectListener(SelectListener listener) {
		this.worldWindow.removeSelectListener(listener);
	}
    public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		orbitView.addPropertyChangeListener(propertyName,listener);
    }
    public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		orbitView.removePropertyChangeListener(propertyName, listener);
    }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
    	orbitView.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
    	orbitView.removePropertyChangeListener(listener);
    }
	public Position getEyeLatLonAltitude() {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		return orbitView.getEyePosition();
	}
	public float getEyePitch() {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		return (float)orbitView.getPitch().degrees;
	}
	public float getEyeZoom() {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		return (float)orbitView.getZoom();
	}
	public double getGlobeElevation(Angle latitude, Angle longitude) {
		Globe globe = worldWindow.getModel().getGlobe();
		return globe.getElevation(latitude, longitude);
	}
	public double getGlobeElevation(Position position) {
		Globe globe = worldWindow.getModel().getGlobe();
		return globe.getElevation(position.getLatitude(), position.getLongitude());
	}
	public double getGlobeMaxElevation() {
		Globe globe = worldWindow.getModel().getGlobe();
		return globe.getMaxElevation();
	}
	public void contributeToStatusLine(IViewSite viewSite) {
		IStatusLineManager statusLineManager = viewSite.getActionBars()
			.getStatusLineManager();
		StatusBar statusBar = new StatusBar(statusLineManager);
		statusBar.setEventSource(this.worldWindow);
	}
	public void contributeToActionBars(IViewSite viewSite) {
		createActions();
		IActionBars bars = viewSite.getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown( final IMenuManager manager ) {
		// nothing yet
	}

	private void fillLocalToolBar( final IToolBarManager toolBarManager ) {
		toolBarManager.add(layersAction);
		toolBarManager.add(tracksAction);
		toolBarManager.add(findPlacesAction);
		toolBarManager.add(measureAction);
		toolBarManager.add(areaAction);
	}

	private void createActions() {
		logger.finest("createActions()");
		layersAction = new Action("Layers") {
			public void run() {
				if (layerViewDialog == null)
					layerViewDialog = new LayerViewDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),getController());
				if (layerViewDialog.isOpen)
					layerViewDialog.getParent().setFocus();
				else
					layerViewDialog.open();
			}
		};
		layersAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Layers 16.png"));
		tracksAction = new Action("Tracks") {
			public void run() {
				if (trackViewDialog == null)
					trackViewDialog = new TrackViewDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),getController());
				if (trackViewDialog.isOpen)
					trackViewDialog.getParent().setFocus();
				else
					trackViewDialog.open();
			}
		};
		tracksAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/TrackView 16.png"));
		findPlacesAction = new Action("Search") {
			public void run() {
				if (findPlacesDialog == null)
					findPlacesDialog = new FindDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),getController());
				if (findPlacesDialog.isOpen)
					findPlacesDialog.getParent().setFocus();
				else
					findPlacesDialog.open();
			}
		};
		findPlacesAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Search 16 h p8.png"));
		measureAction = new Action("Measure") {
			public void run() {
				if (measurementPanel == null)
					measurementPanel = new MeasurementPanel(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				if (measurementPanel.isOpen)
					measurementPanel.getParent().setFocus();
				else
					measurementPanel.open(worldWindow,null,null);
			}
		};
		measureAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Measure 16 n p8.png"));
		areaAction = new Action("Area") {
			public void run() {
				if (polygonPanel == null)
					polygonPanel = new PolygonWidget(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				if (polygonPanel.isOpen)
					polygonPanel.getParent().forceActive();
				else
					polygonPanel.open(worldWindow, controller.get());
			}
		};
		areaAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "images/Stock/16x16/Shapes Colored 16 h p8.png"));
		logger.finest("exit createActions()");
	}

	public void insertBeforeCompass(Layer layer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = this.worldWindow.getModel().getLayers();
        for (Layer l : layers) {
        	if (l instanceof CompassLayer)
        		compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }
    
    public void insertBeforeLayer(Layer layer, Layer newLayer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = this.worldWindow.getModel().getLayers();
        for (Layer l : layers) {
        	if (l.equals(layer))
        		compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, newLayer);
    }
    public void removeLayer(Layer layer) {
        LayerList layers = this.worldWindow.getModel().getLayers();
        layers.remove(layer);
    }
    
	public boolean eyeElevationLowerThan(double eyeElevation) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        if (orbitView != null)
        	return (orbitView.getZoom() < eyeElevation);
        return false;
	}

	public boolean isWithinView(Position position) {
		OrbitView orbitView = (OrbitView)worldWindow.getView();
		Globe globe = worldWindow.getModel().getGlobe();
		Vec4 pos = globe.computePointFromPosition(position);
		Frustum frustum = orbitView.getFrustumInModelCoordinates();
		return frustum.contains(pos);
	}
	public boolean isVisible(double latitude, double longitude, double altitude) {
		Position pos = Position.fromDegrees(latitude, longitude, altitude);
		return this.isWithinView(pos);
	}
	
	public void flytoPosition(Position position, Angle heading, Angle pitch) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        Globe globe = worldWindow.getModel().getGlobe();
        if (globe != null && orbitView != null) {
        	// Use a PanToIterator to iterate view to target position
        	orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
        			orbitView, globe, position,
        			heading, pitch, orbitView.getZoom()));
        }
	}

	public void flytoPosition(Position position) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        if (orbitView != null) {
        	flytoPosition(position,orbitView.getHeading(),orbitView.getPitch());
        }
	}
	
	public void gotoPosition(Position position, double eyeElevation, Angle heading, Angle pitch, boolean animate) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        Globe globe = worldWindow.getModel().getGlobe();
        if (globe != null && orbitView != null) {
        	if (animate)
        		// Use a PanToIterator to iterate view to target position
        		orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
        				orbitView, globe, position,
        				heading, pitch, eyeElevation));
        	else {
        		orbitView.applyStateIterator(ScheduledOrbitViewStateIterator.createCenterIterator(
        				orbitView.getCurrentEyePosition(), position));
        	}
        }
	}

	public void gotoPosition(Position position, double eyeElevation, boolean adjustIfAbove, boolean animate) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        if (orbitView != null) {
        	if (!adjustIfAbove && eyeElevation < orbitView.getZoom())
        		eyeElevation = orbitView.getZoom();
            gotoPosition(position,eyeElevation,orbitView.getHeading(),orbitView.getPitch(),animate);
        }
	}

	public void gotoPosition(Position position, boolean animate) {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        if (orbitView != null) {
        	gotoPosition(position,orbitView.getZoom(),orbitView.getHeading(),orbitView.getPitch(),animate);
        }
	}
	
	public void redraw() {
		super.redraw();
		this.worldWindow.redraw();
	}
	public boolean isInitialized() {
        OrbitView orbitView = (OrbitView)worldWindow.getView();
        if (orbitView == null)
        	return false;
		return (orbitView.getHeading() != null);
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
