package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Faction;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.itracclient.resource.ResourceManager;

public class RemoveFaction extends SingleAction {
	private Faction faction = null;
	
	public RemoveFaction() {
		setText("Remove Group");
		setToolTipText("Remove a group of assets");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Delete AssetGroup 32.png"));
	}
	public void run() {
		if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Confirm delete", 
				"Are you sure you want to delete this Group?"))
			return;
		try {
			DAO.localDAO().beginTransaction();
			DAO.localDAO().deleteFaction(faction);
			DAO.localDAO().commitTransaction();
		} catch (Throwable e) {
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Delete Error", "Error deleting Asset Group:\n"+e.getMessage());
			e.printStackTrace();
		}
	}
	public Faction getFaction() {
		return faction;
	}
	public void setFaction(Faction faction) {
		this.faction = faction;
	}
}
