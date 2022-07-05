package com.ikno.itracclient.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.ikno.dao.business.Management;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.views.widgets.ObjectSelection;

public class ManagementUserPage extends WizardPage {

	public static final String ID = "com.ikno.itracclient.wizards.ManagementUserPage"; //$NON-NLS-1$
	private ObjectSelection<User> userSelection;
	private Management company;
	
	/**
	 * Create the wizard
	 */
	public ManagementUserPage(Management company, boolean isCreating) {
		super(ID);
		setTitle("Management Company Users");
		setDescription("Select Users that have access to this Company");
		this.company = company;
	}

	public void dispose() {
		userSelection.dispose();
		super.dispose();
	}
	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);

		userSelection = new ObjectSelection<User>(container, SWT.NONE,"UserSelection");
		final FormData fd_userSelection = new FormData();
		fd_userSelection.bottom = new FormAttachment(100, -5);
		fd_userSelection.right = new FormAttachment(100, -5);
		fd_userSelection.top = new FormAttachment(0, 5);
		fd_userSelection.left = new FormAttachment(0, 5);
		userSelection.setLayoutData(fd_userSelection);
		userSelection.setLayout(new FormLayout());
		this.buildFromObject();
	}
	public void buildFromObject() {
		if (company == null)
			return;
		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			userSelection.setSelection(DAO.localDAO().getSystemUsers(), DAO.localDAO().getManagementUsers(company));
		} else {
			userSelection.setSelection(DAO.localDAO().getUsersForUserClients(loggedIn), DAO.localDAO().getManagementUsers(company));
		}
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.CLIENTADMIN) || loggedIn.fullfillsRole(User.Roles.POWERUSER))
			userSelection.setEnabled(true);
	}
	public void populateObject() throws Exception {
		for (User user : userSelection.getAdded()) {
			company.addUser(user);
		}
		for (User user : userSelection.getRemoved()) {
			company.removeUser(user);
		}
	}
	public void setManagement(Management company) {
		this.company = company;
		this.buildFromObject();
	}
}
