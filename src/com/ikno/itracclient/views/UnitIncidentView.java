package com.ikno.itracclient.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Incident;
import com.ikno.dao.business.SBD2Status;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.utils.Formatting;
import com.ikno.itracclient.views.widgets.SBD2StatusDetail;
import com.ikno.itracclient.views.widgets.SimpleStatusDetail;
import com.swtdesigner.SWTResourceManager;

public class UnitIncidentView extends ViewPart implements ISelectionListener {

	private SimpleStatusDetail simpleStatusDetail;
	private SBD2StatusDetail sbd2StatusDetail;
	private Composite container;
	private Text recorded;
	private Group timestamp;
	public static final String ID = "com.ikno.itracclient.views.UnitIncidentView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setLayout(new FormLayout());
		container.setVisible(false);
		timestamp = new Group(container, SWT.NONE);
		timestamp.setText("Time of last status change");
		final FormData fd_timestamp = new FormData();
		fd_timestamp.bottom = new FormAttachment(0, 50);
		fd_timestamp.top = new FormAttachment(0, 5);
		fd_timestamp.right = new FormAttachment(100, -5);
		fd_timestamp.left = new FormAttachment(0, 5);
		timestamp.setLayoutData(fd_timestamp);
		timestamp.setLayout(new FormLayout());
		timestamp.setBackground(SWTResourceManager.getColor(255, 255, 255));

		recorded = new Text(timestamp, SWT.NONE);
		recorded.setToolTipText("The time of this status incident, or 'No Detail' if nothing available");
		final FormData fd_recorded = new FormData();
		fd_recorded.bottom = new FormAttachment(0, 26);
		fd_recorded.top = new FormAttachment(0, 5);
		fd_recorded.right = new FormAttachment(0, 160);
		fd_recorded.left = new FormAttachment(0, 5);
		recorded.setLayoutData(fd_recorded);
		recorded.setText("No Detail");
		recorded.setFont(SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD));
		recorded.setEditable(false);
		recorded.setBackground(SWTResourceManager.getColor(255, 255, 255));

		sbd2StatusDetail = new SBD2StatusDetail(container, SWT.NONE);
		final FormData fd_sbd2StatusDetail = new FormData();
		fd_sbd2StatusDetail.bottom = new FormAttachment(0, 185);
		fd_sbd2StatusDetail.top = new FormAttachment(0, 91);
		fd_sbd2StatusDetail.right = new FormAttachment(100, -5);
		fd_sbd2StatusDetail.left = new FormAttachment(0, 5);
		sbd2StatusDetail.setLayoutData(fd_sbd2StatusDetail);
		sbd2StatusDetail.setLayout(new FormLayout());

		simpleStatusDetail = new SimpleStatusDetail(container, SWT.NONE);
		final FormData fd_simpleStatusDetail = new FormData();
		fd_simpleStatusDetail.top = new FormAttachment(0, 50);
		fd_simpleStatusDetail.right = new FormAttachment(100, -5);
		fd_simpleStatusDetail.left = new FormAttachment(0, 4);
		simpleStatusDetail.setLayoutData(fd_simpleStatusDetail);
		simpleStatusDetail.setLayout(new FormLayout());
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
		recorded.setText("No specific detail");
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object element = ssel.getFirstElement();
		if (element != null) {
			if (PersistantObject.instanceOf(element,Unit.class)) {
				setUnit((Unit)element);
			} else {
				setUnit(null);
			}
		}
	}
	
	public void setUnit(Unit unit) {
		if (unit == null) {
			container.setVisible(false);
		} else {
			List<Incident> incidents = unit.getLastIncidents();
			Date lastRecording = new Date(0);
			boolean hasSBD2Status = false;
			boolean hasSimpleStatus = false;
			noDetail();
			if (incidents != null) {
				for (Iterator<Incident> ii = incidents.iterator(); ii.hasNext();) {
					Incident incident = ii.next();
					if (incident.getTimestamp().after(lastRecording))
						lastRecording = incident.getTimestamp();
					if (PersistantObject.instanceOf(incident,SBD2Status.class)) {
						sbd2StatusDetail.setSBD2Status((SBD2Status)incident);
						hasSBD2Status = true;
					} else if (PersistantObject.instanceOf(incident,SimpleStatus.class)) {
						simpleStatusDetail.setSimpleStatus((SimpleStatus)incident);
						hasSimpleStatus = true;
					}
				}
				if (incidents.size() > 0) {
					recorded.setText(Formatting.format(lastRecording));
				}
			}
			if (hasSBD2Status == false) {
				sbd2StatusDetail.setSize(0, 0);
			} else {
				sbd2StatusDetail.setSize(sbd2StatusDetail.computeSize(SWT.DEFAULT, SWT.DEFAULT, false));
			}
			if (hasSimpleStatus == false) {
				simpleStatusDetail.setSize(0, 0);
			} else {
				simpleStatusDetail.setSize(simpleStatusDetail.computeSize(SWT.DEFAULT, SWT.DEFAULT, false));
			}
			container.setVisible(true);
		}
	}
	
	@Override
	public void setFocus() {
		this.container.setFocus();
	}

}
