package com.ikno.itracclient.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import itracclient.Activator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Animal;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.Management;
import com.ikno.dao.business.Owner;
import com.ikno.dao.business.Person;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.MTTextMessage;
import com.ikno.dao.business.UnitRecipient;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserRecipient;
import com.ikno.dao.business.Vehicle;
import com.ikno.dao.business.Watcher;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.geocode.DefaultCoding;
import com.ikno.dao.geocode.IGeoCoder;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IPropertyComposite;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.dialogs.AssetRuleDialog;
import com.ikno.itracclient.dialogs.ConfirmObjectSelection;
import com.ikno.itracclient.dialogs.WatcherDialog;
import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.DoubleClickEvent;

public class AssetPropertyPage extends PropertyPage {
	private TableViewer unitTableViewer;
	class UnitTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Unit unit = (Unit)element;
			switch (columnIndex) {
			case 0:
				return unit.getUnitType().toString();
			case 1:
				return unit.getUnitName();
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class UnitContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Table table;
	private Button mercuryInstalled;
	private Spinner interval;
	private Button rulesButton;
	private Button ownedByButton;
	private Combo timezone;
	private Combo codingCombo;
	private ComboViewer clientListViewer;
	private Combo clientList;
	private static final Logger logger = Logging.getLogger(AssetPropertyPage.class.getName());
	private boolean finished = false;
	private Button panicSwitchFitted;
	private Canvas pinColor;
	private Canvas trackColor;
	private Button trackColorButton;
	private Button pinColorButton;
	private Text assetType;
	private Composite assetDetail;
	private Text assetName;
	private StackLayout detailLayout;
	private Composite powner = null;
	private List<String> codingTypes;
	private List<User> addedOwners = null;
	private List<User> removedOwners = null;
	private boolean ownersChanged = false;
	List<String> tzIDList = Arrays.asList(TimeZone.getAvailableIDs());
	private Button clearEvents;
	private PropertyDialogAction propertyDialogAction = null;
	
	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Client)e1).getClientName().compareTo(((Client)e2).getClientName());
		}
	}
	class ClientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Client)element).getClientName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ClientListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			User selected = (User)inputElement;
			return selected.getClients().toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	@Override
	public void createControl(Composite parent) {
		this.powner = parent;
		this.noDefaultAndApplyButton();
		super.createControl(parent);
	}

	/**
	 * Create the property page
	 */
	public AssetPropertyPage() {
		super();
	}

	/**
	 * Create contents of the property page
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());

		final Group assetDetailGroup = new Group(container, SWT.NONE);
		assetDetailGroup.setText("General Detail");
		assetDetailGroup.setLayout(null);
		final FormData fd_assetDetailGroup = new FormData();
		fd_assetDetailGroup.right = new FormAttachment(100, -5);
		fd_assetDetailGroup.top = new FormAttachment(0, 5);
		assetDetailGroup.setLayoutData(fd_assetDetailGroup);

		assetName = new Text(assetDetailGroup, SWT.BORDER);
		assetName.setBounds(93, 20, 365, 23);
		assetName.setEditable(true);

		Label assetNameLabel;
		assetNameLabel = new Label(assetDetailGroup, SWT.RIGHT);
		assetNameLabel.setBounds(10, 20, 77, 19);
		assetNameLabel.setAlignment(SWT.RIGHT);
		assetNameLabel.setText("Asset Name");

		final Label clientLabel = new Label(assetDetailGroup, SWT.NONE);
		clientLabel.setBounds(10, 49, 77, 20);
		clientLabel.setAlignment(SWT.RIGHT);
		clientLabel.setText("Contract");

		assetDetail = new Composite(container, SWT.NONE);
		fd_assetDetailGroup.bottom = new FormAttachment(0, 170);
		fd_assetDetailGroup.left = new FormAttachment(assetDetail, 0, SWT.LEFT);
		detailLayout = new StackLayout();
		assetDetail.setLayout(detailLayout);
		final FormData fd_detail = new FormData();
		fd_detail.top = new FormAttachment(assetDetailGroup);
		fd_detail.bottom = new FormAttachment(100, -94);
		fd_detail.left = new FormAttachment(assetDetailGroup, 0, SWT.LEFT);
		fd_detail.right = new FormAttachment(100, -5);
		assetDetail.setLayoutData(fd_detail);
		detailLayout.topControl = null;

		final Label typeLabel = new Label(assetDetailGroup, SWT.NONE);
		typeLabel.setBounds(10, 75, 77, 20);
		typeLabel.setAlignment(SWT.RIGHT);
		typeLabel.setText("Type");

		assetType = new Text(assetDetailGroup, SWT.BORDER);
		assetType.setBounds(93, 75, 100, 20);
		assetType.setEditable(false);

		pinColorButton = new Button(assetDetailGroup, SWT.NONE);
		pinColorButton.setBounds(219, 101, 25, 23);
		pinColorButton.setText("Pin");
		pinColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				Asset asset = AssetPropertyPage.this.getAsset();
				String currRGB = asset.getPinRGBValue();
				if (currRGB == null) {
					Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
					currRGB = preferenceStore.getDefaultString(PreferenceConstants.PIN_COLOR);
				}
				ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				String[] tokens = currRGB.split(",");
				RGB rgb = new RGB(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
				dialog.setRGB(rgb);
				rgb = dialog.open();
				if (rgb != null) {
					asset.setPinRGBValue(String.format("%d,%d,%d", rgb.red,rgb.green,rgb.blue));
					Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
					pinColor.setBackground(newColor);
				}
			}
		});

		trackColorButton = new Button(assetDetailGroup, SWT.NONE);
		trackColorButton.setBounds(282, 101, 31, 23);
		trackColorButton.setText("Track");
		trackColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				Asset asset = AssetPropertyPage.this.getAsset();
				String currRGB = asset.getTrackRGBValue();
				if (currRGB == null) {
					Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
					currRGB = preferenceStore.getDefaultString(PreferenceConstants.TRACK_COLOR);
				}
				ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				String[] tokens = currRGB.split(",");
				RGB rgb = new RGB(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
				dialog.setRGB(rgb);
				rgb = dialog.open();
				if (rgb != null) {
					asset.setTrackRGBValue(String.format("%d,%d,%d", rgb.red,rgb.green,rgb.blue));
					Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
					trackColor.setBackground(newColor);
				}
			}
		});

		pinColor = new Canvas(assetDetailGroup, SWT.NONE);
		pinColor.setBounds(250, 101, 24, 23);

		trackColor = new Canvas(assetDetailGroup, SWT.NONE);
		trackColor.setBounds(318, 101, 25, 23);

		panicSwitchFitted = new Button(assetDetailGroup, SWT.CHECK);
		panicSwitchFitted.setBounds(198, 131, 85, 23);
		panicSwitchFitted.setText("Panic Fitted");

		rulesButton = new Button(assetDetailGroup, SWT.NONE);
		rulesButton.setBounds(318, 47, 49, 23);
		rulesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				AssetRuleDialog dialog = new AssetRuleDialog(AssetPropertyPage.this.powner.getShell());
				dialog.open(AssetPropertyPage.this.getAsset());
			}
		});
		rulesButton.setText("Rules");

		clientListViewer = new ComboViewer(assetDetailGroup, SWT.BORDER);
		clientListViewer.setContentProvider(new ClientListContentProvider());
		clientListViewer.setInput(TracController.getLoggedIn());
		clientListViewer.setLabelProvider(new ClientListLabelProvider());
		clientListViewer.setSorter(new ClientSorter());
		clientList = clientListViewer.getCombo();
		clientList.setBounds(93, 47, 155, 23);
		clientList.setVisibleItemCount(10);

		final Label locationCodingLabel = new Label(assetDetailGroup, SWT.NONE);
		locationCodingLabel.setBounds(10, 101, 77, 20);
		locationCodingLabel.setAlignment(SWT.RIGHT);
		locationCodingLabel.setText("Geo Coding");

		codingCombo = new Combo(assetDetailGroup, SWT.NONE);
		codingCombo.setBounds(93, 101, 120, 23);

		final Label label = new Label(assetDetailGroup, SWT.NONE);
		label.setBounds(199, 75, 60, 20);
		label.setText("Time Zone");

		timezone = new Combo(assetDetailGroup, SWT.NONE);
		timezone.setBounds(264, 75, 199, 23);
		timezone.setVisibleItemCount(15);
		timezone.setItems(tzIDList.toArray(new String[]{}));

		ownedByButton = new Button(assetDetailGroup, SWT.NONE);
		ownedByButton.setBounds(254, 47, 60, 23);
		ownedByButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				List<User>owners = AssetPropertyPage.this.getAsset().getOwners();
				ConfirmObjectSelection<	User> dialog = new ConfirmObjectSelection<User>(AssetPropertyPage.this.getShell(),"UserSelection");
				ownersChanged = (dialog.open(DAO.localDAO().getSystemUsers(),owners) != null);
				if (ownersChanged) {
					addedOwners = dialog.getAdded();
					removedOwners = dialog.getRemoved();
				}
			}
		});
		ownedByButton.setText("Owned By");

		final Label intervalLabel = new Label(assetDetailGroup, SWT.NONE);
		intervalLabel.setBounds(10, 131, 77, 20);
		intervalLabel.setAlignment(SWT.RIGHT);
		intervalLabel.setText("Interval");

		interval = new Spinner(assetDetailGroup, SWT.BORDER);
		interval.setBounds(93, 131, 75, 23);
		interval.setSelection(3);
		interval.setMinimum(1);
		interval.setMaximum(3600);

		mercuryInstalled = new Button(assetDetailGroup, SWT.CHECK);
		mercuryInstalled.setBounds(288, 131, 110, 23);
		mercuryInstalled.setText("Mercury installed");
		
		clearEvents = new Button(assetDetailGroup, SWT.NONE);
		clearEvents.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					DAO.localDAO().beginTransaction();
					int count = DAO.localDAO().clearSimpleStatusesForAsset(AssetPropertyPage.this.getAsset());
					logger.fine("Removed "+count+" SimpleStatus records");
					DAO.localDAO().commitTransaction();
				} catch (Exception exc) {
					DAO.localDAO().rollbackTransaction();
				}
			}
		});
		clearEvents.setBounds(373, 47, 75, 23);
		clearEvents.setText("Clear Events");

		unitTableViewer = new TableViewer(container, SWT.BORDER);
		unitTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent arg0) {
				IStructuredSelection selection = (IStructuredSelection)unitTableViewer.getSelection();
				Object[] selected = selection.toArray();
				if (selection != null && selection.size() > 0) {
					propertyDialogAction.selectionChanged(selection);
					propertyDialogAction.run();
				}
			}
		});
		unitTableViewer.setLabelProvider(new UnitTableLabelProvider());
		unitTableViewer.setContentProvider(new UnitContentProvider());
		table = unitTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.top = new FormAttachment(assetDetail, 0, SWT.BOTTOM);
		fd_table.left = new FormAttachment(assetDetail, 0, SWT.LEFT);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn unitTypeColumnTableColumn = new TableColumn(table, SWT.NONE);
		unitTypeColumnTableColumn.setWidth(150);
		unitTypeColumnTableColumn.setText("Unit");

		final TableColumn unitNameColumnTableColumn = new TableColumn(table, SWT.NONE);
		unitNameColumnTableColumn.setWidth(250);
		unitNameColumnTableColumn.setText("Unit Name");
		
		buildFromObject();
		// $hide<<$
		propertyDialogAction = new PropertyDialogAction(this.getShell(),unitTableViewer);
		// $hide>>$
		return container;
	}
	public Asset getAsset() {
		AssetWrapper wrapper = (AssetWrapper)this.getElement().getAdapter(AssetWrapper.class);
		return (Asset)PersistantObject.resolveProxy(wrapper.getObject());
	}
	public void buildFromObject() {
		Asset asset = this.getAsset();
		assetName.setText(asset.getAssetName() == null ? "" : asset.getAssetName());
		if (asset != null && clientList.indexOf(asset.getClient().getClientName()) != -1)
			clientList.select(clientList.indexOf(asset.getClient().getClientName()));
		else
			clientList.select(0);
		assetType.setText(asset.getAssetType().toString());
		try {
			if (PersistantObject.instanceOf(asset,Vehicle.class)) {
				detailLayout.topControl = new VehicleProperties(assetDetail,SWT.None, this);
			} else if (PersistantObject.instanceOf(asset,Aircraft.class)) {
				detailLayout.topControl = new AircraftProperties(assetDetail,SWT.None,this);
			} else if (PersistantObject.instanceOf(asset,Person.class)) {
				detailLayout.topControl = new PersonProperties(assetDetail,SWT.None, this);
			} else if (PersistantObject.instanceOf(asset,Animal.class)) {
				detailLayout.topControl = new AnimalProperties(assetDetail,SWT.None, this);
			}
			assetDetail.layout();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		pinColor.setBackground(TracController.pinColor(asset));
		trackColor.setBackground(TracController.trackColor(asset));
		panicSwitchFitted.setSelection(asset.isPanicEnabled());
		
		String[] defaultNames = DefaultCoding.getAvailableCodingNames();
		List<KMLLayer> kmlLayers = DAO.localDAO().getKMLLayersForClient(asset.getClient());
		codingTypes = new ArrayList<String>();
		for (String codingName : defaultNames) {
			codingTypes.add(codingName);
		}
		if (kmlLayers != null) {
			for (KMLLayer layer : kmlLayers) {
				if (!layer.isLinkedToKML())
					codingTypes.add(layer.getName());
			}
		}
		String[] sorted = codingTypes.toArray(new String[]{});
		Arrays.sort(sorted);
		codingCombo.setItems(sorted);
		codingCombo.select(Arrays.binarySearch(sorted, asset.getCodingType()));
		String setting = asset.getTimeZoneID();
		if (setting != null) {
			timezone.select(tzIDList.indexOf(setting));
		}
		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			rulesButton.setEnabled(true);
			clearEvents.setEnabled(true);
		} else {
			rulesButton.setEnabled(false);
			clearEvents.setEnabled(false);
		}
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) ||
			loggedIn.fullfillsRole(User.Roles.CLIENTADMIN))
			clientList.setEnabled(true);
		else
			clientList.setEnabled(false);
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) ||
			loggedIn.fullfillsRole(User.Roles.ASSETADMIN)) {
			ownedByButton.setEnabled(true);
			codingCombo.setEnabled(true);
			timezone.setEnabled(true);
			panicSwitchFitted.setEnabled(true);
			detailLayout.topControl.setEnabled(true);
			interval.setEnabled(true);
			mercuryInstalled.setEnabled(true);
		} else {
			ownedByButton.setEnabled(false);
			codingCombo.setEnabled(false);
			timezone.setEnabled(false);
			panicSwitchFitted.setEnabled(false);
			detailLayout.topControl.setEnabled(false);
			interval.setEnabled(false);
			mercuryInstalled.setEnabled(false);
		}
		int intValue = 1;
		boolean textSupported = false;
		for (Unit unit : asset.getUnits()) {
			if (unit.getReportingInterval() > intValue)
				intValue = unit.getReportingInterval();
			if (unit.isTextMessagesAccepted())
				textSupported = true;
		}
		interval.setSelection(intValue);
		mercuryInstalled.setSelection(textSupported);
		unitTableViewer.setInput(asset.getUnits().toArray());
	}
	public boolean performOk() {
		if (finished == true)
			return true;
		finished = false;
		boolean result = false;
		try {
			DAO.localDAO().beginTransaction();
			Asset asset = this.getAsset();
			String text = assetName.getText();
			if (text != null)
				asset.setAssetName(text);
			asset.setPanicEnabled(panicSwitchFitted.getSelection());
			asset.setCodingType(codingCombo.getItem(codingCombo.getSelectionIndex()));
			int selected = timezone.getSelectionIndex();
			if (selected != -1)
				asset.setTimeZoneID(timezone.getItem(selected));
			StructuredSelection clientSel = (StructuredSelection)clientListViewer.getSelection();
			if (clientSel != null) {
				Client client = (Client)clientSel.getFirstElement();
				if (!asset.getClient().equals(client)) {
					Client current = asset.getClient();
					current.removeAsset(asset);
					client.addAsset(asset);
					DAO.localDAO().saveOrUpdate(current);
					DAO.localDAO().saveOrUpdate(client);
				}
				asset.setManagement(client.getManagement());
			}
			if (ownersChanged) {
				for (User owner : addedOwners)
					((Owner)owner).addOwnedAsset(asset);
				for (User owner : removedOwners)
					((Owner)owner).removeOwnedAsset(asset);
				ownersChanged = false;
			}
			result = ((IPropertyComposite)detailLayout.topControl).performOk();
			if (result) {
				DAO.localDAO().saveOrUpdate(asset);
				int intValue = interval.getSelection();
				int timeout = intValue;
				User loggedIn = TracController.getLoggedIn();
				String message = "%P"+timeout+"\r\n";
				for (Unit unit : asset.getUnits()) {
					if (unit.getReportingInterval() != intValue) {
						UserRecipient recipient = DAO.localDAO().getRecipientForUser(loggedIn);
						try {
							MTTextMessage textMessage = DAO.localDAO().loadSBD2MTMessage(unit, recipient, message, MTTextMessage.Origin.ITRAC, MTTextMessage.MessageType.COMMAND);
							unit.setReportingInterval(intValue);
							DAO.localDAO().saveOrUpdate(unit);
						} catch (Throwable e) {
							logger.severe("Error loading SBD2 MTMessage: "+e);
						}
					}
					unit.setTextMessagesAccepted(mercuryInstalled.getSelection());
					UnitRecipient recipient = DAO.localDAO().getRecipientForUnit(unit);
					if (recipient == null) {
						recipient = new UnitRecipient(unit);
						DAO.localDAO().save(recipient);
					}
					DAO.localDAO().saveOrUpdate(unit);
				}
				DAO.localDAO().commitTransaction();
			}
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
		}
		return result;
	}
}
