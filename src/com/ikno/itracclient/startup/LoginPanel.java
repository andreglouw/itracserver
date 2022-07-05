package com.ikno.itracclient.startup;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import itracclient.Activator;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class LoginPanel extends Dialog {

	private String pUserName;
	private Button cancelButton;
	private Button loginButton;
	private Label userNameLabel;
	private Label passwordLabel;
	private Label loginMessage;
	private Text password;
	private Text userName;
	protected Shell shell;
	private User user = null;

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public LoginPanel(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public LoginPanel(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open(String pUserName) {
		this.pUserName = pUserName;
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return user;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.APPLICATION_MODAL);
		shell.setBackgroundImage(ResourceManager.getPluginImage(Activator.getDefault(), "images/datascout/datascout.png"));
		shell.setLayout(new FormLayout());
		shell.setText("Login");

		userNameLabel = new Label(shell, SWT.NONE);
		userNameLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		userNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_userNameLabel = new FormData();
		fd_userNameLabel.left = new FormAttachment(0, 125);
		fd_userNameLabel.right = new FormAttachment(0, 180);
		userNameLabel.setLayoutData(fd_userNameLabel);
		userNameLabel.setText("User Name");

		userName = new Text(shell, SWT.BORDER);
		userName.addFocusListener(new FocusAdapter() {
			public void focusGained(final FocusEvent e) {
				((Text)e.widget).selectAll();
			}
		});
		final FormData fd_userName = new FormData();
		fd_userName.bottom = new FormAttachment(userNameLabel, 20, SWT.TOP);
		fd_userName.top = new FormAttachment(userNameLabel, 0, SWT.TOP);
		fd_userName.right = new FormAttachment(userNameLabel, 136, SWT.RIGHT);
		fd_userName.left = new FormAttachment(userNameLabel, 5, SWT.RIGHT);
		userName.setLayoutData(fd_userName);

		passwordLabel = new Label(shell, SWT.NONE);
		passwordLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		passwordLabel.setAlignment(SWT.RIGHT);
		final FormData fd_passwordLabel = new FormData();
		fd_passwordLabel.left = new FormAttachment(userNameLabel, -55, SWT.RIGHT);
		fd_passwordLabel.right = new FormAttachment(userNameLabel, 0, SWT.RIGHT);
		fd_passwordLabel.bottom = new FormAttachment(userNameLabel, 25, SWT.BOTTOM);
		fd_passwordLabel.top = new FormAttachment(userNameLabel, 5, SWT.BOTTOM);
		passwordLabel.setLayoutData(fd_passwordLabel);
		passwordLabel.setText("Password");

		password = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		final FormData fd_password = new FormData();
		fd_password.left = new FormAttachment(userName, -131, SWT.RIGHT);
		fd_password.right = new FormAttachment(userName, 0, SWT.RIGHT);
		fd_password.bottom = new FormAttachment(userNameLabel, 25, SWT.BOTTOM);
		fd_password.top = new FormAttachment(userNameLabel, 5, SWT.BOTTOM);
		password.setLayoutData(fd_password);

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				user = null;
				shell.close();
			}
		});
		final FormData fd_cancelButton = new FormData();
		fd_cancelButton.bottom = new FormAttachment(0, 215);
		fd_cancelButton.top = new FormAttachment(password, 5, SWT.BOTTOM);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		loginButton = new Button(shell, SWT.NONE);
		fd_cancelButton.left = new FormAttachment(loginButton, -55, SWT.LEFT);
		fd_cancelButton.right = new FormAttachment(loginButton, -5, SWT.LEFT);
		loginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				String message = null;
				try {
					user = DAO.localDAO().getLogin(userName.getText(),password.getText());
					if (user != null) {
						IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
						IDialogSettings section = dialogSettings.getSection(LoginPanel.class.getSimpleName());
						if (section == null) {
							section = dialogSettings.addNewSection(LoginPanel.class.getSimpleName());
						}
						section.put("userName", userName.getText());
						shell.close();
					} else {
						message = "Invalid user/password, please try again";
					}
				} catch (Exception e1) {
					message = "Unable to connect with server. Please try again later.";
				}
				if (message != null) {
				    loginMessage.setText(message);
				    password.setText("");
				    password.setFocus();
				    return;
				}
			}
		});
		final FormData fd_loginButton = new FormData();
		fd_loginButton.left = new FormAttachment(0, 250);
		fd_loginButton.right = new FormAttachment(0, 300);
		fd_loginButton.bottom = new FormAttachment(cancelButton, 30, SWT.TOP);
		fd_loginButton.top = new FormAttachment(cancelButton, 0, SWT.TOP);
		loginButton.setLayoutData(fd_loginButton);
		loginButton.setText("Login");
		shell.setDefaultButton(loginButton);

		loginMessage = new Label(shell, SWT.CENTER | SWT.WRAP);
		loginMessage.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		fd_userNameLabel.bottom = new FormAttachment(loginMessage, 25, SWT.BOTTOM);
		fd_userNameLabel.top = new FormAttachment(loginMessage, 5, SWT.BOTTOM);
		loginMessage.setFont(SWTResourceManager.getFont("", 12, SWT.NONE));
		final FormData fd_loginMessage = new FormData();
		fd_loginMessage.right = new FormAttachment(100, -5);
		fd_loginMessage.left = new FormAttachment(0, 5);
		fd_loginMessage.bottom = new FormAttachment(0, 130);
		fd_loginMessage.top = new FormAttachment(0, 105);
		loginMessage.setLayoutData(fd_loginMessage);
		loginMessage.setText("Please enter login details");
		shell.setTabList(new Control[] {userName, password, loginButton, cancelButton, passwordLabel, loginMessage, userNameLabel});
		int width = 476;
		int height = 222;
		shell.setSize(476, 222);
		Monitor[] monitors = getParent().getDisplay().getMonitors();
		if (monitors.length == 0) {
			shell.setLocation(0,0);
		} else {
			Rectangle bounds = monitors[0].getBounds();
			int minX = (bounds.width/2)-(width/2);
			int minY = (bounds.height/2)-(height/2);
			shell.setLocation(minX,minY);
		}
		if (pUserName != null) {
			userName.setText(pUserName);
			password.setFocus();
		} else {
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			IDialogSettings section = dialogSettings.getSection(LoginPanel.class.getSimpleName());
			if (section != null) {
				String previous = section.get("userName");
				if (previous != null) {
					userName.setText(previous);
					password.setFocus();
				}
			}
		}
		//
	}
}
