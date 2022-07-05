package com.ikno.itracclient.views;

import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Incident.Level;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Configuration;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.sounds.WAVPlayer;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.utils.Formatting;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;
import com.swtdesigner.SWTResourceManager;

public class IncidentView extends ViewPart implements IEntityChangeListener, ISelectionProvider {

	private Asset selectedAsset = null;
	List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((SimpleStatus)e1).compareTo((SimpleStatus)e2);
		}
	}
	private CheckboxTableViewer ackTableViewer;
	private Table ackTable;
	public static final String ID = "com.ikno.itracclient.views.IncidentView"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(IncidentView.class.getName());
	private Integer[] ackColumnWidths = null;
	private List<SimpleStatus> unacknowledged = null;
	
	class AckLabelProvider extends LabelProvider implements ITableLabelProvider,IColorProvider,IFontProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element == null)
				return null;
			SimpleStatus simpleStatus = (SimpleStatus)element;
			Asset asset = simpleStatus.getAsset();
			switch (columnIndex) {
				case 0:
					return Formatting.format(simpleStatus.getTimestamp());
				case 1:
					return asset.getAssetName();
				case 2:
					return simpleStatus.getStateDescription();
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public Color getBackground(Object element) {
			return SWTResourceManager.getColor(SWT.COLOR_WHITE);
		}
		public Color getForeground(Object element) {
			if (element == null)
				return null;
			SimpleStatus simpleStatus = (SimpleStatus)element;
			Level notLevel = simpleStatus.getLevel();
			if (notLevel == Level.WARNING)
				return SWTResourceManager.getColor(253, 192, 66);
			else if (notLevel == Level.SEVERE)
				return SWTResourceManager.getColor(SWT.COLOR_RED);
			return SWTResourceManager.getColor(SWT.COLOR_BLACK);
		}
		public Font getFont(Object element) {
			if (element == null)
				return null;
			SimpleStatus simpleStatus = (SimpleStatus)element;
			Level notLevel = simpleStatus.getLevel();
			if (notLevel == Level.SEVERE)
				return SWTResourceManager.getFont("Verdana", 10, SWT.BOLD);
			return null;
		}
	}
	class AckContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return (Object[])inputElement;
			return null;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private Composite container;
	private static int MaxEntryCount = 50;

	public void dispose() {
		TracController.singleton().removeEntityChangeListener(IncidentView.this);
		getViewSite().setSelectionProvider(null);
		super.dispose();
	}

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setVisible(true);

		if (ackColumnWidths == null)
			ackColumnWidths = new Integer[]{120,100,200};
		ackTableViewer = CheckboxTableViewer.newCheckList(container, SWT.FULL_SELECTION | SWT.BORDER);
		ackTableViewer.setSorter(new Sorter());
		ackTableViewer.setLabelProvider(new AckLabelProvider());
		ackTableViewer.setContentProvider(new AckContentProvider());
		ackTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				InputDialog dlg = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Action","What action was taken?","",null);
				int result = dlg.open();
				if (result == Window.OK) {
					SimpleStatus simpleStatus = (SimpleStatus)event.getElement();
					try {
						DAO.localDAO().beginTransaction();
						simpleStatus.setComment(dlg.getValue());
						simpleStatus.setAcknowledgedAt(Calendar.getInstance().getTime());
						simpleStatus.setAcknowledgedBy(TracController.getLoggedIn().getId());
						simpleStatus.setMustAcknowledge(false);
						DAO.localDAO().saveOrUpdate(simpleStatus);
						DAO.localDAO().commitTransaction();
						unacknowledged.remove(simpleStatus);
						ackTableViewer.remove(simpleStatus);
					} catch (Exception e) {
						simpleStatus.setComment(null);
						simpleStatus.setAcknowledgedAt(null);
						simpleStatus.setAcknowledgedBy(0);
						simpleStatus.setMustAcknowledge(true);
						logger.log(java.util.logging.Level.SEVERE,"Error updating SimpleStatus to acknowledged: ",e);
						DAO.localDAO().rollbackTransaction();
					}
				}
				ackTableViewer.setCheckedElements(new Object[]{});
			}
		});
		ackTable = ackTableViewer.getTable();
		ackTable.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		ackTable.setLinesVisible(true);
		ackTable.setHeaderVisible(true);

		final TableColumn ackNotifyTimeColumn = new TableColumn(ackTable, SWT.NONE);
		ackNotifyTimeColumn.setWidth(ackColumnWidths[0]);
		ackNotifyTimeColumn.setText("Event Time");

		final TableColumn ackAssetColumn = new TableColumn(ackTable, SWT.NONE);
		ackAssetColumn.setWidth(ackColumnWidths[1]);
		ackAssetColumn.setText("Asset");

		final TableColumn ackDescrColumn = new TableColumn(ackTable, SWT.NONE);
		ackDescrColumn.setWidth(ackColumnWidths[2]);
		ackDescrColumn.setText("Description");

		ackTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				SimpleStatus simpleStatus = (SimpleStatus)selection.getFirstElement();
				Asset asset = simpleStatus.getAsset();
				IncidentView.this.setSelectedAsset(asset);
				IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
				if (mappingView != null)
					mappingView.AddAssetWithHistory(asset,null,true);
			}
		});

		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE
		};
		Calendar start = Calendar.getInstance();
		start.add(Calendar.HOUR_OF_DAY, -12);
		unacknowledged = DAO.localDAO().getUnacknowledgedSimpleStatuses(TracController.getLoggedIn(), null);
		if (unacknowledged != null) {
			SimpleStatus last = null;
			for (SimpleStatus incident : unacknowledged) {
				ackTableViewer.add(incident);
				last = incident;
			}
			if (last != null)
				ackTableViewer.reveal(last);
		}
		logger.finest("adding as general EntityChangeListener");
		TracController.singleton().addEntityChangeListener(IncidentView.this,interest);
		getViewSite().setSelectionProvider(this);
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

	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		if (PersistantObject.instanceOf(entityName, Incident.class)) {
			Level notLevel = Level.INFO;
			if (PersistantObject.instanceOf(entityName, SimpleStatus.class))
				notLevel = Level.values()[(Integer)notification.getKeyAttributes().get("level")];
			if (PersistantObject.instanceOf(notification.getEntityName(), SimpleStatus.class)) {
				Boolean mustAcknowledge = notification.getBooleanAttribute("mustAcknowledge");
				if (mustAcknowledge != null && mustAcknowledge == true) {
					IWorkbenchSiteProgressService service = null;
					Object siteService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
					if (siteService != null) {
						service = (IWorkbenchSiteProgressService)siteService;
						service.warnOfContentChange();
					}
					String configKey = "com.ikno.play"+notLevel.toString().toLowerCase();
					if (Configuration.configCenter().getBoolean(configKey,false) == true)
						WAVPlayer.playWave(notLevel.sound());
					SimpleStatus simpleStatus = (SimpleStatus)notification.getResolved();
					if (simpleStatus.getAcknowledgedBy() == 0) {
						if (!unacknowledged.contains(simpleStatus)) {
							unacknowledged.add(simpleStatus);
							ackTableViewer.add(simpleStatus);
							ackTableViewer.reveal(simpleStatus);
						}
					} else {
						System.out.println("Acknowledged status "+simpleStatus.getId()+" will be removed");
						unacknowledged.remove(simpleStatus);
						ackTableViewer.setInput(unacknowledged.toArray());
					}
				}
			}
		}
	}

	public void onEntityNotFound(EntityNotification notification) {
		// TODO Auto-generated method stub
		
	}
	public void setSelectedAsset(Asset asset) {
		this.selectedAsset = asset;
		this.fireSelectionChanged();
	}
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null && !selectionListeners.contains(listener))
			selectionListeners.add(listener);
	}
	public ISelection getSelection() {
		if (selectedAsset != null)
			return new StructuredSelection(selectedAsset);
		return null;
	}
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}
	public void setSelection(ISelection selection) {
		this.selectedAsset = (Asset)((IStructuredSelection)selection).getFirstElement();
	}
	private void fireSelectionChanged() {
		AssetWrapper wrapper = new AssetWrapper(selectedAsset.getIdentifier(),selectedAsset.getId());
		final SelectionChangedEvent e = new SelectionChangedEvent(this,new StructuredSelection(wrapper));
		for (final ISelectionChangedListener l : selectionListeners) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("incidentTableColumnCount");
			if (tableColumnCount != null) {
				ackColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("incidentTableColumnWidth_"+idx);
					if (cw != null)
						ackColumnWidths[idx] = cw; 
				}
			}
		}
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		Table table = ackTableViewer.getTable();
		memento.putInteger("incidentTableColumnCount", table.getColumnCount());
		int idx = 0;
		for (TableColumn column : table.getColumns()) {
			memento.putInteger("incidentTableColumnWidth_"+idx++, column.getWidth());
		}
		super.saveState(memento);
	}
}
