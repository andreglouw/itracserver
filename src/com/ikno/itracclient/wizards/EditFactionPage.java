package com.ikno.itracclient.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;

public class EditFactionPage extends WizardPage {
	public static final String ID = "com.ikno.itracclient.wizards.EditFactionPage";
	private boolean isCreating;
	private Button delAsset;
	private Button addAsset;
	private Label groupNameLabel;
	private ListViewer targetListViewer;
	private ListViewer sourceListViewer;
	private List targetList;
	private List sourceList;
	private Text groupName;

	private Faction faction;
	private java.util.List<AssetWrapper> sourceAssets;
	private java.util.List<AssetWrapper> targetAssets;
	private java.util.List<AssetWrapper> added = new ArrayList<AssetWrapper>();
	private java.util.List<AssetWrapper> removed = new ArrayList<AssetWrapper>();

	class SourceSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	class SourceLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((AssetWrapper)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class SourceContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((ArrayList<AssetWrapper>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class TargetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	class TargetLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((AssetWrapper)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class TargetContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((ArrayList<AssetWrapper>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	/**
	 * Create the wizard
	 */
	public EditFactionPage(Faction faction, boolean isCreating, java.util.List<AssetWrapper> assets) {
		super(EditFactionPage.ID);
		setTitle("Asset group");
		setDescription("Create or Edit a group of assets.");
		this.faction = faction;
		this.targetAssets = DAO.localDAO().getAssetWrappersForFaction(faction.getId());
		this.sourceAssets = assets;
		for (Iterator<AssetWrapper> ai = this.targetAssets.iterator();ai.hasNext();) {
			this.sourceAssets.remove(ai.next());
		}
	}

	public void populateFaction(Faction faction) {
		for (AssetWrapper asset : removed) {
			faction.removeAsset(asset.getObject());
		}
		for (AssetWrapper asset : added) {
			faction.addAsset(asset.getObject());
		}
	}
	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		
		final FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(100, -5);
		fd_composite.right = new FormAttachment(100, -5);
		fd_composite.top = new FormAttachment(0, 5);
		fd_composite.left = new FormAttachment(0, 5);
		container.setLayoutData(fd_composite);
		container.setLayout(new FormLayout());

		groupNameLabel = new Label(container, SWT.NONE);
		groupNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_groupNameLabel = new FormData();
		fd_groupNameLabel.right = new FormAttachment(0, 70);
		fd_groupNameLabel.bottom = new FormAttachment(0, 25);
		fd_groupNameLabel.top = new FormAttachment(0, 5);
		fd_groupNameLabel.left = new FormAttachment(0, 5);
		groupNameLabel.setLayoutData(fd_groupNameLabel);
		groupNameLabel.setText("Group Name");

		groupName = new Text(container, SWT.BORDER);
		groupName.addFocusListener(new FocusAdapter() {
			public void focusLost(final FocusEvent e) {
				if (groupName.getText().equals("")) {
					setErrorMessage("You must supply a valid group name");
					groupName.setText(faction.getFactionName());
					setPageComplete(false);
				} else {
					User loggedIn = TracController.getLoggedIn();
					String newName = groupName.getText();
					if (isCreating && DAO.localDAO().getFactionWithName(newName, loggedIn.getId()) != null) {
						setErrorMessage("A group with that name already exists for this user");
						setPageComplete(false);
					} else {
						faction.setFactionName(groupName.getText());
						setPageComplete(true);
					}
				}
			}
		});
		final FormData fd_groupName = new FormData();
		fd_groupName.right = new FormAttachment(0, 350);
		fd_groupName.bottom = new FormAttachment(groupNameLabel, 0, SWT.BOTTOM);
		fd_groupName.top = new FormAttachment(groupNameLabel, 0, SWT.TOP);
		fd_groupName.left = new FormAttachment(groupNameLabel, 5, SWT.RIGHT);
		groupName.setLayoutData(fd_groupName);

		sourceListViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		sourceListViewer.setSorter(new SourceSorter());
		sourceListViewer.setLabelProvider(new SourceLabelProvider());
		sourceListViewer.setContentProvider(new SourceContentProvider());
		sourceListViewer.setInput(sourceAssets);
		sourceList = sourceListViewer.getList();
		final FormData fd_sourceList = new FormData();
		fd_sourceList.bottom = new FormAttachment(100, -5);
		fd_sourceList.top = new FormAttachment(0, 30);
		fd_sourceList.right = new FormAttachment(47, 0);
		fd_sourceList.left = new FormAttachment(0, 5);
		sourceList.setLayoutData(fd_sourceList);

		targetListViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		targetListViewer.setSorter(new TargetSorter());
		targetListViewer.setLabelProvider(new TargetLabelProvider());
		targetListViewer.setContentProvider(new TargetContentProvider());
		targetListViewer.setInput(targetAssets);
		targetList = targetListViewer.getList();
		final FormData fd_targetList = new FormData();
		fd_targetList.bottom = new FormAttachment(sourceList, 0, SWT.BOTTOM);
		fd_targetList.top = new FormAttachment(0, 30);
		fd_targetList.right = new FormAttachment(100, -5);
		fd_targetList.left = new FormAttachment(55, 0);
		targetList.setLayoutData(fd_targetList);

		addAsset = new Button(container, SWT.NONE);
		addAsset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)sourceListViewer.getSelection();
				if (selection != null) {
					for (Iterator oi = selection.iterator();oi.hasNext();) {
						AssetWrapper element = (AssetWrapper)oi.next();
						targetAssets.add(element);
						sourceAssets.remove(element);
						added.add(element);
						removed.remove(element);
					}
					sourceListViewer.setInput(sourceAssets);
					targetListViewer.setInput(targetAssets);
				}
			}
		});
		final FormData fd_addAsset = new FormData();
		fd_addAsset.left = new FormAttachment(targetList, -34, SWT.LEFT);
		fd_addAsset.right = new FormAttachment(targetList, -5, SWT.LEFT);
		addAsset.setLayoutData(fd_addAsset);
		addAsset.setText(">>");

		delAsset = new Button(container, SWT.NONE);
		fd_addAsset.top = new FormAttachment(delAsset, -30, SWT.TOP);
		fd_addAsset.bottom = new FormAttachment(delAsset, -5, SWT.TOP);
		delAsset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)targetListViewer.getSelection();
				if (selection != null) {
					for (Iterator oi = selection.iterator();oi.hasNext();) {
						AssetWrapper element = (AssetWrapper)oi.next();
						targetAssets.remove(element);
						sourceAssets.add(element);
						removed.add(element);
						added.remove(element);
					}
					sourceListViewer.setInput(sourceAssets);
					targetListViewer.setInput(targetAssets);
				}
			}
		});
		final FormData fd_delAsset = new FormData();
		fd_delAsset.bottom = new FormAttachment(0, 125);
		fd_delAsset.top = new FormAttachment(0, 100);
		fd_delAsset.right = new FormAttachment(addAsset, 0, SWT.RIGHT);
		fd_delAsset.left = new FormAttachment(addAsset, 0, SWT.LEFT);
		delAsset.setLayoutData(fd_delAsset);
		delAsset.setText("<<");
		//
		String value = faction.getFactionName();
		groupName.setText(value);
		groupName.setSelection(0, value.length());
		groupName.setFocus();
		setControl(container);
		container.setTabList(new Control[] {groupNameLabel, groupName, sourceList, addAsset, targetList, delAsset});
	}
}
