package com.ikno.itracclient.preferences;

import java.awt.Color;

import org.eclipse.core.runtime.Preferences;

import com.ikno.itracclient.utils.Formatting;


import itracclient.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public static void initializeDefaultPreferences() {
		Preferences store = Activator.getDefault().getPluginPreferences();
		Color defColor = Color.WHITE;
		String defRGB = String.format("%d,%d,%d", defColor.getRed(),defColor.getGreen(),defColor.getBlue());
		store.setDefault(PreferenceConstants.PIN_COLOR, defRGB);
		store.setDefault(PreferenceConstants.TRACK_COLOR, defRGB);
		store.setDefault(PreferenceConstants.LATLON_FORMAT, Formatting.DECIMAL_DEGREES);
		store.setDefault(PreferenceConstants.ALT_UNITS, Formatting.ALT_METRES);
		store.setDefault(PreferenceConstants.TIMEZONE, "Africa/Johannesburg");
	}

}
