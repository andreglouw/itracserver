package com.ikno.itracclient.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.FencedArea;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Configuration;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;

public class FencedAreaDetail extends Dialog {
	private static final Logger logger = Logging.getLogger(FencedAreaDetail.class.getName());

	private Button assetExitButton;
	private Button assetEnterButton;
	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Client)e1).getClientName().compareTo(((Client)e2).getClientName());
		}
	}
	
	class ClientTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Client client = (Client)element;
			return client.getClientName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ClientContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<Client>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private CheckboxTableViewer clientListTableViewer;
	private Table clientListTable;
	public List<Client> added = new ArrayList<Client>();
	public List<Client> removed = new ArrayList<Client>();
	private Button okButton;
	private Text resetTime;
	private DateTime activeTo;
	private DateTime activeFrom;
	private Text description;
	private Text ruleName;
	protected Object result;
	protected Shell shell;
	private FencedArea fencedArea;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public FencedAreaDetail(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public FencedAreaDetail(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(FencedArea fencedArea) {
		createContents();
		this.setFencedArea(fencedArea);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	public void setFencedArea(FencedArea fencedArea) {
		this.fencedArea = fencedArea;
		if (fencedArea != null) {
			ruleName.setText(fencedArea.getRuleName() != null ? fencedArea.getRuleName() : "");
			description.setText(fencedArea.getDescription() != null ? fencedArea.getDescription() : "");
			Calendar from = new GregorianCalendar();
			if (fencedArea.getActiveFrom() != null)
				from.setTimeInMillis(fencedArea.getActiveFrom().getTime());
			activeFrom.setHours(from.get(Calendar.HOUR_OF_DAY));
			activeFrom.setMinutes(from.get(Calendar.MINUTE));
			activeFrom.setSeconds(from.get(Calendar.SECOND));
			Calendar to = new GregorianCalendar();
			if (fencedArea.getActiveTo() != null)
				to.setTimeInMillis(fencedArea.getActiveTo().getTime());
			activeTo.setHours(to.get(Calendar.HOUR_OF_DAY));
			activeTo.setMinutes(to.get(Calendar.MINUTE));
			activeTo.setSeconds(to.get(Calendar.SECOND));
			if (fencedArea.getType() == FencedArea.AreaType.INCLUSION) {
				assetExitButton.setSelection(true);
				assetEnterButton.setSelection(false);
			} else {
				assetExitButton.setSelection(false);
				assetEnterButton.setSelection(true);
			}
			resetTime.setText(String.valueOf(fencedArea.getResetTime()));
			clientListTableViewer.setCheckedElements(fencedArea.getClients().toArray());
		} else {
			User loggedIn = TracController.getLoggedIn();
			clientListTableViewer.setCheckedElements(loggedIn.getClients().toArray());
		}
		added = new ArrayList<Client>();
		removed = new ArrayList<Client>();
		validate();
	}
	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 418);
		shell.setText("Fenced Area");

		final Label ruleNameLabel = new Label(shell, SWT.NONE);
		ruleNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_ruleNameLabel = new FormData();
		fd_ruleNameLabel.bottom = new FormAttachment(0, 25);
		fd_ruleNameLabel.right = new FormAttachment(0, 80);
		fd_ruleNameLabel.top = new FormAttachment(0, 5);
		fd_ruleNameLabel.left = new FormAttachment(0, 5);
		ruleNameLabel.setLayoutData(fd_ruleNameLabel);
		ruleNameLabel.setText("Rule Name");

		ruleName = new Text(shell, SWT.BORDER);
		ruleName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				validate();
			}
		});
		final FormData fd_ruleName = new FormData();
		fd_ruleName.bottom = new FormAttachment(ruleNameLabel, 0, SWT.BOTTOM);
		fd_ruleName.right = new FormAttachment(0, 265);
		fd_ruleName.top = new FormAttachment(ruleNameLabel, 0, SWT.TOP);
		fd_ruleName.left = new FormAttachment(ruleNameLabel, 5, SWT.RIGHT);
		ruleName.setLayoutData(fd_ruleName);

		final Label descriptionLabel = new Label(shell, SWT.NONE);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.bottom = new FormAttachment(ruleNameLabel, 25, SWT.BOTTOM);
		fd_descriptionLabel.top = new FormAttachment(ruleNameLabel, 5, SWT.BOTTOM);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setAlignment(SWT.RIGHT);
		descriptionLabel.setText("Description");

		description = new Text(shell, SWT.BORDER);
		fd_descriptionLabel.left = new FormAttachment(description, -75, SWT.LEFT);
		fd_descriptionLabel.right = new FormAttachment(description, -5, SWT.LEFT);
		final FormData fd_description = new FormData();
		fd_description.left = new FormAttachment(0, 85);
		description.setLayoutData(fd_description);

		Group activePeriodGroup;
		activePeriodGroup = new Group(shell, SWT.NONE);
		fd_description.top = new FormAttachment(activePeriodGroup, -24, SWT.TOP);
		fd_description.bottom = new FormAttachment(activePeriodGroup, -5, SWT.TOP);
		final FormData fd_activePeriodGroup = new FormData();
		fd_activePeriodGroup.bottom = new FormAttachment(descriptionLabel, 70, SWT.TOP);
		fd_activePeriodGroup.top = new FormAttachment(descriptionLabel, 25, SWT.TOP);
		fd_activePeriodGroup.right = new FormAttachment(descriptionLabel, 360, SWT.LEFT);
		fd_activePeriodGroup.left = new FormAttachment(descriptionLabel, 75, SWT.LEFT);
		activePeriodGroup.setLayoutData(fd_activePeriodGroup);
		activePeriodGroup.setLayout(new FormLayout());
		activePeriodGroup.setText("Active Period");

		final Label fromLabel = new Label(activePeriodGroup, SWT.NONE);
		final FormData fd_fromLabel = new FormData();
		fd_fromLabel.bottom = new FormAttachment(100, -5);
		fd_fromLabel.top = new FormAttachment(0, 5);
		fd_fromLabel.right = new FormAttachment(0, 40);
		fd_fromLabel.left = new FormAttachment(0, 5);
		fromLabel.setLayoutData(fd_fromLabel);
		fromLabel.setAlignment(SWT.RIGHT);
		fromLabel.setText("From");

		activeFrom = new DateTime(activePeriodGroup, SWT.TIME);
		final FormData fd_activeFrom = new FormData();
		fd_activeFrom.bottom = new FormAttachment(fromLabel, 0, SWT.BOTTOM);
		fd_activeFrom.top = new FormAttachment(fromLabel, 0, SWT.TOP);
		fd_activeFrom.right = new FormAttachment(0, 135);
		fd_activeFrom.left = new FormAttachment(fromLabel, 5, SWT.RIGHT);
		activeFrom.setLayoutData(fd_activeFrom);
		activeFrom.setSeconds(0);
		activeFrom.setMinutes(0);
		activeFrom.setHours(0);

		final Label toLabel = new Label(activePeriodGroup, SWT.NONE);
		final FormData fd_toLabel = new FormData();
		fd_toLabel.bottom = new FormAttachment(activeFrom, 0, SWT.BOTTOM);
		fd_toLabel.top = new FormAttachment(activeFrom, 0, SWT.TOP);
		fd_toLabel.right = new FormAttachment(0, 170);
		fd_toLabel.left = new FormAttachment(activeFrom, 5, SWT.RIGHT);
		toLabel.setLayoutData(fd_toLabel);
		toLabel.setAlignment(SWT.RIGHT);
		toLabel.setText("To");

		activeTo = new DateTime(activePeriodGroup, SWT.TIME);
		final FormData fd_activeTo = new FormData();
		fd_activeTo.bottom = new FormAttachment(toLabel, 0, SWT.BOTTOM);
		fd_activeTo.top = new FormAttachment(toLabel, 0, SWT.TOP);
		fd_activeTo.right = new FormAttachment(0, 265);
		fd_activeTo.left = new FormAttachment(toLabel, 5, SWT.RIGHT);
		activeTo.setLayoutData(fd_activeTo);
		activeTo.setSeconds(0);
		activeTo.setMinutes(0);
		activeTo.setHours(0);

		final Label resetAfterLabel = new Label(shell, SWT.NONE);
		final FormData fd_resetAfterLabel = new FormData();
		fd_resetAfterLabel.bottom = new FormAttachment(descriptionLabel, 95, SWT.TOP);
		fd_resetAfterLabel.top = new FormAttachment(descriptionLabel, 75, SWT.TOP);
		fd_resetAfterLabel.right = new FormAttachment(descriptionLabel, 135, SWT.LEFT);
		fd_resetAfterLabel.left = new FormAttachment(descriptionLabel, 75, SWT.LEFT);
		resetAfterLabel.setLayoutData(fd_resetAfterLabel);
		resetAfterLabel.setAlignment(SWT.RIGHT);
		resetAfterLabel.setText("Reset After");

		resetTime = new Text(shell, SWT.BORDER);
		resetTime.setToolTipText("The period after which a warning will again be issued if the zone is still being violated\nSetting this = 0 will prevent a re-issue");
		final FormData fd_resetTime = new FormData();
		fd_resetTime.bottom = new FormAttachment(descriptionLabel, 95, SWT.TOP);
		fd_resetTime.top = new FormAttachment(descriptionLabel, 75, SWT.TOP);
		fd_resetTime.right = new FormAttachment(descriptionLabel, 170, SWT.LEFT);
		fd_resetTime.left = new FormAttachment(descriptionLabel, 140, SWT.LEFT);
		resetTime.setLayoutData(fd_resetTime);
		resetTime.setText("30");

		final Label minutesLabel = new Label(shell, SWT.NONE);
		final FormData fd_minutesLabel = new FormData();
		fd_minutesLabel.bottom = new FormAttachment(descriptionLabel, 95, SWT.TOP);
		fd_minutesLabel.top = new FormAttachment(descriptionLabel, 75, SWT.TOP);
		fd_minutesLabel.right = new FormAttachment(descriptionLabel, 215, SWT.LEFT);
		fd_minutesLabel.left = new FormAttachment(descriptionLabel, 175, SWT.LEFT);
		minutesLabel.setLayoutData(fd_minutesLabel);
		minutesLabel.setText("minutes");

		okButton = new Button(shell, SWT.NONE);
		okButton.setEnabled(false);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					DAO.localDAO().beginTransaction();
					fencedArea.setRuleName(ruleName.getText());
					fencedArea.setDescription(description.getText());
					FencedArea.AreaType type = FencedArea.AreaType.INCLUSION;
					if (assetEnterButton.getSelection())
						type = FencedArea.AreaType.EXCLUSION;
					fencedArea.setType(type);
					int value = (resetTime.getText() == null) ? 30 : Integer.parseInt(resetTime.getText());
					fencedArea.setResetTime(value);
					fencedArea.setImmediate(true);
					Calendar dt = Calendar.getInstance();
					dt.set(activeFrom.getYear(),activeFrom.getMonth(),activeFrom.getDay(),activeFrom.getHours(),activeFrom.getMinutes(),activeFrom.getSeconds());
					dt.set(Calendar.MILLISECOND, 0);
					fencedArea.setActiveFrom(dt.getTime());
					dt = Calendar.getInstance();
					dt.set(activeTo.getYear(),activeTo.getMonth(),activeTo.getDay(),activeTo.getHours(),activeTo.getMinutes(),activeTo.getSeconds());
					dt.set(Calendar.MILLISECOND, 0);
					fencedArea.setActiveTo(dt.getTime());
					fencedArea.setActive(true);
					fencedArea.setPublic(true);
					for (Client client : added) {
						client.addRule(fencedArea);
					}
					for (Client client : removed) {
						client.removeRule(fencedArea);
					}
					DAO.localDAO().save(fencedArea);
					DAO.localDAO().commitTransaction();
					result = fencedArea;
				} catch (Exception exc) {
					logger.log(Level.SEVERE,"Exception commiting FencedArea mods:",exc);
					DAO.localDAO().rollbackTransaction();
					result = null;
				}
				shell.close();
			}
		});
		fd_description.right = new FormAttachment(okButton, 0, SWT.RIGHT);
		final FormData fd_okButton = new FormData();
		fd_okButton.bottom = new FormAttachment(100, -5);
		fd_okButton.top = new FormAttachment(100, -29);
		fd_okButton.right = new FormAttachment(100, -4);
		fd_okButton.left = new FormAttachment(100, -44);
		okButton.setLayoutData(fd_okButton);
		okButton.setText("OK");

		Button cancelButton;
		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.top = new FormAttachment(okButton, -24, SWT.BOTTOM);
		fd_cancelButton.bottom = new FormAttachment(okButton, 0, SWT.BOTTOM);
		fd_cancelButton.right = new FormAttachment(100, -44);
		fd_cancelButton.left = new FormAttachment(100, -94);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		clientListTableViewer = CheckboxTableViewer.newCheckList(shell, SWT.BORDER);
		clientListTableViewer.setSorter(new ClientSorter());
		clientListTableViewer.setLabelProvider(new ClientTableLabelProvider());
		clientListTableViewer.setContentProvider(new ClientContentProvider());
		clientListTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Client client = (Client)event.getElement();
				if (event.getChecked()) {
					added.add(client);
					if (removed.contains(client))
						removed.remove(client);
				} else {
					removed.add(client);
					if (added.contains(client))
						added.remove(client);
				}
				validate();
			}
		});
		clientListTable = clientListTableViewer.getTable();
		final FormData fd_clientListTable = new FormData();
		fd_clientListTable.bottom = new FormAttachment(cancelButton, -5, SWT.TOP);
		fd_clientListTable.right = new FormAttachment(okButton, 0, SWT.RIGHT);
		fd_clientListTable.left = new FormAttachment(ruleNameLabel, 0, SWT.LEFT);
		clientListTable.setLayoutData(fd_clientListTable);
		clientListTable.setLinesVisible(true);
		clientListTable.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(clientListTable, SWT.NONE);
		newColumnTableColumn.setWidth(145);
		newColumnTableColumn.setText("Contract");

		User loggedIn = TracController.getLoggedIn();
		List<Client> possibles = null;
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			possibles = DAO.localDAO().getClients(Configuration.configCenter().getString("dataScoutId"));
			clientListTable.setEnabled(true);
		} else if (loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) {
			possibles = new ArrayList<Client>(loggedIn.getClients());
			clientListTable.setEnabled(true);
		} else if (loggedIn.fullfillsRole(User.Roles.ASSETADMIN)) {
			possibles = new ArrayList<Client>(loggedIn.getClients());
			clientListTable.setEnabled(false);
		} else if (loggedIn.fullfillsRole(User.Roles.POWERUSER)) {
			possibles = new ArrayList<Client>(loggedIn.getClients());
			clientListTable.setEnabled(false);
		}
		clientListTableViewer.setInput(possibles);

		Composite composite;
		composite = new Composite(shell, SWT.NONE);
		fd_clientListTable.top = new FormAttachment(composite, 5, SWT.BOTTOM);
		final FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(0, 85);
		fd_composite.right = new FormAttachment(0, 320);
		fd_composite.bottom = new FormAttachment(resetAfterLabel, 50, SWT.BOTTOM);
		fd_composite.top = new FormAttachment(resetAfterLabel, 5, SWT.BOTTOM);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FormLayout());

		assetEnterButton = new Button(composite, SWT.RADIO);
		assetEnterButton.setSelection(true);
		final FormData fd_assetEnterButton = new FormData();
		fd_assetEnterButton.bottom = new FormAttachment(0, 25);
		fd_assetEnterButton.right = new FormAttachment(0, 230);
		fd_assetEnterButton.top = new FormAttachment(0, 5);
		fd_assetEnterButton.left = new FormAttachment(0, 5);
		assetEnterButton.setLayoutData(fd_assetEnterButton);
		assetEnterButton.setText("Warn on Asset ENTERING area");

		assetExitButton = new Button(composite, SWT.RADIO);
		final FormData fd_assetExitButton = new FormData();
		fd_assetExitButton.bottom = new FormAttachment(0, 40);
		fd_assetExitButton.right = new FormAttachment(assetEnterButton, 0, SWT.RIGHT);
		fd_assetExitButton.top = new FormAttachment(assetEnterButton, 0, SWT.BOTTOM);
		fd_assetExitButton.left = new FormAttachment(assetEnterButton, 0, SWT.LEFT);
		assetExitButton.setLayoutData(fd_assetExitButton);
		assetExitButton.setText("Warn on Asset EXITING area");
		//
	}
	public void validate() {
		if (fencedArea != null) {
			if (ruleName.getText() == null || ruleName.getText().equals(""))
				okButton.setEnabled(false);
			else {
				Object[] checkedClients = clientListTableViewer.getCheckedElements(); 
				if (checkedClients == null || checkedClients.length == 0) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		} else
			okButton.setEnabled(false);
	}

}
