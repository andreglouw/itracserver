package com.ikno.itracclient.views.widgets;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;

public class ObjectSelection<T> extends Composite {

	private TableColumn fullNameColumnTableColumn;
	private CheckboxTableViewer checkboxTableViewer;
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null)
				return new Object[]{};
			return ((List<T>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null | e2 == null)
				return 0;
			return (e1.toString().compareTo(e2.toString()));
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element != null)
				return element.toString();
			return "N/A";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	private Table table;
	private List<T> possibles;
	private List<T> selected = new ArrayList<T>();
	private List<T> added = new ArrayList<T>();
	private List<T> removed = new ArrayList<T>();
	private String qualifier;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ObjectSelection(Composite parent, int style, String qualifier) {
		super(parent, style);
		this.qualifier = qualifier;
		setLayout(new FormLayout());

		checkboxTableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER);
		checkboxTableViewer.setContentProvider(new ContentProvider());
		checkboxTableViewer.setSorter(new Sorter());
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				T object = (T)event.getElement();
				if (event.getChecked()) {
					if  (removed.contains(object))
						removed.remove(object);
					added.add(object);
					if (!selected.contains(object)) {
						selected.add(object);
					}
				} else {
					if (added.contains(object))
						added.remove(object);
					removed.add(object);
					if (selected.contains(object)) {
						selected.remove(object);
					}
				}
			}
		});
		table = checkboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, 0);
		fd_table.top = new FormAttachment(0, 0);
		fd_table.right = new FormAttachment(100, 0);
		fd_table.left = new FormAttachment(0, 0);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		fullNameColumnTableColumn = new TableColumn(table, SWT.NONE);
		fullNameColumnTableColumn.setWidth(100);
		fullNameColumnTableColumn.setText("Description");
		//
		this.restoreState();
	}

	public void setSelection(List<T> possibles, List<T> selected) {
		this.possibles = possibles;
		if (selected == null)
			selected = new ArrayList<T>();
		this.selected = selected;
		checkboxTableViewer.setInput(possibles);
		checkboxTableViewer.setCheckedElements(selected.toArray());
	}
	private void restoreState() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(ObjectSelection.class.getSimpleName()+"_"+qualifier);
		if (section == null)
			section = dialogSettings.addNewSection(ObjectSelection.class.getSimpleName());
		try {
			String value = section.get("columnWidth");
			if (value != null) {
				fullNameColumnTableColumn.setWidth(Integer.parseInt(value));
			}
		} catch (Exception e) {
			System.out.println("Exception in restoreState: "+e);
		}
	}
	private void saveState() {
		int width = fullNameColumnTableColumn.getWidth();
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(ObjectSelection.class.getSimpleName()+"_"+qualifier);
		if (section == null)
			section = dialogSettings.addNewSection(ObjectSelection.class.getSimpleName()+"_"+qualifier);
		section.put("columnWidth", ""+width);
	}
	public List<T> getSelected() {
		return selected;
	}
	public List<T> getAdded() {
		return added;
	}
	public List<T> getRemoved() {
		return removed;
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		this.saveState();
		super.dispose();
	}

}
