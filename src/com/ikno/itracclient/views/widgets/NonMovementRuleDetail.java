package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NonMovementRuleDetail extends Composite {

	private Text resetAfter;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public NonMovementRuleDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());

		final Label activeFromLabel = new Label(this, SWT.NONE);
		activeFromLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		activeFromLabel.setAlignment(SWT.RIGHT);
		final FormData fd_activeFromLabel = new FormData();
		fd_activeFromLabel.bottom = new FormAttachment(0, 25);
		fd_activeFromLabel.right = new FormAttachment(0, 90);
		fd_activeFromLabel.top = new FormAttachment(0, 5);
		fd_activeFromLabel.left = new FormAttachment(0, 5);
		activeFromLabel.setLayoutData(fd_activeFromLabel);
		activeFromLabel.setText("Active From");

		final DateTime activeFrom = new DateTime(this, SWT.TIME);
		final FormData fd_activeFrom = new FormData();
		fd_activeFrom.bottom = new FormAttachment(activeFromLabel, 0, SWT.BOTTOM);
		fd_activeFrom.right = new FormAttachment(0, 190);
		fd_activeFrom.top = new FormAttachment(activeFromLabel, 0, SWT.TOP);
		fd_activeFrom.left = new FormAttachment(activeFromLabel, 5, SWT.RIGHT);
		activeFrom.setLayoutData(fd_activeFrom);

		final Label activeToLabel = new Label(this, SWT.NONE);
		activeToLabel.setAlignment(SWT.CENTER);
		activeToLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_activeToLabel = new FormData();
		fd_activeToLabel.right = new FormAttachment(0, 255);
		fd_activeToLabel.bottom = new FormAttachment(activeFrom, 0, SWT.BOTTOM);
		fd_activeToLabel.top = new FormAttachment(activeFrom, 0, SWT.TOP);
		fd_activeToLabel.left = new FormAttachment(activeFrom, 5, SWT.RIGHT);
		activeToLabel.setLayoutData(fd_activeToLabel);
		activeToLabel.setText("Active To");

		final DateTime activeTo = new DateTime(this, SWT.TIME);
		final FormData fd_activeTo = new FormData();
		fd_activeTo.right = new FormAttachment(0, 355);
		fd_activeTo.bottom = new FormAttachment(activeToLabel, 0, SWT.BOTTOM);
		fd_activeTo.top = new FormAttachment(activeToLabel, 0, SWT.TOP);
		fd_activeTo.left = new FormAttachment(activeToLabel, 5, SWT.RIGHT);
		activeTo.setLayoutData(fd_activeTo);

		final Label resetAfterLabel = new Label(this, SWT.NONE);
		resetAfterLabel.setAlignment(SWT.RIGHT);
		resetAfterLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_resetAfterLabel = new FormData();
		fd_resetAfterLabel.bottom = new FormAttachment(0, 50);
		fd_resetAfterLabel.right = new FormAttachment(activeFromLabel, 0, SWT.RIGHT);
		fd_resetAfterLabel.top = new FormAttachment(activeFromLabel, 5, SWT.BOTTOM);
		fd_resetAfterLabel.left = new FormAttachment(activeFromLabel, 0, SWT.LEFT);
		resetAfterLabel.setLayoutData(fd_resetAfterLabel);
		resetAfterLabel.setText("Reset After");

		resetAfter = new Text(this, SWT.BORDER);
		final FormData fd_resetAfter = new FormData();
		fd_resetAfter.bottom = new FormAttachment(resetAfterLabel, 0, SWT.BOTTOM);
		fd_resetAfter.right = new FormAttachment(0, 150);
		fd_resetAfter.top = new FormAttachment(activeFrom, 5, SWT.BOTTOM);
		fd_resetAfter.left = new FormAttachment(resetAfterLabel, 5, SWT.RIGHT);
		resetAfter.setLayoutData(fd_resetAfter);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
