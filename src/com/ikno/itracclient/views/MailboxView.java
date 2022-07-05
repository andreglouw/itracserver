package com.ikno.itracclient.views;

import itracclient.Activator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.ikno.dao.ITextMessage;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.MOTextMessage;
import com.ikno.dao.business.MTTextMessage;
import com.ikno.dao.business.Management;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.User;
import com.ikno.dao.business.TextMessageResult;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.dialogs.AssetMessageDialog;
import com.ikno.itracclient.dialogs.ConfirmObjectSelection;
import com.ikno.itracclient.views.widgets.MessageBoxWidget;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.widgets.Label;

public class MailboxView extends ViewPart {
	public MailboxView() {
	}
	public static final String ID = "com.ikno.itracclient.views.MailboxView"; //$NON-NLS-1$
	private static final Logger logger = Logging.getLogger(MailboxView.class.getName());
	private Integer[] inboxColumnWidths = null;
	private Integer[] outboxColumnWidths = null;
	private MessageBoxWidget messageWidget;

	private AssetMessageDialog messageDialog;
	private Combo assetCombo;
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		//
		parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		parent.setLayout(new FormLayout());

		Button newMessageButton;
		newMessageButton = new Button(parent, SWT.NONE);
		final FormData fd_newMessageButton = new FormData();
		fd_newMessageButton.bottom = new FormAttachment(100, -5);
		newMessageButton.setLayoutData(fd_newMessageButton);
		newMessageButton.setToolTipText("Send a new message");
		newMessageButton.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Add Email 16 n p8.png"));
		newMessageButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (messageDialog == null)
					messageDialog = new AssetMessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),null);
				if (messageDialog.isOpen)
					messageDialog.getParent().setFocus();
				else
					messageDialog.open();
				MTTextMessage sent = messageDialog.message;
				if (sent != null) {
				//	outboxWidget.addMessage(new TextMessageResult(sent));
				}
			}
		});
		fd_newMessageButton.right = new FormAttachment(100);

		ComboViewer comboViewer = new ComboViewer(parent, SWT.NONE);
		final Combo combo = comboViewer.getCombo();
		combo.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NORMAL));
		combo.setVisibleItemCount(10);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String cpyName = combo.getItem(combo.getSelectionIndex());
				List<String> assets = DAO.localDAO().getAssetNamesForCompany(cpyName);
				if (assets != null && assets.size() > 0) {
					String[] sortedNames = assets.toArray(new String[assets.size()]);
					Arrays.sort(sortedNames);
					assetCombo.setItems(sortedNames);
					assetCombo.select(-1);
				}
			}
		});
		FormData fd_combo = new FormData();
		fd_combo.bottom = new FormAttachment(0, 30);
		fd_combo.right = new FormAttachment(0, 220);
		fd_combo.top = new FormAttachment(0, 7);
		fd_combo.left = new FormAttachment(0, 7);
		combo.setLayoutData(fd_combo);
				
		final Button deleteButton = new Button(parent, SWT.NONE);
		deleteButton.setToolTipText("Delete the highlighted messages");
		deleteButton.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Delete Email 16 n p8.png"));
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				TextMessageResult[] selected = messageWidget.getSelectedMessages();
				if (selected != null && selected.length > 0) {
					try {
						DAO.localDAO().beginTransaction();
						for (TextMessageResult message : selected) {
							List<TextMessageResult> msgs = null;
							if (message.isMO())
								msgs = DAO.localDAO().getMOTextMessageResultById(message.getMessageId());
							else
								msgs = DAO.localDAO().getMTTextMessageResultById(message.getMessageId());
							for (TextMessageResult result : msgs) {
							}
						}
						DAO.localDAO().commitTransaction();
						messageWidget.removeMessages(selected);
					} catch (Throwable exc) {
						logger.severe("Error deleting Text message: "+exc);
						DAO.localDAO().rollbackTransaction();
					}
				}
			}
		});
		final FormData fd_deleteButton = new FormData();
		fd_deleteButton.left = new FormAttachment(newMessageButton, -71, SWT.LEFT);
		fd_deleteButton.right = new FormAttachment(100, -70);
		fd_deleteButton.bottom = new FormAttachment(newMessageButton, 26);
		deleteButton.setLayoutData(fd_deleteButton);
		
		messageWidget = new MessageBoxWidget(parent, SWT.NONE, inboxColumnWidths);
		FormData fd_messageWidget = new FormData();
		fd_messageWidget.bottom = new FormAttachment(deleteButton, -6);
		fd_messageWidget.right = new FormAttachment(100, -12);
		fd_messageWidget.left = new FormAttachment(0, 7);
		fd_messageWidget.top = new FormAttachment(0, 40);
		messageWidget.setLayoutData(fd_messageWidget);
		messageWidget.setLayout(new FillLayout());
		
		assetCombo = new Combo(parent, SWT.NONE);
		FormData fd_assetCombo = new FormData();
		assetCombo.setLayoutData(fd_assetCombo);
		assetCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String cpyName = combo.getItem(combo.getSelectionIndex());
				String assetName = assetCombo.getItem(assetCombo.getSelectionIndex());
				companyNameSelected(cpyName, assetName);
			}
		});
		
		Label lblAsset = new Label(parent, SWT.NONE);
		fd_assetCombo.top = new FormAttachment(lblAsset, -23);
		fd_assetCombo.bottom = new FormAttachment(lblAsset, 0, SWT.BOTTOM);
		fd_assetCombo.right = new FormAttachment(lblAsset, 181, SWT.RIGHT);
		fd_assetCombo.left = new FormAttachment(lblAsset, 6);
		lblAsset.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAsset.setAlignment(SWT.RIGHT);
		FormData fd_lblAsset = new FormData();
		fd_lblAsset.top = new FormAttachment(combo, 0, SWT.TOP);
		fd_lblAsset.right = new FormAttachment(0, 295);
		fd_lblAsset.bottom = new FormAttachment(combo, 0, SWT.BOTTOM);
		fd_lblAsset.left = new FormAttachment(0, 235);
		lblAsset.setLayoutData(fd_lblAsset);
		lblAsset.setText("Asset");
		messageWidget.prepareListener(MOTextMessage.class);
		
		final User user = TracController.getLoggedIn();
		List<String> companies = DAO.localDAO().getCompanyNamesForUser(user);
		if (companies != null && companies.size() > 0) {
			String[] sortedNames = companies.toArray(new String[companies.size()]);
			Arrays.sort(sortedNames);
			combo.setItems(sortedNames);
			combo.select(0);
			String cpyName = combo.getItem(combo.getSelectionIndex());
			List<String> assets = DAO.localDAO().getAssetNamesForCompany(cpyName);
			if (assets != null && assets.size() > 0) {
				String[] sortedAssets = assets.toArray(new String[assets.size()]);
				Arrays.sort(sortedAssets);
				assetCombo.setItems(sortedAssets);
				assetCombo.select(0);
				String assetName = assetCombo.getItem(combo.getSelectionIndex());
				companyNameSelected(cpyName, assetName);
			}
		}
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	private void companyNameSelected(String cpyName, String assetName) {
		messageWidget.setSelection(cpyName, assetName);
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

	public void dispose() {
		super.dispose();
	}
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("inboxTableColumnCount");
			if (tableColumnCount != null) {
				inboxColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("inboxTableColumnWidth_"+idx);
					if (cw != null)
						inboxColumnWidths[idx] = cw; 
				}
			}
			tableColumnCount = memento.getInteger("outboxTableColumnCount");
			if (tableColumnCount != null) {
				outboxColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("outboxTableColumnWidth_"+idx);
					if (cw != null)
						outboxColumnWidths[idx] = cw; 
				}
			}
		}
	}
	public void saveState(IMemento memento) {
		if (memento != null){
			messageWidget.saveState(memento, "messages");
		}
		super.saveState(memento);
	}
}
