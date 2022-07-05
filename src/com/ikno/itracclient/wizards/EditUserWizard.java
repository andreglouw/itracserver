package com.ikno.itracclient.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikno.dao.business.Owner;
import com.ikno.dao.business.Recipient;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserClient;
import com.ikno.dao.business.UserRecipient;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;

public class EditUserWizard extends Wizard implements INewWizard {

	private boolean finished = false;
	private User user = null;
	private boolean isCreating;
	private boolean isClientUser;
	
	public EditUserWizard(User user, boolean isCreating, boolean isClientUser) {
		this.user = user;
		this.isCreating = isCreating;
		this.isClientUser = isClientUser;
	}
	public boolean performFinish() {
		if (finished == true)
			return true;
		finished = false;
		try {
			DAO.localDAO().beginTransaction();
			EditUserPage userPage = (EditUserPage)this.getPage(EditUserPage.ID);
			userPage.populateObject();
			EditUserAssetPage assetPage = (EditUserAssetPage)this.getPage(EditUserAssetPage.ID);
			if (assetPage != null)
				assetPage.populateObject();
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
			e.printStackTrace();
		}
		return true;
	}
	public void setUser(User user) {
		EditUserPage userPage = (EditUserPage)this.getPage(EditUserPage.ID);
		userPage.setUser(user);
		EditUserAssetPage assetPage = (EditUserAssetPage)this.getPage(EditUserAssetPage.ID);
		if (assetPage != null)
			assetPage.setUser(user);
	}
	public boolean performCancel() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

	public void addPages() {
		EditUserPage userPage = new EditUserPage(user,isCreating,isClientUser);
		addPage(userPage);
		EditUserAssetPage assetPage = new EditUserAssetPage(user,isCreating,isClientUser);
		addPage(assetPage);
	}
}
