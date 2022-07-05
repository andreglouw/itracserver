package com.ikno.itracclient.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.business.SecTrackUnit;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;

public class SecTrackProperties extends Group implements IPropertyComposite {

	private Text imei;
	private Text identifier;
	private UnitPropertyPage control = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SecTrackProperties(Composite parent, int style, UnitPropertyPage control) {
		super(parent, style);
		setLayout(new FormLayout());
		this.control = control;
		setText("SAT201 Unit");

		final Label identifierLabel = new Label(this, SWT.NONE);
		identifierLabel.setAlignment(SWT.RIGHT);
		final FormData fd_identifierLabel = new FormData();
		fd_identifierLabel.bottom = new FormAttachment(0, 25);
		fd_identifierLabel.right = new FormAttachment(0, 95);
		fd_identifierLabel.top = new FormAttachment(0, 5);
		fd_identifierLabel.left = new FormAttachment(0, 5);
		identifierLabel.setLayoutData(fd_identifierLabel);
		identifierLabel.setText("Identifier");

		identifier = new Text(this, SWT.BORDER);
		identifier.setEditable(true);
		final FormData fd_identifier = new FormData();
		fd_identifier.bottom = new FormAttachment(identifierLabel, 0, SWT.BOTTOM);
		fd_identifier.right = new FormAttachment(0, 210);
		fd_identifier.top = new FormAttachment(identifierLabel, 0, SWT.TOP);
		fd_identifier.left = new FormAttachment(identifierLabel, 5, SWT.RIGHT);
		identifier.setLayoutData(fd_identifier);

		final Label imeiNumberLabel = new Label(this, SWT.NONE);
		imeiNumberLabel.setAlignment(SWT.RIGHT);
		final FormData fd_imeiNumberLabel = new FormData();
		fd_imeiNumberLabel.bottom = new FormAttachment(0, 50);
		fd_imeiNumberLabel.right = new FormAttachment(identifierLabel, 0, SWT.RIGHT);
		fd_imeiNumberLabel.top = new FormAttachment(identifierLabel, 5, SWT.BOTTOM);
		fd_imeiNumberLabel.left = new FormAttachment(identifierLabel, 0, SWT.LEFT);
		imeiNumberLabel.setLayoutData(fd_imeiNumberLabel);
		imeiNumberLabel.setText("IMEI Number");

		imei = new Text(this, SWT.BORDER);
		imei.setEditable(true);
		final FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		fd_imei.right = new FormAttachment(0, 255);
		fd_imei.top = new FormAttachment(identifier, 5, SWT.BOTTOM);
		fd_imei.left = new FormAttachment(imeiNumberLabel, 5, SWT.RIGHT);
		imei.setLayoutData(fd_imei);
		buildFromObject();
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void buildFromObject() {
		SecTrackUnit unit = (SecTrackUnit)PersistantObject.resolveProxy(control.getElement().getAdapter(Object.class));
		identifier.setText(unit.getUnitName());
		imei.setText(unit.getImei() == null ? "" : unit.getImei());
	}
	public boolean performOk() {
		SecTrackUnit unit = (SecTrackUnit)control.getUnit();
		try {
			unit.setUnitName(identifier.getText());
			unit.setImei(imei.getText());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
