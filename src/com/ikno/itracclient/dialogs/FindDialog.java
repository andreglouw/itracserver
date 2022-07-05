package com.ikno.itracclient.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.views.widgets.FindPlaces;

public class FindDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	public boolean isOpen = false;
	private FindPlaces findPlaces;
	private IMappingView mappingView = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public FindDialog(Shell parent, int style, IMappingView mappingView) {
		super(parent, style);
		this.mappingView = mappingView;
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public FindDialog(Shell parent, IMappingView mappingView) {
		this(parent, SWT.NONE,mappingView);
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
			findPlaces.dispose();
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
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setLayout(new FillLayout());
		shell.setSize(500, 375);
		shell.setText("Find Places");

		findPlaces = new FindPlaces(shell, SWT.NONE);
		findPlaces.setMappingView(mappingView);
		//
	}

}
