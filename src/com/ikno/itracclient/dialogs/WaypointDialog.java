package com.ikno.itracclient.dialogs;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.IKMLMappingLayer;
import com.ikno.dao.business.FencedArea;
import com.ikno.dao.business.GeoPoint;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.Validate;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.mapping.widgets.Feature;
import com.ikno.itracclient.googleearth.GoogleImageAlbum.AlbumEntry;
import com.ikno.itracclient.wizards.AddAssetWizard;
import com.ikno.itracclient.wizards.MapLayerWizard;

public class WaypointDialog extends Dialog {
	private Button newLayerButton;
	private Button editButton;
	private Spinner labelScale;
	private Canvas labelColor;
	private Spinner iconScale;
	private Button layersStyleButton;
	private static final Logger logger = Logging.getLogger(WaypointDialog.class.getName());

	private Canvas iconView;
	private Button okButton;
	private Button cancelButton;
	private Button deleteButton;
	private Text longitude;
	private Text latitude;
	private Text waypointDescr;
	private Text waypointName;
	private Feature placemark;
	private Image image;
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((MapLayer)e1).getName().compareTo(((MapLayer)e2).getName());
		}
	}
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((MapLayer)element).getName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<MapLayer>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Combo layerCombo;
	protected Object result;
	protected Shell shell;
	public boolean isOpen = false;
	private AlbumEntry selectedAlbumEntry = null;
	private WeakReference<GoogleEarth> mappingView = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public WaypointDialog(Shell parent, int style, GoogleEarth mappingView) {
		super(parent, style);
		this.mappingView = new WeakReference<GoogleEarth>(mappingView);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public WaypointDialog(Shell parent, GoogleEarth mappingView) {
		this(parent, SWT.NONE, mappingView);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(Feature placemark) {
		try {
			isOpen = true;
			createContents();
			shell.open();
			shell.layout();
			setPlacemark(placemark);
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				boolean value = false;
				try {
					value = display.readAndDispatch();
				} catch (Exception e) {
					System.out.println("Exception during readAndDispatch processing: ");
					e.printStackTrace();
					logger.log(Level.SEVERE, "Exception during readAndDispatch processing:",e);
				}
				if (!value)
					display.sleep();
			}
			shell.dispose();
			if (image != null) {
				image.dispose();
				image = null;
			}
		} catch (Exception e) {
			System.out.println("Exception during open processing: ");
			e.printStackTrace();
		} finally {
			if (this.placemark != null)
				cancelEditing();
			isOpen = false;
		}
		return result;
	}
	public void updatePosition(double latitude,double longitude) {
		if (placemark != null) {
			placemark.position.latitude = latitude;
			String text = String.format("%.3f", latitude);
			this.latitude.setText(text);
			placemark.position.longitude = longitude;
			text = String.format("%.3f", longitude);
			this.longitude.setText(text);
		}
	}
	public void setPlacemark(Feature placemark) {
		this.placemark = placemark;
		if (placemark == null) {
			waypointName.setText("");
			waypointDescr.setText("");
			latitude.setText("");
			longitude.setText("");
			layersStyleButton.setSelection(false);
			iconScale.setSelection(100);
			labelScale.setSelection(100);
			labelColor.setBackground(null);
			layerCombo.deselectAll();
			okButton.setEnabled(false);
			cancelButton.setEnabled(false);
			deleteButton.setEnabled(false);
			iconView.redraw();
		} else {
			GeoPoint resolved = (GeoPoint)placemark.resolved(mappingView.get().getAvailableFeatures());
			if (resolved != null) {
				ImageDescriptor descriptor = null;
				try {
					descriptor = ImageDescriptor.createFromURL(new URL(resolved.getIconUrl()));
				} catch (Exception e) {
				}
				if (descriptor != null) {
					if (image != null && !image.isDisposed())
						image.dispose();
					image = descriptor.createImage(shell.getDisplay());
				}
				waypointName.setText((resolved.getAreaName() == null) ? "" : resolved.getAreaName());
				waypointDescr.setText((resolved.getDescription() == null) ? "" : resolved.getDescription());
				this.latitude.setText(String.format("%.3f", resolved.getLatitude()));
				this.longitude.setText(String.format("%.3f", resolved.getLongitude()));
				MapLayer mapLayer = resolved.getMapLayer();
				if (mapLayer != null) {
					layerCombo.select(layerCombo.indexOf(mapLayer.getName()));
				} else {
					layerCombo.deselectAll();
				}
				layersStyleButton.setSelection(resolved.isStyledByLayer());
				float value = (float)resolved.getIconScale()*(float)100;
				iconScale.setSelection((int)value);
				value = (float)resolved.getLabelScale()*(float)100;
				labelScale.setSelection((int)value);
				Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgbFromLabelColor(resolved.getLabelColor()));
				labelColor.setBackground(newColor);
				okButton.setEnabled(true);
				cancelButton.setEnabled(true);
				deleteButton.setEnabled(true);
				iconView.redraw();
			}
		}
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
		} else if (lColor != null && lColor.length() == 6) {
			int a,b,g,r;
			r = Integer.parseInt(lColor.substring(0,2),16);
			g = Integer.parseInt(lColor.substring(2,4),16);
			b = Integer.parseInt(lColor.substring(4,6),16);
			rgb = new RGB(r,g,b);
		} else {
			rgb = new RGB(255,255,255);
		}
		return rgb;
	}
	public String stringFromLabelColor(Canvas canvas) {
		RGB rgb = canvas.getBackground().getRGB();
		return String.format("%02X%02X%02X%02X",(255/2),rgb.blue,rgb.green,rgb.red);
	}
	public void cancelEditing() {
		if (placemark != null && placemark.modified == true) {
			GeoPoint geoPoint = (GeoPoint)WaypointDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
			if (geoPoint != null) {
				if (geoPoint.getId() == 0) {
					mappingView.get().gi.removePlacemark(placemark.id);
				} else {
					try {
						DAO.localDAO().beginTransaction();
						DAO.localDAO().refresh(geoPoint);
						mappingView.get().gi.revertWaypoint(placemark.id, geoPoint.getLatitude(), geoPoint.getLongitude());
						DAO.localDAO().commitTransaction();
					} catch (Exception exc) {
						DAO.localDAO().rollbackTransaction();
					}
				}
			}
			placemark.modified = false;
			placemark = null;
		}
	}
	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.TITLE);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 251);
		shell.setText("Waypoint");

		final Label layerLabel = new Label(shell, SWT.NONE);
		layerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_layerLabel = new FormData();
		fd_layerLabel.bottom = new FormAttachment(0, 25);
		fd_layerLabel.top = new FormAttachment(0, 5);
		fd_layerLabel.right = new FormAttachment(0, 90);
		fd_layerLabel.left = new FormAttachment(0, 5);
		layerLabel.setLayoutData(fd_layerLabel);
		layerLabel.setText("Layer");

		final ComboViewer layerListViewer = new ComboViewer(shell, SWT.READ_ONLY);
		layerListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				if (placemark != null) {
					placemark.modified = true;
				}
				if (event.getSelection() != null)
					editButton.setEnabled(true);
				else
					editButton.setEnabled(false);
			}
		});
		layerListViewer.setSorter(new Sorter());
		layerListViewer.setLabelProvider(new ListLabelProvider());
		layerListViewer.setContentProvider(new ContentProvider());
		List<MapLayer> allLayers = mappingView.get().layerList();
		List<MapLayer> editable = new ArrayList<MapLayer>();
		for (MapLayer layer : allLayers) {
			if (layer.isEditable())
				editable.add(layer);
		}
		layerListViewer.setInput(editable);
		layerCombo = layerListViewer.getCombo();
		final FormData fd_layerCombo = new FormData();
		fd_layerCombo.bottom = new FormAttachment(0, 26);
		fd_layerCombo.top = new FormAttachment(0, 5);
		fd_layerCombo.right = new FormAttachment(0, 250);
		fd_layerCombo.left = new FormAttachment(0, 95);
		layerCombo.setLayoutData(fd_layerCombo);

		newLayerButton = new Button(shell, SWT.NONE);
		newLayerButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				KMLLayer mapLayer = new KMLLayer();
				MapLayerWizard wizard = new MapLayerWizard(mapLayer);
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
				editButton.setEnabled(false);
				newLayerButton.setEnabled(false);
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				deleteButton.setEnabled(false);
				dialog.open();
				if (dialog.getReturnCode() == Window.OK) {
					mappingView.get().addLayer(mapLayer);
					List<MapLayer> allLayers = mappingView.get().layerList();
					List<MapLayer> editable = new ArrayList<MapLayer>();
					for (MapLayer layer : allLayers) {
						if (layer.isEditable())
							editable.add(layer);
					}
					layerListViewer.setInput(editable);
				}
				editButton.setEnabled(true);
				newLayerButton.setEnabled(true);
				okButton.setEnabled(true);
				cancelButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
		});
		final FormData fd_newLayerButton = new FormData();
		newLayerButton.setLayoutData(fd_newLayerButton);
		newLayerButton.setText("New");

		final Label waypointNameLabel = new Label(shell, SWT.NONE);
		waypointNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_waypointNameLabel = new FormData();
		fd_waypointNameLabel.bottom = new FormAttachment(0, 50);
		fd_waypointNameLabel.top = new FormAttachment(0, 30);
		fd_waypointNameLabel.right = new FormAttachment(0, 90);
		fd_waypointNameLabel.left = new FormAttachment(0, 5);
		waypointNameLabel.setLayoutData(fd_waypointNameLabel);
		waypointNameLabel.setText("Waypoint Name");

		waypointName = new Text(shell, SWT.BORDER);
		waypointName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (placemark != null) {
					placemark.modified = true;
				}
			}
		});
		final FormData fd_waypointName = new FormData();
		fd_waypointName.bottom = new FormAttachment(0, 50);
		fd_waypointName.top = new FormAttachment(0, 31);
		fd_waypointName.left = new FormAttachment(0, 95);
		waypointName.setLayoutData(fd_waypointName);

		final Label descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.bottom = new FormAttachment(0, 75);
		fd_descriptionLabel.top = new FormAttachment(0, 55);
		fd_descriptionLabel.right = new FormAttachment(0, 90);
		fd_descriptionLabel.left = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		waypointDescr = new Text(shell, SWT.BORDER);
		waypointDescr.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (placemark != null) {
					placemark.modified = true;
				}
			}
		});
		fd_waypointName.right = new FormAttachment(waypointDescr, 0, SWT.RIGHT);
		final FormData fd_waypointDescr = new FormData();
		fd_waypointDescr.bottom = new FormAttachment(0, 75);
		fd_waypointDescr.top = new FormAttachment(0, 55);
		fd_waypointDescr.right = new FormAttachment(100, -5);
		fd_waypointDescr.left = new FormAttachment(0, 95);
		waypointDescr.setLayoutData(fd_waypointDescr);

		iconView = new Canvas(shell, SWT.BORDER);
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
		final FormData fd_iconView = new FormData();
		fd_iconView.bottom = new FormAttachment(0, 145);
		fd_iconView.top = new FormAttachment(0, 105);
		fd_iconView.right = new FormAttachment(0, 130);
		fd_iconView.left = new FormAttachment(0, 95);
		fd_iconView.height = 32;
		fd_iconView.width = 32;
		iconView.setLayoutData(fd_iconView);

		Button newiconButton;
		newiconButton = new Button(shell, SWT.NONE);
		newiconButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				newLayerButton.setEnabled(false);
				editButton.setEnabled(false);
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				deleteButton.setEnabled(false);
				IconSelectionDialog iconSelectionDialog = new IconSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				selectedAlbumEntry = (AlbumEntry)iconSelectionDialog.open();
				if (selectedAlbumEntry != null) {
					if (placemark != null) {
						placemark.modified = true;
						if (image != null && !image.isDisposed())
							image.dispose();
						image = new Image(Display.getCurrent(),selectedAlbumEntry.getImage(),SWT.IMAGE_COPY);
						iconView.redraw();
					}
				}
				editButton.setEnabled(true);
				newLayerButton.setEnabled(true);
				okButton.setEnabled(true);
				cancelButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
		});
		final FormData fd_newiconButton = new FormData();
		fd_newiconButton.bottom = new FormAttachment(0, 145);
		fd_newiconButton.top = new FormAttachment(0, 125);
		fd_newiconButton.right = new FormAttachment(0, 180);
		fd_newiconButton.left = new FormAttachment(0, 135);
		newiconButton.setLayoutData(fd_newiconButton);
		newiconButton.setText("Change");

		okButton = new Button(shell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)layerListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					
				} else {
					try {
						DAO.localDAO().beginTransaction();
						GeoPoint geoPoint = (GeoPoint)WaypointDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
						if (geoPoint != null) {
							if (geoPoint.getId() != 0)
								DAO.localDAO().refresh(geoPoint);
							if (selectedAlbumEntry != null) {
								geoPoint.setIconUrl(selectedAlbumEntry.imageURL);
							}
							double latitude = Double.parseDouble(WaypointDialog.this.latitude.getText());
							double longitude = Double.parseDouble(WaypointDialog.this.longitude.getText());
							mappingView.get().gi.updatePlacemark(WaypointDialog.this.placemark.id, 
									waypointName.getText(), waypointDescr.getText(),
									geoPoint.getIconUrl(),geoPoint.getIconScale(),
									latitude,longitude,geoPoint.getAltitude());
							geoPoint.setAreaName(waypointName.getText());
							geoPoint.setDescription(waypointDescr.getText());
							geoPoint.setLatitude((float)latitude);
							geoPoint.setLongitude((float)longitude);
							geoPoint.setStyledByLayer(layersStyleButton.getSelection());
							geoPoint.setIconScale((float)((float)iconScale.getSelection()/(float)100));
							geoPoint.setLabelScale((float)((float)labelScale.getSelection()/(float)100));
							geoPoint.setLabelColor(stringFromLabelColor(labelColor));
							MapLayer currentLayer = geoPoint.getMapLayer(); 
							if (currentLayer == null || !currentLayer.equals((MapLayer)selection.getFirstElement())) {
								IKMLMappingLayer refLayer = ((MapLayer)selection.getFirstElement()).getReferenceLayer();
								if (refLayer != null)
									refLayer.addGeoArea(geoPoint);
							}
							DAO.localDAO().saveGeoArea(geoPoint);
							placemark.modified = false;
						}
						DAO.localDAO().commitTransaction();
						shell.close();
						placemark = null;
					} catch (Exception exc) {
						DAO.localDAO().rollbackTransaction();
					}
				}
			}
		});
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -5);
		fd_okButton.top = new FormAttachment(100, -31);
		fd_okButton.right = new FormAttachment(100, -5);
		fd_okButton.left = new FormAttachment(100, -47);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				cancelEditing();
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(100, -5);
		fd_cancelButton.top = new FormAttachment(100, -31);
		fd_cancelButton.right = new FormAttachment(100, -47);
		fd_cancelButton.left = new FormAttachment(100, -87);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		deleteButton = new Button(shell, SWT.NONE);
		fd_newLayerButton.left = new FormAttachment(deleteButton, -50, SWT.LEFT);
		fd_newLayerButton.right = new FormAttachment(deleteButton, 0, SWT.LEFT);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				editButton.setEnabled(false);
				newLayerButton.setEnabled(false);
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				deleteButton.setEnabled(false);
				try {
					if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							"Confirm delete", 
					"Are you sure you want to delete this waypoint?"))
						return;
					if (placemark != null && placemark.modified == true) {
						GeoPoint geoPoint = (GeoPoint)WaypointDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
						mappingView.get().gi.removePlacemark(placemark.id);
						if (geoPoint.getId() != 0) {
							try {
								DAO.localDAO().beginTransaction();
								FencedArea rule = DAO.localDAO().fencedAreaRuleForArea(geoPoint);
								if (rule != null) {
									DAO.localDAO().deleteRule(rule.getId());
								}
								DAO.localDAO().deleteGeoArea(geoPoint);
								DAO.localDAO().commitTransaction();
							} catch (Exception exc) {
								DAO.localDAO().rollbackTransaction();
							}
						}
					}
				} finally {
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
				shell.close();
			}
		});
		final FormData fd_deleteButton = new FormData();
		fd_deleteButton.bottom = new FormAttachment(cancelButton, 26, SWT.TOP);
		fd_deleteButton.top = new FormAttachment(cancelButton, 0, SWT.TOP);
		fd_deleteButton.right = new FormAttachment(100, -87);
		fd_deleteButton.left = new FormAttachment(100, -132);
		deleteButton.setLayoutData(fd_deleteButton);
		deleteButton.setText("Delete");

		Label latLabel;
		latLabel = new Label(shell, SWT.NONE);
		latLabel.setAlignment(SWT.RIGHT);
		final FormData fd_latLabel = new FormData();
		fd_latLabel.top = new FormAttachment(descriptionLabel, 5, SWT.BOTTOM);
		latLabel.setLayoutData(fd_latLabel);
		latLabel.setText("Lat");

		latitude = new Text(shell, SWT.BORDER);
		fd_latLabel.bottom = new FormAttachment(latitude, 0, SWT.BOTTOM);
		fd_latLabel.left = new FormAttachment(latitude, -60, SWT.LEFT);
		fd_latLabel.right = new FormAttachment(latitude, -5, SWT.LEFT);
		final FormData fd_latitude = new FormData();
		fd_latitude.right = new FormAttachment(0, 170);
		fd_latitude.bottom = new FormAttachment(0, 100);
		fd_latitude.top = new FormAttachment(0, 80);
		fd_latitude.left = new FormAttachment(0, 95);
		latitude.setLayoutData(fd_latitude);
		latitude.addVerifyListener(new Validate.Floating());

		final Label lonLabel = new Label(shell, SWT.NONE);
		lonLabel.setAlignment(SWT.RIGHT);
		final FormData fd_lonLabel = new FormData();
		fd_lonLabel.right = new FormAttachment(0, 205);
		fd_lonLabel.bottom = new FormAttachment(latitude, 0, SWT.BOTTOM);
		fd_lonLabel.top = new FormAttachment(latitude, 0, SWT.TOP);
		fd_lonLabel.left = new FormAttachment(latitude, 5, SWT.RIGHT);
		lonLabel.setLayoutData(fd_lonLabel);
		lonLabel.setText("Lon");

		longitude = new Text(shell, SWT.BORDER);
		final FormData fd_longitude = new FormData();
		fd_longitude.bottom = new FormAttachment(lonLabel, 0, SWT.BOTTOM);
		fd_longitude.right = new FormAttachment(0, 290);
		fd_longitude.top = new FormAttachment(lonLabel, 0, SWT.TOP);
		fd_longitude.left = new FormAttachment(lonLabel, 5, SWT.RIGHT);
		longitude.setLayoutData(fd_longitude);
		longitude.addVerifyListener(new Validate.Floating());

		editButton = new Button(shell, SWT.NONE);
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IStructuredSelection selection = (IStructuredSelection)layerListViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					KMLLayer mapLayer = (KMLLayer)selection.getFirstElement();
					editButton.setEnabled(false);
					newLayerButton.setEnabled(false);
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					deleteButton.setEnabled(false);
					MapLayerWizard wizard = new MapLayerWizard(mapLayer);
					wizard.init(workbench, null);
					WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
					dialog.open();
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});
		fd_newLayerButton.top = new FormAttachment(editButton, -21, SWT.BOTTOM);
		fd_newLayerButton.bottom = new FormAttachment(editButton, 0, SWT.BOTTOM);
		final FormData fd_editButton = new FormData();
		fd_editButton.bottom = new FormAttachment(layerCombo, 21, SWT.TOP);
		fd_editButton.top = new FormAttachment(layerCombo, 0, SWT.TOP);
		fd_editButton.right = new FormAttachment(layerCombo, 60, SWT.RIGHT);
		fd_editButton.left = new FormAttachment(layerCombo, 5, SWT.RIGHT);
		editButton.setLayoutData(fd_editButton);
		editButton.setText("Edit");

		layersStyleButton = new Button(shell, SWT.CHECK);
		final FormData fd_layersStyleButton = new FormData();
		fd_layersStyleButton.bottom = new FormAttachment(0, 155);
		fd_layersStyleButton.top = new FormAttachment(0, 135);
		fd_layersStyleButton.right = new FormAttachment(0, 430);
		fd_layersStyleButton.left = new FormAttachment(0, 325);
		layersStyleButton.setLayoutData(fd_layersStyleButton);
		layersStyleButton.setText("Use layer's style");

		final Label iconScaleLabel = new Label(shell, SWT.NONE);
		final FormData fd_iconScaleLabel = new FormData();
		iconScaleLabel.setLayoutData(fd_iconScaleLabel);
		iconScaleLabel.setAlignment(SWT.RIGHT);
		iconScaleLabel.setText("Icon Scale");

		iconScale = new Spinner(shell, SWT.BORDER);
		final FormData fd_iconScale = new FormData();
		fd_iconScale.right = new FormAttachment(layerCombo, 50, SWT.RIGHT);
		fd_iconScale.left = new FormAttachment(layerCombo, 0, SWT.RIGHT);
		iconScale.setLayoutData(fd_iconScale);
		iconScale.setSelection(100);
		iconScale.setMaximum(200);
		iconScale.setIncrement(5);
		iconScale.setDigits(2);

		final Button labelColorButton = new Button(shell, SWT.NONE);
		final FormData fd_labelColorButton = new FormData();
		fd_labelColorButton.top = new FormAttachment(0, 150);
		fd_labelColorButton.bottom = new FormAttachment(0, 170);
		fd_labelColorButton.right = new FormAttachment(0, 145);
		fd_labelColorButton.left = new FormAttachment(0, 80);
		labelColorButton.setLayoutData(fd_labelColorButton);
		labelColorButton.setText("Label Color");
		labelColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (placemark != null) {
					GeoPoint geoPoint = (GeoPoint)WaypointDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
					RGB rgb = rgbFromLabelColor(geoPoint.getLabelColor());
					editButton.setEnabled(false);
					newLayerButton.setEnabled(false);
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					deleteButton.setEnabled(false);
					ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					dialog.setRGB(rgb);
					rgb = dialog.open();
					if (rgb != null) {
						Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
						labelColor.setBackground(newColor);
					}
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});

		labelColor = new Canvas(shell, SWT.BORDER);
		fd_iconScale.top = new FormAttachment(labelColor, -25, SWT.TOP);
		fd_iconScale.bottom = new FormAttachment(labelColor, -5, SWT.TOP);
		fd_iconScaleLabel.top = new FormAttachment(labelColor, -25, SWT.TOP);
		fd_iconScaleLabel.bottom = new FormAttachment(labelColor, -5, SWT.TOP);
		fd_iconScaleLabel.right = new FormAttachment(labelColor, 60, SWT.RIGHT);
		fd_iconScaleLabel.left = new FormAttachment(labelColor, 5, SWT.RIGHT);
		final FormData fd_labelColor = new FormData();
		fd_labelColor.top = new FormAttachment(0, 150);
		fd_labelColor.bottom = new FormAttachment(0, 170);
		fd_labelColor.right = new FormAttachment(0, 185);
		fd_labelColor.left = new FormAttachment(0, 150);
		labelColor.setLayoutData(fd_labelColor);

		final Label label = new Label(shell, SWT.NONE);
		final FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(labelColor, -20, SWT.BOTTOM);
		fd_label.bottom = new FormAttachment(labelColor, 0, SWT.BOTTOM);
		fd_label.right = new FormAttachment(labelColor, 60, SWT.RIGHT);
		fd_label.left = new FormAttachment(labelColor, 5, SWT.RIGHT);
		label.setLayoutData(fd_label);
		label.setAlignment(SWT.RIGHT);
		label.setText("Label Scale");

		labelScale = new Spinner(shell, SWT.BORDER);
		final FormData fd_labelScale = new FormData();
		fd_labelScale.top = new FormAttachment(labelColor, -20, SWT.BOTTOM);
		fd_labelScale.bottom = new FormAttachment(labelColor, 0, SWT.BOTTOM);
		fd_labelScale.right = new FormAttachment(layerCombo, 50, SWT.RIGHT);
		fd_labelScale.left = new FormAttachment(layerCombo, 0, SWT.RIGHT);
		labelScale.setLayoutData(fd_labelScale);
		labelScale.setSelection(100);
		labelScale.setMaximum(200);
		labelScale.setIncrement(5);
		labelScale.setDigits(2);
		//
	}
}
