package com.ikno.itracclient.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetRule;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Rule;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Faction.FactionWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.ObjectWrapper;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;

public class RuleAssignmentView extends ViewPart implements ISelectionListener,IEntityChangeListener {
	private static final Logger logger = Logging.getLogger(RuleAssignmentView.class.getName());
	public static final String ID = "com.ikno.itracclient.views.RuleAssignmentView"; //$NON-NLS-1$

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
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FormLayout());
		checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
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
		table = checkboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -36);
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
		//
		checkboxTableViewer.setInput(DAO.localDAO().getAllRules().toArray());
		checkboxTableViewer.getTable().setEnabled(false);

		final Button revertButton = new Button(container, SWT.NONE);
		revertButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				RuleAssignmentView.this.setAsset(asset);
			}
		});
		final FormData fd_revertButton = new FormData();
		fd_revertButton.bottom = new FormAttachment(100, -6);
		fd_revertButton.top = new FormAttachment(100, -31);
		fd_revertButton.right = new FormAttachment(100, -5);
		fd_revertButton.left = new FormAttachment(100, -49);
		revertButton.setLayoutData(fd_revertButton);
		revertButton.setText("Revert");

		Button okButton;
		okButton = new Button(container, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (asset == null)
					return;
				try {
					DAO.localDAO().beginTransaction();
					List<Rule> rules = asset.getRules();
					for (Rule rule : added) {
						asset.addRule(rule);
					}
					for (Rule rule : removed) {
						if (rules.contains(rule)) {
							asset.removeRule(rule);
						}
					}
					DAO.localDAO().commitTransaction();
				} catch (Exception exc) {
					DAO.localDAO().rollbackTransaction();
				}
			}
		});
		final FormData fd_okButton = new FormData();
		fd_okButton.top = new FormAttachment(revertButton, -25, SWT.BOTTOM);
		fd_okButton.bottom = new FormAttachment(revertButton, 0, SWT.BOTTOM);
		fd_okButton.right = new FormAttachment(100, -49);
		fd_okButton.left = new FormAttachment(100, -79);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");
		getViewSite().getPage().addSelectionListener(AssetView.ID, this);
		IViewPart assetView = ApplicationWorkbenchWindowAdvisor.getView(AssetView.ID);
		if (assetView != null) {
			ISelection selection = assetView.getViewSite().getSelectionProvider().getSelection();
			if (selection != null) {
				this.selectionChanged(null, selection);
			}
		}
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE,
				EntityNotification.Type.DELETE
				};
		logger.finest("adding as EntityChangeListener");
		TracController.singleton().addEntityChangeListener(this, interest);
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	public void dispose() {
		getViewSite().getPage().removeSelectionListener(AssetView.ID, this);
		super.dispose();
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

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object element = ssel.getFirstElement();
		Asset asset = null;
		if (element != null) {
			if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
				asset = ((AssetWrapper)element).getObject();
			} else if (PersistantObject.instanceOf(element,Unit.class)) {
				asset = ((Unit)element).getAsset();
			}
		}
		setAsset(asset);
	}
	public void setAsset(Asset asset) {
		added.clear();
		removed.clear();
		this.asset = asset;
		List<Rule> checked = new ArrayList<Rule>();
		checkboxTableViewer.getTable().setEnabled(true);
		if (asset != null) {
			List<Rule> rules = asset.getRules();
			for (Iterator<Rule> ri = rules.iterator(); ri.hasNext();) {
				checked.add(ri.next());
			}
		}
		checkboxTableViewer.setCheckedElements(checked.toArray());
	}
	public void onEntityNotFound(EntityNotification notification) {
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		long objectId = notification.getObjectId();
		if (PersistantObject.instanceOf(entityName, Rule.class)) {
			logger.finer("Rule updated, will force populate...");
			Object[] current = (Object[])checkboxTableViewer.getInput();
			List<Rule> rules = new ArrayList<Rule>();
			boolean present = false;
			for (Object rule : current) {
				if (((Rule)rule).getId() == objectId) {
					present = true;
					break;
				}
				rules.add((Rule)rule);
			}
			if (!present) {
				checkboxTableViewer.setInput(rules.toArray());
			}
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
		super.init(site, memento);
	}

	public void saveState(IMemento memento) {
		Table table = checkboxTableViewer.getTable();
		memento.putInteger("tableColumnCount", table.getColumnCount());
		int idx = 0;
		for (TableColumn column : table.getColumns()) {
			memento.putInteger("tableColumnWidth_"+idx++, column.getWidth());
		}
		super.saveState(memento);
	}
}
