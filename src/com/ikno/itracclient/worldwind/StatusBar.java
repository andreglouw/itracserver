package com.ikno.itracclient.worldwind;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.OrbitView;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import com.ikno.itracclient.TracController;
import com.ikno.itracclient.utils.Formatting;

class StatusBar implements PositionListener {
	StatusLineContributionItem latlonItem;
	StatusLineContributionItem eleItem;
	StatusLineContributionItem eyeItem;
	private SubStatusLineManager statusManager;
	private WorldWindow eventSource;

	StatusBar(final IStatusLineManager statusLineManager) {
		this.latlonItem = new StatusLineContributionItem("latlonItem",true,30); //$NON-NLS-1$
		this.eleItem = new StatusLineContributionItem("eleItem",true,15); //$NON-NLS-1$
		this.eyeItem = new StatusLineContributionItem("eyeItem",true,18); //$NON-NLS-1$
		this.statusManager = new SubStatusLineManager(statusLineManager);
		this.statusManager.add(this.latlonItem);
		this.statusManager.add(this.eleItem);
		this.statusManager.add(this.eyeItem);
		this.statusManager.setVisible(true);
	}

	public void moved( final PositionEvent event ) {
		this.handleCursorPositionChange(event);
	}

	private void handleCursorPositionChange( final PositionEvent event ) {
		final Position newPos = event.getPosition();
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
				if (newPos != null) {
					String lls = Formatting.formatLatLon(newPos.getLatitude().getDegrees(),newPos.getLongitude().getDegrees());
					String els = Formatting.formatElevation(newPos.getElevation());
			        OrbitView orbitView = (OrbitView)StatusBar.this.eventSource.getView();
			        String eye = "Eye: N/A";
			        if (orbitView != null) {
						eye = Formatting.formatEyeAltitude(orbitView.getZoom());
			        }
					StatusBar.this.latlonItem.setText( lls );
					StatusBar.this.eleItem.setText( els );
					StatusBar.this.eyeItem.setText( eye );
				} else {
					StatusBar.this.latlonItem.setText( "" ); //$NON-NLS-1$
					StatusBar.this.eleItem.setText( "" ); //$NON-NLS-1$
					StatusBar.this.eyeItem.setText( "" ); //$NON-NLS-1$
				}
			}
		} );
	}

	void setEventSource( final WorldWindow newEventSource ) {
		if (this.eventSource != null) {
			this.eventSource.removePositionListener(this);
		}

		if (newEventSource != null) {
			newEventSource.addPositionListener(this);
		}

		this.eventSource = newEventSource;
	}            
}