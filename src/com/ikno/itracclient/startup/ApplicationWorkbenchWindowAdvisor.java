package com.ikno.itracclient.startup;

import itracclient.Activator;

import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.resource.ResourceManager;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private static final Logger logger = Logging.getLogger(ApplicationWorkbenchWindowAdvisor.class.getName());

	public static IViewPart getView(String id) {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				IViewReference viewReferences[] = activePage.getViewReferences();
				if (viewReferences != null) {
					for (int i = 0; i < viewReferences.length; i++) {
						if (id.equals(viewReferences[i].getId())) {
							return viewReferences[i].getView(false);
						}
					}
				}
			}
		}
		return null;
	}
	
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		logger.finest("ApplicationWorkbenchWindowAdvisor(configurer)");
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		logger.finest("createActionBarAdvisor(configurer)");
		ActionBarAdvisor result = new ApplicationActionBarAdvisor(configurer);
		logger.finest("exit createActionBarAdvisor(configurer): "+result);
		return result;
	}

	public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		logger.finest("preWindowOpen()");
//        configurer.setShowPerspectiveBar(true);
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setTitle("iTrac Client");
		logger.finest("exit preWindowOpen()");
	}

	public void postWindowOpen() {
		logger.finest("postWindowOpen()");
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.getWindow().getShell().setImage(ResourceManager.getPluginImage(Activator.getDefault(),"images/16x16/icon-earth.png"));
		logger.finest("exit postWindowOpen()");
	}
}
