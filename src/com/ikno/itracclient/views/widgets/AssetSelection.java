package com.ikno.itracclient.views.widgets;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ikno.dao.business.Asset;

public class AssetSelection extends Composite {

	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Asset)e1).compareTo((Asset)e2);
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	private CheckboxTableViewer checkboxTableViewer;
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Table table;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AssetSelection(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());

		checkboxTableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER);
		checkboxTableViewer.setSorter(new Sorter());
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ContentProvider());
		table = checkboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(0, 375);
		fd_table.top = new FormAttachment(0, 0);
		fd_table.right = new FormAttachment(0, 301);
		fd_table.left = new FormAttachment(0, 0);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn assetColumn = new TableColumn(table, SWT.NONE);
		assetColumn.setWidth(296);
		assetColumn.setText("Asset Name");
		//
	}

	public void setAssets(Object[] assets, Object[] selected) {
		checkboxTableViewer.setInput(assets);
		if (selected != null)
			checkboxTableViewer.setCheckedElements(selected);
	}
	public Object[] getSelected() {
		return checkboxTableViewer.getCheckedElements();
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
