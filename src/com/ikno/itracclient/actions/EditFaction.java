package com.ikno.itracclient.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.wizards.EditFactionWizard;

public class EditFaction extends Action implements IActionDelegate, IObjectActionDelegate {

	private Faction faction = null;
	
	public EditFaction() {
		setText("Edit Group");
		setToolTipText("Edit a group of assets");
	}
	public void run() {
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
		EditFactionWizard wizard = new EditFactionWizard(faction,false,assets);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
	}
	public Faction getFaction() {
		return faction;
	}
	public void setFaction(Faction faction) {
		this.faction = faction;
	}
	public void run(IAction action) {
		run();
	}
	public void selectionChanged(IAction action, ISelection selection) {
		Object element = ((IStructuredSelection)selection).getFirstElement();
		if (element != null) {
			if (PersistantObject.instanceOf(element, Faction.class)) {
				setFaction((Faction)element);
			}
		}
	}
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		System.out.println("IWorkbenchPart set to "+targetPart);
	}
}
