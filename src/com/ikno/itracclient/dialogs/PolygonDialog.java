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
import com.ikno.dao.business.PolyLine;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.mapping.widgets.Feature;
import com.ikno.itracclient.mapping.widgets.Position;
import com.ikno.itracclient.googleearth.GoogleImageAlbum.AlbumEntry;
import com.ikno.itracclient.wizards.MapLayerWizard;

public class PolygonDialog extends Dialog {
	private Button labelColorButton;
	private Button newiconButton;
	private Button newLayerButton;
	private Button editButton;
	private Canvas fillCanvas;
	private Canvas lineCanvas;
	private static final Logger logger = Logging.getLogger(PolygonDialog.class.getName());
	private Spinner labelScale;
	private Canvas labelColor;
	private Spinner iconScale;
	private Canvas iconView;
	private Button okButton;
	private Button cancelButton;
	private Button deleteButton;
	private Image image;
	private AlbumEntry selectedAlbumEntry = null;
	public boolean isOpen = false;
	private Feature placemark;
	private WeakReference<GoogleEarth> mappingView = null;
	protected Object result;
	protected Shell shell;
	private Text areaDescr;
	private Text areaName;
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

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public PolygonDialog(Shell parent, int style, GoogleEarth mappingView) {
		super(parent, style);
		this.mappingView = new WeakReference<GoogleEarth>(mappingView);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public PolygonDialog(Shell parent, GoogleEarth mappingView) {
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
			this.setFeature(placemark);
			shell.open();
			shell.layout();
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

	public RGB rgbFromStringColor(String lColor) {
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
	public String stringFromCanvasColor(Canvas canvas) {
		RGB rgb = canvas.getBackground().getRGB();
		return String.format("%02X%02X%02X%02X",(255/2),rgb.blue,rgb.green,rgb.red);
	}
	public void setFeature(Feature placemark) {
		this.placemark = placemark;
		if (placemark == null) {
			areaName.setText("");
			areaDescr.setText("");
			layerCombo.deselectAll();
			iconScale.setSelection(100);
			labelScale.setSelection(100);
			labelColor.setBackground(null);
			okButton.setEnabled(false);
			cancelButton.setEnabled(false);
			deleteButton.setEnabled(false);
			iconView.redraw();
		} else {
			PolygonArea resolved = (PolygonArea)placemark.resolved(mappingView.get().getAvailableFeatures());
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
				if (resolved.getId() == 0) {
					mappingView.get().gi.startPolygon(placemark.id,2,resolved.getOutlineRGBValue());
				}
				areaName.setText((resolved.getAreaName() == null) ? "" : resolved.getAreaName());
				areaDescr.setText((resolved.getDescription() == null) ? "" : resolved.getDescription());
				MapLayer mapLayer = resolved.getMapLayer();
				if (mapLayer != null) {
					layerCombo.select(layerCombo.indexOf(mapLayer.getName()));
				} else {
					layerCombo.deselectAll();
				}
				float value = (float)resolved.getIconScale()*(float)100;
				iconScale.setSelection((int)value);
				value = (float)resolved.getLabelScale()*(float)100;
				labelScale.setSelection((int)value);
				Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgbFromStringColor(resolved.getLabelColor()));
				labelColor.setBackground(newColor);
				newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgbFromStringColor(resolved.getOutlineRGBValue()));
				lineCanvas.setBackground(newColor);
				newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgbFromStringColor(resolved.getFillRGBValue()));
				fillCanvas.setBackground(newColor);
				okButton.setEnabled(true);
				cancelButton.setEnabled(true);
				deleteButton.setEnabled(true);
				iconView.redraw();
			}
		}
	}
	public void cancelEditing() {
		if (placemark != null && placemark.modified == true) {
			PolygonArea polygon = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
			if (polygon != null) {
				if (polygon.getId() == 0) {
					mappingView.get().gi.removePolygon(placemark.id);
				} else {
					try {
						DAO.localDAO().refresh(polygon);
					} catch (Exception exc) {}
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
		shell.setSize(500, 222);
		shell.setText("Define Area");

		final Label layerLabel = new Label(shell, SWT.NONE);
		layerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_layerLabel = new FormData();
		fd_layerLabel.bottom = new FormAttachment(0, 25);
		fd_layerLabel.top = new FormAttachment(0, 5);
		fd_layerLabel.right = new FormAttachment(0, 90);
		fd_layerLabel.left = new FormAttachment(0, 5);
		layerLabel.setLayoutData(fd_layerLabel);
		layerLabel.setText("Layer");

		final ComboViewer layerListViewer = new ComboViewer(shell, SWT.BORDER);
		layerListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				if (placemark != null) {
					placemark.modified = true;
				}
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
				editButton.setEnabled(false);
				newLayerButton.setEnabled(false);
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				deleteButton.setEnabled(false);
				MapLayerWizard wizard = new MapLayerWizard(mapLayer);
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
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
		fd_newLayerButton.right = new FormAttachment(0, 365);
		fd_newLayerButton.left = new FormAttachment(0, 312);
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
		waypointNameLabel.setText("Area Name");

		areaName = new Text(shell, SWT.BORDER);
		areaName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (placemark != null) {
					placemark.modified = true;
				}
			}
		});
		final FormData fd_areaName = new FormData();
		fd_areaName.bottom = new FormAttachment(0, 50);
		fd_areaName.top = new FormAttachment(0, 31);
		fd_areaName.left = new FormAttachment(0, 95);
		areaName.setLayoutData(fd_areaName);

		final Label descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.bottom = new FormAttachment(0, 75);
		fd_descriptionLabel.top = new FormAttachment(0, 55);
		fd_descriptionLabel.right = new FormAttachment(0, 90);
		fd_descriptionLabel.left = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		areaDescr = new Text(shell, SWT.BORDER);
		areaDescr.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (placemark != null) {
					placemark.modified = true;
				}
			}
		});
		fd_areaName.right = new FormAttachment(areaDescr, 0, SWT.RIGHT);
		final FormData fd_areaDescr = new FormData();
		fd_areaDescr.bottom = new FormAttachment(0, 75);
		fd_areaDescr.top = new FormAttachment(0, 55);
		fd_areaDescr.right = new FormAttachment(100, -5);
		fd_areaDescr.left = new FormAttachment(0, 95);
		areaDescr.setLayoutData(fd_areaDescr);

		editButton = new Button(shell, SWT.NONE);
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
		fd_editButton.right = new FormAttachment(0, 310);
		fd_editButton.bottom = new FormAttachment(layerCombo, 0, SWT.BOTTOM);
		fd_editButton.top = new FormAttachment(layerCombo, 0, SWT.TOP);
		fd_editButton.left = new FormAttachment(layerCombo, 5, SWT.RIGHT);
		editButton.setLayoutData(fd_editButton);
		editButton.setText("Edit");

		deleteButton = new Button(shell, SWT.NONE);
		final FormData fd_deleteButton = new FormData();
		fd_deleteButton.bottom = new FormAttachment(100, -4);
		fd_deleteButton.top = new FormAttachment(100, -30);
		fd_deleteButton.right = new FormAttachment(100, -89);
		fd_deleteButton.left = new FormAttachment(100, -134);
		deleteButton.setLayoutData(fd_deleteButton);
		deleteButton.setText("Delete");
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
					"Are you sure you want to delete this area?"))
						return;
					if (placemark != null && placemark.modified == true) {
						PolygonArea polygon = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
						mappingView.get().gi.removePlacemark(placemark.id);
						if (polygon.getId() != 0) {
							try {
								DAO.localDAO().beginTransaction();
								FencedArea rule = DAO.localDAO().fencedAreaRuleForArea(polygon);
								if (rule != null) {
									DAO.localDAO().deleteRule(rule.getId());
								}
								DAO.localDAO().deleteGeoArea(polygon);
								DAO.localDAO().commitTransaction();
							} catch (Exception exc) {
								DAO.localDAO().rollbackTransaction();
							}
						}
					}
					shell.close();
				} finally {
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});

		cancelButton = new Button(shell, SWT.NONE);
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(100, -4);
		fd_cancelButton.top = new FormAttachment(100, -30);
		fd_cancelButton.right = new FormAttachment(100, -47);
		fd_cancelButton.left = new FormAttachment(100, -87);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				cancelEditing();
				shell.close();
			}
		});

		okButton = new Button(shell, SWT.NONE);
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -4);
		fd_okButton.top = new FormAttachment(100, -30);
		fd_okButton.right = new FormAttachment(100, -5);
		fd_okButton.left = new FormAttachment(100, -47);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)layerListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					
				} else {
					try {
						DAO.localDAO().beginTransaction();
						PolygonArea polygon = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
						if (polygon != null) {
							boolean valid = true;
							if (polygon.getId() == 0) {
								List<Position> positions = mappingView.get().gi.fixPolygon(PolygonDialog.this.placemark.id);
								if (positions != null && positions.size() > 1) {
									List<PolyLine> lines = new ArrayList<PolyLine>();
									Position first = positions.get(0);
									Position previous = null;
									for (Position position : positions) {
										if (previous != null) {
											PolyLine line = new PolyLine((float)previous.latitude,(float)previous.longitude,
													(float)position.latitude,(float)position.longitude);
											lines.add(line);
										}
										previous = position;
									}
									// Is it a closed polygon?
									if (previous != null && !previous.equals(first)) {
										PolyLine line = new PolyLine((float)previous.latitude,(float)previous.longitude,
												(float)first.latitude,(float)first.longitude);
										lines.add(line);
									}
									polygon.setPolyLines(lines.toArray(new PolyLine[]{}));
								} else {
									valid = false;
								}
							} else
								DAO.localDAO().refresh(polygon);
							if (valid) {
								polygon.setAreaName(areaName.getText());
								polygon.setDescription(areaDescr.getText());
								if (selectedAlbumEntry != null) {
									polygon.setIconUrl(selectedAlbumEntry.imageURL);
								}
								polygon.setIconScale((float)((float)iconScale.getSelection()/(float)100));
								polygon.setLabelScale((float)((float)labelScale.getSelection()/(float)100));
								polygon.setLabelColor(stringFromCanvasColor(labelColor));
								polygon.setOutlineRGBValue(stringFromCanvasColor(lineCanvas));
								polygon.setFillRGBValue(stringFromCanvasColor(fillCanvas));
								MapLayer currentLayer = polygon.getMapLayer(); 
								if (currentLayer == null || !currentLayer.equals((MapLayer)selection.getFirstElement())) {
									IKMLMappingLayer refLayer = ((MapLayer)selection.getFirstElement()).getReferenceLayer();
									if (refLayer != null)
										refLayer.addGeoArea(polygon);
								}
								DAO.localDAO().saveGeoArea(polygon);
								String imageURL = (selectedAlbumEntry == null) ? null : selectedAlbumEntry.imageURL;
								mappingView.get().gi.updatePolygon(PolygonDialog.this.placemark.id,polygon.getAreaName(),
										polygon.getDescription(),imageURL,polygon.getIconScale(),polygon.getLabelScale(),
										polygon.getLabelColor(),polygon.getOutlineRGBValue(),polygon.getFillRGBValue());								
								placemark.modified = false;
							} else {
								MessageDialog.openError(shell,"Invalid Area","The area you define must have at least 2 lines to form a closed area.");
								mappingView.get().gi.removePolygon(PolygonDialog.this.placemark.id);
							}
						}
						DAO.localDAO().commitTransaction();
						shell.close();
						placemark = null;
					} catch (Exception exc) {
						logger.log(Level.SEVERE,"Error commiting PolygonArea mods:",exc);
						DAO.localDAO().rollbackTransaction();
					}
				}
			}
		});

		iconView = new Canvas(shell, SWT.BORDER);
		final FormData fd_iconView = new FormData();
		fd_iconView.bottom = new FormAttachment(descriptionLabel, 45, SWT.BOTTOM);
		fd_iconView.top = new FormAttachment(descriptionLabel, 5, SWT.BOTTOM);
		fd_iconView.right = new FormAttachment(descriptionLabel, 35, SWT.RIGHT);
		fd_iconView.left = new FormAttachment(descriptionLabel, 0, SWT.RIGHT);
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

		newiconButton = new Button(shell, SWT.NONE);
		final FormData fd_newiconButton = new FormData();
		fd_newiconButton.bottom = new FormAttachment(iconView, 40, SWT.TOP);
		fd_newiconButton.top = new FormAttachment(iconView, 20, SWT.TOP);
		fd_newiconButton.right = new FormAttachment(iconView, 85, SWT.LEFT);
		fd_newiconButton.left = new FormAttachment(iconView, 40, SWT.LEFT);
		newiconButton.setLayoutData(fd_newiconButton);
		newiconButton.setText("Change");
		newiconButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				editButton.setEnabled(false);
				newLayerButton.setEnabled(false);
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

		final Label iconScaleLabel = new Label(shell, SWT.NONE);
		final FormData fd_iconScaleLabel = new FormData();
		fd_iconScaleLabel.bottom = new FormAttachment(iconView, 40, SWT.TOP);
		fd_iconScaleLabel.top = new FormAttachment(iconView, 20, SWT.TOP);
		fd_iconScaleLabel.right = new FormAttachment(iconView, 150, SWT.LEFT);
		fd_iconScaleLabel.left = new FormAttachment(iconView, 95, SWT.LEFT);
		iconScaleLabel.setLayoutData(fd_iconScaleLabel);
		iconScaleLabel.setAlignment(SWT.RIGHT);
		iconScaleLabel.setText("Icon Scale");

		iconScale = new Spinner(shell, SWT.BORDER);
		final FormData fd_iconScale = new FormData();
		fd_iconScale.bottom = new FormAttachment(iconView, 40, SWT.TOP);
		fd_iconScale.top = new FormAttachment(iconView, 20, SWT.TOP);
		fd_iconScale.right = new FormAttachment(iconView, 205, SWT.LEFT);
		fd_iconScale.left = new FormAttachment(iconView, 155, SWT.LEFT);
		iconScale.setLayoutData(fd_iconScale);
		iconScale.setSelection(100);
		iconScale.setMaximum(200);
		iconScale.setIncrement(5);
		iconScale.setDigits(2);

		labelColorButton = new Button(shell, SWT.NONE);
		final FormData fd_labelColorButton = new FormData();
		fd_labelColorButton.bottom = new FormAttachment(iconView, 65, SWT.TOP);
		fd_labelColorButton.top = new FormAttachment(iconView, 45, SWT.TOP);
		fd_labelColorButton.right = new FormAttachment(iconView, 50, SWT.LEFT);
		fd_labelColorButton.left = new FormAttachment(iconView, -15, SWT.LEFT);
		labelColorButton.setLayoutData(fd_labelColorButton);
		labelColorButton.setText("Label Color");
		labelColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (placemark != null) {
					editButton.setEnabled(false);
					newLayerButton.setEnabled(false);
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					deleteButton.setEnabled(false);
					PolygonArea geoPoint = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
					RGB rgb = rgbFromStringColor(geoPoint.getLabelColor());
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
		final FormData fd_labelColor = new FormData();
		fd_labelColor.bottom = new FormAttachment(iconView, 65, SWT.TOP);
		fd_labelColor.top = new FormAttachment(iconView, 45, SWT.TOP);
		fd_labelColor.right = new FormAttachment(iconView, 90, SWT.LEFT);
		fd_labelColor.left = new FormAttachment(iconView, 55, SWT.LEFT);
		labelColor.setLayoutData(fd_labelColor);

		final Label label = new Label(shell, SWT.NONE);
		final FormData fd_label = new FormData();
		fd_label.bottom = new FormAttachment(iconView, 65, SWT.TOP);
		fd_label.top = new FormAttachment(iconView, 45, SWT.TOP);
		fd_label.right = new FormAttachment(iconView, 150, SWT.LEFT);
		fd_label.left = new FormAttachment(iconView, 95, SWT.LEFT);
		label.setLayoutData(fd_label);
		label.setAlignment(SWT.RIGHT);
		label.setText("Label Scale");

		labelScale = new Spinner(shell, SWT.BORDER);
		final FormData fd_labelScale = new FormData();
		fd_labelScale.bottom = new FormAttachment(iconView, 65, SWT.TOP);
		fd_labelScale.top = new FormAttachment(iconView, 45, SWT.TOP);
		fd_labelScale.right = new FormAttachment(iconView, 205, SWT.LEFT);
		fd_labelScale.left = new FormAttachment(iconView, 155, SWT.LEFT);
		labelScale.setLayoutData(fd_labelScale);
		labelScale.setSelection(100);
		labelScale.setMaximum(200);
		labelScale.setIncrement(5);
		labelScale.setDigits(2);

		final Button lineColorButton = new Button(shell, SWT.NONE);
		lineColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (placemark != null) {
					PolygonArea geoPoint = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
					editButton.setEnabled(false);
					newLayerButton.setEnabled(false);
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					deleteButton.setEnabled(false);
					RGB rgb = rgbFromStringColor(geoPoint.getOutlineRGBValue());
					ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					dialog.setRGB(rgb);
					rgb = dialog.open();
					if (rgb != null) {
						Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
						lineCanvas.setBackground(newColor);
					}
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});
		final FormData fd_lineColorButton = new FormData();
		fd_lineColorButton.right = new FormAttachment(deleteButton, 0, SWT.LEFT);
		fd_lineColorButton.bottom = new FormAttachment(iconScale, 0, SWT.BOTTOM);
		fd_lineColorButton.top = new FormAttachment(iconScale, 0, SWT.TOP);
		fd_lineColorButton.left = new FormAttachment(iconScale, 5, SWT.RIGHT);
		lineColorButton.setLayoutData(fd_lineColorButton);
		lineColorButton.setText("Line Color");

		final Button fillColorButton = new Button(shell, SWT.NONE);
		fillColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (placemark != null) {
					PolygonArea geoPoint = (PolygonArea)PolygonDialog.this.placemark.resolved(mappingView.get().getAvailableFeatures());
					editButton.setEnabled(false);
					newLayerButton.setEnabled(false);
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					deleteButton.setEnabled(false);
					RGB rgb = rgbFromStringColor(geoPoint.getFillRGBValue());
					ColorDialog dialog = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					dialog.setRGB(rgb);
					rgb = dialog.open();
					if (rgb != null) {
						Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
						fillCanvas.setBackground(newColor);
					}
					editButton.setEnabled(true);
					newLayerButton.setEnabled(true);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});
		final FormData fd_fillColorButton = new FormData();
		fd_fillColorButton.right = new FormAttachment(deleteButton, 0, SWT.LEFT);
		fd_fillColorButton.bottom = new FormAttachment(labelScale, 0, SWT.BOTTOM);
		fd_fillColorButton.top = new FormAttachment(lineColorButton, 5, SWT.BOTTOM);
		fd_fillColorButton.left = new FormAttachment(labelScale, 5, SWT.RIGHT);
		fillColorButton.setLayoutData(fd_fillColorButton);
		fillColorButton.setText("Fill Color");

		lineCanvas = new Canvas(shell, SWT.BORDER);
		final FormData fd_lineCanvas = new FormData();
		fd_lineCanvas.bottom = new FormAttachment(lineColorButton, 0, SWT.BOTTOM);
		fd_lineCanvas.right = new FormAttachment(cancelButton, -5, SWT.LEFT);
		fd_lineCanvas.top = new FormAttachment(lineColorButton, 0, SWT.TOP);
		fd_lineCanvas.left = new FormAttachment(lineColorButton, 5, SWT.RIGHT);
		lineCanvas.setLayoutData(fd_lineCanvas);

		fillCanvas = new Canvas(shell, SWT.BORDER);
		final FormData fd_fillCanvas = new FormData();
		fd_fillCanvas.right = new FormAttachment(cancelButton, -5, SWT.LEFT);
		fd_fillCanvas.bottom = new FormAttachment(fillColorButton, 0, SWT.BOTTOM);
		fd_fillCanvas.top = new FormAttachment(lineCanvas, 5, SWT.BOTTOM);
		fd_fillCanvas.left = new FormAttachment(fillColorButton, 5, SWT.RIGHT);
		fillCanvas.setLayoutData(fd_fillCanvas);
	}

}
