package com.ikno.itracclient.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.TracController;
import com.swtdesigner.SWTResourceManager;

public class SendSMS extends Dialog {

	// User content providers
	private Label toUserLabel;
	private Label replyToLabel;
	private Button cancelButton;
	private Button sendButton;
	private Label cellphoneLabel;
	class UserSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((User)e1).getUsername().compareTo(((User)e2).getUsername());
		}
	}
	class UserListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((User)element).getUsername();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class UserListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<User>)inputElement).toArray();
			return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private ComboViewer userListViewer;
	private Combo combo;
	private Label characters;
	private Text originator;
	private Text messageText;
	private Text targetNumber;
	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public SendSMS(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public SendSMS(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new FormLayout());
		shell.setSize(500, 205);
		shell.setText("Send SMS");

		cellphoneLabel = new Label(shell, SWT.NONE);
		cellphoneLabel.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		cellphoneLabel.setAlignment(SWT.RIGHT);
		final FormData fd_cellphoneLabel = new FormData();
		fd_cellphoneLabel.bottom = new FormAttachment(0, 25);
		fd_cellphoneLabel.top = new FormAttachment(0, 5);
		fd_cellphoneLabel.right = new FormAttachment(0, 77);
		fd_cellphoneLabel.left = new FormAttachment(0, 5);
		cellphoneLabel.setLayoutData(fd_cellphoneLabel);
		cellphoneLabel.setText("Cellphone");

		targetNumber = new Text(shell, SWT.BORDER);
		targetNumber.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_targetNumber = new FormData();
		fd_targetNumber.bottom = new FormAttachment(cellphoneLabel, 20, SWT.TOP);
		fd_targetNumber.top = new FormAttachment(cellphoneLabel, 0, SWT.TOP);
		fd_targetNumber.right = new FormAttachment(cellphoneLabel, 155, SWT.RIGHT);
		fd_targetNumber.left = new FormAttachment(cellphoneLabel, 5, SWT.RIGHT);
		targetNumber.setLayoutData(fd_targetNumber);

		messageText = new Text(shell, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		messageText.setTextLimit(160);
		messageText.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_messageText = new FormData();
		messageText.setLayoutData(fd_messageText);
		messageText.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.character == '`') {
					e.doit = false;
				} else {
					e.doit = true;
					characters.setText(String.format("%d characters left",(160-messageText.getText().length())));
				}
			}
		});

		sendButton = new Button(shell, SWT.NONE);
		fd_messageText.bottom = new FormAttachment(sendButton, -5, SWT.TOP);
		fd_messageText.left = new FormAttachment(sendButton, -484, SWT.RIGHT);
		fd_messageText.right = new FormAttachment(sendButton, 0, SWT.RIGHT);
		sendButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				StructuredSelection selection = (StructuredSelection)userListViewer.getSelection();
				String target = null;
				String origin = null;
				if (selection != null && selection.getFirstElement() != null) {
					User selected = (User)selection.getFirstElement();
					target = selected.getCellphone();
					if (target == null || target.equals("")) {
						MessageDialog.openError(shell, "Missing Cellphone", "Selected user has no cellphone, please fix and try again");
						return;
					}
					User loggedIn = TracController.getLoggedIn();
					origin = loggedIn.getCellphone();
					if (origin == null || origin.equals("")) {
						MessageDialog.openError(shell, "Missing Cellphone", "Your own user's cellphone is not filled in, please fix and try again");
						return;
					}
				} else {
					target = targetNumber.getText();
					origin = originator.getText();
					if (target == null || target.equals("")) {
						MessageDialog.openError(shell, "Missing Cellphone", "Please supply a valid target cellphone number");
						return;
					}
					if (origin == null || origin.equals("")) {
						MessageDialog.openError(shell, "Missing Cellphone", "Please supply a valid reply-to cellphone number");
						return;
					}
				}
				String message = messageText.getText();
				if (message == null || message.equals(""))
					return;
				try {
					message = URLEncoder.encode(message, "UTF8");
				} catch (UnsupportedEncodingException e1) {
					return;
				}
				String surl = String.format("http://www.i-see.co.za/tracking/servlet/mttextservice?directNumber=%s&origin=%s&message=%s", target, origin, message);
				try {
					URL url = new URL(surl);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.connect();
					if (connection.getResponseCode() != 200) {
						return;
					}
					InputStream istream = connection.getInputStream();
					if (istream != null) {
						System.out.println("InputStream exists");
						System.out.println("Content length: "+connection.getContentLength());
					}
					connection.disconnect();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				shell.close();
			}
		});
		final FormData fd_sendButton = new FormData();
		fd_sendButton.bottom = new FormAttachment(100, -2);
		fd_sendButton.top = new FormAttachment(100, -32);
		fd_sendButton.right = new FormAttachment(100, -5);
		fd_sendButton.left = new FormAttachment(100, -44);
		sendButton.setLayoutData(fd_sendButton);
		sendButton.setText("Send");

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(100, -2);
		fd_cancelButton.top = new FormAttachment(100, -32);
		fd_cancelButton.right = new FormAttachment(100, -44);
		fd_cancelButton.left = new FormAttachment(100, -94);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		replyToLabel = new Label(shell, SWT.NONE);
		replyToLabel.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		replyToLabel.setAlignment(SWT.RIGHT);
		final FormData fd_replyToLabel = new FormData();
		fd_replyToLabel.top = new FormAttachment(targetNumber, -20, SWT.BOTTOM);
		fd_replyToLabel.bottom = new FormAttachment(targetNumber, 0, SWT.BOTTOM);
		fd_replyToLabel.right = new FormAttachment(0, 303);
		fd_replyToLabel.left = new FormAttachment(0, 230);
		replyToLabel.setLayoutData(fd_replyToLabel);
		replyToLabel.setText("Reply To");

		originator = new Text(shell, SWT.BORDER);
		originator.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_originator = new FormData();
		fd_originator.right = new FormAttachment(0, 470);
		fd_originator.top = new FormAttachment(targetNumber, -20, SWT.BOTTOM);
		fd_originator.bottom = new FormAttachment(targetNumber, 0, SWT.BOTTOM);
		fd_originator.left = new FormAttachment(0, 310);
		originator.setLayoutData(fd_originator);

		characters = new Label(shell, SWT.NONE);
		characters.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_characters = new FormData();
		fd_characters.bottom = new FormAttachment(100, -12);
		fd_characters.top = new FormAttachment(100, -32);
		fd_characters.right = new FormAttachment(0, 270);
		fd_characters.left = new FormAttachment(0, 5);
		characters.setLayoutData(fd_characters);
		characters.setText("160 characters left");

		toUserLabel = new Label(shell, SWT.NONE);
		toUserLabel.setAlignment(SWT.RIGHT);
		toUserLabel.setFont(SWTResourceManager.getFont("Verdana", 11, SWT.NONE));
		final FormData fd_toUserLabel = new FormData();
		fd_toUserLabel.bottom = new FormAttachment(0, 50);
		fd_toUserLabel.right = new FormAttachment(cellphoneLabel, 0, SWT.RIGHT);
		fd_toUserLabel.top = new FormAttachment(cellphoneLabel, 5, SWT.BOTTOM);
		fd_toUserLabel.left = new FormAttachment(cellphoneLabel, 0, SWT.LEFT);
		toUserLabel.setLayoutData(fd_toUserLabel);
		toUserLabel.setText("To User");

		userListViewer = new ComboViewer(shell, SWT.BORDER);
		combo = userListViewer.getCombo();
		fd_messageText.top = new FormAttachment(combo, 5, SWT.BOTTOM);
		final FormData fd_combo = new FormData();
		fd_combo.bottom = new FormAttachment(toUserLabel, 0, SWT.BOTTOM);
		fd_combo.right = new FormAttachment(targetNumber, 0, SWT.RIGHT);
		fd_combo.top = new FormAttachment(targetNumber, 5, SWT.BOTTOM);
		fd_combo.left = new FormAttachment(toUserLabel, 5, SWT.RIGHT);
		combo.setLayoutData(fd_combo);
		userListViewer.setSorter(new UserSorter());
		userListViewer.setLabelProvider(new UserListLabelProvider());
		userListViewer.setContentProvider(new UserListContentProvider());
		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN))
			userListViewer.setInput(DAO.localDAO().getSystemUsers());
		else
			userListViewer.setInput(DAO.localDAO().getUsersForUserClients(loggedIn));
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) || loggedIn.fullfillsRole(User.Roles.POWERUSER)) {
			originator.setEnabled(true);
			targetNumber.setEnabled(true);
		} else {
			originator.setEnabled(false);
			targetNumber.setEnabled(false);
		}
		shell.setTabList(new Control[] {cellphoneLabel, targetNumber, originator, messageText, sendButton, cancelButton, replyToLabel, characters, toUserLabel, combo});
		//
	}

}
