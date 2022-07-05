package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.widgets.Composite;

public class NotificationWidget extends Composite {

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public NotificationWidget(Composite parent, int style) {
		super(parent, style);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
