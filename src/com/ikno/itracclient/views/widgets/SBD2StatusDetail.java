package com.ikno.itracclient.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.business.SBD2Status;

public class SBD2StatusDetail extends Group {

	private Text sbdConnect;
	private Text gpsConnect;
	private Text sbdFailed;
	private Text gpsFailed;
	private Text vBat;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SBD2StatusDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());
		setText("SBD Status");

		final Label batteryLevelLabel = new Label(this, SWT.NONE);
		batteryLevelLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		batteryLevelLabel.setAlignment(SWT.RIGHT);
		final FormData fd_batteryLevelLabel = new FormData();
		fd_batteryLevelLabel.bottom = new FormAttachment(0, 25);
		fd_batteryLevelLabel.right = new FormAttachment(0, 100);
		fd_batteryLevelLabel.top = new FormAttachment(0, 5);
		fd_batteryLevelLabel.left = new FormAttachment(0, 5);
		batteryLevelLabel.setLayoutData(fd_batteryLevelLabel);
		batteryLevelLabel.setText("Battery Level");

		vBat = new Text(this, SWT.BORDER);
		vBat.setToolTipText("The level of the unit's battery in Volt");
		vBat.setEditable(false);
		final FormData fd_vBat = new FormData();
		fd_vBat.bottom = new FormAttachment(batteryLevelLabel, 0, SWT.BOTTOM);
		fd_vBat.right = new FormAttachment(0, 150);
		fd_vBat.top = new FormAttachment(batteryLevelLabel, 0, SWT.TOP);
		fd_vBat.left = new FormAttachment(batteryLevelLabel, 5, SWT.RIGHT);
		vBat.setLayoutData(fd_vBat);

		final Label gpsFailedLabel = new Label(this, SWT.NONE);
		gpsFailedLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gpsFailedLabel.setAlignment(SWT.RIGHT);
		final FormData fd_gpsFailedLabel = new FormData();
		fd_gpsFailedLabel.bottom = new FormAttachment(0, 50);
		fd_gpsFailedLabel.right = new FormAttachment(batteryLevelLabel, 0, SWT.RIGHT);
		fd_gpsFailedLabel.top = new FormAttachment(batteryLevelLabel, 5, SWT.BOTTOM);
		fd_gpsFailedLabel.left = new FormAttachment(batteryLevelLabel, 0, SWT.LEFT);
		gpsFailedLabel.setLayoutData(fd_gpsFailedLabel);
		gpsFailedLabel.setText("GPS failed count");

		gpsFailed = new Text(this, SWT.BORDER);
		gpsFailed.setToolTipText("The number of times the GPS on this unit has failed within the GPS time window");
		gpsFailed.setEditable(false);
		final FormData fd_gpsFailed = new FormData();
		fd_gpsFailed.bottom = new FormAttachment(gpsFailedLabel, 0, SWT.BOTTOM);
		fd_gpsFailed.right = new FormAttachment(0, 135);
		fd_gpsFailed.top = new FormAttachment(vBat, 5, SWT.BOTTOM);
		fd_gpsFailed.left = new FormAttachment(gpsFailedLabel, 5, SWT.RIGHT);
		gpsFailed.setLayoutData(fd_gpsFailed);

		final Label sbdFailedLabel = new Label(this, SWT.NONE);
		sbdFailedLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		sbdFailedLabel.setAlignment(SWT.RIGHT);
		final FormData fd_sbdFailedLabel = new FormData();
		fd_sbdFailedLabel.bottom = new FormAttachment(0, 75);
		fd_sbdFailedLabel.right = new FormAttachment(gpsFailedLabel, 0, SWT.RIGHT);
		fd_sbdFailedLabel.top = new FormAttachment(gpsFailedLabel, 5, SWT.BOTTOM);
		fd_sbdFailedLabel.left = new FormAttachment(gpsFailedLabel, 0, SWT.LEFT);
		sbdFailedLabel.setLayoutData(fd_sbdFailedLabel);
		sbdFailedLabel.setText("SBD failed count");

		sbdFailed = new Text(this, SWT.BORDER);
		sbdFailed.setToolTipText("The number of times the SBD modem has failed to connect within the SBD send window");
		sbdFailed.setEditable(false);
		final FormData fd_sbdFailed = new FormData();
		fd_sbdFailed.bottom = new FormAttachment(sbdFailedLabel, 0, SWT.BOTTOM);
		fd_sbdFailed.right = new FormAttachment(gpsFailed, 0, SWT.RIGHT);
		fd_sbdFailed.top = new FormAttachment(gpsFailed, 5, SWT.BOTTOM);
		fd_sbdFailed.left = new FormAttachment(sbdFailedLabel, 5, SWT.RIGHT);
		sbdFailed.setLayoutData(fd_sbdFailed);

		final Label gpsConnectionLabel = new Label(this, SWT.NONE);
		gpsConnectionLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gpsConnectionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_gpsConnectionLabel = new FormData();
		fd_gpsConnectionLabel.bottom = new FormAttachment(gpsFailed, 0, SWT.BOTTOM);
		fd_gpsConnectionLabel.right = new FormAttachment(0, 240);
		fd_gpsConnectionLabel.top = new FormAttachment(vBat, 5, SWT.BOTTOM);
		fd_gpsConnectionLabel.left = new FormAttachment(vBat, 0, SWT.RIGHT);
		gpsConnectionLabel.setLayoutData(fd_gpsConnectionLabel);
		gpsConnectionLabel.setText("GPS connect time");

		final Label sbdConnectionLabel = new Label(this, SWT.NONE);
		sbdConnectionLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		sbdConnectionLabel.setAlignment(SWT.RIGHT);
		final FormData fd_sbdConnectionLabel = new FormData();
		fd_sbdConnectionLabel.bottom = new FormAttachment(sbdFailed, 0, SWT.BOTTOM);
		fd_sbdConnectionLabel.right = new FormAttachment(gpsConnectionLabel, 0, SWT.RIGHT);
		fd_sbdConnectionLabel.top = new FormAttachment(gpsConnectionLabel, 5, SWT.BOTTOM);
		fd_sbdConnectionLabel.left = new FormAttachment(gpsConnectionLabel, 0, SWT.LEFT);
		sbdConnectionLabel.setLayoutData(fd_sbdConnectionLabel);
		sbdConnectionLabel.setText("SBD connect time");

		gpsConnect = new Text(this, SWT.BORDER);
		gpsConnect.setToolTipText("The average time in seconds for the GPS to establish a connection");
		gpsConnect.setEditable(false);
		final FormData fd_gpsConnect = new FormData();
		fd_gpsConnect.bottom = new FormAttachment(gpsConnectionLabel, 0, SWT.BOTTOM);
		fd_gpsConnect.right = new FormAttachment(0, 275);
		fd_gpsConnect.top = new FormAttachment(gpsConnectionLabel, 0, SWT.TOP);
		fd_gpsConnect.left = new FormAttachment(gpsConnectionLabel, 5, SWT.RIGHT);
		gpsConnect.setLayoutData(fd_gpsConnect);

		sbdConnect = new Text(this, SWT.BORDER);
		sbdConnect.setToolTipText("The average time in seconds for the SBD modem to establish a connection");
		sbdConnect.setEditable(false);
		final FormData fd_sbdConnect = new FormData();
		fd_sbdConnect.right = new FormAttachment(gpsConnect, 0, SWT.RIGHT);
		fd_sbdConnect.bottom = new FormAttachment(sbdConnectionLabel, 0, SWT.BOTTOM);
		fd_sbdConnect.top = new FormAttachment(gpsConnect, 5, SWT.BOTTOM);
		fd_sbdConnect.left = new FormAttachment(sbdConnectionLabel, 5, SWT.RIGHT);
		sbdConnect.setLayoutData(fd_sbdConnect);

		final Label secsLabel = new Label(this, SWT.NONE);
		secsLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_secsLabel = new FormData();
		fd_secsLabel.bottom = new FormAttachment(gpsConnect, 0, SWT.BOTTOM);
		fd_secsLabel.right = new FormAttachment(0, 305);
		fd_secsLabel.top = new FormAttachment(gpsConnect, 0, SWT.TOP);
		fd_secsLabel.left = new FormAttachment(gpsConnect, 5, SWT.RIGHT);
		secsLabel.setLayoutData(fd_secsLabel);
		secsLabel.setText("secs");

		final Label secsLabel_1 = new Label(this, SWT.NONE);
		secsLabel_1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_secsLabel_1 = new FormData();
		fd_secsLabel_1.bottom = new FormAttachment(sbdConnect, 0, SWT.BOTTOM);
		fd_secsLabel_1.right = new FormAttachment(secsLabel, 0, SWT.RIGHT);
		fd_secsLabel_1.top = new FormAttachment(secsLabel, 5, SWT.BOTTOM);
		fd_secsLabel_1.left = new FormAttachment(sbdConnect, 5, SWT.RIGHT);
		secsLabel_1.setLayoutData(fd_secsLabel_1);
		secsLabel_1.setText("secs");
		//
	}

	public void clear() {
		vBat.setText("N/A");
		gpsFailed.setText("N/A");
		sbdFailed.setText("N/A");
		gpsConnect.setText("N/A");
		sbdConnect.setText("N/A");
	}
	public void setSBD2Status(SBD2Status sbd2Status) {
		vBat.setText(String.format("%.2f", sbd2Status.getVbat()));
		gpsFailed.setText(String.format("%d", sbd2Status.getGpsFailed()));
		sbdFailed.setText(String.format("%d", sbd2Status.getSbdFailed()));
		gpsConnect.setText(String.format("%d", sbd2Status.getGpsConnect()));
		sbdConnect.setText(String.format("%d", sbd2Status.getSbdConnect()));
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
