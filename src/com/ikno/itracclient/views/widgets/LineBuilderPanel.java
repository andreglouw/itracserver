package com.ikno.itracclient.views.widgets;

import java.util.ArrayList;
import java.util.List;

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
import com.ikno.dao.business.PolyLine;
import com.ikno.itracclient.views.widgets.LineControlWidget;

import com.ikno.itracclient.worldwind.LineBuilder;
import com.swtdesigner.ResourceManager;

public class LineBuilderPanel extends Dialog implements IChangeListener {

	private LineControlWidget lineControlWidget;
	private WorldWindow worldWindow = null;
	protected Shell shell;
	public PolyLine[] result = null;
	private IChangeListener listener;
	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public LineBuilderPanel(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public LineBuilderPanel(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public void open(final WorldWindow wwd, RenderableLayer lineLayer, Polyline polyline, IChangeListener listener) {
		this.listener = listener;
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
	}

	public void close() {
		lineControlWidget.endBuilder();
		lineControlWidget.dispose();
		shell.dispose();
	}
	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE |SWT.PRIMARY_MODAL);
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(final ShellEvent e) {
				lineControlWidget.endBuilder();
				listener.objectChanged(null);
			}
		});
		shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		shell.setLayout(new FormLayout());
		shell.setSize(155, 94);
		shell.setText("Draw");

		lineControlWidget = new LineControlWidget(shell, SWT.NONE);
		final FormData fd_lineControlWidget = new FormData();
		lineControlWidget.setLayoutData(fd_lineControlWidget);
		lineControlWidget.setLayout(new FormLayout());
	}

	public void objectChanged(Object object) {
		LineBuilder lineBuilder = (LineBuilder)object;
		if (lineBuilder != null && lineBuilder.isClosed()) {
			Polyline line = lineBuilder.getLine();
			Position previous = null;
			List<PolyLine> polyLines = new ArrayList<PolyLine>();
			for (Position pos : line.getPositions()) {
				if (previous != null) {
					polyLines.add(new PolyLine((float)previous.getLatitude().getDegrees(),(float)previous.getLongitude().getDegrees(),
							(float)pos.getLatitude().getDegrees(), (float)pos.getLongitude().getDegrees()));
				}
				previous = pos;
			}
			this.result = polyLines.toArray(new PolyLine[]{});
		}
		this.close();
		this.listener.objectChanged(object);
	}
}
