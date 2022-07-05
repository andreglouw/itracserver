package com.ikno.itracclient.wizards;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Management;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;

public class ManagementWizard extends Wizard implements INewWizard {
	private static final Logger logger = Logging.getLogger(ManagementWizard.class.getName());
	private boolean finished = false;
	private Management company = null;
	private boolean isCreating;

	public ManagementWizard(Management company, boolean isCreating) {
		this.company = company;
		this.isCreating = isCreating;
		this.finished = false;
	}
	public boolean performFinish() {
		if (finished == true)
			return true;
		finished = true;
		boolean result = true;
		try {
			DAO.localDAO().beginTransaction();
			ManagementPage managementPage = (ManagementPage)this.getPage(ManagementPage.ID);
			managementPage.populateObject();
			ManagementUserPage userPage = (ManagementUserPage)this.getPage(ManagementUserPage.ID);
			userPage.populateObject();
			ManagementClientPage clientPage = (ManagementClientPage)this.getPage(ManagementClientPage.ID);
			clientPage.populateObject();
			DAO.localDAO().save(this.company);
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error saving Management:\n"+e.getMessage());
			result = false;
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Save Error", "Error saving Management.\n" +
						"Please notify System Administrator");
		}
		return result;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public void addPages() {
		ManagementPage managementPage = new ManagementPage(company, isCreating);
		addPage(managementPage);
		ManagementUserPage userPage = new ManagementUserPage(company, isCreating);
		addPage(userPage);
		ManagementClientPage clientPage = new ManagementClientPage(company, isCreating);
		addPage(clientPage);
	}
	public void setManagement(Management company) {
		this.company = company;
		ManagementUserPage userPage = (ManagementUserPage)this.getPage(ManagementUserPage.ID);
		userPage.setManagement(company);
		ManagementClientPage clientPage = (ManagementClientPage)this.getPage(ManagementClientPage.ID);
		clientPage.setManagement(company);
	}
}
