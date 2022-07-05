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
import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Vehicle;
import com.ikno.dao.business.Aircraft.Model;
import com.ikno.itracclient.IObjectEditor;

public class AircraftDetail extends Group implements IObjectEditor, FocusListener {
	private Combo aircraftType;
	private Text registration;
	
	private Aircraft aircraft;
	IChangeListener listener = null;
	private Combo model;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AircraftDetail(Composite parent, int style) {
		super(parent, style);
		setText("Aircraft Detail");
		setLayout(new FormLayout());

		final Label stateLabel = new Label(this, SWT.NONE);
		FormData fd_stateLabel = new FormData();
		fd_stateLabel.left = new FormAttachment(0, 10);
		stateLabel.setLayoutData(fd_stateLabel);
		stateLabel.setAlignment(SWT.RIGHT);
		stateLabel.setText("Model");

		Label registrationLabel;
		registrationLabel = new Label(this, SWT.NONE);
		fd_stateLabel.right = new FormAttachment(registrationLabel, 0, SWT.RIGHT);
		FormData fd_registrationLabel = new FormData();
		fd_registrationLabel.bottom = new FormAttachment(0, 30);
		fd_registrationLabel.right = new FormAttachment(0, 65);
		fd_registrationLabel.top = new FormAttachment(0, 10);
		fd_registrationLabel.left = new FormAttachment(0, 5);
		registrationLabel.setLayoutData(fd_registrationLabel);
		registrationLabel.setAlignment(SWT.RIGHT);
		registrationLabel.setText("Registration");

		registration = new Text(this, SWT.BORDER);
		FormData fd_registration = new FormData();
		fd_registration.bottom = new FormAttachment(0, 31);
		fd_registration.right = new FormAttachment(0, 190);
		fd_registration.top = new FormAttachment(0, 10);
		fd_registration.left = new FormAttachment(0, 70);
		registration.setLayoutData(fd_registration);
		registration.addFocusListener(this);

		final Label typeLabel = new Label(this, SWT.NONE);
		FormData fd_typeLabel = new FormData();
		fd_typeLabel.bottom = new FormAttachment(0, 55);
		fd_typeLabel.right = new FormAttachment(0, 65);
		fd_typeLabel.top = new FormAttachment(0, 35);
		fd_typeLabel.left = new FormAttachment(0, 5);
		typeLabel.setLayoutData(fd_typeLabel);
		typeLabel.setAlignment(SWT.RIGHT);
		typeLabel.setText("Type");

		aircraftType = new Combo(this, SWT.NONE);
		FormData fd_aircraftType = new FormData();
		fd_aircraftType.right = new FormAttachment(0, 200);
		fd_aircraftType.top = new FormAttachment(0, 36);
		fd_aircraftType.left = new FormAttachment(0, 70);
		aircraftType.setLayoutData(fd_aircraftType);
		aircraftType.select(0);
		aircraftType.setItems(Aircraft.getTypes());
		
		model = new Combo(this, SWT.NONE);
		fd_stateLabel.top = new FormAttachment(model, 3, SWT.TOP);
		FormData fd_model = new FormData();
		fd_model.right = new FormAttachment(100, -255);
		fd_model.left = new FormAttachment(stateLabel, 6);
		fd_model.top = new FormAttachment(aircraftType, 6);
		model.setLayoutData(fd_model);
		model.select(0);
		model.setItems(new String[] {"PAC 750", "EMBRAER 120", "ROBINSON R44"});
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setObject(Object object) {
		this.aircraft = (Aircraft)object;
		aircraftType.select(aircraft.getType().ordinal());
		registration.setText(aircraft.getIdentifier() == null ? "" : aircraft.getIdentifier());
		Model acmodel = aircraft.getModel();
		if (acmodel == null)
			acmodel = Model.EMBREAR_120;
		model.select(acmodel.ordinal());
	}
	public String validate() {
		if (registration.getText() == null || registration.getText().equals(""))
			return "You must specify a valid aircraft registration number";
		return null;
	}
	public void populateObject() {
		aircraft.setTypeValue(aircraftType.getSelectionIndex());
		aircraft.setIdentifier(registration.getText());
		aircraft.setModelValue(model.getSelectionIndex());
	}
	public void setChangeListener(IChangeListener listener) {
		this.listener = listener;
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		if (listener != null)
			this.listener.objectChanged(aircraft);
	}
}
