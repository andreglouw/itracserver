package com.ikno.itracclient.views;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Faction.FactionWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.ObjectWrapper;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.actions.EditFaction;
import com.ikno.itracclient.actions.EditUser;
import com.ikno.itracclient.actions.RemoveAsset;
import com.ikno.itracclient.actions.RemoveFaction;
import com.ikno.itracclient.actions.RemoveUser;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;
import com.ikno.itracclient.worldwind.WorldWindWidget;

public class AssetView extends ViewPart implements IEntityChangeListener, ISelectionListener {
	private Tree tree;
	private static final Logger logger = Logging.getLogger(AssetView.class.getName());
	public static final String ID = "com.ikno.itracclient.views.AssetView";

	private TreeViewer viewer;
	private EditFaction editFaction = null;
	private RemoveFaction removeFaction = null;
	private EditUser editUser = null;
	private RemoveUser removeUser = null;
	private RemoveAsset removeAsset = null;
	private PropertyDialogAction propertyDialogAction = null;

	private List<AssetWrapper> currentAssets = null;
	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getChildren(Object element) {
			if (element.equals("Root")) {
				User user = TracController.getLoggedIn();
				return new Object[]{user};
			} else {
				try {
					if (PersistantObject.instanceOf(element,User.class)) {
						User user = ((User)element);
						List<FactionWrapper> factions = DAO.localDAO().getFactionWrappersForUser(user.getId());
						return factions.toArray();
					} else if (PersistantObject.instanceOf(element,FactionWrapper.class)) {
						FactionWrapper faction = ((FactionWrapper)element);
						currentAssets = DAO.localDAO().getAssetWrappersForFaction(faction.getId());
						return currentAssets.toArray();
					} else if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
						Asset asset = ((AssetWrapper)element).getObject();
						Set<Unit> units = asset.getUnits(); 
						Unit[] result = new Unit[units.size()];
						int i = 0;
						for (Iterator<Unit> ai = units.iterator(); ai.hasNext();) {
							Unit test = ai.next();
							result[i] = test;
							i++;
						}
						return result;
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE,"Error determining child type: ",e);
				}
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}
		
		public boolean hasChildren(Object element) {
			if (element != null) {
				if (PersistantObject.instanceOf(element,AssetWrapper.class))
					return true;
				Object[] children = getChildren(element);
				if (children != null)
					return children.length > 0;
			}
			return false;
		}

		public Object[] getElements(Object element) {
			return getChildren(element);
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object element, int index) {
			return getText(element);
		}

		public String getText(Object element) {
			if (PersistantObject.instanceOf(element,User.class)) {
				return "User "+"'"+((User)element).getUsername()+"'";
			} else if (PersistantObject.instanceOf(element,FactionWrapper.class)) {
				return "Asset Group "+"'"+((FactionWrapper)element).getFactionName()+"'";
			} else if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
				return "Asset "+"'"+((AssetWrapper)element).getAssetName()+"'";
			} else if (PersistantObject.instanceOf(element,Unit.class)) {
				return "Unit "+"'"+((Unit)element).getUnitName()+"'";
			}
			return "Unclassified";
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	public void populateView() {
		viewer.setInput("Root");
	}
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		logger.finest("createPartControl(parent)");
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					Object element = selection.getFirstElement();
					if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
						Asset asset = ((AssetWrapper)element).getObject();
						IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
						mappingView.AddAssetWithHistory(asset,null,true);
					}
				}
			}
		});
		tree = viewer.getTree();

		Transfer[] types = new Transfer[]{TextTransfer.getInstance()};
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		final DragSource source = new DragSource(tree, operations);
		source.setTransfer(types);
		source.addDragListener(new DragSourceListener() {
			String selected = null;
			public void dragStart(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				event.doit = false;
				if (selection != null && selection.getFirstElement() != null) {
					Object element = selection.getFirstElement();
					if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
						event.doit = true;
						selected = ""+((AssetWrapper)element).getId(); 
					}
				}
			}
			public void dragSetData(DragSourceEvent event) {
				event.data = selected;
			}
			public void dragFinished(DragSourceEvent event) {
			}
		});

		ViewContentProvider content = new ViewContentProvider(); 
		viewer.setContentProvider(content);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setAutoExpandLevel(3);
		populateView();
		getViewSite().setSelectionProvider(viewer);
		
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE,
				EntityNotification.Type.DELETE
				};
		logger.finest("adding as EntityChangeListener");
		TracController.singleton().addEntityChangeListener(this, interest);
		logger.finest("Will create actions");
		createActions();
		initializeToolBar();
		initializeMenu();
//		getViewSite().getPage().addSelectionListener(ActiveWorldWindView.ID, this);
		logger.finest("exit createPartControl(parent)");
	}

	public void dispose() {
		TracController.singleton().removeEntityChangeListener(this);
		super.dispose();
	}
	public void onEntityNotFound(EntityNotification notification) {
		String entityName = notification.getEntityName();
		if (!entityName.equals(SimpleStatus.class.getName())) {
			System.out.println("EntityNotFound for entity "+entityName);
			populateView();
		}
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		long objectId = notification.getObjectId();
		IStructuredSelection ssel = (IStructuredSelection)viewer.getSelection();
		Object selected = ssel.getFirstElement();
		long selectedId;
		if (selected != null) {
			if (PersistantObject.instanceOf(selected, AssetWrapper.class) || PersistantObject.instanceOf(selected, FactionWrapper.class)) {
				selectedId = ((ObjectWrapper)selected).getId();
			} else {
				selectedId = ((PersistantObject)selected).getId();
			}
			boolean isSelected = false;
			if (selectedId == objectId) {
				isSelected = true;
				if (PersistantObject.instanceOf(entityName,Asset.class)) {
					try {
						((AssetWrapper)selected).refreshObject();
					} catch (Exception e) {
						logger.severe("Error refreshing resolved AssetWrapper object with ID "+((AssetWrapper)selected).getId());
					}
				}
			} else if (PersistantObject.instanceOf(entityName,Unit.class)) {
				Asset asset = ((Unit)notification.getResolved()).getAsset();
				if (selectedId == asset.getId()) {
					isSelected = true;
				}
			}
			if (isSelected) {
				System.out.println("Need to force selection...");
				viewer.setSelection(ssel);
			} else {
				for (AssetWrapper assetWrapper : currentAssets) {
					if (assetWrapper.getId() == notification.getObjectId()) {
						logger.finest("Previously resolved AssetWrapper found for notification, will refresh");
						try {
							assetWrapper.refreshObject();
						} catch (Exception e) {
							logger.severe("Error refreshing resolved AssetWrapper object with ID "+assetWrapper.getId());
						}
					}
				}
			}
		}
		if (PersistantObject.instanceOf(entityName, Faction.class) || PersistantObject.instanceOf(entityName, Asset.class)) {
			logger.finer("Faction/Asset updated, will force populate...");
			this.populateView();
		}
	}
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		System.out.println("Selection changed!!!");
		if (selection != null) {
			IStructuredSelection ssel = (IStructuredSelection)selection;
			/*
			if (ssel.getFirstElement() != null) {
				viewer.setSelection(ssel);
			}
			*/
		}
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	private void initializeToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
	}
	private void createActions() {
		logger.finest("createActions()");
		editFaction = new EditFaction();
		removeFaction = new RemoveFaction();
		editUser = new EditUser();
		removeUser = new RemoveUser();
		removeAsset = new RemoveAsset();
		propertyDialogAction = new PropertyDialogAction(this.getSite(),viewer);
		logger.finest("exit createActions()");
	}
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		/*
		 * Supposed to do it for action contributions, can't get it to work - menu doesn't open???
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		getSite().registerContextMenu((MenuManager)manager, (ISelectionProvider)viewer);
		*/
		createContextMenuActions();
	}
	private void createContextMenuActions() {
		logger.finest("createContextMenuActions()");
		MenuManager manager = new MenuManager();
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				final IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				Action propAction = new Action() {
					public void run() {
						propertyDialogAction.selectionChanged(viewer.getSelection());
						propertyDialogAction.run();
					}
				};
				propAction.setText("Properties");
				manager.add(propAction);
				Object element = selection.getFirstElement();
				boolean isAdmin = TracController.getLoggedIn().fullfillsRole(User.Roles.CLIENTADMIN);
				if (element != null) {
					if (PersistantObject.instanceOf(element,FactionWrapper.class)) {
						Faction faction = ((FactionWrapper)element).getObject();
						editFaction.setFaction(faction);
						manager.add(editFaction);
						removeFaction.setFaction(faction);
						manager.add(removeFaction);
					} else if (PersistantObject.instanceOf(element,User.class)) {
						User user = (User)element;
						if (isAdmin || TracController.getLoggedIn().getId() == user.getId()) {
							editUser.setUser(user);
							manager.add(editUser);
						}
						if (isAdmin) {
							removeUser.setUser((User)element);
							manager.add(removeUser);
						}
					} else if (PersistantObject.instanceOf(element,AssetWrapper.class)) {
						Asset asset = ((AssetWrapper)element).getObject();
						if (isAdmin) {
							removeAsset.setAsset(asset);
							manager.add(removeAsset);
						}
					}
				}
			}
		});
		logger.finest("exit createContextMenuActions()");
	}
}