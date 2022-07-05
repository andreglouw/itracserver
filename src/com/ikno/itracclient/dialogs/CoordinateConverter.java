package com.ikno.itracclient.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class CoordinateConverter extends Dialog {

	private TableViewer tableViewer;
	private TableColumn longitudeTableColumn;
	private TableColumn latitudeTableColumn;
	private TableColumn mgrsTableColumn;
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[] { "item_0", "item_1", "item_2" };
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Table table;
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public CoordinateConverter(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());

		tableViewer = new TableViewer(container, SWT.BORDER);
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setInput(new Object());
		table = tableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(100, -292);
		fd_table.left = new FormAttachment(100, -487);
		fd_table.right = new FormAttachment(100, -5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		mgrsTableColumn = new TableColumn(table, SWT.NONE);
		mgrsTableColumn.setWidth(135);
		mgrsTableColumn.setText("Millitary Grid Ref");

		latitudeTableColumn = new TableColumn(table, SWT.NONE);
		latitudeTableColumn.setWidth(112);
		latitudeTableColumn.setText("Latitude");

		longitudeTableColumn = new TableColumn(table, SWT.NONE);
		longitudeTableColumn.setWidth(111);
		longitudeTableColumn.setText("Longitude");

		final Button mgrsGeoButton = new Button(container, SWT.NONE);
		final FormData fd_mgrsGeoButton = new FormData();
		fd_mgrsGeoButton.left = new FormAttachment(0, 405);
		fd_mgrsGeoButton.right = new FormAttachment(0, 480);
		fd_mgrsGeoButton.bottom = new FormAttachment(0, 290);
		fd_mgrsGeoButton.top = new FormAttachment(0, 265);
		mgrsGeoButton.setLayoutData(fd_mgrsGeoButton);
		mgrsGeoButton.setText("MGRS > GEO");

		Button geoMgrsButton;
		geoMgrsButton = new Button(container, SWT.NONE);
		fd_table.bottom = new FormAttachment(geoMgrsButton, -5, SWT.TOP);
		final FormData fd_geoMgrsButton = new FormData();
		fd_geoMgrsButton.top = new FormAttachment(100, -30);
		fd_geoMgrsButton.bottom = new FormAttachment(100, -5);
		fd_geoMgrsButton.left = new FormAttachment(0, 320);
		fd_geoMgrsButton.right = new FormAttachment(0, 400);
		geoMgrsButton.setLayoutData(fd_geoMgrsButton);
		geoMgrsButton.setText("GEO > MGRS");
		//
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

}
