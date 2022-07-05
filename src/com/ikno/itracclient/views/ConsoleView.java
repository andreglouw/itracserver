package com.ikno.itracclient.views;

import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.Incident;
import com.ikno.dao.business.MOTextMessageIncident;
import com.ikno.dao.business.PointIncident;
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
import com.ikno.itracclient.Layer;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.sounds.WAVPlayer;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.utils.Formatting;
import com.ikno.itracclient.views.widgets.LocationSummaryDetail;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;
import com.swtdesigner.SWTResourceManager;

public class ConsoleView extends ViewPart implements IEntityChangeListener, ISelectionProvider {

	public static final String ID = "com.ikno.itracclient.views.ConsoleView"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(ConsoleView.class.getName());
	private Integer[] simpleColumnWidths = null;
	private Asset selectedAsset = null;
	List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	
	class EventLabelProvider extends LabelProvider implements ITableLabelProvider,IColorProvider,IFontProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element == null)
				return null;
			EntityNotification notification = (EntityNotification)element;
			Map<String,Object> keyAttributes = notification.getKeyAttributes();
			switch (columnIndex) {
				case 0:
					return Formatting.format(notification.getEventTime());
				case 1:
					if (keyAttributes.containsKey("assetName"))
						return (String)keyAttributes.get("assetName");
					else if (keyAttributes.containsKey("areaName"))
						return "Waypoint registered";
				case 2:
					return (String)keyAttributes.get("description");
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
			EntityNotification notification = (EntityNotification)element;
			Level notLevel = Level.INFO;
			if (PersistantObject.instanceOf(notification.getEntityName(), SimpleStatus.class))
				notLevel = Level.values()[(Integer)notification.getKeyAttributes().get("level")];
			if (notLevel == Level.WARNING)
				return SWTResourceManager.getColor(253, 192, 66);
			else if (notLevel == Level.SEVERE)
				return SWTResourceManager.getColor(SWT.COLOR_RED);
			return SWTResourceManager.getColor(SWT.COLOR_BLACK);
		}
		public Font getFont(Object element) {
			if (element == null)
				return null;
			EntityNotification notification = (EntityNotification)element;
			Level notLevel = Level.INFO;
			if (PersistantObject.instanceOf(notification.getEntityName(), SimpleStatus.class))
				notLevel = Level.values()[(Integer)notification.getKeyAttributes().get("level")];
			if (notLevel == Level.SEVERE)
				return SWTResourceManager.getFont("Verdana", 10, SWT.BOLD);
			return null;
		}
	}
	class EventContentProvider implements IStructuredContentProvider {
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

	private TableViewer simpleTableViewer;
	private Table simpleTable;
	private Composite container;
	private static int MaxEntryCount = 50;

	public void dispose() {
		TracController.singleton().removeEntityChangeListener(ConsoleView.this);
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

		if (simpleColumnWidths == null)
			simpleColumnWidths = new Integer[]{120,100,200};

		simpleTableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		simpleTableViewer.setLabelProvider(new EventLabelProvider());
		simpleTableViewer.setContentProvider(new EventContentProvider());
		simpleTable = simpleTableViewer.getTable();
		simpleTable.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		simpleTable.setLinesVisible(true);
		simpleTable.setHeaderVisible(true);
		
		final TableColumn notifyTimeColumn = new TableColumn(simpleTable, SWT.NONE);
		notifyTimeColumn.setWidth(simpleColumnWidths[0]);
		notifyTimeColumn.setText("Event Time");

		final TableColumn simpleAssetColumn = new TableColumn(simpleTable, SWT.NONE);
		simpleAssetColumn.setWidth(simpleColumnWidths[1]);
		simpleAssetColumn.setText("Origin");

		final TableColumn simpleDescrColumn = new TableColumn(simpleTable, SWT.NONE);
		simpleDescrColumn.setWidth(simpleColumnWidths[2]);
		simpleDescrColumn.setText("Description");

		simpleTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				EntityNotification notification = (EntityNotification)selection.getFirstElement();
				Map<String,Object> keyAttributes = notification.getKeyAttributes();
				if (keyAttributes.containsKey("assetId")) {
					Asset asset = DAO.localDAO().getAssetById((Long)keyAttributes.get("assetId"));
					ConsoleView.this.setSelectedAsset(asset);
					IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
					if (mappingView != null)
						mappingView.AddAssetWithHistory(asset,null,true);
				} else {
					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							"Non Point", 
							"The notification you selected has no asset associated with it.");
				}
			}
		});
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE
		};
		logger.finest("adding as general EntityChangeListener");
		TracController.singleton().addEntityChangeListener(ConsoleView.this,interest,PointIncident.class.getName(),null);
		TracController.singleton().addEntityChangeListener(ConsoleView.this,interest,GeoPoint.class.getName(),null);
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
			if (notification.getNotificationType() != EntityNotification.Type.SAVE)
				return;
			Level notLevel = Level.INFO;
			if (PersistantObject.instanceOf(entityName, SimpleStatus.class))
				notLevel = Level.values()[(Integer)notification.getKeyAttributes().get("level")];
			String configKey = "com.ikno.play"+notLevel.toString().toLowerCase();
			if (Configuration.configCenter().getBoolean(configKey,false) == true)
				WAVPlayer.playWave(notLevel.sound());
			IWorkbenchSiteProgressService service = null;
			Object siteService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
			if (siteService != null) {
				service = (IWorkbenchSiteProgressService)siteService;
				service.warnOfContentChange();
			}
			if (!((String)notification.getKeyAttributes().get("description")).isEmpty()) {
				if (simpleTableViewer.getTable().getItemCount() > ConsoleView.MaxEntryCount) {
					simpleTableViewer.remove(0);
				}
				simpleTableViewer.add(notification);
				simpleTableViewer.reveal(notification);
			}
		} else if (PersistantObject.instanceOf(entityName, GeoArea.class)) {
			if (simpleTableViewer.getTable().getItemCount() > ConsoleView.MaxEntryCount) {
				simpleTableViewer.remove(0);
			}
			simpleTableViewer.add(notification);
			simpleTableViewer.reveal(notification);
		}
	}

	public void onEntityNotFound(EntityNotification notification) {
		// TODO Auto-generated method stub
		
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("consoleTableColumnCount");
			if (tableColumnCount != null) {
				simpleColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("consoleTableColumnWidth_"+idx);
					if (cw != null)
						simpleColumnWidths[idx] = cw; 
				}
			}
		}
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		Table table = simpleTableViewer.getTable();
		memento.putInteger("consoleTableColumnCount", table.getColumnCount());
		int idx = 0;
		for (TableColumn column : table.getColumns()) {
			memento.putInteger("consoleTableColumnWidth_"+idx++, column.getWidth());
		}
		super.saveState(memento);
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
}
