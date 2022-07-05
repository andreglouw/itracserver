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
import com.ikno.dao.business.SecTrackUnit;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.Validate;

public class SecTrackDetail extends Group implements IObjectEditor, FocusListener {
	private Text imei;
	private Text identifier;
	IChangeListener listener = null;
	
	SecTrackUnit unit = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SecTrackDetail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
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
		imei.setTextLimit(15);
		imei.addVerifyListener(new Validate.Numeric());
		final FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		fd_imei.right = new FormAttachment(0, 255);
		fd_imei.top = new FormAttachment(identifier, 5, SWT.BOTTOM);
		fd_imei.left = new FormAttachment(imeiNumberLabel, 5, SWT.RIGHT);
		imei.setLayoutData(fd_imei);
		identifier.addFocusListener(this);
		imei.addFocusListener(this);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void populateObject() {
		unit.setUnitName(identifier.getText());
		unit.setImei(imei.getText());
	}

	public void setObject(Object object) {
		unit = (SecTrackUnit)object;
		identifier.setText(unit.getUnitName() == null ? "" : unit.getUnitName());
		imei.setText(unit.getImei() == null ? "" : unit.getImei());
	}

	public String validate() {
		if (identifier.getText() == null || identifier.getText().equals(""))
			return "You must specify a valid identifier for the unit";
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
