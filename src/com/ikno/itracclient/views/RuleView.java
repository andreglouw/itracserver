package com.ikno.itracclient.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class RuleView extends ViewPart {

	private Text reset;
	private Text description;
	private Text ruleName;
	public static final String ID = "com.ikno.itracclient.views.RuleView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		container.setLayout(new FormLayout());

		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		nameLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_nameLabel = new FormData();
		fd_nameLabel.bottom = new FormAttachment(0, 25);
		fd_nameLabel.right = new FormAttachment(0, 90);
		fd_nameLabel.top = new FormAttachment(0, 5);
		fd_nameLabel.left = new FormAttachment(0, 5);
		nameLabel.setLayoutData(fd_nameLabel);
		nameLabel.setText("Name");

		ruleName = new Text(container, SWT.BORDER);
		final FormData fd_ruleName = new FormData();
		fd_ruleName.bottom = new FormAttachment(0, 25);
		fd_ruleName.top = new FormAttachment(0, 5);
		fd_ruleName.right = new FormAttachment(0, 260);
		fd_ruleName.left = new FormAttachment(0, 95);
		ruleName.setLayoutData(fd_ruleName);

		final Label descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		descriptionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.bottom = new FormAttachment(0, 50);
		fd_descriptionLabel.right = new FormAttachment(nameLabel, 0, SWT.RIGHT);
		fd_descriptionLabel.top = new FormAttachment(nameLabel, 5, SWT.BOTTOM);
		fd_descriptionLabel.left = new FormAttachment(nameLabel, 0, SWT.LEFT);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText("Description");

		description = new Text(container, SWT.BORDER);
		final FormData fd_description = new FormData();
		fd_description.bottom = new FormAttachment(0, 50);
		fd_description.top = new FormAttachment(0, 30);
		fd_description.right = new FormAttachment(100, -5);
		fd_description.left = new FormAttachment(0, 95);
		description.setLayoutData(fd_description);

		final Label activeFromLabel = new Label(container, SWT.NONE);
		activeFromLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		activeFromLabel.setAlignment(SWT.RIGHT);
		final FormData fd_activeFromLabel = new FormData();
		fd_activeFromLabel.right = new FormAttachment(descriptionLabel, 0, SWT.RIGHT);
		fd_activeFromLabel.top = new FormAttachment(descriptionLabel, 5, SWT.BOTTOM);
		fd_activeFromLabel.left = new FormAttachment(descriptionLabel, 0, SWT.LEFT);
		activeFromLabel.setLayoutData(fd_activeFromLabel);
		activeFromLabel.setText("Active From");

		DateTime activeFrom;
		activeFrom = new DateTime(container, SWT.TIME);
		fd_activeFromLabel.bottom = new FormAttachment(activeFrom, 0, SWT.BOTTOM);
		final FormData fd_activeFrom = new FormData();
		fd_activeFrom.right = new FormAttachment(0, 185);
		fd_activeFrom.bottom = new FormAttachment(0, 75);
		fd_activeFrom.top = new FormAttachment(description, 5, SWT.BOTTOM);
		fd_activeFrom.left = new FormAttachment(activeFromLabel, 5, SWT.RIGHT);
		activeFrom.setLayoutData(fd_activeFrom);

		final Label toLabel = new Label(container, SWT.NONE);
		toLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		toLabel.setAlignment(SWT.CENTER);
		final FormData fd_toLabel = new FormData();
		fd_toLabel.right = new FormAttachment(0, 220);
		fd_toLabel.bottom = new FormAttachment(activeFrom, 0, SWT.BOTTOM);
		fd_toLabel.top = new FormAttachment(activeFrom, 0, SWT.TOP);
		fd_toLabel.left = new FormAttachment(activeFrom, 5, SWT.RIGHT);
		toLabel.setLayoutData(fd_toLabel);
		toLabel.setText("To");

		final DateTime activeTo = new DateTime(container, SWT.TIME);
		final FormData fd_activeTo = new FormData();
		fd_activeTo.right = new FormAttachment(0, 310);
		fd_activeTo.bottom = new FormAttachment(toLabel, 0, SWT.BOTTOM);
		fd_activeTo.top = new FormAttachment(toLabel, 0, SWT.TOP);
		fd_activeTo.left = new FormAttachment(toLabel, 0, SWT.RIGHT);
		activeTo.setLayoutData(fd_activeTo);

		final Label resetAfterLabel = new Label(container, SWT.NONE);
		resetAfterLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		resetAfterLabel.setAlignment(SWT.RIGHT);
		final FormData fd_resetAfterLabel = new FormData();
		fd_resetAfterLabel.bottom = new FormAttachment(0, 100);
		fd_resetAfterLabel.right = new FormAttachment(activeFromLabel, 0, SWT.RIGHT);
		fd_resetAfterLabel.top = new FormAttachment(activeFromLabel, 5, SWT.BOTTOM);
		fd_resetAfterLabel.left = new FormAttachment(activeFromLabel, 0, SWT.LEFT);
		resetAfterLabel.setLayoutData(fd_resetAfterLabel);
		resetAfterLabel.setText("Reset After");

		reset = new Text(container, SWT.BORDER);
		final FormData fd_reset = new FormData();
		fd_reset.bottom = new FormAttachment(resetAfterLabel, 0, SWT.BOTTOM);
		fd_reset.right = new FormAttachment(0, 135);
		fd_reset.top = new FormAttachment(activeFrom, 5, SWT.BOTTOM);
		fd_reset.left = new FormAttachment(resetAfterLabel, 5, SWT.RIGHT);
		reset.setLayoutData(fd_reset);
		//
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
