package com.ikno.itracclient.dialogs;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.EyesOn;
import com.ikno.dao.business.Recipient;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.UnitRecipient;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserRecipient;
import com.ikno.dao.business.Watcher;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.servlets.KMLService;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.startup.LoginPanel;
import com.swtdesigner.ResourceManager;

public class WatcherDialog extends Dialog {
	private Button userWatcherButton;
	private Button addUserWatcherButton;
	private Button unitWatcherButton;
	private Button assetButton;
	private static Logger logger = Logging.getLogger(WatcherDialog.class.getName());

	private Button addAssetWatcherButton;
	private Button removeWatcherButton;
	private Button addEyesOnButton;
	private Button removeEyesOnButton;
	private ComboViewer selectionListViewer;
	private TableViewer eyesOnTableViewer;
	private Table eyesOnTable;
	private TableViewer watcherTableViewer;
	private Watcher watcher;
	private List<Watcher> lwatchers;
	private List<Watcher> removedWatchers = new ArrayList<Watcher>();
	private List<Watcher> addedWatchers = new ArrayList<Watcher>();
	private List<Watcher> modifiedWatchers = new ArrayList<Watcher>();
	private Map<Watcher,List<EyesOn>> modifiedEyesOn = new HashMap<Watcher,List<EyesOn>>();
	private Asset asset;
	private Recipient recipient;
	private boolean typeChanged = true;
	private enum TYPE {
		USERWATCHER,
		UNITWATCHER,
		ASSET;
	};
	private TYPE current;

	public abstract class EmulatedNativeCheckBoxLabelProvider extends ColumnLabelProvider {
		private static final String CHECKED_KEY = "CHECKED";
		private static final String UNCHECK_KEY = "UNCHECKED";

		public EmulatedNativeCheckBoxLabelProvider(ColumnViewer viewer)	{
			if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null) {
				JFaceResources.getImageRegistry().put(UNCHECK_KEY, makeShot(viewer.getControl(), false));
				JFaceResources.getImageRegistry().put(CHECKED_KEY, makeShot(viewer.getControl(), true));
			}
		}

		private Image makeShot(Control control, boolean type) {
			Shell shell = new Shell(control.getShell(), SWT.NO_TRIM);

			// otherwise we have a default gray color
			Color backgroundColor = control.getBackground();
			shell.setBackground(backgroundColor);

			Button button = new Button(shell, SWT.CHECK);
			button.setBackground(backgroundColor);
			button.setSelection(type);

			// otherwise an image is located in a corner
			button.setLocation(1, 1);
			Point bsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			// otherwise an image is stretched by width
			bsize.x = Math.max(bsize.x-1, bsize.y-1);
			bsize.y = Math.max(bsize.x-1, bsize.y-1);
			button.setSize(bsize);
			shell.setSize(bsize);

			shell.open();
			GC gc = new GC(shell);
			Image image = new Image(control.getDisplay(), bsize.x, bsize.y);
			gc.copyArea(image, 0, 0);
			gc.dispose();
			shell.close();

			return image;
		}

		public Image getImage(Object element) {
			if (isChecked(element))	{
				return JFaceResources.getImageRegistry().get(CHECKED_KEY);
			} else {
				return JFaceResources.getImageRegistry().get(UNCHECK_KEY);
			}
		}

		protected abstract boolean isChecked(Object element);
	}

	String[] scats;
	SimpleStatus.Category[] categories = SimpleStatus.watchableCategories;
	String[] sstates;
	SimpleStatus.State[] states = SimpleStatus.watchableStates;
	public class EyesOnEditingSupport extends EditingSupport {
		private CellEditor editor;
		private int column;
		public EyesOnEditingSupport(ColumnViewer viewer, int column) {
			super(viewer);
			switch (column) {
			case 0:
				editor = new ComboBoxCellEditor(((TableViewer)viewer).getTable(),scats);
				break;
			case 1:
				editor = new ComboBoxCellEditor(((TableViewer)viewer).getTable(),sstates);
				break;
			case 2:
			case 3:
			case 4:
				editor = new CheckboxCellEditor(null,SWT.CHECK);
				break;
			case 5:
				editor = new TextCellEditor(((TableViewer)viewer).getTable());
				((TextCellEditor)editor).setValidator(new ICellEditorValidator() {
					public String isValid(Object value) {
						try {
							Integer.parseInt((String)value);
							return null;
						} catch (Exception e) {}
						return "Numerics only";
					}
				});
				break;
			default:
				editor = new TextCellEditor(((TableViewer)viewer).getTable());
			}
			this.column = column;
		}
		protected boolean canEdit(Object element) {
			return true;
		}

		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		protected Object getValue(Object element) {
			EyesOn eyesOn = (EyesOn)element;
			switch (column) {
			case 0: // Category
				SimpleStatus.Category cat = eyesOn.getCategory();
				return Arrays.binarySearch(scats, cat.toString());
			case 1: // State
				SimpleStatus.State state = eyesOn.getState();
				return Arrays.binarySearch(sstates, state.toString());
			case 2:
				return (eyesOn.isSet(EyesOn.TriggerReportType.SMS));
			case 3:
				return (eyesOn.isSet(EyesOn.TriggerReportType.Email));
			case 4:
				return (eyesOn.isSet(EyesOn.TriggerReportType.Position));
			case 5:
				return String.valueOf(eyesOn.getReportTime());
			default:
				String value = eyesOn.getMessageFormat();
			if (value == null)
				value = "";
			return value;
			}
		}

		protected void setValue(Object element, Object value) {
			EyesOn eyesOn = (EyesOn)element;
			switch (column) {
			case 0: // Is selected
				String scat = scats[((Integer)value)];
				for (SimpleStatus.Category cat : categories) {
					if (cat.toString().equals(scat)) {
						eyesOn.setCategory(cat);
						break;
					}
				}
				break;
			case 1:
				String sstat = sstates[((Integer)value)];
				for (SimpleStatus.State stat : states) {
					if (stat.toString().equals(sstat)) {
						eyesOn.setState(stat);
						break;
					}
				}
				break;
			case 2:
				if ((Boolean)value)
					eyesOn.switchOn(EyesOn.TriggerReportType.SMS);
				else
					eyesOn.switchOff(EyesOn.TriggerReportType.SMS);
				break;
			case 3:
				if ((Boolean)value)
					eyesOn.switchOn(EyesOn.TriggerReportType.Email);
				else
					eyesOn.switchOff(EyesOn.TriggerReportType.Email);
				break;
			case 4:
				if ((Boolean)value)
					eyesOn.switchOn(EyesOn.TriggerReportType.Position);
				else
					eyesOn.switchOff(EyesOn.TriggerReportType.Position);
				break;
			case 5:
				try {
					eyesOn.setReportTime(Integer.parseInt((String)value));
				} catch (Exception e) {}
				break;
			default:
				String text = (String)value;
			if (text != null && text.length() == 0)
				text = null;
			eyesOn.setMessageFormat(text);
			}
			if (eyesOn.getId() != 0) {
				List<EyesOn> list = modifiedEyesOn.get(watcher);
				if (list == null) {
					list = new ArrayList<EyesOn>();
					modifiedEyesOn.put(watcher, list);
				}
				if (!list.contains(eyesOn))
					list.add(eyesOn);
			}
			getViewer().update(element, null);
		}
	}
	private Table watcherTable;
	class EyesOnTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((Set<EyesOn>)inputElement).toArray();
			return new EyesOn[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class EyesOnTableSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((EyesOn)e1).getCategory().toString().compareTo(((EyesOn)e2).getCategory().toString());
		}
	}
	class WatcherTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<Watcher>)inputElement).toArray();
			return new Watcher[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class WatcherTableSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (current == TYPE.ASSET) {
				Recipient r1 = ((Watcher)e1).getRecipient();
				Recipient r2 = ((Watcher)e2).getRecipient();
				if (r1 != null && r2 != null)
					return r1.getDescription().compareTo(r2.getDescription());
				return 0;
			}
			return ((Watcher)e1).getAsset().getAssetName().compareTo(((Watcher)e2).getAsset().getAssetName());
		}
	}
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	class AssetListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((AssetWrapper)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class AssetListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<AssetWrapper>)inputElement).toArray();
			return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	// User content providers
	class RecipientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Recipient)e1).getDescription().compareTo(((Recipient)e2).getDescription());
		}
	}
	class RecipientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Recipient)element).getDescription();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class RecipientListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<Recipient>)inputElement).toArray();
			return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Combo selectionList;
	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public WatcherDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public WatcherDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	public void saveState() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(WatcherDialog.class.getSimpleName());
		if (section == null)
			section = dialogSettings.addNewSection(WatcherDialog.class.getSimpleName());
		section.put("watcherTableColumnCount", watcherTable.getColumnCount());
		int idx = 0;
		for (TableColumn column : watcherTable.getColumns()) {
			section.put("watcherTableColumnCount_"+idx++, column.getWidth());
		}
		section.put("eyesOnTableColumnCount", eyesOnTable.getColumnCount());
		idx = 0;
		for (TableColumn column : eyesOnTable.getColumns()) {
			section.put("eyesOnTableColumnCount_"+idx++, column.getWidth());
		}
	}
	public void restoreState() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(WatcherDialog.class.getSimpleName());
		if (section == null)
			section = dialogSettings.addNewSection(WatcherDialog.class.getSimpleName());
		try {
			Integer tableColumnCount = section.getInt("watcherTableColumnCount");
			if (tableColumnCount != null && tableColumnCount == 5) {
				watcherColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = section.getInt("watcherTableColumnCount_"+idx);
					if (cw != null)
						watcherColumnWidths[idx] = cw; 
				}
			}
		} catch (Exception e) {}
		try {
			Integer tableColumnCount = section.getInt("eyesOnTableColumnCount");
			if (tableColumnCount != null && tableColumnCount == 6) {
				eyesOnColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = section.getInt("eyesOnTableColumnCount_"+idx);
					if (cw != null)
						eyesOnColumnWidths[idx] = cw; 
				}
			}
		} catch (Exception e) {}
	}
	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		restoreState();
		createContents();
		current = TYPE.ASSET;
		assetButton.setSelection(true);
		this.setAsset(null);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	private Integer[] watcherColumnWidths = null;
	private Integer[] eyesOnColumnWidths = null;

	public boolean applyChanges() {
		boolean result = true;
		try {
			DAO.localDAO().beginTransaction();
			for (Watcher watcher : removedWatchers) {
				if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
					recipient.removeWatcher(watcher);
				} else {
					List<Watcher> toRemove = new ArrayList<Watcher>();
					for (Watcher a_watcher : asset.getWatchers()) {
						if (a_watcher.equals(watcher))
							toRemove.add(a_watcher);
					}
					for (Watcher r_watcher : toRemove) {
						asset.removeWatcher(r_watcher);
					}
				}
			}
			for (Watcher watcher : addedWatchers) {
				if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
					recipient.addWatcher(watcher);
					DAO.localDAO().save(watcher);
				} else {
					asset.addWatcher(watcher);
				}
			}
			for (Watcher watcher : modifiedWatchers) {
				if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
					for (Watcher aw : recipient.getWatchers()) {
						if (aw.equals(watcher)) {
							aw.updateMutable(watcher);
						}
					}
				} else {
					for (Watcher aw : asset.getWatchers()) {
						if (aw.equals(watcher)) {
							aw.updateMutable(watcher);
						}
					}
				}
			}
			for (Entry<Watcher,List<EyesOn>> entry : modifiedEyesOn.entrySet()) {
				if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
					for (Watcher watcher : recipient.getWatchers()) {
						if (watcher.equals(entry.getKey())) {
							Watcher m_watcher = entry.getKey();
							for (EyesOn a_eyesOn : watcher.getEyesOn()) {
								for (EyesOn w_eyesOn : m_watcher.getEyesOn()) {
									if (a_eyesOn.equals(w_eyesOn)) {
										a_eyesOn.updateMutable(w_eyesOn);
									}
								}
							}
						}
					}
				} else {
					for (Watcher watcher : asset.getWatchers()) {
						if (watcher.equals(entry.getKey())) {
							Watcher m_watcher = entry.getKey();
							for (EyesOn a_eyesOn : watcher.getEyesOn()) {
								for (EyesOn w_eyesOn : m_watcher.getEyesOn()) {
									if (a_eyesOn.equals(w_eyesOn)) {
										a_eyesOn.updateMutable(w_eyesOn);
									}
								}
							}
						}
					}
				}
			}
			if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
				DAO.localDAO().save(recipient);
			} else {
				DAO.localDAO().save(asset);
			}
			DAO.localDAO().commitTransaction();
		} catch (Exception exc) {
			DAO.localDAO().rollbackTransaction();
			try {
				if (current == TYPE.USERWATCHER || current == TYPE.UNITWATCHER) {
					DAO.localDAO().refresh(recipient);
				} else {
					DAO.localDAO().refresh(asset);
				}
			} catch (Exception e1) {
				logger.log(Level.SEVERE,"Error restoring Asset "+asset.getId()+" to previous state after error modifying Watchers",e1);
			}
			result = false;
		}
		removedWatchers = new ArrayList<Watcher>();
		addedWatchers = new ArrayList<Watcher>();
		modifiedWatchers = new ArrayList<Watcher>();
		modifiedEyesOn = new HashMap<Watcher,List<EyesOn>>();
		return result;
	}
	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 571);
		shell.setText("Watchers");

		selectionListViewer = new ComboViewer(shell, SWT.BORDER);
		selectionListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				switch (current) {
					case ASSET:
						if (selection != null && selection.getFirstElement() != null) {
							Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
							if (asset != WatcherDialog.this.asset) {
								if (removedWatchers.size() > 0 || addedWatchers.size() > 0 || modifiedWatchers.size() > 0 || modifiedEyesOn.size() > 0) {
									if (MessageDialog.openQuestion(shell, "Error", "There are unsaved changes to asset "+WatcherDialog.this.asset.getAssetName()+".\n" +
									"By continuing all changes will be lost. Continue?") == false) {
										selection = new StructuredSelection(new AssetWrapper(WatcherDialog.this.asset));
										selectionListViewer.setSelection(selection,true);
										return;
									}
								}
								setAsset(asset);
							}
						} else {
							setAsset(null);
						}
						break;
					case USERWATCHER:
					case UNITWATCHER:
						if (selection != null && selection.getFirstElement() != null) {
							Recipient recipient = (Recipient)selection.getFirstElement();
							if (recipient != WatcherDialog.this.recipient) {
								if (removedWatchers.size() > 0 || addedWatchers.size() > 0 || modifiedWatchers.size() > 0 || modifiedEyesOn.size() > 0) {
									if (MessageDialog.openQuestion(shell, "Error", "There are unsaved changes to Recipient "+WatcherDialog.this.recipient.getDescription()+".\n" +
									"By continuing all changes will be lost. Continue?") == false) {
										selection = new StructuredSelection(new AssetWrapper(WatcherDialog.this.asset));
										selectionListViewer.setSelection(selection,true);
										return;
									}
								}
								setRecipient(recipient);
							}
						} else {
							setRecipient(null);
						}
						break;
				}
			}
		});
		selectionList = selectionListViewer.getCombo();
		selectionList.setVisibleItemCount(10);
		final FormData fd_selectionList = new FormData();
		fd_selectionList.right = new FormAttachment(0, 478);
		selectionList.setLayoutData(fd_selectionList);
		final Button okButton = new Button(shell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				applyChanges();
				saveState();
				shell.close();
			}
		});
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -2);
		fd_okButton.top = new FormAttachment(100, -27);
		fd_okButton.right = new FormAttachment(100, -5);
		fd_okButton.left = new FormAttachment(100, -59);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				saveState();
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.top = new FormAttachment(okButton, -25, SWT.BOTTOM);
		fd_cancelButton.bottom = new FormAttachment(okButton, 0, SWT.BOTTOM);
		fd_cancelButton.right = new FormAttachment(100, -109);
		fd_cancelButton.left = new FormAttachment(100, -157);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		final Group keepWatchOnGroup = new Group(shell, SWT.NONE);
		keepWatchOnGroup.setText("Keep Watch On");
		final FormData fd_keepWatchOnGroup = new FormData();
		fd_keepWatchOnGroup.bottom = new FormAttachment(cancelButton, -5, SWT.TOP);
		fd_keepWatchOnGroup.right = new FormAttachment(100, -5);
		fd_keepWatchOnGroup.left = new FormAttachment(0, 5);
		keepWatchOnGroup.setLayoutData(fd_keepWatchOnGroup);
		keepWatchOnGroup.setLayout(new FormLayout());

		if (eyesOnColumnWidths == null)
			eyesOnColumnWidths = new Integer[]{100,100,50,50,50,50,260};
		eyesOnTableViewer = new TableViewer(keepWatchOnGroup, SWT.FULL_SELECTION | SWT.BORDER);
		eyesOnTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					removeEyesOnButton.setEnabled(true);
				} else {
					removeEyesOnButton.setEnabled(false);
				}
			}
		});
		eyesOnTableViewer.setContentProvider(new EyesOnTableContentProvider());
		eyesOnTableViewer.setSorter(new EyesOnTableSorter());
		eyesOnTable = eyesOnTableViewer.getTable();
		final FormData fd_eyesOnTable = new FormData();
		fd_eyesOnTable.bottom = new FormAttachment(100, -35);
		fd_eyesOnTable.top = new FormAttachment(0, 5);
		fd_eyesOnTable.right = new FormAttachment(100, -5);
		fd_eyesOnTable.left = new FormAttachment(0, 5);
		eyesOnTable.setLayoutData(fd_eyesOnTable);
		eyesOnTable.setLinesVisible(true);
		eyesOnTable.setHeaderVisible(true);

		scats = new String[categories.length];
		int i = 0;
		for (SimpleStatus.Category cat : categories) {
			scats[i++] = cat.toString();
		}
		Arrays.sort(scats);

		sstates = new String[states.length];
		i = 0;
		for (SimpleStatus.State state : states) {
			sstates[i++] = state.toString();
		}
		Arrays.sort(sstates);

		addEyesOnButton = new Button(keepWatchOnGroup, SWT.NONE);
		addEyesOnButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)watcherTableViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					EyesOn eyesOn = new EyesOn();
					eyesOn.setCategory(SimpleStatus.Category.ALARM);
					eyesOn.setState(SimpleStatus.State.BEGIN);
					watcher.addEyesOn(eyesOn);
					if (watcher.getId() != 0 && !modifiedWatchers.add(watcher))
						modifiedWatchers.add(watcher);
					eyesOnTableViewer.setInput(watcher.getEyesOn());
				}
			}
		});
		final FormData fd_addEyesOnButton = new FormData();
		fd_addEyesOnButton.left = new FormAttachment(eyesOnTable, -40, SWT.RIGHT);
		fd_addEyesOnButton.right = new FormAttachment(eyesOnTable, 0, SWT.RIGHT);
		addEyesOnButton.setLayoutData(fd_addEyesOnButton);
		addEyesOnButton.setText("Add");
		addEyesOnButton.setEnabled(false);

		removeEyesOnButton = new Button(keepWatchOnGroup, SWT.NONE);
		removeEyesOnButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)eyesOnTableViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					EyesOn eyesOn = (EyesOn)selection.getFirstElement();
					if (eyesOn != null) {
						watcher.removeEyesOn(eyesOn);
						if (watcher.getId() != 0 && !modifiedWatchers.add(watcher))
							modifiedWatchers.add(watcher);
						List<EyesOn> list = modifiedEyesOn.get(watcher);
						if (list != null && list.contains(eyesOn))
							list.remove(eyesOn);
						eyesOnTableViewer.setInput(watcher.getEyesOn());
					}
				}
			}
		});
		fd_addEyesOnButton.top = new FormAttachment(removeEyesOnButton, -25, SWT.BOTTOM);
		fd_addEyesOnButton.bottom = new FormAttachment(removeEyesOnButton, 0, SWT.BOTTOM);
		final FormData fd_removeEyesOnButton = new FormData();
		fd_removeEyesOnButton.bottom = new FormAttachment(100, -5);
		fd_removeEyesOnButton.top = new FormAttachment(100, -30);
		fd_removeEyesOnButton.right = new FormAttachment(100, -46);
		fd_removeEyesOnButton.left = new FormAttachment(100, -96);
		removeEyesOnButton.setLayoutData(fd_removeEyesOnButton);
		removeEyesOnButton.setText("Remove");
		removeEyesOnButton.setEnabled(false);

		final TableViewerColumn categoryColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		categoryColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[0]);
		categoryColumnTableColumn.getColumn().setText("Category");
		categoryColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((EyesOn)element).getCategory().toString();
			}
		});
		categoryColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,0));

		final TableViewerColumn stateColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		stateColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[1]);
		stateColumnTableColumn.getColumn().setText("State");
		stateColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((EyesOn)element).getState().toString();
			}
		});
		stateColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,1));

		final TableViewerColumn e_smsColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		e_smsColumnTableColumn.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(eyesOnTableViewer) {
			public String getText(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return String.valueOf((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.SMS) == EyesOn.TriggerReportType.SMS);
			}
			protected boolean isChecked(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return ((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.SMS) == EyesOn.TriggerReportType.SMS);
			}
		});
		e_smsColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[2]);
		e_smsColumnTableColumn.getColumn().setResizable(true);
		e_smsColumnTableColumn.getColumn().setText("SMS");
		e_smsColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,2));

		final TableViewerColumn e_emailColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		e_emailColumnTableColumn.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(eyesOnTableViewer) {
			public String getText(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return String.valueOf((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.Email) == EyesOn.TriggerReportType.Email);
			}
			protected boolean isChecked(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return ((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.Email) == EyesOn.TriggerReportType.Email);
			}
		});
		e_emailColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[3]);
		e_emailColumnTableColumn.getColumn().setResizable(true);
		e_emailColumnTableColumn.getColumn().setText("EMAIL");
		e_emailColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,3));

		final TableViewerColumn e_positionColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		e_positionColumnTableColumn.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(eyesOnTableViewer) {
			public String getText(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return String.valueOf((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.Position) == EyesOn.TriggerReportType.Position);
			}
			protected boolean isChecked(Object element) {
				EyesOn eyesOn = (EyesOn)element;
				return ((eyesOn.getTriggerReport()&EyesOn.TriggerReportType.Position) == EyesOn.TriggerReportType.Position);
			}
		});
		e_positionColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[4]);
		e_positionColumnTableColumn.getColumn().setResizable(true);
		e_positionColumnTableColumn.getColumn().setText("POS RPT");
		e_positionColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,4));

		final TableViewerColumn rtimeColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		rtimeColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[5]);
		rtimeColumnTableColumn.getColumn().setText("Interval");
		rtimeColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return String.valueOf(((EyesOn)element).getReportTime());
			}
		});
		rtimeColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,5));

		final TableViewerColumn messageColumnTableColumn = new TableViewerColumn(eyesOnTableViewer, SWT.NONE);
		messageColumnTableColumn.getColumn().setWidth(eyesOnColumnWidths[6]);
		messageColumnTableColumn.getColumn().setText("Message");
		messageColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((EyesOn)element).getMessageFormat();
			}
		});
		messageColumnTableColumn.setEditingSupport(new EyesOnEditingSupport(eyesOnTableViewer,6));

		Group watchersGroup;
		watchersGroup = new Group(shell, SWT.NONE);
		fd_keepWatchOnGroup.top = new FormAttachment(watchersGroup, 5, SWT.BOTTOM);
		watchersGroup.setText("Watchers");
		final FormData fd_watchersGroup = new FormData();
		fd_watchersGroup.top = new FormAttachment(0, 35);
		fd_watchersGroup.bottom = new FormAttachment(0, 225);
		fd_watchersGroup.right = new FormAttachment(100, -5);
		fd_watchersGroup.left = new FormAttachment(0, 5);
		watchersGroup.setLayoutData(fd_watchersGroup);
		watchersGroup.setLayout(new FormLayout());

		if (watcherColumnWidths == null)
			watcherColumnWidths = new Integer[]{100,100,100};
		watcherTableViewer = new TableViewer(watchersGroup, SWT.FULL_SELECTION | SWT.BORDER);
		watcherTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					watcher = (Watcher)selection.getFirstElement();
					removeWatcherButton.setEnabled(true);
					addEyesOnButton.setEnabled(true);
					removeEyesOnButton.setEnabled(false);
					Set<EyesOn> seyesOn = watcher.getEyesOn();
					if (seyesOn != null) {
						eyesOnTableViewer.setInput(watcher.getEyesOn());
					} else {
						eyesOnTableViewer.setInput(null);
					}
				} else {
					removeWatcherButton.setEnabled(false);
					addEyesOnButton.setEnabled(false);
					removeEyesOnButton.setEnabled(false);
					eyesOnTableViewer.setInput(null);
				}
			}
		});
		watcherTable = watcherTableViewer.getTable();
		final FormData fd_watcherTable = new FormData();
		fd_watcherTable.bottom = new FormAttachment(100, -34);
		fd_watcherTable.top = new FormAttachment(0, 5);
		fd_watcherTable.right = new FormAttachment(100, -5);
		fd_watcherTable.left = new FormAttachment(0, 5);
		watcherTable.setLayoutData(fd_watcherTable);

		final TableViewerColumn userNameColumnTableColumn = new TableViewerColumn(watcherTableViewer, SWT.NONE);
		userNameColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				Recipient r = ((Watcher)element).getRecipient();
				if (r != null)
					return r.getDescription();
				return "<>";
			}
		});
		userNameColumnTableColumn.getColumn().setWidth(watcherColumnWidths[0]);
		userNameColumnTableColumn.getColumn().setResizable(true);
		userNameColumnTableColumn.getColumn().setText("Recipient");

		final TableViewerColumn assetNameColumnTableColumn = new TableViewerColumn(watcherTableViewer, SWT.NONE);
		assetNameColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((Watcher)element).getAsset().getAssetName();
			}
		});
		assetNameColumnTableColumn.getColumn().setWidth(watcherColumnWidths[1]);
		assetNameColumnTableColumn.getColumn().setResizable(true);
		assetNameColumnTableColumn.getColumn().setText("Asset");

		final TableViewerColumn clientNameColumnTableColumn = new TableViewerColumn(watcherTableViewer, SWT.NONE);
		clientNameColumnTableColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((Watcher)element).getAsset().getClient().getClientName();
			}
		});
		clientNameColumnTableColumn.getColumn().setWidth(watcherColumnWidths[2]);
		clientNameColumnTableColumn.getColumn().setResizable(true);
		clientNameColumnTableColumn.getColumn().setText("Contract");

		watcherTable.setLinesVisible(true);
		watcherTable.setHeaderVisible(true);
		watcherTableViewer.setContentProvider(new WatcherTableContentProvider());
		watcherTableViewer.setSorter(new WatcherTableSorter());

		addAssetWatcherButton = new Button(watchersGroup, SWT.NONE);
		addAssetWatcherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				ConfirmObjectSelection<Asset> confirmAssets = new ConfirmObjectSelection<Asset>(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"AssetSelection");
				List<Asset> selectableAssets = DAO.localDAO().getAssetsAcceptingTextMessages();
				selectableAssets.remove(asset);
				List<Asset> assets = confirmAssets.open(selectableAssets);
				if (assets != null && assets.size() > 0) {
					for (Asset rasset : assets) {
						for (Unit unit : rasset.getUnits()){
							UnitRecipient recipient = DAO.localDAO().getRecipientForUnit(unit);
							if (recipient != null) {
								Watcher watcher = new Watcher();
								watcher.setRecipient(recipient);
								watcher.setAsset(asset);
								lwatchers.add(watcher);
								addedWatchers.add(watcher);
							}
						}
					}
					watcherTableViewer.setInput(lwatchers);
				}
			}
		});
		final FormData fd_addAssetWatcherButton = new FormData();
		fd_addAssetWatcherButton.bottom = new FormAttachment(watcherTable, 29, SWT.BOTTOM);
		fd_addAssetWatcherButton.top = new FormAttachment(watcherTable, 5, SWT.BOTTOM);
		fd_addAssetWatcherButton.right = new FormAttachment(100, -75);
		addAssetWatcherButton.setLayoutData(fd_addAssetWatcherButton);
		addAssetWatcherButton.setText("Add Asset");

		removeWatcherButton = new Button(watchersGroup, SWT.NONE);
		fd_addAssetWatcherButton.left = new FormAttachment(removeWatcherButton, 5, SWT.RIGHT);
		removeWatcherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)watcherTableViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					lwatchers.remove(watcher);
					if (watcher.getId() != 0 && !removedWatchers.contains(watcher))
						removedWatchers.add(watcher);
					modifiedEyesOn.remove(watcher);
					addedWatchers.remove(watcher);
					modifiedWatchers.remove(watcher);
					watcher = null;
					watcherTableViewer.setInput(lwatchers);
					eyesOnTableViewer.setInput(null);
					addEyesOnButton.setEnabled(false);
					removeEyesOnButton.setEnabled(false);
				}
			}
		});
		final FormData fd_removeWatcherButton = new FormData();
		fd_removeWatcherButton.bottom = new FormAttachment(addAssetWatcherButton, 24, SWT.TOP);
		fd_removeWatcherButton.top = new FormAttachment(addAssetWatcherButton, 0, SWT.TOP);
		fd_removeWatcherButton.right = new FormAttachment(100, -151);
		fd_removeWatcherButton.left = new FormAttachment(100, -201);
		removeWatcherButton.setLayoutData(fd_removeWatcherButton);
		removeWatcherButton.setText("Remove");
		removeWatcherButton.setEnabled(false);

		addUserWatcherButton = new Button(watchersGroup, SWT.NONE);
		addUserWatcherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (assetButton.getSelection()) {
					ConfirmObjectSelection<User> dialog = new ConfirmObjectSelection<User>(shell,"RecipientSelection");
					List<User> selected = new ArrayList<User>();
					selected = dialog.open(asset.getClient().getUsers(),null);
					if (selected != null && selected.size() > 0) {
						for (User user : selected) {
							UserRecipient recipient = DAO.localDAO().getRecipientForUser(user);
							if (recipient != null) {
								Watcher watcher = new Watcher();
								watcher.setRecipient(recipient);
								watcher.setAsset(asset);
								lwatchers.add(watcher);
								addedWatchers.add(watcher);
							}
						}
						watcherTableViewer.setInput(lwatchers);
					}
				} else {
					/*
					IStructuredSelection selection = (IStructuredSelection)selectionListViewer.getSelection();
					if (selection != null && selection.getFirstElement() != null) {
						Recipient user = (Recipient)selection.getFirstElement();
						List<AssetWrapper> assetWrappers = DAO.localDAO().getAssetWrappersForUser(user,true);
						for (AssetWrapper assetWrapper : assetWrappers) {
							Watcher watcher = new Watcher();
							watcher.setRecipient(user);
							watcher.setAsset(assetWrapper.getObject());
							lwatchers.add(watcher);
							addedWatchers.add(watcher);
						}
						watcherTableViewer.setInput(lwatchers);
					}
					*/
				}
			}
		});
		final FormData fd_addUserWatcherButton = new FormData();
		fd_addUserWatcherButton.right = new FormAttachment(watcherTable, 0, SWT.RIGHT);
		fd_addUserWatcherButton.bottom = new FormAttachment(addAssetWatcherButton, 0, SWT.BOTTOM);
		fd_addUserWatcherButton.top = new FormAttachment(addAssetWatcherButton, 0, SWT.TOP);
		fd_addUserWatcherButton.left = new FormAttachment(addAssetWatcherButton, 5, SWT.RIGHT);
		addUserWatcherButton.setLayoutData(fd_addUserWatcherButton);
		addUserWatcherButton.setText("Add User");

		Button applyButton;
		applyButton = new Button(shell, SWT.NONE);
		applyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				applyChanges();
			}
		});
		final FormData fd_applyButton = new FormData();
		fd_applyButton.bottom = new FormAttachment(okButton, 0, SWT.BOTTOM);
		fd_applyButton.right = new FormAttachment(okButton, 0, SWT.LEFT);
		fd_applyButton.top = new FormAttachment(cancelButton, 0, SWT.TOP);
		fd_applyButton.left = new FormAttachment(cancelButton, 0, SWT.RIGHT);
		applyButton.setLayoutData(fd_applyButton);
		applyButton.setText("Apply");

		Composite composite;
		composite = new Composite(shell, SWT.NONE);
		fd_selectionList.left = new FormAttachment(composite, 5, SWT.RIGHT);
		fd_selectionList.bottom = new FormAttachment(composite, 21, SWT.TOP);
		fd_selectionList.top = new FormAttachment(composite, 0, SWT.TOP);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		final FormData fd_composite = new FormData();
		fd_composite.right = new FormAttachment(0, 305);
		fd_composite.bottom = new FormAttachment(0, 26);
		fd_composite.top = new FormAttachment(0, 5);
		fd_composite.left = new FormAttachment(0, 5);
		composite.setLayoutData(fd_composite);

		assetButton = new Button(composite, SWT.RADIO);
		assetButton.setText("By Asset");
		assetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (((Button)e.widget).getSelection() && current != TYPE.ASSET) {
					current = TYPE.ASSET;
					typeChanged = true;
					addAssetWatcherButton.setEnabled(true);
					addUserWatcherButton.setEnabled(true);
				}
				setAsset(null);
			}
		});
		final RowData rd_assetButton = new RowData();
		rd_assetButton.width = 63;
		assetButton.setLayoutData(rd_assetButton);

		unitWatcherButton = new Button(composite, SWT.RADIO);
		unitWatcherButton.setText("By Units Watching");
		unitWatcherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (((Button)e.widget).getSelection() && current != TYPE.USERWATCHER) {
					current = TYPE.UNITWATCHER;
					typeChanged = true;
					addAssetWatcherButton.setEnabled(true);
					addUserWatcherButton.setEnabled(false);
				}
				setRecipient(null);
			}
		});
		final RowData rd_unitWatcherButton = new RowData();
		rd_unitWatcherButton.width = 108;
		unitWatcherButton.setLayoutData(rd_unitWatcherButton);

		userWatcherButton = new Button(composite, SWT.RADIO);
		final RowData rd_userWatcherButton = new RowData();
		rd_userWatcherButton.width = 113;
		userWatcherButton.setLayoutData(rd_userWatcherButton);
		userWatcherButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (((Button)e.widget).getSelection() && current != TYPE.USERWATCHER) {
					current = TYPE.USERWATCHER;
					typeChanged = true;
					addAssetWatcherButton.setEnabled(true);
					addUserWatcherButton.setEnabled(false);
				}
				setRecipient(null);
			}
		});
		userWatcherButton.setText("By Users Watching");
		//
	}
	public void setAsset(Asset asset) {
		if (current != TYPE.ASSET)
			return;
		if (typeChanged) {
			typeChanged = false;
			if (selectionListViewer.getContentProvider() != null)
				selectionListViewer.setInput(null);
			selectionListViewer.setSorter(new AssetSorter());
			selectionListViewer.setLabelProvider(new AssetListLabelProvider());
			selectionListViewer.setContentProvider(new AssetListContentProvider());
			selectionListViewer.setInput(DAO.localDAO().getAssetWrappersForUser(TracController.getLoggedIn(),true));
		}
		this.asset = asset;
		IStructuredSelection selection = (IStructuredSelection)selectionListViewer.getSelection();
		if (selection != null && selection.getFirstElement() != null) {
			if (!((AssetWrapper)selection.getFirstElement()).getObject().equals(asset)) {
				selection = new StructuredSelection(new AssetWrapper(asset));
				selectionListViewer.setSelection(selection,true);
			}
		} else if (asset != null) { 
			selection = new StructuredSelection(new AssetWrapper(asset));
			selectionListViewer.setSelection(selection,true);
		}
		lwatchers = new ArrayList<Watcher>();
		if (asset != null) {
			for (Watcher watcher : asset.getWatchers()) {
				lwatchers.add(new Watcher(watcher));
			}
		}
		watcher = null;
		removedWatchers = new ArrayList<Watcher>();
		addedWatchers = new ArrayList<Watcher>();
		modifiedWatchers = new ArrayList<Watcher>();
		modifiedEyesOn = new HashMap<Watcher,List<EyesOn>>();
		watcherTableViewer.setInput(lwatchers);
		eyesOnTableViewer.setInput(null);
	}
	public void setRecipient(Recipient recipient) {
		if (current != TYPE.USERWATCHER && current != TYPE.UNITWATCHER)
			return;
		this.recipient = recipient;
		if (typeChanged) {
			typeChanged = false;
			if (selectionListViewer.getContentProvider() != null)
				selectionListViewer.setInput(null);
			selectionListViewer.setSorter(new RecipientSorter());
			selectionListViewer.setLabelProvider(new RecipientListLabelProvider());
			selectionListViewer.setContentProvider(new RecipientListContentProvider());
			if (current == TYPE.USERWATCHER) {
				User loggedIn = TracController.getLoggedIn();
				List<User> users = null;
				if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN))
					users = DAO.localDAO().getSystemUsers();
				else
					users = DAO.localDAO().getUsersForUserClients(loggedIn);
				List<UserRecipient> userRecipients = new ArrayList<UserRecipient>();
				for (User user : users) {
					UserRecipient ur = DAO.localDAO().getRecipientForUser(user);
					if (ur != null)
						userRecipients.add(ur);
				}
				selectionListViewer.setInput(userRecipients);
			} else {
				List<AssetWrapper> assetWrappers = DAO.localDAO().getAssetWrappersForUser(TracController.getLoggedIn(),true);
				List<UnitRecipient> unitRecipients = new ArrayList<UnitRecipient>();
				for (AssetWrapper assetWrapper : assetWrappers) {
					for (Unit unit : assetWrapper.getObject().getUnits()) {
						UnitRecipient ur = DAO.localDAO().getRecipientForUnit(unit);
						if (ur != null)
							unitRecipients.add(ur);
					}
				}
				selectionListViewer.setInput(unitRecipients);
			}
		}
		IStructuredSelection selection = (IStructuredSelection)selectionListViewer.getSelection();
		if (selection != null && selection.getFirstElement() != null) {
			if (!((Recipient)selection.getFirstElement()).equals(recipient)) {
				selection = new StructuredSelection(recipient);
				selectionListViewer.setSelection(selection,true);
			}
		} else if (recipient != null) { 
			selection = new StructuredSelection(recipient);
			selectionListViewer.setSelection(selection,true);
		}
		if (recipient == null)
			lwatchers = new ArrayList<Watcher>();
		else
			lwatchers = DAO.localDAO().getWatchers(this.recipient);
		watcher = null;
		removedWatchers = new ArrayList<Watcher>();
		addedWatchers = new ArrayList<Watcher>();
		modifiedWatchers = new ArrayList<Watcher>();
		modifiedEyesOn = new HashMap<Watcher,List<EyesOn>>();
		watcherTableViewer.setInput(lwatchers);
		eyesOnTableViewer.setInput(null);
	}
}
