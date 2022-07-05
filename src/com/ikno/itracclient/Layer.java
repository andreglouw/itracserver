package com.ikno.itracclient;

import java.lang.reflect.Constructor;

import com.ikno.dao.business.WWLayer;

import gov.nasa.worldwind.layers.AbstractLayer;

public class Layer {
	private WWLayer wwLayer;
	private gov.nasa.worldwind.layers.Layer referenceLayer;
	
	public Layer(WWLayer wwLayer) {
		String className = wwLayer.getReferenceClass();
		try {
			Class klass = Class.forName(className);
			Constructor constr = klass.getDeclaredConstructor((Class[])null);
			gov.nasa.worldwind.layers.Layer layer = (gov.nasa.worldwind.layers.Layer)constr.newInstance((Object[])null);
			this.wwLayer = wwLayer;
			this.referenceLayer = layer;
			this.referenceLayer.setEnabled(wwLayer.isEnabled());
		} catch (Exception e) {
		}
	}
	public WWLayer getWWLayer() {
		return wwLayer;
	}
	public String getLayerName() {
		return wwLayer.getName();
	}
	public gov.nasa.worldwind.layers.Layer getReferenceLayer() {
		return (gov.nasa.worldwind.layers.Layer)wwLayer.getReferenceLayer();
	}
	public boolean isVisible() {
		return wwLayer.isVisible();
	}
	public boolean isEditable() {
		return wwLayer.isEditable();
	}
	public boolean isEnabled() {
		return wwLayer.isEnabled();
	}
	public void setEnabled(boolean enabled) {
		this.referenceLayer.setEnabled(enabled);
	}
	public String getDesription() {
		return wwLayer.getDescription();
	}
	public String toString() {
		return wwLayer.getDescription();
	}
}
