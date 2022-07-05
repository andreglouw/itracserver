package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Management;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.ManagementWizard;

public class CreateCompany extends Action {
	public CreateCompany() {
		setText("New Management Company");
		setToolTipText("Create a new company");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Add Client 32.png"));
	}
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ManagementWizard wizard = new ManagementWizard(new Management(),true);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
	}
}
