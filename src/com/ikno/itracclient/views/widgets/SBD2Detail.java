package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.Validate;

public class SBD2Detail extends Group implements IObjectEditor, FocusListener {
	private Text unitName;
	private Text imei;
	IChangeListener listener = null;
	
	private SBD2Unit unit = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SBD2Detail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		setText("SBD2 Unit");

		final Label imeiNumberLabel = new Label(this, SWT.NONE);
		imeiNumberLabel.setAlignment(SWT.RIGHT);
		final FormData fd_imeiNumberLabel = new FormData();
		fd_imeiNumberLabel.top = new FormAttachment(0, 30);
		fd_imeiNumberLabel.bottom = new FormAttachment(0, 50);
		fd_imeiNumberLabel.left = new FormAttachment(0, 5);
		fd_imeiNumberLabel.right = new FormAttachment(0, 95);
		imeiNumberLabel.setLayoutData(fd_imeiNumberLabel);
		imeiNumberLabel.setText("IMEI Number");

		imei = new Text(this, SWT.BORDER);
		imei.setTextLimit(15);
		imei.addVerifyListener(new Validate.Numeric());
		final FormData fd_imei = new FormData();
		fd_imei.top = new FormAttachment(imeiNumberLabel, -20, SWT.BOTTOM);
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		imei.setLayoutData(fd_imei);
		imei.addFocusListener(this);

		Label unitNameLabel;
		unitNameLabel = new Label(this, SWT.NONE);
		fd_imei.right = new FormAttachment(unitNameLabel, 155, SWT.RIGHT);
		fd_imei.left = new FormAttachment(unitNameLabel, 5, SWT.RIGHT);
		unitNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_unitNameLabel = new FormData();
		fd_unitNameLabel.bottom = new FormAttachment(imeiNumberLabel, -5, SWT.TOP);
		fd_unitNameLabel.right = new FormAttachment(imeiNumberLabel, 0, SWT.RIGHT);
		fd_unitNameLabel.top = new FormAttachment(0, 5);
		fd_unitNameLabel.left = new FormAttachment(imeiNumberLabel, 0, SWT.LEFT);
		unitNameLabel.setLayoutData(fd_unitNameLabel);
		unitNameLabel.setText("Unit Name");

		unitName = new Text(this, SWT.BORDER);
		final FormData fd_unitName = new FormData();
		fd_unitName.bottom = new FormAttachment(imei, -5, SWT.TOP);
		fd_unitName.right = new FormAttachment(imei, 0, SWT.RIGHT);
		fd_unitName.top = new FormAttachment(unitNameLabel, 0, SWT.TOP);
		fd_unitName.left = new FormAttachment(unitNameLabel, 5, SWT.RIGHT);
		unitName.setLayoutData(fd_unitName);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void populateObject() {
		unit.setUnitName(unitName.getText());
		((SBD2Unit)unit).setImei(imei.getText());
	}

	public void setObject(Object object) {
		unit = (SBD2Unit)object;
		unitName.setText(unit.getUnitName() == null ? "" : unit.getUnitName());
		imei.setText(unit.getImei()== null ? "" : unit.getImei());
	}

	public String validate() {
		if (imei.getText() == null || imei.getText().equals("") || imei.getText().length() != 15)
			return "You must specify a valid IMEI number for the unit (15 characters numeric)";
		return null;
	}
	public void setChangeListener(IChangeListener listener) {
		this.listener = listener;
	}
	public void focusGained(FocusEvent e) {
	}
	public void focusLost(FocusEvent e) {
		if (listener != null)
			listener.objectChanged(unit);
	}
}
