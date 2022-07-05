package com.ikno.itracclient.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;

public class EditFactionWizard extends Wizard implements INewWizard {

	public static final String ID = "com.ikno.itracclient.wizards.EditFactionWizard";
	private boolean isCreating;
	private Faction faction;
	private java.util.List<AssetWrapper> assets;
	private boolean finished = false;

	public EditFactionWizard(Faction faction, boolean isCreating, java.util.List<AssetWrapper> assets) {
		this.faction = faction;
		this.isCreating = isCreating;
		this.assets = assets;
	}
	public boolean performFinish() {
		if (finished)
			return true;
		finished = true;
		try {
			DAO.localDAO().beginTransaction();
			EditFactionPage page = (EditFactionPage)getPage(EditFactionPage.ID);
			page.populateFaction(faction);
			DAO.localDAO().saveOrUpdate(faction);
			DAO.localDAO().commitTransaction();
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
	}

	public void addPages() {
		EditFactionPage page = new EditFactionPage(faction,isCreating,assets);
		addPage(page);
	}
}
