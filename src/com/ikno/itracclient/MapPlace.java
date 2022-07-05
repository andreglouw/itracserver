package com.ikno.itracclient;

import itracclient.Activator;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Color;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.PointIncident;
import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.utils.AWTImageBuilder;

public class MapPlace {
	private IMappingView mappingView;
	private PointIncident incident;
	private double _latitude;
	private double _longitude;
	private double _altitude;
	private double _elevation;
	public boolean anchoring = false;
	
	public MapPlace(IMappingView mappingView, PointIncident incident) {
		this.mappingView = mappingView;
		this.incident = incident;
		this._latitude = incident.getLatitude();
		this._longitude = incident.getLongitude();
		this._altitude = incident.getAltitude();
		this._elevation = 0;
		this.anchorAltitude();
	}
	public String getImageHref() {
		Asset asset = this.getAsset();
		return "http://www.i-see.co.za/images/"+asset.getRotatedImageName(incident,asset.assetImageName());
	}
	public BufferedImage getImage() throws Exception {
		AWTImageBuilder imageBuilder = new AWTImageBuilder(24,24);
		// Check if the asset has an overriding image stored in the database
		Asset asset = this.getAsset();
		BufferedImage result = (BufferedImage)asset.getImage(incident, imageBuilder);
		if (result == null) {
			String imagePath = "images/"+asset.getRotatedImageName(incident,asset.assetImageName());
			if (imagePath != null) {
				try {
					URL url = ResourceManager.getPluginImageURL(Activator.getDefault(),imagePath);
					InputStream is = url.openStream();
					return imageBuilder.buildImage(ImageIO.read(is));
				} catch (Exception e) {}
			}
		}
		return result;
	}
	public String getRotationImageHref() {
		Asset asset = this.getAsset();
		return "http://www.i-see.co.za/images/"+asset.getRotatedImageName(incident,asset.historyImageName());
	}
	public BufferedImage getRotationImage() throws Exception {
		Asset asset = this.getAsset();
		String imagePath = "images/"+asset.getRotatedImageName(incident, asset.historyImageName());
		if (imagePath != null) {
			try {
				URL url = ResourceManager.getPluginImageURL(Activator.getDefault(),imagePath);
				InputStream is = url.openStream();
				return new AWTImageBuilder().buildImage(ImageIO.read(is), 16, 16);
			} catch (Exception e) {}
		}
		return null;
	}
	public double getDegreesLatitude() {
		return this._latitude;
	}
	public void setDegreesLatitude(double value) {
		this._latitude = value;
	}
	public double getDegreesLongitude() {
		return this._longitude;
	}
	public void setDegreesLongitude(double value) {
		this._longitude = value;
	}
	public double getAltitude() {
		return this._altitude;
	}
	public void setAltitude(double value) {
		this._altitude = value;
		this.anchorAltitude();
	}
	public double getElevation() {
		return this._elevation;
	}
	public void setElevation(double value) {
		this._elevation = value;
		this.anchorAltitude();
	}
	public void anchorAltitude() {
		if (!anchoring) {
			try {
				anchoring = true;
				this.mappingView.anchorAltitude(this);
			} finally {
				anchoring = false;
			}
		}
	}
	public PointIncident getIncident() {
		return incident;
	}
	public void setIncident(PointIncident incident) {
		this.incident = incident;
	}
	public Asset getAsset() {
		return this.incident.getAsset();
	}
	public Date getTimestamp() {
		return incident.getTimestamp();
	}
	public Color getPinColor() {
		return TracController.pinColor(getAsset());
	}
	public String getShortDescr() {
		return this.getAsset().getAssetName();
	}
	public String getFullDescr() {
		return this.getAsset().fullDescription(incident, (float)getAltitude());
	}
}
