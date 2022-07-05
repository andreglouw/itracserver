package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Faction;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.EditUserWizard;

public class CreateUser extends SingleAction {

	public CreateUser() {
		setText("New User");
		setToolTipText("Create a new system user");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Add User 3 32 n p8.png"));
	}
	public void run() {
		User user = new User("","");
		IWorkbench workbench = PlatformUI.getWorkbench();
		EditUserWizard wizard = new EditUserWizard(user,true,false);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
		if (dialog.getReturnCode() != WizardDialog.CANCEL) {
			try {
				DAO.localDAO().beginTransaction();
				DAO.localDAO().saveOrUpdate(user);
				DAO.localDAO().commitTransaction();
			} catch (Exception e) {
				DAO.localDAO().rollbackTransaction();
				e.printStackTrace();
			}
		}
	}
}
