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
import com.ikno.dao.business.Person;
import com.ikno.itracclient.IObjectEditor;

public class PersonDetail extends Group implements IObjectEditor, FocusListener {
	private Text lastName;
	private Text firstName;
	IChangeListener listener = null;
	
	Person person = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public PersonDetail(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		setText("Person Details");

		final Label firstNameLabel = new Label(this, SWT.NONE);
		final FormData fd_firstNameLabel = new FormData();
		fd_firstNameLabel.bottom = new FormAttachment(0, 25);
		fd_firstNameLabel.top = new FormAttachment(0, 5);
		fd_firstNameLabel.right = new FormAttachment(0, 75);
		fd_firstNameLabel.left = new FormAttachment(0, 20);
		firstNameLabel.setLayoutData(fd_firstNameLabel);
		firstNameLabel.setAlignment(SWT.RIGHT);
		firstNameLabel.setText("First Name");

		firstName = new Text(this, SWT.BORDER);
		final FormData fd_firstName = new FormData();
		fd_firstName.right = new FormAttachment(0, 245);
		fd_firstName.bottom = new FormAttachment(0, 24);
		fd_firstName.top = new FormAttachment(0, 5);
		fd_firstName.left = new FormAttachment(0, 80);
		firstName.setLayoutData(fd_firstName);

		Label lastNameLabel;
		lastNameLabel = new Label(this, SWT.NONE);
		lastNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_lastNameLabel = new FormData();
		fd_lastNameLabel.top = new FormAttachment(firstNameLabel, 5, SWT.BOTTOM);
		lastNameLabel.setLayoutData(fd_lastNameLabel);
		lastNameLabel.setText("Last Name");

		lastName = new Text(this, SWT.BORDER);
		fd_lastNameLabel.bottom = new FormAttachment(lastName, 0, SWT.BOTTOM);
		fd_lastNameLabel.left = new FormAttachment(lastName, -60, SWT.LEFT);
		fd_lastNameLabel.right = new FormAttachment(lastName, -5, SWT.LEFT);
		final FormData fd_lastName = new FormData();
		fd_lastName.right = new FormAttachment(0, 390);
		fd_lastName.top = new FormAttachment(0, 30);
		fd_lastName.bottom = new FormAttachment(0, 50);
		fd_lastName.left = new FormAttachment(firstNameLabel, 5, SWT.RIGHT);
		lastName.setLayoutData(fd_lastName);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public void setObject(Object object) {
		this.person = (Person)object;
		firstName.setText(person.getFirstName() == null ? "" : person.getFirstName());
		lastName.setText(person.getLastName() == null ? "" : person.getLastName());
	}
	public String validate() {
		return null;
	}
	public void populateObject() {
		person.setFirstName(firstName.getText());
		person.setLastName(lastName.getText());
	}
	public void setChangeListener(IChangeListener listener) {
		this.listener = listener;
	}
	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		if (listener != null)
			listener.objectChanged(person);
	}
}
