package com.ikno.itracclient.wizards;

import java.util.Calendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.EZ10Unit;
import com.ikno.dao.business.GC101Unit;
import com.ikno.dao.business.Person;
import com.ikno.dao.business.SBD1Unit;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.business.SBD9602Unit;
import com.ikno.dao.business.STEPPUnit;
import com.ikno.dao.business.SecTrackUnit;
import com.ikno.dao.business.Unit;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.views.widgets.EZ10Detail;
import com.ikno.itracclient.views.widgets.GC101Detail;
import com.ikno.itracclient.views.widgets.SBD1Detail;
import com.ikno.itracclient.views.widgets.SBD2Detail;
import com.ikno.itracclient.views.widgets.STEPPDetail;
import com.ikno.itracclient.views.widgets.SecTrackDetail;

public class AddUnitPage extends WizardPage implements IChangeListener {
	private DateTime installed;
	public static final String ID = "com.ikno.itracclient.wizards.EditUnitPage"; //$NON-NLS-1$

	private Combo unitType;
	private Composite unitDetail;
	private Text manufacturer;
	private Text description;
	private StackLayout detailLayout;
	private IObjectEditor editor = null;

	private Asset asset = null;
	private Unit unit = null;
	/**
	 * Create the wizard
	 */
	public AddUnitPage(Asset asset, Unit unit) {
		super(ID);
		setTitle("Edit Unit");
		setDescription("Edit an asset's unit");
		this.asset = asset;
		this.unit = unit;
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());

		final Label descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.left = new FormAttachment(0, 5);
		fd_descriptionLabel.right = new FormAttachment(0, 80);
		fd_descriptionLabel.bottom = new FormAttachment(0, 75);
		fd_descriptionLabel.top = new FormAttachment(0, 55);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		description = new Text(container, SWT.BORDER);
		final FormData fd_description = new FormData();
		fd_description.top = new FormAttachment(0, 55);
		fd_description.bottom = new FormAttachment(0, 75);
		fd_description.right = new FormAttachment(0, 470);
		fd_description.left = new FormAttachment(0, 85);
		description.setLayoutData(fd_description);

		final Label manufacturerLabel = new Label(container, SWT.NONE);
		manufacturerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_manufacturerLabel = new FormData();
		fd_manufacturerLabel.left = new FormAttachment(0, 5);
		fd_manufacturerLabel.right = new FormAttachment(0, 80);
		fd_manufacturerLabel.bottom = new FormAttachment(0, 100);
		fd_manufacturerLabel.top = new FormAttachment(0, 80);
		manufacturerLabel.setLayoutData(fd_manufacturerLabel);
		manufacturerLabel.setText("Manufacturer");

		manufacturer = new Text(container, SWT.BORDER);
		final FormData fd_manfacturer = new FormData();
		fd_manfacturer.bottom = new FormAttachment(0, 100);
		fd_manfacturer.top = new FormAttachment(0, 80);
		fd_manfacturer.right = new FormAttachment(0, 305);
		fd_manfacturer.left = new FormAttachment(0, 85);
		manufacturer.setLayoutData(fd_manfacturer);

		final Label installedLabel = new Label(container, SWT.NONE);
		installedLabel.setAlignment(SWT.RIGHT);
		final FormData fd_installedLabel = new FormData();
		fd_installedLabel.left = new FormAttachment(0, 32);
		fd_installedLabel.right = new FormAttachment(0, 80);
		fd_installedLabel.bottom = new FormAttachment(0, 25);
		fd_installedLabel.top = new FormAttachment(0, 5);
		installedLabel.setLayoutData(fd_installedLabel);
		installedLabel.setText("Installed");

		unitDetail = new Composite(container, SWT.NONE);
		detailLayout = new StackLayout();
		unitDetail.setLayout(detailLayout);
		final FormData fd_unitDetail = new FormData();
		fd_unitDetail.bottom = new FormAttachment(100, -5);
		fd_unitDetail.top = new FormAttachment(0, 105);
		fd_unitDetail.right = new FormAttachment(100, -5);
		fd_unitDetail.left = new FormAttachment(0, 5);
		unitDetail.setLayoutData(fd_unitDetail);

		unitType = new Combo(container, SWT.READ_ONLY);
		unitType.select(0);
		unitType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				AddUnitPage.this.typeSelected();
			}
		});
		unitType.setItems(Unit.getUnitTypes());
		final FormData fd_unitType = new FormData();
		fd_unitType.top = new FormAttachment(0, 29);
		fd_unitType.bottom = new FormAttachment(0, 50);
		fd_unitType.right = new FormAttachment(0, 244);
		fd_unitType.left = new FormAttachment(0, 85);
		unitType.setLayoutData(fd_unitType);
		//
		setControl(container);

		installed = new DateTime(container, SWT.NONE);
		final FormData fd_installed = new FormData();
		fd_installed.top = new FormAttachment(0, 5);
		fd_installed.bottom = new FormAttachment(0, 25);
		fd_installed.right = new FormAttachment(0, 202);
		fd_installed.left = new FormAttachment(0, 85);
		installed.setLayoutData(fd_installed);

		Label unitTypeLabel;
		unitTypeLabel = new Label(container, SWT.NONE);
		unitTypeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_unitTypeLabel = new FormData();
		fd_unitTypeLabel.top = new FormAttachment(0, 29);
		fd_unitTypeLabel.bottom = new FormAttachment(0, 50);
		fd_unitTypeLabel.left = new FormAttachment(0, 32);
		fd_unitTypeLabel.right = new FormAttachment(0, 80);
		unitTypeLabel.setLayoutData(fd_unitTypeLabel);
		unitTypeLabel.setText("Unit Type");
		buildFromObject();
	}
	public void buildFromObject() {
		Calendar install = Calendar.getInstance();
		install.setTime(unit.getInstallation());
		installed.setYear(install.get(Calendar.YEAR));
		installed.setMonth(install.get(Calendar.MONTH));
		installed.setDay(install.get(Calendar.DAY_OF_MONTH));
		description.setText((unit.getDescription() == null) ? "" : unit.getDescription());
		manufacturer.setText((unit.getManufacturer() == null) ? "" : unit.getManufacturer());
		unitType.select(unit.getUnitType().ordinal());
		this.typeSelected();
	}
	public String validate() {
		return editor.validate();
	}
	public void populateObject() {
		unit.setDescription(description.getText());
		unit.setManufacturer(manufacturer.getText());
		Calendar install = Calendar.getInstance();
		install.set(Calendar.YEAR, installed.getYear());
		install.set(Calendar.MONTH, installed.getMonth());
		install.set(Calendar.DAY_OF_MONTH, installed.getDay());
		unit.setInstallation(install.getTime());
		editor.populateObject();
	}
	public void typeSelected() {
		if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.DSC) {
			if (!PersistantObject.instanceOf(unit,GC101Unit.class)) {
				try {
					unit = new GC101Unit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to DSC, quit and try again.");
				}
			}
			editor = new GC101Detail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.EZ10) {
			if (!PersistantObject.instanceOf(unit,EZ10Unit.class)) {
				try {
					unit = new EZ10Unit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to EZ10, quit and try again.");
				}
			}
			editor = new EZ10Detail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.SAT201) {
			if (!PersistantObject.instanceOf(unit,SecTrackUnit.class)) {
				try {
					unit = new SecTrackUnit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to SAT201, quit and try again.");
				}
			}
			editor = new SecTrackDetail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.SBD1) {
			if (!PersistantObject.instanceOf(unit,SBD1Unit.class)) {
				try {
					unit = new SBD1Unit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to SBD1, quit and try again.");
				}
			}
			editor = new SBD1Detail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.SBD2) {
			if (!PersistantObject.instanceOf(unit,SBD2Unit.class)) {
				try {
					unit = new SBD2Unit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to SBD2, quit and try again.");
				}
			}
			editor = new SBD2Detail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.SBD9602) {
			if (!PersistantObject.instanceOf(unit,SBD9602Unit.class)) {
				try {
					unit = new SBD9602Unit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to SBD9602, quit and try again.");
				}
			}
			editor = new SBD2Detail(unitDetail,SWT.None);
		} else if (Unit.UnitTypes.values()[unitType.getSelectionIndex()] == Unit.UnitTypes.STEPPII) {
			if (!PersistantObject.instanceOf(unit,STEPPUnit.class)) {
				try {
					unit = new STEPPUnit(this.unit);
				} catch (Exception e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error transforming", "Error transforming to STEPPII, quit and try again.");
				}
			}
			editor = new STEPPDetail(unitDetail,SWT.None);
		}
		editor.setChangeListener(this);
		detailLayout.topControl = (Composite)editor;
		unitDetail.layout();
		editor.setObject(unit);
		objectChanged(unit);
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
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
}
