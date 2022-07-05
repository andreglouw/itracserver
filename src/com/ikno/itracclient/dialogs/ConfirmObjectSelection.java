package com.ikno.itracclient.dialogs;

import java.util.List;

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

import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.views.widgets.ObjectSelection;

public class ConfirmObjectSelection<T> extends Dialog {

	private ObjectSelection<T> objectSelection;
	protected List<T> result;
	private boolean changed = false;
	private String qualifier; 
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public ConfirmObjectSelection(Shell parent, int style, String qualifier) {
		super(parent, style);
		this.qualifier = qualifier;
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public ConfirmObjectSelection(Shell parent, String qualifier) {
		this(parent, SWT.NONE, qualifier);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public List<T> open(List<T> available) {
		return open(available,null);
	}
	public List<T> open(List<T> available, List<T> selected) {
		changed = false;
		createContents();
		objectSelection.setSelection(available,selected);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 375);
		shell.setText("User Selection");

		objectSelection = new ObjectSelection<T>(shell, SWT.NONE, this.qualifier);
		final FormData fd_userSelection = new FormData();
		fd_userSelection.bottom = new FormAttachment(100, -33);
		fd_userSelection.top = new FormAttachment(0, 5);
		fd_userSelection.right = new FormAttachment(100, -5);
		fd_userSelection.left = new FormAttachment(0, 5);
		objectSelection.setLayoutData(fd_userSelection);
		objectSelection.setLayout(new FormLayout());

		final Button okButton = new Button(shell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = objectSelection.getSelected();
				changed = true;
				shell.close();
			}
		});
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -5);
		fd_okButton.top = new FormAttachment(100, -31);
		fd_okButton.right = new FormAttachment(100, -5);
		fd_okButton.left = new FormAttachment(100, -54);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				changed = false;
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(100, -5);
		fd_cancelButton.top = new FormAttachment(100, -31);
		fd_cancelButton.right = new FormAttachment(100, -54);
		fd_cancelButton.left = new FormAttachment(100, -99);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");
		//
	}
	public List<T> getSelected() {
		return objectSelection.getSelected();
	}
	public List<T> getAdded() {
		return objectSelection.getAdded();
	}
	public List<T> getRemoved() {
		return objectSelection.getRemoved();
	}
}
