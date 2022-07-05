package com.ikno.itracclient.dialogs;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.MTTextMessage;
import com.ikno.dao.business.Recipient;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.UnitRecipient;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserRecipient;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class AssetMessageDialog extends Dialog {
	private Button newClientConfigButton;
	private Button clientConfigButton;
	private static final Logger logger = Logging.getLogger(AssetMessageDialog.class.getName());
	public boolean isOpen = false;
	private Point shellSize = null;
	private Point shellLocation = null;

	private Label characters;
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((AssetWrapper)e1).getAssetName().compareTo(((AssetWrapper)e2).getAssetName());
		}
	}
	class AssetListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((AssetWrapper)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class AssetListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<AssetWrapper>)inputElement).toArray();
			return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null || e2 == null)
				return 0;
			String n1 = ((Client)e1).getClientName();
			String n2 = ((Client)e2).getClientName();
			return n1.compareTo(n2);
		}
	}
	class ClientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element == null)
				return "";
			return ((Client)element).getClientName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ClientListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null)
				return new Object[]{};
			return ((List<Client>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private AssetWrapper asset = null;
	public MTTextMessage message = null;
	private Text messageText;
	private ComboViewer assetList;
	private ComboViewer clientList;
	private Combo clientCombo;
	private Combo assetCombo;
	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 * @wbp.parser.constructor
	 */
	public AssetMessageDialog(Shell parent, int style, AssetWrapper asset) {
		super(parent, style);
		this.asset = asset;
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public AssetMessageDialog(Shell parent, AssetWrapper asset) {
		this(parent, SWT.NONE, asset);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		try {
			isOpen = true;
			message = null;
			restoreState();
			createContents();
			shell.open();
			shell.layout();
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			shell.dispose();
		} catch (Exception e) {
			System.out.println("Exception: "+e);
		} finally {
			isOpen = false;
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setSize(450, 251);
		shell.setLayout(new FormLayout());
		shell.setText("Text Message");
		if (shellSize != null)
			shell.setSize(shellSize.x, shellSize.y);
		else
			shell.setSize(500, 208);
		if (shellLocation != null)
			shell.setLocation(shellLocation.x, shellLocation.y);
		shell.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				AssetMessageDialog.this.saveState();
			}
			public void controlResized(ControlEvent e) {
				AssetMessageDialog.this.saveState();
			}
		});

		final Label groupLabel = new Label(shell, SWT.NONE);
		groupLabel.setAlignment(SWT.RIGHT);
		final FormData fd_groupLabel = new FormData();
		fd_groupLabel.right = new FormAttachment(0, 60);
		fd_groupLabel.bottom = new FormAttachment(0, 25);
		fd_groupLabel.top = new FormAttachment(0, 5);
		fd_groupLabel.left = new FormAttachment(0, 5);
		groupLabel.setLayoutData(fd_groupLabel);
		groupLabel.setText("Contract");

		clientList = new ComboViewer(shell, SWT.BORDER);
		clientList.setSorter(new ClientSorter());
		clientList.setLabelProvider(new ClientListLabelProvider());
		clientList.setContentProvider(new ClientListContentProvider());
		clientCombo = clientList.getCombo();
		clientCombo.setToolTipText("Select a subset of Assets");
		clientList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				User user = TracController.getLoggedIn();
				if (selection == null || selection.getFirstElement() == null) {
					assetList.setInput(DAO.localDAO().getAssetWrappersForUser(user,true));
				} else {
					Client client = (Client)selection.getFirstElement();
					assetList.setInput(DAO.localDAO().getAssetWrappersForClient(user, client));
				}
			}
		});
		User user = TracController.getLoggedIn();
		if (asset == null) {
			List<Client> clients = user.getClients();
			if (clients == null)
				clients = new ArrayList<Client>();
			clientList.setInput(clients);
		} else {
			List<Client> clients = new ArrayList<Client>();
			clients.add(asset.getObject().getClient());
			clientList.setInput(clients);
		}
		final FormData fd_factionCombo = new FormData();
		fd_factionCombo.right = new FormAttachment(0, 330);
		fd_factionCombo.bottom = new FormAttachment(groupLabel, 21, SWT.TOP);
		fd_factionCombo.top = new FormAttachment(groupLabel, 0, SWT.TOP);
		fd_factionCombo.left = new FormAttachment(groupLabel, 5, SWT.RIGHT);
		clientCombo.setLayoutData(fd_factionCombo);

		final Label assetLabel = new Label(shell, SWT.NONE);
		assetLabel.setAlignment(SWT.RIGHT);
		final FormData fd_assetLabel = new FormData();
		fd_assetLabel.bottom = new FormAttachment(0, 50);
		fd_assetLabel.right = new FormAttachment(clientCombo, -5, SWT.LEFT);
		fd_assetLabel.top = new FormAttachment(groupLabel, 5, SWT.BOTTOM);
		fd_assetLabel.left = new FormAttachment(groupLabel, 0, SWT.LEFT);
		assetLabel.setLayoutData(fd_assetLabel);
		assetLabel.setText("Asset");

		assetList = new ComboViewer(shell, SWT.BORDER);
		assetList.setSorter(new AssetSorter());
		assetList.setLabelProvider(new AssetListLabelProvider());
		assetList.setContentProvider(new AssetListContentProvider());
		if (asset == null) {
			List<AssetWrapper> assets = DAO.localDAO().getAssetWrappersForUserAcceptingTextMessages(user,true);
			assetList.setInput(assets);
		} else {
			List<AssetWrapper> assets = new ArrayList<AssetWrapper>();
			assets.add(asset);
			assetList.setInput(assets);
		}
		assetCombo = assetList.getCombo();
		assetCombo.setToolTipText("Select the asset to receive your message");
		assetCombo.setVisibleItemCount(10);
		final FormData fd_assetCombo = new FormData();
		fd_assetCombo.right = new FormAttachment(clientCombo, 0, SWT.RIGHT);
		fd_assetCombo.bottom = new FormAttachment(0, 55);
		fd_assetCombo.top = new FormAttachment(clientCombo, 5, SWT.BOTTOM);
		fd_assetCombo.left = new FormAttachment(assetLabel, 5, SWT.RIGHT);
		assetCombo.setLayoutData(fd_assetCombo);
		
		messageText = new Text(shell, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		messageText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				characters.setText(String.format("%d characters used",(messageText.getText().length())));
			}
		});
		messageText.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.character == '`') {
					e.doit = false;
				}
			}
		});
		messageText.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_messageText = new FormData();
		fd_messageText.top = new FormAttachment(assetCombo, 5, SWT.BOTTOM);
		fd_messageText.right = new FormAttachment(100, -5);
		fd_messageText.left = new FormAttachment(assetLabel, 0, SWT.LEFT);
		messageText.setLayoutData(fd_messageText);
		messageText.setTextLimit(22000);

		final Button sendButton = new Button(shell, SWT.NONE);
		sendButton.setToolTipText("Send the message to the asset");
		sendButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)assetList.getSelection();
				if (selection != null && messageText.getText().length() > 0) {
					//String text = messageText.getText().replace("\n", "`").replace("\r", "");
					String text = messageText.getText();
					Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
					for (Unit unit : asset.getUnits()) {
						try {
							DAO.localDAO().beginTransaction();
							UserRecipient recipient = DAO.localDAO().getRecipientForUser(TracController.getLoggedIn());
							message = DAO.localDAO().loadSBD2MTMessage(unit, recipient, text, MTTextMessage.Origin.ITRAC_USER, MTTextMessage.MessageType.TEXT);
							DAO.localDAO().commitTransaction();
						} catch (Throwable exc) {
							logger.severe("Error loading SBD2 Text message: "+exc);
							DAO.localDAO().rollbackTransaction();
						}
					}
				}
				shell.close();
			}
		});
		final FormData fd_sendButton = new FormData();
		sendButton.setLayoutData(fd_sendButton);
		sendButton.setText("Send");

		characters = new Label(shell, SWT.NONE);
		characters.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		fd_messageText.bottom = new FormAttachment(100, -57);
		final FormData fd_characters = new FormData();
		fd_characters.left = new FormAttachment(0, 5);
		characters.setLayoutData(fd_characters);
		characters.setText("0 characters used");

		final Button clearButton = new Button(shell, SWT.NONE);
		fd_sendButton.bottom = new FormAttachment(clearButton, 0, SWT.BOTTOM);
		fd_sendButton.right = new FormAttachment(clearButton, 54, SWT.RIGHT);
		fd_sendButton.left = new FormAttachment(clearButton, 6);
		clearButton.setToolTipText("Clear the typed message text");
		clearButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				messageText.setText("");
				messageText.setFocus();
			}
		});
		final FormData fd_clearButton = new FormData();
		fd_clearButton.left = new FormAttachment(100, -104);
		fd_clearButton.bottom = new FormAttachment(100, -8);
		fd_clearButton.right = new FormAttachment(100, -57);
		clearButton.setLayoutData(fd_clearButton);
		clearButton.setText("Clear");
		//
		if (asset != null) {
			assetList.setSelection(new StructuredSelection(asset));
		}
		messageText.setFocus();

		clientConfigButton = new Button(shell, SWT.NONE);
		final FormData fd_clientConfigButton = new FormData();
		fd_clientConfigButton.right = new FormAttachment(assetCombo, -6);
		fd_clientConfigButton.bottom = new FormAttachment(100, -7);
		fd_clientConfigButton.left = new FormAttachment(0, 5);
		clientConfigButton.setLayoutData(fd_clientConfigButton);
		clientConfigButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)assetList.getSelection();
				if (selection != null) {
					Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
					if (asset != null) {
						List<User> users = null;
						List<Asset> assets = null;
						ConfirmObjectSelection<User> confirmUsers = new ConfirmObjectSelection<User>(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"UserSelection");
						List<User> selectableUsers = DAO.localDAO().getUsersForClient(asset.getClient());
						users = confirmUsers.open(selectableUsers);
						ConfirmObjectSelection<Asset> confirmAssets = new ConfirmObjectSelection<Asset>(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"AssetSelection");
						List<Asset> selectableAssets = DAO.localDAO().getAssetsAcceptingTextMessages();
						assets = confirmAssets.open(selectableAssets);
						List<Recipient> recipients = new ArrayList<Recipient>();
						String text = null;
						try {
							DAO.localDAO().beginTransaction();
							if (users != null) {
								for (User user : users) {
									UserRecipient recipient = DAO.localDAO().getRecipientForUser(user);
									if (recipient == null) {
										recipient = new UserRecipient(user);
										DAO.localDAO().save(recipient);
									}
									recipients.add(recipient);
								}
							}
							if (assets != null) {
								for (Asset rasset : assets) {
									for (Unit unit : rasset.getUnits()){
										UnitRecipient recipient = DAO.localDAO().getRecipientForUnit(unit);
										if (recipient == null) {
											recipient = new UnitRecipient(unit);
											DAO.localDAO().save(recipient);
										}
										recipients.add(recipient);
									}
								}
							}
							if (recipients.size() > 0) {
								text = "CONT[ADD;";
								boolean first = true;
								for (Recipient recipient : recipients) {
									if (!first)
										text = text+";";
									text = text+recipient.toString();
									first = false;
								}
								text = text+"]";
								logger.info("Contact message of "+text.length()+" will be sent to Asset's unit");
								UserRecipient origin = DAO.localDAO().getRecipientForUser(TracController.getLoggedIn());
								if (origin == null) {
									origin = new UserRecipient(TracController.getLoggedIn());
									DAO.localDAO().save(origin);
								}
								/*
								for (Unit unit : asset.getUnits())
									DAO.localDAO().loadSBD2MTMessage(unit,origin,text, MTTextMessage.Origin.ITRAC);
								*/
							}
							DAO.localDAO().commitTransaction();
						} catch (Throwable exc) {
							logger.severe("Error creating Recipients wrapping Users: "+exc);
							DAO.localDAO().rollbackTransaction();
						}
						if (text != null)
							messageText.setText(text);
					}
				}
			}
		});
		clientConfigButton.setToolTipText("Add recipients to Terminal's current client config");
		clientConfigButton.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Add Client 16.png"));
		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.ASSETADMIN))
			clientConfigButton.setEnabled(true);
		else
			clientConfigButton.setEnabled(false);

		newClientConfigButton = new Button(shell, SWT.NONE);
		fd_characters.bottom = new FormAttachment(100, -39);
		newClientConfigButton.setToolTipText("Send new client config to Terminal");
		newClientConfigButton.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/User Group 1 16 n p8.png"));
		newClientConfigButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)assetList.getSelection();
				if (selection != null) {
					Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
					if (asset != null) {
						List<User> users = null;
						List<Asset> assets = null;
						ConfirmObjectSelection<User> confirmUsers = new ConfirmObjectSelection<User>(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"UserSelection");
						List<User> selectableUsers = DAO.localDAO().getUsersForClient(asset.getClient());
						users = confirmUsers.open(selectableUsers);
						ConfirmObjectSelection<Asset> confirmAssets = new ConfirmObjectSelection<Asset>(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"AssetSelection");
						List<Asset> selectableAssets = DAO.localDAO().getAssetsAcceptingTextMessages();
						assets = confirmAssets.open(selectableAssets);
						String text = null;
						try {
							List<Recipient> recipients = new ArrayList<Recipient>();
							DAO.localDAO().beginTransaction();
							if (users != null) {
								for (User user : users) {
									UserRecipient recipient = DAO.localDAO().getRecipientForUser(user);
									if (recipient == null) {
										recipient = new UserRecipient(user);
										DAO.localDAO().save(recipient);
									}
									recipients.add(recipient);
								}
							}
							if (assets != null) {
								for (Asset rasset : assets) {
									for (Unit unit : rasset.getUnits()){
										UnitRecipient recipient = DAO.localDAO().getRecipientForUnit(unit);
										if (recipient == null) {
											recipient = new UnitRecipient(unit);
											DAO.localDAO().save(recipient);
										}
										recipients.add(recipient);
									}
								}
							}
							if (recipients.size() > 0) {
								text = "CONT[CLR;";
								boolean first = true;
								for (Recipient recipient : recipients) {
									if (!first)
										text = text+";";
									text = text+recipient.toString();
									first = false;
								}
								text = text+"]";
								logger.info("Contact message of "+text.length()+" will be sent to Asset's unit");
								UserRecipient origin = DAO.localDAO().getRecipientForUser(TracController.getLoggedIn());
								if (origin == null) {
									origin = new UserRecipient(TracController.getLoggedIn());
									DAO.localDAO().save(origin);
								}
								/*
								for (Unit unit : asset.getUnits())
									DAO.localDAO().loadSBD2MTMessage(unit,origin,text, MTTextMessage.Origin.ITRAC);
								*/
							}
							DAO.localDAO().commitTransaction();
						} catch (Throwable exc) {
							logger.severe("Error creating Recipients wrapping Users: "+exc);
							DAO.localDAO().rollbackTransaction();
						}
						if (text != null)
							messageText.setText(text);
					}
				}
			}
		});
		final FormData fd_newClientConfigButton = new FormData();
		fd_newClientConfigButton.right = new FormAttachment(0, 125);
		fd_newClientConfigButton.bottom = new FormAttachment(100, -7);
		fd_newClientConfigButton.left = new FormAttachment(0, 64);
		newClientConfigButton.setLayoutData(fd_newClientConfigButton);
		newClientConfigButton.setToolTipText("Send client config to Terminal");
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.ASSETADMIN))
			newClientConfigButton.setEnabled(true);
		else
			newClientConfigButton.setEnabled(false);
	}
	public void setAsset(AssetWrapper asset) {
		this.asset = asset;
		if (asset == null) {
			User user = TracController.getLoggedIn();
			List<AssetWrapper> assets = DAO.localDAO().getAssetWrappersForUserAcceptingTextMessages(user,true);
			assetList.setInput(assets);
		} else {
			List<AssetWrapper> assets = new ArrayList<AssetWrapper>();
			assets.add(asset);
			assetList.setInput(assets);
		}
	}
	public void saveState() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(AssetMessageDialog.class.getSimpleName());
		if (section == null)
			section = dialogSettings.addNewSection(AssetMessageDialog.class.getSimpleName());
		Point size = shell.getSize();
		section.put("shellSize", ""+size.x+","+size.y);
		Point location = shell.getLocation();
		section.put("shellLocation", ""+location.x+","+location.y);
	}
	public void restoreState() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(AssetMessageDialog.class.getSimpleName());
		if (section == null)
			section = dialogSettings.addNewSection(AssetMessageDialog.class.getSimpleName());
		try {
			String point = section.get("shellSize");
			if (point != null) {
				String[] tokens = point.split(",");
				shellSize = new Point(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]));
			}
			point = section.get("shellLocation");
			if (point != null) {
				String[] tokens = point.split(",");
				shellLocation = new Point(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]));
			}
		} catch (Exception e) {
			System.out.println("Exception in restoreState: "+e);
		}
	}

}
