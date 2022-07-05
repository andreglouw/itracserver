package com.ikno.itracclient.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikno.dao.business.ClientMapLayer;
import com.ikno.dao.business.KMLLayer;
import com.ikno.dao.business.MapLayer;
import com.ikno.dao.business.User;
import com.ikno.dao.business.UserClient;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.mapping.layers.GeoAreaLayer;
import com.ikno.itracclient.TracController;

public class MapLayerWizard extends Wizard implements INewWizard {
	public KMLLayer mapLayer = null;
	
	public MapLayerWizard(KMLLayer mapLayer) {
		if (mapLayer == null)
			mapLayer = new KMLLayer();
		this.mapLayer = mapLayer;
	}
	@Override
	public boolean performFinish() {
		try {
			DAO.localDAO().beginTransaction();
			MapLayerPage layerPage = (MapLayerPage)this.getPage(MapLayerPage.ID);
			layerPage.populateObject();
			for (com.ikno.dao.business.Client client : layerPage.removed) {
				ClientMapLayer uc = client.getClientMapLayer(mapLayer);
				if (uc != null) {
					DAO.localDAO().delete(uc);
					client.removeClientMapLayer(uc);
					mapLayer.removeClientMapLayer(uc);
				}
			}
			for (com.ikno.dao.business.Client client : layerPage.added) {
				mapLayer.addClient(client);
				DAO.localDAO().saveOrUpdate(mapLayer);
			}
			DAO.localDAO().commitTransaction();
		} catch (Exception e) {
			DAO.localDAO().rollbackTransaction();
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

	public void addPages() {
		MapLayerPage layerPage = new MapLayerPage();
		addPage(layerPage);
	}
}
