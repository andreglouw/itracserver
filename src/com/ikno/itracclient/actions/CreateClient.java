package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.EditClientWizard;

public class CreateClient extends Action {
	
	public CreateClient() {
		setText("New Contract");
		setToolTipText("Create a new contract");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Add Contract 32.png"));
	}
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		EditClientWizard wizard = new EditClientWizard(true);
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
	}
}
