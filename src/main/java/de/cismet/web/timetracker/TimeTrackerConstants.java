package de.cismet.web.timetracker;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeTrackerConstants {
	public final static double HOURS_WITHOUT_AUTO_PAUSE = 6.0;
	public static final int COME = 4;
	public static final int GO = 3;
	public static final int PROJECT_SUBSEQUENT = 7;
	public static final int HOLIDAY_HOURS = 9;
	public static final int ILLNESS_HOURS = 10;
	public static final int ACCOUNT_RESET = 11;
	public static final int HOLIDAY_ADD = 12;
	public static final int PROJECT_COME = 5;
	public static final int CORRECTION = 6;
	public static final int NET_HOURS_OF_WORK_START = 14;
	public static final int NET_HOURS_OF_WORK_END = 15;
	public static final String WORKDAY_STRING = "Arbeitszeit";
	public static final String HOLIDAY_STRING = "Urlaub";
	public static final String ILLNESS_STRING = "Krank";
	public static final String CORRECTION_STRING = "Korrektur";
	public static final int PROJ_PAUSE = 0;
	public static final int WORK_DAYS_PER_WEEK = 5;
	/**
	 * formatiert ein Datum zu dem, vom Kalender benoetigten, Format
	 */
	public static SimpleDateFormat formater = new SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale.US);
	public static SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MMM.yyyy", Locale.US);
}
