package com.ikno.itracclient.startup;

import java.util.logging.Logger;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.falconview.FalconView;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.views.AssetIncidentView;
import com.ikno.itracclient.views.AssetStatusView;
import com.ikno.itracclient.views.AssetView;
import com.ikno.itracclient.views.ConsoleView;
import com.ikno.itracclient.views.IncidentSummaryView;
import com.ikno.itracclient.views.IncidentView;
import com.ikno.itracclient.views.InfoBarView;
import com.ikno.itracclient.views.MailboxView;
import com.ikno.itracclient.views.ReportRequestView;
import com.ikno.itracclient.views.ReportViewer;
import com.ikno.itracclient.views.RuleAssignmentView;
import com.ikno.itracclient.views.StatusSummaryView;
import com.ikno.itracclient.views.UnitIncidentView;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

public class GroupPerspective implements IPerspectiveFactory {
	private static final Logger logger = Logging.getLogger(GroupPerspective.class.getName());
	public static final String ID = "com.ikno.itracclient.groupperspective";

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		logger.finest("createInitialLayout(layout)");
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
		layout.addShowViewShortcut(AssetStatusView.ID);
		layout.addShowViewShortcut(AssetView.ID);
		layout.addShowViewShortcut(StatusSummaryView.ID);
		String mapping = System.getProperty("com.ikno.config.mapping",null);
		if (mapping.equals("WorldWind"))
			layout.addShowViewShortcut(ActiveWorldWindView.ID);
		else if (mapping.equals("GoogleEarth"))
			layout.addShowViewShortcut(GoogleEarth.ID);
		else if (mapping.equals("FalconView"))
			layout.addShowViewShortcut(FalconView.ID);
		layout.addShowViewShortcut(AssetIncidentView.ID);
		layout.addShowViewShortcut(UnitIncidentView.ID);
		layout.addShowViewShortcut(IncidentSummaryView.ID);
		layout.addShowViewShortcut(ConsoleView.ID);
		layout.addShowViewShortcut(IncidentView.ID);
		layout.addShowViewShortcut(ReportViewer.ID);
		layout.addShowViewShortcut(ReportRequestView.ID);
		layout.addShowViewShortcut(RuleAssignmentView.ID);

		layout.addPerspectiveShortcut(Perspective.ID);
		layout.addPerspectiveShortcut(LightPerspective.ID);
		layout.addPerspectiveShortcut(GroupPerspective.ID);

		/*
		layout.addStandaloneView(InfoBarView.ID, false, IPageLayout.TOP, 0.05f, editorArea);
		IViewLayout view = layout.getViewLayout(InfoBarView.ID);
		view.setCloseable(false);
		view.setMoveable(false);
		*/
		IFolderLayout bottom = layout.createFolder("com.ikno.bottom.folder", IPageLayout.BOTTOM, 0.73f, editorArea);
		bottom.addView(IncidentView.ID);
		IFolderLayout botRight = layout.createFolder("com.ikno.bottomright.folder", IPageLayout.RIGHT, 0.50f, "com.ikno.bottom.folder");
		botRight.addView(ConsoleView.ID);
		IFolderLayout left = layout.createFolder("com.ikno.left.folder", IPageLayout.LEFT, 0.72f, editorArea);
		left.addView(AssetStatusView.ID);
		left.addView(IncidentSummaryView.ID);
//		left.addView(ReportRequestView.ID);
		IFolderLayout right = layout.createFolder("com.ikno.right.folder", IPageLayout.RIGHT, 0.28f, "com.ikno.left.folder");
		if (mapping.equals("WorldWind")) {
			right.addView(ActiveWorldWindView.ID);
			IViewLayout view = layout.getViewLayout(ActiveWorldWindView.ID);
			view.setMoveable(true);
			view.setCloseable(true);
		} else if (mapping.equals("GoogleEarth")) {
			right.addView(GoogleEarth.ID);
			IViewLayout view = layout.getViewLayout(GoogleEarth.ID);
			view.setMoveable(true);
			view.setCloseable(true);
		} else if (mapping.equals("FalconView")) {
			right.addView(FalconView.ID);
			IViewLayout view = layout.getViewLayout(FalconView.ID);
			view.setMoveable(false);
			view.setCloseable(true);
		}
		right.addView(ReportViewer.ID);
		right.addView(MailboxView.ID);
		logger.finest("exit createInitialLayout(layout)");
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}
