package com.ikno.itracclient.startup;

import itracclient.Activator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.actions.BuildReportData;
import com.ikno.itracclient.actions.CreateAsset;
import com.ikno.itracclient.actions.CreateClient;
import com.ikno.itracclient.actions.CreateCompany;
import com.ikno.itracclient.actions.CreateFaction;
import com.ikno.itracclient.actions.CreateFencedArea;
import com.ikno.itracclient.actions.CreateUser;
import com.ikno.itracclient.actions.EditClient;
import com.ikno.itracclient.actions.EditCompany;
import com.ikno.itracclient.actions.EditPreferences;
import com.ikno.itracclient.actions.EditUser;
import com.ikno.itracclient.actions.EditWatcher;
import com.ikno.itracclient.dialogs.MapSelectionDialog;
import com.ikno.itracclient.dialogs.PrintDialog;
import com.ikno.itracclient.dialogs.SendSMS;
import com.ikno.itracclient.falconview.FalconView;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public static final String TC_FILE = "com.ikno.itracclient.menu.File";
	public static final String TC_ADMIN = "com.ikno.itracclient.menu.Admin";
	public static final String TC_WINDOW = "com.ikno.itracclient.menu.Window";
	public static final String TC_TOOLS = "com.ikno.itracclient.menu.Tools";
	public static final String TC_HELP = "com.ikno.itracclient.menu.Help";

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private OpenGoogleMapAction createGoogleMap = null;
	private CreateFaction createFaction = null;
	private CreateClient createClient = null;
	private EditClient editClient = null;
	private CreateUser createUser = null;
	private CreateAsset createAsset = null;
	private CreateCompany createCompany = null;
	private EditCompany editCompany = null;
	private CreateFencedArea createFencedArea = null;
	private LogoutAction logoutAction = null;
	private IContributionItem perspectives = null;
	private EditPreferences preferences = null;
	private IContributionItem views = null;
	private IWorkbenchAction quit = null;
	private EditUser editUser = null;
	private BuildReportData buildReportData = null;
	private PrintAction printAction = null;
	private EditWatcher editWatcher = null;
	private SendSMSAction sendSMSAction = null;

	private class ToBeImplemented extends Action {
		String action = null;
		private ToBeImplemented(String action) {
			this.action = action;
			setText(action);
			setToolTipText("Push to activate "+action);
		}
		public void run() {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Future", action+" still to be implemented");
		}
	}
	private class MemoryUsage extends Action {
		private MemoryUsage() {
			setText("Memory");
			setToolTipText("Show the amount of free memory");
		}
		public void run() {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Memory", ""+Runtime.getRuntime().freeMemory()+" bytes free memory");
		}
	}
	private class LogoutAction extends Action {
		private LogoutAction() {
			setText("Logout");
			setToolTipText("Push to logout and login again");
		}
		public void run() {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench == null)
				return;
			workbench.restart();
		}
	}
	private class PrintAction extends Action {
		private PrintAction() {
			setText("Report Tool");
			setToolTipText("Generate reports");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Printer 32 n p8.png"));
		}
		public void run() {
			PrintDialog printDialog = new PrintDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			printDialog.open(null);
		}
	}
	private class SendSMSAction extends Action {
		private SendSMSAction() {
			setText("Send SMS");
			setToolTipText("Send an SMS");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Cellphone 2 32 n p8.png"));
		}
		public void run() {
			SendSMS sendSMS = new SendSMS(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			sendSMS.open();
		}
	}
	private class OpenGoogleMapAction extends Action {
		private OpenGoogleMapAction() {
			setText("Open New Map");
			setToolTipText("Open New Map");
			setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/32x32/Globe.png"));
		}
		public void run() {
			MapSelectionDialog mapTypeDialog = new MapSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.NONE, new String[] {"Falcon View", "Google Earth"});
			mapTypeDialog.open();
			/*
			String mapping = System.getProperty("com.ikno.config.mapping",null);
			String viewID = null;
			int maxViews = 4;
			if (mapping.equals("WorldWind"))
				viewID = ActiveWorldWindView.ID;
			else if (mapping.equals("GoogleEarth"))
				viewID = GoogleEarth.ID;
			else if (mapping.equals("FalconView")) {
				viewID = FalconView.ID;
				maxViews = 1;
			}
			// First check if the default Google View has been closed. If so open that
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID) == null) {
				try {
					IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
				} catch (PartInitException e) {}
				return;
			}
			int index = 1;
			String altViewId = String.format(viewID+"_alt_%d", index);
			while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(viewID, altViewId) != null) {
				altViewId = String.format(viewID+"_alt_%d", ++index);
			}
			if (index > maxViews)
				return;
			try {
				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID, altViewId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				
			}
			*/
		}
	}
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		perspectives = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		preferences = new EditPreferences();
		views = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		quit = ActionFactory.QUIT.create(window);
    	createFaction = new CreateFaction();
    	createClient = new CreateClient();
    	editClient = new EditClient();
    	createUser = new CreateUser();
    	createAsset = new CreateAsset();
    	createCompany = new CreateCompany();
    	editCompany = new EditCompany();
    	createFencedArea = new CreateFencedArea();
    	editUser = new EditUser();
    	editUser.setUser(TracController.getLoggedIn());
    	logoutAction = new LogoutAction();
    	buildReportData = new BuildReportData();
    	printAction = new PrintAction();
    	editWatcher = new EditWatcher();
    	sendSMSAction = new SendSMSAction();
    	createGoogleMap = new OpenGoogleMapAction();
}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",ApplicationActionBarAdvisor.TC_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(logoutAction);
		fileMenu.add(quit);
		
		MenuManager toolsMenu = new MenuManager("&Tools",ApplicationActionBarAdvisor.TC_TOOLS);
		menuBar.add(toolsMenu);
		toolsMenu.add(preferences);
		toolsMenu.add(buildReportData);

		MenuManager windowMenu = new MenuManager("&Window",ApplicationActionBarAdvisor.TC_WINDOW);
		menuBar.add(windowMenu);
		windowMenu.add(new Separator());
	    MenuManager perspectiveMenu = new MenuManager("&Open Perspective");
	    perspectiveMenu.add(perspectives);
	    windowMenu.add(perspectiveMenu);
	    MenuManager viewMenu = new MenuManager("Show &View");
	    viewMenu.add(views);
	    windowMenu.add(viewMenu);

		MenuManager helpMenu = new MenuManager("&Help",IWorkbenchActionConstants.M_HELP);
		menuBar.add(helpMenu);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(ActionFactory.HELP_CONTENTS.create(getActionBarConfigurer().getWindowConfigurer().getWindow()));
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(new MemoryUsage());
		helpMenu.add(new ToBeImplemented("Tips and Tricks"));
	}

	protected void fillCoolBar(ICoolBarManager coolBarManager) {
		coolBarManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
		IToolBarManager appToolBar = new ToolBarManager(coolBarManager.getStyle());
		appToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));

		User loggedIn = TracController.getLoggedIn();
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			appToolBar.add(createCompany);
			appToolBar.add(editCompany);
			appToolBar.add(new Separator());
		}
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) ||
			loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) {
			appToolBar.add(createClient);
			appToolBar.add(editClient);
			appToolBar.add(createUser);
			appToolBar.add(new Separator());
		}
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN)) {
			appToolBar.add(createAsset);
		}
		if (loggedIn.fullfillsRole(User.Roles.SYSTEMADMIN) ||
			loggedIn.fullfillsRole(User.Roles.POWERUSER) ||
			loggedIn.fullfillsRole(User.Roles.ASSETADMIN) ||
			loggedIn.fullfillsRole(User.Roles.CLIENTADMIN)) {
			appToolBar.add(editWatcher);
			appToolBar.add(createFencedArea);
		}
		appToolBar.add(new Separator());
		
		appToolBar.add(editUser);
		appToolBar.add(new Separator());
		appToolBar.add(printAction);
		appToolBar.add(sendSMSAction);
		appToolBar.add(createGoogleMap);
		// Add to the cool bar manager
		coolBarManager.add(new ToolBarContributionItem(appToolBar,
				IWorkbenchActionConstants.TOOLBAR_FILE));
	}
}
