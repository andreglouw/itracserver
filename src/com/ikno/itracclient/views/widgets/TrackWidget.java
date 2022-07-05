package com.ikno.itracclient.views.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.ITrackListener;
import com.ikno.itracclient.Track;

public class TrackWidget extends Composite implements ITrackListener {

	private CheckboxTableViewer checkboxTableViewer;
	private Table table;
	private IMappingView mappingView = null;
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return ((Track)element).getTrackName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((IMappingView)inputElement).trackList().toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public TrackWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		checkboxTableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER);
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ContentProvider());
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Track track = (Track)event.getElement();
				track.setEnabled(event.getChecked());
				track.redraw(mappingView);
//				mappingView.redrawTrack(track);
			}
		});
		table = checkboxTableViewer.getTable();
		table.getHorizontalBar().setEnabled(false);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setResizable(false);
		newColumnTableColumn.setWidth(487);
		newColumnTableColumn.setText("Track Name");

		final Menu menu = new Menu(table);
		table.setMenu(menu);

		final MenuItem zoomToMenuItem = new MenuItem(menu, SWT.NONE);
		zoomToMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)checkboxTableViewer.getSelection();
				Track selected = (Track)selection.getFirstElement();
				selected.zoomToTrack(mappingView, 2000.0, false);
//				mappingView.zoomToTrack(selected, false);
			}
		});
		zoomToMenuItem.setText("Zoom To");

		final MenuItem deleteMenuItem = new MenuItem(menu, SWT.NONE);
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)checkboxTableViewer.getSelection();
				Track selected = (Track)selection.getFirstElement();
				selected.remove(mappingView);
//				mappingView.removeTrack(selected);
				checkboxTableViewer.refresh();
			}
		});
		deleteMenuItem.setText("Delete");
		//
	}

	public void tracksUpdated() {
		System.out.println("Refreshing TrackViewer");
		checkboxTableViewer.refresh();
		List<Track> tracks = mappingView.trackList();
		List<Track> checked = new ArrayList<Track>();
		for (Iterator<Track> li = tracks.iterator(); li.hasNext();) {
			Track track = li.next();
			if (track.isEnabled())
				checked.add(track);
		}
		checkboxTableViewer.setCheckedElements(checked.toArray());
	}
	
	public void setMappingView(IMappingView mappingView, boolean listenForChanges) {
		this.mappingView = mappingView;
		if (mappingView != null) {
			checkboxTableViewer.setInput(mappingView);
			List<Track> tracks = mappingView.trackList();
			List<Track> checked = new ArrayList<Track>();
			for (Iterator<Track> li = tracks.iterator(); li.hasNext();) {
				Track track = li.next();
				if (track.isEnabled())
					checked.add(track);
			}
			checkboxTableViewer.setCheckedElements(checked.toArray());
			if (listenForChanges)
				mappingView.addTrackListener(this);
		}
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
