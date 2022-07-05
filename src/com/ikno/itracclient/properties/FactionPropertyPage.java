package com.ikno.itracclient.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Faction.FactionWrapper;
import com.ikno.dao.persistance.PersistantObject;

public class FactionPropertyPage extends PropertyPage {

	private Text factionName;

	@Override
	public void createControl(Composite parent) {
		this.noDefaultAndApplyButton();
		super.createControl(parent);
	}

	/**
	 * Create the property page
	 */
	public FactionPropertyPage() {
		super();
	}

	/**
	 * Create contents of the property page
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());

		final Label groupLabel = new Label(container, SWT.NONE);
		final FormData fd_groupLabel = new FormData();
		fd_groupLabel.bottom = new FormAttachment(0, 25);
		fd_groupLabel.top = new FormAttachment(0, 5);
		groupLabel.setLayoutData(fd_groupLabel);
		groupLabel.setAlignment(SWT.RIGHT);
		groupLabel.setText("Group Name");

		factionName = new Text(container, SWT.BORDER);
		fd_groupLabel.left = new FormAttachment(factionName, -64, SWT.LEFT);
		fd_groupLabel.right = new FormAttachment(factionName, -5, SWT.LEFT);
		final FormData fd_factionName = new FormData();
		fd_factionName.right = new FormAttachment(0, 225);
		fd_factionName.left = new FormAttachment(0, 79);
		fd_factionName.bottom = new FormAttachment(groupLabel, 19, SWT.TOP);
		fd_factionName.top = new FormAttachment(groupLabel, 0, SWT.TOP);
		factionName.setLayoutData(fd_factionName);
		factionName.setEditable(false);
		buildFromObject();
		//
		return container;
	}
	public Faction getFaction() {
		FactionWrapper wrapper = (FactionWrapper)this.getElement().getAdapter(FactionWrapper.class);
		return (Faction)PersistantObject.resolveProxy(wrapper.getObject());
	}
	public void buildFromObject() {
		Faction faction = this.getFaction();
		factionName.setText(faction.getFactionName() == null ? "" : faction.getFactionName());
	}
}
