package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Management;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.ManagementWizard;

public class EditCompany extends Action {
	private Management management = null;
	
	public EditCompany() {
		setText("Edit Company");
		setToolTipText("Edit a company");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Edit Client 32.png"));
	}

	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ManagementWizard wizard = new ManagementWizard(management,false);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		int result = dialog.open();
	}
	
	public Management getManagement() {
		return management;
	}

	public void setManagement(Management management) {
		this.management = management;
	}
}
