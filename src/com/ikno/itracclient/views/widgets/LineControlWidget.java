package com.ikno.itracclient.views.widgets;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import itracclient.Activator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.ikno.dao.IChangeListener;
import com.ikno.itracclient.worldwind.LineBuilder;
import com.swtdesigner.ResourceManager;

public class LineControlWidget extends Composite {

	private Button abortButton;
	private Button endButton;
	private Button pauseButton;
	private Button recordButton;
	private LineBuilder lineBuilder;
	private IChangeListener listener = null;
	private Image pauseImage = ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/24x24/Pause 24 n p8.png");
	private Image resumeImage = ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/24x24/Forward or Next 24 n p8.png");
	private Image stopImage = ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/24x24/Green Checkmark 24 h p8.png");
	private Image recordImage = ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/24x24/Green Plus 24 n p8.png");
	private Image abortImage = ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/24x24/Red Delete 24 h p8.png");
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public LineControlWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		pauseButton = new Button(this, SWT.NONE);
		pauseButton.setToolTipText("Pause the line-building to zoom/pan the map");
		final FormData fd_pauseButton = new FormData();
		fd_pauseButton.bottom = new FormAttachment(0, 31);
		fd_pauseButton.top = new FormAttachment(0, 0);
		fd_pauseButton.right = new FormAttachment(0, 37);
		fd_pauseButton.left = new FormAttachment(0, 0);
		pauseButton.setLayoutData(fd_pauseButton);
		pauseButton.setImage(pauseImage);

		endButton = new Button(this, SWT.NONE);
		endButton.setToolTipText("End the line-building session");
		final FormData fd_endButton = new FormData();
		fd_endButton.bottom = new FormAttachment(0, 31);
		fd_endButton.top = new FormAttachment(0, 0);
		fd_endButton.right = new FormAttachment(0, 74);
		fd_endButton.left = new FormAttachment(0, 37);
		endButton.setLayoutData(fd_endButton);
		endButton.setImage(stopImage);

		recordButton = new Button(this, SWT.NONE);
		recordButton.setToolTipText("Press to start building the line");
		final FormData fd_recordButton = new FormData();
		fd_recordButton.bottom = new FormAttachment(0, 31);
		fd_recordButton.top = new FormAttachment(0, 0);
		fd_recordButton.right = new FormAttachment(0, 111);
		fd_recordButton.left = new FormAttachment(0, 74);
		recordButton.setLayoutData(fd_recordButton);
		recordButton.setImage(recordImage);

		abortButton = new Button(this, SWT.NONE);
		abortButton.setToolTipText("Abort the line-building process");
		final FormData fd_abortButton = new FormData();
		fd_abortButton.bottom = new FormAttachment(0, 31);
		fd_abortButton.top = new FormAttachment(0, 0);
		fd_abortButton.right = new FormAttachment(0, 148);
		fd_abortButton.left = new FormAttachment(0, 111);
		abortButton.setLayoutData(fd_abortButton);
		abortButton.setImage(abortImage);
		
		recordButton.setEnabled(true);
		pauseButton.setEnabled(false);
		endButton.setEnabled(false);
		abortButton.setEnabled(false);
		pauseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (lineBuilder.isArmed()) {
	        		pauseButton.setImage(resumeImage);
	                lineBuilder.setArmed(false);
	                lineBuilder.disableCrossHair();
				} else {
	        		pauseButton.setImage(pauseImage);
	                lineBuilder.setArmed(true);
	                lineBuilder.enableCrossHair();
				}
			}
		});
		endButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
                lineBuilder.setArmed(false);
                lineBuilder.disableCrossHair();
                recordButton.setEnabled(true);
        		pauseButton.setEnabled(false);
        		endButton.setEnabled(false);
        		abortButton.setEnabled(false);
        		listener.objectChanged(lineBuilder);
			}
		});
		recordButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
                lineBuilder.clear();
                lineBuilder.setArmed(true);
                lineBuilder.enableCrossHair();
        		pauseButton.setImage(pauseImage);
                pauseButton.setEnabled(true);
                endButton.setEnabled(true);
        		abortButton.setEnabled(true);
                recordButton.setEnabled(false);
			}
		});
		abortButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
                lineBuilder.clear();
                lineBuilder.setArmed(false);
                lineBuilder.disableCrossHair();
                recordButton.setEnabled(true);
        		pauseButton.setEnabled(false);
        		endButton.setEnabled(false);
        		abortButton.setEnabled(false);
        		listener.objectChanged(null);
			}
		});
	}

	public void setEnabled(boolean enabled) {
		recordButton.setEnabled(enabled);
	}
	
	public void startBuilder(IChangeListener listener, WorldWindow wwd, RenderableLayer lineLayer, Polyline polyline) {
		this.lineBuilder = new LineBuilder(wwd,lineLayer,polyline);
		this.listener = listener;
		recordButton.setEnabled(true);
		pauseButton.setEnabled(false);
		endButton.setEnabled(false);
		abortButton.setEnabled(false);
	}
	public void endBuilder() {
		if (this.lineBuilder != null) {
			this.lineBuilder.clear();
			this.lineBuilder.setArmed(false);
			this.lineBuilder.disableCrossHair();
		}
		recordButton.setEnabled(true);
		pauseButton.setEnabled(false);
		endButton.setEnabled(false);
		abortButton.setEnabled(false);
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
