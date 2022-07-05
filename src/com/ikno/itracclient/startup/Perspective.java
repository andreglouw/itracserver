package com.ikno.itracclient.startup;

import java.util.logging.Logger;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.views.AssetIncidentView;
import com.ikno.itracclient.views.AssetView;
import com.ikno.itracclient.views.ConsoleView;
import com.ikno.itracclient.views.IncidentSummaryView;
import com.ikno.itracclient.views.InfoBarView;
import com.ikno.itracclient.views.ReportRequestView;
import com.ikno.itracclient.views.ReportViewer;
import com.ikno.itracclient.views.RuleAssignmentView;
import com.ikno.itracclient.views.StatusSummaryView;
import com.ikno.itracclient.views.UnitIncidentView;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;

public class Perspective implements IPerspectiveFactory {
	private static final Logger logger = Logging.getLogger(Perspective.class.getName());
	public static final String ID = "com.ikno.itracclient.perspective";

	public void createInitialLayout(IPageLayout layout) {
		logger.finest("createInitialLayout(layout)");
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		layout.addShowViewShortcut(AssetView.ID);
		layout.addShowViewShortcut(StatusSummaryView.ID);
		String mapping = System.getProperty("com.ikno.config.mapping",null);
		if (mapping.equals("WorldWind"))
			layout.addShowViewShortcut(ActiveWorldWindView.ID);
		else if (mapping.equals("GoogleEarth"))
			layout.addShowViewShortcut(GoogleEarth.ID);
		layout.addShowViewShortcut(AssetIncidentView.ID);
		layout.addShowViewShortcut(UnitIncidentView.ID);
		layout.addShowViewShortcut(IncidentSummaryView.ID);
		layout.addShowViewShortcut(ConsoleView.ID);
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
		IFolderLayout bottom = layout.createFolder("com.ikno.bottom.folder", IPageLayout.BOTTOM, 0.78f, editorArea);
		bottom.addView(AssetIncidentView.ID);
		bottom.addView(UnitIncidentView.ID);
		bottom.addView(StatusSummaryView.ID);
		IFolderLayout botRight = layout.createFolder("com.ikno.bottomright.folder", IPageLayout.RIGHT, 0.56f, "com.ikno.bottom.folder");
		botRight.addView(ConsoleView.ID);
		IFolderLayout left = layout.createFolder("com.ikno.left.folder", IPageLayout.LEFT, 0.72f, editorArea);
		left.addView(AssetView.ID);
		left.addView(IncidentSummaryView.ID);
		left.addView(ReportRequestView.ID);
		IFolderLayout right = layout.createFolder("com.ikno.right.folder", IPageLayout.RIGHT, 0.28f, "com.ikno.left.folder");
		if (mapping.equals("WorldWind"))
			right.addView(ActiveWorldWindView.ID);
		else if (mapping.equals("GoogleEarth"))
			right.addView(GoogleEarth.ID);
		right.addView(ReportViewer.ID);
		logger.finest("exit createInitialLayout(layout)");
	}

}
