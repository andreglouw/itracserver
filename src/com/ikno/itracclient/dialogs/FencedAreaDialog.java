package com.ikno.itracclient.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.FencedArea;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.business.Rule;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.GeoArea.GeoAreaWrapper;
import com.ikno.dao.business.rules.implementation.GeoFence;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Configuration;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.Validate;

public class FencedAreaDialog extends Dialog {
	public static final String ID = "com.ikno.itracclient.wizards.AddFencedAreaPage"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(FencedAreaDialog.class.getName());
	private Button okButton;
	private Button deleteButton;
	private Button editButton;
	private CheckboxTableViewer assetTableViewer;
	private Table assetList;
	private List<AssetWrapper> allAssets = null;
	private List<GeoAreaWrapper> allAreas = null;
	private List<AssetWrapper> added = new ArrayList<AssetWrapper>();
	private List<AssetWrapper> removed = new ArrayList<AssetWrapper>();
	private boolean didModify = false;
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((GeoAreaWrapper)e1).compareTo((GeoAreaWrapper)e2);
		}
	}
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			GeoAreaWrapper area = (GeoAreaWrapper)element;
			switch (columnIndex) {
			case 0:
				return area.getAreaName();
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (GeoAreaWrapper[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	class AssetTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			AssetWrapper assetWrapper = (AssetWrapper)element;
			return assetWrapper.getAssetName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class AssetContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class RuleSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((FencedArea)e1).getRuleName().compareTo(((FencedArea)e2).getRuleName());
		}
	}
	class RuleTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return ((FencedArea)element).getRuleName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class RuleTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private CheckboxTableViewer areaTableViewer;
	private Table areaTable;
	private ComboViewer fencedAreaListViewer;
	private Combo fencedAreaList;
	protected Object result;
	protected Shell shell;

	public FencedArea fencedArea;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public FencedAreaDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public FencedAreaDialog(Shell parent) {
		this(parent, SWT.NONE);
	}
	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 586);
		shell.setText("Fenced Area");

		final Label ruleNameLabel = new Label(shell, SWT.NONE);
		ruleNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_ruleNameLabel = new FormData();
		fd_ruleNameLabel.bottom = new FormAttachment(0, 25);
		fd_ruleNameLabel.right = new FormAttachment(0, 75);
		fd_ruleNameLabel.top = new FormAttachment(0, 5);
		fd_ruleNameLabel.left = new FormAttachment(0, 5);
		ruleNameLabel.setLayoutData(fd_ruleNameLabel);
		ruleNameLabel.setText("Rule Name");

		fencedAreaListViewer = new ComboViewer(shell, SWT.BORDER);
		fencedAreaListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection selection = null;
				if (didModify) {
					if (!MessageDialog.openQuestion(shell, "Unsaved", "There are unsaved modifications to the current Rule, continuing will lose these.\nDo you want to continue?")) {
						fencedAreaListViewer.setSelection(new StructuredSelection(fencedArea));
						return;
					}
				}
				selection = (StructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					deleteButton.setEnabled(true);
					editButton.setEnabled(true);
					assetList.setEnabled(true);
					areaTable.setEnabled(true);
					setFencedArea((FencedArea)selection.getFirstElement());
				} else {
					deleteButton.setEnabled(false);
					editButton.setEnabled(false);
					assetList.setEnabled(false);
					areaTable.setEnabled(false);
					setFencedArea(null);
				}
				didModify = false;
			}
		});
		fencedAreaListViewer.setSorter(new RuleSorter());
		fencedAreaListViewer.setLabelProvider(new RuleTableLabelProvider());
		fencedAreaListViewer.setContentProvider(new RuleTableContentProvider());
		fencedAreaListViewer.setInput(DAO.localDAO().fencedAreasForUser(TracController.getLoggedIn()).toArray(new FencedArea[]{}));
		fencedAreaList = fencedAreaListViewer.getCombo();
		final FormData fd_fencedAreaList = new FormData();
		fd_fencedAreaList.right = new FormAttachment(0, 250);
		fd_fencedAreaList.top = new FormAttachment(ruleNameLabel, 0, SWT.TOP);
		fd_fencedAreaList.left = new FormAttachment(ruleNameLabel, 5, SWT.RIGHT);
		fencedAreaList.setLayoutData(fd_fencedAreaList);
		fencedAreaList.setVisibleItemCount(10);

		areaTableViewer = CheckboxTableViewer.newCheckList(shell, SWT.BORDER | SWT.FULL_SELECTION);
		areaTableViewer.setSorter(new Sorter());
		areaTableViewer.setLabelProvider(new TableLabelProvider());
		areaTableViewer.setContentProvider(new ContentProvider());
		areaTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked() == true) {
					GeoAreaWrapper[] checked = new GeoAreaWrapper[]{(GeoAreaWrapper)event.getElement()};
					areaTableViewer.setCheckedElements(checked);
					areaTableViewer.setSelection(new StructuredSelection(checked));
				}
				didModify = true;
				validate();
			}
		});
		allAreas = DAO.localDAO().getAreaWrappersForUser(TracController.getLoggedIn());
		if (allAreas == null)
			allAreas = new ArrayList<GeoAreaWrapper>();
		areaTableViewer.setInput(allAreas.toArray(new GeoAreaWrapper[]{}));
		areaTable = areaTableViewer.getTable();
		areaTable.setEnabled(false);
		final FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(fencedAreaList, 5, SWT.BOTTOM);
		fd_table.bottom = new FormAttachment(100, -227);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0, 5);
		areaTable.setLayoutData(fd_table);
		areaTable.setLinesVisible(true);
		areaTable.setHeaderVisible(true);

		final TableColumn areaNameTableColumn = new TableColumn(areaTable, SWT.NONE);
		areaNameTableColumn.setWidth(200);
		areaNameTableColumn.setText("Area Name");

		assetTableViewer = CheckboxTableViewer.newCheckList(shell, SWT.BORDER);
		assetTableViewer.setSorter(new AssetSorter());
		assetTableViewer.setLabelProvider(new AssetTableLabelProvider());
		assetTableViewer.setContentProvider(new AssetContentProvider());
		assetTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				AssetWrapper asset = (AssetWrapper)event.getElement();
				if (event.getChecked() == true) {
					if (!added.contains(asset))
						added.add(asset);
					removed.remove(asset);
				} else {
					if (!removed.contains(asset))
						removed.add(asset);
					added.remove(asset);
				}
				didModify = true;
				validate();
			}
		});
		assetList = assetTableViewer.getTable();
		assetList.setEnabled(false);
		final FormData fd_assetList = new FormData();
		fd_assetList.top = new FormAttachment(areaTable, 5, SWT.BOTTOM);
		fd_assetList.bottom = new FormAttachment(100, -30);
		fd_assetList.right = new FormAttachment(100, -5);
		fd_assetList.left = new FormAttachment(0, 5);
		assetList.setLayoutData(fd_assetList);
		assetList.setLinesVisible(true);
		assetList.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(assetList, SWT.NONE);
		newColumnTableColumn.setWidth(200);
		newColumnTableColumn.setText("Asset");

		allAssets = DAO.localDAO().getAssetWrappersForUser(TracController.getLoggedIn(),true);
		if (allAssets == null)
			allAssets = new ArrayList<AssetWrapper>();
		assetTableViewer.setInput(allAssets.toArray(new AssetWrapper[]{}));
		assetTableViewer.setCheckedElements(new AssetWrapper[]{});

		okButton = new Button(shell, SWT.NONE);
		okButton.setEnabled(false);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				if (fencedArea != null && didModify == true) {
					if (areaTableViewer.getCheckedElements().length == 0) {
						MessageDialog.openError(shell, "No Area", "Please select an area associated with this rule");
						return;
					}
					try {
						DAO.localDAO().beginTransaction();
						Object[] checked = areaTableViewer.getCheckedElements();
						fencedArea.setGeoArea(((GeoAreaWrapper)checked[0]).getObject());
						for (AssetWrapper asset : added) {
							asset.getObject().addRule(fencedArea);
						}
						for (AssetWrapper asset : removed) {
							asset.getObject().removeRule(fencedArea);
						}
						DAO.localDAO().commitTransaction();
						result = fencedArea;
					} catch (Exception exc) {
						logger.log(Level.SEVERE,"Exception commiting FencedArea mods:",exc);
						DAO.localDAO().rollbackTransaction();
					}
				}
				shell.close();
			}
		});
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -2);
		fd_okButton.top = new FormAttachment(100, -25);
		fd_okButton.right = new FormAttachment(100, -5);
		fd_okButton.left = new FormAttachment(100, -47);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(100, -2);
		fd_cancelButton.top = new FormAttachment(100, -25);
		fd_cancelButton.right = new FormAttachment(100, -47);
		fd_cancelButton.left = new FormAttachment(100, -92);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		editButton = new Button(shell, SWT.NONE);
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)fencedAreaListViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					FencedAreaDetail dialog = new FencedAreaDetail(shell);
					Object result = dialog.open((FencedArea)selection.getFirstElement());
					if (result != null) {
						fencedAreaListViewer.update(result, null);
						fencedAreaListViewer.reveal(result);
					}
				}
			}
		});
		final FormData fd_editButton = new FormData();
		editButton.setLayoutData(fd_editButton);
		editButton.setText("Edit");

		Button newButton;
		newButton = new Button(shell, SWT.NONE);
		fd_editButton.top = new FormAttachment(newButton, -21, SWT.BOTTOM);
		fd_editButton.bottom = new FormAttachment(newButton, 0, SWT.BOTTOM);
		fd_editButton.right = new FormAttachment(newButton, 50, SWT.RIGHT);
		fd_editButton.left = new FormAttachment(newButton, 0, SWT.RIGHT);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FencedAreaDetail dialog = new FencedAreaDetail(shell);
				FencedArea fencedArea = new FencedArea();
				Object result = dialog.open(fencedArea);
				if (result != null) {
					fencedAreaListViewer.add(result);
					fencedAreaListViewer.reveal(result);
				}
			}
		});
		final FormData fd_newButton = new FormData();
		fd_newButton.bottom = new FormAttachment(fencedAreaList, 21, SWT.TOP);
		fd_newButton.top = new FormAttachment(fencedAreaList, 0, SWT.TOP);
		fd_newButton.right = new FormAttachment(fencedAreaList, 60, SWT.RIGHT);
		fd_newButton.left = new FormAttachment(fencedAreaList, 5, SWT.RIGHT);
		newButton.setLayoutData(fd_newButton);
		newButton.setText("New");

		deleteButton = new Button(shell, SWT.NONE);
		deleteButton.setEnabled(false);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (!MessageDialog.openQuestion(shell,"Delete Rule","Are you sure you want to Delete this Rule?"))
					return;
				try {
					DAO.localDAO().beginTransaction();
					DAO.localDAO().deleteRule(fencedArea);
					DAO.localDAO().commitTransaction();
					fencedAreaListViewer.remove(fencedArea);
					fencedAreaListViewer.setSelection(null);
				} catch (Exception exc) {
					logger.log(Level.SEVERE,"Exception deleteing FencedArea:",exc);
					DAO.localDAO().rollbackTransaction();
				}
			}
		});
		final FormData fd_deleteButton = new FormData();
		fd_deleteButton.top = new FormAttachment(editButton, -21, SWT.BOTTOM);
		fd_deleteButton.bottom = new FormAttachment(editButton, 0, SWT.BOTTOM);
		fd_deleteButton.left = new FormAttachment(0, 360);
		fd_deleteButton.right = new FormAttachment(0, 415);
		deleteButton.setLayoutData(fd_deleteButton);
		deleteButton.setText("Delete");
	}
	
	public void setFencedArea(FencedArea fencedArea) {
		this.fencedArea = fencedArea;
		if (fencedArea != null) {
			fencedAreaListViewer.reveal(fencedArea);
			GeoArea area = fencedArea.getGeoArea();
			GeoAreaWrapper[] checkedAreas = null;
			if (area == null)
				checkedAreas = new GeoAreaWrapper[]{};
			else {
				for (GeoAreaWrapper warea : allAreas) {
					if (warea.getId() == area.getId()) {
						checkedAreas = new GeoAreaWrapper[]{warea};
						break;
					}
				}
			}
			areaTableViewer.setCheckedElements(checkedAreas);
			List<Asset> assets = fencedArea.getAssets();
			AssetWrapper[] checkedAssets = null;
			if (assets == null)
				checkedAssets = new AssetWrapper[]{};
			else {
				checkedAssets = new AssetWrapper[assets.size()];
				int i = 0;
				for (Asset asset : assets) {
					for (AssetWrapper wasset : allAssets) {
						if (wasset.getId() == asset.getId()) {
							checkedAssets[i++] = wasset;
							break;
						}
					}
				}
			}
			assetTableViewer.setCheckedElements(checkedAssets);
		} else {
			areaTableViewer.setCheckedElements(new GeoAreaWrapper[]{});
			assetTableViewer.setCheckedElements(new Asset[]{});
		}
		validate();
	}
	public void validate() {
		if (fencedArea != null) {
			Object[] checkedAssets = assetTableViewer.getCheckedElements(); 
			if (checkedAssets == null || checkedAssets.length == 0) {
				okButton.setEnabled(false);
			} else {
				Object[] checkedArea = areaTableViewer.getCheckedElements(); 
				if (checkedArea == null || checkedArea.length == 0) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		} else
			okButton.setEnabled(false);
	}
}
