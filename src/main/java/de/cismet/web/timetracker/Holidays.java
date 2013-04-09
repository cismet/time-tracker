/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Vector;

import de.cismet.web.timetracker.types.Holiday;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Holidays {

    //~ Static fields/initializers ---------------------------------------------

    public static final int HOLIDAY = 0;
    public static final int HALF_HOLIDAY = 1;
    public static final int WORKDAY = -1;

    //~ Instance fields --------------------------------------------------------

    private Vector holidays;
    private int year;
    private DatabaseCache db;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Holidays object.
     *
     * @param  db  DOCUMENT ME!
     */
    public Holidays(final DatabaseCache db) {
        this.year = 0;
        holidays = null;
        this.db = db;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * liefert -1, wenn der uebergebene Tag kein Feiertag ist, 0 wenn der uebergebene Tag ein Feiertag ist 1 wenn der
     * uebergebene Tag ein halber Feiertag ist.
     *
     * @param   day  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public int isHoliday(final GregorianCalendar day) throws SQLException {
        if (day.get(GregorianCalendar.YEAR) != this.year) {
            calculateHolidays(day.get(GregorianCalendar.YEAR));
        }
        final int index = Collections.binarySearch(this.holidays, new Holiday(day));
        if (index >= 0) {
            final Holiday hol = (Holiday)holidays.elementAt(index);
            if (hol.isHalfDay()) {
                return HALF_HOLIDAY;
            } else {
                return HOLIDAY;
            }
        } else {
            return WORKDAY;
        }
    }

    /**
     * berechnet alle beweglichen Feiertage des Saarlandes und speichert diese im Vector holidays. year bezeichnet das
     * Jahr, fuer die die Feiertageberechnet werden sollen.
     *
     * @param   year  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void calculateHolidays(final int year) throws SQLException {
        Holiday currentHoliday;
        final GregorianCalendar easter = getEaster(year);
        GregorianCalendar tmp;
        this.year = year;
        holidays = new Vector();

        final String holidayQuery = "SELECT name, date, is_every_year, days_after_easter, is_half_holiday "
                    + "FROM tt_holidays";

        final ResultSet rs = db.execute(holidayQuery);

        while (rs.next()) {
            currentHoliday = new Holiday(rs.getString(1));
            tmp = new GregorianCalendar();

            if (rs.getBoolean(5)) {
                currentHoliday.setIsHalfDay(true);
            }

            if (rs.getTimestamp(2) != null) {
                tmp.setTimeInMillis(rs.getTimestamp(2).getTime());

                if (rs.getBoolean(3)) { // Feiertag ist jedes Jahr
                    tmp.set(GregorianCalendar.YEAR, year);
                } else {
                    if (tmp.get(GregorianCalendar.YEAR) != year) {
                        tmp = null;     // Feiertag liegt nicht im geforderten Jahr
                    }
                }
            } else {
                // Feiertag abhaengig von Ostern
                tmp = (GregorianCalendar)easter.clone();
                tmp.add(GregorianCalendar.DATE, rs.getInt(4));
            }
            if (tmp != null) {
                currentHoliday.setDate(tmp);
                this.holidays.add(currentHoliday);
            }
        }
        Collections.sort(this.holidays);
    }

    /**
     * berechnet den Ostersonntag und liefert diesen als Rueckgabewert.
     *
     * @param   year  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private GregorianCalendar getEaster(final int year) {
        final int D;
        final GregorianCalendar easter;
        D = (((255 - (11 * (year % 19))) - 21) % 30) + 21;
        easter = new GregorianCalendar(year, 3 - 1, 1);
        final int days = D + ((D > 48) ? 1 : 0) + 6 - ((year + (year / 4) + D + ((D > 48) ? 1 : 0) + 1) % 7);
        easter.add(GregorianCalendar.DATE, days);
        return easter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  arg  DOCUMENT ME!
     */
    public static void main(final String[] arg) {
    }
}
