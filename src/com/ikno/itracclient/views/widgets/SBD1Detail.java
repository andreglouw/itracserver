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
import com.ikno.dao.business.SBD1Unit;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.itracclient.IObjectEditor;
import com.ikno.itracclient.Validate;

public class SBD1Detail extends Group implements IObjectEditor, FocusListener {

	private Text imei;
	IChangeListener listener = null;
	
	private SBD1Unit unit = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SBD1Detail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
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
		imei.addVerifyListener(new Validate.Numeric());
		imei.setTextLimit(15);
		final FormData fd_imei = new FormData();
		fd_imei.bottom = new FormAttachment(imeiNumberLabel, 0, SWT.BOTTOM);
		fd_imei.right = new FormAttachment(0, 250);
		fd_imei.top = new FormAttachment(imeiNumberLabel, 0, SWT.TOP);
		fd_imei.left = new FormAttachment(imeiNumberLabel, 5, SWT.RIGHT);
		imei.setLayoutData(fd_imei);
		imei.addFocusListener(this);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void populateObject() {
		unit.setUnitName(imei.getText());
	}

	public void setObject(Object object) {
		unit = (SBD1Unit)object;
		imei.setText(unit.getUnitName() == null ? "" : unit.getUnitName());
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
