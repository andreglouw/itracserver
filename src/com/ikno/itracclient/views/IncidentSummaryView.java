package com.ikno.itracclient.views;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import itracclient.Activator;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.views.widgets.LocationSummaryDetail;
import com.ikno.itracclient.views.widgets.SelectionDetail.IQuerySelection;
import com.ikno.itracclient.views.widgets.SelectionDetail.QueryDetail;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

public class IncidentSummaryView extends ViewPart implements IQuerySelection {
	public IncidentSummaryView() {
	}
	private Button showEventsButton;
	public static final String ID = "com.ikno.itracclient.views.IncidentSummaryView"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(IncidentSummaryView.class.getName());

	private Composite container;
	private Composite incidentDetail;
	private StackLayout incidentLayout;
	private LocationSummaryDetail locationView = null;
	private Asset asset = null;
	
	private DeleteIncident deleteIncident = null;
	private ShowLineTrack showLineTrack = null;
	private ExportSelection exportSelection = null;
	private ImportTrack importTrack = null;

	class DeleteIncident extends Action {
		public DeleteIncident() {
			setText("Delete History");
			setToolTipText("Delete historical fixes");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Delete Task 16 h p8.png"));
		}
		public void run() {
			Object[] selected = locationView.getSelection(true);
			try {
				DAO.localDAO().beginTransaction();
				PointIncident[] incidents = new PointIncident[selected.length];
				for (int i=0;i<selected.length;i++) {
					PointIncident incident = (PointIncident)selected[i];
					incidents[i] = incident;
					DAO.localDAO().delete(incident);
				}
				DAO.localDAO().commitTransaction();
				locationView.removeIncidents(incidents);
			} catch (Exception e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Error deleting incidents");
				DAO.localDAO().rollbackTransaction();
			}
		}
	}
	class ShowLineTrack extends Action {
		public ShowLineTrack() {
			setText("Add Track");
			setToolTipText("Show line track on the active map");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/TrackLine 16.png"));
		}
		public void run() {
			Object[] selected = locationView.getSelection(true);
			if (selected == null || selected.length == 0) {
				return;
			}
			SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
			String defaultName = null;
			PointIncident[] incidents = new PointIncident[selected.length];
			Asset asset = null;
			for (int i=0;i<selected.length;i++) {
				PointIncident incident = (PointIncident)selected[i];
				incidents[i] = incident;
				asset = incident.getAsset();
				if (defaultName == null)
					defaultName = String.format("%s @ %s",incident.getAsset().getAssetName(),formatter.format(incident.getTimestamp()));
				if (i == selected.length-1)
					defaultName = defaultName+" - "+formatter.format(incident.getTimestamp());
			}
			IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
			mappingView.AddAssetWithHistory(asset, incidents, true);
			/*
			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),"","Enter a name for the track",defaultName,
					new IInputValidator(){
						public String isValid(String newText) {
							if (newText == null || newText.length() == 0)
								return "Error: You must supply a name";
							return null;
						}
					});
			if (dlg.open() == Window.OK) {
				Color pinColor = TracController.pinColor(asset);
				Color trackColor = TracController.trackColor(asset);
				IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
				mappingView.ShowLineTrack(incidents, dlg.getValue(), pinColor, trackColor, 500000.0, false, asset.followsTerrain(), true);
			}
			*/
		}
	}
	class ExportSelection extends Action {
		public ExportSelection() {
			setText("Export Selection");
			setToolTipText("Export a selection of points");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Export from Database 16 h p8.png"));
		}
		public void run() {
			Object[] selected = locationView.getSelection(true);
			if (selected == null || selected.length == 0) {
				return;
			}
			FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(),SWT.SAVE);
			dlg.setFileName(asset.getAssetName());
			dlg.setFilterExtensions(new String[]{"csv"});
			SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
			String fileName = dlg.open();
			if (fileName != null) {
				try {
					FileOutputStream fout = new FileOutputStream(fileName);
					for (int i=0;i<selected.length;i++) {
						PointIncident incident = (PointIncident)selected[i];
						String line = String.format("%s,%s,%s,%.5f,%.5f,%.0f,%s,%s\n", incident.getAsset().getAssetName(),formatter.format(incident.getTimestamp()),formatter.format(incident.getCreated()),incident.getLatitude(),incident.getLongitude(),incident.getAssetSpeed(),incident.getAssetSpeedIndicator(),incident.getLocation());
						fout.write(line.getBytes());
					}
					fout.close();
				} catch (IOException e) {
					logger.severe("Error writing CSV File "+fileName+":"+e);
				}
			}
		}
	}
	class ImportTrack extends Action {
		public ImportTrack() {
			setText("Import Track");
			setToolTipText("Import a track");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Import to Database 16 h p8.png"));
		}
		public void run() {
			System.out.println("Should run ImportTrack action");
		}
	}
	public void incidentSelected(PointIncident incident) {
		IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
		mappingView.GotoIncident(incident, 50000.0, false, true);
	}
	
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());
		parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container = new Composite(parent, SWT.NONE);
		final FormData fd_container = new FormData();
		fd_container.bottom = new FormAttachment(100, 0);
		fd_container.top = new FormAttachment(0, 25);
		fd_container.right = new FormAttachment(100, 0);
		fd_container.left = new FormAttachment(0, 0);
		container.setLayoutData(fd_container);
		container.setVisible(true);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setToolTipText("Query locations specific to the currently selected asset in the 'Hierarchy' view");
		container.setLayout(new FormLayout());

		incidentDetail = new Composite(container, SWT.NONE);
		incidentDetail.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		incidentLayout = new StackLayout();
		incidentDetail.setLayout(incidentLayout);
		final FormData fd_incidentDetail = new FormData();
		fd_incidentDetail.top = new FormAttachment(0, 5);
		fd_incidentDetail.bottom = new FormAttachment(100, -5);
		fd_incidentDetail.right = new FormAttachment(100, -5);
		fd_incidentDetail.left = new FormAttachment(0, 5);
		incidentDetail.setLayoutData(fd_incidentDetail);

		showEventsButton = new Button(parent, SWT.CHECK);
		showEventsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				locationView.showIncidents(showEventsButton.getSelection());
			}
		});
		showEventsButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_showEventsButton = new FormData();
		fd_showEventsButton.bottom = new FormAttachment(container, -5, SWT.TOP);
		fd_showEventsButton.right = new FormAttachment(0, 100);
		fd_showEventsButton.top = new FormAttachment(0, 5);
		fd_showEventsButton.left = new FormAttachment(0, 10);
		showEventsButton.setLayoutData(fd_showEventsButton);
		showEventsButton.setText("Show Events");

		// $hide<<$
		locationView = new LocationSummaryDetail(incidentDetail,SWT.None);
		incidentLayout.topControl = locationView;
		locationView.setController(this);
		incidentDetail.layout();
		locationView.setContext(TracController.getLoggedIn(), null);
		locationView.showIncidents(showEventsButton.getSelection());
		// $hide>>$

		createActions();
		initializeToolBar();
		initializeMenu();
		
		//
	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		showLineTrack = new ShowLineTrack();
		exportSelection = new ExportSelection();
		importTrack = new ImportTrack();
		deleteIncident = new DeleteIncident();
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager manager = getViewSite().getActionBars()
				.getToolBarManager();
		manager.add(showLineTrack);
		manager.add(exportSelection);
		manager.add(importTrack);
		if (TracController.getLoggedIn().fullfillsRole(User.Roles.SYSTEMADMIN)) {
			manager.add(deleteIncident);
		}
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}
	
	@Override
	public void setFocus() {
		Control control = incidentLayout.topControl;
		if (control != null)
			control.setFocus();
	}

	public static abstract class DisplayTask implements Runnable {
		public boolean done = false;
		public Display display;
		public DisplayTask() {
			this.display = Display.getCurrent();
		}
		public void finished() {
			this.done = true;
			this.display.wake();
		}
		abstract public void run();
	}
	
	public void busyWhile(DisplayTask runnable) {
		final DisplayTask localJob = runnable;
		final Display display = Display.getCurrent();
		Runnable longJob = new Runnable() {
			public void run() {
				Thread thread = new Thread(localJob);
				thread.start();
				while (!localJob.done) {
					if (display.readAndDispatch())
						display.sleep();
				}
			}
		};
		this.showBusy(true);
		try {
			longJob.run();
		} finally {
			this.showBusy(false);
		}
	}
	public class Resolve extends DisplayTask {
		private QueryDetail queryDetail;
		
		public Resolve(QueryDetail queryDetail) {
			super();
			this.queryDetail = queryDetail;
		}
		public void run() {
			try {
				if (queryDetail != null) {
					this.display.syncExec(new Runnable() {
						public void run() {
							try {
								System.out.println("Start collecting data...");
								if (showEventsButton.getSelection() == true) {
									List<SimpleStatus> incidents = null;
									incidents = DAO.localDAO().historicalSimpleStatuses(queryDetail.getAsset(), queryDetail.getFrom(), queryDetail.getTo());
									System.out.println("Finished collecting data...");
									locationView.setSimpleStatuses(incidents);
								} else {
									List<PointIncident> incidents = null;
									incidents = DAO.localDAO().historicalPointIncidents(queryDetail.getAsset(), queryDetail.getFrom(), queryDetail.getTo());
									System.out.println("Finished collecting data...");
									locationView.setPointIncidents(incidents);
								}
							} catch (Exception e) {}
						}
					});
				}
			} finally {
				this.finished();
			}
		}
	}
	
	public void selectionChanged(Client client, Asset asset) {
		this.setAsset(asset);
	}

	public void queryProcessed(QueryDetail queryDetail) {
		this.setAsset(queryDetail.getAsset());
		this.busyWhile(new Resolve(queryDetail));
	}
}
