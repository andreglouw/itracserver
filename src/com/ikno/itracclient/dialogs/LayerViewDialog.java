package com.ikno.itracclient.dialogs;

import java.lang.ref.WeakReference;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.ClientMapLayer;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.views.widgets.LayerWidget;
import com.ikno.itracclient.wizards.MapLayerPage;
import com.ikno.itracclient.wizards.MapLayerWizard;

public class LayerViewDialog extends Dialog {
	private Button newButton;
	protected Object result;
	protected Shell shell;
	public boolean isOpen = false;
	private LayerWidget layerWidget = null;
	private WeakReference<IMappingView> mappingView = null;
	private KMLLayer selectedLayer = null;
	private Button deleteButton;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public LayerViewDialog(Shell parent, int style, IMappingView mappingView) {
		super(parent, style);
		this.mappingView = new WeakReference<IMappingView>(mappingView);
	}

	/**
	 * Create the dialog
	 * @param parent
	 * @wbp.parser.constructor
	 */
	public LayerViewDialog(Shell parent, IMappingView mappingView) {
		this(parent, SWT.NONE, mappingView);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		try {
			isOpen = true;
			createContents();
			shell.open();
			shell.layout();
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			layerWidget.dispose();
			shell.dispose();
		} finally {
			isOpen = false;
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
		shell.setSize(501, 365);
		shell.setText("Layer Viewer");
		shell.setLayout(null);

		layerWidget = new LayerWidget(shell, SWT.NONE);
		layerWidget.setBounds(0, 0, 492, 285);
		layerWidget.setMappingView(mappingView.get());
		layerWidget.setDialog(this);

		newButton = new Button(shell, SWT.NONE);
		newButton.setBounds(428, 291, 47, 35);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				KMLLayer mapLayer = new KMLLayer();
				newButton.setEnabled(false);
				MapLayerWizard wizard = new MapLayerWizard(mapLayer);
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(),wizard);
				dialog.open();
				if (dialog.getReturnCode() == Window.OK) {
					mappingView.get().addLayer((MapLayer)mapLayer);
				}
				newButton.setEnabled(true);
			}
		});
		newButton.setText("New");
		
		deleteButton = new Button(shell, SWT.NONE);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					DAO.localDAO().beginTransaction();
					DAO.localDAO().delete(selectedLayer);
					DAO.localDAO().commitTransaction();
					mappingView.get().removeLayer((MapLayer)selectedLayer);
					layerWidget.refreshLayers();
				} catch (Exception exc) {
					DAO.localDAO().rollbackTransaction();
				}
			}
		});
		deleteButton.setBounds(375, 291, 47, 35);
		deleteButton.setText("Delete");
		deleteButton.setEnabled(false);
		//
	}
	public void layerSelectionChanged(KMLLayer selected) {
		this.selectedLayer = selected;
		if (!selected.isShared())
			deleteButton.setEnabled(true);
		else
			deleteButton.setEnabled(false);
	}
}
