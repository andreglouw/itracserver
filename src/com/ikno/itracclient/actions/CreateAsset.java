package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.wizards.AddAssetWizard;

public class CreateAsset extends Action {

	public CreateAsset() {
		setText("New Asset");
		setToolTipText("Create a new asset");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Add Asset 32.png"));
	}
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		AddAssetWizard wizard = new AddAssetWizard();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
		dialog.open();
	}
}
