package com.ikno.itracclient.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.properties.GeneralProperties;

public class EditPreferences extends Action {

	public EditPreferences() {
		setText("Preferences");
		setToolTipText("Edit general preferences");
	}
	public void run() {
		GeneralProperties dialog = new GeneralProperties(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.open();
	}
}
