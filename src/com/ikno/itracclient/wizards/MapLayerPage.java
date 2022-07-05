package com.ikno.itracclient.wizards;

import itracclient.Activator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.mapping.layers.GeoAreaLayer;
import com.ikno.dao.utils.Configuration;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.Validate;
import com.ikno.itracclient.dialogs.IconSelectionDialog;
import com.ikno.itracclient.googleearth.GoogleImageAlbum.AlbumEntry;
import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.wizards.EditUserPage.ClientContentProvider;
import com.ikno.itracclient.wizards.EditUserPage.ClientSorter;
import com.ikno.itracclient.wizards.EditUserPage.ClientTableLabelProvider;

public class MapLayerPage extends WizardPage {
	private Button newiconButton;
	private Button labelColorButton;
	private Text webURL;
	private Canvas iconView;
	private Spinner labelScale;
	private Canvas labelColor;
	private Spinner iconScale;
	private Button providesStyleButton;
	private CheckboxTableViewer checkboxTableViewer;
	private Table table;
	private Text maxLOD;
	private Text minLOD;
	private Button sharedButton;
	private Button editableButton;
	private Button visibleButton;
	private Button enabledButton;
	public List<Client> added = new ArrayList<Client>();
	public List<Client> removed = new ArrayList<Client>();
	private boolean webURLModified = false;
	private Image image;
	private AlbumEntry selectedAlbumEntry = null;
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Client client = (Client)element;
			return client.getClientName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<Client>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Client)e1).getClientName().compareTo(((Client)e2).getClientName());
		}
	}
	
	public static final String ID = "com.ikno.itracclient.wizards.MapLayerPage"; //$NON-NLS-1$

	private Text description;
	private Text layerName;
	private User loggedIn = null;
	/**
	 * Create the wizard
	 */
	public MapLayerPage() {
		super(MapLayerPage.ID);
		setTitle("Map Layer Wizard");
		setDescription("Create / Edit a Map Layer");
		this.loggedIn = TracController.getLoggedIn();
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);

		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_nameLabel = new FormData();
		fd_nameLabel.bottom = new FormAttachment(0, 25);
		fd_nameLabel.top = new FormAttachment(0, 5);
		fd_nameLabel.left = new FormAttachment(0, 5);
		nameLabel.setLayoutData(fd_nameLabel);
		nameLabel.setText("Name");

		layerName = new Text(container, SWT.BORDER);
		layerName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (layerName.getText().length() > 0) {
					setErrorMessage(null);
					setPageComplete(true);
				} else {
					setErrorMessage("You must give the layer a valid name");
					setPageComplete(false);
				}
			}
		});
		final FormData fd_layerName = new FormData();
		fd_layerName.bottom = new FormAttachment(nameLabel, 20);
		fd_layerName.top = new FormAttachment(nameLabel, 0, SWT.TOP);
		fd_layerName.right = new FormAttachment(0, 265);
		fd_layerName.left = new FormAttachment(0, 80);
		layerName.setLayoutData(fd_layerName);

		final Label descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.bottom = new FormAttachment(0, 50);
		fd_descriptionLabel.right = new FormAttachment(nameLabel, 0, SWT.RIGHT);
		fd_descriptionLabel.top = new FormAttachment(nameLabel, 5, SWT.BOTTOM);
		fd_descriptionLabel.left = new FormAttachment(nameLabel, 0, SWT.LEFT);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		description = new Text(container, SWT.BORDER);
		final FormData fd_description = new FormData();
		fd_description.bottom = new FormAttachment(nameLabel, 26, SWT.BOTTOM);
		fd_description.top = new FormAttachment(nameLabel, 6);
		fd_description.right = new FormAttachment(0, 490);
		fd_description.left = new FormAttachment(0, 80);
		description.setLayoutData(fd_description);

		enabledButton = new Button(container, SWT.CHECK);
		enabledButton.setText("Enabled");
		final FormData fd_enabledButton = new FormData();
		fd_enabledButton.bottom = new FormAttachment(descriptionLabel, 26, SWT.BOTTOM);
		fd_enabledButton.top = new FormAttachment(descriptionLabel, 6);
		fd_enabledButton.right = new FormAttachment(0, 150);
		fd_enabledButton.left = new FormAttachment(0, 80);
		enabledButton.setLayoutData(fd_enabledButton);

		visibleButton = new Button(container, SWT.CHECK);
		final FormData fd_visibleButton = new FormData();
		fd_visibleButton.bottom = new FormAttachment(descriptionLabel, 26, SWT.BOTTOM);
		fd_visibleButton.top = new FormAttachment(descriptionLabel, 6);
		visibleButton.setLayoutData(fd_visibleButton);
		visibleButton.setText("Visible");

		editableButton = new Button(container, SWT.CHECK);
		editableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (editableButton.getSelection()) {
					minLOD.setEnabled(true);
					maxLOD.setEnabled(true);
					providesStyleButton.setEnabled(true);
					iconScale.setEnabled(true);
					labelScale.setEnabled(true);
					labelColorButton.setEnabled(true);
					newiconButton.setEnabled(true);
					webURL.setEditable(false);
				} else {
					minLOD.setEnabled(false);
					maxLOD.setEnabled(false);
					providesStyleButton.setEnabled(false);
					iconScale.setEnabled(false);
					labelScale.setEnabled(false);
					labelColorButton.setEnabled(false);
					newiconButton.setEnabled(false);
					webURL.setEditable(true);
				}
			}
		});
		final FormData fd_editableButton = new FormData();
		fd_editableButton.bottom = new FormAttachment(descriptionLabel, 26, SWT.BOTTOM);
		fd_editableButton.top = new FormAttachment(descriptionLabel, 6);
		fd_editableButton.right = new FormAttachment(0, 300);
		fd_editableButton.left = new FormAttachment(0, 230);
		editableButton.setLayoutData(fd_editableButton);
		editableButton.setText("Editable");

		sharedButton = new Button(container, SWT.CHECK);
		sharedButton.setText("Shared");
		final FormData fd_sharedButton = new FormData();
		fd_sharedButton.bottom = new FormAttachment(descriptionLabel, 26, SWT.BOTTOM);
		fd_sharedButton.top = new FormAttachment(descriptionLabel, 6);
		fd_sharedButton.right = new FormAttachment(0, 375);
		fd_sharedButton.left = new FormAttachment(0, 305);
		sharedButton.setLayoutData(fd_sharedButton);

		final Label minLodLabel = new Label(container, SWT.NONE);
		minLodLabel.setAlignment(SWT.RIGHT);
		final FormData fd_minLodLabel = new FormData();
		fd_minLodLabel.bottom = new FormAttachment(0, 155);
		fd_minLodLabel.top = new FormAttachment(0, 135);
		fd_minLodLabel.left = new FormAttachment(0, 345);
		minLodLabel.setLayoutData(fd_minLodLabel);
		minLodLabel.setText("Min LOD");

		minLOD = new Text(container, SWT.BORDER);
		minLOD.setText("0");
		final FormData fd_minLOD = new FormData();
		fd_minLOD.top = new FormAttachment(minLodLabel, -20);
		fd_minLOD.bottom = new FormAttachment(minLodLabel, 0, SWT.BOTTOM);
		fd_minLOD.right = new FormAttachment(0, 480);
		fd_minLOD.left = new FormAttachment(0, 430);
		minLOD.setLayoutData(fd_minLOD);
		minLOD.addVerifyListener(new Validate.Numeric());

		final Label maxLodLabel = new Label(container, SWT.NONE);
		maxLodLabel.setAlignment(SWT.RIGHT);
		final FormData fd_maxLodLabel = new FormData();
		fd_maxLodLabel.left = new FormAttachment(minLodLabel, 0, SWT.LEFT);
		fd_maxLodLabel.bottom = new FormAttachment(0, 180);
		fd_maxLodLabel.top = new FormAttachment(0, 160);
		maxLodLabel.setLayoutData(fd_maxLodLabel);
		maxLodLabel.setText("Max LOD");

		maxLOD = new Text(container, SWT.BORDER);
		fd_maxLodLabel.right = new FormAttachment(maxLOD, -6);
		fd_minLodLabel.right = new FormAttachment(maxLOD, -6);
		maxLOD.setText("-1");
		final FormData fd_maxLOD = new FormData();
		fd_maxLOD.top = new FormAttachment(maxLodLabel, -20);
		fd_maxLOD.bottom = new FormAttachment(maxLodLabel, 0, SWT.BOTTOM);
		fd_maxLOD.right = new FormAttachment(0, 480);
		fd_maxLOD.left = new FormAttachment(0, 430);
		maxLOD.setLayoutData(fd_maxLOD);
		maxLOD.addVerifyListener(new Validate.Numeric());

		checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		checkboxTableViewer.setSorter(new Sorter());
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ContentProvider());
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Client client = (Client)event.getElement();
				if (event.getChecked()) {
					added.add(client);
					if (removed.contains(client))
						removed.remove(client);
				} else {
					removed.add(client);
					if (added.contains(client))
						added.remove(client);
				}
			}
		});
		table = checkboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(0, 195);
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.right = new FormAttachment(100, -8);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(145);
		newColumnTableColumn.setText("Contract");

		Label iconScaleLabel;
		iconScaleLabel = new Label(container, SWT.NONE);
		iconScaleLabel.setAlignment(SWT.RIGHT);
		final FormData fd_iconScaleLabel = new FormData();
		fd_iconScaleLabel.left = new FormAttachment(visibleButton);
		fd_iconScaleLabel.bottom = new FormAttachment(0, 155);
		fd_iconScaleLabel.top = new FormAttachment(0, 135);
		iconScaleLabel.setLayoutData(fd_iconScaleLabel);
		iconScaleLabel.setText("Icon Scale");

		iconScale = new Spinner(container, SWT.BORDER);
		iconScale.setMaximum(200);
		iconScale.setSelection(100);
		iconScale.setDigits(2);
		iconScale.setIncrement(5);
		final FormData fd_iconScale = new FormData();
		fd_iconScale.bottom = new FormAttachment(minLodLabel, 20);
		fd_iconScale.top = new FormAttachment(minLodLabel, 0, SWT.TOP);
		fd_iconScale.right = new FormAttachment(editableButton, 56, SWT.RIGHT);
		fd_iconScale.left = new FormAttachment(editableButton, 6);
		iconScale.setLayoutData(fd_iconScale);

		labelColorButton = new Button(container, SWT.NONE);
		labelColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				RGB rgb = rgbFromLabelColor(((MapLayerWizard)getWizard()).mapLayer.getLabelColor());
				ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				dialog.setRGB(rgb);
				rgb = dialog.open();
				if (rgb != null) {
					Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
					labelColor.setBackground(newColor);
				}
			}
		});
		final FormData fd_labelColorButton = new FormData();
		fd_labelColorButton.bottom = new FormAttachment(iconScaleLabel, 26, SWT.BOTTOM);
		fd_labelColorButton.top = new FormAttachment(iconScaleLabel, 6);
		fd_labelColorButton.right = new FormAttachment(0, 160);
		fd_labelColorButton.left = new FormAttachment(0, 95);
		labelColorButton.setLayoutData(fd_labelColorButton);
		labelColorButton.setText("Label Color");

		labelColor = new Canvas(container, SWT.BORDER);
		final FormData fd_labelColor = new FormData();
		fd_labelColor.bottom = new FormAttachment(iconScaleLabel, 26, SWT.BOTTOM);
		fd_labelColor.top = new FormAttachment(iconScaleLabel, 6);
		labelColor.setLayoutData(fd_labelColor);

		Label label;
		label = new Label(container, SWT.NONE);
		fd_labelColor.left = new FormAttachment(label, -41, SWT.LEFT);
		fd_labelColor.right = new FormAttachment(label, -6);
		label.setAlignment(SWT.RIGHT);
		final FormData fd_label = new FormData();
		fd_label.left = new FormAttachment(iconScaleLabel, 0, SWT.LEFT);
		fd_label.bottom = new FormAttachment(0, 180);
		fd_label.top = new FormAttachment(0, 160);
		label.setLayoutData(fd_label);
		label.setText("Label Scale");

		labelScale = new Spinner(container, SWT.BORDER);
		fd_label.right = new FormAttachment(labelScale, -6);
		fd_iconScaleLabel.right = new FormAttachment(labelScale, -6);
		labelScale.setSelection(100);
		labelScale.setIncrement(5);
		labelScale.setMaximum(200);
		labelScale.setDigits(2);
		final FormData fd_labelScale = new FormData();
		fd_labelScale.bottom = new FormAttachment(minLodLabel, 26, SWT.BOTTOM);
		fd_labelScale.top = new FormAttachment(minLodLabel, 6);
		fd_labelScale.right = new FormAttachment(editableButton, 56, SWT.RIGHT);
		fd_labelScale.left = new FormAttachment(editableButton, 6);
		labelScale.setLayoutData(fd_labelScale);

		providesStyleButton = new Button(container, SWT.CHECK);
		fd_table.left = new FormAttachment(providesStyleButton, 0, SWT.LEFT);
		final FormData fd_providesStyleButton = new FormData();
		fd_providesStyleButton.left = new FormAttachment(0, 5);
		fd_providesStyleButton.bottom = new FormAttachment(0, 140);
		fd_providesStyleButton.top = new FormAttachment(0, 120);
		providesStyleButton.setLayoutData(fd_providesStyleButton);
		providesStyleButton.setText("Provides Style");

		iconView = new Canvas(container, SWT.BORDER);
		fd_visibleButton.right = new FormAttachment(iconView, 76, SWT.RIGHT);
		fd_visibleButton.left = new FormAttachment(iconView, 6);
		fd_providesStyleButton.right = new FormAttachment(iconView, -6);
		final FormData fd_iconView = new FormData();
		fd_iconView.bottom = new FormAttachment(providesStyleButton, 37);
		fd_iconView.top = new FormAttachment(providesStyleButton, 0, SWT.TOP);
		fd_iconView.right = new FormAttachment(0, 145);
		fd_iconView.left = new FormAttachment(0, 110);
		iconView.setLayoutData(fd_iconView);
		iconView.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent e) {
				if (image != null) {
					Rectangle bounds = image.getBounds();
					e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, 32, 32);
				} else {
					e.gc.fillRectangle(0, 0, 32, 32);
				}
			}
		});

		newiconButton = new Button(container, SWT.NONE);
		newiconButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IconSelectionDialog iconSelectionDialog = new IconSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				selectedAlbumEntry = (AlbumEntry)iconSelectionDialog.open();
				if (selectedAlbumEntry != null) {
					image = new Image(Display.getCurrent(),selectedAlbumEntry.getImage(),SWT.IMAGE_COPY);
					iconView.redraw();
				}
			}
		});
		final FormData fd_newiconButton = new FormData();
		fd_newiconButton.bottom = new FormAttachment(iconScaleLabel, 20);
		fd_newiconButton.top = new FormAttachment(iconScaleLabel, 0, SWT.TOP);
		fd_newiconButton.right = new FormAttachment(0, 195);
		fd_newiconButton.left = new FormAttachment(0, 150);
		newiconButton.setLayoutData(fd_newiconButton);
		newiconButton.setText("Change");

		final Label kmlAddressLabel = new Label(container, SWT.NONE);
		fd_nameLabel.right = new FormAttachment(kmlAddressLabel, 0, SWT.RIGHT);
		kmlAddressLabel.setAlignment(SWT.RIGHT);
		final FormData fd_kmlAddressLabel = new FormData();
		fd_kmlAddressLabel.left = new FormAttachment(enabledButton, -60, SWT.LEFT);
		fd_kmlAddressLabel.right = new FormAttachment(enabledButton, -5, SWT.LEFT);
		fd_kmlAddressLabel.bottom = new FormAttachment(0, 105);
		fd_kmlAddressLabel.top = new FormAttachment(0, 85);
		kmlAddressLabel.setLayoutData(fd_kmlAddressLabel);
		kmlAddressLabel.setText("Web");

		webURL = new Text(container, SWT.BORDER);
		webURL.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				String text = webURL.getText();
				if (text == null || text.equals("")) {
					editableButton.setSelection(true);
				} else {
					editableButton.setSelection(false);
				}
				webURLModified = true;
			}
		});
		final FormData fd_webURL = new FormData();
		fd_webURL.bottom = new FormAttachment(kmlAddressLabel, 20);
		fd_webURL.top = new FormAttachment(kmlAddressLabel, 0, SWT.TOP);
		fd_webURL.left = new FormAttachment(0, 80);
		fd_webURL.right = new FormAttachment(0, 490);
		webURL.setLayoutData(fd_webURL);

		final Label label_1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		final FormData fd_label_1 = new FormData();
		fd_label_1.bottom = new FormAttachment(iconView, -5, SWT.TOP);
		fd_label_1.right = new FormAttachment(webURL, 0, SWT.RIGHT);
		fd_label_1.top = new FormAttachment(kmlAddressLabel, 5, SWT.BOTTOM);
		fd_label_1.left = new FormAttachment(providesStyleButton, 0, SWT.LEFT);
		label_1.setLayoutData(fd_label_1);

		final Label label_2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		final FormData fd_label_2 = new FormData();
		fd_label_2.top = new FormAttachment(enabledButton);
		fd_label_2.left = new FormAttachment(table, 0, SWT.LEFT);
		fd_label_2.bottom = new FormAttachment(webURL, -5, SWT.TOP);
		fd_label_2.right = new FormAttachment(webURL, 0, SWT.RIGHT);
		label_2.setLayoutData(fd_label_2);

		setPageComplete(false);
		this.buildFromObject();
	}

	public RGB rgbFromLabelColor(String lColor) {
		RGB rgb = null;
		if (lColor != null && lColor.length() == 8) {
			int a,b,g,r;
			a = Integer.parseInt(lColor.substring(0,2),16);
			b = Integer.parseInt(lColor.substring(2,4),16);
			g = Integer.parseInt(lColor.substring(4,6),16);
			r = Integer.parseInt(lColor.substring(6,8),16);
			rgb = new RGB(r,g,b);
		} else {
			rgb = new RGB(255,255,255);
		}
		return rgb;
	}
	public String stringFromLabelColor() {
		RGB rgb = labelColor.getBackground().getRGB();
		return String.format("%02X%02X%02X%02X",(255/2),rgb.blue,rgb.red,rgb.green);
	}
	public void buildFromObject() {
		KMLLayer mapLayer = ((MapLayerWizard)this.getWizard()).mapLayer;
		layerName.setText(mapLayer.getName() == null ? "" : mapLayer.getName());
		description.setText(mapLayer.getDescription() == null ? "" : mapLayer.getDescription());
		enabledButton.setSelection(mapLayer.isEnabled());
		visibleButton.setSelection(mapLayer.isVisible());
		editableButton.setSelection(mapLayer.isEditable());
		sharedButton.setSelection(mapLayer.isShared());
		if (mapLayer.isLinkedToKML()) {
			webURL.setText(mapLayer.getUrl() == null ? "" : mapLayer.getUrl());
			editableButton.setSelection(false);
		} else {
			webURL.setText("");
		}
		webURLModified = false;
		if (mapLayer.isEditable()) {
			minLOD.setEnabled(true);
			maxLOD.setEnabled(true);
			providesStyleButton.setEnabled(true);
			iconScale.setEnabled(true);
			labelScale.setEnabled(true);
			labelColorButton.setEnabled(true);
			newiconButton.setEnabled(true);
			webURL.setEditable(false);
		} else {
			minLOD.setEnabled(false);
			maxLOD.setEnabled(false);
			providesStyleButton.setEnabled(false);
			iconScale.setEnabled(false);
			labelScale.setEnabled(false);
			labelColorButton.setEnabled(false);
			newiconButton.setEnabled(false);
			webURL.setEditable(true);
		}
		minLOD.setText(String.format("%.0f", mapLayer.getMinLodPixels()));
		maxLOD.setText(String.format("%.0f", mapLayer.getMaxLodPixels()));
		if (mapLayer.getStyleId() != null) {
			providesStyleButton.setSelection(true);
			float value = (float)mapLayer.getIconScale()*(float)100;
			iconScale.setSelection((int)value);
			value = (float)mapLayer.getLabelScale()*(float)100;
			labelScale.setSelection((int)value);
			Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgbFromLabelColor(mapLayer.getLabelColor()));
			labelColor.setBackground(newColor);
		} else {
			providesStyleButton.setSelection(false);
			iconScale.setSelection(100);
			labelScale.setSelection(100);
		}
		ImageDescriptor descriptor = null;
		try {
			descriptor = ImageDescriptor.createFromURL(new URL(mapLayer.getIconUrl()));
		} catch (Exception e) {
		}
		if (descriptor != null) {
			if (image != null && !image.isDisposed())
				image.dispose();
			image = descriptor.createImage(Display.getCurrent());
		}
		List<Client> possibles = null;
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			table.setEnabled(true);
			possibles = DAO.localDAO().getClients(Configuration.configCenter().getString("dataScoutId"));
		} else {
			table.setEnabled(true);
			possibles = new ArrayList<Client>(loggedIn.getClients());
		}
		checkboxTableViewer.setInput(possibles);
		List<Client> clients = mapLayer.getClients();
		if (clients != null)
			checkboxTableViewer.setCheckedElements(clients.toArray());
		added = new ArrayList<Client>();
		removed = new ArrayList<Client>();
	}

	public void populateObject() {
		KMLLayer mapLayer = ((MapLayerWizard)this.getWizard()).mapLayer;
		mapLayer.setName(layerName.getText());
		mapLayer.setDescription(description.getText());
		mapLayer.setEnabled(enabledButton.getSelection());
		mapLayer.setVisible(visibleButton.getSelection());
		mapLayer.setEditable(editableButton.getSelection());
		mapLayer.setShared(sharedButton.getSelection());
		mapLayer.setMinLodPixels(Float.parseFloat(minLOD.getText()));
		mapLayer.setMaxLodPixels(Float.parseFloat(maxLOD.getText()));
		mapLayer.setReferenceClass(GeoAreaLayer.class.getName());
		mapLayer.setRootIdentifier(mapLayer.getName().replaceAll(" ", "_"));
		if (webURLModified && webURL.getText() != null && webURL.getText() != "") {
			mapLayer.setUrl(webURL.getText());
			mapLayer.setEditable(false);
			mapLayer.setLinkedToKML(true);
		} else if (mapLayer.isLinkedToKML()) {
			if (webURL.getText() == null || webURL.getText() == "") {
				mapLayer.setLinkedToKML(false);
				mapLayer.setEditable(true);
			}
		}
		if (!mapLayer.isLinkedToKML()) {
			mapLayer.setUrl("http://www.i-see.co.za/tracking/servlet/kmllayer?rootIdentifier="+mapLayer.getRootIdentifier()+"&userId=%USERID%");
		}
		if (providesStyleButton.getSelection()) {
			mapLayer.setIconScale((float)((float)iconScale.getSelection()/(float)100));
			mapLayer.setLabelScale((float)((float)labelScale.getSelection()/(float)100));
			mapLayer.setLabelColor(stringFromLabelColor());
			mapLayer.setStyleId(mapLayer.getRootIdentifier()+"_style");
		} else {
			mapLayer.setStyleId(null);
		}
		if (selectedAlbumEntry != null) {
			mapLayer.setIconUrl(selectedAlbumEntry.imageURL);
		}
	}
}
