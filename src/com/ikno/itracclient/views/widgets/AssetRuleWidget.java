package com.ikno.itracclient.views.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetRule;
import com.ikno.dao.business.Rule;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.views.widgets.GeneralRuleDetail;

public class AssetRuleWidget extends Composite {

	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Rule)e1).getRuleName().compareTo(((Rule)e2).getRuleName());
		}
	}
	private GeneralRuleDetail generalRuleDetail;
	private CheckboxTableViewer checkboxTableViewer;
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return ((Rule)element).getRuleName();
				case 1:
					return ((Rule)element).getDescription();
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
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
	private Table table;
	private Asset asset;
	private Integer[] columnWidths = null;
	private List<Rule> added = new ArrayList<Rule>();
	private List<Rule> removed = new ArrayList<Rule>();
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AssetRuleWidget(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());
		checkboxTableViewer = CheckboxTableViewer.newCheckList(this, SWT.FULL_SELECTION | SWT.BORDER);
		checkboxTableViewer.setSorter(new Sorter());
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ContentProvider());
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (asset == null) {
					event.getCheckable().setChecked(event.getElement(), false);
					return;
				}
				Rule rule = (Rule)event.getElement();
				boolean isChecked = event.getChecked();
				if (rule.isAddedByDefault() && !isChecked) {
					event.getCheckable().setChecked(event.getElement(), true);
					return;
				}
				if (isChecked) {
					if (!added.contains(rule))
						added.add(rule);
					removed.remove(rule);
				} else {
					removed.add(rule);
					added.remove(rule);
				}
			}
		});
		checkboxTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null) {
					generalRuleDetail.populateObject();
					Rule rule = (Rule)selection.getFirstElement();
					Object[] checked = checkboxTableViewer.getCheckedElements();
					boolean isChecked = false;
					for (Object obj : checked) {
						if (((Rule)obj).equals(rule)) {
							AssetRule assetRule = asset.getAssetRule(rule);
							generalRuleDetail.populateView(assetRule);
							isChecked = true;
							break;
						}
					}
					if (isChecked == false) {
						generalRuleDetail.populateView(null);
					}
				}
			}
		});
		table = checkboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(0, 5);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0, 5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		if (columnWidths == null)
			columnWidths = new Integer[]{100,280};
		final TableColumn ruleNameTableColumn = new TableColumn(table, SWT.NONE);
		ruleNameTableColumn.setWidth(columnWidths[0]);
		ruleNameTableColumn.setText("Name");

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(columnWidths[1]);
		newColumnTableColumn.setText("Description");
		checkboxTableViewer.setInput(null);
		checkboxTableViewer.getTable().setEnabled(false);

		generalRuleDetail = new GeneralRuleDetail(this, SWT.NONE);
		fd_table.bottom = new FormAttachment(generalRuleDetail, -5, SWT.TOP);
		final FormData fd_generalRuleDetail = new FormData();
		fd_generalRuleDetail.top = new FormAttachment(0, 230);
		fd_generalRuleDetail.bottom = new FormAttachment(100, -5);
		fd_generalRuleDetail.right = new FormAttachment(table, 0, SWT.RIGHT);
		fd_generalRuleDetail.left = new FormAttachment(table, 0, SWT.LEFT);
		generalRuleDetail.setLayoutData(fd_generalRuleDetail);
		generalRuleDetail.setLayout(new FormLayout());
		//
	}

	public void dispose() {
		this.asset = null;
		this.added = null;
		this.removed = null;
		super.dispose();
	}
	public void setAsset(Asset asset) {
		checkboxTableViewer.setInput(DAO.localDAO().getAllRules().toArray());
		this.asset = asset;
		added.clear();
		removed.clear();
		List<Rule> checked = new ArrayList<Rule>();
		checkboxTableViewer.getTable().setEnabled(true);
		if (asset != null) {
			List<Rule> rules = asset.getRules();
			for (Iterator<Rule> ri = rules.iterator(); ri.hasNext();) {
				checked.add(ri.next());
			}
		}
		checkboxTableViewer.setCheckedElements(checked.toArray());
		generalRuleDetail.populateView(null);
	}
	public void applyChanges() {
		List<Rule> rules = asset.getRules();
		for (Rule rule : added) {
			asset.addRule(rule);
		}
		for (Rule rule : removed) {
			if (rules.contains(rule)) {
				asset.removeRule(rule);
			}
		}
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
