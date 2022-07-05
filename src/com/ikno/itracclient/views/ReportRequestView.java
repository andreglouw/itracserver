package com.ikno.itracclient.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.reporting.Report;
import com.ikno.dao.business.reporting.ReportParameter;
import com.ikno.dao.business.reporting.ReportRequest;
import com.ikno.dao.business.reporting.ReportRequest.BuildTask;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.views.widgets.SelectionDetail;
import com.ikno.itracclient.views.widgets.SelectionDetail.IQuerySelection;
import com.ikno.itracclient.views.widgets.SelectionDetail.QueryDetail;

public class ReportRequestView extends ViewPart implements ISelectionListener,IQuerySelection {
	private static final Logger logger = Logging.getLogger(ReportRequestView.class.getName());
	public static final String ID = "com.ikno.itracclient.views.ReportRequestView"; //$NON-NLS-1$

	class TreeSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (PersistantObject.instanceOf(e1,com.ikno.dao.business.reporting.Group.class)) {
				return ((com.ikno.dao.business.reporting.Group)e1).getGroupName().compareTo(((com.ikno.dao.business.reporting.Group)e2).getGroupName());
			} else if (PersistantObject.instanceOf(e1,Report.class)) {
				return ((Report)e1).getDescription().compareTo(((Report)e2).getDescription());
			}
			return 0;
		}
	}
	class TreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (PersistantObject.instanceOf(element,com.ikno.dao.business.reporting.Group.class)) {
				return ((com.ikno.dao.business.reporting.Group)element).getGroupName();
			} else if (PersistantObject.instanceOf(element,Report.class)) {
				return ((Report)element).getDescription();
			}
			return "";
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement.equals("Root")) {
				List<com.ikno.dao.business.reporting.Group> groups = DAO.localDAO().getReportGroups();
				if (groups != null)
					return groups.toArray();
				return new Group[]{};
			} else if (PersistantObject.instanceOf(parentElement,com.ikno.dao.business.reporting.Group.class)) {
				com.ikno.dao.business.reporting.Group selected = (com.ikno.dao.business.reporting.Group)parentElement;
				List<Report> reports = DAO.localDAO().getGroupedReports(selected.getId());
				if (reports != null)
					return reports.toArray();
				return new Report[]{};
			}
			return new Object[]{};
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private TreeViewer treeViewer;
	private SelectionDetail selectionDetail;
	private Tree tree;
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		//
		parent.setLayout(new FormLayout());

		final SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);

		treeViewer = new TreeViewer(sashForm, SWT.BORDER);
		treeViewer.setSorter(new TreeSorter());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setContentProvider(new TreeContentProvider());
		tree = treeViewer.getTree();
		tree.setToolTipText("Select a report to process");
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setInput("Root");
		final FormData fd_tree = new FormData();
		fd_tree.bottom = new FormAttachment(0, 480);
		fd_tree.right = new FormAttachment(sashForm, 0, SWT.RIGHT);
		fd_tree.top = new FormAttachment(sashForm, 5, SWT.BOTTOM);
		fd_tree.left = new FormAttachment(sashForm, 0, SWT.LEFT);
		tree.setLayoutData(fd_tree);

		final Group group = new Group(sashForm, SWT.NONE);
		group.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		group.setLayout(new FormLayout());

		selectionDetail = new SelectionDetail(group, SWT.NONE);
		final FormData fd_selectionDetail = new FormData();
		fd_selectionDetail.bottom = new FormAttachment(100, -5);
		fd_selectionDetail.top = new FormAttachment(0, 5);
		fd_selectionDetail.right = new FormAttachment(100, -5);
		fd_selectionDetail.left = new FormAttachment(0, 5);
		selectionDetail.setLayoutData(fd_selectionDetail);
		selectionDetail.setLayout(new FormLayout());
		selectionDetail.setCallback(this);
		final FormData fd_sashForm = new FormData();
		fd_sashForm.bottom = new FormAttachment(100, -5);
		fd_sashForm.right = new FormAttachment(100, -5);
		fd_sashForm.top = new FormAttachment(0, 5);
		fd_sashForm.left = new FormAttachment(0, 5);
		sashForm.setLayoutData(fd_sashForm);

		selectionDetail.setContext(TracController.getLoggedIn(), null);
		
		sashForm.setWeights(new int[] {80, 150 });
		createActions();
		initializeToolBar();
		initializeMenu();
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

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object element = ssel.getFirstElement();
		if (element != null) {
			if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
				selectionDetail.setContext(TracController.getLoggedIn(), ((AssetWrapper)element).getObject());
			} else {
				selectionDetail.setContext(TracController.getLoggedIn(), null);
			}
		}
	}

	public void queryProcessed(QueryDetail queryDetail) {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null) {
			return;
		}
		Object element = selection.getFirstElement();
		String reportName = null;
		if (PersistantObject.instanceOf(element,Report.class)) {
			reportName = ((Report)element).getReportName();
		} else {
			return;
		}
		Map<String,String> parameters = new HashMap<String,String>();
		for (Iterator<ReportParameter> ri = ((Report)element).getReportParameters().iterator();ri.hasNext();) {
			parameters.put(ri.next().getParameter().getParameterName(), null);
		}
		Asset asset = queryDetail.getAsset();
		parameters.put("ASSET", asset.getAssetName());
		parameters.put("CONTRACT", queryDetail.getClient().getClientName());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		parameters.put("FROM", formatter.format(queryDetail.getFrom().getTime()));
		parameters.put("TO", formatter.format(queryDetail.getTo().getTime()));
		String defaultName = reportName+" ["+asset.getAssetName()+"] from "+parameters.get("FROM")+", to "+parameters.get("TO");
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),"","Enter a description for the report",defaultName,
				new IInputValidator(){
					public String isValid(String newText) {
						if (newText == null || newText.length() == 0)
							return "Error: You must supply a description";
						return null;
					}
				});
		if (dlg.open() == Window.OK) {
			ReportRequest reportRequest = new ReportRequest(reportName, parameters,dlg.getValue());
			ReportViewer reportViewer = (ReportViewer)ApplicationWorkbenchWindowAdvisor.getView(ReportViewer.ID);
			reportViewer.addReportRequest(reportRequest);
		}
	}
	public void selectionChanged(Client client, Asset asset) {
	}
}
