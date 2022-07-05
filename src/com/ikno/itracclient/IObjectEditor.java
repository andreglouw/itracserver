package com.ikno.itracclient;

import com.ikno.dao.IChangeListener;

public interface IObjectEditor {
	public void setObject(Object object);
	public String validate();
	public void populateObject();
	public void setChangeListener(IChangeListener listener);
}
