package com.ikno.itracclient.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import itracclient.Activator;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.utils.Formatting;

public class GeneralProperties extends Dialog {
	private Combo timezone;
	private Combo altUnits;
	private Combo latlonFormat;
	private Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public GeneralProperties(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());

		final Label formatLatlonAsLabel = new Label(container, SWT.NONE);
		formatLatlonAsLabel.setAlignment(SWT.RIGHT);
		final FormData fd_formatLatlonAsLabel = new FormData();
		fd_formatLatlonAsLabel.right = new FormAttachment(0, 110);
		fd_formatLatlonAsLabel.bottom = new FormAttachment(0, 25);
		fd_formatLatlonAsLabel.top = new FormAttachment(0, 5);
		fd_formatLatlonAsLabel.left = new FormAttachment(0, 5);
		formatLatlonAsLabel.setLayoutData(fd_formatLatlonAsLabel);
		formatLatlonAsLabel.setText("Format Lat/Lon as");

		latlonFormat = new Combo(container, SWT.NONE);
		String[] items = new String[] {Formatting.DECIMAL_DEGREES, Formatting.DEGREES_MINS_SECS, Formatting.MGRS, Formatting.UTM};
		Arrays.sort(items);
		latlonFormat.setItems(items);
		String setting = preferenceStore.getString(PreferenceConstants.LATLON_FORMAT);
		if (setting == null || setting.length() == 0) {
			setting = preferenceStore.getDefaultString(PreferenceConstants.LATLON_FORMAT);
		}
		latlonFormat.select(Arrays.binarySearch(latlonFormat.getItems(),setting));

		final FormData fd_latlonFormat = new FormData();
		fd_latlonFormat.right = new FormAttachment(0, 320);
		fd_latlonFormat.bottom = new FormAttachment(formatLatlonAsLabel, 0, SWT.BOTTOM);
		fd_latlonFormat.top = new FormAttachment(formatLatlonAsLabel, 0, SWT.TOP);
		fd_latlonFormat.left = new FormAttachment(formatLatlonAsLabel, 5, SWT.RIGHT);
		latlonFormat.setLayoutData(fd_latlonFormat);

		final Label showAltitudeInLabel = new Label(container, SWT.NONE);
		showAltitudeInLabel.setAlignment(SWT.RIGHT);
		final FormData fd_showAltitudeInLabel = new FormData();
		fd_showAltitudeInLabel.bottom = new FormAttachment(0, 45);
		fd_showAltitudeInLabel.right = new FormAttachment(latlonFormat, -5, SWT.LEFT);
		fd_showAltitudeInLabel.top = new FormAttachment(formatLatlonAsLabel, 5, SWT.BOTTOM);
		fd_showAltitudeInLabel.left = new FormAttachment(formatLatlonAsLabel, 0, SWT.LEFT);
		showAltitudeInLabel.setLayoutData(fd_showAltitudeInLabel);
		showAltitudeInLabel.setText("Show altitude in");

		altUnits = new Combo(container, SWT.NONE);
		items = new String[] {Formatting.ALT_METRES, Formatting.ALT_FEET};
		Arrays.sort(items);
		altUnits.setItems(items);
		setting = preferenceStore.getString(PreferenceConstants.ALT_UNITS);
		if (setting == null || setting.length() == 0) {
			setting = preferenceStore.getDefaultString(PreferenceConstants.ALT_UNITS);
		}
		altUnits.select(Arrays.binarySearch(altUnits.getItems(),setting));

		final FormData fd_altUnits = new FormData();
		fd_altUnits.right = new FormAttachment(0, 195);
		fd_altUnits.bottom = new FormAttachment(0, 50);
		fd_altUnits.top = new FormAttachment(latlonFormat, 5, SWT.BOTTOM);
		fd_altUnits.left = new FormAttachment(showAltitudeInLabel, 5, SWT.RIGHT);
		altUnits.setLayoutData(fd_altUnits);

		final Label timeZoneLabel = new Label(container, SWT.NONE);
		timeZoneLabel.setAlignment(SWT.RIGHT);
		final FormData fd_timeZoneLabel = new FormData();
		fd_timeZoneLabel.bottom = new FormAttachment(0, 75);
		fd_timeZoneLabel.right = new FormAttachment(altUnits, -5, SWT.LEFT);
		fd_timeZoneLabel.top = new FormAttachment(altUnits, 5, SWT.BOTTOM);
		fd_timeZoneLabel.left = new FormAttachment(showAltitudeInLabel, 0, SWT.LEFT);
		timeZoneLabel.setLayoutData(fd_timeZoneLabel);
		timeZoneLabel.setText("Time Zone");

		timezone = new Combo(container, SWT.NONE);
		timezone.setVisibleItemCount(15);
		final FormData fd_timezone = new FormData();
		fd_timezone.right = new FormAttachment(0, 425);
		fd_timezone.bottom = new FormAttachment(timeZoneLabel, 0, SWT.BOTTOM);
		fd_timezone.top = new FormAttachment(altUnits, 5, SWT.BOTTOM);
		fd_timezone.left = new FormAttachment(timeZoneLabel, 5, SWT.RIGHT);
		timezone.setLayoutData(fd_timezone);
		List<String> tzIDList = Arrays.asList(TimeZone.getAvailableIDs());
		timezone.setItems(tzIDList.toArray(new String[]{}));
		setting = preferenceStore.getString(PreferenceConstants.TIMEZONE);
		if (setting == null || setting.length() == 0) {
			setting = preferenceStore.getDefaultString(PreferenceConstants.TIMEZONE);
		}
		timezone.select(tzIDList.indexOf(setting));
		//
		return container;
	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	protected void okPressed() {
		preferenceStore.setValue(PreferenceConstants.LATLON_FORMAT, latlonFormat.getItem(latlonFormat.getSelectionIndex()));
		preferenceStore.setValue(PreferenceConstants.ALT_UNITS, altUnits.getItem(altUnits.getSelectionIndex()));
		preferenceStore.setValue(PreferenceConstants.TIMEZONE, timezone.getItem(timezone.getSelectionIndex()));
		close();
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

}
