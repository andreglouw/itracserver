package com.ikno.itracclient.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;

public class RemoveAsset extends SingleAction {
	private Asset asset;

	public RemoveAsset() {
		setText("Remove Asset");
		setToolTipText("Remove an asset");
	}
	public void run() {
		if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Confirm delete", 
				"Are you sure you want to delete this Asset?\nPressing OK will permanently remove the asset from the system!"))
			return;
		try {
			DAO.localDAO().beginTransaction();
			DAO.localDAO().deleteAsset(asset);
			DAO.localDAO().commitTransaction();
		} catch (Throwable e) {
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Delete Error", "Error deleting Asset:\n"+e.getMessage());
			e.printStackTrace();
		}
	}

	public Asset getAsset() {
		return asset;
	}
	public void setAsset(Asset asset) {
		this.asset = asset;
	}
}
