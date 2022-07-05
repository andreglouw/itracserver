package com.ikno.itracclient.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetOwner;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Owner;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.views.AssetStatusView;

public class EditUserAssetPage extends WizardPage {

	private static final Logger logger = Logging.getLogger(EditUserAssetPage.class.getName());
	private CheckboxTableViewer assetCheckboxTableViewer;
	private Table table;
	public static final String ID = "com.ikno.itracclient.wizards.EditUserAssetPage"; //$NON-NLS-1$
	private User user = null;
	private boolean isCreating = false;
	private boolean isClientUser = false;
	public List<AssetWrapper> possibles = null;
	public List<AssetWrapper> selected = null;

	public List<AssetWrapper> added = new ArrayList<AssetWrapper>();
	public List<AssetWrapper> removed = new ArrayList<AssetWrapper>();
	
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	
	class AssetTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			AssetWrapper asset = (AssetWrapper)element;
			if (columnIndex == 1)
				return asset.getAssetName();
			return asset.getAssetName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class AssetContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<AssetWrapper>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	/**
	 * Create the wizard
	 */
	public EditUserAssetPage(User user, boolean isCreating, boolean isClientUser) {
		super(ID);
		setTitle("Restrict Assets");
		setDescription("Marking an asset restricts the user to viewing only these selected assets.");
		this.user = user;
		this.isCreating = isCreating;
		this.isClientUser = isClientUser;
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		assetCheckboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		assetCheckboxTableViewer.setSorter(new AssetSorter());
		assetCheckboxTableViewer.setLabelProvider(new AssetTableLabelProvider());
		assetCheckboxTableViewer.setContentProvider(new AssetContentProvider());
		assetCheckboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				AssetWrapper asset = (AssetWrapper)event.getElement();
				if (event.getChecked()) {
					added.add(asset);
					if (removed.contains(asset))
						removed.remove(asset);
				} else {
					removed.add(asset);
					if (added.contains(asset))
						added.remove(asset);
				}
			}
		});
		table = assetCheckboxTableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(145);
		newColumnTableColumn.setText("Asset");
		this.setEditable(true,isCreating,isClientUser);
		this.setUser(user);
		this.objectChanged(user);
	}
	public void objectChanged(Object object) {
		String message = validate();
		if (message == null) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(message);
			setPageComplete(false);
		}
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
		this.buildFromObject();
	}
	public void setEditable(boolean editable,boolean isCreating, boolean isClientUser) {
		this.isCreating = isCreating;
		this.isClientUser = isClientUser;
	}
	public String validate() {
		return null;
	}
	public void userClientsModified(List<Client> added, List<Client> removed) {
		for (Client client : removed) {
			for (Asset asset : client.getAssets()) {
				AssetWrapper wrapper = new AssetWrapper(asset);
				if (possibles.contains(wrapper))
					possibles.remove(wrapper);
			}
		}
		for (Client client : added) {
			for (Asset asset : client.getAssets()) {
				AssetWrapper wrapper = new AssetWrapper(asset);
				if (!possibles.contains(wrapper))
					possibles.add(wrapper);
			}
		}
		assetCheckboxTableViewer.setInput(possibles);
	}
	public void buildFromObject() {
		if (user == null)
			return;
		// Give possible access to all the user's client's assets
		boolean ownedAssets = false;
		User loggedIn = TracController.getLoggedIn();
		if (PersistantObject.instanceOf(loggedIn,Owner.class)) {
			ownedAssets = true;
		}
		// All assets selectable to user - depends on the contracts available
		possibles = DAO.localDAO().getAssetWrappersForUser(user,ownedAssets);
		selected = new ArrayList<AssetWrapper>();
		if (possibles == null || possibles.size() == 0) {
			possibles = new ArrayList<AssetWrapper>();
			List<Asset> owned = null;
			if (PersistantObject.instanceOf(user,Owner.class)) {
				this.user = (User)PersistantObject.resolveProxy(user);
				owned = ((Owner)user).getOwnedAssets();
			}
			if (owned != null && owned.size() > 0) {
				for (Asset asset : owned) {
					AssetWrapper wrapper = new AssetWrapper(asset);
					selected.add(wrapper);
				}
			}
		} else {
			List<Asset> owned = null;
			if (PersistantObject.instanceOf(user,Owner.class)) { 
				this.user = (User)PersistantObject.resolveProxy(user);
				owned = ((Owner)user).getOwnedAssets();
			}
			if (owned != null && owned.size() > 0) {
				selected = new ArrayList<AssetWrapper>();
				for (Asset asset : owned) {
					AssetWrapper wrapper = new AssetWrapper(asset);
					int idx = possibles.indexOf(wrapper);
					if (idx >= 0) {
						AssetWrapper link = possibles.get(idx);
						if (link != null)
							selected.add(link);
					} else
						logger.severe("Asset "+asset.getAssetName()+" owned by user "+user.getUsername()+" is no longer in list of assets available to user (as determined by contracts available to user)");
				}
			}
		}
		assetCheckboxTableViewer.setInput(possibles);
		if (selected!= null)
			assetCheckboxTableViewer.setCheckedElements(selected.toArray());
		added = new ArrayList<AssetWrapper>();
		removed = new ArrayList<AssetWrapper>();
	}
	public void populateObject() throws Exception {
		for (AssetWrapper wrapper : removed) {
			Asset asset = wrapper.getObject();
			AssetOwner uc = (PersistantObject.instanceOf(user,Owner.class)) ? ((Owner)user).getAssetOwner(asset) : null;
			if (uc != null) {
				DAO.localDAO().delete(uc);
				((Owner)user).removeAssetOwner(uc);
				asset.removeAssetOwner(uc);
			}
		}
		for (AssetWrapper wrapper : added) {
			Asset asset = wrapper.getObject();
			if (PersistantObject.instanceOf(user,Owner.class)) 
				((Owner)user).addOwnedAsset(asset);
			DAO.localDAO().saveOrUpdate(user);
		}
	}
}
