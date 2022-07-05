package com.ikno.itracclient.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Animal;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetSession;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.JavaRule;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.Management;
import com.ikno.dao.business.Person;
import com.ikno.dao.business.Rule;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Vehicle;
import com.ikno.dao.business.rules.JavaRuleImpl;
import com.ikno.dao.business.rules.JavaRuleSession;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Configuration;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.views.widgets.AircraftDetail;
import com.ikno.itracclient.views.widgets.AnimalDetail;
import com.ikno.itracclient.views.widgets.PersonDetail;
import com.ikno.itracclient.views.widgets.VehicleDetail;
import com.ikno.dao.geocode.DefaultCoding;
import com.ikno.dao.geocode.IGeoCoder;

public class AddAssetPage extends WizardPage implements IChangeListener {
	private Combo codingCombo;
	public static final String ID = "com.ikno.itracclient.wizards.EditAssetPage"; //$NON-NLS-1$

	private Combo clientNames;
	private Composite assetDetail;
	private StackLayout assetLayout = new StackLayout();
	private Combo assetType;
	private Text assetName;
	private IObjectEditor editor = null;
	private List<String> codingTypes;
	
	private Asset asset = null;
	private Client client = null;
	/**
	 * Create the wizard
	 */
	public AddAssetPage(Asset asset) {
		super(ID);
		setTitle("Edit Asset");
		setDescription("Edit an Asset");
		this.asset = asset;
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		
		final Group generalDetailGroup = new Group(container, SWT.NONE);
		generalDetailGroup.setText("General Detail");
		final FormData fd_generalDetailGroup = new FormData();
		fd_generalDetailGroup.right = new FormAttachment(100, -5);
		fd_generalDetailGroup.top = new FormAttachment(0, 5);
		fd_generalDetailGroup.left = new FormAttachment(0, 5);
		generalDetailGroup.setLayoutData(fd_generalDetailGroup);
		generalDetailGroup.setLayout(new FormLayout());

		final Label nameLabel = new Label(generalDetailGroup, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_nameLabel = new FormData();
		fd_nameLabel.bottom = new FormAttachment(0, 25);
		fd_nameLabel.top = new FormAttachment(0, 5);
		fd_nameLabel.right = new FormAttachment(0, 65);
		fd_nameLabel.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(fd_nameLabel);
		nameLabel.setText("Name");

		assetName = new Text(generalDetailGroup, SWT.BORDER);
		assetName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				AddAssetPage.this.objectChanged(asset);
			}
		});
		final FormData fd_assetName = new FormData();
		fd_assetName.bottom = new FormAttachment(0, 25);
		fd_assetName.top = new FormAttachment(0, 5);
		fd_assetName.right = new FormAttachment(0, 360);
		fd_assetName.left = new FormAttachment(0, 70);
		assetName.setLayoutData(fd_assetName);

		Label typeLabel;
		typeLabel = new Label(generalDetailGroup, SWT.NONE);
		typeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_typeLabel = new FormData();
		fd_typeLabel.bottom = new FormAttachment(0, 50);
		fd_typeLabel.top = new FormAttachment(0, 30);
		fd_typeLabel.right = new FormAttachment(0, 65);
		fd_typeLabel.left = new FormAttachment(0, 0);
		typeLabel.setLayoutData(fd_typeLabel);
		typeLabel.setText("Type");

		assetType = new Combo(generalDetailGroup, SWT.READ_ONLY);
		assetType.select(0);
		assetType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				AddAssetPage.this.typeSelected();
			}
		});
		assetType.setItems(Asset.getAssetTypes());
		final FormData fd_assetType = new FormData();
		fd_assetType.bottom = new FormAttachment(0, 51);
		fd_assetType.top = new FormAttachment(0, 30);
		fd_assetType.right = new FormAttachment(0, 180);
		fd_assetType.left = new FormAttachment(0, 70);
		assetType.setLayoutData(fd_assetType);

		assetDetail = new Composite(container, SWT.NONE);
		fd_generalDetailGroup.bottom = new FormAttachment(assetDetail, 0, SWT.TOP);

		final Label clientLabel = new Label(generalDetailGroup, SWT.NONE);
		clientLabel.setAlignment(SWT.RIGHT);
		final FormData fd_clientLabel = new FormData();
		fd_clientLabel.bottom = new FormAttachment(0, 100);
		fd_clientLabel.top = new FormAttachment(0, 82);
		fd_clientLabel.right = new FormAttachment(0, 65);
		fd_clientLabel.left = new FormAttachment(0, 10);
		clientLabel.setLayoutData(fd_clientLabel);
		clientLabel.setText("Contract");

		clientNames = new Combo(generalDetailGroup, SWT.READ_ONLY);
		clientNames.setVisibleItemCount(10);
		clientNames.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				int index = clientNames.getSelectionIndex();
				if (index >= 0) {
					String name = clientNames.getItem(index);
					client = DAO.localDAO().getClientByName(name);
				}
			}
		});
		clientNames.select(0);
		final FormData fd_client = new FormData();
		fd_client.right = new FormAttachment(assetName, 0, SWT.RIGHT);
		fd_client.bottom = new FormAttachment(0, 101);
		fd_client.top = new FormAttachment(0, 80);
		fd_client.left = new FormAttachment(0, 70);
		clientNames.setLayoutData(fd_client);
		List<String> cNames = null;
		if (TracController.getLoggedIn().fullfillsRole(User.Roles.SYSTEMADMIN)) {
			cNames = new ArrayList<String>();
			List<Client> allClients = DAO.localDAO().getClients(Configuration.configCenter().getString("dataScoutId"));
			for (Iterator<Client> ci = allClients.iterator();ci.hasNext();) {
				Client client = ci.next();
				cNames.add(client.getClientName());
			}
		} else {
			cNames = TracController.getLoggedIn().getClientNames();
		}
		Collections.sort(cNames);
		String[] names = new String[cNames.size()];
		int i = 0;
		for (Iterator<String> ni = cNames.iterator();ni.hasNext();) {
			names[i++] = ni.next();
		}
		clientNames.setItems(names);
		if (client != null) {
			clientNames.select(clientNames.indexOf(client.getClientName()));
			clientNames.setEnabled(false);
		}
		else
			clientNames.select(0);

		Label locationCodingLabel;
		locationCodingLabel = new Label(generalDetailGroup, SWT.NONE);
		locationCodingLabel.setAlignment(SWT.RIGHT);
		final FormData fd_locationCodingLabel = new FormData();
		fd_locationCodingLabel.bottom = new FormAttachment(0, 77);
		fd_locationCodingLabel.top = new FormAttachment(0, 55);
		fd_locationCodingLabel.right = new FormAttachment(0, 65);
		fd_locationCodingLabel.left = new FormAttachment(0, 0);
		locationCodingLabel.setLayoutData(fd_locationCodingLabel);
		locationCodingLabel.setText("Geo Coding");

		codingCombo = new Combo(generalDetailGroup, SWT.NONE);
		final FormData fd_codingCombo = new FormData();
		fd_codingCombo.right = new FormAttachment(0, 235);
		fd_codingCombo.bottom = new FormAttachment(0, 77);
		fd_codingCombo.top = new FormAttachment(0, 56);
		fd_codingCombo.left = new FormAttachment(0, 70);
		codingCombo.setLayoutData(fd_codingCombo);

		final FormData fd_assetDetail = new FormData();
		fd_assetDetail.top = new FormAttachment(0, 130);
		fd_assetDetail.right = new FormAttachment(generalDetailGroup, 0, SWT.RIGHT);
		fd_assetDetail.left = new FormAttachment(generalDetailGroup, 0, SWT.LEFT);
		fd_assetDetail.bottom = new FormAttachment(0, 244);
		assetDetail.setLayoutData(fd_assetDetail);
		assetLayout = new StackLayout();
		assetDetail.setLayout(assetLayout);
		//
		setControl(container);
		buildFromObject();
	}
	public void buildFromObject() {
		assetName.setText(asset.getAssetName() == null ? "" : asset.getAssetName());
		assetType.select(asset.getAssetType().ordinal());
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
		this.typeSelected();
	}
	public String validate() {
		if (assetName.getText() == null | assetName.getText().equals(""))
			return "You must specify a valid name for the asset";
		return editor.validate();
	}
	public void populateObject() {
		asset.setAssetName(assetName.getText());
		asset.setCodingType(codingCombo.getItem(codingCombo.getSelectionIndex()));
		editor.populateObject();
	}
	public void typeSelected() {
		if (Asset.AssetTypes.values()[assetType.getSelectionIndex()] == Asset.AssetTypes.Aircraft) {
			if (!PersistantObject.instanceOf(asset,Aircraft.class)) {
				try {
					asset = new Aircraft(this.asset);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to Aircraft, quit and try again.");
				}
			}
			editor = new AircraftDetail(assetDetail,SWT.None);
		} else if (Asset.AssetTypes.values()[assetType.getSelectionIndex()] == Asset.AssetTypes.Animal) {
			if (!PersistantObject.instanceOf(asset,Animal.class)) {
				try {
					asset = new Animal(this.asset);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to Animal, quit and try again.");
				}
			}
			editor = new AnimalDetail(assetDetail,SWT.None);
		} else if (Asset.AssetTypes.values()[assetType.getSelectionIndex()] == Asset.AssetTypes.Person) {
			if (!PersistantObject.instanceOf(asset,Person.class)) {
				try {
					asset = new Person(this.asset);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to Person, quit and try again.");
				}
			}
			editor = new PersonDetail(assetDetail,SWT.None);
		} else if (Asset.AssetTypes.values()[assetType.getSelectionIndex()] == Asset.AssetTypes.Vehicle) {
			if (!PersistantObject.instanceOf(asset,Vehicle.class)) {
				try {
					asset = new Vehicle(this.asset);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to Vehicle, quit and try again.");
				}
			}
			editor = new VehicleDetail(assetDetail,SWT.None);
		}
		editor.setChangeListener(this);
		assetLayout.topControl = (Composite)editor;
		assetDetail.layout();
		editor.setObject(asset);
		objectChanged(asset);
	}
	public void objectChanged(Object object) {
		String message = this.validate();
		if (message == null) {
			setErrorMessage(null);
			this.populateObject();
			setPageComplete(true);
		} else {
			setErrorMessage(message);
			setPageComplete(false);
		}
	}
	public Asset getAsset() {
		return asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}
