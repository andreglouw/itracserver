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

import com.ikno.dao.business.EZ10Unit;
import com.ikno.dao.business.SBD1Unit;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;

public class SBD1Properties extends Group implements IPropertyComposite {

	private Text imei;
	private UnitPropertyPage control = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SBD1Properties(Composite parent, int style, UnitPropertyPage control) {
		super(parent, style);
		setLayout(new FormLayout());
		this.control = control;
		setText("SBD1 Unit");

		final Label imeiNumberLabel = new Label(this, SWT.NONE);
		imeiNumberLabel.setAlignment(SWT.RIGHT);
		final FormData fd_imeiNumberLabel = new FormData();
		fd_imeiNumberLabel.bottom = new FormAttachment(0, 25);
		fd_imeiNumberLabel.right = new FormAttachment(0, 95);
		fd_imeiNumberLabel.top = new FormAttachment(0, 5);
		fd_imeiNumberLabel.left = new FormAttachment(0, 5);
		imeiNumberLabel.setLayoutData(fd_imeiNumberLabel);
		imeiNumberLabel.setText("IMEI Number");

		imei = new Text(this, SWT.BORDER);
		imei.setEditable(true);
		final FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		fd_imei.right = new FormAttachment(0, 250);
		fd_imei.top = new FormAttachment(imeiNumberLabel, 0, SWT.TOP);
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
		SBD1Unit unit = (SBD1Unit)PersistantObject.resolveProxy(control.getElement().getAdapter(Object.class));
		imei.setText(unit.getUnitName());
	}
	public boolean performOk() {
		SBD1Unit unit = (SBD1Unit)control.getUnit();
		try {
			unit.setUnitName(imei.getText());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
