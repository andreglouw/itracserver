package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import itracclient.Activator;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ikno.dao.IChangeListener;
import com.ikno.itracclient.views.widgets.LineControlWidget;

import com.ikno.itracclient.worldwind.LineBuilder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class MeasurementPanel extends Dialog implements IChangeListener {

	private Label result;
	private LineControlWidget lineControlWidget;
	public boolean isOpen = false;
	private WorldWindow worldWindow = null;
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public MeasurementPanel(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public MeasurementPanel(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public void open(final WorldWindow wwd, RenderableLayer lineLayer, Polyline polyline) {
		try {
			isOpen = true;
			createContents();
			shell.open();
			shell.layout();
			lineControlWidget.startBuilder(this, wwd, lineLayer, polyline);
			this.worldWindow = wwd;
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			lineControlWidget.endBuilder();
			lineControlWidget.dispose();
			shell.dispose();
		} finally {
			isOpen = false;
		}
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(final ShellEvent e) {
				lineControlWidget.endBuilder();
			}
		});
		shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		shell.setLayout(new FormLayout());
		shell.setSize(135, 94);
		shell.setText("Measure");

		lineControlWidget = new LineControlWidget(shell, SWT.NONE);
		final FormData fd_lineControlWidget = new FormData();
		lineControlWidget.setLayoutData(fd_lineControlWidget);
		lineControlWidget.setLayout(new FormLayout());

		result = new Label(shell, SWT.SHADOW_IN | SWT.BORDER);
		result.setFont(SWTResourceManager.getFont("", 12, SWT.NONE));
		fd_lineControlWidget.top = new FormAttachment(result, -35, SWT.TOP);
		fd_lineControlWidget.bottom = new FormAttachment(result, -5, SWT.TOP);
		fd_lineControlWidget.left = new FormAttachment(result, -110, SWT.RIGHT);
		fd_lineControlWidget.right = new FormAttachment(result, 0, SWT.RIGHT);
		result.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_result = new FormData();
		fd_result.bottom = new FormAttachment(0, 60);
		fd_result.top = new FormAttachment(0, 36);
		fd_result.right = new FormAttachment(0, 120);
		fd_result.left = new FormAttachment(0, 10);
		result.setLayoutData(fd_result);
	}

	public void objectChanged(Object object) {
		LineBuilder lineBuilder = (LineBuilder)object;
		Polyline line = lineBuilder.getLine();
		Globe globe = worldWindow.getModel().getGlobe();
		double distance = 0.0;
		Position previous = null;
		for (Position pos : line.getPositions()) {
			if (previous != null) {
		        LatLon llA = new LatLon(previous.getLatitude(), previous.getLongitude());
		        LatLon llB = new LatLon(pos.getLatitude(), pos.getLongitude());

		        /* Version 0.4.0
		        Angle ang = LatLon.sphericalDistance(llA, llB);
		        */
		        Angle ang = LatLon.greatCircleDistance(llA, llB);
				distance += ang.radians*globe.getRadius();
			}
			previous = pos;
		}
		if (distance > 1000.0) {
			result.setText(String.format("%.2f km", (distance / 1000.0)));
		} else {
			result.setText(String.format("%.2f m", distance));
		}
	}
}
