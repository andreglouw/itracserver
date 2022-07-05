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

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.PointIncident;

public class PointIncidentDetail extends Group {

	private Label speedIndicator;
	private Text latitude;
	private Text longitude;
	private Text altitude;
	private Text speed;
	private Text heading;
	private Text location;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public PointIncidentDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FormLayout());
		setText("Location");

		final Label locationLabel = new Label(this, SWT.NONE);
		locationLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		locationLabel.setAlignment(SWT.RIGHT);
		final FormData fd_locationLabel = new FormData();
		fd_locationLabel.right = new FormAttachment(0, 65);
		fd_locationLabel.bottom = new FormAttachment(0, 25);
		fd_locationLabel.top = new FormAttachment(0, 5);
		fd_locationLabel.left = new FormAttachment(0, 5);
		locationLabel.setLayoutData(fd_locationLabel);
		locationLabel.setText("Location");

		location = new Text(this, SWT.BORDER);
		location.setEditable(false);
		location.setToolTipText("The closest available location description for this Lat/Lon, or 'N/A' if not available");
		final FormData fd_location = new FormData();
		fd_location.bottom = new FormAttachment(0, 19);
		fd_location.top = new FormAttachment(0, -1);
		fd_location.right = new FormAttachment(100, -5);
		fd_location.left = new FormAttachment(0, 70);
		location.setLayoutData(fd_location);

		final Label headingLabel = new Label(this, SWT.NONE);
		headingLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		headingLabel.setAlignment(SWT.RIGHT);
		final FormData fd_headingLabel = new FormData();
		fd_headingLabel.left = new FormAttachment(locationLabel, -60, SWT.RIGHT);
		fd_headingLabel.right = new FormAttachment(locationLabel, 0, SWT.RIGHT);
		fd_headingLabel.bottom = new FormAttachment(locationLabel, 20, SWT.BOTTOM);
		fd_headingLabel.top = new FormAttachment(locationLabel, 0, SWT.BOTTOM);
		headingLabel.setLayoutData(fd_headingLabel);
		headingLabel.setText("Heading");

		heading = new Text(this, SWT.BORDER);
		heading.setEditable(false);
		heading.setToolTipText("The heading in degrees, or 'N/A' if not available");
		final FormData fd_heading = new FormData();
		fd_heading.right = new FormAttachment(0, 125);
		fd_heading.bottom = new FormAttachment(headingLabel, 0, SWT.BOTTOM);
		fd_heading.top = new FormAttachment(location, 5, SWT.BOTTOM);
		fd_heading.left = new FormAttachment(headingLabel, 5, SWT.RIGHT);
		heading.setLayoutData(fd_heading);

		final Label speedLabel = new Label(this, SWT.NONE);
		speedLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		speedLabel.setAlignment(SWT.RIGHT);
		final FormData fd_speedLabel = new FormData();
		fd_speedLabel.right = new FormAttachment(0, 185);
		fd_speedLabel.bottom = new FormAttachment(heading, 0, SWT.BOTTOM);
		fd_speedLabel.top = new FormAttachment(heading, 0, SWT.TOP);
		fd_speedLabel.left = new FormAttachment(heading, 5, SWT.RIGHT);
		speedLabel.setLayoutData(fd_speedLabel);
		speedLabel.setText("Speed");

		speed = new Text(this, SWT.BORDER);
		speed.setEditable(false);
		speed.setToolTipText("The asset's speed, or 'N/A' if not available");
		final FormData fd_speed = new FormData();
		fd_speed.bottom = new FormAttachment(speedLabel, 0, SWT.BOTTOM);
		fd_speed.right = new FormAttachment(0, 245);
		fd_speed.top = new FormAttachment(speedLabel, 0, SWT.TOP);
		fd_speed.left = new FormAttachment(speedLabel, 5, SWT.RIGHT);
		speed.setLayoutData(fd_speed);

		speedIndicator = new Label(this, SWT.NONE);
		speedIndicator.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_speedIndicator = new FormData();
		fd_speedIndicator.right = new FormAttachment(0, 285);
		fd_speedIndicator.bottom = new FormAttachment(speed, 0, SWT.BOTTOM);
		fd_speedIndicator.top = new FormAttachment(speed, 0, SWT.TOP);
		fd_speedIndicator.left = new FormAttachment(speed, 5, SWT.RIGHT);
		speedIndicator.setLayoutData(fd_speedIndicator);

		final Label altitudeLabel = new Label(this, SWT.NONE);
		altitudeLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		altitudeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_altitudeLabel = new FormData();
		fd_altitudeLabel.left = new FormAttachment(0, 295);
		fd_altitudeLabel.bottom = new FormAttachment(speedIndicator, 0, SWT.BOTTOM);
		fd_altitudeLabel.right = new FormAttachment(0, 340);
		fd_altitudeLabel.top = new FormAttachment(speedIndicator, 0, SWT.TOP);
		altitudeLabel.setLayoutData(fd_altitudeLabel);
		altitudeLabel.setText("Altitude");

		altitude = new Text(this, SWT.BORDER);
		altitude.setEditable(false);
		altitude.setToolTipText("The altitude in metres above sea level, or 'N/A' if not available");
		final FormData fd_altitude = new FormData();
		fd_altitude.bottom = new FormAttachment(altitudeLabel, 0, SWT.BOTTOM);
		fd_altitude.right = new FormAttachment(0, 405);
		fd_altitude.top = new FormAttachment(altitudeLabel, 0, SWT.TOP);
		fd_altitude.left = new FormAttachment(altitudeLabel, 5, SWT.RIGHT);
		altitude.setLayoutData(fd_altitude);

		final Label longitudeLabel = new Label(this, SWT.NONE);
		longitudeLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		longitudeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_longitudeLabel = new FormData();
		fd_longitudeLabel.bottom = new FormAttachment(0, 70);
		fd_longitudeLabel.right = new FormAttachment(headingLabel, 0, SWT.RIGHT);
		fd_longitudeLabel.top = new FormAttachment(headingLabel, 5, SWT.BOTTOM);
		fd_longitudeLabel.left = new FormAttachment(headingLabel, 0, SWT.LEFT);
		longitudeLabel.setLayoutData(fd_longitudeLabel);
		longitudeLabel.setText("Longitude");

		longitude = new Text(this, SWT.BORDER);
		longitude.setEditable(false);
		final FormData fd_longitude = new FormData();
		fd_longitude.right = new FormAttachment(0, 165);
		fd_longitude.bottom = new FormAttachment(longitudeLabel, 0, SWT.BOTTOM);
		fd_longitude.top = new FormAttachment(heading, 5, SWT.BOTTOM);
		fd_longitude.left = new FormAttachment(longitudeLabel, 5, SWT.RIGHT);
		longitude.setLayoutData(fd_longitude);

		final Label latitudeLabel = new Label(this, SWT.NONE);
		latitudeLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		latitudeLabel.setAlignment(SWT.RIGHT);
		final FormData fd_latitudeLabel = new FormData();
		fd_latitudeLabel.right = new FormAttachment(0, 230);
		fd_latitudeLabel.bottom = new FormAttachment(longitude, 0, SWT.BOTTOM);
		fd_latitudeLabel.top = new FormAttachment(speedLabel, 5, SWT.BOTTOM);
		fd_latitudeLabel.left = new FormAttachment(longitude, 5, SWT.RIGHT);
		latitudeLabel.setLayoutData(fd_latitudeLabel);
		latitudeLabel.setText("Latitude");

		latitude = new Text(this, SWT.BORDER);
		latitude.setEditable(false);
		final FormData fd_latitude = new FormData();
		fd_latitude.right = new FormAttachment(0, 330);
		fd_latitude.bottom = new FormAttachment(latitudeLabel, 0, SWT.BOTTOM);
		fd_latitude.top = new FormAttachment(speedIndicator, 5, SWT.BOTTOM);
		fd_latitude.left = new FormAttachment(latitudeLabel, 5, SWT.RIGHT);
		latitude.setLayoutData(fd_latitude);
		//
	}

	public Composite stackView() {
		return this;
	}

	public void clear() {
		heading.setText("N/A");
		speed.setText("N/A");
		speedIndicator.setText("N/A");
		altitude.setText("N/A");
		longitude.setText("N/A");
		latitude.setText("N/A");
		location.setText("N/A");
	}
	public void setPointIncident(PointIncident point) {
		if (point == null)
			return;
		heading.setText(String.format("%d",point.getCourse()));
		speed.setText(String.format("%.0f", point.getAssetSpeed()));
		speedIndicator.setText(point.getAssetSpeedIndicator());
		altitude.setText(String.format("%.0f", point.getAltitude()));
		longitude.setText(String.format("%.5f", point.getLongitude()));
		latitude.setText(String.format("%.5f", point.getLatitude()));
		location.setText(point.getLocation());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
