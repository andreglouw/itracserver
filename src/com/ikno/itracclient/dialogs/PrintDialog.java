package com.ikno.itracclient.dialogs;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.reporting.Report;
import com.ikno.dao.business.reporting.ReportParameter;
import com.ikno.dao.business.reporting.ReportRequest;
import com.ikno.dao.business.reporting.Report.ReportWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.views.ConsoleView;
import com.ikno.itracclient.views.ReportViewer;
import com.ikno.itracclient.views.widgets.SelectionDetail;
import com.ikno.itracclient.views.widgets.SelectionDetail.IQuerySelection;
import com.ikno.itracclient.views.widgets.SelectionDetail.QueryDetail;

public class PrintDialog extends Dialog implements IQuerySelection {

	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return (((ReportWrapper)e1).getReportName().compareTo(((ReportWrapper)e2).getReportName()));
		}
	}
	private ComboViewer reportListViewer;
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((ReportWrapper)element).getReportName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			List<ReportWrapper> reports = (List<ReportWrapper>)inputElement;
			return reports.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Combo reportList;
	protected Object result;
	protected Shell shell;
	private Asset asset = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public PrintDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public PrintDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(Asset asset) {
		this.asset = asset;
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setMinimumSize(new Point(400, 375));
		shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		shell.setLayout(new FormLayout());
		shell.setSize(522, 396);
		shell.setText("Print");

		final Label reportLabel = new Label(shell, SWT.NONE);
		reportLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		reportLabel.setAlignment(SWT.RIGHT);
		final FormData fd_reportLabel = new FormData();
		fd_reportLabel.right = new FormAttachment(0, 55);
		fd_reportLabel.bottom = new FormAttachment(0, 25);
		fd_reportLabel.top = new FormAttachment(0, 5);
		fd_reportLabel.left = new FormAttachment(0, 5);
		reportLabel.setLayoutData(fd_reportLabel);
		reportLabel.setText("Report");

		reportListViewer = new ComboViewer(shell, SWT.BORDER);
		reportListViewer.setSorter(new Sorter());
		reportListViewer.setLabelProvider(new ListLabelProvider());
		reportListViewer.setContentProvider(new ContentProvider());
		reportListViewer.setInput(DAO.localDAO().getEnabledReportWrappers());
		reportList = reportListViewer.getCombo();
		reportList.setVisibleItemCount(10);
		final FormData fd_reportList = new FormData();
		fd_reportList.right = new FormAttachment(0, 365);
		fd_reportList.bottom = new FormAttachment(reportLabel, 0, SWT.BOTTOM);
		fd_reportList.top = new FormAttachment(reportLabel, 0, SWT.TOP);
		fd_reportList.left = new FormAttachment(reportLabel, 5, SWT.RIGHT);
		reportList.setLayoutData(fd_reportList);

		SelectionDetail selectionDetail;
		selectionDetail = new SelectionDetail(shell, SWT.NONE);
		final FormData fd_selectionDetail = new FormData();
		fd_selectionDetail.right = new FormAttachment(100, -5);
		fd_selectionDetail.bottom = new FormAttachment(100, -5);
		fd_selectionDetail.top = new FormAttachment(reportLabel, 5, SWT.BOTTOM);
		fd_selectionDetail.left = new FormAttachment(reportLabel, 0, SWT.LEFT);
		selectionDetail.setLayoutData(fd_selectionDetail);
		selectionDetail.setLayout(new FormLayout());
		selectionDetail.setContext(TracController.getLoggedIn(), asset);
		selectionDetail.setCallback(this);
		//
	}

	public void queryProcessed(QueryDetail queryDetail) {
		IStructuredSelection selection = (IStructuredSelection)reportListViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null) {
			return;
		}
		Object element = selection.getFirstElement();
		if (!PersistantObject.instanceOf(element,ReportWrapper.class)) {
			return;
		}
		Report report = ((ReportWrapper)element).getObject();
		String reportName = report.getReportName();
		Map<String,String> parameters = new HashMap<String,String>();
		for (Iterator<ReportParameter> ri = report.getReportParameters().iterator();ri.hasNext();) {
			parameters.put(ri.next().getParameter().getParameterName(), null);
		}
		Asset asset = queryDetail.getAsset();
		parameters.put("ASSET", asset.getAssetName());
		parameters.put("CONTRACT", queryDetail.getClient().getClientName());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		parameters.put("FROM", formatter.format(queryDetail.getFrom().getTime()));
		parameters.put("TO", formatter.format(queryDetail.getTo().getTime()));
		String defaultName = reportName+" ["+asset.getAssetName()+"] from "+parameters.get("FROM")+", to "+parameters.get("TO");
		ReportRequest reportRequest = new ReportRequest(reportName, parameters, defaultName);
		ReportViewer reportViewer = (ReportViewer)ApplicationWorkbenchWindowAdvisor.getView(ReportViewer.ID);
		if (reportViewer == null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				page.showView(ReportViewer.ID,null,IWorkbenchPage.VIEW_CREATE);
				reportViewer = (ReportViewer)ApplicationWorkbenchWindowAdvisor.getView(ReportViewer.ID);
			} catch (PartInitException e) {
				System.out.println("Error initialising Console view...");
			}
		}
		if (reportViewer != null)
			reportViewer.addReportRequest(reportRequest);
		shell.close();
	}

	public void selectionChanged(Client client, Asset asset) {
	}

	public static void main(String[] args) {
		try {
			DAO.localDAO().beginTransaction();
			List<Report> reports = DAO.localDAO().getAllEnabledReports();
			for (Report report : reports) {
				InputStream istream = report.reportStreamFromFile();
				if (istream != null) {
					try {
						byte[] bytes = new byte[istream.available()];
						istream.read(bytes);
						report.setReportData(bytes);
						DAO.localDAO().save(report);
					} catch (Exception e) {
						System.out.println("Error reading from Report "+report.getReportName()+"'s stream: "+e);
					}
				}
			}
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
		}
	}
}
