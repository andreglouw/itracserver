package com.ikno.itracclient.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.reporting.ReportRequest;
import com.ikno.dao.business.reporting.ReportRequest.BuildTask;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.views.widgets.ReportViewerWidget;

public class ReportViewer extends ViewPart implements IChangeListener {
	public static final String ID = "com.ikno.itracclient.views.ReportViewer"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(ReportViewer.class.getName());

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			TableColumn column = table.getColumn(columnIndex);
			ReportRequest request = (ReportRequest)element;
			if (column.getText().equals("Requested")) {
				return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(request.getTimestamp());
			} else if (column.getText().equals("Status")) {
				return request.getStatus().toString();
			} else if (column.getText().equals("Description")) {
				return request.getDescription();
			} else if (column.getText().equals("Message")) {
				return request.getMessage() == null ? "" : request.getMessage();
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return reportRequests.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private TableViewer tableViewer;
	private Table table;
	private List<ReportRequest> reportRequests = new ArrayList<ReportRequest>();
	private ReportViewerWidget imageViewer;
	private Integer[] simpleColumnWidths = null;

	private ReportRequest currentView = null;

 	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		/**
		 * initialise JPedal PDF view on first call
		 */
		parent.setLayout(new FillLayout());
		final SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setOrientation(SWT.VERTICAL);

		tableViewer = new TableViewer(sashForm, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				ReportRequest selected = (ReportRequest)selection.getFirstElement();
				if (selected != null && selected.getStatus() == ReportRequest.Status.SUCCESS) {
					ReportViewer.this.setReportRequest(selected);
				}
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		if (simpleColumnWidths == null)
			simpleColumnWidths = new Integer[]{120,100,350,200};
		
		imageViewer = new ReportViewerWidget(sashForm, SWT.NONE);
		imageViewer.setLayout(new FormLayout());
		sashForm.setWeights(new int[] {101, 242 });

		final Menu menu = new Menu(table);
		table.setMenu(menu);

		final MenuItem closeMenuItem = new MenuItem(menu, SWT.NONE);
		closeMenuItem.setText("Close");
		closeMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				ReportRequest selected = (ReportRequest)selection.getFirstElement();
				reportRequests.remove(selected);
				ReportViewer.this.setReportRequest(null);
				tableViewer.refresh();
			}
		});
		
		final TableColumn requestedTableColumn = new TableColumn(table, SWT.NONE);
		requestedTableColumn.setWidth(simpleColumnWidths[0]);
		requestedTableColumn.setText("Requested");

		final TableColumn statusTableColumn = new TableColumn(table, SWT.NONE);
		statusTableColumn.setWidth(simpleColumnWidths[1]);
		statusTableColumn.setText("Status");

		final TableColumn descrTableColumn = new TableColumn(table, SWT.NONE);
		descrTableColumn.setWidth(simpleColumnWidths[2]);
		descrTableColumn.setText("Description");

		final TableColumn messageTableColumn = new TableColumn(table, SWT.NONE);
		messageTableColumn.setWidth(simpleColumnWidths[3]);
		messageTableColumn.setText("Message");
		
		tableViewer.setInput(new Object());
		//
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		imageViewer.contributeToToolBar(toolBarManager);
	}
	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setFocus() {
		imageViewer.setFocus();
	}

	public void dispose() {
		super.dispose();
	}
 
	public void addReportRequest(ReportRequest reportRequest) {
		logger.finer("Submitting report "+reportRequest.getReportName()+" for execution");
		reportRequests.add(reportRequest);
		BuildTask task = new BuildTask(reportRequest,this);
		task.start();
		tableViewer.refresh();
	}
	public void objectChanged(Object object) {
		if (PersistantObject.instanceOf(object,ReportRequest.class)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					tableViewer.refresh();
				}
			});
		}
	}
	public void setReportRequest(ReportRequest reportRequest) {
		if (reportRequest == null) {
			imageViewer.closePDF();
			currentView = null;
		} else {
			if (reportRequest.getStatus() == ReportRequest.Status.SUCCESS && currentView != reportRequest) {
				String pdf = reportRequest.getResultURL();
				imageViewer.openPDF(pdf,reportRequest.getDescription());
				currentView = reportRequest;
			}
		}
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("reportRequestTableColumnCount");
			if (tableColumnCount != null) {
				simpleColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("reportRequestTableColumnWidth_"+idx);
					if (cw != null)
						simpleColumnWidths[idx] = cw; 
				}
			}
		}
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		memento.putInteger("reportRequestTableColumnCount", table.getColumnCount());
		int idx = 0;
		for (TableColumn column : table.getColumns()) {
			memento.putInteger("reportRequestTableColumnWidth_"+idx++, column.getWidth());
		}
		super.saveState(memento);
	}

}
