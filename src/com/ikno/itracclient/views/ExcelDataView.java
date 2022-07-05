package com.ikno.itracclient.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.wizards.MapLayerWizard;

public class ExcelDataView extends ViewPart {

	private ComboViewer layerListViewer;
	private Combo layerCombo;
	class GeoPointLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			GeoPoint geoPoint = (GeoPoint)element;
			if (columnIndex == Columns.AREANAME.ordinal())
				return geoPoint.getAreaName();
			else if (columnIndex == Columns.ICON.ordinal())
				return geoPoint.getIconUrl();
			else if (columnIndex == Columns.LATITUDE.ordinal())
				return String.format("%.5f", geoPoint.getLatitude());
			else if (columnIndex == Columns.LONGITUDE.ordinal())
				return String.format("%.5f", geoPoint.getLongitude());
			return "Unknown";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class GeoPointContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((MapLayer)e1).getName().compareTo(((MapLayer)e2).getName());
		}
	}
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((MapLayer)element).getName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private TableViewer tableViewer;
	private Table table;
	private Text fileName;
	private List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	public static final String ID = "com.ikno.itracclient.views.ExcelDataView"; //$NON-NLS-1$

	public enum Columns {
		AREANAME,
		LONGITUDE,
		LATITUDE,
		ICON
	};
	public GeoPoint buildGeoPoint(Cell[] row) {
		IStructuredSelection selection = (IStructuredSelection)layerListViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null)
			return null;
		KMLLayer mapLayer = (KMLLayer)selection.getFirstElement();
		GeoPoint geoPoint = new GeoPoint();
		geoPoint.setMapLayer(mapLayer);
		geoPoint.setAltitude(0);
		geoPoint.setRadius(100);
		String content = row[Columns.AREANAME.ordinal()].getContents();
		if (content == null || content.equals(""))
			return null;
		geoPoint.setAreaName(content.trim());
		String lat = row[Columns.LATITUDE.ordinal()].getContents().trim();
		geoPoint.setLatitude(Float.parseFloat(lat));
		String lon = row[Columns.LONGITUDE.ordinal()].getContents().trim();
		geoPoint.setLongitude(Float.parseFloat(lon));
		content = row[Columns.ICON.ordinal()].getContents();
		if (content != null && !content.equals(""))
			geoPoint.setIconUrl(content);
		return geoPoint;
	}
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FormLayout());

		fileName = new Text(container, SWT.BORDER);
		final FormData fd_fileName = new FormData();
		fd_fileName.bottom = new FormAttachment(0, 25);
		fd_fileName.top = new FormAttachment(0, 5);
		fd_fileName.right = new FormAttachment(100, -59);
		fd_fileName.left = new FormAttachment(0, 5);
		fileName.setLayoutData(fd_fileName);

		final Button browseButton = new Button(container, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(),SWT.OPEN);
				dialog.setFilterNames(new String[] {"Excel Files"});
				dialog.setFilterExtensions(new String[] {"*.xls"});
				if (fileName.getText() != null) {
					File test = new File(fileName.getText());
					if (test.exists())
						dialog.setFileName(fileName.getText());
				}
				dialog.open();
				fileName.setText(dialog.getFileName() == null ? "" : dialog.getFilterPath()+File.separator+dialog.getFileName());
			}
		});
		final FormData fd_browseButton = new FormData();
		fd_browseButton.bottom = new FormAttachment(0, 25);
		fd_browseButton.top = new FormAttachment(0, 5);
		fd_browseButton.right = new FormAttachment(100, -4);
		fd_browseButton.left = new FormAttachment(100, -54);
		browseButton.setLayoutData(fd_browseButton);
		browseButton.setText("Browse");

		tableViewer = new TableViewer(container, SWT.BORDER);
		tableViewer.setLabelProvider(new GeoPointLabelProvider());
		tableViewer.setContentProvider(new GeoPointContentProvider());
		table = tableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(fileName, 5, SWT.BOTTOM);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(fileName, 0, SWT.LEFT);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		Button importButton;
		importButton = new Button(container, SWT.NONE);
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)layerListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					System.out.println("No layer selected, please select a layer");
					return;
				}
				table.clearAll();
				for (;table.getColumns().length > 0;) {
					table.getColumns()[0].dispose();
				}
				String fName = fileName.getText();
				if (fName == null || fName.equals(""))
					return;
				Workbook workbook = null;
				int i = 0;
				try {
					workbook = Workbook.getWorkbook(new File(fName));
					if (workbook != null) {
						Sheet sheet = workbook.getSheet("Data");
						if (sheet != null) {
							Cell[] row = sheet.getRow(0);
							for (Cell cell : row) {
								String label = cell.getContents();
								if (label == null || label.equals(""))
									break;
								TableColumn column = new TableColumn(table, SWT.LEFT);
								column.setText(label);
								column.pack();
							}
							int count = sheet.getRows();
							Object[] result = new Object[count-1]; 
							geoPoints.clear();
							for (i=1;i<count;i++) {
								Cell[] cells = sheet.getRow(i);
								GeoPoint geoPoint = buildGeoPoint(cells);
								if (geoPoint != null) {
									result[i-1] = geoPoint;
									geoPoints.add(geoPoint);
								} else
									System.out.println("Problem parsing row number "+i);
							}
							tableViewer.setInput(result);
						}
					}
				} catch (BiffException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					System.out.println("Problem in row number "+i);
					e1.printStackTrace();
				} finally {
					if (workbook != null)
						workbook.close();
				}
			}
		});
		fd_table.bottom = new FormAttachment(importButton, -5, SWT.TOP);

		final FormData fd_importButton = new FormData();
		fd_importButton.top = new FormAttachment(100, -25);
		fd_importButton.bottom = new FormAttachment(100, -5);
		fd_importButton.left = new FormAttachment(table, -49, SWT.RIGHT);
		fd_importButton.right = new FormAttachment(table, 0, SWT.RIGHT);
		importButton.setLayoutData(fd_importButton);
		importButton.setText("Import");

		final Button exportButton = new Button(container, SWT.NONE);
		exportButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (geoPoints.size() == 0)
					return;
				try {
					DAO.localDAO().beginTransaction();
					for (GeoPoint geoPoint : geoPoints) {
						System.out.println("Saving GeoPoint '"+geoPoint+"'");
						DAO.localDAO().saveGeoArea(geoPoint);
					}
					DAO.localDAO().commitTransaction();
				} catch (Throwable exc) {
					System.out.println("Problem saving GeoPoint");
					exc.printStackTrace();
					DAO.localDAO().rollbackTransaction();
				}
			}
		});
		final FormData fd_exportButton = new FormData();
		fd_exportButton.bottom = new FormAttachment(100, -5);
		fd_exportButton.top = new FormAttachment(100, -25);
		fd_exportButton.right = new FormAttachment(100, -54);
		fd_exportButton.left = new FormAttachment(100, -104);
		exportButton.setLayoutData(fd_exportButton);
		exportButton.setText("Export");

		layerListViewer = new ComboViewer(container, SWT.BORDER);
		layerListViewer.setLabelProvider(new ListLabelProvider());
		layerListViewer.setContentProvider(new ContentProvider());
		layerCombo = layerListViewer.getCombo();
		final FormData fd_layerCombo = new FormData();
		fd_layerCombo.bottom = new FormAttachment(100, -5);
		fd_layerCombo.top = new FormAttachment(100, -26);
		fd_layerCombo.right = new FormAttachment(0, 160);
		fd_layerCombo.left = new FormAttachment(0, 5);
		layerCombo.setLayoutData(fd_layerCombo);
		List<KMLLayer> kmlLayers = DAO.localDAO().getSharedKMLLayersForUser(TracController.getLoggedIn());
		if (kmlLayers != null)
			layerListViewer.setInput(kmlLayers.toArray(new KMLLayer[]{}));

		final Button newLayerButton = new Button(container, SWT.NONE);
		final FormData fd_newLayerButton = new FormData();
		fd_newLayerButton.bottom = new FormAttachment(100, -5);
		fd_newLayerButton.top = new FormAttachment(100, -26);
		fd_newLayerButton.right = new FormAttachment(0, 215);
		fd_newLayerButton.left = new FormAttachment(0, 165);
		newLayerButton.setLayoutData(fd_newLayerButton);
		newLayerButton.setText("New");
		newLayerButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				KMLLayer mapLayer = new KMLLayer();
				MapLayerWizard wizard = new MapLayerWizard(mapLayer);
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
				dialog.open();
			}
		});
		//
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

}
