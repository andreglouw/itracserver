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
import com.ikno.dao.business.Animal;
import com.ikno.itracclient.IObjectEditor;

public class AnimalDetail extends Group implements IObjectEditor, FocusListener {

	private Text species;
	IChangeListener listener;

	private Animal animal = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimalDetail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		setText("Animal Detail");

		Label speciesLabel;
		speciesLabel = new Label(this, SWT.NONE);
		speciesLabel.setAlignment(SWT.RIGHT);
		final FormData fd_speciesLabel = new FormData();
		fd_speciesLabel.bottom = new FormAttachment(0, 24);
		fd_speciesLabel.top = new FormAttachment(0, 5);
		speciesLabel.setLayoutData(fd_speciesLabel);
		speciesLabel.setText("Species");

		species = new Text(this, SWT.BORDER);
		fd_speciesLabel.left = new FormAttachment(species, -66, SWT.LEFT);
		fd_speciesLabel.right = new FormAttachment(species, -5, SWT.LEFT);
		final FormData fd_species = new FormData();
		fd_species.right = new FormAttachment(0, 390);
		fd_species.left = new FormAttachment(0, 73);
		fd_species.top = new FormAttachment(speciesLabel, -19, SWT.BOTTOM);
		fd_species.bottom = new FormAttachment(speciesLabel, 0, SWT.BOTTOM);
		species.setLayoutData(fd_species);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setObject(Object object) {
		this.animal = (Animal)object;
		species.setText(animal.getSpecies() == null ? "" : animal.getSpecies());
	}
	public String validate() {
		return null;
	}
	public void populateObject() {
		animal.setSpecies(species.getText());
	}
	public void setChangeListener(IChangeListener listener) {
		this.listener = listener;
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		if (listener != null)
			listener.objectChanged(animal);
	}
}
