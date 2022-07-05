package com.ikno.itracclient.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserClient;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;

public class EditClientWizard extends Wizard implements INewWizard {
	private boolean finished = false;
	private Client client = null;
	private User user = null;
	public boolean canFinish = false;
	private boolean isCreating;
	
	public EditClientWizard(boolean isCreating) {
		this(new Client(),new User(),isCreating);
	}
	public EditClientWizard(Client client, User user, boolean isCreating) {
		this.client = client;
		this.user = user;
		this.isCreating = isCreating;
	}
	public boolean performFinish() {
		if (finished == true)
			return true;
		finished = false;
		try {
			DAO.localDAO().beginTransaction();
			EditClientPage clientPage = (EditClientPage)this.getPage(EditClientPage.ID);
			if (clientPage.performFinish(user)) {
				DAO.localDAO().commitTransaction();
			}
			/*
			Client client = clientPage.getClient();
			DAO.localDAO().save(client);
			if (user != null)
				DAO.localDAO().save(user);
			*/
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
			e.printStackTrace();
		}
		return true;
	}
	public boolean performCancel() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	public boolean canFinish() {
		return canFinish;
	}
	public void setCanFinish(boolean value) {
		this.canFinish = value;
	}
	public void addPages() {
		EditClientPage clientPage = new EditClientPage(client,isCreating); 
		addPage(clientPage);
	}
}
