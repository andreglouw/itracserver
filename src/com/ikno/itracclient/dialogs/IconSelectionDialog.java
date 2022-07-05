package com.ikno.itracclient.dialogs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.googleearth.GoogleImageAlbum.AlbumEntry;
import com.ikno.itracclient.views.widgets.ImageAlbumCanvas;

public class IconSelectionDialog extends Dialog {
	private static final Logger logger = Logging.getLogger(IconSelectionDialog.class.getName());

	private Text imageURL;
	protected Object result;
	protected Shell shell;
	private ImageAlbumCanvas canvas;
	public WeakReference<AlbumEntry> albumEntry = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public IconSelectionDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public IconSelectionDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			boolean value = false;
			try {
				value = display.readAndDispatch();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception during readAndDispatch processing:",e);
			}
			if (!value)
				display.sleep();
		}
		shell.dispose();
		canvas.dispose();
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 389);
		shell.setText("Choose Icon");

		canvas = new ImageAlbumCanvas(shell, SWT.NONE, this);
		final FormData fd_canvas = new FormData();
		fd_canvas.bottom = new FormAttachment(0, 305);
		fd_canvas.top = new FormAttachment(0, 0);
		fd_canvas.right = new FormAttachment(0, 494);
		fd_canvas.left = new FormAttachment(0, 0);
		canvas.setLayoutData(fd_canvas);

		imageURL = new Text(shell, SWT.BORDER);
		imageURL.setEditable(false);
		final FormData fd_imageURL = new FormData();
		fd_imageURL.left = new FormAttachment(canvas, 0, SWT.LEFT);
		fd_imageURL.bottom = new FormAttachment(canvas, 25, SWT.BOTTOM);
		fd_imageURL.top = new FormAttachment(canvas, 5, SWT.BOTTOM);
		imageURL.setLayoutData(fd_imageURL);

		Button okButton;
		okButton = new Button(shell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				if (albumEntry != null)
					result = albumEntry.get();
				shell.close();
			}
		});
		fd_imageURL.right = new FormAttachment(okButton, 0, SWT.RIGHT);
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(imageURL, 27, SWT.BOTTOM);
		fd_okButton.top = new FormAttachment(imageURL, 0, SWT.BOTTOM);
		fd_okButton.right = new FormAttachment(0, 494);
		fd_okButton.left = new FormAttachment(0, 440);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.right = new FormAttachment(okButton, 0, SWT.LEFT);
		fd_cancelButton.bottom = new FormAttachment(okButton, 0, SWT.BOTTOM);
		fd_cancelButton.top = new FormAttachment(okButton, 0, SWT.TOP);
		fd_cancelButton.left = new FormAttachment(0, 385);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");
		//
	}
	public void albumEntrySelected(AlbumEntry albumEntry) {
		if (imageURL != null) {
			this.albumEntry = new WeakReference<AlbumEntry>(albumEntry);
			this.imageURL.setText(albumEntry.imageURL);
		} else {
			albumEntry = null;
			this.imageURL.setText("");
		}
	}
}
