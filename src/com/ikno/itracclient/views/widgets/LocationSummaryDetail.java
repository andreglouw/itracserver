package com.ikno.itracclient.views.widgets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.utils.Formatting;
import com.ikno.itracclient.views.IncidentSummaryView;
import com.ikno.itracclient.views.widgets.SelectionDetail.IQuerySelection;
import com.ikno.itracclient.views.widgets.SelectionDetail.QueryDetail;

public class LocationSummaryDetail extends Composite implements IQuerySelection {

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			PointIncident pointIncident = null;
			SimpleStatus simpleStatus = null;
			if (PersistantObject.instanceOf(element,PointIncident.class)) {
				pointIncident = (PointIncident)element;
			} else {
				simpleStatus = (SimpleStatus)element;
				pointIncident = DAO.localDAO().lastPointIncidentForAsset(simpleStatus.getAsset(),simpleStatus.getTimestamp());
			}
			switch (columnIndex) {
				case 0: // Timestamp
					return Formatting.format(pointIncident.getTimestamp());
				case 1: // Timestamp
					return Formatting.format(pointIncident.getCreated());
				case 2: // Speed
					return String.format("%.0f %s", pointIncident.getAssetSpeed(),pointIncident.getAssetSpeedIndicator());
				case 3: // Altitude
					return Formatting.formatElevation(pointIncident.getAltitude());
				case 4: // LatLon
					return Formatting.formatLatLon(pointIncident.getLatitude(),pointIncident.getLongitude());
				case 5: // Distance
					return String.format("%.2f KM", pointIncident.getDistance());
				case 6: // Location
					return pointIncident.getLocation();
			}
			TableColumn column = table.getColumn(columnIndex);
			if (column == idTableColumn)
				return String.format("%d", pointIncident.getId());
			else if (column == eventDescrTableColumn)
				return simpleStatus.getFullDescription();
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ViewContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null) {
				return ((List<PointIncident>)inputElement).toArray();
			}
			return null;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private TableViewer tableViewer;
	private Table table;
	private TableColumn eventDescrTableColumn = null;
	private TableColumn idTableColumn = null;
	private Asset asset = null;
	private SelectionDetail selectionDetail;
	private IncidentSummaryView controller;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public LocationSummaryDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());

		SashForm sashForm;
		sashForm = new SashForm(this, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);
		final FormData fd_sashForm = new FormData();
		fd_sashForm.bottom = new FormAttachment(100, -5);
		fd_sashForm.top = new FormAttachment(0, 5);
		fd_sashForm.right = new FormAttachment(100, -5);
		fd_sashForm.left = new FormAttachment(0, 5);
		sashForm.setLayoutData(fd_sashForm);

		tableViewer = new TableViewer(sashForm, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object selected = selection.getFirstElement();
				PointIncident incident = null;
				if (PersistantObject.instanceOf(selected, PointIncident.class))
					incident = (PointIncident)selected;
				else {
					SimpleStatus simpleStatus = (SimpleStatus)selected;
					incident = DAO.localDAO().lastPointIncidentForAsset(simpleStatus.getAsset(),simpleStatus.getTimestamp());
				}
				LocationSummaryDetail.this.controller.incidentSelected(incident);
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ViewContentProvider());
		table = tableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.right = new FormAttachment(0, 495);
		fd_table.left = new FormAttachment(0, 5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn timestampTableColumn = new TableColumn(table, SWT.CENTER);
		timestampTableColumn.setToolTipText("The timestamp at which the location was recorded");
		timestampTableColumn.setAlignment(SWT.CENTER);
		timestampTableColumn.setWidth(113);
		timestampTableColumn.setText("Timestamp");

		final TableColumn createdTableColumn = new TableColumn(table, SWT.NONE);
		createdTableColumn.setWidth(113);
		createdTableColumn.setText("Created");

		final TableColumn speedTableColumn = new TableColumn(table, SWT.NONE);
		speedTableColumn.setWidth(83);
		speedTableColumn.setText("Speed");

		final TableColumn altitudeTableColumn = new TableColumn(table, SWT.NONE);
		altitudeTableColumn.setWidth(76);
		altitudeTableColumn.setText("Altitude");

		final TableColumn latlonTableColumn = new TableColumn(table, SWT.NONE);
		latlonTableColumn.setWidth(200);
		latlonTableColumn.setText("Lat/Lon");

		final TableColumn distanceTableColumn = new TableColumn(table, SWT.NONE);
		distanceTableColumn.setWidth(80);
		distanceTableColumn.setText("Distance");

		final TableColumn locationTableColumn = new TableColumn(table, SWT.NONE);
		locationTableColumn.setWidth(200);
		locationTableColumn.setText("Location");

		// $hide<<$
		User loggedIn = TracController.getLoggedIn(); 
		if (loggedIn != null && loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			idTableColumn = new TableColumn(table, SWT.NONE);
			idTableColumn.setWidth(60);
			idTableColumn.setText("id");
		}
		// $hide>>$
		fd_table.top = new FormAttachment(sashForm, -120, SWT.TOP);
		fd_table.bottom = new FormAttachment(sashForm, 0, SWT.TOP);

		tableViewer.setInput(null);

		selectionDetail = new SelectionDetail(sashForm, SWT.BORDER);
		selectionDetail.setLayout(new FormLayout());
		selectionDetail.setCallback(this);
		sashForm.setWeights(new int[] {134, 323});
		//
	}
	
	public void setController(IncidentSummaryView controller) {
		this.controller = controller;
	}

	public void setContext(User user, Asset asset) {
		selectionDetail.setContext(user, asset);
		tableViewer.setInput(null);
	}
	
	public ISelectionProvider getSelectionProvider() {
		return tableViewer;
	}
	
	public Object[] getSelection(boolean fullIfEmpty) {
		IStructuredSelection ssel = (IStructuredSelection)tableViewer.getSelection();
		if (ssel == null) {
			if (fullIfEmpty)
				return (((ArrayList)tableViewer.getInput()).toArray());
			return null;
		}
		Object[] selected = ssel.toArray();
		if (selected.length == 0) {
			if (fullIfEmpty)
				return (((ArrayList)tableViewer.getInput()).toArray());
			return null;
		}
		return selected;
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setPointIncidents(List<PointIncident> incidents) {
		tableViewer.setInput(incidents);
	}

	public void setSimpleStatuses(List<SimpleStatus> incidents) {
		tableViewer.setInput(incidents);
	}

	public void showIncidents(boolean show) {
		if (show) {
			eventDescrTableColumn = new TableColumn(table, SWT.NONE);
			eventDescrTableColumn.setWidth(200);
			eventDescrTableColumn.setText("Event");
			eventDescrTableColumn.pack();
		} else if (eventDescrTableColumn != null){
			eventDescrTableColumn.dispose();
			eventDescrTableColumn = null;
		}
	}
	public void removeIncidents(PointIncident[] incidents) {
		tableViewer.remove(incidents);
	}
	public void selectionChanged(Client client, Asset asset) {
		this.controller.selectionChanged(client, asset);
		tableViewer.setInput(null);
	}

	public void queryProcessed(QueryDetail queryDetail) {
		this.controller.queryProcessed(queryDetail);
	}

}
