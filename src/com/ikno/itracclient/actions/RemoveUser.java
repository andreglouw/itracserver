package com.ikno.itracclient.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;

public class RemoveUser extends SingleAction {

	private User user = null;
	
	public RemoveUser() {
		setText("Remove User");
		setToolTipText("Remove a system user");
	}

	public void run() {
		if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Confirm delete", 
				"Are you sure you want to delete this User?"))
			return;
		try {
			DAO.localDAO().beginTransaction();
			DAO.localDAO().delete(user);
			DAO.localDAO().commitTransaction();
		} catch (Throwable e) {
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Delete Error", "Error deleting User:\n"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
