package com.ikno.itracclient.wizards;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.business.Aircraft;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.AssetSession;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.JavaRule;
import com.ikno.dao.business.SBD2Unit;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.User;
import com.ikno.dao.business.rules.JavaRuleSession;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.utils.Logging;

public class AddAssetWizard extends Wizard implements INewWizard {
	private static final Logger logger = Logging.getLogger(AddAssetWizard.class.getName());
	private boolean finished = false;

	public AddAssetWizard() {
	}
	public boolean performFinish() {
		if (finished == true)
			return true;
		finished = true;
		boolean result = true;
		try {
			DAO.localDAO().beginTransaction();
			AddUnitPage unitPage = (AddUnitPage)this.getPage(AddUnitPage.ID);
			Asset asset = null;
			Client client = null;
			Unit unit = null;
			if (unitPage != null) {
				unit = unitPage.getUnit();
			}
			AddAssetPage assetPage = (AddAssetPage)this.getPage(AddAssetPage.ID);
			asset = assetPage.getAsset();
			client = assetPage.getClient();
			if (unit != null)
				asset.addUnit(unit);
			client.addAsset(asset);
			AssetSession session = new AssetSession();
			asset.setAssetSession(session);
			session.setAsset(asset);
			JavaRuleSession ruleSession = new JavaRuleSession();
			session.setJavaRuleSession(ruleSession);
			DAO.localDAO().save(asset);
			for (Iterator<User> ui = client.getUsers().iterator();ui.hasNext();) {
				User user = ui.next();
				for (Iterator<Faction> fi = user.getFactions().iterator(); fi.hasNext();) {
					Faction faction = fi.next();
					if (faction.isDefaultFaction())
						faction.addAsset(asset);
				}
			}
			
			List<JavaRule> rules = DAO.localDAO().getPublicJavaRules();
			if (rules != null) {
				for (Iterator<JavaRule> ri = rules.iterator(); ri.hasNext();) {
					JavaRule rule = ri.next();
					if (rule.isAddedByDefault())
						asset.addRule(rule);
				}
			}
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error saving Asset:\n"+e.getMessage());
			result = false;
			DAO.localDAO().rollbackTransaction();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Save Error", "Error saving Asset.\n" +
						"Please notify System Administrator");
		}
		return result;
	}
	public boolean performCancel() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

	public void addPages() {
		Asset asset = new Aircraft();
		AddAssetPage assetPage = new AddAssetPage(asset);
		addPage(assetPage);
		AddUnitPage unitPage = new AddUnitPage(asset,new SBD2Unit());
		addPage(unitPage);
	}
}
