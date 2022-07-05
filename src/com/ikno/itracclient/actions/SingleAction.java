package com.ikno.itracclient.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

public class SingleAction extends Action {

	public void runWithEvent(Event event) {
		if (event.doit == true) {
			super.runWithEvent(event);
			event.doit = false;
		}
	}

}
