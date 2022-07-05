package com.ikno.itracclient.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Animal;
import com.ikno.dao.business.Asset;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.IPropertyComposite;

public class AnimalProperties extends Group implements IPropertyComposite {

	private Text species;
	private Text identifier;
	
	private AssetPropertyPage control = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimalProperties(Composite parent, int style, AssetPropertyPage control) {
		super(parent, style);
		this.control = control;
		setLayout(new FormLayout());
		setText("Animal Detail");

		final Label identifierLabel = new Label(this, SWT.NONE);
		final FormData fd_identifierLabel = new FormData();
		fd_identifierLabel.top = new FormAttachment(0, 5);
		fd_identifierLabel.bottom = new FormAttachment(0, 25);
		fd_identifierLabel.left = new FormAttachment(0, 4);
		fd_identifierLabel.right = new FormAttachment(0, 65);
		identifierLabel.setLayoutData(fd_identifierLabel);
		identifierLabel.setAlignment(SWT.RIGHT);
		identifierLabel.setText("Identifier");

		identifier = new Text(this, SWT.BORDER);
		final FormData fd_identifier = new FormData();
		fd_identifier.right = new FormAttachment(0, 200);
		fd_identifier.top = new FormAttachment(identifierLabel, -20, SWT.BOTTOM);
		fd_identifier.bottom = new FormAttachment(identifierLabel, 0, SWT.BOTTOM);
		fd_identifier.left = new FormAttachment(identifierLabel, 5, SWT.RIGHT);
		identifier.setLayoutData(fd_identifier);
		identifier.setEditable(false);

		Label speciesLabel;
		speciesLabel = new Label(this, SWT.NONE);
		speciesLabel.setAlignment(SWT.RIGHT);
		final FormData fd_speciesLabel = new FormData();
		fd_speciesLabel.bottom = new FormAttachment(identifierLabel, 24, SWT.BOTTOM);
		fd_speciesLabel.top = new FormAttachment(identifierLabel, 5, SWT.BOTTOM);
		speciesLabel.setLayoutData(fd_speciesLabel);
		speciesLabel.setText("Species");

		species = new Text(this, SWT.BORDER);
		fd_speciesLabel.left = new FormAttachment(species, -66, SWT.LEFT);
		fd_speciesLabel.right = new FormAttachment(species, -5, SWT.LEFT);
		species.setEditable(false);
		final FormData fd_species = new FormData();
		fd_species.top = new FormAttachment(speciesLabel, -19, SWT.BOTTOM);
		fd_species.bottom = new FormAttachment(speciesLabel, 0, SWT.BOTTOM);
		fd_species.right = new FormAttachment(identifier, 350, SWT.LEFT);
		fd_species.left = new FormAttachment(identifier, 0, SWT.LEFT);
		species.setLayoutData(fd_species);
		//
		buildFromObject();
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setEnabled(boolean enabled) {
		identifier.setEnabled(enabled);
		species.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	public void buildFromObject() {
		Animal animal = (Animal)control.getAsset();
		identifier.setText(animal.getIdentifier() == null ? "" : animal.getIdentifier());
		species.setText(animal.getSpecies() == null ? "" : animal.getSpecies());
	}
	public boolean performOk() {
		return true;
	}
}
