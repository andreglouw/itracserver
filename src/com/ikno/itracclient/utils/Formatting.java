package com.ikno.itracclient.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import itracclient.Activator;

import org.eclipse.core.runtime.Preferences;

import com.ikno.dao.hibernate.DAO;
import com.ikno.itracclient.preferences.PreferenceConstants;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globes.Globe;

public class Formatting {

	private static Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
	
	public static final String DECIMAL_DEGREES = "Decimal degrees";
	public static final String DEGREES_MINS_SECS = "Degrees,mins,secs";
	public static final String MGRS = "Military Grid Ref System (MGRS)";
	public static final String UTM = "Universal Transverse Mercator Grid Ref System (UTM)";
	public static final String STRING_HEADING = "Text Heading";
	public static final String DEGREES_HEADING = "Heading in degrees";
	public static final String ALT_METRES = "Metres";
	public static final String ALT_FEET = "Feet";
    private final static double METER_TO_FEET = 3.280839895;
    private final static double METER_TO_MILE = 0.000621371192;

	public static String formatLatLon(double latitude, double longitude) {
		String setting = preferenceStore.getString(PreferenceConstants.LATLON_FORMAT);
		String las = "";
		String los = "";
		if (setting.equals(Formatting.DECIMAL_DEGREES)) {
			las = String.format("%.5f\u00B0 %s", latitude, (latitude < 0.0) ? "S" : "N");
			los = String.format("%.5f\u00B0 %s", longitude, (longitude < 0.0) ? "W" : "E");
			return las+"   "+los;
		} else if (setting.equals(Formatting.MGRS)) {
            MGRSCoord MGRS = MGRSCoord.fromLatLon(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), (Globe)null);
            return MGRS.toString();
		} else if (setting.equals(Formatting.UTM)) {
			UTMCoord UTM = UTMCoord.fromLatLon(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), (Globe)null);
            return UTM.toString();
		} else {
			double nlatitude = java.lang.Math.abs(latitude);
			int deg = (int)nlatitude;
			double real = nlatitude-deg;
			double min = 60.0*real;
			real = min-(int)min;
			int sec = (int)(60.0*real);
			las = String.format("%02d\u00B0%02d'%02d\" %s", deg,(int)min,sec,(latitude < 0.0) ? "S" : "N");
			double nlongitude = java.lang.Math.abs(longitude);
			deg = (int)nlongitude;
			real = nlongitude-deg;
			min = 60.0*real;
			real = min-(int)min;
			sec = (int)(60.0*real);
			los = String.format("%03d\u00B0%02d'%02d\" %s", deg,(int)min,sec,(longitude < 0.0) ? "W" : "E");
			return las+"   "+los;
		}
	}
	public static String formatElevation(double altitude) {
		String setting = preferenceStore.getString(PreferenceConstants.ALT_UNITS);
		if (setting.equals(Formatting.ALT_METRES))
			return String.format("Elev: %d m", (int)altitude);
		return String.format("Elev: %d ft", (int)(altitude * METER_TO_FEET));
	}
	public static String formatEyeAltitude(double altitude) {
		String setting = preferenceStore.getString(PreferenceConstants.ALT_UNITS);
		if (setting.equals(Formatting.ALT_METRES)) {
			if (altitude < 10000)
				return String.format("Eye: %d m", (int)Math.round(altitude));
			return String.format("Eye: %d km", (int)Math.round(altitude / 1e3));
		}
		if (altitude < 10000)
			return String.format("Eye: %d ft", (int)Math.round(altitude * METER_TO_FEET));
		return String.format("Eye: %d mi", (int)Math.round(altitude * METER_TO_MILE));
	}
	public static String formatHeading(double heading) {
		String setting = preferenceStore.getString(PreferenceConstants.HEADING_FORMAT);
		return String.format("Head: %.0f\u00B0", heading);
	}
	public static String format(Date date, String format, TimeZone localTimezone) {
		SimpleDateFormat form = new SimpleDateFormat(format);
		form.setTimeZone(localTimezone);
		return form.format(date);
	}
	public static String format(Date date, String format) {
		return format(date,format,TimeZone.getTimeZone(preferenceStore.getString(PreferenceConstants.TIMEZONE)));
	}
	public static String format(Date date) {
		return format(date,"yyyy/MM/dd HH:mm:ss",TimeZone.getTimeZone(preferenceStore.getString(PreferenceConstants.TIMEZONE)));
	}
	public static void main(String[] args) {
	}
}
