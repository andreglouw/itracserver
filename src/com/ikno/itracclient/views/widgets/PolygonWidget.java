package com.ikno.itracclient.views.widgets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polyline;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.GeoArea;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.PolyLine;
import com.ikno.dao.business.PolygonArea;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.views.AssetView;
import com.ikno.itracclient.views.widgets.LineControlWidget;
import com.ikno.itracclient.worldwind.LineBuilder;
import com.swtdesigner.SWTResourceManager;

public class PolygonWidget extends Dialog implements IChangeListener {
	class LayerSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Object item1 = e1;
			Object item2 = e2;
			return 0;
		}
	}
	class LayerLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((MapLayer)element).getName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class LayerContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<MapLayer>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private ComboViewer layerListViewer;
	private Combo layerCombo;
	private Button addPointButton;
	private Button cancelButton;
	private ComboViewer containerListViewer;
	public boolean isOpen = false;
	public boolean isCreating = true;
	private Combo combo;
	private static final Logger logger = Logging.getLogger(PolygonWidget.class.getName());

	public enum AreaType {
		NONE,
		POLYGON,
		POINT
	}
	private Text message;
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((GeoArea)e1).compareTo((GeoArea)e2);
		}
	}
	private CheckboxTableViewer areaTableViewer;
	private Button deleteButton;
	private Button addAreaButton;
	private Button okButton;
	private LineBuilderPanel linePanel = null;
	private PolygonArea polygonArea = null;
	private AreaType areaType = AreaType.NONE;
	private Text description;
	private Text areaName;
	boolean selectionChanging = false;
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			GeoArea area = (GeoArea)element;
			switch (columnIndex) {
			case 0:
				return area.getAreaName();
			case 1:
				return area.getDescription();
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (GeoArea[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ContainerSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((GeoArea)e1).getAreaName().compareTo(((GeoArea)e2).getAreaName());
		}
	}
	class ContainerListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((GeoArea)element).getAreaName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ContainerListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (GeoArea[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private Table table;
	protected Object result;
	protected Shell shell;
	private WeakReference<WorldWindow> worldWindow = null;
	private WeakReference<IMappingView> mappingView = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public PolygonWidget(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public PolygonWidget(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(final WorldWindow wwd, final IMappingView mappingView) {
		try {
			isOpen = true;
			isCreating = true;
			createContents();
			shell.open();
			shell.layout();
			this.worldWindow = new WeakReference<WorldWindow>(wwd);
			this.mappingView = new WeakReference<IMappingView>(mappingView);
			Object[] checked = this.mappingView.get().getGeoAreas();
			if (checked == null)
				checked = new Object[]{};
			this.areaTableViewer.setCheckedElements(checked);
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} finally {
			isOpen = false;
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 444);
		shell.setText("Areas");

		areaTableViewer = CheckboxTableViewer.newCheckList(shell, SWT.BORDER | SWT.FULL_SELECTION);
		areaTableViewer.setSorter(new Sorter());
		areaTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection == null || selection.getFirstElement() == null)
					return;
				try {
					selectionChanging = true;
					GeoArea area = (GeoArea)selection.getFirstElement();
					if (area.getContainer() == null)
						containerListViewer.setSelection(new StructuredSelection());
					else
						containerListViewer.setSelection(new StructuredSelection(area.getContainer()));
					String value = area.getAreaName();
					areaName.setText((value == null) ? "" : value);
					value = area.getDescription();
					description.setText((value == null) ? "" : value);
				} finally {
					selectionChanging = false;
				}
			}
		});
		areaTableViewer.setLabelProvider(new TableLabelProvider());
		areaTableViewer.setContentProvider(new ContentProvider());
		List<GeoArea> areas = DAO.localDAO().getAreasForUser(TracController.getLoggedIn());
		if (areas == null)
			areas = new ArrayList<GeoArea>();
		areaTableViewer.setInput(areas.toArray(new GeoArea[]{}));
		areaTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				final boolean isChecked = event.getChecked();
				final GeoArea area = (GeoArea)event.getElement();
				try {
					if (isChecked) {
						mappingView.get().showGeoArea(area);
					} else {
						mappingView.get().removeGeoArea(area);
					}
				} catch (Exception e) {
					System.out.println("Caught...");
				}
			}
		});
		table = areaTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(0, 220);
		fd_table.top = new FormAttachment(0, 5);
		fd_table.right = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0, 5);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn areaNameTableColumn = new TableColumn(table, SWT.NONE);
		areaNameTableColumn.setWidth(100);
		areaNameTableColumn.setText("Area Name");

		final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		descriptionTableColumn.setWidth(378);
		descriptionTableColumn.setText("Description");

		containerListViewer = new ComboViewer(shell, SWT.BORDER);
		containerListViewer.setContentProvider(new ContainerListContentProvider());
		containerListViewer.setLabelProvider(new ContainerListLabelProvider());
		containerListViewer.setSorter(new ContainerSorter());
		combo = containerListViewer.getCombo();
		combo.setToolTipText("Selected the containing area");
		final FormData fd_combo = new FormData();
		fd_combo.top = new FormAttachment(0, 249);
		fd_combo.bottom = new FormAttachment(0, 270);
		fd_combo.right = new FormAttachment(0, 485);
		fd_combo.left = new FormAttachment(0, 330);
		combo.setLayoutData(fd_combo);
		List<GeoArea> userAreas = DAO.localDAO().getAreasForUser(TracController.getLoggedIn());
		if (userAreas == null)
			userAreas = new ArrayList<GeoArea>();
		containerListViewer.setInput(userAreas.toArray(new GeoArea[]{}));
		containerListViewer.setSelection(new StructuredSelection());

		final Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_nameLabel = new FormData();
		fd_nameLabel.bottom = new FormAttachment(0, 245);
		fd_nameLabel.top = new FormAttachment(0, 225);
		fd_nameLabel.right = new FormAttachment(0, 62);
		fd_nameLabel.left = new FormAttachment(0, 5);
		nameLabel.setLayoutData(fd_nameLabel);
		nameLabel.setText("Name");

		Label descriptionLabel;
		descriptionLabel = new Label(shell, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.top = new FormAttachment(0, 274);
		fd_descriptionLabel.bottom = new FormAttachment(0, 295);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		areaName = new Text(shell, SWT.BORDER);
		areaName.setToolTipText("The area's name");
		areaName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (selectionChanging)
					return;
				if (areaName.getText() != null && areaName.getText().length() > 0) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}				
			}
		});
		final FormData fd_areaName = new FormData();
		fd_areaName.bottom = new FormAttachment(0, 245);
		fd_areaName.top = new FormAttachment(0, 225);
		fd_areaName.right = new FormAttachment(0, 400);
		fd_areaName.left = new FormAttachment(0, 67);
		areaName.setLayoutData(fd_areaName);

		description = new Text(shell, SWT.BORDER);
		final FormData fd_description = new FormData();
		fd_description.top = new FormAttachment(0, 274);
		fd_description.bottom = new FormAttachment(0, 295);
		description.setLayoutData(fd_description);
		combo.setEnabled(false);
		areaName.setEditable(false);
		description.setEditable(false);

		addAreaButton = new Button(shell, SWT.NONE);
		addAreaButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				areaType = AreaType.POLYGON;
				combo.setEnabled(true);
				areaName.setEditable(true);
				areaName.setText("");
				description.setEditable(true);
				description.setText("");
				message.setText("Use the mouse to draw your box. Hold down 'alt-key' and left click to finish");
				addAreaButton.setEnabled(false);
				addPointButton.setEnabled(false);
				deleteButton.setEnabled(false);
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				linePanel.open(worldWindow.get(),null,null,PolygonWidget.this);
			}
		});
		final FormData fd_addButton = new FormData();
		fd_addButton.left = new FormAttachment(100, -104);
		fd_addButton.bottom = new FormAttachment(100, -6);
		fd_addButton.top = new FormAttachment(100, -41);
		fd_addButton.right = new FormAttachment(100, -49);
		addAreaButton.setLayoutData(fd_addButton);
		addAreaButton.setText("Add Area");

		deleteButton = new Button(shell, SWT.NONE);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)areaTableViewer.getSelection();
				if (selection != null) {
					GeoArea area = (GeoArea)selection.getFirstElement();
					if (area != null) {
						if (areaTableViewer.getChecked(area)) {
							mappingView.get().removeGeoArea(area);
						}
						try {
							DAO.localDAO().beginTransaction();
							DAO.localDAO().delete(area);
							DAO.localDAO().commitTransaction();
							areaTableViewer.remove(area);
						} catch (Exception exc) {
							DAO.localDAO().rollbackTransaction();
						}
					}
				}
			}
		});
		final FormData fd_deleteButton = new FormData();
		fd_deleteButton.bottom = new FormAttachment(100, -6);
		fd_deleteButton.top = new FormAttachment(100, -41);
		fd_deleteButton.right = new FormAttachment(100, -9);
		fd_deleteButton.left = new FormAttachment(100, -49);
		deleteButton.setLayoutData(fd_deleteButton);
		deleteButton.setText("Delete");

		message = new Text(shell, SWT.MULTI);
		message.setFont(SWTResourceManager.getFont("Arial", 10, SWT.BOLD));
		message.setEditable(false);
		final FormData fd_message = new FormData();
		fd_message.bottom = new FormAttachment(0, 341);
		fd_message.top = new FormAttachment(0, 300);
		message.setLayoutData(fd_message);

		Label containerLabel;
		containerLabel = new Label(shell, SWT.NONE);
		containerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_containerLabel = new FormData();
		fd_containerLabel.left = new FormAttachment(0, 231);
		fd_containerLabel.right = new FormAttachment(0, 325);
		fd_containerLabel.bottom = new FormAttachment(areaName, 24, SWT.BOTTOM);
		fd_containerLabel.top = new FormAttachment(areaName, 5, SWT.BOTTOM);
		containerLabel.setLayoutData(fd_containerLabel);
		containerLabel.setText("Containing Area");

		okButton = new Button(shell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection layerSelection = (IStructuredSelection)layerListViewer.getSelection();
				if (layerSelection == null || layerSelection.getFirstElement() == null) {
					message.setText("Invalid layer selection, please select a layer");
				} else {
					MapLayer mapLayer = (MapLayer)layerSelection.getFirstElement();
					PolygonArea curr = (PolygonArea)DAO.localDAO().getAreaWithName(mapLayer,areaName.getText());
					if (curr == null && isCreating) {
						if (areaType == AreaType.POLYGON && polygonArea != null){
							polygonArea.setAreaName(areaName.getText());
							polygonArea.setDescription(description.getText());
							IStructuredSelection selection = (IStructuredSelection)containerListViewer.getSelection();
							GeoArea container = (GeoArea)selection.getFirstElement();
							polygonArea.setContainer(container);
							polygonArea.setMapLayer(mapLayer);
							try {
								DAO.localDAO().beginTransaction();
								DAO.localDAO().saveGeoArea(polygonArea);
								DAO.localDAO().commitTransaction();
								areaTableViewer.add(polygonArea);
								areaTableViewer.setChecked(polygonArea, true);
							} catch (Throwable exc) {
								logger.log(Level.SEVERE,"Error saving new Polygon: ",exc);
								DAO.localDAO().rollbackTransaction();
							}
						}
					} else {
						
					}
					okButton.setEnabled(false);
					cancelButton.setEnabled(false);
					combo.setEnabled(false);
					areaName.setEditable(false);
					areaName.setText("");
					description.setEditable(false);
					description.setText("");
					addAreaButton.setEnabled(true);
					addPointButton.setEnabled(true);
					deleteButton.setEnabled(true);
					message.setText("");
				}
			}
		});
		final FormData fd_okButton = new FormData();
		okButton.setLayoutData(fd_okButton);
		okButton.setText("Finish");
		okButton.setEnabled(false);

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				combo.setEnabled(false);
				areaName.setEditable(false);
				areaName.setText("");
				description.setEditable(false);
				description.setText("");
				addAreaButton.setEnabled(true);
				addPointButton.setEnabled(true);
				deleteButton.setEnabled(true);
				message.setText("");
				if (polygonArea != null) {
					mappingView.get().removeGeoArea(polygonArea);
				}
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.left = new FormAttachment(okButton, -40, SWT.LEFT);
		fd_cancelButton.right = new FormAttachment(okButton, 0, SWT.LEFT);
		fd_cancelButton.bottom = new FormAttachment(okButton, 35, SWT.TOP);
		fd_cancelButton.top = new FormAttachment(okButton, 0, SWT.TOP);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.setEnabled(false);

		linePanel = new LineBuilderPanel(shell);

		addPointButton = new Button(shell, SWT.NONE);
		fd_okButton.top = new FormAttachment(addPointButton, 0, SWT.TOP);
		fd_okButton.bottom = new FormAttachment(addPointButton, 0, SWT.BOTTOM);
		fd_okButton.left = new FormAttachment(addPointButton, -35, SWT.LEFT);
		fd_okButton.right = new FormAttachment(addPointButton, 0, SWT.LEFT);
		final FormData fd_addPoinButton = new FormData();
		fd_addPoinButton.top = new FormAttachment(addAreaButton, 0, SWT.TOP);
		fd_addPoinButton.bottom = new FormAttachment(100, -5);
		fd_addPoinButton.right = new FormAttachment(addAreaButton, 0, SWT.LEFT);
		fd_addPoinButton.left = new FormAttachment(0, 340);
		addPointButton.setLayoutData(fd_addPoinButton);
		addPointButton.setText("Add Point");

		Label layerLabel;
		layerLabel = new Label(shell, SWT.NONE);
		fd_message.right = new FormAttachment(layerLabel, 484, SWT.LEFT);
		fd_message.left = new FormAttachment(layerLabel, 0, SWT.LEFT);
		fd_description.right = new FormAttachment(layerLabel, 427, SWT.RIGHT);
		fd_description.left = new FormAttachment(layerLabel, 5, SWT.RIGHT);
		fd_descriptionLabel.left = new FormAttachment(layerLabel, -57, SWT.RIGHT);
		fd_descriptionLabel.right = new FormAttachment(layerLabel, 0, SWT.RIGHT);
		layerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_layerLabel = new FormData();
		fd_layerLabel.left = new FormAttachment(nameLabel, -57, SWT.RIGHT);
		fd_layerLabel.right = new FormAttachment(nameLabel, 0, SWT.RIGHT);
		fd_layerLabel.bottom = new FormAttachment(nameLabel, 25, SWT.BOTTOM);
		fd_layerLabel.top = new FormAttachment(nameLabel, 5, SWT.BOTTOM);
		layerLabel.setLayoutData(fd_layerLabel);
		layerLabel.setText("Layer");

		layerListViewer = new ComboViewer(shell, SWT.BORDER);
		layerListViewer.setSorter(new LayerSorter());
		layerListViewer.setLabelProvider(new LayerLabelProvider());
		layerListViewer.setContentProvider(new LayerContentProvider());
		layerListViewer.setInput(mappingView.get().layerList());
		layerCombo = layerListViewer.getCombo();
		layerCombo.setEnabled(false);
		final FormData fd_layerCombo = new FormData();
		fd_layerCombo.top = new FormAttachment(0, 249);
		fd_layerCombo.bottom = new FormAttachment(0, 270);
		fd_layerCombo.left = new FormAttachment(0, 67);
		fd_layerCombo.right = new FormAttachment(0, 230);
		layerCombo.setLayoutData(fd_layerCombo);
		//
	}

	public void objectChanged(Object object) {
		if (object == null) {
			polygonArea = null;
			okButton.setEnabled(false);
			cancelButton.setEnabled(false);
			combo.setEnabled(false);
			areaName.setEditable(false);
			description.setEditable(false);
			addAreaButton.setEnabled(true);
			addPointButton.setEnabled(true);
			deleteButton.setEnabled(true);
			message.setText("");
		} else {
			try {
				polygonArea = new PolygonArea();
				polygonArea.setPolyLines(linePanel.result);
				mappingView.get().showGeoArea(polygonArea);
				message.setText("Please supply the area with a valid name");
				areaName.setFocus();
				cancelButton.setEnabled(true);
			} catch (Exception e) {
				System.out.println("Oops!");
			}
		}
	}
}
