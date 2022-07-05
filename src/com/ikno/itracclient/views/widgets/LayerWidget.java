package com.ikno.itracclient.views.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.itracclient.Layer;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.dialogs.LayerViewDialog;
import com.ikno.itracclient.wizards.MapLayerWizard;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class LayerWidget extends Composite {

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((MapLayer)element).getName();
			case 1:
				return ((MapLayer)element).getDescription();
			}
			return ((MapLayer)element).getName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((IMappingView)inputElement).layerList().toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private CheckboxTableViewer checkboxTableViewer;
	private Table table;
	private IMappingView mappingView;
	private LayerViewDialog dialog = null;
	private Integer[] columnWidths = null;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public LayerWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		checkboxTableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					KMLLayer mapLayer = (KMLLayer)selection.getFirstElement();
					if (dialog != null)
						dialog.layerSelectionChanged(mapLayer);
				}
			}
		});
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ContentProvider());
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				MapLayer layer = (MapLayer)event.getElement();
				mappingView.changeLayerVisibility(layer, event.getChecked());
			}
		});
		checkboxTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					KMLLayer mapLayer = (KMLLayer)selection.getFirstElement();
					MapLayerWizard wizard = new MapLayerWizard(mapLayer);
					wizard.init(workbench, null);
					WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
					dialog.open();
					if (dialog.getReturnCode() == Window.OK) {
						mappingView.addLayer((MapLayer)mapLayer);
					}
				}
			}
		});
		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.getHorizontalBar().setEnabled(false);

		if (columnWidths == null)
			columnWidths = new Integer[]{230,310};
		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(columnWidths[0]);
		newColumnTableColumn.setText("Layer Name");
		
		final TableColumn newColumnTableColumn1 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn1.setWidth(columnWidths[1]);
		newColumnTableColumn1.setText("Description");
		//
	}

	public void setMappingView(IMappingView mappingView) {
		this.mappingView = mappingView;
		if (mappingView != null) {
			checkboxTableViewer.setInput(mappingView);
			List<MapLayer> layers = mappingView.layerList();
			List<MapLayer> checked = new ArrayList<MapLayer>();
			for (Iterator<MapLayer> li = layers.iterator(); li.hasNext();) {
				MapLayer layer = li.next();
				if (layer.isVisible())
					checked.add(layer);
			}
			checkboxTableViewer.setCheckedElements(checked.toArray());
		}
	}
	public void refreshLayers() {
		if (mappingView != null) {
			checkboxTableViewer.setInput(mappingView);
			List<MapLayer> layers = mappingView.layerList();
			List<MapLayer> checked = new ArrayList<MapLayer>();
			for (Iterator<MapLayer> li = layers.iterator(); li.hasNext();) {
				MapLayer layer = li.next();
				if (layer.isVisible())
					checked.add(layer);
			}
			checkboxTableViewer.setCheckedElements(checked.toArray());
		}
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("tableColumnCount");
			if (tableColumnCount != null) {
				columnWidths = new Integer[tableColumnCount];
				for (int idx=0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("tableColumnWidth_"+idx);
					if (cw != null)
						columnWidths[idx] = cw; 
				}
			}
		}
	}

	public void saveState(IMemento memento) {
		Table table = checkboxTableViewer.getTable();
		memento.putInteger("tableColumnCount", table.getColumnCount());
		int idx = 0;
		for (TableColumn column : table.getColumns()) {
			memento.putInteger("tableColumnWidth_"+idx++, column.getWidth());
		}
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setDialog(LayerViewDialog dialog) {
		this.dialog = dialog;
	}

}
