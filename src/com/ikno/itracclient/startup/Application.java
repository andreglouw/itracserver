package com.ikno.itracclient.startup;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	private static final Logger logger = Logging.getLogger(Application.class.getName());

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			try {
				String userName = System.getProperty("com.ikno.login.username");
				String password = System.getProperty("com.ikno.login.password");
				User loggedIn = null;
				if (userName != null && password != null)
					loggedIn = DAO.localDAO().getLogin(userName,password);
				if (loggedIn == null) {
					Shell shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
					LoginPanel loginPanel = new LoginPanel(shell);
					loggedIn = (User)loginPanel.open(userName);
				}
				if (loggedIn != null) {
					int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor(loggedIn));
					logger.finest("PlatformUI.createAndRunWorkbench returned "+returnCode);
					if (returnCode == PlatformUI.RETURN_RESTART) {
						return IApplication.EXIT_RESTART;
					}
				}
			} catch (Exception e) {
				MessageDialog.openError(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), "Error", "Error connecting to server.\nPlease check your network connection");
				logger.log(Level.SEVERE,"Error during createAndRunWorkbench: ",e);
			}
		} finally {
			display.dispose();
		}
		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					if (!display.isDisposed())
						workbench.close();
				} catch (Exception e) {}
			}
		});
	}
}
