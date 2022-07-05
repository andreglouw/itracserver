package com.ikno.itracclient.actions;

import java.io.InputStream;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.reporting.Report;
import com.ikno.dao.hibernate.DAO;

public class BuildReportData extends SingleAction {

	public BuildReportData() {
		setText("Build Report Data");
		setToolTipText("Build Report Data");
	}
	public void run() {
		if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Confirm update", 
				"Are you sure you want to update all Report Database representations with their current Disk representation?"))
			return;
		try {
			DAO.localDAO().beginTransaction();
			List<Report> reports = DAO.localDAO().getAllReports(false);
			for (Report report : reports) {
				InputStream istream = report.reportStreamFromFile();
				if (istream != null) {
					try {
						byte[] bytes = new byte[istream.available()];
						istream.read(bytes);
						report.setReportData(bytes);
						DAO.localDAO().save(report);
					} catch (Exception exc) {
						System.out.println("Error reading from Report "+report.getReportName()+"'s stream: "+exc);
					}
				}
			}
			DAO.localDAO().commitTransaction();
		} catch (Throwable e) {
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Update Error", "Error updating reports:\n"+e.getMessage());
			e.printStackTrace();
		}
	}

}
