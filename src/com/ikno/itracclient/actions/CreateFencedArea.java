package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.dialogs.FencedAreaDialog;
import com.ikno.itracclient.resource.ResourceManager;

public class CreateFencedArea extends Action {
	public CreateFencedArea() {
		setText("New Fenced Area");
		setToolTipText("Create a new fenced area");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Rubber Stamp 32 h p8.png"));
	}
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		FencedAreaDialog dialog = new FencedAreaDialog(workbench.getActiveWorkbenchWindow().getShell());
		dialog.open();
	}
}
