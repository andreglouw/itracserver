package com.ikno.itracclient.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Asset;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class AircraftProperties extends Group implements IPropertyComposite {

	private Combo model;
	private Combo aircraftType;
	private Text landing;
	private Text takeoff;
	private Text registration;
	
	private AssetPropertyPage control = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AircraftProperties(Composite parent, int style, AssetPropertyPage control) {
		super(parent, style);
		this.control = control;
		setText("Aircraft Detail");

		Label registrationLabel;
		setLayout(null);
		registrationLabel = new Label(this, SWT.NONE);
		registrationLabel.setBounds(8, 23, 63, 15);
		registrationLabel.setAlignment(SWT.RIGHT);
		registrationLabel.setText("Registration");

		registration = new Text(this, SWT.BORDER);
		registration.setBounds(76, 20, 74, 21);
						
								final Label typeLabel = new Label(this, SWT.NONE);
								typeLabel.setBounds(8, 50, 26, 15);
								typeLabel.setAlignment(SWT.RIGHT);
								typeLabel.setText("Type");
				
						aircraftType = new Combo(this, SWT.NONE);
						aircraftType.setBounds(76, 46, 111, 23);
						aircraftType.select(0);
						aircraftType.setItems(Aircraft.getTypes());
		
				final Label stateLabel = new Label(this, SWT.NONE);
				stateLabel.setBounds(8, 78, 34, 15);
				stateLabel.setAlignment(SWT.RIGHT);
				stateLabel.setText("Model");
		model = new Combo(this, SWT.NONE);
		model.setBounds(76, 74, 111, 23);
		model.select(0);
		model.setItems(new String[] {"PAC 750", "EMBRAER 120", "ROBINSON R44"});
		final Label takeOffLabel = new Label(this, SWT.NONE);
		takeOffLabel.setBounds(192, 78, 45, 15);
		takeOffLabel.setAlignment(SWT.RIGHT);
		takeOffLabel.setText("Take Off");

		takeoff = new Text(this, SWT.BORDER);
		takeoff.setBounds(242, 75, 45, 21);

		final Label landingLabel = new Label(this, SWT.NONE);
		landingLabel.setBounds(293, 78, 43, 15);
		landingLabel.setAlignment(SWT.RIGHT);
		landingLabel.setText("Landing");

		landing = new Text(this, SWT.BORDER);
		landing.setBounds(342, 75, 45, 21);
		//
		buildFromObject();
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setEnabled(boolean enabled) {
		registration.setEnabled(enabled);
		takeoff.setEnabled(enabled);
		landing.setEnabled(enabled);
		aircraftType.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	public void buildFromObject() {
		Aircraft aircraft = (Aircraft)control.getAsset();
		aircraftType.select(aircraft.getType().ordinal());
		model.select(aircraft.getModel().ordinal());
		registration.setText(aircraft.getIdentifier() == null ? "" : aircraft.getIdentifier());
		String text = String.format("%.0f", aircraft.getTakeOffSpeed());
		takeoff.setText(text);
		text = String.format("%.0f", aircraft.getLandingSpeed());
		landing.setText(text);
	}
	public boolean performOk() {
		Aircraft aircraft = (Aircraft)control.getAsset();
		try {
			aircraft.setIdentifier(registration.getText() == null ? "" : registration.getText());
			aircraft.setTypeValue(aircraftType.getSelectionIndex());
			aircraft.setModelValue(model.getSelectionIndex());
			aircraft.setTakeOffSpeed(Float.parseFloat(takeoff.getText()));
			aircraft.setLandingSpeed(Float.parseFloat(landing.getText()));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
