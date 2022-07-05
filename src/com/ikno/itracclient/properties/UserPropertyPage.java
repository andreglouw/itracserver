package com.ikno.itracclient.properties;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;
import com.ikno.dao.persistance.PersistantObject;

public class UserPropertyPage extends PropertyPage {

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Client client = (Client)element;
			return client.getClientName();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((Set<Client>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private boolean finished = false;
	private TableViewer viewer;
	private Button administratorButton;
	private Text email;
	private Text cellPhone;
	private Text fullName;
	private Text userName;

	@Override
	public void createControl(Composite parent) {
		this.noDefaultAndApplyButton();
		super.createControl(parent);
	}

	/**
	 * Create the property page
	 */
	public UserPropertyPage() {
		super();
	}

	/**
	 * Create contents of the property page
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		
		Label loginNameLabel = new Label(container, SWT.NONE);
		loginNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_loginNameLabel = new FormData();
		fd_loginNameLabel.bottom = new FormAttachment(0, 25);
		fd_loginNameLabel.right = new FormAttachment(0, 80);
		fd_loginNameLabel.top = new FormAttachment(0, 5);
		fd_loginNameLabel.left = new FormAttachment(0, 5);
		loginNameLabel.setLayoutData(fd_loginNameLabel);
		loginNameLabel.setText("Login Name");

		userName = new Text(container, SWT.BORDER);
		userName.setEditable(false);
		final FormData fd_userName = new FormData();
		fd_userName.bottom = new FormAttachment(loginNameLabel, 0, SWT.BOTTOM);
		fd_userName.right = new FormAttachment(0, 210);
		fd_userName.top = new FormAttachment(loginNameLabel, 0, SWT.TOP);
		fd_userName.left = new FormAttachment(loginNameLabel, 5, SWT.RIGHT);
		userName.setLayoutData(fd_userName);

		Label fullNameLabel = new Label(container, SWT.NONE);
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

		Label cellPhoneLabel = new Label(container, SWT.NONE);
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
		fd_cellPhone.right = new FormAttachment(userName, 0, SWT.RIGHT);
		fd_cellPhone.bottom = new FormAttachment(cellPhoneLabel, 0, SWT.BOTTOM);
		fd_cellPhone.top = new FormAttachment(fullName, 5, SWT.BOTTOM);
		fd_cellPhone.left = new FormAttachment(cellPhoneLabel, 5, SWT.RIGHT);
		cellPhone.setLayoutData(fd_cellPhone);

		Label emailLabel = new Label(container, SWT.NONE);
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

		administratorButton = new Button(container, SWT.CHECK);
		final FormData fd_administratorButton = new FormData();
		fd_administratorButton.left = new FormAttachment(0, 262);
		fd_administratorButton.right = new FormAttachment(0, 345);
		fd_administratorButton.bottom = new FormAttachment(userName, 20, SWT.TOP);
		fd_administratorButton.top = new FormAttachment(userName, 0, SWT.TOP);
		administratorButton.setLayoutData(fd_administratorButton);
		administratorButton.setText("Administrator");
		administratorButton.setEnabled(false);

		viewer = new TableViewer(container, SWT.BORDER);
		viewer.setLabelProvider(new TableLabelProvider());
		viewer.setContentProvider(new ContentProvider());
		Table table = viewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.right = new FormAttachment(100, -5);
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.top = new FormAttachment(cellPhoneLabel, 5, SWT.BOTTOM);
		fd_table.left = new FormAttachment(cellPhoneLabel, 0, SWT.LEFT);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(100);
		newColumnTableColumn.setText("Contract");
		buildFromObject();
		//
		return container;
	}
	public void buildFromObject() {
		User user = (User)PersistantObject.resolveProxy(this.getElement().getAdapter(Object.class));
		userName.setText((user.getUsername() == null) ? "" : user.getUsername());
		fullName.setText((user.getFullname() == null) ? "" : user.getFullname());
		cellPhone.setText((user.getCellphone() == null) ? "" :user.getCellphone());
		email.setText((user.getEmail() == null) ? "" : user.getEmail());
		administratorButton.setSelection(user.fullfillsRole(User.Roles.CLIENTADMIN));
		viewer.setInput(user.getClients());
	}
	public boolean performOk() {
		if (finished == true)
			return true;
		finished = false;
		return true;
	}
}
