package com.ikno.itracclient;

import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.notification.EntityNotification.Type;

public interface ITracController {

	public void removeEntityChangeListener(IEntityChangeListener listener);
	public void addTrackListener(ITrackListener listener);
	public void removeTrackListener(ITrackListener listener);
	public void addMappingSelectListener(ViewPart listener);
	public void setCurrentMappingView(IMappingView currentMappingView);
	public IMappingView getCurrentMappingView();

}
