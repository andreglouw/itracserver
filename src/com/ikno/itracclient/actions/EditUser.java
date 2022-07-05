package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.EditUserAssetPage;
import com.ikno.itracclient.wizards.EditUserPage;
import com.ikno.itracclient.wizards.EditUserWizard;

public class EditUser extends Action {

	private User user = null;
	
	public EditUser() {
		setText("Edit User");
		setToolTipText("Edit a system user");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Edit User 32.png"));
	}

	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		EditUserWizard wizard = new EditUserWizard(user,false,user.fullfillsRole(User.Roles.CLIENT));
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		int result = dialog.open();
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
