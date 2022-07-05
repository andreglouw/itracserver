package com.ikno.itracclient;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.PointIncident;

public interface IMappingAssetTracker {

	public Asset getAsset();
	public void zoomToAsset();
	public void setHistory(PointIncident[] incidents);
	public void clearHistory();
	public void removeAll();
	public void moveCurrentPoint(PointIncident newIncident);
	public boolean equals(Object o);
	public boolean isFollow();
	public void setFollow(boolean follow);
	public boolean isShowHistory();
	public void setShowHistory(boolean showLine);
	public boolean isShowLimitedHistory();
	public void setShowLimitedHistory(boolean showLine);
	
}
