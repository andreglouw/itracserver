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
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.GC101Unit;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.Validate;

public class GC101Detail extends Group implements IObjectEditor, FocusListener {

	private Text imei;
	private Text cellphone;
	private Text unitName;
	IChangeListener listener;
	
	private GC101Unit unit = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public GC101Detail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		setText("DSC Unit");

		final Label unitNameLabel = new Label(this, SWT.NONE);
		unitNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_unitNameLabel = new FormData();
		fd_unitNameLabel.bottom = new FormAttachment(0, 25);
		fd_unitNameLabel.top = new FormAttachment(0, 5);
		fd_unitNameLabel.left = new FormAttachment(0, 5);
		unitNameLabel.setLayoutData(fd_unitNameLabel);
		unitNameLabel.setText("Unit Name");

		unitName = new Text(this, SWT.BORDER);
		fd_unitNameLabel.right = new FormAttachment(unitName, -5, SWT.LEFT);
		final FormData fd_unitName = new FormData();
		fd_unitName.left = new FormAttachment(0, 105);
		fd_unitName.right = new FormAttachment(0, 235);
		fd_unitName.bottom = new FormAttachment(unitNameLabel, 20, SWT.TOP);
		fd_unitName.top = new FormAttachment(unitNameLabel, 0, SWT.TOP);
		unitName.setLayoutData(fd_unitName);

		final Label cellphoneNumberLabel = new Label(this, SWT.NONE);
		cellphoneNumberLabel.setAlignment(SWT.RIGHT);
		final FormData fd_cellphoneNumberLabel = new FormData();
		fd_cellphoneNumberLabel.bottom = new FormAttachment(0, 50);
		fd_cellphoneNumberLabel.right = new FormAttachment(unitNameLabel, 0, SWT.RIGHT);
		fd_cellphoneNumberLabel.top = new FormAttachment(unitNameLabel, 5, SWT.BOTTOM);
		fd_cellphoneNumberLabel.left = new FormAttachment(unitNameLabel, 0, SWT.LEFT);
		cellphoneNumberLabel.setLayoutData(fd_cellphoneNumberLabel);
		cellphoneNumberLabel.setText("Cellphone Number");

		cellphone = new Text(this, SWT.BORDER);
		cellphone.setTextLimit(12);
		cellphone.addVerifyListener(new Validate.Numeric());
		final FormData fd_cellphone = new FormData();
		fd_cellphone.bottom = new FormAttachment(cellphoneNumberLabel, 0, SWT.BOTTOM);
		fd_cellphone.right = new FormAttachment(0, 220);
		fd_cellphone.top = new FormAttachment(unitName, 5, SWT.BOTTOM);
		fd_cellphone.left = new FormAttachment(cellphoneNumberLabel, 5, SWT.RIGHT);
		cellphone.setLayoutData(fd_cellphone);

		final Label imeiNumberLabel = new Label(this, SWT.NONE);
		imeiNumberLabel.setAlignment(SWT.RIGHT);
		final FormData fd_imeiNumberLabel = new FormData();
		fd_imeiNumberLabel.bottom = new FormAttachment(0, 75);
		fd_imeiNumberLabel.right = new FormAttachment(cellphoneNumberLabel, 0, SWT.RIGHT);
		fd_imeiNumberLabel.top = new FormAttachment(cellphoneNumberLabel, 5, SWT.BOTTOM);
		fd_imeiNumberLabel.left = new FormAttachment(cellphoneNumberLabel, 0, SWT.LEFT);
		imeiNumberLabel.setLayoutData(fd_imeiNumberLabel);
		imeiNumberLabel.setText("IMEI Number");

		imei = new Text(this, SWT.BORDER);
		imei.setTextLimit(15);
		imei.addVerifyListener(new Validate.Numeric());
		final FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		fd_imei.right = new FormAttachment(0, 260);
		fd_imei.top = new FormAttachment(cellphone, 5, SWT.BOTTOM);
		fd_imei.left = new FormAttachment(imeiNumberLabel, 5, SWT.RIGHT);
		imei.setLayoutData(fd_imei);
		unitName.addFocusListener(this);
		cellphone.addFocusListener(this);
		imei.addFocusListener(this);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void populateObject() {
		unit.setUnitName(unitName.getText());
		unit.setPhoneNumber(cellphone.getText());
		unit.setImei(imei.getText());
	}

	public void setObject(Object object) {
		unit = (GC101Unit)object;
		unitName.setText(unit.getUnitName() == null ? "" : unit.getUnitName());
		cellphone.setText(unit.getPhoneNumber() == null ? "" : unit.getPhoneNumber());
		imei.setText(unit.getImei() == null ? "" : unit.getImei());
	}

	public String validate() {
		if (unitName.getText() == null || unitName.getText().equals(""))
			return "You must specify a name (as programmed) for the unit";
		if (cellphone.getText() == null || cellphone.getText().equals(""))
			return "You must specify a valid cell phone number for the unit";
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
