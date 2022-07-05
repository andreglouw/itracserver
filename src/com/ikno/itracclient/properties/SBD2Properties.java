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

import com.ikno.dao.business.SBD1Unit;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;

public class SBD2Properties extends Group implements IPropertyComposite {

	private Text unitName;
	private UnitPropertyPage control = null;
	private Text imei;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SBD2Properties(Composite parent, int style, UnitPropertyPage control) {
		super(parent, style);
		this.control = control;
		setText("SBD2 Unit");
		setLayout(new FormLayout());

		final Label imeiNumberLabel = new Label(this, SWT.NONE);
		FormData fd_imeiNumberLabel = new FormData();
		fd_imeiNumberLabel.bottom = new FormAttachment(0, 25);
		fd_imeiNumberLabel.right = new FormAttachment(0, 95);
		fd_imeiNumberLabel.top = new FormAttachment(0, 5);
		fd_imeiNumberLabel.left = new FormAttachment(0, 5);
		imeiNumberLabel.setLayoutData(fd_imeiNumberLabel);
		imeiNumberLabel.setAlignment(SWT.RIGHT);
		imeiNumberLabel.setText("Unit Name");

		unitName = new Text(this, SWT.BORDER);
		FormData fd_unitName = new FormData();
		fd_unitName.bottom = new FormAttachment(imeiNumberLabel, 20);
		fd_unitName.top = new FormAttachment(imeiNumberLabel, 0, SWT.TOP);
		unitName.setLayoutData(fd_unitName);
		unitName.setEditable(true);
		
		Label label = new Label(this, SWT.NONE);
		fd_unitName.right = new FormAttachment(label, 156, SWT.RIGHT);
		fd_unitName.left = new FormAttachment(label, 6);
		FormData fd_label = new FormData();
		fd_label.bottom = new FormAttachment(0, 51);
		fd_label.right = new FormAttachment(0, 95);
		fd_label.top = new FormAttachment(0, 31);
		fd_label.left = new FormAttachment(0, 5);
		label.setLayoutData(fd_label);
		label.setText("IMEI Number");
		label.setAlignment(SWT.RIGHT);
		
		imei = new Text(this, SWT.BORDER);
		FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(unitName, 26, SWT.BOTTOM);
		fd_imei.top = new FormAttachment(unitName, 6);
		fd_imei.right = new FormAttachment(label, 159, SWT.RIGHT);
		fd_imei.left = new FormAttachment(label, 6);
		imei.setLayoutData(fd_imei);
		imei.setEditable(true);
		buildFromObject();
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void buildFromObject() {
		SBD2Unit unit = (SBD2Unit)PersistantObject.resolveProxy(control.getElement().getAdapter(Object.class));
		imei.setText(unit.getImei());
		unitName.setText(unit.getUnitName());
	}
	public boolean performOk() {
		SBD2Unit unit = (SBD2Unit)control.getUnit();
		try {
			unit.setImei(imei.getText());
			unit.setUnitName(unitName.getText());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
