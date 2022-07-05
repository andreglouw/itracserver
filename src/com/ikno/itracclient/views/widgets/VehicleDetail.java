package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Vehicle;
import com.ikno.itracclient.IObjectEditor;

public class VehicleDetail extends Group implements IObjectEditor, FocusListener {
	private Combo vehicleType;
	private Text registration;
	IChangeListener listener = null;
	
	private Vehicle vehicle = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public VehicleDetail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		setText("Vehicle Detail");

		final Label registrationLabel = new Label(this, SWT.NONE);
		final FormData fd_registrationLabel = new FormData();
		fd_registrationLabel.top = new FormAttachment(0, 10);
		fd_registrationLabel.bottom = new FormAttachment(0, 30);
		fd_registrationLabel.left = new FormAttachment(0, 10);
		registrationLabel.setLayoutData(fd_registrationLabel);
		registrationLabel.setAlignment(SWT.RIGHT);
		registrationLabel.setText("Registration");

		registration = new Text(this, SWT.BORDER);
		final FormData fd_registration = new FormData();
		fd_registration.bottom = new FormAttachment(registrationLabel, 20, SWT.TOP);
		fd_registration.top = new FormAttachment(registrationLabel, 0, SWT.TOP);
		fd_registration.right = new FormAttachment(registrationLabel, 92, SWT.RIGHT);
		fd_registration.left = new FormAttachment(registrationLabel, 5, SWT.RIGHT);
		registration.setLayoutData(fd_registration);

		final Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_typeLabel = new FormData();
		fd_typeLabel.left = new FormAttachment(registrationLabel, -58, SWT.RIGHT);
		fd_typeLabel.right = new FormAttachment(registrationLabel, 0, SWT.RIGHT);
		fd_typeLabel.bottom = new FormAttachment(registrationLabel, 25, SWT.BOTTOM);
		fd_typeLabel.top = new FormAttachment(registrationLabel, 5, SWT.BOTTOM);
		typeLabel.setLayoutData(fd_typeLabel);
		typeLabel.setText("Type");

		vehicleType = new Combo(this, SWT.READ_ONLY);
		vehicleType.select(0);
		vehicleType.setItems(Vehicle.getTypes());
		final FormData fd_vehicleType = new FormData();
		fd_vehicleType.right = new FormAttachment(0, 190);
		fd_vehicleType.bottom = new FormAttachment(typeLabel, 0, SWT.BOTTOM);
		fd_vehicleType.top = new FormAttachment(registration, 5, SWT.BOTTOM);
		fd_vehicleType.left = new FormAttachment(typeLabel, 5, SWT.RIGHT);
		vehicleType.setLayoutData(fd_vehicleType);
		registration.addFocusListener(this);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setObject(Object object) {
		this.vehicle = (Vehicle)object;
		registration.setText(vehicle.getIdentifier() == null ? "" : vehicle.getIdentifier());
		vehicleType.select(vehicle.getType().ordinal());
	}
	public String validate() {
		if (registration.getText() == null || registration.getText().equals(""))
			return "You must specify a valid registration number for the vehicle";
		return null;
	}
	public void populateObject() {
		vehicle.setIdentifier(registration.getText());
		vehicle.setTypeValue(vehicleType.getSelectionIndex());
	}
	public void setChangeListener(IChangeListener listener) {
		this.listener = listener;
	}
	public void focusGained(FocusEvent e) {
	}
	public void focusLost(FocusEvent e) {
		if (listener != null)
			listener.objectChanged(vehicle);
	}
}
