package com.ikno.itracclient.startup;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.StartupThreading;
import org.eclipse.ui.internal.UISynchronizer;
import org.eclipse.ui.internal.WorkbenchConfigurer;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;

import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.falconview.FalconView;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	private static final Logger logger = Logging.getLogger(ApplicationWorkbenchAdvisor.class.getName());
	private static final String PERSPECTIVE_ID = "com.ikno.itracclient.groupperspective";
	private DBKeepAlive dbkeepAlive = null;

	public class DBKeepAlive extends Thread {
		public boolean shutdown = false;
		private Display display;
		private int interval;
		public DBKeepAlive(Display display,int interval) {
			setDaemon(true);
			setName("DB KeepAlive");
			this.display = display;
			this.interval = interval;
		}
		public void run() {
			logger.info("DBKeepAlive thread started, will schedule every "+interval+" seconds");
			while (shutdown == false) {
				final boolean[] initDone = new boolean[]{false};
				long start = Calendar.getInstance().getTimeInMillis();
				display.syncExec(new Runnable() {
					public void run() {
						logger.info("Starting scheduled DB KeepAlive process");
						try {
							DAO.localDAO().beginTransaction();
							DAO.localDAO().keepAlive();
							DAO.localDAO().commitTransaction();
						} catch (Exception e) {
							logger.log(Level.SEVERE,"Error during DB KeepAlive process",e);
							DAO.localDAO().rollbackTransaction();
						} finally {
							initDone[0] = true;
						}
					}
				});
				while (true) {
					if (initDone[0])
						break;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				long end = Calendar.getInstance().getTimeInMillis();
				long sleep = (interval*1000)-(end-start);
				if (shutdown == false && sleep > 0) {
					logger.finer("DBKeepAlive process took "+(end-start)+" milliseconds, will sleep for "+sleep);
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
					}
				} else
					logger.finer("No time for sleeping, continue processing");
			}
			logger.severe("Thread forced to shutdown, terminating");
		}
		public void shutdown() {
			this.shutdown = true;
		}
	}
	public ApplicationWorkbenchAdvisor(User loggedIn) {
		super();
		logger.finest("ApplicationWorkbenchAdvisor(loggedIn)");
		TracController.setLoggedIn(loggedIn);
	}

	public void postStartup() {
		try {
			TracController.postStartup();
		} catch (Exception e) {}
		dbkeepAlive = new DBKeepAlive(PlatformUI.getWorkbench().getDisplay(),180);
		dbkeepAlive.start();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			window.getShell().setText("iTrac Client ("+TracController.getLoggedIn().getUsername()+")");
		/*
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			String mapping = System.getProperty("com.ikno.config.mapping",null);
			String viewID = null;
			if (mapping.equals("WorldWind"))
				viewID = ActiveWorldWindView.ID;
			else if (mapping.equals("GoogleEarth"))
				viewID = GoogleEarth.ID;
			else if (mapping.equals("FalconView"))
				viewID = FalconView.ID;
			IViewPart view = activePage.findView(viewID);
			if (view == null) {
				int index = 1;
				IViewReference ref = null;
				while (ref == null && index <= 4) {
					ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(viewID, String.format(viewID+"_alt_%d", index));
					if (ref != null) {
						view = ref.getView(true);
						break;
					}
					++index;
				}
			}
			if (view != null)
				activePage.activate(view);
		}
		*/
		super.postStartup();
	}

	public boolean openWindows() {
		logger.finest("openWindows()");
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final boolean result [] = new boolean[1];
		
		// spawn another init thread.  For API compatibility We guarantee this method is called from 
		// the UI thread but it could take enough time to disrupt progress reporting.
		// spawn a new thread to do the grunt work of this initialization and spin the event loop 
		// ourselves just like it's done in Workbench.
		final boolean[] initDone = new boolean[]{false};
		Thread initThread = new Thread() {
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				try {
					//declare us to be a startup thread so that our syncs will be executed 
					UISynchronizer.startupThread.set(Boolean.TRUE);
					final IWorkbenchConfigurer [] myConfigurer = new IWorkbenchConfigurer[1];
					StartupThreading.runWithoutExceptions(new StartupRunnable() {
	
						public void runWithException() throws Throwable {
							myConfigurer[0] = getWorkbenchConfigurer();
							
						}});
					
					IStatus status = myConfigurer[0].restoreState();
					logger.finest("restoreState(status): "+status);
					if (!status.isOK()) {
						if (status.getCode() == IWorkbenchConfigurer.RESTORE_CODE_EXIT) {
							logger.finest("status == IWorkbenchConfigurer.RESTORE_CODE_EXIT");
							result[0] = false;
							return;
						}
						if (status.getCode() == IWorkbenchConfigurer.RESTORE_CODE_RESET) {
							logger.finest("status == IWorkbenchConfigurer.RESTORE_CODE_RESET");
							try {
								myConfigurer[0].openFirstTimeWindow();
							} catch (Throwable t) {
								logger.log(Level.SEVERE,"openFirstTimeWindow exception: ",t);
							}
							logger.finest("waiting for openFirstTimeWindow()");
						}
					}
					result[0] = true;
				} finally {
					initDone[0] = true;
					display.wake();
				}
			}};
			initThread.start();

			while (true) {
				if (!display.readAndDispatch()) {
					if (initDone[0])
						break;
					display.sleep();
				}
				
			}
			logger.finest("exit openWindows(): "+result[0]);
			return result[0];
	}
	public void eventLoopException(Throwable exception) {
		logger.finest("eventLoopException(exception)");
		super.eventLoopException(exception);
	}
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		logger.finest("createWorkbenchWindowAdvisor(configurer)");
		WorkbenchWindowAdvisor result = new ApplicationWorkbenchWindowAdvisor(configurer);
		logger.finest("exit createWorkbenchWindowAdvisor(configurer): "+result);
		return result;
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		logger.finest("initialize(configurer)");
		configurer.setSaveAndRestore(true);
		TracController.setConfigurer(configurer);
		logger.finest("exit initialize(configurer)");
	}

	public boolean preShutdown() {
		logger.finest("preShutdown(configurer)");
		try {
			TracController.shutdown();
		} catch (Exception e) {}
		dbkeepAlive.shutdown();
		boolean result = super.preShutdown(); 
		logger.finest("exit preShutdown(configurer)");
		return result;
	}
}
