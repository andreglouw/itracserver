package com.ikno.itracclient.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.Management;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Configuration;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.views.widgets.ObjectSelection;

public class ManagementClientPage extends WizardPage {

	private ObjectSelection<Client> contractSelection;
	public static final String ID = "com.ikno.itracclient.wizards.ManagementClientPage"; //$NON-NLS-1$
	private Management company;
	private boolean isCreating;

	/**
	 * Create the wizard
	 */
	public ManagementClientPage(Management company, boolean isCreating) {
		super(ID);
		setTitle("Management Company Contracts");
		setDescription("Select Contracts managed by this Company");
		this.company = company;
		this.isCreating = isCreating;
	}

	public void dispose() {
		contractSelection.dispose();
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

		contractSelection = new ObjectSelection<Client>(container, SWT.NONE,"ClientSelection");
		final FormData fd_contractSelection = new FormData();
		fd_contractSelection.bottom = new FormAttachment(100, -5);
		fd_contractSelection.right = new FormAttachment(100, -5);
		fd_contractSelection.top = new FormAttachment(0, 5);
		fd_contractSelection.left = new FormAttachment(0, 5);
		contractSelection.setLayoutData(fd_contractSelection);
		contractSelection.setLayout(new FormLayout());
		this.buildFromObject();
	}
	public void buildFromObject() {
		if (company == null)
			return;
		User loggedIn = TracController.getLoggedIn();
		List<Client> possibles = null;
		List<Client> selected = DAO.localDAO().getManagementClients(company);
		if (selected == null)
			selected = new ArrayList<Client>();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN))
			possibles = DAO.localDAO().getUnmanagedClients(Configuration.configCenter().getString("dataScoutId"));
		else
			possibles = DAO.localDAO().getUnmanagedClientsForUser(loggedIn);
		if (!isCreating) {
			for (Client client : selected)
				possibles.add(client);
		}
		contractSelection.setSelection(possibles, selected);
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.CLIENTADMIN) || loggedIn.fullfillsRole(User.Roles.POWERUSER))
			contractSelection.setEnabled(true);
	}
	public void populateObject() throws Exception {
		for (Client client : contractSelection.getAdded()) {
			company.addClient(client);
		}
		for (Client client : contractSelection.getRemoved()) {
			company.removeClient(client);
		}
	}
	public void setManagement(Management company) {
		this.company = company;
		this.buildFromObject();
	}
}
