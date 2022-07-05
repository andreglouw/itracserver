package com.ikno.itracclient.wizards;

import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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

import com.ikno.dao.business.Management;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.wizards.EditUserPage.ListLabelProvider;
import com.ikno.itracclient.wizards.EditUserPage.UserNameContentProvider;
import com.ikno.itracclient.wizards.EditUserPage.UserNameSorter;

public class ManagementPage extends WizardPage {

	public static final String ID = "com.ikno.itracclient.wizards.ManagementPage"; //$NON-NLS-1$
	private Management company;
	private boolean isCreating;
//	private Text companyName;

	class UserNameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Management)e1).getCompanyName().compareTo(((Management)e2).getCompanyName());
		}
	}
	class UserNameContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<Management>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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
	private ComboViewer companyNameViewer;
	private Control companyName;
	/**
	 * Create the wizard
	 */
	public ManagementPage(Management company, boolean isCreating) {
		super(ID);
		setTitle("Management Company");
		setDescription("Edit a management company");
		this.company = company;
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

		final Label companyLabel = new Label(container, SWT.NONE);
		companyLabel.setAlignment(SWT.RIGHT);
		final FormData fd_companyLabel = new FormData();
		fd_companyLabel.left = new FormAttachment(0, 10);
		fd_companyLabel.right = new FormAttachment(0, 65);
		fd_companyLabel.bottom = new FormAttachment(0, 30);
		fd_companyLabel.top = new FormAttachment(0, 10);
		companyLabel.setLayoutData(fd_companyLabel);
		companyLabel.setText("Company");

		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) && !isCreating) {
			companyNameViewer = new ComboViewer(container, SWT.READ_ONLY);
			companyNameViewer.setSorter(new UserNameSorter());
			companyNameViewer.setContentProvider(new UserNameContentProvider());
			companyNameViewer.setLabelProvider(new ListLabelProvider());
			companyName = companyNameViewer.getCombo();
			((Combo)companyName).setVisibleItemCount(10);
			companyNameViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(final SelectionChangedEvent event) {
					StructuredSelection sel = (StructuredSelection)event.getSelection();
					if (sel != null && sel.getFirstElement() != null) {
						((ManagementWizard)ManagementPage.this.getWizard()).setManagement((Management)sel.getFirstElement());
						ManagementPage.this.setErrorMessage(null);
						ManagementPage.this.setPageComplete(true);
					}
				}
			});
		} else {
			companyName = new Text(container, SWT.BORDER);
			((Text)companyName).addModifyListener(new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					String cpyName = ((Text)companyName).getText();
					if (cpyName == null || cpyName.isEmpty()) {
						ManagementPage.this.setErrorMessage("Please supply a valid company name");
						ManagementPage.this.setPageComplete(false);
					} else {
						ManagementPage.this.setErrorMessage(null);
						ManagementPage.this.setPageComplete(true);
					}
				}
			});
		}

		
		final FormData fd_companyName = new FormData();
		fd_companyName.bottom = new FormAttachment(0, 30);
		fd_companyName.top = new FormAttachment(0, 10);
		fd_companyName.right = new FormAttachment(0, 470);
		fd_companyName.left = new FormAttachment(0, 70);
		companyName.setLayoutData(fd_companyName);
		this.buildFromObject();
	}
	public void buildFromObject() {
		if (isCreating) {
			String cpyName = company.getCompanyName();
			((Text)companyName).setText((cpyName == null) ? "" : cpyName);
			if (cpyName == null || cpyName.isEmpty()) {
				this.setErrorMessage("Please supply a valid company name");
				this.setPageComplete(false);
			} else {
				this.setErrorMessage("");
				this.setPageComplete(true);
			}
		} else {
			User loggedIn = TracController.getLoggedIn();
			if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
				companyNameViewer.setInput(DAO.localDAO().getAllManagements());
				this.setErrorMessage("Please select the company to edit");
				this.setPageComplete(false);
			}
		}
	}
	public void populateObject() throws Exception {
		if (isCreating) {
			company.setCompanyName(((Text)companyName).getText());
		}
	}
}
