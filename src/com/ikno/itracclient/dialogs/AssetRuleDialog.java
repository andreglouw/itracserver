package com.ikno.itracclient.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ikno.dao.business.Asset;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.views.widgets.AssetRuleWidget;

public class AssetRuleDialog extends Dialog {

	private AssetRuleWidget assetRuleWidget;
	private Asset asset = null;
	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public AssetRuleDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public AssetRuleDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(Asset asset) {
		createContents();
		this.asset = asset;
		assetRuleWidget.setAsset(asset);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		assetRuleWidget = null;
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE);
		shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		shell.setLayout(new FormLayout());
		shell.setSize(500, 364);
		shell.setText("Asset Rules");

		assetRuleWidget = new AssetRuleWidget(shell, SWT.NONE);
		final FormData fd_assetRuleWidget = new FormData();
		fd_assetRuleWidget.bottom = new FormAttachment(0, 295);
		fd_assetRuleWidget.top = new FormAttachment(0, 5);
		fd_assetRuleWidget.right = new FormAttachment(100, -5);
		fd_assetRuleWidget.left = new FormAttachment(0, 5);
		assetRuleWidget.setLayoutData(fd_assetRuleWidget);
		assetRuleWidget.setLayout(new FormLayout());

		final Button okButton = new Button(shell, SWT.NONE);
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(assetRuleWidget, 32, SWT.BOTTOM);
		fd_okButton.top = new FormAttachment(assetRuleWidget, 5, SWT.BOTTOM);
		fd_okButton.right = new FormAttachment(0, 485);
		fd_okButton.left = new FormAttachment(0, 435);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("Update");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (asset == null)
					return;
				try {
					DAO.localDAO().beginTransaction();
					assetRuleWidget.applyChanges();
					DAO.localDAO().commitTransaction();
				} catch (Exception exc) {
					DAO.localDAO().rollbackTransaction();
				}
				AssetRuleDialog.this.shell.dispose();
			}
		});

		final Button cancelButton = new Button(shell, SWT.NONE);
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(okButton, 0, SWT.BOTTOM);
		fd_cancelButton.right = new FormAttachment(okButton, -5, SWT.LEFT);
		fd_cancelButton.top = new FormAttachment(okButton, 0, SWT.TOP);
		fd_cancelButton.left = new FormAttachment(0, 385);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (asset == null)
					return;
				try {
					DAO.localDAO().refresh(asset);
				} catch (Exception exc) {
					
				}
				shell.dispose();
			}
		});
		//
	}
}
