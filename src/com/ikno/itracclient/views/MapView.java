package com.ikno.itracclient.views;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class MapView extends ViewPart {
	public static final String ID = "com.ikno.itracclient.views.MapView";
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
	}

	public void dispose() {
		
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
	}
	public void saveState(IMemento memento) {
		
	}
}