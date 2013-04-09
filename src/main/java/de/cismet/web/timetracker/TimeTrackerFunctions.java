/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.GregorianCalendar;
import java.util.Scanner;

import de.cismet.web.timetracker.types.TimesheetEntry;
import de.cismet.web.timetracker.types.TimesheetSet;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class TimeTrackerFunctions {

    //~ Static fields/initializers ---------------------------------------------

    private static final char[] chars = {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        };

    //~ Methods ----------------------------------------------------------------

    /**
     * Berechnet die Differenz der beiden uebergebenen Zeiten.
     *
     * @param   calA  Zeit1
     * @param   calB  Zeit2
     *
     * @return  die Differenz der beiden uebergebenen Zeiten in Stunden
     */
    public static double calcDifferenceInHours(final GregorianCalendar calA, final GregorianCalendar calB) {
        final long differenceInMillis = (calA.getTimeInMillis() - calB.getTimeInMillis());
        return Math.abs(differenceInMillis / (1000.0 * 3600.0));
    }

    /**
     * Ueberprueft, ob noch, fuer die Dauer der Tagesarbeitszeit, Gehen-Buchungen im ResultSet vorhanden sind.
     *
     * @param   set  rSet das zu ueberpruefende ResultSet
     *
     * @return  true, wenn im RseultSet noch eine Gehen-Buchung oder Kommen-Buchung folgt
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static boolean isLastGo(final TimesheetSet set) throws SQLException {
        int i = 1; // weil beim letzten rSet.next() die Schleife nicht betreten wird
        boolean lastGo = true;
        TimesheetEntry entry;

        while ((entry = set.next()) != null) {
            i++;
            if (entry.getAction() == TimeTrackerConstants.GO) {
                lastGo = false;
                --i; // Aktion aus Kommentar oben findet nicht statt.
                break;
            }
        }

        for (; i > 0; --i) {
            set.previous();
        }
        return lastGo;
    }

    /**
     * Ueberprueft, ob noch, fuer die Dauer der Tagesarbeitszeit, Gehen-Buchungen im ResultSet vorhanden sind.
     *
     * @param   set  rSet das zu ueberpruefende ResultSet
     * @param   n    DOCUMENT ME!
     *
     * @return  true, wenn im RseultSet noch eine Gehen-Buchung oder Kommen-Buchung folgt
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static boolean isLastGo(final ResultSet set, final int n) throws SQLException {
        int i = 1; // weil beim letzten rSet.next() die Schleife nicht betreten wird
        boolean lastGo = true;
        // TimesheetEntry entry;

        while (set.next()) {
            i++;
            if (set.getInt(n) == TimeTrackerConstants.GO) {
                lastGo = false;
                --i; // Aktion aus Kommentar oben findet nicht statt.
                break;
            }
        }

        for (; i > 0; --i) {
            set.previous();
        }
        return lastGo;
    }

    /**
     * Ueberprueft, ob noch, fuer die Dauer der Tagesarbeitszeit, relevante Datens�tze im ResultSet vorhanden sind.
     *
     * @param   set  rSet das zu ueberpruefende ResultSet
     *
     * @return  true, wenn im RseultSet noch eine Gehen-Buchung oder Kommen-Buchung folgt
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static boolean hasRelevantEntry(final TimesheetSet set) throws SQLException {
        int i = 1; // weil beim letzten rSet.next() die Schleife nicht betreten wird
        boolean relevantEntryExist = false;
        TimesheetEntry entry;

        while ((entry = set.next()) != null) {
            i++;
            if ((entry.getAction() == TimeTrackerConstants.COME) || (entry.getAction() == TimeTrackerConstants.GO)) {
                relevantEntryExist = true;
                --i; // Aktion aus Kommentar oben findet nicht statt.
                break;
            }
        }

        for (; i > 0; --i) {
            set.previous();
        }
        return relevantEntryExist;
    }

    /**
     * �berpr�ft, ob noch, f�r die Dauer der Tagesarbeitszeit, relevante Datens�tze im ResultSet vorhanden sind.
     *
     * @param   rSet         das zu �berpr�fende ResultSet
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  true, wenn im RseultSet noch eine Gehen-Buchung oder Kommen-Buchung folgt
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static boolean hasRelevantEntry(final ResultSet rSet, final int columnIndex) throws SQLException {
        int i = 1; // weil beim letzten rSet.next() die Schleife nicht betreten wird
        boolean relevantEntryExist = false;

        while (rSet.next()) {
            i++;
            if ((rSet.getInt(columnIndex) == TimeTrackerConstants.COME)
                        || (rSet.getInt(columnIndex) == TimeTrackerConstants.GO)) {
                relevantEntryExist = true;
                --i; // Aktion aus Kommentar oben findet nicht statt.
                break;
            }
        }

        for (; i > 0; --i) {
            rSet.previous();
        }
        return relevantEntryExist;
    }

    /**
     * konvertiert den �bergebenen Wert in einen String, der einen Zeitintervall angibt Variable isSigned bestimmt, ob
     * ein positives Vorzeichen ins Ergebnis geschrieben wird.
     *
     * @param   timeAsDouble  DOCUMENT ME!
     * @param   isSigned      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String convert2Time(double timeAsDouble, final boolean isSigned) {
        final boolean isNegativ = (timeAsDouble < 0);
        timeAsDouble = Math.abs(timeAsDouble);

        final int hours = (int)timeAsDouble;
        final int minutes = (int)((timeAsDouble - hours) * 60.0);
        return (isNegativ ? "-" : (isSigned ? "+" : "")) + hours + ":"
                    + ((minutes < 10) ? ("0" + minutes) : ("" + minutes));
    }

    /**
     * liefert das �bergebene Datum als Postrgres-Datumszeichenkette.
     *
     * @param   date  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getDateString(final GregorianCalendar date) {
        final int month = (date.get(GregorianCalendar.MONTH) + 1);
        final int day = date.get(GregorianCalendar.DATE);
        final StringBuffer dateAsString = new StringBuffer(date.get(GregorianCalendar.YEAR) + "-");

        dateAsString.append(((month < 10) ? ("0" + month) : ("" + month)));
        dateAsString.append(((day < 10) ? ("" + "-0" + day) : ("-" + day)));

        return dateAsString.toString();
    }

    /**
     * liefert den ersten Tag der Woche, in der der �bergebene Tag enthalten ist.
     *
     * @param   day  Tag, der die gew�nschte Woche kennzeichnet.
     *
     * @return  der erste Tag der Woche, in der der �bergebene Tag enthalten ist
     */
    public static GregorianCalendar getFirstDayOfWeek(final GregorianCalendar day) {
        final GregorianCalendar firstDay = (GregorianCalendar)day.clone();
        while (firstDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            firstDay.add(GregorianCalendar.DATE, -1);
        }
        return firstDay;
    }

    /**
     * liefert den letzten Tag der Woche, in der der �bergebene Tag enthalten ist.
     *
     * @param   day  Tag, der die gew�nschte Woche kennzeichnet.
     *
     * @return  der letzte Tag der Woche, in der der �bergebene Tag enthalten ist
     */
    public static GregorianCalendar getLastDayOfWeek(final GregorianCalendar day) {
        final GregorianCalendar lastDay = (GregorianCalendar)day.clone();
        while (lastDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY) {
            lastDay.add(GregorianCalendar.DATE, 1);
        }
        return lastDay;
    }

    /**
     * liefert die Differenz zweier GregorianCalendar in Tagen. Wobei beide Tage im selben Monat sein m�ssen Wenn dayA >
     * dayB, dann wird der R�ckgabewert negativ
     *
     * @param   dayA  Datum 1
     * @param   dayB  Datum 2
     *
     * @return  die Differenz zweier GregorianCalendar in Tagen
     */
    public static int getDifferenceInDays(final GregorianCalendar dayA, final GregorianCalendar dayB) {
        int days = 0;
        int add;

        final GregorianCalendar tmp = (GregorianCalendar)dayA.clone();

        if (dayA.compareTo(dayB) > 0) {
            add = -1;
        } else {
            add = 1;
        }

        while (tmp.get(GregorianCalendar.DATE) != dayB.get(GregorianCalendar.DATE)) {
            tmp.add(GregorianCalendar.DATE, add);
            days += add;
        }

        return days;
    }

    /**
     * konvertiert einen String in eine Zahl. Falls dies nicht moeglich ist, dann wird ein Standardwert zurueckgegeben.
     *
     * @param   value         Wert, der konvertiert werden soll
     * @param   defaultValue  Wert, der im Fehlerfall zurueckgegeben wird
     *
     * @return  die konvertierte Zahl
     */
    public static int parseStringToInt(final String value, final int defaultValue) {
        int result;

        try {
            if (value != null) {
                result = Integer.parseInt(value);
            } else {
                result = defaultValue;
            }
        } catch (NumberFormatException e) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * liefert den zur uebergebenen Monatsnummer geh�renden Namen.
     *
     * @param   month  Nummer des gewuenschten Monats. Beginnt mit 0 (Januar)
     *
     * @return  den zur uebergebenen Monatsnummer gehoerenden Namen
     */
    public static String toNameOfMonth(final int month) {
        final String[] names = {
                "Januar",
                "Februar",
                "M�rz",
                "April",
                "Mai",
                "Juni",
                "Juli",
                "August",
                "September",
                "Oktober",
                "November",
                "Dezember"
            };

        return names[month];
    }

    /**
     * berechnet die aktuelle Kalenderwoche.
     *
     * @return  die aktuelle Kalenderwoche
     */
    public static int getCurrentCW() {
        final GregorianCalendar now = new GregorianCalendar();
        now.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        now.setMinimalDaysInFirstWeek(4);
        return now.get(GregorianCalendar.WEEK_OF_YEAR);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   calA  DOCUMENT ME!
     * @param   calB  DOCUMENT ME!
     *
     * @return  true, falls die beiden �bergebenen Calendar-Objekte den selben Tag enthalten. Sonst false
     */
    public static boolean isSameDate(final GregorianCalendar calA, final GregorianCalendar calB) {
        if (calA.get(GregorianCalendar.DAY_OF_YEAR) == calB.get(GregorianCalendar.DAY_OF_YEAR)) {
            if (calA.get(GregorianCalendar.YEAR) == calB.get(GregorianCalendar.YEAR)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   calA  DOCUMENT ME!
     * @param   calB  DOCUMENT ME!
     *
     * @return  true, falls calA < calB, wobei nur das Datum ber�cksichtigt wird. Nicht die Uhrzeit. Sonst false
     */
    public static boolean isDateLess(final GregorianCalendar calA, final GregorianCalendar calB) {
        if (!isSameDate(calA, calB)) {
            return calA.before(calB);
        }

        return false;
    }

    /**
     * berechnet aus dem uebergebenen String die SHA1 Pruefsumme.
     *
     * @param   passwd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NoSuchAlgorithmException  DOCUMENT ME!
     */
    public static String calcSHA1(final String passwd) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(passwd.getBytes());
        final byte[] bytes = md.digest();
        final String newPassword = toHex(bytes);

        return newPassword;
    }

    /**
     * konvertiert das uebergebene byte-Array in einen Hex-String.
     *
     * @param   bytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String toHex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        for (final byte b : bytes) {
            sb.append(chars[(b >>> 4) & 0xF]);
            sb.append(chars[b & 0xF]);
        }

        return sb.toString();
    }

    /**
     * bereitet den uebergebenen String so vor, dass er in eine Datenbankanweisung eingefuegt werden kann.
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String prepareString(final String s) {
        return (((s == null) || s.equals("")) ? "null" : ("'" + s + "'"));
    }
}
