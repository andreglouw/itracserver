package com.ikno.itracclient.dialogs;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ikno.itracclient.falconview.FalconView;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

public class MapSelectionDialog extends Dialog {

	protected Object result;
	protected Shell shlChooseMapType;
	private String[] available;
	public boolean isOpen = false;
	private Combo mapType;
	private Button okButton;
	private Button cancelButton;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public MapSelectionDialog(Shell parent, int style, String[] available) {
		super(parent, style);
		setText("SWT Dialog");
		this.available = available;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		try {
			isOpen = true;
			createContents();
			shlChooseMapType.open();
			shlChooseMapType.layout();
			Display display = getParent().getDisplay();
			while (!shlChooseMapType.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} finally {
			isOpen = false;
			shlChooseMapType.dispose();
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlChooseMapType = new Shell(getParent(), getStyle());
		shlChooseMapType.setSize(450, 124);
		shlChooseMapType.setText("Choose Map Type");
		
		Label lblTypeOfMap = new Label(shlChooseMapType, SWT.NONE);
		lblTypeOfMap.setAlignment(SWT.RIGHT);
		lblTypeOfMap.setBounds(38, 26, 88, 15);
		lblTypeOfMap.setText("Type Of Map");
		
		mapType = new Combo(shlChooseMapType, SWT.NONE);
		mapType.setBounds(132, 23, 187, 23);

		mapType.setItems(this.available);
		
		okButton = new Button(shlChooseMapType, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String viewID = null;
				String mapping = mapType.getText();
				int maxViews = 4;
				if (mapping.equals("WorldWind"))
					viewID = ActiveWorldWindView.ID;
				else if (mapping.equals("Google Earth"))
					viewID = GoogleEarth.ID;
				else if (mapping.equals("Falcon View")) {
					viewID = FalconView.ID;
					maxViews = 1;
				}
				// First check if the default Google View has been closed. If so open that
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID) == null) {
					try {
						IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
					} catch (PartInitException e) {}
					return;
				}
				int index = 1;
				String altViewId = String.format(viewID+"_alt_%d", index);
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(viewID, altViewId) != null) {
					altViewId = String.format(viewID+"_alt_%d", ++index);
				}
				if (index > maxViews)
					return;
				try {
					IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID, altViewId, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					
				}
				shlChooseMapType.close();
			}
		});
		okButton.setBounds(359, 61, 75, 25);
		okButton.setText("Create");
		
		cancelButton = new Button(shlChooseMapType, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shlChooseMapType.close();
			}
		});
		cancelButton.setBounds(278, 61, 75, 25);
		cancelButton.setText("Cancel");
	}
	
}
