package com.ikno.itracclient.properties;

import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.EZ10Unit;
import com.ikno.dao.business.GC101Unit;
import com.ikno.dao.business.SBD1Unit;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.business.SecTrackUnit;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.Vehicle;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;
import com.ikno.itracclient.utils.Formatting;

public class UnitPropertyPage extends PropertyPage {
	private boolean finished = false;
	private Composite unitDetail;
	private Text installed;
	private Text manufacturer;
	private Text description;
	private StackLayout detailLayout;
	private Text eMail;
	private Text password;
	private Text cellphone;

	@Override
	public void createControl(Composite parent) {
		this.noDefaultAndApplyButton();
		super.createControl(parent);
	}

	/**
	 * Create the property page
	 */
	public UnitPropertyPage() {
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

		final Label descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.left = new FormAttachment(0, 5);
		fd_descriptionLabel.right = new FormAttachment(0, 80);
		fd_descriptionLabel.bottom = new FormAttachment(0, 25);
		fd_descriptionLabel.top = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		description = new Text(container, SWT.BORDER);
		description.setEditable(true);
		final FormData fd_description = new FormData();
		fd_description.top = new FormAttachment(0, 5);
		fd_description.bottom = new FormAttachment(0, 25);
		fd_description.right = new FormAttachment(0, 470);
		fd_description.left = new FormAttachment(0, 85);
		description.setLayoutData(fd_description);

		final Label manufacturerLabel = new Label(container, SWT.NONE);
		manufacturerLabel.setAlignment(SWT.RIGHT);
		final FormData fd_manufacturerLabel = new FormData();
		fd_manufacturerLabel.top = new FormAttachment(0, 30);
		fd_manufacturerLabel.bottom = new FormAttachment(0, 50);
		fd_manufacturerLabel.left = new FormAttachment(0, 5);
		fd_manufacturerLabel.right = new FormAttachment(0, 80);
		manufacturerLabel.setLayoutData(fd_manufacturerLabel);
		manufacturerLabel.setText("Manufacturer");

		manufacturer = new Text(container, SWT.BORDER);
		manufacturer.setEditable(false);
		final FormData fd_manfacturer = new FormData();
		fd_manfacturer.bottom = new FormAttachment(0, 50);
		fd_manfacturer.top = new FormAttachment(0, 30);
		fd_manfacturer.right = new FormAttachment(0, 305);
		fd_manfacturer.left = new FormAttachment(0, 85);
		manufacturer.setLayoutData(fd_manfacturer);

		final Label installedLabel = new Label(container, SWT.NONE);
		installedLabel.setAlignment(SWT.RIGHT);
		final FormData fd_installedLabel = new FormData();
		fd_installedLabel.top = new FormAttachment(manufacturer, 6);
		fd_installedLabel.left = new FormAttachment(0, 5);
		fd_installedLabel.right = new FormAttachment(0, 80);
		fd_installedLabel.bottom = new FormAttachment(0, 75);
		installedLabel.setLayoutData(fd_installedLabel);
		installedLabel.setText("Installed");

		installed = new Text(container, SWT.BORDER);
		installed.setEditable(false);
		final FormData fd_installed = new FormData();
		fd_installed.bottom = new FormAttachment(0, 75);
		fd_installed.top = new FormAttachment(0, 55);
		fd_installed.right = new FormAttachment(0, 210);
		fd_installed.left = new FormAttachment(0, 85);
		installed.setLayoutData(fd_installed);

		unitDetail = new Composite(container, SWT.NONE);
		detailLayout = new StackLayout();
		unitDetail.setLayout(detailLayout);
		final FormData fd_unitDetail = new FormData();
		fd_unitDetail.bottom = new FormAttachment(100, -5);
		fd_unitDetail.right = new FormAttachment(100, -12);
		fd_unitDetail.top = new FormAttachment(0, 155);
		fd_unitDetail.left = new FormAttachment(installedLabel, 0, SWT.LEFT);
		unitDetail.setLayoutData(fd_unitDetail);
		
		Label lblEmail = new Label(container, SWT.NONE);
		lblEmail.setAlignment(SWT.RIGHT);
		FormData fd_lblEmail = new FormData();
		fd_lblEmail.right = new FormAttachment(installedLabel, 0, SWT.RIGHT);
		fd_lblEmail.left = new FormAttachment(0, 12);
		lblEmail.setLayoutData(fd_lblEmail);
		lblEmail.setText("e-Mail");
		
		eMail = new Text(container, SWT.BORDER);
		fd_lblEmail.top = new FormAttachment(eMail, 0, SWT.TOP);
		fd_lblEmail.bottom = new FormAttachment(eMail, 0, SWT.BOTTOM);
		FormData fd_eMail = new FormData();
		fd_eMail.right = new FormAttachment(0, 270);
		fd_eMail.top = new FormAttachment(installed, 6);
		fd_eMail.left = new FormAttachment(lblEmail, 6);
		eMail.setLayoutData(fd_eMail);
		
		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setAlignment(SWT.RIGHT);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.top = new FormAttachment(0, 85);
		fd_lblPassword.left = new FormAttachment(eMail, 6);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText("Password");
		
		password = new Text(container, SWT.BORDER);
		FormData fd_password = new FormData();
		fd_password.right = new FormAttachment(unitDetail, 0, SWT.RIGHT);
		fd_password.top = new FormAttachment(eMail, 0, SWT.TOP);
		fd_password.left = new FormAttachment(lblPassword, 6);
		password.setLayoutData(fd_password);
		
		Label lblCellphone = new Label(container, SWT.NONE);
		lblCellphone.setAlignment(SWT.RIGHT);
		FormData fd_lblCellphone = new FormData();
		fd_lblCellphone.right = new FormAttachment(eMail, -6);
		fd_lblCellphone.top = new FormAttachment(eMail, 6);
		fd_lblCellphone.left = new FormAttachment(lblEmail, 0, SWT.LEFT);
		lblCellphone.setLayoutData(fd_lblCellphone);
		lblCellphone.setText("Cellphone");
		
		cellphone = new Text(container, SWT.BORDER);
		FormData fd_cellphone = new FormData();
		fd_cellphone.right = new FormAttachment(eMail, 0, SWT.RIGHT);
		fd_cellphone.top = new FormAttachment(eMail, 6);
		fd_cellphone.left = new FormAttachment(lblCellphone, 6);
		cellphone.setLayoutData(fd_cellphone);
		buildFromObject();
		//
		return container;
	}

	public void buildFromObject() {
		Unit unit = (Unit)this.getElement().getAdapter(Unit.class);
		description.setText((unit.getDescription() == null) ? "" : unit.getDescription());
		manufacturer.setText((unit.getManufacturer() == null) ? "" : unit.getManufacturer());
		installed.setText(unit.getInstallation() == null ? "" : Formatting.format(unit.getInstallation()));
		eMail.setText((unit.getEmail() == null) ? "" : unit.getEmail());
		password.setText((unit.getEmailPassword() == null) ? "" : unit.getEmailPassword());
		cellphone.setText((unit.getCellphone() == null) ? "" : unit.getCellphone());
		try {
			if (PersistantObject.instanceOf(unit,GC101Unit.class)) {
				detailLayout.topControl = new GC101Properties(unitDetail,SWT.None, this);
			} else if (PersistantObject.instanceOf(unit,EZ10Unit.class)) {
				detailLayout.topControl = new EZ10Properties(unitDetail,SWT.None, this);
			} else if (PersistantObject.instanceOf(unit,SecTrackUnit.class)) {
				detailLayout.topControl = new SecTrackProperties(unitDetail,SWT.None,this);
			} else if (PersistantObject.instanceOf(unit,SBD1Unit.class)) {
				detailLayout.topControl = new SBD1Properties(unitDetail,SWT.None, this);
			} else if (PersistantObject.instanceOf(unit,SBD2Unit.class)) {
				detailLayout.topControl = new SBD2Properties(unitDetail,SWT.None, this);
			}
			unitDetail.layout();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	public Unit getUnit() {
		Unit unit = (Unit)this.getElement().getAdapter(Unit.class);
		return (Unit)PersistantObject.resolveProxy(unit);
	}
	public boolean performOk() {
		if (finished == true)
			return true;
		finished = false;
		boolean result = false;
		try {
			DAO.localDAO().beginTransaction();
			Unit unit = this.getUnit();
			String text = description.getText();
			if (text != null)
				unit.setDescription(text);
			text = eMail.getText();
			if (text != null)
				unit.setEmail(text);
			text = password.getText();
			if (text != null)
				unit.setEmailPassword(text);
			text = cellphone.getText();
			if (text != null)
				unit.setCellphone(text);
			result = ((IPropertyComposite)detailLayout.topControl).performOk();
			if (result) {
				DAO.localDAO().saveOrUpdate(unit);
				DAO.localDAO().commitTransaction();
			}
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
		}
		return result;
	}
}
