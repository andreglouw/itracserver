package com.ikno.itracclient.wizards;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.business.Unit;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.utils.Logging;

public class AddUnitWizard extends Wizard implements INewWizard {
	private static final Logger logger = Logging.getLogger(AddUnitWizard.class.getName());

	private Unit unit = null;
	private Asset asset = null;
	private boolean isCreating = false;
	private boolean finished = false;
	
	public AddUnitWizard(Asset asset, Unit unit, boolean isCreating) {
		this.asset = asset;
		this.unit = unit;
		this.isCreating = isCreating;
	}
	public AddUnitWizard(Asset asset) {
		this(asset,new SBD2Unit(),true);
	}
	@Override
	public boolean performFinish() {
		if (finished == true)
			return true;
		finished = false;
		try {
			DAO.localDAO().beginTransaction();
			AddUnitPage unitPage = (AddUnitPage)this.getPage(AddUnitPage.ID);
			Unit unit = unitPage.getUnit();
			asset.addUnit(unit);
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error Saving Unit:"+e.getMessage());
			DAO.localDAO().rollbackTransaction();
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	public void addPages() {
		AddUnitPage unitPage = new AddUnitPage(asset,unit);
		addPage(unitPage);
	}
	@Override
	public boolean canFinish() {
		if (isCreating)
			return false;
		return true;
	}
}
