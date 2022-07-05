package com.ikno.itracclient.views;

import itracclient.Activator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Faction.FactionWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.utils.Formatting;
import com.swtdesigner.ResourceManager;

public class StatusSummaryView extends ViewPart implements ISelectionListener, IEntityChangeListener {
	public static final String ID = "com.ikno.itracclient.views.StatusSummaryView";
	private static final Logger logger = Logging.getLogger(StatusSummaryView.class.getName());
	
	private Table table;
	private Composite container;
	private TableViewer viewer;
	
	public class UnitWrapper {
		private Unit unit = null;
		private long _lastInfoStatus = 0;
		private SimpleStatus lastInfoStatus = null;
		private long _lastAlarmStatus = 0;
		private SimpleStatus lastAlarmStatus = null;
		public UnitWrapper(Unit unit) {
			this.unit = unit;
		}
		public Unit getUnit() {
			return unit;
		}
		public void setUnit(Unit unit) {
			this.unit = unit;
		}
		public SimpleStatus getLastInfoStatus() {
			if (_lastInfoStatus == 0) {
				lastInfoStatus = unit.getAsset().getLastSimpleStatus(new Incident.Category[]{
						SimpleStatus.Category.MOVEMENT,
						SimpleStatus.Category.STATIONARY,
						SimpleStatus.Category.ALARM,
						SimpleStatus.Category.IGNITION,
						SimpleStatus.Category.INTERVAL_REPORT,
						SimpleStatus.Category.VBAT_WARNING,
						SimpleStatus.Category.ZONE_VIOLATION,
				});
				if (lastInfoStatus == null)
					_lastInfoStatus = -1;
			}
			return lastInfoStatus;
		}
		public SimpleStatus getLastAlarmStatus() {
			if (_lastAlarmStatus == 0) {
				lastAlarmStatus = unit.getAsset().getLastSimpleStatus(new Incident.Category[]{
						SimpleStatus.Category.ALARM,
						SimpleStatus.Category.VBAT_WARNING,
						SimpleStatus.Category.ZONE_VIOLATION,
				});
				if (lastAlarmStatus == null)
					_lastAlarmStatus = -1;
			}
			return lastAlarmStatus;
		}
	}
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((UnitWrapper)e1).getUnit().getAsset().getAssetName().compareTo(((UnitWrapper)e2).getUnit().getAsset().getAssetName());
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Unit unit = ((UnitWrapper)element).getUnit();
			Asset asset = unit.getAsset();
			Date lastComms = null;
			Date lastIncident = null;
			switch (columnIndex) {
				case 0: // Asset name
					return asset.getAssetName();
				case 1: // Unit name
					return unit.getUnitName();
				case 2: // Status description
					SimpleStatus lastInfoStatus = ((UnitWrapper)element).getLastInfoStatus();
					if (lastInfoStatus != null) {
						String status = "Normal";
						if (lastInfoStatus.getLevel().fits(SimpleStatus.Level.INFO))
							status = asset.getShortStateDescription(lastInfoStatus);
						return status;
					}
					return "N/A";

				case 3: // Alarm
					SimpleStatus lastAlarmStatus = ((UnitWrapper)element).getLastAlarmStatus();
					String status = "Normal";
					if (lastAlarmStatus != null) {
						status = asset.getShortStateDescription(lastAlarmStatus);
					}
					return status;
				case 4: // Last comms
					lastComms = unit.getLastComms();
					if (lastComms != null) {
						return Formatting.format(lastComms);
					}
					return "";
				case 5: // Comms lag
					lastComms = unit.getLastComms();
					if (lastComms != null) {
						Date now = Calendar.getInstance().getTime();
						long delta = (now.getTime()-lastComms.getTime())/1000;
						long days = delta/86400;
						long hrs = (delta-(days*86400))/3600;
						long min = (delta-(days*86400)-(hrs*3600))/60;
						return String.format("%d days, %d hrs, %d min", days,hrs,min);
					}
					return "";
				case 6: // Last fix
					lastIncident = unit.getLastIncident();
					if (lastIncident != null) {
						return Formatting.format(lastIncident);
					}
					return "";
				case 7: // Fix lag
					lastIncident = unit.getLastIncident();
					if (lastIncident != null) {
						Date now = Calendar.getInstance().getTime();
						long delta = (now.getTime()-lastIncident.getTime())/1000;
						long days = delta/86400;
						long hrs = (delta-(days*86400))/3600;
						long min = (delta-(days*86400)-(hrs*3600))/60;
						return String.format("%d days, %d hrs, %d min", days,hrs,min);
					}
					return "";
			}
			return unit.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			SimpleStatus lastAlarmStatus = ((UnitWrapper)element).getLastAlarmStatus();
			switch (columnIndex) {
				case 3: // Alarm
					if (lastAlarmStatus != null) {
						if (lastAlarmStatus.getState() == SimpleStatus.State.BEGIN)
							return ResourceManager.getPluginImage(Activator.getDefault(), "images/redsquare.gif");
						else
							return ResourceManager.getPluginImage(Activator.getDefault(), "images/greensquare.gif");
					}
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/bluesquare.gif");
			}
			return null;
		}
	}
	class TableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object element) {
			return (UnitWrapper[])element;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setLayout(new FormLayout());
		container.setVisible(false);
		viewer = new TableViewer(container, SWT.BORDER);
		viewer.setSorter(new Sorter());
		viewer.setLabelProvider(new TableLabelProvider());
		viewer.setContentProvider(new TableContentProvider());
		table = viewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -15);
		fd_table.top = new FormAttachment(0, 5);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0, 5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn assetColumn = new TableColumn(table, SWT.NONE);
		assetColumn.setWidth(150);
		assetColumn.setText("Asset");

		final TableColumn unitColumn = new TableColumn(table, SWT.NONE);
		unitColumn.setWidth(120);
		unitColumn.setText("Unit");

		final TableColumn statusTableColumn = new TableColumn(table, SWT.NONE);
		statusTableColumn.setWidth(250);
		statusTableColumn.setText("Status");

		final TableColumn alarmColumn = new TableColumn(table, SWT.NONE);
		alarmColumn.setWidth(80);
		alarmColumn.setText("Alarm");

		final TableColumn lastCommsColumn = new TableColumn(table, SWT.NONE);
		lastCommsColumn.setWidth(120);
		lastCommsColumn.setText("Last Comms");

		final TableColumn commsLagColumn = new TableColumn(table, SWT.NONE);
		commsLagColumn.setWidth(150);
		commsLagColumn.setText("Comms Lag");

		final TableColumn lastFixColumn = new TableColumn(table, SWT.NONE);
		lastFixColumn.setWidth(120);
		lastFixColumn.setText("Last Fix");

		final TableColumn fixLagColumn = new TableColumn(table, SWT.NONE);
		fixLagColumn.setWidth(150);
		fixLagColumn.setText("Fix Lag");
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE,
				EntityNotification.Type.DELETE
		};
		TracController.singleton().addEntityChangeListener(this, interest);
		initializeToolBar();
		
		getViewSite().getPage().addSelectionListener(AssetView.ID, this);
		IViewPart assetView = ApplicationWorkbenchWindowAdvisor.getView(AssetView.ID);
		if (assetView != null) {
			ISelection selection = assetView.getViewSite().getSelectionProvider().getSelection();
			if (selection != null) {
				this.selectionChanged(null, selection);
			}
		}
	}

	public void onEntityNotFound(EntityNotification notification) {
		String entityName = notification.getEntityName();
		if (!entityName.equals("SimpleStatus")) {
			System.out.println("EntityNotFound for entity "+entityName);
			viewer.refresh();
		}
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		long objectId = notification.getObjectId();
		if (PersistantObject.instanceOf(entityName, Faction.class)) {
			viewer.refresh();
		} else {
			Unit current = null;
			if (PersistantObject.instanceOf(entityName,Unit.class)) {
				for (Unit unit : (Unit[])viewer.getInput()) {
					if (unit.getId() == objectId) {
						current = unit;
					}
				}
			} else if (PersistantObject.instanceOf(entityName,Incident.class)) {
				Incident incident = (Incident)notification.getResolved();
				Unit notunit = incident.getUnit();
				for (Unit unit : (Unit[])viewer.getInput()) {
					if (unit.getId() == notunit.getId()) {
						current = unit;
					}
				}
			}
			if (current != null) {
				viewer.update(current,null);
			}
		}
	}
	public void dispose() {
		TracController.singleton().removeEntityChangeListener(this);
		super.dispose();
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		this.setCurrentElement(ssel.getFirstElement());
	}
	public void setCurrentElement(Object element) {
		boolean visible = true;
		List<UnitWrapper> lunits = new ArrayList<UnitWrapper>();
		if (element == null) {
			visible = false;
		} else {
			if (PersistantObject.instanceOf(element,FactionWrapper.class)) {
				for (Iterator<Asset> ai = (((FactionWrapper)element).getObject()).getAssets().iterator();ai.hasNext();) {
					for (Unit unit : ai.next().getUnits()) {
						lunits.add(new UnitWrapper(unit));
					}
				}
			} else if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
				for (Unit unit : (((AssetWrapper)element).getObject()).getUnits()) {
					lunits.add(new UnitWrapper(unit));
				}
			} else if (PersistantObject.instanceOf(element,Unit.class)) {
				lunits.add(new UnitWrapper((Unit)element));
			}
			viewer.setInput(lunits.toArray(new UnitWrapper[lunits.size()]));
		}
		container.setVisible(visible);
	}
}