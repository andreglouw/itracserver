package com.ikno.itracclient.dialogs;

import java.lang.ref.WeakReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.views.widgets.TrackWidget;

public class TrackViewDialog extends Dialog {

	private TrackWidget trackWidget;
	protected Object result;
	public boolean isOpen = false;
	protected Shell shell;
	private WeakReference<IMappingView> mappingView = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public TrackViewDialog(Shell parent, int style, IMappingView mappingView) {
		super(parent, style);
		this.mappingView = new WeakReference<IMappingView>(mappingView);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public TrackViewDialog(Shell parent, IMappingView mappingView) {
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
			trackWidget.dispose();
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
		shell = new Shell(getParent(), SWT.TITLE | SWT.RESIZE | SWT.CLOSE);
		shell.setLayout(new FillLayout());
		shell.setSize(500, 355);
		shell.setText("Track Viewer");

		trackWidget = new TrackWidget(shell, SWT.NONE);
		trackWidget.setMappingView(mappingView.get(),false);
		//
	}
}
