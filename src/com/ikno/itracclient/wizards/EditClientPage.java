package com.ikno.itracclient.wizards;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.Management;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Configuration;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.wizards.EditUserPage.ListLabelProvider;
import com.ikno.itracclient.wizards.EditUserPage.UserNameContentProvider;
import com.ikno.itracclient.wizards.EditUserPage.UserNameSorter;

public class EditClientPage extends WizardPage {
	public static final String ID = "com.ikno.itracclient.wizards.EditClientPage"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(EditClientPage.class.getName());

	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Management)e1).getCompanyName().compareTo(((Management)e2).getCompanyName());
		}
	}
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Management)element).getCompanyName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<Management>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Client)e1).getClientName().compareTo(((Client)e2).getClientName());
		}
	}
	class ClientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Client)element).getClientName();
		}
		public Image getImage(Object element) {
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
	private ComboViewer managementListViewer;
	private Combo combo;
	private Control clientName;
	
	private Client client = null;
	private boolean isCreating = false;
	/**
	 * Create the wizard
	 */
	public EditClientPage(Client client, boolean isCreating) {
		super(ID);
		setTitle("Edit Contract");
		setDescription("Edit a contract's detail");
		this.client = client;
		this.isCreating = isCreating;
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		//
		setControl(container);

		final Label clientNameLabel = new Label(container, SWT.NONE);
		clientNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_clientNameLabel = new FormData();
		fd_clientNameLabel.right = new FormAttachment(0, 90);
		fd_clientNameLabel.bottom = new FormAttachment(0, 25);
		fd_clientNameLabel.top = new FormAttachment(0, 5);
		fd_clientNameLabel.left = new FormAttachment(0, 5);
		clientNameLabel.setLayoutData(fd_clientNameLabel);
		clientNameLabel.setText("Contract Name");

		User loggedIn = TracController.getLoggedIn();
		if ((loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) && !isCreating) {
			ComboViewer clientNameViewer = new ComboViewer(container, SWT.NONE);
			clientNameViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(final SelectionChangedEvent event) {
					Client selected = (Client)((IStructuredSelection)event.getSelection()).getFirstElement();
					if (selected != null) {
						Management current = selected.getManagement();
						if (current != null)
							combo.select(combo.indexOf(current.getCompanyName()));
						else
							combo.select(-1);
					}
				}
			});
			clientNameViewer.setSorter(new ClientSorter());
			clientNameViewer.setContentProvider(new ClientContentProvider());
			clientNameViewer.setLabelProvider(new ClientListLabelProvider());
			final FormData fd_clientName = new FormData();
			fd_clientName.right = new FormAttachment(0, 477);
			fd_clientName.bottom = new FormAttachment(clientNameLabel, 0, SWT.BOTTOM);
			fd_clientName.top = new FormAttachment(clientNameLabel, 0, SWT.TOP);
			fd_clientName.left = new FormAttachment(clientNameLabel, 5, SWT.RIGHT);
			clientName = clientNameViewer.getCombo();
			((Combo)clientName).addModifyListener(new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					if (((Combo)clientName).getText() == null || ((Combo)clientName).getText().equals("")) {
						setErrorMessage("You must specify a contract name");
						((EditClientWizard)getWizard()).setCanFinish(false);
						setPageComplete(false);
					} else {
						setErrorMessage(null);
						((EditClientWizard)getWizard()).setCanFinish(true);
						setPageComplete(true);
					}
				}
			});
			((Combo)clientName).setVisibleItemCount(10);
			clientName.setLayoutData(fd_clientName);
			((Combo)clientName).setVisibleItemCount(10);
			if (loggedIn.fullfillsRole(User.Roles.CLIENTADMIN))
				clientNameViewer.setInput(DAO.localDAO().getClientsForUser(loggedIn));
			else
				clientNameViewer.setInput(DAO.localDAO().getClients(Configuration.configCenter().getString("dataScoutId")));
		} else {
			clientName = new Text(container, SWT.BORDER);
			final FormData fd_clientName = new FormData();
			fd_clientName.right = new FormAttachment(0, 477);
			fd_clientName.bottom = new FormAttachment(clientNameLabel, 0, SWT.BOTTOM);
			fd_clientName.top = new FormAttachment(clientNameLabel, 0, SWT.TOP);
			fd_clientName.left = new FormAttachment(clientNameLabel, 5, SWT.RIGHT);
			clientName.setLayoutData(fd_clientName);
			if (isCreating) {
				((Text)clientName).setEditable(true);
				clientName.setFocus();
			} else {
				((Text)clientName).setEditable(false);
			}
			clientName.addFocusListener(new FocusAdapter() {
				public void focusLost(final FocusEvent e) {
					if (((Text)clientName).getText() == null || ((Text)clientName).getText().equals("")) {
						((Text)clientName).selectAll();
						clientName.setFocus();
						setErrorMessage("You must specify a contract name");
						((EditClientWizard)getWizard()).setCanFinish(false);
						setPageComplete(false);
					} else {
						if (isCreating && DAO.localDAO().getClientByName(((Text)clientName).getText()) != null) {
							((Text)clientName).selectAll();
							clientName.setFocus();
							setErrorMessage("A contract by that name allready exists");
							((EditClientWizard)getWizard()).setCanFinish(false);
							setPageComplete(false);
						} else {
							setErrorMessage(null);
							((EditClientWizard)getWizard()).setCanFinish(true);
							setPageComplete(true);
						}
					}
				}
			});
			((Text)clientName).addModifyListener(new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					if (((Text)clientName).getText() == null || ((Text)clientName).getText().equals("")) {
						((Text)clientName).selectAll();
						clientName.setFocus();
						setErrorMessage("You must specify a contract name");
						((EditClientWizard)getWizard()).setCanFinish(false);
						setPageComplete(false);
					} else {
						setErrorMessage(null);
						((EditClientWizard)getWizard()).setCanFinish(true);
						setPageComplete(true);
					}
				}
			});
		}
		
		final Label managedByLabel = new Label(container, SWT.NONE);
		managedByLabel.setAlignment(SWT.RIGHT);
		final FormData fd_managedByLabel = new FormData();
		fd_managedByLabel.top = new FormAttachment(0, 30);
		fd_managedByLabel.bottom = new FormAttachment(0, 50);
		fd_managedByLabel.left = new FormAttachment(clientNameLabel, -85, SWT.RIGHT);
		fd_managedByLabel.right = new FormAttachment(clientNameLabel, 0, SWT.RIGHT);
		managedByLabel.setLayoutData(fd_managedByLabel);
		managedByLabel.setText("Managed by");

		managementListViewer = new ComboViewer(container, SWT.BORDER);
		managementListViewer.setSorter(new Sorter());
		managementListViewer.setLabelProvider(new ListLabelProvider());
		managementListViewer.setContentProvider(new ContentProvider());
		combo = managementListViewer.getCombo();
		combo.setEnabled(false);
		final FormData fd_combo = new FormData();
		fd_combo.top = new FormAttachment(0, 29);
		fd_combo.bottom = new FormAttachment(0, 50);
		fd_combo.right = new FormAttachment(clientNameLabel, 205, SWT.RIGHT);
		fd_combo.left = new FormAttachment(clientNameLabel, 5, SWT.RIGHT);
		combo.setLayoutData(fd_combo);
		//
		this.buildFromObject();
	}

	public boolean performFinish(User user) {
		try {
			this.populateObject();
			DAO.localDAO().saveOrUpdate(client);
			if (user != null && user.getId() > 0 && !user.getUsername().isEmpty()) {
				user.addClient(client);
				DAO.localDAO().save(user);
			}
		} catch (Exception e) {
			logger.severe("Error performing finish: "+e);
			return false;
		}
		return true;
	}
	public void buildFromObject() {
		Management current = null;
		if (client != null) {
			if (clientName instanceof Text)
				((Text)clientName).setText((client.getClientName() == null) ? "" : client.getClientName());
			else {
				if (client.getClientName() != null)
					((Combo)clientName).select(((Combo)clientName).indexOf(client.getClientName()));
				else
					((Combo)clientName).select(0);
			}
			current = client.getManagement();
		}
		User loggedIn = TracController.getLoggedIn();
		combo.setEnabled(true);
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			managementListViewer.setInput(DAO.localDAO().getAllManagements());
		} else {
			managementListViewer.setInput(DAO.localDAO().getManagementForUser(loggedIn));
		}
		if (current == null)
			combo.select(0);
		else
			combo.select(combo.indexOf(current.getCompanyName()));
	}

	public void populateObject() {
		if (clientName instanceof Text)
			client.setClientName(((Text)clientName).getText());
		else
			client.setClientName(((Combo)clientName).getText());
		Management selected = (Management)((IStructuredSelection)managementListViewer.getSelection()).getFirstElement();
		Management current = client.getManagement();
		if (current != null && current.getId() != selected.getId())
			client.setManagement(selected);
	}
	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}
