package com.ikno.itracclient.actions;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.EditFactionWizard;

public class CreateFaction extends Action {

	public CreateFaction() {
		setText("New Group");
		setToolTipText("Create a new group of assets");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Add AssetGroup 32.png"));
	}
	public void run() {
		Faction faction = new Faction("Undefined",TracController.getLoggedIn());
		/*
		List<Asset> assets = new ArrayList<Asset>();
		for (Iterator<Client> ci = TracController.getClients().iterator();ci.hasNext();) {
			Client client = ci.next();
			assets.addAll(client.getAssets());
		}
		Collections.sort(assets);
		*/
		List<AssetWrapper> assets = DAO.localDAO().getAssetWrappersForUser(TracController.getLoggedIn(),false);
		IWorkbench workbench = PlatformUI.getWorkbench();
		EditFactionWizard wizard = new EditFactionWizard(faction,true,assets);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
	}
}
