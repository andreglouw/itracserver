package com.ikno.itracclient.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.IChangeListener;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Owner;
import com.ikno.dao.business.Recipient;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserClient;
import com.ikno.dao.business.UserRecipient;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Configuration;
import com.ikno.itracclient.TracController;

public class EditUserPage extends WizardPage implements IChangeListener, FocusListener {
	private Button assetOwner;
	private Button recipient;
	private Button powerUserButton;
	private Button assetAdminButton;
	private Button userAdminButton;
	class UserNameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null || ((User)e1).getUsername() == null || e2 == null || ((User)e2).getUsername() == null)
				return 0;
			return ((User)e1).getUsername().compareTo(((User)e2).getUsername());
		}
	}
	class UserNameContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<User>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((User)element).getUsername();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	private Control userNameController;
	public static final String ID = "com.ikno.itracclient.wizards.EditUserPage"; //$NON-NLS-1$

	private Group container;
	private Label confirmLabel;
	private Label passwordLabel;
	private Label emailLabel;
	private Label cellPhoneLabel;
	private Label fullNameLabel;
	private Label loginNameLabel;
	private Text password2;
	private Text password1;
	private CheckboxTableViewer clientCheckboxTableViewer;
	private Table table;
	private Button systemAdminButton;
	private Text email;
	private Text cellPhone;
	private Text fullName;
	
	private User user = null;
	private User loggedIn = null;
	private boolean isCreating = false;
	private boolean isClientUser = false;
	private List<IChangeListener> changeListeners = new ArrayList<IChangeListener>();
	
	public List<Client> added = new ArrayList<Client>();
	public List<Client> removed = new ArrayList<Client>();
	
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
	/**
	 * Create the wizard
	 */
	public EditUserPage(User user, boolean isCreating, boolean isClientUser) {
		super(ID);
		setTitle("Edit User");
		setDescription("Edit a system user");
		this.user = user;
		this.loggedIn = TracController.getLoggedIn();
		this.isCreating = isCreating;
		this.isClientUser = isClientUser;
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		container = new Group(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);

		container.setLayout(new FormLayout());
		container.setText("User Detail");

		loginNameLabel = new Label(container, SWT.NONE);
		loginNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_loginNameLabel = new FormData();
		fd_loginNameLabel.bottom = new FormAttachment(0, 25);
		fd_loginNameLabel.right = new FormAttachment(0, 80);
		fd_loginNameLabel.top = new FormAttachment(0, 5);
		fd_loginNameLabel.left = new FormAttachment(0, 5);
		loginNameLabel.setLayoutData(fd_loginNameLabel);
		loginNameLabel.setText("Login Name");

		fullNameLabel = new Label(container, SWT.NONE);
		fullNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_fullNameLabel = new FormData();
		fd_fullNameLabel.bottom = new FormAttachment(0, 50);
		fd_fullNameLabel.right = new FormAttachment(loginNameLabel, 0, SWT.RIGHT);
		fd_fullNameLabel.top = new FormAttachment(loginNameLabel, 5, SWT.BOTTOM);
		fd_fullNameLabel.left = new FormAttachment(loginNameLabel, 0, SWT.LEFT);
		fullNameLabel.setLayoutData(fd_fullNameLabel);
		fullNameLabel.setText("Full Name");

		fullName = new Text(container, SWT.BORDER);
		fullName.setEditable(false);
		final FormData fd_fullName = new FormData();
		fd_fullName.bottom = new FormAttachment(0, 50);
		fd_fullName.top = new FormAttachment(0, 30);
		fd_fullName.right = new FormAttachment(100, -8);
		fd_fullName.left = new FormAttachment(0, 85);
		fullName.setLayoutData(fd_fullName);

		cellPhoneLabel = new Label(container, SWT.NONE);
		cellPhoneLabel.setAlignment(SWT.RIGHT);
		final FormData fd_cellPhoneLabel = new FormData();
		fd_cellPhoneLabel.bottom = new FormAttachment(0, 75);
		fd_cellPhoneLabel.right = new FormAttachment(fullNameLabel, 0, SWT.RIGHT);
		fd_cellPhoneLabel.top = new FormAttachment(fullNameLabel, 5, SWT.BOTTOM);
		fd_cellPhoneLabel.left = new FormAttachment(fullNameLabel, 0, SWT.LEFT);
		cellPhoneLabel.setLayoutData(fd_cellPhoneLabel);
		cellPhoneLabel.setText("Cell Phone");

		cellPhone = new Text(container, SWT.BORDER);
		cellPhone.setEditable(false);
		final FormData fd_cellPhone = new FormData();
		fd_cellPhone.right = new FormAttachment(0, 213);
		fd_cellPhone.bottom = new FormAttachment(cellPhoneLabel, 0, SWT.BOTTOM);
		fd_cellPhone.top = new FormAttachment(fullName, 5, SWT.BOTTOM);
		fd_cellPhone.left = new FormAttachment(cellPhoneLabel, 5, SWT.RIGHT);
		cellPhone.setLayoutData(fd_cellPhone);

		emailLabel = new Label(container, SWT.NONE);
		emailLabel.setAlignment(SWT.RIGHT);
		final FormData fd_emailLabel = new FormData();
		fd_emailLabel.right = new FormAttachment(0, 255);
		fd_emailLabel.bottom = new FormAttachment(cellPhone, 0, SWT.BOTTOM);
		fd_emailLabel.top = new FormAttachment(cellPhone, 0, SWT.TOP);
		fd_emailLabel.left = new FormAttachment(cellPhone, 5, SWT.RIGHT);
		emailLabel.setLayoutData(fd_emailLabel);
		emailLabel.setText("E-mail");

		email = new Text(container, SWT.BORDER);
		email.setEditable(false);
		final FormData fd_email = new FormData();
		fd_email.bottom = new FormAttachment(0, 75);
		fd_email.top = new FormAttachment(0, 55);
		fd_email.right = new FormAttachment(0, 420);
		fd_email.left = new FormAttachment(0, 260);
		email.setLayoutData(fd_email);

		systemAdminButton = new Button(container, SWT.CHECK);
		final FormData fd_systemAdminButton = new FormData();
		fd_systemAdminButton.right = new FormAttachment(0, 150);
		fd_systemAdminButton.top = new FormAttachment(0, 104);
		fd_systemAdminButton.bottom = new FormAttachment(0, 120);
		fd_systemAdminButton.left = new FormAttachment(0, 27);
		systemAdminButton.setLayoutData(fd_systemAdminButton);
		systemAdminButton.setText("Administrate System");
		//
		clientCheckboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		clientCheckboxTableViewer.setSorter(new ClientSorter());
		clientCheckboxTableViewer.setLabelProvider(new ClientTableLabelProvider());
		clientCheckboxTableViewer.setContentProvider(new ClientContentProvider());
		clientCheckboxTableViewer.addCheckStateListener(new ICheckStateListener() {
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
				EditUserAssetPage assetPage = (EditUserAssetPage)EditUserPage.this.getWizard().getPage(EditUserAssetPage.ID);
				assetPage.userClientsModified(added, removed);
			}
		});
		table = clientCheckboxTableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.right = new FormAttachment(100, -5);
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0, 2);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(221);
		newColumnTableColumn.setText("Contract");

		passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setAlignment(SWT.RIGHT);
		final FormData fd_passwordLabel = new FormData();
		fd_passwordLabel.left = new FormAttachment(cellPhoneLabel, -78, SWT.RIGHT);
		fd_passwordLabel.right = new FormAttachment(cellPhoneLabel, 0, SWT.RIGHT);
		fd_passwordLabel.bottom = new FormAttachment(cellPhoneLabel, 25, SWT.BOTTOM);
		fd_passwordLabel.top = new FormAttachment(cellPhoneLabel, 5, SWT.BOTTOM);
		passwordLabel.setLayoutData(fd_passwordLabel);
		passwordLabel.setText("Password");

		password1 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		password1.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				password2.setEditable(true);
			}
		});
		password1.setEditable(false);
		final FormData fd_password1 = new FormData();
		fd_password1.bottom = new FormAttachment(cellPhone, 25, SWT.BOTTOM);
		fd_password1.top = new FormAttachment(cellPhone, 5, SWT.BOTTOM);
		fd_password1.right = new FormAttachment(passwordLabel, 112, SWT.RIGHT);
		fd_password1.left = new FormAttachment(passwordLabel, 5, SWT.RIGHT);
		password1.setLayoutData(fd_password1);

		confirmLabel = new Label(container, SWT.NONE);
		confirmLabel.setAlignment(SWT.RIGHT);
		final FormData fd_confirmLabel = new FormData();
		fd_confirmLabel.bottom = new FormAttachment(cellPhone, 25, SWT.BOTTOM);
		fd_confirmLabel.top = new FormAttachment(cellPhone, 5, SWT.BOTTOM);
		fd_confirmLabel.right = new FormAttachment(password1, 100, SWT.RIGHT);
		fd_confirmLabel.left = new FormAttachment(password1, 5, SWT.RIGHT);
		confirmLabel.setLayoutData(fd_confirmLabel);
		confirmLabel.setText("Confirm password");

		password2 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		password2.setEditable(false);
		final FormData fd_password2 = new FormData();
		fd_password2.right = new FormAttachment(0, 410);
		fd_password2.bottom = new FormAttachment(confirmLabel, 21, SWT.TOP);
		fd_password2.top = new FormAttachment(confirmLabel, 0, SWT.TOP);
		fd_password2.left = new FormAttachment(confirmLabel, 5, SWT.RIGHT);
		password2.setLayoutData(fd_password2);
		//
		if ((loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) && !isCreating) {
			ComboViewer userName = new ComboViewer(container, SWT.READ_ONLY);
			userName.setSorter(new UserNameSorter());
			userName.setContentProvider(new UserNameContentProvider());
			userName.setLabelProvider(new ListLabelProvider());
			userNameController = userName.getCombo();
			((Combo)userNameController).setVisibleItemCount(10);
			if (loggedIn.fullfillsRole(User.Roles.CLIENTADMIN))
				userName.setInput(DAO.localDAO().getUsersForUserClients(user));
			else
				userName.setInput(DAO.localDAO().getSystemUsers());
			((Combo)userNameController).select(((Combo)userNameController).indexOf(user.getUsername()));
			userName.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(final SelectionChangedEvent event) {
					StructuredSelection sel = (StructuredSelection)event.getSelection();
					if (sel != null && sel.getFirstElement() != null) {
						((EditUserWizard)EditUserPage.this.getWizard()).setUser((User)sel.getFirstElement());
					}
				}
			});
		} else {
			userNameController = new Text(container, SWT.BORDER);
			final FormData fd_userNameController = new FormData();
			fd_userNameController.right = new FormAttachment(0, 230);
			fd_userNameController.top = new FormAttachment(loginNameLabel, 0, SWT.TOP);
			fd_userNameController.left = new FormAttachment(loginNameLabel, 5, SWT.RIGHT);
			userNameController.setLayoutData(fd_userNameController);
			((Text)userNameController).setText((user.getUsername() == null) ? "" : user.getUsername());
			if (isCreating) {
				((Text)userNameController).setEditable(true);
				userNameController.setFocus();
			} else {
				((Text)userNameController).setEditable(false);
				fullName.setFocus();
			}
			((Text)userNameController).addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					EditUserPage.this.objectChanged(user);
				}
			});
		}
		final FormData fd_combo = new FormData();
		fd_combo.bottom = new FormAttachment(fullName, -5, SWT.TOP);
		fd_combo.right = new FormAttachment(0, 235);
		fd_combo.top = new FormAttachment(loginNameLabel, 0, SWT.TOP);
		fd_combo.left = new FormAttachment(loginNameLabel, 5, SWT.RIGHT);
		userNameController.setLayoutData(fd_combo);

		userAdminButton = new Button(container, SWT.CHECK);
		final FormData fd_userAdminButton = new FormData();
		fd_userAdminButton.bottom = new FormAttachment(0, 121);
		fd_userAdminButton.top = new FormAttachment(0, 105);
		fd_userAdminButton.right = new FormAttachment(0, 275);
		fd_userAdminButton.left = new FormAttachment(0, 165);
		userAdminButton.setLayoutData(fd_userAdminButton);
		userAdminButton.setText("Administrate Users");

		assetAdminButton = new Button(container, SWT.CHECK);
		final FormData fd_assetAdminButton = new FormData();
		fd_assetAdminButton.top = new FormAttachment(userAdminButton, -15, SWT.BOTTOM);
		fd_assetAdminButton.bottom = new FormAttachment(userAdminButton, 0, SWT.BOTTOM);
		fd_assetAdminButton.left = new FormAttachment(password2, -123, SWT.RIGHT);
		fd_assetAdminButton.right = new FormAttachment(password2, 0, SWT.RIGHT);
		assetAdminButton.setLayoutData(fd_assetAdminButton);
		assetAdminButton.setText("Administrate Assets");
		
		container.setTabList(new Control[] {userNameController, systemAdminButton, fullName, cellPhone, email, passwordLabel, table, password1, password2});
		fullName.addFocusListener(this);
		cellPhone.addFocusListener(this);
		email.addFocusListener(this);
		password1.addFocusListener(this);
		password2.addFocusListener(this);

		powerUserButton = new Button(container, SWT.CHECK);
		fd_table.top = new FormAttachment(powerUserButton, 5, SWT.BOTTOM);
		final FormData fd_powerUserButton = new FormData();
		fd_powerUserButton.top = new FormAttachment(systemAdminButton, 0, SWT.BOTTOM);
		fd_powerUserButton.right = new FormAttachment(0, 110);
		fd_powerUserButton.bottom = new FormAttachment(0, 140);
		fd_powerUserButton.left = new FormAttachment(systemAdminButton, 0, SWT.LEFT);
		powerUserButton.setLayoutData(fd_powerUserButton);
		powerUserButton.setText("Power User");

		recipient = new Button(container, SWT.CHECK);
		recipient.setEnabled(false);
		final FormData fd_recipient = new FormData();
		fd_recipient.top = new FormAttachment(table, -24, SWT.TOP);
		fd_recipient.bottom = new FormAttachment(table, -5, SWT.TOP);
		recipient.setLayoutData(fd_recipient);
		recipient.setText("Register as text message recipient");

		assetOwner = new Button(container, SWT.CHECK);
		fd_recipient.right = new FormAttachment(assetOwner, 195, SWT.RIGHT);
		fd_recipient.left = new FormAttachment(assetOwner, 5, SWT.RIGHT);
		assetOwner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (assetOwner.getSelection() == true) {
					if (!PersistantObject.instanceOf(user, Owner.class)) {
						try {
							user = new Owner(user);
						} catch (Exception e1) {
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									"Error transforming", "Error transforming to Owner, quit and try again.");
						}
					}
				} else {
					if (PersistantObject.instanceOf(user, Owner.class)) {
						try {
							user = new User(user);
						} catch (Exception e1) {
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									"Error transforming", "Error transforming to User, quit and try again.");
						}
					}
				}
			}
		});
		assetOwner.setEnabled(false);
		final FormData fd_assetOwner = new FormData();
		fd_assetOwner.right = new FormAttachment(cellPhone, 0, SWT.RIGHT);
		fd_assetOwner.top = new FormAttachment(recipient, -19, SWT.BOTTOM);
		fd_assetOwner.bottom = new FormAttachment(recipient, 0, SWT.BOTTOM);
		fd_assetOwner.left = new FormAttachment(powerUserButton, 5, SWT.RIGHT);
		assetOwner.setLayoutData(fd_assetOwner);
		assetOwner.setText("Asset Owner");

		this.setEditable(true,isCreating,isClientUser);
		this.addChangeListener(this);
		this.setUser(user);
		this.objectChanged(user);
	}

	public boolean performFinish() {
		try {
			DAO.localDAO().saveOrUpdate(user);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public void objectChanged(Object object) {
		String message = validate();
		if (message == null) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(message);
			setPageComplete(false);
		}
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
		this.buildFromObject();
	}
	public void setEditable(boolean editable,boolean isCreating, boolean isClientUser) {
		this.isCreating = isCreating;
		this.isClientUser = isClientUser;
		if (isClientUser) {
		} else {
			userNameController.setEnabled(true);
		}
		fullName.setEditable(editable);
		cellPhone.setEditable(editable);
		email.setEditable(editable);
		password1.setEditable(editable);
		password2.setEditable(false);
		systemAdminButton.setEnabled(editable);
		assetAdminButton.setEnabled(editable);
		userAdminButton.setEnabled(editable);
		assetOwner.setEnabled(editable);
		recipient.setEnabled(editable);
	}
	public String validate() {
		String userName = null;
		if ((loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) && !isCreating) {
			userName = ((Combo)userNameController).getText();
		} else {
			userName = ((Text)userNameController).getText();
		}
		if (userName == null || userName.equals(""))
			return "Please enter a valid user name";
		else if (isCreating) {
			User existing = null;
			try {
				existing = DAO.localDAO().getUserByName(userName);
			} catch (Exception e) {}
			if (existing != null) {
				return "A user by that name allready exists";
			}
		}
		if (password1.getText() != null) {
			if (password1.getText() == null) {
				password1.setFocus();
				return "You cannot have an empty password";
			}
			if (!password1.getText().equals(password2.getText())) {
				password1.selectAll();
				password1.setFocus();
				password2.setText("");
				return "The 2 passwords must match";
			}
		}
		return null;
	}
	
	public void buildFromObject() {
		if (user == null)
			return;
		fullName.setText((user.getFullname() == null) ? "" : user.getFullname());
		cellPhone.setText((user.getCellphone() == null) ? "" :user.getCellphone());
		email.setText((user.getEmail() == null) ? "" : user.getEmail());
		systemAdminButton.setEnabled(false);
		userAdminButton.setEnabled(false);
		assetAdminButton.setEnabled(false);
		powerUserButton.setEnabled(false);
		assetOwner.setEnabled(false);
		recipient.setEnabled(false);
		table.setEnabled(false);
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			systemAdminButton.setEnabled(true);
			userAdminButton.setEnabled(true);
			assetAdminButton.setEnabled(true);
			powerUserButton.setEnabled(true);
			assetOwner.setEnabled(true);
			recipient.setEnabled(true);
		}
		if (loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) {
			userAdminButton.setEnabled(true);
			assetOwner.setEnabled(true);
			recipient.setEnabled(true);
		}
		if (loggedIn.fullfillsRole(User.Roles.ASSETADMIN)) {
			assetAdminButton.setEnabled(true);
		}
		if (loggedIn.fullfillsRole(User.Roles.POWERUSER)) {
			powerUserButton.setEnabled(true);
		}
		List<Client> possibles = null;
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			possibles = DAO.localDAO().getClients(Configuration.configCenter().getString("dataScoutId"));
			table.setEnabled(true);
		} else {
			possibles = new ArrayList<Client>(loggedIn.getClients());
			if (loggedIn.fullfillsRole(User.Roles.CLIENTADMIN))
				table.setEnabled(true);
			else
				table.setEnabled(false);
		}
		clientCheckboxTableViewer.setInput(possibles);
		clientCheckboxTableViewer.setCheckedElements(user.getClients().toArray());
		added = new ArrayList<Client>();
		removed = new ArrayList<Client>();
		assetAdminButton.setSelection(user.fullfillsRole(User.Roles.ASSETADMIN));
		systemAdminButton.setSelection(user.fullfillsRole(User.Roles.SYSTEMADMIN));
		powerUserButton.setSelection(user.fullfillsRole(User.Roles.POWERUSER));
		userAdminButton.setSelection(user.fullfillsRole(User.Roles.CLIENTADMIN));
		assetOwner.setSelection(PersistantObject.instanceOf(user,Owner.class));
		String password = user.getPassword();
		password1.setText((password == null) ? "" : password);
		password2.setText((password == null) ? "" : password);
	}
	public void populateObject() throws Exception {
		if (isCreating)
			user.setUsername(((Text)userNameController).getText());
		user.setFullname(fullName.getText());
		user.setCellphone(cellPhone.getText());
		user.setEmail(email.getText());
		if (isClientUser)
			user.addRole(User.Roles.CLIENT);
		else
			user.removeRole(User.Roles.CLIENT);
		if (systemAdminButton.getSelection())
			user.addRole(User.Roles.SYSTEMADMIN);
		else
			user.removeRole(User.Roles.SYSTEMADMIN);
		if (powerUserButton.getSelection())
			user.addRole(User.Roles.POWERUSER);
		else
			user.removeRole(User.Roles.POWERUSER);
		if (userAdminButton.getSelection())
			user.addRole(User.Roles.CLIENTADMIN);
		else
			user.removeRole(User.Roles.CLIENTADMIN);
		if (assetAdminButton.getSelection())
			user.addRole(User.Roles.ASSETADMIN);
		else
			user.removeRole(User.Roles.ASSETADMIN);
		if (password1.getText() != null && password2.getText() != null && password1.getText().equals(password2.getText())) {
			user.setPassword(password1.getText());
		}
		for (Client client : removed) {
			UserClient uc = client.getUserClient(user);
			if (uc != null) {
				DAO.localDAO().delete(uc);
				client.removeUserClient(uc);
				user.removeUserClient(uc);
			}
		}
		for (Client client : added) {
			user.addClient(client);
			DAO.localDAO().saveOrUpdate(user);
		}
		if (recipient.getSelection()) {
			if (isCreating) {
				UserRecipient recipient = Recipient.recipientFromUser(user,true,true);
				DAO.localDAO().save(recipient);
			} else {
				UserRecipient recipient = DAO.localDAO().getRecipientForUser(user);
				if (recipient == null) {
					recipient = Recipient.recipientFromUser(user,true,true);
					DAO.localDAO().save(recipient);
				}
			}
		}
	}
	public void addChangeListener(IChangeListener listener) {
		changeListeners.add(listener);
	}
	public void focusLost(final FocusEvent e) {
		for (Iterator<IChangeListener> cli = changeListeners.iterator(); cli.hasNext();) {
			cli.next().objectChanged(user);
		}
	}
	public void focusGained(final FocusEvent e) {
	}
}
