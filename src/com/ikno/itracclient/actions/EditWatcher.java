package com.ikno.itracclient.actions;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.dialogs.WatcherDialog;
import com.ikno.itracclient.resource.ResourceManager;

public class EditWatcher extends Action {

	public EditWatcher() {
		setText("Edit Watchers");
		setToolTipText("Edit Watchers on Asset/User");
		setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Asset Watcher 32.png"));
	}
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		WatcherDialog dialog = new WatcherDialog(workbench.getActiveWorkbenchWindow().getShell());
		dialog.open();
	}
}
