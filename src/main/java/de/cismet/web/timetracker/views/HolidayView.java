/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.views;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import de.cismet.web.timetracker.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class HolidayView extends AbstractView {

    //~ Instance fields --------------------------------------------------------

    SimpleDateFormat formaterDB = new SimpleDateFormat("yyyy-MM-d HH:mm");

    private Database db;
    private double dailyHoursOfWork = 8.0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HolidayView object.
     *
     * @param  db  DOCUMENT ME!
     */
    public HolidayView(final Database db) {
        this.db = db;
        if (!db.isConnectionOk()) {
            System.out.println(db.getErrorMessage());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Vector getItemsOfDay(final GregorianCalendar day, final int u_id) throws SQLException {
        final Vector items = new Vector();
        setDailyHoursOfWork(u_id);

        try {
            final GregorianCalendar firstDayOfWeek = TimeTrackerFunctions.getFirstDayOfWeek(day);
            final GregorianCalendar lastDayOfWeek = TimeTrackerFunctions.getLastDayOfWeek(day);

            final StringBuffer sqlQuery = new StringBuffer("SELECT oid, annotation, duration_in_hours, time "
                            + "FROM tt_timesheet WHERE action = " + TimeTrackerConstants.HOLIDAY_HOURS
                            + " AND u_id = " + u_id
                            + " AND date_trunc('day', time) >= date_trunc('day', timestamp '"
                            + formaterDB.format(firstDayOfWeek.getTime()) + "')"
                            + " AND date_trunc('day', time) <= date_trunc('day', timestamp '"
                            + formaterDB.format(lastDayOfWeek.getTime()) + "') ORDER BY time");
//              System.out.println(sqlQuery.toString());
            final ResultSet rSet = db.execute(sqlQuery.toString());

            if (rSet != null) {
                final CalendarItem item = new CalendarItem();
                final GregorianCalendar start = new GregorianCalendar();
                GregorianCalendar end = new GregorianCalendar();
                String id = null;
                String annotation = "";
                double duration = 0.0;
                GregorianCalendar lastHoliday = null;

                while (rSet.next()) {
//                      System.out.println("Daten: " + rSet.getString(1) + ", " + rSet.getString(2) + ", " + rSet.getString(3) + ", " + rSet.getString(4));

                    final GregorianCalendar tmp = new GregorianCalendar();
                    tmp.setTimeInMillis(rSet.getTimestamp(4).getTime());

                    if ((id == null) && (tmp.get(GregorianCalendar.DATE) > day.get(GregorianCalendar.DATE))) {
                        break;
                    }

                    if (id != null) {
                        final double tmpDuration = rSet.getDouble(3);
                        tmp.add(GregorianCalendar.DATE, -1);

                        if ((tmpDuration == 0.0) || (tmpDuration == dailyHoursOfWork)) {
                            duration = dailyHoursOfWork;
                            start.set(GregorianCalendar.HOUR_OF_DAY, 9);
                            start.set(GregorianCalendar.MINUTE, 0);
                        } else if (start.get(GregorianCalendar.HOUR) == 0) {
                            start.set(GregorianCalendar.HOUR_OF_DAY, 9);
                            start.set(GregorianCalendar.MINUTE, 0);
                        }

                        if ((lastHoliday != null) && (tmpDuration == duration)
                                    && (tmp.get(GregorianCalendar.DATE) == lastHoliday.get(GregorianCalendar.DATE))) {
                            // Zusammenhaengender Urlaub
                            end.setTimeInMillis(rSet.getTimestamp(4).getTime());
                            end.set(GregorianCalendar.HOUR_OF_DAY, start.get(GregorianCalendar.HOUR_OF_DAY));
                            end.set(GregorianCalendar.MINUTE, start.get(GregorianCalendar.MINUTE));

                            end.add(GregorianCalendar.MINUTE, (int)(tmpDuration * 60));
                            lastHoliday = (GregorianCalendar)end.clone();
                        } else {
                            // nicht zusammenh�ngend
                            if (start.get(GregorianCalendar.DATE) == day.get(GregorianCalendar.DATE)) {
                                // Urlaubsdaten �bertragen
                                break;
                            }
                            id = null;
                        }
                    }

                    if (id == null) {
                        // Urlaubsbeginn
                        id = rSet.getString(1);
                        annotation = ((rSet.getString(2) == null) ? "" : (rSet.getString(2) + "<br />"));
                        duration = rSet.getDouble(3);
                        start.setTimeInMillis(rSet.getTimestamp(4).getTime());
//                                              System.out.println(formaterDB.format(rSet.getTimestamp(4)));
                        if ((duration == 0.0) || (duration == dailyHoursOfWork)) {
                            duration = dailyHoursOfWork;
                            start.set(GregorianCalendar.HOUR_OF_DAY, 9);
                        }
                        end = (GregorianCalendar)start.clone();
                        end.add(GregorianCalendar.MINUTE, (int)(duration * 60));
                        lastHoliday = (GregorianCalendar)end.clone();
                    }
                }

                if ((id != null) && (start.get(GregorianCalendar.DATE) == day.get(GregorianCalendar.DATE))) {
                    item.setId(id);
                    item.setAction(TimeTrackerConstants.HOLIDAY_STRING);
                    item.setAnnotation(annotation);
                    item.setStart(start);
                    item.setEnd(end);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return items;
        }
        return items;
    }

    @Override
    public Vector getActions() {
        final Vector actions = new Vector();
        actions.add(TimeTrackerConstants.HOLIDAY_STRING);
        return actions;
    }

    @Override
    public String saveNewItem(final CalendarItem item, final int u_id) {
        String id = "-1";
        try {
            if (item.getAction().equals(TimeTrackerConstants.HOLIDAY_STRING)) {
                final GregorianCalendar tmpTime = (GregorianCalendar)item.getStart().clone();

                // solange durchlaufen, bis f�r jeden Tag, von item.getStart() bis item.getEnd(), ein
                // Urlaubs-Eintrag eingef�gt wurde
                while (tmpTime.get(GregorianCalendar.DATE) != (item.getEnd().get(GregorianCalendar.DATE) + 1)) {
                    final GregorianCalendar tmp = (GregorianCalendar)item.getEnd().clone();
                    tmp.set(GregorianCalendar.HOUR_OF_DAY, item.getStart().get(GregorianCalendar.HOUR_OF_DAY));
                    tmp.set(GregorianCalendar.MINUTE, item.getStart().get(GregorianCalendar.MINUTE));

                    final double duration = roundToIntervals(TimeTrackerFunctions.calcDifferenceInHours(
                                item.getEnd(),
                                tmp),
                            u_id);
                    String annotation = item.getAnnotation();
                    if ((annotation == null) || annotation.equals("")) {
                        annotation = "null";
                    } else {
                        annotation = "'" + annotation + "'";
                    }

                    final GregorianCalendar tmpStart = new GregorianCalendar(tmpTime.get(GregorianCalendar.YEAR),
                            tmpTime.get(GregorianCalendar.MONTH),
                            tmpTime.get(GregorianCalendar.DAY_OF_MONTH));
                    tmpStart.set(GregorianCalendar.HOUR_OF_DAY, item.getStart().get(GregorianCalendar.HOUR_OF_DAY));
                    tmpStart.set(GregorianCalendar.MINUTE, item.getStart().get(GregorianCalendar.MINUTE));

                    final String sqlString =
                        "INSERT INTO tt_timesheet (time, action, annotation, manual, u_id, project_id, duration_in_hours) "
                                + "VALUES ('"
                                + formaterDB.format(tmpStart.getTime())
                                + "', "
                                + TimeTrackerConstants.HOLIDAY_HOURS
                                + ", "
                                + (item.getAnnotation().equals("") ? "null" : ("'" + item.getAnnotation() + "'"))
                                + ", true, "
                                + u_id
                                + ", null, "
                                + duration
                                + ")";
//                      System.out.println(sqlString);
                    db.executeUpdate(sqlString);
                    tmpTime.add(GregorianCalendar.DATE, 1);
                }
                final CalendarItem newItem;
                final Vector items = getItemsOfDay(item.getStart(), u_id);
                final Iterator it = items.iterator();
                if (it.hasNext()) {
                    newItem = (CalendarItem)items.iterator().next();
                    id = newItem.getId();
                }
            } else {
                // Diese Klasse behandelt nur Urlaubstage
                return "-1";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
        return id;
    }

    @Override
    public boolean saveModifiedItem(final CalendarItem item, final int u_id) {
        try {
            String sqlString = "SELECT time from tt_timesheet WHERE oid ="
                        + item.getId();
            final ResultSet rSet = db.execute(sqlString);
            if ((rSet != null) && rSet.next()) {
                // speichert, ob die �nderung der Endzeit schon durch eine �nderung der Startzeit behoben wurde
                boolean endTimeOK = false;
                final CalendarItem original;
                final GregorianCalendar day = new GregorianCalendar();
                day.setTimeInMillis(rSet.getTimestamp(1).getTime());
                final Vector items = getItemsOfDay(day, u_id);
                original = (CalendarItem)items.iterator().next();

                if (item.getStart().compareTo(original.getStart()) != 0) {
                    // Startzeit hat sich ge�ndert
// System.out.println("andere Startzeit");
                    if (item.getStart().get(GregorianCalendar.DATE)
                                != original.getStart().get(GregorianCalendar.DATE)) {
                        // Startzeit hat sich um einen oder mehrere Tage verschoben
                        endTimeOK = true;
                        boolean rightShift;
                        GregorianCalendar tmpTime;
                        GregorianCalendar targetDate;
                        if (item.getStart().compareTo(original.getStart()) > 0) {
                            // Start findet sp�ter statt (Alle Eintr�ge werden nach rechts verschoben)
                            rightShift = true;
                            targetDate = (GregorianCalendar)original.getStart().clone();
                            targetDate.add(GregorianCalendar.DATE, -1);
                            tmpTime = (GregorianCalendar)original.getEnd().clone();
                            tmpTime.set(
                                GregorianCalendar.HOUR_OF_DAY,
                                item.getStart().get(GregorianCalendar.HOUR_OF_DAY));
                            tmpTime.set(GregorianCalendar.MINUTE, item.getStart().get(GregorianCalendar.MINUTE));
                        } else {
                            // Start findet fr�her statt (Alle Eintr�ge werden nach links verschoben)
                            rightShift = false;
                            targetDate = (GregorianCalendar)original.getEnd().clone();
                            targetDate.add(GregorianCalendar.DATE, 1);
                            tmpTime = (GregorianCalendar)original.getStart().clone();
                        }

                        int a = 0; // dient nur als Sicherung, um keine Endlos-Schleife auszul�sen

                        while (tmpTime.get(GregorianCalendar.DATE) != targetDate.get(GregorianCalendar.DATE)) {
                            final GregorianCalendar newTime = (GregorianCalendar)tmpTime.clone();
                            newTime.add(
                                GregorianCalendar.DATE,
                                TimeTrackerFunctions.getDifferenceInDays(original.getStart(), item.getStart()));
                            sqlString = "UPDATE tt_timesheet SET time = '"
                                        + formaterDB.format(newTime.getTime())
                                        + "' "
                                        + ", manual = true "
                                        + " WHERE date_trunc('day' ,time) = date_trunc('day', timestamp '"
                                        + formaterDB.format(tmpTime.getTime())
                                        + "')"
                                        + " AND action = "
                                        + TimeTrackerConstants.HOLIDAY_HOURS
                                        + " AND u_id = "
                                        + u_id;
                            tmpTime.add(GregorianCalendar.DATE, (rightShift ? -1 : 1));

//                                                      System.out.println(sqlString);
                            a++;
                            if (a > 10) {
                                break;
                            }
                            db.executeUpdate(sqlString);
                        }
                    } else {
                        // Startzeit hat sich ge�ndert. Allerdings hat sich der Starttag nicht ge�ndert
                        final GregorianCalendar tmpTime = (GregorianCalendar)item.getStart().clone();
//                                              System.out.println("andere Startzeit aber selber Tag");
                        while (tmpTime.get(GregorianCalendar.DATE) != (item.getEnd().get(GregorianCalendar.DATE) + 1)) {
                            sqlString = "UPDATE tt_timesheet SET time = '"
                                        + formaterDB.format(tmpTime.getTime())
                                        + "'"
                                        + ", manual = true "
                                        + " WHERE date_trunc('day' ,time) = date_trunc('day', timestamp '"
                                        + formaterDB.format(tmpTime.getTime())
                                        + "')"
                                        + " AND action = "
                                        + TimeTrackerConstants.HOLIDAY_HOURS
                                        + " AND u_id = "
                                        + u_id;
//                                                      System.out.println(sqlString);
                            db.executeUpdate(sqlString);
                            tmpTime.add(GregorianCalendar.DATE, 1);
                        }
                    }
                } else if (!item.getAnnotation().equals(original.getAnnotation())) {
                    // Kommentar hat sich ge�ndert
                    final String annotation = item.getAnnotation().equals("") ? "null"
                                                                              : ("'" + item.getAnnotation() + "'");
                    final GregorianCalendar tmpTime = (GregorianCalendar)item.getStart().clone();
                    while (tmpTime.get(GregorianCalendar.DATE) != (item.getEnd().get(GregorianCalendar.DATE) + 1)) {
                        sqlString = "UPDATE tt_timesheet SET annotation = "
                                    + annotation
                                    + ", manual = true "
                                    + " WHERE date_trunc('day' ,time) = date_trunc('day', timestamp '"
                                    + formaterDB.format(tmpTime.getTime())
                                    + "')"
                                    + " AND action = "
                                    + TimeTrackerConstants.HOLIDAY_HOURS
                                    + " AND u_id = "
                                    + u_id;
//                                              System.out.println(sqlString);
                        db.executeUpdate(sqlString);
                        tmpTime.add(GregorianCalendar.DATE, 1);
                    }
                }

                if (!item.getEnd().equals(original.getEnd()) && !endTimeOK) {
                    // Endzeit hat sich ge�ndert (Tag ist gleich)
                    if (original.getEnd().get(GregorianCalendar.DATE) == item.getEnd().get(GregorianCalendar.DATE)) {
                        final GregorianCalendar tmpTime = (GregorianCalendar)item.getStart().clone();
                        final GregorianCalendar tmp = (GregorianCalendar)item.getStart().clone();
                        tmp.set(GregorianCalendar.HOUR_OF_DAY, item.getEnd().get(GregorianCalendar.HOUR_OF_DAY));
                        tmp.set(GregorianCalendar.MINUTE, item.getEnd().get(GregorianCalendar.MINUTE));

                        final double duration = roundToIntervals(TimeTrackerFunctions.calcDifferenceInHours(
                                    item.getStart(),
                                    tmp),
                                u_id);

                        while (tmpTime.get(GregorianCalendar.DATE) != (item.getEnd().get(GregorianCalendar.DATE) + 1)) {
                            sqlString = "UPDATE tt_timesheet SET duration_in_hours = "
                                        + duration
                                        + ", manual = true "
                                        + " WHERE date_trunc('day' ,time) = date_trunc('day', timestamp '"
                                        + formaterDB.format(tmpTime.getTime())
                                        + "')"
                                        + " AND action = "
                                        + TimeTrackerConstants.HOLIDAY_HOURS
                                        + " AND u_id = "
                                        + u_id;
//                                                      System.out.println(sqlString);
                            db.executeUpdate(sqlString);
                            tmpTime.add(GregorianCalendar.DATE, 1);
                        }
                    } else {
                        // Endtag hat sich ge�ndert
                        if (item.getEnd().compareTo(original.getEnd()) > 0) {
                            // Zus�tzliche Tage hinzuf�gen
// System.out.println("Tage hinzuf�gen");
                            final GregorianCalendar tmpStart = (GregorianCalendar)original.getEnd().clone();
                            tmpStart.add(GregorianCalendar.DATE, 1);
                            tmpStart.set(
                                GregorianCalendar.HOUR_OF_DAY,
                                item.getStart().get(GregorianCalendar.HOUR_OF_DAY));
                            tmpStart.set(GregorianCalendar.MINUTE, item.getStart().get(GregorianCalendar.MINUTE));
                            final CalendarItem newItem = new CalendarItem(item.getAction(),
                                    item.getAnnotation(),
                                    item.getEnd(),
                                    "",
                                    item.getProject(),
                                    tmpStart);
                            saveNewItem(newItem, u_id);
                        } else {
                            // Tage l�schen
// System.out.println("Tage l�schen");
                            final GregorianCalendar tmpTime = (GregorianCalendar)item.getEnd().clone();
                            tmpTime.add(GregorianCalendar.DATE, 1);
                            while (tmpTime.get(GregorianCalendar.DATE)
                                        != (original.getEnd().get(GregorianCalendar.DATE) + 1)) {
                                sqlString = "DELETE FROM tt_timesheet "
                                            + " WHERE date_trunc('day' ,time) = date_trunc('day', timestamp '"
                                            + formaterDB.format(tmpTime.getTime())
                                            + "')"
                                            + " AND action = "
                                            + TimeTrackerConstants.HOLIDAY_HOURS
                                            + " AND u_id = "
                                            + u_id;
//                                                              System.out.println(sqlString);
                                db.executeUpdate(sqlString);
                                tmpTime.add(GregorianCalendar.DATE, 1);
                            }
                        }
                    }
                }
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Element kann nicht modifiziert werden, da es in der DB nicht existiert.");
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteItem(final String id) {
        try {
            String sqlString = "SELECT action, time, u_id FROM tt_timesheet WHERE oid = "
                        + id;
            final ResultSet rSet = db.execute(sqlString);
            if (rSet.next()) {
                if (rSet.getInt(1) == TimeTrackerConstants.HOLIDAY_HOURS) {
                    final CalendarItem ci;
                    final GregorianCalendar tmpTime = new GregorianCalendar();
                    tmpTime.setTimeInMillis(rSet.getTimestamp(2).getTime());
                    final Vector items = getItemsOfDay(tmpTime, rSet.getInt(3));
                    ci = (CalendarItem)items.iterator().next();

                    while (tmpTime.get(GregorianCalendar.DATE) != (ci.getEnd().get(GregorianCalendar.DATE) + 1)) {
                        sqlString = "DELETE FROM tt_timesheet WHERE u_id = "
                                    + rSet.getString(3)
                                    + " AND date_trunc('day', time) = date_trunc('day', timestamp '"
                                    + formaterDB.format(tmpTime.getTime())
                                    + "') AND "
                                    + "action ="
                                    + TimeTrackerConstants.HOLIDAY_HOURS;
//                                              System.out.println(sqlString);
                        db.executeUpdate(sqlString);
                        tmpTime.add(GregorianCalendar.DATE, 1);
                    }
                } else {
                    // Diese Klasse behandelt nur Urlaubstage
                    return false;
                }
            } else {
                System.out.println("null ret: false");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Vector getTimeIntervals(final int u_id) {
        final Vector intervals = new Vector();
        setDailyHoursOfWork(u_id);
        final int minutesPerDay = (int)(dailyHoursOfWork * 60);
        intervals.add("" + (minutesPerDay / 2));
        intervals.add("" + minutesPerDay);
        return intervals;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        /*        try{
         * GregorianCalendar gc = new GregorianCalendar(2006,7,10); GregorianCalendar gc2 = new
         * GregorianCalendar(2006,7,13);
         *
         * HolidayView hv = new HolidayView(); Vector items = hv.getItemsOfDay(gc, 3); Iterator it = items.iterator();
         * while(it.hasNext()){ CalendarItem ci = (CalendarItem)it.next(); System.out.println(ci.toString()); }
         *
         * System.out.println(TimeTrackerFunctions.getDifferenceInDays(gc, gc2)); }catch(SQLException e){
         * e.printStackTrace(); }
         */
    }

    /**
     * DOCUMENT ME!
     *
     * @param  u_id  DOCUMENT ME!
     */
    private void setDailyHoursOfWork(final int u_id) {
        final Query query = new Query(db);
        dailyHoursOfWork = query.getTargetHoursPerWeek(u_id, new GregorianCalendar(), null, false)
                    / TimeTrackerConstants.WORK_DAYS_PER_WEEK;
    }

    /**
     * rundet zum n�chsten, laut getTimeIntervals, g�ltigen Wert.
     *
     * @param   value  Wert, der gerundet werden soll
     * @param   u_id   user id
     *
     * @return  gerundeter Wert
     */
    private double roundToIntervals(final double value, final int u_id) {
        final Vector intervals = getTimeIntervals(u_id);
        final Iterator it = intervals.iterator();

        final int halfDay = Integer.parseInt((String)it.next());
        final int fullDay = Integer.parseInt((String)it.next());
        final double halfDayInHours = halfDay
                    / 60.0;
        final double fullDayInHours = fullDay
                    / 60.0;

        final double absToHalfDay = Math.abs(halfDayInHours - value);
        final double absToFullDay = Math.abs(fullDayInHours - value);
        if (absToFullDay < absToHalfDay) {
            return (fullDay / 60.0);
        } else {
            return (halfDay / 60.0);
        }
    }
}
