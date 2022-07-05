package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.SimpleStatus;

public class SimpleStatusDetail extends Group {

	private Text description;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SimpleStatusDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());
		setText("Status");

		description = new Text(this, SWT.NONE);
		description.setToolTipText("A description of the asset's status, or 'N/A' if not available");
		description.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		description.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		description.setEditable(false);
		final FormData fd_description = new FormData();
		fd_description.right = new FormAttachment(100, -5);
		fd_description.bottom = new FormAttachment(0, 25);
		fd_description.top = new FormAttachment(0, 5);
		fd_description.left = new FormAttachment(0, 5);
		description.setLayoutData(fd_description);
		//
	}
	
	public Composite stackView() {
		return this;
	}

	public void clear() {
		description.setText("N/A");
	}
	public void setSimpleStatus(SimpleStatus simpleStatus) {
		description.setText((simpleStatus.getFullDescription() == null) ? "N/A" : simpleStatus.getFullDescription());
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
