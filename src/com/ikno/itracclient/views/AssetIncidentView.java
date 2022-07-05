package com.ikno.itracclient.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.utils.Formatting;
import com.ikno.itracclient.views.widgets.PointIncidentDetail;
import com.ikno.itracclient.views.widgets.SimpleStatusDetail;
import com.swtdesigner.SWTResourceManager;

public class AssetIncidentView extends ViewPart implements ISelectionListener {
	private Text assetName;
	public static final String ID = "com.ikno.itracclient.views.AssetIncidentView"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(AssetIncidentView.class.getName());

	private Composite container;
	private PointIncidentDetail pointIncidentView;
	private Group timestamp;
	private Text recorded;

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		System.currentTimeMillis();
		container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setLayout(new FormLayout());
		container.setVisible(false);
		timestamp = new Group(container, SWT.NONE);
		timestamp.setText("Time of last Incident");
		timestamp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_timestamp = new FormData();
		fd_timestamp.left = new FormAttachment(100, -194);
		fd_timestamp.bottom = new FormAttachment(0, 55);
		fd_timestamp.top = new FormAttachment(0, 5);
		fd_timestamp.right = new FormAttachment(100, -5);
		timestamp.setLayoutData(fd_timestamp);
		timestamp.setLayout(new FormLayout());

		recorded = new Text(timestamp, SWT.NONE);
		recorded.setEditable(false);
		recorded.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		recorded.setText("No Detail");
		recorded.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		final FormData fd_recorded = new FormData();
		fd_recorded.left = new FormAttachment(0, 5);
		fd_recorded.right = new FormAttachment(0, 160);
		fd_recorded.bottom = new FormAttachment(0, 26);
		fd_recorded.top = new FormAttachment(0, 5);
		recorded.setLayoutData(fd_recorded);

		final Group assetGroup = new Group(container, SWT.NONE);
		assetGroup.setText("Asset");
		assetGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_assetGroup = new FormData();
		fd_assetGroup.right = new FormAttachment(timestamp, -5, SWT.LEFT);
		fd_assetGroup.bottom = new FormAttachment(0, 55);
		fd_assetGroup.top = new FormAttachment(0, 5);
		fd_assetGroup.left = new FormAttachment(0, 5);
		assetGroup.setLayoutData(fd_assetGroup);
		assetGroup.setLayout(new FormLayout());
		
		assetName = new Text(assetGroup, SWT.CENTER);
		assetName.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		assetName.setEditable(false);
		assetName.setForeground(SWTResourceManager.getColor(0, 0, 255));
		assetName.setFont(SWTResourceManager.getFont("Times New Roman", 14, SWT.BOLD | SWT.ITALIC));
		final FormData fd_assetName = new FormData();
		fd_assetName.right = new FormAttachment(100, -5);
		fd_assetName.bottom = new FormAttachment(100, -5);
		fd_assetName.top = new FormAttachment(0, 5);
		fd_assetName.left = new FormAttachment(0, 5);
		assetName.setLayoutData(fd_assetName);
		
		pointIncidentView = new PointIncidentDetail(container, SWT.NONE);
		final FormData fd_pointIncidentView = new FormData();
		fd_pointIncidentView.bottom = new FormAttachment(0, 145);
		fd_pointIncidentView.top = new FormAttachment(0, 55);
		fd_pointIncidentView.right = new FormAttachment(100, -5);
		fd_pointIncidentView.left = new FormAttachment(0, 5);
		pointIncidentView.setLayoutData(fd_pointIncidentView);
		pointIncidentView.setLayout(new FormLayout());

		createActions();
		initializeToolBar();
		initializeMenu();
		getViewSite().getPage().addSelectionListener(AssetView.ID, this);
		IViewPart assetView = ApplicationWorkbenchWindowAdvisor.getView(AssetView.ID);
		if (assetView != null) {
			ISelection selection = assetView.getViewSite().getSelectionProvider().getSelection();
			if (selection != null) {
				this.selectionChanged(null, selection);
			}
		}
		//
	}

	@Override
	public void dispose() {
		getViewSite().getPage().removeSelectionListener(AssetView.ID, this);
		super.dispose();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void noDetail() {
		recorded.setText("No Detail");
		pointIncidentView.clear();
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object element = ssel.getFirstElement();
		Asset asset = null;
		if (element != null) {
			if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
				asset = ((AssetWrapper)element).getObject();
			} else if (PersistantObject.instanceOf(element,Unit.class)) {
				asset = ((Unit)element).getAsset();
			}
		}
		setAsset(asset);
	}
	/*
	public void setAsset(Asset asset) {
		if (asset == null) {
			container.setVisible(false);
		} else {
			List<Incident> incidents = asset.getLastIncidents();
			Date lastRecording = new Date(0);
			noDetail();
			if (incidents != null) {
				for (Iterator<Incident> ii = incidents.iterator(); ii.hasNext();) {
					Incident incident = ii.next();
					if (incident.getTimestamp().after(lastRecording))
						lastRecording = incident.getTimestamp();
					if (PersistantObject.instanceOf(incident,PointIncident.class)) {
						pointIncidentView.setPointIncident((PointIncident)incident);
					} else if (PersistantObject.instanceOf(incident,SimpleStatus.class)) {
						simpleStatusView.setSimpleStatus((SimpleStatus)incident);
					}
				}
				if (incidents.size() > 0) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					recorded.setText(formatter.format(lastRecording));
				}
			}
			container.setVisible(true);
		}
	}
	*/
	public void setAsset(Asset asset) {
		if (asset == null) {
			container.setVisible(false);
		} else {
			assetName.setText(asset.getAssetName());
	    	for (Iterator<Unit> ui = asset.getUnits().iterator(); ui.hasNext();) {
	    		Unit unit = ui.next();
				PointIncident incident = (PointIncident)unit.getLastIncident(PointIncident.class.getSimpleName());
				noDetail();
				if (incident != null) {
					pointIncidentView.setPointIncident(incident);
					recorded.setText(Formatting.format(incident.getTimestamp(),"yyyy/MM/dd HH:mm:ss z"));
					break;
				}
	    	}
			container.setVisible(true);
		}
	}

	@Override
	public void setFocus() {
		this.container.setFocus();
	}

}
